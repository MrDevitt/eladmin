package me.zhengjie.modules.keyuan.domain.chartOption;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BaseOption {

    private Timeline timeline = new Timeline();

    private Legend legend = new Legend();

    private List<OptionSeries> series = new ArrayList<>();

}
