package com.wind.myLuence.index;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d1xn.common.log.Log;
import com.d1xn.common.util.DateUtil;
import com.wind.myLuence.Context;
import com.wind.myLuence.exception.ConfigurableException;
import com.wind.myLuence.exception.IndexException;

/**
 * Bean对象索引
 * 
 * @author zhouyanjun
 * @version 1.0 2014-4-24
 */
public class BeanIndex extends AbstractIndex {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private static final String ANALYZEDINDEXFIELDS = "analyzedIndexFields";
	private static final String NOTANALYZEDINDEXFIELDS = "notAnalyzedIndexFields";
	private static final String STOREFIELDS = "storeFields";
	private static final String INDEXFILEPATH = "indexFilePath";
	private static final String CLASSNAME = "beanClass";
	private static final String ANALYZERCLASS = "analyzerClass";

	private static final String PKNAME = "pkName";
	private static final String PKNAME_DEFAULT = "id";
	private static final String PK_FIELD = "pkFields";
	private static final String DATAFIELDNAME = "dataFieldName";
	private static final String DATAFIELDNAME_DEFAULT = "data";

	private List<String> analyzedIndexFields; // 需要分词索引的字段
	private List<String> notAnalyzedIndexFields; // 不分词索引字段
	private List<String> storeFields; // 需要存储的字段名称
	private Class<?> clazz;
	private List<String> pkFields; // 主键字段
	private String pkName; // 主键字段名
	private String dataFieldName; // 数据字段名

