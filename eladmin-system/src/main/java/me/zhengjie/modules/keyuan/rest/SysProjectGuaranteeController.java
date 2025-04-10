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
import me.zhengjie.modules.keyuan.domain.SysGuaranteeData;
import me.zhengjie.modules.keyuan.domain.SysProjectGuarantee;
import me.zhengjie.modules.keyuan.service.SysProjectGuaranteeService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectGuaranteeDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectGuaranteeQueryCriteria;
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
 * @date 2025-04-11
 **/
@RestController
@RequiredArgsConstructor
@Api(tags = "项目担保管理")
@RequestMapping("/api/sysProjectGuarantee")
public class SysProjectGuaranteeController {

    private final SysProjectGuaranteeService sysProjectGuaranteeService;

    @Log("导出数据")
    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('sysProjectGuarantee:list')")
    public void exportSysProjectGuarantee(HttpServletResponse response, SysProjectGuaranteeQueryCriteria criteria) throws IOException {
        sysProjectGuaranteeService.download(sysProjectGuaranteeService.queryAll(criteria), response);
    }

    @GetMapping
    @Log("查询项目担保")
    @ApiOperation("查询项目担保")
    @PreAuthorize("@el.check('sysProjectGuarantee:list')")
    public ResponseEntity<PageResult<SysProjectGuaranteeDto>> querySysProjectGuarantee(SysProjectGuaranteeQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(sysProjectGuaranteeService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @GetMapping("/data")
    @Log("查询项目担保统计")
    @ApiOperation("查询项目担保统计")
    @PreAuthorize("@el.check('sysProjectGuarantee:list')")
    public ResponseEntity<SysGuaranteeData> querySysProjectGuaranteeData() {
        return new ResponseEntity<>(sysProjectGuaranteeService.getSysGuaranteeData(), HttpStatus.OK);
    }

    @PostMapping
    @Log("新增项目担保")
    @ApiOperation("新增项目担保")
    @PreAuthorize("@el.check('sysProjectGuarantee:add')")
    public ResponseEntity<Object> createSysProjectGuarantee(@Validated @RequestBody SysProjectGuarantee resources) {
        sysProjectGuaranteeService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改项目担保")
    @ApiOperation("修改项目担保")
    @PreAuthorize("@el.check('sysProjectGuarantee:edit')")
    public ResponseEntity<Object> updateSysProjectGuarantee(@Validated @RequestBody SysProjectGuarantee resources) {
        sysProjectGuaranteeService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除项目担保")
    @ApiOperation("删除项目担保")
    @PreAuthorize("@el.check('sysProjectGuarantee:del')")
    public ResponseEntity<Object> deleteSysProjectGuarantee(@RequestBody Long[] ids) {
        sysProjectGuaranteeService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}