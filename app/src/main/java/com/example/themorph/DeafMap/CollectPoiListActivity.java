package com.example.themorph.DeafMap;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.themorph.R;

import java.util.List;

public class CollectPoiListActivity extends AppCompatActivity {

    private Button btn_back;
    private RecyclerView recycler;
    private CollectPoiAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collect_poi_list);

        btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(v -> finish());

        recycler = findViewById(R.id.recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CollectPoiAdapter();
        recycler.setAdapter(adapter);

        loadCollectData();
    }

    // 加载真实名称+地址
    private void loadCollectData() {
        List<CollectPoiBean> dataList = CollectManager.getCollectPoiList(this);

        if (dataList.isEmpty()) {
            Toast.makeText(this, "暂无收藏的场所", Toast.LENGTH_SHORT).show();
            return;
        }

        // 直接传给适配器
        adapter.setList(dataList);
    }
}