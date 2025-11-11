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
}
