package me.zhengjie.modules.keyuan.domain.statistics.table;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TabTable {
    List<TableOption> tableOptionList = new ArrayList<>();
}
