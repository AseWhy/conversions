package io.github.asewhy.conversions.support.naming;

import org.jetbrains.annotations.NotNull;

/**
 * Исключающая политика именования. Позволяет исключить некоторые классы или поля некоторых классов из политики.
 */
public abstract class ExtrudableNamingStrategy implements iConversionNamingStrategy {
    protected abstract boolean isExcluded(@NotNull String defaultName, @NotNull Class<?> rawReturnType);
    protected abstract String convert(@NotNull String defaultName);

    @Override
    public final @NotNull String convert(String defaultName, Class<?> rawReturnType) {
        if(rawReturnType != null && isExcluded(defaultName, rawReturnType)) {
            return defaultName;
        }

        return convert(defaultName);
    }
}
