package io.github.asewhy.conversions;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class ConversionUtils {
    /**
     * Получить первый generic параметр у метода
     *
     * @param from класс из суперкласса которого нужно получить генерик
     * @return генерик тип
     */
    public static Class<?> findXGeneric(Class<?> from) {
        var superClass = from.getGenericSuperclass();

        if(superClass instanceof ParameterizedType parameterizedClass) {
            var fromGenerics = parameterizedClass.getActualTypeArguments();
            var fromGeneric = (Class<?>) null;

            if (fromGenerics.length > 0) {
                var genericType = fromGenerics[0];

                if (genericType instanceof Class<?> c) {
                    fromGeneric = c;
                }
            }

            return fromGeneric;
        } else {
            return null;
        }
    }

    /**
     * Безопасный доступ к полю
     *
     * @param field поле
     * @param caller инстанс класса поле которого получаем
     * @return полученное значение
     * @throws IllegalAccessException если получить значение не удалось
     */
    public static Object safeAccess(Field field, Object caller) throws IllegalAccessException {
        var access = field.canAccess(caller);

        field.setAccessible(true);

        var found = field.get(caller);

        field.setAccessible(access);

        return found;
    }

    /**
     * Сканирует класс, получая от туда все методы и поля, из него и подклассов
     *
     * @param clazz целевой класс
     * @param explode исключения в дереве
     * @return поля и методы класса и супер класса
     */
    public static List<AccessibleObject> scan(Class<?> clazz, @NotNull Set<Class<?>> explode) {
        var fields = new ArrayList<AccessibleObject>();

        while(clazz != null) {
            if(explode.contains(clazz)) {
                break;
            }

            var interfaces = clazz.getInterfaces();

            fields.addAll(List.of(clazz.getDeclaredFields()));
            fields.addAll(List.of(clazz.getDeclaredMethods()));

            for(var current: interfaces) {
                fields.addAll(scan(current, explode));
            }

            clazz = clazz.getSuperclass();
        }

        return fields;
    }

    public static List<AccessibleObject> scan(Class<?> clazz) {
        return scan(clazz, Set.of());
    }
}
