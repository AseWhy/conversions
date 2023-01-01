package io.github.asewhy.conversions.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import io.github.asewhy.conversions.ConversionStore;
import io.github.asewhy.conversions.support.annotations.EnableConversions;
import io.github.asewhy.conversions.support.ConversionConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;

@Component
@EnableConversions
@ComponentScan("io.github.asewhy.conversions.config.*")
public class ConversationalTestConfiguration implements ConversionConfiguration {
    @Autowired
    protected ApplicationContext context;

    @Override
    public ConversionStore conversionStore() {
        var store = new ConversionStore(context);
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
