package io.github.asewhy.conversions.defaults;

import io.github.asewhy.ReflectionUtils;
import io.github.asewhy.conversions.ConversionProvider;
import io.github.asewhy.conversions.ConversionResolver;
import io.github.asewhy.conversions.support.annotations.DataResolver;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@DataResolver
@SuppressWarnings("unchecked")
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
    protected boolean canProcess(Class<?> from, Class<?> generic, ConversionProvider provider, String mapping) {
        if(Collection.class.isAssignableFrom(from)) {
            return provider.getFactory().getStore().isPresentResponse(generic);
        } else {
            return false;
        }
    }
}
