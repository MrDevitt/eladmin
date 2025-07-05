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
import me.zhengjie.modules.keyuan.domain.statistics.SysProjectStatistics;
import me.zhengjie.modules.keyuan.repository.SysProjectDetailRepository;
import me.zhengjie.modules.keyuan.service.SysProjectDetailService;
import me.zhengjie.modules.keyuan.service.SysProjectPersonService;
import me.zhengjie.modules.keyuan.service.SysProjectReceiveService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectDetailQueryCriteria;
import me.zhengjie.modules.keyuan.service.dto.SysProjectPersonDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectReceiveDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectReceiveQueryCriteria;
import me.zhengjie.modules.keyuan.service.mapstruct.SysProjectDetailMapper;
import me.zhengjie.modules.keyuan.utils.ProjectUtils;
import me.zhengjie.service.LocalStorageService;
import me.zhengjie.service.dto.LocalStorageQueryCriteria;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.PageResult;
import me.zhengjie.utils.PageUtil;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.ValidationUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
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

    private final SysProjectReceiveService sysProjectReceiveService;

    private final SysProjectPersonService projectPersonService;

    private final LocalStorageService localStorageService;

    private Map<Long, SysProjectDetailDto> sysProjectDetailDtoMap = null;


    @Override
    public PageResult<SysProjectDetailDto> queryAll(SysProjectDetailQueryCriteria criteria, Pageable pageable) {
        updateQueryCriteria(criteria);
        Page<SysProjectDetail> page = sysProjectDetailRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page.map(sysProjectDetailMapper::toDto));
    }

    @Override
    public List<SysProjectDetailDto> queryAll(SysProjectDetailQueryCriteria criteria) {
        updateQueryCriteria(criteria);
        return sysProjectDetailMapper.toDto(sysProjectDetailRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder)));
    }

    @Override
    @Transactional
    public SysProjectDetailDto findById(Long id) {
        SysProjectDetail sysProjectDetail = sysProjectDetailRepository.findById(id).orElseGet(SysProjectDetail::new);
        ValidationUtil.isNull(sysProjectDetail.getId(), "SysProjectDetail", "id", id);
        return sysProjectDetailMapper.toDto(sysProjectDetail);
    }

    private void updateQueryCriteria(SysProjectDetailQueryCriteria criteria) {
        if (CollectionUtils.isNotEmpty(criteria.getReceiveTime())) {
            SysProjectReceiveQueryCriteria receiveQueryCriteria = new SysProjectReceiveQueryCriteria();
            receiveQueryCriteria.setReceiveTime(criteria.getReceiveTime());
            receiveQueryCriteria.setReceiveAmount(0);
            criteria.setIds(sysProjectReceiveService.queryAll(receiveQueryCriteria).stream().map(SysProjectReceiveDto::getProjectId).distinct().collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(criteria.getAttachmentStatus())) {
            List<Integer> attachmentStatus = criteria.getAttachmentStatus();
            LocalStorageQueryCriteria localStorageQueryCriteria = new LocalStorageQueryCriteria();
            localStorageQueryCriteria.setName1("_明细表");
            List<Long> detailIdList = localStorageService.queryAll(localStorageQueryCriteria)
                    .stream().map(e -> {
                        String[] strs = e.getName().split("_");
                        return Long.parseLong(strs[0]);
                    }).collect(Collectors.toList());
            localStorageQueryCriteria.setName1("_合同");
            List<Long> contractIdList = localStorageService.queryAll(localStorageQueryCriteria)
                    .stream().map(e -> {
                        String[] strs = e.getName().split("_");
                        return Long.parseLong(strs[0]);
                    }).collect(Collectors.toList());
            Set<Long> notInIdSet = new HashSet<>();
            if (attachmentStatus.contains(0)) {
                notInIdSet.addAll(contractIdList);
            }
            if (attachmentStatus.contains(1)) {
                notInIdSet.addAll(detailIdList);
            }
            criteria.setIdsNotIn(new ArrayList<>(notInIdSet));
            if (attachmentStatus.contains(2) && attachmentStatus.contains(3)) {
                criteria.setIds(new ArrayList<>(CollectionUtils.intersection(detailIdList, contractIdList)));
            } else if (attachmentStatus.contains(2)) {
                criteria.setIds(contractIdList);
            } else if (attachmentStatus.contains(3)) {
                criteria.setIds(detailIdList);
            }
        }
        if (StringUtils.isNotEmpty(criteria.getIdsStr())) {
            String[] ids = criteria.getIdsStr().split(",");
            List<Long> idsList = new ArrayList<>();
            for (String id : ids) {
                idsList.add(Long.parseLong(id));
            }
            criteria.setIds(idsList);
        }
        if (criteria.getReceiveFinished() != null) {
            if (criteria.getReceiveFinished()) {
                criteria.setReceiveAmount("contractAmount");
            } else {
                criteria.setReceiveAmountLess("contractAmount");
            }
        }
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
        SysProjectStatistics.CACHE = null;
        sysProjectDetailDtoMap = null;
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
        SysProjectStatistics.CACHE = null;
        sysProjectDetailDtoMap = null;
    }

    @Override
    public void deleteAll(Long[] ids) {
        Set<Long> receiveIdSet = new HashSet<>();
        for (Long id : ids) {
            sysProjectDetailRepository.deleteById(id);
            SysProjectReceiveQueryCriteria criteria = new SysProjectReceiveQueryCriteria();
            criteria.setProjectId(id);
            sysProjectReceiveService.queryAll(criteria).forEach(e -> receiveIdSet.add(e.getId()));
        }
        sysProjectReceiveService.deleteAll(receiveIdSet.toArray(new Long[0]));
        SysProjectStatistics.CACHE = null;
        sysProjectDetailDtoMap = null;
    }

    @Override
    public void download(List<SysProjectDetailDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<Long, SysProjectPersonDto> personDtoMap = projectPersonService.getIdToPersonMap();
        for (SysProjectDetailDto sysProjectDetail : all) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("项目类型", ProjectUtils.projectTypeToName(sysProjectDetail.getProjectType()));
            map.put("项目名", sysProjectDetail.getProjectName());
            map.put("项目区域", sysProjectDetail.getProjectRegion());
            map.put("甲方名称", sysProjectDetail.getPartyA());
            map.put("乙方名称", sysProjectDetail.getPartyB());
            map.put("合同编号", sysProjectDetail.getContractNumber());
            map.put("签订时间", sysProjectDetail.getContractTime());
            map.put("合同付款方式", ProjectUtils.PROJECT_PAY_WAYS[sysProjectDetail.getContractPayWay()]);
            map.put("合同金额", ProjectUtils.dbPriceToRealPrice(sysProjectDetail.getContractAmount()));
            map.put("已收款", ProjectUtils.dbPriceToRealPrice(sysProjectDetail.getReceiveAmount()));
            map.put("未收款", ProjectUtils.dbPriceToRealPrice(sysProjectDetail.getContractAmount()) - ProjectUtils.dbPriceToRealPrice(sysProjectDetail.getReceiveAmount()));
            map.put("项目进度", sysProjectDetail.getProjectProgress());
            map.put("应收款", ProjectUtils.dbPriceToRealPrice(sysProjectDetail.getShouldReceiveAmount()));
            map.put("业务人员", personDtoMap.get(sysProjectDetail.getSalesPerson()).getName());
            map.put("甲方负责人", personDtoMap.getOrDefault(sysProjectDetail.getPartyAPerson(), new SysProjectPersonDto()).getName());
            map.put("发票类型", ProjectUtils.PROJECT_INVOICE_NAMES[sysProjectDetail.getInvoiceType()]);
            map.put("备注", sysProjectDetail.getRemark());
            map.put("业务中心百分比", sysProjectDetail.getSalesPercent());
            map.put("技术中心百分比", sysProjectDetail.getTechnicalPercent());
            map.put("管理中心百分比", sysProjectDetail.getManagementPercent());
            map.put("总裁办百分比", sysProjectDetail.getPresidentPercent());
            map.put("创建时间", sysProjectDetail.getCreateTime());
            map.put("修改时间", sysProjectDetail.getUpdateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Map<Long, SysProjectDetailDto> getSysProjectDetailDtoMap() {
        if (sysProjectDetailDtoMap == null) {
            sysProjectDetailDtoMap = queryAll(new SysProjectDetailQueryCriteria()).stream().collect(Collectors.toMap(
                    SysProjectDetailDto::getId,
                    Function.identity(),
                    (x, y) -> x
            ));
        }
        return sysProjectDetailDtoMap;
    }

}