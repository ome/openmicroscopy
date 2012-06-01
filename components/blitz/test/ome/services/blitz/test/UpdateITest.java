/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test;

import static omero.rtypes.rstring;

import java.util.List;

import ome.model.annotations.ProjectAnnotationLink;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import omero.ApiUsageException;
import omero.api.AMD_IUpdate_deleteObject;
import omero.model.Annotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.IObject;
import omero.model.ImageI;
import omero.model.Project;
import omero.model.ProjectI;
import omero.sys.ParametersI;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = "integration")
public class UpdateITest extends AbstractServantTest {

    @Override
    @BeforeMethod
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Test(groups = "ticket:1183")
    public void testProjectWithAnnotationCausesError() throws Exception {
        Project p = new ProjectI();
        p.setName(rstring("ticket:1183"));
        p.linkAnnotation(new CommentAnnotationI());
        p = (Project) assertSaveAndReturn(p);
    }

    public void testQueryWithSelect() throws Exception {
        Project p = new ProjectI();
        p.setName(rstring(""));
        Dataset d = new DatasetI();
        d.setName(rstring(""));
        p.linkDataset(d);
        assertSaveAndReturn(p);
        List<IObject> objects = assertFindByQuery(
                "from Project p join fetch p.datasetLinks pdl "
                        + "join fetch pdl.child ", null);
        for (IObject object : objects) {
            assertTrue(object instanceof Project);
        }
    }
    
    @Test(expectedExceptions = ApiUsageException.class)
    public void testNPEOnMissingQuotes() throws Exception {
        Project p = new ProjectI();
        p.setName(rstring(""));
        assertSaveAndReturn(p);
        List<IObject> objects = assertFindByQuery(
                "from Project p where p.name = foo ", null);
        for (IObject object : objects) {
            assertTrue(object instanceof Project);
        }
    }

    @Test(groups = "ticket:1193")
    public void testNullDetails() throws Exception {
        Project prj = new ProjectI();
        prj.setName(rstring("1193"));
        Annotation a = new CommentAnnotationI();
        prj.linkAnnotation(a);
        assertSaveAndReturn(prj);

        String q = "select pal from ProjectAnnotationLink pal join fetch pal.child";
        Parameters param = new Parameters(new Filter().page(0, 2));
        List<ProjectAnnotationLink> pals = user.managedSf.getQueryService()
                .findAllByQuery(q, param);
        assertNotNull(pals.get(0).getDetails());

        omero.sys.Parameters p = new ParametersI().page(0, 2);
        List<IObject> objects = assertFindByQuery(q, p);
        assertNotNull(objects.get(0).getDetails());
    }

    @Test(groups = "ticket:587", expectedExceptions = ApiUsageException.class)
    public void testDeleteImageNotPixels() throws Exception {
        final long iid = makeImage();
        final RV rv = new RV();
        user.update.deleteObject_async(new AMD_IUpdate_deleteObject() {
            public void ice_response() {
                rv.rv = Boolean.TRUE;
            }

            public void ice_exception(Exception ex) {
                rv.ex = ex;
            }
        }, new ImageI(iid, false), current("deleteObject"));
        assertNull(rv.rv);
        assertNotNull(rv.ex);
        throw rv.ex;
    }

}
