/*
 *   Copyright 2008 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.delete;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import ome.api.IDelete;
import ome.api.local.LocalAdmin;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.core.PlaneInfo;
import ome.model.display.RenderingDef;
import ome.model.display.Thumbnail;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.security.AdminAction;
import ome.security.SecuritySystem;
import ome.system.EventContext;
import ome.system.Roles;
import ome.testing.ObjectFactory;
import ome.tools.hibernate.HibernateUtils;
import ome.tools.hibernate.SessionFactory;
import ome.util.CBlock;

import org.hibernate.Query;
import org.hibernate.Session;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.core.Constraint;
import org.jmock.core.Invocation;
import org.jmock.core.Stub;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test
public class DeleteUnitTest extends MockObjectTestCase {

    DeleteBean bean;
    IDelete service;

    Image i;

    Mock am, qm, um, ecm, sm, hm, xm;
    LocalAdmin a;
    LocalQuery q;
    LocalUpdate u;
    EventContext ec;
    SecuritySystem s;
    Session hibernate;
    Query query;

    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();
        am = mock(LocalAdmin.class);
        a = (LocalAdmin) am.proxy();

        hm = mock(Session.class);
        hibernate = (Session) hm.proxy();

        xm = mock(Query.class);
        query = (Query) xm.proxy();
        hm.expects(atLeastOnce()).method("createQuery").will(returnValue(query));
        xm.expects(atLeastOnce()).method("executeUpdate").will(returnValue(0));

        Mock sfMock = mock(org.hibernate.SessionFactory.class);
        sfMock.expects(once()).method("getAllClassMetadata").will(
                returnValue(new HashMap<String, Object>()));
        org.hibernate.SessionFactory sf = (org.hibernate.SessionFactory) sfMock.proxy();

        bean = new DeleteBean(a, new SessionFactory(sf, null){
            @Override
            public Session getSession() {
                return hibernate;
            }
        });
        service = bean;

        qm = mock(LocalQuery.class);
        q = (LocalQuery) qm.proxy();
        bean.setQueryService(q);

        um = mock(LocalUpdate.class);
        u = (LocalUpdate) um.proxy();
        bean.setUpdateService(u);

        sm = mock(SecuritySystem.class);
        s = (SecuritySystem) sm.proxy();
        sm.expects(atLeastOnce()).method("getSecurityRoles").will(
                returnValue(new Roles()));
        bean.setSecuritySystem(s);

        ecm = mock(EventContext.class);
        ec = (EventContext) ecm.proxy();

    }

    @Test(expectedExceptions = SecurityViolation.class)
    public void testImageNotOwnedByUser() throws Exception {

        setReturnedImage(1L, 1L);
        setCurrentEventContext("user", 100L, false);
        setConstrainingObjects(Arrays.<Dataset> asList(), false);
        bean.deleteImage(4, true);

    }

    @Test
    public void testImageNotOwnedByPI() throws Exception {

        setReturnedImage(1L, 1L);
        setCurrentEventContext("pi", 50L, false, 1L);
        setConstrainingObjects(Arrays.<Dataset> asList(), false);
        setDeleteObjects(i);
        bean.deleteImage(4, true);
    }

    @Test
    public void testImageNotOwnedByRoot() throws Exception {

        setReturnedImage(1L, 1L);
        setCurrentEventContext("root", 0L, true, 0L);
        setConstrainingObjects(Arrays.<Dataset> asList(), false);
        setDeleteObjects(i);
        bean.deleteImage(4, true);
    }

    /*
     * Annotations are currently not restrictive
     * 
     * @Test(expectedExceptions = ApiUsageException.class) public void
     * testImageHasAnnotations() throws Exception {
     * 
     * setReturnedImage(100L, 100L); setCurrentEventContext("user", 100L);
     * setConstrainingObjects(Arrays.<Dataset> asList(), false);
     * bean.deleteImage(4, false); }
     */

    @Test(expectedExceptions = ApiUsageException.class)
    public void testImageInOtherDatasets() throws Exception {

        setReturnedImage(100L, 100L);
        setCurrentEventContext("user", 100L, false);
        setConstrainingObjects(Arrays.<Dataset> asList(new Dataset()), false);
        bean.deleteImage(4, false);
    }

    @Test(expectedExceptions = ApiUsageException.class)
    public void testImageInOwnDatasetsNoForce() throws Exception {

        setReturnedImage(100L, 100L);
        setCurrentEventContext("user", 100L, false);
        setConstrainingObjects(Arrays.<Dataset> asList(new Dataset()), false);
        bean.deleteImage(4, false);
    }

    @Test
    public void testImageInOwnDatasetsForce() throws Exception {

        setReturnedImage(100L, 100L);
        setCurrentEventContext("user", 100L, false);
        setConstrainingObjects(Arrays.<Dataset> asList(), true);
        setDeleteObjects(i);
        bean.deleteImage(4, true);
    }

    @Test
    public void testAllMetadataIsGone() throws Exception {
        // Setup graph
        Pixels p = ObjectFactory.createPixelGraph(null);
        i = new Image();
        i.addPixels(p);

        // Locate deletes
        List<IObject> deletes = new ArrayList<IObject>();
        deletes.add(i);
        deletes.add(p);
        deletes.addAll(p.linkedOriginalFileList());
        deletes.addAll(p.collectPlaneInfo((CBlock<PlaneInfo>) null));
        deletes.addAll(p.collectSettings((CBlock<RenderingDef>) null));
        deletes.addAll(p.collectThumbnails((CBlock<Thumbnail>) null));

    }

    private void setDeleteObjects(IObject... objects) {
        qm.expects(once()).method("execute"); // Clearing session
        for (final IObject o : objects) {
            um.expects(once()).method("deleteObject").with(new Constraint() {

                public boolean eval(Object arg0) {
                    if (arg0 instanceof IObject) {
                        IObject obj = (IObject) arg0;
                        return HibernateUtils.idEqual(o, obj);
                    }
                    return false;
                }

                public StringBuffer describeTo(StringBuffer arg0) {
                    return arg0.append("has id " + o.getId());
                }
            });
        }
    }

    private void setReturnedImage(long owner, long group) {
        i = new Image(-1L, true);
        i.getDetails().setOwner(new Experimenter(owner, false));
        i.getDetails().setGroup(new ExperimenterGroup(group, false));
        // The findByQuery is now run within an AdminAction
        hm.expects(atLeastOnce()).method("clear");
        qm.expects(atLeastOnce()).method("get").will(returnValue(i));
        xm.expects(atLeastOnce()).method("setParameter");
        sm.expects(atLeastOnce()).method("runAsAdmin").will(new Stub() {

            public Object invoke(Invocation arg0) throws Throwable {
                AdminAction aa = (AdminAction) arg0.parameterValues.get(0);
                aa.runAsAdmin();
                return null;
            }

            public StringBuffer describeTo(StringBuffer arg0) {
                return arg0.append(" calls runAsAdmin");
            }
        });
        // delete settings & channels
        qm.expects(atLeastOnce()).method("projection").will(returnValue(new ArrayList()));
    }

    private void setCurrentEventContext(String name, long user, boolean admin,
            Long... leaderof) {
        sm.expects(atLeastOnce()).method("getEventContext").will(returnValue(ec));
        ecm.expects(atLeastOnce()).method("getCurrentUserName").will(
                returnValue(name));
        ecm.expects(atLeastOnce()).method("getCurrentUserId").will(
                returnValue(user));
        ecm.expects(atLeastOnce()).method("isCurrentUserAdmin").will(
                returnValue(admin));
        ecm.expects(atLeastOnce()).method("getLeaderOfGroupsList").will(
                returnValue(Arrays.asList(leaderof)));
    }

    private void setConstrainingObjects(final List<Dataset> datasets,
            boolean force) {
        sm.expects(once()).method("runAsAdmin").will(new Stub() {

            public Object invoke(Invocation arg0) throws Throwable {
                QueryConstraints qc = (QueryConstraints) arg0.parameterValues
                        .get(0);
                qc.getResults().addAll(datasets);
                return null;
            }

            public StringBuffer describeTo(StringBuffer arg0) {
                return arg0.append("fill result list");
            }

        });
        // force is currently not used but determines which query will be passed
        // in
    }
}
