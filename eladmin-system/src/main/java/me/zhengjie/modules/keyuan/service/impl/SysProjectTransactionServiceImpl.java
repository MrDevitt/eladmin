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
import me.zhengjie.modules.keyuan.domain.SysProjectDetail;
import me.zhengjie.modules.keyuan.domain.SysProjectTransaction;
import me.zhengjie.modules.keyuan.domain.config.AccountNumberConfig;
import me.zhengjie.modules.keyuan.domain.statistics.transaction.SummaryData;
import me.zhengjie.modules.keyuan.repository.SysProjectDetailRepository;
import me.zhengjie.modules.keyuan.repository.SysProjectTransactionRepository;
import me.zhengjie.modules.keyuan.service.SysProjectAccountService;
import me.zhengjie.modules.keyuan.service.SysProjectConfigService;
import me.zhengjie.modules.keyuan.service.SysProjectPersonService;
import me.zhengjie.modules.keyuan.service.SysProjectTransactionService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectAccountDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectAccountQueryCriteria;
import me.zhengjie.modules.keyuan.service.dto.SysProjectReceiveDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectTransactionDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectTransactionQueryCriteria;
import me.zhengjie.modules.keyuan.service.mapstruct.SysProjectTransactionMapper;
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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author MrDevitt
 * @website https://eladmin.vip
 * @description 服务实现
 * @date 2025-11-11
 **/
@Service
@Slf4j
@RequiredArgsConstructor
public class SysProjectTransactionServiceImpl implements SysProjectTransactionService {

    private final SysProjectTransactionRepository sysProjectTransactionRepository;
    private final SysProjectTransactionMapper sysProjectTransactionMapper;

    private final SysProjectAccountService sysProjectAccountService;

    private final SysProjectConfigService sysProjectConfigService;

    private final SysProjectDetailRepository sysProjectDetailRepository;

    private final SysProjectPersonService sysProjectPersonService;

