package com.hti.smpp.common.addressbook.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.addressbook.request.ContactEntryRequest;
import com.hti.smpp.common.addressbook.request.GroupDataEntryRequest;
import com.hti.smpp.common.addressbook.request.GroupEntryRequest;
import com.hti.smpp.common.addressbook.services.ContactEntryService;
import com.hti.smpp.common.addressbook.services.GroupDataEntryService;
import com.hti.smpp.common.addressbook.services.GroupEntryService;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@OpenAPIDefinition(info = @Info(title = "SMPP AddressBook API", version = "1.0", description = "API for managing SMPP AddressBook"))
@RestController
@RequestMapping("/addressbook")
@Tag(name = "AddressBookController", description = "API's for address book")
public class AddressBookController {

	@Autowired
	private ContactEntryService contactEntryService;

	@Autowired
	private GroupDataEntryService groupDataEntryService;

	@Autowired
	private GroupEntryService entryService;

	/**
	 * This is a Smpp comment for a Save Contact Entry
	 * 
	 * @param contactFile         : contactFile data save in this file.
	 * @param contactEntryRequest : The JSON string representing the contact entry
	 *                            request.save the data in contactEntryRequest.
	 * @param username            : user data save in username.
	 * @return :
	 */
	@Operation(summary = "Save Contact Entry", description = "Save a new contact entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "ContactEntry Saved Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/save/contact-entry", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> saveContactEntry(
			@RequestPart(value = "contactFile", required = true) MultipartFile contactFile,
			@Parameter(description = "Contact Entry request", content = @Content(schema = @Schema(implementation = ContactEntryRequest.class))) @RequestParam(value = "contactEntryRequest", required = true) String contactEntryRequest,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {

		return this.contactEntryService.saveContactEntry(contactEntryRequest, contactFile, username);

	}

	/**
	 * This endpoint is designed to save group data entries.
	 * 
	 * @param contactNumberFile     : contactFile data save in contactNumberFile.
	 * @param groupDataEntryRequest : The JSON string representing the contact entry
	 *                              request.save the data in groupDataEntryRequest
	 * @param username              : user data save in username.
	 * @return : ResponseEntity indicating the success or failure of the save
	 *         operation.
	 */

	@Operation(summary = "Save Group Data Entry", description = "Save a new group data entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "GroupDataEntry Saved Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
	@PostMapping(value = "/save/group-data-entry", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<?> saveGroupDataEntry(
			@RequestPart(value = "contactNumberFile", required = true) MultipartFile contactNumberFile,
			@Parameter(description = "Group Data Entry request", content = @Content(schema = @Schema(implementation = GroupDataEntryRequest.class))) @RequestParam("groupDataEntryRequest") String groupDataEntryRequest,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {

		return this.groupDataEntryService.saveGroupData(groupDataEntryRequest, contactNumberFile, username);
	}

	/**
	 * 
	 * This endpoint is designed to save GroupEntry.
	 * 
	 * @param entryRequest : The request body containing details for the new group
	 *                     entry.
	 * @param username     : user data save in username.
	 * @return
	 */

	@Operation(summary = "Save Group Entry", description = "Save a new group entry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "GroupEntry Saved Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
	@PostMapping("/save/group-entry")
	public ResponseEntity<?> saveGroupEntry(@Valid @RequestBody GroupEntryRequest entryRequest,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.entryService.saveGroupEntry(entryRequest, username);
	}

	/**
	 * Retrieves contact information for bulk processing.
	 * 
	 * @param numbers  : List of numbers.
	 * @param groupId  : Identifier for the group.
	 * @param username : Username of the requester.
	 * @return : ResponseEntity indicating the success or failure of the save
	 *         operation.
	 */

	@Operation(summary = "Contact for bulk", description = "Gives the Contact For Bulk Response")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful response ContactForBulk"),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), })
	@GetMapping("/get/contact-for-bulk")
	public ResponseEntity<?> contactForBulk(
			@Parameter(description = "List of Numbers") @RequestParam(value = "numbers", required = true) List<Long> numbers,
			@Parameter(description = "Group Id") @RequestParam(value = "groupId", required = true) int groupId,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.contactEntryService.contactForBulk(numbers, groupId, username);
	}

	/**
	 * Retrieves GroupData information for bulk processing
	 * 
	 * @param numbers  : List of numbers.
	 * @param groupId  : Identifier for the group.
	 * @param username : Username of the requester.
	 * @return :
	 */

	@Operation(summary = "GroupData for Bulk", description = "Gives the Contact For Bulk Response")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful response ContactForBulk"),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway. Unable to Process Request", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))), })
	@GetMapping("/get/groupdata-for-bulk")
	public ResponseEntity<?> groupDataForBulk(
			@Parameter(description = "List of Numbers") @RequestParam(value = "numbers", required = true) List<Long> numbers,
			@Parameter(description = "Group Id") @RequestParam(value = "groupId", required = true) int groupId,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.groupDataEntryService.groupDataForBulk(numbers, groupId, username);

	}

