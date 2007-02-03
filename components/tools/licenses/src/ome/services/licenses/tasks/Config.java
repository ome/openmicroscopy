/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.licenses.tasks;

import java.util.Properties;

import ome.annotations.RevisionDate;
import ome.annotations.RevisionNumber;
import ome.services.licenses.LicensedServiceFactory;
import ome.system.ServiceFactory;
import ome.util.tasks.Configuration;
import ome.util.tasks.Task;

/**
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @see Configuration
 * @see Task
 * @see Run
 * @see Configuration
 * @since 3.0-RC1
 */
@RevisionDate("$Date: 2006-12-15 11:39:34 +0100 (Fri, 15 Dec 2006) $")
@RevisionNumber("$Revision: 1167 $")
public class Config extends Configuration {

    public Config(Properties props) {
        super(props);
    }

    @Override
    public ServiceFactory createServiceFactory() {
        return new LicensedServiceFactory(getProperties(), getServer(),
                getLogin());
    }

}
