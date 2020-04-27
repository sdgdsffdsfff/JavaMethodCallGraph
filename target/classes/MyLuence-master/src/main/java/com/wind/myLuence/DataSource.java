package com.wind.myLuence;

/**
 * 数据源
 * <p>
 * 需要建立索引的数据源
 * </p>
 * 
 * @author zhouyanjun
 * @version 1.0 2014-4-24
 */
public interface DataSource extends Configurable {
	/**
	 * 打开最初数据
	 */
	public void InitialDataOpen();

	/**
	 * 打开新增数据
	 */
	public void IncreaseDataOpen();
}
