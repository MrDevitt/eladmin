package me.zhengjie.modules.keyuan.domain;

import lombok.Data;
import me.zhengjie.modules.keyuan.domain.chartOption.BarDataSeries;
import me.zhengjie.modules.keyuan.domain.chartOption.BarOptionSeries;
import me.zhengjie.modules.keyuan.domain.chartOption.ChartOption;
import me.zhengjie.modules.keyuan.domain.chartOption.Option;
import me.zhengjie.modules.keyuan.domain.chartOption.PieDataSeries;
import me.zhengjie.modules.keyuan.domain.chartOption.PieOptionSeries;
import me.zhengjie.modules.keyuan.domain.chartOption.SeriesData;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
public class SysProjectStatistics {

    Map<String, Map<String, double[]>> contractByYearAndType = new HashMap<>();

    ChartOption contractChartOption = new ChartOption();

    Map<String, Double> contractTotalByType = new HashMap<>();

    public void calcInnerData() {
        Set<String> typeNameSet = new HashSet<>();
        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        for (Map.Entry<String, Map<String, double[]>> entry : contractByYearAndType.entrySet()) {
            String year = entry.getKey();
            contractChartOption.getBaseOption().getTimeline().getData().add(year);
            Option option = new Option();
            option.getTitle().put("text", year + "业务量指标");
            PieDataSeries pieDataSeries = new PieDataSeries();
            for (Map.Entry<String, double[]> entry1 : entry.getValue().entrySet()) {
                String typeName = entry1.getKey();
                if (typeNameSet.add(typeName)) {
                    contractChartOption.getBaseOption().getLegend().getData().add(typeName);
                    contractChartOption.getBaseOption().getSeries().add(new BarOptionSeries(typeName));
                }

                option.getSeries().add(new BarDataSeries(Arrays.copyOf(entry1.getValue(), 12)));
                pieDataSeries.getData().add(new SeriesData(typeName, entry1.getValue()[12]));

                if (currentYear.equals(year)) {
                    contractTotalByType.put(typeName, entry1.getValue()[12]);
                }
            }
            option.getSeries().add(pieDataSeries);
            contractChartOption.getOptions().add(option);
        }
        contractChartOption.getBaseOption().getSeries().add(new PieOptionSeries());
    }

}
