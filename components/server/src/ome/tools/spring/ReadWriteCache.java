/*   
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.tools.spring;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Statistics;
import net.sf.ehcache.Status;
import net.sf.ehcache.bootstrap.BootstrapCacheLoader;
import net.sf.ehcache.event.RegisteredEventListeners;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * {@link net.sf.ehcache.constructs.BlockingCache}
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta2
 */
public class ReadWriteCache implements Ehcache {

	private static final Log logger = LogFactory.getLog(ReadWriteCache.class);

	private Ehcache cache;

	private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();

	public ReadWriteCache(Ehcache ehcache) {
		this.cache = ehcache;
	}
	
	public ReadLock readLock() {
		return rwl.readLock();
	}
	
	public WriteLock writeLock() {
		return rwl.writeLock();
	}
	
	public Element get(Object arg0) throws IllegalStateException,
			CacheException {
		ReadLock lock = rwl.readLock();
		lock.lock();
		try {
			return cache.get(arg0);
		} finally {
			lock.unlock();
		}
	}

	public Element get(Serializable arg0) throws IllegalStateException,
			CacheException {
		ReadLock lock = rwl.readLock();
		lock.lock();
		try {
			return cache.get(arg0);
		} finally {
			lock.unlock();
		}
	}


	public void put(Element arg0, boolean arg1)
			throws IllegalArgumentException, IllegalStateException,
			CacheException {
		WriteLock lock = rwl.writeLock();
		lock.lock();
		try {
			cache.put(arg0, arg1);
		} finally {
			lock.unlock();
		}
	}

	public void put(Element arg0) throws IllegalArgumentException,
			IllegalStateException, CacheException {
		WriteLock lock = rwl.writeLock();
		lock.lock();
		try {
			cache.put(arg0);
		} finally {
			lock.unlock();
		}
	}

	public void putQuiet(Element arg0) throws IllegalArgumentException,
			IllegalStateException, CacheException {
		WriteLock lock = rwl.writeLock();
		lock.lock();
		try {
			cache.putQuiet(arg0);
		} finally {
			lock.unlock();
		}
	}

	public boolean remove(Object arg0, boolean arg1)
			throws IllegalStateException {
		WriteLock lock = rwl.writeLock();
		lock.lock();
		try {
			return cache.remove(arg0, arg1);
		} finally {
			lock.unlock();
		}
	}

	public boolean remove(Object arg0) throws IllegalStateException {
		WriteLock lock = rwl.writeLock();
		lock.lock();
		try {
			return cache.remove(arg0);
		} finally {
			lock.unlock();
		}
	}

	public boolean remove(Serializable arg0, boolean arg1)
			throws IllegalStateException {
		WriteLock lock = rwl.writeLock();
		lock.lock();
		try {
			return cache.remove(arg0, arg1);
		} finally {
			lock.unlock();
		}
	}

	public boolean remove(Serializable arg0) throws IllegalStateException {
		WriteLock lock = rwl.writeLock();
		lock.lock();
		try {
			return cache.remove(arg0);
		} finally {
			lock.unlock();
		}
	}

	public void removeAll() throws IllegalStateException, CacheException {
		WriteLock lock = rwl.writeLock();
		lock.lock();
		try {
			cache.removeAll();
		} finally {
			lock.unlock();
		}
	}

	public void removeAll(boolean arg0) throws IllegalStateException,
			CacheException {
		WriteLock lock = rwl.writeLock();
		lock.lock();
		try {
			cache.removeAll(arg0);
		} finally {
			lock.unlock();
		}
	}

	public boolean removeQuiet(Object arg0) throws IllegalStateException {
		WriteLock lock = rwl.writeLock();
		lock.lock();
		try {
			return cache.removeQuiet(arg0);
		} finally {
			lock.unlock();
		}
	}

	public boolean removeQuiet(Serializable arg0) throws IllegalStateException {
		WriteLock lock = rwl.writeLock();
		lock.lock();
		try {
			return cache.removeQuiet(arg0);
		} finally {
			lock.unlock();
		}
	}
	
	public boolean isExpired(Element arg0) throws IllegalStateException,
			NullPointerException {
		ReadLock lock = rwl.readLock();
		lock.lock();
		try {
			return cache.isExpired(arg0);
		} finally {
			lock.unlock();
		}
	}

	public boolean isKeyInCache(Object arg0) {
		ReadLock lock = rwl.readLock();
		lock.lock();
		try {
			return cache.isKeyInCache(arg0);
		} finally {
			lock.unlock();
		}
	}
	
	public int getSize() throws IllegalStateException, CacheException {
		ReadLock lock = rwl.readLock();
		lock.lock();
		try {
			return cache.getSize();
		} finally {
			lock.unlock();
		}
	}
	
