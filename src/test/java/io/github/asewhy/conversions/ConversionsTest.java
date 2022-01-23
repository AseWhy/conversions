package io.github.asewhy.conversions;

import io.github.asewhy.ReflectionUtils;
import io.github.asewhy.conversions.config.ConversationalTestFactory;
import io.github.asewhy.conversions.config.classMap.A;
import io.github.asewhy.conversions.config.classMap.B;
import io.github.asewhy.conversions.config.classMap.C;
import io.github.asewhy.conversions.config.classMap.D;
import io.github.asewhy.conversions.config.converters.ExampleTestBookResponseConverter;
import io.github.asewhy.conversions.config.entities.ExampleTestAuthorEntity;
import io.github.asewhy.conversions.config.entities.ExampleTestBookEntity;
import io.github.asewhy.conversions.config.entities.ExampleTestPointEntity;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { ConversationalTestFactory.class })
public class ConversionsTest {
    @Autowired
    protected ConversionProvider provider;

    @Test
    public void testConversionUtils() {
        var map = new HashMap<Class<?>, Integer>();
        map.put(C.class, 2);
        map.put(A.class, 0);
        map.put(B.class, 1);
        var result = ReflectionUtils.findOnClassMap(map, D.class);
        Assertions.assertThat(result).isEqualTo(2);
    }

    @Test
    public void conversionMapResponse() {
        var source = new HashMap<String, String>();

        source.put("isbin", "sdasdasda");
        source.put("name", "somename");

        var stamp = System.currentTimeMillis();
        var result = provider.createResponse(source, "some_mapping");
        var timeSkip = System.currentTimeMillis() - stamp;

        System.out.println("TOOK " + timeSkip + " (ms)");
        System.out.println(result);
    }

    @Test
    public void conversionsTest() {
        var book = new ExampleTestBookEntity();

        book.setIsbin("141523rwer23234");
        book.setName("dasfsdgsdgsdgs");
        book.setPageCount(200L);
        book.setAuthors(Stream.generate(ExampleTestAuthorEntity::new).peek(e -> {
            e.setBirthDate(LocalDate.now());
            e.setName("Abdula");
            e.setLocation(
                new ExampleTestPointEntity() {{
                    setX(20L);
                    setY(20L);
                }}
            );
        }).limit(20).collect(Collectors.toList()));

        var stamp = System.currentTimeMillis();
        var result = provider.createResponse(book);
        var timeSkip = System.currentTimeMillis() - stamp;

        System.out.println("TOOK " + timeSkip + " (ms)");

        if(result instanceof ExampleTestBookResponseConverter bookResponse) {
            Assertions.assertThat(timeSkip).isLessThan(10);
            Assertions.assertThat(bookResponse.getAuthors()).hasSize(20);
            Assertions.assertThat(bookResponse.getPageCount()).isEqualTo(200L);
            Assertions.assertThat(bookResponse.getName()).isEqualTo("dasfsdgsdgsdgs");
            Assertions.assertThat(bookResponse.getIsbin()).isEqualTo("141523rwer23234");
            Assertions.assertThat(bookResponse.getAuthors()).allMatch(e -> e.getName().equals("Abdula") && e.getLocation().getX().equals(20L));
        } else {
            Assertions.fail("Result must be a ExampleTestBookResponseConverter instance.");
        }
    }
}
