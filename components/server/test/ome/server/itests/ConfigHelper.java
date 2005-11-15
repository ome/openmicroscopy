/*
 * ome.server.itests.ConfigHelper
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */
package ome.server.itests;

//Java imports


//Third-party libraries


//Application-internal dependencies


/** 
 * tests for a HQL join bug.
 *  
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 */
public class ConfigHelper {

    final static String[] ALL = new String[] { 
        "WEB-INF/aop.xml",
        "WEB-INF/services.xml",
        "WEB-INF/security.xml",
        "WEB-INF/dao.xml",
        "WEB-INF/hibernate.xml",
        "WEB-INF/dbcp.xml", 
        "WEB-INF/config-local.xml",
        "WEB-INF/test/test.xml"};
    
    final static String[] DAO = new String[] { 
        "WEB-INF/aop.xml",
        "WEB-INF/dao.xml",
        "WEB-INF/hibernate.xml",
        "WEB-INF/dbcp.xml", 
        "WEB-INF/config-local.xml",
        "WEB-INF/test/test.xml"};
    
    final static String[] DBUNIT = new String[] { 
        "WEB-INF/aop.xml",
        "WEB-INF/dao.xml",
        "WEB-INF/hibernate.xml",
        "WEB-INF/test/dbcp.xml", 
        "WEB-INF/config-local.xml",
        "WEB-INF/test/test.xml"};
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    public static String[] getConfigLocations() {

        return ALL;
    }

    public static String[] getDaoConfigLocations() {

        return DAO;
    }

    public static String[] getDbUnitConfigLocations() {

        return DBUNIT;
    }    
    
    
}
