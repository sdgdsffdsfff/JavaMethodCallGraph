package com.wind.myLuence.source;

import com.wind.myLuence.DataSource;
import com.wind.myLuence.Index;

/**
 * 数据库源
 * 
 * @author zhouyanjun
 * @version 1.0 2014-4-24
 */
public abstract class IndexSource implements DataSource {
	protected Index index;

	public Index getIndex() {
		return index;
	}

	public void setIndex(Index index) {
		this.index = index;
	}
}
