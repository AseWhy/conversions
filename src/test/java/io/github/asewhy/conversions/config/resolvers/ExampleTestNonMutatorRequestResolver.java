package io.github.asewhy.conversions.config.resolvers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.asewhy.conversions.ConversionProvider;
import io.github.asewhy.conversions.RequestResolver;
import io.github.asewhy.conversions.config.converters.book.ExampleTestNonMutatorRequest;
import io.github.asewhy.conversions.support.annotations.ContextLoadable;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
@ContextLoadable
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

            provider.createMutator(data.getRequest(), node.get("request"));

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
