package com.wind.myLuence;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertyParser {

	public static Map<String, String> parse(String filePath) throws IOException {
		Properties prop = new Properties();
		prop.load(PropertyParser.class.getResourceAsStream(filePath));
		Map<String, String> configs = new HashMap<String, String>();
		Enumeration<?> keys = prop.keys();
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			String value = (String) prop.get(key);
			configs.put(key, value);
		}
		return configs;
	}

	public static void main(String[] args) {
		try {
			Map<String, String> map = PropertyParser.parse("/data/lucence_strategy.properties");
			for (String key : map.keySet()) {
				System.out.println(key + "@" + map.get(key));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
