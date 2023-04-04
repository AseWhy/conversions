package io.github.asewhy.conversions.config.converters.book;

import io.github.asewhy.conversions.ConversionResponse;
import io.github.asewhy.conversions.config.entities.book.ExampleTestPointEntity;
import io.github.asewhy.conversions.support.annotations.ResponseDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ResponseDTO
public class ExampleTestPointResponse extends ConversionResponse<ExampleTestPointEntity> {
    private Long x;
    private Long y;
}
