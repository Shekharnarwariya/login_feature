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
import com.hti.smpp.common.request.CustomReportDTO;
import com.hti.smpp.common.request.CustomReportForm;
import com.hti.smpp.common.response.BatchDTO;
import com.hti.smpp.common.response.DeliveryDTO;
import com.hti.smpp.common.response.TrackResultResponse;
import com.hti.smpp.common.rmi.dto.LookupReport;
import com.hti.smpp.common.schedule.dto.ScheduleEntryExt;
import com.hti.smpp.common.service.CustomizedReportServices;
import com.hti.smpp.common.service.DlrSummaryReportService;
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

	@Autowired
	private LookupReportService lookupReportService;

	@Autowired
	private CustomizedReportServices customizedReportService;

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

	@GetMapping("/abort-batch")
	@Operation(summary = "Abort Batch Report", description = "Abort a batch report")
	public List<BulkEntry> abortBatchReport(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang) {
		return reportService.abortBatchReport(username, customReportForm, lang);
	}

	@GetMapping("/balance-report-view")
	@Operation(summary = "Balance Report View", description = "View balance report")
	public Map<String, List<DeliveryDTO>> balanceReportView(
			@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang) {
		return reportService.BalanceReportView(username, customReportForm, lang);
	}

	@GetMapping("/balance-report-xls")
	@Operation(summary = "Balance Report XLS", description = "Generate balance report in XLS format")
	public String balanceReportXls(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return reportService.BalanceReportxls(username, customReportForm, response, lang);
	}

	@GetMapping("/balance-report-pdf")
	@Operation(summary = "Balance Report PDF", description = "Generate balance report in PDF format")
	public String balanceReportPdf(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return reportService.balanceReportPdf(username, customReportForm, response, lang);
	}

	@GetMapping("/balance-report-doc")
	@Operation(summary = "Balance Report DOC", description = "Generate balance report in DOC format")
	public String balanceReportDoc(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return reportService.BalanceReportDoc(username, customReportForm, response, lang);
	}

	@GetMapping("/blocked-report")
	@Operation(summary = "Blocked Report View", description = "View blocked report")
	public List<DeliveryDTO> blockedReportView(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @org.springframework.web.bind.annotation.RequestBody CustomReportForm customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang) {
		return reportService.BlockedReportView(username, customReportForm, lang);
	}

	@GetMapping("/blocked-report-xls")
	@Operation(summary = "Blocked Report XLS", description = "Generate blocked report in XLS format")
	public String blockedReportXls(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return reportService.BlockedReportxls(username, customReportForm, response, lang);
	}

	@GetMapping("/blocked-report-pdf")
	@Operation(summary = "Blocked Report PDF", description = "Generate blocked report in PDF format")
	public String blockedReportPdf(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return reportService.BlockedReportPdf(username, customReportForm, response, lang);
	}

	@GetMapping("/blocked-report-doc")
	@Operation(summary = "Blocked Report DOC", description = "Generate blocked report in DOC format")
	public String blockedReportDoc(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return reportService.BlockedReportDoc(username, customReportForm, response, lang);
	}

	@GetMapping("/campaign-report-view")
	@Operation(summary = "View Campaign Report", description = "Generates and returns the Campaign Report for viewing")
	public JasperPrint campaignReportview(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return reportService.CampaignReportview(username, customReportForm, lang);
	}

	@GetMapping("/campaign-report-doc")
	@Operation(summary = "Doc Campaign Report", description = "Generates and returns the Campaign Report DOC format")
	public JasperPrint campaignReportdoc(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return reportService.CampaignReportDoc(username, customReportForm, response, lang);
	}

	@GetMapping("/campaign-report-xls")
	@Operation(summary = "Campaign Report XLS", description = "Generate Campaign report in XLS format")
	public JasperPrint campaignReportxls(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return reportService.CampaignReportxls(username, customReportForm, response, lang);
	}

	@GetMapping("/campaign-report-pdf")
	@Operation(summary = "Campaign Report PDF", description = "Generate Campaign report in PDF format")
	public JasperPrint campaignReportPdf(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return reportService.CampaignReportPdf(username, customReportForm, response, lang);
	}

	@GetMapping("/content-report-view")
	public List<DeliveryDTO> contentReportView(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang) {
		return reportService.ContentReportView(username, customReportForm, lang);
	}

	@GetMapping("/content-report-xls")
	@Operation(summary = "content Report PDF", description = "Generate content report in PDF format")
	public String contentReportxls(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return reportService.ContentReportxls(username, customReportForm, response, lang);
	}

	@GetMapping("/content-report-pdf")
	@Operation(summary = "content Report PDF", description = "Generate content report in PDF format")
	public String contentReportPdf(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return reportService.ContentReportPdf(username, customReportForm, response, lang);
	}

	@GetMapping("/content-report-Doc")
	@Operation(summary = "content Report Doc", description = "Generate content report in Doc format")
	public String contentReportDoc(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return reportService.ContentReportDoc(username, customReportForm, response, lang);
	}

	@Operation(summary = " Download User Delivery Report View")
	@GetMapping("/userDelivery-report-view")
	public ResponseEntity<List<DeliveryDTO>> userDeliveryReportView(@RequestParam String username,
			@RequestBody CustomReportForm customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang) {
		List<DeliveryDTO> report = deliveryService.UserDeliveryReportView(username, customReportForm, lang);
		return ResponseEntity.ok(report);
	}

	@Operation(summary = "Download User Delivery Report as XLS")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully downloaded XLS file"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	@GetMapping("/userDelivery-report-xls")
	public ResponseEntity<String> userDeliveryReportXls(@RequestParam String username,
			@RequestBody CustomReportForm customReportForm, HttpServletResponse response,
			@Parameter(description = "language of the report") @RequestParam String lang) {
		String filePath = deliveryService.UserDeliveryReportxls(username, customReportForm, response, lang);
		return ResponseEntity.ok(filePath);
	}

	@Operation(summary = "Download User Delivery Report as PDF")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully downloaded PDF file"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	@GetMapping("/userDelivery-report-pdf")
	public ResponseEntity<String> userDeliveryReportPdf(@RequestParam String username,
			@RequestBody CustomReportForm customReportForm, HttpServletResponse response,
			@Parameter(description = "language of the report") @RequestParam String lang) {
		String filePath = deliveryService.UserDeliveryReportPdf(username, customReportForm, response, lang);
		return ResponseEntity.ok(filePath);
	}

	@Operation(summary = "Download User Delivery Report as DOC")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Successfully downloaded DOC file"),
			@ApiResponse(responseCode = "500", description = "Internal Server Error") })
	@GetMapping("/userDelivery-report-doc")
	public ResponseEntity<String> userDeliveryReportDoc(@RequestParam String username,
			@RequestBody CustomReportForm customReportForm, HttpServletResponse response,
			@Parameter(description = "language of the report") @RequestParam String lang) {
		String filePath = deliveryService.UserDeliveryReportDoc(username, customReportForm, response, lang);
		return ResponseEntity.ok(filePath);
	}

	@Operation(summary = "Get Track Result Report")
	@GetMapping("/track-result-report")
	public ResponseEntity<TrackResultResponse> trackResultReport(@RequestParam String username,
			@RequestBody CustomReportForm customReportForm,
			@Parameter(description = "language of the report") @RequestParam String lang) {
		TrackResultResponse result = trackResultService.TrackResultReport(username, customReportForm, lang);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Get Lookup Report View")
	@GetMapping("/lookup-report-view")
	public ResponseEntity<List<LookupReport>> getLookupReportView(@RequestParam String username,
			@RequestBody CustomReportForm customReportForm, @RequestParam String lang) {
		List<LookupReport> result = lookupReportService.LookupReportview(username, customReportForm, lang);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Download Lookup Report Excel")
	@GetMapping("/lookup-report-xls")
	public ResponseEntity<String> downloadLookupReportXLS(@RequestParam String username,
			@RequestBody CustomReportForm customReportForm, @RequestParam String lang, HttpServletResponse response) {
		String result = lookupReportService.LookupReportxls(username, customReportForm, lang, response);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Download Lookup Report PDF")
	@GetMapping("/lookup-report-pdf")
	public ResponseEntity<String> downloadLookupReportPDF(@RequestParam String username,
			@RequestBody CustomReportForm customReportForm, @RequestParam String lang, HttpServletResponse response) {
		String result = lookupReportService.LookupReportPdf(username, customReportForm, lang, response);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Download Lookup Report DOC")
	@GetMapping("/lookup-report-doc")
	public ResponseEntity<String> downloadLookupReportDoc(@RequestParam String username,
			@RequestBody CustomReportForm customReportForm, @RequestParam String lang, HttpServletResponse response) {
		String result = lookupReportService.LookupReportDoc(username, customReportForm, lang, response);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Recheck Lookup Report")
	@GetMapping("/recheck-report")
	public ResponseEntity<String> recheckLookupReport(@RequestParam String username,
			@RequestBody CustomReportForm customReportForm, @RequestParam String lang, HttpServletResponse response) {
		String result = lookupReportService.LookupReportRecheck(username, customReportForm, lang, response);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Get Customized Report View")
	@PostMapping("/customized-report-view")
	public ResponseEntity<List<DeliveryDTO>> getCustomizedReportView(@RequestParam String username,
			@RequestBody CustomReportForm customReportForm, @RequestParam String lang) {
		List<DeliveryDTO> result = customizedReportService.CustomizedReportView(username, customReportForm, lang);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Download Customized Report DOC")
	@GetMapping("/customized-report-doc")
	public ResponseEntity<String> downloadCustomizedReportDoc(@RequestParam String username,
			@RequestBody CustomReportForm customReportForm, @RequestParam String lang, HttpServletResponse response) {
		String result = customizedReportService.CustomizedReportdoc(username, customReportForm, response, lang);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Download Customized Report Excel")
	@GetMapping("/customized-report-xls")
	public ResponseEntity<String> downloadCustomizedReportXLS(@RequestParam String username,
			@RequestBody CustomReportForm customReportForm, @RequestParam String lang, HttpServletResponse response) {
		String result = customizedReportService.CustomizedReportxls(username, customReportForm, response, lang);
		return ResponseEntity.ok(result);
	}

	@Operation(summary = "Download Customized Report PDF")
	@GetMapping("/customized-report-pdf")
	public ResponseEntity<String> downloadCustomizedReportPDF(@RequestParam String username,
			@RequestBody CustomReportForm customReportForm, @RequestParam String lang, HttpServletResponse response) {
		String result = customizedReportService.CustomizedReportpdf(username, customReportForm, response, lang);
		return ResponseEntity.ok(result);

	}

	@GetMapping("/performance-report-view")
	@Operation(summary = "Performance Report View", description = "View performance report")
	public List<DeliveryDTO> performanceReportView(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang) {
		return performanceReportService.PerformanceReportview(username, customReportForm, lang);
	}

	@GetMapping("/performance-report-xls")
	@Operation(summary = "Download Performance Report as XLS", description = "Download performance report in XLS format")
	public String downloadPerformanceReportXLS(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return performanceReportService.PerformanceReportxls(username, customReportForm, lang, response);
	}

	@GetMapping("/performance-report-pdf")
	@Operation(summary = "Download Performance Report as PDF", description = "Download performance report in PDF format")
	public String downloadPerformanceReportPDF(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return performanceReportService.PerformanceReportPdf(username, customReportForm, lang, response);
	}

	@GetMapping("/performance-report-doc")
	@Operation(summary = "Download Performance Report as DOC", description = "Download performance report in DOC format")
	public String downloadPerformanceReportDOC(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return performanceReportService.PerformanceReportDoc(username, customReportForm, lang, response);
	}

	@GetMapping("/dlr-summary-report-view")
	@Operation(summary = "DLR Summary Report View", description = "View DLR summary report")
	public List<DeliveryDTO> dlrSummaryReportView(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang) {
		return dlrSummaryReportService.DlrSummaryReportview(username, customReportForm, lang);
	}

	@GetMapping("/dlr-summary-report-doc")
	@Operation(summary = "Download DLR Summary Report as DOC", description = "Download DLR summary report in DOC format")
	public String downloadDlrSummaryReportDOC(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return dlrSummaryReportService.DlrSummaryReportdoc(username, customReportForm, response, lang);
	}

	@GetMapping("/dlr-summary-report-pdf")
	@Operation(summary = "Download DLR Summary Report as PDF", description = "Download DLR summary report in PDF format")
	public String downloadDlrSummaryReportPDF(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return dlrSummaryReportService.DlrSummaryReportdpdf(username, customReportForm, response, lang);
	}

	@GetMapping("/dlr-summary-report-xls")
	@Operation(summary = "Download DLR Summary Report as XLS", description = "Download DLR summary report in XLS format")
	public String downloadDlrSummaryReportXLS(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return dlrSummaryReportService.DlrSummaryReportdxls(username, customReportForm, response, lang);
	}

///////////////////////
	@GetMapping("/latency-report-view")
	@Operation(summary = "Latency Report View", description = "View latency report")
	public List<DeliveryDTO> latencyReportView(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang) {
		return latencyReportService.LatencyReportView(username, customReportForm, lang);
	}

	@GetMapping("/latency-report-xls")
	@Operation(summary = "Download Latency Report as XLS", description = "Download latency report in XLS format")
	public String downloadLatencyReportXLS(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return latencyReportService.LatencyReportxls(username, customReportForm, response, lang);
	}

	@GetMapping("/latency-report-pdf")
	@Operation(summary = "Download Latency Report as PDF", description = "Download latency report in PDF format")
	public String downloadLatencyReportPDF(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return latencyReportService.LatencyReportpdf(username, customReportForm, response, lang);
	}

	@GetMapping("/latency-report-doc")
	@Operation(summary = "Download Latency Report as DOC", description = "Download latency report in DOC format")
	public String downloadLatencyReportDOC(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return latencyReportService.LatencyReportdoc(username, customReportForm, response, lang);
	}

////////////////
	@GetMapping("/profit-report-view")
	@Operation(summary = "Profit Report View", description = "View profit report")
	public JasperPrint profitReportView(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang) {
		return profitReportService.ProfitReportview(username, customReportForm, lang);
	}

	@GetMapping("/profit-report-xls")
	@Operation(summary = "Generate Profit Report in XLS format", description = "This endpoint generates a Profit Report in XLS format.")
	public String generateProfitReportXls(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang,
			HttpServletResponse response) {

		return profitReportService.ProfitReportxls(username, customReportForm, lang, response);
	}

	@GetMapping("/profit-report-pdf")
	@Operation(summary = "Generate Profit Report in PDF format", description = "This endpoint generates a Profit Report in PDF format.")
	public String generateProfitReportPdf(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return profitReportService.ProfitReportpdf(username, customReportForm, lang, response);
	}

	@GetMapping("/profit-report-doc")
	@Operation(summary = "Generate Profit Report in DOC format", description = "This endpoint generates a Profit Report in DOC format.")
	public String generateProfitReportDoc(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return profitReportService.ProfitReportdoc(username, customReportForm, lang, response);
	}

	///////////////////////////////////
	@GetMapping("/Schedule-report")
	@Operation(summary = "Generate Schedule Report", description = "Generates a schedule report based on the provided parameters.")
	public List<ScheduleEntryExt> generateScheduleReport(
			@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return scheduleReportService.ScheduleReport(username, customReportForm, lang, response);
	}

	///////////////// SmscDlr
	@GetMapping("/SmscDlr-report-view")
	@Operation(summary = "Generate SmscDlr Delivery Report in View format", description = "This endpoint generates an SMS Delivery Report in View format.")
	public List<DeliveryDTO> generateSmscDlrReportView(
			@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang) {
		return smscDlrReportService.SmscDlrReportview(username, customReportForm, lang);
	}

	@GetMapping("/SmscDlr-report-xls")
	@Operation(summary = "Generate SmscDlr Delivery Report in XLS format", description = "This endpoint generates an SMS Delivery Report in XLS format.")
	public List<DeliveryDTO> generateSmscDlrReportXls(
			@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return smscDlrReportService.SmscDlrReportvxls(username, customReportForm, lang, response);
	}

	@GetMapping("/SmscDlr-report-pdf")
	@Operation(summary = "Generate SmscDlr Delivery Report in PDF format", description = "This endpoint generates an SMS Delivery Report in PDF format.")
	public List<DeliveryDTO> generateSmscDlrReportPdf(
			@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return smscDlrReportService.SmscDlrReportvpdf(username, customReportForm, lang, response);
	}

	@GetMapping("/SmscDlr-report-doc")
	@Operation(summary = "Generate SmscDlr Delivery Report in DOC format", description = "This endpoint generates an SMS Delivery Report in DOC format.")
	public List<DeliveryDTO> generateSmscDlrReportDoc(
			@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang,
			HttpServletResponse response) {
		return smscDlrReportService.SmscDlrReportdoc(username, customReportForm, lang, response);
	}

	/////////////////// SubmissionReport///////////////
	@GetMapping("/execute")
	@Operation(summary = "Execute Custom Report", description = "This endpoint executes a custom report.")
	public List<CustomReportDTO> executeCustomReport(@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			HttpServletResponse response) {
		return submissionReportService.execute(username, customReportForm, response);

	}

	////////////////////

	@PostMapping("/summary-report-view")
	@Operation(summary = "Generate Summary Report in View format", description = "This endpoint generates a Summary Report in View format.")
	public ResponseEntity<List<BatchDTO>> generateSummaryReportView(
			@Parameter(description = "Username") @RequestParam String username,
			@RequestBody CustomReportForm customReportForm,
			@Parameter(description = "Language of the report") @RequestParam String lang) {
		System.out.println("run 570 in controller");
		List<BatchDTO> reportList = summaryReportService.SummaryReportview(username, customReportForm, lang);
		return ResponseEntity.ok(reportList);
	}

	@GetMapping("/summary-report-xls")
	@Operation(summary = "Generate Summary Report in XLS format", description = "This endpoint generates a Summary Report in XLS format.")
	public ResponseEntity<String> generateSummaryReportXls(
			@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			HttpServletResponse response,
			@Parameter(description = "Language of the report") @RequestParam String lang) {
		String reportPath = summaryReportService.SummaryReportxls(username, customReportForm, response, lang);
		return ResponseEntity.ok(reportPath);
	}

	@GetMapping("/summary-report-pdf")
	@Operation(summary = "Generate Summary Report in PDF format", description = "This endpoint generates a Summary Report in PDF format.")
	public ResponseEntity<String> generateSummaryReportPdf(
			@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			HttpServletResponse response,
			@Parameter(description = "Language of the report") @RequestParam String lang) {
		String reportPath = summaryReportService.SummaryReportpdf(username, customReportForm, response, lang);
		return ResponseEntity.ok(reportPath);
	}

	@GetMapping("/summary-report-doc")
	@Operation(summary = "Generate Summary Report in DOC format", description = "This endpoint generates a Summary Report in DOC format.")
	public ResponseEntity<String> generateSummaryReportDoc(
			@Parameter(description = "Username") @RequestParam String username,
			@Parameter(description = "Custom Report Form") @RequestParam CustomReportForm customReportForm,
			HttpServletResponse response,
			@Parameter(description = "Language of the report") @RequestParam String lang) {
		String reportPath = summaryReportService.SummaryReportdoc(username, customReportForm, response, lang);
		return ResponseEntity.ok(reportPath);
	}

}
