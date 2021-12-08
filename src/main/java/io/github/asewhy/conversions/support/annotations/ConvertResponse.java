package io.github.asewhy.conversions.support.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Говорит что в этом методе (или классе есть методы) использующие возвращаемое значение, которое необходимо преобразовать
 * в формат ответа. Аннотация автоматические позволяет сущностям которые зарегистрированы в текущем сторе, преобразоваться
 * в зарегистрированный формат ответа.
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ConvertResponse {

}
