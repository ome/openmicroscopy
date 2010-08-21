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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import ome.services.blitz.impl.ServiceFactoryI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    private final static Log log = LogFactory.getLog(ServantHolder.class);

    /**
     * Note: servants are stored by String since {@link Ice.Identity} does not
     * behave properly as a key.
     */
    private final ConcurrentHashMap<String, Ice.Object> servants = new ConcurrentHashMap<String, Ice.Object>();

    /**
     * Write-once map which contains a {@link Lock} for each given name.
     */
    private final ConcurrentHashMap<String, Lock> locks = new ConcurrentHashMap<String, Lock>();

    /**
     * Acquires the given lock or if necessary creates a new one.
     * 
     * @param key
     */
    public void acquireLock(String key) {
        Lock lock = new ReentrantLock();
        Lock oldLock = locks.putIfAbsent(key, lock);
        // If there was already a lock,
        // then the new lock can be gc'd
        if (oldLock != null) {
            lock = oldLock;
        }
        lock.lock();
    }

    /**
     * Releases the given lock if found, otherwise throws an
     * {@link ome.conditions.InternalException}
     */
    public void releaseLock(String key) {
        Lock lock = locks.get(key);
        if (lock == null) {
            throw new ome.conditions.InternalException("No lock found: " + key);
        }
        lock.unlock();
    }

    public Ice.Object get(Ice.Identity id) {
        return get(id.name);
    }

    public Object getUntied(Ice.Identity id) {
        Ice.Object servantOrTie = get(id.name);
         if (servantOrTie instanceof Ice.TieBase) {
             return ((Ice.TieBase) servantOrTie).ice_delegate();
         } else {
             return servantOrTie;
         }
    }

    public void put(Ice.Identity id, Ice.Object servant) {
        put(id.name, servant);
    }

    public Ice.Object remove(Ice.Identity id) {
        return remove(id.name);
    }

    public List<String> getServantList() {
        return new ArrayList<String>(servants.keySet());
    }

    //
    // Implementation
    //

    private void put(String key, Ice.Object servant) {
        Object old = servants.put(key, servant);
        if (old == null) {
            log.debug(String.format("Added %s to %s as %s", servant, this, key));
        } else {
            log.debug(String.format("Replaced %s with %s to %s as %s", old, servant, this, key));
        }
    }

    private Ice.Object remove(String key) {
        Ice.Object servant = servants.remove(key);
        log.debug(String.format("Removed %s from %s as %s", servant, this, key));
        return servant;
    }

    private Ice.Object get(String key) {
        return servants.get(key);
    }


}
