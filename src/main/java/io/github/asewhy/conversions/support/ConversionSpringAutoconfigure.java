package io.github.asewhy.conversions.support;

import io.github.asewhy.conversions.ConversionProvider;
import io.github.asewhy.conversions.MutatorArgumentResolver;
import io.github.asewhy.conversions.ResponseMessageHandler;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ConversionSpringAutoconfigure implements WebMvcConfigurer {
    @Autowired
    private ConversionProvider provider;

    @Override
    public void addArgumentResolvers(@NotNull List<HandlerMethodArgumentResolver> resolvers){
        resolvers.add(new MutatorArgumentResolver(provider));
    }

    @Override
    public void addReturnValueHandlers(@NotNull List<HandlerMethodReturnValueHandler> handlers) {
        var messageConverters = new ArrayList<HttpMessageConverter<?>>();
        messageConverters.add(new MappingJackson2HttpMessageConverter(provider.getFactory().getObjectMapper()));
        handlers.add(new ResponseMessageHandler(messageConverters, provider));
    }
}
