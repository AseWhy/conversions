package io.github.asewhy.conversions.support.naming;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Политика именования
 */
public interface ConversionNamingStrategy {
    @NotNull
    String convert(String defaultName, @Nullable Class<?> rawReturnType);
}
