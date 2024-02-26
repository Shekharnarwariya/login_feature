package com.hti.smpp.common.services.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import com.hti.smpp.common.dto.MccMncDTO;
import com.hti.smpp.common.exception.InternalServerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hti.smpp.common.util.MessageResourceBundle;



public class FileDataParser {
	
    private String cell = null;
    private ArrayList<MccMncDTO> list = null;
    
	private final Logger logger = LoggerFactory.getLogger(NetworkServiceImpl.class);

	@Autowired
	private MessageResourceBundle messageResourceBundle;
    
    public ArrayList<MccMncDTO> getMccMncList(MultipartFile list_file) {
    	
    	list = new ArrayList<>();
        MccMncDTO mccMncDTO = null;
        try {
        	Workbook workbook = null;
        	if (list_file.getOriginalFilename().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(list_file.getInputStream());
            } else {
                workbook = new HSSFWorkbook(list_file.getInputStream());
                System.out.println("in workbook");
                logger.info(messageResourceBundle.getLogMessage("network.info.inWorkbook"));
            }
        	int numberOfSheets = workbook.getNumberOfSheets();
        	for (int i = 0; i < numberOfSheets; i++) {
                Sheet firstSheet = workbook.getSheetAt(i);
                System.out.println("Total Rows: " + firstSheet.getPhysicalNumberOfRows());
                logger.info(messageResourceBundle.getLogMessage("network.info.totalRows"), firstSheet.getPhysicalNumberOfRows());

                Iterator<Row> iterator = firstSheet.iterator();
                int j = 1;
                skipLabal:

                while (iterator.hasNext()) {
                    Row nextRow = iterator.next();
                    mccMncDTO = new MccMncDTO();
                    Iterator<Cell> cellIterator = nextRow.cellIterator();
                    int k = 0;
                    while (cellIterator.hasNext()) {
                    	Cell cell_obj = cellIterator.next();
                        cell = new DataFormatter().formatCellValue(cell_obj);
                        if (cell == null || cell.equals("")) {
                            System.out.println("Invalid Cell Value ->" + cell.trim() + " Skipped Row ->" + j + 1);
                        	logger.error(messageResourceBundle.getLogMessage("network.error.invalidCellValue"), cell.trim(), j + 1);
                            continue skipLabal;
                        } else {
                            if (k == 0) {
                                try {
                                    Integer.parseInt(cell.trim());
                                    mccMncDTO.setCc(cell.trim());
                                } catch (NumberFormatException ne) {
                                    System.out.println("Invalid Country Code ->" + cell.trim() + " Skipped Row ->" + j + 1);
                                	logger.error(messageResourceBundle.getLogMessage("network.error.invalidCountryCode"), cell.trim(), j + 1);

                                    continue skipLabal;
                                }
                            }
                            if (k == 1) {
                                mccMncDTO.setCountry(cell);
                            }
                            if (k == 2) {
                                mccMncDTO.setOperator(cell);
                            }
                            if (k == 3) {
                                try {
                                    Integer.parseInt(cell.trim());
                                    mccMncDTO.setMcc(cell.trim());
                                } catch (NumberFormatException ne) {
                                    System.out.println("Invalid MCC ->" + cell.trim() + " Skipped Row ->" + j + 1);
                                	logger.error(messageResourceBundle.getLogMessage("network.error.invalidMCC"), cell.trim(), j + 1);
                                	continue skipLabal;
                                }
                            }
                            if (k == 4) {
                                if (cell.length() > 0) {
                                    if (cell.contains(",")) {
                                        String mncStr = "";
                                        StringTokenizer tokenizer = new StringTokenizer(cell, ",");
                                        while (tokenizer.hasMoreTokens()) {
                                            String mnc = tokenizer.nextToken();
                                            try {
                                                Integer.parseInt(mnc);
                                                mncStr += mnc + ",";
                                            } catch (Exception ex) {
                                                System.out.println("Invalid MNC -> " + mnc);
                                            	logger.error(messageResourceBundle.getLogMessage("network.error.invalidMNC"), mnc);

                                            }
                                        }
                                        if (mncStr.length() > 0) {
                                            mncStr = mncStr.substring(0, mncStr.length() - 1);
                                            mccMncDTO.setMnc(mncStr);
                                        } else {
                                             System.out.println("Invalid MNC ->" + cell + " Skipped Row ->" + j + 1);
                                        	logger.error(messageResourceBundle.getLogMessage("network.error.invalidMNC"), cell, j + 1);
                                            continue skipLabal;
                                        }

                                    } else {
                                        try {
                                            Integer.parseInt(cell);
                                            mccMncDTO.setMnc(cell);
                                        } catch (Exception ex) {
                                             System.out.println("Invalid MNC ->" + cell + " Skipped Row ->" + j + 1);
                                        	logger.error(messageResourceBundle.getLogMessage("network.error.invalidMNC"), cell, j + 1);
                                            continue skipLabal;
                                        }
                                    }
                                } else {
                                     System.out.println("Invalid MNC ->" + cell + " Skipped Row ->" + j + 1);
                                	logger.error(messageResourceBundle.getLogMessage("network.error.invalidMNC"), cell, j + 1);
                                    continue skipLabal;
                                }
                            }
                            if (k == 5) {
                                if (mccMncDTO.getOperator() != null && mccMncDTO.getOperator().equalsIgnoreCase("Rest")) {
                                    mccMncDTO.setPrefix("0");
                                } else {
                                    mccMncDTO.setPrefix(cell.trim());
                                }

                            }
                            if (k == 6) {
                                try {
                                    int id = Integer.parseInt(cell);
                                    mccMncDTO.setId(id);
                                } catch (NumberFormatException nfe) {
                                    Ignore
                                } catch (NullPointerException ne) {
                                    Ignore
                                }
                            }
                        }
                        k++;
                    }
                    list.add(mccMncDTO);
                }
            }
        } catch(Exception e) {
        	throw new InternalServerException(e.getLocalizedMessage());
        }
        
        System.out.println("Network Update List Size ::" + list.size());
        logger.info(messageResourceBundle.getLogMessage("network.info.updateListSize"), list.size());

        if(list.isEmpty()) {
        	throw new InternalServerException("Unable to process file!");
        }
        return list;
        
    }

}
