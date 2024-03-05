package com.hti.smpp.common.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.messages.dto.BulkEntry;
import com.hti.smpp.common.request.AbortBatchReportRequest;
import com.hti.smpp.common.request.BalanceReportRequest;
import com.hti.smpp.common.request.BlockedReportRequest;
import com.hti.smpp.common.request.CampaignReportRequest;
import com.hti.smpp.common.request.ContentReportRequest;
import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.request.CustomizedReportRequest;
import com.hti.smpp.common.request.DashboardRequest;
import com.hti.smpp.common.request.DlrSummaryReport;
import com.hti.smpp.common.request.LetencyReportRequest;
import com.hti.smpp.common.request.LookUpReportRequest;
import com.hti.smpp.common.request.PerformanceReportRequest;
import com.hti.smpp.common.request.ProfitReportRequest;
import com.hti.smpp.common.request.ScheduleReportRequest;
import com.hti.smpp.common.request.SendAttachmentRequest;
import com.hti.smpp.common.request.SmsReportRequest;
import com.hti.smpp.common.request.SmscDlrReportRequest;
import com.hti.smpp.common.request.SubmissionReportRequest;
import com.hti.smpp.common.request.SummaryReportForm;
import com.hti.smpp.common.request.UserDeliveryForm;
import com.hti.smpp.common.response.TrackResultResponse;
import com.hti.smpp.common.service.AbortBatchReportService;
import com.hti.smpp.common.service.CampaignReportService;
import com.hti.smpp.common.service.ContentReportService;
import com.hti.smpp.common.service.CustomizedReportService;
import com.hti.smpp.common.service.DashboardService;
import com.hti.smpp.common.service.DlrSummaryReportService;
import com.hti.smpp.common.service.FileAttachmentSenderService;
import com.hti.smpp.common.service.LatencyReportService;
import com.hti.smpp.common.service.LookupReportService;
import com.hti.smpp.common.service.PerformanceReportService;
import com.hti.smpp.common.service.ProfitReportService;
import com.hti.smpp.common.service.ReportService;
import com.hti.smpp.common.service.ScheduleReportService;
import com.hti.smpp.common.service.SmscDlrReportReportService;
import com.hti.smpp.common.service.SubmissionReportService;
import com.hti.smpp.common.service.SummaryReportService;
import com.hti.smpp.common.service.TrackResultService;
import com.hti.smpp.common.service.TransactionReportService;
import com.hti.smpp.common.service.UserDeliveryReportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.ws.rs.HeaderParam;

@RestController
@RequestMapping("/reports")
@Tag(name = "Reports", description = "APIs for generating and retrieving reports")
public class ReportController {

	@Autowired
	private ReportService reportService;

	@Autowired
	private CampaignReportService campaignReportService;

	@Autowired
	private UserDeliveryReportService deliveryService;

	@Autowired
	private TrackResultService trackResultService;

	@Autowired
	private AbortBatchReportService abortBatchReportService;

	@Autowired
	private LookupReportService lookupReportService;

	@Autowired
	private ContentReportService contentReportService;
	@Autowired
	private TransactionReportService transactionReportService;

	@Autowired
	private CustomizedReportService customizedReportService;

	@Autowired
	private PerformanceReportService performanceReportService;

	@Autowired
	private DlrSummaryReportService dlrSummaryReportService;

	@Autowired
	private LatencyReportService latencyReportService;

	@Autowired
	private ProfitReportService profitReportService;

	@Autowired
	private ScheduleReportService scheduleReportService;

	@Autowired
	private SmscDlrReportReportService smscDlrReportService;

	@Autowired
	private SubmissionReportService submissionReportService;

	@Autowired
	private SummaryReportService summaryReportService;

	@Autowired
	private DashboardService dashboardService;

	@Autowired
	private FileAttachmentSenderService fileAttachmentSenderService;

