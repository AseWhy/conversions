package io.github.asewhy.conversions.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.asewhy.conversions.ConversionStore;
import io.github.asewhy.conversions.support.naming.iConversionNamingStrategy;

public interface iConversionConfiguration {
    /**
     * Функция поставщик стора
     *
     * @return стор
     */
    ConversionStore conversionStore();

    /**
     * Функция потравщик маппера
     *
     * @return json маппер
     */
    ObjectMapper objectMapper();

    /**
     * Предоставить контекст для заполнения сущностей
     * <p>
     * !! Поле не кешируется !!
     * Контекст будет запрашиваться каждый раз при вызове метода fill у мутатора без параметра контекста.
     *
     * @return контекст для заполнения сущностей
     */
    default Object context() {
        return null;
    }

    /**
     * Должен возвращать TRUE, если нужен вывод отладочной информации
     *
     * @return true если нужен вывод отладочной информации
     */
    default Boolean isDebug() {
        return false;
    }

    /**
     * Предоставить стратегию именования полей конвертируемых в DTO сущностей
     *
     * @return стратегия именования
     */
    default iConversionNamingStrategy namingStrategy() {
        return (defaultName, rawReturnType) -> defaultName;
    }
}
