package io.github.asewhy.conversions.defaults;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.asewhy.ReflectionUtils;
import io.github.asewhy.conversions.ConversionMutator;
import io.github.asewhy.conversions.ConversionProvider;
import io.github.asewhy.conversions.RequestResolver;
import io.github.asewhy.conversions.support.annotations.DataResolver;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@DataResolver
@SuppressWarnings("unchecked")
public class CollectionRequestResolver extends RequestResolver<Collection<?>> {
    @Override
    protected Collection<?> resolveInternalRequest(
        JsonNode node,
        Class<? extends Collection<?>> fromClass,
        Type generics,
        @NotNull ConversionProvider provider
    ) {
        var config = provider.getConfig();
        var store = config.getStore();
        var objectMapper = config.getObjectMapper();
        var generic = ReflectionUtils.findXGeneric(generics, 0);

        try {
            var result = objectMapper.treeToValue(node, fromClass);

            if(generic != null && store.isPresentMutator(generic)) {
                var parsedGeneric = (Class<? extends ConversionMutator<?>>) generic;
                var ghosts = ReflectionUtils.makeCollectionInstance(fromClass);

                for (var current : result) {
                    if (current instanceof HashMap<?, ?>) {
                        var mirror = (Map<?, ?>) current;
                        var mutator = objectMapper.convertValue(mirror, parsedGeneric);
                        var castedMirror = (Map<String, Object>) mirror;

                        provider.createMutator(mutator, castedMirror);

                        ghosts.add(mutator);
                    }
                }

                return ghosts;
            } else {
                return null;
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected boolean canProcess(Class<?> from, Type generics, ConversionProvider provider) {
        return Collection.class.isAssignableFrom(from);
    }
}
