package me.zhengjie.modules.keyuan.domain.config;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class SysProjectStatisticsConfig {
    private Set<Long> salesPersonBlackList = new HashSet<>();
}
