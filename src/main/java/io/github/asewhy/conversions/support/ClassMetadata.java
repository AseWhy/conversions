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
    // Поля пересечения с классом конверсии <Поле источника, Поле цели>
    //
    private Map<iBound, iBound> intersect = new HashMap<>();
    //
    // Все найденные поля класса источника
    //
    private Set<iBound> found = new HashSet<>();
    //
    // Все найденные поля класса получателя
    //
    private Set<iBound> bound = new HashSet<>();
    //
    // Все найденные поля класса (ключ по имени)
    //
    private Map<String, iBound> boundFieldsNameMap = new HashMap<>();
    //
    // Класс с которым искались пересечения
    //
    private Class<?> boundClass;
    //
    // True если получатель является картой вида ключ - значение
    //
    private Boolean isMap;

    /**
     * Найти поле по его имени
     *
     * @param field название поля
     * @return найденное поле (если есть) или null
     */
    public iBound getBoundField(String field) {
        return boundFieldsNameMap.get(field);
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
     * Добавить поле получетаеля
     *
     * @param bound поле получателя
     */
    public void addBound(iBound bound) {
        if(bound != null) {
            this.bound.add(bound);
            this.boundFieldsNameMap.put(bound.getPureName(), bound);
        }
    }
}
