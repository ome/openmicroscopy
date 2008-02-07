/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import ome.model.meta.Session;


/**
 * Simple implementation of {@link SessionCache} which guarantees quick
 * lookups by either index (id or uuid) and maintains the proper locks.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public class SessionCacheImpl implements SessionCache {

	private static class Wrapper {

		long time;
		SessionContext ctx;
		
		Wrapper(SessionContext ctx) {
			this.ctx = ctx;
			touch();
		}
		
		void touch() {
			this.time = System.currentTimeMillis();
		}
		
	}

	ReentrantReadWriteLock rwl = new ReentrantReadWriteLock();	
	Map<Long, Wrapper> idMap = new HashMap<Long, Wrapper>();
	Map<String, Wrapper> uuidMap = new HashMap<String, Wrapper>();

	public void put(SessionContext sessionContext) {
		
		if (sessionContext == null) return;
		
		rwl.writeLock().lock();
		assert preConditions();
		try {
			Session session = sessionContext.getSession();
			idMap.put(session.getId(), new Wrapper(sessionContext));
			uuidMap.put(session.getUuid(), new Wrapper(sessionContext));
		} finally {
			assert postConditions();
			rwl.writeLock().unlock();
		}
		
	}

	public boolean contains(String uuid) {
		
		if (uuid == null) return false;
		
		rwl.readLock().lock();
		assert preConditions();
		try {
			return uuidMap.containsKey(uuid);
		} finally {
			assert postConditions();
			rwl.readLock().unlock();
		}
	}

	public boolean contains(long id) {
		
		// long can't be null
		
		rwl.readLock().lock();
		assert preConditions();
		try {
			return idMap.containsKey(id);
		} finally {
			assert postConditions();
			rwl.readLock().unlock();
		}
	}


	public SessionContext get(String uuid) {

		if (uuid == null) return null;
		
		rwl.readLock().lock();
		assert preConditions();
		try {
			return uuidMap.get(uuid).ctx;
		} finally {
			assert postConditions();
			rwl.readLock().unlock();
		}
	}

	public SessionContext get(long id) {

		// long cannot be null
		
		rwl.readLock().lock();
		assert preConditions();
		try {
			return idMap.get(id).ctx;
		} finally {
			assert postConditions();
			rwl.readLock().unlock();
		}
	}

	
	public SessionContext remove(long id) {
		
		// long can't be null
		
		rwl.writeLock().lock();
		assert preConditions();
		try {
			SessionContext ctx = idMap.remove(id).ctx;
			if (ctx != null) {
				uuidMap.remove(ctx.getSession().getUuid());
			}
			return ctx;
		} finally {
			assert postConditions();
			rwl.writeLock().unlock();
		}		
	}

	public SessionContext remove(String uuid) {
		
		if (uuid == null) return null;
		
		rwl.writeLock().lock();
		assert preConditions();
		try {
			SessionContext ctx = uuidMap.remove(uuid).ctx;
			if (ctx != null) {
				idMap.remove(ctx.getSession().getId());
			}
			return ctx;
		} finally {
			assert postConditions();
			rwl.writeLock().unlock();
		}		
	}

	public int clear() {
		rwl.writeLock().lock();
		assert preConditions();
		try {
			// preCondition asserts they are the same
			int cnt = uuidMap.size();
			uuidMap.clear();
			idMap.clear();
			return cnt;
		} finally {
			assert postConditions();
			rwl.writeLock().unlock();
		}		
	}

	public long timestamp(long id) {

		// long cannot be null
		
		rwl.readLock().lock();
		assert preConditions();
		try {
			Wrapper w = idMap.get(id);
			if (w != null) {
				return w.time;
			} else {
				return -2;
			}
		} finally {
			assert postConditions();
			rwl.readLock().unlock();
		}	
	}
	
	public long timestamp(String uuid) {

		if (uuid == null) return -1;
		
		rwl.readLock().lock();
		assert preConditions();
		try {
			Wrapper w = uuidMap.get(uuid);
			if (w != null) {
				return w.time;
			} else {
				return -2;
			}
		} finally {
			assert postConditions();
			rwl.readLock().unlock();
		}

	}
	
	public void touch(long id) {

		// long can't be null
		
		rwl.writeLock().lock();
		assert preConditions();
		try {
			Wrapper w = idMap.get(id);
			if (w != null) {
				w.touch();
			}
		} finally {
			assert postConditions();
			rwl.writeLock().unlock();
		}		
	}
	
	public void touch(String uuid) {
		
		if (uuid == null) return;
		
		rwl.writeLock().lock();
		assert preConditions();
		try {
			Wrapper w = uuidMap.get(uuid);
			if (w != null) {
				w.touch();
			}
		} finally {
			assert postConditions();
			rwl.writeLock().unlock();
		}		

	}
	
	public void each(Visitor visitor) {
		// TODO Auto-generated method stub
		
	}
	
	public void readLock() {
		// TODO Auto-generated method stub
		
	}

	public void readUnlock() {
		// TODO Auto-generated method stub
		
	}

	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeLock() {
		// TODO Auto-generated method stub
		
	}

	public void writeUnlock() {
		// TODO Auto-generated method stub
		
	}

	// Helpers
	// ========================================================================
	
	/**
	 * Should be called only when holding a lock.
	 */
	private boolean preConditions() {
		return
			idMap.size() == uuidMap.size();
	}
	
	/**
	 * Should be called only when holding a lock.
	 */
	private boolean postConditions() {
		return
			idMap.size() == uuidMap.size();
	}

}