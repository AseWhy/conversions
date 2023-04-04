package io.github.asewhy.conversions.config.converters.book;

import io.github.asewhy.conversions.ConversionResponse;
import io.github.asewhy.conversions.config.support.ExampleTestBook;
import io.github.asewhy.conversions.support.annotations.ResponseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ResponseDTO
public class ExampleTestBookInterfaceResponse extends ConversionResponse<ExampleTestBook> {
    private String name;
    private String isbin;
}
