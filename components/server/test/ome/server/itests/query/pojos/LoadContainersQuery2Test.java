/*
 *   $Id$
 *
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests.query.pojos;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import ome.model.IObject;
import ome.model.annotations.CommentAnnotation;
import ome.model.annotations.CommentAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.parameters.Parameters;
import ome.server.itests.AbstractManagedContextTest;
import ome.services.query.PojosLoadHierarchyQueryDefinition;

import org.testng.annotations.Test;

/**
 * This class is marked "2" since because the other version is failing due to
 * issues with CreatePojosFixture
 * 
 * @author josh
 * 
 */
@Test
public class LoadContainersQuery2Test extends AbstractManagedContextTest {
    PojosLoadHierarchyQueryDefinition q;

    List list;

    List level2cg;

    List level2p;

    List level1c;

    List level1ds;

    List level0img;

    Parameters po10000;

    Parameters filterForUser;

    Parameters noFilter;

    public void testQueryReturnsCounts() throws Exception {
        Dataset d = createDataset();

        long self = iAdmin.getEventContext().getCurrentUserId();

        Set<IObject> ds = iContainer.loadContainerHierarchy(Dataset.class,
                Collections.singleton(d.getId()), null);
        d = (Dataset) ds.iterator().next();
        assertNotNull(d.getAnnotationLinksCountPerOwner());
        assertTrue(d.getAnnotationLinksCountPerOwner().get(self).longValue() == 1L);
    }

    public void testQueryReturnsCountsForTwoLevels() throws Exception {
        Dataset d = createDataset();
        Project p = new Project("name");
        CommentAnnotation t = new CommentAnnotation();
        t.setNs("");
        t.setTextValue("t");
        p.linkDataset(d);
        p.linkAnnotation(t);
        p = iUpdate.saveAndReturnObject(p);

        long self = iAdmin.getEventContext().getCurrentUserId();

        Set<IObject> ps = iContainer.loadContainerHierarchy(Project.class,
                Collections.singleton(p.getId()), null);
        p = (Project) ps.iterator().next();
        d = p.linkedDatasetList().get(0);
        assertNotNull(p.getAnnotationLinksCountPerOwner());
        assertTrue(p.getAnnotationLinksCountPerOwner().get(self).longValue() == 1L);
        assertNotNull(d.getAnnotationLinksCountPerOwner());
        assertTrue(d.getAnnotationLinksCountPerOwner().get(self).longValue() == 1L);
        assertNotNull(d.getImageLinksCountPerOwner());
        assertTrue(d.getImageLinksCountPerOwner().get(self).longValue() == 1L);

    }

    @Test(groups = "ticket:882")
    public void testDatasetImageCounts() throws Exception {
        Dataset d = createDataset();
        Set<IObject> ds = this.iContainer.loadContainerHierarchy(Dataset.class,
                Collections.singleton(d.getId()), null);
        assertTrue(ds.size() == 1);
        d = (Dataset) ds.iterator().next();
        assertTrue(d.getImageLinksCountPerOwner() != null);
        assertTrue(d.getAnnotationLinksCountPerOwner() != null);

        // With leaves
        ds = this.iContainer.loadContainerHierarchy(Dataset.class, Collections
                .singleton(d.getId()), new Parameters().leaves());
        assertTrue(ds.size() == 1);
        d = (Dataset) ds.iterator().next();
        assertTrue(d.getImageLinksCountPerOwner() != null);
        assertTrue(d.getAnnotationLinksCountPerOwner() != null);
        Image i = d.linkedImageIterator().next();
        assertTrue(i.toString(), i.getAnnotationLinksCountPerOwner() != null);
    }

    @Test(groups = "ticket:907")
    public void testRootDatasetAlwaysLoadsLeaves() throws Exception {
        Dataset d = createDataset();

        // with leaves
        Set<IObject> ds = this.iContainer.loadContainerHierarchy(Dataset.class,
                Collections.singleton(d.getId()), new Parameters().leaves());
        assertTrue(ds.size() == 1);
        assertTrue(((Dataset)ds.iterator().next()).sizeOfImageLinks() == 1);

        // without leaves
        ds = this.iContainer.loadContainerHierarchy(Dataset.class, Collections
                .singleton(d.getId()), new Parameters().noLeaves());
        assertTrue(ds.size() == 1);
        assertTrue(((Dataset)ds.iterator().next()).sizeOfImageLinks() < 0);

    }
    
    @Test(groups = {"ticket:1223", "longrunning"})
    public void testStackOverflowWithLotsOfDatasets() throws Exception {
        
        loginNewUser();
        long uid = iAdmin.getEventContext().getCurrentUserId();

        for (int i = 0; i < 10; i++) {
            
            // Project with dataset
            Project p = new Project("stackoverflow: 1223");
            for (int j = 0; j < 1000; j++) {
                Dataset d = new Dataset("stackoverflow: 1223");
                p.linkDataset(d);
            }
            iUpdate.saveObject(p);
            
            // Other datasets
            Dataset[] ds = new Dataset[1000];
            for (int j = 0; j < 1000; j++) {
                ds[j] = new Dataset("stackoverflow: 1223");
            }
            iUpdate.saveArray(ds);
        }

        // with leaves
        Set<IObject> res = this.iContainer.loadContainerHierarchy(Project.class,
                null, new Parameters().exp(uid).orphan());
        assertEquals(10, res.size());
    }

    // Helpers
    // =======================================

    private Dataset createDataset() {
        Dataset d = new Dataset("name");
        Image i = new Image("name");
        CommentAnnotation t = new CommentAnnotation();
        t.setNs("");
        t.setTextValue("t");
        i.linkAnnotation(t);
        d.linkImage(i);
        d.linkAnnotation(t);
        d = iUpdate.saveAndReturnObject(d);
        return d;
    }

}
