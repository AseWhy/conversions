package io.github.asewhy.conversions.config.converters.people;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import io.github.asewhy.conversions.ConversionMutator;
import io.github.asewhy.conversions.config.entities.people.Car;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
@JsonSubTypes(
    {
        @JsonSubTypes.Type(value = MutatorNissanMicra.class, name = "woman"),
        @JsonSubTypes.Type(value = MutatorPorsche911.class, name = "man")
    }
)
public class MutatorCar<T extends Car> extends ConversionMutator<T> {

}
