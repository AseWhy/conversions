package io.github.asewhy.conversions;

import io.github.asewhy.conversions.support.BoundedReceiver;
import io.github.asewhy.conversions.support.BoundedSource;
import io.github.asewhy.conversions.support.CaseUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ConversionUtils {
    public final static String COMMON_MAPPING = "common";

    /**
     * Получить предполагаемое имя поля без геттера или сеттера
     *
     * @param sourceName исходное имя метода
     * @return предполагаемое название поля
     */
    public static @NotNull String getPureName(@NotNull String sourceName) {
        if(sourceName.startsWith("set") || sourceName.startsWith("get")) {
            var strip = sourceName.substring(3);

            if(strip.length() > 1) {
                return strip.substring(0, 1).toLowerCase(Locale.ROOT) + strip.substring(1);
            } else {
                return strip.toLowerCase(Locale.ROOT);
            }
        } else {
            return sourceName;
        }
    }

    /**
     * Получить биндинг получателя поля
     *
     * @param methods карта с методами
     * @param field поле для которого получаем биндинг
     * @return биндинг на метод или поле получателя
     */
    @Contract("_, _ -> new")
    protected static @NotNull BoundedReceiver getReceiverForField(@NotNull Map<String, Method> methods, @NotNull Field field) {
        var methodName = "set" + CaseUtil.toPascalCase(field.getName());
        var annotations = Set.of(field.getDeclaredAnnotations());

        if(methods.containsKey(methodName)) {
            var method = methods.get(methodName);

            if(Modifier.isStatic(field.getModifiers())){
                return new BoundedReceiver(field, annotations);
            }

            if(method.getReturnType().isAssignableFrom(field.getType())) {
                var mergedAnnotations = new HashSet<Annotation>();
                var methodAnnotations = Set.of(method.getDeclaredAnnotations());

                mergedAnnotations.addAll(annotations);
                mergedAnnotations.addAll(methodAnnotations);

                return new BoundedReceiver(method, mergedAnnotations);
            } else {
                return new BoundedReceiver(field, annotations);
            }
        } else {
            return new BoundedReceiver(field, annotations);
        }
    }

    /**
     * Получить биндинг источника поля
     *
     * @param methods карта с методами
     * @param field поле для которого получаем биндинг
     * @return биндинг на метод или поле источника
     */
    @Contract("_, _ -> new")
    protected static @NotNull BoundedSource getSourceForField(@NotNull Map<String, Method> methods, @NotNull Field field) {
        var methodName = "get" + CaseUtil.toPascalCase(field.getName());
        var annotations = Set.of(field.getDeclaredAnnotations());

        if(methods.containsKey(methodName)) {
            var method = methods.get(methodName);

            if(Modifier.isStatic(field.getModifiers())){
                return new BoundedSource(field, annotations);
            }

            if(method.getReturnType().isAssignableFrom(field.getType())) {
                var mergedAnnotations = new HashSet<Annotation>();
                var methodAnnotations = Set.of(method.getDeclaredAnnotations());

                mergedAnnotations.addAll(annotations);
                mergedAnnotations.addAll(methodAnnotations);

                return new BoundedSource(method, mergedAnnotations);
            } else {
                return new BoundedSource(field, annotations);
            }
        } else {
            return new BoundedSource(field, annotations);
        }
    }
}