	@PostMapping("/dashboard")
	@Operation(summary = "dashboard post Report", description = "dashboard post report")
	public ResponseEntity<?> processRequest(@Valid @Parameter(description = "Username") @RequestHeader String username,
			@RequestBody DashboardRequest request) {
		return dashboardService.processRequest(request, username);
	}

	@PostMapping("/abort-batch")
	@Operation(summary = "Abort Batch Report", description = "Abort a batch report")
	public List<BulkEntry> abortBatchReport(@Valid @Parameter(description = "Username") @RequestHeader String username,
			@Parameter(description = "Custom Report Form") @RequestBody AbortBatchReportRequest customReportForm) {
		return abortBatchReportService.abortBatchReport(username, customReportForm);
	}

	@PostMapping("/balance-report-view")
	@Operation(summary = "Balance Report View", description = "View balance report")
	public ResponseEntity<?> balanceReportView(
			@Valid @Parameter(description = "Username") @RequestHeader String username,
			@Parameter(description = "Custom Report Form") @RequestBody BalanceReportRequest customReportForm) {
		return reportService.BalanceReportView(username, customReportForm);
	}

	@PostMapping("/balance-report-xls")
	@Operation(summary = "Balance Report XLS", description = "Generate balance report in XLS format")
	public ResponseEntity<?> balanceReportXls(@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody BalanceReportRequest customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return reportService.BalanceReportxls(username, customReportForm, response);
	}

	@PostMapping("/balance-report-pdf")
	@Operation(summary = "Balance Report PDF", description = "Generate balance report in PDF format")
	public ResponseEntity<?> balanceReportPdf(@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody BalanceReportRequest customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return reportService.balanceReportPdf(username, customReportForm, response);
	}

	@PostMapping("/balance-report-doc")
	@Operation(summary = "Balance Report DOC", description = "Generate balance report in DOC format")
	public ResponseEntity<?> balanceReportDoc(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody BalanceReportRequest customReportForm,
			HttpServletResponse response) {
		return reportService.BalanceReportDoc(username, customReportForm, response);
	}

/////////////////////Blocked
	@PostMapping("/blocked-report")
	@Operation(summary = "Blocked Report View", description = "View blocked report")
	public ResponseEntity<?> blockedReportView(
			@Valid @Parameter(description = "Username") @RequestHeader String username,
			@Parameter(description = "Custom Report Form") @RequestBody BlockedReportRequest customReportForm) {
		return reportService.BlockedReportView(username, customReportForm);
	}

	@PostMapping("/blocked-report-xls")
	@Operation(summary = "Blocked Report XLS", description = "Generate blocked report in XLS format")
	public ResponseEntity<?> blockedReportXls(@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody BlockedReportRequest customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return reportService.BlockedReportxls(username, customReportForm, response);
	}

	@PostMapping("/blocked-report-pdf")
	@Operation(summary = "Blocked Report PDF", description = "Generate blocked report in PDF format")
	public ResponseEntity<?> blockedReportPdf(@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody BlockedReportRequest customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return reportService.BlockedReportPdf(username, customReportForm, response);
	}

	@PostMapping("/blocked-report-doc")
	@Operation(summary = "Blocked Report DOC", description = "Generate blocked report in DOC format")
	public ResponseEntity<?> blockedReportDoc(@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody BlockedReportRequest customReportForm,
			HttpServletResponse response) {
		return reportService.BlockedReportDoc(username, customReportForm, response);
	}