	/**
	 * Retrieves Search Contact information.
	 * 
	 * @param ids      : List of Id.
	 * @param username : Username of the requester.
	 * @return : ResponseEntity containing the ContactForBulk response.
	 */

	@Operation(summary = "View Search Contact", description = "Returns the list of ContactEntry")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful response ContactEntry list"),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
	@GetMapping("/get/view-search-contact")
	public ResponseEntity<?> viewSearchContact(
			@Parameter(description = "List of Id's") @RequestParam(value = "ids", required = true) List<Integer> ids,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.contactEntryService.viewSearchContact(ids, username);
	}

	/**
	 * Retrieves contact information for bulk processing.
	 * 
	 * @param ids      : List of Id.
	 * @param username : Username of the requester.
	 * @return : ContactForBulk response.
	 */

	@Operation(summary = "Proceed Search Contact", description = "Gives the ContactForBulk Response")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful response of ContactForBulk"),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
	@GetMapping("/get/proceed-search-contact")
	public ResponseEntity<?> proceedSearchContact(
			@Parameter(description = "List of Id's") @RequestParam(value = "ids", required = true) List<Integer> ids,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.contactEntryService.proceedSearchContact(ids, username);
	}

	/**
	 * Performs a search for GroupDataEntries using a POST request.
	 * 
	 * @param request  : Search criteria for GroupDataEntries.
	 * @param username : Username of the requester.
	 * @return : ResponseEntity with the result of the search.
	 */

