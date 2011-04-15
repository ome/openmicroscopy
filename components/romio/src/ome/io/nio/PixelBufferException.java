/*
 * ome.io.nio.PixelBufferException
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio;

/**
 * @author callan
 * 
 */
public class PixelBufferException extends RuntimeException {
    /**
     * 
     */
    private static final long serialVersionUID = -1928144831989213423L;

    protected String message;

    public PixelBufferException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
