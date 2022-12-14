package io.github.asewhy.conversions.builders;

import io.github.asewhy.conversions.ConversionMutator;
import io.github.asewhy.conversions.ConversionProvider;
import io.github.asewhy.conversions.support.Buildable;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class MutatorArrayBuilder<P extends Buildable> extends ArrayList<Object> implements Buildable {
    private final ConversionProvider factory;
    private final P root;

    public MutatorArrayBuilder(ConversionProvider factory, P root) {
        this.factory = factory;
        this.root = root;
    }

    public MutatorArrayBuilder<MutatorArrayBuilder<P>> nestedArray() {
        var result = new MutatorArrayBuilder<>(factory, this);
        this.add(result);
        return result;
    }

    public MutatorObjectBuilder<MutatorArrayBuilder<P>> nestedObject() {
        var result = new MutatorObjectBuilder<>(factory, this);
        this.add(result);
        return result;
    }

    public MutatorArrayBuilder<P> with(Object value) {
        this.add(value); return this;
    }

    public P back() {
        return root;
    }

    public <T extends ConversionMutator<?>> T build(Class<T> target) {
        return root.build(target);
    }
}
