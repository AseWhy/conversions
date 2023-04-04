package io.github.asewhy.conversions.support;

import io.github.asewhy.ReflectionUtils;
import io.github.asewhy.conversions.ConversionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

/**
 * Основной оперируемый интерфейс обертки для полей конвертации, используется для упрощения операций с заполнением полей
 */
public interface Bound {
    Field getField();
    Method getGetter();
    Method getSetter();

    Collection<Annotation> getAnnotations();

    /**
     * Получить название поля будь то поле или класс
     *
     * @return название поля
     */
    default String getName() {
        return Optional.ofNullable(getField())
            .map(Field::getName)
            .or(() -> Optional.ofNullable(getGetter()).map(e -> ConversionUtils.getPureName(e.getName())))
            .or(() -> Optional.ofNullable(getSetter()).map(e -> ConversionUtils.getPureName(e.getName())))
        .orElseThrow(() -> new RuntimeException("No source provided!"));
    }

    /**
     * Получить класс, который предоставляет результирующее значение поля или результат метода
     *
     * @return тип поля или тип руль тата выполнения метода
     */
    default Class<?> getType() {
        var field = getField();

        if(field != null) {
            return field.getType();
        }

        var getter = getGetter();

        if(getter != null) {
            return getter.getReturnType();
        }

        var setter = getSetter();

        if(setter != null) {
            return Arrays.stream(setter.getParameters())
                .map(Parameter::getType)
                .findFirst()
            .orElseThrow(() -> new RuntimeException("Invalid setter parameters!"));
        }

        throw new RuntimeException("No source provided!");
    }

    /**
     * Получить класс, в котором был объявлен это поле или метод
     *
     * @return класс в котором были объявлены поле или метод
     */
    default Class<?> getDeclaredClass() {
        var field = getField();

        if(field != null) {
            return field.getDeclaringClass();
        }

        return Optional
            .ofNullable(getGetter())
            .or(() -> Optional.ofNullable(getSetter()))
            .map(Method::getDeclaringClass)
        .orElseThrow(() -> new RuntimeException("No source provided!"));
    }

    /**
     * Получить результат вызова метода или значение поля от объекта taget
     *
     * @param target целевой объект вызова
     * @return результат вызова
     */
    default Object getComputedResult(Object target) {
        var getter = getGetter();
        var field = getField();

        if(getter != null) {
            return ReflectionUtils.safeInvoke(getter, target);
        } else {
            return ReflectionUtils.safeAccess(field, target);
        }
    }

    /**
     * Установить значение поля, или вызвать сеттер для этого поля
     *
     * @param target целевой объект вызова
     * @param value значение, которое устанавливаем
     */
    default void setComputedResult(Object target, Object value) {
        var setter = getSetter();
        var field = getField();

        if(setter != null) {
            ReflectionUtils.safeInvoke(setter, target, value);
        } else {
            ReflectionUtils.safeSet(field, target, value);
        }
    }

    /**
     * Проверить доступна ли аннотация над текущим объектом доступа
     *
     * @param annotation аннотация
     * @return true если аннотация актуальна
     */
    default boolean isAnnotated(Class<? extends Annotation> annotation) {
        return getAnnotation(annotation) != null;
    }

    /**
     * Получить аннотацию annotation если она присутствует в поле или методе
     *
     * @param annotation клас аннотации
     * @return аннотация если найдена
     */
    default <T extends Annotation> T getAnnotation(Class<T> annotation) {
        var field = getField();

        if(field != null) {
            return field.getAnnotation(annotation);
        }

        return Optional
            .ofNullable(getGetter())
            .or(() -> Optional.ofNullable(getSetter()))
            .map(e -> e.getAnnotation(annotation))
        .orElseThrow(() -> new RuntimeException("No source provided!"));
    }

    /**
     * Получить x дженерик текущего объекта доступа (поля или метода)
     *
     * @return Класс x дженерика
     */
    default Class<?> findXGeneric(int x) {
        var field = getField();

        if(field != null) {
            return ReflectionUtils.findXGeneric(field, x);
        }

        return Optional
            .ofNullable(getGetter())
            .or(() -> Optional.ofNullable(getSetter()))
            .map(method -> {
                if(method.getName().startsWith("set")) {
                    return ReflectionUtils.findXGeneric(
                        Arrays.stream(method.getParameters())
                            .map(Parameter::getParameterizedType)
                            .findFirst()
                        .orElseThrow(),
                        x
                    );
                } else {
                    return ReflectionUtils.findXGeneric(method, x);
                }
            })
        .orElseThrow(() -> new RuntimeException("No source provided!"));
    }

    /**
     * Получить первый дженерик текущего объекта доступа (поля или метода)
     *
     * @return Класс первого дженерика
     */
    default Class<?> findXGeneric() {
        return findXGeneric(0);
    }

    /**
     * В биндинге присутствует то что может его идентифицировать (поле или геттер или сеттер)
     *
     */
    default boolean isPresent() {
        return getField() != null || getGetter() != null || getSetter() != null;
    }

    /**
     * В биндинге присутствует возможность и записи и чтения
     *
     * @return true если есть поле можно записывать и читать
     */
    default boolean isFullfilled() {
        return getField() != null || getGetter() != null && getSetter() != null;
    }
}
