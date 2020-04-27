package com.wind.myLuence.factory;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.wind.myLuence.search.AbstarctSearcher;
import com.wind.myLuence.source.IndexSource;

public class XmlMyLuenceFactory implements MyLuenceFactory {
	private BeanFactory beanFactory;

	public XmlMyLuenceFactory(String[] xmls) {
		beanFactory = new ClassPathXmlApplicationContext(xmls);
	}

	@Override
	public IndexSource getIxSourceInstance(String name) {
		if (name != null && !name.isEmpty()) {
			return (IndexSource) beanFactory.getBean(name);
		}
		return (IndexSource) beanFactory.getBean(INDEX_SOURCE_NAME);
	}

	@Override
	public AbstarctSearcher getSearcherInstance() {
		return (AbstarctSearcher) beanFactory.getBean(SEARCHER_NAME);
	}
}
