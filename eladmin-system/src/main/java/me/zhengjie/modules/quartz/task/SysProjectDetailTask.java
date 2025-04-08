package me.zhengjie.modules.quartz.task;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.modules.keyuan.service.SysProjectDetailService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailQueryCriteria;
import me.zhengjie.modules.keyuan.service.mapstruct.SysProjectDetailMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysProjectDetailTask {

    private final SysProjectDetailService sysProjectDetailService;
    private final SysProjectDetailMapper sysProjectDetailMapper;


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

}
