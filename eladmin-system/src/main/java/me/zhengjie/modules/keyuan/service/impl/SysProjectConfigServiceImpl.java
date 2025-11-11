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

import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import me.zhengjie.exception.EntityExistException;
import me.zhengjie.modules.keyuan.domain.SysProjectConfig;
import me.zhengjie.modules.keyuan.repository.SysProjectConfigRepository;
import me.zhengjie.modules.keyuan.service.SysProjectConfigService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectConfigDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectConfigQueryCriteria;
import me.zhengjie.modules.keyuan.service.mapstruct.SysProjectConfigMapper;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.PageResult;
import me.zhengjie.utils.PageUtil;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.ValidationUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author MrDevitt
 * @website https://eladmin.vip
 * @description 服务实现
 * @date 2025-11-14
 **/
@Service
@RequiredArgsConstructor
public class SysProjectConfigServiceImpl implements SysProjectConfigService {

    private final SysProjectConfigRepository sysProjectConfigRepository;
    private final SysProjectConfigMapper sysProjectConfigMapper;

    @Override
    public PageResult<SysProjectConfigDto> queryAll(SysProjectConfigQueryCriteria criteria, Pageable pageable) {
        Page<SysProjectConfig> page = sysProjectConfigRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page.map(sysProjectConfigMapper::toDto));
    }

    @Override
    public List<SysProjectConfigDto> queryAll(SysProjectConfigQueryCriteria criteria) {
        return sysProjectConfigMapper.toDto(sysProjectConfigRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder)));
    }

    @Override
    @Transactional
    public SysProjectConfigDto findById(Long id) {
        SysProjectConfig sysProjectConfig = sysProjectConfigRepository.findById(id).orElseGet(SysProjectConfig::new);
        ValidationUtil.isNull(sysProjectConfig.getId(), "SysProjectConfig", "id", id);
        return sysProjectConfigMapper.toDto(sysProjectConfig);
    }

    @Override
    @Transactional
    public <T> T findConfigByKey(String key, Class<T> tClass) {
        SysProjectConfigQueryCriteria criteria = new SysProjectConfigQueryCriteria();
        criteria.setConfigKey(key);
        List<SysProjectConfigDto> configDtoList = queryAll(criteria);
        if (CollectionUtils.isEmpty(configDtoList)) {
            return null;
        }
        return JSON.parseObject(configDtoList.get(0).getConfigValue(), tClass);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(SysProjectConfig resources) {
        if (sysProjectConfigRepository.findByConfigKey(resources.getConfigKey()) != null) {
            throw new EntityExistException(SysProjectConfig.class, "config_key", resources.getConfigKey());
        }
        sysProjectConfigRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysProjectConfig resources) {
        SysProjectConfig sysProjectConfig = sysProjectConfigRepository.findById(resources.getId()).orElseGet(SysProjectConfig::new);
        ValidationUtil.isNull(sysProjectConfig.getId(), "SysProjectConfig", "id", resources.getId());
        SysProjectConfig sysProjectConfig1 = null;
        sysProjectConfig1 = sysProjectConfigRepository.findByConfigKey(resources.getConfigKey());
        if (sysProjectConfig1 != null && !sysProjectConfig1.getId().equals(sysProjectConfig.getId())) {
            throw new EntityExistException(SysProjectConfig.class, "config_key", resources.getConfigKey());
        }
        sysProjectConfig.copy(resources);
        sysProjectConfigRepository.save(sysProjectConfig);
    }

    @Override
    public void deleteAll(Long[] ids) {
        for (Long id : ids) {
            sysProjectConfigRepository.deleteById(id);
        }
    }

    @Override
    public void download(List<SysProjectConfigDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (SysProjectConfigDto sysProjectConfig : all) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("配置键", sysProjectConfig.getConfigKey());
            map.put("配置值", sysProjectConfig.getConfigValue());
            map.put("描述", sysProjectConfig.getDescription());
            map.put("创建时间", sysProjectConfig.getCreateTime());
            map.put("修改时间", sysProjectConfig.getUpdateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}