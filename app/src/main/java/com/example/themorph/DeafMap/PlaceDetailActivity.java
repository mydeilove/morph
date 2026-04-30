package com.example.themorph.DeafMap;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.services.core.PoiItem;
import com.example.themorph.R;

public class PlaceDetailActivity extends AppCompatActivity {

    private TextView tvName, tvAddress;
    private Button btnNavi, btnCollect;
    private MapView mapView;
    private AMap aMap;
    private PoiItem mPoiItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_detail);

        tvName = findViewById(R.id.tv_name);
        tvAddress = findViewById(R.id.tv_address);
        btnNavi = findViewById(R.id.btn_navi);
        btnCollect = findViewById(R.id.btn_collect);
        mapView = findViewById(R.id.map_view);

        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();

        mPoiItem = getIntent().getParcelableExtra("poi");
        if (mPoiItem == null) {
            finish();
            return;
        }

        tvName.setText(mPoiItem.getTitle());
        tvAddress.setText(mPoiItem.getSnippet());

        LatLng latLng = new LatLng(mPoiItem.getLatLonPoint().getLatitude(), mPoiItem.getLatLonPoint().getLongitude());
        aMap.addMarker(new MarkerOptions().position(latLng).title(mPoiItem.getTitle()));
        aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));

        //  直接写导航逻辑，不依赖getParentFragment
        btnNavi.setOnClickListener(v -> openGaodeNavi());

        // 收藏逻辑不变
        boolean collected = CollectManager.isCollectedPoi(this, mPoiItem);
        btnCollect.setText(collected ? "取消收藏" : "收藏");
        btnCollect.setOnClickListener(v -> {
            boolean isCollected = CollectManager.toggleCollectPoi(this, mPoiItem);
            btnCollect.setText(isCollected ? "取消收藏" : "收藏");
            Toast.makeText(this, isCollected ? "收藏成功" : "已取消收藏", Toast.LENGTH_SHORT).show();
        });
    }

    // 直接实现高德导航（起点：当前定位，终点：POI）
    private void openGaodeNavi() {
        if (mPoiItem == null) return;

        // 终点经纬度、名称
        double dLat = mPoiItem.getLatLonPoint().getLatitude();
        double dLng = mPoiItem.getLatLonPoint().getLongitude();
        String dName = mPoiItem.getTitle();

        // 高德URI导航（起点自动取当前定位，无需传起点坐标）
        String uri = "amapuri://route/plan/?sourceApplication=TheMorph"
                + "&sname=我的位置"  // 起点自动定位
                + "&dname=" + Uri.encode(dName)
                + "&dlat=" + dLat
                + "&dlng=" + dLng
                + "&dev=0&t=0"; // t=0驾车，t=1公交，t=2步行

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "未安装高德地图App", Toast.LENGTH_SHORT).show();
        }
    }

    @Override protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override protected void onDestroy() { super.onDestroy(); mapView.onDestroy(); }
}