/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.util;

import org.apache.commons.logging.Log;

import Ice.Logger;

/**
 * A simple Adapter to have the <i>ICE</i> runtime log to our log file.
 * 
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>
 *         Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:a.falconi@dundee.ac.uk"> a.falconi@dundee.ac.uk</a>
 * @version 2.2 <small> (<b>Internal version:</b> $Revision: 1.1 $ $Date:
 *          2005/06/20 09:46:34 $) </small>
 * @since OME2.2
 */
public class CommonsLoggingAdapter implements Logger {

    private final Log log;

    public CommonsLoggingAdapter(Log logger) {
        this.log = logger;
    }

    public void error(String message) {
        log.error(message);
    }

    public void print(String message) {
        log.info(message);
    }

    public void trace(String category, String message) {
        log.debug(category + ":" + message);
    }

    public void warning(String message) {
        log.warn(message);
    }

    public Logger cloneWithPrefix(java.lang.String unused) {
        throw new UnsupportedOperationException("NYI");
    }

}
