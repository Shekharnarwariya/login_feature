package com.hti.smpp.common.util;

import java.util.Calendar;
import java.util.regex.Pattern;

public class Validation {

	public static boolean isValidNumber(String number) {

		String regex = "^[0-9]{10}$";
		return Pattern.matches(regex, number);
	}

	public static String getTodayDateFormat() {
		String Return = "";
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);

		month++;

		int daya = cal.get(Calendar.DAY_OF_MONTH);
		int year = cal.get(Calendar.YEAR);
		Return += year + "-";

		if (month < 10)
			Return += "0";

		Return += month + "-";

		if (daya < 10)
			Return += "0";

		Return += daya;

		return Return;
	}

}
