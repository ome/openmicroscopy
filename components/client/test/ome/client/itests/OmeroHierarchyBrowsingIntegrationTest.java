/*
 * ome.client.itests.OmeroHierarchyBrowsingIntegrationTest
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

package ome.client.itests;

//Java imports

//Third-party libraries

//Application-internal dependencies
import ome.testing.AbstractOmeroHierarchyBrowserIntegrationTest;
import ome.testing.OMEData;
import ome.testing.OMEPerformanceData;

/** Tests calls through to the database, using all necessary layers. Specifics 
 * are available in abstract super class.
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 * @see ome.tests.AbstractOmeroHierarchyBrowserIntegrationTest
 */
public class OmeroHierarchyBrowsingIntegrationTest
        extends
            AbstractOmeroHierarchyBrowserIntegrationTest {
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {
        return new String[]{
                "ome/client/spring.xml",
                "ome/client/itests/test.xml",
                "ome/client/itests/data.xml"
                }; 
    }
    
    public OmeroHierarchyBrowsingIntegrationTest(String name) {
        super(name,new OMEPerformanceData());
    }

    public OmeroHierarchyBrowsingIntegrationTest(OMEData data) {
        super("Omero Integration Test with Data",data);
    }
   
}
