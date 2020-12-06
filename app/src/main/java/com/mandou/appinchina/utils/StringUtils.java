package com.mandou.appinchina.utils;

import java.text.DecimalFormat;

/**
 * Created by yanfuchang on 2017/11/20.
 */

public class StringUtils {
    private static final long K = 1024;
    private static final long M = K * K;
    private static final long G = M * K;
    private static final long T = G * K;

    public static String bytesToHuman(long value) {
        long[] dividers = new long[]{T, G, M, K, 1};
        String[] units = new String[]{"TB", "GB", "MB", "KB", "B"};
        if (value < 1) {
            return 0 + " " + units[units.length - 1];
        }
        String result = null;
        for (int i = 0; i < dividers.length; i++) {
            long divider = dividers[i];
            if (value >= divider) {
                result = format(value, divider, units[i]);
                break;
            }
        }
        return result;
    }

    private static String format(long value,
                                 long divider,
                                 String unit) {
        double result =
                divider > 1 ? (double) value / (double) divider : (double) value;
        return new DecimalFormat("#.##").format(result) + " " + unit;
    }
}
