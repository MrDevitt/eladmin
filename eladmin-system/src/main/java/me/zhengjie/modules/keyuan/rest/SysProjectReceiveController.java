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
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.annotation.Log;
import me.zhengjie.modules.keyuan.domain.SysProjectReceive;
import me.zhengjie.modules.keyuan.service.SysProjectReceiveService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectReceiveDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectReceiveQueryCriteria;
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

/**
 * @author MrDevitt
 * @website https://eladmin.vip
 * @date 2024-07-03
 **/
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = "项目收款信息管理")
@RequestMapping("/api/sysProjectReceive")
public class SysProjectReceiveController {

    private final SysProjectReceiveService sysProjectReceiveService;

    @Log("导出数据")
    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('sysProjectReceive:list')")
    public void exportSysProjectReceive(HttpServletResponse response, SysProjectReceiveQueryCriteria criteria) throws IOException {
        sysProjectReceiveService.download(sysProjectReceiveService.queryAll(criteria), response);
    }

    @GetMapping
    @Log("查询项目收款信息")
    @ApiOperation("查询项目收款信息")
    @PreAuthorize("@el.check('sysProjectReceive:list')")
    public ResponseEntity<PageResult<SysProjectReceiveDto>> querySysProjectReceive(SysProjectReceiveQueryCriteria criteria, Pageable pageable) {
        log.info(criteria.toString());
        return new ResponseEntity<>(sysProjectReceiveService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @PostMapping
    @Log("新增项目收款信息")
    @ApiOperation("新增项目收款信息")
    @PreAuthorize("@el.check('sysProjectReceive:add')")
    public ResponseEntity<Object> createSysProjectReceive(@Validated @RequestBody SysProjectReceive resources) {
        sysProjectReceiveService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改项目收款信息")
    @ApiOperation("修改项目收款信息")
    @PreAuthorize("@el.check('sysProjectReceive:edit')")
    public ResponseEntity<Object> updateSysProjectReceive(@Validated @RequestBody SysProjectReceive resources) {
        sysProjectReceiveService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除项目收款信息")
    @ApiOperation("删除项目收款信息")
    @PreAuthorize("@el.check('sysProjectReceive:del')")
    public ResponseEntity<Object> deleteSysProjectReceive(@RequestBody Long[] ids) {
        sysProjectReceiveService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}