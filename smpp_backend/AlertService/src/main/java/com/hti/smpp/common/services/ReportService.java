package com.hti.smpp.common.services;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hazelcast.query.Predicate;
import com.hazelcast.query.impl.PredicateBuilderImpl;
import com.hti.smpp.common.contacts.dto.GroupEntry;
import com.hti.smpp.common.network.dto.NetworkEntry;
import com.hti.smpp.common.request.DBMessage;
import com.hti.smpp.common.route.dto.RouteEntry;
import com.hti.smpp.common.route.dto.RouteEntryExt;
import com.hti.smpp.common.smsc.dto.SmscEntry;
import com.hti.smpp.common.util.GlobalVars;
import com.hti.smpp.common.util.IConstants;
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

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
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
public class ReportService  {
	//IDatabaseService dbService = HtiSmsDB.getInstance();
	// private static Map network_map = null;
	// private static Map mccmnc_map = null;
	// private RouteDAService routeService = new RouteDAServiceImpl();
	
	 private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
	  @Autowired
	    private SessionFactory sessionFactory;

	public String getCustomizedReport(String username, String role, Collection list_collection)
			throws WriteException, IOException {
		DBMessage reportDTO = null;
		String filename = IConstants.WEBAPP_DIR + "report//" + username + "_delivery.xls";
		WritableFont courier = new WritableFont(WritableFont.COURIER, 12, WritableFont.BOLD);
		WritableFont times = new WritableFont(WritableFont.ARIAL, 11);
		WritableCellFormat courierformat = new WritableCellFormat(courier);
		WritableCellFormat timesformat = new WritableCellFormat(times);
		courierformat.setAlignment(jxl.format.Alignment.CENTRE);
		courierformat.setBackground(Colour.GREY_25_PERCENT);
		courierformat.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);
		timesformat.setAlignment(Alignment.LEFT);
		timesformat.setBackground(Colour.LIGHT_GREEN);
		timesformat.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.DARK_GREEN);
		WritableWorkbook workbook = Workbook.createWorkbook(new File(filename));
		int sheet_number = 0;
		WritableSheet sheet = null;
		List list = new ArrayList(list_collection);
		while (!list.isEmpty()) {
			++sheet_number;
			sheet = workbook.createSheet(username + "(" + sheet_number + ")", sheet_number);
			sheet.getSettings().setDefaultColumnWidth(25);
			sheet.addCell(new Label(0, 0, "MsgId", courierformat));
			sheet.addCell(new Label(1, 0, "Country", courierformat));
			sheet.addCell(new Label(2, 0, "Operator", courierformat));
			sheet.addCell(new Label(3, 0, "Destination", courierformat));
			sheet.addCell(new Label(4, 0, "SenderId", courierformat));
			sheet.addCell(new Label(5, 0, "Submit_time", courierformat));
			sheet.addCell(new Label(6, 0, "Done_time", courierformat));
			sheet.addCell(new Label(7, 0, "Status", courierformat));
			sheet.addCell(new Label(8, 0, "Cost", courierformat));
			if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system")) {
				sheet.addCell(new Label(9, 0, "ResponseId", courierformat));
				sheet.addCell(new Label(10, 0, "Route", courierformat));
			}
			int rowNum = 1;
			while (!list.isEmpty()) {
				reportDTO = (DBMessage) list.remove(0);
				sheet.addCell(new Label(0, rowNum, reportDTO.getHtiMsgId(), timesformat));
				sheet.addCell(new Label(1, rowNum, reportDTO.getCountry(), timesformat));
				sheet.addCell(new Label(2, rowNum, reportDTO.getOperator(), timesformat));
				sheet.addCell(new Label(3, rowNum, reportDTO.getDestination(), timesformat));
				sheet.addCell(new Label(4, rowNum, reportDTO.getSender(), timesformat));
				sheet.addCell(new Label(5, rowNum, reportDTO.getTimestamp(), timesformat));
				sheet.addCell(new Label(6, rowNum, reportDTO.getDoneTime(), timesformat));
				sheet.addCell(new Label(7, rowNum, reportDTO.getStatus(), timesformat));
				sheet.addCell(new Label(8, rowNum, reportDTO.getCost(), timesformat));
				if (role.equalsIgnoreCase("superadmin") || role.equalsIgnoreCase("system")) {
					sheet.addCell(new Label(9, rowNum, reportDTO.getResponseId(), timesformat));
					sheet.addCell(new Label(10, rowNum, reportDTO.getRoute(), timesformat));
				}
				if (++rowNum >= 50000) {
					break;
				}
			}
		}
		workbook.write();
		workbook.close();
		return filename;
	}

	// *******************************End Amit_vish on date 22-March-11*******************
	public Collection getMessageStatusCodes() {
		return null;
	}
	/*
	 * public Collection userInformation() throws DBException {
	 * 
	 * return dbService.userInformation(); }
	 */

	// @*********** @Added by Amit_vish*****************
	public static String getFormattedAmountX(String strPrice, int roundDigit) {
		double price = 0;
		try {
			price = Double.parseDouble(strPrice);
			BigDecimal d = new BigDecimal(price);
			NumberFormat formater = null;
			if (roundDigit == 2) {
				formater = new DecimalFormat("0.00");
			} else if (roundDigit == 3) {
				formater = new DecimalFormat("0.000");
			} else if (roundDigit == 4) {
				formater = new DecimalFormat("0.0000");
			} else if (roundDigit == 5) {
				formater = new DecimalFormat("0.00000");
			}
			strPrice = formater.format(d);
		} catch (Exception ex) {
			// System.out.println("EXC(strPrice)::"+strPrice);
		}
		return strPrice;
	}

	public String getCoverageReport(String username, String format, boolean cached)
			throws WriteException, IOException, DocumentException, SQLException {
		String filename = null;
		Collection<RouteEntryExt> list = listCoverage(username, true, cached).values();
		if (format.equalsIgnoreCase("xls")) {
			filename = getCoverageReportXLS(username, list);
		} else if (format.equalsIgnoreCase("pdf")) {
			filename = getCoverageReportPDF(username, list);
		} else {
			filename = getCoverageReportCSV(username, list);
		}
		return filename;
	}

		public Map<Integer, RouteEntryExt> listCoverage(String systemId, boolean display, boolean cached) {
		int userId = GlobalVars.UserMapping.get(systemId);
		return listCoverage(userId, display, cached);
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
			logger.info("listing RouteEntries From Database: " + userId);
			List<RouteEntry> db_list = listRoute(userId);
			for (RouteEntry basic : db_list) {
				RouteEntryExt entry = new RouteEntryExt(basic);
				if (display) {
					// ------ set user values -----------------
					if (GlobalVars.UserEntries.containsKey(entry.getBasic().getUserId())) {
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
		}
		return list;
	}


public Map<Integer, String> listNames() {
		Map<Integer, String> names = new HashMap<Integer, String>();
		for (SmscEntry entry : GlobalVars.SmscEntries.values()) {
			names.put(entry.getId(), entry.getName());
		}
		names = names.entrySet().stream().sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return names;
	}


	public Map<Integer, String> listGroupNames() {
		Map<Integer, String> names = new HashMap<Integer, String>();
		names.put(0, "NONE");
		List<GroupEntry> groups = listGroup();
		for (GroupEntry entry : groups) {
			names.put(entry.getId(), entry.getName());
		}
		names = names.entrySet().stream().sorted(Entry.comparingByValue())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
		return names;
	}


	public List<GroupEntry> listGroup() {
	    List<GroupEntry> results = new ArrayList<>();
	    Session session = null;
	    Transaction transaction = null;

	    logger.info("Listing Group Entries");
	    try {
	        session = sessionFactory.openSession();
	        transaction = session.beginTransaction();

	        CriteriaBuilder builder = session.getCriteriaBuilder();
	        CriteriaQuery<GroupEntry> query = builder.createQuery(GroupEntry.class);
	        Root<GroupEntry> root = query.from(GroupEntry.class);
	        query.select(root).where(builder.gt(root.get("id"), 0)); // Assuming 'id' is of a numeric type

	        results = session.createQuery(query).getResultList();

	        transaction.commit();
	        logger.info("GroupEntry list: {}", results.size());
	    } catch (RuntimeException e) {
	        if (transaction != null) {
	            transaction.rollback();
	        }
	        logger.error("Error retrieving group entries: {}", e.getMessage());
	    } finally {
	        if (session != null && session.isOpen()) {
	            session.close();
	        }
	    }
	    return results;
	}


public List<RouteEntry> listRoute(int userId) {
    logger.debug("Listing Users Routing");
    Transaction transaction = null;
    List<RouteEntry> results = new ArrayList<>();
    
    try (Session session = sessionFactory.openSession()) {
        transaction = session.beginTransaction();

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<RouteEntry> query = builder.createQuery(RouteEntry.class);
        Root<RouteEntry> root = query.from(RouteEntry.class);

        // Since userId is an int, it can't be null or have a length. We just check if it's a valid ID.
        if (userId > 0) { // Assuming userId > 0 is considered valid
            query.where(builder.equal(root.get("userId"), userId));
        }

        results = session.createQuery(query).getResultList();
        transaction.commit();

        logger.info("Routes fetched: {}", results.size());
    } catch (Exception e) {
        if (transaction != null) {
            transaction.rollback();
        }
        logger.error("Error retrieving routes: {}", e.getMessage());
        return Collections.emptyList();
    }
    return results;
}

	private String getCoverageReportPDF(String username, Collection<RouteEntryExt> coverageList)
			throws DocumentException, FileNotFoundException, IOException {
		String filename = IConstants.WEBAPP_DIR + "report//" + username + "_coverage.pdf";
		Document document = new Document(PageSize.A4, 5, 5, 35, 35);
		PdfWriter.getInstance(document, new FileOutputStream(filename));
		// ---Font Definitions------------------------
		Font font_header = new Font(Font.TIMES_ROMAN, 18, 1, Color.BLUE);
		Font font_headLine = new Font(Font.TIMES_ROMAN, 12, 1, Color.WHITE);
		Font font_footer = new Font(Font.COURIER, 10, 1, Color.BLUE);
		Font font_ConHead = new Font(Font.COURIER, 11, 1, Color.red);
		Font font_Content = new Font(Font.TIMES_ROMAN, 10, 1, Color.BLACK);
		// ---Font Definitions------------------------
		Image logo = Image.getInstance(IConstants.WEBAPP_DIR + "//images//logo.jpg");
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
		int NumColumns = 6;
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
		// --------------------------------------------------Adding Tables to Document-------------------
		document.add(head_line);
		document.add(Under_line);
		document.add(datatable);
		// --------------------------------------------------Adding Tables to Document-------------------
		document.close();
		return filename;
	}

	private String getCoverageReportXLS(String username, Collection<RouteEntryExt> coverageList)
			throws WriteException, IOException {
		String filename = IConstants.WEBAPP_DIR + "report//" + username + "_coverage.xls";
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

	public String getCoverageReportXLS(String username) throws WriteException, IOException {
		Collection<RouteEntryExt> list = listCoverage(username, true, true).values();
		System.out.println(username + " CoverageList Size: " + list.size());
		String filename = IConstants.WEBAPP_DIR + "report//" + username + "_coverage.xls";
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
		sheet.addCell(new Label(4, 0, "OldCost", courierformat));
		sheet.addCell(new Label(5, 0, "NewCost", courierformat));
		sheet.addCell(new Label(6, 0, "EffectiveOn(GMT +2)", courierformat));
		sheet.addCell(new Label(7, 0, "Currency", courierformat));
		sheet.addCell(new Label(8, 0, "Remarks", courierformat));
		int rowNum = 1;
		for (RouteEntryExt entry : list) {
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
			sheet.addCell(new Label(5, rowNum, String.valueOf(entry.getBasic().getCost()), timesformat));
			sheet.addCell(new Label(6, rowNum, "No Change", timesformat));
			sheet.addCell(new Label(7, rowNum, entry.getCurrency(), timesformat));
			sheet.addCell(new Label(8, rowNum, entry.getBasic().getRemarks(), timesformat));
			rowNum++;
		}
		workbook.write();
		workbook.close();
		return filename;
	}
	// @*********** @ by Amit_vish*****************

	private String getCoverageReportCSV(String username, Collection<RouteEntryExt> coverageList) throws IOException { // mimansha
		System.out.println("CoverageList Size: " + coverageList.size());
		String filename = IConstants.WEBAPP_DIR + "report//" + username + "_coverage.csv";
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
				mnc = mnc.replaceAll(",", "|");
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
}

