/*
 * ome.server.itests.ConfigHelper
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests;

// Java imports

// Third-party libraries

// Application-internal dependencies

/**
 * tests for a HQL join bug.
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
public class ConfigHelper {

    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    public static String[] getConfigLocations() {

        return new String[] { "ome/services/aop.xml",
                "ome/services/services.xml", "ome/services/security.xml",
                "ome/services/hibernate.xml", "ome/services/dbcp.xml",
                "ome/services/config-local.xml", "ome/services/test/test.xml" };
    }

    public static String[] getDaoConfigLocations() {

        return new String[] { "ome/services/aop.xml",
                "ome/services/hibernate.xml", "ome/services/dbcp.xml",
                "ome/services/config-local.xml", "ome/services/test/test.xml" };
    }

    public static String[] getDbUnitConfigLocations() {

        return new String[] { "ome/services/aop.xml",
                "ome/services/hibernate.xml", "ome/services/test/dbcp.xml",
                "ome/services/config-local.xml", "ome/services/test/test.xml" };
    }

}
