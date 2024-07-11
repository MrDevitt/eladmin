package me.zhengjie.modules.keyuan.domain.chartOption;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PieDataSeries implements DataSeries {

    private List<SeriesData> data = new ArrayList<>();
}
