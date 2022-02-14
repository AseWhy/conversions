package io.github.asewhy.conversions.support.annotations;

import java.lang.annotation.*;

/**
 * Указывает на то, что этот класс является получателем контекста для сущностей ответа
 *
 * Аннотируемый компонент должен является компонентом spring
 *
 * Преобразование контекста работает только на уровне {@link io.github.asewhy.conversions.ConversionProvider#createResponseResolve(Object, String, Object)} или выше
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ContextRecipient {
}
