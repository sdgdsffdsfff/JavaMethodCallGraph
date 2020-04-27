package com.wind.myLuence;

import com.wind.myLuence.exception.ConfigurableException;

/**
 * 配置类
 * 
 * @author zhouyanjun
 * @version 1.0 2014-1-2
 */
public interface Configurable {
	public void configure(Context context) throws ConfigurableException, ClassNotFoundException;
}
