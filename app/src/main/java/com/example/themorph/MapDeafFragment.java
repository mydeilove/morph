package com.example.themorph;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.route.BusPath;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.BusStep;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.DriveStep;
import com.amap.api.services.route.RideRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.WalkPath;
import com.amap.api.services.route.WalkRouteResult;
import com.amap.api.services.route.WalkStep;
import com.amap.api.maps.MapsInitializer;
import com.example.themorph.DeafMap.CollectManager;


import java.util.ArrayList;
import java.util.List;

public class MapDeafFragment extends Fragment implements RouteSearch.OnRouteSearchListener {
    private MapView mMapView;
    private AMap aMap;
    private EditText etSearch;
    private RecyclerView rvSuggest;
    private LinearLayout llNavBar, llRouteDetail;
    private Button btnBus, btnCar, btnWalk, btnStartNav, btnBack,btnCollect;
    private TextView tvCurrentLocation, tvRouteSummary, tvCurrentStep;
    private TextView tv_poi_name, tv_poi_address, tv_poi_tel, tv_poi_type, tv_poi_distance;

    private AMapLocationClient mLocationClient;
    private LatLng mMyLatLng;
    private String mCityName;
    private String mSearchCity = "";

    private PoiItem mTargetPoi;
    private RouteSearch routeSearch;
    private int currentNavMode = -1;

    private WalkPath mWalkPath;
    private DrivePath mDrivePath;
    private BusPath mBusPath;
    private int currentStepIndex = 0;
    private boolean isNavigating = false;

    private static final int REQUEST_LOCATION = 100;