	@PostMapping("/campaign-report-view")
	@Operation(summary = "View Campaign Report", description = "Generates and returns the Campaign Report for viewing")
	public ResponseEntity<?> campaignReportview(
			@Valid @Parameter(description = "Username") @RequestHeader String username,
			@Parameter(description = "Custom Report Form") @RequestBody CampaignReportRequest customReportForm) {
		return campaignReportService.CampaignReportview(username, customReportForm);
	}

//	@PostMapping("/campaign-report-doc")
//	@Operation(summary = "Doc Campaign Report", description = "Generates and returns the Campaign Report DOC format")
//	public ResponseEntity<?> campaignReportdoc(
//			@Valid @Parameter(description = "Username") @RequestParam String username,
//			@Parameter(description = "Custom Report Form") @RequestBody CampaignReportRequest customReportForm,
//			HttpServletResponse response) {
//		return campaignReportService.CampaignReportDoc(username, customReportForm, response);
//	}

//	@PostMapping("/campaign-report-xls")
//	@Operation(summary = "Campaign Report XLS", description = "Generate Campaign report in XLS format")
//	public ResponseEntity<?> campaignReportxls(
//			@Valid @Parameter(description = "Username") @RequestParam String username,
//			@Parameter(description = "Custom Report Form") @RequestBody CampaignReportRequest customReportForm,
//			HttpServletResponse response) {
//		return campaignReportService.CampaignReportxls(username, customReportForm, response);
//	}

//	@PostMapping("/campaign-report-pdf")
//	@Operation(summary = "Campaign Report PDF", description = "Generate Campaign report in PDF format")
//	public ResponseEntity<?> campaignReportPdf(
//			@Valid @Parameter(description = "Username") @RequestParam String username,
//			@Parameter(description = "Custom Report Form") @RequestBody CampaignReportRequest customReportForm,
//			HttpServletResponse response) {
//		return campaignReportService.CampaignReportPdf(username, customReportForm, response);
//	}

///////////////
	@PostMapping("/content-report-view")
	@Operation(summary = "content Report view", description = "Generates and returns the Campaign Report for viewing")
	public ResponseEntity<?> contentReportView(
			@Valid @Parameter(description = "Username") @RequestHeader String username,
			@Parameter(description = "Custom Report Form") @RequestBody ContentReportRequest customReportForm) {
		return contentReportService.ContentReportView(username, customReportForm);
	}

	@PostMapping("/content-report-pdf")
	@Operation(summary = "content Report PDF", description = "Generate content report in PDF format")
	public ResponseEntity<?> contentReportPdf(@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody ContentReportRequest customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return contentReportService.ContentReportPdf(username, customReportForm, response);
	}

	@PostMapping("/content-report-Doc")
	@Operation(summary = "content Report Doc", description = "Generate content report in Doc format")
	public ResponseEntity<?> contentReportDoc(@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody ContentReportRequest customReportForm,
			HttpServletResponse response) {
		return contentReportService.ContentReportDoc(username, customReportForm, response);
	}

	@Operation(summary = " Download User Delivery Report View")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully downloaded DOC file"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	@PostMapping("/userDelivery-report-view")
	public ResponseEntity<?> userDeliveryReportView(@Valid @RequestHeader String username,
			@RequestBody UserDeliveryForm customReportForm) {
		return deliveryService.UserDeliveryReportView(username, customReportForm);
	}

