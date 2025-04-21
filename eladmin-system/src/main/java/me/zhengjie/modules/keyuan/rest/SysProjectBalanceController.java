package me.zhengjie.modules.keyuan.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import me.zhengjie.modules.keyuan.domain.balance.BalanceData;
import me.zhengjie.modules.keyuan.service.impl.BalanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Api(tags = "项目明细管理")
@RequestMapping("/api/sysProjectBalance")
public class SysProjectBalanceController {

    private final BalanceService balanceService;

    @GetMapping
    @Log("查询余额表")
    @ApiOperation("查询余额表")
    public ResponseEntity<BalanceData> querySysProjectBalance() {
        return new ResponseEntity<>(balanceService.getBalanceData(), HttpStatus.OK);
    }
}
