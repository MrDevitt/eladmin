package me.zhengjie.modules.keyuan.domain.balance;

import lombok.Data;

import java.util.Comparator;

@Data
public class BalanceTableRow {
    private String name;
    private String examLast;
    private String examThisMonth;
    private String examThisYear;
    private String superviseLast;
    private String superviseThisMonth;
    private String superviseThisYear;
    private String designLast;
    private String designThisMonth;
    private String designThisYear;
    private String expenseLast = "0.00";
    private String expenseThisMonth = "0.00";
    private String expenseThisYear = "0.00";
    private String sumLast;
    private String sumThisMonth;
    private String sumThisYear;
    private String initialBalance = "0.00";

    public static final Comparator<BalanceTableRow> COMPARATOR_DESC = Comparator.comparing(row -> -Double.parseDouble(row.sumThisYear));
}
