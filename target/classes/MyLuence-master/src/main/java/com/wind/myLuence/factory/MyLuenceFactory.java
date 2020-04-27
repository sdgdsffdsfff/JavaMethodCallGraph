package com.wind.myLuence.factory;

import com.wind.myLuence.search.AbstarctSearcher;
import com.wind.myLuence.source.IndexSource;

public interface MyLuenceFactory {
	
	public static String INDEX_SOURCE_NAME = "myLuence_index_source";
	public static String SEARCHER_NAME = "myLuence_searcher";

	public IndexSource getIxSourceInstance(String name);

	public AbstarctSearcher getSearcherInstance();
}
