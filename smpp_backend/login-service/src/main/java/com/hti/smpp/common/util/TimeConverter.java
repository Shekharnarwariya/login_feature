package com.hti.smpp.common.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
/**
 * The {@code TimeConverter} class provides utility methods for converting time units.
 */
public class TimeConverter {
/**
 * Converts a {@code LocalDateTime} object to Coordinated Universal Time (UTC).
 * @param date
 * @return
 */
	public static LocalDateTime UTC(LocalDateTime date) {
		ZonedDateTime ldtZoned = date.atZone(ZoneId.systemDefault());

		ZonedDateTime utcZoned = ldtZoned.withZoneSameInstant(ZoneId.of("UTC"));
		return utcZoned.toLocalDateTime();
	}
}
