package io.github.asewhy.conversions.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.asewhy.conversions.ConversionStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public interface iConversionFactory {
    String ANY = "$any";

    /**
     * Функция поставщик стора
     *
     * @return стор
     */
    ConversionStore provideStore();

    /**
     * Функция потравщик маппера
     *
     * @return json маппер
     */
    ObjectMapper provideObjectMapper();

    /**
     * Предоставить контекст для заполнения сущностей
     *
     * @return контекст для заполнения сущностей
     */
    default Object provideContext() {
        return null;
    }

    /**
     * Поставщик исключений для convertFieldName, в случае совпадения типа поля или метода геттера, будет использоваться чистое имя
     * поля, или чистое значение jsonProperty
     *
     * @return набор с классами полей, названия которых нужно резрешать с помощью стандартного механизма
     */
    default Map<Class<?>, Set<String>> provideExcludes() {
        return new HashMap<>();
    }

    /**
     * Метод должен возвращать имя поля, которое ожидает поле класса
     *
     * [поле класса] -> convertFieldName(пое класса) -> [поле метода]
     *
     * @param fromName название поля класса
     * @return название поля в json
     */
    default String convertFieldName(String fromName) {
        return CaseUtil.toLowerSnakeCase(fromName);
    }
}
