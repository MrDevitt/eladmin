package me.zhengjie.modules.keyuan.domain.config;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Data
public class SalesStatisticMessageConfig {
    private Set<Long> topAccountSet = Set.of(10010103L);
    private List<Long> messageReceiverList = new ArrayList<>();
    private String allMessageReceivers = "";
}
