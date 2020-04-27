package com.wind.myLuence.exception;

/**
 * 配置异常
 * 
 * @author zhouyanjun
 * @version 1.0 2015-1-13
 */
public class ConfigurableException extends Exception {

	private static final long serialVersionUID = 7750826735020761837L;

	public ConfigurableException() {}

	public ConfigurableException(String message) {
		super(message);
	}

	public ConfigurableException(String message, Throwable cause) {
		super(message, cause);
	}
}
