package io.github.asewhy.conversions.config.converters.people;

import io.github.asewhy.conversions.config.entities.people.Porsche911;
import io.github.asewhy.conversions.support.annotations.MutatorDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@MutatorDTO
@ToString
public class MutatorPorsche911 extends MutatorCar<Porsche911> {
    public String porscheSpecificProperty;
}
