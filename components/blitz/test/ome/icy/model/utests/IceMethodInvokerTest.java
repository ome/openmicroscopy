/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.icy.model.utests;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import ome.api.IAdmin;
import ome.api.IConfig;
import ome.api.IPojos;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.api.RawPixelsStore;
import ome.api.ServiceInterface;
import ome.conditions.SecurityViolation;
import ome.model.enums.Family;
import ome.services.icy.util.IceMethodInvoker;
import ome.services.icy.util.ServantHelper;
import ome.system.EventContext;
import ome.system.Roles;
import ome.util.builders.PojoOptions;
import omeis.providers.re.RGBBuffer;
import omeis.providers.re.RenderingEngine;
import omero.RString;
import omero.model.Experimenter;
import omero.model.ExperimenterI;
import omero.model.Image;
import omero.model.ImageI;
import omero.romio.XY;
import omero.util.IceMapper;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.builder.ArgumentsMatchBuilder;
import org.jmock.core.Constraint;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IceMethodInvokerTest extends MockObjectTestCase {

    Class<? extends ServiceInterface> c = null;

    ServiceInterface srv = null;

    Ice.Current curr = null;

    Mock mock = null;

    IceMethodInvoker invoker = null;

    IceMapper mapper = null;

    @BeforeMethod
    public void setUp() throws Exception {
        curr = new Ice.Current();
    }
    
    protected Object invoke(Object...args) throws omero.ServerError {
        Object rv = invoker.invoke(srv, curr, mapper, args);
        return rv;
    }

    public void init(Class<? extends ServiceInterface> c, String op) {
        this.c = c;
        mock = mock(this.c);
        srv = (ServiceInterface) mock.proxy();
        curr.operation = op;
        invoker = new IceMethodInvoker(this.c);
        mapper = new IceMapper();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testObjectCtorCanThrowNPE() throws Exception {
        new IceMethodInvoker((ServiceInterface) null);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testClassCtorCanThrowNPE() throws Exception {
        new IceMethodInvoker((Class<ServiceInterface>) null);
    }

    @Test
    public void testClassCtorStoresTheMethodsAndOtherInfo() throws Exception {
        c = IAdmin.class;
        IceMethodInvoker imi = new IceMethodInvoker(c);
        assertNotNull(imi.getMethod("changePassword"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvokeChecksNumberOfArguments() throws Exception {
        c = IAdmin.class;
        curr.operation = "changePassword";
        IceMethodInvoker imi = new IceMethodInvoker(c);
        imi.invoke(null, curr, new IceMapper());
    }

    @Test
    public void testInvokeMatchesStringArguments() throws Exception {

        init(IAdmin.class, "changePassword");
        method();
        
        Object rv = invoke("foo");
        ServantHelper.throwIfNecessary(rv);

    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvokeThrowsIllegalArgumentExceptionOnUnknownMethod()
            throws Exception {
        c = IAdmin.class;

        Mock mockA = mock(c);
        IAdmin prx = (IAdmin) mockA.proxy();

        curr.operation = "XXXXXXXXXXXXXXXXXXXXXXXX";
        IceMethodInvoker imi = new IceMethodInvoker(c);
        imi.invoke(prx, curr, new IceMapper());

    }

    @Test
    public void testInvokeInvokesOnProxy() throws Exception {
        c = IAdmin.class;

        Mock mockA = mock(c);
        mockA.expects(once()).method("changePassword");
        IAdmin prx = (IAdmin) mockA.proxy();

        curr.operation = "changePassword";
        IceMethodInvoker imi = new IceMethodInvoker(c);
        imi.invoke(prx, curr, new IceMapper(), "foo");

    }

    @Test
    public void testInvokeHandlesExceptionMapping() throws Exception {
        c = IAdmin.class;

        SecurityViolation sv = new SecurityViolation("foo");
        Experimenter exp = new ExperimenterI();

        Mock mockA = mock(c);
        mockA.expects(once()).method("createUser").will(throwException(sv));
        IAdmin prx = (IAdmin) mockA.proxy();

        curr.operation = "createUser";
        IceMethodInvoker imi = new IceMethodInvoker(c);
        imi.invoke(prx, curr, new IceMapper(), exp, "default");

    }

    @Test
    public void testInvokeProperlyMapsIObject() throws Exception {

        Image i = new ImageI();
        i.name = new RString(false, "foo");

        init(IUpdate.class, "saveObject");
        mock.expects(once()).method(curr.operation).with(new Constraint() {

            public boolean eval(Object o) {
                if (o instanceof ome.model.core.Image) {
                    ome.model.core.Image test = (ome.model.core.Image) o;
                    return "foo".equals(test.getName());
                }
                return false;
            }

            public StringBuffer describeTo(StringBuffer buffer) {
                return buffer.append("name equal foo");
            }

        });
        Object rv = invoker.invoke(srv, curr, mapper, i);

        ServantHelper.throwIfNecessary(rv);
        assertNull(rv);
    }

    // Admin


    @Test
    public void testAdminWorks() throws Exception {

        IAdmin a;

        init(IAdmin.class, "getSecurityRoles");
        method().will(returnValue(new Roles()));
        
        Object rv = invoke();
        ServantHelper.throwIfNecessary(rv);
        assertNotNull(rv);
        assertEquals(new Roles().getRootName(),((omero.sys.Roles)rv).rootName);

        init(IAdmin.class, "getEventContext");
        method().will(returnValue(new EventContext(){

            public Long getCurrentEventId() { return 1L; }
            public String getCurrentEventType() { return "type"; }
            public Long getCurrentGroupId() { return 2L; }
            public String getCurrentGroupName() { return "group"; }
            public Long getCurrentUserId() { return 3L; }
            public String getCurrentUserName() { return "user"; }
            public List<Long> getLeaderOfGroupsList() { return Arrays.asList(4L); }
            public List<Long> getMemberOfGroupsList() { return Arrays.asList(5L); }
            public boolean isCurrentUserAdmin() { return true; }
            public boolean isReadOnly() { return false; }
            }));
        
        rv = invoke();
        ServantHelper.throwIfNecessary(rv);
        omero.sys.EventContext ec = (omero.sys.EventContext) rv;
        assertTrue(ec.eventType.equals("type"));
        assertTrue(ec.groupName.equals("group"));
        assertTrue(ec.userName.equals("user"));
    }
    
    // Query

    @Test
    public void testQueryWorks() throws Exception {

        IQuery q;

        init(IQuery.class, "findAllByQuery");
        method().will(returnValue(Arrays.asList(new ome.model.core.Image())));
        
        Object rv = invoke("my query", new omero.sys.Parameters());
        ServantHelper.throwIfNecessary(rv);
        assertNotNull(rv);
        
        init(IQuery.class, "findAll");
        method().will(returnValue(Arrays.asList(new ome.model.meta.Experimenter())));
        rv = invoke("Experimenter",new omero.sys.Filter());
        ServantHelper.throwIfNecessary(rv);
        List l = (List) rv;
        rv = l.get(0);
        assertTrue(rv.getClass().getName(),
                rv instanceof omero.model.Experimenter);
    }
    
    // RawFileStore
    
    @Test
    public void testFileStoreWorks() throws Exception {

        RawFileStore fs;

        init(RawFileStore.class, "write");
        method();
        
        Object rv = invoke(new byte[]{1,2,3}, 1L, 1);
        ServantHelper.throwIfNecessary(rv);
        assertNull(rv);

    }
        
    // RawPixelsStore

    @Test
    public void testPixelsStoreWorks() throws Exception {

        RawPixelsStore ps;
        
        init(RawPixelsStore.class, "getRegion");
        method().will( returnValue(new byte[]{1,2,3}) );
        
        Object rv = invoke(1, 1L);
        ServantHelper.throwIfNecessary(rv);
        assertTrue(1 == ((byte[])rv)[0]);

    }
    
    // RenderingEngine

    @Test
    public void testRenderingEngineWorks() throws Exception {

        RenderingEngine re;

        Family m1 = new Family();
        Family m2 = new Family();
        List l = Arrays.asList(m1, m2);

        init(RenderingEngine.class, "getAvailableFamilies");
        mock.expects(once()).method(curr.operation).will(returnValue(l));
        Object rv = invoke();
        ServantHelper.throwIfNecessary(rv);
        assertTrue(((List)rv).size()==2);
        
        init(RenderingEngine.class, "getChannelFamily");
        mock.expects(once()).method(curr.operation).will(returnValue(m1));
        rv = invoke(1);
        ServantHelper.throwIfNecessary(rv);
        assertTrue(rv instanceof omero.model.Family);
    
        init(RenderingEngine.class, "setActive");
        method();
        rv = invoke(1,true);
        ServantHelper.throwIfNecessary(rv);
        
        RGBBuffer buffer = new RGBBuffer(10,10);
        omero.romio.PlaneDef def = new omero.romio.PlaneDef();
        def.slice = XY.value;
        def.t = 0;
        def.z = 1;
        
        init(RenderingEngine.class, "render");
        method().will(returnValue(buffer));
        rv = invoke(def);
        ServantHelper.throwIfNecessary(rv);
        
        init(RenderingEngine.class, "renderAsPackedInt");
        method().will(returnValue(new int[]{1,2,3,4}));
        rv = invoke(def);
        ServantHelper.throwIfNecessary(rv);
        
    }
    
    // Config
    
    @Test
    public void testConfigWorks() throws Exception {

        IConfig cfg;

        init(IConfig.class, "getServerTime");
        method().will(returnValue(new Timestamp(System.currentTimeMillis())));
        Object rv = invoke();
        ServantHelper.throwIfNecessary(rv);
        assertNotNull(rv);
    
    }
    
    // Pojos
    
    @Test
    public void testPojosWorks() throws Exception {

        IPojos p;

        init(IPojos.class, "getUserDetails");
        method().will(returnValue(new HashMap()));
        Object rv = invoke(new HashSet(Arrays.asList("u1","u2")), new PojoOptions().map());
        ServantHelper.throwIfNecessary(rv);
        Map map = (Map)rv;
        assertNotNull(map);
    
    }
    
    
    // what happens if sth returns a prx
    // what happens

    // Invokers could use inheritance for special things (PlaneDef, e.g.)
    // method with: int, double, float, boolean, arrays, lists, etc.
    // input values:
    // long, int, double, Long, Integer, String, RString,
    // arrags

    // ~ Helpers
    // =========================================================================
    
    protected ArgumentsMatchBuilder method() {
        return mock.expects(once()).method(curr.operation);
    }
    
}
