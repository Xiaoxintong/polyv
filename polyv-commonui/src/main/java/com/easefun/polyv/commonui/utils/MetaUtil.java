package com.easefun.polyv.commonui.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * Created by Luke on 16/7/19.
 */
public class MetaUtil {

    public final static String HOST_ID = "HOST_ID";

    /** 河南校讯通HostId */
    public final static int HOST_ID_HNXXT = 1;

    /** 众享圈HostId */
    public final static int HOST_ID_ZXQ = 2;

    /** 众享家校HostId */
    public final static int HOST_ID_ZXJX = 7;

    /** 家校通HostId */
    public final static int HOST_ID_JXT = 9;

    /** 在线课程HostId */
    public final static int HOST_ID_ZXKC = 10;

    /** 在线课程HostId */
    public final static int HOST_ID_ZXJY = 12;

    /**
     * 获取String类型的meta-data
     * @param context 上下文
     * @param name key
     * @return value
     */
    public static String getMetaStringValue(Context context, String name) {
        String value= "";
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            value = appInfo.metaData.getString(name);
        } catch (PackageManager.NameNotFoundException e) {
        }
        return value;
    }

    /**
     * 获取int类型的meta-data
     * @param context 上下文
     * @param name key
     * @return value
     */
    public static int getMetaIntValue(Context context, String name) {
        int value= -1;
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            value = appInfo.metaData.getInt(name);
        } catch (Exception e) {
        }
        return value;
    }

    /**
     * 获取boolean类型的meta-data
     * @param context 上下文
     * @param name key
     * @return value
     */
    public static boolean getMetaBooleanValue(Context context, String name) {
        boolean value= false;
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            value = appInfo.metaData.getBoolean(name);
        } catch (PackageManager.NameNotFoundException e) {
        }
        return value;
    }

    /**
     * 获取float类型的meta-data
     * @param context 上下文
     * @param name key
     * @return value
     */
    public static float getMetaFloatValue(Context context, String name) {
        float value= 0.0f;
        try {
            ApplicationInfo appInfo = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            value = appInfo.metaData.getFloat(name);
        } catch (PackageManager.NameNotFoundException e) {
        }
        return value;
    }

}
