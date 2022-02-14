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
     * Распаковать конвертируемый контейнер T и преобразовать его в тип, содержащийся в этом контейнере T
     *
     * @param from объект, из которого происходит преобразование
     */
    protected abstract Class<?> extractInternalExample(T from, String mapping, Object globalContextOrPassedContext);

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
     * Т.к. этот класс представляет собой своего рода переупаковщик, этот метод должен получить оригинальный тип содержащийся
     * в текущем наборе
     *
     * @param from набор из которого получить тип
     * @param mapping маппинг для получения типа
     * @param globalContextOrPassedContext глобальный или локальный контекст переданный извне
     * @return тип содержащийся в текущем контейнере
     */
    public final Class<?> extractExample(Object from, String mapping, Object globalContextOrPassedContext) {
        return extractInternalExample((T) from, mapping, globalContextOrPassedContext);
    }

    /**
     * Произвести преобразование типа T
     *
     * @param from объект, из которого происходит преобразование
     * @param context исходный поставляемый контекст
     */
    public final <F> F resolveResponse(F from, Class<?> type, ConversionProvider provider, String mapping, Object context) {
        return (F) resolveInternalResponse((T) from, (Class<? extends T>) type, provider, mapping);
    }
}
