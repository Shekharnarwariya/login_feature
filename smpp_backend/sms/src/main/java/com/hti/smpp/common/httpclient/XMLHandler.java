package com.hti.smpp.common.httpclient;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLHandler extends DefaultHandler {
	// List to hold Employees object
	private List<BaseApiDTO> bulkList = null;
	private BaseApiDTO bulk = null;
	private String tmpValue = null;
	private int format = 1;
	private String[] custom_arr;

	// getter method for bulk list
	public List<BaseApiDTO> getBulkList() {
		return bulkList;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		// System.out.println("NODE: " + qName + " localname: " + localName + " URI: " + uri);
		// System.out.println("Start: " + qName);
		if (qName.equalsIgnoreCase("SMS")) {
			String formattype = attributes.getValue("format");
			if (formattype != null) {
				if (formattype.equalsIgnoreCase("1") || formattype.equalsIgnoreCase("2")) {
					format = Integer.parseInt(formattype);
				} else {
					System.out.println("Invalid Format Specified: " + formattype);
				}
			}
			// System.out.println("Format: " + format);
			tmpValue = null;
			bulk = new BaseApiDTO();
			bulk.setFormat(format);
			// initialize list
			if (bulkList == null)
				bulkList = new ArrayList<BaseApiDTO>();
		} else if (qName.equalsIgnoreCase("custom")) {
			custom_arr = new String[2];
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		// System.out.println("EndNODE: " + qName + " tmp:" + tmpValue);
		if (qName.equalsIgnoreCase("SMS")) {
			// add bulk object to list
			bulkList.add(bulk);
		} else if (qName.equalsIgnoreCase("username")) {
			bulk.setUsername(tmpValue);
		} else if (qName.equalsIgnoreCase("password")) {
			bulk.setPassword(tmpValue);
		} else if (qName.equalsIgnoreCase("accesskey")) {
			bulk.setAccessKey(tmpValue);
		} else if (qName.equalsIgnoreCase("type")) {
			bulk.setType(Integer.parseInt(tmpValue));
		} else if (qName.equalsIgnoreCase("sender")) {
			bulk.setSender(tmpValue);
		} else if (qName.equalsIgnoreCase("schtime")) {
			bulk.setScheduleTime(tmpValue);
		} else if (qName.equalsIgnoreCase("gmt")) {
			bulk.setGmt(tmpValue);
		} else if (qName.equalsIgnoreCase("peid")) {
			bulk.setPeId(tmpValue);
		} else if (qName.equalsIgnoreCase("templateid")) {
			bulk.setTemplateId(tmpValue);
		} else if (qName.equalsIgnoreCase("tmid")) {
			bulk.setTelemarketerId(tmpValue);
		} else if (qName.equalsIgnoreCase("custom")) {
			bulk.getCustomReceipients().add(custom_arr);
		} else {
			if (format == 1) {
				if (qName.equalsIgnoreCase("text")) {
					bulk.setText(tmpValue);
				} else if (qName.equalsIgnoreCase("gsm")) {
					bulk.getReceipients().add(tmpValue);
				} else if (qName.equalsIgnoreCase("limit")) {
					bulk.setLimit(Integer.parseInt(tmpValue));
				} else if (qName.equalsIgnoreCase("group")) {
					bulk.getGroup().add(tmpValue);
				}
			} else {
				if (qName.equalsIgnoreCase("gsm")) {
					custom_arr[0] = tmpValue;
				} else if (qName.equalsIgnoreCase("text")) {
					custom_arr[1] = tmpValue;
				}
			}
		}
	}

	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		tmpValue = new String(ch, start, length);
	}
}
