package io.github.asewhy.conversions;

import lombok.extern.log4j.Log4j2;

/**
 * Конвертируемый ответ сервера
 *
 * @param <T> тип ответа, из которого можно создать текущий тип ответа
 * @param <C> тип контекста, принимаемого конвертером
 */
@Log4j2
@SuppressWarnings("unchecked")
public abstract class ConversionContextResponse<T, C> extends ConversionResponse<T> {
    protected void fillPureInternal(T fill, C castedContext) {
        // Do something here...
    }

    protected void fillPureInternal(T fill, ConversionProvider provider, C castedContext) {
        // Do something here...
    }

    @Override
    protected final void fillInternal(T fill, Object context) {
        this.fillPureInternal(fill, (C) context);
    }

    @Override
    protected final void fillInternal(T fill, ConversionProvider provider, Object context) {
        this.fillPureInternal(fill, provider, (C) context);
    }
}
