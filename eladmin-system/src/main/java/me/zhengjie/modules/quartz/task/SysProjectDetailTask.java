package me.zhengjie.modules.quartz.task;

import com.kingdee.service.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.modules.keyuan.service.SysProjectDetailService;
import me.zhengjie.modules.keyuan.service.SysProjectGuaranteeService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailQueryCriteria;
import me.zhengjie.modules.keyuan.service.dto.SysProjectGuaranteeDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectGuaranteeQueryCriteria;
import me.zhengjie.modules.keyuan.service.impl.KingdeeService;
import me.zhengjie.modules.keyuan.service.mapstruct.SysProjectDetailMapper;
import me.zhengjie.modules.keyuan.service.mapstruct.SysProjectGuaranteeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysProjectDetailTask {

    private final SysProjectDetailService sysProjectDetailService;
    private final SysProjectDetailMapper sysProjectDetailMapper;

    private final SysProjectGuaranteeService sysProjectGuaranteeService;

    private final SysProjectGuaranteeMapper sysProjectGuaranteeMapper;

    private final KingdeeService kingdeeService;


    public void updateShouldReceiveAmount() {
        log.info("updateShouldReceiveAmount begin");
        long st = System.currentTimeMillis();
        try {
            List<SysProjectDetailDto> sysProjectDetailDtoList = sysProjectDetailService.queryAll(new SysProjectDetailQueryCriteria());
            for (SysProjectDetailDto sysProjectDetailDto : sysProjectDetailDtoList) {
                int shouldReceiveAmount = calcShouldReceiveAmount(sysProjectDetailDto);
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

    private int calcShouldReceiveAmount(SysProjectDetailDto sysProjectDetailDto) {
        int progress = sysProjectDetailDto.getProjectProgress();
        int contractAmount = sysProjectDetailDto.getContractAmount();
        int shouldPay;
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
            default:
                throw new RuntimeException("invalid contractPayWay, projectId=" + sysProjectDetailDto.getId());
        }
        int receive = sysProjectDetailDto.getReceiveAmount() == null ? 0 : sysProjectDetailDto.getReceiveAmount();
        return Math.max(shouldPay - receive, 0);
    }

    public void updateGuaranteeStatus() {
        log.info("updateGuaranteeStatus begin");
        long st = System.currentTimeMillis();
        try {
            SysProjectGuaranteeQueryCriteria criteria = new SysProjectGuaranteeQueryCriteria();
            criteria.setStatus(SysProjectGuaranteeDto.STATUS_NORMAL);
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

    //每天
    public void refreshKingdeeToken() throws ApiException {
        kingdeeService.refreshToken();
    }

    //每小时
    public void refreshKingdeeCache() throws ApiException {
        kingdeeService.refreshBalanceRowCache();
    }
}
