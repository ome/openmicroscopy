/*
 * ome.io.nio.DimensionsOutOfBoundsException
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio;

/**
 * @author callan
 * 
 */
public class DimensionsOutOfBoundsException extends PixelBufferException {
    /**
     * 
     */
    private static final long serialVersionUID = -3048308196188011243L;

    public DimensionsOutOfBoundsException(String message) {
        super(message);
    }

}
