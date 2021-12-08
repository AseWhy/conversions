package io.github.asewhy.conversions.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.asewhy.conversions.ConversionStore;

public interface iConversionFactory {
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
