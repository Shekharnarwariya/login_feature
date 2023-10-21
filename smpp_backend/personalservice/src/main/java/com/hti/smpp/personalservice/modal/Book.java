package com.hti.smpp.personalservice.modal;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Book {
    private Integer id;
    private String name;
    private Integer pageCount;
    private Author author;
}
