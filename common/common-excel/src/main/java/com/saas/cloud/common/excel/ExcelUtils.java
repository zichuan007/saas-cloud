package com.saas.cloud.common.excel;

import cn.idev.excel.FastExcel;
import cn.idev.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Excel 导出工具类，基于 FastExcel（EasyExcel 继任者）
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
@Slf4j
public final class ExcelUtils {

    private ExcelUtils() {
    }

    /**
     * 导出 Excel 到 HTTP 响应（流式下载）
     *
     * @param response  HTTP 响应
     * @param fileName  文件名（不含后缀）
     * @param sheetName 工作表名称
     * @param clazz     数据类型
     * @param data      数据列表
     * @param <T>       数据泛型
     */
    public static <T> void write(HttpServletResponse response, String fileName, String sheetName,
                                 Class<T> clazz, List<T> data) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-Disposition", "attachment;filename=" + encodedFileName + ".xlsx");

        FastExcel.write(response.getOutputStream(), clazz)
                .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                .sheet(sheetName)
                .doWrite(data);
    }

    /**
     * 从输入流读取 Excel 数据
     *
     * @param inputStream 输入流
     * @param clazz       数据类型
     * @param <T>         数据泛型
     * @return 数据列表
     */
    public static <T> List<T> read(java.io.InputStream inputStream, Class<T> clazz) {
        return FastExcel.read(inputStream, clazz, null)
                .sheet()
                .doReadSync();
    }
}
