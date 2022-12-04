package io.github.asewhy.conversions;

import io.github.asewhy.ReflectionUtils;
import io.github.asewhy.conversions.builders.MutatorObjectBuilder;
import io.github.asewhy.conversions.support.annotations.ResponseDTO;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
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
    protected ConversionConfigurationInternal config;

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
     * @param mirror набор отражений
     * @param <T> Тип мутатора
     */
    public <T extends ConversionMutator<?>> void createMutator(T from, Map<String, Object> mirror) {
        if(from == null) {
            return;
        }

        from.registerStore(this.getConfig());

        var clazz = from.getClass();
        var store = this.config.getStore();
        var namingStrategy = this.config.getNamingStrategy();
        var metadata = store.getMutatorBound(clazz);

        var founds = metadata.getFound();

        from.touchedFields.addAll(mirror.keySet());

        for (var current: founds) {
            var jsonName = namingStrategy.convert(current.getPureName(), current.getType());

            if (mirror.containsKey(jsonName)) {
                var mirrorValue = mirror.get(jsonName);
                var found = current.getComputedResult(from);

                if(metadata.getIsMap()) {
                    continue;
                }

                if (
                    found instanceof ConversionMutator<?> &&
                    found.getClass() != clazz &&
                    mirrorValue instanceof Map<?, ?>
                ) {
                    createMutator((ConversionMutator<?>) found, (Map<String, Object>) mirrorValue);
                }

                if (found instanceof Collection<?> ) {
                    var collection = (Collection<?>) found;
                    var foundIterator = collection.iterator();

                    if (mirrorValue instanceof Collection<?>) {
                        var receivedCollection = (Collection<?>) mirrorValue;
                        var mirrorIterator = receivedCollection.iterator();

                        if (foundIterator.hasNext() && mirrorIterator.hasNext()) {
                            var currentFound = foundIterator.next();
                            var currentMirror = mirrorIterator.next();

                            if (
                                currentFound instanceof ConversionMutator<?> &&
                                currentFound.getClass() != clazz &&
                                currentMirror instanceof Map<?, ?>
                            ) {
                                createMutator((ConversionMutator<?>) currentFound, (Map<String, Object>) currentMirror);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Проверить, может ли какой-либо конвертер принять исходный тип и его дженерик
     *
     * @param type тип
     * @param generics generic тип этого типа
     * @param mapping маппинг
     * @return true если может
     */
    public boolean canResolveResponse(Class<?> type, Type generics, String mapping) {
        var store = config.getStore();
        var resolver = store.findResolver(type);

        if(resolver != null) {
            return resolver.canProcess(type, generics, this, mapping);
        } else {
            return false;
        }
    }

    /**
     * Создает ответ из конвертируемого элемента
     *
     * @param from конвертируемый объект
     * @param mapping исходный маппинг
     * @return конвертированный объект
     */
    public Object createResponseResolve(Object from, String mapping) {
        return createResponseResolve(from, mapping, null);
    }

    /**
     * Создает ответ из конвертируемого элемента
     *
     * @param from конвертируемый объект
     * @param mapping исходный маппинг
     * @param context поставляемый конвертируемый контекст
     * @return конвертированный объект
     */
    public Object createResponseResolve(Object from, String mapping, Object context) {
        if(from == null) {
            return null;
        }

        var providedContext = config.getConfig().context();
        var castedContext = context != null ? context : providedContext;
        var type = ReflectionUtils.skipAnonClasses(from.getClass());
        var store = config.getStore();
        var resolver = store.findResolver(type);

        if(resolver != null) {
            var example = resolver.extractExample(from, mapping, castedContext);

            if(example != null) {
                var recipient = store.findContextRecipient(example);

                if(recipient != null) {
                    castedContext = recipient.mapContext(from);
                }
            }

            return resolver.resolveResponse(from, type, this, mapping, castedContext);
        } else {
            var recipient = store.findContextRecipient(type);

            if(recipient != null) {
                castedContext = recipient.mapContext(from);
            }

            return createResponse(from, mapping, castedContext);
        }
    }

    /**
     * Создать ответ из сущности from
     *
     * @param from исходная сущность для создания ответа
     * @param <T> тип сущности ответа
     * @param <R> тип сущности, из которой будет создана сущность ответа
     */
    public <T extends ConversionResponse<R>, R> T createResponse(R from) {
        return createResponse(from, ConversionUtils.COMMON_MAPPING, true, null);
    }

    /**
     * Создать ответ из сущности from
     *
     * @param from исходная сущность для создания ответа
     * @param mapping исходный маппинг
     * @param <T> тип сущности ответа
     * @param <R> тип сущности, из которой будет создана сущность ответа
     */
    public <T extends ConversionResponse<R>, R> T createResponse(R from, String mapping) {
        return createResponse(from, mapping, true, null);
    }

    /**
     * Создать ответ из сущности from
     *
     * @param from исходная сущность для создания ответа
     * @param mapping исходный маппинг
     * @param context поставляемый конвертируемый контекст
     * @param <T> тип сущности ответа
     * @param <R> тип сущности, из которой будет создана сущность ответа
     */
    public <T extends ConversionResponse<R>, R> T createResponse(R from, String mapping, Object context) {
        return createResponse(from, mapping, true, context);
    }

    /**
     * Создать ответ из сущности from
     *
     * @param from исходная сущность для создания ответа
     * @param mapping исходный маппинг
     * @param context поставляемый конвертируемый контекст
     * @param <T> тип сущности ответа
     * @param <R> тип сущности, из которой будет создана сущность ответа
     */
    public <T extends ConversionResponse<R>, R> T createResponse(R from, String mapping, Boolean applyMappingConversion, Object context) {
        if(from == null) {
            return null;
        }

        var store = config.getStore();
        var fromClass = ReflectionUtils.skipAnonClasses(from.getClass());

        if(applyMappingConversion) {
            var resolver = (ConversionMapper<Object>) store.findMapper(fromClass);

            if(resolver != null) {
                mapping = resolver.resolveMapping(from, mapping);
                applyMappingConversion = resolver.propagation(from, mapping);
            } else {
                applyMappingConversion = false;
            }
        }

        if(!store.isPresentResponse(fromClass)) {
            throw new IllegalArgumentException(
                "It's entity is not registered on current store. " + fromClass + "\n" +
                "Check the classloader used to initialize the store, and the current classloader."
            );
        }

        var instance = (T) null;
        var result = (Object) null;
        var foundType = (Class<?>) null;
        var boundType = (Class<?>) null;
        var config = this.config.getConfig();
        var namingStrategy = this.config.getNamingStrategy();
        var metadata = store.getResponseBound(fromClass, mapping);
        var boundClass = metadata.getBoundClass();

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
        if(metadata.getIsMap() && from instanceof Map) {
            var map = (Map<?, ?>) from;

            for(var bound: metadata.getBound()) {
                result = map.containsKey(bound.getName()) ?
                    map.get(bound.getName()) :
                    map.get(namingStrategy.convert(bound.getName(), bound.getType()));

                if(result != null) {
                    foundType = result.getClass();
                    boundType = bound.getType();

                    if(ConversionResponse.class.isAssignableFrom(boundType)) {
                        result = createResponse(result, getEntityMapping(boundType), applyMappingConversion);
                    }

                    if(result instanceof Collection<?>) {
                        var collection = (Collection<?>) result;
                        var tempArray = ReflectionUtils.makeCollectionInstance(foundType);
                        var boundGeneric = bound.findXGeneric();
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
                    bound.setComputedResult(instance, result);
                }
            }
        } else {
            for(var current: metadata.getIntersect().entrySet()) {
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
                var declaredClazz = bound.getDeclaredClass();

                //
                // Если класс, которому принадлежит поле не совпадает с классом биндинга, то ошибка
                //
                if(!declaredClazz.isAssignableFrom(boundClass)) {
                    throw new RuntimeException(
                        "Cannot cast " + declaredClazz.getName() + " to " + boundClass.getName() + "[" + fromClass.getName() + " (" + mapping + ")]. " +
                        "Have you registered two converters with the same mappings?"
                    );
                }

                result = found.getComputedResult(from);

                if(result != null) {
                    foundType = found.getType();
                    boundType = bound.getType();

                    if(ConversionResponse.class.isAssignableFrom(boundType)) {
                        result = createResponse(result, getEntityMapping(boundType), applyMappingConversion, context);
                    }

                    if(result instanceof Collection<?>) {
                        var collection = (Collection<?>) result;
                        var tempArray = ReflectionUtils.makeCollectionInstance(foundType);
                        var boundGeneric = bound.findXGeneric();
                        var isBoundedArray = boundGeneric != null && ConversionResponse.class.isAssignableFrom(boundGeneric);

                        for(var item: collection) {
                            if(item == null) {
                                continue;
                            }

                            if(isBoundedArray) {
                                tempArray.add(createResponse(item, getEntityMapping(boundGeneric), applyMappingConversion, context));
                            } else {
                                tempArray.add(item);
                            }
                        }

                        result = tempArray;
                    }
                }

                bound.setComputedResult(instance, result);
            }
        }

        instance.fillInternal(from, this, context != null ? context : config.context());

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
            var annotation = AnnotationUtils.findAnnotation(clazz, ResponseDTO.class);

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
