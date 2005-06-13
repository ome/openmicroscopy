/*
 * org.openmicroscopy.omero.server.itests
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
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
package org.openmicroscopy.omero.server.itests;

//Java imports

//Third-party libraries
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

//Application-internal dependencies
import org.openmicroscopy.omero.tests.AbstractOmeroHierarchyBrowserIntegrationTest;
import org.openmicroscopy.omero.tests.OMEData;
import org.openmicroscopy.omero.tests.OMEPerformanceData;
import org.openmicroscopy.omero.util.Utils;

/** 
 * tests to the database but without going over the wire. 
 * <code>testHessian</code> also tests the serialization 
 * process. 
 * 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
 * @DEV.TODO find the hessian bug!
 */
public class OmeroServiceTest
        extends
            AbstractOmeroHierarchyBrowserIntegrationTest {

    private static Log log = LogFactory.getLog(OmeroServiceTest.class);
    
    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {

        return new String[]{
                "WEB-INF/services.xml",
                "WEB-INF/dao.xml",
                "WEB-INF/data.xml",
                "WEB-INF/test/config-test.xml"}; 
    }
    
    public OmeroServiceTest(String name) {
        super(name,new OMEPerformanceData());
    }

    public OmeroServiceTest(OMEData data) {
        super("OmeroGrinderTest with Data",data);
    }
    
    public void testHessian(){
        log.info(getData());
        Utils.structureSize(this.testFindCGCIHierarchies());
        Utils.structureSize(this.testFindDatasetAnnotationsSet());
        Utils.structureSize(this.testFindDatasetAnnotationsSetForExperimenter());
        Utils.structureSize(this.testFindImageAnnotationsSet());
        Utils.structureSize(this.testFindImageAnnotationsSetForExperimenter());
        Utils.structureSize(this.testFindPDIHierarchies());
        Utils.structureSize(this.testLoadCGCIHierarchyCategory());
        Utils.structureSize(this.testLoadCGCIHierarchyCategoryGroup());
        Utils.structureSize(this.testLoadPDIHierarchyDataset());
        Utils.structureSize(this.testLoadPDIHierarchyProject());
    }
}
