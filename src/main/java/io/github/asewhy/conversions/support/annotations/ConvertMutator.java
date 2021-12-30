package io.github.asewhy.conversions.support.annotations;

import java.lang.annotation.*;

/**
 * Говорит что данный параметр установлен как мутатор, для мутаторов помеченных при получении
 * этой аннотацией происходит их автоматическая инициализация стора, после чего данными этих
 * мутаторов можно заполнять другие сущности встроенным методом fill
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConvertMutator {

}
