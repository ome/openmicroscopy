/*
 * ome.server.itests.PojosServiceTest
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests;

// Java imports

// Third-party libraries
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import ome.api.IPojos;
import ome.conditions.ApiUsageException;
import ome.model.annotations.DatasetAnnotation;
import ome.model.annotations.ImageAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.testing.OMEData;
import ome.util.builders.PojoOptions;

import org.testng.annotations.Test;

/**
 * 
 * @author Josh Moore &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:josh.moore@gmx.de">josh.moore@gmx.de</a>
 * @version 1.0 <small> (<b>Internal version:</b> $Rev$ $Date$) </small>
 * @since 1.0
 */
public class PojosServiceTest extends AbstractManagedContextTest {

    protected IPojos iPojos;

    protected OMEData data;

    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        DataSource dataSource = (DataSource) applicationContext
                .getBean("dataSource");
        data = new OMEData();
        data.setDataSource(dataSource);
        iPojos = factory.getPojosService();
    }

    @Test
    public void test_unannotated_Event_version() throws Exception {
        DatasetAnnotation da = createLinkedDatasetAnnotation();
        DatasetAnnotation da_test = new DatasetAnnotation(da.getId(), false);
        iPojos.deleteDataObject(da_test, null);

    }

    @Test
    public void test_cgc_Event_version() throws Exception {
        Set results = iPojos.findCGCPaths(new HashSet(data.getMax("Image.ids",
                2)), IPojos.CLASSIFICATION_ME, null);
    }

    @Test(groups = "ticket:318")
    public void testLoadHiearchiesHandlesNullRootNodeIds() throws Exception {
        PojoOptions po;

        try {
            iPojos.loadContainerHierarchy(Project.class, null, null);
            fail("Should throw ApiUsage.");
        } catch (ApiUsageException aue) {
            // ok
        }
        po = new PojoOptions().exp(0L);
        iPojos.loadContainerHierarchy(Project.class, null, po.map());

        po = new PojoOptions().grp(0L);
        iPojos.loadContainerHierarchy(Project.class, null, po.map());

    }

    @Test(groups = "ticket:657")
    public void testAnnotationsStillCounted() throws Exception {
        Dataset d = new Dataset();
        d.setName("ticket:657");
        DatasetAnnotation da = new DatasetAnnotation();
        da.setContent("ticket:657");
        d.addDatasetAnnotation(da);
        Image i = new Image();
        i.setName("ticket:657");
        i.linkDataset(d);
        ImageAnnotation ia = new ImageAnnotation();
        ia.setContent("ticket:657");
        ia.setImage(i);
        i.addImageAnnotation(ia);

        d = iUpdate.saveAndReturnObject(d);

        Set<Image> list = iPojos.getImages(Dataset.class, Collections
                .singleton(d.getId()), null);

        i = list.iterator().next();
        d = (Dataset) i.linkedDatasetList().get(0);

        assertTrue(d.getDetails().getCounts() != null);
        assertEquals(d.getDetails().getCounts().get(DatasetAnnotation.DATASET),
                1L);
        assertTrue(i.getDetails().getCounts() != null);
        assertEquals(i.getDetails().getCounts().get(ImageAnnotation.IMAGE), 1L);

    }

    // ~ Helpers
    // =========================================================================

    private Project reloadProject(DatasetAnnotation da) {
        Dataset ds;
        Project p;
        ds = da.getDataset();
        p = (Project) ds.linkedProjectList().get(0);

        Project p_test = iPojos.loadContainerHierarchy(Project.class,
                Collections.singleton(p.getId()), null).iterator().next();
        return p_test;
    }

    private DatasetAnnotation createLinkedDatasetAnnotation() {
        DatasetAnnotation da = new DatasetAnnotation();
        Dataset ds = new Dataset();
        Project p = new Project();

        p.setName("uEv");
        p.linkDataset(ds);
        ds.setName("uEv");
        da.setContent("uEv");
        da.setDataset(ds);
        da = iPojos.createDataObject(da, null);
        return da;
    }

}
