package io.github.asewhy.conversions.support.annotations;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Controller;

import java.lang.annotation.*;

@Documented
@Controller
@ConvertResponse
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConversionController {
    @AliasFor(annotation = Controller.class)
    String value() default "";
}
