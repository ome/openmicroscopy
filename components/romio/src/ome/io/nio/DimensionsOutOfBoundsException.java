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
public class DimensionsOutOfBoundsException extends Exception
{
    private String message;

    public DimensionsOutOfBoundsException(String message)
    {
        this.message = message;
    }
    
    public String getMessage()
    {
        return message;
    }
}
