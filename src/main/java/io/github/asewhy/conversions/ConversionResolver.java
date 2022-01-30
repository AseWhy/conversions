package io.github.asewhy.conversions;

import java.lang.reflect.Type;

/**
 * Для списков, карт, и других контейнеров данных нужно конвертировать данные
 *
 * @param <T> тип сущности T ответ на которую регистрируем.
 */
@SuppressWarnings({"unused", "unchecked"})
public abstract class ConversionResolver<T> {
    /**
     * Произвести преобразование типа T
     *
     * @param from объект, из которого происходит преобразование
     */
    protected abstract T resolveInternalResponse(T from, Class<? extends T> type, ConversionProvider provider, String mapping);

    /**
     * Проверить, сможет ли этот конвертер обработать поступающее значение
     *
     * @param from тип, значения которое будет обработано
     * @param generics generic типы меода, который возвращает конвертируемый объект
     * @param mapping маппинг
     * @return true если может
     */
    protected abstract boolean canProcess(Class<?> from, Type generics, ConversionProvider provider, String mapping);

    /**
     * Произвести преобразование типа T
     *
     * @param from объект, из которого происходит преобразование
     */
    public final <F> F resolveResponse(F from, Class<?> type, ConversionProvider provider, String mapping) {
        return (F) resolveInternalResponse((T) from, (Class<? extends T>) type, provider, mapping);
    }
}
