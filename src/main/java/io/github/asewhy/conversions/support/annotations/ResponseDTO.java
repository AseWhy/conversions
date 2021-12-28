package io.github.asewhy.conversions.support.annotations;

import io.github.asewhy.conversions.ConversionUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * При автоматическом сканировании пакета регистрирует ответ в сторе,
 * и устанавливает маппинг ответа на указанный в аннотации.
 * При ручном добавлении, просто указывает маппинг
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseDTO {
    /**
     * Маппинг для конвертера
     */
    String mapping() default ConversionUtils.COMMON_MAPPING;
}
