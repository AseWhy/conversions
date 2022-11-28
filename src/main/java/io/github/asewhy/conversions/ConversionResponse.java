package io.github.asewhy.conversions;

import io.github.asewhy.conversions.support.iConversionConfiguration;

/**
 * Конвертируемый ответ сервера
 *
 * @param <T> тип ответа, из которого можно создать текущий тип ответа
 */
public abstract class ConversionResponse<T> {
    /**
     * Метод для ручного заполнения текущей сущности
     *
     * @param from сущность из которой происходит заполнение
     * @param context контекст поставляемый конфигурацией {@link iConversionConfiguration#context()}
     */
    protected void fillInternal(T from, Object context) {
        // Stub
    }

    /**
     * Метод для ручного заполнения текущей сущности
     *
     * @param from сущность из которой происходит заполнение
     * @param provider текущий поставщик конверсии
     * @param context контекст поставляемый конфигурацией {@link iConversionConfiguration#context()}
     */
    protected void fillInternal(T from, ConversionProvider provider, Object context) {
        fillInternal(from, context);
    }
}
