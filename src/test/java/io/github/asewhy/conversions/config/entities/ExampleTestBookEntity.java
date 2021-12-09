package io.github.asewhy.conversions.config.entities;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
public class ExampleTestBookEntity {
    private String name;
    private String isbin;
    private Long pageCount;
    private List<ExampleTestAuthorEntity> authors = new ArrayList<>();
}