	public long calculateInMemorySize() throws IllegalStateException,
			CacheException {
		ReadLock lock = rwl.readLock();
		lock.lock();
		try {
			return cache.calculateInMemorySize();
		} finally {
			lock.unlock();
		}
	}
	
	// ~ Simple Delegation
	//==========================================================================
	
	public void bootstrap() {
		cache.bootstrap();
	}

	public void clearStatistics() {
		cache.clearStatistics();
	}

	public Object clone() throws CloneNotSupportedException {
		return cache.clone();
	}

	public void dispose() throws IllegalStateException {
		cache.dispose();
	}

	public void evictExpiredElements() {
		cache.evictExpiredElements();
	}

	public void flush() throws IllegalStateException, CacheException {
		cache.flush();
	}

	public BootstrapCacheLoader getBootstrapCacheLoader() {
		return cache.getBootstrapCacheLoader();
	}

	public RegisteredEventListeners getCacheEventNotificationService() {
		return cache.getCacheEventNotificationService();
	}

	public CacheManager getCacheManager() {
		return cache.getCacheManager();
	}

	public long getDiskExpiryThreadIntervalSeconds() {
		return cache.getDiskExpiryThreadIntervalSeconds();
	}

	public int getDiskStoreSize() throws IllegalStateException {
		return cache.getDiskStoreSize();
	}

	public String getGuid() {
		return cache.getGuid();
	}

	public List getKeys() throws IllegalStateException, CacheException {
		return cache.getKeys();
	}

	public List getKeysNoDuplicateCheck() throws IllegalStateException {
		return cache.getKeysNoDuplicateCheck();
	}

	public List getKeysWithExpiryCheck() throws IllegalStateException,
			CacheException {
		return cache.getKeysWithExpiryCheck();
	}

	public int getMaxElementsInMemory() {
		return cache.getMaxElementsInMemory();
	}

	public int getMaxElementsOnDisk() {
		return cache.getMaxElementsOnDisk();
	}

	public MemoryStoreEvictionPolicy getMemoryStoreEvictionPolicy() {
		return cache.getMemoryStoreEvictionPolicy();
	}

	public long getMemoryStoreSize() throws IllegalStateException {
		return cache.getMemoryStoreSize();
	}

	public String getName() {
		return cache.getName();
	}

	public Element getQuiet(Object arg0) throws IllegalStateException,
			CacheException {
		return cache.getQuiet(arg0);
	}

	public Element getQuiet(Serializable arg0) throws IllegalStateException,
			CacheException {
		return cache.getQuiet(arg0);
	}

	public Statistics getStatistics() throws IllegalStateException {
		return cache.getStatistics();
	}

	public int getStatisticsAccuracy() {
		return cache.getStatisticsAccuracy();
	}

	public Status getStatus() {
		return cache.getStatus();
	}

	public long getTimeToIdleSeconds() {
		return cache.getTimeToIdleSeconds();
	}

	public long getTimeToLiveSeconds() {
		return cache.getTimeToLiveSeconds();
	}

	public void initialise() {
		cache.initialise();
	}

	public boolean isDiskPersistent() {
		return cache.isDiskPersistent();
	}

	public boolean isElementInMemory(Object arg0) {
		return cache.isElementInMemory(arg0);
	}

	public boolean isElementInMemory(Serializable arg0) {
		return cache.isElementInMemory(arg0);
	}

	public boolean isElementOnDisk(Object arg0) {
		return cache.isElementOnDisk(arg0);
	}

	public boolean isElementOnDisk(Serializable arg0) {
		return cache.isElementOnDisk(arg0);
	}

	public boolean isEternal() {
		return cache.isEternal();
	}

	public boolean isOverflowToDisk() {
		return cache.isOverflowToDisk();
	}

	public boolean isValueInCache(Object arg0) {
		return cache.isValueInCache(arg0);
	}

	public void setBootstrapCacheLoader(BootstrapCacheLoader arg0)
			throws CacheException {
		cache.setBootstrapCacheLoader(arg0);
	}

	public void setCacheManager(CacheManager arg0) {
		cache.setCacheManager(arg0);
	}

	public void setDiskStorePath(String arg0) throws CacheException {
		cache.setDiskStorePath(arg0);
	}

	public void setName(String arg0) {
		cache.setName(arg0);
	}

	public void setStatisticsAccuracy(int arg0) {
		cache.setStatisticsAccuracy(arg0);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		sb.append(this.getClass().getName());
		sb.append("(");
		sb.append(cache.toString());
		sb.append(")");
		return sb.toString();
	}

}
