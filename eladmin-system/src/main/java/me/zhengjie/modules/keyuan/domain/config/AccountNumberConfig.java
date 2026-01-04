package me.zhengjie.modules.keyuan.domain.config;

import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class AccountNumberConfig {
    private boolean autoTransactionEnable = false;
    private Map<String, Long> bankAccountMap = new HashMap<>();
    private Map<Integer, Map<String, Long>> typeAndRegionMap = new HashMap<>();
    private Set<Long> projectBlackList = new HashSet<>();
    private String bankAccountPrefix = "-";
    private String personAccountPrefix = "-";
    private long initialTime = 0L;
    private Long personTopAccount = -1L;

    public boolean isBankAccount(Long accountNumber) {
        return accountNumber != null && accountNumber.toString().startsWith(bankAccountPrefix);
    }

    public boolean isPersonAccount(Long accountNumber) {
        return accountNumber != null && accountNumber.toString().startsWith(personAccountPrefix);
    }
}
