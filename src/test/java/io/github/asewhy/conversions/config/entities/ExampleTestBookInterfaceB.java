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
public class ExampleTestBookInterfaceB implements IExampleTestBook {
    public String name;
    public String isbin;
    public Integer pageCount;
}
