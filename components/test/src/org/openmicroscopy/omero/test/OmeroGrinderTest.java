/*
 * org.openmicroscopy.omero.test.OmeroGrinderTest
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
package org.openmicroscopy.omero.test;

//Java imports
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.omero.model.Category;
import org.openmicroscopy.omero.model.CategoryGroup;
import org.openmicroscopy.omero.model.Dataset;
import org.openmicroscopy.omero.model.Project;
import org.openmicroscopy.omero.shoolaadapter.AdapterUtils;
import org.openmicroscopy.omero.tests.AbstractOmeroHierarchyBrowserIntegrationTest;
import org.openmicroscopy.omero.tests.OMEData;
import org.openmicroscopy.omero.tests.OMEPerformanceData;

/** 
 * used externally by Grinder to test not just client code but also
 * conversion by AdapterUtils to Shoola code. 
 * @author  Josh Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 
 * <small>
 * (<b>Internal version:</b> $Rev$ $Date$)
 * </small>
 * @since 1.0
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
