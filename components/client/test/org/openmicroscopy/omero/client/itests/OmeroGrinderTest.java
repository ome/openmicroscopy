/*
 * Created on Jun 7, 2005
 */
package org.openmicroscopy.omero.client.itests;

import org.openmicroscopy.omero.tests.AbstractOmeroHierarchyBrowserIntegrationTest;
import org.openmicroscopy.omero.tests.OMEData;
import org.openmicroscopy.omero.tests.OMEPerformanceData;

/**
 * @author josh
 */
public class OmeroGrinderTest
        extends
            AbstractOmeroHierarchyBrowserIntegrationTest {

    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#getConfigLocations()
     */
    protected String[] getConfigLocations() {
        return new String[]{
                "org/openmicroscopy/omero/client/spring.xml",
                "org/openmicroscopy/omero/client/itests/test.xml"}; 
    }
    
    public OmeroGrinderTest(String name) {
        super(name,new OMEPerformanceData());
    }

    public OmeroGrinderTest(OMEData data) {
        super("OmeroGrinderTest with Data",data);
    }

}
