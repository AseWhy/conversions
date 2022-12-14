package io.github.asewhy.conversions.support;

import io.github.asewhy.ReflectionUtils;
import io.github.asewhy.conversions.ConversionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Основной оперируемый интерфейс обертки для полей конвертации, используется для упрощения операций с заполнением полей
 */
public interface Bound {
    boolean isSource();
    boolean isReceiver();
    boolean isMethod();
    boolean isField();

    Field getField();
    Method getMethod();
    Collection<Annotation> getAnnotations();

    /**
     * Получить класс, который предоставляет результирующее значение поля или резльтат метода
     *
     * @return тип поля или тип рультата выполнения метода
     */
    default String getName() {
        if(isField()) {
            return getField().getName();
        } else {
            return getMethod().getName();
        }
    }

    /**
     * Получить название поля будь то поле или класс
     *
     * @return название поля
     */
    default String getPureName() {
        if(isField()) {
            return getName();
        } else {
            return ConversionUtils.getPureName(getName());
        }
    }

    /**
     * Получить класс, который предоставляет результирующее значение поля или резльтат метода
     *
     * @return тип поля или тип рультата выполнения метода
     */
    default Class<?> getType() {
        if(isField()) {
            return getField().getType();
        } else {
            return getMethod().getReturnType();
        }
    }

    /**
     * Получить класс, в котором был объявлен это поле или метод
     *
     * @return класс в котором были объявлены поле или метод
     */
    default Class<?> getDeclaredClass() {
        if(isField()) {
            return getField().getDeclaringClass();
        } else {
            return getMethod().getDeclaringClass();
        }
    }

    /**
     * Получить результат вызова метода или значение поля от объекта taget
     *
     * @param target целевой объект вызова
     * @return результат вызова
     */
    default Object getComputedResult(Object target) {
        if(isField()) {
            return ReflectionUtils.safeAccess(getField(), target);
        } else {
            return ReflectionUtils.safeInvoke(getMethod(), target);
        }
    }

    /**
     * Установить значение поля, или вызвать сеттер для этого поля
     *
     * @param target целевой объект вызова
     * @param value значение, которое устанавливаем
     */
    default void setComputedResult(Object target, Object value) {
        if(isField()) {
            ReflectionUtils.safeSet(getField(), target, value);
        } else {
            ReflectionUtils.safeInvoke(getMethod(), target, value);
        }
    }

    /**
     * Проверить доступна ли аннотация над текущим объектом доступа
     *
     * @param annotation аннотация
     * @return true если аннотация актуальна
     */
    default boolean isAnnotated(Class<? extends Annotation> annotation) {
        if(isField()) {
            return getField().isAnnotationPresent(annotation);
        } else {
            return getMethod().isAnnotationPresent(annotation);
        }
    }

    /**
     * Получить аннотацию annotation если она присутствует в поле или методе
     *
     * @param annotation клас аннотации
     * @return аннотация если найдена
     */
    default <T extends Annotation> T getAnnotation(Class<T> annotation) {
        if(isField()) {
            return getField().getAnnotation(annotation);
        } else {
            return getMethod().getAnnotation(annotation);
        }
    }

    /**
     * Получить x дженерик текущего объекта доступа (поля или метода)
     *
     * @return Класс x дженерика
     */
    default Class<?> findXGeneric(int x) {
        if(isField()) {
            return ReflectionUtils.findXGeneric(getField(), x);
        } else {
            return ReflectionUtils.findXGeneric(getMethod(), x);
        }
    }

    /**
     * Получить первый дженерик текущего объекта доступа (поля или метода)
     *
     * @return Класс первого дженерика
     */
    default Class<?> findXGeneric() {
        return findXGeneric(0);
    }
}
