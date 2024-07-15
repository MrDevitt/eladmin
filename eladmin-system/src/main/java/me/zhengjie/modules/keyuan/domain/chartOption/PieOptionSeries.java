package me.zhengjie.modules.keyuan.domain.chartOption;

import lombok.Data;

@Data
public class PieOptionSeries implements OptionSeries {
    private String name = "占比";
    private String type = "pie";
    private String[] center = new String[]{"85%", "25%"};
    private String radius = "25%";
    private int z = 100;
}
