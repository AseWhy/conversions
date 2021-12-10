package io.github.asewhy.conversions.support;

import java.io.ByteArrayOutputStream;
import java.util.Locale;
import java.util.Set;

public class CaseUtil {
    private static final Set<Character> separators = Set.of('-', ' ', '_');

    public static String toUpperCase(String input) {
        var pascal = toPascalCase(input);
        return pascal.substring(0, 1).toLowerCase(Locale.ROOT) + pascal.substring(1);
    }

    public static String toPascalCase(String input) {
        if (input == null) {
            return "";
        } else if (input.isBlank()) {
            return "";
        }

        var result = new ByteArrayOutputStream();
        var length = input.length();
        var needToNextUpper = true;

        for (int i = 0; i < length; i++) {
            var ch = input.charAt(i);

            while(separators.contains(ch)) {
                ch = input.charAt(++i); needToNextUpper = true;
            }

            if (needToNextUpper) {
                result.write(Character.toUpperCase(ch));
            } else {
                result.write(ch);
            }

            needToNextUpper = false;
        }

        return result.toString();
    }

    public static String toLowerSnakeCase(String input) {
        if (input == null) {
            return "";
        } else if (input.isBlank()) {
            return "";
        }

        var result = new ByteArrayOutputStream();
        var length = input.length();

        for (int i = 0; i < length; i++) {
            var ch = input.charAt(i);

            while(separators.contains(ch)) {
                ch = input.charAt(++i);
            }

            if (i != 0 && Character.isUpperCase(ch)) {
                result.write('_');
                result.write(Character.toLowerCase(ch));
            } else {
                result.write(ch);
            }
        }

        return result.toString();
    }
}