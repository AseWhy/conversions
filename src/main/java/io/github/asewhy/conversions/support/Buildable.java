package io.github.asewhy.conversions.support;

import io.github.asewhy.conversions.ConversionMutator;

public interface Buildable {
    <T extends ConversionMutator<?>> T build(Class<T> target);
}
