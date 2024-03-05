package com.hti.smpp.common.service.impl;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.contacts.dto.GroupEntry;
import com.hti.smpp.common.contacts.repository.GroupEntryRepository;
import com.hti.smpp.common.exception.InternalServerException;
import com.hti.smpp.common.exception.NotFoundException;
import com.hti.smpp.common.exception.UnauthorizedException;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.network.repository.NetworkEntryRepository;
import com.hti.smpp.common.route.dto.RouteEntry;
import com.hti.smpp.common.route.dto.RouteEntryExt;
import com.hti.smpp.common.route.repository.RouteEntryRepository;
import com.hti.smpp.common.service.DownloadService;
import com.hti.smpp.common.smsc.dto.SmscEntry;
import com.hti.smpp.common.smsc.repository.SmscEntryRepository;
import com.hti.smpp.common.user.dto.UserEntry;
import com.hti.smpp.common.user.dto.WebMasterEntry;
import com.hti.smpp.common.user.repository.UserEntryRepository;
import com.hti.smpp.common.user.repository.WebMasterEntryRepository;
import com.hti.smpp.common.util.Access;
import com.hti.smpp.common.util.ConstantMessages;
import com.hti.smpp.common.util.Constants;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
import com.hti.smpp.common.util.MessageResourceBundle;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

@Service
public class DownloadServiceImpl implements DownloadService {

	private static final Logger logger = LoggerFactory.getLogger(DownloadServiceImpl.class);

	@Autowired
	private UserEntryRepository userRepository;

	@Autowired
	private MessageResourceBundle messageResourceBundle;

	@Autowired
	private WebMasterEntryRepository webMasterRepo;

	@Autowired
	private SmscEntryRepository smscEntryRepo;

	@Autowired
	private GroupEntryRepository groupEntryRepo;

	@Autowired
	private RouteEntryRepository routeRepo;

	@Autowired
	private NetworkEntryRepository networkRepo;

