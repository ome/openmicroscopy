/*
 * Created on Jun 7, 2005
 */
package org.openmicroscopy.omero.test;

import java.util.Map;
import java.util.Set;

import org.openmicroscopy.omero.model.Category;
import org.openmicroscopy.omero.model.CategoryGroup;
import org.openmicroscopy.omero.model.Dataset;
import org.openmicroscopy.omero.model.Project;
import org.openmicroscopy.omero.shoolaadapter.AdapterUtils;
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
        return new String[] { "org/openmicroscopy/omero/client/spring.xml",
                "org/openmicroscopy/omero/client/itests/test.xml" };
    }

    public OmeroGrinderTest(String name) {
        super(name, new OMEPerformanceData());
    }

    public OmeroGrinderTest(OMEData data) {
        super("OmeroGrinderTest with Data", data);
    }

    public Object testFindCGCIHierarchies() {
        return AdapterUtils.adaptFoundCGCIHierarchies((Set) super
                .testFindCGCIHierarchies());
    }

    public Object testFindDatasetAnnotationsSet() {
        return AdapterUtils.adaptFoundDatasetAnnotations((Map) super
                .testFindDatasetAnnotationsSet());
    }

    public Object testFindDatasetAnnotationsSetForExperimenter() {
        return AdapterUtils.adaptFoundDatasetAnnotations((Map) super
                .testFindDatasetAnnotationsSetForExperimenter());
    }

    public Object testFindImageAnnotationsSet() {
        return AdapterUtils.adaptFoundImageAnnotations((Map) super
                .testFindImageAnnotationsSet());
    }

    public Object testFindImageAnnotationsSetForExperimenter() {
        return AdapterUtils.adaptFoundImageAnnotations((Map) super
                .testFindImageAnnotationsSetForExperimenter());
    }

    public Object testFindPDIHierarchies() {
        return AdapterUtils.adaptFoundPDIHierarchies((Set) super
                .testFindPDIHierarchies());
    }

    public Object testLoadCGCIHierarchyCategory() {
        return AdapterUtils.adaptLoadedCGCIHierarchy(Category.class,
                super.testLoadCGCIHierarchyCategory());                
    }

    public Object testLoadCGCIHierarchyCategoryGroup() {
        return AdapterUtils.adaptLoadedCGCIHierarchy(CategoryGroup.class,
                super.testLoadCGCIHierarchyCategoryGroup());                
    }

    public Object testLoadPDIHierarchyDataset() {
        return AdapterUtils.adaptLoadedPDIHierarchy(Dataset.class,
                super.testLoadPDIHierarchyDataset());
    }

    public Object testLoadPDIHierarchyProject() {
        return AdapterUtils.adaptLoadedPDIHierarchy(Project.class,
                super.testLoadPDIHierarchyProject());

    }
}
