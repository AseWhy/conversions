package io.github.asewhy.conversions.support.annotations;

import java.lang.annotation.*;

/**
 * Говорит что данный параметр установлен как конвертируемый запрос, необязательно должен быть помечен классом мутатора
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConvertRequest {

}
