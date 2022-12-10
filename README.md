# Conversions

Небольшой модуль для конвертации сущностей в DTO. Конвертация происходит за счет рефлексии. Для начала работы с модулем
необходимо настроить сканирование компонентов, для чего нужно создать бин iConversionConfiguration любым удобным способом. Для
автоматического сканирования компонентов приложения нужно установить EnableConversions аннотацию в любую точку конфигурации
приложения.

### Пример конфигурации модуля

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.asewhy.conversions.ConversionStore;
import io.github.asewhy.conversions.support.annotations.EnableConversions;
import io.github.asewhy.conversions.support.iConversionConfiguration;
import io.github.asewhy.conversions.support.naming.iConversionNamingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConversions
public class ConversionConfig implements iConversionConfiguration {
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    protected ApplicationContext context;
    @Autowired
    protected ConversionSupportContext supportContext;

    @Override
    public ConversionStore conversionStore() {
        var store = new ConversionStore(context);

        store.from("com.example");

        return store;
    }

    @Override
    public ObjectMapper objectMapper() {
        return objectMapper;
    }

    @Override
    public Object context() {
        return supportContext;
    }
}
```

Как показано выше в методе `provideStore` необходимо вернуть экземпляр хранилища конверсий. В хранилище хранятся информация
о конвертируемых сущностях, и маппинги полей к ним. Далее поставщиком конверсий все сущности будут создаваться из экземпляра этого
хранилища.

Если необходима собственная политика именования сущностей, то необходимо предоставить её в методе конфигурации `namingPolicy`. Пример измененной конфигурации показан ниже:

```java
public class ConversionConfig implements iConversionConfiguration {
    // ...
    @Autowired
    protected ConversionNamingStrategy conversionNamingStrategy;

    @Override
    public iConversionNamingStrategy namingStrategy() {
        return conversionNamingStrategy;
    }

    // ...
}
```

Пример политики именования можно увидеть ниже:

```java
import io.github.asewhy.conversions.support.CaseUtil;
import io.github.asewhy.conversions.support.naming.ExtrudableNamingStrategy;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import paa.coder.noodleCriteriaBuilder.restFilter.payloads.RestFilter;
import paa.coder.noodleCriteriaBuilder.restFilter.payloads.RestOrder;
import paa.coder.noodleCriteriaBuilder.restFilter.payloads.RestPage;
import java.util.Set;

@Component
public class ConversionNamingStrategy extends ExtrudableNamingStrategy {
    /**
     * Все сущности кроме этих будут следовать политике
     */
    private final static Set<Class<?>> EXCLUDED = Set.of(
        RestFilter.class,
        RestPage.class,
        RestOrder.class
    );

    @Override
    protected boolean isExcluded(@NotNull String defaultName, @NotNull Class<?> rawReturnType) {
        return EXCLUDED.contains(rawReturnType);
    }

    /**
     * Следуя этой политике, конвертер думает что все поля сущностей имеют snakeCase в запросе, и ответе
     */
    @Override
    protected String convert(@NotNull String defaultName) {
        return CaseUtil.toLowerSnakeCase(defaultName);
    }
}
```

В примере показана работа с конкретно `ExtrudableNamingStrategy` классом, но никто не запрещает реализовать свою логику используя `iConversionNamingStrategy`;

## Конвертация сущностей

Конвертеры в модуле делятся на конвертеры ответа и конвертеры запроса.

### Конвертация ответа

Для указания того что класс является целью конвертации ответа необходимо пометить его как `@ResponseDTO`, как на примере ниже.

```java
import io.github.asewhy.conversions.ConversionResponse;
import io.github.asewhy.conversions.support.annotations.ResponseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@ResponseDTO
public class SomeSourceObjectDTO extends ConversionResponse<SomeSourceObject> {
    private Long id;
}
```

Если есть какие-то поля, которые конвертер не может разрешить сам, можно указать метод fillInternal куда первым параметром
будет подаваться исходная сущность. Пример показан ниже.

```java
import io.github.asewhy.conversions.ConversionResponse;
import io.github.asewhy.conversions.support.annotations.ResponseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@ResponseDTO
public class SomeSourceObjectDTO extends ConversionResponse<SomeSourceObject> {
    private Long id;
    private String someUnfilledField;
    
