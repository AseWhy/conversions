package io.github.asewhy.conversions.defaults;

import io.github.asewhy.ReflectionUtils;
import io.github.asewhy.conversions.ConversionProvider;
import io.github.asewhy.conversions.ConversionResolver;
import io.github.asewhy.conversions.support.annotations.DataResolver;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.Collection;

@Component
@DataResolver
@SuppressWarnings({"unchecked", "unused"})
public class CollectionResponseResolver extends ConversionResolver<Collection<?>> {
    @Override
    public Collection<?> resolveInternalResponse(@NotNull Collection<?> from, Class<? extends Collection<?>> type, ConversionProvider provider, String mapping) {
        var iterator = from.iterator();
        var result = ReflectionUtils.makeCollectionInstance(type);

        while(iterator.hasNext()) {
            result.add(provider.createResponse(iterator.next(), mapping));
        }

        return result;
    }

    @Override
    protected Class<?> extractInternalExample(@NotNull Collection<?> from, String mapping, Object globalContextOrPassedContext) {
        return from.stream().map(Object::getClass).findFirst().orElse(null);
    }

    @Override
    protected boolean canProcess(Class<?> from, Type generics, ConversionProvider provider, String mapping) {
        return Collection.class.isAssignableFrom(from);
    }
}
