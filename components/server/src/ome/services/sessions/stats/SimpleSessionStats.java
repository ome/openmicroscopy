/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.sessions.stats;



/**
 * 
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since Beta4
 */
public class SimpleSessionStats implements SessionStats {

    private final ObjectsReadCounter read;
    private final ObjectsWrittenCounter written;
    
    public SimpleSessionStats(ObjectsReadCounter read, ObjectsWrittenCounter written) {
        this.read = read;
        this.written = written;
    }

    public void loadedObjects(int objects) {
        this.read.increment(objects);
    }

    public void readBytes(int bytes) {
        throw new UnsupportedOperationException();
    }

    public void updatedObjects(int objects) {
        this.written.increment(objects);
    }

    public void writtenBytes(int bytes) {
        throw new UnsupportedOperationException();   
    }

}
