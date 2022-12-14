package io.github.asewhy.conversions;

import com.github.javafaker.Faker;
import io.github.asewhy.ReflectionUtils;
import io.github.asewhy.conversions.config.ConversationalTestConfiguration;
import io.github.asewhy.conversions.config.classMap.A;
import io.github.asewhy.conversions.config.classMap.B;
import io.github.asewhy.conversions.config.classMap.C;
import io.github.asewhy.conversions.config.classMap.D;
import io.github.asewhy.conversions.config.converters.ExampleTestBookInterfaceResponse;
import io.github.asewhy.conversions.config.converters.ExampleTestBookResponse;
import io.github.asewhy.conversions.config.converters.ExampleTestMapResponse;
import io.github.asewhy.conversions.config.entities.ExampleTestAuthorEntity;
import io.github.asewhy.conversions.config.entities.ExampleTestBookEntity;
import io.github.asewhy.conversions.config.entities.ExampleTestBookInterfaceA;
import io.github.asewhy.conversions.config.entities.ExampleTestPointEntity;
import io.github.asewhy.conversions.config.support.ExampleTestBook;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ConversationalTestConfiguration.class })
public class ConversionsResponseTest {
    @Autowired
    protected ConversionProvider provider;
    @Autowired
    protected Faker faker;

    @Test
    @DisplayName("Тест инструментария работы с рефлексией")
    public void testConversionUtils() {
        var map = new HashMap<Class<?>, Integer>();

        map.put(C.class, 2);
        map.put(A.class, 0);
        map.put(B.class, 1);

        var result = ReflectionUtils.findOnClassMap(map, D.class);

        Assertions.assertThat(result).isEqualTo(2);
    }

    @Test
    @DisplayName("Тест конвертации карты в DTO")
    public void convertMapToDto() {
        var isbin = "978-3-16-148410-0";
        var name = faker.book().title();
        var source = (Map<String, String>) new HashMap<String, String>();

        source.put("isbin", isbin);
        source.put("name", name);

        var stamp = System.currentTimeMillis();
        var result = (ExampleTestMapResponse) provider.createResponse(source, "some_mapping");
        var timeSkip = System.currentTimeMillis() - stamp;

        System.out.println("TOOK " + timeSkip + " (ms)");

        Assertions.assertThat(result.getIsbin()).isEqualTo(isbin);
        Assertions.assertThat(result.getName()).isEqualTo(name);
    }

    @Test
    @DisplayName("Тест сущности в DTO и с иcпользованием вложенных сущностей")
    public void convertEntityToDto() {
        var book = new ExampleTestBookEntity();
        var isbin = "978-3-16-148410-0";
        var name = faker.book().title();

        book.setIsbin(isbin);
        book.setName(name);
        book.setPageCount(200L);
        book.setAuthors(
            Stream
                .generate(ExampleTestAuthorEntity::new).peek(e -> {
                    e.setBirthDate(LocalDate.now());
                    e.setName(faker.book().author());
                    e.setLocation(
                        new ExampleTestPointEntity() {{
                            setX(20L);
                            setY(20L);
                        }}
                    );
                })
                .limit(20)
            .collect(Collectors.toList())
        );

        var stamp = System.currentTimeMillis();
        var result = provider.createResponse(book);
        var timeSkip = System.currentTimeMillis() - stamp;

        System.out.println("TOOK " + timeSkip + " (ms)");

        if(result instanceof ExampleTestBookResponse) {
            var bookResponse = (ExampleTestBookResponse) result;

            Assertions.assertThat(bookResponse.getAuthors()).hasSize(20);
            Assertions.assertThat(bookResponse.getPageCount()).isEqualTo(200L);
            Assertions.assertThat(bookResponse.getName()).isEqualTo(name);
            Assertions.assertThat(bookResponse.getIsbin()).isEqualTo(isbin);
            Assertions.assertThat(bookResponse.getAuthors()).allMatch(e -> e.getName() != null && e.getLocation().getX().equals(20L));
        } else {
            Assertions.fail("Result must be a ExampleTestBookResponseConverter instance.");
        }
    }

    @Test
    @DisplayName("Тест конвертации сущностей с использованием общего интерфейса")
    public void convertInterfaceToDto() {
        var bookA = (ExampleTestBook) new ExampleTestBookInterfaceA(faker.book().title(), "978-3-16-148410-0", "fantastic");
        var bookB = (ExampleTestBook) new ExampleTestBookInterfaceA(faker.book().title(), "978-3-16-148410-1", "history");

        var stamp = System.currentTimeMillis();
        var resultA = (ExampleTestBookInterfaceResponse) provider.createResponse(bookA);
        var resultB = (ExampleTestBookInterfaceResponse) provider.createResponse(bookB);
        var timeSkip = System.currentTimeMillis() - stamp;

        System.out.println("TOOK " + timeSkip + " (ms)");

        Assertions.assertThat(bookA.getIsbin()).isEqualTo(resultA.getIsbin());
        Assertions.assertThat(bookA.getName()).isEqualTo(resultA.getName());

        Assertions.assertThat(bookB.getIsbin()).isEqualTo(resultB.getIsbin());
        Assertions.assertThat(bookB.getName()).isEqualTo(resultB.getName());
    }
}
