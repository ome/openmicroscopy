/*
 * ome.io.nio.DimensionsOutOfBounds
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio;

/**
 * @author callan
 * 
 */
public class DimensionsOutOfBoundsException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = -3048308196188011243L;
    private String message;

    public DimensionsOutOfBoundsException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
