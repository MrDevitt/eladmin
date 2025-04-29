package me.zhengjie.modules.keyuan.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.annotation.Log;
import me.zhengjie.modules.keyuan.domain.statistics.InvoiceTableRow;
import me.zhengjie.modules.keyuan.domain.statistics.SysProjectStatistics;
import me.zhengjie.modules.keyuan.domain.statistics.SysShouldReceiveData;
import me.zhengjie.modules.keyuan.domain.statistics.balance.BalanceData;
import me.zhengjie.modules.keyuan.service.impl.SysProjectStatisticsService;
import me.zhengjie.modules.keyuan.utils.CalendarUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = "项目收款信息管理")
@RequestMapping("/api/sysProjectStatistics")
public class SysProjectStatisticsController {

    private final SysProjectStatisticsService sysProjectStatisticsService;


    @GetMapping
    @Log("查询项目统计数据")
    @ApiOperation("查询项目统计数据")
    @PreAuthorize("@el.check('sysProjectStatistics:list')")
    public ResponseEntity<SysProjectStatistics> querySysProjectStatistics() {
        return new ResponseEntity<>(sysProjectStatisticsService.getSysProjectStatisticsInfo(), HttpStatus.OK);
    }


    @GetMapping("/shouldReceive")
    @Log("查询项目应收款数据")
    @ApiOperation("查询项目应收款数据")
    @PreAuthorize("@el.check('sysProjectStatistics:list')")
    public ResponseEntity<SysShouldReceiveData> querySysShouldReceiveData() {
        return new ResponseEntity<>(sysProjectStatisticsService.getSysShouldReceiveData(), HttpStatus.OK);
    }

    @GetMapping("/invoice")
    @Log("查询项目发票统计数据")
    @ApiOperation("查询项目发票统计数据")
    @PreAuthorize("@el.check('sysProjectStatistics:list')")
    public ResponseEntity<List<InvoiceTableRow>> queryInvoiceData(@RequestParam(required = false) Integer month) {
        long endTime = System.currentTimeMillis();
        if (month != null) {
            endTime = CalendarUtils.getEndOfMonth(month).getTimeInMillis();
        }
        return new ResponseEntity<>(sysProjectStatisticsService.getInvoiceData(endTime), HttpStatus.OK);
    }

    @GetMapping("/balance")
    @Log("查询余额表")
    @ApiOperation("查询余额表")
    @PreAuthorize("@el.check('sysProjectStatistics:list')")
    public ResponseEntity<BalanceData> querySysProjectBalance(@RequestParam(required = false) Integer month) {
        long endTime = System.currentTimeMillis();
        if (month != null) {
            endTime = CalendarUtils.getEndOfMonth(month).getTimeInMillis();
        }
        return new ResponseEntity<>(sysProjectStatisticsService.getBalanceData(endTime), HttpStatus.OK);
    }

}
