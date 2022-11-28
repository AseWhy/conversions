package io.github.asewhy.conversions.support.annotations;

import io.github.asewhy.conversions.ConversionUtils;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Controller;

import java.lang.annotation.*;


/**
 * Используется для автоматического преобразования сущностей
 * которые помечены как ResponseDTO в формат ответа
 * <p>
 * Для преобразования необходима регистрация стора в поставляемой конфигурации
 */
@Documented
@Controller
@ConvertResponse
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ShiftController {
    /**
     * Название бин компонента для этого контроллера {{@link Controller#value()}}
     */
    @AliasFor(annotation = Controller.class)
    String value() default "";
    /**
     * Маппинг для конвертера
     */
    @AliasFor(annotation = ConvertResponse.class)
    String mapping() default ConversionUtils.COMMON_MAPPING;
}
