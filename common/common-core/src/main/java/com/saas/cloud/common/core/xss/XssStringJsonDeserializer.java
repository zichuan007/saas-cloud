package com.saas.cloud.common.core.xss;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * Jackson String 反序列化时自动进行 XSS 清洗
 *
 * @author saas-cloud
 * @version V1.0
 * @since 2026-05-21
 */
public class XssStringJsonDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        return XssUtil.clean(value);
    }

    @Override
    public Class<String> handledType() {
        return String.class;
    }
}
