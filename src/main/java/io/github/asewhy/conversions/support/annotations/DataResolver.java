package io.github.asewhy.conversions.support.annotations;

import java.lang.annotation.*;

/**
 * Помечает класс как обработчик {@link io.github.asewhy.conversions.ConversionResolver} сущностей ответов
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataResolver {

}
