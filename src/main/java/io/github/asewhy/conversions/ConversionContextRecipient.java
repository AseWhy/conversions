package io.github.asewhy.conversions;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unchecked")
public abstract class ConversionContextRecipient<T extends ConversionResponse<?>, C> {
    /**
     * Получить контекст для единой сущности
     *
     * @param entity сущность для получения контекста
     * @return контекст
     */
    @NotNull
    public C provideContextForEntity(T entity) {
        return provideContextForEntities(List.of(entity));
    }

    /**
     * Получить контекст для группы сущностей
     *
     * @param entities группа сущностей
     * @return контекст
     */
    @NotNull
    public abstract C provideContextForEntities(Collection<T> entities);

    /**
     * Получить новый контекст независимо от типа поставляемой сущности
     *
     * @param entities поставляемая сущность (сущности)
     * @return контекст обработки этих сущностей
     */
    @NotNull
    public C mapContext(Object entities) {
        if(entities instanceof Collection<?>) {
            return provideContextForEntities((Collection<T>) entities);
        } else {
            return provideContextForEntity((T) entities);
        }
    }
}
