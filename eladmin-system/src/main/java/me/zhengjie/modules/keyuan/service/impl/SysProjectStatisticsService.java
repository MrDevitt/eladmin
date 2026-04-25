package me.zhengjie.modules.keyuan.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.modules.keyuan.domain.config.SysProjectStatisticsConfig;
import me.zhengjie.modules.keyuan.domain.statistics.InvoiceTableRow;
import me.zhengjie.modules.keyuan.domain.statistics.InvoicedNotReceiveData;
import me.zhengjie.modules.keyuan.domain.statistics.SysProjectStatistics;
import me.zhengjie.modules.keyuan.domain.statistics.SysReceiveStatistics;
import me.zhengjie.modules.keyuan.service.SysProjectConfigService;
import me.zhengjie.modules.keyuan.service.SysProjectDetailService;
import me.zhengjie.modules.keyuan.service.SysProjectPersonService;
import me.zhengjie.modules.keyuan.service.SysProjectReceiveService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailQueryCriteria;
import me.zhengjie.modules.keyuan.service.dto.SysProjectPersonDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectReceiveDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectReceiveQueryCriteria;
import me.zhengjie.modules.keyuan.utils.ProjectUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
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

import static me.zhengjie.modules.keyuan.domain.statistics.InvoicedNotReceiveData.Row.COMPARATOR;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysProjectStatisticsService {

    private final SysProjectDetailService sysProjectDetailService;

    private final SysProjectReceiveService sysProjectReceiveService;

    private final SysProjectPersonService sysProjectPersonService;

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

}
