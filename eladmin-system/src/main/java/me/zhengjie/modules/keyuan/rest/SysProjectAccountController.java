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

import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import me.zhengjie.modules.keyuan.domain.SysProjectAccount;
import me.zhengjie.modules.keyuan.service.SysProjectAccountService;
import me.zhengjie.modules.keyuan.service.SysProjectTransactionService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectAccountDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectAccountQueryCriteria;
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
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author MrDevitt
 * @website https://eladmin.vip
 * @date 2025-11-11
 **/
@RestController
@RequiredArgsConstructor
@Api(tags = "项目科目信息管理")
@RequestMapping("/api/sysProjectAccount")
public class SysProjectAccountController {

    private final SysProjectAccountService sysProjectAccountService;

    private final SysProjectTransactionService sysProjectTransactionService;

    @Log("导出数据")
    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('sysProjectAccount:list')")
    public void exportSysProjectAccount(HttpServletResponse response, SysProjectAccountQueryCriteria criteria) throws IOException {
        sysProjectAccountService.download(sysProjectAccountService.queryAll(criteria), response);
    }

    @GetMapping
    @Log("查询项目科目信息")
    @ApiOperation("查询项目科目信息")
    @PreAuthorize("@el.check('sysProjectAccount:list')")
    public ResponseEntity<PageResult<SysProjectAccountDto>> querySysProjectAccount(SysProjectAccountQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(sysProjectAccountService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @PostMapping
    @Log("新增项目科目信息")
    @ApiOperation("新增项目科目信息")
    @PreAuthorize("@el.check('sysProjectAccount:add')")
    public ResponseEntity<Object> createSysProjectAccount(@Validated @RequestBody SysProjectAccount resources) {
        sysProjectAccountService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改项目科目信息")
    @ApiOperation("修改项目科目信息")
    @PreAuthorize("@el.check('sysProjectAccount:edit')")
    public ResponseEntity<Object> updateSysProjectAccount(@Validated @RequestBody SysProjectAccount resources) {
        sysProjectAccountService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除项目科目信息")
    @ApiOperation("删除项目科目信息")
    @PreAuthorize("@el.check('sysProjectAccount:del')")
    public ResponseEntity<Object> deleteSysProjectAccount(@RequestBody Long[] ids) {
        SysProjectTransactionQueryCriteria criteria = new SysProjectTransactionQueryCriteria();
        criteria.setAccountNumberList(Arrays.asList(ids));
        if (CollectionUtils.isNotEmpty(sysProjectTransactionService.queryAll(criteria))) {
            throw new RuntimeException(Arrays.toString(ids) + "有交易记录，无法删除");
        }
        criteria.setAccountNumberList(null);
        criteria.setBankNumberList(Arrays.asList(ids));
        if (CollectionUtils.isNotEmpty(sysProjectTransactionService.queryAll(criteria))) {
            throw new RuntimeException(Arrays.toString(ids) + "有交易记录，无法删除");
        }
        sysProjectAccountService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}