package io.github.asewhy.conversions.config.converters;

import io.github.asewhy.conversions.ConversionResponse;
import io.github.asewhy.conversions.support.annotations.ResponseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

@Getter
@Setter
@ToString
@ResponseDTO(mapping = "some_mapping")
public class ExampleTestMapResponseConverter extends ConversionResponse<Map<String, String>> {
    private String name;
    private String isbin;
}
