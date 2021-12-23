package io.github.asewhy.conversions;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.asewhy.conversions.support.CallbackNameStrategy;
import io.github.asewhy.conversions.support.iConversionFactory;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Getter
@Service
public class ConversionFactoryInternal {
    private final ObjectMapper objectMapper;
    private final ConversionStore store;
    private final Function<String, String> convertFieldName;
    private final iConversionFactory factory;

    @Autowired
    public ConversionFactoryInternal(iConversionFactory factory) {
        this.convertFieldName = factory::convertFieldName;
        this.objectMapper = factory.provideObjectMapper().copy();
        this.objectMapper.setPropertyNamingStrategy(new CallbackNameStrategy(this.convertFieldName, factory.provideExcludes()));
        this.store = factory.provideStore();
        this.factory = factory;
    }
}
