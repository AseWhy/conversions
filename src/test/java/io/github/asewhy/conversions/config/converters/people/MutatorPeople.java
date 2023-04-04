package io.github.asewhy.conversions.config.converters.people;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.github.asewhy.conversions.ConversionMutator;
import io.github.asewhy.conversions.config.entities.people.Car;
import io.github.asewhy.conversions.config.entities.people.People;
import io.github.asewhy.conversions.support.annotations.IgnoreMatch;
import io.github.asewhy.conversions.support.annotations.MutatorDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@MutatorDTO
@ToString
public class MutatorPeople extends ConversionMutator<People> {
    private String gender;
    private int age;

    @IgnoreMatch
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "gender")
    private MutatorCar<Car> car;
}
