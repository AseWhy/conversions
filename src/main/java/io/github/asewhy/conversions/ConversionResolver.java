package io.github.asewhy.conversions;

@SuppressWarnings("unused")
public abstract class ConversionResolver<T> {
    public String resolveMapping(T from, String defaultMapping) {
        return defaultMapping;
    }

    /**
     * Должен вернуть true если необходимо обновлять маппинг для каждой вложенной сущности
     *
     * @param defaultMapping маппинг по умолчанию
     * @return true если это необходимо
     */
    public Boolean propagation(T from, String defaultMapping) {
        return false;
    }
}
