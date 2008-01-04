/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.client.itests;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import junit.framework.TestCase;
import ome.api.IAdmin;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.DatasetAnnotationLink;
import ome.model.annotations.TextAnnotation;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.OriginalFile;
import ome.model.enums.Format;
import ome.model.meta.Experimenter;
import ome.system.Login;
import ome.system.ServiceFactory;
import ome.util.Filter;
import ome.util.Filterable;
import ome.util.builders.PojoOptions;

import org.testng.annotations.Test;

@Test(groups = { "client", "integration" })
public class TicketsUpTo1000Test extends TestCase {

    ServiceFactory sf = new ServiceFactory("ome.client.test");

    IUpdate iUpdate = sf.getUpdateService();

    IQuery iQuery = sf.getQueryService();

    IAdmin iAdmin = sf.getAdminService();

    Login rootLogin = (Login) sf.getContext().getBean("rootLogin");

    // ~ Ticket 509
    // =========================================================================

    @Test(groups = "ticket:509")
    public void test_connectionsShouldBeFreedOnClose() throws Exception {
        OriginalFile of = makeFile();
        for (int i = 0; i < 50; i++) {
            RawFileStore rfs = sf.createRawFileStore();
            rfs.setFileId(of.getId());
            rfs.close();
        }

    }

    @Test(groups = "ticket:509")
    public void test_connectionsShouldBeFreedOnTimeout() throws Exception {
        OriginalFile of = makeFile();
        int count = 0;
        for (int i = 0; i < 50; i++) {
            try {
                RawFileStore rfs = sf.createRawFileStore();
                rfs.setFileId(of.getId());
            } catch (Exception e) {
                count++;
            }
        }
        assertTrue(count + " fails!", count == 0);

    }

    @Test(groups = "ticket:509")
    public void test_simplestPossibleFail() throws Exception {
        // The TxInterceptor throws an exception which makes the id for this of
        // invalid
        OriginalFile of = makeFile();

        // must restart the app server to prime this method.
        // but probably only when it occurs in the same thread.
        RawFileStore rfs = sf.createRawFileStore();
        rfs.setFileId(of.getId().longValue());
        rfs.close();

        sf.getQueryService().get(OriginalFile.class, of.getId());

        // The TxInterceptor throws an exception which makes the id for this of
        // invalid
        of = makeFile();

        // must restart the app server to prime this method.
        // but probably only when it occurs in the same thread.
        rfs = sf.createRawFileStore();
        rfs.setFileId(of.getId());
        rfs.close();

        sf.getQueryService().get(OriginalFile.class, of.getId());

    }

    // ~ Ticket 530
    // =========================================================================

    @Test(groups = "ticket:530")
    public void test_hierarchyInversionShouldWork() throws Exception {
        Dataset d = new Dataset();
        d.setName("d");
        Image i = new Image();
        i.setName("i");
        Category c = new Category();
        c.setName("c");
        d.linkImage(i);
        c.linkImage(i);
        i = sf.getUpdateService().saveAndReturnObject(i);

        d = i.linkedDatasetList().get(0);
        c = i.linkedCategoryList().get(0);

        Long user_id = sf.getAdminService().getEventContext()
                .getCurrentUserId();
        PojoOptions po = new PojoOptions();
        po.leaves();
        po.exp(user_id);
        po.countsFor(user_id);

        Set s = sf.getPojosService().findContainerHierarchies(Project.class,
                Collections.singleton(i.getId()), po.map());
        s = sf.getPojosService().findContainerHierarchies(CategoryGroup.class,
                Collections.singleton(i.getId()), po.map());

        assertTrue(s.size() > 0);
    }

    // ~ Ticket 541
    // =========================================================================

    @Test(groups = "ticket:541")
    public void test_updateMultipleThrowsOptimisticLock() throws Exception {
        Image image = new Image();
        Dataset dataset = new Dataset();
        image.setName("ticket:541");
        dataset.setName("ticket:541");
        image.linkDataset(dataset);
        image = iUpdate.saveAndReturnObject(image);
        dataset = image.linkedDatasetList().get(0);
        image.unlinkDataset(dataset);
        iUpdate.saveArray(new IObject[] { image, dataset });
    }

    // ~ Ticket 546
    // =========================================================================

    @Test(groups = "ticket:546")
    public void test_createDataObjectsShouldLoadAnnotations() throws Exception {
        Dataset d = makeDataset();
        Annotation annotation = makeAnnotation();
        DatasetAnnotationLink link = d.linkAnnotation(annotation);
        link = sf.getPojosService().createDataObject(link, null);

        annotation = link.child();
        assertNotNull(link.parent());
        assertTrue(link.parent().isLoaded());

        d = makeDataset();
        annotation = makeAnnotation();
        link = d.linkAnnotation(annotation);

        link = (DatasetAnnotationLink) sf.getPojosService().createDataObjects(
                new DatasetAnnotationLink[] { link }, null)[0];
        assertNotNull(link.parent());
        assertTrue(link.parent().isLoaded());

    }

    private Annotation makeAnnotation() {
        TextAnnotation annotation = new TextAnnotation();
        annotation.setName("ticket:546");
        annotation.setTextValue("ticket:546");
        return annotation;
    }

    private Dataset makeDataset() {
        Dataset d = new Dataset();
        d.setName("ticket:546");
        d = sf.getPojosService().createDataObject(d, null);
        return d;
    }

    // ~ Ticket 555
    // =========================================================================

    @Test(groups = "ticket:555")
    public void test_iadminAllowsUpdatingUsers() throws Exception {
        ServiceFactory root = new ServiceFactory(rootLogin);
        String newSysUser = updateNewUser(root);
        Login systemUser = new Login(newSysUser, "", "system", "Test");
        ServiceFactory sys = new ServiceFactory(systemUser);
        updateNewUser(sys);
    }

    protected String updateNewUser(ServiceFactory services) {
        String name = UUID.randomUUID().toString();
        Experimenter e = new Experimenter();
        e.setOmeName(name);
        e.setFirstName("ticket:555");
        e.setLastName("ticket:555");
        long id = services.getAdminService().createSystemUser(e);
        Experimenter test = services.getAdminService().lookupExperimenter(name);
        String email = "ticket@555";
        test.setEmail(email);
        services.getAdminService().updateExperimenter(test);
        test = services.getAdminService().lookupExperimenter(name);
        assertEquals(email, test.getEmail());
        return name;
    }

    // =========================================================================

    @Test(groups = "ticket:778")
    public void test_ProjAnnCollectionError() throws Exception {
        Project p = new Project();
        p.setName("ticket:778");
        p = sf.getPojosService().createDataObject(p, new PojoOptions().map());

        Project fromServer = sf.getQueryService().get(Project.class, p.getId());
        fromServer.setDescription("desc updated");
        fromServer.acceptFilter(new Filter() {
            public Filterable filter(String fieldId, Filterable f) {
                return f;
            }

            public Collection filter(String fieldId, Collection c) {
                return null;
            }

            public Map filter(String fieldId, Map m) {
                return m;
            }

            public Object filter(String fieldId, Object o) {
                return o;
            }
        });

        sf.getPojosService().updateDataObject(fromServer,
                new PojoOptions().map());

    }

    // ~ Helpers
    // =========================================================================

    private OriginalFile makeFile() {
        OriginalFile of = new OriginalFile();
        of.setSha1("ticket:509");
        of.setSize(0L);
        of.setName("ticket:509");
        of.setPath("/dev/null");
        of.setFormat(new Format(1L, false));
        of = sf.getUpdateService().saveAndReturnObject(of);
        return of;
    }

}