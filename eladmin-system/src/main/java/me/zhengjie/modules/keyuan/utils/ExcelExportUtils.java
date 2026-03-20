package me.zhengjie.modules.keyuan.utils;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import org.apache.commons.lang3.tuple.Triple;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExcelExportUtils {
    public static void exportSummaryData(HttpServletResponse response, List<Triple<Class<?>, List<?>, String>> dataList) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("result", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build()) {
            for (int i = 0; i < dataList.size(); i++) {
                Triple<Class<?>, List<?>, String> triple = dataList.get(i);
                WriteSheet sheet = EasyExcel.writerSheet(i, triple.getRight())
                        .head(triple.getLeft())
                        .build();
                excelWriter.write(triple.getMiddle(), sheet);
            }
        }
    }
}