package com.wind.myLuence.search;

import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.Sort;
import org.apache.lucene.store.FSDirectory;

import com.wind.myLuence.Configurable;

public abstract class AbstarctSearcher implements Configurable {
	protected Analyzer analyzer; // 分词器
	protected FSDirectory directory; // 索引目录

	public abstract <T> List<T> search(String queryStr, Sort sort, int num);

	public abstract <T> List<T> search(String queryStr, Sort sort, int pageSize, int curPage);

	public abstract <T> List<T> search(String[] queryStr, Sort sort, int pageSize, int curPage);

	public abstract <T> List<T> search(String[] queryStrs, Sort sort, int num);

	public abstract <T> List<T> Exactsearch(Map<String, List<String>> searchTermMap, Sort sort, int num);

	public Analyzer getAnalyzer() {
		return analyzer;
	}

	public void setAnalyzer(Analyzer analyzer) {
		this.analyzer = analyzer;
	}
}
