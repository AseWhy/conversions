package io.github.asewhy.conversions.support.annotations;

import io.github.asewhy.conversions.ConversionUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Бесполезна при ручном добавлении мутаторов. При сканировании текущего пути класса автоматические регистрирует
 * текущий помеченный тип ответа в сторе.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseDTO {
    /**
     * Маппинг для конвертера
     */
    String mapping() default ConversionUtils.COMMON_MAPPING;
}