	@Override
	public ResponseEntity<?> downloadPricing(String format, String username) {

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
			throw new UnauthorizedException(messageResourceBundle.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION,
					new Object[] { username }));
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}

	}

	@Override
	public ResponseEntity<List<Object>> downloadPricingInList(String username) {
		List<Object> resultList = new ArrayList<>();

		Optional<UserEntry> userOptional = userRepository.findBySystemId(username);
		UserEntry user = null;
		if (userOptional.isPresent()) {
			user = userOptional.get();
			if (!Access.isAuthorized(user.getRole(), "isAuthorizedAll")) {
				throw new UnauthorizedException(messageResourceBundle
						.getExMessage(ConstantMessages.UNAUTHORIZED_OPERATION, new Object[] { username }));
			}
		} else {
			throw new NotFoundException(
					messageResourceBundle.getExMessage(ConstantMessages.USER_NOT_FOUND, new Object[] { username }));
		}

		boolean proceedFurther = true;
		// String filename = null;
		String userid = user.getSystemId();
		logger.info("Coverage Report Request " + user.getSystemId());

		// proceedFurther = checkClientFlag(userid);

		if (proceedFurther) {
			try {
				Collection<RouteEntryExt> result = getCoverageReportInList(user.getSystemId(), user.getId(), false);

				if (!result.isEmpty()) {
					for (RouteEntryExt entry : result) {
						Map<String, Object> entryMap = new HashMap<>();
						entryMap.put("username", entry.getSystemId());
						entryMap.put("country", entry.getCountry());
						entryMap.put("operator", entry.getOperator());
						entryMap.put("mcc", entry.getMcc());
						entryMap.put("mnc", entry.getMnc());
						entryMap.put("cost", entry.getBasic().getCost());
						entryMap.put("currency", entry.getCurrency());
						entryMap.put("remarks", entry.getRemarks());
						resultList.add(entryMap);
					}
				} else {
					System.out.println("Routing Error For " + userid);
					throw new InternalServerException("Routing Error For " + userid);
				}
			} catch (WriteException | IOException | DocumentException e) {
				e.printStackTrace();
				proceedFurther = false;
				throw new InternalServerException(e.getMessage());
			} catch (NotFoundException e) {
				proceedFurther = false;
				throw new NotFoundException(e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
				proceedFurther = false;
				throw new InternalServerException("Requested Resource is Temporary Unavailable");
			}

		} else {
			throw new InternalServerException("Unable to proceed the request!");
		}
		return ResponseEntity.ok(resultList);
	}

	private boolean checkClientFlag(String userId) {
		try {
			String clientfileName = Constants.USER_FLAG_DIR + userId + ".txt";
			BufferedReader in;
			in = new BufferedReader(new FileReader(clientfileName));
			String flageValue = in.readLine();
			in.close();
			if (flageValue.indexOf("404") > -1) {
				return false;
			}
		} catch (IOException e) {
			throw new InternalServerException(e.getMessage());
		}

		return true;
	}

	public Map<Integer, String> listNames() {
		Map<Integer, String> names = new HashMap<Integer, String>();
		List<SmscEntry> allSmscEntries = null;
		try {
			allSmscEntries = this.smscEntryRepo.findAll();
		} catch (Exception e) {
			throw new NotFoundException("No Smsc Entries Found!");
		}
		for (SmscEntry entry : allSmscEntries) {
			names.put(entry.getId(), entry.getName());
		}
		names = names.entrySet().stream().sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return names;
	}

	public Map<Integer, String> listGroupNames() {
		Map<Integer, String> names = new HashMap<Integer, String>();
		names.put(0, "NONE");
		List<GroupEntry> groups = this.groupEntryRepo.findAll();
		for (GroupEntry entry : groups) {
			names.put(entry.getId(), entry.getName());
		}
		names = names.entrySet().stream().sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return names;
	}

	public Map<Integer, RouteEntryExt> listCoverage(int userId, boolean display, boolean cached) {
		Map<Integer, RouteEntryExt> list = new LinkedHashMap<Integer, RouteEntryExt>();
		Map<Integer, String> smsc_name_mapping = null;
		Map<Integer, String> group_name_mapping = null;
		if (display) {
			smsc_name_mapping = listNames();
			group_name_mapping = listGroupNames();
		}

		if (cached) {
			Predicate<Integer, RouteEntry> p = new PredicateBuilderImpl().getEntryObject().get("userId").equal(userId);
			for (RouteEntry basic : GlobalVars.BasicRouteEntries.values(p)) {
				RouteEntryExt entry = new RouteEntryExt(basic);
				if (display) {
					// ------ set user values -----------------
					if (GlobalVars.UserEntries.containsKey(basic.getUserId())) {
						entry.setSystemId(GlobalVars.UserEntries.get(basic.getUserId()).getSystemId());
						entry.setMasterId(GlobalVars.UserEntries.get(basic.getUserId()).getMasterId());
						entry.setCurrency(GlobalVars.UserEntries.get(basic.getUserId()).getCurrency());
						entry.setAccountType(GlobalVars.WebmasterEntries.get(basic.getUserId()).getAccountType());
					}
					// ------ set network values -----------------
					// NetworkEntry network = CacheService.getNetworkEntry(entry.getNetworkId());
					if (GlobalVars.NetworkEntries.containsKey(entry.getBasic().getNetworkId())) {
						NetworkEntry network = GlobalVars.NetworkEntries.get(entry.getBasic().getNetworkId());
						entry.setCountry(network.getCountry());
						entry.setOperator(network.getOperator());
						entry.setMcc(network.getMcc());
						entry.setMnc(network.getMnc());
					}
					// ------ set Smsc values -----------------
					if (entry.getBasic().getSmscId() == 0) {
						entry.setSmsc("Down");
					} else {
						if (smsc_name_mapping.containsKey(entry.getBasic().getSmscId())) {
							entry.setSmsc(smsc_name_mapping.get(entry.getBasic().getSmscId()));
						}
					}
					if (group_name_mapping.containsKey(entry.getBasic().getGroupId())) {
						entry.setGroup(group_name_mapping.get(entry.getBasic().getGroupId()));
					}
				}
				list.put(entry.getBasic().getNetworkId(), entry);
			}
		} else {
			// database operation
			logger.info("listing RouteEntries From Database: " + userId);
			List<RouteEntry> db_list = this.routeRepo.findByUserId(userId);
			UserEntry user = null;
			WebMasterEntry webMasterEntry = null;
			NetworkEntry network = null;

			for (RouteEntry basic : db_list) {
				RouteEntryExt entry = new RouteEntryExt(basic);
				if (display) {

					try {
						Optional<UserEntry> userOptional = this.userRepository.findById(entry.getBasic().getUserId());
						if (userOptional.isPresent()) {
							user = userOptional.get();
						} else {
							throw new NotFoundException("User not found!");
						}
					} catch (Exception e) {
						throw new NotFoundException(e.getMessage());
					}
					try {
						webMasterEntry = this.webMasterRepo.findByUserId(entry.getBasic().getUserId());
						if (webMasterEntry == null) {
							throw new NotFoundException("Web master entry not found!");
						}
					} catch (Exception e) {
						throw new NotFoundException(e.getMessage());
					}
					try {
						Optional<NetworkEntry> networkOptional = this.networkRepo
								.findById(entry.getBasic().getNetworkId());
						if (networkOptional.isPresent()) {
							network = networkOptional.get();
						} else {
							System.out.println("Network Entry Not Found!");
						}
					} catch (Exception e) {
						throw new NotFoundException(e.getMessage());
					}
					// ------ set user values -----------------
					if (user != null && webMasterEntry != null) {
						entry.setSystemId(user.getSystemId());
						entry.setMasterId(user.getMasterId());
						entry.setCurrency(user.getCurrency());
						entry.setAccountType(webMasterEntry.getAccountType());
					}
					// ------ set network values -----------------
					if (network != null) {
						entry.setCountry(network.getCountry());
						entry.setOperator(network.getOperator());
						entry.setMcc(network.getMcc());
						entry.setMnc(network.getMnc());
					}
					// ------ set Smsc values -----------------
					if (entry.getBasic().getSmscId() == 0) {
						entry.setSmsc("Down");
					} else {
						if (smsc_name_mapping.containsKey(entry.getBasic().getSmscId())) {
							entry.setSmsc(smsc_name_mapping.get(entry.getBasic().getSmscId()));
						}
					}
					if (group_name_mapping.containsKey(entry.getBasic().getGroupId())) {
						entry.setGroup(group_name_mapping.get(entry.getBasic().getGroupId()));
					}
				}
				list.put(entry.getBasic().getNetworkId(), entry);
			}
		}
		return list;
	}

	private String getCoverageReportXLS(String username, Collection<RouteEntryExt> coverageList)
			throws WriteException, IOException {
		String filename = IConstants.WEBSMPP_EXT_DIR + "report//" + username + "_coverage.xls";
		WritableFont courier = new WritableFont(WritableFont.createFont("Calibri"), 11, WritableFont.BOLD);
		WritableFont times = new WritableFont(WritableFont.createFont("Calibri"), 11);
		WritableCellFormat courierformat = new WritableCellFormat(courier);
		WritableCellFormat timesformat = new WritableCellFormat(times);
		courierformat.setAlignment(jxl.format.Alignment.CENTRE);
		courierformat.setBackground(Colour.GREY_25_PERCENT);
		courierformat.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);
		timesformat.setAlignment(Alignment.LEFT);
		// timesformat.setBackground(Colour.LIGHT_GREEN);
		// timesformat.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.DARK_GREEN);
		WritableWorkbook workbook = Workbook.createWorkbook(new File(filename));
		WritableSheet sheet = workbook.createSheet(username + "(0)", 0);
		sheet.getSettings().setDefaultColumnWidth(25);
		sheet.addCell(new Label(0, 0, "Country", courierformat));
		sheet.addCell(new Label(1, 0, "Operator", courierformat));
		sheet.addCell(new Label(2, 0, "MCC", courierformat));
		sheet.addCell(new Label(3, 0, "MNC", courierformat));
		sheet.addCell(new Label(4, 0, "Cost", courierformat));
		sheet.addCell(new Label(5, 0, "Currency", courierformat));
		sheet.addCell(new Label(6, 0, "Remarks", courierformat));
		int rowNum = 1;
		for (RouteEntryExt entry : coverageList) {
			String mnc = entry.getMnc();
			try {
				if (Integer.parseInt(entry.getMnc()) == 0) {
					if (entry.getOperator() != null && entry.getOperator().equalsIgnoreCase("Rest")) {
						mnc = " ";
					}
				}
			} catch (Exception ex) {
			}
			sheet.addCell(new Label(0, rowNum, entry.getCountry(), timesformat));
			sheet.addCell(new Label(1, rowNum, entry.getOperator(), timesformat));
			sheet.addCell(new Label(2, rowNum, entry.getMcc(), timesformat));
			sheet.addCell(new Label(3, rowNum, mnc, timesformat));
			sheet.addCell(new Label(4, rowNum, String.valueOf(entry.getBasic().getCost()), timesformat));
			sheet.addCell(new Label(5, rowNum, entry.getCurrency(), timesformat));
			sheet.addCell(new Label(6, rowNum, entry.getBasic().getRemarks(), timesformat));
			rowNum++;
		}
		workbook.write();
		workbook.close();
		return filename;
	}

	private String getCoverageReportPDF(String username, Collection<RouteEntryExt> coverageList)
			throws DocumentException, FileNotFoundException, IOException {
		String filename = IConstants.WEBSMPP_EXT_DIR + "report//" + username + "_coverage.pdf";
		Document document = new Document(PageSize.A4, 5, 5, 35, 35);
		PdfWriter.getInstance(document, new FileOutputStream(filename));
		// ---Font Definitions------------------------
		Font font_header = new Font(Font.TIMES_ROMAN, 18, 1, Color.BLUE);
		Font font_headLine = new Font(Font.TIMES_ROMAN, 12, 1, Color.WHITE);
		Font font_footer = new Font(Font.COURIER, 10, 1, Color.BLUE);
		Font font_ConHead = new Font(Font.COURIER, 11, 1, Color.red);
		Font font_Content = new Font(Font.TIMES_ROMAN, 10, 1, Color.BLACK);
		// ---Font Definitions------------------------
		Image logo = Image.getInstance(IConstants.WEBSMPP_EXT_DIR + "//images//logo.jpg");
		logo.setAlignment(Image.MIDDLE);
		logo.scaleToFit(30, 24);
		String report_Heading = "Current Pricing List";
		String footer_MSG = "";
		// -------------Set header & Footer------------------
		HeaderFooter header = new HeaderFooter(new Phrase(report_Heading, font_header), false);
		header.setAlignment(Element.ALIGN_LEFT);
		HeaderFooter footer = new HeaderFooter(new Phrase(footer_MSG + "         Page No. ", font_footer), true);
		footer.setAlignment(Element.ALIGN_CENTER);
		header.setBackgroundColor(Color.orange);
		document.setHeader(header);
		document.setFooter(footer);
		// -------------Set header & Footer------------------
		document.open();
		int NumColumns = 8;
		int sno = 1;
		username = "\tUsername :: " + username;
		// -----------------------------------------------
		PdfPTable head_line = new PdfPTable(2);
		head_line.getDefaultCell().setBorder(0);
		head_line.getDefaultCell().setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
		head_line.getDefaultCell().setBackgroundColor(Color.GRAY);
		head_line.getDefaultCell().setVerticalAlignment(Element.ALIGN_JUSTIFIED);
		head_line.getDefaultCell().setPaddingBottom(10);
		head_line.setWidthPercentage(90);
		head_line.addCell(new Phrase(username, font_headLine));
		head_line.addCell(new Phrase(new Date().toString(), font_headLine));
		// -----------------------------------------------
		PdfPTable Under_line = new PdfPTable(1);
		Under_line.getDefaultCell().setBorder(0);
		Under_line.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
		head_line.setWidthPercentage(90);
		Under_line.addCell("    ");
		// -----------------------------------------------
		PdfPTable datatable = new PdfPTable(NumColumns);
		int headerwidths[] = { 8, 10, 10, 8, 8, 10, 10, 10 }; // percentage
		datatable.setWidths(headerwidths);
		datatable.setWidthPercentage(90);
		datatable.getDefaultCell().setPaddingBottom(5);
		datatable.getDefaultCell().setBorderWidth(1);
		datatable.addCell(new Phrase(" S No.", font_ConHead));
		datatable.addCell(new Phrase(" Country ", font_ConHead));
		datatable.addCell(new Phrase(" Operator ", font_ConHead));
		datatable.addCell(new Phrase(" MCC ", font_ConHead));
		datatable.addCell(new Phrase(" MNC ", font_ConHead));
		datatable.addCell(new Phrase(" Cost ", font_ConHead));
		datatable.addCell(new Phrase(" Currency ", font_ConHead));
		datatable.addCell(new Phrase(" Remarks ", font_ConHead));
		datatable.getDefaultCell().setGrayFill(1);
		datatable.setHeaderRows(1);
		for (RouteEntryExt entry : coverageList) {
			if (sno % 2 == 1) {
				datatable.getDefaultCell().setGrayFill(0.9f);
			}
			String mnc = entry.getMnc();
			try {
				if (Integer.parseInt(entry.getMnc()) == 0) {
					if (entry.getOperator() != null && entry.getOperator().equalsIgnoreCase("Rest")) {
						mnc = " ";
					}
				}
			} catch (Exception ex) {
			}
			datatable.addCell(new Phrase(sno + "", font_Content));
			datatable.addCell(new Phrase(entry.getCountry(), font_Content));
			datatable.addCell(new Phrase(entry.getOperator(), font_Content));
			datatable.addCell(new Phrase(entry.getMcc(), font_Content));
			datatable.addCell(new Phrase(mnc, font_Content));
			datatable.addCell(new Phrase(String.valueOf(entry.getBasic().getCost()), font_Content));
			datatable.addCell(new Phrase(entry.getCurrency(), font_Content));
			datatable.addCell(new Phrase(entry.getBasic().getRemarks(), font_Content));
			sno = sno + 1;
		}
		// --------------------------------------------------Adding Tables to
		// Document-------------------
		document.add(head_line);
		document.add(Under_line);
		document.add(datatable);
		// --------------------------------------------------Adding Tables to
		// Document-------------------
		document.close();
		return filename;
	}

	private String getCoverageReportCSV(String username, Collection<RouteEntryExt> coverageList) throws IOException {
		System.out.println("CoverageList Size: " + coverageList.size());
		String filename = IConstants.WEBSMPP_EXT_DIR + "report//" + username + "_coverage.csv";
		try {
			FileWriter writer = new FileWriter(filename);
			StringBuffer strbuff = new StringBuffer();
			strbuff.append("Country");
			strbuff.append(',');
			strbuff.append("Operator");
			strbuff.append(',');
			strbuff.append("MCC");
			strbuff.append(',');
			strbuff.append("MNC");
			strbuff.append(',');
			strbuff.append("Cost");
			strbuff.append(',');
			strbuff.append("Currency");
			strbuff.append(',');
			strbuff.append("Remarks");
			strbuff.append('\n');
			for (RouteEntryExt entry : coverageList) {
				strbuff.append(entry.getCountry());
				strbuff.append(',');
				strbuff.append(entry.getOperator());
				strbuff.append(',');
				strbuff.append(entry.getMcc());
				strbuff.append(',');
				String mnc = entry.getMnc();
				try {
					if (Integer.parseInt(entry.getMnc()) == 0) {
						if (entry.getOperator() != null && entry.getOperator().equalsIgnoreCase("Rest")) {
							mnc = " ";
						}
					}
				} catch (Exception ex) {
				}
//				mnc = mnc.replaceAll(",", "|");
				strbuff.append(mnc);
				strbuff.append(',');
				strbuff.append(entry.getBasic().getCost());
				strbuff.append(',');
				strbuff.append(entry.getCurrency());
				strbuff.append(',');
				strbuff.append(entry.getBasic().getRemarks());
				strbuff.append('\n');
			}
			writer.write(strbuff.toString());
			writer.flush();
			writer.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return filename;
	}

	private String getCoverageReport(String username, int userid, String format, boolean cached)
			throws WriteException, IOException, DocumentException {
		String filename = null;
		Collection<RouteEntryExt> list = listCoverage(userid, true, cached).values();
		if (format.equalsIgnoreCase("xls")) {
			filename = getCoverageReportXLS(username, list);
		} else if (format.equalsIgnoreCase("pdf")) {
			filename = getCoverageReportPDF(username, list);
		} else if (format.equalsIgnoreCase("csv")) {
			filename = getCoverageReportCSV(username, list);
		}
		return filename;
	}

	private Collection<RouteEntryExt> getCoverageReportInList(String username, int userid, boolean cached)
			throws WriteException, IOException, DocumentException {
		Collection<RouteEntryExt> list = listCoverage(userid, true, cached).values();
		return list;
	}

}
