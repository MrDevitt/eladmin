package me.zhengjie.modules.keyuan.domain.statistics.transaction;

import com.alibaba.excel.converters.Converter;
import com.alibaba.excel.enums.CellDataTypeEnum;
import com.alibaba.excel.metadata.GlobalConfiguration;
import com.alibaba.excel.metadata.data.WriteCellData;
import com.alibaba.excel.metadata.property.ExcelContentProperty;

public class TransactionTypeConverter implements Converter<Integer> {
    @Override
    public Class<Integer> supportJavaTypeKey() {
        return Integer.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public WriteCellData<?> convertToExcelData(Integer value, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        if (value == null) {
            return new WriteCellData<>("/");
        }
        switch (value) {
            case 0:
                return new WriteCellData<>("收入");
            case 1:
                return new WriteCellData<>("支出");
            default:
                return new WriteCellData<>("/");
        }
    }
}
