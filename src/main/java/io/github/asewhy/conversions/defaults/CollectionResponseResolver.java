package io.github.asewhy.conversions.defaults;

import io.github.asewhy.ReflectionUtils;
import io.github.asewhy.conversions.ConversionProvider;
import io.github.asewhy.conversions.ConversionResolver;
import io.github.asewhy.conversions.support.annotations.DataResolver;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
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
    protected boolean canProcess(Class<?> from, Class<?> generic, ConversionProvider provider, String mapping) {
        if(Collection.class.isAssignableFrom(from)) {
            return provider.getFactory().getStore().isPresentResponse(generic);
        } else {
            return false;
        }
    }

    @Override
    public Class<?> getConversionReference(@NotNull Method from) {
        var type = from.getReturnType();

        if(Collection.class.isAssignableFrom(type)) {
            return ReflectionUtils.findXGeneric(from);
        } else {
            return null;
        }
    }
}
