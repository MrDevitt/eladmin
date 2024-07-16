package me.zhengjie.modules.keyuan.domain.statistics.chartOption;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ChartOption {

    private BaseOption baseOption = new BaseOption();

    private List<Option> options = new ArrayList<>();
}
