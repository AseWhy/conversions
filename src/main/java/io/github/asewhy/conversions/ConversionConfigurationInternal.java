package io.github.asewhy.conversions;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.asewhy.conversions.support.CallbackNameStrategy;
import io.github.asewhy.conversions.support.iConversionConfiguration;
import io.github.asewhy.conversions.support.naming.iConversionNamingStrategy;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Другими словами это кеш для конфигурации конвертера
 */
@Getter
@Service
public class ConversionConfigurationInternal {
    private final ObjectMapper objectMapper;
    private final ConversionStore store;
    private final CallbackNameStrategy callbackNameStrategy;
    private final iConversionNamingStrategy namingStrategy;

    @NotNull
    private final iConversionConfiguration config;

    @Autowired
    public ConversionConfigurationInternal(@NotNull iConversionConfiguration config) {
        this.objectMapper = config.objectMapper().copy();
        this.namingStrategy = config.namingStrategy();
        this.callbackNameStrategy = new CallbackNameStrategy(namingStrategy);
        this.store = config.conversionStore();
        this.config = config;

        this.objectMapper.setPropertyNamingStrategy(this.callbackNameStrategy);
    }
}
