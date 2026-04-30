package com.example.themorph.DeafMap;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.themorph.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class CollectPoiAdapter extends RecyclerView.Adapter<CollectPoiAdapter.VH> {

    // 泛型改为 CollectPoiBean，不再用 PoiItem
    private List<CollectPoiBean> list = new ArrayList<>();

    // 接收 CollectPoiBean 列表
    public void setList(List<CollectPoiBean> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        CollectPoiBean bean = list.get(position);
        holder.title.setText(bean.getTitle());
        holder.addr.setText(bean.getAddress());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), MainActivity.class);
            intent.putExtra("from_collect", true);
            intent.putExtra("poi_id", bean.getPoiId());
            intent.putExtra("poi_title", bean.getTitle());
            intent.putExtra("poi_address", bean.getAddress());
            // 把用户类型一起带回去（修复用户类型异常）
            intent.putExtra("user_type", "1");
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class VH extends RecyclerView.ViewHolder {
        TextView title, addr;

        public VH(View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
            addr = itemView.findViewById(android.R.id.text2);
        }
    }
}