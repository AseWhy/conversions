package io.github.asewhy.conversions.support.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * В случае если сущность запроса будет состоять как член какой-либо коллекции, то эта аннотация
 * будет указывать на поле-идентификатор этой сущности работает только в {@link io.github.asewhy.conversions.ConversionMutator}
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Identifier {

}
