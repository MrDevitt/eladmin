package me.zhengjie.modules.quartz.task;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.kingdee.service.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import me.zhengjie.modules.keyuan.service.dto.SysProjectReceiveDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectReceiveQueryCriteria;
import me.zhengjie.modules.keyuan.service.impl.KingdeeService;
import me.zhengjie.modules.keyuan.service.mapstruct.SysProjectDetailMapper;
import me.zhengjie.modules.keyuan.service.mapstruct.SysProjectGuaranteeMapper;
import me.zhengjie.modules.keyuan.utils.PinyinUtils;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysProjectDetailTask {

    private final SysProjectDetailService sysProjectDetailService;
    private final SysProjectDetailMapper sysProjectDetailMapper;

    private final SysProjectGuaranteeService sysProjectGuaranteeService;

    private final SysProjectGuaranteeMapper sysProjectGuaranteeMapper;

    private final KingdeeService kingdeeService;

    private final SysProjectReceiveService sysProjectReceiveService;

    private final SysProjectPersonService sysProjectPersonService;

    private final SysProjectTransactionService sysProjectTransactionService;


    public void updateShouldReceiveAmount() {
        log.info("updateShouldReceiveAmount begin");
        long st = System.currentTimeMillis();
        try {
            List<SysProjectDetailDto> sysProjectDetailDtoList = sysProjectDetailService.queryAll(new SysProjectDetailQueryCriteria());
            for (SysProjectDetailDto sysProjectDetailDto : sysProjectDetailDtoList) {
                long shouldReceiveAmount = calcShouldReceiveAmount(sysProjectDetailDto);
                if (sysProjectDetailDto.getShouldReceiveAmount() == null ||
                        sysProjectDetailDto.getShouldReceiveAmount() != shouldReceiveAmount) {
                    sysProjectDetailDto.setShouldReceiveAmount(shouldReceiveAmount);
                    sysProjectDetailService.update(sysProjectDetailMapper.toEntity(sysProjectDetailDto));
                }
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

    //每天
    public void refreshKingdeeToken() throws ApiException {
        kingdeeService.refreshToken();
    }

    //每小时
    public void refreshKingdeeCache() throws ApiException {
        kingdeeService.refreshBalanceRowCache();
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
}
