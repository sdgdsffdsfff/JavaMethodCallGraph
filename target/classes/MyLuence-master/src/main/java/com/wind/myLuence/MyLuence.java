package com.wind.myLuence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.lucene.search.Sort;
import com.wind.myLuence.config.CfFileParser;
import com.wind.myLuence.config.PropertiesCfParser;
import com.wind.myLuence.config.XmlCfParser;
import com.wind.myLuence.exception.ConfigurableException;
import com.wind.myLuence.exception.IndexException;
import com.wind.myLuence.exception.SearchException;
import com.wind.myLuence.factory.MyLuenceFactory;
import com.wind.myLuence.search.AbstarctSearcher;
import com.wind.myLuence.source.IndexSource;

/**
 * myLuence
 * 
 * @author zhouyanjun
 * @version 1.0 2014-8-28
 */
public class MyLuence {
	private List<String> itemList = new ArrayList<String>(); // 实体List
	private String configPath; // 配置文件路径
	private Map<String, IndexSource> indexSourceMap; // 索引源Map
	private Map<String, AbstarctSearcher> searcherMap; // 搜索工具
	private MyLuenceFactory factory;
	//
	private Class<?> clazz; // bean class
	protected String incFieldName; // 增长字段名称

	// protected void test(Object data) throws Exception {

	// 		String value = null;
	// 		Method m = clazz.getMethod("get" + incFieldName.substring(0, 1).toUpperCase() + incFieldName.substring(1));
	// 		String returnValueType = m.getReturnType().getSimpleName();
	// 		Object mValue = m.invoke(data);
	// 	}

	public void init() throws IOException, ClassNotFoundException, ConfigurableException {
		indexSourceMap = new HashMap<String, IndexSource>();
		searcherMap = new HashMap<String, AbstarctSearcher>();
		CfFileParser cfFileParser = null;
		if (configPath.contains(".properties")) {
			cfFileParser = new PropertiesCfParser();
		} else if (configPath.contains(".xml")) {
			cfFileParser = new XmlCfParser();
		}
		cfFileParser.setFilePath(configPath);
		Map<String, Context> cfContextMap = cfFileParser.parse();
		itemList.addAll(cfContextMap.keySet());
		for (String cfContextKey : cfContextMap.keySet()) {
			Context context = cfContextMap.get(cfContextKey);
			IndexSource indexSource = factory.getIxSourceInstance(context.getString(CfFileParser.ConfigItem.INDEXSOURCE.value));
			indexSource.configure(context);
			indexSourceMap.put(cfContextKey, indexSource);
			AbstarctSearcher searcher = factory.getSearcherInstance();
			searcher.configure(context);
			searcherMap.put(cfContextKey, searcher);
		}
	}

	public void writeIndex() {
		for (String iSourceKey : indexSourceMap.keySet()) {
			indexSourceMap.get(iSourceKey).InitialDataOpen();
		}
	}

	public void writeIndex(String item) throws IndexException {
		if (!itemList.contains(item)) {
			throw new IndexException("item doesn't exist.");
		}
		indexSourceMap.get(item).InitialDataOpen();
	}

	public void appendIndex() {
		for (String iSourceKey : indexSourceMap.keySet()) {
			indexSourceMap.get(iSourceKey).IncreaseDataOpen();
		}
	}

	public void appendIndex(String item) throws IndexException {
		if (!itemList.contains(item)) {
			throw new IndexException("item doesn't exist.");
		}
		indexSourceMap.get(item).IncreaseDataOpen();
	}

	public <T> List<T> search(String item, String queryStr, Sort sort, int num) throws SearchException {
		if (!itemList.contains(item)) {
			throw new SearchException("item doesn't exist.");
		}
		return searcherMap.get(item).search(queryStr, sort, num);
	}

	public <T> List<T> search(String item, String[] queryStr, Sort sort, int num) throws SearchException {
		if (!itemList.contains(item)) {
			throw new SearchException("item doesn't exist.");
		}
		return searcherMap.get(item).search(queryStr, sort, num);
	}

	public <T> List<T> search(String item, String queryStr, Sort sort, int pageSize, int curPage) throws SearchException {
		if (!itemList.contains(item)) {
			throw new SearchException("item doesn't exist.");
		}
		return searcherMap.get(item).search(queryStr, sort, pageSize, curPage);
	}

	public <T> List<T> Exactsearch(String item, Map<String, List<String>> searchTermMap, Sort sort, int num) throws SearchException {
		if (!itemList.contains(item)) {
			throw new SearchException("item doesn't exist.");
		}
		return searcherMap.get(item).Exactsearch(searchTermMap, sort, 1);
	}

	public <T> List<T> search(String item, String[] queryStr, Sort sort, int pageSize, int curPage) throws SearchException {
		if (!itemList.contains(item)) {
			throw new SearchException("item doesn't exist.");
		}
		return searcherMap.get(item).search(queryStr, sort, pageSize, curPage);
	}

	public String getConfigPath() {
		return configPath;
	}

	public void setConfigPath(String configPath) {
		this.configPath = configPath;
	}

	public MyLuenceFactory getFactory() {
		return factory;
	}

	public void setFactory(MyLuenceFactory factory) {
		this.factory = factory;
	}

	
}
