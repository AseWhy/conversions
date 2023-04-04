package io.github.asewhy.conversions.config.entities.people;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NissanMicra extends Car {
    public String micraSpecificProperty;

    public NissanMicra() {
        super("Nissan Micra");
    }
}
