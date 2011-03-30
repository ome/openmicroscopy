/*
 *   $Id$
 *
 *   Copyright 2008 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.io.nio;


/**
 * Exception that wraps any internal errors that arise during the instantiation
 * of a pyramid pixel buffer instance.
 * @author <br>
 *         Chris Allan&nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:callan@blackcat.ca">callan@blackcat.ca</a>
 * @since OMERO-Beta4.3
 */
public class PyramidPixelBufferException extends Exception
{
    public PyramidPixelBufferException(String message, Exception exception)
    {
        super(message, exception);
    }
}