package com.hti.smpp.twoway.service;

import java.util.List;
import java.util.Optional;

import com.hti.smpp.common.twoway.dto.KeywordEntry;

public interface KeywordService {
    public List<KeywordEntry> list();

	public Optional<KeywordEntry> getEntry(Integer id);

	public void update(KeywordEntry entry);

	public void delete(KeywordEntry entry);

	public void save(KeywordEntry entry) ;//throws DuplicateEntryException;
}
