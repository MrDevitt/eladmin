package me.zhengjie.modules.keyuan.utils;

import java.util.Calendar;

public class CalendarUtils {


    public static Calendar getBeginningOfYear() {
        Calendar calendar = getBeginningOfMonth();
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        return calendar;
    }

    public static Calendar getBeginningOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
