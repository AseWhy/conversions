package io.github.asewhy.conversions.support.annotations;

import java.lang.annotation.*;

/**
 * При автоматическом сканировании пакета автоматически регистрирует мутатор в сторе.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MutatorDTO {

}
