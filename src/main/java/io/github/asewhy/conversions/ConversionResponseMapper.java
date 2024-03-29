package io.github.asewhy.conversions;

/**
 * Допустим есть класс N и X для ответа на сущность T
 * Сущность S универсальная и в зависимости от определенных условий может быть преобразована в N или X
 * Для того, чтобы указать поставщику в какую сущность преобразовывать, нужно указать маппинг (который по умолчанию common)
 *
 * @param <T> тип сущности T ответ на которую регистрируем.
 */
@SuppressWarnings("unused")
public abstract class ConversionResponseMapper<T> {
    /**
     * Функция разрешения названия маппинга для текущей пробразовываемой сущности. Т.е. строка, которую вернет этот
     * метод, будет являться новым маппингом для провайдера при поиске соответствующего конвертера типа ответа. Если тип ответа
     * с маппингом, который вернул этот метод не будет найдена, то будет использоваться стандартный common тип ответа.
     *
     * @param from объект переданный на преобразование в сущность ответа.
     * @param defaultMapping маппинг переданный по умолчанию (например если это вызов функции конверсии в ручном режиме,
     *                       и маппинг был передан руками или маппинг был указан как соответствующее свойство аннотации
     * @return конечный маппинг используемый поставщиком.
     */
    public String resolveMapping(T from, String defaultMapping) {
        return defaultMapping;
    }

    /**
     * Должен вернуть true если необходимо обновлять маппинг для каждой вложенной сущности
     * <p>
     * Допустим у сущности N есть зависимые сущности D и P которые преобразуются из поля x T сущности.
     * В случае, если propagation T вернет true, то для сущности N будет найден {@link ConversionResponseMapper}&lt;N&gt;
     * с помощью которого будет выполнен повторный поиск маппинга для сущности N для преобразования в D или P
     * сущность ответа
     *
     * @param from исходный класс
     * @param defaultMapping маппинг по умолчанию
     * @return true если это необходимо
     */
    public Boolean propagation(T from, String defaultMapping) {
        return false;
    }
}
