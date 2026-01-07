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
import me.zhengjie.modules.keyuan.domain.SysProjectPerson;
import me.zhengjie.modules.keyuan.repository.SysProjectPersonRepository;
import me.zhengjie.modules.keyuan.service.SysProjectPersonService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectPersonDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectPersonQueryCriteria;
import me.zhengjie.modules.keyuan.service.mapstruct.SysProjectPersonMapper;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author MrDevitt
 * @website https://eladmin.vip
 * @description 服务实现
 * @date 2024-04-04
 **/
@Service
@RequiredArgsConstructor
public class SysProjectPersonServiceImpl implements SysProjectPersonService {

    private final SysProjectPersonRepository sysProjectPersonRepository;
    private final SysProjectPersonMapper sysProjectPersonMapper;

    private static Map<Long, SysProjectPersonDto> idToPersonMap = null;
    private static Map<Long, SysProjectPersonDto> accountNumberToPersonMap = null;

    @Override
    public PageResult<SysProjectPersonDto> queryAll(SysProjectPersonQueryCriteria criteria, Pageable pageable) {
        Page<SysProjectPerson> page = sysProjectPersonRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page.map(sysProjectPersonMapper::toDto));
    }

    @Override
    public List<SysProjectPersonDto> queryAll(SysProjectPersonQueryCriteria criteria) {
        return sysProjectPersonMapper.toDto(sysProjectPersonRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder)));
    }

    @Override
    @Transactional
    public SysProjectPersonDto findById(Long id) {
        SysProjectPerson sysProjectPerson = sysProjectPersonRepository.findById(id).orElseGet(SysProjectPerson::new);
        ValidationUtil.isNull(sysProjectPerson.getId(), "SysProjectPerson", "id", id);
        return sysProjectPersonMapper.toDto(sysProjectPerson);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(SysProjectPerson resources) {
        sysProjectPersonRepository.save(resources);
        idToPersonMap = null;
        accountNumberToPersonMap = null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysProjectPerson resources) {
        SysProjectPerson sysProjectPerson = sysProjectPersonRepository.findById(resources.getId()).orElseGet(SysProjectPerson::new);
        ValidationUtil.isNull(sysProjectPerson.getId(), "SysProjectPerson", "id", resources.getId());
        sysProjectPerson.copy(resources);
        sysProjectPersonRepository.save(sysProjectPerson);
        idToPersonMap = null;
        accountNumberToPersonMap = null;
    }

    @Override
    public void deleteAll(Long[] ids) {
        for (Long id : ids) {
            sysProjectPersonRepository.deleteById(id);
        }
        idToPersonMap = null;
        accountNumberToPersonMap = null;
    }

    @Override
    public void download(List<SysProjectPersonDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (SysProjectPersonDto sysProjectPerson : all) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("姓名", sysProjectPerson.getName());
            map.put("手机号", sysProjectPerson.getPhoneNumber());
            map.put("记录创建的时间", sysProjectPerson.getCreateTime());
            map.put("记录修改的时间", sysProjectPerson.getUpdateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public Map<Long, SysProjectPersonDto> getIdToPersonMap() {
        if (idToPersonMap == null) {
            List<SysProjectPersonDto> sysProjectPersonDtoList = queryAll(new SysProjectPersonQueryCriteria());
            idToPersonMap = sysProjectPersonDtoList.stream().collect(Collectors.toMap(
                    SysProjectPersonDto::getId,
                    Function.identity(),
                    (x, y) -> x
            ));
        }
        return idToPersonMap;
    }

    @Override
    public Map<Long, SysProjectPersonDto> getAccountNumberToPersonMap() {
        if (accountNumberToPersonMap == null) {
            List<SysProjectPersonDto> sysProjectPersonDtoList = queryAll(new SysProjectPersonQueryCriteria());
            accountNumberToPersonMap = sysProjectPersonDtoList.stream().collect(Collectors.toMap(
                    e -> e.getAccountNumber() == null ? -1L : e.getAccountNumber(),
                    Function.identity(),
                    (x, y) -> x
            ));
        }
        return accountNumberToPersonMap;
    }

}