/*
 * adminTool.model.UnknownException 
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package src.adminTool.model;

// Java imports

// Third-party libraries

// Application-internal dependencies

/**
 * 
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $)
 *          </small>
 * @since OME3.0
 */
public class UnknownException extends AdminToolException {
    /**
     * 
     */
    private static final long serialVersionUID = 8488989184105545895L;
    public Exception e;

    public UnknownException(Exception parent) {
        super(parent);
    }
}
