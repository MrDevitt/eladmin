/*
 *  Copyright 2019-2020 Zheng Jie
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.zhengjie.modules.keyuan.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import me.zhengjie.modules.keyuan.domain.statistics.transaction.SummaryData;
import me.zhengjie.modules.keyuan.service.SysProjectTransactionService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectBatchTransactionDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectTransactionDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectTransactionQueryCriteria;
import me.zhengjie.utils.PageResult;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

/**
 * @author MrDevitt
 * @website https://eladmin.vip
 * @date 2025-11-11
 **/
@RestController
@RequiredArgsConstructor
@Api(tags = "项目收支信息管理")
@RequestMapping("/api/sysProjectTransaction")
public class SysProjectTransactionController {

    private final SysProjectTransactionService sysProjectTransactionService;

    @Log("导出数据")
    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('sysProjectTransaction:list')")
    public void exportSysProjectTransaction(HttpServletResponse response, SysProjectTransactionQueryCriteria criteria) throws IOException {
        sysProjectTransactionService.download(sysProjectTransactionService.queryAll(criteria), response);
    }

    @GetMapping
    @Log("查询项目收支信息")
    @ApiOperation("查询项目收支信息")
    @PreAuthorize("@el.check('sysProjectTransaction:list')")
    public ResponseEntity<PageResult<SysProjectTransactionDto>> querySysProjectTransaction(SysProjectTransactionQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(sysProjectTransactionService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @PostMapping
    @Log("新增项目收支信息")
    @ApiOperation("新增项目收支信息")
    @PreAuthorize("@el.check('sysProjectTransaction:add')")
    public ResponseEntity<Object> createSysProjectTransaction(@Validated @RequestBody SysProjectBatchTransactionDto resources) {
        sysProjectTransactionService.create(resources.toDomainList());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改项目收支信息")
    @ApiOperation("修改项目收支信息")
    @PreAuthorize("@el.check('sysProjectTransaction:edit')")
    public ResponseEntity<Object> updateSysProjectTransaction(@Validated @RequestBody SysProjectBatchTransactionDto resources) {
        sysProjectTransactionService.update(resources.toDomain());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除项目收支信息")
    @ApiOperation("删除项目收支信息")
    @PreAuthorize("@el.check('sysProjectTransaction:del')")
    public ResponseEntity<Object> deleteSysProjectTransaction(@RequestBody Long[] ids) {
        sysProjectTransactionService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/summary")
    @Log("查询科目余额表")
    @ApiOperation("查询科目余额表")
    @PreAuthorize("@el.check('sysProjectAccountSummay:list')")
    public ResponseEntity<List<SummaryData>> querySysProjectTransaction(@RequestParam Timestamp begin, @RequestParam Timestamp end, @RequestParam String type) {
        return new ResponseEntity<>(sysProjectTransactionService.getTransactionSummary(begin, end, type), HttpStatus.OK);
    }
}