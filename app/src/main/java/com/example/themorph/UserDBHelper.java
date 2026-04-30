package com.example.themorph;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.themorph.DeafMap.CollectPoiBean;

import java.util.ArrayList;
import java.util.List;

public class UserDBHelper extends SQLiteOpenHelper {

    // 数据库配置
    private static final String DB_NAME = "morph_user.db";
    private static final int DB_VERSION = 5; // 升级以重建收藏表

    // 用户表字段
    private static final String TABLE_USER = "t_user";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_PHONE = "phone";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_DISABILITY_CARD = "disability_card";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_REGISTER_TIME = "register_time";

    // ===================== 收藏表配置（支持3种类型） =====================
    public static final String TABLE_COLLECT = "t_collect";
    public static final String KEY_COLLECT_ID = "collect_id";          // 主键
    public static final String KEY_COLLECT_USER_ID = "user_id";       // 用户ID
    public static final String KEY_COLLECT_TYPE = "collect_type";     // 1=POI场所 2=词汇 3=课程
    public static final String KEY_COLLECT_RELATE_ID = "relate_id";   // 关联ID（poiId/词汇Id）
    public static final String KEY_COLLECT_TAGS = "tags";             // 自定义标签
    public static final String KEY_COLLECT_TIME = "collect_time";     // 收藏时间

    // 收藏类型常量
    public static final int TYPE_POI = 1;    // 高德POI场所（听障用户用）
    public static final int TYPE_WORD = 2;   // 手语词汇
    public static final int TYPE_COURSE = 3;// 手语课程

    // 单例
    private static UserDBHelper instance;

    public static synchronized UserDBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new UserDBHelper(context.getApplicationContext());
        }
        return instance;
    }

    private UserDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // 创建表：用户表 + 收藏表
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建用户表
        String createUserTable = "CREATE TABLE " + TABLE_USER + "("
                + KEY_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_PHONE + " TEXT UNIQUE NOT NULL,"
                + KEY_PASSWORD + " TEXT NOT NULL,"
                + KEY_DISABILITY_CARD + " TEXT,"
                + KEY_USER_TYPE + " INTEGER NOT NULL DEFAULT 2,"
                + KEY_REGISTER_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(createUserTable);

        // 创建收藏表（支持多类型、多用户、标签）
        String createCollectTable = "CREATE TABLE " + TABLE_COLLECT + "("
                + KEY_COLLECT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + KEY_COLLECT_USER_ID + " INTEGER NOT NULL,"
                + KEY_COLLECT_TYPE + " INTEGER NOT NULL,"
                + KEY_COLLECT_RELATE_ID + " TEXT NOT NULL,"
                + "title TEXT NOT NULL,"      // 名称
                + "address TEXT NOT NULL,"    // 地址
                + KEY_COLLECT_TAGS + " TEXT,"
                + KEY_COLLECT_TIME + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(createCollectTable);
        Log.d("DB", "用户表 + 收藏表 创建成功");
    }

    // 升级数据库：重建表（开发阶段用）
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COLLECT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    // ===================== 收藏相关方法 =====================

    public long addCollect(int userId, int type, String relateId, String title, String address, String tags) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(KEY_COLLECT_USER_ID, userId);
        cv.put(KEY_COLLECT_TYPE, type);
        cv.put(KEY_COLLECT_RELATE_ID, relateId);
        cv.put("title", title);
        cv.put("address", address);
        cv.put(KEY_COLLECT_TAGS, tags);
        long id = db.insert(TABLE_COLLECT, null, cv);
        db.close();
        return id;
    }

    /**
     * 取消收藏
     */
    public void deleteCollect(int userId, int collectType, String relateId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_COLLECT,
                KEY_COLLECT_USER_ID + "=? AND " + KEY_COLLECT_TYPE + "=? AND " + KEY_COLLECT_RELATE_ID + "=?",
                new String[]{userId + "", collectType + "", relateId});
        db.close();
    }

    /**
     * 判断是否已收藏
     */
    public boolean isCollected(int userId, int collectType, String relateId) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_COLLECT, null,
                KEY_COLLECT_USER_ID + "=? AND " + KEY_COLLECT_TYPE + "=? AND " + KEY_COLLECT_RELATE_ID + "=?",
                new String[]{userId + "", collectType + "", relateId},
                null, null, null);
        boolean isExist = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return isExist;
    }

    // 获取POI收藏列表（带标题地址）
    public List<CollectPoiBean> getCollectPoiList(int userId) {
        List<CollectPoiBean> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query(TABLE_COLLECT,
                new String[]{"relate_id", "title", "address"},
                "user_id=? AND collect_type=?",
                new String[]{userId + "", TYPE_POI + ""},
                null, null, "collect_time DESC");

        if (c != null) {
            while (c.moveToNext()) {
                String id = c.getString(0);
                String title = c.getString(1);
                String address = c.getString(2);
                list.add(new CollectPoiBean(id, title, address));
            }
            c.close();
        }
        db.close();
        return list;
    }

    // ===================== 用户相关方法 =====================

    /**
     * 注册用户
     */
    public long registerUser(String phone, String password, String disabilityCard) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_USER, new String[]{KEY_PHONE},
                KEY_PHONE + "=?", new String[]{phone}, null, null, null);
        if (cursor.getCount() > 0) {
            cursor.close();
            db.close();
            return -1;
        }
        cursor.close();

        int userType = 2;
        if (disabilityCard != null && disabilityCard.length() == 20 && disabilityCard.matches("\\d+")) {
            userType = 1;
        }

        ContentValues values = new ContentValues();
        values.put(KEY_PHONE, phone);
        values.put(KEY_PASSWORD, password);
        values.put(KEY_DISABILITY_CARD, disabilityCard);
        values.put(KEY_USER_TYPE, userType);
        long result = db.insert(TABLE_USER, null, values);
        db.close();
        return result;
    }

    /**
     * 登录验证
     */
    public String loginUser(String phone, String password) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER, new String[]{KEY_USER_TYPE},
                KEY_PHONE + "=? AND " + KEY_PASSWORD + "=?",
                new String[]{phone, password}, null, null, null);
        String userType = null;
        if (cursor.moveToFirst()) {
            userType = cursor.getString(0);
        }
        cursor.close();
        db.close();
        return userType;
    }

    /**
     * 根据手机号获取用户ID
     */
    public int getUserIdByPhone(String phone) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER,
                new String[]{KEY_USER_ID},
                KEY_PHONE + "=?",
                new String[]{phone},
                null, null, null);
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return userId;
    }

    /**
     * 手机号格式校验
     */
    public static boolean isPhoneValid(String phone) {
        return phone != null && phone.length() == 11 && phone.matches("\\d+");
    }

    /**
     * 残疾人证校验
     */
    public static boolean isDisabilityCardValid(String card) {
        if (card == null || card.isEmpty()) return true;
        return card.length() == 20 && card.matches("\\d+");
    }
}