package com.hti.smpp.common.httpclient;

import java.text.ParseException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author ABHISHEK JAIN 09/02/2010
 */
public class CurrTimeZone {
	public Calendar CurrentTime(String gmt) throws ParseException {
		Calendar cal = new GregorianCalendar(TimeZone.getTimeZone(gmt));
		int hour = cal.get(Calendar.HOUR); // 0..11
		// int minutes = cal.get(Calendar.MINUTE); // 0..59
		// int seconds = cal.get(Calendar.SECOND); // 0..59
		boolean am = cal.get(Calendar.AM_PM) == Calendar.AM;
		// int day = cal.get(Calendar.DAY_OF_MONTH);
		int month = cal.get(Calendar.MONTH);
		// int year = cal.get(Calendar.YEAR);
		if (!am) {
			hour = hour + 12;
		}
		month = month + 1;
		return (cal);
	}
}
