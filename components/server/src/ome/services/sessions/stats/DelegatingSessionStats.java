/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions.stats;



/**
 * Delegates to a {@link SessionStats} which is acquired from {@link #stats()}.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4
 */
public abstract class DelegatingSessionStats implements SessionStats {

    protected abstract SessionStats stats();

    public final void loadedObjects(int objects) {
        stats().loadedObjects(objects);
    }

    public final void readBytes(int bytes) {
        stats().readBytes(bytes);
    }

    public final void updatedObjects(int objects) {
        stats().updatedObjects(objects);
    }

    public final void writtenBytes(int bytes) {
        stats().writtenBytes(bytes);
    }

}
