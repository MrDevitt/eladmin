package me.zhengjie.modules.keyuan.domain.statistics.table;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TableColumn {
    private String prop;
    private String label;
    private boolean sortable;
}
