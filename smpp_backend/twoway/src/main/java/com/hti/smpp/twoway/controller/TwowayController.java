package com.hti.smpp.twoway.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import com.hti.smpp.common.twoway.dto.KeywordEntry;
import com.hti.smpp.twoway.service.KeywordService;

@Controller
public class TwowayController {
    
    @Autowired KeywordService keywordService;

    @QueryMapping
    public Optional<KeywordEntry> viewKeyword(@Argument Integer id) {
        return keywordService.getEntry(id);
    }

}
