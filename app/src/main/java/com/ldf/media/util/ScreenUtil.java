package com.ldf.media.util;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * author：   ldf
 * date：      2021/1/14 & 20:39
 * version    1.0
 * description
 * modify by
 */
public class ScreenUtil {

    /**
     * 获取屏幕宽度(像素px)
     */
    public static int getPxWidth(Context context) {
        DisplayMetrics metric = context.getResources().getDisplayMetrics();
        return metric.widthPixels;
    }

    /**
     * 获取屏幕高度(像素px)
     */
    public static int getPxHeight(Context context) {
        DisplayMetrics metric = context.getResources().getDisplayMetrics();
        return metric.heightPixels;
    }

}
