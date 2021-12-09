package io.github.asewhy.conversions;

import io.github.asewhy.conversions.support.iConversionFactory;
import lombok.extern.log4j.Log4j2;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@SuppressWarnings({"UnusedReturnValue", "unused", "unchecked"})
public abstract class ConversionMutator<T> {
    protected final Set<String> touchedFields = new HashSet<>();

    private ConversionStore store;
    private iConversionFactory factory;

    /**
     * Регистрирует хранилище типов для этого мутатора
     *
     * @param internal хранилище типов
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
            throw new RuntimeException("Store not be provided on this ConversionMutator, use @ConvertMutator annotation for fill it automatically. " + fill.getClass());
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

            var foundType = found.getType();
            var boundType = bound.getType();
            var foundAccess = found.canAccess(this);
            var boundAccess = bound.canAccess(fill);

            found.setAccessible(true);
            bound.setAccessible(true);

            try {
                if(hasField(found)) {
                    var received = found.get(this);
                    var exists = bound.get(fill);

                    if(received != null) {
                        if(received instanceof ConversionMutator mutator) {
                            if(exists != null) {
                                received = mutator.fill(exists);
                            } else {
                                received = mutator.fill(boundType.getConstructor().newInstance());
                            }
                        }

                        if(received instanceof Collection<?> foundCollection) {
                            var foundSubtype = ConversionUtils.findXGeneric(found);
                            var boundSubtype = ConversionUtils.findXGeneric(bound);
                            var foundIdField = ConversionUtils.findTypeId(foundSubtype);
                            var boundIdField = ConversionUtils.findTypeId(boundSubtype);

                            if(boundIdField != null && foundIdField != null && boundSubtype != null) {
                                if(exists == null) {
                                    //
                                    // Создадим инстанс нужной коллекции
                                    //
                                    exists = boundType.getConstructor().newInstance();
                                }

                                var boundCollection = (Collection<Object>) exists;
                                var existsMap = boundCollection.stream().collect(Collectors.toMap(e -> ConversionUtils.safeAccess(boundIdField, e), e -> e));
                                var foundMap = foundCollection.stream().collect(Collectors.toMap(e -> ConversionUtils.safeAccess(foundIdField, e), e -> e));

                                boundCollection.removeIf(e -> !foundMap.containsKey(ConversionUtils.safeAccess(boundIdField, e)));

                                for(var item: foundCollection) {
                                    if(item instanceof ConversionMutator mutator) {
                                        var mutatorId = ConversionUtils.safeAccess(foundIdField, mutator);
                                        var existsItem = existsMap.get(mutatorId);

                                        if (mutatorId == null || existsItem == null) {
                                            //
                                            // Создадим инстанс нужного члена коллекции
                                            //
                                            boundCollection.add(existsItem = boundSubtype.getConstructor().newInstance());
                                        }

                                        mutator.fill(existsItem);
                                    }
                                }

                                received = boundCollection;
                            }
                        }
                    }

                    bound.set(fill, received);
                }
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
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
