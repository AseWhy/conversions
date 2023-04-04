package io.github.asewhy.conversions.config.converters.people;

import io.github.asewhy.conversions.config.entities.people.NissanMicra;
import io.github.asewhy.conversions.support.annotations.MutatorDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@MutatorDTO
@ToString
public class MutatorNissanMicra extends MutatorCar<NissanMicra> {
    public String micraSpecificProperty;
}
