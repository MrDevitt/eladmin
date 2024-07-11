package me.zhengjie.modules.keyuan.domain.chartOption;

import lombok.Data;
import me.zhengjie.modules.keyuan.utils.ProjectUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class BarDataSeries implements DataSeries {
    private List<SeriesData> data = new ArrayList<>();

    public BarDataSeries(double[] data) {
        for (int i = 0; i < data.length; i++) {
            SeriesData seriesData = new SeriesData(ProjectUtils.monthNumberToName(i), data[i]);
            this.data.add(seriesData);
        }
    }
}
