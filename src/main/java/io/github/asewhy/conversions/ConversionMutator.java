package io.github.asewhy.conversions;

import io.github.asewhy.conversions.support.iConversionFactory;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
     * Заполняет родительскую сущность для сущности T
     *
     * @param fill объект заполнения
     * @param parent родительская сущность
     */
    protected void fillParent(T fill, Object parent) {
        var parentClazz = parent.getClass();
        var metadata = store.getMutatorBound(this.getClass());
        var parentField = metadata.getBoundField(parentClazz);

        if(parentField != null) {
            var parentSetter = metadata.getAvailableBoundSetter(parentField);

            if(parentSetter != null) {
                ConversionUtils.safeInvoke(parentSetter, fill, parent);
            } else {
                ConversionUtils.safeSet(parentField, fill, parent);
            }
        }

        fillParentInternal(fill, parent, factory.provideContext());
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
        var metadata = store.getMutatorBound(this.getClass());
        var foundFields = metadata.getIntersects();
        var context = factory.provideContext();

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
            var parentSetter = metadata.getAvailableBoundSetter(bound);

            found.setAccessible(true);
            bound.setAccessible(true);

            try {
                if(hasField(found)) {
                    var received = found.get(this);
                    var exists = bound.get(fill);

                    if(received != null) {
                        if(received instanceof ConversionMutator mutator && requireProcessNested(mutator)) {
                            if(exists == null) {
                                exists = boundType.getConstructor().newInstance();
                            }

                            mutator.fill(exists);
                            mutator.fillParent(exists, fill);

                            received = exists;
                        }

                        if(received instanceof Collection<?> foundCollection && requireProcessNested(foundCollection)) {
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
                                        mutator.fillParent(existsItem, fill);
                                    }
                                }

                                received = boundCollection;
                            }
                        }
                    }

                    if(requireProcessField(found.getName(), context, fill)) {
                        if(parentSetter != null) {
                            parentSetter.invoke(fill, received);
                        } else {
                            bound.set(fill, received);
                        }
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException | InstantiationException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            found.setAccessible(foundAccess);
            bound.setAccessible(boundAccess);
        }

        fillInternal(fill, context);

        return fill;
    }

    public boolean requireProcessField(String name, Object context, T fill) {
        return true;
    }

    public boolean requireProcessNested(Object object) {
        return true;
    }

    public final boolean hasField(@NotNull Field field) {
        return hasField(field.getName());
    }

    public final boolean hasField(String name) {
        return touchedFields.contains(factory.convertFieldName(name));
    }

    @Contract(" -> new")
    public final @NotNull Set<String> getAvailableFields() {
        return new HashSet<>(this.touchedFields);
    }

    protected void fillParentInternal(T fill, Object parent, Object context) {
        // Do not nothing
    }

    protected void fillInternal(T fill, Object context) {
        // Do not nothing
    }
}
