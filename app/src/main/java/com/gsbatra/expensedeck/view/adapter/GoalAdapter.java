package com.gsbatra.expensedeck.view.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.gsbatra.expensedeck.EditGoalActivity;
import com.gsbatra.expensedeck.R;
import com.gsbatra.expensedeck.db.Goal;

import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {

    public static class GoalViewHolder extends RecyclerView.ViewHolder {
        private final TextView goalName;
        private final TextView goalTag;
        private final TextView goalAmount;
        private final TextView goalAmountTotal;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            goalName = itemView.findViewById(R.id.goalName);
            goalTag = itemView.findViewById(R.id.goalTag);
            goalAmount = itemView.findViewById(R.id.goalAmount);
            goalAmountTotal = itemView.findViewById(R.id.goal);
        }
    }

    private List<Goal> goals;

    public GoalAdapter(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
    }

    @NotNull
    @Override
    public GoalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.goal_card_view, parent, false);
        return new GoalViewHolder(rowItem);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        holder.goalName.setText(goals.get(position).title);
        holder.goalTag.setText(goals.get(position).tag);

        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.getDefault());
        format.setCurrency(Currency.getInstance("USD"));
        String amount_str = format.format(goals.get(position).amount);

        holder.goalAmount.setText(amount_str);
        holder.goalAmount.setTextColor(Color.parseColor("#e7e6e1"));

        // get goal amount total
        holder.goalAmountTotal.setText(amount_str);
        holder.goalAmountTotal.setTextColor(Color.parseColor("#6FCF97"));

        holder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(view.getContext(), EditGoalActivity.class);
            intent.putExtra("id", goals.get(position).id);
            view.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return goals != null ? goals.size() : 0;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setGoals(List<Goal> goals) {
        this.goals = goals;
        getAmounts();
        notifyDataSetChanged();
    }

    public void getAmounts(){
        if(goals == null) {
            onResult(0, 0);
            return;
        }

        double recurring = 0;
        for(Goal goal : goals){
            recurring += goal.amount;
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
