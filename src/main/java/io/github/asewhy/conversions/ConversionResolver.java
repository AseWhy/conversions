package io.github.asewhy.conversions;

import java.lang.reflect.Method;

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
     * @param mapping маппинг
     * @return true если может
     */
    protected abstract boolean canProcess(Class<?> from, Class<?> generic, ConversionProvider provider, String mapping);

    /**
     * Получить класс, в который по итогу будет преобразован ответ (может использоваться при документировании)
     *
     * @return класс в который конвертируется ответ
     */
    public abstract Class<?> getConversionReference(Method from);

    /**
     * Произвести преобразование типа T
     *
     * @param from объект, из которого происходит преобразование
     */
    public final <F> F resolveResponse(F from, Class<?> type, ConversionProvider provider, String mapping) {
        return (F) resolveInternalResponse((T) from, (Class<? extends T>) type, provider, mapping);
    }
}
