package io.github.asewhy.conversions;

import io.github.asewhy.conversions.support.CaseUtil;
import io.github.asewhy.conversions.support.ClassMetadata;
import io.github.asewhy.conversions.support.annotations.MutatorDTO;
import io.github.asewhy.conversions.support.annotations.ResponseDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Log4j2
@SuppressWarnings("unused")
public class ConversionStore {
    private final Map<Class<?>, ClassMetadata> mutatorsMap = new HashMap<>();
    private final Map<Class<?>, ClassMetadata> responseMap = new HashMap<>();

    /**
     * Проверяет, иметься ли такой тип ответа в наличии в текущем сторе
     *
     * @param clazz класс мутатора
     * @return true если имеется
     */
    public boolean isPresentResponse(Class<?> clazz) {
        return responseMap.containsKey(clazz);
    }

    /**
     * Проверяет, имеется ли такой мутатор в наличии в текущем сторе
     *
     * @param clazz класс мутатора
     * @return true если имеется
     */
    public boolean isPresentMutator(Class<?> clazz) {
        return mutatorsMap.containsKey(clazz);
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

        for(var current: scanner.findCandidateComponents(packageName)) {
            try {
                var clazz = Class.forName(current.getBeanClassName());
                var generic = ConversionUtils.findXGeneric(clazz);

                if(
                    generic != null && (
                        ConversionMutator.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(MutatorDTO.class) ||
                        ConversionResponse.class.isAssignableFrom(clazz) && clazz.isAnnotationPresent(ResponseDTO.class)
                    )
                ) {
                    register(clazz, generic);
                }
            } catch (ClassNotFoundException e) {
                // Do not nothing
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
    public void registerResponse(Class<?> target, Class<?> response) {
        var metadata = getBound(target);
        var fieldsFound = metadata.getIntersects();
        var fieldsTotal = metadata.getFoundFields();
        var fieldsSetters = metadata.getBoundSetters();
        var fieldsBound = metadata.getBoundFields();
        var foundFields = ConversionUtils.scanFieldsToMap(target);
        var boundFields = ConversionUtils.scanFieldsToMap(response);
        var boundMethods = ConversionUtils.scanMethodsToMap(target);

        metadata.setBoundClass(response);

        for(var current: foundFields.entrySet()) {
            var found = current.getValue();
            var bound = boundFields.get(current.getKey());

            if(bound != null) {
                var foundType = found.getType();
                var boundType = bound.getType();

                if(
                    boundType == foundType &&
                    (
                        !Collection.class.isAssignableFrom(foundType) || isConventionalCollection(found, bound)
                    ) ||
                    isConverterOwn(found, boundType) &&
                    ConversionResponse.class.isAssignableFrom(boundType)
                ) {
                    fieldsFound.put(found, bound);
                }

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

            fieldsBound.put(field.getType(), field);
        }

        responseMap.put(target, metadata);
    }

    /**
     * Зарегистрировать мутатор или тип ответа в сторе
     *
     * @param mutator мутатор, с которого будет происходить маппинг {@link ConversionMutator}
     * @param target подтип мутатора на который будет происходить маппинг <T> у {@link ConversionMutator}
     */
    public void registerMutator(Class<?> mutator, Class<?> target) {
        var metadata = getBound(mutator);
        var fieldsFound = metadata.getIntersects();
        var fieldsTotal = metadata.getFoundFields();
        var fieldsSetters = metadata.getBoundSetters();
        var fieldsBound = metadata.getBoundFields();
        var foundFields = ConversionUtils.scanFieldsToMap(mutator);
        var boundFields = ConversionUtils.scanFieldsToMap(target);
        var boundMethods = ConversionUtils.scanMethodsToMap(target);

        metadata.setBoundClass(target);

        for(var current: foundFields.entrySet()) {
            var found = current.getValue();
            var bound = boundFields.get(current.getKey());

            if(bound != null) {
                var foundType = found.getType();
                var boundType = bound.getType();

                if(
                    boundType == foundType &&
                    (
                        !Collection.class.isAssignableFrom(foundType) || isConventionalCollection(bound, found)
                    ) ||
                    isConverterOwn(found, boundType) &&
                    ConversionMutator.class.isAssignableFrom(foundType)

                ) {
                    fieldsFound.put(found, bound);
                }

                fieldsTotal.add(found);
            }
        }

        for(var current: boundFields.entrySet()) {
            var field = current.getValue();
            var setter = boundMethods.get("set" + CaseUtil.toPascalCase(current.getKey()));

            if(setter != null) {
                fieldsSetters.put(field, setter);
            }

            fieldsBound.put(field.getType(), field);
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
            return ConversionUtils.findXGeneric(compare) == ConversionUtils.findXGeneric(requireBeConverter);
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
        return ConversionUtils.findXGeneric(boundClazz) == found.getType();
    }

    /**
     * Получить бинды для класса конвертера
     *
     * @param forClass класс для получения биндов
     * @return найденные бинды, или пустая карта
     */
    protected ClassMetadata getBound(Class<?> forClass) {
        var result = (ClassMetadata) null;

        if(ConversionMutator.class.isAssignableFrom(forClass)) {
            result = this.mutatorsMap.get(forClass);
        } else {
            result = this.responseMap.get(forClass);
        }

        if(result == null) {
            result = new ClassMetadata();
        }

        return result;
    }

    @Override
    public String toString() {
        var builder = new StringBuilder();

        builder.append("mutators: ");

        for(var mutator: mutatorsMap.entrySet()) {
            var clazz = mutator.getKey();
            var metadata = mutator.getValue();

            builder.append("\n\t[").append(clazz.getTypeName()).append("] ").append(clazz.getSimpleName()).append(" ->");

            for(var current: metadata.getIntersects().entrySet()) {
                var found = current.getKey();
                var bound = current.getValue();

                builder.append("\n\t\t[").append(found.getType().getSimpleName()).append("] ").append(found.getName()).append(" -> [").append(bound.getType().getSimpleName()).append("] ").append(bound.getName()).append(";");
            }
        }

        builder.append("\nresponses: ");

        for(var mutator: responseMap.entrySet()) {
            var clazz = mutator.getKey();
            var metadata = mutator.getValue();

            builder.append("\n\t[").append(clazz.getTypeName()).append("] ").append(clazz.getSimpleName()).append(" ->");

            for(var current: metadata.getIntersects().entrySet()) {
                var found = current.getKey();
                var bound = current.getValue();

                builder.append("\n\t\t[").append(found.getType().getSimpleName()).append("] ").append(found.getName()).append(" <- [").append(bound.getType().getSimpleName()).append("] ").append(bound.getName()).append(";");
            }
        }

        return builder.toString();
    }
}
