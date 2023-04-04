package io.github.asewhy.conversions.support;

import org.springframework.core.annotation.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings("unchecked")
public class BoundedAccessible implements Bound {
    protected final Field field;
    protected final Method getter;
    protected final Method setter;
    protected final Map<Class<? extends Annotation>, Annotation> annotations;

    public BoundedAccessible(
        Field field,
        Method getter,
        Method setter
    ) {
        this.field = field;
        this.getter = getter;
        this.setter = setter;
        this.annotations = new HashMap<>();

        if(field != null) {
            for(var current: field.getDeclaredAnnotations()) {
                annotations.put(current.getClass(), current);
            }
        }

        if(getter != null) {
            for(var current: getter.getDeclaredAnnotations()) {
                annotations.put(current.getClass(), current);
            }
        }

        if(setter != null) {
            for(var current: setter.getDeclaredAnnotations()) {
                annotations.put(current.getClass(), current);
            }
        }
    }

    @Override
    public Field getField() {
        return field;
    }

    @Override
    public Method getGetter() {
        return getter;
    }

    @Override
    public Method getSetter() {
        return setter;
    }

    @Override
    public Collection<Annotation> getAnnotations() {
        return annotations.values();
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotation) {
        if(annotations.containsKey(annotation)) {
            return (T) annotations.get(annotation);
        } else {
            return annotations
                .values()
                .stream()
                .map(e -> AnnotationUtils.getAnnotation(e, annotation))
                .filter(Objects::nonNull)
                .findAny()
            .orElse(null);
        }
    }
}
