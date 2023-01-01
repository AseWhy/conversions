package io.github.asewhy.conversions.support.annotations;

import io.github.asewhy.conversions.ResponseResolver;

import java.lang.annotation.*;

/**
 * Помечает класс как обработчик {@link ResponseResolver} сущностей ответов
 *
 * Аннотируемый компонент должен является компонентом spring
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataResolver {

}
