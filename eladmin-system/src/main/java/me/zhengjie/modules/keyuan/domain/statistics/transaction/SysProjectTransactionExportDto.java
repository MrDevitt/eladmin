package me.zhengjie.modules.keyuan.domain.statistics.transaction;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class SysProjectTransactionExportDto {

    @ExcelProperty("记录ID") // 如果内部使用，也可以用 @ExcelIgnore 忽略
    private Long id;

    @ExcelProperty("摘要")
    private String comment;

    /**
     * 使用 MoneyConverter：将 10050 转换为 100.50
     * 使用 @NumberFormat：在 Excel 中显示千分位
     */
    @ExcelProperty(value = "金额", converter = MoneyConverter.class)
    @NumberFormat("#,##0.00")
    private Long amount;

    /**
     * 交易类型（如果是数字，导出时显示 1/2，
     * 如果想显示“收入/支出”，建议后端处理好文本后再导出，或者使用 EasyExcel 的映射转换）
     */
    @ExcelProperty(value = "交易类型", converter = TransactionTypeConverter.class)
    private Integer direction;
    /**
     * 使用 AccountNumberConverter：防止长编号被 Excel 科学计数法
     */
    @ExcelProperty(value = "科目编号", converter = AccountNumberConverter.class)
    private Long accountNumber;

    @ExcelProperty(value = "科目名称")
    private String accountName;

    @ExcelProperty("记账凭证编号")
    private String certificateNumber;

    @ExcelProperty(value = "交易时间", converter = TimestampConverter.class)
    private Timestamp transactionTime;

}
