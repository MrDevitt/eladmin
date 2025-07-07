package me.zhengjie.modules.keyuan.domain.statistics;

import lombok.Data;

import java.util.List;

@Data
public class SysReceiveStatistics {

    private List<ReceiveAmountRow> receiveAmountRows;

    private List<ReceiveRateRow> receiveRateRows;


    @Data
    public static class ReceiveAmountRow {
        private String name;
        private String exam;
        private String supervise;
        private String design;
        private String other;
        private String sum;
    }

    @Data
    public static class ReceiveRateRow {
        private String name;
        private String rate_2023;
        private String rate_2024;
        private String rate_2025;
        private String rate;
    }
}
