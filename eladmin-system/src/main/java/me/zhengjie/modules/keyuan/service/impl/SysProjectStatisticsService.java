package me.zhengjie.modules.keyuan.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.modules.keyuan.domain.config.SysProjectStatisticsConfig;
import me.zhengjie.modules.keyuan.domain.statistics.InvoiceTableRow;
import me.zhengjie.modules.keyuan.domain.statistics.InvoicedNotReceiveData;
import me.zhengjie.modules.keyuan.domain.statistics.SysProjectStatistics;
import me.zhengjie.modules.keyuan.domain.statistics.SysReceiveStatistics;
import me.zhengjie.modules.keyuan.domain.statistics.balance.AccountBalanceData;
import me.zhengjie.modules.keyuan.domain.statistics.balance.BalanceData;
import me.zhengjie.modules.keyuan.domain.statistics.balance.BalanceTableRow;
import me.zhengjie.modules.keyuan.domain.statistics.balance.ProjectDepartment;
import me.zhengjie.modules.keyuan.service.SysProjectConfigService;
import me.zhengjie.modules.keyuan.service.SysProjectDetailService;
import me.zhengjie.modules.keyuan.service.SysProjectPersonService;
import me.zhengjie.modules.keyuan.service.SysProjectReceiveService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailQueryCriteria;
import me.zhengjie.modules.keyuan.service.dto.SysProjectPersonDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectReceiveDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectReceiveQueryCriteria;
import me.zhengjie.modules.keyuan.utils.CalendarUtils;
import me.zhengjie.modules.keyuan.utils.ProjectUtils;
import me.zhengjie.utils.FileUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static me.zhengjie.modules.keyuan.domain.statistics.InvoicedNotReceiveData.Row.COMPARATOR;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysProjectStatisticsService {

    private final SysProjectDetailService sysProjectDetailService;

    private final SysProjectReceiveService sysProjectReceiveService;

    private final SysProjectPersonService sysProjectPersonService;

    private final KingdeeService kingdeeService;

    private final SysProjectConfigService sysProjectConfigService;

    public SysProjectStatistics getSysProjectStatisticsInfo(String contractYear, String receiveYear) {
        String key = contractYear + receiveYear;
        if (!SysProjectStatistics.CACHE_MAP.containsKey(key)) {
            SysProjectStatistics sysProjectStatistics = new SysProjectStatistics();
            SysProjectStatisticsConfig sysProjectConfig = sysProjectConfigService.findConfigByKey(ProjectUtils.PROJECT_CONFIG_KEY_STATISTICS, SysProjectStatisticsConfig.class);
            List<SysProjectDetailDto> sysProjectDetailDtoList = sysProjectDetailService.queryAll(new SysProjectDetailQueryCriteria());
            List<SysProjectDetailDto> projectBlackList = sysProjectDetailDtoList
                    .stream()
                    .filter(e -> sysProjectConfig.getSalesPersonBlackList().contains(e.getSalesPerson()))
                    .collect(Collectors.toList());
            sysProjectDetailDtoList.removeAll(projectBlackList);
            Map<Long, SysProjectPersonDto> sysProjectPersonDtoMap = sysProjectPersonService.getIdToPersonMap();
            buildContractStatistics(sysProjectStatistics, sysProjectDetailDtoList, sysProjectPersonDtoMap, contractYear);

            Map<Long, SysProjectDetailDto> sysProjectDetailDtoMap = sysProjectDetailDtoList.stream().collect(Collectors.toMap(
                    SysProjectDetailDto::getId,
                    Function.identity(),
                    (x, y) -> x
            ));

            SysProjectReceiveQueryCriteria criteria = new SysProjectReceiveQueryCriteria();
            criteria.setProjectIdsNotIn(projectBlackList.stream().map(SysProjectDetailDto::getId).collect(Collectors.toList()));
            List<SysProjectReceiveDto> sysProjectReceiveDtoList = sysProjectReceiveService.queryAll(criteria);
            buildReceiveStatistics(sysProjectStatistics, sysProjectReceiveDtoList, sysProjectDetailDtoMap, sysProjectPersonDtoMap, receiveYear);

            sysProjectStatistics.calcInnerData(contractYear, receiveYear);
            SysProjectStatistics.CACHE_MAP.put(key, sysProjectStatistics);
        }
        return SysProjectStatistics.CACHE_MAP.get(key);
    }

    private static void buildContractStatistics(
            SysProjectStatistics sysProjectStatistics,
            List<SysProjectDetailDto> sysProjectDetailDtoList,
            Map<Long, SysProjectPersonDto> sysProjectPersonDtoMap,
            String contractYear) {
        for (SysProjectDetailDto detailDto : sysProjectDetailDtoList) {
            double contractAmount = ProjectUtils.dbPriceToRealPrice(detailDto.getContractAmount());
            String projectType = ProjectUtils.projectTypeToName(detailDto.getProjectType());

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(detailDto.getCreateTime().getTime());
            int month = calendar.get(Calendar.MONTH);
            String year = String.valueOf(calendar.get(Calendar.YEAR));

            Map<String, double[]> typeMap = sysProjectStatistics.getContractByYearAndType().computeIfAbsent(year, k -> new HashMap<>());
            double[] typeMonthData = typeMap.computeIfAbsent(projectType, k -> new double[13]);
            typeMonthData[month] += contractAmount;
            typeMonthData[12] += contractAmount;

            if (contractYear.equals(year)) {
                Map<String, double[]> regionMap = sysProjectStatistics.getContractByTypeAndRegion().get(projectType);
                double[] regionData = regionMap.computeIfAbsent(detailDto.getProjectRegion(), k -> new double[13]);
                regionData[month] += contractAmount;
                regionData[12] += contractAmount;

                String personName = sysProjectPersonDtoMap.get(detailDto.getSalesPerson()).getName();
                Map<String, double[]> personMap = sysProjectStatistics.getContractByTypeAndPerson().get(projectType);
                double[] personData = personMap.computeIfAbsent(personName, k -> new double[13]);
                personData[month] += contractAmount;
                personData[12] += contractAmount;

            }

        }
    }

    private static void fillDepartmentMap(Map<String, double[]> departmentMap, SysProjectDetailDto detailDto, int month, double amount) {
        double[] presidentData = departmentMap.computeIfAbsent(ProjectUtils.PROJECT_DEPARTMENT_PRESIDENT, k -> new double[13]);
        double[] managementData = departmentMap.computeIfAbsent(ProjectUtils.PROJECT_DEPARTMENT_MANAGEMENT, k -> new double[13]);
        double[] salesData = departmentMap.computeIfAbsent(ProjectUtils.PROJECT_DEPARTMENT_SALES, k -> new double[13]);
        double[] techData = departmentMap.computeIfAbsent(ProjectUtils.PROJECT_DEPARTMENT_TECH, k -> new double[13]);

        double presidentAmount = amount * detailDto.getPresidentPercent() / 100;
        double managementAmount = amount * detailDto.getManagementPercent() / 100;
        double salesAmount = amount * detailDto.getSalesPercent() / 100;
        double techAmount = amount * detailDto.getTechnicalPercent() / 100;

        presidentData[month] += presidentAmount;
        presidentData[12] += presidentAmount;
        managementData[month] += managementAmount;
        managementData[12] += managementAmount;
        salesData[month] += salesAmount;
        salesData[12] += salesAmount;
        techData[month] += techAmount;
        techData[12] += techAmount;
    }

    private static void buildReceiveStatistics(
            SysProjectStatistics sysProjectStatistics,
            List<SysProjectReceiveDto> sysProjectReceiveDtoList,
            Map<Long, SysProjectDetailDto> sysProjectDetailDtoMap,
            Map<Long, SysProjectPersonDto> sysProjectPersonDtoMap,
            String receiveYear) {
        for (SysProjectReceiveDto receiveDto : sysProjectReceiveDtoList) {
            double receiveAmount = ProjectUtils.dbPriceToRealPrice(receiveDto.getReceiveAmount());
            SysProjectDetailDto detailDto = sysProjectDetailDtoMap.get(receiveDto.getProjectId());
            if (detailDto == null) {
                log.error("收款数据不存在！ receiveDto:{}", receiveDto);
                continue;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(receiveDto.getReceiveTime().getTime());
            int month = calendar.get(Calendar.MONTH);
            String year = String.valueOf(calendar.get(Calendar.YEAR));
            String projectType = ProjectUtils.projectTypeToName(detailDto.getProjectType());

            Map<String, double[]> typeMap = sysProjectStatistics.getReceiveByYearAndType().computeIfAbsent(year, k -> new HashMap<>());
            double[] typeMonthData = typeMap.computeIfAbsent(projectType, k -> new double[13]);
            typeMonthData[month] += receiveAmount;
            typeMonthData[12] += receiveAmount;

            if (receiveYear.equals(year)) {
                Map<String, double[]> regionMap = sysProjectStatistics.getReceiveByTypeAndRegion().get(projectType);
                double[] regionData = regionMap.computeIfAbsent(detailDto.getProjectRegion(), k -> new double[13]);
                regionData[month] += receiveAmount;
                regionData[12] += receiveAmount;

                Map<String, double[]> personMap = sysProjectStatistics.getReceiveByTypeAndPerson().get(projectType);
                String personName = sysProjectPersonDtoMap.get(detailDto.getSalesPerson()).getName();
                double[] personData = personMap.computeIfAbsent(personName, k -> new double[13]);
                personData[month] += receiveAmount;
                personData[12] += receiveAmount;

            }
        }
    }

    public SysReceiveStatistics getSysReceiveStatistics(boolean shouldReceive) {
        SysReceiveStatistics sysReceiveStatistics = new SysReceiveStatistics();
        SysProjectDetailQueryCriteria queryCriteria = new SysProjectDetailQueryCriteria();
        queryCriteria.setCreateTime(List.of(
                Timestamp.valueOf(LocalDateTime.of(2023, 1, 1, 0, 0, 0, 0)),
                Timestamp.valueOf(LocalDateTime.now())));
        List<SysProjectDetailDto> sysProjectDetailDtoList = sysProjectDetailService.queryAll(queryCriteria);

        sysReceiveStatistics.setReceiveAmountRows(buildReceiveAmountRowList(sysProjectDetailDtoList, shouldReceive));
        sysReceiveStatistics.setReceiveRateRows(buildReceiveRateRowList(sysProjectDetailDtoList, shouldReceive));
        return sysReceiveStatistics;
    }

    @NotNull
    private List<SysReceiveStatistics.ReceiveAmountRow> buildReceiveAmountRowList(List<SysProjectDetailDto> sysProjectDetailDtoList, boolean shouldReceive) {
        List<SysProjectDetailDto> sysProjectDetailDtoListShouldReceive = shouldReceive ?
                sysProjectDetailDtoList.stream().filter(e -> e.getShouldReceiveAmount() != null && e.getShouldReceiveAmount() > 0).collect(Collectors.toList()) :
                sysProjectDetailDtoList;
        Map<Long, Map<Integer, Long>> shouldReceiveByNameAndType = new HashMap<>();
        Map<Long, SysProjectPersonDto> sysProjectPersonDtoMap = sysProjectPersonService.getIdToPersonMap();
        for (SysProjectDetailDto dto : sysProjectDetailDtoListShouldReceive) {
            Map<Integer, Long> shouldReceiveByType = shouldReceiveByNameAndType.computeIfAbsent(dto.getSalesPerson(), k -> new HashMap<>());
            long currentAmount = shouldReceiveByType.computeIfAbsent(dto.getProjectType(), k -> 0L);
            long totalAmount = shouldReceiveByType.computeIfAbsent(-1, k -> 0L);
            long toAdd = shouldReceive ?
                    Optional.ofNullable(dto.getShouldReceiveAmount()).orElse(0L) :
                    dto.getContractAmount() - Optional.ofNullable(dto.getReceiveAmount()).orElse(0L);
            shouldReceiveByType.put(dto.getProjectType(), currentAmount + Math.max(toAdd, 0));
            shouldReceiveByType.put(-1, totalAmount + Math.max(toAdd, 0));
        }

        List<SysReceiveStatistics.ReceiveAmountRow> receiveAmountRows = new ArrayList<>();
        shouldReceiveByNameAndType.forEach((key, value) -> {
            SysReceiveStatistics.ReceiveAmountRow row = new SysReceiveStatistics.ReceiveAmountRow();
            String name = sysProjectPersonDtoMap.get(key).getName();
            row.setName(name);
            row.setExam(ProjectUtils.dbPriceToRealPriceString(value.getOrDefault(ProjectUtils.PROJECT_TYPE_EXAM, 0L)));
            row.setSupervise(ProjectUtils.dbPriceToRealPriceString(value.getOrDefault(ProjectUtils.PROJECT_TYPE_SUPERVISE, 0L)));
            row.setDesign(ProjectUtils.dbPriceToRealPriceString(value.getOrDefault(ProjectUtils.PROJECT_TYPE_DESIGN, 0L)));
            row.setOther(ProjectUtils.dbPriceToRealPriceString(value.getOrDefault(ProjectUtils.PROJECT_TYPE_OTHER, 0L)));
            row.setSum(ProjectUtils.dbPriceToRealPriceString(value.getOrDefault(-1, 0L)));
            receiveAmountRows.add(row);
        });
        receiveAmountRows.sort(Comparator.comparingDouble(o -> -Double.parseDouble(o.getSum())));
        return receiveAmountRows;
    }

    private List<SysReceiveStatistics.ReceiveRateRow> buildReceiveRateRowList(List<SysProjectDetailDto> sysProjectDetailDtoList, boolean shouldReceive) {
        Map<Long, Map<Integer, long[]>> personAndYearMap = new HashMap<>();
        for (SysProjectDetailDto detailDto : sysProjectDetailDtoList) {
            Map<Integer, long[]> yearMap = personAndYearMap.computeIfAbsent(detailDto.getSalesPerson(), k -> new HashMap<>());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(detailDto.getCreateTime().getTime());
            int year = calendar.get(Calendar.YEAR);
            long[] amountArray = yearMap.computeIfAbsent(year, k -> new long[2]);
            long[] amountTotalArray = yearMap.computeIfAbsent(-1, k -> new long[2]);
            long receiveAmount = Optional.ofNullable(detailDto.getReceiveAmount()).orElse(0L);
            long shouldReceiveAmount = Optional.ofNullable(detailDto.getShouldReceiveAmount()).orElse(0L);
            amountArray[0] += receiveAmount;
            amountArray[1] += shouldReceive ? receiveAmount + shouldReceiveAmount : Math.max(receiveAmount, detailDto.getContractAmount());
            amountTotalArray[0] += receiveAmount;
            amountTotalArray[1] += shouldReceive ? receiveAmount + shouldReceiveAmount : Math.max(receiveAmount, detailDto.getContractAmount());
        }
        List<SysReceiveStatistics.ReceiveRateRow> receiveRateRowList = new ArrayList<>();
        Map<Long, SysProjectPersonDto> sysProjectPersonDtoMap = sysProjectPersonService.getIdToPersonMap();
        personAndYearMap.forEach((personId, yearMap) -> {
            SysReceiveStatistics.ReceiveRateRow receiveRateRow = new SysReceiveStatistics.ReceiveRateRow();
            SysProjectPersonDto personDto = sysProjectPersonDtoMap.get(personId);
            receiveRateRow.setName(personDto.getName());
            receiveRateRow.setRate_2023(calcRate(yearMap.getOrDefault(2023, new long[]{0, 0})));
            receiveRateRow.setRate_2024(calcRate(yearMap.getOrDefault(2024, new long[]{0, 0})));
            receiveRateRow.setRate_2025(calcRate(yearMap.getOrDefault(2025, new long[]{0, 0})));
            receiveRateRow.setRate(calcRate(yearMap.getOrDefault(-1, new long[]{0, 0})));
            receiveRateRowList.add(receiveRateRow);
        });
        receiveRateRowList.sort(Comparator.comparingDouble(o -> "/".equals(o.getRate()) ? -1d : Double.parseDouble(o.getRate())));
        return receiveRateRowList;
    }

    private static String calcRate(long[] amountArray) {
        if (amountArray[1] == 0) {
            return "/";
        }
        return String.format("%.2f", amountArray[0] * 1.0 / amountArray[1] * 100);
    }

    public BalanceData getBalanceData(long endTime) {
        BalanceData balanceData = new BalanceData();
        SysProjectReceiveQueryCriteria criteria = new SysProjectReceiveQueryCriteria();
        List<Timestamp> receiveTimeList = new ArrayList<>();
        receiveTimeList.add(new Timestamp(CalendarUtils.getBeginningOfYear().getTimeInMillis()));
        receiveTimeList.add(new Timestamp(endTime));
        criteria.setReceiveTime(receiveTimeList);
        criteria.setReceiveAmount(0);
        List<SysProjectReceiveDto> sysProjectReceiveDtoList = sysProjectReceiveService.queryAll(criteria);
        Map<Long, SysProjectDetailDto> detailMap = new HashMap<>();
        balanceData.setDepartmentRows(buildDepartmentRows(sysProjectReceiveDtoList, detailMap, endTime));
        balanceData.setPersonRows(buildPersonRows(sysProjectReceiveDtoList, detailMap, endTime));
        return balanceData;
    }


    private List<BalanceTableRow> buildDepartmentRows(List<SysProjectReceiveDto> receiveDtoList, Map<Long, SysProjectDetailDto> detailMap, long end) {
        List<ProjectDepartment> departmentList = ProjectUtils.PROJECT_DEPARTMENT_LIST.stream().map(ProjectDepartment::new).collect(Collectors.toList());
        long beginningOfMonth = CalendarUtils.getBeginningOfMonth(end).getTimeInMillis();
        for (SysProjectReceiveDto receiveDto : receiveDtoList) {
            SysProjectDetailDto detail = detailMap.computeIfAbsent(receiveDto.getProjectId(), sysProjectDetailService::findById);
            int projectType = detail.getProjectType();
            long receiveAmount = receiveDto.getReceiveAmount();
            String region = detail.getProjectRegion();

            for (ProjectDepartment department : departmentList) {
                int percent = department.getPercentageGetter().apply(detail);
                long amount = receiveAmount * percent / 100;
                long[] amounts = department.getReceiveByType().computeIfAbsent(projectType, k -> new long[]{0, 0, 0});
                Map<String, long[]> regionMap = department.getReceiveByTypeAndRegion().computeIfAbsent(projectType, k -> new HashMap<>());
                long[] regionAmounts = regionMap.computeIfAbsent(region, k -> new long[]{0, 0, 0});
                if (receiveDto.getReceiveTime().getTime() < beginningOfMonth) {
                    amounts[0] += amount;
                    regionAmounts[0] += amount;
                } else {
                    amounts[1] += amount;
                    regionAmounts[1] += amount;
                }
                amounts[2] += amount;
                regionAmounts[2] += amount;
            }
        }
        List<BalanceTableRow> departmentRows = new ArrayList<>();
        int month = CalendarUtils.getBeginningOfMonth(end).get(Calendar.MONTH) + 1;
        departmentList.forEach(department -> {
            BalanceTableRow row = departmentToRow(department, null, month);
            departmentRows.add(row);
        });
        return departmentRows;
    }

    private BalanceTableRow departmentToRow(ProjectDepartment department, ProjectDepartment parent, int month) {
        if (department.getDataCalculator() != null && parent != null) {
            department.setReceiveByTypeAndRegion(parent.getReceiveByTypeAndRegion());
            department.setReceiveByType(department.getDataCalculator().apply(parent));
        }
        AccountBalanceData expense = kingdeeService.getAccountBalanceByNumberList(department.getAccountNumberList(), month);
        expense.setInitialBalance(department.getInitialBalance());
        BalanceTableRow row = typeMapToRow(department.getReceiveByType(), expense);
        row.setName(department.getName());
        if (CollectionUtils.isNotEmpty(department.getChildren())) {
            List<BalanceTableRow> childrenList = new ArrayList<>();
            for (ProjectDepartment children : department.getChildren()) {
                childrenList.add(departmentToRow(children, department, month));
            }
            childrenList.sort(BalanceTableRow.COMPARATOR_DESC);
            row.setChildren(childrenList);
        }
        return row;
    }

    private List<BalanceTableRow> buildPersonRows(List<SysProjectReceiveDto> receiveDtoList, Map<Long, SysProjectDetailDto> detailMap, long end) {
        Map<Long, Map<Integer, long[]>> receiveByPersonAndType = new HashMap<>();
        for (SysProjectReceiveDto receiveDto : receiveDtoList) {
            SysProjectDetailDto detail = detailMap.computeIfAbsent(receiveDto.getProjectId(), sysProjectDetailService::findById);
            int projectType = detail.getProjectType();
            long receiveAmount = receiveDto.getReceiveAmount();
            //用long防止乘后int溢出
            long salesAmount = (long) receiveAmount * detail.getSalesPercent() / 100;
            Map<Integer, long[]> typeMap = receiveByPersonAndType.computeIfAbsent(detail.getSalesPerson(), k -> new HashMap<>());
            long[] amounts = typeMap.computeIfAbsent(projectType, k -> new long[]{0, 0, 0});
            if (receiveDto.getReceiveTime().getTime() < CalendarUtils.getBeginningOfMonth(end).getTimeInMillis()) {
                amounts[0] += salesAmount;
            } else {
                amounts[1] += salesAmount;
            }
            amounts[2] += salesAmount;
        }
        List<BalanceTableRow> departmentRows = new ArrayList<>();
        Map<Long, SysProjectPersonDto> personMap = sysProjectPersonService.getIdToPersonMap();
//        personMap.forEach((k, v) -> {
//            if (v.getInitialBalance() != null ||
//                    StringUtils.isNotEmpty(v.getAccountNumber()) ||
//                    StringUtils.isNotEmpty(v.getReserveFundNumber())) {
//                receiveByPersonAndType.putIfAbsent(k, new HashMap<>());
//            }
//        });
//        Map<Long, Long> remainingByPerson = sysProjectDetailService.getPersonRemainingMap();
//        receiveByPersonAndType.forEach((k, v) -> {
//            AccountBalanceData data = new AccountBalanceData();
//            SysProjectPersonDto personDto = personMap.get(k);
//            if (StringUtils.isNotEmpty(personDto.getReserveFundNumber()) || StringUtils.isNotEmpty(personDto.getAccountNumber())) {
//                int month = CalendarUtils.getBeginningOfMonth(end).get(Calendar.MONTH) + 1;
//                AccountBalanceData accountData = kingdeeService.getAccountBalanceByNumber(personDto.getAccountNumber(), month);
//                AccountBalanceData reserveFundData = kingdeeService.getAccountBalanceByNumber(personDto.getReserveFundNumber(), month);
//                data = AccountBalanceData.add(accountData, reserveFundData);
//            }
//            Optional.ofNullable(personDto.getInitialBalance()).ifPresent(data::setInitialBalance);
//            BalanceTableRow row = typeMapToRow(v, data);
//            row.setName(personMap.get(k).getName());
//            row.setRemaining(ProjectUtils.dbPriceToRealPriceString(remainingByPerson.getOrDefault(k, 0L)));
//            departmentRows.add(row);
//        });
        departmentRows.sort(BalanceTableRow.COMPARATOR_DESC);
        return departmentRows;
    }

    private BalanceTableRow typeMapToRow(Map<Integer, long[]> typeMap, AccountBalanceData expenseData) {
        BalanceTableRow row = new BalanceTableRow();
        row.setExamLast(String.format("%.2f", ProjectUtils.dbPriceToRealPrice(typeMap.getOrDefault(ProjectUtils.PROJECT_TYPE_EXAM, new long[]{0, 0, 0})[0])));
        row.setExamThisMonth(String.format("%.2f", ProjectUtils.dbPriceToRealPrice(typeMap.getOrDefault(ProjectUtils.PROJECT_TYPE_EXAM, new long[]{0, 0, 0})[1])));
        row.setExamThisYear(String.format("%.2f", ProjectUtils.dbPriceToRealPrice(typeMap.getOrDefault(ProjectUtils.PROJECT_TYPE_EXAM, new long[]{0, 0, 0})[2])));
        row.setSuperviseLast(String.format("%.2f", ProjectUtils.dbPriceToRealPrice(typeMap.getOrDefault(ProjectUtils.PROJECT_TYPE_SUPERVISE, new long[]{0, 0, 0})[0])));
        row.setSuperviseThisMonth(String.format("%.2f", ProjectUtils.dbPriceToRealPrice(typeMap.getOrDefault(ProjectUtils.PROJECT_TYPE_SUPERVISE, new long[]{0, 0, 0})[1])));
        row.setSuperviseThisYear(String.format("%.2f", ProjectUtils.dbPriceToRealPrice(typeMap.getOrDefault(ProjectUtils.PROJECT_TYPE_SUPERVISE, new long[]{0, 0, 0})[2])));
        row.setDesignLast(String.format("%.2f", ProjectUtils.dbPriceToRealPrice(typeMap.getOrDefault(ProjectUtils.PROJECT_TYPE_DESIGN, new long[]{0, 0, 0})[0])));
        row.setDesignThisMonth(String.format("%.2f", ProjectUtils.dbPriceToRealPrice(typeMap.getOrDefault(ProjectUtils.PROJECT_TYPE_DESIGN, new long[]{0, 0, 0})[1])));
        row.setDesignThisYear(String.format("%.2f", ProjectUtils.dbPriceToRealPrice(typeMap.getOrDefault(ProjectUtils.PROJECT_TYPE_DESIGN, new long[]{0, 0, 0})[2])));
        row.setOtherLast(String.format("%.2f", ProjectUtils.dbPriceToRealPrice(typeMap.getOrDefault(ProjectUtils.PROJECT_TYPE_OTHER, new long[]{0, 0, 0})[0])));
        row.setOtherThisMonth(String.format("%.2f", ProjectUtils.dbPriceToRealPrice(typeMap.getOrDefault(ProjectUtils.PROJECT_TYPE_OTHER, new long[]{0, 0, 0})[1])));
        row.setOtherThisYear(String.format("%.2f", ProjectUtils.dbPriceToRealPrice(typeMap.getOrDefault(ProjectUtils.PROJECT_TYPE_OTHER, new long[]{0, 0, 0})[2])));
        row.setExpenseLast(String.format("%.2f", ProjectUtils.dbPriceToRealPrice(expenseData.getExpenseLast())));
        row.setExpenseThisMonth(String.format("%.2f", ProjectUtils.dbPriceToRealPrice(expenseData.getExpenseThisMonth())));
        row.setExpenseThisYear(String.format("%.2f", ProjectUtils.dbPriceToRealPrice(expenseData.getExpenseThisYear())));
        long sum0 = typeMap.values().stream().mapToLong(val -> val[0]).sum() - expenseData.getExpenseLast() + expenseData.getInitialBalance();
        long sum1 = typeMap.values().stream().mapToLong(val -> val[1]).sum() - expenseData.getExpenseThisMonth();
        long sum2 = typeMap.values().stream().mapToLong(val -> val[2]).sum() - expenseData.getExpenseThisYear() + expenseData.getInitialBalance();
        row.setSumLast(String.format("%.2f", ProjectUtils.dbPriceToRealPrice(sum0)));
        row.setSumThisMonth(String.format("%.2f", ProjectUtils.dbPriceToRealPrice(sum1)));
        row.setSumThisYear(String.format("%.2f", ProjectUtils.dbPriceToRealPrice(sum2)));
        row.setInitialBalance(String.format("%.2f", ProjectUtils.dbPriceToRealPrice(expenseData.getInitialBalance())));
        return row;
    }

    public Map<Long, Long> getBalancePersonMap() {
        Map<Long, Long> personBalanceMap = new HashMap<>();
        SysProjectReceiveQueryCriteria criteria = new SysProjectReceiveQueryCriteria();
        criteria.setReceiveTime(List.of(new Timestamp(CalendarUtils.getBeginningOfYear().getTimeInMillis()), new Timestamp(System.currentTimeMillis())));
        List<SysProjectReceiveDto> receiveDtoList = sysProjectReceiveService.queryAll(criteria);
        Map<Long, SysProjectDetailDto> detailMap = new HashMap<>();
        for (SysProjectReceiveDto receiveDto : receiveDtoList) {
            SysProjectDetailDto detail = detailMap.computeIfAbsent(receiveDto.getProjectId(), sysProjectDetailService::findById);
            long receiveAmount = receiveDto.getReceiveAmount();
            long salesAmount = receiveAmount * detail.getSalesPercent() / 100;
            long person = detail.getSalesPerson();
            personBalanceMap.put(person, personBalanceMap.getOrDefault(person, 0L) + salesAmount);
        }
//        Map<Long, SysProjectPersonDto> personDtoMap = sysProjectPersonService.getIdToPersonMap();
//        for (Map.Entry<Long, Long> entry : personBalanceMap.entrySet()) {
//            SysProjectPersonDto personDto = personDtoMap.get(entry.getKey());
//            AccountBalanceData data = kingdeeService.getAccountBalanceByNumberList(List.of(
//                    Optional.ofNullable(personDto.getAccountNumber()).orElse(""),
//                    Optional.ofNullable(personDto.getReserveFundNumber()).orElse("")), Calendar.getInstance().get(Calendar.MONTH) + 1);
//            entry.setValue(entry.getValue() - data.getExpenseThisYear() + Optional.ofNullable(personDto.getInitialBalance()).orElse(0));
//        }
        return personBalanceMap;
    }

    public List<InvoiceTableRow> getInvoiceData(long endTime) {
        List<InvoiceTableRow> invoiceTableRowList = new ArrayList<>();
        SysProjectReceiveQueryCriteria receiveQueryCriteria = new SysProjectReceiveQueryCriteria();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(endTime);
        calendar.add(Calendar.YEAR, -1);
        receiveQueryCriteria.setInvoiceTime(List.of(new Timestamp(calendar.getTimeInMillis()), new Timestamp(endTime)));
        receiveQueryCriteria.setInvoiceAmount(0);
        List<SysProjectReceiveDto> receiveDtoList = sysProjectReceiveService.queryAll(receiveQueryCriteria);
        Map<Long, SysProjectDetailDto> sysProjectDetailDtoMap = sysProjectDetailService.getSysProjectDetailDtoMap();
        Map<String, long[]> invoiceByCompanyMap = new TreeMap<>();

        for (SysProjectReceiveDto receiveDto : receiveDtoList) {
            SysProjectDetailDto projectDetailDto = sysProjectDetailDtoMap.get(receiveDto.getProjectId());
            String company = projectDetailDto.getPartyB();
            long[] data = invoiceByCompanyMap.computeIfAbsent(company, k -> new long[]{0, 0});
            data[0] += receiveDto.getInvoiceAmount();

        }
        sysProjectDetailDtoMap.forEach((k, v) -> {
            String company = v.getPartyB();
            long[] data = invoiceByCompanyMap.computeIfAbsent(company, k1 -> new long[]{0, 0});
            if (v.getInvoiceType() != ProjectUtils.INVOICE_TYPE_NONE) {
                data[1] += Optional.ofNullable(v.getShouldReceiveAmount()).orElse(0L);
            }
        });

        invoiceByCompanyMap.forEach((k, v) -> {
            InvoiceTableRow row = new InvoiceTableRow();
            row.setName(k);
            row.setInvoiced(ProjectUtils.dbPriceToRealPriceString(v[0]));
            row.setToInvoice(ProjectUtils.dbPriceToRealPriceString(v[1]));
            if (ProjectUtils.GENERAL_TAXPAYERS.contains(k)) {
                row.setRemaining("");
            } else {
                row.setRemaining(ProjectUtils.dbPriceToRealPriceString(ProjectUtils.MAX_INVOICE_AMOUNT - v[0]));
            }
            invoiceTableRowList.add(row);
        });
        return invoiceTableRowList;
    }

    public InvoicedNotReceiveData getInvoicedNotReceiveData(SysProjectDetailQueryCriteria criteria, Pageable pageable) {
        InvoicedNotReceiveData res = new InvoicedNotReceiveData();
        List<SysProjectReceiveDto> receiveDtoList = sysProjectReceiveService.queryInvoicedNotReceive();
        Map<Long, SysProjectDetailDto> sysProjectDetailDtoMap = sysProjectDetailService.getSysProjectDetailDtoMap();
        Map<Long, SysProjectPersonDto> sysProjectPersonDtoMap = sysProjectPersonService.getIdToPersonMap();
        Map<String, Long> personMap = new HashMap<>();
        for (SysProjectReceiveDto receiveDto : receiveDtoList) {
            long amount = receiveDto.getInvoiceAmount() - receiveDto.getReceiveAmount();
            long projectId = receiveDto.getProjectId();
            String name = sysProjectPersonDtoMap.get(sysProjectDetailDtoMap.get(projectId).getSalesPerson()).getName();
            personMap.put(name, personMap.getOrDefault(name, 0L) + amount);
        }
        List<InvoicedNotReceiveData.Row> rows = new ArrayList<>();
        personMap.forEach((k, v) -> {
            InvoicedNotReceiveData.Row row = new InvoicedNotReceiveData.Row();
            row.setName(k);
            row.setAmount(ProjectUtils.dbPriceToRealPriceString(v));
            rows.add(row);
        });
        rows.sort(COMPARATOR.reversed());
        res.setRows(rows);
        return res;
    }

    public void downloadSysProjectBalance(int month, HttpServletResponse response) throws IOException {
        long timeInMillis = CalendarUtils.getEndOfMonth(month).getTimeInMillis();
        BalanceData balanceData = getBalanceData(timeInMillis);
        Map<String, List<Map<String, Object>>> excelMap = new LinkedHashMap<>();
        excelMap.put("部门余额统计", buildExcelList(balanceData.getDepartmentRowsWithChildren(), "部门名称"));
        excelMap.put("业务人余额统计", buildExcelList(balanceData.getPersonRows(), "业务人"));
        excelMap.put("收款明细", buildDetailExcelMap(timeInMillis));
        FileUtil.downloadExcel(excelMap, response);
    }

    private List<Map<String, Object>> buildDetailExcelMap(long timeInMillis) {
        List<Map<String, Object>> res = new ArrayList<>();
        SysProjectReceiveQueryCriteria receiveQueryCriteria = new SysProjectReceiveQueryCriteria();
        receiveQueryCriteria.setReceiveTime(List.of(
                new Timestamp(CalendarUtils.getBeginningOfMonth(timeInMillis).getTimeInMillis()),
                new Timestamp(timeInMillis)));
        receiveQueryCriteria.setReceiveAmount(0);
        List<SysProjectReceiveDto> receiveDtoList = sysProjectReceiveService.queryAll(receiveQueryCriteria);
        Map<Long, SysProjectDetailDto> sysProjectDetailDtoMap = sysProjectDetailService.getSysProjectDetailDtoMap();
        Map<Long, List<SysProjectReceiveDto>> receiveDtoMapByProjectId = receiveDtoList.stream().collect(Collectors.groupingBy(SysProjectReceiveDto::getProjectId));
        Map<Long, SysProjectPersonDto> personDtoMap = sysProjectPersonService.getIdToPersonMap();
        receiveDtoMapByProjectId.forEach((k, v) -> {
            SysProjectDetailDto projectDetailDto = sysProjectDetailDtoMap.get(k);
            Map<String, Object> o = new LinkedHashMap<>();
            o.put("项目类型", ProjectUtils.projectTypeToName(projectDetailDto.getProjectType()));
            o.put("项目名", projectDetailDto.getProjectName());
            o.put("项目编号", projectDetailDto.getContractNumber());
            o.put("项目区域", projectDetailDto.getProjectRegion());
            o.put("业务人", personDtoMap.get(projectDetailDto.getSalesPerson()).getName());
            o.put("合同金额", ProjectUtils.dbPriceToRealPrice(projectDetailDto.getContractAmount()));
            o.put("签订时间", projectDetailDto.getContractTime());
            o.put("乙方名称", projectDetailDto.getPartyB());
            long receiveAmount = 0, invoiceAmount = 0;
            Timestamp latestReceiveTime = new Timestamp(0);
            for (SysProjectReceiveDto receiveDto : v) {
                invoiceAmount += receiveDto.getInvoiceAmount();
                receiveAmount += receiveDto.getReceiveAmount();
                if (receiveDto.getReceiveTime().after(latestReceiveTime)) {
                    latestReceiveTime = receiveDto.getReceiveTime();
                }
            }
            o.put("开票总金额", ProjectUtils.dbPriceToRealPrice(invoiceAmount));
            o.put("收款总金额", ProjectUtils.dbPriceToRealPrice(projectDetailDto.getReceiveAmount()));
            o.put("项目进度", projectDetailDto.getProjectProgress() + "%");
            o.put("最新收款时间", latestReceiveTime);
            o.put("本月收款金额", ProjectUtils.dbPriceToRealPrice(receiveAmount));
            o.put("业务中心应计金额", ProjectUtils.dbPriceToRealPrice(projectDetailDto.getSalesPercent() * receiveAmount / 100));
            if (projectDetailDto.getProjectType() == ProjectUtils.PROJECT_TYPE_SUPERVISE) {
                o.put("技术中心应计金额", ProjectUtils.dbPriceToRealPrice(projectDetailDto.getTechnicalPercent() * receiveAmount / 100 * 4 / 30));
                o.put("技术地区应计金额", ProjectUtils.dbPriceToRealPrice(projectDetailDto.getTechnicalPercent() * receiveAmount / 100 * 26 / 30));
            } else {
                o.put("技术中心应计金额", ProjectUtils.dbPriceToRealPrice(projectDetailDto.getTechnicalPercent() * receiveAmount / 100));
                o.put("技术地区应计金额", 0);
            }
            o.put("管理中心应计金额", ProjectUtils.dbPriceToRealPrice(projectDetailDto.getManagementPercent() * receiveAmount / 100));
            o.put("总裁办应计金额", ProjectUtils.dbPriceToRealPrice(projectDetailDto.getPresidentPercent() * receiveAmount / 100));
            o.put("备注", projectDetailDto.getRemark());
            res.add(o);
        });
        return res;
    }

    private List<Map<String, Object>> buildExcelList(List<BalanceTableRow> balanceTableRowList, String firstColumnName) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (BalanceTableRow row : balanceTableRowList) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put(firstColumnName, row.getName());
            map.put("上年结转", row.getInitialBalance());
            map.put("上月累计支出", row.getExpenseLast());
            map.put("本月支出", row.getExpenseThisMonth());
            map.put("本年累计支出", row.getExpenseThisYear());
            map.put("上月检测累计收入", row.getExamLast());
            map.put("本月检测收入", row.getExamThisMonth());
            map.put("本年检测累计收入", row.getExamThisYear());
            map.put("上月监理累计收入", row.getSuperviseLast());
            map.put("本月监理收入", row.getSuperviseThisMonth());
            map.put("本年监理累计收入", row.getSuperviseThisYear());
            map.put("上月设计累计收入", row.getDesignLast());
            map.put("本月设计收入", row.getDesignThisMonth());
            map.put("本年设计累计收入", row.getDesignThisYear());
            map.put("上月其他累计收入", row.getOtherLast());
            map.put("本月其他收入", row.getOtherThisMonth());
            map.put("本年其他累计收入", row.getOtherThisYear());
            map.put("上月累计余额", row.getSumLast());
            map.put("本月余额", row.getSumThisMonth());
            map.put("本年累计余额", row.getSumThisYear());
            list.add(map);
        }
        return list;
    }

}
