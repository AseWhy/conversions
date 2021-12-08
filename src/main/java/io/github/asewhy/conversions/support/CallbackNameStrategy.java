package io.github.asewhy.conversions.support;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;

import java.util.function.Function;

public class CallbackNameStrategy extends PropertyNamingStrategy {
    private final Function<String, String> callback;

    public CallbackNameStrategy(Function<String, String> callback) {
        this.callback = callback;
    }

    @Override
    public String nameForConstructorParameter(MapperConfig<?> config, AnnotatedParameter ctorParam, String defaultName) {
        return convert(defaultName);
    }

    @Override
    public String nameForField(MapperConfig config, AnnotatedField field, String defaultName) {
        return convert(defaultName);
    }

    @Override
    public String nameForGetterMethod(MapperConfig config, AnnotatedMethod method, String defaultName) {
        return convert(defaultName);
    }

    @Override
    public String nameForSetterMethod(MapperConfig config, AnnotatedMethod method, String defaultName) {
        return convert(defaultName);
    }

    public String convert(String defaultName) {
        return callback.apply(defaultName);
    }
}