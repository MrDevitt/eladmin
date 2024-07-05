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
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.modules.keyuan.domain.SysProjectDetail;
import me.zhengjie.modules.keyuan.domain.SysProjectStatistics;
import me.zhengjie.modules.keyuan.repository.SysProjectDetailRepository;
import me.zhengjie.modules.keyuan.service.SysProjectDetailService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailQueryCriteria;
import me.zhengjie.modules.keyuan.service.mapstruct.SysProjectDetailMapper;
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author MrDevitt
 * @website https://eladmin.vip
 * @description 服务实现
 * @date 2024-04-04
 **/
@Service
@RequiredArgsConstructor
public class SysProjectDetailServiceImpl implements SysProjectDetailService {

    private final SysProjectDetailRepository sysProjectDetailRepository;
    private final SysProjectDetailMapper sysProjectDetailMapper;

    @Override
    public PageResult<SysProjectDetailDto> queryAll(SysProjectDetailQueryCriteria criteria, Pageable pageable) {
        Page<SysProjectDetail> page = sysProjectDetailRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page.map(sysProjectDetailMapper::toDto));
    }

    @Override
    public List<SysProjectDetailDto> queryAll(SysProjectDetailQueryCriteria criteria) {
        return sysProjectDetailMapper.toDto(sysProjectDetailRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder)));
    }

    @Override
    @Transactional
    public SysProjectDetailDto findById(Long id) {
        SysProjectDetail sysProjectDetail = sysProjectDetailRepository.findById(id).orElseGet(SysProjectDetail::new);
        ValidationUtil.isNull(sysProjectDetail.getId(), "SysProjectDetail", "id", id);
        return sysProjectDetailMapper.toDto(sysProjectDetail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(SysProjectDetail resources) {
        if (resources.totalPercentage() != 100) {
            throw new EntityExistException(SysProjectDetail.class, "total_percentage", String.valueOf(resources.totalPercentage()));
        }
        if (sysProjectDetailRepository.findByContractNumber(resources.getContractNumber()) != null) {
            throw new EntityExistException(SysProjectDetail.class, "contract_number", resources.getContractNumber());
        }
        sysProjectDetailRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysProjectDetail resources) {
        if (resources.totalPercentage() != 100) {
            throw new EntityExistException(SysProjectDetail.class, "total_percentage", String.valueOf(resources.totalPercentage()));
        }
        SysProjectDetail sysProjectDetail = sysProjectDetailRepository.findById(resources.getId()).orElseGet(SysProjectDetail::new);
        ValidationUtil.isNull(sysProjectDetail.getId(), "SysProjectDetail", "id", resources.getId());
        SysProjectDetail sysProjectDetail1 = sysProjectDetailRepository.findByContractNumber(resources.getContractNumber());
        if (sysProjectDetail1 != null && !sysProjectDetail1.getId().equals(sysProjectDetail.getId())) {
            throw new EntityExistException(SysProjectDetail.class, "contract_number", resources.getContractNumber());
        }
        sysProjectDetail.copy(resources);
        sysProjectDetailRepository.save(sysProjectDetail);
    }

    @Override
    public void deleteAll(Long[] ids) {
        for (Long id : ids) {
            sysProjectDetailRepository.deleteById(id);
        }
    }

    @Override
    public void download(List<SysProjectDetailDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (SysProjectDetailDto sysProjectDetail : all) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("项目类型 0-检测，1-监理，2-设计", sysProjectDetail.getProjectType());
            map.put("项目名", sysProjectDetail.getProjectName());
            map.put("甲方名称", sysProjectDetail.getPartyA());
            map.put("乙方名称", sysProjectDetail.getPartyB());
            map.put("合同编号", sysProjectDetail.getContractNumber());
            map.put("合同金额", sysProjectDetail.getContractAmount());
            map.put("签订时间", sysProjectDetail.getContractTime());
            map.put("开工时间", sysProjectDetail.getProjectStartTime());
            map.put("竣工时间", sysProjectDetail.getProjectFinishTime());
            map.put("业务人员", sysProjectDetail.getSalesPerson());
            map.put("技术人员", sysProjectDetail.getTechnicalPerson());
            map.put("甲方负责人", sysProjectDetail.getPartyAPerson());
            map.put("甲方领导", sysProjectDetail.getPartyALeader());
            map.put("发票类型 0-专票，1-普票", sysProjectDetail.getInvoiceType());
            map.put("备注", sysProjectDetail.getRemark());
            map.put("业务中心百分比", sysProjectDetail.getSalesPercent());
            map.put("技术中心百分比", sysProjectDetail.getTechnicalPercent());
            map.put("管理中心百分比", sysProjectDetail.getManagementPercent());
            map.put("总裁办百分比", sysProjectDetail.getPresidentPercent());
            map.put("收款金额", sysProjectDetail.getReceiveAmount());
            map.put("0-未删除，1-已删除", sysProjectDetail.getIsDeleted());
            map.put("记录创建的时间", sysProjectDetail.getCreateTime());
            map.put("记录修改的时间", sysProjectDetail.getUpdateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public SysProjectStatistics getSysProjectStatisticsInfo(long end) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 0);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return getSysProjectStatisticsInfo(calendar.getTimeInMillis(), end);
    }

    private SysProjectStatistics getSysProjectStatisticsInfo(long begin, long end) {
        SysProjectDetailQueryCriteria criteria = new SysProjectDetailQueryCriteria();
        criteria.setBeginContractTime(new Timestamp(begin));
        criteria.setEndContractTime(new Timestamp(end));
        List<SysProjectDetailDto> sysProjectDetailDtoList = queryAll(criteria);
        SysProjectStatistics sysProjectStatistics = new SysProjectStatistics();
        for (SysProjectDetailDto detailDto : sysProjectDetailDtoList) {
            buildContractStatistics(sysProjectStatistics, detailDto);
        }
        return sysProjectStatistics;
    }

    private static void buildContractStatistics(SysProjectStatistics sysProjectStatistics, SysProjectDetailDto detailDto) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(detailDto.getContractTime().getTime());
        int month = calendar.get(Calendar.MONTH);

        Map<String, long[]> typeMap = sysProjectStatistics.getContractByTypeAndRegion().computeIfAbsent(detailDto.getProjectType(), k -> new HashMap<>());
        long[] regionMonthData = typeMap.computeIfAbsent(detailDto.getProjectRegion(), k -> new long[13]);
        regionMonthData[month] += detailDto.getContractAmount();
        regionMonthData[12] += detailDto.getContractAmount();

        Map<Integer, long[]> personMap = sysProjectStatistics.getContractByPersonAndType().computeIfAbsent(detailDto.getSalesPerson(), k -> new HashMap<>());
        long[] typeMonthData = personMap.computeIfAbsent(detailDto.getProjectType(), k -> new long[13]);
        typeMonthData[month] += detailDto.getContractAmount();
        typeMonthData[12] += detailDto.getContractAmount();
    }
}