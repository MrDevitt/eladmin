package me.zhengjie.modules.keyuan.domain.statistics.transaction;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import lombok.Data;

import java.util.List;

@Data
public class SummaryData {

    // 使用 AccountNumberConverter 强制导出为文本，防止科学计数法
    @ExcelProperty(value = "科目编号", converter = AccountNumberConverter.class)
    private Long accountNumber;

    @ExcelProperty("科目名称")
    private String name;

    // 使用 MoneyConverter 除以100。
    // 配合 @NumberFormat("#,##0.00") 可以在 Excel 里呈现完美的会计千分位格式！
    @ExcelProperty(value = "初始余额", converter = MoneyConverter.class)
    @NumberFormat("###,##0.00")
    private long initialAmount;

    @ExcelProperty(value = {"期初余额", "收入"}, converter = MoneyConverter.class)
    @NumberFormat("###,##0.00")
    private long beginIncome;

    @ExcelProperty(value = {"期初余额", "支出"}, converter = MoneyConverter.class)
    @NumberFormat("#,##0.00")
    private long beginExpense;

    @ExcelProperty(value = {"期初余额", "结余"}, converter = MoneyConverter.class)
    @NumberFormat("#,##0.00")
    private long beginRemain;

    @ExcelProperty(value = {"本期余额", "收入"}, converter = MoneyConverter.class)
    @NumberFormat("#,##0.00")
    private long currentIncome;

    @ExcelProperty(value = {"本期余额", "支出"}, converter = MoneyConverter.class)
    @NumberFormat("#,##0.00")
    private long currentExpense;

    @ExcelProperty(value = {"本期余额", "结余"}, converter = MoneyConverter.class)
    @NumberFormat("#,##0.00")
    private long currentRemain;

    @ExcelProperty(value = {"期末余额", "收入"}, converter = MoneyConverter.class)
    @NumberFormat("#,##0.00")
    private long endIncome;

    @ExcelProperty(value = {"期末余额", "支出"}, converter = MoneyConverter.class)
    @NumberFormat("#,##0.00")
    private long endExpense;

    @ExcelProperty(value = {"期末余额", "结余"}, converter = MoneyConverter.class)
    @NumberFormat("#,##0.00")
    private long endRemain;
    @ExcelIgnore
    private long remainingShare;
    @ExcelIgnore
    private long guaranteeAmount;

    @ExcelIgnore
    private List<SummaryData> children;

    public SummaryData add(SummaryData other) {
        this.initialAmount += other.initialAmount;
        this.beginIncome += other.beginIncome;
        this.beginExpense += other.beginExpense;
        this.currentIncome += other.currentIncome;
        this.currentExpense += other.currentExpense;
        this.endIncome += other.endIncome;
        this.endExpense += other.endExpense;
        this.remainingShare += other.remainingShare;
        this.guaranteeAmount += other.guaranteeAmount;
        return this;
    }

    public void calcRemain() {
        this.beginRemain = this.beginIncome - this.beginExpense + this.initialAmount;
        this.currentRemain = this.currentIncome - this.currentExpense;
        this.endRemain = this.endIncome - this.endExpense + this.initialAmount;
    }

    public boolean isEmptyValue() {
        return this.initialAmount == 0 &&
                this.beginIncome == 0 &&
                this.beginExpense == 0 &&
                this.currentIncome == 0 &&
                this.currentExpense == 0 &&
                this.endIncome == 0 &&
                this.endExpense == 0;
    }
}
