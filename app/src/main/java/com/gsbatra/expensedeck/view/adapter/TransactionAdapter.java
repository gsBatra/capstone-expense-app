package com.gsbatra.expensedeck.view.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gsbatra.expensedeck.EditTransactionActivity;
import com.gsbatra.expensedeck.R;
import com.gsbatra.expensedeck.db.Transaction;

import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> implements Filterable {

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView transactionName;
        private final TextView transactionTag;
        private final TextView transactionAmount;
        private final TextView transactionDate;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            transactionName = itemView.findViewById(R.id.transactionName);
            transactionTag = itemView.findViewById(R.id.transactionTag);
            transactionAmount = itemView.findViewById(R.id.transactionAmount);
            transactionDate = itemView.findViewById(R.id.transactionDate);
        }
    }

    private List<Transaction> transactions;
    private List<Transaction> transactionsAll;

    @Override
    public Filter getFilter() {
        return filter;
    }

    Filter filter = new Filter() {
        // background thread
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {

            FilterResults filterResults = new FilterResults();
            if(charSequence != null && charSequence.toString().length() > 0) {
                List<Transaction> filteredTransactions = new ArrayList<>();
                for(Transaction transaction : transactionsAll) {
                    if(transaction.title.toLowerCase().contains(charSequence.toString().toLowerCase())) {
                        filteredTransactions.add(transaction);
                    }
                }

                filterResults.count = filteredTransactions.size();
                filterResults.values = filteredTransactions;
            } else {
                synchronized (this) {
                    filterResults.values = transactionsAll;
                    filterResults.count = transactionsAll.size();
                }
            }

            return filterResults;
        }

        // ui thread
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            transactions = (List<Transaction>) filterResults.values;
            notifyDataSetChanged();
        }
    };

    public TransactionAdapter(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
    }

    @NotNull
    @Override
    public TransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_card_view, parent, false);
        return new TransactionViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        holder.transactionName.setText(transactions.get(position).title);
        holder.transactionTag.setText(transactions.get(position).tag);
        holder.transactionDate.setText(transactions.get(position).when);

        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
        format.setCurrency(Currency.getInstance("USD"));
        String amount_str = format.format(transactions.get(position).amount);

        holder.transactionAmount.setText(amount_str);
        if(transactions.get(position).type.equals("Income"))
            holder.transactionAmount.setTextColor(Color.parseColor("#6FCF97"));
        else
            holder.transactionAmount.setTextColor(Color.parseColor("#EB5757"));

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), EditTransactionActivity.class);
            intent.putExtra("id", transactions.get(position).id);
            view.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return transactions != null ? transactions.size() : 0;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
        this.transactionsAll = transactions;
        getAmounts();
        notifyDataSetChanged();
    }

    public void getAmounts(){
        if(transactions == null) {
            onResult(0, 0, 0, 0);
            return;
        }

        double balance;
        double income = 0;
        double expense = 0;
        for(Transaction transaction : transactions){
            if(transaction.type.equals("Income"))
                income += transaction.amount;
            if(transaction.type.equals("Expense"))
                expense += transaction.amount;
        }

        balance = income + expense * -1;
        onResult(balance, income, expense, getItemCount());
    }

    private void onResult(double balance, double income, double expense, int size) {
        if(onAmountsDataReceivedListener != null){
            onAmountsDataReceivedListener.onAmountsDataReceived(balance, income, expense, size);
        }
    }

    private OnAmountsDataReceivedListener onAmountsDataReceivedListener;

    public interface OnAmountsDataReceivedListener {
        void onAmountsDataReceived(double balance, double income, double expense, int size);
    }

    public void setOnAmountsDataReceivedListener(OnAmountsDataReceivedListener listener){
        this.onAmountsDataReceivedListener = listener;
    }
}
