package io.github.asewhy.conversions;

import io.github.asewhy.ReflectionUtils;
import io.github.asewhy.conversions.exceptions.StoreNotFoundException;
import io.github.asewhy.conversions.support.annotations.MutatorExcludes;
import io.github.asewhy.conversions.support.iConversionFactory;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
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
    protected void registerStore(@NotNull ConversionFactoryInternal internal) {
        this.factory = internal.getFactory();
        this.store = internal.getStore();
    }

    /**
     * Заполняет родительскую сущность для сущности T
     *
     * @param fill объект заполнения
     * @param parent родительская сущность
     */
    protected void fillParent(T fill, @NotNull Object parent) {
        var parentClazz = parent.getClass();
        var metadata = store.getMutatorBound(this.getClass());
        var parentField = metadata.getBoundField(parentClazz);
        var boundSetters = metadata.getBoundSetters();

        if(parentField != null) {
            var parentSetter = boundSetters.get(parentField);

            if(parentSetter != null) {
                ReflectionUtils.safeInvoke(parentSetter, fill, parent);
            } else {
                ReflectionUtils.safeSet(parentField, fill, parent);
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
            throw new StoreNotFoundException(fill);
        }

        //
        // Получаю бинды для класса
        //
        var metadata = store.getMutatorBound(this.getClass());
        var foundFields = metadata.getIntersects();
        var boundSetters = metadata.getBoundSetters();
        var foundGetters = metadata.getFoundGetters();
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

            //
            // Если есть поле
            //
            if(hasField(found)) {
                if(requireProcessField(found, context, fill)) {
                    var received = foundGetters.containsKey(found) ? ReflectionUtils.safeInvoke(foundGetters.get(found), this) : ReflectionUtils.safeAccess(found, this);

                    if(received != null) {
                        var exists = ReflectionUtils.safeAccess(bound, fill);

                        if(received instanceof ConversionMutator mutator && requireProcessNested(found, mutator)) {
                            if(exists == null) {
                                exists = ReflectionUtils.safeInstance(boundType);
                            }

                            mutator.fill(exists);
                            mutator.fillParent(exists, fill);

                            received = exists;
                        }

                        if(received instanceof Collection<?> foundCollection && requireProcessNested(found, foundCollection)) {
                            var foundSubtype = ReflectionUtils.findXGeneric(found);
                            var boundSubtype = ReflectionUtils.findXGeneric(bound);
                            var foundIdField = ReflectionUtils.findTypeId(foundSubtype);
                            var boundIdField = ReflectionUtils.findTypeId(boundSubtype);

                            if(boundIdField != null && foundIdField != null && boundSubtype != null) {
                                if(exists == null) {
                                    //
                                    // Создадим экземпляр нужной коллекции
                                    //
                                    exists = ReflectionUtils.makeCollectionInstance(boundType);
                                }

                                var boundCollection = (Collection<Object>) exists;
                                var existsMap = boundCollection.stream().collect(Collectors.toMap(e -> ReflectionUtils.safeAccess(boundIdField, e), e -> e));
                                var foundMap = foundCollection.stream().collect(Collectors.toMap(e -> ReflectionUtils.safeAccess(foundIdField, e), e -> e));

                                boundCollection.removeIf(e -> !foundMap.containsKey(ReflectionUtils.safeAccess(boundIdField, e)));

                                for(var item: foundCollection) {
                                    if(item instanceof ConversionMutator mutator) {
                                        var mutatorId = ReflectionUtils.safeAccess(foundIdField, mutator);
                                        var existsItem = existsMap.get(mutatorId);

                                        if (mutatorId == null || existsItem == null) {
                                            //
                                            // Создадим экземпляр нужного члена коллекции
                                            //
                                            if(Map.class.isAssignableFrom(boundSubtype)) {
                                                boundCollection.add(existsItem = ReflectionUtils.makeMapInstance(boundSubtype));
                                            } else if(Collection.class.isAssignableFrom(boundSubtype)) {
                                                boundCollection.add(existsItem = ReflectionUtils.makeCollectionInstance(boundSubtype));
                                            } else {
                                                boundCollection.add(existsItem = ReflectionUtils.safeInstance(boundSubtype));
                                            }
                                        }

                                        mutator.fill(existsItem);
                                        mutator.fillParent(existsItem, fill);
                                    }
                                }

                                received = boundCollection;
                            }
                        }
                    }

                    if(fill instanceof Map map) {
                        map.put(found.getName(), received);
                    } else if(boundSetters.containsKey(bound)) {
                        ReflectionUtils.safeInvoke(boundSetters.get(bound), fill, received);
                    } else {
                        ReflectionUtils.safeSet(bound, fill, received);
                    }
                }
            }
        }

        fillInternal(fill, context);

        return fill;
    }

    public boolean requireProcessField(@NotNull Field field, Object context, T fill) {
        return field.getAnnotation(MutatorExcludes.class) == null;
    }

    public boolean requireProcessNested(Field field, Object found) {
        return true;
    }

    public final boolean hasField(@NotNull Field field) {
        return hasField(field.getName());
    }

    public final String convertFieldName(String name) {
        return factory.convertFieldName(name);
    }

    public final boolean hasField(String name) {
        return touchedFields.contains(convertFieldName(name));
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
