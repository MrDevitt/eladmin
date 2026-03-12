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
import me.zhengjie.modules.keyuan.domain.SysProjectGuarantee;
import me.zhengjie.modules.keyuan.domain.SysProjectTransaction;
import me.zhengjie.modules.keyuan.domain.config.AccountNumberConfig;
import me.zhengjie.modules.keyuan.domain.statistics.transaction.SummaryData;
import me.zhengjie.modules.keyuan.repository.SysProjectDetailRepository;
import me.zhengjie.modules.keyuan.repository.SysProjectGuaranteeRepository;
import me.zhengjie.modules.keyuan.repository.SysProjectTransactionRepository;
import me.zhengjie.modules.keyuan.service.SysProjectAccountService;
import me.zhengjie.modules.keyuan.service.SysProjectConfigService;
import me.zhengjie.modules.keyuan.service.SysProjectPersonService;
import me.zhengjie.modules.keyuan.service.SysProjectTransactionService;
import me.zhengjie.modules.keyuan.service.dto.SysProjectAccountDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectAccountQueryCriteria;
import me.zhengjie.modules.keyuan.service.dto.SysProjectGuaranteeDto;
import me.zhengjie.modules.keyuan.service.dto.SysProjectGuaranteeQueryCriteria;
import me.zhengjie.modules.keyuan.service.dto.SysProjectPersonDto;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
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

    private final SysProjectGuaranteeRepository sysProjectGuaranteeRepository;

    @Override
    public PageResult<SysProjectTransactionDto> queryAll(SysProjectTransactionQueryCriteria criteria, Pageable pageable) {
        updateQueryCriteria(criteria);
        Page<SysProjectTransaction> page = sysProjectTransactionRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page.map(sysProjectTransactionMapper::toDto));
    }

    private void updateQueryCriteria(SysProjectTransactionQueryCriteria criteria) {
        if (criteria != null && getAccountNumberConfig().isBankAccount(criteria.getAccountNumber())) {
            criteria.setBankNumber(criteria.getAccountNumber());
            criteria.setAccountNumber(null);
        }
    }

    @Override
    public List<SysProjectTransactionDto> queryAll(SysProjectTransactionQueryCriteria criteria) {
        updateQueryCriteria(criteria);
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
        checkTransaction(resources);
        resources.setCreateBy(SecurityUtils.getCurrentUsername());
        sysProjectTransactionRepository.save(resources);
    }

    private void checkTransaction(SysProjectTransaction resources) {
        AccountNumberConfig accountNumberConfig = getAccountNumberConfig();
        if (!accountNumberConfig.isBankAccount(resources.getBankNumber())) {
            throw new RuntimeException("科目" + resources.getBankNumber() + "不是银行科目！");
        }
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
        checkTransaction(resources);
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
        Map<Long, SysProjectAccountDto> accountDtoMap = sysProjectAccountService.queryAll(new SysProjectAccountQueryCriteria())
                .stream().collect(Collectors.toMap(
                        SysProjectAccountDto::getAccountNumber,
                        Function.identity(),
                        (x, y) -> x
                ));
        for (SysProjectTransactionDto sysProjectTransaction : all) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("摘要", sysProjectTransaction.getComment());
            map.put("金额", ProjectUtils.dbPriceToRealPrice(sysProjectTransaction.getAmount()));
            map.put("交易类型", ProjectUtils.PROJECT_TRANSACTION_DIRECTIONS[sysProjectTransaction.getDirection()]);
            Long accountNumber = sysProjectTransaction.getAccountNumber();
            map.put("科目编号", String.valueOf(accountNumber));
            map.put("科目描述", accountDtoMap.get(accountNumber).getDescription());
            Long bankNumber = sysProjectTransaction.getBankNumber();
            map.put("银行编号", String.valueOf(bankNumber));
            map.put("银行描述", accountDtoMap.get(bankNumber).getDescription());
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
    public List<SummaryData> getTransactionSummary(Timestamp begin, Timestamp end, String type) {
        AccountNumberConfig accountNumberConfig = getAccountNumberConfig();
        List<SysProjectAccountDto> accountDtoList = sysProjectAccountService.queryAll(new SysProjectAccountQueryCriteria());
        List<SysProjectAccountDto> topAccountList = accountDtoList
                .stream()
                .filter(accountDto -> accountNumberConfig.getTypeTopAccountMap().get(type).contains(accountDto.getAccountNumber()))
                .collect(Collectors.toList());
        Map<Long, SummaryData> summaryMap = calcSummaryMap(begin, end, "person".equals(type));
        Map<Long, List<SysProjectAccountDto>> childrenMap = calcChildrenMap(accountDtoList);
        return topAccountList
                .stream()
                .map(accountDto -> calcSummaryData(accountDto, childrenMap, summaryMap))
                .collect(Collectors.toList());
    }

    //科目余额统计，无父子关系，无初始余额
    private Map<Long, SummaryData> calcSummaryMap(Timestamp begin, Timestamp end, boolean person) {
        AccountNumberConfig accountNumberConfig = getAccountNumberConfig();
        SysProjectTransactionQueryCriteria criteria = new SysProjectTransactionQueryCriteria();
        criteria.setTransactionTime(List.of(new Timestamp(accountNumberConfig.getInitialTime()), end));
        List<SysProjectTransactionDto> transactionDtoList = queryAll(criteria);
        Map<Long, SummaryData> summaryMap = new HashMap<>();
        summaryMap.putAll(buildMap(begin, transactionDtoList, false));
        summaryMap.putAll(buildMap(begin, transactionDtoList, true));
        if (person) {
            Map<Long, SysProjectPersonDto> personMap = sysProjectPersonService.getAccountNumberToPersonMap();
            personMap.forEach((k, v) -> {
                if (summaryMap.containsKey(k)) {
                    return;
                }
                summaryMap.put(k, new SummaryData());//保证业务人数据
            });
            //计算提成余额，重复代码，待优化依赖关系
            List<SysProjectDetail> detailList = sysProjectDetailRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, null, criteriaBuilder));
            Map<Long, Long> remainingByPerson = new HashMap<>();
            for (SysProjectDetail detail : detailList) {
                long remaining = detail.getContractAmount() - Optional.ofNullable(detail.getReceiveAmount()).orElse(0L);
                remaining = remaining * detail.getSalesPercent() / 100;
                remainingByPerson.put(detail.getSalesPerson(), remainingByPerson.getOrDefault(detail.getSalesPerson(), 0L) + Math.max(remaining, 0));
            }
            //获取担保金额
            SysProjectGuaranteeQueryCriteria guaranteeCriteria = new SysProjectGuaranteeQueryCriteria();
            guaranteeCriteria.setStatus(List.of(SysProjectGuaranteeDto.STATUS_NORMAL, SysProjectGuaranteeDto.STATUS_ABNORMAL));
            List<SysProjectGuarantee> guaranteeList = sysProjectGuaranteeRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, guaranteeCriteria, criteriaBuilder));
            Map<Long, Long> guaranteeByPerson = new HashMap<>();
            for (SysProjectGuarantee guarantee : guaranteeList) {
                long amount = guarantee.getGuaranteeAmount();
                guaranteeByPerson.put(guarantee.getGuaranteePerson(), guaranteeByPerson.getOrDefault(guarantee.getGuaranteePerson(), 0L) + amount);
            }

            summaryMap.forEach((k, v) -> {
                if (!accountNumberConfig.isPersonAccount(k)) {
                    return;
                }
                SysProjectPersonDto salesPerson = personMap.get(k);
                if (salesPerson == null) {
                    log.error("科目编号对应业务人员不存在：{}", k);
                    return;
                }
                v.setRemainingShare(remainingByPerson.getOrDefault(salesPerson.getId(), 0L));
                v.setGuaranteeAmount(guaranteeByPerson.getOrDefault(salesPerson.getId(), 0L));
            });
        }
        return summaryMap;
    }

    private Map<Long, SummaryData> buildMap(Timestamp begin, List<SysProjectTransactionDto> transactionDtoList, boolean bank) {
        Map<Long, SummaryData> summaryMap = new HashMap<>();
        for (SysProjectTransactionDto transactionDto : transactionDtoList) {
            Long key = bank ? transactionDto.getBankNumber() : transactionDto.getAccountNumber();
            SummaryData summary = summaryMap.computeIfAbsent(key, k -> new SummaryData());
            Long amount = transactionDto.getAmount();
            if (transactionDto.getTransactionTime().before(begin)) {
                if (transactionDto.getDirection() == ProjectUtils.PROJECT_TRANSACTION_DIRECTION_INCOME) {
                    summary.setBeginIncome(summary.getBeginIncome() + amount);
                    summary.setEndIncome(summary.getEndIncome() + amount);
                } else {
                    summary.setBeginExpense(summary.getBeginExpense() + amount);
                    summary.setEndExpense(summary.getEndExpense() + amount);
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
    public void updateTransactionByReceive(SysProjectReceiveDto receive, boolean create) {
        AccountNumberConfig accountNumberConfig = getAccountNumberConfig();
        if (!accountNumberConfig.isAutoTransactionEnable() ||
                receive.getReceiveTime().getTime() < accountNumberConfig.getInitialTime() ||
                accountNumberConfig.getProjectBlackList().contains(receive.getProjectId())) {
            return;
        }
        SysProjectDetail detailDto = sysProjectDetailRepository.findById(receive.getProjectId()).orElse(null);
        if (detailDto == null) {
            throw new RuntimeException("项目不存在,receiveId=" + receive.getId() + " projectId=" + receive.getProjectId());
        }
        if (detailDto.getProjectType().equals(ProjectUtils.PROJECT_TYPE_OTHER) ||
                accountNumberConfig.getPartyBBlackList().contains(detailDto.getPartyB())) {//特殊项目、特殊账户手动录入
            return;
        }

        long personAmount = receive.getReceiveAmount() * detailDto.getSalesPercent() / 100;
        long otherAmount = receive.getReceiveAmount() - personAmount;

        List<SysProjectTransactionDto> transactionDtoList = new ArrayList<>();
        if (!create) {
            SysProjectTransactionQueryCriteria criteria = new SysProjectTransactionQueryCriteria();
            criteria.setProjectReceiveId(receive.getId());
            transactionDtoList = queryAll(criteria);
        }
        List<SysProjectTransaction> transactionList = new ArrayList<>();
        if (detailDto.getProjectType().equals(ProjectUtils.PROJECT_TYPE_EXAM) &&
                accountNumberConfig.getBranchRegionSet().contains(detailDto.getProjectRegion())) {//检测分公司
            long hqAmount = receive.getReceiveAmount() * detailDto.getManagementPercent() / 100;
            long branchAmount = otherAmount - hqAmount;
            if (CollectionUtils.isEmpty(transactionDtoList)) {
                Long personAccountNumber = getPersonAccountNumber(detailDto);
                transactionList.add(buildTransactionFromReceive(receive, detailDto, personAmount, personAccountNumber, "-业务人收款-"));

                Long branchAccountNumber = getCompanyAccountNumber(detailDto);
                transactionList.add(buildTransactionFromReceive(receive, detailDto, branchAmount, branchAccountNumber, "-分公司收款-"));

                transactionList.add(buildTransactionFromReceive(receive, detailDto, hqAmount, accountNumberConfig.getHqAccountNumber(), "-总公司收款-"));
            } else if (transactionDtoList.size() == 3) {
                for (SysProjectTransactionDto transactionDto : transactionDtoList) {
                    if (accountNumberConfig.isPersonAccount(transactionDto.getAccountNumber())) {
                        transactionDto.setAmount(personAmount);
                    } else if (transactionDto.getAccountNumber().equals(accountNumberConfig.getHqAccountNumber())) {
                        transactionDto.setAmount(hqAmount);
                    } else {
                        transactionDto.setAmount(branchAmount);
                    }
                    transactionDto.setTransactionTime(receive.getReceiveTime());
                    transactionDto.setUpdateBy(SecurityUtils.getCurrentUsername());
                }
                transactionList.addAll(sysProjectTransactionMapper.toEntity(transactionDtoList));
            } else {
                throw new RuntimeException("收款生成交易数据异常，receiveId=" + receive.getId());
            }
        } else {
            if (CollectionUtils.isEmpty(transactionDtoList)) {
                Long personAccountNumber = getPersonAccountNumber(detailDto);
                transactionList.add(buildTransactionFromReceive(receive, detailDto, personAmount, personAccountNumber, "业务人收款"));

                Long otherAccountNumber = getCompanyAccountNumber(detailDto);
                transactionList.add(buildTransactionFromReceive(receive, detailDto, otherAmount, otherAccountNumber, "公司收款"));
            } else if (transactionDtoList.size() == 2) {
                for (SysProjectTransactionDto transactionDto : transactionDtoList) {
                    if (accountNumberConfig.isPersonAccount(transactionDto.getAccountNumber())) {
                        transactionDto.setAmount(personAmount);
                    } else {
                        transactionDto.setAmount(otherAmount);
                    }
                    transactionDto.setTransactionTime(receive.getReceiveTime());
                    transactionDto.setUpdateBy("系统");
                }
                transactionList.addAll(sysProjectTransactionMapper.toEntity(transactionDtoList));
            } else {
                throw new RuntimeException("收款生成交易数据异常，receiveId=" + receive.getId());
            }
        }
        sysProjectTransactionRepository.saveAll(transactionList);
    }

    private Long getPersonAccountNumber(SysProjectDetail detailDto) {
        SysProjectPersonDto personDto = sysProjectPersonService.findById(detailDto.getSalesPerson());
        if (personDto == null) {
            throw new RuntimeException("业务人不存在, projectId=" + detailDto.getId());
        }
        Long personAccountNumber = personDto.getAccountNumber();
        if (personAccountNumber == null) {
            throw new RuntimeException("person accountNumber不存在, person=" + detailDto.getSalesPerson());
        }
        return personAccountNumber;
    }

    private Long getCompanyAccountNumber(SysProjectDetail detailDto) {
        AccountNumberConfig accountNumberConfig = getAccountNumberConfig();
        Map<String, Long> regionMap = accountNumberConfig.getTypeAndRegionMap().get(detailDto.getProjectType());
        if (regionMap == null) {
            throw new RuntimeException("regionMap不存在, type=" + detailDto.getProjectType());
        }
        Long accountNumber = regionMap.get(detailDto.getProjectRegion());
        if (accountNumber == null) {
            throw new RuntimeException("region accountNumber不存在, region=" + detailDto.getProjectRegion());
        }
        return accountNumber;
    }

    private SysProjectTransaction buildTransactionFromReceive(SysProjectReceiveDto receive,
                                                              SysProjectDetail detailDto,
                                                              long amount,
                                                              long accountNumber,
                                                              String comment) {
        SysProjectTransaction transaction = new SysProjectTransaction();
        AccountNumberConfig accountNumberConfig = getAccountNumberConfig();
        Long bankNumber = accountNumberConfig.getBankAccountMap().get(detailDto.getPartyB());
        if (bankNumber == null) {
            throw new RuntimeException("partyB bankNumber不存在, partyB=" + detailDto.getPartyB());
        }
        transaction.setBankNumber(bankNumber);
        transaction.setAccountNumber(accountNumber);
        checkTransaction(transaction);
        transaction.setAmount(amount);
        transaction.setTransactionTime(receive.getReceiveTime());
        transaction.setComment(detailDto.getProjectName() + comment + receive.getId());
        transaction.setCertificateNumber(detailDto.getContractNumber() + "-" + receive.getId());
        transaction.setDirection(ProjectUtils.PROJECT_TRANSACTION_DIRECTION_INCOME);
        transaction.setProjectReceiveId(receive.getId());
        transaction.setCreateBy(SecurityUtils.getCurrentUsername());
        return transaction;
    }

    @Override
    public void deleteTransactionByReceive(Long[] receiveIds) {
        SysProjectTransactionQueryCriteria criteria = new SysProjectTransactionQueryCriteria();
        criteria.setProjectReceiveIds(Arrays.asList(receiveIds));
        Long[] ids = queryAll(criteria).stream().map(SysProjectTransactionDto::getId).toArray(Long[]::new);
        deleteAll(ids);
    }

    private AccountNumberConfig getAccountNumberConfig() {
        return sysProjectConfigService.findConfigByKey(ProjectUtils.PROJECT_CONFIG_KEY_BANK_ACCOUNT, AccountNumberConfig.class);
    }
}