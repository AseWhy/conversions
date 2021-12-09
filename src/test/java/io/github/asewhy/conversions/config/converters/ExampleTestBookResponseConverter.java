package io.github.asewhy.conversions.config.converters;

import io.github.asewhy.conversions.ConversionResponse;
import io.github.asewhy.conversions.config.entities.ExampleTestBookEntity;
import io.github.asewhy.conversions.support.annotations.ResponseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
@ResponseDTO
public class ExampleTestBookResponseConverter extends ConversionResponse<ExampleTestBookEntity> {
    private String name;
    private String isbin;
    private Long pageCount;
    private List<ExampleTestAuthorResponseConverter> authors;
}
