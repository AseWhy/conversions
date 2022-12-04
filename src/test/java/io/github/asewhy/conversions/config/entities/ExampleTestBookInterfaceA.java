package io.github.asewhy.conversions.config.entities;

import io.github.asewhy.conversions.config.support.IExampleTestBook;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ExampleTestBookInterfaceA implements IExampleTestBook {
    public String name;
    public String isbin;
    public String genre;
}
