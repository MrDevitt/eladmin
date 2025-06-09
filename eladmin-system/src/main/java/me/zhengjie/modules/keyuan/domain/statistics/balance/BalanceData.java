package me.zhengjie.modules.keyuan.domain.statistics.balance;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BalanceData {
    private List<BalanceTableRow> departmentRows = new ArrayList<>();
    private List<BalanceTableRow> personRows = new ArrayList<>();

    public List<BalanceTableRow> getDepartmentRowsWithChildren() {
        List<BalanceTableRow> rows = new ArrayList<>();
        for (BalanceTableRow row : departmentRows) {
            addDepartmentRow(row, rows);
        }
        return rows;
    }

    private void addDepartmentRow(BalanceTableRow row, List<BalanceTableRow> allRows) {
        if (row == null) {
            return;
        }
        allRows.add(row);
        if (row.getChildren() == null) {
            return;
        }
        for (BalanceTableRow child : row.getChildren()) {
            addDepartmentRow(child, allRows);
        }
    }
}
