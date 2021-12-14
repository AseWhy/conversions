package io.github.asewhy.conversions;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Getter
@Service
@SuppressWarnings({"unchecked", "unused"})
public class ConversionProvider {
    @Autowired
    protected ConversionFactoryInternal factory;

    /**
     * Получить строитель мутатора
     *
     * @return мутатор, для корневой сущности
     */
    public MutatorObjectBuilder<?> createMutatorBuilder() {
        return new MutatorObjectBuilder<>(this, null);
    }

    /**
     * Заполняет мутатор данными из
     *
     * @param from Мутатор для заполнения (перед, тем как мутатор будет работать его нужно заполнить.)
     * @param <T> Тип мутатора
     */
    public <T extends ConversionMutator<?>> void createMutator(T from, Map<String, Object> mirror) {
        if(from == null) {
            return;
        }

        from.registerStore(this.getFactory());

        var clazz = from.getClass();
        var factory = this.factory.getFactory();
        var store = this.factory.getStore();
        var metadata = store.getMutatorBound(clazz);
        var founds = metadata.getFoundFields();

        for(var current: founds) {
            var jsonName = factory.convertFieldName(current.getName());

            if (mirror.containsKey(jsonName)) {
                from.touchedFields.add(jsonName);

                var mirrorValue = mirror.get(jsonName);
                var found = ConversionUtils.safeAccess(current, from);

                if (
                    found instanceof ConversionMutator<?> foundMutator &&
                    foundMutator.getClass() != clazz &&
                    mirrorValue instanceof Map<?, ?> map
                ) {
                    createMutator(foundMutator, (Map<String, Object>) map);
                }

                if (found instanceof Collection<?> collection) {
                    var foundIterator = collection.iterator();

                    if (mirrorValue instanceof Collection<?> receivedCollection) {
                        var mirrorIterator = receivedCollection.iterator();

                        if (foundIterator.hasNext() && mirrorIterator.hasNext()) {
                            var currentFound = foundIterator.next();
                            var currentMirror = mirrorIterator.next();

                            if (
                                currentFound instanceof ConversionMutator<?> foundMutator &&
                                foundMutator.getClass() != clazz &&
                                currentMirror instanceof Map<?, ?> map
                            ) {
                                createMutator(foundMutator, (Map<String, Object>) map);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Создает ответ из коллекции элементов
     *
     * @param from коллекция для создания ответа
     * @param <L> тип коллекции
     * @param <T> тип ответа
     * @param <R> типа, объекта конверсии (исходного объекта)
     * @return конвертированный объект
     */
    public <L extends Collection<R>, T extends ConversionResponse<R>, R> Collection<T> createResponse(L from, String mapping) {
        var iterator = from.iterator();
        var result = new ArrayList<T>();

        while(iterator.hasNext()) {
            result.add(createResponse(iterator.next(), mapping));
        }

        return result;
    }

    /**
     * Создать ответ из сущности from
     *
     * @param from исходная сущность для создания ответа
     * @param <T> тип сущности ответа
     * @param <R> тип сущности, из которой будет создана сущность ответа
     */
    public <T extends ConversionResponse<R>, R> T createResponse(R from) {
        return createResponse(from, ConversionUtils.COMMON_MAPPING, true);
    }

    /**
     * Создать ответ из сущности from
     *
     * @param from исходная сущность для создания ответа
     * @param <T> тип сущности ответа
     * @param <R> тип сущности, из которой будет создана сущность ответа
     */
    public <T extends ConversionResponse<R>, R> T createResponse(R from, String mapping) {
        return createResponse(from, mapping, true);
    }

    /**
     * Создать ответ из сущности from
     *
     * @param from исходная сущность для создания ответа
     * @param <T> тип сущности ответа
     * @param <R> тип сущности, из которой будет создана сущность ответа
     */
    public <T extends ConversionResponse<R>, R> T createResponse(R from, String mapping, Boolean applyMappingConversion) {
        if(from == null) {
            return null;
        }

        var store = factory.getStore();
        var fromClass = ConversionUtils.skipAnonClasses(from.getClass());

        if(applyMappingConversion) {
            var resolver = store.findMappingResolver(fromClass);

            if(resolver != null) {
                mapping = resolver.resolveMapping(mapping);
                applyMappingConversion = resolver.propagation(mapping);
            } else {
                applyMappingConversion = false;
            }
        }

        if(!store.isPresentResponse(fromClass)) {
            //
            // Класс не найден в сторе для преобразования
            //
            throw new IllegalArgumentException("It's entity is not registered on current store. " + fromClass);
        }

        var instance = (T) null;
        var metadata = store.getResponseBound(fromClass, mapping);
        var boundClass = metadata.getBoundClass();
        var foundFields = metadata.getIntersects();

        try {
            instance = (T) boundClass.getConstructor().newInstance();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Cannot find default constructor on " + boundClass.getName(), e);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }

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


            var foundAccess = found.canAccess(from);
            var boundAccess = bound.canAccess(instance);
            var foundType = found.getType();
            var boundType = bound.getType();

            found.setAccessible(true);
            bound.setAccessible(true);

            try {
                var result = found.get(from);

                if(result != null) {
                    if(ConversionResponse.class.isAssignableFrom(boundType)) {
                        result = createResponse(result, mapping, applyMappingConversion);
                    }

                    if(result instanceof Collection<?> collection) {
                        var tempArray = new ArrayList<>();

                        for(var item: collection) {
                            tempArray.add(createResponse(item, mapping, applyMappingConversion));
                        }

                        result = tempArray;
                    }
                }

                bound.set(instance, result);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            found.setAccessible(foundAccess);
            bound.setAccessible(boundAccess);
        }

        instance.fillInternal(from, this, factory.getFactory().provideContext());

        return instance;
    }
}
