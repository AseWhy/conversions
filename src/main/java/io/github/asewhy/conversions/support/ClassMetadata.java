package io.github.asewhy.conversions.support;

import io.github.asewhy.ReflectionUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
    // Все найденные поля класса целевого класса
    //
    private Set<Field> boundFields = new HashSet<>();
    //
    // Все найденные поля класса
    //
    private Map<Class<?>, Field> boundFieldsMap = new HashMap<>();
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
        return boundFieldsMap.get(forClass);
    }

    /**
     * Установить класс получателя
     *
     * @param boundClass класс получателя
     */
    public void setBoundClass(Class<?> boundClass) {
        this.boundClass = boundClass;
    }

    /**
     * Получить значение поля для поля и класса
     *
     * @param from поле для получения значения
     * @param found объект для получения значения
     * @return полученное значение
     */
    public Object getFieldValue(Object from, Field found) {
        return foundGetters.containsKey(found) ? ReflectionUtils.safeInvoke(foundGetters.get(found), from) : ReflectionUtils.safeAccess(found, from);
    }

    /**
     * Добавить поле получетаеля
     *
     * @param field поле получателя
     */
    public void addBoundField(Field field) {
        if(field != null) {
            this.boundFields.add(field);
            this.boundFieldsMap.put(field.getType(), field);
        }
    }
}
