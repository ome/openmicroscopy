/*
 * Created on Jun 7, 2005
 */
package org.openmicroscopy.omero.server.itests;

import org.openmicroscopy.omero.tests.AbstractOmeroHierarchyBrowserIntegrationTest;
import org.openmicroscopy.omero.tests.OMEData;
import org.openmicroscopy.omero.tests.OMEPerformanceData;

/**
 * @author josh
 */
public class OmeroServiceTest
        extends
            AbstractOmeroHierarchyBrowserIntegrationTest {

    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {

        return new String[]{
                "WEB-INF/services.xml",
                "WEB-INF/dao.xml",
                "WEB-INF/data.xml",
                "WEB-INF/config-test.xml"}; 
    }
    
    public OmeroServiceTest(String name) {
        super(name,new OMEPerformanceData());
    }

    public OmeroServiceTest(OMEData data) {
        super("OmeroGrinderTest with Data",data);
    }

}
