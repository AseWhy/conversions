package io.github.asewhy.conversions.support;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class CallbackNameStrategy extends PropertyNamingStrategy {
    private final Function<String, String> callback;
    private final Map<Class<?>, String> excludes;

    public CallbackNameStrategy(Function<String, String> callback, Map<Class<?>, String> excludes) {
        this.callback = callback;
        this.excludes = excludes;
    }

    @Override
    public String nameForConstructorParameter(MapperConfig<?> config, AnnotatedParameter ctorParam, String defaultName) {
        return convert(defaultName, ctorParam.getDeclaringClass());
    }

    @Override
    public String nameForField(MapperConfig config, AnnotatedField field, String defaultName) {
        return convert(defaultName, field.getDeclaringClass());
    }

    @Override
    public String nameForGetterMethod(MapperConfig config, AnnotatedMethod method, String defaultName) {
        return convert(defaultName, method.getDeclaringClass());
    }

    @Override
    public String nameForSetterMethod(MapperConfig config, AnnotatedMethod method, String defaultName) {
        return convert(defaultName, method.getDeclaringClass());
    }

    public String convert(String defaultName, Class<?> rawReturnType) {
        var found = excludes.get(rawReturnType);

        if((excludes.containsKey(rawReturnType) && Objects.equals(found, "$any")) || found != null && Objects.equals(found, defaultName)) {
            return defaultName;
        }

        return callback.apply(defaultName);
    }
}