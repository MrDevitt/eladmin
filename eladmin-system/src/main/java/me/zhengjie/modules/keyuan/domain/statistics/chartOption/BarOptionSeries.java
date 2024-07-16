package me.zhengjie.modules.keyuan.domain.statistics.chartOption;

import lombok.Data;

@Data
public class BarOptionSeries implements OptionSeries {
    private String name;
    private String type = "bar";

    public BarOptionSeries(String name) {
        this.name = name;
    }
}
