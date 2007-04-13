/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.jboss;

// Java imports

// Third-party libraries
import org.hibernate.SessionFactory;
import org.jboss.annotation.ejb.Service;
import org.jboss.annotation.ejb.Management;

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.system.OmeroContext;
import ome.system.Version;

/**
 * Hook run after all the application has been deployed to the server. At that
 * point, it can be guaranteed that the Omero classes are available and so
 * attempting to connect to the database "internally" should work.
 *
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision$, $Date: 2006-12-15 11:39:34 +0100 (Fri, 15 Dec
 *          2006) $
 * @since 3.0-Beta1
 * @see <a href="https://trac.openmicroscopy.org.uk/omero/ticket/444">ticket:444</a>
 */
@RevisionDate("$Date$")
@RevisionNumber("$Revision$")
@Service(objectName = "omero:service=OmeroContextHook")
@Management(StartupAndShutdown.class)
public class OmeroContextHook implements StartupAndShutdown {

    OmeroContext ctx = OmeroContext.getManagedServerContext();

    SessionFactory sf = (SessionFactory) ctx.getBean("sessionFactory");

    /**
     * Attempts twice to connect to the server to overcome any initial
     * difficulties.
     *
     * @see <a
     *      href="https://trac.openmicroscopy.org.uk/omero/ticket/444">ticket:444</a>
     */
    public void start() throws Exception {

        try {
            connect();
        } catch (Exception e) {
            // ok
        }

        connect();
        printBanner("OMERO Version: %s (Rev: %s) Ready.",
                Version.OMERO, Version.getRevision(Version.class));

    }

    /**
     * Closes the {@link OmeroContext} instance. This should shut down
     * all asynchronous resources.
     */
    public void stop() throws Exception {

        printBanner("Stoping OMERO...");

        if (ctx!=null) {
            try {
                ctx.close();
            } finally {
                // Setting it to null, so that it won't be tried again
                // and again.
                ctx = null;
            }
        }
    }

    /**
     * Attempts a simple database query.
     */
    protected void connect() {
        sf.openSession().createQuery("select count(*) from Experimenter");
    }

    private void printBanner(String format, Object...objs) {
        System.out.println("-------------------------------------------------");
        System.out.println(String.format(format, objs));
        System.out.println("-------------------------------------------------");
    }


}
