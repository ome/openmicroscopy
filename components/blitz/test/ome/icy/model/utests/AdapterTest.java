/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.utests;

import junit.framework.TestCase;
import ome.model.annotations.ProjectAnnotation;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.containers.ProjectDatasetLink;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.display.CodomainMapContext;
import ome.model.display.PlaneSlicingContext;
import ome.model.display.RenderingDef;
import omero.model.PlaneSlicingContextI;
import omero.model.ProjectAnnotationI;
import omero.model.ProjectDatasetLinkI;
import omero.model.ProjectI;
import omero.util.IceMapper;

import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

public class AdapterTest extends TestCase {

    Project p;

    Dataset d;

    Image i;

    Pixels pix;

    @Override
    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception {
        p = new Project();
        d = new Dataset();
        i = new Image();
        pix = new Pixels();
    }

    @Test
    public void test_simple() throws Exception {
        IceMapper mapper = new IceMapper();
        ProjectI p_remote = (ProjectI) mapper.map(p);
        Project p_test = (Project) mapper.reverse(p_remote);
    }

    @Test
    public void test_with_values() throws Exception {
        p.setName("test");
        IceMapper mapper = new IceMapper();
        ProjectI p_remote = (ProjectI) mapper.map(p);
        Project p_test = (Project) mapper.reverse(p_remote);
        assertTrue("test".equals(p_remote.getName()));
        assertTrue("test".equals(p_test.getName()));
    }

    @Test
    public void test_with_collections() throws Exception {
        p.linkDataset(d);
        IceMapper mapper = new IceMapper();
        ProjectI p_remote = (ProjectI) mapper.map(p);
        Project p_test = (Project) mapper.reverse(p_remote);
        assertTrue(p_remote.datasetLinks.size() == 1);
        assertTrue(p_test.sizeOfDatasetLinks() == 1);
    }

    @Test
    public void test_complex() throws Exception {
        p.linkDataset(d);
        d.linkImage(i);
        i.addPixels(pix);
        IceMapper mapper = new IceMapper();
        ProjectI p_remote = (ProjectI) mapper.map(p);
        Project p_test = (Project) mapper.reverse(p_remote);
        assertTrue(p_remote.datasetLinks.size() == 1);
        assertTrue(p_test.sizeOfDatasetLinks() == 1);
        ProjectDatasetLinkI pdl_remote = (ProjectDatasetLinkI) p_remote.datasetLinks
                .get(0);
        ProjectDatasetLink pdl_test = (ProjectDatasetLink) mapper
                .reverse(pdl_remote);
        assertTrue(pdl_remote.parent == p_remote);
        assertTrue(pdl_test.parent() != p.collectDatasetLinks(null).get(0));

        omero.model.Dataset d_remote = (omero.model.Dataset) pdl_remote.child;
        assertTrue(d_remote.imageLinks.size() == 1);
        omero.model.DatasetImageLink dil_remote = (omero.model.DatasetImageLink) d_remote.imageLinks
                .get(0);
        assertTrue(dil_remote.parent == d_remote);
        omero.model.Image i_remote = (omero.model.Image) dil_remote.child;
        assertTrue(i_remote.pixels.size() == 1);
        omero.model.Pixels pix_remote = (omero.model.Pixels) i_remote.pixels
                .get(0);
        assertTrue(pix_remote.image == i_remote);
    }

    @Test
    public void testInheritance() throws Exception {

        IceMapper mapper = new IceMapper();

        RenderingDef def = new RenderingDef();
        CodomainMapContext cmc = new PlaneSlicingContext();
        cmc.setRenderingDef(def);

        PlaneSlicingContextI cmc_remote = (PlaneSlicingContextI) mapper
                .map(cmc);
        CodomainMapContext cmc_test = (CodomainMapContext) mapper
                .reverse(cmc_remote);
    }

    @Test
    public void testUnloadedCollectionIsMappedUnloaded() throws Exception {

        IceMapper mapper = new IceMapper();

        Project p = new Project();
        p.putAt(Project.DATASETLINKS, null);
        assertTrue(p.sizeOfDatasetLinks() < 0);

        ProjectI p_remote = (ProjectI) mapper.map(p);
        assert (p_remote.datasetLinks != null);
    }

    @Test
    public void testUnloadedCollectionisReversedUnloaded() throws Exception {

        IceMapper mapper = new IceMapper();

        ProjectI p_remote = new ProjectI();
        p_remote.datasetLinksLoaded = false;
        
        Project p = (Project) mapper.reverse(p_remote);
        
        assert (p.sizeOfDatasetLinks() < 0);

    }

    @Test
    public void testUnloadedObjectisMappedUnloaded() throws Exception {

        IceMapper mapper = new IceMapper();
     
        ProjectAnnotation pa = new ProjectAnnotation();
        pa.setProject(new Project(null,false));
        
        ProjectAnnotationI pa_remote = (ProjectAnnotationI) mapper.map(pa);
        assertFalse(pa_remote.project.loaded);
        
    }
    @Test
    public void testUnloadedObjectIsReversedUnloaded() throws Exception {

        IceMapper mapper = new IceMapper();
        
        ProjectAnnotationI pa_remote = new ProjectAnnotationI();
        ProjectI p_remote = new ProjectI();
        p_remote.unload();
        pa_remote.project = p_remote;
        
        ProjectAnnotation pa = (ProjectAnnotation) mapper.reverse(pa_remote);
        assertFalse(pa.getProject().isLoaded());
    }
    
}