	private Map<String, FieldLucproperty> fieldPropertyMap; // 字段索引设定

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
				this.analyzedIndexFields = new ArrayList<String>(Arrays.asList(analyzedIndexFields.split(",")));
				if (!pojoFields.containsAll(this.analyzedIndexFields)) {
					logger.error("indexField's range beyong the range of pojoFields");
					throw new ConfigurableException("indexField's range beyong the range of pojoFields");
				}
			}
			String notAnalyzedIndexFields = context.getString(NOTANALYZEDINDEXFIELDS);
			if (notAnalyzedIndexFields != null && !notAnalyzedIndexFields.isEmpty()) {
				this.notAnalyzedIndexFields = new ArrayList<String>(Arrays.asList(notAnalyzedIndexFields.split(",")));
				if (!pojoFields.containsAll(this.notAnalyzedIndexFields)) {
					logger.error("notAnalyzedIndexFields's range beyong the range of pojoFields");
					throw new ConfigurableException("notAnalyzedIndexFields's range beyong the range of pojoFields");
				}
			}
			String storeFields = context.getString(STOREFIELDS);
			if (storeFields != null && !storeFields.isEmpty()) {
				this.storeFields = new ArrayList<String>(Arrays.asList(storeFields.split(",")));
				if (!pojoFields.containsAll(this.storeFields) && !this.storeFields.contains(DATAFIELDNAME_DEFAULT)) {
					logger.error("storeFields's range beyong the range of pojoFields");
					throw new ConfigurableException("storeFields's range beyong the range of pojoFields");
				}
			}
			if (analyzedIndexFields == null && notAnalyzedIndexFields == null && storeFields == null) {
				logger.error("You must set one of analyzedIndexFields,notAnalyzedIndexFields,storeFields at least.");
				throw new ConfigurableException(
					"You must set one of analyzedIndexFields,notAnalyzedIndexFields,storeFields at least.");
			}
			String pkFields = context.getString(PK_FIELD);
			if (pkFields != null && !pkFields.isEmpty()) {
				this.pkFields = new ArrayList<String>(Arrays.asList(pkFields.split(",")));
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
			this.pkName = context.getString(PKNAME, PKNAME_DEFAULT);
			this.dataFieldName = context.getString(DATAFIELDNAME, DATAFIELDNAME_DEFAULT);
			fieldPropertyMap = getFieldLucProperty(); // 获取字段索引设定
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

	public void write(List<Object> datas) throws IndexException {
		Log.info(BeanIndex.class, "#################### createIndex  starting");
		long start = System.currentTimeMillis();
		if (datas == null || datas.isEmpty()) return;
		IndexWriter indexWriter = null;
		try {
			// 循环检测 索引目录是否被锁
			while (!IndexWriter.isLocked(directory)) {
				IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_8, analyzer);
				indexWriter = new IndexWriter(directory, indexWriterConfig);
				indexWriter.deleteAll();
				indexWriter.forceMergeDeletes();
				indexWriter.commit();
				/**
				 * 数据写入索引文件
				 */
				for (Object data : datas) {
					if (data == null) continue;
					Document doc = new Document();
					/**
					 * 如果设置了主键，则把主键存储到索引文件
					 */
					if (this.pkFields != null) {
						List<String> pkFieldValueList = new ArrayList<String>();
						for (String pkField : pkFields) {
							pkFieldValueList.add(getFieldValue(clazz, pkField, data));
						}
						StringBuilder id = new StringBuilder();
						for (String pkFieldValue : pkFieldValueList) {
							id.append(pkFieldValue + "@");
						}
						FieldType fieldType = new FieldType();
						fieldType.setStored(true);
						fieldType.setIndexed(false);
						fieldType.setTokenized(false);
						doc.add(new Field(pkName, id.substring(0, id.length() - 1), fieldType));
					}
					/**
					 * 建立索引文件
					 */
					for (String fieldName : fieldPropertyMap.keySet()) {
						String value = null;
						if (fieldName.equalsIgnoreCase(dataFieldName)) { // 如果为data
							value = JSONObject.fromObject(data, config).toString();
						} else {
							value = getFieldValue(clazz, fieldName, data);
						}
						if (value != null) {
							FieldType fieldType = new FieldType();
							FieldLucproperty fieldLucproperty = fieldPropertyMap.get(fieldName);
							fieldType.setStored(fieldLucproperty.isStore());
							fieldType.setIndexed(fieldLucproperty.isIndex());
							fieldType.setTokenized(fieldLucproperty.isAnalyzed());
							Field field = new Field(fieldName, value, fieldType);
							doc.add(field);
						}
					}
					indexWriter.addDocument(doc);
				}
				indexWriter.commit();
				break;
			}
		} catch (Exception e) {
			Log.error(BeanIndex.class, e, "###############create index happened error" + e.getMessage());
		} finally {
			if (indexWriter != null) {
				try {
					indexWriter.close();
					IndexWriter.unlock(directory);
				} catch (IOException e) {
					Log.error(this.getClass(), e);
				}
				indexWriter = null;
			}
		}
		Log.warn(BeanIndex.class, "#################### createIndex  finished, this process spent "
				+ (System.currentTimeMillis() - start) / 1000 + " seconds");
	}

	@Override
	public void append(List<Object> datas) throws IndexException {
		Log.info(BeanIndex.class, "#################### append index  starting");
		if (datas == null || datas.isEmpty()) return;
		long start = System.currentTimeMillis();
		IndexWriter indexWriter = null;
		try {
			// 循环检测 索引目录是否被锁
			while (!IndexWriter.isLocked(directory)) {
				IndexWriterConfig indexWriterConfig = new IndexWriterConfig(Version.LUCENE_46, analyzer);
				indexWriter = new IndexWriter(directory, indexWriterConfig);
				/**
				 * 数据写入索引文件
				 */
				for (Object data : datas) {
					if (data == null) continue;
					Document doc = new Document();
					/**
					 * 如果设置了主键，则把主键存储到索引文件
					 */
					if (this.pkFields != null) {
						List<String> pkFieldValueList = new ArrayList<String>();
						for (String pkField : pkFields) {
							pkFieldValueList.add(getFieldValue(clazz, pkField, data));
						}
						StringBuilder id = new StringBuilder();
						for (String pkFieldValue : pkFieldValueList) {
							id.append(pkFieldValue + "@");
						}
						FieldType fieldType = new FieldType();
						fieldType.setStored(true);
						fieldType.setIndexed(false);
						fieldType.setTokenized(false);
						doc.add(new Field(pkName, id.substring(0, id.length() - 1), fieldType));
					}
					/**
					 * 建立索引文件
					 */
					for (String fieldName : fieldPropertyMap.keySet()) {
						String value = null;
						if (fieldName.equalsIgnoreCase(dataFieldName)) { // 如果为data
							value = JSONObject.fromObject(data, config).toString();
						} else {
							value = getFieldValue(clazz, fieldName, data);
						}
						if (value != null) {
							FieldType fieldType = new FieldType();
							FieldLucproperty fieldLucproperty = fieldPropertyMap.get(fieldName);
							fieldType.setStored(fieldLucproperty.isStore());
							fieldType.setIndexed(fieldLucproperty.isIndex());
							fieldType.setTokenized(fieldLucproperty.isAnalyzed());
							Field field = new Field(fieldName, value, fieldType);
							doc.add(field);
						}
					}
					indexWriter.addDocument(doc);
				}
				indexWriter.forceMerge(50);
				indexWriter.commit();
				break;
			}
		} catch (Exception e) {
			Log.error(BeanIndex.class, e, "###############append index happened error" + e.getMessage());
		} finally {
			if (indexWriter != null) {
				try {
					indexWriter.close();
					IndexWriter.unlock(directory);
				} catch (IOException e) {
					Log.error(this.getClass(), e);
				}
				indexWriter = null;
			}
		}
		Log.warn(BeanIndex.class, "#################### append index  finished, this process spent "
				+ (System.currentTimeMillis() - start) / 1000 + " seconds");
	}

	@Override
	public void remove() throws IndexException {

	}

	/**
	 * 获取字段值
	 * 
	 * @param clazz
	 *            Class对象描述
	 * @param fieldName
	 *            字段名称
	 * @param data
	 *            数据对象
	 * @return 指定字段的值
	 * @throws Exception
	 * 
	 */
	private String getFieldValue(Class<?> clazz, String fieldName, Object data) throws Exception {
		String value = null;
		Method m = clazz.getMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1));
		String returnValueType = m.getReturnType().getSimpleName();
		Object mValue = m.invoke(data);
		if (mValue != null) {
			if ("String".equals(returnValueType) || "Integer".equals(returnValueType) || "Long".equals(returnValueType)
					|| "Double".equals(returnValueType) || "Float".equals(returnValueType) || "Boolean".equals(returnValueType)
					|| "Short".equalsIgnoreCase(returnValueType)) {
				value = mValue.toString();
			} else if ("Date".equals(returnValueType)) {
				value = DateUtil.timeFormat((Date) mValue);
			} else if ("Timestamp".equals(returnValueType)) {
				value = DateUtil.timeFormat((Timestamp) mValue);
			} else if ("HashMap".equals(returnValueType)) {
				value = JSONObject.fromObject(mValue, config).toString();
			} else if ("ArrayList".equals(returnValueType)) {
				value = JSONArray.fromObject(mValue, config).toString();
			} else {
				value = getFieldValue(m.getReturnType(), fieldName, mValue);
			}
		}
		return value;
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

	/**
	 * 整理索引与存储字段
	 * 
	 * @return
	 */
	private Map<String, FieldLucproperty> getFieldLucProperty() {
		Map<String, FieldLucproperty> fieldPropertyMap = new HashMap<String, FieldLucproperty>();
		// 分词索引字段
		if (analyzedIndexFields != null) {
			for (String analyzedIndexField : analyzedIndexFields) {
				FieldLucproperty fieldLucproperty = new FieldLucproperty();
				fieldLucproperty.setFieldName(analyzedIndexField);
				fieldLucproperty.setAnalyzed(true);
				fieldLucproperty.setStore(false);
				fieldLucproperty.setIndex(true);
				fieldPropertyMap.put(analyzedIndexField, fieldLucproperty);
			}
		}
		// 不分词索引字段
		if (notAnalyzedIndexFields != null) {
			for (String notAnalyzedIndexField : notAnalyzedIndexFields) {
				FieldLucproperty fieldLucproperty = new FieldLucproperty();
				fieldLucproperty.setFieldName(notAnalyzedIndexField);
				fieldLucproperty.setIndex(true);
				fieldLucproperty.setAnalyzed(false);
				fieldLucproperty.setStore(false);
				fieldPropertyMap.put(notAnalyzedIndexField, fieldLucproperty);
			}
		}
		// 存储字段
		if (storeFields != null) {
			for (String storeField : storeFields) {
				if (fieldPropertyMap.containsKey(storeField)) {
					FieldLucproperty fieldLucproperty = fieldPropertyMap.get(storeField);
					fieldLucproperty.setStore(true);
				} else {
					FieldLucproperty fieldLucproperty = new FieldLucproperty();
					fieldLucproperty.setFieldName(storeField);
					fieldLucproperty.setIndex(false);
					fieldLucproperty.setAnalyzed(false);
					fieldLucproperty.setStore(true);
					fieldPropertyMap.put(storeField, fieldLucproperty);
				}
			}
		}
		return fieldPropertyMap;
	}

	private static JsonConfig config;
	static {
		config = new JsonConfig();
		config.registerJsonValueProcessor(java.sql.Timestamp.class, new DateJsonValueProcessor("yyyy-MM-dd HH:mm:ss"));
		config.registerJsonValueProcessor(java.util.Date.class, new DateJsonValueProcessor("yyyy-MM-dd HH:mm:ss"));
		config.registerJsonValueProcessor(java.sql.Date.class, new DateJsonValueProcessor("yyyy-MM-dd HH:mm:ss"));
	}

	static class DateJsonValueProcessor implements JsonValueProcessor {
		private String format;

		public DateJsonValueProcessor(String format) {
			this.format = format;
		}

		@Override
		public Object processArrayValue(Object arg0, JsonConfig arg1) {
			return processObjectValue(null, arg0, arg1);
		}

		@Override
		public Object processObjectValue(String key, Object value, JsonConfig arg2) {
			if (value == null) {
				return "";
			}
			if (value instanceof java.sql.Timestamp) {
				String str = new SimpleDateFormat(format).format((java.sql.Timestamp) value);
				return str;
			}
			if (value instanceof java.util.Date) {
				String str = new SimpleDateFormat(format).format((java.util.Date) value);
				return str;
			}

			return value.toString();
		}
	}
}
