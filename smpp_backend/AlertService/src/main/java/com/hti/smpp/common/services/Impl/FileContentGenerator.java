package com.hti.smpp.common.services.Impl;

import java.io.File;
import java.util.List;

import com.hti.smpp.common.request.DBMessage;
import com.hti.smpp.common.util.IConstants;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class FileContentGenerator {
	public static String createDlrXLSContent(List<DBMessage> report, String username) {
		String filename = IConstants.WEBAPP_DIR + "report//" + username + "_Delivery.xls";
		WritableFont heading = new WritableFont(WritableFont.TAHOMA, 13, WritableFont.BOLD, false,
				UnderlineStyle.NO_UNDERLINE, Colour.WHITE);
		WritableFont courier = new WritableFont(WritableFont.COURIER, 12, WritableFont.BOLD);
		WritableFont times = new WritableFont(WritableFont.ARIAL, 11);
		WritableCellFormat courierformat = new WritableCellFormat(courier);
		WritableCellFormat timesformat = new WritableCellFormat(times);
		WritableCellFormat headformat1 = new WritableCellFormat(heading);
		WritableCellFormat headformat2 = new WritableCellFormat(heading);
		try {
			File file = new File(filename);
			courierformat.setAlignment(jxl.format.Alignment.CENTRE);
			courierformat.setBackground(Colour.GREY_25_PERCENT);
			courierformat.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);
			timesformat.setAlignment(Alignment.LEFT);
			timesformat.setBackground(Colour.LIGHT_GREEN);
			timesformat.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.DARK_GREEN);
			headformat1.setAlignment(Alignment.CENTRE);
			headformat1.setBackground(Colour.BLUE_GREY);
			headformat1.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.RED);
			headformat2.setAlignment(Alignment.LEFT);
			headformat2.setBackground(Colour.DARK_YELLOW);
			headformat2.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.RED);
			WritableWorkbook workbook = Workbook.createWorkbook(file);
			WritableSheet sheet = null;
			// -------------------------------------------------------
			int sheet_number = 0;
			while (!report.isEmpty()) {
				++sheet_number;
				sheet = workbook.createSheet("Sheet-" + sheet_number, sheet_number);
				// sheet.mergeCells(0, 0, 1, 0);
				sheet.mergeCells(1, 0, 2, 0);
				sheet.getSettings().setDefaultColumnWidth(25);
				sheet.addCell(new Label(0, 0, "Username :" + username, headformat1));
				sheet.addCell(new Label(0, 2, "Sender", courierformat));
				sheet.addCell(new Label(1, 2, "Mobile", courierformat));
				sheet.addCell(new Label(2, 2, "Submit_Time", courierformat));
				sheet.addCell(new Label(3, 2, "Deliver_Time", courierformat));
				sheet.addCell(new Label(4, 2, "Status", courierformat));
				int rowNum = 3;
				int row_counter = 0;
				while (!report.isEmpty()) {
					DBMessage dBMessage = (DBMessage) report.remove(0);
					sheet.addCell(new Label(0, rowNum, dBMessage.getSender(), timesformat));
					sheet.addCell(new Label(1, rowNum, dBMessage.getDestination(), timesformat));
					sheet.addCell(new Label(2, rowNum, dBMessage.getSub_Time(), timesformat));
					sheet.addCell(new Label(3, rowNum, dBMessage.getDoneTime(), timesformat));
					sheet.addCell(new Label(4, rowNum, dBMessage.getStatus(), timesformat));
					rowNum = rowNum + 1;
					if (++row_counter >= 50000) {
						break;
					}
				}
			}
			workbook.write();
			workbook.close();
		} catch (Exception ex) {
			System.out.println("Exception ::" + ex);
		}
		return filename;
	}
	}