    @Override
    public PageResult<SysProjectTransactionDto> queryAll(SysProjectTransactionQueryCriteria criteria, Pageable pageable) {
        updateQueryCriteria(criteria);
        Page<SysProjectTransaction> page = sysProjectTransactionRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page.map(sysProjectTransactionMapper::toDto));
    }

    private void updateQueryCriteria(SysProjectTransactionQueryCriteria criteria) {
        if (getAccountNumberConfig().getBankAccountNumberSet().contains(criteria.getAccountNumber())) {
            criteria.setBankNumber(criteria.getAccountNumber());
            criteria.setAccountNumber(null);
        }
    }

    @Override
    public List<SysProjectTransactionDto> queryAll(SysProjectTransactionQueryCriteria criteria) {
        return sysProjectTransactionMapper.toDto(sysProjectTransactionRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder)));
    }

    @Override
    @Transactional
    public SysProjectTransactionDto findById(Long id) {
        SysProjectTransaction sysProjectTransaction = sysProjectTransactionRepository.findById(id).orElseGet(SysProjectTransaction::new);
        ValidationUtil.isNull(sysProjectTransaction.getId(), "SysProjectTransaction", "id", id);
        return sysProjectTransactionMapper.toDto(sysProjectTransaction);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(SysProjectTransaction resources) {
        checkChildren(resources);
        resources.setCreateBy(SecurityUtils.getCurrentUsername());
        sysProjectTransactionRepository.save(resources);
    }

    private void checkChildren(SysProjectTransaction resources) {
        SysProjectAccountQueryCriteria criteria = new SysProjectAccountQueryCriteria();
        criteria.setParents(List.of(resources.getAccountNumber(), resources.getBankNumber()));
        if (CollectionUtils.isNotEmpty(sysProjectAccountService.queryAll(criteria))) {
            throw new RuntimeException("科目" + resources.getAccountNumber() + "或银行" + resources.getBankNumber() + "有子科目，无法录入！");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SysProjectTransaction resources) {
        SysProjectTransaction sysProjectTransaction = sysProjectTransactionRepository.findById(resources.getId()).orElseGet(SysProjectTransaction::new);
        ValidationUtil.isNull(sysProjectTransaction.getId(), "SysProjectTransaction", "id", resources.getId());
        checkChildren(resources);
        sysProjectTransaction.copy(resources);
        sysProjectTransaction.setUpdateBy(SecurityUtils.getCurrentUsername());
        sysProjectTransactionRepository.save(sysProjectTransaction);
    }

    @Override
    public void deleteAll(Long[] ids) {
        for (Long id : ids) {
            sysProjectTransactionRepository.deleteById(id);
        }
    }

    @Override
    public void download(List<SysProjectTransactionDto> all, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (SysProjectTransactionDto sysProjectTransaction : all) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("摘要", sysProjectTransaction.getComment());
            map.put("金额", sysProjectTransaction.getAmount());
            map.put("交易类型", sysProjectTransaction.getDirection());
            map.put("科目编号（关联业务人、部门）", sysProjectTransaction.getAccountNumber());
            map.put("银行账号编号（关联银行账户）", sysProjectTransaction.getBankNumber());
            map.put("记账凭证编号", sysProjectTransaction.getCertificateNumber());
            map.put("交易时间", sysProjectTransaction.getTransactionTime());
            map.put("创建人", sysProjectTransaction.getCreateBy());
            map.put("修改人", sysProjectTransaction.getUpdateBy());
            map.put("创建时间", sysProjectTransaction.getCreateTime());
            map.put("修改时间", sysProjectTransaction.getUpdateTime());
            list.add(map);
        }
        FileUtil.downloadExcel(list, response);
    }

    @Override
    public List<SummaryData> getTransactionSummary(Timestamp begin, Timestamp end) {
        List<SysProjectAccountDto> accountDtoList = sysProjectAccountService.queryAll(new SysProjectAccountQueryCriteria());
        List<SysProjectAccountDto> topAccountList = accountDtoList.stream().filter(accountDto -> accountDto.getParent() == null).collect(Collectors.toList());
        Map<Long, SummaryData> summaryMap = calcSummaryMap(begin, end);
        Map<Long, List<SysProjectAccountDto>> childrenMap = calcChildrenMap(accountDtoList);
        return topAccountList
                .stream()
                .map(accountDto -> calcSummaryData(accountDto, childrenMap, summaryMap))
                .collect(Collectors.toList());
    }

    //科目余额统计，无父子关系，无初始余额
    private Map<Long, SummaryData> calcSummaryMap(Timestamp begin, Timestamp end) {
        SysProjectTransactionQueryCriteria criteria = new SysProjectTransactionQueryCriteria();
        criteria.setTransactionTime(List.of(new Timestamp(0), end));
        List<SysProjectTransactionDto> transactionDtoList = queryAll(criteria);
        Map<Long, SummaryData> summaryMap = new HashMap<>();
        summaryMap.putAll(buildMap(begin, transactionDtoList, false));
        summaryMap.putAll(buildMap(begin, transactionDtoList, true));
        return summaryMap;
    }

    private Map<Long, SummaryData> buildMap(Timestamp begin, List<SysProjectTransactionDto> transactionDtoList, boolean bank) {
        Map<Long, SummaryData> summaryMap = new HashMap<>();
        for (SysProjectTransactionDto transactionDto : transactionDtoList) {
            Long key = bank ? transactionDto.getBankNumber() : transactionDto.getAccountNumber();
            SummaryData summary = summaryMap.computeIfAbsent(key, k -> new SummaryData());
            Integer amount = transactionDto.getAmount();
            if (transactionDto.getTransactionTime().before(begin)) {
                if (transactionDto.getDirection() == ProjectUtils.PROJECT_TRANSACTION_DIRECTION_INCOME) {
                    summary.setBeginIncome(summary.getBeginIncome() + amount);
                    summary.setEndIncome(summary.getBeginIncome() + amount);
                } else {
                    summary.setBeginExpense(summary.getBeginExpense() + amount);
                    summary.setEndExpense(summary.getBeginExpense() + amount);
                }
            } else {
                if (transactionDto.getDirection() == ProjectUtils.PROJECT_TRANSACTION_DIRECTION_INCOME) {
                    summary.setCurrentIncome(summary.getCurrentIncome() + amount);
                    summary.setEndIncome(summary.getEndIncome() + amount);
                } else {
                    summary.setCurrentExpense(summary.getCurrentExpense() + amount);
                    summary.setEndExpense(summary.getEndExpense() + amount);
                }
            }
        }
        return summaryMap;
    }

    private Map<Long, List<SysProjectAccountDto>> calcChildrenMap(List<SysProjectAccountDto> accountDtoList) {
        Map<Long, List<SysProjectAccountDto>> childrenMap = new HashMap<>();
        for (SysProjectAccountDto accountDto : accountDtoList) {
            childrenMap.computeIfAbsent(accountDto.getParent(), k -> new ArrayList<>()).add(accountDto);
        }
        return childrenMap;
    }

    private SummaryData calcSummaryData(
            SysProjectAccountDto accountDto,
            Map<Long, List<SysProjectAccountDto>> childrenMap,
            Map<Long, SummaryData> summaryMap) {
        SummaryData summaryData = new SummaryData();
        Long accountNumber = accountDto.getAccountNumber();
        summaryData.setAccountNumber(accountNumber);
        summaryData.setName(accountDto.getDescription());
        summaryData.setInitialAmount(accountDto.getInitialAmount());
        List<SysProjectAccountDto> children = childrenMap.get(accountNumber);
        if (children != null) {
            summaryData.setChildren(new ArrayList<>());
            for (SysProjectAccountDto child : children) {
                SummaryData childSummary = calcSummaryData(child, childrenMap, summaryMap);
                summaryData.getChildren().add(childSummary);
                summaryData.add(childSummary);
            }
        }
        summaryData.add(summaryMap.getOrDefault(accountNumber, new SummaryData()));
        summaryData.calcRemain();
        return summaryData;
    }

    @Override
    public void updateTransactionByReceive(SysProjectReceiveDto receive) {
        try {
            SysProjectDetail detailDto = sysProjectDetailRepository.findById(receive.getProjectId()).orElse(null);
            assert detailDto != null;
            AccountNumberConfig accountNumberConfig = getAccountNumberConfig();
            if (!accountNumberConfig.getPersonWhiteList().contains(detailDto.getSalesPerson())) {
                return;
            }
            long personAmount = (long) receive.getReceiveAmount() * detailDto.getSalesPercent() / 100;
            long otherAmount = (long) receive.getReceiveAmount() - personAmount;

            SysProjectTransactionQueryCriteria criteria = new SysProjectTransactionQueryCriteria();
            criteria.setProjectReceiveId(receive.getId());
            List<SysProjectTransactionDto> transactionDtoList = queryAll(criteria);
            List<SysProjectTransaction> transactionList = new ArrayList<>();
            if (CollectionUtils.isEmpty(transactionDtoList)) {
                SysProjectTransaction personTransaction = buildTransactionFromReceive(receive, detailDto, true);
                SysProjectTransaction otherTransaction = buildTransactionFromReceive(receive, detailDto, false);
                transactionList.add(personTransaction);
                transactionList.add(otherTransaction);
            } else if (transactionDtoList.size() == 2) {
                for (SysProjectTransactionDto transactionDto : transactionDtoList) {
                    if (accountNumberConfig.getPersonAccountNumberSet().contains(transactionDto.getAccountNumber())) {
                        transactionDto.setAmount((int) personAmount);
                    } else {
                        transactionDto.setAmount((int) otherAmount);
                    }
                    transactionDto.setTransactionTime(receive.getReceiveTime());
                    transactionDto.setUpdateBy("系统");
                }
                transactionList.addAll(sysProjectTransactionMapper.toEntity(transactionDtoList));
            } else {
                throw new RuntimeException("收款生成交易数据异常，receiveId=" + receive.getId());
            }
            sysProjectTransactionRepository.saveAll(transactionList);
        } catch (Throwable e) {
            log.error("updateTransactionByReceive error", e);
        }
    }

    private SysProjectTransaction buildTransactionFromReceive(SysProjectReceiveDto receive, SysProjectDetail detailDto, boolean person) {
        SysProjectTransaction transaction = new SysProjectTransaction();
        AccountNumberConfig accountNumberConfig = getAccountNumberConfig();
        long personAmount = (long) receive.getReceiveAmount() * detailDto.getSalesPercent() / 100;
        long amount = person ? personAmount : (long) receive.getReceiveAmount() - personAmount;
        Long accountNumber = person ?
                Long.parseLong(sysProjectPersonService.findById(detailDto.getSalesPerson()).getAccountNumber()) :
                accountNumberConfig.getTypeAndRegionMap().get(detailDto.getProjectType()).get(detailDto.getProjectRegion());
        String comment = person ? "-业务人收款-" : "-公司收款-";
        transaction.setAccountNumber(accountNumber);
        transaction.setBankNumber(accountNumberConfig.getBankAccountMap().get(detailDto.getPartyB()));
        checkChildren(transaction);
        transaction.setAmount((int) amount);
        transaction.setTransactionTime(receive.getReceiveTime());
        transaction.setComment(detailDto.getProjectName() + comment + receive.getId());
        transaction.setCertificateNumber(detailDto.getContractNumber() + "-" + receive.getId());
        transaction.setDirection(ProjectUtils.PROJECT_TRANSACTION_DIRECTION_INCOME);
        transaction.setProjectReceiveId(receive.getId());
        transaction.setCreateBy("系统");
        return transaction;
    }

    private AccountNumberConfig getAccountNumberConfig() {
        return sysProjectConfigService.findConfigByKey(ProjectUtils.PROJECT_CONFIG_KEY_BANK_ACCOUNT, AccountNumberConfig.class);
    }
}