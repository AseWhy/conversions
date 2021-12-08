package io.github.asewhy.conversions.support;

import java.io.ByteArrayOutputStream;
import java.util.Set;

public class CaseUtil {
    private static final Set<Character> separators = Set.of('-', ' ', '_');

    public static String toLowerSnakeCase(String input) {
        if (input == null) {
            return "";
        } else if (input.isBlank()) {
            return "";
        }

        var result = new ByteArrayOutputStream();

        for (int i = 0; i < input.length(); i++) {
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