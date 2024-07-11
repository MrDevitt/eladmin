package me.zhengjie.modules.keyuan.domain.chartOption;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class PieOptionSeries implements OptionSeries {
    private String name = "占比";
    private String type = "pie";
    private String[] center = new String[]{"85%", "25%"};
    private String radius = "25%";
    private int z = 100;
//    private List<PieOptionSeries.Data> data=new ArrayList<>();

    @lombok.Data
    @AllArgsConstructor
    public static class Data {
        private String name;
        private long value;
    }
}
