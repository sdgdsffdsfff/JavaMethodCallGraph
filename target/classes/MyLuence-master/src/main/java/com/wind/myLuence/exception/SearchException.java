package com.wind.myLuence.exception;

/**
 * 搜索异常
 * 
 * @author zhouyanjun
 * @version 1.0 2015-1-13
 */
public class SearchException extends Exception {

	private static final long serialVersionUID = -2940773275100427250L;

	public SearchException() {}

	public SearchException(String message) {
		super(message);
	}

	public SearchException(String message, Throwable cause) {
		super(message, cause);
	}
}
