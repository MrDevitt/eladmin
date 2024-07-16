package me.zhengjie.modules.keyuan.domain.statistics.chartOption;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Legend {

    private String x = "right";

    private List<String> data = new ArrayList<>();

}
