package io.github.asewhy.conversions;

import io.github.asewhy.conversions.support.ClassMetadata;
import io.github.asewhy.conversions.support.annotations.MutatorDTO;
import io.github.asewhy.conversions.support.annotations.ResponseDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
                var generics = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments();
                var generic = (Class<?>) null;

                if(generics.length > 0) {
                    var genericType = generics[0];

                    if(genericType instanceof Class<?> c) {
                        generic = c;
                    }
                }

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
        var fieldsTotal = metadata.getFound();
        var foundFields = allFieldsIn(target);
        var boundFields = allFieldsIn(response);

        metadata.setBoundClass(response);

        for(var current: foundFields.entrySet()) {
            var found = current.getValue();
            var bound = boundFields.get(current.getKey());

            if(bound != null) {
                var foundType = found.getType();
                var boundType = bound.getType();

                if(
                    boundType == foundType || isConverterOwn(found, boundType) &&
                    ConversionResponse.class.isAssignableFrom(foundType)
                ) {
                    fieldsFound.put(found, bound);
                }

                fieldsTotal.add(found);
            }
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
        var fieldsTotal = metadata.getFound();
        var foundFields = allFieldsIn(mutator);
        var boundFields = allFieldsIn(target);

        metadata.setBoundClass(target);

        for(var current: foundFields.entrySet()) {
            var found = current.getValue();
            var bound = boundFields.get(current.getKey());

            if(bound != null) {
                var foundType = found.getType();
                var boundType = bound.getType();

                if(
                    boundType == foundType || isConverterOwn(found, boundType) &&
                    ConversionMutator.class.isAssignableFrom(foundType)
                ) {
                    fieldsFound.put(found, bound);
                }

                fieldsTotal.add(found);
            }
        }

        mutatorsMap.put(mutator, metadata);
    }

    /**
     * Вернет true если это поле конвертера, для поля целевого класса
     *
     * @param found поле с конвертером
     * @param boundClazz тип целевого поля
     * @return true если истина
     */
    protected boolean isConverterOwn(Field found, Class<?> boundClazz) {
        var genericsParams = found.getGenericType();

        if(genericsParams instanceof ParameterizedType pt) {
            var generics = pt.getActualTypeArguments();

            if(generics.length > 0) {
                var generic = generics[0];

                if(generic instanceof Class<?> clazz) {
                    return clazz == boundClazz;
                }
            }
        }

        return false;
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

    /**
     * Получить все карту типа <название, поле> для класса clazz
     *
     * @param clazz класс для сканирования полей
     * @return найденная карта полей
     */
    protected static Map<String, Field> allFieldsIn(Class<?> clazz) {
        var foundFields = new HashMap<String, Field>();
        var objects = ConversionUtils.scan(clazz, Set.of(ConversionMutator.class));

        for(var field: objects) {
            if(field instanceof Field currentField) {
                foundFields.put(currentField.getName(), currentField);
            }
        }

        return foundFields;
    }
}
