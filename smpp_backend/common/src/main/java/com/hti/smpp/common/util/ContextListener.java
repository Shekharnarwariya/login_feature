package com.hti.smpp.common.util;

import java.io.InputStream;
import java.util.Properties;

public class ContextListener {
	public static Properties property = new Properties();

	static {
		try (InputStream input = ContextListener.class.getClassLoader()
				.getResourceAsStream("ApplicationResources.properties")) {
			property.load(input);
		} catch (Exception e) {
			e.printStackTrace(); // Handle the exception appropriately
		}
	}
}
