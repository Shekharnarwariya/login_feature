package com.hti.smpp.common.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.hlr.dto.HlrSmscEntry;
import com.hti.smpp.common.request.HlrSmscEntryRequest;
import com.hti.smpp.common.service.HlrSmscService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/smsc")
public class HlrSmscController {

	private final HlrSmscService hlrSmscService;

	@Autowired
	public HlrSmscController(HlrSmscService hlrSmscService) {
		this.hlrSmscService = hlrSmscService;
	}

	@PostMapping
	public ResponseEntity<?> saveHlrSmscEntry(@Valid @RequestBody HlrSmscEntryRequest hlrSmscEntryRequest,
			@RequestHeader("username") String username) {
		return ResponseEntity.ok(hlrSmscService.save(hlrSmscEntryRequest, username));
	}

	@PutMapping("/{id}")
	public ResponseEntity<?> updateHlrSmscEntry(@PathVariable int id,
			@Valid @RequestBody HlrSmscEntryRequest hlrSmscEntryRequest, @RequestHeader("username") String username) {
		return ResponseEntity.ok(hlrSmscService.update(id, hlrSmscEntryRequest, username));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteHlrSmscEntry(@PathVariable int id, @RequestHeader("username") String username) {
		hlrSmscService.delete(id, username);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/{id}")
	public ResponseEntity<HlrSmscEntry> getHlrSmscEntry(@PathVariable int id,
			@RequestHeader("username") String username) {

		return hlrSmscService.getEntry(id, username);
	}

	@GetMapping
	public ResponseEntity<List<HlrSmscEntry>> listHlrSmscEntries(@RequestHeader("username") String username) {
		return ResponseEntity.ok(hlrSmscService.list(username));
	}
}
