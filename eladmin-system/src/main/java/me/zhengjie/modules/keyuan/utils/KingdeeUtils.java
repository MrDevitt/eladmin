package me.zhengjie.modules.keyuan.utils;

import java.util.Calendar;

public class KingdeeUtils {

    public static String getThisMonthPeriod() {
        Calendar calendar = Calendar.getInstance();
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        return year + month;
    }
}
