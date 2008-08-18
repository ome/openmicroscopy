/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package ome.services.blitz.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ome.services.blitz.impl.ServiceFactoryI;

/**
 * Manager for all active servants in a single {@link ServiceFactoryI}.
 * 
 * To reduce the need of using {@link Ice.Util#stringToIdentity(String)} and
 * {@link Ice.Util#identityToString(Ice.Identity)} the servant tries to make the
 * two usages equivalent.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 */
public class ServantHolder {

    /**
     * Note: servants are stored by String since {@link Ice.Identity} does not
     * behave properly as a key.
     */
    final Map<String, Ice.Object> servants = new ConcurrentHashMap<String, Ice.Object>();

    /**
     * Write-once map which contains a {@link Lock} for each given name.
     */
    final Map<String, Lock> locks = new ConcurrentHashMap<String, Lock>();

    /**
     * Acquires the given lock or if necessary creates a new one.
     * 
     * @param key
     */
    public void acquireLock(String key) {
        Lock lock = locks.get(key);
        if (lock == null) {
            synchronized (locks) {
                // check again in case added while waiting.
                lock = locks.get(key);
                if (lock == null) {
                    lock = new ReentrantLock();
                    locks.put(key, lock);
                }
            }
        }
        lock.lock();
    }

    /**
     * Releases the given lock if found, otherwise throws an
     * {@link InternalException}
     */
    public void releaseLock(String key) {
        Lock lock = locks.get(key);
        if (lock == null) {
            synchronized (locks) {
                // check again in case added while waiting.
                lock = locks.get(key);
                if (lock == null) {
                    throw new ome.conditions.InternalException(
                            "No lock found: " + key);
                }
            }
        }
        lock.unlock();
    }

    public Ice.Object get(String key) {
        return servants.get(key);
    }

    /**
     */
    public Ice.Object get(Ice.Identity id) {
        return get(id.name);
    }

    public void put(String key, Ice.Object servant) {
        servants.put(key, servant);
    }

    public Ice.Object remove(String key) {
        return servants.remove(key);
    }

    public Ice.Object remove(Ice.Identity id) {
        return remove(id.name);
    }

    public List<String> getServantList() {
        return new ArrayList<String>(servants.keySet());
    }

}
