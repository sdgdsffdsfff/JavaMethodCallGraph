package com.wind.myLuence.util;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class ClassUtil {

	public static Map<String, String> getFieldNameType(Class<?> clazz) {
		Map<String, String> fieldNameTypeMap = new HashMap<String, String>();
		if (clazz != null) {
			Field[] fields = clazz.getDeclaredFields();
			for (int j = 0; j < fields.length; j++) {
				String fieldName = fields[j].getName();
				String classPath = fields[j].getType().getName();
				fieldNameTypeMap.put(fieldName, classPath);
			}
		}
		return fieldNameTypeMap;
	}

	public static void main(String[] args) throws ClassNotFoundException {
		Map<String, String> fields = getFieldNameType(Class.forName("com.snail.cloudlevel.app.assistant.res.YdlCrawlStrategy"));
		for (String field : fields.keySet()) {
			System.out.println(field + "  :" + fields.get(field));
		}
	}
}
