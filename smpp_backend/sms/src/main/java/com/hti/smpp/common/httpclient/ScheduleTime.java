/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hti.smpp.common.httpclient;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Abhishek Jain
 */
public class ScheduleTime {
	public Date getScheduleTime(Calendar cuttTime, String Syear, String Smonth, String Sday, String Shour,
			String Smin) {
		// ****************************************************************
		int Chour = cuttTime.get(Calendar.HOUR); // 0..11
		int Cminutes = cuttTime.get(Calendar.MINUTE); // 0..59
		// int Cseconds = cuttTime.get(Calendar.SECOND); // 0..59
		boolean am = cuttTime.get(Calendar.AM_PM) == Calendar.AM;
		int Cday = cuttTime.get(Calendar.DAY_OF_MONTH);
		int Cmonth = cuttTime.get(Calendar.MONTH);
		int Cyear = cuttTime.get(Calendar.YEAR);
		if (!am) {
			Chour = Chour + 12;
		}
		// ***************************************************************
		int Hyear = Integer.parseInt(Syear);
		int Hmonth = Integer.parseInt(Smonth);
		int Hday = Integer.parseInt(Sday);
		int Hhour = Integer.parseInt(Shour);
		int Hmin = Integer.parseInt(Smin);
		Date Cdate = new Date(Cyear, Cmonth, Cday, Chour, Cminutes);
		Date Sdate = new Date(Hyear, (Hmonth - 1), Hday, Hhour, Hmin);
		long diff = Sdate.getTime() - Cdate.getTime();
		// long diffSeconds = diff / 1000;
		long diffMinutes = diff / (60 * 1000);
		// long diffHours = diff / (60 * 60 * 1000);
		// long diffDays = diff / (24 * 60 * 60 * 1000);
		Timestamp TIME = new java.sql.Timestamp(System.currentTimeMillis());
		Date sysDate = (Date) TIME;
		Calendar calendar3 = Calendar.getInstance();
		calendar3.setTime(sysDate);
		calendar3.add(Calendar.MINUTE, (int) diffMinutes);
		Date dt = calendar3.getTime();
		// System.out.println("Date" + dt);
		return dt;
	}
}
