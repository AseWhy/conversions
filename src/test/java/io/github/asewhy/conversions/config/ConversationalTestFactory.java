package io.github.asewhy.conversions.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.asewhy.conversions.ConversionStore;
import io.github.asewhy.conversions.support.annotations.EnableConversions;
import io.github.asewhy.conversions.support.iConversionFactory;
import org.springframework.stereotype.Component;

@Component
@EnableConversions
public class ConversationalTestFactory implements iConversionFactory {
    @Override
    public ConversionStore provideStore() {
        var store = new ConversionStore();
        store.from("io.github.asewhy.conversions.config.converters");
        return store;
    }

    @Override
    public ObjectMapper provideObjectMapper() {
        return new ObjectMapper();
    }
}
