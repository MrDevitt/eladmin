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
import me.zhengjie.modules.keyuan.domain.SysProjectAccount;
import me.zhengjie.modules.keyuan.repository.SysProjectAccountRepository;
import me.zhengjie.modules.keyuan.service.SysProjectAccountService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectAccountDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectAccountQueryCriteria;
import me.zhengjie.modules.keyuan.service.mapstruct.SysProjectAccountMapper;
import me.zhengjie.modules.keyuan.utils.ProjectUtils;
import me.zhengjie.utils.FileUtil;
import me.zhengjie.utils.PageResult;
import me.zhengjie.utils.PageUtil;
import me.zhengjie.utils.QueryHelp;
import me.zhengjie.utils.SecurityUtils;
import me.zhengjie.utils.ValidationUtil;
import org.apache.commons.collections4.CollectionUtils;
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
 * @date 2025-11-11
 **/
@Service
@RequiredArgsConstructor
public class SysProjectAccountServiceImpl implements SysProjectAccountService {

    private final SysProjectAccountRepository sysProjectAccountRepository;
    private final SysProjectAccountMapper sysProjectAccountMapper;

    @Override
    public PageResult<SysProjectAccountDto> queryAll(SysProjectAccountQueryCriteria criteria, Pageable pageable) {
        if (ProjectUtils.allFieldsNull(criteria)) {
            criteria.setNullParent(true);
        }
        Page<SysProjectAccount> page = sysProjectAccountRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page.map(sysProjectAccountMapper::toDto));
    }

    @Override
    public List<SysProjectAccountDto> queryAll(SysProjectAccountQueryCriteria criteria) {
        return sysProjectAccountMapper.toDto(sysProjectAccountRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder)));
    }

    @Override
    @Transactional
    public SysProjectAccountDto findById(Long accountNumber) {
        SysProjectAccount sysProjectAccount = sysProjectAccountRepository.findById(accountNumber).orElse(null);
        if (sysProjectAccount == null) {
            return null;
        }
        ValidationUtil.isNull(sysProjectAccount.getAccountNumber(), "SysProjectAccount", "accountNumber", accountNumber);
        return sysProjectAccountMapper.toDto(sysProjectAccount);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(SysProjectAccount resources) {
        if (resources.getAccountNumber() > 9007199254740991L) {//前端js能处理的最大数
            throw new RuntimeException("科目编号过长");
        }
        checkPrefix(resources);
        if (findById(resources.getAccountNumber()) != null) {
            throw new RuntimeException("该科目编号已存在,请检查后重新修改！");
        }
        if (resources.getParent() != null) {
            SysProjectAccount parent = sysProjectAccountRepository.findById(resources.getParent()).orElse(null);
            if (parent == null) {
                throw new RuntimeException("父科目不存在,请检查后重新修改！");
            }
            if (!parent.getHasChildren()) {
                parent.setHasChildren(true);
                sysProjectAccountRepository.save(parent);
            }
        }
        resources.setCreateBy(SecurityUtils.getCurrentUsername());
        resources.setHasChildren(false);
        sysProjectAccountRepository.save(resources);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysProjectAccount resources) {
        SysProjectAccount sysProjectAccount = sysProjectAccountRepository.findById(resources.getAccountNumber()).orElseGet(SysProjectAccount::new);
        ValidationUtil.isNull(sysProjectAccount.getAccountNumber(), "SysProjectAccount", "id", resources.getAccountNumber());
        sysProjectAccount.copy(resources);
        Map<Long, SysProjectAccount> accountMap = queryAll(new SysProjectAccountQueryCriteria()).stream().map(sysProjectAccountMapper::toEntity).collect(Collectors.toMap(
                SysProjectAccount::getAccountNumber,
                Function.identity(),
                (x, y) -> x
        ));
        checkPrefix(sysProjectAccount);
        if (hasCircle(accountMap, sysProjectAccount.getAccountNumber(), new HashSet<>())) {
            throw new RuntimeException("父科目成环,请检查后重新修改！");
        }
        sysProjectAccount.setUpdateBy(SecurityUtils.getCurrentUsername());
        sysProjectAccountRepository.save(sysProjectAccount);
    }

    private boolean hasCircle(
            Map<Long, SysProjectAccount> accountMap,
            Long current,
            Set<Long> currentPath) {
        if (currentPath.contains(current)) {
            return true;
        }
        // 标记为当前路径中
        currentPath.add(current);
        // 获取当前节点的父节点
        SysProjectAccount currentAccount = accountMap.get(current);
        Long parent = currentAccount.getParent();
        // 如果父节点为null，是根节点，无环
        if (parent == null) {
            return false;
        }
        // 如果父节点不存在（数据不一致），视为无环
        if (!accountMap.containsKey(parent)) {
            currentPath.remove(current);
            return false;
        }
        // 递归检查父节点
        boolean hasCycle = hasCircle(accountMap, parent, currentPath);
        // 回溯：从当前路径移除
        currentPath.remove(current);
        return hasCycle;
    }

    private void checkPrefix(SysProjectAccount account) {
        if (account.getParent() != null) {
            if (!account.getAccountNumber().toString().startsWith(account.getParent().toString())) {
                throw new RuntimeException("科目" + account.getAccountNumber() + "的编号起始与父科目不一致");
            }
        }
    }

    @Override
    public void deleteAll(Long[] ids) {
        for (Long accountNumber : ids) {
            SysProjectAccountQueryCriteria criteria = new SysProjectAccountQueryCriteria();
            criteria.setParent(accountNumber);
            if (CollectionUtils.isNotEmpty(queryAll(criteria))) {
                throw new RuntimeException("科目" + accountNumber + "仍有子科目，无法删除");
            }
            Long parentId = findById(accountNumber).getParent();
            sysProjectAccountRepository.deleteById(accountNumber);
            updateParent(parentId);
        }
    }

    private void updateParent(Long parentId) {
        SysProjectAccountDto parent = findById(parentId);
        if (parent == null) {
            return;
        }
        SysProjectAccountQueryCriteria criteria = new SysProjectAccountQueryCriteria();
        criteria.setParent(parentId);
        if (CollectionUtils.isEmpty(queryAll(criteria))) {
            parent.setHasChildren(false);
            sysProjectAccountRepository.save(sysProjectAccountMapper.toEntity(parent));
        }
    }

    @Override
    public void download(List<SysProjectAccountDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (SysProjectAccountDto sysProjectAccount : all) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("科目含义", sysProjectAccount.getDescription());
            map.put("上级科目", sysProjectAccount.getParent());
            map.put("初始余额", sysProjectAccount.getInitialAmount());
            map.put("创建人", sysProjectAccount.getCreateBy());
            map.put("修改人", sysProjectAccount.getUpdateBy());
            map.put("创建时间", sysProjectAccount.getCreateTime());
            map.put("修改时间", sysProjectAccount.getUpdateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }
}