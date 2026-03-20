package me.zhengjie.modules.keyuan.domain.statistics.transaction;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class SummaryDataExcelExport {

    @ExcelProperty("科目编号")
    private Long accountNumber;

    @ExcelProperty("科目名称")
    private String name;

    @ExcelProperty("初始余额")
    private long initialAmount;

    @ExcelProperty({"期初余额", "收入"})
    private long beginIncome;

    @ExcelProperty({"期初余额", "支出"})
    private long beginExpense;

    @ExcelProperty({"期初余额", "结余"})
    private long beginRemain;

    // ... 其他字段同理，可以用大括号实现复杂表头合并
    @ExcelProperty({"本期余额", "收入"})
    private long currentIncome;
    // ... 省略部分字段，保持文章清爽
}