package com.example.themorph;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.themorph.DeafMap.CollectPoiListActivity;

public class HomeDeafFragment extends Fragment {

    private Button btn_collect;

    public HomeDeafFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home_deaf, container, false);

        // 收藏按钮
        btn_collect = view.findViewById(R.id.btn_collect);

        // 跳收藏列表
        btn_collect.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), CollectPoiListActivity.class);
            startActivity(intent);
        });

        return view;
    }
}