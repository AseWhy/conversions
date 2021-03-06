package io.github.asewhy.conversions.config.converters;

import io.github.asewhy.conversions.ConversionResponse;
import io.github.asewhy.conversions.config.entities.ExampleTestAuthorEntity;
import io.github.asewhy.conversions.support.annotations.ResponseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@ResponseDTO
public class ExampleTestAuthorResponseConverter extends ConversionResponse<ExampleTestAuthorEntity> {
    private String name;
    private LocalDate birthDate;
    private ExampleTestPointResponseConverter location;
}
