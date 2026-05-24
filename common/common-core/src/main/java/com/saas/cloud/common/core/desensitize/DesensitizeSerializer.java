package com.saas.cloud.common.core.desensitize;

import java.io.IOException;
import java.lang.annotation.Annotation;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;

/**
 * 数据脱敏 Jackson 序列化器
 * <p>配合 {@link DesensitizeBy} 元注解使用，在 JSON 序列化时自动对标注了脱敏注解的字段进行脱敏处理。</p>
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-24
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class DesensitizeSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private DesensitizeHandler handler;

    private Annotation annotation;

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (handler == null || value == null) {
            gen.writeString(value);
            return;
        }
        gen.writeString(handler.desensitize(value, annotation));
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property)
            throws JsonMappingException {
        if (property == null) {
            return prov.findNullValueSerializer(null);
        }
        if (!String.class.equals(property.getType().getRawClass())) {
            return prov.findValueSerializer(property.getType(), property);
        }
        for (Annotation ann : property.getMember().getAllAnnotations().annotations()) {
            DesensitizeBy desensitizeBy = ann.annotationType().getAnnotation(DesensitizeBy.class);
            if (desensitizeBy != null) {
                try {
                    DesensitizeSerializer serializer = new DesensitizeSerializer();
                    serializer.handler = desensitizeBy.handler().getDeclaredConstructor().newInstance();
                    serializer.annotation = ann;
                    return serializer;
                } catch (Exception e) {
                    throw JsonMappingException.from(prov,
                            "创建脱敏处理器失败: " + desensitizeBy.handler().getName(), e);
                }
            }
        }
        return this;
    }
}
