package com.wind.myLuence;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Context {
	private Map<String, String> parameters;

	public Context() {
		parameters = Collections.synchronizedMap(new HashMap<String, String>());
	}

	public Context(Map<String, String> paramters) {
		this();
		this.putAll(paramters);
	}

	public void clear() {
		parameters.clear();
	}

	public void putAll(Map<String, String> map) {
		parameters.putAll(map);
	}

	public void put(String key, String value) {
		parameters.put(key, value);
	}

	public Boolean getBoolean(String key, Boolean defaultValue) {
		String value = get(key);
		if (value != null) {
			return Boolean.parseBoolean(value.trim());
		}
		return defaultValue;
	}
	
	public Boolean getBoolean(String key) {
		return getBoolean(key, null);
	}

	public Integer getInteger(String key, Integer defaultValue) {
		String value = get(key);
		if (value != null) {
			return Integer.parseInt(value.trim());
		}
		return defaultValue;
	}

	private String get(String key, String defaultValue) {
		String result = parameters.get(key);
		if (result != null) {
			return result;
		}
		return defaultValue;
	}

	public Integer getInteger(String key) {
		return getInteger(key, null);
	}

	public Long getLong(String key, Long defaultValue) {
		String value = get(key);
		if (value != null) {
			return Long.parseLong(value.trim());
		}
		return defaultValue;
	}

	public Long getLong(String key) {
		return getLong(key, null);
	}

	public String getString(String key) {
		return get(key);
	}

	public String getString(String key, String defaultValue) {
		return get(key, defaultValue);
	}

	private String get(String key) {
		return get(key, null);
	}

	public String toString() {
		return "{ parameters:" + parameters + " }";
	}
}
