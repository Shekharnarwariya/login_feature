package com.hti.smpp.common.httpclient;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLResultHandler extends DefaultHandler {
	// List to hold Employees object
	private List<BaseApiDTO> bulkList = null;
	private BaseApiDTO bulk = null;
	private String tmpValue = null;

	// getter method for employee list
	public List<BaseApiDTO> getBulkList() {
		return bulkList;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		// System.out.println("NODE: " + qName + " localname: " + localName + " URI: " + uri);
		if (qName.equalsIgnoreCase("enquiry")) {
			// create a new Employee and put it in Map
			tmpValue = null;
			bulk = new BaseApiDTO();
			// initialize list
			if (bulkList == null)
				bulkList = new ArrayList<BaseApiDTO>();
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		// System.out.println("EndNODE: " + qName + " tmp:" + tmpValue);
		if (qName.equalsIgnoreCase("enquiry")) {
			// add Employee object to list
			bulkList.add(bulk);
		} else if (qName.equalsIgnoreCase("username")) {
			bulk.setUsername(tmpValue);
		} else if (qName.equalsIgnoreCase("password")) {
			bulk.setPassword(tmpValue);
		} else if (qName.equalsIgnoreCase("accesskey")) {
			bulk.setAccessKey(tmpValue);
		} else if (qName.equalsIgnoreCase("batch_id")) {
			bulk.setWebid(tmpValue);
		}
	}

	@Override
	public void characters(char ch[], int start, int length) throws SAXException {
		tmpValue = new String(ch, start, length);
	}
}
