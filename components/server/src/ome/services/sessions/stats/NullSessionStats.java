/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions.stats;



/**
 * Implementation of {@link SessionStats} which does nothing. This is used for
 * internal and privileged sessions which are not counted.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4
 */
public class NullSessionStats implements SessionStats {

    public void methodIn() {
        // nothing
    }

    public long methodCount() {
        return 0;
    }

    public void methodOut() {
        // nothing
    }

    public void loadedObjects(int objects) {
        // nothing
    }

    public void readBytes(int bytes) {
        // nothing
    }

    public void updatedObjects(int objects) {
        // nothing
    }

    public void writtenBytes(int bytes) {
        // nothing
    }

}