	@Operation(summary = "Get Track Result Report")
	@PostMapping("/track-result-report")
	public ResponseEntity<TrackResultResponse> trackResultReport(@Valid @RequestParam String username,
			@RequestBody CustomReportForm customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang) {
		TrackResultResponse result = trackResultService.TrackResultReport(username, customReportForm, lang);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Get Lookup Report View")
	@PostMapping("/lookup-report-view")
	public ResponseEntity<?> getLookupReportView(@Valid @RequestHeader String username,
			@RequestBody LookUpReportRequest customReportForm) {
		return lookupReportService.LookupReportview(username, customReportForm);

	}

	@Operation(summary = "Download Lookup Report Excel")
	@PostMapping("/lookup-report-xls")
	public ResponseEntity<?> downloadLookupReportXLS(@Valid @RequestParam String username,
			@RequestBody LookUpReportRequest customReportForm, HttpServletResponse response) {
		return lookupReportService.LookupReportxls(username, customReportForm, response);

	}

	@Operation(summary = "Download Lookup Report PDF")
	@PostMapping("/lookup-report-pdf")
	public ResponseEntity<?> downloadLookupReportPDF(@Valid @RequestParam String username,
			@RequestBody LookUpReportRequest customReportForm, HttpServletResponse response) {
		return lookupReportService.LookupReportPdf(username, customReportForm, response);

	}

	@Operation(summary = "Download Lookup Report DOC")
	@PostMapping("/lookup-report-doc")
	public ResponseEntity<?> downloadLookupReportDoc(@Valid @RequestParam String username,
			@RequestBody LookUpReportRequest customReportForm, HttpServletResponse response) {
		return lookupReportService.LookupReportDoc(username, customReportForm, response);

	}

	@Operation(summary = "Recheck Lookup Report")
	@PostMapping("/recheck-report")
	public ResponseEntity<?> recheckLookupReport(@Valid @RequestParam String username,
			@RequestBody LookUpReportRequest customReportForm, HttpServletResponse response) {
		return lookupReportService.LookupReportRecheck(username, customReportForm, response);

	}

	@Operation(summary = "Get Customized Report View")
	@PostMapping("/customized-report-view")
	public ResponseEntity<?> getCustomizedReportView(@Valid @RequestHeader String username,
			@RequestBody CustomizedReportRequest customReportForm) {
		return customizedReportService.CustomizedReportView(username, customReportForm);
	}

	@GetMapping("/sms/report")
	public ResponseEntity<?> getSmsReport(@RequestHeader String username,
			@RequestBody SmsReportRequest smsReportRequest) {
		return customizedReportService.SmsReport(username, smsReportRequest);
	}

	@Operation(summary = "Download Customized Report DOC")
	@PostMapping("/customized-report-doc")
	public ResponseEntity<?> downloadCustomizedReportDoc(@Valid @RequestParam String username,
			@RequestBody CustomizedReportRequest customReportForm, @RequestParam String lang,
			HttpServletResponse response) {
		return customizedReportService.CustomizedReportdoc(username, customReportForm, response);

	}

	@Operation(summary = "Download Customized Report Excel")
	@PostMapping("/customized-report-xls")
	public ResponseEntity<String> downloadCustomizedReportXLS(@Valid @RequestParam String username,
			@RequestBody CustomizedReportRequest customReportForm, HttpServletResponse response) {
		String result = customizedReportService.CustomizedReportxls(username, customReportForm, response);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Download Customized Report PDF")
	@PostMapping("/customized-report-pdf")
	public ResponseEntity<?> downloadCustomizedReportPDF(@Valid @RequestParam String username,
			@RequestBody CustomizedReportRequest customReportForm, HttpServletResponse response) {
		return customizedReportService.CustomizedReportpdf(username, customReportForm, response);
	}

////////////////performance
	@PostMapping("/performance-report-view")
	@Operation(summary = "Performance Report View", description = "View performance report")
	public ResponseEntity<?> performanceReportView(
			@Valid @Parameter(description = "Username") @RequestHeader String username,
			@Parameter(description = "Custom Report Form") @RequestBody PerformanceReportRequest customReportForm) {
		return performanceReportService.PerformanceReportview(username, customReportForm);
	}

	@PostMapping("/performance-report-xls")
	@Operation(summary = "Download Performance Report as XLS", description = "Download performance report in XLS format")
	public ResponseEntity<?> downloadPerformanceReportXLS(
			@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody PerformanceReportRequest customReportForm,
			HttpServletResponse response) {
		return performanceReportService.PerformanceReportxls(username, customReportForm, response);
	}

	@PostMapping("/performance-report-pdf")
	@Operation(summary = "Download Performance Report as PDF", description = "Download performance report in PDF format")
	public ResponseEntity<?> downloadPerformanceReportPDF(
			@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody PerformanceReportRequest customReportForm,
			HttpServletResponse response) {
		return performanceReportService.PerformanceReportPdf(username, customReportForm, response);
	}

	@PostMapping("/performance-report-doc")
	@Operation(summary = "Download Performance Report as DOC", description = "Download performance report in DOC format")
	public ResponseEntity<?> downloadPerformanceReportDOC(
			@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody PerformanceReportRequest customReportForm,
			HttpServletResponse response) {
		return performanceReportService.PerformanceReportDoc(username, customReportForm, response);
	}

	@PostMapping("/dlr-summary-report-view")
	@Operation(summary = "DLR Summary Report View", description = "View DLR summary report")
	public ResponseEntity<?> dlrSummaryReportView(
			@Valid @Parameter(description = "Username") @RequestHeader String username,
			@Parameter(description = "Custom Report Form") @RequestBody DlrSummaryReport customReportForm) {
		return dlrSummaryReportService.DlrSummaryReportview(username, customReportForm);
	}

	@PostMapping("/dlr-summary-report-doc")
	@Operation(summary = "Download DLR Summary Report as DOC", description = "Download DLR summary report in DOC format")
	public ResponseEntity<?> downloadDlrSummaryReportDOC(
			@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody DlrSummaryReport customReportForm,
			HttpServletResponse response) {
		return dlrSummaryReportService.DlrSummaryReportdoc(username, customReportForm, response);
	}

	@PostMapping("/dlr-summary-report-pdf")
	@Operation(summary = "Download DLR Summary Report as PDF", description = "Download DLR summary report in PDF format")
	public ResponseEntity<?> downloadDlrSummaryReportPDF(
			@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody DlrSummaryReport customReportForm,
			HttpServletResponse response) {
		return dlrSummaryReportService.DlrSummaryReportdpdf(username, customReportForm, response);
	}

	@PostMapping("/dlr-summary-report-xls")
	@Operation(summary = "Download DLR Summary Report as XLS", description = "Download DLR summary report in XLS format")
	public ResponseEntity<?> downloadDlrSummaryReportXLS(
			@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody DlrSummaryReport customReportForm,
			HttpServletResponse response) {
		return dlrSummaryReportService.DlrSummaryReportdxls(username, customReportForm, response);
	}

///////////////////////latency/////////////
	@PostMapping("/latency-report-view")
	@Operation(summary = "Latency Report View", description = "View latency report")
	public ResponseEntity<?> latencyReportView(
			@Valid @Parameter(description = "Username") @RequestHeader String username,
			@Parameter(description = "Custom Report Form") @RequestBody LetencyReportRequest customReportForm) {
		return latencyReportService.LatencyReportView(username, customReportForm);
	}

	@PostMapping("/latency-report-xls")
	@Operation(summary = "Download Latency Report as XLS", description = "Download latency report in XLS format")
	public ResponseEntity<?> downloadLatencyReportXLS(
			@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody LetencyReportRequest customReportForm,
			HttpServletResponse response) {
		return latencyReportService.LatencyReportxls(username, customReportForm, response);
	}

	@PostMapping("/latency-report-pdf")
	@Operation(summary = "Download Latency Report as PDF", description = "Download latency report in PDF format")
	public ResponseEntity<?> downloadLatencyReportPDF(
			@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody LetencyReportRequest customReportForm,
			HttpServletResponse response) {
		return latencyReportService.LatencyReportpdf(username, customReportForm, response);
	}

	@PostMapping("/latency-report-doc")
	@Operation(summary = "Download Latency Report as DOC", description = "Download latency report in DOC format")
	public ResponseEntity<?> downloadLatencyReportDOC(
			@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody LetencyReportRequest customReportForm,
			HttpServletResponse response) {
		return latencyReportService.LatencyReportdoc(username, customReportForm, response);
	}

////////////////
	@PostMapping("/profit-report-view")
	@Operation(summary = "Profit Report View", description = "View profit report")
	public ResponseEntity<?> profitReportView(
			@Valid @Parameter(description = "Username") @RequestHeader String username,
			@Parameter(description = "Custom Report Form") @RequestBody ProfitReportRequest customReportForm) {

		// Extract page and size from the customReportForm
		int page = customReportForm.getPage();
		int size = customReportForm.getSize();

		// Validate page and size if necessary (e.g., ensure size is not too large)

		return profitReportService.ProfitReportview(username, customReportForm, page, size);
	}

	@PostMapping("/profit-report-xls")
	@Operation(summary = "Generate Profit Report in XLS format", description = "This endpoint generates a Profit Report in XLS format.")
	public ResponseEntity<?> generateProfitReportXls(
			@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody ProfitReportRequest customReportForm,
			HttpServletResponse response) {

		return profitReportService.ProfitReportxls(username, customReportForm, response);
	}

	@PostMapping("/profit-report-pdf")
	@Operation(summary = "Generate Profit Report in PDF format", description = "This endpoint generates a Profit Report in PDF format.")
	public ResponseEntity<?> generateProfitReportPdf(
			@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody ProfitReportRequest customReportForm,
			HttpServletResponse response) {
		return profitReportService.ProfitReportpdf(username, customReportForm, response);
	}

	@PostMapping("/profit-report-doc")
	@Operation(summary = "Generate Profit Report in DOC format", description = "This endpoint generates a Profit Report in DOC format.")
	public ResponseEntity<?> generateProfitReportDoc(
			@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody ProfitReportRequest customReportForm,
			HttpServletResponse response) {
		return profitReportService.ProfitReportdoc(username, customReportForm, response);
	}

	///////////////////////////////////
	@PostMapping("/Schedule-report")
	@Operation(summary = "Generate Schedule Report", description = "Generates a schedule report based on the provided parameters.")
	public ResponseEntity<?> generateScheduleReport(
			@Valid @Parameter(description = "Username") @RequestHeader String username,
			@Parameter(description = "Custom Report Form") @RequestBody ScheduleReportRequest customReportForm) {
		return scheduleReportService.ScheduleReport(username, customReportForm);
	}

	///////////////// SmscDlr
	@PostMapping("/SmscDlr-report-view")
	@Operation(summary = "Generate SmscDlr Delivery Report in View format", description = "This endpoint generates an SMS Delivery Report in View format.")
	public ResponseEntity<?> generateSmscDlrReportView(
			@Valid @Parameter(description = "Username") @RequestHeader String username,
			@Parameter(description = "Custom Report Form") @RequestBody SmscDlrReportRequest customReportForm) {
		return smscDlrReportService.SmscDlrReportview(username, customReportForm);
	}

	@PostMapping("/SmscDlr-report-xls")
	@Operation(summary = "Generate SmscDlr Delivery Report in XLS format", description = "This endpoint generates an SMS Delivery Report in XLS format.")
	public ResponseEntity<?> generateSmscDlrReportXls(
			@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody SmscDlrReportRequest customReportForm,
			HttpServletResponse response) {
		return smscDlrReportService.SmscDlrReportxls(username, customReportForm, response);
	}

	@PostMapping("/SmscDlr-report-pdf")
	@Operation(summary = "Generate SmscDlr Delivery Report in PDF format", description = "This endpoint generates an SMS Delivery Report in PDF format.")
	public ResponseEntity<?> generateSmscDlrReportPdf(
			@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody SmscDlrReportRequest customReportForm,
			HttpServletResponse response) {
		return smscDlrReportService.SmscDlrReportpdf(username, customReportForm, response);
	}

	@PostMapping("/SmscDlr-report-doc")
	@Operation(summary = "Generate SmscDlr Delivery Report in DOC format", description = "This endpoint generates an SMS Delivery Report in DOC format.")
	public ResponseEntity<?> generateSmscDlrReportDoc(
			@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody SmscDlrReportRequest customReportForm,
			HttpServletResponse response) {
		return smscDlrReportService.SmscDlrReportdoc(username, customReportForm, response);
	}

	/////////////////// SubmissionReport///////////////
	@PostMapping("/submission-execute")
	@Operation(summary = "Execute submission Report", description = "This endpoint executes a submission report.")
	public ResponseEntity<?> executeCustomReport(
			@Valid @Parameter(description = "Username") @RequestHeader String username,
			@Parameter(description = "submission Report Form") @RequestBody SubmissionReportRequest customReportForm,
			HttpServletResponse response) {
		return submissionReportService.execute(username, customReportForm, response);

	}

	////////////////////

	@PostMapping("/summary-report-view")
	@Operation(summary = "Generate Summary Report in View format", description = "This endpoint generates a Summary Report in View format.")
	public ResponseEntity<?> generateSummaryReportView(
			@Valid @Parameter(description = "Username") @RequestHeader String username,
			@RequestBody SummaryReportForm customReportForm) {
		return summaryReportService.SummaryReportview(username, customReportForm);
	}

	@PostMapping("/summary-report-xls")
	@Operation(summary = "Generate Summary Report in XLS format", description = "This endpoint generates a Summary Report in XLS format.")
	public ResponseEntity<?> generateSummaryReportXls(
			@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody SummaryReportForm customReportForm,
			HttpServletResponse response,
			@Parameter(description = "Language of the report") @RequestParam String lang) {
		System.out.println("run 570 in controller");
		return summaryReportService.SummaryReportxls(username, customReportForm, response);

	}

	@PostMapping("/summary-report-pdf")
	@Operation(summary = "Generate Summary Report in PDF format", description = "This endpoint generates a Summary Report in PDF format.")
	public ResponseEntity<?> generateSummaryReportPdf(
			@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody SummaryReportForm customReportForm,
			HttpServletResponse response,
			@Parameter(description = "Language of the report") @RequestParam String lang) {
		return summaryReportService.SummaryReportpdf(username, customReportForm, response);

	}

	@PostMapping("/summary-report-doc")
	@Operation(summary = "Generate Summary Report in DOC format", description = "This endpoint generates a Summary Report in DOC format.")
	public ResponseEntity<?> generateSummaryReportDoc(
			@Valid @Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestBody SummaryReportForm customReportForm,
			HttpServletResponse response,
			@Parameter(description = "Language of the report") @RequestParam String lang) {
		return summaryReportService.SummaryReportdoc(username, customReportForm, response);

	}

	@GetMapping("/transactions")
	public ResponseEntity<?> executeTransaction(@RequestHeader("username") String username) {
		return transactionReportService.executeTransaction(username);
	}

<<<<<<< HEAD


    @GetMapping("/transactions")
    public ResponseEntity<?> executeTransaction(@RequestHeader("username") String username) {
        return transactionReportService.executeTransaction(username);
    }

    
	@PostMapping(value="/send-attachment" ,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary="send email with file atttachement to the User",description = "This endPoint send a file attaced to the email")
	public ResponseEntity<?>sentAttachmentWithEmail(
			@RequestPart(value="file",required = false) MultipartFile attachment,
			@Parameter(description = "attach file for sending to the email",content = @Content(schema = @Schema(implementation = SendAttachmentRequest.class))) @RequestParam(value = "sendAttachmentRequest", required = true) String sendAttachmentRequest
			){
			fileAttachmentSenderService.sendEmailWithAttachment(attachment, sendAttachmentRequest);
			return new ResponseEntity<>("Email Sent Successfully",HttpStatus.OK);
		}
=======
	@PostMapping(value = "/send-attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(summary = "send email with file atttachement to the User", description = "This endPoint send a file attaced to the email")
	public ResponseEntity<?> sentAttachmentWithEmail(@RequestHeader("username") String username,
			@RequestPart(value = "file", required = false) MultipartFile attachment,
			@Parameter(description = "attach file for sending to the email", content = @Content(schema = @Schema(implementation = SendAttachmentRequest.class))) @RequestParam(value = "sendAttachmentRequest", required = true) String sendAttachmentRequest) {
		fileAttachmentSenderService.sendEmailWithAttachment(username,attachment, sendAttachmentRequest);
		return new ResponseEntity<>("Email Sent Successfully", HttpStatus.OK);
	}
>>>>>>> 62b80b8 (add this)

}
