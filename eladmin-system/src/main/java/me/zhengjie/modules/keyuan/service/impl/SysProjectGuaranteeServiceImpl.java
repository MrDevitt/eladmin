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
import me.zhengjie.modules.keyuan.domain.SysGuaranteeData;
import me.zhengjie.modules.keyuan.domain.SysProjectGuarantee;
import me.zhengjie.modules.keyuan.repository.SysProjectGuaranteeRepository;
import me.zhengjie.modules.keyuan.service.SysProjectGuaranteeService;
import me.zhengjie.modules.keyuan.service.SysProjectPersonService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectGuaranteeDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectGuaranteeQueryCriteria;
import me.zhengjie.modules.keyuan.service.dto.SysProjectPersonDto;
import me.zhengjie.modules.keyuan.service.mapstruct.SysProjectGuaranteeMapper;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author MrDevitt
 * @website https://eladmin.vip
 * @description 服务实现
 * @date 2025-04-11
 **/
@Service
@RequiredArgsConstructor
public class SysProjectGuaranteeServiceImpl implements SysProjectGuaranteeService {

    private final SysProjectGuaranteeRepository sysProjectGuaranteeRepository;
    private final SysProjectGuaranteeMapper sysProjectGuaranteeMapper;

    private final SysProjectPersonService sysProjectPersonService;

    private final SysProjectStatisticsService sysProjectStatisticsService;


    @Override
    public PageResult<SysProjectGuaranteeDto> queryAll(SysProjectGuaranteeQueryCriteria criteria, Pageable pageable) {
        Page<SysProjectGuarantee> page = sysProjectGuaranteeRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page.map(sysProjectGuaranteeMapper::toDto));
    }

    @Override
    public List<SysProjectGuaranteeDto> queryAll(SysProjectGuaranteeQueryCriteria criteria) {
        return sysProjectGuaranteeMapper.toDto(sysProjectGuaranteeRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder)));
    }

    @Override
    @Transactional
    public SysProjectGuaranteeDto findById(Long id) {
        SysProjectGuarantee sysProjectGuarantee = sysProjectGuaranteeRepository.findById(id).orElseGet(SysProjectGuarantee::new);
        ValidationUtil.isNull(sysProjectGuarantee.getId(), "SysProjectGuarantee", "id", id);
        return sysProjectGuaranteeMapper.toDto(sysProjectGuarantee);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(SysProjectGuarantee resources) {
        sysProjectGuaranteeRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysProjectGuarantee resources) {
        SysProjectGuarantee sysProjectGuarantee = sysProjectGuaranteeRepository.findById(resources.getId()).orElseGet(SysProjectGuarantee::new);
        ValidationUtil.isNull(sysProjectGuarantee.getId(), "SysProjectGuarantee", "id", resources.getId());
        sysProjectGuarantee.copy(resources);
        sysProjectGuaranteeRepository.save(sysProjectGuarantee);
    }

    @Override
    public void deleteAll(Long[] ids) {
        for (Long id : ids) {
            sysProjectGuaranteeRepository.deleteById(id);
        }
    }

    @Override
    public void download(List<SysProjectGuaranteeDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<Long, SysProjectPersonDto> personDtoMap = sysProjectPersonService.getIdToPersonMap();
        for (SysProjectGuaranteeDto sysProjectGuarantee : all) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("担保状态", ProjectUtils.PROJECT_GUARANTEE_STATUS[sysProjectGuarantee.getStatus()]);
            map.put("项目类型", ProjectUtils.PROJECT_TYPE_NAMES[sysProjectGuarantee.getProjectType()]);
            map.put("项目名", sysProjectGuarantee.getProjectName());
            map.put("甲方名称", sysProjectGuarantee.getPartyA());
            map.put("甲方联系人", personDtoMap.getOrDefault(sysProjectGuarantee.getPartyAPerson(), new SysProjectPersonDto()).getName());
            map.put("担保金额", ProjectUtils.dbPriceToRealPrice(sysProjectGuarantee.getGuaranteeAmount()));
            map.put("担保结束时间", sysProjectGuarantee.getGuaranteeTime());
            map.put("担保人员", personDtoMap.getOrDefault(sysProjectGuarantee.getGuaranteePerson(), new SysProjectPersonDto()).getName());
            map.put("备注", sysProjectGuarantee.getRemark());
            map.put("创建时间", sysProjectGuarantee.getCreateTime());
            map.put("修改时间", sysProjectGuarantee.getUpdateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public SysGuaranteeData getSysGuaranteeData() {
        SysGuaranteeData sysGuaranteeData = new SysGuaranteeData();
        SysProjectGuaranteeQueryCriteria criteria = new SysProjectGuaranteeQueryCriteria();
        criteria.setStatus(List.of(SysProjectGuaranteeDto.STATUS_NORMAL, SysProjectGuaranteeDto.STATUS_ABNORMAL));
        List<SysProjectGuaranteeDto> dtoList = queryAll(criteria);
        Map<Long, SysProjectPersonDto> personMap = sysProjectPersonService.getIdToPersonMap();
        Map<Long, Map<Integer, Integer>> guaranteeByPersonAndStatus = new HashMap<>();
        for (SysProjectGuaranteeDto dto : dtoList) {
            Map<Integer, Integer> statusMap = guaranteeByPersonAndStatus.computeIfAbsent(dto.getGuaranteePerson(), k -> new HashMap<>());
            Integer amount = statusMap.computeIfAbsent(dto.getStatus(), k -> 0);
            Integer totalAmount = statusMap.computeIfAbsent(-1, k -> 0);
            statusMap.put(dto.getStatus(), amount + dto.getGuaranteeAmount());
            statusMap.put(-1, totalAmount + dto.getGuaranteeAmount());
        }
        Map<Long, Long> remainingByPerson = sysProjectStatisticsService.getPersonRemainingMap();
        Map<Long, Long> balanceByPerson = sysProjectStatisticsService.getBalancePersonMap();
        List<Map<String, String>> tableData = new ArrayList<>();
        guaranteeByPersonAndStatus.forEach((k, v) -> {
            Map<String, String> data = new HashMap<>();
            data.put("name", personMap.get(k).getName());
            data.put("normal", String.format("%.2f", ProjectUtils.dbPriceToRealPrice(v.getOrDefault(SysProjectGuaranteeDto.STATUS_NORMAL, 0))));
            data.put("abnormal", String.format("%.2f", ProjectUtils.dbPriceToRealPrice(v.getOrDefault(SysProjectGuaranteeDto.STATUS_ABNORMAL, 0))));
            data.put("sum", String.format("%.2f", ProjectUtils.dbPriceToRealPrice(v.getOrDefault(-1, 0))));
            long remaining = remainingByPerson.getOrDefault(k, 0L) + balanceByPerson.getOrDefault(k, 0L) - v.getOrDefault(SysProjectGuaranteeDto.STATUS_NORMAL, 0) - v.getOrDefault(SysProjectGuaranteeDto.STATUS_ABNORMAL, 0);
            data.put("remaining", String.format("%.2f", ProjectUtils.dbPriceToRealPrice(remaining)));
            tableData.add(data);
        });
        tableData.sort(Comparator.comparing(a -> -Integer.parseInt(a.get("sum").substring(0, a.get("sum").length() - 3))));
        sysGuaranteeData.setTableData(tableData);
        return sysGuaranteeData;
    }
}