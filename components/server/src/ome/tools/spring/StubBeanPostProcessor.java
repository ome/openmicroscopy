/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.tools.spring;

import java.util.List;

import net.sf.ehcache.constructs.blocking.BlockingCache;

import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Spring {@link BeanPostProcessor} which uses a {@link BlockingCache} to pass
 * through instances. This is mainly used for testing.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class StubBeanPostProcessor implements BeanPostProcessor {

	List<String> names;
	BlockingCache cache;
	
	public void setNames(List<String> names) {
		this.names = names;
	}
	
	public BlockingCache getCache() {
		return cache;
	}
	
	public void setCache(BlockingCache cache) {
		this.cache = cache;
	}

	public Object postProcessBeforeInitialization(Object bean, String beanName) {
		if (names.contains(beanName))
				return cache.get(beanName);
		return bean;
	}

	public Object postProcessAfterInitialization(Object bean, String beanName) {
		return bean;
	}

}
