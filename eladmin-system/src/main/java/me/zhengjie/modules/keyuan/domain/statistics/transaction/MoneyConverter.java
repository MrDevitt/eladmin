package me.zhengjie.modules.keyuan.domain.statistics.transaction;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MoneyConverter implements Converter<Long> {
    @Override
    public Class<Long> supportJavaTypeKey() {
        return Long.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.NUMBER; // 告诉 Excel 这是一个数字
    }

    @Override
    public WriteCellData<?> convertToExcelData(Long value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        if (value == null) {
            return new WriteCellData<>(new BigDecimal("0.00"));
        }
        // 将金额除以 100，保留两位小数
        BigDecimal result = new BigDecimal(value).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        return new WriteCellData<>(result);
    }
}