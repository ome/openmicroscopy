/*
 * Created on Jun 7, 2005
 */
package org.openmicroscopy.omero.tests.client;

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
                "org/openmicroscopy/omero/tests/client/test.xml"}; 
    }
    
    public OmeroGrinderTest(String name) {
        super(name,new OMEPerformanceData());
    }

    public OmeroGrinderTest(OMEData data) {
        super("OmeroGrinderTest with Data",data);
    }

}
