package io.github.asewhy.conversions.support.annotations;

import java.lang.annotation.*;

/**
 * Указывает, что данный компонент должен быть загружен стором для дальнейшего использования конверсиями
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ContextLoadable {
}
