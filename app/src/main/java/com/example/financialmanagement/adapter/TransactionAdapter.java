package com.example.financialmanagement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financialmanagement.R;
import com.example.financialmanagement.model.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private List<Transaction> transactions = new ArrayList<>();
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditClick(Transaction transaction);
        void onDeleteClick(Transaction transaction);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions != null ? transactions : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        boolean isIncome = transaction.isIncome();
        holder.tvType.setText(isIncome ? R.string.type_income : R.string.type_expense);
        holder.tvType.setBackgroundResource(isIncome ? R.drawable.bg_type_income : R.drawable.bg_type_expense);

        String amountStr = String.format(Locale.getDefault(), "%.2f", transaction.getAmount());
        holder.tvAmount.setText(amountStr);
        holder.tvAmount.setTextColor(holder.itemView.getContext().getColor(
                isIncome ? R.color.income_green : R.color.expense_red));

        String personEvent = transaction.getPerson() + " - " + transaction.getEvent();
        holder.tvPersonEvent.setText(personEvent);

        String datetime = transaction.getDate() + " " + transaction.getTime();
        holder.tvDateTime.setText(datetime);

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(transaction);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDeleteClick(transaction);
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvType, tvAmount, tvPersonEvent, tvDateTime;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvType = itemView.findViewById(R.id.tv_type);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvPersonEvent = itemView.findViewById(R.id.tv_person_event);
            tvDateTime = itemView.findViewById(R.id.tv_datetime);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
