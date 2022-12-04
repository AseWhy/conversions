package io.github.asewhy.conversions;

import io.github.asewhy.ReflectionUtils;
import io.github.asewhy.conversions.exceptions.StoreNotFoundException;
import io.github.asewhy.conversions.support.annotations.MutatorExcludes;
import io.github.asewhy.conversions.support.iBound;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Конвертируемый мутатор сервиса
 *
 * @param <T> тип мутируемого элемента, тип который будет принимать метод fill мутатора
 */
@Log4j2
@SuppressWarnings({"UnusedReturnValue", "unused", "unchecked"})
public abstract class ConversionMutator<T> {
    protected final Set<String> touchedFields = new HashSet<>();

    private ConversionConfigurationInternal config;

    /**
     * Регистрирует хранилище типов для этого мутатора
     *
     * @param internal хранилище типов
     */
    protected void registerStore(@NotNull ConversionConfigurationInternal internal) {
        this.config = internal;
    }

    /**
     * Заполняет родительскую сущность для сущности
     * <p>
     * Вызывается перед fill для вложенных сущностей
     * <p>
     * Сначала заполняет родительская сущность, т.к. если заполнять родителей после заполнения
     * основной сущности получается так что родитель одной сущности заполнен, а родитель родителя
     * текущей сущности ещё не определен
     *
     * @param fill объект заполнения
     * @param parent родительская сущность
     */
    protected void fillParent(T fill, @NotNull Object parent) {
        var parentClazz = parent.getClass();
        var config = this.config.getConfig();
        var store = this.config.getStore();
        var metadata = store.getMutatorBound(this.getClass());
        var bound = metadata.getBound();

        for(var current: bound) {
            if(!parentClazz.isAssignableFrom(current.getType())) {
                continue;
            }

            current.setComputedResult(fill, parent);
        }

        fillParentInternal(fill, parent, config.context());
    }

    /**
     * Заполнить целевую сущность значениями из этого мутатора
     *
     * @param fill сущность для заполнения
     * @return заполненная сущность
     */
    public final T fill(T fill) {
        return fill(fill, this.config.getConfig().context());
    }

    /**
     * Заполнить целевую сущность значениями из этого мутатора
     *
     * @param fill сущность для заполнения
     * @param context контекст заполнения
     * @return заполненная сущность
     */
    public final T fill(T fill, Object context) {
        var store = this.config.getStore();

        if(store == null) {
            throw new StoreNotFoundException(fill);
        }

        //
        // Получаю бинды для класса
        //
        var metadata = store.getMutatorBound(this.getClass());

        //
        // Перебираю поля, с совпадающими типами
        //
        for(var current: metadata.getIntersect().entrySet()) {
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
                    var received = found.getComputedResult(this);

                    if(received != null) {
                        var exists = bound.getComputedResult(fill);

                        if(received instanceof ConversionMutator && requireProcessNested(found, received)) {
                            if(exists == null) {
                                exists = ReflectionUtils.safeInstance(boundType);
                            }

                            var mutator = (ConversionMutator<Object>) received;

                            mutator.fillParent(exists, fill);
                            mutator.fill(exists, context);

                            received = exists;
                        }

                        if(received instanceof Collection<?> && requireProcessNested(found, received)) {
                            var foundCollection = (Collection<?>) received;
                            var foundSubtype = found.findXGeneric();
                            var boundSubtype = bound.findXGeneric();
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
                                    if(item instanceof ConversionMutator) {
                                        var mutator = (ConversionMutator<Object>) item;
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

                                        mutator.fillParent(existsItem, fill);
                                        mutator.fill(existsItem, context);
                                    }
                                }

                                received = boundCollection;
                            }
                        }
                    }

                    if(fill instanceof Map) {
                        ((Map<Object, Object>) fill).put(found.getPureName(), received);
                    } else {
                        bound.setComputedResult(fill, received);
                    }
                }
            }
        }

        fillInternal(fill, context);

        return fill;
    }

    public boolean requireProcessField(@NotNull iBound bound, Object context, T fill) {
        return !bound.isAnnotated(MutatorExcludes.class);
    }

    public boolean requireProcessNested(iBound bound, Object found) {
        return true;
    }

    public final boolean hasField(@NotNull iBound field) {
        return hasField(field.getPureName());
    }

    public final @NotNull String convertFieldName(String name) {
        var store = this.config.getStore();

        if(store == null) {
            throw new StoreNotFoundException(name);
        }

        var metadata = store.getMutatorBound(this.getClass());
        var field = metadata.getBoundField(name);

        if(field != null) {
            return this.config.getNamingStrategy().convert(name, field.getType());
        } else {
            return this.config.getNamingStrategy().convert(name, null);
        }
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
