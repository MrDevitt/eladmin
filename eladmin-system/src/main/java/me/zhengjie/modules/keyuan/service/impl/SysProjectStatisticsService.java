package me.zhengjie.modules.keyuan.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.modules.keyuan.domain.statistics.InvoiceTableRow;
import me.zhengjie.modules.keyuan.domain.statistics.SysProjectStatistics;
import me.zhengjie.modules.keyuan.domain.statistics.SysShouldReceiveData;
import me.zhengjie.modules.keyuan.domain.statistics.balance.AccountBalanceData;
import me.zhengjie.modules.keyuan.domain.statistics.balance.BalanceData;
import me.zhengjie.modules.keyuan.domain.statistics.balance.BalanceTableRow;
import me.zhengjie.modules.keyuan.domain.statistics.balance.ProjectDepartment;
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
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysProjectStatisticsService {

    private final SysProjectDetailService sysProjectDetailService;

    private final SysProjectReceiveService sysProjectReceiveService;

    private final SysProjectPersonService sysProjectPersonService;

    private final KingdeeService kingdeeService;

    public SysProjectStatistics getSysProjectStatisticsInfo() {
        if (SysProjectStatistics.CACHE == null) {
            SysProjectStatistics sysProjectStatistics = new SysProjectStatistics();
            List<SysProjectDetailDto> sysProjectDetailDtoList = sysProjectDetailService.queryAll(new SysProjectDetailQueryCriteria());
            Map<Long, SysProjectPersonDto> sysProjectPersonDtoMap = sysProjectPersonService.getIdToPersonMap();
            buildContractStatistics(sysProjectStatistics, sysProjectDetailDtoList, sysProjectPersonDtoMap);

            Map<Long, SysProjectDetailDto> sysProjectDetailDtoMap = sysProjectDetailDtoList.stream().collect(Collectors.toMap(
                    SysProjectDetailDto::getId,
                    Function.identity(),
                    (x, y) -> x
            ));
            List<SysProjectReceiveDto> sysProjectReceiveDtoList = sysProjectReceiveService.queryAll(new SysProjectReceiveQueryCriteria());
            buildReceiveStatistics(sysProjectStatistics, sysProjectReceiveDtoList, sysProjectDetailDtoMap, sysProjectPersonDtoMap);

            sysProjectStatistics.calcInnerData();
            SysProjectStatistics.CACHE = sysProjectStatistics;
        }
        return SysProjectStatistics.CACHE;
    }

    private static void buildContractStatistics(
            SysProjectStatistics sysProjectStatistics,
            List<SysProjectDetailDto> sysProjectDetailDtoList,
            Map<Long, SysProjectPersonDto> sysProjectPersonDtoMap) {
        String currentYear = getLastMonthYear();
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

            if (currentYear.equals(year)) {
                Map<String, double[]> regionMap = sysProjectStatistics.getContractByTypeAndRegion().get(projectType);
                double[] regionData = regionMap.computeIfAbsent(detailDto.getProjectRegion(), k -> new double[13]);
                regionData[month] += contractAmount;
                regionData[12] += contractAmount;

                String personName = sysProjectPersonDtoMap.get(detailDto.getSalesPerson()).getName();
                Map<String, double[]> personMap = sysProjectStatistics.getContractByTypeAndPerson().get(projectType);
                double[] personData = personMap.computeIfAbsent(personName, k -> new double[13]);
                personData[month] += contractAmount;
                personData[12] += contractAmount;

                Map<String, double[]> departmentMap = sysProjectStatistics.getContractByTypeAndDepartment().get(projectType);
                fillDepartmentMap(departmentMap, detailDto, month, contractAmount);

                Map<String, double[]> personShareMap = sysProjectStatistics.getContractShareByTypeAndPerson().get(projectType);
                double[] personShareData = personShareMap.computeIfAbsent(personName, k -> new double[13]);
                personShareData[month] += contractAmount * detailDto.getSalesPercent() / 100;
                personShareData[12] += contractAmount * detailDto.getSalesPercent() / 100;

                if (detailDto.getProjectType() == ProjectUtils.PROJECT_TYPE_EXAM) {
                    Map<String, double[]> examRegionMap = sysProjectStatistics.getExamContractByRegionAndPerson().get(detailDto.getProjectRegion());
                    double[] examPersonData = examRegionMap.computeIfAbsent(personName, k -> new double[13]);
                    examPersonData[month] += contractAmount;
                    examPersonData[12] += contractAmount;
                }
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
            Map<Long, SysProjectPersonDto> sysProjectPersonDtoMap) {
        String currentYear = getLastMonthYear();
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

            if (currentYear.equals(year)) {
                Map<String, double[]> regionMap = sysProjectStatistics.getReceiveByTypeAndRegion().get(projectType);
                double[] regionData = regionMap.computeIfAbsent(detailDto.getProjectRegion(), k -> new double[13]);
                regionData[month] += receiveAmount;
                regionData[12] += receiveAmount;

                Map<String, double[]> personMap = sysProjectStatistics.getReceiveByTypeAndPerson().get(projectType);
                String personName = sysProjectPersonDtoMap.get(detailDto.getSalesPerson()).getName();
                double[] personData = personMap.computeIfAbsent(personName, k -> new double[13]);
                personData[month] += receiveAmount;
                personData[12] += receiveAmount;

                Map<String, double[]> departmentMap = sysProjectStatistics.getReceiveByTypeAndDepartment().get(projectType);
                fillDepartmentMap(departmentMap, detailDto, month, receiveAmount);

                Map<String, double[]> personShareMap = sysProjectStatistics.getReceiveShareByTypeAndPerson().get(projectType);
                double[] personShareData = personShareMap.computeIfAbsent(personName, k -> new double[13]);
                personShareData[month] += receiveAmount * detailDto.getSalesPercent() / 100;
                personShareData[12] += receiveAmount * detailDto.getSalesPercent() / 100;

                if (detailDto.getProjectType() == ProjectUtils.PROJECT_TYPE_EXAM) {
                    Map<String, double[]> examRegionMap = sysProjectStatistics.getExamReceiveByRegionAndPerson().get(detailDto.getProjectRegion());
                    double[] examPersonData = examRegionMap.computeIfAbsent(personName, k -> new double[13]);
                    examPersonData[month] += receiveAmount;
                    examPersonData[12] += receiveAmount;
                }
            }
        }
    }

    private static String getLastMonthYear() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        return String.valueOf(calendar.get(Calendar.YEAR));
    }

    public SysShouldReceiveData getSysShouldReceiveData() {
        SysShouldReceiveData sysShouldReceiveData = new SysShouldReceiveData();
        SysProjectDetailQueryCriteria queryCriteria = new SysProjectDetailQueryCriteria();
        queryCriteria.setShouldReceiveAmount(0);
        List<SysProjectDetailDto> sysProjectDetailDtoList = sysProjectDetailService.queryAll(queryCriteria);

        Map<Long, Map<String, Double>> shouldReceiveByNameAndType = new HashMap<>();
        Map<Long, SysProjectPersonDto> sysProjectPersonDtoMap = sysProjectPersonService.getIdToPersonMap();
        for (SysProjectDetailDto sysProjectDetailDto : sysProjectDetailDtoList) {
            String type = ProjectUtils.projectTypeToName(sysProjectDetailDto.getProjectType());
            SysProjectPersonDto personDto = sysProjectPersonDtoMap.get(sysProjectDetailDto.getSalesPerson());
            Map<String, Double> shouldReceiveByType = shouldReceiveByNameAndType.computeIfAbsent(personDto.getId(), k -> new HashMap<>());
            double currentAmount = shouldReceiveByType.computeIfAbsent(type, k -> 0d);
            double totalAmount = shouldReceiveByType.computeIfAbsent("sum", k -> 0d);
            shouldReceiveByType.put(type, currentAmount + ProjectUtils.dbPriceToRealPrice(sysProjectDetailDto.getShouldReceiveAmount()));
            shouldReceiveByType.put("sum", totalAmount + ProjectUtils.dbPriceToRealPrice(sysProjectDetailDto.getShouldReceiveAmount()));
        }

        List<Map<String, String>> tableData = new ArrayList<>();
        shouldReceiveByNameAndType.forEach((key, value) -> {
            Map<String, String> data = new HashMap<>();
            String name = sysProjectPersonDtoMap.get(key).getName();
            data.put("name", name);
            value.forEach((key1, value1) -> data.put(key1, String.format("%.2f", value1)));
            tableData.add(data);
        });
        tableData.sort(Comparator.comparing(a -> -Integer.parseInt(a.get("sum").substring(0, a.get("sum").length() - 3))));
        sysShouldReceiveData.setTableData(tableData);
        return sysShouldReceiveData;
    }


    public BalanceData getBalanceData(long endTime) {
        BalanceData balanceData = new BalanceData();
        SysProjectReceiveQueryCriteria criteria = new SysProjectReceiveQueryCriteria();
        List<Timestamp> receiveTimeList = new ArrayList<>();
        receiveTimeList.add(new Timestamp(CalendarUtils.getBeginningOfYear().getTimeInMillis()));
        receiveTimeList.add(new Timestamp(endTime));
        criteria.setReceiveTime(receiveTimeList);
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
            int receiveAmount = receiveDto.getReceiveAmount();
            String region = detail.getProjectRegion();

            for (ProjectDepartment department : departmentList) {
                int percent = department.getPercentageGetter().apply(detail);
                long amount = (long) receiveAmount * percent / 100;
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
            int receiveAmount = receiveDto.getReceiveAmount();
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
        personMap.forEach((k, v) -> {
            if (v.getInitialBalance() != null ||
                    StringUtils.isNotEmpty(v.getAccountNumber()) ||
                    StringUtils.isNotEmpty(v.getReserveFundNumber())) {
                receiveByPersonAndType.putIfAbsent(k, new HashMap<>());
            }
        });
        Map<Long, Long> remainingByPerson = getPersonRemainingMap();
        receiveByPersonAndType.forEach((k, v) -> {
            AccountBalanceData data = new AccountBalanceData();
            SysProjectPersonDto personDto = personMap.get(k);
            if (StringUtils.isNotEmpty(personDto.getReserveFundNumber()) || StringUtils.isNotEmpty(personDto.getAccountNumber())) {
                int month = CalendarUtils.getBeginningOfMonth(end).get(Calendar.MONTH) + 1;
                AccountBalanceData accountData = kingdeeService.getAccountBalanceByNumber(personDto.getAccountNumber(), month);
                AccountBalanceData reserveFundData = kingdeeService.getAccountBalanceByNumber(personDto.getReserveFundNumber(), month);
                data = AccountBalanceData.add(accountData, reserveFundData);
            }
            Optional.ofNullable(personDto.getInitialBalance()).ifPresent(data::setInitialBalance);
            BalanceTableRow row = typeMapToRow(v, data);
            row.setName(personMap.get(k).getName());
            row.setRemaining(ProjectUtils.dbPriceToRealPriceString(remainingByPerson.getOrDefault(k, 0L)));
            departmentRows.add(row);
        });
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

    public Map<Long, Long> getPersonRemainingMap() {
        List<SysProjectDetailDto> detailDtoList = sysProjectDetailService.queryAll(new SysProjectDetailQueryCriteria());
        Map<Long, Long> remainingByPerson = new HashMap<>();
        for (SysProjectDetailDto dto : detailDtoList) {
            long remaining = dto.getContractAmount() - Optional.ofNullable(dto.getReceiveAmount()).orElse(0);
            remaining = remaining * dto.getSalesPercent() / 100;
            remainingByPerson.put(dto.getSalesPerson(), remainingByPerson.getOrDefault(dto.getSalesPerson(), 0L) + Math.max(remaining, 0));
        }
        return remainingByPerson;
    }

    public Map<Long, Long> getBalancePersonMap() {
        Map<Long, Long> personBalanceMap = new HashMap<>();
        SysProjectReceiveQueryCriteria criteria = new SysProjectReceiveQueryCriteria();
        criteria.setReceiveTime(List.of(new Timestamp(CalendarUtils.getBeginningOfYear().getTimeInMillis()), new Timestamp(System.currentTimeMillis())));
        List<SysProjectReceiveDto> receiveDtoList = sysProjectReceiveService.queryAll(criteria);
        Map<Long, SysProjectDetailDto> detailMap = new HashMap<>();
        for (SysProjectReceiveDto receiveDto : receiveDtoList) {
            SysProjectDetailDto detail = detailMap.computeIfAbsent(receiveDto.getProjectId(), sysProjectDetailService::findById);
            int receiveAmount = receiveDto.getReceiveAmount();
            long salesAmount = (long) receiveAmount * detail.getSalesPercent() / 100;
            long person = detail.getSalesPerson();
            personBalanceMap.put(person, personBalanceMap.getOrDefault(person, 0L) + salesAmount);
        }
        Map<Long, SysProjectPersonDto> personDtoMap = sysProjectPersonService.getIdToPersonMap();
        for (Map.Entry<Long, Long> entry : personBalanceMap.entrySet()) {
            SysProjectPersonDto personDto = personDtoMap.get(entry.getKey());
            AccountBalanceData data = kingdeeService.getAccountBalanceByNumberList(List.of(
                    Optional.ofNullable(personDto.getAccountNumber()).orElse(""),
                    Optional.ofNullable(personDto.getReserveFundNumber()).orElse("")), Calendar.getInstance().get(Calendar.MONTH) + 1);
            entry.setValue(entry.getValue() - data.getExpenseThisYear() + Optional.ofNullable(personDto.getInitialBalance()).orElse(0));
        }
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
        Map<String, int[]> invoiceByCompanyMap = new TreeMap<>();

        for (SysProjectReceiveDto receiveDto : receiveDtoList) {
            SysProjectDetailDto projectDetailDto = sysProjectDetailDtoMap.get(receiveDto.getProjectId());
            String company = projectDetailDto.getPartyB();
            int[] data = invoiceByCompanyMap.computeIfAbsent(company, k -> new int[]{0, 0});
            data[0] += receiveDto.getInvoiceAmount();

        }
        sysProjectDetailDtoMap.forEach((k, v) -> {
            String company = v.getPartyB();
            int[] data = invoiceByCompanyMap.computeIfAbsent(company, k1 -> new int[]{0, 0});
            if (v.getInvoiceType() != ProjectUtils.INVOICE_TYPE_NONE) {
                data[1] += Optional.ofNullable(v.getShouldReceiveAmount()).orElse(0);
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

}
