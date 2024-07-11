package me.zhengjie.modules.keyuan.domain.chartOption;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Option {

    private Map<String, String> title = new HashMap<>();

    private List<DataSeries> series = new ArrayList<>();

}
