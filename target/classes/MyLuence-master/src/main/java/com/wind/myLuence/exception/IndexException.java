package com.wind.myLuence.exception;

/**
 * 索引异常
 * 
 * @author zhouyanjun
 * @version 1.0 2015-1-13
 */
public class IndexException extends Exception {

	private static final long serialVersionUID = -2049475935211044964L;

	public IndexException() {}

	public IndexException(String message) {
		super(message);
	}

	public IndexException(String message, Throwable cause) {
		super(message, cause);
	}
}
