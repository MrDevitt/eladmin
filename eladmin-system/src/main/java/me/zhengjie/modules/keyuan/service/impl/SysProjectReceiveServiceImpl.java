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
package me.zhengjie.modules.keyuan.service.impl;

import lombok.RequiredArgsConstructor;
import me.zhengjie.modules.keyuan.domain.SysProjectDetail;
import me.zhengjie.modules.keyuan.domain.SysProjectReceive;
import me.zhengjie.modules.keyuan.domain.statistics.SysProjectStatistics;
import me.zhengjie.modules.keyuan.repository.SysProjectDetailRepository;
import me.zhengjie.modules.keyuan.repository.SysProjectReceiveRepository;
import me.zhengjie.modules.keyuan.service.SysProjectReceiveService;
import me.zhengjie.modules.keyuan.service.SysProjectTransactionService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectReceiveDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectReceiveQueryCriteria;
import me.zhengjie.modules.keyuan.service.mapstruct.SysProjectReceiveMapper;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.PageResult;
import me.zhengjie.utils.PageUtil;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.ValidationUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author MrDevitt
 * @website https://eladmin.vip
 * @description 服务实现
 * @date 2024-07-03
 **/
@Service
@RequiredArgsConstructor
public class SysProjectReceiveServiceImpl implements SysProjectReceiveService {

    private final SysProjectReceiveRepository sysProjectReceiveRepository;
    private final SysProjectReceiveMapper sysProjectReceiveMapper;

    private final SysProjectDetailRepository sysProjectDetailRepository;

    private final SysProjectTransactionService sysProjectTransactionService;


    @Override
    public PageResult<SysProjectReceiveDto> queryAll(SysProjectReceiveQueryCriteria criteria, Pageable pageable) {
        Page<SysProjectReceive> page = sysProjectReceiveRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page.map(sysProjectReceiveMapper::toDto));
    }

    @Override
    public List<SysProjectReceiveDto> queryAll(SysProjectReceiveQueryCriteria criteria) {
        return sysProjectReceiveMapper.toDto(sysProjectReceiveRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder)));
    }

    @Override
    public List<SysProjectReceiveDto> queryInvoicedNotReceive() {
        return sysProjectReceiveMapper.toDto(sysProjectReceiveRepository.findInvoicedNotReceive());
    }

    @Override
    @Transactional
    public SysProjectReceiveDto findById(Long id) {
        SysProjectReceive sysProjectReceive = sysProjectReceiveRepository.findById(id).orElseGet(SysProjectReceive::new);
        ValidationUtil.isNull(sysProjectReceive.getId(), "SysProjectReceive", "id", id);
        return sysProjectReceiveMapper.toDto(sysProjectReceive);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(SysProjectReceive resources) {
        SysProjectReceive saved = sysProjectReceiveRepository.saveAndFlush(resources);//确保立即更新，保证updateReceiveAmount中读取到最新数据
        if (saved.getReceiveAmount() > 0L) {//返回值才会有Id
            sysProjectTransactionService.updateTransactionByReceive(sysProjectReceiveMapper.toDto(saved), true);
        }
        updateReceiveAmount(resources.getProjectId());
        SysProjectStatistics.CACHE_MAP.clear();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysProjectReceive resources) {
        SysProjectReceive sysProjectReceive = sysProjectReceiveRepository.findById(resources.getId()).orElseGet(SysProjectReceive::new);
        ValidationUtil.isNull(sysProjectReceive.getId(), "SysProjectReceive", "id", resources.getId());
        sysProjectReceive.copy(resources);
        sysProjectTransactionService.updateTransactionByReceive(sysProjectReceiveMapper.toDto(sysProjectReceive), false);
        sysProjectReceiveRepository.saveAndFlush(sysProjectReceive);//确保立即更新，保证updateReceiveAmount中读取到最新数据
        updateReceiveAmount(sysProjectReceive.getProjectId());
        SysProjectStatistics.CACHE_MAP.clear();
    }

    @Override
    public void deleteAll(Long[] ids) {
        sysProjectTransactionService.deleteTransactionByReceive(ids);
        Set<Long> projectIdSet = new HashSet<>();
        for (Long id : ids) {
            long projectId = sysProjectReceiveRepository.findById(id)
                    .map(SysProjectReceive::getProjectId)
                    .orElse(-1L);
            projectIdSet.add(projectId);
            sysProjectReceiveRepository.deleteById(id);
        }
        projectIdSet.forEach(this::updateReceiveAmount);
        SysProjectStatistics.CACHE_MAP.clear();
    }

    @Override
    public void download(List<SysProjectReceiveDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (SysProjectReceiveDto sysProjectReceive : all) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("项目ID", sysProjectReceive.getProjectId());
            map.put("开票金额", sysProjectReceive.getInvoiceAmount());
            map.put("开票时间", sysProjectReceive.getInvoiceTime());
            map.put("到账金额", sysProjectReceive.getReceiveAmount());
            map.put("到账时间", sysProjectReceive.getReceiveTime());
            map.put("记录创建的时间", sysProjectReceive.getCreateTime());
            map.put("记录修改的时间", sysProjectReceive.getUpdateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    private void updateReceiveAmount(long projectId) {
        SysProjectDetail sysProjectDetail = sysProjectDetailRepository.findById(projectId).orElse(null);
        if (sysProjectDetail == null) {
            return;
        }
        SysProjectReceiveQueryCriteria criteria = new SysProjectReceiveQueryCriteria();
        criteria.setProjectId(projectId);
        List<SysProjectReceiveDto> receiveDtoList = queryAll(criteria);
        long totalReceive = receiveDtoList.stream().mapToLong(SysProjectReceiveDto::getReceiveAmount).sum();
        sysProjectDetail.setReceiveAmount(totalReceive);
        sysProjectDetailRepository.save(sysProjectDetail);
    }
}