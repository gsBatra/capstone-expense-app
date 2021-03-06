package com.gsbatra.expensedeck.view.adapter;

import android.annotation.SuppressLint;
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

import com.gsbatra.expensedeck.EditSubscriptionActivity;
import com.gsbatra.expensedeck.EditTransactionActivity;
import com.gsbatra.expensedeck.R;
import com.gsbatra.expensedeck.db.Subscription;
import com.gsbatra.expensedeck.db.Transaction;

import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class SubscriptionAdapter extends RecyclerView.Adapter<SubscriptionAdapter.SubscriptionViewHolder> implements Filterable {

    public static class SubscriptionViewHolder extends RecyclerView.ViewHolder {
        private final TextView subscriptionName;
        private final TextView subscriptionTag;
        private final TextView subscriptionAmount;

        public SubscriptionViewHolder(@NonNull View itemView) {
            super(itemView);
            subscriptionName = itemView.findViewById(R.id.subscriptionName);
            subscriptionTag = itemView.findViewById(R.id.subscriptionTag);
            subscriptionAmount = itemView.findViewById(R.id.subscriptionAmount);
        }
    }

    private List<Subscription> subscriptions;
    private List<Subscription> subscriptionsAll;

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
                List<Subscription> filteredSubscriptions = new ArrayList<>();
                for(Subscription subscription : subscriptionsAll) {
                    if(subscription.title.toLowerCase().contains(charSequence.toString().toLowerCase())) {
                        filteredSubscriptions.add(subscription);
                    }
                }

                filterResults.count = filteredSubscriptions.size();
                filterResults.values = filteredSubscriptions;
            } else {
                synchronized (this) {
                    filterResults.values = subscriptionsAll;
                    filterResults.count = subscriptionsAll.size();
                }
            }

            return filterResults;
        }

        // ui thread
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            subscriptions = (List<Subscription>) filterResults.values;
            notifyDataSetChanged();
        }
    };

    public SubscriptionAdapter(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
    }

    @NotNull
    @Override
    public SubscriptionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.subscription_card_view, parent, false);
        return new SubscriptionViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull SubscriptionViewHolder holder, int position) {
        holder.subscriptionName.setText(subscriptions.get(position).title);
        holder.subscriptionTag.setText(subscriptions.get(position).tag);

        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
        format.setCurrency(Currency.getInstance("USD"));
        String amount_str = format.format(subscriptions.get(position).amount);

        holder.subscriptionAmount.setText(amount_str);
        holder.subscriptionAmount.setTextColor(Color.parseColor("#EB5757"));

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), EditSubscriptionActivity.class);
            intent.putExtra("id", subscriptions.get(position).id);
            view.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return subscriptions != null ? subscriptions.size() : 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
        this.subscriptionsAll = subscriptions;
        getAmounts();
        notifyDataSetChanged();
    }

    public void getAmounts(){
        if(subscriptions == null) {
            onResult(0, 0);
            return;
        }

        double recurring = 0;
        for(Subscription subscription : subscriptions){
            recurring += subscription.amount;
        }

        onResult(recurring, getItemCount());
    }

    private void onResult(double recurring, int size) {
        if(onAmountsDataReceivedListener != null){
            onAmountsDataReceivedListener.onAmountsDataReceived(recurring, size);
        }
    }

    private OnAmountsDataReceivedListener onAmountsDataReceivedListener;

    public interface OnAmountsDataReceivedListener {
        void onAmountsDataReceived(double recurring, int size);
    }

    public void setOnAmountsDataReceivedListener(OnAmountsDataReceivedListener listener){
        this.onAmountsDataReceivedListener = listener;
    }
}
