package io.github.asewhy.conversions.config.converters.book;

import io.github.asewhy.conversions.ConversionResponse;
import io.github.asewhy.conversions.config.entities.book.ExampleTestAuthorEntity;
import io.github.asewhy.conversions.support.annotations.ResponseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
@ResponseDTO
public class ExampleTestAuthorResponse extends ConversionResponse<ExampleTestAuthorEntity> {
    private String name;
    private LocalDate birthDate;
    private ExampleTestPointResponse location;
}
