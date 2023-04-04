package io.github.asewhy.conversions;

import io.github.asewhy.conversions.support.BoundedAccessible;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Locale;

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
     * @return биндинг на метод или поле получателя
     */
    protected static BoundedAccessible getBoundForField(@NotNull Collection<AccessibleObject> objects) {
        var field = (Field) null;
        var setter = (Method) null;
        var getter = (Method) null;

        for(var current: objects) {
            if(current instanceof Field && field == null) {
                field = (Field) current;
            } else if(current instanceof Method && setter == null) {
                var method = (Method) current;

                if(method.getName().startsWith("set")) {
                    setter = method;
                } else if(method.getName().startsWith("get")) {
                    getter = method;
                }
            }
        }

        return new BoundedAccessible(field, getter, setter);
    }
}
