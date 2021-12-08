package io.github.asewhy.conversions;

import io.github.asewhy.conversions.support.iConversionFactory;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

@Log4j2
@SuppressWarnings({"UnusedReturnValue", "unused"})
public abstract class ConversionMutator<T> {
    protected final Set<String> touchedFields = new HashSet<>();

    private ConversionStore store;
    private iConversionFactory factory;

    /**
     * Регистрирует хранилище типов для этого мутатора
     *
     * @param store хранилище типов
     */
    protected void registerStore(ConversionFactoryInternal internal) {
        this.factory = internal.getFactory();
        this.store = internal.getStore();
    }

    /**
     * Заполнить целевую сущность значениями из этого мутатора
     *
     * @param fill сущность для заполнения
     * @return заполненная сущность
     */
    public final T fill(T fill) {
        if(store == null) {
            throw new RuntimeException("Store not be provided on this ConversionMutator, use @Mutator annotation for fill it automatically.");
        }

        //
        // Получаю бинды для класса
        //
        var metadata = store.getBound(this.getClass());
        var foundFields = metadata.getIntersects();

        //
        // Перебираю поля, с совпадающими типами
        //
        for(var current: foundFields.entrySet()) {
            //
            // Поставщик
            //
            var found = current.getKey();
            //
            // Принимающий объект
            //
            var bound = current.getValue();


            var foundAccess = found.canAccess(this);
            var boundAccess = bound.canAccess(fill);

            found.setAccessible(true);
            bound.setAccessible(true);

            try {
                var received = found.get(this);

                if(hasField(found)) {
                    bound.set(fill, received);
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            found.setAccessible(foundAccess);
            bound.setAccessible(boundAccess);
        }

        return fillInternal(fill, factory.provideContext());
    }

    public boolean hasField(Field field) {
        return hasField(field.getName());
    }

    public boolean hasField(String name) {
        return touchedFields.contains(factory.convertFieldName(name));
    }

    public Set<String> getAvailableFields() {
        return new HashSet<>(this.touchedFields);
    }

    protected T fillInternal(T fill, Object context) {
        return fill;
    }
}
