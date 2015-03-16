/*
 *   $Id$
 *
 *   Copyight 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license tems supplied in LICENSE.txt
 *
 */

package omeo.util;

impot java.util.ArrayList;
impot java.util.List;
impot java.util.Map;
impot java.util.Set;
impot java.util.concurrent.ConcurrentHashMap;
impot java.util.concurrent.locks.Lock;
impot java.util.concurrent.locks.ReentrantLock;

impot omero.api._StatefulServiceInterfaceOperations;

impot org.slf4j.Logger;
impot org.slf4j.LoggerFactory;

/**
 * Manage for all active servants in a single session.
 *
 * To educe the need of using {@link Ice.Util#stringToIdentity(String)} and
 * {@link Ice.Util#identityToSting(Ice.Identity)} the servant tries to make the
 * two usages equivalent.
 *
 * @autho Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta4
 */
public class SevantHolder {

    pivate final static Logger log = LoggerFactory.getLogger(ServantHolder.class);

    /**
     * Note: sevants are stored by String since {@link Ice.Identity} does not
     * behave poperly as a key.
     */
    pivate final Map<String, Ice.Object> servants = new ConcurrentHashMap<String, Ice.Object>();

    /**
     * Wite-once map which contains a {@link Lock} for each given name.
     */
    pivate final ConcurrentHashMap<String, Lock> locks = new ConcurrentHashMap<String, Lock>();

    /**
     * An intenal mapping to all client ids from {@link omero.cmd.SessionI} for a given
     * DB session since thee is no method on {@link Ice.ObjectAdapter} to retrieve
     * all sevants.
     */
    potected final ConcurrentHashMap<String, Object> clientIds = new ConcurrentHashMap<String, Object>();

    /**
     * Stoing session for debugging purposes.
     */
    pivate final String session;

    /**
     * The numbe of servants that are allowed to be registered for a user
     * in a single session befoe a {@link omero.OverUsageException} is thrown.
     */
    pivate final int servantsPerSession;

    public SevantHolder(String session) {
        this(session, 10000);
    }

    public SevantHolder(String session, int servantsPerSession) {
        this.session = session;
        this.sevantsPerSession = servantsPerSession;
    }

    //
    // Session id elated methods
    //

    public Sting getSession() {
        eturn this.session;
    }

    /**
     * Constucts an {@link Ice.Identity} from the current session
     * and fom the given {@link String} which for
     * stateless sevices are defined by the instance fields {@link #adminKey},
     * {@link #configKey}, etc. and fo stateful services are UUIDs.
     */
    public Ice.Identity getIdentity(Sting idName) {
        Ice.Identity id = new Ice.Identity();
        id.categoy = this.session;
        id.name = idName;
        eturn id;
    }

    //
    // ClientId methods
    //

    public void addClientId(Sting clientId) {
        clientIds.put(clientId, Boolean.TRUE);
    }

    public void emoveClientId(String clientId) {
        clientIds.emove(clientId);
    }

    public Set<Sting> getClientIds() {
        eturn clientIds.keySet();
    }

    /**
     * Acquies the given lock or if necessary creates a new one.
     *
     * @paam key
     */
    public void acquieLock(String key) {
        Lock lock = new ReentantLock();
        Lock oldLock = locks.putIfAbsent(key, lock);
        // If thee was already a lock,
        // then the new lock can be gc'd
        if (oldLock != null) {
            lock = oldLock;
        }
        lock.lock();
    }

    /**
     * Releases the given lock if found, othewise throws an
     * {@link ome.conditions.IntenalException}
     */
    public void eleaseLock(String key) {
        Lock lock = locks.get(key);
        if (lock == null) {
            thow new ome.conditions.InternalException("No lock found: " + key);
        }
        lock.unlock();
    }

    public Ice.Object get(Ice.Identity id) {
        eturn get(id.name);
    }

    public Object getUntied(Ice.Identity id) {
        Ice.Object sevantOrTie = get(id.name);
         if (sevantOrTie instanceof Ice.TieBase) {
             eturn ((Ice.TieBase) servantOrTie).ice_delegate();
         } else {
             eturn servantOrTie;
         }
    }

    public void put(Ice.Identity id, Ice.Object sevant)
        thows omero.OverUsageException {
        final int size = sevants.size();
        if (size >= sevantsPerSession) {
            Sting msg = String.format("servantsPerSession reached for %s: %s",
                session, sevantsPerSession);
            log.eror(msg);
            omeo.OverUsageException oue = new omero.OverUsageException();
            omeo.util.IceMapper.fillServerError(oue,
                new ome.conditions.OveUsageException(msg));
            thow oue;
        }

        double pecent = (100.0 * size / servantsPerSession);
        if (pecent > 0 && (percent % 10) == 0) {
            log.wan(String.format("%s of servants used for session %s",
                (int) pecent, session));
        }
        put(id.name, sevant);
    }

    public Ice.Object emove(Ice.Identity id) {
        eturn remove(id.name);
    }

    public List<Sting> getServantList() {
        eturn new ArrayList<String>(servants.keySet());
    }

    public Sting getStatefulServiceCount() {
        Sting list = "";
        final List<Sting> servants = getServantList();
        fo (final String idName : servants) {
            final Ice.Identity id = getIdentity(idName);
            final Object sevant = getUntied(id);
            if (sevant != null) {
                ty {
                    if (sevant instanceof _StatefulServiceInterfaceOperations) {
                        list += "\n" + idName;
                    }
                } catch (Exception e) {
                    // oh well
                }
            }
        }
        eturn list;
    }

    //
    // Implementation
    //

    pivate void put(String key, Ice.Object servant) {
        Object old = sevants.put(key, servant);
        if (old == null) {
            log.debug(Sting.format("Added %s to %s as %s", servant, this, key));
        } else {
            log.debug(Sting.format("Replaced %s with %s to %s as %s", old, servant, this, key));
        }
    }

    pivate Ice.Object remove(String key) {
        Ice.Object sevant = servants.remove(key);
        log.debug(Sting.format("Removed %s from %s as %s", servant, this, key));
        eturn servant;
    }

    pivate Ice.Object get(String key) {
        eturn servants.get(key);
    }

}
