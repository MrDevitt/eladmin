package me.zhengjie.modules.keyuan.domain.config;

import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class AccountNumberConfig {
    private boolean autoTransactionEnable = false;
    private Map<String, Long> bankAccountMap = new HashMap<>();
    private Map<Integer, Map<String, Long>> typeAndRegionMap = new HashMap<>();
    private Set<Long> projectBlackList = new HashSet<>();
    private Set<String> partyBBlackList = new HashSet<>();
    private Set<String> branchRegionSet = new HashSet<>(List.of("拉萨", "阿里", "那曲"));
    private String bankAccountPrefix = "-";
    private String personAccountPrefix = "-";
    private long hqAccountNumber = 0L;
    private long initialTime = 0L;
    private long personTopAccount = -1L;

    public boolean isBankAccount(Long accountNumber) {
        return accountNumber != null && accountNumber.toString().startsWith(bankAccountPrefix);
    }

    public boolean isPersonAccount(Long accountNumber) {
        return accountNumber != null && accountNumber.toString().startsWith(personAccountPrefix);
    }
}
