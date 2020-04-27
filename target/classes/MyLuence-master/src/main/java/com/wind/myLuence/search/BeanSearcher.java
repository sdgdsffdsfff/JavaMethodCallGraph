package com.wind.myLuence.search;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.ezmorph.object.DateMorpher;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d1xn.common.log.Log;
import com.d1xn.common.util.DateUtil;
import com.wind.myLuence.Context;
import com.wind.myLuence.exception.ConfigurableException;
import com.wind.myLuence.exception.SearchException;

/**
 * Bean查询器
 * 
 * @author zhouyanjun
 * @version 1.0 2014-8-20
 */
public class BeanSearcher extends AbstarctSearcher {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String ANALYZEDINDEXFIELDS = "analyzedIndexFields";
	private static final String NOTANALYZEDINDEXFIELDS = "notAnalyzedIndexFields";
	private static final String CLASSNAME = "beanClass";
	private static final String STOREFIELDS = "storeFields";
	private static final String ANALYZERCLASS = "analyzerClass";
	private static final String INDEXFILEPATH = "indexFilePath";
	private static final String DATAFIELDNAME_DEFAULT = "data";

	private List<String> analyzedIndexKeys = new ArrayList<String>(); // 分词索引keys
	private List<String> notAnalyzedIndexKeys = new ArrayList<String>(); // 非分词索引keys
	private List<String> storeKeys = new ArrayList<String>(); // 存储字段Keys
	private Class<?> clazz; // bean Class
	private boolean hasDataKey; // 是否有DataKey(整个bean存储)

