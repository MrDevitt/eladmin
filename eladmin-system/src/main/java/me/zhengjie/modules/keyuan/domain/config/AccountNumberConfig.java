package me.zhengjie.modules.keyuan.domain.config;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class AccountNumberConfig {
    private Map<String, Long> bankAccountMap;
    private Map<Integer, Map<String, Long>> typeAndRegionMap;
    private Set<Long> personAccountNumberSet;
    private Set<Long> bankAccountNumberSet;
    private Set<Long> personWhiteList;
    private String bankAccountPrefix = "2001";
    private String personAccountPrefix;

    public boolean isBankAccount(Long accountNumber) {
        return accountNumber != null && accountNumber.toString().startsWith(bankAccountPrefix);
    }

    public boolean isPersonAccount(Long accountNumber) {
        return accountNumber != null && !accountNumber.toString().startsWith(bankAccountPrefix);
    }
}
