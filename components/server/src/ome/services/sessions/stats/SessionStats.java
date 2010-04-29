/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions.stats;

/**
 * Thread-safe counter for all
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4
 */
public interface SessionStats {

    void methodIn();

    long methodCount();

    void methodOut();

    void readBytes(int bytes);

    void writtenBytes(int bytes);

    void loadedObjects(int objects);

    void updatedObjects(int objects);
}
