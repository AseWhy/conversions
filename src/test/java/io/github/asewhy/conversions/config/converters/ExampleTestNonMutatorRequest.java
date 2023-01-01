package io.github.asewhy.conversions.config.converters;

import io.github.asewhy.conversions.support.annotations.MutatorDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ExampleTestNonMutatorRequest {
    public Long externalData1;
    public Long externalData2;
    public ExampleTestMutatorRequest request;
}
