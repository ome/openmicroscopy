/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test;

import static omero.rtypes.rstring;

import java.util.List;

import junit.framework.TestCase;
import ome.logic.HardWiredInterceptor;
import ome.model.annotations.ProjectAnnotationLink;
import ome.parameters.Filter;
import ome.parameters.Parameters;
import ome.security.SecuritySystem;
import ome.services.blitz.fire.AopContextInitializer;
import ome.services.blitz.impl.AbstractAmdServant;
import ome.services.blitz.impl.QueryI;
import ome.services.blitz.impl.ServiceFactoryI;
import ome.services.blitz.impl.UpdateI;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.sessions.SessionManager;
import ome.services.throttling.InThreadThrottlingStrategy;
import ome.system.OmeroContext;
import ome.system.ServiceFactory;
import omero.api.AMD_IQuery_findAllByQuery;
import omero.api.AMD_IUpdate_saveAndReturnObject;
import omero.model.Annotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.IObject;
import omero.model.Project;
import omero.model.ProjectI;
import omero.sys.ParametersI;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
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

}
