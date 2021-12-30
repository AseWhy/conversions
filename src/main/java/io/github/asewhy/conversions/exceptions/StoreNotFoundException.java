package io.github.asewhy.conversions.exceptions;

import org.jetbrains.annotations.NotNull;

public class StoreNotFoundException extends RuntimeException {
    public StoreNotFoundException(@NotNull Object object) {
        super("Store not be provided on this ConversionMutator, use @ConvertMutator annotation for fill it automatically. " + object.getClass());
    }
}
