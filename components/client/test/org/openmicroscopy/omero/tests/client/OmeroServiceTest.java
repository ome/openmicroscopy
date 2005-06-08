/*
 * Created on Jun 7, 2005
 */
package org.openmicroscopy.omero.tests.client;

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
        System.out.println(System.getProperty("user.dir"));
        return new String[]{
                "file:../server/web/WEB-INF/services.xml",
                "org/openmicroscopy/omero/tests/client/test.xml",
                "file:../server/web/WEB-INF/dao.xml"}; 
    }
    
    public OmeroServiceTest(String name) {
        super(name,new OMEPerformanceData());
    }

    public OmeroServiceTest(OMEData data) {
        super("OmeroGrinderTest with Data",data);
    }

}
