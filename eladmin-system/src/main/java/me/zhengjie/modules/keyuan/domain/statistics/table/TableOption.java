package me.zhengjie.modules.keyuan.domain.statistics.table;

import lombok.Data;
import me.zhengjie.modules.keyuan.utils.ProjectUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class TableOption {
    private String label;
    private List<TableColumn> columns = new ArrayList<>();
    private List<TableData> data = new ArrayList<>();

    public TableOption(String label, String column1Name) {
        this.label = label;
        columns.add(new TableColumn("name", column1Name, false));
        for (int i = 0; i < 12; i++) {
            columns.add(new TableColumn("month" + (i + 1), ProjectUtils.monthNumberToName(i), true));
        }
        columns.add(new TableColumn("year", "合计", true));
    }
}
