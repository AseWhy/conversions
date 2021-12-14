package io.github.asewhy.conversions;

import io.github.asewhy.conversions.support.annotations.Identifier;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.*;
import java.util.*;

public class ConversionUtils {
    public final static String COMMON_MAPPING = "common";

    /**
     * Быстрый поиск по карте, ключом в которой является класс, метод учитывает наследование классов
     *
     * @param input входящая карта с классами
     * @param key ключ для поиска
     * @param <T> тип значений карты
     * @return значение или null если не нашел (так-же будет кеширован null на текущий ключ)
     */
    public static <T> T findOnClassMap(Map<Class<?>, T> input, Class<?> key) {
        if(input.containsKey(key)) {
            return input.get(key);
        }

        for (var current: input.entrySet()) {
            var parent = current.getKey();

            if(parent.isAssignableFrom(key)) {
                input.put(key, current.getValue());

                return current.getValue();
            }
        }

        input.put(key, null);

        return null;
    }

    /**
     * Пропустить анонимный класс, если он есть, и получить элемент ниже по дереву зависимостей
     *
     * @param clazz класс для пропуска анонимного класса
     * @param <T> типа класса
     * @return класс который нам нужен
     */
    public static <T> Class<? super T> skipAnonClasses(Class<T> clazz) {
        if(clazz.getSimpleName().equals("")) {
            //
            // Анонимные классы не имеют названия
            //
            return clazz.getSuperclass();
        }

        return clazz;
    }

    /**
     * Получает поле идентификатор в целевом классе.
     *
     * @param clazz класс для поиска идентификатора
     * @return найденное поле-идентификатор
     */
    public static Field findTypeId(Class<?> clazz) {
        var fields = scanFields(clazz);
        var idField = (Field) null;

        for(var field: fields) {
            if(field.getName().equals("id")) {
                idField = field;
            }

            if(field.getAnnotation(Identifier.class) != null) {
                return field;
            }
        }

        return idField;
    }

    /**
     * Получить первый generic параметр у поля
     *
     * @param from поле, generic значение которого нужно получить
     * @return generic тип или nell
     */
    public static Class<?> findXGeneric(Field from) {
        var genericsParams = from.getGenericType();

        if(genericsParams instanceof ParameterizedType pt) {
            var generics = pt.getActualTypeArguments();

            if(generics.length > 0) {
                var generic = generics[0];

                if(generic instanceof Class<?> clazz) {
                    return clazz;
                }
            }
        }

        return null;
    }