    @Override
    protected void fillInternal(SomeSourceObject from, Object context) {
        this.someUnfilledField = from.someMethodWhoReturnsSomeUnfilledFieldValue();
    }
}
```

Таким образом поле `someUnfilledField` будет заполнено результатом выполнения метода `someMethodWhoReturnsSomeUnfilledFieldValue`
у `SomeSourceObject`.

#### Конвертация из общего интерфейса

Начиная с версии 1.0.3 есть возможность выполнять преобразования сущностей A и B реализовывающих общий 
интерфейс в одну сущность ответа. Пример можно увидеть ниже:

```java
public interface IExampleTestBook {
    String getName();
    String getIsbin();
}
```

```java
@Getter
@Setter
@ToString
@AllArgsConstructor
public class ExampleTestBookInterfaceA implements IExampleTestBook {
    private String name;
    private String isbin;
    private String genre;
}
```

```java
@Getter
@Setter
@ToString
@AllArgsConstructor
public class ExampleTestBookInterfaceB implements IExampleTestBook {
    private String name;
    private String isbin;
    private Integer pageCount;
}
```

```java
@Getter
@Setter
@ToString
@ResponseDTO
public class ExampleTestBookInterfaceResponse extends ConversionResponse<IExampleTestBook> {
    private String name;
    private String isbin;
}
```

Пример выше скажет конвертеру конвертировать все экземпляры `IExampleTestBook` в `ExampleTestBookInterfaceResponse`

### Конвертация запроса

Для указания того, что исходный объект является целью конвертации запроса нужно пометить его аннотацией `@ConversionMutator`, как в примере ниже.

```java
import io.github.asewhy.conversions.ConversionMutator;
import io.github.asewhy.conversions.support.annotations.MutatorDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Min;

@Setter
@Getter
@ToString
@MutatorDTO
public class SomeSourceObjectMutatorDTO extends ConversionMutator<SomeSourceObjectDTO> {
    @Min(0)
    private int someInt;
}
```

Если есть какие-то поля, которые конвертер не может разрешить сам, можно указать метод fillInternal куда первым параметром
будет подаваться исходная сущность. Пример показан ниже.

```java
import io.github.asewhy.conversions.ConversionMutator;
import io.github.asewhy.conversions.support.annotations.MutatorDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Min;

@Setter
@Getter
@ToString
@MutatorDTO
public class SomeSourceObjectMutatorDTO extends ConversionMutator<SomeSourceObjectDTO> {
    @Min(0)
    private int someInt;
    private long p;

    @Override
    protected void fillInternal(SomeSourceObjectDTO fill, Object context) {
        if(context instanceof SomeService ss) {
            p = ss.doSomeWhoReturnsP();
        }
    }
}
```

На примере выше, в процессе заполнения сущности поле p будет заполнено значением которое будет получено из сервиса SomeService. При 
этом экземпляр SomeService нужно передать в конфигурации как контекст.

Если есть мутируемая сущность является вложенной, то в ней можно заполнить некоторые поля из родительской сущности. Используя метод `fillParentInternal`
можно инициализировать текущий объект при помощи родительского объекта. Пример показан ниже.

```java
import io.github.asewhy.conversions.ConversionMutator;
import io.github.asewhy.conversions.support.annotations.MutatorDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Min;

@Setter
@Getter
@ToString
@MutatorDTO
public class SomeParentSourceObjectMutatorDTO extends ConversionMutator<SomeSourceObjectDTO> {
    private int id;
    private SomeSourceObjectMutatorDTO children;
}
```

```java
import io.github.asewhy.conversions.ConversionMutator;
import io.github.asewhy.conversions.support.annotations.MutatorDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.Min;

