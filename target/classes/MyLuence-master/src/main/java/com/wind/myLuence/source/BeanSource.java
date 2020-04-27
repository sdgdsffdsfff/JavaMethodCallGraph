package com.wind.myLuence.source;

import com.wind.myLuence.Context;
import com.wind.myLuence.DataSource;
import com.wind.myLuence.Index;
import com.wind.myLuence.exception.ConfigurableException;

/**
 * Bean源
 * 
 * @author zhouyanjun
 * @version 1.0 2014-4-24
 */
public class BeanSource implements DataSource {

	public void configure(Context context) throws ConfigurableException {

	}

	public void open(Index index) {}

	@Override
	public void InitialDataOpen() {

	}

	@Override
	public void IncreaseDataOpen() {

	}
}
