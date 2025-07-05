package me.zhengjie.modules.keyuan.domain.statistics;

import lombok.Data;
import me.zhengjie.modules.keyuan.domain.statistics.table.TableColumn;

import java.util.List;
import java.util.Map;

@Data
public class SysShouldReceiveData {

    private List<Map<String, String>> tableData;

    private List<TableColumn> receiveRateColumn = List.of(
            new TableColumn("name", "业务人", false),
            new TableColumn("rate_2023", "2023年", false),
            new TableColumn("rate_2024", "2024年", false),
            new TableColumn("rate_2025", "2025年", false));
    private List<ReceiveRateRow> receiveRateRow;

    @Data
    public static class ReceiveRateRow {
        private String name;
        private String rate_2023;
        private String rate_2024;
        private String rate_2025;
    }
}
