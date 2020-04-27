package com.wind.myLuence.source;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.d1xn.common.json.JSONObject;
import com.d1xn.common.util.JSONObjUtil;
import com.d1xn.common.vo.ResultVO;
import com.d1xn.ddal.client.socket.AsynDDALHelper;
import com.d1xn.ddal.client.socket.ddal.ResListCallback;
import com.d1xn.ddal.core.config.Compares;
import com.d1xn.ddal.core.config.Condition;
import com.d1xn.ddal.core.config.DataOrder;
import com.wind.myLuence.Context;
import com.wind.myLuence.config.CfFileParser;
import com.wind.myLuence.exception.ConfigurableException;
import com.wind.myLuence.index.BeanIndex;

/**
 * DDAL数据源
 * 
 * @author zhouyanjun
 * @version 1.0 2014-4-24
 */
public class DDALDatabaseSource extends IndexSource {
	private final Logger logger = LoggerFactory.getLogger(DDALDatabaseSource.class);
	private AsynDDALHelper asynHelper;
	private Class<?> pojoClass;
	private IncFieldRW incFieldRW; // 增长字段读写工具
	private Map<String, String> fieldFilterMap = new HashMap<String, String>(); // 数据过滤条件Map（目前只支持静态字符串类型）

	public void configure(Context context) throws ConfigurableException, ClassNotFoundException {
		String classPath = context.getString(CfFileParser.ConfigItem.BEANCLASS.value);
		pojoClass = Class.forName(classPath);
		// 增长字段
		String incField = context.getString(CfFileParser.ConfigItem.INCFIELD.value);
		if (incField != null) {
			String indexPath = context.getString(CfFileParser.ConfigItem.INDEXFILEPATH.value);
			String incFieldPath = indexPath + "/inc/fieldLast.txt";
			incFieldRW = new IncFieldRW(incField, new File(incFieldPath), pojoClass);
		}
		String dataFilterStr = context.getString(CfFileParser.ConfigItem.DATAFILTER.value);
		if (dataFilterStr != null) {
			String[] dataFilterArray = dataFilterStr.split(":");
			fieldFilterMap.put(dataFilterArray[0], dataFilterArray[1]);
		}
		index.configure(context);
	}

	@Override
	public void InitialDataOpen() {
		DataOrder order = null;
		if (incFieldRW != null) {
			order = new DataOrder(incFieldRW.incFieldName, true);
		}
		List<Condition> filter = new ArrayList<Condition>();
		if (!fieldFilterMap.isEmpty()) {
			for (String field : fieldFilterMap.keySet()) {
				filter.add(new Condition(field, fieldFilterMap.get(field)));
			}
		}
		asynHelper.query(pojoClass, null, Integer.MAX_VALUE, (filter.isEmpty() ? null : filter), order,
			new ResListCallback<Object>() {
				@Override
				public void succeed(List<Object> datas) throws Exception {
					if (datas != null && !datas.isEmpty() && incFieldRW != null) {
						incFieldRW.writeValue(datas.get(0));
					}
					BeanIndex databaseIndex = (BeanIndex) index;
					databaseIndex.write(datas); // 写索引
				}

				@Override
				public void failure(ResultVO result) throws Exception {
					logger.error(result.getErrorMsg());
				}
			});
	}

	@Override
	public void IncreaseDataOpen() {
		if (incFieldRW == null) {
			throw new NullPointerException("increase field doesn't set");
		}
		try {
			List<Condition> filter = new ArrayList<Condition>();
			filter.add(new Condition(incFieldRW.incFieldName, Compares.MORE_THAN, incFieldRW.readValue()));
			if (!fieldFilterMap.isEmpty()) {
				for (String field : fieldFilterMap.keySet()) {
					filter.add(new Condition(field, fieldFilterMap.get(field)));
				}
			}
			DataOrder order = new DataOrder(incFieldRW.incFieldName, true);
			asynHelper.query(pojoClass, null, filter, order, null, new ResListCallback<Object>() {

				@Override
				public void succeed(List<Object> datas) throws Exception {
					if (datas != null && !datas.isEmpty()) {
						incFieldRW.writeValue(datas.get(0));
					}
					BeanIndex databaseIndex = (BeanIndex) index;
					databaseIndex.append(datas); // 更新索引
				}

				@Override
				public void failure(ResultVO result) throws Exception {
					logger.error(result.getErrorMsg());
				}
			});
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	class IncFieldRW {
		protected File incFieldLastStore; // 增长字段保存位置
		protected String incFieldName; // 增长字段名称
		private Class<?> clazz; // bean class
		private static final String FIELDTYPE = "type";
		private SimpleDateFormat TimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		IncFieldRW(String incFieldName, File incFieldLastStore, Class<?> clazz) {
			this.incFieldName = incFieldName;
			this.incFieldLastStore = incFieldLastStore;
			this.clazz = clazz;
		}

		/**
		 * 写入增长字段值
		 * 
		 * @param clazz
		 * @param fieldName
		 * @param data
		 * @throws Exception
		 */
		protected void writeValue(Object data) throws Exception {
			String value = null;
			Method m = clazz.getMethod("get" + incFieldName.substring(0, 1).toUpperCase() + incFieldName.substring(1));
			String returnValueType = m.getReturnType().getSimpleName();
			Object mValue = m.invoke(data);
			if (mValue != null) {
				if ("Integer".equals(returnValueType) || "Long".equals(returnValueType)
						|| "Short".equalsIgnoreCase(returnValueType)) {
					value = mValue.toString();
				} else if ("Date".equals(returnValueType)) {
					value = TimeFormat.format((Date) mValue);
				} else if ("Timestamp".equals(returnValueType)) {
					value = TimeFormat.format((Timestamp) mValue);
				}
			}
			Map<String, String> map = new HashMap<String, String>();
			map.put(incFieldName, value);
			map.put(FIELDTYPE, returnValueType);
			JSONObjUtil.saveJson2File(new JSONObject(map), incFieldLastStore);
		}

		/**
		 * 读取增长字段值
		 * 
		 * @return
		 * @throws Exception
		 */
		protected Object readValue() throws Exception {
			Object result = null;
			if (incFieldLastStore.exists()) {// 时间戳文件存在
				String content = JSONObjUtil.getFileJSONContent(incFieldLastStore);
				JSONObject jsonObeject = new JSONObject(content);
				String value = jsonObeject.getString(incFieldName);
				String fieldType = jsonObeject.getString(FIELDTYPE);
				if ("Integer".equals(fieldType)) {
					result = Integer.parseInt(value);
				} else if ("Long".equals(fieldType)) {
					result = Long.parseLong(value);
				} else if ("Short".equals(fieldType)) {
					result = Short.parseShort(value);
				} else if ("Date".equals(fieldType)) {
					result = TimeFormat.parse(value);
				} else if ("Timestamp".equals(fieldType)) {
					result = TimeFormat.parse(value);
				}
			}
			return result;
		}
	}

	public AsynDDALHelper getAsynHelper() {
		return asynHelper;
	}

	public void setAsynHelper(AsynDDALHelper asynHelper) {
		this.asynHelper = asynHelper;
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
