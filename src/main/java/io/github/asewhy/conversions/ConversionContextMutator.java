package io.github.asewhy.conversions;

import io.github.asewhy.conversions.support.Bound;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;

/**
 * Конвертируемый мутатор сервиса
 *
 * @param <T> тип мутируемого элемента, тип который будет принимать метод fill мутатора
 * @param <C> тип контекста принимаемого мутатором
 */
@Log4j2
@SuppressWarnings("unchecked")
public abstract class ConversionContextMutator<T, C> extends ConversionMutator<T> {
    protected void fillPureParentInternal(T fill, Object parent, C castedContext) {
        // Do something here...
    }

    protected void fillPureInternal(T fill, C castedContext) {
        // Do something here...
    }

    protected boolean requirePureProcessField(@NotNull Bound field, C context, T fill) {
        return super.requireProcessField(field, context, fill);
    }

    @Override
    public boolean requireProcessField(@NotNull Bound field, Object context, T fill) {
        return requirePureProcessField(field, (C) context, fill);
    }

    @Override
    protected final void fillInternal(T fill, Object context) {
        this.fillPureInternal(fill, (C) context);
    }

    @Override
    protected final void fillParentInternal(T fill, Object parent, Object context) {
        this.fillPureParentInternal(fill, parent, (C) context);
    }
}
