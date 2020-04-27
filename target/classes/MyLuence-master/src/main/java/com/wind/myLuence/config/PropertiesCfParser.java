package com.wind.myLuence.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.wind.myLuence.Context;
import com.wind.myLuence.PropertyParser;

/**
 * 属性配置文件解析器
 * 
 * @author zhouyanjun
 * @version 1.0 2015-1-13
 */
public class PropertiesCfParser extends CfFileParser {

	@Override
	public Map<String, Context> parse() throws IOException {
		Properties prop = new Properties();
		prop.load(PropertyParser.class.getResourceAsStream(this.getFilePath()));
		Map<String, String> configs = new HashMap<String, String>(); // 所有配置项与值
		Enumeration<?> keys = prop.keys();
		List<String> cfItemArray = new ArrayList<String>(); // 配置实体集合
		while (keys.hasMoreElements()) {
			String key = (String) keys.nextElement();
			if (key.contains(ConfigItem.BEANCLASS.value)) {
				cfItemArray.add(key.split("\\.")[0]);
			}
			String value = (String) prop.get(key);
			configs.put(key, value);
		}
		/**
		 * 组合不同实体配置
		 */
		Map<String, Context> results = new HashMap<String, Context>();
		for (String cfItem : cfItemArray) {
			Context context = new Context();
			for (String key : configs.keySet()) {
				if (key.contains(cfItem)) {
					context.put(key.split("\\.")[1], configs.get(key));
				}
			}
			results.put(cfItem, context);
		}
		return results;
	}
}
