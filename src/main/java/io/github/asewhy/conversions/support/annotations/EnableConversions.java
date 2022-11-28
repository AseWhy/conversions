package io.github.asewhy.conversions.support.annotations;

import io.github.asewhy.conversions.ConversionConfigurationInternal;
import io.github.asewhy.conversions.ConversionProvider;
import io.github.asewhy.conversions.support.ConversionSpringAutoconfigure;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({ConversionSpringAutoconfigure.class, ConversionConfigurationInternal.class, ConversionProvider.class})
public @interface EnableConversions {

}
