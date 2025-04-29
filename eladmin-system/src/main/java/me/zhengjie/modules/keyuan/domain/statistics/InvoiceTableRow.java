package me.zhengjie.modules.keyuan.domain.statistics;

import lombok.Data;

@Data
public class InvoiceTableRow {
    private String name = "";
    private String invoiced = "0.00";
    private String toInvoice = "0.00";
}
