package io.github.asewhy.conversions.support;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Getter
@Setter
@ToString
public class ClassMetadata {
    //
    // Поля пересечения с классом конверсии
    //
    private Map<Field, Field> intersects = new HashMap<>();
    //
    // Все найденные поля класса целевого класса
    //
    private Set<Field> foundFields = new HashSet<>();
    //
    // Все найденные поля класса
    //
    private Map<Class<?>, Field> boundFields = new HashMap<>();
    //
    // Сеттеры для всех найденных полей
    //
    private Map<Field, Method> boundSetters = new HashMap<>();
    //
    // Сеттеры для всех найденных полей
    //
    private Map<Field, Method> foundGetters = new HashMap<>();
    //
    // Класс с которым искались пересечения
    //
    private Class<?> boundClass;
    //
    // True если получатель является картой вида ключ - значение
    //
    private Boolean isMap;

    /**
     * Найти поле по его типу
     *
     * @param forClass тип для поиска
     * @return найденное поле тип которого соответствует искомому
     */
    public Field getBoundField(Class<?> forClass) {
        return boundFields.get(forClass);
    }
}
