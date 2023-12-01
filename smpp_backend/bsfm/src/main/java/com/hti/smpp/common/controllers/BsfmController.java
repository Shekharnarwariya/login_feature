package com.hti.smpp.common.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.request.BsfmFilterFrom;
import com.hti.smpp.common.services.BsfmService;

@RestController
@RequestMapping("/bsfm")
public class BsfmController {

	@Autowired
	private BsfmService bsfmService;

	@PostMapping("/save")
	public ResponseEntity<?> saveBsfm(@RequestHeader("username") String username,
			@RequestBody BsfmFilterFrom bsfmFilterFrom) {
		try {
			this.bsfmService.addBsfmProfile(bsfmFilterFrom, username);
			return ResponseEntity.status(HttpStatus.CREATED).build();
		} catch (Exception e) {
			// Provide more information in the response body for debugging
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error saving Bsfm: " + e.getMessage());
		}
	}

//	@GetMapping("/showprofiles/{masterid}")
//	public ResponseEntity<?> showBsfmProfiles(@PathVariable("masterid") String masterid) {
//		List<Bsfm> profiles = this.bsfmService.showBsfmProfile(masterid);
//
//		if (profiles.isEmpty()) {
//			return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
//		} else {
//			return ResponseEntity.ok(profiles);
//		}
//	}
//
//	@PutMapping("/update")
//	public ResponseEntity<?> updateBsfmProfiles(@RequestHeader("username") String username, @RequestBody BsfmDto bsfm) {
//		try {
//			this.bsfmService.updateBsfmProfile(bsfm, username);
//			return ResponseEntity.status(HttpStatus.CREATED).build();
//		} catch (Exception e) {
//			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error updating Bsfm: " + e.getMessage());
//		}
//	}
//
//	@DeleteMapping("/delete/activeprofile/{profilename}")
//	public ResponseEntity<?> bsfmActiveProfileDelete(@PathVariable("profilename") String profilename) {
//		try {
//			this.bsfmService.deleteBsfmActiveProfile(profilename);
//			return ResponseEntity.status(HttpStatus.OK).build();
//		} catch (Exception e) {
//			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//					.body("Error deleting Bsfm active profile: " + e.getMessage());
//		}
//	}
//
//	@PutMapping("/update/flag")
//	public ResponseEntity<?> updateProfileFlag(@RequestParam("flag") String flag) {
//		try {
//			boolean isUpdated = this.bsfmService.updateBsfmProfileFlag(flag);
//			if (!isUpdated) {
//				return new ResponseEntity<>("Flag Not Updated.", HttpStatus.CONFLICT);
//			}
//			return new ResponseEntity<>("Flag Updated Successfully.", HttpStatus.OK);
//		} catch (Exception e) {
//			return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//					.body("Error updating Bsfm profile flag: " + e.getMessage());
//		}
//	}
}