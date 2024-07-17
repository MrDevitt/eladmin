package me.zhengjie.modules.keyuan.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ProjectUtils {
    private static final String[] MONTH_NAMES = new String[]{"一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"};
    public static final String[] PROJECT_TYPE_NAMES = new String[]{"检测", "监理", "设计", "其他"};
    public static final String PROJECT_DEPARTMENT_PRESIDENT = "总裁办";
    public static final String PROJECT_DEPARTMENT_MANAGEMENT = "管理中心";
    public static final String PROJECT_DEPARTMENT_SALES = "业务中心";
    public static final String PROJECT_DEPARTMENT_TECH = "技术中心";

    public static String projectTypeToName(int typeId) {
        return PROJECT_TYPE_NAMES[typeId];
    }

    public static int projectTypeNameToId(String typeName) {
        switch (typeName) {
            case "检测":
                return 0;
            case "监理":
                return 1;
            case "设计":
                return 2;
            case "其他":
                return 3;
            default:
                return -1;
        }
    }

    public static String monthNumberToName(int month) {
        return MONTH_NAMES[month];
    }

    public static double dbPriceToRealPrice(Number dbPrice) {
        return dbPrice.doubleValue() / 100;
    }

    public static Map<String, Map<String, double[]>> generateTypeMap() {
        Map<String, Map<String, double[]>> map = new HashMap<>();
        Arrays.stream(ProjectUtils.PROJECT_TYPE_NAMES).forEach(e -> map.put(e, new HashMap<>()));
        return map;
    }

    public static double roundFix2(double num) {
        return ((double) Math.round(num * 100)) / 100;
    }
}