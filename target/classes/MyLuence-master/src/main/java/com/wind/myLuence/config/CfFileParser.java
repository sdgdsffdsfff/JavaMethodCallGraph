package com.wind.myLuence.config;

import java.io.IOException;
import java.util.Map;
import com.wind.myLuence.Context;

/**
 * 配置文件解析
 * 
 * @author zhouyanjun
 * @version 1.0 2014-8-23
 */
public abstract class CfFileParser {

	/**
	 * 配置项
	 */
	public enum ConfigItem {
		ANALYZEDINDEXFIELDS("analyzedIndexFields"),
		NOTANALYZEDINDEXFIELDS("notAnalyzedIndexFields"),
		STOREFIELDS("storeFields"),
		INDEXFILEPATH("indexFilePath"),
		BEANCLASS("beanClass"),
		ANALYZERCLASS("analyzerClass"),
		INDEXSOURCE("indexsource"),
		INCFIELD("incField"),
		DATAFILTER("dataFilter");

		public String value;

		private ConfigItem(String value) {
			this.value = value;
		}
	}

	private String filePath; // 配置文件路径

	public abstract Map<String, Context> parse() throws IOException;

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
}
