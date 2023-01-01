package io.github.asewhy.conversions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.javafaker.Faker;
import io.github.asewhy.conversions.config.ConversationalTestConfiguration;
import io.github.asewhy.conversions.config.converters.ExampleTestMutatorRequest;
import io.github.asewhy.conversions.config.converters.ExampleTestNonMutatorRequest;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Description;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SuppressWarnings("unchecked")
@SpringBootTest(classes = { ConversationalTestConfiguration.class })
public class ConversionRequestTest {
    @Autowired
    protected ConversionProvider provider;
    @Autowired
    protected Faker faker;

    @Test
    @DisplayName("Конвертируем не MutatorDTO в валидный запрос")
    @Description("Если этот запрос содержит поле MutatorDto то он будет работать правильно")
    public void convertNotMutatorDtoToValidRequest() throws JsonProcessingException {
        var config = provider.getConfig();
        var objectMapper = config.getObjectMapper();

        var isbin = "978-3-16-148410-0";
        var name = faker.book().title();
        var pageCount = 666;
        var data1 = 1;
        var data2 = 2;

        var tree = objectMapper.readTree(
            "{\"externalData1\":" + data1 + ",\"externalData2\":" + data2 + ",\"request\":" + "{\"isbin\":\"" +
                isbin +
                "\",\"name\":\"" +
                name +
                "\",\"pageCount\":" + pageCount +
            "}}"
        );

        var resolvedRequest = (ExampleTestNonMutatorRequest) provider.createRequestResolve(tree, ExampleTestNonMutatorRequest.class, null);

        Assertions.assertInstanceOf(ExampleTestNonMutatorRequest.class, resolvedRequest);
        Assertions.assertInstanceOf(ExampleTestMutatorRequest.class, resolvedRequest.getRequest());

        Assertions.assertEquals(resolvedRequest.getExternalData1(), data1);
        Assertions.assertEquals(resolvedRequest.getExternalData2(), data2);
        Assertions.assertEquals(resolvedRequest.getRequest().getPageCount(), pageCount);
        Assertions.assertEquals(resolvedRequest.getRequest().getName(), name);
        Assertions.assertEquals(resolvedRequest.getRequest().getIsbin(), isbin);
    }

    @Test
    @DisplayName("Конвертируем коллекцию в коллекцию мутаторов")
    @Description("Если как тело запроса указана коллекция, то она будет преобразована в коллекцию мутаторов, если это возможно.")
    public void convertMutatorCollectionToValidCollection() throws JsonProcessingException {
        var config = provider.getConfig();
        var objectMapper = config.getObjectMapper();

        var isbin = "978-3-16-148410-0";
        var name = faker.book().title();
        var pageCount = 666;

        var tree = objectMapper.readTree(
    "[{\"isbin\":\"" +
                isbin +
                "\",\"name\":\"" +
                name +
                "\",\"pageCount\":" + pageCount +
            "}]"
        );

        var type = TypeUtils.parameterize(List.class, ExampleTestMutatorRequest.class);
        var resolvedRequest = (List<ExampleTestMutatorRequest>) provider.createRequestResolve(tree, (Class<?>) type.getRawType(), type);

        Assertions.assertInstanceOf(List.class, resolvedRequest);

        Assertions.assertEquals(resolvedRequest.size(), 1);
        Assertions.assertEquals(resolvedRequest.get(0).getPageCount(), pageCount);
        Assertions.assertEquals(resolvedRequest.get(0).getName(), name);
        Assertions.assertEquals(resolvedRequest.get(0).getIsbin(), isbin);
    }
}
