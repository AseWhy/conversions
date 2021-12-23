package io.github.asewhy.conversions.support;

import io.github.asewhy.conversions.ConversionProvider;
import io.github.asewhy.conversions.MutatorArgumentResolver;
import io.github.asewhy.conversions.ResponseMessageHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class ConversionSpringAutoconfigure implements WebMvcConfigurer {
    @Autowired
    protected ConversionProvider provider;

    @Override
    public void addArgumentResolvers(@NotNull List<HandlerMethodArgumentResolver> resolvers){
        resolvers.add(new MutatorArgumentResolver(provider));
    }

    @Override
    public void addReturnValueHandlers(@NotNull List<HandlerMethodReturnValueHandler> handlers) {
        handlers.add(0, new ResponseMessageHandler(List.of(new MappingJackson2HttpMessageConverter(provider.getFactory().getObjectMapper())), provider));
    }
}
