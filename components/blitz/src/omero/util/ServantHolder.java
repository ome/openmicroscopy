/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 *
 */

package omero.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import omero.api._StatefulServiceInterfaceOperations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for all active servants in a single session.
 *
 * To reduce the need of using {@link Ice.Util#stringToIdentity(String)} and
 * {@link Ice.Util#identityToString(Ice.Identity)} the servant tries to make the
 * two usages equivalent.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 */
public class ServantHolder {

    private final static Logger log = LoggerFactory.getLogger(ServantHolder.class);

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
     * An internal mapping to all client ids from {@link omero.cmd.SessionI} for a given
     * DB session since there is no method on {@link Ice.ObjectAdapter} to retrieve
     * all servants.
     */
    protected final ConcurrentHashMap<String, Object> clientIds = new ConcurrentHashMap<String, Object>();

    /**
     * Storing session for debugging purposes.
     */
    private final String session;

    /**
     * The number of servants that are allowed to be registered for a user
     * in a single session before a {@link omero.OverUsageException} is thrown.
     */
    private final int servantsPerSession;

    public ServantHolder(String session) {
        this(session, 10000);
    }

    public ServantHolder(String session, int servantsPerSession) {
        this.session = session;
        this.servantsPerSession = servantsPerSession;
    }

    //
    // Session id related methods
    //

    public String getSession() {
        return this.session;
    }

    /**
     * Constructs an {@link Ice.Identity} from the current session
     * and from the given {@link String} which for
     * stateless services are defined by the instance fields {@link #adminKey},
     * {@link #configKey}, etc. and for stateful services are UUIDs.
     */
    public Ice.Identity getIdentity(String idName) {
        Ice.Identity id = new Ice.Identity();
        id.category = this.session;
        id.name = idName;
        return id;
    }

    //
    // ClientId methods
    //

    public void addClientId(String clientId) {
        clientIds.put(clientId, Boolean.TRUE);
    }

    public void removeClientId(String clientId) {
        clientIds.remove(clientId);
    }

    public Set<String> getClientIds() {
        return clientIds.keySet();
    }

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

    public void put(Ice.Identity id, Ice.Object servant)
        throws omero.OverUsageException {
        final int size = servants.size();
        if (size >= servantsPerSession) {
            String msg = String.format("servantsPerSession reached for %s: %s",
                session, servantsPerSession);
            log.error(msg);
            omero.OverUsageException oue = new omero.OverUsageException();
            omero.util.IceMapper.fillServerError(oue,
                new ome.conditions.OverUsageException(msg));
            throw oue;
        }

        double percent = (100.0 * size / servantsPerSession);
        if (percent > 0 && (percent % 10) == 0) {
            log.warn(String.format("%s of servants used for session %s",
                (int) percent, session));
        }
        put(id.name, servant);
    }

    public Ice.Object remove(Ice.Identity id) {
        return remove(id.name);
    }

    public List<String> getServantList() {
        return new ArrayList<String>(servants.keySet());
    }

    public String getStatefulServiceCount() {
        String list = "";
        final List<String> servants = getServantList();
        for (final String idName : servants) {
            final Ice.Identity id = getIdentity(idName);
            final Object servant = getUntied(id);
            if (servant != null) {
                try {
                    if (servant instanceof _StatefulServiceInterfaceOperations) {
                        list += "\n" + idName;
                    }
                } catch (Exception e) {
                    // oh well
                }
            }
        }
        return list;
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
