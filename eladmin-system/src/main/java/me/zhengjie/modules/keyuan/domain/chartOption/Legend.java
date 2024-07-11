package me.zhengjie.modules.keyuan.domain.chartOption;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Legend {

    private String x = "right";

    private List<String> data = new ArrayList<>();

}
