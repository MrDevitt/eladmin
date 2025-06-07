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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import me.zhengjie.annotation.Log;
import me.zhengjie.modules.keyuan.domain.SysProjectDetail;
import me.zhengjie.modules.keyuan.service.SysProjectDetailService;
import me.zhengjie.modules.keyuan.service.SysProjectReceiveService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailQueryCriteria;
import me.zhengjie.modules.keyuan.service.dto.SysProjectReceiveDto;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author MrDevitt
 * @website https://eladmin.vip
 * @date 2024-04-04
 **/
@RestController
@RequiredArgsConstructor
@Api(tags = "项目明细管理")
@RequestMapping("/api/sysProjectDetail")
public class SysProjectDetailController {

    private final SysProjectDetailService sysProjectDetailService;

    private final SysProjectReceiveService sysProjectReceiveService;


    @Log("导出数据")
    @ApiOperation("导出数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@el.check('sysProjectDetail:list')")
    public void exportSysProjectDetail(HttpServletResponse response, SysProjectDetailQueryCriteria criteria) throws IOException {
        sysProjectDetailService.download(sysProjectDetailService.queryAll(criteria), response);
    }

    @GetMapping
    @Log("查询项目明细")
    @ApiOperation("查询项目明细")
    @PreAuthorize("@el.check('sysProjectDetail:list')")
    public ResponseEntity<PageResult<SysProjectDetailDto>> querySysProjectDetail(SysProjectDetailQueryCriteria criteria, Pageable pageable) {
        PageResult<SysProjectDetailDto> res = sysProjectDetailService.queryAll(criteria, pageable);
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @GetMapping("/invoicedNotReceive")
    @Log("查询开票未收款项目明细")
    @ApiOperation("查询开票未收款项目明细")
    @PreAuthorize("@el.check('sysProjectDetail:list')")
    public ResponseEntity<PageResult<JSONObject>> queryInvoicedNotReceiveDetail(SysProjectDetailQueryCriteria criteria, Pageable pageable) {
        Map<Long, Integer> invoicedNotReceiveMap = new HashMap<>();
        List<SysProjectReceiveDto> receiveDtoList = sysProjectReceiveService.queryInvoicedNotReceive();
        for (SysProjectReceiveDto receiveDto : receiveDtoList) {
            int amount = receiveDto.getInvoiceAmount() - receiveDto.getReceiveAmount();
            long projectId = receiveDto.getProjectId();
            invoicedNotReceiveMap.put(projectId, invoicedNotReceiveMap.getOrDefault(projectId, 0) + amount);
        }
        List<Long> ids = receiveDtoList.stream().map(SysProjectReceiveDto::getProjectId).collect(Collectors.toList());
        criteria.setIds(ids);
        PageResult<SysProjectDetailDto> detailDtoPageResult = sysProjectDetailService.queryAll(criteria, pageable);
        PageResult<JSONObject> res = detailDtoPageResult.map(e -> {
            JSONObject o = JSON.parseObject(JSON.toJSONString(e));
            o.put("invoiceNotReceiveAmount", invoicedNotReceiveMap.get(e.getId()));
            return o;
        });
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PostMapping
    @Log("新增项目明细")
    @ApiOperation("新增项目明细")
    @PreAuthorize("@el.check('sysProjectDetail:add')")
    public ResponseEntity<Object> createSysProjectDetail(@Validated @RequestBody SysProjectDetail resources) {
        sysProjectDetailService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping
    @Log("修改项目明细")
    @ApiOperation("修改项目明细")
    @PreAuthorize("@el.check('sysProjectDetail:edit','sysProjectDetail:editProgress')")
    public ResponseEntity<Object> updateSysProjectDetail(@Validated @RequestBody SysProjectDetail resources) {
        sysProjectDetailService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping
    @Log("删除项目明细")
    @ApiOperation("删除项目明细")
    @PreAuthorize("@el.check('sysProjectDetail:del')")
    public ResponseEntity<Object> deleteSysProjectDetail(@RequestBody Long[] ids) {
        sysProjectDetailService.deleteAll(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}