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
import me.zhengjie.modules.keyuan.domain.SysProjectPerson;
import me.zhengjie.modules.keyuan.service.SysProjectPersonService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectPersonDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectPersonQueryCriteria;
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
 * @date 2024-04-04
 **/
@RestController
@RequiredArgsConstructor
@Api(tags = "项目人员管理")
@RequestMapping("/api/sysProjectPerson")
public class SysProjectPersonController {

    private final SysProjectPersonService sysProjectPersonService;

    @Log("导出数据")
    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('sysProjectPerson:list')")
    public void exportSysProjectPerson(HttpServletResponse response, SysProjectPersonQueryCriteria criteria) throws IOException {
        sysProjectPersonService.download(sysProjectPersonService.queryAll(criteria), response);
    }

    @GetMapping
    @Log("查询项目人员")
    @ApiOperation("查询项目人员")
    @PreAuthorize("@el.check('sysProjectPerson:list')")
    public ResponseEntity<PageResult<SysProjectPersonDto>> querySysProjectPerson(SysProjectPersonQueryCriteria criteria, Pageable pageable) {
        return new ResponseEntity<>(sysProjectPersonService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @PostMapping
    @Log("新增项目人员")
    @ApiOperation("新增项目人员")
    @PreAuthorize("@el.check('sysProjectPerson:add')")
    public ResponseEntity<Object> createSysProjectPerson(@Validated @RequestBody SysProjectPerson resources) {
        sysProjectPersonService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改项目人员")
    @ApiOperation("修改项目人员")
    @PreAuthorize("@el.check('sysProjectPerson:edit')")
    public ResponseEntity<Object> updateSysProjectPerson(@Validated @RequestBody SysProjectPerson resources) {
        sysProjectPersonService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除项目人员")
    @ApiOperation("删除项目人员")
    @PreAuthorize("@el.check('sysProjectPerson:del')")
    public ResponseEntity<Object> deleteSysProjectPerson(@RequestBody Long[] ids) {
        sysProjectPersonService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}