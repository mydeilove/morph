package com.example.themorph.DeafMap;

import android.content.Context;
import com.amap.api.services.core.PoiItem;
import com.example.themorph.UserDBHelper;

import java.util.ArrayList;
import java.util.List;

public class CollectManager {

    /**
     * 获取当前登录用户的ID（从全局手机号获取）
     */
    private static int getCurrentUserId(Context context) {
        if (MyApplication.loginPhone == null || MyApplication.loginPhone.isEmpty()) {
            return -1;
        }
        UserDBHelper db = UserDBHelper.getInstance(context);
        return db.getUserIdByPhone(MyApplication.loginPhone);
    }

    /**
     * 收藏/取消收藏 POI（场所）
     * @return 收藏成功返回true，取消返回false
     */
    // 收藏
    public static boolean toggleCollectPoi(Context context, PoiItem item) {
        int uid = getCurrentUserId(context);
        if (uid < 0) return false;
        UserDBHelper db = UserDBHelper.getInstance(context);
        String poiId = item.getPoiId();

        if (db.isCollected(uid, UserDBHelper.TYPE_POI, poiId)) {
            db.deleteCollect(uid, UserDBHelper.TYPE_POI, poiId);
            return false;
        } else {
            // 保存：ID + 名称 + 地址 + 标签
            db.addCollect(uid, UserDBHelper.TYPE_POI, poiId,
                    item.getTitle(),
                    item.getSnippet(),
                    "无障碍场所");
            return true;
        }
    }

    // 获取收藏列表（带名称地址）
    public static List<CollectPoiBean> getCollectPoiList(Context context) {
        int uid = getCurrentUserId(context);
        if (uid < 0) return new ArrayList<>();
        return UserDBHelper.getInstance(context).getCollectPoiList(uid);
    }

    /**
     * 判断当前用户是否收藏了该POI
     */
    public static boolean isCollectedPoi(Context context, PoiItem item) {
        int userId = getCurrentUserId(context);
        if (userId == -1) return false;
        return UserDBHelper.getInstance(context).isCollected(
                userId, UserDBHelper.TYPE_POI, item.getPoiId());
    }

}