/*
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
    private final MethodCounter methods;
    
    public SimpleSessionStats(ObjectsReadCounter read, ObjectsWrittenCounter written, MethodCounter methods) {
        this.read = read;
        this.written = written;
        this.methods = methods;
    }

    public void methodIn() {
        this.methods.increment(1);
    }

    public long methodCount() {
        return this.methods.count;
    }

    public void methodOut() {
        this.methods.increment(-1);
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
