package me.zhengjie.modules.keyuan.utils;

import java.util.Calendar;

public class CalendarUtils {


    public static Calendar getBeginningOfYear() {
        Calendar calendar = getBeginningOfMonth(System.currentTimeMillis());
        calendar.set(Calendar.MONTH, Calendar.JANUARY);
        return calendar;
    }

    public static Calendar getBeginningOfMonth(long time) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    //1=一月
    public static Calendar getEndOfMonth(int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, 0);//上月最后一天
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
