/*
 * ome.formats.importer.HTMLMessengerException
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats.importer.util;

/**
 * @author TheBrain
 * 
 */
public class HtmlMessengerException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 6967360147496748871L;

    public HtmlMessengerException(String message, Exception e) {
        super(message, e);
    }
}
