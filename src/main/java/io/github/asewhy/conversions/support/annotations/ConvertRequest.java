package io.github.asewhy.conversions.support.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Говорит что данный параметр установлен как конвертируемый запрос, необязательно должен быть помечен классом мутатора
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConvertRequest {

}
