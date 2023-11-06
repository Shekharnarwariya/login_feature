package com.hti.smpp.common.service;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.hti.smpp.common.hlr.dto.HlrSmscEntry;
import com.hti.smpp.common.request.HlrSmscEntryRequest;

public interface HlrSmscService {

	public ResponseEntity<?> save(HlrSmscEntryRequest hlrSmscEntryRequest, String username);

	public ResponseEntity<?> update(int id, HlrSmscEntryRequest hlrSmscEntryRequest, String username);

	public ResponseEntity<?> delete(int id, String username);

	public ResponseEntity<HlrSmscEntry> getEntry(int id, String username);

	public List<HlrSmscEntry> list(String username);
}
