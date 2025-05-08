package me.zhengjie.modules.keyuan.utils;

import me.zhengjie.modules.keyuan.domain.statistics.balance.ProjectDepartment;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailDto;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ProjectUtils {
    private static final String[] MONTH_NAMES = new String[]{"一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"};
    public static final String[] PROJECT_TYPE_NAMES = new String[]{"检测", "监理", "设计", "其他"};
    public static final String[] PROJECT_PAY_WAYS = new String[]{"签合同50，完工结清", "一次性付清", "签合同30进度50付30完工结清", "按进度拨付"};
    public static final String[] PROJECT_INVOICE_NAMES = new String[]{"专票", "普票", "无票"};
    public static final String[] PROJECT_EXAM_REGIONS = new String[]{"日喀则", "拉萨", "阿里", "那曲"};
    public static final String PROJECT_DEPARTMENT_PRESIDENT = "总裁办";
    public static final String PROJECT_DEPARTMENT_MANAGEMENT = "管理中心";
    public static final String PROJECT_DEPARTMENT_SALES = "业务中心";
    public static final String PROJECT_DEPARTMENT_TECH = "技术中心";
    public static final String[] PROJECT_DEPARTMENTS = new String[]{"业务中心", "技术中心", "管理中心", "总裁办"};
    public static final List<ProjectDepartment> PROJECT_DEPARTMENT_LIST = new ArrayList<>();

    public static final int PROJECT_TYPE_EXAM = 0;
    public static final int PROJECT_TYPE_SUPERVISE = 1;
    public static final int PROJECT_TYPE_DESIGN = 2;
    public static final int PROJECT_TYPE_OTHER = 3;

    public static final int INVOICE_TYPE_NONE = 2;
    public static final Set<String> GENERAL_TAXPAYERS = new HashSet<>(List.of("西藏科源工程检测有限公司", "西藏鸿沣工程技术管理有限公司"));
    public static final int MAX_INVOICE_AMOUNT = 500 * 1000 * 1000;

    //所有部门汇算方法
    static {
        ProjectDepartment sales = new ProjectDepartment(PROJECT_DEPARTMENT_SALES);
        sales.setAccountNumberList(List.of("540101", "113302"));
        sales.setInitialBalance(-82090843L);
        sales.setPercentageGetter(SysProjectDetailDto::getSalesPercent);
        PROJECT_DEPARTMENT_LIST.add(sales);

        ProjectDepartment management = new ProjectDepartment(PROJECT_DEPARTMENT_MANAGEMENT);
        management.setAccountNumberList(List.of("540103", "113303", "113317"));
        management.setInitialBalance(-425042267);
        management.setPercentageGetter(SysProjectDetailDto::getManagementPercent);
        PROJECT_DEPARTMENT_LIST.add(management);

        ProjectDepartment president = new ProjectDepartment(PROJECT_DEPARTMENT_PRESIDENT);
        president.setAccountNumberList(List.of("113304", "11330206"));
        president.setInitialBalance(356720754L);
        president.setPercentageGetter(SysProjectDetailDto::getPresidentPercent);
        PROJECT_DEPARTMENT_LIST.add(president);

        ProjectDepartment tech = new ProjectDepartment(PROJECT_DEPARTMENT_TECH);
        tech.setAccountNumberList(List.of("540102", "113301"));
        tech.setInitialBalance(32665835L);
        tech.setPercentageGetter(SysProjectDetailDto::getTechnicalPercent);
        tech.setChildren(new ArrayList<>());
        PROJECT_DEPARTMENT_LIST.add(tech);

        //检测
        ProjectDepartment exam = new ProjectDepartment("检测技术");
        tech.getChildren().add(exam);
        exam.setAccountNumberList(List.of("54010201", "11330101"));
        exam.setInitialBalance(141717692L);
        exam.setDataCalculator(parent -> Map.of(PROJECT_TYPE_EXAM, parent.getReceiveByType().get(PROJECT_TYPE_EXAM)));
        exam.setChildren(new ArrayList<>());
        Map<String, Pair<List<String>, Long>> examMap = new HashMap<>();
        examMap.put("日喀则", Pair.of(List.of("5401020103", "5401020106", "1133010101"), 122574775L));
        examMap.put("拉萨", Pair.of(List.of("5401020101", "5401020108", "1133010102"), 13472853L));
        examMap.put("阿里", Pair.of(List.of("5401020102", "5401020107", "1133010103"), 7997788L));
        examMap.put("那曲", Pair.of(List.of("5401020110", "5401020111", "1133010113"), -2327724L));
        examMap.forEach((k, v) -> {
            ProjectDepartment department = new ProjectDepartment(k + "检测");
            department.setAccountNumberList(v.getLeft());
            department.setInitialBalance(v.getRight());
            department.setDataCalculator(parent -> Map.of(PROJECT_TYPE_EXAM, Optional.ofNullable(parent.getReceiveByTypeAndRegion())
                    .map(e -> e.get(PROJECT_TYPE_EXAM))
                    .map(e -> e.get(k))
                    .orElse(new long[]{0, 0, 0})));
            exam.getChildren().add(department);
        });

        //监理
        ProjectDepartment supervise = new ProjectDepartment("监理技术");
        tech.getChildren().add(supervise);
        supervise.setAccountNumberList(List.of("54010202", "11330102"));
        supervise.setInitialBalance(31038732L);
        supervise.setDataCalculator(parent -> Map.of(PROJECT_TYPE_SUPERVISE, Optional.ofNullable(parent.getReceiveByType())
                .map(e -> e.get(PROJECT_TYPE_SUPERVISE))
                .orElse(new long[]{0, 0, 0})));
        supervise.setChildren(new ArrayList<>());
        Map<String, Pair<List<String>, Long>> superviseMap = new HashMap<>();
        superviseMap.put("康马", Pair.of(List.of("5401020203", "1133010203"), 11115380L));
        superviseMap.put("聂拉木", Pair.of(List.of("5401020207", "1133010205"), 3713918L));
        superviseMap.put("萨嗄", Pair.of(List.of("1133010206"), 1959487L));
        superviseMap.put("仁布", Pair.of(List.of("5401020205", "1133010209"), 6042130L));
        superviseMap.put("谢通门", Pair.of(List.of("5401020216", "1133010212"), 0L));
        superviseMap.put("岗巴", Pair.of(List.of("5401020202"), 1167580L));
        superviseMap.put("白朗", Pair.of(List.of("5401020204"), -11835924L));
        superviseMap.put("定日", Pair.of(List.of("5401020206", "1133010202"), 8612806L));
        superviseMap.put("日喀则市区", Pair.of(List.of("5401020201"), 0L));
        superviseMap.put("萨嘎", Pair.of(List.of("5401020209"), 0L));
        superviseMap.put("昌都", Pair.of(List.of("5401020217"), 0L));
        superviseMap.put("总工办", Pair.of(List.of("5401020214", "1133010211"), 10263355L));
        superviseMap.forEach((k, v) -> {
            ProjectDepartment department = new ProjectDepartment(k + "监理");
            department.setAccountNumberList(v.getLeft());
            department.setInitialBalance(v.getRight());
            department.setDataCalculator(parent -> Map.of(PROJECT_TYPE_SUPERVISE, Optional.ofNullable(parent.getReceiveByTypeAndRegion())
                    .map(e -> e.get(PROJECT_TYPE_SUPERVISE))
                    .map(e -> {
                        if ("总工办".equals(k)) {
                            long[] amounts = new long[]{0, 0, 0};
                            e.values().forEach(values -> {
                                amounts[0] += values[0] * 4 / 30;
                                amounts[1] += values[1] * 4 / 30;
                                amounts[2] += values[2] * 4 / 30;
                            });
                            return amounts;
                        } else {
                            long[] amounts = e.getOrDefault(k, new long[]{0, 0, 0});
                            return new long[]{amounts[0] * 26 / 30, amounts[1] * 26 / 30, amounts[2] * 26 / 30};
                        }
                    })
                    .orElse(new long[]{0, 0, 0})));
            supervise.getChildren().add(department);
        });

        //设计
        ProjectDepartment design = new ProjectDepartment("设计技术");
        tech.getChildren().add(design);
        design.setAccountNumberList(List.of("54010203", "11330103"));
        design.setInitialBalance(-140090589L);
        design.setDataCalculator(parent -> Map.of(PROJECT_TYPE_DESIGN, Optional.ofNullable(parent.getReceiveByType())
                .map(e -> e.get(PROJECT_TYPE_DESIGN))
                .orElse(new long[]{0, 0, 0})));

    }

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
        if (dbPrice == null) {
            return 0d;
        }
        return dbPrice.doubleValue() / 100;
    }

    public static String dbPriceToRealPriceString(Number dbPrice) {
        if (dbPrice == null) {
            return "0.00";
        }
        return String.format("%.2f", dbPrice.doubleValue() / 100);
    }

    public static long realPriceToDbPrice(String realPrice) {
        return (long) Double.parseDouble(realPrice) * 100;
    }

    public static Map<String, Map<String, double[]>> generateTypeMap() {
        Map<String, Map<String, double[]>> map = new HashMap<>();
        Arrays.stream(ProjectUtils.PROJECT_TYPE_NAMES).forEach(e -> map.put(e, new HashMap<>()));
        return map;
    }

    public static Map<String, Map<String, double[]>> generateRegionMap() {
        Map<String, Map<String, double[]>> map = new HashMap<>();
        Arrays.stream(ProjectUtils.PROJECT_EXAM_REGIONS).forEach(e -> map.put(e, new HashMap<>()));
        return map;
    }

    public static double roundFix2(double num) {
        return ((double) Math.round(num * 100)) / 100;
    }
}