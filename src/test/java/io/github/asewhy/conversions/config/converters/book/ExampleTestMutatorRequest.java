package io.github.asewhy.conversions.config.converters.book;

import io.github.asewhy.conversions.ConversionMutator;
import io.github.asewhy.conversions.config.entities.book.ExampleTestBookEntity;
import io.github.asewhy.conversions.support.annotations.MutatorDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@MutatorDTO
public class ExampleTestMutatorRequest extends ConversionMutator<ExampleTestBookEntity> {
    private String name;
    private String isbin;
    private Long pageCount;
}
