package com.example.themorph.DeafMap;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.amap.api.services.core.PoiItem;
import java.util.ArrayList;
import java.util.List;

public class SuggestAdapter extends RecyclerView.Adapter<SuggestAdapter.Holder> {
    private List<PoiItem> poiList = new ArrayList<>();
    private final OnPoiClickListener listener;

    public SuggestAdapter(List<PoiItem> list, OnPoiClickListener listener) {
        this.poiList = list;
        this.listener = listener;
    }

    public void setData(List<PoiItem> data) {
        poiList.clear();
        poiList.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        PoiItem item = poiList.get(position);
        holder.tvTitle.setText(item.getTitle() + " - " + item.getSnippet());
        holder.itemView.setOnClickListener(v -> listener.onPoiClick(item));
    }

    @Override public int getItemCount() { return poiList.size(); }

    public static class Holder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        public Holder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(android.R.id.text1);
        }
    }

    public interface OnPoiClickListener { void onPoiClick(PoiItem item); }
}