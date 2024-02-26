package com.hti.smpp.common.services.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.hti.smpp.common.dto.MccMncDTO;
import com.hti.smpp.common.exception.InternalServerException;

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FileContentGenerator {
	
	WritableFont courier = new WritableFont(WritableFont.COURIER, 12, WritableFont.BOLD);
    WritableFont times = new WritableFont(WritableFont.ARIAL, 11);
    WritableFont infoFont = new WritableFont(WritableFont.ARIAL, 9);
    WritableCellFormat courierformat = new WritableCellFormat(courier);
    WritableCellFormat headformat = new WritableCellFormat(courier);
    WritableCellFormat timesformat = new WritableCellFormat(times);
    WritableCellFormat unlocked = new WritableCellFormat(times);
    WritableCellFormat infoFormat = new WritableCellFormat(infoFont);
    WritableWorkbook workbook = null;
    WritableSheet sheet = null;
    File file = null;
    String fileName = null;
    
    private static final Logger logger = LoggerFactory.getLogger(FileContentGenerator.class);
    
    @Autowired
	private MessageResourceBundle messageResourceBundle;


    public boolean createMccMncContent(ArrayList<MccMncDTO> list, String filename) {
        System.out.println("<---- File Creating with " + list.size() + " Records ---> ");
    	logger.info("<---- " + messageResourceBundle.getLogMessage("network.info.fileCreating") + " ---> ", list.size());
        MccMncDTO mccMncDTO = null;
        try {
            file = new File(filename);

            courierformat.setAlignment(Alignment.CENTRE);
            courierformat.setBackground(Colour.GREY_25_PERCENT);
            courierformat.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);
            timesformat.setAlignment(Alignment.LEFT);
            timesformat.setBackground(Colour.LIGHT_GREEN);
            timesformat.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.DARK_GREEN);
            workbook = Workbook.createWorkbook(file);
            int sheet_number = 0;
            //   while (!list.isEmpty()) {
            sheet = workbook.createSheet("Sheet(" + sheet_number + ")", sheet_number);
            sheet.getSettings().setDefaultColumnWidth(25);
            sheet.addCell(new Label(0, 0, "Country Code", courierformat));
            sheet.addCell(new Label(1, 0, "Country", courierformat));
            sheet.addCell(new Label(2, 0, "Operator", courierformat));
            sheet.addCell(new Label(3, 0, "MCC", courierformat));
            sheet.addCell(new Label(4, 0, "MNC", courierformat));
            sheet.addCell(new Label(5, 0, "Prefix", courierformat));
            sheet.addCell(new Label(6, 0, "DataBase_ID", courierformat));
            int rowNum = 1;
            while (!list.isEmpty()) {
                mccMncDTO = (MccMncDTO) list.remove(0);
                sheet.addCell(new Label(0, rowNum, mccMncDTO.getCc(), timesformat));
                sheet.addCell(new Label(1, rowNum, mccMncDTO.getCountry(), timesformat));
                sheet.addCell(new Label(2, rowNum, mccMncDTO.getOperator(), timesformat));
                sheet.addCell(new Label(3, rowNum, mccMncDTO.getMcc(), timesformat));
                sheet.addCell(new Label(4, rowNum, mccMncDTO.getMnc(), timesformat));
                
                String value = mccMncDTO.getPrefix();
                if(value.length()>32767) {
                	value = value.substring(0, 32767);
                }
                sheet.addCell(new Label(5, rowNum, value, timesformat));
                
                sheet.addCell(new Label(6, rowNum, mccMncDTO.getId() + "", timesformat));
                rowNum = rowNum + 1;
                
            }
            workbook.write();
            return true;
        } catch (IOException | WriteException ex) {
        	ex.printStackTrace();
            System.err.println(ex.getLocalizedMessage());
        	logger.error(messageResourceBundle.getLogMessage("network.error.createFile"), ex.getMessage(), ex);
            return false;
        } finally {
            // Close the workbook in the finally block to ensure it's closed even if an exception occurs
            try {
                if (workbook != null) {
                    workbook.close();
                }
            } catch (IOException | WriteException e) {
                System.out.println(e.toString());
                throw new InternalServerException(e.getLocalizedMessage());
            }
        }
    }
}
