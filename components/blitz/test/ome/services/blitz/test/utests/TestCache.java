/*
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test.utests;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.event.RegisteredEventListeners;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

/**
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class TestCache extends Cache {

	static CacheManager ehMgr = new CacheManager();
	static volatile int count = 1;
	
//	super("test", 10 /* elts */, MemoryStoreEvictionPolicy.LFU,
//			false /* disk */, null /* path */, true /* eternal */,
//			10 /* time to live */, 10 /* time to idle */,
//			false /* disk persistent */, 10 /* disk thread interval */,
//			null /* listeners */);
	
	public TestCache() {
		this("testcache"+count++, 
				10 /* elts */, 10 /* time to live */, 
				10 /* time to idle */, null /* listeners */);
	}

	public TestCache(String name, int elements, int timeToLive, int timeToIdle, 
			RegisteredEventListeners listeners) {
		super(name, elements, MemoryStoreEvictionPolicy.LFU,
				false /* disk */, null /* path */, true /* eternal */,
				timeToLive, timeToIdle,
				false /* disk persistent */, 10 /* disk thread interval */,
				listeners);
		ehMgr.addCache(this);
	}

}
