package com.example.financialmanagement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.financialmanagement.R;
import com.example.financialmanagement.model.PersonIncome;

import java.util.ArrayList;
import java.util.List;

public class RankingAdapter extends RecyclerView.Adapter<RankingAdapter.ViewHolder> {

    private List<PersonIncome> items = new ArrayList<>();

    public void setItems(List<PersonIncome> items) {
        this.items = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ranking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PersonIncome item = items.get(position);
        int rank = position + 1;
        holder.tvRank.setText(String.valueOf(rank));
        holder.tvName.setText(item.getName());
        holder.tvAmount.setText(String.format("+%.0f", item.getTotalIncome()));
        holder.tvCount.setText(item.getCount() + " 笔");

        int colorRes;
        if (rank == 1) colorRes = R.color.rank_gold;
        else if (rank == 2) colorRes = R.color.rank_silver;
        else if (rank == 3) colorRes = R.color.rank_bronze;
        else colorRes = R.color.on_surface;
        holder.tvRank.setTextColor(holder.itemView.getContext().getColor(colorRes));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvAmount, tvCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tv_rank);
            tvName = itemView.findViewById(R.id.tv_name);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvCount = itemView.findViewById(R.id.tv_count);
        }
    }
}
