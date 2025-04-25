package me.zhengjie.modules.keyuan.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.modules.keyuan.domain.balance.AccountBalanceData;
import me.zhengjie.modules.keyuan.domain.balance.BalanceData;
import me.zhengjie.modules.keyuan.domain.balance.BalanceTableRow;
import me.zhengjie.modules.keyuan.service.SysProjectDetailService;
import me.zhengjie.modules.keyuan.service.SysProjectPersonService;
import me.zhengjie.modules.keyuan.service.SysProjectReceiveService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectPersonDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectReceiveDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectReceiveQueryCriteria;
import me.zhengjie.modules.keyuan.utils.CalendarUtils;
import me.zhengjie.modules.keyuan.utils.ProjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceService {

    private final SysProjectDetailService sysProjectDetailService;

    private final SysProjectReceiveService sysProjectReceiveService;

    private final SysProjectPersonService sysProjectPersonService;

    private final KingdeeService kingdeeService;


    public BalanceData getBalanceData() {
        return getBalanceData(System.currentTimeMillis());
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
        Map<String, Map<Integer, long[]>> receiveByDepartmentAndType = new HashMap<>();
        Arrays.stream(ProjectUtils.PROJECT_DEPARTMENTS).forEach(e -> {
            Map<Integer, long[]> map = new HashMap<>(Map.of(
                    ProjectUtils.PROJECT_TYPE_EXAM, new long[]{0, 0, 0},
                    ProjectUtils.PROJECT_TYPE_SUPERVISE, new long[]{0, 0, 0},
                    ProjectUtils.PROJECT_TYPE_DESIGN, new long[]{0, 0, 0}));
            receiveByDepartmentAndType.put(e, map);
        });
        for (SysProjectReceiveDto receiveDto : receiveDtoList) {
            SysProjectDetailDto detail = detailMap.computeIfAbsent(receiveDto.getProjectId(), sysProjectDetailService::findById);
            int projectType = detail.getProjectType();
            int receiveAmount = receiveDto.getReceiveAmount();
            //用long防止乘后int溢出
            long techAmount = (long) receiveAmount * detail.getTechnicalPercent() / 100;
            long salesAmount = (long) receiveAmount * detail.getSalesPercent() / 100;
            long managementAmount = (long) receiveAmount * detail.getManagementPercent() / 100;
            long presidentAmount = (long) receiveAmount * detail.getPresidentPercent() / 100;
            long[] techAmounts = receiveByDepartmentAndType.get(ProjectUtils.PROJECT_DEPARTMENT_TECH).computeIfAbsent(projectType, k -> new long[]{0, 0, 0});
            long[] salesAmounts = receiveByDepartmentAndType.get(ProjectUtils.PROJECT_DEPARTMENT_SALES).computeIfAbsent(projectType, k -> new long[]{0, 0, 0});
            long[] manageAmounts = receiveByDepartmentAndType.get(ProjectUtils.PROJECT_DEPARTMENT_MANAGEMENT).computeIfAbsent(projectType, k -> new long[]{0, 0, 0});
            long[] presidentAmounts = receiveByDepartmentAndType.get(ProjectUtils.PROJECT_DEPARTMENT_PRESIDENT).computeIfAbsent(projectType, k -> new long[]{0, 0, 0});
            if (receiveDto.getReceiveTime().getTime() < CalendarUtils.getBeginningOfMonth(end).getTimeInMillis()) {
                techAmounts[0] += techAmount;
                salesAmounts[0] += salesAmount;
                manageAmounts[0] += managementAmount;
                presidentAmounts[0] += presidentAmount;
            } else {
                techAmounts[1] += techAmount;
                salesAmounts[1] += salesAmount;
                manageAmounts[1] += managementAmount;
                presidentAmounts[1] += presidentAmount;
            }
            techAmounts[2] += techAmount;
            salesAmounts[2] += salesAmount;
            manageAmounts[2] += managementAmount;
            presidentAmounts[2] += presidentAmount;
        }
        List<BalanceTableRow> departmentRows = new ArrayList<>();
        receiveByDepartmentAndType.forEach((k, v) -> {
            BalanceTableRow row = typeMapToRow(v);
            row.setName(k);
            departmentRows.add(row);
        });
        departmentRows.sort(BalanceTableRow.COMPARATOR_DESC);
        return departmentRows;
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
            departmentRows.add(row);
        });
        departmentRows.sort(BalanceTableRow.COMPARATOR_DESC);
        return departmentRows;
    }

    private BalanceTableRow typeMapToRow(Map<Integer, long[]> typeMap) {
        return typeMapToRow(typeMap, new AccountBalanceData());
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

}
