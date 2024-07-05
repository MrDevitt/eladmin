package me.zhengjie.modules.keyuan.domain;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class SysProjectStatistics {
    Map<Integer, Map<String, long[]>> contractByTypeAndRegion = new HashMap<>();
    Map<Long, Map<Integer, long[]>> contractByPersonAndType = new HashMap<>();
}
