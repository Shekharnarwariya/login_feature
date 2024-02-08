package com.hti.smpp.common.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * The {@code TimeConverter} class provides utility methods for converting time
 * units.
 */
public class TimeConverter {
	/**
	 * Converts a {@code LocalDateTime} object to Coordinated Universal Time (UTC).
	 * 
	 * @param localDateTime The local date and time to be converted to UTC.
	 * @return The {@code LocalDateTime} in UTC.
	 */
	public static LocalDateTime toUtc(LocalDateTime localDateTime) {
		ZonedDateTime localZonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
		ZonedDateTime utcZonedDateTime = localZonedDateTime.withZoneSameInstant(ZoneId.of("UTC"));
		return utcZonedDateTime.toLocalDateTime();
	}
}
