package com.wind.myLuence.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;

import com.wind.myLuence.Index;

public abstract class AbstractIndex implements Index {
	protected Analyzer analyzer; // 分词器
	protected Directory directory; // 索引目录

	public Analyzer getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}
}
