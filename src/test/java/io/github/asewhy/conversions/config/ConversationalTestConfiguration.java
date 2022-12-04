package io.github.asewhy.conversions.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import io.github.asewhy.conversions.ConversionStore;
import io.github.asewhy.conversions.support.annotations.EnableConversions;
import io.github.asewhy.conversions.support.iConversionConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@EnableConversions
public class ConversationalTestConfiguration implements iConversionConfiguration {
    @Override
    public ConversionStore conversionStore() {
        var store = new ConversionStore();
        store.from("io.github.asewhy.conversions.config.converters");
        return store;
    }

    @Override
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public Faker faker() {
        return new Faker();
    }
}
