package me.zhengjie.modules.quartz.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.modules.keyuan.domain.config.SalesStatisticMessageConfig;
import me.zhengjie.modules.keyuan.domain.statistics.transaction.SummaryData;
import me.zhengjie.modules.keyuan.service.SysProjectConfigService;
import me.zhengjie.modules.keyuan.service.SysProjectDetailService;
import me.zhengjie.modules.keyuan.service.SysProjectGuaranteeService;
import me.zhengjie.modules.keyuan.service.SysProjectPersonService;
import me.zhengjie.modules.keyuan.service.SysProjectReceiveService;
import me.zhengjie.modules.keyuan.service.SysProjectTransactionService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailQueryCriteria;
import me.zhengjie.modules.keyuan.service.dto.SysProjectGuaranteeDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectGuaranteeQueryCriteria;
import me.zhengjie.modules.keyuan.service.dto.SysProjectPersonDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectPersonQueryCriteria;
import me.zhengjie.modules.keyuan.service.dto.SysProjectReceiveDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectReceiveQueryCriteria;
import me.zhengjie.modules.keyuan.service.impl.DingTalkService;
import me.zhengjie.modules.keyuan.service.mapstruct.SysProjectDetailMapper;
import me.zhengjie.modules.keyuan.service.mapstruct.SysProjectGuaranteeMapper;
import me.zhengjie.modules.keyuan.utils.CalendarUtils;
import me.zhengjie.modules.keyuan.utils.PinyinUtils;
import me.zhengjie.modules.keyuan.utils.ProjectUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysProjectDetailTask {

    private final SysProjectDetailService sysProjectDetailService;
    private final SysProjectDetailMapper sysProjectDetailMapper;

    private final SysProjectGuaranteeService sysProjectGuaranteeService;

    private final SysProjectGuaranteeMapper sysProjectGuaranteeMapper;

    private final SysProjectReceiveService sysProjectReceiveService;

    private final SysProjectPersonService sysProjectPersonService;

    private final SysProjectTransactionService sysProjectTransactionService;

    private final DingTalkService dingTalkService;

    private final SysProjectConfigService sysProjectConfigService;


    public void updateShouldReceiveAmount() {
        log.info("updateShouldReceiveAmount begin");
        long st = System.currentTimeMillis();
        try {
            List<SysProjectDetailDto> sysProjectDetailDtoList = sysProjectDetailService.queryAll(new SysProjectDetailQueryCriteria());
            StringBuilder newProjects = new StringBuilder();
            for (SysProjectDetailDto sysProjectDetailDto : sysProjectDetailDtoList) {
                long shouldReceiveAmount = calcShouldReceiveAmount(sysProjectDetailDto);
                long originalShouldReceiveAmount = Optional.ofNullable(sysProjectDetailDto.getShouldReceiveAmount()).orElse(0L);
                if (shouldReceiveAmount != originalShouldReceiveAmount) {
                    if (shouldReceiveAmount > originalShouldReceiveAmount) {
                        newProjects.append(sysProjectDetailDto.getId()).append(",");
                    }
                    sysProjectDetailDto.setShouldReceiveAmount(shouldReceiveAmount);
                    sysProjectDetailService.update(sysProjectDetailMapper.toEntity(sysProjectDetailDto));
                }
            }
            if (newProjects.length() > 0) {
                JSONObject message = new JSONObject();
                message.put("msgtype", "text");
                JSONObject text = new JSONObject();
                newProjects.deleteCharAt(newProjects.length() - 1);
                text.put("content", "新增应收款项目:" + newProjects);
                message.put("text", text);
                dingTalkService.sendMessage(message, "3253152364991358,1946596041989985,2662613715782423");
            }
            log.info("updateShouldReceiveAmount success, time = " + (System.currentTimeMillis() - st));
        } catch (Exception e) {
            log.error("updateShouldReceiveAmount error", e);
        }
    }

    private long calcShouldReceiveAmount(SysProjectDetailDto sysProjectDetailDto) {
        long progress = sysProjectDetailDto.getProjectProgress();
        long contractAmount = sysProjectDetailDto.getContractAmount();
        long shouldPay;
        switch (sysProjectDetailDto.getContractPayWay()) {
            case 0:
                shouldPay = progress < 100 ? contractAmount / 2 : contractAmount;
                break;
            case 1:
                shouldPay = progress < 100 ? 0 : contractAmount;
                break;
            case 2:
                shouldPay = progress < 50 ? contractAmount * 30 / 100 : progress < 100 ? contractAmount * 60 / 100 : contractAmount;
                break;
            case 3:
                shouldPay = contractAmount * progress / 100;
                break;
            case 4:
                //noinspection DuplicateExpressions
                shouldPay = progress < 90 ? contractAmount * 60 / 100 : progress < 100 ? contractAmount * 90 / 100 : contractAmount;
                break;
            case 5:
                //noinspection DuplicateExpressions
                shouldPay = progress < 80 ? contractAmount * 50 / 100 : progress < 100 ? contractAmount * 80 / 100 : contractAmount;
                break;
            case 6:
                //noinspection DuplicateExpressions
                shouldPay = progress < 100 ? contractAmount * 90 / 100 : contractAmount;
                break;
            case 7:
                //noinspection DuplicateExpressions
                shouldPay = progress < 99 ? contractAmount * 50 / 100 : progress < 100 ? contractAmount * 90 / 100 : contractAmount;
                break;
            case 8:
                shouldPay = progress < 70 ? contractAmount * 30 / 100 : progress < 100 ? contractAmount * 70 / 100 : contractAmount;
                break;
            case 9:
                //noinspection DuplicateExpressions
                shouldPay = progress < 70 ? contractAmount * 30 / 100 : progress < 100 ? contractAmount * 80 / 100 : contractAmount;
                break;
            default:
                throw new RuntimeException("invalid contractPayWay, projectId=" + sysProjectDetailDto.getId());
        }
        long receive = sysProjectDetailDto.getReceiveAmount() == null ? 0 : sysProjectDetailDto.getReceiveAmount();
        return Math.max(shouldPay - receive, 0);
    }

    public void updateProjectProgress() {
        SysProjectDetailQueryCriteria criteria = new SysProjectDetailQueryCriteria();
        criteria.setProjectProgress(List.of(0, 99));
        List<SysProjectDetailDto> sysProjectDetailDtoList = sysProjectDetailService.queryAll(criteria);
        for (SysProjectDetailDto sysProjectDetailDto : sysProjectDetailDtoList) {
            if (sysProjectDetailDto.getReceiveAmount() != null &&
                    sysProjectDetailDto.getContractAmount() != null &&
                    sysProjectDetailDto.getContractAmount() != 0 &&
                    sysProjectDetailDto.getReceiveAmount() >= sysProjectDetailDto.getContractAmount()) {

                sysProjectDetailDto.setProjectProgress(100);
                sysProjectDetailService.update(sysProjectDetailMapper.toEntity(sysProjectDetailDto));
            }
        }
    }

    public void updateGuaranteeStatus() {
        log.info("updateGuaranteeStatus begin");
        long st = System.currentTimeMillis();
        try {
            SysProjectGuaranteeQueryCriteria criteria = new SysProjectGuaranteeQueryCriteria();
            criteria.setStatus(List.of(SysProjectGuaranteeDto.STATUS_NORMAL));
            List<SysProjectGuaranteeDto> sysProjectGuaranteeDtoList = sysProjectGuaranteeService.queryAll(criteria);
            for (SysProjectGuaranteeDto dto : sysProjectGuaranteeDtoList) {
                if (dto.getGuaranteeTime().getTime() < st) {
                    dto.setStatus(SysProjectGuaranteeDto.STATUS_ABNORMAL);
                    sysProjectGuaranteeService.update(sysProjectGuaranteeMapper.toEntity(dto));
                }
            }
            log.info("updateGuaranteeStatus success, time = " + (System.currentTimeMillis() - st));
        } catch (Exception e) {
            log.error("updateGuaranteeStatus error", e);
        }
    }

    public String validateSysProjectData() {
        JSONObject result = new JSONObject();

        List<SysProjectDetailDto> sysProjectDetailDtoList = sysProjectDetailService.queryAll(new SysProjectDetailQueryCriteria());
        List<SysProjectReceiveDto> sysProjectReceiveDtoList = sysProjectReceiveService.queryAll(new SysProjectReceiveQueryCriteria());
        Map<Long, List<SysProjectReceiveDto>> sysProjectReceiveDtoMap = sysProjectReceiveDtoList.stream().collect(Collectors.groupingBy(SysProjectReceiveDto::getProjectId));
        //检查收款不一致
        StringBuilder receiveErrorIds = new StringBuilder();
        for (SysProjectDetailDto detailDto : sysProjectDetailDtoList) {
            long amount = 0;
            for (SysProjectReceiveDto receiveDto : sysProjectReceiveDtoMap.getOrDefault(detailDto.getId(), List.of())) {
                amount += receiveDto.getReceiveAmount();
            }
            if (Optional.ofNullable(detailDto.getReceiveAmount()).orElse(0L) != amount) {
                receiveErrorIds.append(detailDto.getId()).append(",");
            }
        }
        if (receiveErrorIds.length() > 0) {
            receiveErrorIds.deleteCharAt(receiveErrorIds.length() - 1);
        }
        result.put("收款与明细不一致项目", receiveErrorIds.toString());
        //检查收款大于合同金额
        StringBuilder contractErrorIds = new StringBuilder();
        for (SysProjectDetailDto detailDto : sysProjectDetailDtoList) {
            if (Optional.ofNullable(detailDto.getReceiveAmount()).orElse(0L) > detailDto.getContractAmount()) {
                contractErrorIds.append(detailDto.getId()).append(",");
            }
        }
        if (contractErrorIds.length() > 0) {
            contractErrorIds.deleteCharAt(contractErrorIds.length() - 1);
        }
        result.put("收款大于合同金额项目", contractErrorIds);
        //检查担保
        StringBuilder guaranteeErrorIds = new StringBuilder();
        Set<Long> guaranteeWhiteList = new HashSet<>(List.of(-1L));
        SysProjectGuaranteeQueryCriteria guaranteeQueryCriteria = new SysProjectGuaranteeQueryCriteria();
        guaranteeQueryCriteria.setStatus(List.of(SysProjectGuaranteeDto.STATUS_NORMAL, SysProjectGuaranteeDto.STATUS_ABNORMAL));
        Set<String> projectNameSet = sysProjectGuaranteeService.queryAll(guaranteeQueryCriteria).stream().map(SysProjectGuaranteeDto::getProjectName).collect(Collectors.toSet());
        for (SysProjectDetailDto detailDto : sysProjectDetailDtoList) {
            if (projectNameSet.contains(detailDto.getProjectName()) && !guaranteeWhiteList.contains(detailDto.getId())) {
                guaranteeErrorIds.append(detailDto.getId()).append(",");
            }
        }
        if (guaranteeErrorIds.length() > 0) {
            guaranteeErrorIds.deleteCharAt(guaranteeErrorIds.length() - 1);
        }
        result.put("担保与明细重复项目", guaranteeErrorIds.toString());
        //检查业务人和项目编号匹配
        StringBuilder salesPersonErrorIds = new StringBuilder();
        Map<Long, SysProjectPersonDto> sysProjectPersonMap = sysProjectPersonService.getIdToPersonMap();
        for (SysProjectDetailDto detailDto : sysProjectDetailDtoList) {
            SysProjectPersonDto personDto = sysProjectPersonMap.get(detailDto.getSalesPerson());
            String personInitials = PinyinUtils.getFirstLetters(personDto.getName());
            if (!detailDto.getContractNumber().contains(personInitials)) {
                salesPersonErrorIds.append(detailDto.getId()).append(",");
            }
        }
        if (salesPersonErrorIds.length() > 0) {
            salesPersonErrorIds.deleteCharAt(salesPersonErrorIds.length() - 1);
        }
        result.put("业务人与编号不匹配项目", salesPersonErrorIds.toString());
        //检查项目名重复
        StringBuilder projectNameErrorIds = new StringBuilder();
        Map<String, List<Long>> repeatedNameMap = new HashMap<>();
        for (SysProjectDetailDto detailDto : sysProjectDetailDtoList) {
            String key = detailDto.getProjectType() + "_" + detailDto.getProjectName();
            repeatedNameMap.computeIfAbsent(key, k -> new ArrayList<>());
            repeatedNameMap.get(key).add(detailDto.getId());
        }
        repeatedNameMap.forEach((k, v) -> {
            if (v.size() > 1) {
                for (Long id : v) {
                    projectNameErrorIds.append(id).append(",");
                }
            }
        });
        if (projectNameErrorIds.length() > 0) {
            projectNameErrorIds.deleteCharAt(projectNameErrorIds.length() - 1);
        }
        result.put("项目名重复项目", projectNameErrorIds.toString());
        return JSON.toJSONString(result);
    }

    public void updateTransactionByReceive() {
        SysProjectReceiveQueryCriteria criteria = new SysProjectReceiveQueryCriteria();
        criteria.setReceiveAmount(0);
        criteria.setReceiveTime(List.of(new Timestamp(1767200461000L), new Timestamp(System.currentTimeMillis())));
        List<SysProjectReceiveDto> sysProjectReceiveDtoList = sysProjectReceiveService.queryAll(criteria);
        for (SysProjectReceiveDto receiveDto : sysProjectReceiveDtoList) {
            try {
                sysProjectTransactionService.updateTransactionByReceive(receiveDto, false);
            } catch (Throwable throwable) {
                log.error("更新收款数据异常", throwable);
            }
        }
    }

    //每90min
    public void refreshDingTalkToken() {
        dingTalkService.refreshAccessToken();
    }

    public List<String> sendSalesStatisticMessage() {
        SalesStatisticMessageConfig salesStatisticMessageConfig = sysProjectConfigService.findConfigByKey(ProjectUtils.PROJECT_CONFIG_KEY_SALES_STATISTICS_MESSAGE, SalesStatisticMessageConfig.class);
        Map<Long, JSONObject> messageMap = new HashMap<>();
        Calendar lastWeek = Calendar.getInstance();
        lastWeek.add(Calendar.WEEK_OF_YEAR, -1);
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd");

        SysProjectPersonQueryCriteria personQueryCriteria = new SysProjectPersonQueryCriteria();
        personQueryCriteria.setIds(salesStatisticMessageConfig.getMessageReceiverList());
        List<SysProjectPersonDto> personDtoList = sysProjectPersonService.queryAll(personQueryCriteria);
        Map<Long, SysProjectPersonDto> personMap = personDtoList.stream().collect(Collectors.toMap(
                SysProjectPersonDto::getId,
                Function.identity(),
                (x, y) -> x
        ));
        personDtoList.forEach(personDto -> {
            JSONObject message = new JSONObject();
            message.put("msgtype", "markdown");
            JSONObject markdown = new JSONObject();
            String title = personDto.getName() + formatter.format(new Date(lastWeek.getTimeInMillis())) + "～" + formatter.format(new Date()) + "统计数据";
            markdown.put("title", title);
            markdown.put("text", new StringBuilder());
            message.put("markdown", markdown);
            messageMap.put(personDto.getId(), message);
        });


        //收支
        List<SummaryData> summaryDataList = sysProjectTransactionService.getTransactionSummary(
                new Timestamp(lastWeek.getTimeInMillis()), new Timestamp(System.currentTimeMillis()), salesStatisticMessageConfig.getTopAccountSet(), false);
        Map<Long, SummaryData> summaryDataMap = summaryDataList.stream().flatMap(summaryData -> summaryData.getChildren().stream()).collect(Collectors.toMap(
                SummaryData::getAccountNumber,
                Function.identity(),
                (x, y) -> x));
        messageMap.forEach((id, message) -> {
            StringBuilder markdownText = (StringBuilder) message.getObject("markdown", JSONObject.class).get("text");
            String title = (String) message.getObject("markdown", JSONObject.class).get("title");
            SysProjectPersonDto personDto = personMap.get(id);
            markdownText.append("# ").append(title);
            markdownText.append("  \n  ### 收支统计");
            Long accountNumber = personDto.getAccountNumber() / 100;//收入科目移除后两位为总科目
            SummaryData data = summaryDataMap.getOrDefault(accountNumber, new SummaryData());
            markdownText.append("  \n  - 周收入:").append(ProjectUtils.dbPriceToRealPriceString(data.getCurrentIncome()))
                    .append("  \n  - 周支出:").append(ProjectUtils.dbPriceToRealPriceString(data.getCurrentExpense()))
                    .append("  \n  - 周结余:").append(ProjectUtils.dbPriceToRealPriceString(data.getCurrentRemain()))
                    .append("  \n  - 往年结余:").append(ProjectUtils.dbPriceToRealPriceString(data.getInitialAmount()))
                    .append("  \n  - 年累计收入:").append(ProjectUtils.dbPriceToRealPriceString(data.getEndIncome()))
                    .append("  \n  - 年累计支出:").append(ProjectUtils.dbPriceToRealPriceString(data.getEndExpense()))
                    .append("  \n  - 年累计结余:").append(ProjectUtils.dbPriceToRealPriceString(data.getEndRemain()))
                    .append("  \n  - 未收款总提成:").append(ProjectUtils.dbPriceToRealPriceString(data.getRemainingShare()))
                    .append("  \n  - [查看详情](https://frp-put.com:24693/keyuan/statistics/personSummary)");
        });
        //合同、应收款
        SysProjectDetailQueryCriteria projectDetailCriteria = new SysProjectDetailQueryCriteria();
        projectDetailCriteria.setSalesPersons(salesStatisticMessageConfig.getMessageReceiverList());
        List<SysProjectDetailDto> projectDetailDtoList = sysProjectDetailService.queryAll(projectDetailCriteria);
        Map<Long, long[]> personProjectMap = new HashMap<>();//[周合同,年合同,总应收,总收款]
        Calendar yearBegin = CalendarUtils.getBeginningOfYear();
        for (SysProjectDetailDto detailDto : projectDetailDtoList) {
            long[] amounts = personProjectMap.computeIfAbsent(detailDto.getSalesPerson(), k -> new long[4]);
            if (detailDto.getCreateTime().getTime() >= lastWeek.getTimeInMillis()) {
                amounts[0] += detailDto.getContractAmount();
            }
            if (detailDto.getCreateTime().getTime() >= yearBegin.getTimeInMillis()) {
                amounts[1] += detailDto.getContractAmount();
            }
            amounts[2] += Optional.ofNullable(detailDto.getShouldReceiveAmount()).orElse(0L);
            amounts[3] += Optional.ofNullable(detailDto.getReceiveAmount()).orElse(0L);
        }
        messageMap.forEach((k, message) -> {
            long[] amounts = personProjectMap.getOrDefault(k, new long[4]);
            StringBuilder markdownText = (StringBuilder) message.getObject("markdown", JSONObject.class).get("text");
            markdownText.append("  \n  ### 业务统计");
            markdownText.append("  \n  - 周业务量:").append(ProjectUtils.dbPriceToRealPriceString(amounts[0]))
                    .append("  \n  - 本年业务量:").append(ProjectUtils.dbPriceToRealPriceString(amounts[1]))
                    .append("  \n  - [查看详情](https://frp-put.com:24693/keyuan/sysProjectDetail)")
                    .append("  \n  - 总应收款:").append(ProjectUtils.dbPriceToRealPriceString(amounts[2]))
                    .append("  \n  - 应收款收款率:").append(String.format("%.2f", ((double) amounts[3] / (amounts[2] + amounts[3])) * 100)).append("%")
                    .append("  \n  - [查看详情](https://frp-put.com:24693/keyuan/statistics/sysShouldReceiveStatistics)");
        });

        //担保
        SysProjectGuaranteeQueryCriteria guaranteeCriteria = new SysProjectGuaranteeQueryCriteria();
        guaranteeCriteria.setStatus(List.of(SysProjectGuaranteeDto.STATUS_NORMAL, SysProjectGuaranteeDto.STATUS_ABNORMAL));
        guaranteeCriteria.setGuaranteePersons(salesStatisticMessageConfig.getMessageReceiverList());
        List<SysProjectGuaranteeDto> guaranteeList = sysProjectGuaranteeService.queryAll(guaranteeCriteria);
        Map<Long, long[]> guaranteeByPerson = new HashMap<>();//[担保中,已逾期,总担保]
        for (SysProjectGuaranteeDto guarantee : guaranteeList) {
            long[] amounts = guaranteeByPerson.computeIfAbsent(guarantee.getGuaranteePerson(), k -> new long[3]);
            if (guarantee.getStatus() == SysProjectGuaranteeDto.STATUS_NORMAL) {
                amounts[0] += guarantee.getGuaranteeAmount();
            } else if (guarantee.getStatus() == SysProjectGuaranteeDto.STATUS_ABNORMAL) {
                amounts[1] += guarantee.getGuaranteeAmount();
            }
            amounts[2] += guarantee.getGuaranteeAmount();
        }
        messageMap.forEach((k, message) -> {
            JSONObject markdown = message.getObject("markdown", JSONObject.class);
            long[] amounts = guaranteeByPerson.getOrDefault(k, new long[3]);
            StringBuilder markdownText = (StringBuilder) message.getObject("markdown", JSONObject.class).get("text");
            markdownText.append("  \n  ### 担保统计");
            markdownText.append("  \n  - 担保中金额:").append(ProjectUtils.dbPriceToRealPriceString(amounts[0]))
                    .append("  \n  - 担保逾期金额:").append(ProjectUtils.dbPriceToRealPriceString(amounts[1]))
                    .append("  \n  - 总担保金额:").append(ProjectUtils.dbPriceToRealPriceString(amounts[2]))
                    .append("  \n  - [查看详情](https://frp-put.com:24693/keyuan/sysProjectGuarantee)");
            markdown.put("text", markdownText.toString());
        });

        List<String> noUserIdList = new ArrayList<>();
        messageMap.forEach((k, v) -> {
            String userId = personMap.get(k).getDingTalkUserId();
            if (userId == null) {
                noUserIdList.add(personMap.get(k).getName());
                return;
            }
            dingTalkService.sendMessage(v, userId + salesStatisticMessageConfig.getAllMessageReceivers());
        });
        return noUserIdList;
    }
}
