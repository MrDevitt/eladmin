package me.zhengjie.modules.keyuan.domain.config;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class AccountNumberConfig {
    private boolean autoTransactionEnable = false;
    private Map<String, Long> bankAccountMap;
    private Map<Integer, Map<String, Long>> typeAndRegionMap;
    private Set<Long> projectBlackList;
    private String bankAccountPrefix;
    private String personAccountPrefix;
    private long initialTime = 0L;

    public boolean isBankAccount(Long accountNumber) {
        return accountNumber != null && accountNumber.toString().startsWith(bankAccountPrefix);
    }

    public boolean isPersonAccount(Long accountNumber) {
        return accountNumber != null && accountNumber.toString().startsWith(personAccountPrefix);
    }
}
