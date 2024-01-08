package com.hti.smpp.common.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hti.smpp.common.messages.dto.BulkEntry;
import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.DeliveryDTO;
import com.hti.smpp.common.response.TrackResultResponse;
import com.hti.smpp.common.service.ReportService;
import com.hti.smpp.common.service.TrackResultService;
import com.hti.smpp.common.service.UserDeliveryReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JasperPrint;

@RestController
@RequestMapping("/reports")
@Tag(name = "Reports", description = "APIs for generating and retrieving reports")
public class ReportController {

	@Autowired
	private ReportService reportService;

	@Autowired
	private UserDeliveryReportService deliveryService;

	@Autowired
	private TrackResultService trackResultService;

	@GetMapping("/abort-batch")
	@Operation(summary = "Abort Batch Report", description = "Abort a batch report")
	public List<BulkEntry> abortBatchReport(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm) {
		return reportService.abortBatchReport(username, customReportForm);
	}

	@GetMapping("/balance-report")
	@Operation(summary = "Balance Report View", description = "View balance report")
	public Map<String, List<DeliveryDTO>> balanceReportView(
			@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm) {
		return reportService.BalanceReportView(username, customReportForm);
	}

	@GetMapping("/balance-report-xls")
	@Operation(summary = "Balance Report XLS", description = "Generate balance report in XLS format")
	public String balanceReportXls(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			HttpServletResponse response) {
		return reportService.BalanceReportxls(username, customReportForm, response);
	}

	@GetMapping("/balance-report-pdf")
	@Operation(summary = "Balance Report PDF", description = "Generate balance report in PDF format")
	public String balanceReportPdf(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			HttpServletResponse response) {
		return reportService.balanceReportPdf(username, customReportForm, response);
	}

	@GetMapping("/balance-report-doc")
	@Operation(summary = "Balance Report DOC", description = "Generate balance report in DOC format")
	public String balanceReportDoc(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			HttpServletResponse response) {
		return reportService.BalanceReportDoc(username, customReportForm, response);
	}

	@GetMapping("/blocked-report")
	@Operation(summary = "Blocked Report View", description = "View blocked report")
	public List<DeliveryDTO> blockedReportView(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @org.springframework.web.bind.annotation.RequestBody CustomReportForm customReportForm) {
		return reportService.BlockedReportView(username, customReportForm);
	}

	@GetMapping("/blocked-report-xls")
	@Operation(summary = "Blocked Report XLS", description = "Generate blocked report in XLS format")
	public String blockedReportXls(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			HttpServletResponse response) {
		return reportService.BlockedReportxls(username, customReportForm, response);
	}

	@GetMapping("/blocked-report-pdf")
	@Operation(summary = "Blocked Report PDF", description = "Generate blocked report in PDF format")
	public String blockedReportPdf(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			HttpServletResponse response) {
		return reportService.BlockedReportPdf(username, customReportForm, response);
	}

	@GetMapping("/blocked-report-doc")
	@Operation(summary = "Blocked Report DOC", description = "Generate blocked report in DOC format")
	public String blockedReportDoc(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			HttpServletResponse response) {
		return reportService.BlockedReportDoc(username, customReportForm, response);
	}

	@GetMapping("/campaign-report-view")
	@Operation(summary = "View Campaign Report", description = "Generates and returns the Campaign Report for viewing")
	public JasperPrint campaignReportview(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			HttpServletResponse response) {
		return reportService.CampaignReportview(username, customReportForm);
	}

	@GetMapping("/campaign-report-doc")
	@Operation(summary = "Doc Campaign Report", description = "Generates and returns the Campaign Report DOC format")
	public JasperPrint campaignReportdoc(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			HttpServletResponse response) {
		return reportService.CampaignReportDoc(username, customReportForm, response);
	}

	@GetMapping("/campaign-report-xls")
	@Operation(summary = "Campaign Report XLS", description = "Generate Campaign report in XLS format")
	public JasperPrint campaignReportxls(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			HttpServletResponse response) {
		return reportService.CampaignReportxls(username, customReportForm, response);
	}

	@GetMapping("/campaign-report-pdf")
	@Operation(summary = "Campaign Report PDF", description = "Generate Campaign report in PDF format")
	public JasperPrint campaignReportPdf(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			HttpServletResponse response) {
		return reportService.CampaignReportPdf(username, customReportForm, response);
	}

	@GetMapping("/content-report")
	public List<DeliveryDTO> contentReportView(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm) {
		return reportService.ContentReportView(username, customReportForm);
	}

	@GetMapping("/content-report-xls")
	@Operation(summary = "content Report PDF", description = "Generate content report in PDF format")
	public List<DeliveryDTO> contentReportxls(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			HttpServletResponse response) {
		return reportService.ContentReportxls(username, customReportForm, response);
	}

	@GetMapping("/content-report-pdf")
	@Operation(summary = "content Report PDF", description = "Generate content report in PDF format")
	public List<DeliveryDTO> contentReportPdf(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			HttpServletResponse response) {
		return reportService.ContentReportPdf(username, customReportForm, response);
	}

	@GetMapping("/content-report-Doc")
	@Operation(summary = "content Report Doc", description = "Generate content report in Doc format")
	public List<DeliveryDTO> contentReportDoc(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			HttpServletResponse response) {
		return reportService.ContentReportDoc(username, customReportForm, response);
	}

	@Operation(summary = "Get User Delivery Report View")
	@PostMapping("/report/view")
	public ResponseEntity<List<DeliveryDTO>> userDeliveryReportView(@RequestParam String username,
			@RequestBody CustomReportForm customReportForm) {
		List<DeliveryDTO> report = deliveryService.UserDeliveryReportView(username, customReportForm);
		return ResponseEntity.ok(report);
	}

	@Operation(summary = "Download User Delivery Report as XLS")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully downloaded XLS file"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	@GetMapping("/report/xls")
	public ResponseEntity<String> userDeliveryReportXls(@RequestParam String username,
			@RequestBody CustomReportForm customReportForm, HttpServletResponse response) {
		String filePath = deliveryService.UserDeliveryReportxls(username, customReportForm, response);
		return ResponseEntity.ok(filePath);
	}

	@Operation(summary = "Download User Delivery Report as PDF")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully downloaded PDF file"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	@GetMapping("/report/pdf")
	public ResponseEntity<String> userDeliveryReportPdf(@RequestParam String username,
			@RequestBody CustomReportForm customReportForm, HttpServletResponse response) {
		String filePath = deliveryService.UserDeliveryReportPdf(username, customReportForm, response);
		return ResponseEntity.ok(filePath);
	}

	@Operation(summary = "Download User Delivery Report as DOC")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully downloaded DOC file"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	@GetMapping("/report/doc")
	public ResponseEntity<String> userDeliveryReportDoc(@RequestParam String username,
			@RequestBody CustomReportForm customReportForm, HttpServletResponse response) {
		String filePath = deliveryService.UserDeliveryReportDoc(username, customReportForm, response);
		return ResponseEntity.ok(filePath);
	}

	@Operation(summary = "Get Track Result Report")
	@PostMapping("/report/track-result")
	public ResponseEntity<TrackResultResponse> trackResultReport(@RequestParam String username,
			@RequestBody CustomReportForm customReportForm) {
		TrackResultResponse result = trackResultService.TrackResultReport(username, customReportForm);
		return ResponseEntity.ok(result);
	}

}
