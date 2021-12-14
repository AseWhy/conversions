package io.github.asewhy.conversions;

public abstract class ConversionResponse<T> {
    protected void fillInternal(T from, Object context) {
        // Stub
    }

    protected void fillInternal(T from, ConversionProvider provider, Object context) {
        fillInternal(from, context);
    }
}
