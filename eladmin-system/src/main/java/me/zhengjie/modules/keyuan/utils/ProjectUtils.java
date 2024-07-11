package me.zhengjie.modules.keyuan.utils;

public class ProjectUtils {
    private static final String[] MONTH_NAMES = new String[]{"一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"};

    public static String projectTypeToName(int typeId) {
        switch (typeId) {
            case 0:
                return "检测";
            case 1:
                return "监理";
            case 2:
                return "设计";
            case 3:
                return "其他";
            default:
                return "unknown";
        }
    }

    public static String monthNumberToName(int month) {
        return MONTH_NAMES[month];
    }

    public static double dbPriceToRealPrice(Number dbPrice) {
        return dbPrice.doubleValue() / 100;
    }
}