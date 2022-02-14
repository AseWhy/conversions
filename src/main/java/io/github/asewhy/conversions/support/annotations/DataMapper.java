package io.github.asewhy.conversions.support.annotations;

import io.github.asewhy.conversions.ConversionMapper;

import java.lang.annotation.*;

/**
 * Помечает класс как обработчик {@link ConversionMapper} маппингов для сущностей ответа
 *
 * Аннотируемый компонент должен является компонентом spring
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataMapper {

}
