/*
 * ome.io.nio.AbstractBuffer
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio;

/**
 * @author jmoore
 * 
 */
public class AbstractBuffer {
    private String path;

    AbstractBuffer(String path) {
        if (path == null) {
            throw new NullPointerException("Expecting not-null path argument.");
        }

        this.path = path;
    }

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return getPath();
    }

}
