package io.github.asewhy.conversions.support.annotations;

import java.lang.annotation.*;

/**
 * При создании экземпляра мутатора, стор сканирует мутируемый класс на совпадение полей с полями мутатора. Поля, которые не
 * совпадают с полями мутатора, не будут включены в поля, применяемые к мутируемому объекту.
 * <br>
 * Для {@link io.github.asewhy.conversions.ConversionResponse} это бесполезная аннотация
 */
@Target({ ElementType.FIELD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface IgnoreMatch {

}
