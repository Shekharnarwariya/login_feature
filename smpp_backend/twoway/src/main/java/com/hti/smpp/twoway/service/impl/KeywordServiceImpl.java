package com.hti.smpp.twoway.service.impl;

import java.util.List;
import java.util.Optional;

import jakarta.persistence.Id;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hti.smpp.common.twoway.dto.KeywordEntry;
import com.hti.smpp.common.twoway.repository.KeywordEntryRepository;
import com.hti.smpp.twoway.exception.RecordNotFoundException;
import com.hti.smpp.twoway.service.KeywordService;

@Service
public class KeywordServiceImpl implements KeywordService{

    @Autowired KeywordEntryRepository keywordEntryRepository;

    @Override
    public List<KeywordEntry> list() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'list'");
    }

    @Override
    public Optional<KeywordEntry> getEntry(Integer id) {        
        Optional<KeywordEntry> keywordEntry = keywordEntryRepository.findById(id);
            if(!(keywordEntry.isPresent())){
                throw new RecordNotFoundException("Keyword Not Found");
            }
        return keywordEntry;  
    }

    @Override
    public void update(KeywordEntry entry) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'update'");
    }

    @Override
    public void delete(KeywordEntry entry) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'delete'");
    }

    @Override
    public void save(KeywordEntry entry) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }
    
}
