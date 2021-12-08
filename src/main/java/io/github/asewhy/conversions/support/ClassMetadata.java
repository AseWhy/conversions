package io.github.asewhy.conversions.support;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@ToString
public class ClassMetadata {
    private Map<Field, Field> intersects = new HashMap<>();
    private Set<Field> found = new HashSet<>();
    private Class<?> boundClass;
}
