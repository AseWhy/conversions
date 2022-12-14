package io.github.asewhy.conversions.support;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("unchecked")
public class BoundedReceiver implements Bound {
    private final AccessibleObject accessible;
    private final Map<Class<? extends Annotation>, Annotation> annotations;

    public BoundedReceiver(AccessibleObject accessible, @NotNull Set<Annotation> annotations) {
        this.accessible = accessible;
        this.annotations = annotations.stream().collect(Collectors.toMap(Annotation::getClass, e -> e));
    }

    @Override
    public Collection<Annotation> getAnnotations() {
        return annotations.values();
    }

    @Override
    public boolean isSource() {
        return false;
    }

    @Override
    public boolean isReceiver() {
        return true;
    }

    @Override
    public boolean isMethod() {
        return accessible instanceof Method;
    }

    @Override
    public boolean isField() {
        return accessible instanceof Field;
    }

    @Override
    public Field getField() {
        return (Field) accessible;
    }

    @Override
    public Method getMethod() {
        return (Method) accessible;
    }

    @Override
    public boolean isAnnotated(Class<? extends Annotation> annotation) {
        return annotations.containsKey(annotation);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotation) {
        return (T) annotations.get(annotation);
    }
}
