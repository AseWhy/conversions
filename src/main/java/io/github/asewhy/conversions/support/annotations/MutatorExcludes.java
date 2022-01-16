package io.github.asewhy.conversions.support.annotations;

import java.lang.annotation.*;

/**
 * При стандартном методе requireProcess отключает обработку поля для аннотированного поля
 */

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MutatorExcludes {
}
