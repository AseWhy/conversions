package io.github.asewhy.conversions.support;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import io.github.asewhy.conversions.support.naming.iConversionNamingStrategy;

public class CallbackNameStrategy extends PropertyNamingStrategy {
    private final iConversionNamingStrategy strategy;

    public CallbackNameStrategy(iConversionNamingStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public String nameForConstructorParameter(MapperConfig<?> config, AnnotatedParameter ctorParam, String defaultName) {
        return strategy.convert(defaultName, ctorParam.getDeclaringClass());
    }

    @Override
    public String nameForField(MapperConfig config, AnnotatedField field, String defaultName) {
        return strategy.convert(defaultName, field.getDeclaringClass());
    }

    @Override
    public String nameForGetterMethod(MapperConfig config, AnnotatedMethod method, String defaultName) {
        return strategy.convert(defaultName, method.getDeclaringClass());
    }

    @Override
    public String nameForSetterMethod(MapperConfig config, AnnotatedMethod method, String defaultName) {
        return strategy.convert(defaultName, method.getDeclaringClass());
    }
}