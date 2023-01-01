package io.github.asewhy.conversions.config.resolvers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.asewhy.conversions.ConversionProvider;
import io.github.asewhy.conversions.RequestResolver;
import io.github.asewhy.conversions.config.converters.ExampleTestMutatorRequest;
import io.github.asewhy.conversions.config.converters.ExampleTestNonMutatorRequest;
import io.github.asewhy.conversions.support.annotations.DataResolver;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Map;

@Component
@DataResolver
@SuppressWarnings("unchecked")
public class ExampleTestNonMutatorRequestResolver extends RequestResolver<ExampleTestNonMutatorRequest> {
    @Override
    protected ExampleTestNonMutatorRequest resolveInternalRequest(
        @NotNull JsonNode node,
        Class<? extends ExampleTestNonMutatorRequest> fromClass,
        Type generics,
        @NotNull ConversionProvider provider
    ) {
        var config = provider.getConfig();
        var objectMapper = config.getObjectMapper();

        try {
            var data = objectMapper.treeToValue(node, ExampleTestNonMutatorRequest.class);

            provider.createMutator(data.getRequest(), objectMapper.treeToValue(node.get("request"), Map.class));

            return data;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean canProcess(Class<?> from, Type generics, ConversionProvider provider) {
        return ExampleTestNonMutatorRequest.class.isAssignableFrom(from);
    }
}
