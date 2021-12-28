package io.github.asewhy.conversions.support.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * При создании инстанса мутатора, стор сканирует мутируемый класс на совпадение полей с полями мутатора, и поля, которые не
 * совпадают с полями мутатора, не будут включены в поля, применяемые к мутируемому объекту, и которыые запишет в
 * availableFields мутатора.
 *
 * Для {@link io.github.asewhy.conversions.ConversionResponse} это бесполезная аннотация
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreMatch {

}
