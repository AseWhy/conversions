package io.github.asewhy.conversions;

import io.github.asewhy.conversions.support.CaseUtil;
import io.github.asewhy.conversions.support.ClassMetadata;
import io.github.asewhy.conversions.support.annotations.IgnoreMatch;
import io.github.asewhy.conversions.support.annotations.MutatorDTO;
import io.github.asewhy.conversions.support.annotations.ResponseDTO;
import io.github.asewhy.conversions.support.annotations.ResponseResolver;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
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
    private final Map<Class<?>, ConversionResolver<?>> resolversMap = new HashMap<>();

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
        return clazz != null && ConversionUtils.findOnClassMap(responseMap, clazz) != null;
    }

    /**
     * Проверяет, имеется ли такой мутатор в наличии в текущем сторе
     *
     * @param clazz класс мутатора
     * @return true если имеется
     */
    public boolean isPresentMutator(Class<?> clazz) {
        return clazz != null && ConversionUtils.findOnClassMap(mutatorsMap, clazz) != null;
    }

    /**
     * Добавляет все компоненты автоматически из выбранного пакета
     *
     * @param packageName название пакета
     */
    public void from(String packageName) {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);

        scanner.addIncludeFilter(new AnnotationTypeFilter(MutatorDTO.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(ResponseDTO.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(ResponseResolver.class));

        for(var current: scanner.findCandidateComponents(packageName)) {
            try {
                var clazz = Class.forName(current.getBeanClassName());
                var generic = ConversionUtils.findXGeneric(clazz);

                if(
                    generic != null && (
                        ConversionMutator.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(MutatorDTO.class) ||
                        ConversionResponse.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(ResponseDTO.class) ||
                        ConversionResolver.class.isAssignableFrom(clazz) && clazz.isAssignableFrom(ResponseResolver.class)
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
        } else if(ConversionResolver.class.isAssignableFrom(reg)) {
            this.registerResolver(reg, target);
        } else {
            throw new IllegalArgumentException("Received class is not ConversionResponse or ConversionMutator");
        }
    }

    /**
     * Зарегистрировать обработчик типов ответа
     *
     * @param reg регистрируемый обработчик
     * @param target цель обработчика, сущности ответа какого типа он будет обрабатывать
     */
    private void registerResolver(@NotNull Class<?> reg, Class<?> target) {
        try {
            this.resolversMap.put(target, (ConversionResolver<?>) reg.getConstructor().newInstance());
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Cannot find default constructor on " + reg.getName(), e);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
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
        var foundFields = ConversionUtils.scanFieldsToMap(target);
        var boundFields = ConversionUtils.scanFieldsToMap(response);
        var foundMethods = ConversionUtils.scanMethodsToMap(target);
        var boundMethods = ConversionUtils.scanMethodsToMap(response);

        metadata.setBoundClass(response);
        metadata.setIsMap(Map.class.isAssignableFrom(target));

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
        var foundFields = ConversionUtils.scanFieldsToMap(mutator);
        var boundFields = ConversionUtils.scanFieldsToMap(target);
        var foundMethods = ConversionUtils.scanMethodsToMap(mutator);
        var boundMethods = ConversionUtils.scanMethodsToMap(target);

        metadata.setBoundClass(target);
        metadata.setIsMap(Map.class.isAssignableFrom(target));

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
                    isConverterOwn(found, boundType) &&
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
    protected boolean isConventionalCollection(Field compare, Field source) {
        var requireBeConverter = ConversionUtils.findXGeneric(source);

        if(requireBeConverter == null) {
            return false;
        } else {
            var compareGeneric = ConversionUtils.findXGeneric(compare);

            if(compareGeneric != null && requireBeConverter == compareGeneric) {
                return true;
            }

            var sourceGeneric = ConversionUtils.findXGeneric(requireBeConverter);

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
    protected boolean isConverterOwn(Field found, Class<?> boundClazz) {
        var generic = ConversionUtils.findXGeneric(boundClazz);

        if(generic != null) {
            return generic.isAssignableFrom(found.getType());
        } else {
            return false;
        }
    }

    /**
     * Найти подходящий текущему классу обработчик маппингов
     *
     * @param forClass Класс для поиска обработчика
     * @param <T> тип обработчика
     * @return найденный обработчик или null
     */
    public <T> ConversionResolver<T> findMappingResolver(Class<? extends T> forClass) {
        return (ConversionResolver<T>) ConversionUtils.findOnClassMap(resolversMap, forClass);
    }

    /**
     * Получить бинды для класса конвертера
     *
     * @param forClass класс для получения биндов
     * @return найденные бинды, или пустая карта
     */
    protected @NotNull Map<String, ClassMetadata> getResponseBound(Class<?> forClass) {
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
    protected @NotNull ClassMetadata getResponseBound(Class<?> forClass, String mapping) {
        var source = ConversionUtils.findOnClassMap(responseMap, forClass);

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
    protected @NotNull ClassMetadata getMutatorBound(Class<?> forClass) {
        var result = this.mutatorsMap.get(forClass);

        if(result == null) {
            result = new ClassMetadata();
        }

        return result;
    }
}