@Setter
@Getter
@ToString
@MutatorDTO
public class SomeSourceObjectMutatorDTO extends ConversionMutator<SomeSourceObjectDTO> {
    @Min(0)
    private int someInt;
    private int parentId;

    @Override
    protected void fillParentInternal(SomeSourceObjectDTO fill, Object parent, Object context) {
        if(parent instanceof SomeParentSourceObjectMutatorDTO p) {
            this.parentId = p.getId();
        }
    }
}
```

Таким образом поле `parentId` у мутатора `SomeSourceObjectMutatorDTO` будет заполнено родительским идентификатором, в случае
если сущность будет вложенной.

## Работа с контроллерами

На примере выше показан процесс декларации мутатора запроса, и объекта ответа. После декларации, его можно использовать в контроллере просто указав
контроллер как `@ShiftController` или пометив метод как `@ConvertResponse`, не будет работать если метод уже помечен, на пример как `@ResponseBody`. 
Пример контроллера показан ниже.

```java
import io.github.asewhy.conversions.support.annotations.ShiftController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@ShiftController
@RequestMapping("/comments")
public class CommentController {
    @GetMapping("/{id}")
    public SomeSourceObjectMutatorDTO get(@PathVariable("id") I id) {
        // Метод должен возвращать экземпляр SomeSourceObject
        return (SomeSourceObjectMutatorDTO) provideService().restFindById(id);
    }
}
```

Пример использования конвертера запроса показан ниже.

```java
import io.github.asewhy.conversions.support.annotations.ConvertMutator;
import io.github.asewhy.conversions.support.annotations.ShiftController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@ShiftController
@RequestMapping("/comments")
public class CommentController {
    @PostMapping
    public void editSomeEntity(@ConvertMutator @Valid SomeSourceObjectMutatorDTO payload) {
        var foundEntity = null;// Получаем экземпляр редактируемой сущности
        
        // Метод fill поставляется базовым классом мутатора и автоматически заполняет поля с теми же названиями и типом в целевом объекте
        // важно чтобы целевой объект был объектом указанным в generic при декларации мутатора
        payload.fill(foundEntity);
    }
}
```

## Конвертация контейнеров

У вас могут быть контейнеры, которые включают в себя сущности для конвертации. Модуль не может их распознать автоматически, поэтому
необходимо создавать специальные ресолверы для таких контейнеров. Пример ресолвера показан ниже.

```java
import io.github.asewhy.conversions.ConversionProvider;
import io.github.asewhy.conversions.ConversionResolver;
import io.github.asewhy.conversions.support.annotations.DataResolver;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import paa.coder.noodleCriteriaBuilder.restFilter.payloads.RestPage;

import java.lang.reflect.Type;

@Component
@DataResolver
public class RestPageResponseResolver extends ConversionResolver<RestPage<?>> {
    @Override
    protected RestPage<?> resolveInternalResponse(
        @NotNull RestPage<?> restPage,
        Class<? extends RestPage<?>> aClass,
        @NotNull ConversionProvider conversionProvider,
        String mapping
    ) {
        return new RestPage<>(
            restPage.getFilter(),
            restPage.getContent().parallelStream().map(conversionProvider::createResponse).toList(),
            restPage.getTotalElements()
        );
    }

    @Override
    protected Class<?> extractInternalExample(@NotNull RestPage<?> from, String mapping, Object globalContextOrPassedContext) {
        return from.stream().map(Object::getClass).findFirst().orElse(null);
    }

    @Override
    protected boolean canProcess(Class<?> from, Type generics, ConversionProvider provider, String mapping) {
        return RestPage.class.isAssignableFrom(from);
    }
}
```

В примере выше ресолвится REST страница. Пример выше позволяет конвертировать содержимое Rest страницы. При этом оставляя тот же формат.
