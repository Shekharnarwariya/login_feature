package com.hti.smpp.common.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeConverter {

	public static LocalDateTime UTC(LocalDateTime date) {
		ZonedDateTime ldtZoned = date.atZone(ZoneId.systemDefault());

		ZonedDateTime utcZoned = ldtZoned.withZoneSameInstant(ZoneId.of("UTC"));
		return utcZoned.toLocalDateTime();
	}
}
