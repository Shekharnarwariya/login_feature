package com.hti.smpp.personalservice.controller;

import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.hti.smpp.personalservice.modal.Author;
import com.hti.smpp.personalservice.modal.Book;

@Controller
public class PersonalController {

    @QueryMapping
    public String personalDetails(@Argument String id) {
        return id;
    }

     @QueryMapping
    public Book personalTemp(@Argument String id) {
        return new Book(1,"Umesh",100,new Author(1,"umesh","tomar"));
    }


}
