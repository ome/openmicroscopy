/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.jboss;

import java.util.ResourceBundle;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.system.OmeroContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.Management;
import org.jboss.annotation.ejb.Service;

/**
 * Hook run after all the application has been deployed to the server. At that
 * point, it can be guaranteed that the Omero classes are available.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @since 3.0-Beta1
 * @see <a href="https://trac.openmicroscopy.org.uk/omero/ticket/444">ticket:444</a>
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
@Service(objectName = "omero:service=OmeroContextHook")
@Management(StartupAndShutdown.class)
public class OmeroContextHook implements StartupAndShutdown {

    public static Log log = LogFactory.getLog(OmeroContextHook.class);

    ResourceBundle bundle = ResourceBundle.getBundle("omero");

    OmeroContext ctx;

    /**
     * Creates the {@link OmeroContext} and prints out a banner on succes.
     * 
     * @see <a
     *      href="https://trac.openmicroscopy.org.uk/omero/ticket/444">ticket:444</a>
     */
    public void start() throws Exception {

        ctx = OmeroContext.getManagedServerContext();

    }

    /**
     * Closes the {@link OmeroContext} instance. This should shut down all
     * asynchronous resources.
     */
    public void stop() throws Exception {

        if (ctx != null) {
            try {
                ctx.close();
            } finally {
                // Setting it to null, so that it won't be tried again
                // and again.
                ctx = null;
            }
        }
    }

}
