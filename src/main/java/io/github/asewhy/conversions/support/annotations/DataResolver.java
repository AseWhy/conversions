package io.github.asewhy.conversions.support.annotations;

import java.lang.annotation.*;

/**
 * Помечает класс как обработчик {@link io.github.asewhy.conversions.ConversionResolver} сущностей ответов
 *
 * Аннотируемый компонент должен является компонентом spring
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataResolver {

}