	{
		JSONUtils.getMorpherRegistry().registerMorpher(new DateMorpher(new String[] { "yyyy-MM-dd HH:mm:ss" }));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> search(String queryStr, Sort sort, int num) {
		List<T> results = new ArrayList<T>();
		try {
			IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
			MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_46, analyzedIndexKeys
				.toArray(new String[] {}), analyzer);
			Query q = parser.parse(queryStr);
			TopDocs topDocs = searcher.search(q, num);
			ScoreDoc[] dosList = topDocs.scoreDocs;
			for (int i = 0; i < dosList.length; i++) {
				Document doc = searcher.doc(dosList[i].doc);
				if (hasDataKey) {
					String objJson = doc.get(DATAFIELDNAME_DEFAULT);
					T t = (T) JSONObject.toBean(JSONObject.fromObject(objJson), clazz);
					results.add(t);
				} else {
					T t = (T) clazz.newInstance();
					for (String storeKey : storeKeys) {
						String value = doc.get(storeKey);
						Map<String, String> fieldNameTypes = getFieldNameType(clazz);
						setFieldValue(clazz, storeKey, value, fieldNameTypes.get(storeKey), t);
					}
					results.add(t);
				}
			}
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return results;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> search(String[] queryStrs, Sort sort, int num) {
		List<T> results = new ArrayList<T>();
		try {
			IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
			MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_46, analyzedIndexKeys
				.toArray(new String[] {}), analyzer);
			for (String queryStr : queryStrs) {
				Query q = parser.parse(queryStr);
				TopDocs topDocs = searcher.search(q, num);
				ScoreDoc[] dosList = topDocs.scoreDocs;
				for (int i = 0; i < dosList.length; i++) {
					Document doc = searcher.doc(dosList[i].doc);
					if (hasDataKey) {
						String objJson = doc.get(DATAFIELDNAME_DEFAULT);
						T t = (T) JSONObject.toBean(JSONObject.fromObject(objJson), clazz);
						results.add(t);
					} else {
						T t = (T) clazz.newInstance();
						for (String storeKey : storeKeys) {
							String value = doc.get(storeKey);
							Map<String, String> fieldNameTypes = getFieldNameType(clazz);
							setFieldValue(clazz, storeKey, value, fieldNameTypes.get(storeKey), t);
						}
						results.add(t);
					}
				}
			}
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return results;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> search(String queryStr, Sort sort, int pageSize, int curPage) {
		List<T> results = new ArrayList<T>();
		try {
			IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
			MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_46, analyzedIndexKeys
				.toArray(new String[] {}), analyzer);
			Query q = parser.parse(queryStr);
			TopDocs topDocs = searcher.search(q, 3000);
			ScoreDoc[] dosList = topDocs.scoreDocs;
			// 查询起始记录位置
			int begin = pageSize * (curPage - 1);
			// 查询终止记录位置
			int end = Math.min(begin + pageSize, dosList.length);
			for (int i = begin; i < end; i++) {
				Document doc = searcher.doc(dosList[i].doc);
				if (hasDataKey) {
					String objJson = doc.get(DATAFIELDNAME_DEFAULT);
					T t = (T) JSONObject.toBean(JSONObject.fromObject(objJson), clazz);
					results.add(t);
				} else {
					T t = (T) clazz.newInstance();
					for (String storeKey : storeKeys) {
						String value = doc.get(storeKey);
						Map<String, String> fieldNameTypes = getFieldNameType(clazz);
						setFieldValue(clazz, storeKey, value, fieldNameTypes.get(storeKey), t);
					}
					results.add(t);
				}
			}
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return results;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> search(String[] queryStr, Sort sort, int pageSize, int curPage) {
		List<T> results = new ArrayList<T>();
		try {
			IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
			BooleanQuery Bquery = new BooleanQuery();
			MultiFieldQueryParser parser = new MultiFieldQueryParser(Version.LUCENE_46, analyzedIndexKeys
				.toArray(new String[] {}), analyzer);
			for (int i = 0; i < queryStr.length; i++) {
				Query query = parser.parse(queryStr[i]);
				Bquery.add(query, BooleanClause.Occur.SHOULD);
			}
			TopDocs topDocs = searcher.search(Bquery, 3000);
			ScoreDoc[] dosList = topDocs.scoreDocs;
			// 查询起始记录位置
			int begin = pageSize * (curPage - 1);
			// 查询终止记录位置
			int end = Math.min(begin + pageSize, dosList.length);
			for (int i = begin; i < end; i++) {
				Document doc = searcher.doc(dosList[i].doc);
				if (hasDataKey) {
					String objJson = doc.get(DATAFIELDNAME_DEFAULT);
					T t = (T) JSONObject.toBean(JSONObject.fromObject(objJson), clazz);
					results.add(t);
				} else {
					T t = (T) clazz.newInstance();
					for (String storeKey : storeKeys) {
						String value = doc.get(storeKey);
						Map<String, String> fieldNameTypes = getFieldNameType(clazz);
						setFieldValue(clazz, storeKey, value, fieldNameTypes.get(storeKey), t);
					}
					results.add(t);
				}
			}
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return results;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> Exactsearch(Map<String, List<String>> searchTermMap, Sort sort, int num) {
		List<T> results = new ArrayList<T>();
		try {
			if (!notAnalyzedIndexKeys.containsAll(searchTermMap.keySet())) {
				throw new SearchException("search key collection don't exist.");
			}
			IndexSearcher searcher = new IndexSearcher(DirectoryReader.open(directory));
			for (String searchKey : searchTermMap.keySet()) {
				List<String> searchTermList = searchTermMap.get(searchKey);
				for (String searchTerm : searchTermList) {
					Query query = new TermQuery(new Term(searchKey, searchTerm));
					TopDocs topDocs = searcher.search(query, num);
					ScoreDoc[] dosList = topDocs.scoreDocs;
					for (int i = 0; i < dosList.length; i++) {
						Document doc = searcher.doc(dosList[i].doc);
						if (hasDataKey) {
							String objJson = doc.get(DATAFIELDNAME_DEFAULT);
							T t = (T) JSONObject.toBean(JSONObject.fromObject(objJson), clazz);
							results.add(t);
						} else {
							T t = (T) clazz.newInstance();
							for (String storeKey : storeKeys) {
								String value = doc.get(storeKey);
								Map<String, String> fieldNameTypes = getFieldNameType(clazz);
								setFieldValue(clazz, storeKey, value, fieldNameTypes.get(storeKey), t);
							}
							results.add(t);
						}
					}
				}
			}
		} catch (ParseException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return results;
	}

	public void configure(Context context) throws ConfigurableException {
		try {
			String analyzerClass = context.getString(ANALYZERCLASS);
			this.analyzer = (Analyzer) Class.forName(analyzerClass).newInstance();

			List<String> pojoFields = new ArrayList<String>();
			String className = context.getString(CLASSNAME);
			try {
				this.clazz = Class.forName(className);
				getPojoFileds(pojoFields, clazz);
			} catch (ClassNotFoundException e1) {
				logger.error("bean class error to create.");
				throw new ConfigurableException(e1.getMessage(), e1);
			}
			String analyzedIndexFields = context.getString(ANALYZEDINDEXFIELDS);
			if (analyzedIndexFields != null && !analyzedIndexFields.isEmpty()) {
				List<String> analyzedIndexFieldList = new ArrayList<String>(Arrays.asList(analyzedIndexFields.split(",")));
				if (!pojoFields.containsAll(analyzedIndexFieldList)) {
					logger.error("indexField's range beyong the range of pojoFields");
					throw new ConfigurableException("indexField's range beyong the range of pojoFields");
				}
				analyzedIndexKeys.addAll(analyzedIndexFieldList);
			}
			String notAnalyzedIndexFields = context.getString(NOTANALYZEDINDEXFIELDS);
			if (notAnalyzedIndexFields != null && !notAnalyzedIndexFields.isEmpty()) {
				List<String> notAnalyzedIndexFieldList = new ArrayList<String>(Arrays.asList(notAnalyzedIndexFields.split(",")));
				if (!pojoFields.containsAll(notAnalyzedIndexFieldList)) {
					logger.error("notAnalyzedIndexFields's range beyong the range of pojoFields");
					throw new ConfigurableException("notAnalyzedIndexFields's range beyong the range of pojoFields");
				}
				notAnalyzedIndexKeys.addAll(notAnalyzedIndexFieldList);
			}
			String storeFields = context.getString(STOREFIELDS);
			if (storeFields != null && !storeFields.isEmpty()) {
				List<String> storeFieldList = new ArrayList<String>(Arrays.asList(storeFields.split(",")));
				if (!pojoFields.containsAll(storeFieldList) && !storeFieldList.contains(DATAFIELDNAME_DEFAULT)) {
					logger.error("storeFields's range beyong the range of pojoFields");
					throw new ConfigurableException("storeFields's range beyong the range of pojoFields");
				}
				storeKeys.addAll(storeFieldList);
			}
			if (storeKeys.contains(DATAFIELDNAME_DEFAULT)) {
				hasDataKey = true;
			}
			String indexFilePath = context.getString(INDEXFILEPATH);
			if (indexFilePath == null || indexFilePath.isEmpty()) {
				logger.error("indexFilePath must to be set.");
				throw new ConfigurableException("indexFilePath must to be set.");
			}
			try {
				this.directory = FSDirectory.open(new File(indexFilePath));
			} catch (IOException e) {
				logger.error("index file dir error to create.");
				throw new ConfigurableException("index file dir error to create.");
			}
		} catch (InstantiationException e2) {
			Log.error(this.getClass(), e2.getMessage());
			throw new ConfigurableException(e2.getMessage(), e2);
		} catch (IllegalAccessException e2) {
			Log.error(this.getClass(), e2.getMessage());
			throw new ConfigurableException(e2.getMessage(), e2);
		} catch (ClassNotFoundException e2) {
			Log.error(this.getClass(), e2.getMessage());
			throw new ConfigurableException(e2.getMessage(), e2);
		}
	}

	/**
	 * 获取数据库表字段列表
	 * 
	 * @param fields
	 * @param clazz
	 */
	private void getPojoFileds(List<String> fields, Class<?> clazz) {
		Method[] methods = clazz.getDeclaredMethods();
		for (Method m : methods) {
			String name = m.getName();
			if (name.startsWith("get")) {
				String returnValueType = m.getReturnType().getSimpleName();
				if ("String".equals(returnValueType) || "Integer".equals(returnValueType) || "Long".equals(returnValueType)
						|| "Double".equals(returnValueType) || "Float".equals(returnValueType)
						|| "Boolean".equals(returnValueType) || "Short".equalsIgnoreCase(returnValueType)
						|| "Date".equals(returnValueType) || "Timestamp".equals(returnValueType)
						|| "HashMap".equals(returnValueType) || "ArrayList".equals(returnValueType)) {
					name = name.substring(3, 4).toLowerCase() + name.substring(4);
				} else {
					getPojoFileds(fields, m.getReturnType());
				}
				fields.add(name);
			}
		}
	}

	private void setFieldValue(Class<?> clazz, String fieldName, String fieldvalue, String fieldType, Object data)
		throws Exception {
		Object relValue = null;
		if ("String".equals(fieldType)) {
			relValue = fieldvalue;
		} else if ("Integer".equals(fieldType)) {
			relValue = Integer.parseInt(fieldvalue);
		} else if ("Long".equals(fieldType)) {
			relValue = Long.parseLong(fieldvalue);
		} else if ("Double".equals(fieldType)) {
			relValue = Double.parseDouble(fieldvalue);
		} else if ("Float".equals(fieldType)) {
			relValue = Float.parseFloat(fieldvalue);
		} else if ("Boolean".equals(fieldType)) {
			relValue = Boolean.parseBoolean(fieldvalue);
		} else if ("Short".equalsIgnoreCase(fieldType)) {
			relValue = Short.parseShort(fieldvalue);
		} else if ("Date".equals(fieldType)) {
			relValue = DateUtil.parseTime(fieldvalue);
		} else if ("Timestamp".equals(fieldvalue)) {
			relValue = DateUtil.parseTime(fieldvalue);
		} else if ("HashMap".equals(fieldvalue)) {
			relValue = JSONObject.toBean(JSONObject.fromObject(fieldvalue), HashMap.class);
		} else if ("ArrayList".equals(fieldvalue)) {
			relValue = JSONArray.toArray(JSONArray.fromObject(fieldvalue));
		}
		Method m = clazz.getMethod("set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
		m.invoke(data, relValue);
	}

	private Map<String, String> getFieldNameType(Class<?> clazz) {
		Map<String, String> fieldNameTypeMap = new HashMap<String, String>();
		Method[] methods = clazz.getDeclaredMethods();
		for (Method m : methods) {
			String name = m.getName();
			if (name.startsWith("set")) {
				String returnValueType = m.getReturnType().getSimpleName();
				name = name.substring(3, 4).toLowerCase() + name.substring(4);
				fieldNameTypeMap.put(name, returnValueType);
			}
		}
		return fieldNameTypeMap;
	}
}
