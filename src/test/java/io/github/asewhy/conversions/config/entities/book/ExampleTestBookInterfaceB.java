package io.github.asewhy.conversions.config.entities.book;

import io.github.asewhy.conversions.config.support.ExampleTestBook;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ExampleTestBookInterfaceB implements ExampleTestBook {
    public String name;
    public String isbin;
    public Integer pageCount;
}
