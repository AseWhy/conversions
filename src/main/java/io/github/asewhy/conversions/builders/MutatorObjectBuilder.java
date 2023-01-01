package io.github.asewhy.conversions.builders;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.asewhy.conversions.ConversionMutator;
import io.github.asewhy.conversions.ConversionProvider;
import io.github.asewhy.conversions.support.Buildable;

import java.util.HashMap;

public class MutatorObjectBuilder<P extends Buildable> extends HashMap<String, Object> implements Buildable {
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
            var config = provider.getConfig();
            var mapper = config.getObjectMapper();

            try {
                var tree = mapper.valueToTree(this);
                var result = (T) mapper.treeToValue(tree, target);

                provider.createMutator(result, tree);

                return result;
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        } else {
            return root.build(target);
        }
    }
}
