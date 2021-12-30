package io.github.asewhy.conversions.support.annotations;

import java.lang.annotation.*;

/**
 * В случае если сущность запроса будет состоять как член какой-либо коллекции, то эта аннотация
 * будет указывать на поле-идентификатор этой сущности работает только в {@link io.github.asewhy.conversions.ConversionMutator}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Identifier {

}
