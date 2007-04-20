/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.icy;

// Java imports

// Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.annotation.ejb.Service;
import org.jboss.annotation.ejb.Management;

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.services.jboss.StartupAndShutdown;
import ome.system.OmeroContext;
import ome.system.ServiceFactory;

/**
 * Hook run after all the application has been deployed to the server. At that
 * point, it can be guaranteed that the Omero classes are available and so
 * attempting to connect to the database "internally" should work.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision: 1175 $, $Date: 2006-12-15 11:39:34 +0100 (Fri, 15 Dec
 *          2006) $
 * @since 3.0-Beta1
 * @see <a href="https://trac.openmicroscopy.org.uk/omero/ticket/444">ticket:444</a>
 */
@RevisionDate("$Date: 2006-12-15 12:28:54 +0100 (Fri, 15 Dec 2006) $")
@RevisionNumber("$Revision: 1175 $")
//@Service(objectName = "omero:service=JBossService")
//@Management(StartupAndShutdown.class)
public class JBossService implements StartupAndShutdown {

    private final static Log log = LogFactory.getLog(JBossService.class);

    public void start() throws Exception {
        log.info("Starting.");
        log.debug("Acquiring OMERO.blitz context.");
        OmeroContext ctx = OmeroContext.getInstance("OMERO.blitz");
        
    }

    public void stop() throws Exception {
        log.info("Stopping.");
        log.debug("Destroying OMERO.blitz context.");
        OmeroContext.getInstance("OMERO.blitz").destroy();
    }

}
