package io.github.asewhy.conversions;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.asewhy.conversions.support.CallbackNameStrategy;
import io.github.asewhy.conversions.support.iConversionFactory;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Другими словами это кеш для фабрики конвертера
 */

@Getter
@Service
public class ConversionFactoryInternal {
    private final ObjectMapper objectMapper;
    private final ConversionStore store;
    private final Function<String, String> convertFieldName;
    private final iConversionFactory factory;
    private final Map<Class<?>, Set<String>> conversionExcludes;
    private final CallbackNameStrategy callbackNameStrategy;

    @Autowired
    public ConversionFactoryInternal(@NotNull iConversionFactory factory) {
        this.convertFieldName = factory::convertFieldName;
        this.conversionExcludes = factory.provideExcludes();
        this.objectMapper = factory.provideObjectMapper().copy();
        this.callbackNameStrategy = new CallbackNameStrategy(this.convertFieldName, this.conversionExcludes);
        this.objectMapper.setPropertyNamingStrategy(this.callbackNameStrategy);
        this.store = factory.provideStore();
        this.factory = factory;
    }
}
