package io.github.asewhy.conversions.support.annotations;

import io.github.asewhy.conversions.ConversionUtils;

import java.lang.annotation.*;

/**
 * При автоматическом сканировании пакета регистрирует ответ в сторе,
 * и устанавливает маппинг ответа на указанный в аннотации.
 * При ручном добавлении, просто указывает маппинг
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ResponseDTO {
    /**
     * Маппинг для конвертера
     */
    String mapping() default ConversionUtils.COMMON_MAPPING;
}
