package me.zhengjie.modules.keyuan.domain.chartOption;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Timeline {

    private String axisType = "category";

    private boolean autoPlay = false;

    private int playInterval = 1000;

    private List<String> data = new ArrayList<>();
}