	@Operation(summary = "Search GroupData", description = "Returns the list of GroupDataEntry")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful response GroupDataEntry list"),
			@ApiResponse(responseCode = "404", description = "Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
	@PostMapping("/get/view-search-groupdata")
	public ResponseEntity<?> viewSearchGroupData(@Valid @RequestBody GroupDataEntryRequest request,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.groupDataEntryService.viewSearchGroupData(request, username);
	}

	/**
	 * Performs a Search GroupData using a POST request.
	 * 
	 * @param request  : Search criteria for GroupDataEntries
	 * @param username : Username of the requester.
	 * @return : ResponseEntity with the result of the search
	 */
	@Operation(summary = "Proceed Search GroupData", description = "Gives the ContactForBulk Response")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successful response of ContactForBulk"),
			@ApiResponse(responseCode = "404", description = "Content Not Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
	@PostMapping("/get/proceed-search-groupdata")
	public ResponseEntity<?> proceedSearchGroupData(@Valid @RequestBody GroupDataEntryRequest request,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.groupDataEntryService.proceedSearchGroupData(request, username);

	}

	/**
	 * Updates a ContactEntry using a PUT request.
	 * 
	 * @param request  : Request containing data for updating the ContactEntry.
	 * @param username : Username of the requester.
	 * @return : ResponseEntity indicating the success or failure of the update
	 *         operation.
	 */

	@Operation(summary = "Update ContactEntry", description = "To update the ContactEntry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "ContactEntry Updated Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "No Content Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
	@PutMapping("/update/contact")
	public ResponseEntity<?> modifyContactUpdate(@Valid @RequestBody ContactEntryRequest request,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.contactEntryService.modifyContactUpdate(request, username);
	}

	/**
	 * Delete a ContactEntry using a Delete request.
	 * 
	 * @param ids      : List of Id to be deleted.
	 * @param username : Username of the requester.
	 * @return : ResponseEntity indicating the success or failure of the delete
	 *         operation.
	 */
	@Operation(summary = "Delete ContactEntry", description = "To delete the ContactEntry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "ContactEntry Deleted Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
	@DeleteMapping("/delete/contact")
	public ResponseEntity<?> modifyContactDelete(
			@Parameter(description = "List of Id's") @RequestParam(value = "ids", required = true) List<Integer> ids,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.contactEntryService.modifyContactDelete(ids, username);
	}

	/**
	 * Exports a ContactEntry using a POST request.
	 * 
	 * @param request  : Search criteria for exporting ContactEntry.
	 * @param username : Username of the requester.
	 * @return : ResponseEntity indicating the success or failure of the post
	 *         operation.
	 */

	@Operation(summary = "Export ContactEntry", description = "To export the ContactEntry")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "ContactEntry Exported Successfully."),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "No Content Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
	@PostMapping("/export/contact")
	public ResponseEntity<?> modifyContactExport(@Valid @RequestBody ContactEntryRequest request,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.contactEntryService.modifyContactExport(request, username);
	}

	/**
	 * Updates a GroupDataEntry using a PUT request.
	 * 
	 * @param request  : Request containing data for updating the GroupDataEntry
	 * @param username : Username of the requester.
	 * @return : ResponseEntity indicating the success or failure of the Put
	 *         operation.
	 */
	@Operation(summary = "Update GroupDataEntry", description = "To update the GroupDataEntry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "GroupDataEntry Updated Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "No Content Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
	@PutMapping("/update/group-data-entry")
	public ResponseEntity<?> modifyGroupDataUpdate(@Valid @RequestBody GroupDataEntryRequest request,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.groupDataEntryService.modifyGroupDataUpdate(request, username);
	}

	/**
	 * Delete a GroupDataEntry using a Delete request.
	 * 
	 * @param ids      : List of Id to be deleted.
	 * @param username : Username of the requester.
	 * @return : ResponseEntity indicating the success or failure of the Delete
	 *         operation.
	 */
	@Operation(summary = "Delete GroupDataEntry", description = "To delete the GroupDataEntry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "GroupDataEntry Deleted Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "No Content Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
	@DeleteMapping("/delete/group-data-entry")
	public ResponseEntity<?> modifyGroupDataDelete(
			@Parameter(description = "List of Id's") @RequestParam(value = "ids", required = true) List<Integer> ids,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.groupDataEntryService.modifyGroupDataDelete(ids, username);
	}

	/**
	 * Exports a ContactEntry using a POST request.
	 * 
	 * @param request  : criteria for exporting ContactEntry.
	 * @param username : Username of the requester.
	 * @return : ResponseEntity indicating the success or failure of the Post
	 *         operation.
	 */
	@Operation(summary = "Export ContactEntry", description = "To export the ContactEntry")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "GroupDataEntry Exported Successfully."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "No Content Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
	@PostMapping("/export/group-data-entry")
	public ResponseEntity<?> modifyGroupDataExport(@Valid @RequestBody GroupDataEntryRequest request,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.groupDataEntryService.modifyGroupDataExport(request, username);
	}

	/**
	 * Updates a GroupEntry using a PUT request.
	 * 
	 * @param groupEntryRequest : Request containing data for updating the
	 *                          GroupEntry
	 * @param username          : Username of the requester.
	 * @return : ResponseEntity indicating the success or failure of the Put
	 *         operation.
	 */
	@Operation(summary = "Update GroupEntry", description = "To update the GroupEntry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "201", description = "GroupEntry Updated Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "No Records Found To Update.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
	@PutMapping("/update/group-entry")
	public ResponseEntity<?> modifyGroupEntryUpdate(@Valid @RequestBody GroupEntryRequest groupEntryRequest,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.entryService.modifyGroupEntryUpdate(groupEntryRequest, username);
	}

	/**
	 * Delete a GroupEntry using a Delete request.
	 * 
	 * @param groupEntryRequest : Request containing data for Delete the GroupEntry
	 * @param username          : Username of the requester.
	 * @return : ResponseEntity indicating the success or failure of the Delete
	 *         operation.
	 */
	@Operation(summary = "Delete GroupEntry", description = "To delete the GroupEntry")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "GroupEntry Deleted Successfully.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "No Records Found To Delete.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
	@DeleteMapping("/delete/group-entry")
	public ResponseEntity<?> modifyGroupEntryDelete(@Valid @RequestBody GroupEntryRequest groupEntryRequest,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.entryService.modifyGroupEntryDelete(groupEntryRequest, username);
	}

	/**
	 * Retrieves the response of GroupDataSearch for a specified Group Id..
	 * 
	 * @param groupId  : The Group Id for which GroupDataSearch is performed.
	 * @param username : sername of the requester.
	 * @return : ResponseEntity containing the GroupDataSearch response.
	 */
	@Operation(summary = "GroupData Search", description = "To get the response of GroupDataSearch")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "GroupDataSearch Response."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway."),
			@ApiResponse(responseCode = "404", description = "No Records Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))) })
	@GetMapping("/search/group-data/{groupId}")
	public ResponseEntity<?> editGroupDataSearch(
			@Parameter(description = "Group Id") @PathVariable(value = "groupId", required = true) int groupId,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.groupDataEntryService.editGroupDataSearch(groupId, username);
	}

	/**
	 * Retrieves the response of ListGroup based on specified parameters.
	 * 
	 * @param purpose   : The purpose for listing groups.
	 * @param groupData : The data related to the groups
	 * @param username  : Username of the requester.
	 * @return : ResponseEntity containing the ListGroup response.
	 */
	@Operation(summary = "ListGroup", description = "To get response of ListGroup")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "ListGroup Response."),
			@ApiResponse(responseCode = "500", description = "Internal Server Error.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "404", description = "No Records Found.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "401", description = "Unauthorized User.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
			@ApiResponse(responseCode = "502", description = "Bad Gateway.") })
	@GetMapping("/listgroup")
	public ResponseEntity<?> listGroup(
			@Parameter(description = "Purpose") @RequestParam(value = "purpose", required = true) String purpose,
			@Parameter(description = "GroupData") @RequestParam(value = "groupData", required = true) String groupData,
			@Parameter(description = "Username in header") @RequestHeader(value = "username", required = true) String username) {
		return this.entryService.listGroup(purpose, groupData, username);
	}

}
