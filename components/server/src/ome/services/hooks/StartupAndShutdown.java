/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.hooks;

// Java imports

// Third-party libraries

// Application-internal dependencies
import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;

/**
 * JMX-Start and Stop methods. 
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision: 1167 $, $Date: 2006-12-15 11:39:34 +0100 (Fri, 15 Dec 2006) $
 * @since 3.0-Beta1
 * @see Startup
 * @see Shutdown
 */
@RevisionDate("$Date: 2006-12-15 11:39:34 +0100 (Fri, 15 Dec 2006) $")
@RevisionNumber("$Revision: 1167 $")
public interface StartupAndShutdown extends Startup, Shutdown {

}
