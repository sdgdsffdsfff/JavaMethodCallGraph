package com.wind.myLuence;

import java.util.List;
import com.wind.myLuence.exception.IndexException;

public interface Index extends Configurable {
	
	public void write(List<Object> datas) throws IndexException;

	public void append(List<Object> datas) throws IndexException;

	public void remove() throws IndexException;

	class FieldLucproperty {
		private String fieldName; // 字段名
		private boolean isStore; // 字段是否存储
		private boolean isIndex; // 字段是否索引
		private boolean isAnalyzed; // 是否分词

		public FieldLucproperty() {}

		public String getFieldName() {
			return fieldName;
		}

		public void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		public boolean isStore() {
			return isStore;
		}

		public void setStore(boolean isStore) {
			this.isStore = isStore;
		}

		public boolean isIndex() {
			return isIndex;
		}

		public void setIndex(boolean isIndex) {
			this.isIndex = isIndex;
		}

		public boolean isAnalyzed() {
			return isAnalyzed;
		}

		public void setAnalyzed(boolean isAnalyzed) {
			this.isAnalyzed = isAnalyzed;
		}
	}
}
