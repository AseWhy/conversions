package io.github.asewhy.conversions;

import io.github.asewhy.conversions.support.iBuildable;

import java.util.ArrayList;

public class MutatorArrayBuilder<P extends iBuildable> extends ArrayList<Object> implements iBuildable {
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
