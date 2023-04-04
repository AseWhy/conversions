package io.github.asewhy.conversions.config.entities.book;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class ExampleTestAuthorEntity {
    private String name;
    private LocalDate birthDate;
    private ExampleTestPointEntity location;
}
