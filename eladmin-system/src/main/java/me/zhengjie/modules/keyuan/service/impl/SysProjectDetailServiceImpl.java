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
import lombok.extern.slf4j.Slf4j;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.modules.keyuan.domain.SysProjectDetail;
import me.zhengjie.modules.keyuan.domain.SysProjectStatistics;
import me.zhengjie.modules.keyuan.repository.SysProjectDetailRepository;
import me.zhengjie.modules.keyuan.repository.SysProjectReceiveRepository;
import me.zhengjie.modules.keyuan.service.SysProjectDetailService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailQueryCriteria;
import me.zhengjie.modules.keyuan.service.dto.SysProjectReceiveDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectReceiveQueryCriteria;
import me.zhengjie.modules.keyuan.service.mapstruct.SysProjectDetailMapper;
import me.zhengjie.modules.keyuan.service.mapstruct.SysProjectReceiveMapper;
import me.zhengjie.modules.keyuan.utils.ProjectUtils;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author MrDevitt
 * @website https://eladmin.vip
 * @description 服务实现
 * @date 2024-04-04
 **/
@Slf4j
@Service
@RequiredArgsConstructor
public class SysProjectDetailServiceImpl implements SysProjectDetailService {

    private final SysProjectDetailRepository sysProjectDetailRepository;
    private final SysProjectDetailMapper sysProjectDetailMapper;

    private final SysProjectReceiveRepository sysProjectReceiveRepository;

    private final SysProjectReceiveMapper sysProjectReceiveMapper;

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
        Set<Long> receiveIdSet = new HashSet<>();
        for (Long id : ids) {
            sysProjectDetailRepository.deleteById(id);
            SysProjectReceiveQueryCriteria criteria = new SysProjectReceiveQueryCriteria(id);
            sysProjectReceiveRepository
                    .findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder))
                    .forEach(e -> receiveIdSet.add(e.getId()));
        }
        sysProjectReceiveRepository.deleteAllById(receiveIdSet);
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
            map.put("开工时间", sysProjectDetail.getProjectStartTime());
            map.put("竣工时间", sysProjectDetail.getProjectFinishTime());
            map.put("业务人员", sysProjectDetail.getSalesPerson());
            map.put("技术人员", sysProjectDetail.getTechnicalPerson());
            map.put("甲方负责人", sysProjectDetail.getPartyAPerson());
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
            map.put(" projectRegion", sysProjectDetail.getProjectRegion());
            map.put("签订时间", sysProjectDetail.getContractTime());
            map.put("合同收到时间", sysProjectDetail.getContractReceiveTime());
            map.put("合同付款方式 0-签合同50，完工结清；1-一次性付清；2-签合同30进度50付30完工结清；3-按进度拨付", sysProjectDetail.getContractPayWay());
            map.put("应收款金额", sysProjectDetail.getShouldReceiveAmount());
            map.put("项目进度", sysProjectDetail.getProjectProgress());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public SysProjectStatistics getSysProjectStatisticsInfo() {
        SysProjectStatistics sysProjectStatistics = new SysProjectStatistics();
        List<SysProjectDetailDto> sysProjectDetailDtoList = queryAll(new SysProjectDetailQueryCriteria());
        buildContractStatistics(sysProjectStatistics, sysProjectDetailDtoList);

        Map<Long, SysProjectDetailDto> sysProjectDetailDtoMap = sysProjectDetailDtoList.stream().collect(Collectors.toMap(
                SysProjectDetailDto::getId,
                Function.identity(),
                (x, y) -> x
        ));
        List<SysProjectReceiveDto> sysProjectReceiveDtoList = sysProjectReceiveMapper.toDto(sysProjectReceiveRepository.findAll());
        buildReceiveStatistics(sysProjectStatistics, sysProjectReceiveDtoList, sysProjectDetailDtoMap);

        sysProjectStatistics.calcInnerData();
        return sysProjectStatistics;
    }

    private static void buildContractStatistics(SysProjectStatistics sysProjectStatistics, List<SysProjectDetailDto> sysProjectDetailDtoList) {
        for (SysProjectDetailDto detailDto : sysProjectDetailDtoList) {
            double contractAmount = ProjectUtils.dbPriceToRealPrice(detailDto.getContractAmount());

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(detailDto.getContractTime().getTime());
            int month = calendar.get(Calendar.MONTH);
            String year = String.valueOf(calendar.get(Calendar.YEAR));

            Map<String, double[]> typeMap = sysProjectStatistics.getContractByYearAndType().computeIfAbsent(year, k -> new HashMap<>());
            double[] typeMonthData = typeMap.computeIfAbsent(ProjectUtils.projectTypeToName(detailDto.getProjectType()), k -> new double[13]);
            typeMonthData[month] += contractAmount;
            typeMonthData[12] += contractAmount;
        }
    }

    private static void buildReceiveStatistics(
            SysProjectStatistics sysProjectStatistics,
            List<SysProjectReceiveDto> sysProjectReceiveDtoList,
            Map<Long, SysProjectDetailDto> sysProjectDetailDtoMap) {
        for (SysProjectReceiveDto receiveDto : sysProjectReceiveDtoList) {
            double receiveAmount = ProjectUtils.dbPriceToRealPrice(receiveDto.getReceiveAmount());
            SysProjectDetailDto detailDto = sysProjectDetailDtoMap.get(receiveDto.getProjectId());
            if (detailDto == null) {
                log.error("收款数据不存在！ receiveDto:{}", receiveDto);
                continue;
            }

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(receiveDto.getReceiveTime().getTime());
            int month = calendar.get(Calendar.MONTH);
            String year = String.valueOf(calendar.get(Calendar.YEAR));

            Map<String, double[]> typeMap = sysProjectStatistics.getReceiveByYearAndType().computeIfAbsent(year, k -> new HashMap<>());
            double[] typeMonthData = typeMap.computeIfAbsent(ProjectUtils.projectTypeToName(detailDto.getProjectType()), k -> new double[13]);
            typeMonthData[month] += receiveAmount;
            typeMonthData[12] += receiveAmount;
        }
    }
}