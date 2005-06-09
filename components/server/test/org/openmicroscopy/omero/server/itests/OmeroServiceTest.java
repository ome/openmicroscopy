/*
 * Created on Jun 7, 2005
 */
package org.openmicroscopy.omero.server.itests;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openmicroscopy.omero.tests.AbstractOmeroHierarchyBrowserIntegrationTest;
import org.openmicroscopy.omero.tests.OMEData;
import org.openmicroscopy.omero.tests.OMEPerformanceData;
import org.openmicroscopy.omero.util.Utils;

/**
 * @author josh
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
                "WEB-INF/config-test.xml"}; 
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
