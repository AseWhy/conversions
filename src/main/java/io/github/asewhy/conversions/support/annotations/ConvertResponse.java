package io.github.asewhy.conversions.support.annotations;

import io.github.asewhy.conversions.ConversionUtils;

import java.lang.annotation.*;

/**
 * Говорит что в этом методе (или классе есть методы) использующие возвращаемое значение, которое необходимо преобразовать
 * в формат ответа. Аннотация автоматические позволяет сущностям которые зарегистрированы в текущем сторе, преобразоваться
 * в зарегистрированный формат ответа.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConvertResponse {
    /**
     * Маппинг для конвертера
     */
    String mapping() default ConversionUtils.COMMON_MAPPING;
}
