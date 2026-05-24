package com.saas.cloud.common.excel.dict;

import java.lang.reflect.Field;
import java.util.Map;

import cn.idev.excel.converters.Converter;
import cn.idev.excel.converters.ReadConverterContext;
import cn.idev.excel.converters.WriteConverterContext;
import cn.idev.excel.enums.CellDataTypeEnum;
import cn.idev.excel.metadata.data.WriteCellData;
import com.saas.cloud.common.excel.annotation.DictFormat;
import lombok.extern.slf4j.Slf4j;

/**
 * 字典格式化转换器
 * <p>配合 {@link DictFormat} 注解使用，导出时将字典值转为中文标签，导入时将标签转回字典值。</p>
 * <p>使用方式：在 @ExcelProperty 中指定 converter = DictFormatConverter.class，
 * 同时在字段上标注 @DictFormat("dict_type_code")。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@Slf4j
public class DictFormatConverter implements Converter<Object> {

    private static DictDataProvider dictDataProvider;

    /**
     * 初始化字典数据提供者（由自动配置调用）
     */
    public static void init(DictDataProvider provider) {
        dictDataProvider = provider;
    }

    @Override
    public Class<?> supportJavaTypeKey() {
        return Object.class;
    }

    @Override
    public CellDataTypeEnum supportExcelTypeKey() {
        return CellDataTypeEnum.STRING;
    }

    @Override
    public Object convertToJavaData(ReadConverterContext<?> context) {
        String label = context.getReadCellData().getStringValue();
        if (label == null || label.isBlank() || dictDataProvider == null) {
            return label;
        }

        String dictType = getDictType(context.getContentProperty().getField());
        if (dictType == null) {
            return label;
        }

        Map<String, String> dictMap = dictDataProvider.getDictDataMap(dictType);
        for (Map.Entry<String, String> entry : dictMap.entrySet()) {
            if (entry.getValue().equals(label)) {
                return entry.getKey();
            }
        }
        return label;
    }

    @Override
    public WriteCellData<?> convertToExcelData(WriteConverterContext<Object> context) {
        Object value = context.getValue();
        if (value == null || dictDataProvider == null) {
            return new WriteCellData<>(value != null ? value.toString() : "");
        }

        String dictType = getDictType(context.getContentProperty().getField());
        if (dictType == null) {
            return new WriteCellData<>(value.toString());
        }

        Map<String, String> dictMap = dictDataProvider.getDictDataMap(dictType);
        String label = dictMap.get(value.toString());
        return new WriteCellData<>(label != null ? label : value.toString());
    }

    private String getDictType(Field field) {
        if (field == null) {
            return null;
        }
        DictFormat dictFormat = field.getAnnotation(DictFormat.class);
        return dictFormat != null ? dictFormat.value() : null;
    }
}
