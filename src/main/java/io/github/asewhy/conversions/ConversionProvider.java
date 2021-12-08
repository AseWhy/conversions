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
        var metadata = store.getBound(clazz);

        for(var current: metadata.getFound()) {
            var jsonName = factory.convertFieldName(current.getName());

            if (mirror.containsKey(jsonName)) {
                from.touchedFields.add(jsonName);

                try {
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
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
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
    public <L extends Collection<R>, T extends ConversionResponse<R>, R> Collection<T> createResponse(L from) {
        var iterator = from.iterator();
        var result = new ArrayList<T>();

        while(iterator.hasNext()) {
            result.add(createResponse(iterator.next()));
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
        var store = factory.getStore();

        if(from == null) {
            return null;
        }

        var fromClass = from.getClass();

        if(!store.isPresentResponse(fromClass)) {
            throw new IllegalArgumentException("It's entity is not registered on current store.");
        }

        var instance = (T) null;
        var metadata = store.getBound(fromClass);
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

            found.setAccessible(true);
            bound.setAccessible(true);

            try {
                bound.set(instance, found.get(from));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            found.setAccessible(foundAccess);
            bound.setAccessible(boundAccess);
        }

        return instance;
    }
}
