package io.github.asewhy.conversions;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.asewhy.conversions.support.ConversionConfiguration;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Type;

/**
 * Для списков, карт, и других контейнеров данных нужно конвертировать данные
 *
 * @param <T> тип сущности T ответ на которую регистрируем.
 */
@SuppressWarnings({"unused", "unchecked"})
public abstract class RequestResolver<T> {
    /**
     * Произвести преобразование типа T
     */
    protected abstract T resolveInternalRequest(JsonNode node, Class<? extends  T> fromClass, Type generics, ConversionProvider provider);

    /**
     * Проверить, сможет ли этот конвертер обработать поступающее значение
     *
     * @param from тип, значения которое будет обработано
     * @param generics generic типы меода, который возвращает конвертируемый объект
     * @return true если может
     */
    protected abstract boolean canProcess(Class<?> from, Type generics, ConversionProvider provider);

    /**
     * Произвести преобразование типа T
     *
     * @param context исходный поставляемый контекст
     */
    public final <F> F resolveRequest(JsonNode node, Class<?> fromClass, Type generics, ConversionProvider provider, Object context) {
        return (F) resolveInternalRequest(node, (Class<? extends T>) fromClass, generics, provider);
    }
}
