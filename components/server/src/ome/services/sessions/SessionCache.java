/*
 *   $Id$
 *
 *   Copyright 2007 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.sessions;

import ome.model.meta.Session;

/**
 * Structure for holding {@link SessionContext} instances. Responsible for properly
 * synchronizing all access. 
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 3.0-Beta3
 */
public interface SessionCache {

    public static interface Visitor {
        void visit(SessionContext session);
    }
    
    boolean contains(String uuid);
    boolean contains(long id);
    void put(SessionContext session);
    SessionContext get(String uuid);
    SessionContext get(long id);
    void remove(String uuid);
    void remove(long id);
    void clear();
    int size();
    
    void readLock();
    void readUnlock();
    void writeLock();
    void writeUnlock();
    
    void each(Visitor visitor);
    
}