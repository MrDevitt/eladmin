package me.zhengjie.modules.keyuan.utils;

import java.util.Calendar;
import java.util.Map;

public class KingdeeUtils {

    public static final Map<Long, String> PERSON_ACCOUNT_NUMBER_MAP = Map.of(
            10L, "54010103"
    );

    public static String getThisMonthPeriod() {
        Calendar calendar = Calendar.getInstance();
        String year = String.valueOf(calendar.get(Calendar.YEAR));
        String month = String.format("%02d", calendar.get(Calendar.MONTH) + 1);
        return year + month;
    }
}
