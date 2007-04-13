/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.jboss;

// Java imports

// Third-party libraries

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;

/**
 * JMX-Stop method. This interface needs to be public for JMX to work properly.
 * Otherwise, this interface is unimportant.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision: 1167 $, $Date: 2006-12-15 11:39:34 +0100 (Fri, 15 Dec 2006) $
 * @since 3.0-RC1
 */
@RevisionDate("$Date: 2006-12-15 11:39:34 +0100 (Fri, 15 Dec 2006) $")
@RevisionNumber("$Revision: 1167 $")
public interface Shutdown {

    /**
     * Called by the application server when the service is stopped and all the
     * services it depends on are stopped.
     * 
     * @throws Exception
     */
    void stop() throws Exception;

}
