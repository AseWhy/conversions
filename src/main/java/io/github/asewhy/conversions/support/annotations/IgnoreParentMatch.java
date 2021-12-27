package io.github.asewhy.conversions.support.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация говорит, что это поле должно быть заполнено из пейлоада, даже несмотря на то, что его нет в мутируемом
 * классе (класс который передается дженериком в ConversionMutator<Мутируемый класс>)
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IgnoreParentMatch {

}
