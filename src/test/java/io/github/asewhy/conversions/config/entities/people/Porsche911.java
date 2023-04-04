package io.github.asewhy.conversions.config.entities.people;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Porsche911 extends Car {
    public String porscheSpecificProperty;

    public Porsche911() {
        super("Porsche 911");
    }
}
