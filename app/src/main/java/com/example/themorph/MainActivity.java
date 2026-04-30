package com.example.themorph;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.amap.api.services.core.PoiItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private String userType; // 1=听障用户,2=健听用户
    // 听障模式Fragment
    private HomeDeafFragment homeDeafFragment;
    private WarnDeafFragment warnDeafFragment;
    private TranslateDeafFragment translateDeafFragment;
    private MapDeafFragment mapDeafFragment;
    private SettingDeafFragment settingDeafFragment;
    // 健听模式Fragment
    private HomeHearingFragment homeHearingFragment;
    private TeachHearingFragment teachHearingFragment;
    private TranslateHearingFragment translateHearingFragment;
    private WordHearingFragment wordHearingFragment;
    private SettingHearingFragment settingHearingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 获取用户类型
        userType = getIntent().getStringExtra("user_type");

        // 如果还是空，给默认值 1（听障用户），不会崩溃
        if (userType == null) {
            userType = "1";
        }

        // 初始化控件
        bottomNavigationView = findViewById(R.id.bottom_nav);
        // 根据用户类型加载对应模式
        initModeByUserType();
        // 底部导航点击事件
        initBottomNavClick();

        // ===================== 收藏列表跳转地图（听障用户专用） ====================
        Intent fromIntent = getIntent();
        if (fromIntent.hasExtra("from_collect") && userType != null) {
            String poiId = fromIntent.getStringExtra("poi_id");
            String poiTitle = fromIntent.getStringExtra("poi_title");
            String poiAddress = fromIntent.getStringExtra("poi_address");

            if (mapDeafFragment != null && poiId != null) {
                // 正确构造方式（只传ID、标题、描述）
                PoiItem poiItem = new PoiItem(poiId, null, poiTitle, poiAddress);

                Bundle bundle = new Bundle();
                bundle.putParcelable("poi", poiItem);
                mapDeafFragment.setArguments(bundle);

                switchFragment(mapDeafFragment);
                bottomNavigationView.setSelectedItemId(R.id.nav_deaf_map);
            }
        }
    }

    /**
     * 根据用户类型初始化对应模式的导航和Fragment
     */
    private void initModeByUserType() {
        if (userType.equals("1")) {
            // 听障用户:加载听障模式菜单
            bottomNavigationView.inflateMenu(R.menu.menu_bottom_nav_deaf);
            // 中间翻译按钮加大突出
            bottomNavigationView.setItemIconSize(80); // 中间图标尺寸
            bottomNavigationView.setItemTextAppearanceActive(R.style.NavTextActive);
            bottomNavigationView.setItemTextAppearanceInactive(R.style.NavTextInactive);
            // 初始化听障模式Fragment
            initDeafFragment();
            // 默认显示首页
            switchFragment(homeDeafFragment);
        } else if (userType.equals("2")) {
            // 健听用户:加载健听模式菜单
            bottomNavigationView.inflateMenu(R.menu.menu_bottom_nav_hearing);
            // 中间翻译按钮加大突出
            bottomNavigationView.setItemIconSize(80);
            bottomNavigationView.setItemTextAppearanceActive(R.style.NavTextActive);
            bottomNavigationView.setItemTextAppearanceInactive(R.style.NavTextInactive);
            // 初始化健听模式Fragment
            initHearingFragment();
            // 默认显示首页
            switchFragment(homeHearingFragment);
        }
    }

    /**
     * 初始化听障模式Fragment
     */
    private void initDeafFragment() {
        homeDeafFragment = new HomeDeafFragment();
        warnDeafFragment = new WarnDeafFragment();
        translateDeafFragment = new TranslateDeafFragment();
        mapDeafFragment = new MapDeafFragment();
        settingDeafFragment = new SettingDeafFragment();
    }

    /**
     * 初始化健听模式Fragment
     */
    private void initHearingFragment() {
        homeHearingFragment = new HomeHearingFragment();
        teachHearingFragment = new TeachHearingFragment();
        translateHearingFragment = new TranslateHearingFragment();
        wordHearingFragment = new WordHearingFragment();
        settingHearingFragment = new SettingHearingFragment();
    }

    /**
     * 底部导航点击事件
     */
    private void initBottomNavClick() {
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (userType.equals("1")) {
                    // 听障模式导航点击
                    switch (item.getItemId()) {
                        case R.id.nav_deaf_home:
                            switchFragment(homeDeafFragment);
                            break;
                        case R.id.nav_deaf_warn:
                            switchFragment(warnDeafFragment);
                            break;
                        case R.id.nav_deaf_translate:
                            switchFragment(translateDeafFragment);
                            break;
                        case R.id.nav_deaf_map:
                            switchFragment(mapDeafFragment);
                            break;
                        case R.id.nav_deaf_setting:
                            switchFragment(settingDeafFragment);
                            break;
                    }
                } else if (userType.equals("2")) {
                    // 健听模式导航点击
                    switch (item.getItemId()) {
                        case R.id.nav_hearing_home:
                            switchFragment(homeHearingFragment);
                            break;
                        case R.id.nav_hearing_teach:
                            switchFragment(teachHearingFragment);
                            break;
                        case R.id.nav_hearing_translate:
                            switchFragment(translateHearingFragment);
                            break;
                        case R.id.nav_hearing_word:
                            switchFragment(wordHearingFragment);
                            break;
                        case R.id.nav_hearing_setting:
                            switchFragment(settingHearingFragment);
                            break;
                    }
                }
                return true;
            }
        });
    }

    /**
     * Fragment切换方法
     */
    private void switchFragment(Fragment targetFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        // 替换容器内的Fragment
        transaction.replace(R.id.fragment_container, targetFragment);
        transaction.commit();
    }
}