    // 顶部添加这个变量，暂存收藏跳过来的地点
    private PoiItem mPendingPoi;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map_deaf, container, false);

        // 只保存，不执行
        if (getArguments() != null) {
            mPendingPoi = getArguments().getParcelable("poi");
        }

        mMapView = view.findViewById(R.id.map_view);
        etSearch = view.findViewById(R.id.et_search);
        rvSuggest = view.findViewById(R.id.rv_suggest);
        llNavBar = view.findViewById(R.id.ll_nav_bar);
        llRouteDetail = view.findViewById(R.id.ll_route_detail);
        btnBus = view.findViewById(R.id.btn_bus);
        btnCar = view.findViewById(R.id.btn_car);
        btnWalk = view.findViewById(R.id.btn_walk);
        btnStartNav = view.findViewById(R.id.btn_start_nav);
        btnBack = view.findViewById(R.id.btn_back);
        btnCollect = view.findViewById(R.id.btn_collect);
        tvCurrentLocation = view.findViewById(R.id.tv_current_location);
        tvRouteSummary = view.findViewById(R.id.tv_route_summary);
        tvCurrentStep = view.findViewById(R.id.tv_current_step);

        tv_poi_name = view.findViewById(R.id.tv_poi_name);
        tv_poi_address = view.findViewById(R.id.tv_poi_address);
        tv_poi_tel = view.findViewById(R.id.tv_poi_tel);
        tv_poi_type = view.findViewById(R.id.tv_poi_type);
        tv_poi_distance = view.findViewById(R.id.tv_poi_distance);

        MapsInitializer.updatePrivacyShow(getContext(), true, true);
        MapsInitializer.updatePrivacyAgree(getContext(), true);
        mMapView.onCreate(savedInstanceState);
        aMap = mMapView.getMap();
        aMap.setMyLocationEnabled(true);
        // 关闭高德地图自带的“定位蓝点自动回到屏幕中心”
        aMap.getUiSettings().setMyLocationButtonEnabled(false);
        // 彻底关闭高德自带的定位跟随模式（核心！）
        aMap.setMyLocationRotateAngle(0);
        aMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE); // 只画蓝点，不移动镜头
        aMap.animateCamera(CameraUpdateFactory.zoomTo(17));

        startRealLocation();
        rvSuggest.setLayoutManager(new LinearLayoutManager(getContext()));
        SuggestAdapter adapter = new SuggestAdapter();
        rvSuggest.setAdapter(adapter);

        try {
            routeSearch = new RouteSearch(getContext());
        } catch (AMapException e) {
            throw new RuntimeException(e);
        }
        routeSearch.setRouteSearchListener(this);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                String key = s.toString().trim();
                if (key.length() > 1) {
                    try {
                        parseCityAndSearch(key);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    rvSuggest.setVisibility(View.VISIBLE);
                } else {
                    adapter.setList(new ArrayList<>());
                    rvSuggest.setVisibility(View.GONE);
                }
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        btnBus.setOnClickListener(v -> selectNavMode(0));
        btnCar.setOnClickListener(v -> selectNavMode(1));
        btnWalk.setOnClickListener(v -> selectNavMode(2));
        btnStartNav.setOnClickListener(v -> startRoutePlan());
        btnBack.setOnClickListener(v -> onBackClicked());

        llNavBar.setVisibility(View.GONE);
        llRouteDetail.setVisibility(View.GONE);
        resetNavButtonState();

        // 收藏跳转
        if (mPendingPoi != null) {
            // 只把名称填入搜索框
            etSearch.setText(mPendingPoi.getTitle());
            // 清空，避免重复
            mPendingPoi = null;
        }

        return view;
    }

    // 统一重置按钮为未选中状态
    private void resetNavButtonState() {
        currentNavMode = -1;
        // 浅蓝背景 #E3F2FD
        btnBus.setBackgroundColor(0xFFE3F2FD);
        btnCar.setBackgroundColor(0xFFE3F2FD);
        btnWalk.setBackgroundColor(0xFFE3F2FD);
        // 黑色文字 #333333
        btnBus.setTextColor(0xFF333333);
        btnCar.setTextColor(0xFF333333);
        btnWalk.setTextColor(0xFF333333);
    }


    // 选择出行模式（高亮：深蓝底白字，未选中浅蓝底黑字）
    private void selectNavMode(int mode) {
        resetNavButtonState();
        currentNavMode = mode;
        switch (mode) {
            case 0:
                // 深蓝背景 #0066CC
                btnBus.setBackgroundColor(0xFF0066CC);
                // 白色文字
                btnBus.setTextColor(0xFFFFFFFF);
                break;
            case 1:
                btnCar.setBackgroundColor(0xFF0066CC);
                btnCar.setTextColor(0xFFFFFFFF);
                break;
            case 2:
                btnWalk.setBackgroundColor(0xFF0066CC);
                btnWalk.setTextColor(0xFFFFFFFF);
                break;
        }
    }

    // 智能解析城市，支持跨城搜索
    private void parseCityAndSearch(String key) throws Exception {
        String[] keywords = key.split("\\s+");
        String realKey = key;
        mSearchCity = (mCityName == null || mCityName.isEmpty()) ? "全国" : mCityName;

        if (keywords.length >= 2) {
            String first = keywords[0];
            if (first.contains("省") || first.contains("市") || first.contains("区") || first.contains("县")) {
                mSearchCity = first;
                realKey = key.replaceFirst(first, "").trim();
            }
        }

        PoiSearch.Query query = new PoiSearch.Query(realKey, "", mSearchCity);
        query.setPageSize(20);
        PoiSearch search = new PoiSearch(getContext(), query);
        search.setOnPoiSearchListener(new PoiSearch.OnPoiSearchListener() {
            @Override
            public void onPoiSearched(PoiResult poiResult, int i) {
                if (poiResult != null) {
                    ((SuggestAdapter) rvSuggest.getAdapter()).setList(poiResult.getPois());
                }
            }
            @Override public void onPoiItemSearched(PoiItem poiItem, int i) {}
        });
        search.searchPOIAsyn();
    }

    // 开始规划路线，小于1km公交提示并自动切换步行
    private void startRoutePlan() {
        if (mMyLatLng == null || mTargetPoi == null || currentNavMode == -1) {
            Toast.makeText(getContext(), "请先选地点和出行方式", Toast.LENGTH_SHORT).show();
            return;
        }

        LatLonPoint start = new LatLonPoint(mMyLatLng.latitude, mMyLatLng.longitude);
        LatLonPoint end = mTargetPoi.getLatLonPoint();
        float[] dis = new float[1];
        android.location.Location.distanceBetween(
                start.getLatitude(), start.getLongitude(),
                end.getLatitude(), end.getLongitude(), dis);

        if (currentNavMode == 0 && dis[0] < 1000) {
            Toast.makeText(getContext(), "距离过近，建议步行", Toast.LENGTH_SHORT).show();
            selectNavMode(2);
        }

        Toast.makeText(getContext(), "路线规划中...", Toast.LENGTH_SHORT).show();
        RouteSearch.FromAndTo fromAndTo = new RouteSearch.FromAndTo(start, end);

        switch (currentNavMode) {
            case 0:
                RouteSearch.BusRouteQuery busQuery = new RouteSearch.BusRouteQuery(fromAndTo, RouteSearch.BUS_DEFAULT, mCityName, 0);
                routeSearch.calculateBusRouteAsyn(busQuery);
                break;
            case 1:
                RouteSearch.DriveRouteQuery driveQuery = new RouteSearch.DriveRouteQuery(fromAndTo, RouteSearch.DRIVING_SINGLE_DEFAULT, null, null, null);
                routeSearch.calculateDriveRouteAsyn(driveQuery);
                break;
            case 2:
                RouteSearch.WalkRouteQuery walkQuery = new RouteSearch.WalkRouteQuery(fromAndTo);
                routeSearch.calculateWalkRouteAsyn(walkQuery);
                break;
        }
    }

    // 返回逻辑
    private void onBackClicked() {
        if (llRouteDetail.getVisibility() == View.VISIBLE) {
            // ==============================================
            // 阶段三 → 阶段二：返回 → 镜头回到【目的地】
            // ==============================================
            llRouteDetail.setVisibility(View.GONE);
            llNavBar.setVisibility(View.VISIBLE);
            isNavigating = false;

            if (mTargetPoi != null) {
                LatLonPoint p = mTargetPoi.getLatLonPoint();
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(p.getLatitude(), p.getLongitude()), 17
                ));
            }

        } else if (llNavBar.getVisibility() == View.VISIBLE) {
            // ==============================================
            // 阶段二 → 阶段一：返回 → 镜头回到【当前定位】
            // ==============================================
            exitNavigation();
        } else {
            // ==============================================
            // 阶段一点返回：回到【当前定位】
            // ==============================================
            if (mMyLatLng != null) {
                aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMyLatLng, 17));
            }
            Toast.makeText(getContext(), "已在首页", Toast.LENGTH_SHORT).show();
        }
    }

    // 退出到首页，完全重置状态
    private void exitNavigation() {
        llNavBar.setVisibility(View.GONE);
        llRouteDetail.setVisibility(View.GONE);
        mTargetPoi = null;
        isNavigating = false;
        resetNavButtonState();

        // ==============================================
        // 修复：只清除标记+路线，保留定位蓝点
        // ==============================================
        aMap.clear(true);

        etSearch.setText("");

        if (mMyLatLng != null) {
            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMyLatLng, 17));
        }
    }

    // 选中POI，进入阶段二，重置按钮
    private void onPoiClick(PoiItem item) {
        // 1. 判空，防止崩溃
        if (item == null || getView() == null) {
            return;
        }

        mTargetPoi = item;
        etSearch.setText(item.getTitle());
        rvSuggest.setVisibility(View.GONE);
        llNavBar.setVisibility(View.VISIBLE);
        llRouteDetail.setVisibility(View.GONE);
        resetNavButtonState();

        aMap.clear(true);

        // ===================== 【核心修复：判断经纬度是否为空】 =====================
        LatLonPoint p = item.getLatLonPoint();
        if (p != null) {
            // 正常POI：有经纬度 → 显示标记
            aMap.addMarker(new MarkerOptions()
                    .position(new LatLng(p.getLatitude(), p.getLongitude()))
                    .title("目的地")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

            aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(p.getLatitude(), p.getLongitude()), 17
            ));
        } else {
            // 收藏来的POI：没有经纬度 → 不移动地图，只显示信息
            Toast.makeText(getContext(), "已打开收藏地点信息", Toast.LENGTH_SHORT).show();
        }

        // 显示信息（一定执行）
        tv_poi_name.setText(item.getTitle());
        tv_poi_address.setText("地址：" + item.getSnippet());
        tv_poi_tel.setText("电话：暂无");
        tv_poi_type.setText("类型：无障碍场所");

        // 距离计算（只有定位成功才显示）
        if (mMyLatLng != null && p != null) {
            float[] dis = new float[1];
            android.location.Location.distanceBetween(
                    mMyLatLng.latitude, mMyLatLng.longitude,
                    p.getLatitude(), p.getLongitude(), dis);
            tv_poi_distance.setText("距离：约" + Math.round(dis[0]) + "米");
        } else {
            tv_poi_distance.setText("距离：未知");
        }

        // 收藏逻辑
        boolean isCollected = CollectManager.isCollectedPoi(getContext(), item);
        btnCollect.setText(isCollected ? "取消收藏" : "收藏");

        btnCollect.setOnClickListener(v -> {
            boolean result = CollectManager.toggleCollectPoi(getContext(), item);
            btnCollect.setText(result ? "取消收藏" : "收藏");
            Toast.makeText(getContext(), result ? "收藏成功" : "已取消收藏", Toast.LENGTH_SHORT).show();
        });
    }

    // 实时定位（不自动移动地图）
    private void startRealLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                return;
            }
            mLocationClient = new AMapLocationClient(getContext());
            AMapLocationClientOption option = new AMapLocationClientOption();
            option.setNeedAddress(true);
            option.setInterval(2000);
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            mLocationClient.setLocationOption(option);
            mLocationClient.setLocationListener(new AMapLocationListener() {
                @Override
                public void onLocationChanged(AMapLocation loc) {
                    if (loc == null || loc.getErrorCode() != 0) return;
                    mMyLatLng = new LatLng(loc.getLatitude(), loc.getLongitude());
                    mCityName = loc.getCity();
                    tvCurrentLocation.setText("当前位置：" + loc.getAddress());

                    // ==============================================
                    // 彻底修复：只有导航中(isNavigating=true)才移动镜头
                    // 阶段一 / 阶段二 绝不移动地图！
                    // ==============================================
                    if (isNavigating) {
                        checkDistanceAndNextStep(loc);
                        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mMyLatLng, 17));
                    }

                }
            });
            mLocationClient.startLocation();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 实时导航，到达自动下一步
    private void checkDistanceAndNextStep(AMapLocation loc) {
        try {
            LatLng my = new LatLng(loc.getLatitude(), loc.getLongitude());
            LatLonPoint target = null;

            if (currentNavMode == 2 && mWalkPath != null) {
                List<WalkStep> steps = mWalkPath.getSteps();
                if (currentStepIndex < steps.size()) target = steps.get(currentStepIndex).getPolyline().get(0);
            } else if (currentNavMode == 1 && mDrivePath != null) {
                List<DriveStep> steps = mDrivePath.getSteps();
                if (currentStepIndex < steps.size()) target = steps.get(currentStepIndex).getPolyline().get(0);
            }

            if (target == null) return;
            float[] dis = new float[1];
            android.location.Location.distanceBetween(my.latitude, my.longitude, target.getLatitude(), target.getLongitude(), dis);
            if (dis[0] < 15) {
                currentStepIndex++;
                showCurrentStep();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 只显示当前一步
    private void showCurrentStep() {
        try {
            if (currentNavMode == 2 && mWalkPath != null) {
                List<WalkStep> steps = mWalkPath.getSteps();
                tvCurrentStep.setText(currentStepIndex < steps.size() ? steps.get(currentStepIndex).getInstruction() : "已到达目的地");
            } else if (currentNavMode == 1 && mDrivePath != null) {
                List<DriveStep> steps = mDrivePath.getSteps();
                tvCurrentStep.setText(currentStepIndex < steps.size() ? steps.get(currentStepIndex).getInstruction() : "已到达目的地");
            }
        } catch (Exception e) {
            tvCurrentStep.setText("继续直行");
        }
    }

    // 路线绘制
    private void drawWalkRoute(WalkPath path) {
        aMap.clear(true);
        List<LatLng> pts = new ArrayList<>();
        for (WalkStep step : path.getSteps()) {
            for (LatLonPoint p : step.getPolyline()) {
                pts.add(new LatLng(p.getLatitude(), p.getLongitude()));
            }
        }
        aMap.addPolyline(new PolylineOptions().addAll(pts).color(0xFF0066CC).width(12));
        addDestMarker();
    }

    private void drawDriveRoute(DrivePath path) {
        aMap.clear(true);
        List<LatLng> pts = new ArrayList<>();
        for (DriveStep step : path.getSteps()) {
            for (LatLonPoint p : step.getPolyline()) {
                pts.add(new LatLng(p.getLatitude(), p.getLongitude()));
            }
        }
        aMap.addPolyline(new PolylineOptions().addAll(pts).color(0xFF0066CC).width(12));
        addDestMarker();
    }

    private void drawBusRoute(BusPath path) {
        aMap.clear(true);
        List<LatLng> pts = new ArrayList<>();
        for (BusStep step : path.getSteps()) {
            if (step.getWalk() != null) {
                for (WalkStep ws : step.getWalk().getSteps()) {
                    for (LatLonPoint p : ws.getPolyline()) {
                        pts.add(new LatLng(p.getLatitude(), p.getLongitude()));
                    }
                }
            }
        }
        aMap.addPolyline(new PolylineOptions().addAll(pts).color(0xFF0066CC).width(12));
        addDestMarker();
    }

    private void addDestMarker() {
        if (mTargetPoi != null) {
            LatLonPoint p = mTargetPoi.getLatLonPoint();
            aMap.addMarker(new MarkerOptions()
                    .position(new LatLng(p.getLatitude(), p.getLongitude()))
                    .title("目的地")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
        }
    }

    // 路线回调
    @Override
    public void onWalkRouteSearched(WalkRouteResult result, int rCode) {
        if (rCode == 1000 && result != null && !result.getPaths().isEmpty()) {
            mWalkPath = result.getPaths().get(0);
            tvRouteSummary.setText("步行 | 约" + mWalkPath.getDuration() / 60 + "分钟");
            currentStepIndex = 0;
            isNavigating = true;
            showCurrentStep();
            drawWalkRoute(mWalkPath);
            llNavBar.setVisibility(View.GONE);
            llRouteDetail.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDriveRouteSearched(DriveRouteResult result, int rCode) {
        if (rCode == 1000 && result != null && !result.getPaths().isEmpty()) {
            mDrivePath = result.getPaths().get(0);
            tvRouteSummary.setText("驾车 | 约" + mDrivePath.getDuration() / 60 + "分钟");
            currentStepIndex = 0;
            isNavigating = true;
            showCurrentStep();
            drawDriveRoute(mDrivePath);
            llNavBar.setVisibility(View.GONE);
            llRouteDetail.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onBusRouteSearched(BusRouteResult result, int rCode) {
        if (rCode == 1000 && result != null && !result.getPaths().isEmpty()) {
            mBusPath = result.getPaths().get(0);
            tvRouteSummary.setText("公交 | 约" + mBusPath.getDuration() / 60 + "分钟");
            currentStepIndex = 0;
            isNavigating = true;
            showCurrentStep();
            drawBusRoute(mBusPath);
            llNavBar.setVisibility(View.GONE);
            llRouteDetail.setVisibility(View.VISIBLE);
        }
    }

    @Override public void onRideRouteSearched(RideRouteResult rideRouteResult, int i) {}

    // 搜索适配器
    class SuggestAdapter extends RecyclerView.Adapter<SuggestAdapter.VH> {
        private List<PoiItem> list = new ArrayList<>();
        public void setList(List<PoiItem> list) { this.list = list; notifyDataSetChanged(); }

        @Override
        public VH onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(VH holder, int position) {
            PoiItem item = list.get(position);
            holder.tv.setText(item.getTitle() + " - " + item.getSnippet());
            holder.itemView.setOnClickListener(v -> onPoiClick(item));
        }

        @Override public int getItemCount() { return list.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tv;
            public VH(View itemView) {
                super(itemView);
                tv = itemView.findViewById(android.R.id.text1);
            }
        }
    }

    @Override public void onResume() { super.onResume(); mMapView.onResume(); }
    @Override public void onPause() { super.onPause(); mMapView.onPause(); }
    @Override public void onDestroy() { super.onDestroy(); mMapView.onDestroy(); }
}