    /**
     * Получить первый generic параметр у класса
     *
     * @param from класс из супер класса, которого нужно получить generic
     * @return generic тип или null
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
     * Безопасный вызов метода
     *
     * @param method метод
     * @param caller инстанс класса метод которого вызываем
     * @param args аргументы для вызова
     * @return полученное значение
     */
    public static Object safeInvoke(Method method, Object caller, Object ...args) {
        try {
            var access = method.canAccess(caller);

            method.setAccessible(true);

            var found = method.invoke(caller, args);

            method.setAccessible(access);

            return found;
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Безопасно устанавливает значение полю
     *
     * @param field поле
     * @param caller инстанс класса поле которого изменяем
     */
    public static void safeSet(Field field, Object caller, Object set) {
        try {
            var access = field.canAccess(caller);

            field.setAccessible(true);

            field.set(caller, set);

            field.setAccessible(access);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Безопасный доступ к полю
     *
     * @param field поле
     * @param caller инстанс класса поле которого получаем
     * @return полученное значение
     */
    public static Object safeAccess(Field field, Object caller) {
        try {
            var access = field.canAccess(caller);

            field.setAccessible(true);

            var found = field.get(caller);

            field.setAccessible(access);

            return found;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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

    /**
     * Сканирует методы класса, получая методы, из него и подклассов
     *
     * @param clazz целевой класс
     * @param explode исключения в дереве
     * @return методы класса и супер класса
     */
    public static List<Method> scanMethods(Class<?> clazz, @NotNull Set<Class<?>> explode) {
        var fields = new ArrayList<Method>();

        while(clazz != null) {
            if(explode.contains(clazz)) {
                break;
            }

            var interfaces = clazz.getInterfaces();

            fields.addAll(List.of(clazz.getDeclaredMethods()));

            for(var current: interfaces) {
                fields.addAll(scanMethods(current, explode));
            }

            clazz = clazz.getSuperclass();
        }

        return fields;
    }

    /**
     * Сканирует поля класса, получая поля, из него и подклассов
     *
     * @param clazz целевой класс
     * @param explode исключения в дереве
     * @return поля класса и супер класса
     */
    public static List<Field> scanFields(Class<?> clazz, @NotNull Set<Class<?>> explode) {
        var fields = new ArrayList<Field>();

        while(clazz != null) {
            if(explode.contains(clazz)) {
                break;
            }

            var interfaces = clazz.getInterfaces();

            fields.addAll(List.of(clazz.getDeclaredFields()));

            for(var current: interfaces) {
                fields.addAll(scanFields(current, explode));
            }

            clazz = clazz.getSuperclass();
        }

        return fields;
    }


    /**
     * Сканирует класс, получая от туда все методы и поля, из него и подклассов
     *
     * @param clazz целевой класс
     * @param explode исключения в дереве
     * @return поля и методы класса и супер класса
     */
    public static Map<String, AccessibleObject> scanToMap(Class<?> clazz, @NotNull Set<Class<?>> explode) {
        var fields = new HashMap<String, AccessibleObject>();

        while(clazz != null) {
            if(explode.contains(clazz)) {
                break;
            }

            var interfaces = clazz.getInterfaces();

            for(var field: List.of(clazz.getDeclaredFields())) {
                fields.put(field.getName(), field);
            }

            for(var method: List.of(clazz.getDeclaredMethods())) {
                fields.put(method.getName(), method);
            }

            for(var current: interfaces) {
                fields.putAll(scanToMap(current, explode));
            }

            clazz = clazz.getSuperclass();
        }

        return fields;
    }

    /**
     * Сканирует методы класса, получая методы, из него и подклассов
     *
     * @param clazz целевой класс
     * @param explode исключения в дереве
     * @return методы класса и супер класса
     */
    public static Map<String, Method> scanMethodsToMap(Class<?> clazz, @NotNull Set<Class<?>> explode) {
        var fields = new HashMap<String, Method>();

        while(clazz != null) {
            if(explode.contains(clazz)) {
                break;
            }

            var interfaces = clazz.getInterfaces();

            for(var method: List.of(clazz.getDeclaredMethods())) {
                fields.put(method.getName(), method);
            }

            for(var current: interfaces) {
                fields.putAll(scanMethodsToMap(current, explode));
            }

            clazz = clazz.getSuperclass();
        }

        return fields;
    }

    /**
     * Сканирует поля класса, получая поля, из него и подклассов
     *
     * @param clazz целевой класс
     * @param explode исключения в дереве
     * @return поля класса и супер класса
     */
    public static Map<String, Field> scanFieldsToMap(Class<?> clazz, @NotNull Set<Class<?>> explode) {
        var fields = new HashMap<String, Field>();

        while(clazz != null) {
            if(explode.contains(clazz)) {
                break;
            }

            var interfaces = clazz.getInterfaces();

            for(var field: List.of(clazz.getDeclaredFields())) {
                fields.put(field.getName(), field);
            }

            for(var current: interfaces) {
                fields.putAll(scanFieldsToMap(current, explode));
            }

            clazz = clazz.getSuperclass();
        }

        return fields;
    }

    public static Map<String, Method> scanMethodsToMap(Class<?> clazz) {
        return scanMethodsToMap(clazz, Set.of());
    }

    public static Map<String, Field> scanFieldsToMap(Class<?> clazz) {
        return scanFieldsToMap(clazz, Set.of());
    }

    public static Map<String, AccessibleObject> scanToMap(Class<?> clazz) {
        return scanToMap(clazz, Set.of());
    }

    public static List<Method> scanMethods(Class<?> clazz) {
        return scanMethods(clazz, Set.of());
    }

    public static List<Field> scanFields(Class<?> clazz) {
        return scanFields(clazz, Set.of());
    }

    public static List<AccessibleObject> scan(Class<?> clazz) {
        return scan(clazz, Set.of());
    }
}
