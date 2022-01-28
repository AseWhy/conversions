package io.github.asewhy.conversions;

import io.github.asewhy.ReflectionUtils;
import io.github.asewhy.conversions.support.CaseUtil;
import io.github.asewhy.conversions.support.ClassMetadata;
import io.github.asewhy.conversions.support.annotations.*;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Log4j2
@Getter
@SuppressWarnings({"unused", "unchecked"})
public class ConversionStore {
    private final Map<Class<?>, ClassMetadata> mutatorsMap = new HashMap<>();
    private final Map<Class<?>, Map<String, ClassMetadata>> responseMap = new HashMap<>();
    private final Map<Class<?>, ConversionMapper<?>> mappersMap = new HashMap<>();
    private final Map<Class<?>, ConversionResolver<?>> containerConverterMap = new HashMap<>();

    /**
     * Создать экземпляр стора
     */
    public ConversionStore() {

    }

    /**
     * Создать экземпляр стора и загрузить сервисные компоненты из контекста приложения
     *
     * @param context контекст приложения
     */
    public ConversionStore(ApplicationContext context) {
        loadContext(context);
    }

    /**
     * Загрузить сервисные компоненты из контекста приложения
     *
     * @param context контекст приложения
     */
    public void loadContext(@NotNull ApplicationContext context) {
        var mappers = context.getBeansWithAnnotation(DataMapper.class).values();
        var resolvers = context.getBeansWithAnnotation(DataResolver.class).values();

        for(var current: mappers) {
            var type = current.getClass();
            var generic = ReflectionUtils.findXGeneric(type);

            if(current instanceof ConversionMapper<?> mapper) {
                this.mappersMap.put(generic, mapper);
            }

            log.info("Initialize conversion mapper for " + generic);
        }

        for(var current: resolvers) {
            var type = current.getClass();
            var generic = ReflectionUtils.findXGeneric(type);

            if(current instanceof ConversionResolver<?> resolver) {
                this.containerConverterMap.put(generic, resolver);
            }

            log.info("Initialize conversion resolver for " + generic);
        }
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

    /**
     * Проверяет, иметься ли такой тип ответа в наличии в текущем сторе
     *
     * @param clazz класс мутатора
     * @return true если имеется
     */
    public boolean isPresentResponse(Class<?> clazz) {
        return clazz != null && ReflectionUtils.findOnClassMap(responseMap, clazz) != null;
    }

    /**
     * Проверяет, имеется ли такой мутатор в наличии в текущем сторе
     *
     * @param clazz класс мутатора
     * @return true если имеется
     */
    public boolean isPresentMutator(Class<?> clazz) {
        return clazz != null && ReflectionUtils.findOnClassMap(mutatorsMap, clazz) != null;
    }

    /**
     * Добавляет все компоненты автоматически из выбранного пакета (Использует текущий загрузчик классов)
     *
     * @param packageName название пакета
     */
    public void from(String packageName) {
        from(packageName, getClass().getClassLoader());
    }

    /**
     * Добавляет все компоненты автоматически из выбранного пакета
     *
     * @param packageName название пакета
     * @param loader загрузчик классов, который следует использовать для загрузки аннотированных классов
     */
    public void from(String packageName, ClassLoader loader) {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(MutatorDTO.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(ResponseDTO.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(DataMapper.class));

        for(var current: scanner.findCandidateComponents(packageName)) {
            try {
                var clazz = Class.forName(current.getBeanClassName(), false, loader);
                var generic = ReflectionUtils.findXGeneric(clazz);

                if(
                    generic != null && (
                        ConversionMutator.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(MutatorDTO.class) ||
                        ConversionResponse.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(ResponseDTO.class)
                    )
                ) {
                    register(clazz, generic);
                }
            } catch (ClassNotFoundException e) {
                log.error("Cannot create conversion for " + current.getBeanClassName());
            }
        }
    }

    /**
     * Зарегистрировать мутатор или тип ответа в сторе
     *
     * @param reg регистрируемый тип
     * @param target что этот мутатор будет менять
     */
    public void register(Class<?> reg, Class<?> target) {
        if(ConversionResponse.class.isAssignableFrom(reg)) {
            this.registerResponse(target, reg);
        } else if(ConversionMutator.class.isAssignableFrom(reg)) {
            this.registerMutator(reg, target);
        } else {
            throw new IllegalArgumentException("Received class is not ConversionResponse or ConversionMutator");
        }
    }

    /**
     * Зарегистрировать мутатор или тип ответа в сторе
     *
     * @param target целевой тип ответа, с которого будет происходить маппинг <T> у {@link ConversionResponse}
     * @param response регистрируемый тип ответа, на который будет происходить маппинг, сам {@link ConversionResponse}
     */
    private void registerResponse(Class<?> target, Class<?> response) {
        var mapping = getEntityMapping(response);
        var metadataMap = getResponseBound(target);
        var metadata = metadataMap.computeIfAbsent(mapping, (e) -> new ClassMetadata());
        var fieldsFound = metadata.getIntersects();
        var fieldsTotal = metadata.getFoundFields();
        var fieldsSetters = metadata.getBoundSetters();
        var fieldsGetters = metadata.getFoundGetters();
        var fieldsBound = metadata.getBoundFieldsMap();
        var foundFields = ReflectionUtils.scanFieldsToMap(target);
        var boundFields = ReflectionUtils.scanFieldsToMap(response);
        var foundMethods = ReflectionUtils.scanMethodsToMap(target);
        var boundMethods = ReflectionUtils.scanMethodsToMap(response);

        metadata.setBoundClass(response);
        metadata.setIsMap(Map.class.isAssignableFrom(target));

        log.info("Register conversion response from " + target.getSimpleName() + " to " + response.getSimpleName());

        for(var current: foundFields.entrySet()) {
            var found = current.getValue();
            var bound = boundFields.get(current.getKey());

            if(bound != null) {
                var foundType = found.getType();
                var boundType = bound.getType();

                if(
                    boundType == foundType && (
                        !Collection.class.isAssignableFrom(foundType) || isConventionalCollection(found, bound)
                    ) ||
                    isConverterOwn(found, boundType) &&
                    ConversionResponse.class.isAssignableFrom(boundType)
                ) {
                    fieldsFound.put(found, bound);
                }

                fieldsTotal.add(found);
            } else if(metadata.getIsMap() || found.getAnnotation(IgnoreMatch.class) != null) {
                fieldsTotal.add(found);
            }
        }

        for(var current: boundFields.entrySet()) {
            var field = current.getValue();
            var setter = boundMethods.get("set" + CaseUtil.toPascalCase(current.getKey()));

            if(setter != null) {
                var parameters = setter.getParameterTypes();

                if(parameters.length > 0 && parameters[0] == field.getType()) {
                    fieldsSetters.put(field, setter);
                }
            }

            metadata.addBoundField(field);
        }

        for(var current: foundFields.entrySet()) {
            var field = current.getValue();
            var getter = foundMethods.get("get" + CaseUtil.toPascalCase(current.getKey()));

            if(getter != null && getter.getReturnType().isAssignableFrom(field.getType())) {
                fieldsGetters.put(field, getter);
            }
        }

        responseMap.put(target, metadataMap);
    }

    /**
     * Зарегистрировать мутатор или тип ответа в сторе
     *
     * @param mutator мутатор, с которого будет происходить маппинг {@link ConversionMutator}
     * @param target подтип мутатора на который будет происходить маппинг <T> у {@link ConversionMutator}
     */
    private void registerMutator(Class<?> mutator, Class<?> target) {
        var metadata = getMutatorBound(mutator);
        var fieldsFound = metadata.getIntersects();
        var fieldsTotal = metadata.getFoundFields();
        var fieldsSetters = metadata.getBoundSetters();
        var fieldsGetters = metadata.getFoundGetters();
        var fieldsBound = metadata.getBoundFieldsMap();
        var foundFields = ReflectionUtils.scanFieldsToMap(mutator);
        var boundFields = ReflectionUtils.scanFieldsToMap(target);
        var foundMethods = ReflectionUtils.scanMethodsToMap(mutator);
        var boundMethods = ReflectionUtils.scanMethodsToMap(target);

        metadata.setBoundClass(target);
        metadata.setIsMap(Map.class.isAssignableFrom(target));

        log.info("Register conversion mutator from " + mutator.getSimpleName() + " to " + target.getSimpleName());

        for(var current: foundFields.entrySet()) {
            var found = current.getValue();
            var bound = boundFields.get(current.getKey());

            if(bound != null) {
                var foundType = found.getType();
                var boundType = bound.getType();

                if(
                    boundType == foundType && (
                        !Collection.class.isAssignableFrom(foundType) || isConventionalCollection(bound, found)
                    ) ||
                    isConverterOwn(bound, foundType) &&
                    ConversionMutator.class.isAssignableFrom(foundType)
                ) {
                    fieldsFound.put(found, bound);
                }

                fieldsTotal.add(found);
            } else if(metadata.getIsMap() || found.getAnnotation(IgnoreMatch.class) != null) {
                fieldsTotal.add(found);
            }
        }

        for(var current: boundFields.entrySet()) {
            var field = current.getValue();
            var setter = boundMethods.get("set" + CaseUtil.toPascalCase(current.getKey()));

            if(setter != null) {
                fieldsSetters.put(field, setter);
            }

            metadata.addBoundField(field);
        }

        for(var current: foundFields.entrySet()) {
            var field = current.getValue();
            var getter = foundMethods.get("get" + CaseUtil.toPascalCase(current.getKey()));

            if(getter != null && getter.getReturnType().isAssignableFrom(field.getType())) {
                fieldsGetters.put(field, getter);
            }
        }

        mutatorsMap.put(mutator, metadata);
    }

    /**
     * Сравнивает подтипы двух коллекций
     *
     * @param compare найденное значение
     * @param source значение для преобразования
     * @return true если коллекции можно конвертировать
     */
    public boolean isConventionalCollection(Field compare, Field source) {
        var requireBeConverter = ReflectionUtils.findXGeneric(source);

        if(requireBeConverter == null) {
            return false;
        } else {
            var compareGeneric = ReflectionUtils.findXGeneric(compare);

            if(compareGeneric != null && requireBeConverter == compareGeneric) {
                return true;
            }

            var sourceGeneric = ReflectionUtils.findXGeneric(requireBeConverter);

            if(compareGeneric != null && sourceGeneric != null) {
                return sourceGeneric.isAssignableFrom(compareGeneric);
            } else {
                return false;
            }
        }
    }

    /**
     * Вернет true если это поле конвертера, для поля целевого класса
     *
     * @param found поле с конвертером
     * @param boundClazz тип целевого поля
     * @return true если истина
     */
    public boolean isConverterOwn(Field found, Class<?> boundClazz) {
        var generic = ReflectionUtils.findXGeneric(boundClazz);

        if(generic != null) {
            return generic.isAssignableFrom(found.getType());
        } else {
            return false;
        }
    }

    /**
     * Найти конвертер для конвертации по целевому типу
     *
     * @param forClass класс для которого нужен конвертер
     * @param <T> целевой тип
     * @return конвертер
     */
    public <T> ConversionResolver<T> findResolver(Class<? extends T> forClass) {
        return (ConversionResolver<T>) ReflectionUtils.findOnClassMap(containerConverterMap, forClass);
    }

    /**
     * Найти подходящий текущему классу обработчик маппингов
     *
     * @param forClass Класс для поиска обработчика
     * @param <T> тип обработчика
     * @return найденный обработчик или null
     */
    public <T> ConversionMapper<T> findMapper(Class<? extends T> forClass) {
        return (ConversionMapper<T>) ReflectionUtils.findOnClassMap(mappersMap, forClass);
    }

    /**
     * Получить бинды для класса конвертера
     *
     * @param forClass класс для получения биндов
     * @return найденные бинды, или пустая карта
     */
    public @NotNull Map<String, ClassMetadata> getResponseBound(Class<?> forClass) {
        var result = responseMap.get(forClass);

        if(result == null) {
            result = new HashMap<>();
        }

        return result;
    }

    /**
     * Получить бинды для класса конвертера
     *
     * @param forClass класс для получения биндов
     * @param mapping маппинг для поиска
     * @return найденные бинды, или пустая карта
     */
    public @NotNull ClassMetadata getResponseBound(Class<?> forClass, String mapping) {
        var source = ReflectionUtils.findOnClassMap(responseMap, forClass);

        if(source == null) {
            source = new HashMap<>();
        }

        if(!source.containsKey(mapping)) {
            mapping = ConversionUtils.COMMON_MAPPING;
        }

        return Objects.requireNonNullElseGet(source.get(mapping), ClassMetadata::new);
    }

    /**
     * Получить бинды для класса конвертера
     *
     * @param forClass класс для получения биндов
     * @return найденные бинды, или пустая карта
     */
    public @NotNull ClassMetadata getMutatorBound(Class<?> forClass) {
        var result = this.mutatorsMap.get(forClass);

        if(result == null) {
            result = new ClassMetadata();
        }

        return result;
    }
}
