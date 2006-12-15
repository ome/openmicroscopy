/*
 * ome.formats.importer.HTMLMessengerException
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package src.adminTool.ui.messenger;

/**
 * @author TheBrain
 * 
 */
public class HtmlMessengerException extends Exception {
    /**
     * 
     */
    private static final long serialVersionUID = 464850411907138178L;

    public HtmlMessengerException(String message, Exception e) {
        super(message, e);
    }
}
