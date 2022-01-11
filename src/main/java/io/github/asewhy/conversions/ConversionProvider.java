package io.github.asewhy.conversions;

import io.github.asewhy.conversions.builders.MutatorObjectBuilder;
import io.github.asewhy.conversions.support.annotations.ResponseDTO;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Поставщик конверсии, предоставляет базовые методы для создания сущностей
 * ответа путем преобразования сущностей в их конвертируемые эквиваленты.
 */
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

        for (var current : founds) {
            var jsonName = factory.convertFieldName(current.getName());

            if (mirror.containsKey(jsonName)) {
                var mirrorValue = mirror.get(jsonName);
                var found = ConversionUtils.safeAccess(current, from);

                from.touchedFields.add(jsonName);

                if(metadata.getIsMap()) {
                    continue;
                }

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
            var resolver = (ConversionResolver<Object>) store.findMappingResolver(fromClass);

            if(resolver != null) {
                mapping = resolver.resolveMapping(from, mapping);
                applyMappingConversion = resolver.propagation(from, mapping);
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
        var result = (Object) null;
        var foundType = (Class<?>) null;
        var boundType = (Class<?>) null;
        var factory = this.factory.getFactory();
        var metadata = store.getResponseBound(fromClass, mapping);
        var boundClass = metadata.getBoundClass();
        var setters = metadata.getBoundSetters();

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
        if(metadata.getIsMap() && from instanceof Map map) {
            for(var bound: metadata.getBoundFields()) {
                result = map.containsKey(bound.getName()) ? map.get(bound.getName()) : map.get(factory.convertFieldName(bound.getName()));

                if(result != null) {
                    foundType = result.getClass();
                    boundType = bound.getType();

                    if(ConversionResponse.class.isAssignableFrom(boundType)) {
                        result = createResponse(result, getEntityMapping(boundType), applyMappingConversion);
                    }

                    if(result instanceof Collection<?> collection) {
                        var tempArray = ConversionUtils.makeCollectionInstance(foundType);
                        var boundGeneric = ConversionUtils.findXGeneric(bound);
                        var isBoundedArray = boundGeneric != null && ConversionResponse.class.isAssignableFrom(boundGeneric);

                        for(var item: collection) {
                            if(item == null) {
                                continue;
                            }

                            if(isBoundedArray) {
                                tempArray.add(createResponse(item, getEntityMapping(boundGeneric), applyMappingConversion));
                            } else {
                                tempArray.add(item);
                            }
                        }

                        result = tempArray;
                    }
                } else {
                    foundType = null;
                    boundType = null;
                }

                if(foundType == null || foundType.isAssignableFrom(boundType)) {
                    if(setters.containsKey(bound)) {
                        ConversionUtils.safeInvoke(setters.get(bound), instance, result);
                    } else {
                        ConversionUtils.safeSet(bound, instance, result);
                    }
                }
            }
        } else {
            for(var current: metadata.getIntersects().entrySet()) {
                //
                // Поставляющее поле
                //
                var found = current.getKey();
                //
                // Принимающее поле
                //
                var bound = current.getValue();
                //
                // Класс, которому принадлежит поле
                //
                var declaredClazz = bound.getDeclaringClass();

                //
                // Если класс, которому принадлежит поле не совпадает с классом биндинга, то ошибка
                //
                if(!declaredClazz.isAssignableFrom(boundClass)) {
                    throw new RuntimeException("Cannot cast " + declaredClazz.getName() + " to " + boundClass.getName() + "[ " + fromClass.getName() + " (" + mapping + ")]");
                }

                result = metadata.getFieldValue(from, found);

                if(result != null) {
                    foundType = found.getType();
                    boundType = bound.getType();

                    if(ConversionResponse.class.isAssignableFrom(boundType)) {
                        result = createResponse(result, getEntityMapping(boundType), applyMappingConversion);
                    }

                    if(result instanceof Collection<?> collection) {
                        var tempArray = ConversionUtils.makeCollectionInstance(foundType);
                        var boundGeneric = ConversionUtils.findXGeneric(bound);
                        var isBoundedArray = boundGeneric != null && ConversionResponse.class.isAssignableFrom(boundGeneric);

                        for(var item: collection) {
                            if(item == null) {
                                continue;
                            }

                            if(isBoundedArray) {
                                tempArray.add(createResponse(item, getEntityMapping(boundGeneric), applyMappingConversion));
                            } else {
                                tempArray.add(item);
                            }
                        }

                        result = tempArray;
                    }
                }

                if (setters.containsKey(bound)) {
                    ConversionUtils.safeInvoke(setters.get(bound), instance, result);
                } else {
                    ConversionUtils.safeSet(bound, instance, result);
                }
            }
        }

        instance.fillInternal(from, this, factory.provideContext());

        return instance;
    }


    /**
     * Получить маппинг из класса
     *
     * @param clazz класс для получения маппинга
     * @return найденный маппинг или common
     */
    public String getEntityMapping(Class<?> clazz) {
        if(ConversionResponse.class.isAssignableFrom(clazz)) {
            var annotation = clazz.getAnnotation(ResponseDTO.class);

            if(annotation != null) {
                return annotation.mapping();
            } else {
                return ConversionUtils.COMMON_MAPPING;
            }
        } else {
            return ConversionUtils.COMMON_MAPPING;
        }
    }
}
