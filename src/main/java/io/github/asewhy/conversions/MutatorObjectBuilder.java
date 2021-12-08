package io.github.asewhy.conversions;

import io.github.asewhy.conversions.support.iBuildable;

import java.util.HashMap;

public class MutatorObjectBuilder<P extends iBuildable> extends HashMap<String, Object> implements iBuildable {
    private final ConversionProvider provider;
    private final P root;

    public MutatorObjectBuilder(ConversionProvider factory, P root) {
        this.provider = factory;
        this.root = root;
    }

    public MutatorArrayBuilder<MutatorObjectBuilder<P>> nestedArray(String name) {
        var result = new MutatorArrayBuilder<>(provider, this);
        this.put(name, result);
        return result;
    }

    public MutatorObjectBuilder<MutatorObjectBuilder<P>> nestedObject(String name) {
        var result = new MutatorObjectBuilder<>(provider, this);
        this.put(name, result);
        return result;
    }

    public MutatorObjectBuilder<P> with(String name, Object value) {
        this.put(name, value); return this;
    }

    public P back() {
        return root;
    }

    public <T extends ConversionMutator<?>> T build(Class<T> target) {
        if(root == null) {
            var factory = provider.getFactory();
            var result = (T) factory.getObjectMapper().convertValue(this, target);

            provider.createMutator(result, this);

            return result;
        } else {
            return root.build(target);
        }
    }
}
