package me.zhengjie.modules.keyuan.domain.statistics;

import lombok.Data;
import me.zhengjie.modules.keyuan.domain.statistics.chartOption.BarDataSeries;
import me.zhengjie.modules.keyuan.domain.statistics.chartOption.BarOptionSeries;
import me.zhengjie.modules.keyuan.domain.statistics.chartOption.ChartOption;
import me.zhengjie.modules.keyuan.domain.statistics.chartOption.DataSeries;
import me.zhengjie.modules.keyuan.domain.statistics.chartOption.Option;
import me.zhengjie.modules.keyuan.domain.statistics.chartOption.PieDataSeries;
import me.zhengjie.modules.keyuan.domain.statistics.chartOption.PieOptionSeries;
import me.zhengjie.modules.keyuan.domain.statistics.chartOption.SeriesData;
import me.zhengjie.modules.keyuan.domain.statistics.table.TableData;
import me.zhengjie.modules.keyuan.domain.statistics.table.TableOption;
import me.zhengjie.modules.keyuan.utils.ProjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class SysProjectStatistics {

    Map<String, Map<String, double[]>> contractByYearAndType = new HashMap<>();
    ChartOption contractChartOption = new ChartOption();
    Map<String, Double> contractTotalByType = new HashMap<>();


    Map<String, Map<String, double[]>> receiveByYearAndType = new HashMap<>();
    ChartOption receiveChartOption = new ChartOption();
    Map<String, Double> receiveTotalByType = new HashMap<>();

    Map<String, Map<String, double[]>> contractByTypeAndRegion = new HashMap<>();
    List<TableOption> contractByRegionTableList = new ArrayList<>();
    Map<String, Map<String, double[]>> contractByTypeAndPerson = new HashMap<>();
    List<TableOption> contractByPersonTableList = new ArrayList<>();


    public void calcInnerData() {
        buildOptionAndTotal(contractByYearAndType, contractChartOption, contractTotalByType, "业务量指标");
        buildOptionAndTotal(receiveByYearAndType, receiveChartOption, receiveTotalByType, "收款指标");
        buildTableFromMap(contractByRegionTableList, contractByTypeAndRegion, "地区");
        buildTableFromMap(contractByPersonTableList, contractByTypeAndPerson, "业务人");
    }

    private void buildOptionAndTotal(
            Map<String, Map<String, double[]>> dataByYearAndType,
            ChartOption chartOption,
            Map<String, Double> dataTotalByType,
            String titleText) {
        for (String typeName : ProjectUtils.PROJECT_TYPE_NAMES) {
            chartOption.getBaseOption().getLegend().getData().add(typeName);
            chartOption.getBaseOption().getSeries().add(new BarOptionSeries(typeName));
        }
        chartOption.getBaseOption().getSeries().add(new PieOptionSeries());
        String currentYear = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        for (Map.Entry<String, Map<String, double[]>> entry : dataByYearAndType.entrySet()) {
            String year = entry.getKey();
            chartOption.getBaseOption().getTimeline().getData().add(year);
            Option option = new Option();
            option.getTitle().put("text", year + titleText);
            PieDataSeries pieDataSeries = new PieDataSeries();
            List<DataSeries> barDataList = new ArrayList<>(List.of(BarDataSeries.instance, BarDataSeries.instance, BarDataSeries.instance, BarDataSeries.instance));
            for (Map.Entry<String, double[]> entry1 : entry.getValue().entrySet()) {
                String typeName = entry1.getKey();
                int typeId = ProjectUtils.projectTypeNameToId(typeName);
                barDataList.set(typeId, new BarDataSeries(Arrays.copyOf(entry1.getValue(), 12)));
                pieDataSeries.getData().add(new SeriesData(typeName, entry1.getValue()[12]));

                if (currentYear.equals(year)) {
                    dataTotalByType.put(typeName, entry1.getValue()[12]);
                }
            }
            option.getSeries().addAll(barDataList);
            option.getSeries().add(pieDataSeries);
            chartOption.getOptions().add(option);
        }
        chartOption.getBaseOption().getSeries().add(new PieOptionSeries());
    }

    private void buildTableFromMap(List<TableOption> tableList, Map<String, Map<String, double[]>> dataMap, String column1Name) {
        Arrays.stream(ProjectUtils.PROJECT_TYPE_NAMES).forEach(e -> tableList.add(new TableOption(e, column1Name)));
        for (TableOption tableOption : tableList) {
            Map<String, double[]> detailMap = dataMap.get(tableOption.getLabel());
            for (Map.Entry<String, double[]> entry : detailMap.entrySet()) {
                tableOption.getData().add(new TableData(entry.getKey(), entry.getValue()));
            }
        }
    }
}
