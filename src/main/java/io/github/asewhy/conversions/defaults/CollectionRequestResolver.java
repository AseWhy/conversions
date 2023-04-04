package io.github.asewhy.conversions.defaults;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.asewhy.ReflectionUtils;
import io.github.asewhy.conversions.ConversionMutator;
import io.github.asewhy.conversions.ConversionProvider;
import io.github.asewhy.conversions.RequestResolver;
import io.github.asewhy.conversions.support.annotations.ContextLoadable;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Collection;

@Component
@ContextLoadable
@SuppressWarnings("unchecked")
public class CollectionRequestResolver extends RequestResolver<Collection<?>> {
    @Override
    protected Collection<?> resolveInternalRequest(
        JsonNode node,
        Class<? extends Collection<?>> fromClass,
        Type generics,
        @NotNull ConversionProvider provider
    ) {
        if(!node.isArray()) {
            return ReflectionUtils.makeCollectionInstance(fromClass);
        }

        var config = provider.getConfig();
        var store = config.getStore();
        var objectMapper = config.getObjectMapper();
        var generic = ReflectionUtils.findXGeneric(generics, 0);

        try {
            if(generic != null && store.isPresentMutator(generic)) {
                var collection = ReflectionUtils.makeCollectionInstance(fromClass);

                for (var current: node) {
                    var mutator = objectMapper.convertValue(current, (Class<? extends ConversionMutator<?>>) generic);

                    if (current.isObject()) {
                        provider.createMutator(mutator, current);
                    }

                    collection.add(mutator);
                }

                return collection;
            } else {
                return objectMapper.treeToValue(node, fromClass);
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
