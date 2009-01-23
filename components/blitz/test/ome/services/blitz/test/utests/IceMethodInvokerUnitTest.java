/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.test.utests;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.api.IAdmin;
import ome.api.IConfig;
import ome.api.IPojos;
import ome.api.IQuery;
import ome.api.ISession;
import ome.api.IShare;
import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.api.RawPixelsStore;
import ome.api.Search;
import ome.api.ServiceInterface;
import ome.api.ThumbnailStore;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.enums.Family;
import ome.model.internal.Permissions;
import ome.model.meta.ExperimenterGroup;
import ome.parameters.QueryParameter;
import ome.services.blitz.util.ConvertToBlitzExceptionMessage;
import ome.services.blitz.util.IceMethodInvoker;
import ome.services.messages.GlobalMulticaster;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.system.Roles;
import ome.util.builders.PojoOptions;
import omeis.providers.re.RGBBuffer;
import omeis.providers.re.RenderingEngine;
import omero.RString;
import omero.RType;
import omero.constants.POJOEXPERIMENTER;
import omero.constants.POJOLEAVES;
import omero.model.Experimenter;
import omero.model.ExperimenterI;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.TagAnnotationI;
import omero.romio.XY;
import omero.sys.Filter;
import omero.util.IceMap;
import omero.util.IceMapper;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.builder.ArgumentsMatchBuilder;
import org.jmock.core.Constraint;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

import static omero.rtypes.*;

@Test
public class IceMethodInvokerUnitTest extends MockObjectTestCase {

    IceMethodInvoker invoker;
    Mock tbMock;
    Destroyable tb;
    IceMapper mapper;
    Ice.Current current;
    OmeroContext ctx;
    GlobalMulticaster multicaster;

    @Override
    @Configuration(beforeTestMethod = true)
    protected void setUp() throws Exception {
        tb = new Destroyable();
        ctx = new OmeroContext("classpath:ome/services/messaging.xml");
        multicaster = (GlobalMulticaster) ctx
                .getBean("applicationEventMulticaster");
        invoker = new IceMethodInvoker(ThumbnailStore.class, ctx);
        mapper = new IceMapper();
        current = new Ice.Current();
        current.operation = "close";
        current.id = Ice.Util.stringToIdentity("test");
    }

    @Test(groups = "ticket:880")
    void testDetailsAreMappedToOmero() throws Exception {
        ome.model.core.Image i = new ome.model.core.Image();
        assertNotNull(i.getDetails());
        Object o = mapper.handleOutput(ome.model.core.Image.class, i);
        Image rv = (Image) o;
        assertNotNull(rv.getDetails());
    }

    @Test(groups = "ticket:880")
    void testDetailsAreMappedFromOmero() throws Exception {
        Image i = new ImageI();
        Object o = mapper.handleInput(ome.model.core.Image.class, i);
        ome.model.core.Image rv = (ome.model.core.Image) o;
        assertNotNull(rv.getDetails());
    }

    /**
     * See the note in {#link {@link IceMethodInvoker#callorClose(...)} for info
     * on why tb.closed == 0.
     */
    @Test
    void testAllCallsOnCloseAlsoCallDestroy() throws Exception {
        invoker.invoke(tb, current, mapper);
        assertTrue(tb.toString(), tb.closed == 1);
    }

    public static class Destroyable implements ThumbnailStore {

        @Override
        public String toString() {
            return String
		.format("%d closes", closed);
        }

        int closed = 0;

	public void activate() {
	}

	public void passivate() {
	}

        public void close() {
            closed++;
        }

        public void createThumbnail(Integer sizeX, Integer sizeY) {
        }

        public void createThumbnails() {
        }

        public byte[] getThumbnail(Integer sizeX, Integer sizeY) {
            return null;
        }

        public byte[] getThumbnailByLongestSide(Integer size) {
            return null;
        }

        public byte[] getThumbnailByLongestSideDirect(Integer size) {
            return null;
        }

        public byte[] getThumbnailDirect(Integer sizeX, Integer sizeY) {
            return null;
        }

        public Map<Long, byte[]> getThumbnailSet(Integer sizeX, Integer sizeY,
                Set<Long> pixelsIds) {
            return null;
        }

        public Map<Long, byte[]> getThumbnailByLongestSideSet(Integer size,
                Set<Long> pixelsIds) {
            return null;
        }

        public void resetDefaults() {
        }

        public boolean setPixelsId(long pixelsId) {
            return false;
        }

        public void setRenderingDefId(Long renderingDefId) {
        }

        public boolean thumbnailExists(Integer sizeX, Integer sizeY) {
            return false;
        }

        public EventContext getCurrentEventContext() {
            return null;
        }

        public byte[] getThumbnailForSectionByLongestSideDirect(int theZ,
                int theT, Integer size) {
            return null;
        }

        public byte[] getThumbnailForSectionDirect(int theZ, int theT,
                Integer sizeX, Integer sizeY) {
            return null;
        }

        public void setRenderingDefId(long renderingDefId) {
        }

    }

    // 
    // Copying ome.services.blitz.test.utests.IceMethodInvokerTest
    //

    Class<? extends ServiceInterface> c = null;
    ServiceInterface srv = null;
    Mock mock;

    protected Object invoke(Object... args) throws Ice.UserException {
        Object rv = invoker.invoke(srv, current, mapper, args);
        return rv;
    }

    protected void init(Class<? extends ServiceInterface> c, String op) {
        this.c = c;
        mock = mock(this.c);
        srv = (ServiceInterface) mock.proxy();
        current.operation = op;
        current.id = Ice.Util.stringToIdentity("test");
        invoker = new IceMethodInvoker(this.c, ctx);
        mapper = new IceMapper();
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testObjectCtorCanThrowNPE() throws Exception {
        new IceMethodInvoker((ServiceInterface) null, ctx);
    }

    @Test(expectedExceptions = NullPointerException.class)
    public void testClassCtorCanThrowNPE() throws Exception {
        new IceMethodInvoker((Class<ServiceInterface>) null, ctx);
    }

    @Test
    public void testClassCtorStoresTheMethodsAndOtherInfo() throws Exception {
        c = IAdmin.class;
        IceMethodInvoker imi = new IceMethodInvoker(c, ctx);
        assertNotNull(imi.getMethod("changePassword"));
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvokeChecksNumberOfArguments() throws Exception {
        c = IAdmin.class;
        current.operation = "changePassword";
        IceMethodInvoker imi = new IceMethodInvoker(c, ctx);
        imi.invoke(null, current, new IceMapper());
    }

    @Test
    public void testInvokeMatchesStringArguments() throws Exception {

        init(IAdmin.class, "changePassword");
        method();

        Object rv = invoke("foo");

    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testInvokeThrowsIllegalArgumentExceptionOnUnknownMethod()
            throws Exception {
        c = IAdmin.class;

        Mock mockA = mock(c);
        IAdmin prx = (IAdmin) mockA.proxy();

        current.operation = "XXXXXXXXXXXXXXXXXXXXXXXX";
        IceMethodInvoker imi = new IceMethodInvoker(c, ctx);
        imi.invoke(prx, current, new IceMapper(), (java.lang.Object[])null);

    }

    @Test
    public void testInvokeInvokesOnProxy() throws Exception {
        c = IAdmin.class;

        Mock mockA = mock(c);
        mockA.expects(once()).method("changePassword");
        IAdmin prx = (IAdmin) mockA.proxy();

        current.operation = "changePassword";
        IceMethodInvoker imi = new IceMethodInvoker(c, ctx);
        imi.invoke(prx, current, new IceMapper(), "foo");

    }

    @Test(expectedExceptions = omero.SecurityViolation.class)
    public void testInvokeHandlesExceptionMapping() throws Exception {
        c = IAdmin.class;

        SecurityViolation sv = new SecurityViolation("foo");
        Experimenter exp = new ExperimenterI();

        Mock mockA = mock(c);
        mockA.expects(once()).method("createUser").will(throwException(sv));
        IAdmin prx = (IAdmin) mockA.proxy();

        current.operation = "createUser";
        IceMethodInvoker imi = new IceMethodInvoker(c, ctx);
        imi.invoke(prx, current, new IceMapper(), exp, "default");

    }

    @Test
    public void testInvokeProperlyMapsIObject() throws Exception {

        Image i = new ImageI();
        i.setName(rstring("foo"));

        init(IUpdate.class, "saveObject");
        mock.expects(once()).method(current.operation).with(new Constraint() {

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
        Object rv = invoker.invoke(srv, current, mapper, i);
        assertNull(rv);
    }

    // Session

    @Test
    public void testSessionsWorks() throws Exception {

        ISession s;

        init(ISession.class, "getInput");
        method().will(returnValue(rint(1)));
        Object rv = invoke("a", "a");
        assertNotNull(rv);

        init(ISession.class, "setInput");
        method();
        rv = invoke("a", "a", rint(2));

        init(ISession.class, "getOutput");
        method().will(returnValue(new omero.Point()));
        rv = invoke("a", "a");

        init(ISession.class, "setOutput");
        method();
        rv = invoke("a", "a", rinternal(new omero.grid.JobParams()));

    }

    @Test(groups = "ticket:1036")
    public void testSessionsGetInputString() throws Exception {

        ISession s;

        String sess = new String("sess");
        String key = new String("key");
        RString value = rstring("value");

        init(ISession.class, "setInput");
        method();
        Object rv = invoke(sess, key, value);

        init(ISession.class, "getInput");
        method().will(returnValue("value"));
        rv = invoke(sess, key);
        assertNotNull(rv);
        // This is how the invoker previously broke. Should be true.
        assertFalse(rv instanceof RString);

        // Now using the #mapReturnValue method
        init(ISession.class, "getInput");
        method().will(returnValue("value"));
        // Convert string to rstring
        mapper = new IceMapper(IceMapper.STRING_TO_RSTRING);
        rv = invoke(sess, key);
        assertNotNull(rv);
        assertTrue(rv instanceof RString);

    }

    @Test
    public void testSessionEnvironment() throws Exception {
        // Used by InteractiveProcessorI

        Map m = new HashMap();
        m.put("string", new ome.model.core.Image());

        mapper.toRType(1);
        mapper.toRType(rint(1));
        mapper.toRType(m);
        mapper.toRType(new ome.model.core.Image());
        mapper.toRType(Arrays.asList(m));
    }

    // Share

    @Test
    public void testShareWorks() throws Exception {

        IShare s;

        init(IShare.class, "createShare");
        method().will(returnValue(1L));
        Object rv = invoke("d", null, null, null, null, true);
        assertNotNull(rv);
    }

    // Admin

    @Test
    public void testAdminWorks() throws Exception {

        IAdmin a;

        init(IAdmin.class, "getSecurityRoles");
        method().will(returnValue(new Roles()));

        Object rv = invoke();
        assertNotNull(rv);
        assertEquals(new Roles().getRootName(), ((omero.sys.Roles) rv).rootName);

        init(IAdmin.class, "getEventContext");
        method().will(returnValue(new EventContext() {

            public Long getCurrentShareId() {
                return null;
            }

            public Long getCurrentSessionId() {
                return 1L;
            }

            public String getCurrentSessionUuid() {
                return "uuid";
            }

            public Long getCurrentEventId() {
                return 1L;
            }

            public String getCurrentEventType() {
                return "type";
            }

            public Long getCurrentGroupId() {
                return 2L;
            }

            public String getCurrentGroupName() {
                return "group";
            }

            public Long getCurrentUserId() {
                return 3L;
            }

            public String getCurrentUserName() {
                return "user";
            }

            public List<Long> getLeaderOfGroupsList() {
                return Arrays.asList(4L);
            }

            public List<Long> getMemberOfGroupsList() {
                return Arrays.asList(5L);
            }

            public boolean isCurrentUserAdmin() {
                return true;
            }

            public boolean isReadOnly() {
                return false;
            }

            public Permissions getCurrentUmask() {
                return null;
            }
        }));

        rv = invoke();
        omero.sys.EventContext ec = (omero.sys.EventContext) rv;
        assertTrue(ec.eventType.equals("type"));
        assertTrue(ec.groupName.equals("group"));
        assertTrue(ec.userName.equals("user"));
    }

    @Test
    public void testAdminCanReturnArrays() throws Exception {

        IAdmin a;

        init(IAdmin.class, "containedGroups");
        method()
                .will(
                        returnValue(new ExperimenterGroup[] { new ExperimenterGroup() }));

        Object rv = invoke(1L);
        assertNotNull(rv);
        assertTrue("is list", List.class.isAssignableFrom(rv.getClass()));
        List l = (List) rv;
        assertTrue("with group", omero.model.ExperimenterGroup.class
                .isAssignableFrom(l.get(0).getClass()));

    }

    @Test
    public void testAdminNullAndEmptyArraysShouldWorkToo() throws Exception {

        IAdmin a;
        init(IAdmin.class, "containedGroups");

        method().will(returnValue(null));
        Object rv = invoke(1L);
        assertTrue("is null", rv == null);

        method().will(returnValue(new ExperimenterGroup[] {}));
        rv = invoke(1L);
        List l = (List) rv;
        assertTrue("is empty", l.size() == 0);

    }

    @Test
    public void testAdminUnlockCanHandleVarargs() throws Exception {

        IAdmin a;
        init(IAdmin.class, "unlock");

        method().will(returnValue(null));
        Object rv = invoke(new Object[] { null });
        assertTrue("is null", rv == null);

        method().will(returnValue(new boolean[] {}));
        rv = invoke(Collections.EMPTY_LIST);
        boolean[] l = (boolean[]) rv;
        assertTrue("is empty", l.length == 0);

        method().will(returnValue(new boolean[] { true, false }));
        rv = invoke(Arrays.asList(new omero.model.ImageI(),
                new omero.model.ImageI()));
        l = (boolean[]) rv;
        assertTrue("is 2", l.length == 2);

    }

    // Query

    @Test
    public void testQueryWorks() throws Exception {

        IQuery q;

        init(IQuery.class, "findAllByQuery");
        method().will(returnValue(Arrays.asList(new ome.model.core.Image())));

        Object rv = invoke("my query", new omero.sys.Parameters());
        assertNotNull(rv);

        init(IQuery.class, "findAll");
        method().will(
                returnValue(Arrays.asList(new ome.model.meta.Experimenter())));
        rv = invoke("Experimenter", new omero.sys.Filter());
        List l = (List) rv;
        rv = l.get(0);
        assertTrue(rv.getClass().getName(),
                rv instanceof omero.model.Experimenter);
    }

    @Test
    public void testQueryReceivesProperQueryParameters() throws Exception {
        IQuery q;

        init(IQuery.class, "findAllByQuery");
        method().with(eq("my query"), new Constraint() {
            public StringBuffer describeTo(StringBuffer buffer) {
                buffer.append(" propery quer parameters ");
                return buffer;
            }

            public boolean eval(Object o) {
                if (o instanceof ome.parameters.Parameters) {
                    ome.parameters.Parameters p = (ome.parameters.Parameters) o;
                    if (!Long.valueOf(p.getFilter().owner()).equals(2L)) {
                        return false;
                    }
                    QueryParameter qp;
                    qp = p.get("S");
                    if (!qp.name.equals("S") || !qp.value.equals("S")
                            || !qp.type.equals(String.class)) {
                        return false;
                    }
                    qp = p.get("Time");
                    if (!qp.name.equals("Time")
                            || !Long.valueOf(((Timestamp) qp.value).getTime())
                                    .equals(10L)) {
                        return false;
                    }
                    return true;

                }
                return false;
            }
        }).will(returnValue(Arrays.asList(new ome.model.core.Image())));
        omero.sys.Parameters p = new omero.sys.Parameters();
        p.theFilter = new Filter();
        p.theFilter.ownerId = rlong(2L);
        p.map = new HashMap<String, RType>();
        p.map.put("S", rstring("S"));
        p.map.put("List", rlist(rlong(1L), rlong(2L)));
        p.map.put("Time", rtime(10L));
        // p.map.put("Array",new JArray(new JString("A")));
        // FIXME: Not supported. Array class needed for query parameters.
        Object rv = invoke("my query", p);
        assertNotNull(rv);
    }

    // Update

    @Test
    public void testUpdateHanlesArraysProperly() throws Exception {

        init(IUpdate.class, "saveArray");
        method(); // void

        Object rv = invoke(Arrays.asList(new omero.model.ImageI()));

        init(IUpdate.class, "saveAndReturnArray");
        method()
                .will(
                        returnValue(new ome.model.core.Image[] { new ome.model.core.Image() }));
        rv = invoke(Arrays.asList(new omero.model.ImageI()));
        List l = (List) rv;
        assertTrue(l.size() == 1);
    }

    // RawFileStore

    @Test
    public void testFileStoreWorks() throws Exception {

        RawFileStore fs;

        init(RawFileStore.class, "write");
        method();

        Object rv = invoke(new byte[] { 1, 2, 3 }, 1L, 1);
        assertNull(rv);

    }

    // RawPixelsStore

    @Test
    public void testPixelsStoreWorks() throws Exception {

        RawPixelsStore ps;

        init(RawPixelsStore.class, "getRegion");
        method().will(returnValue(new byte[] { 1, 2, 3 }));

        Object rv = invoke(1, 1L);
        assertTrue(1 == ((byte[]) rv)[0]);

        init(RawPixelsStore.class, "getTimepointSize");
        method().will(returnValue(Integer.valueOf(1)));

        rv = invoke();
        assertTrue(1 == (Integer) rv);

    }

    // RenderingEngine

    @Test
    public void testRenderingEngineWorks() throws Exception {

        RenderingEngine re;

        Family m1 = new Family();
        Family m2 = new Family();
        List l = Arrays.asList(m1, m2);

        init(RenderingEngine.class, "getAvailableFamilies");
        mock.expects(once()).method(current.operation).will(returnValue(l));
        Object rv = invoke();
        assertTrue(((List) rv).size() == 2);

        init(RenderingEngine.class, "getChannelFamily");
        mock.expects(once()).method(current.operation).will(returnValue(m1));
        rv = invoke(1);
        assertTrue(rv instanceof omero.model.Family);

        init(RenderingEngine.class, "setActive");
        method();
        rv = invoke(1, true);

        RGBBuffer buffer = new RGBBuffer(10, 10);
        omero.romio.PlaneDef def = new omero.romio.PlaneDef();
        def.slice = XY.value;
        def.t = 0;
        def.z = 1;

        init(RenderingEngine.class, "render");
        method().will(returnValue(buffer));
        rv = invoke(def);

        init(RenderingEngine.class, "renderAsPackedInt");
        method().will(returnValue(new int[] { 1, 2, 3, 4 }));
        rv = invoke(def);

    }

    // ThumbnailStore

    @Test
    public void testGetThumbnailSet() throws Exception {

        ThumbnailStore ts;

        assertNotNull(IceMap.OMEtoOMERO.get(Family.Details.class));

        byte[] b = new byte[] { 1, 2, 3 };
        Set<byte[]> set = new HashSet<byte[]>();
        set.add(b);
        assertTrue("Arrays don't work this way", set.contains(b));

        Map<Long, byte[]> map = new HashMap<Long, byte[]>();
        map.put(1L, new byte[] { 1, 2, 3 });

        init(ThumbnailStore.class, "getThumbnailSet");
        method().will(returnValue(map));

        Object rv = invoke(32, 32, Collections.singleton(1L));
        byte[] results = ((Map<Long, byte[]>) rv).get(1L);
        assertNotNull(results);
        assertTrue(Arrays.equals(b, results));

    }

    // Config

    @Test
    public void testConfigWorks() throws Exception {

        IConfig cfg;

        init(IConfig.class, "getServerTime");
        method().will(returnValue(new Timestamp(System.currentTimeMillis())));
        Object rv = invoke();
        assertNotNull(rv);

    }

    // Pojos

    @Test
    public void testPojosWorks() throws Exception {

        IPojos p;

        Map<String, RType> paramMap = new HashMap<String, RType>();
        paramMap.put("foo", rstring("bar"));

        init(IPojos.class, "getUserDetails");
        method().will(returnValue(new HashMap()));
        Object rv = invoke(Arrays.asList("u1", "u2"), paramMap);
        Map map = (Map) rv;
        assertNotNull(map);

        init(IPojos.class, "loadContainerHierarchy");
        method().will(returnValue(new HashSet()));
        rv = invoke("Project", Arrays.asList(1L), paramMap);

    }

    @Test
    public void testPojosCanFindAnnotations() throws Exception {
        IPojos p;

        Map<String, RType> paramMap = new HashMap<String, RType>();
        paramMap.put("foo", rbool(true));

        Map<Long, Set<? extends IObject>> retVal = new HashMap<Long, Set<? extends IObject>>();

        init(IPojos.class, "findAnnotations");
        method().will(returnValue(retVal));
        Object rv = invoke("Image", Arrays.asList(1L, 2L), Arrays
                .asList(1L, 2L), paramMap);
        Map map = (Map) rv;
        assertNotNull(map);

    }

    @Test
    public void testPojosReceivesProperPojoOptions() throws Exception {
        IPojos p;

        Map<String, RType> paramMap = new HashMap<String, RType>();
        paramMap.put(POJOLEAVES.value, rbool(true));
        paramMap.put(POJOEXPERIMENTER.value, rlong(1L));

        init(IPojos.class, "loadContainerHierarchy");
        method().with(ANYTHING, ANYTHING, new Constraint() {
            public StringBuffer describeTo(StringBuffer buffer) {
                buffer.append(" proper PojoOptions ");
                return buffer;
            }

            public boolean eval(Object o) {
                if (o instanceof Map) {
                    Map map = (Map) o;
                    PojoOptions po = new PojoOptions(map);
                    if (!po.isLeaves()) {
                        return false;
                    }
                    if (!po.isExperimenter()) {
                        return false;
                    }
                    if (!po.getExperimenter().equals(1L)) {
                        return false;
                    }
                }
                return true;
            }
        }).will(returnValue(new HashSet()));
        Object rv = invoke("Image", Arrays.asList(1L, 2L), paramMap);
    }

    @Test
    public void testPojosHandlesArraysProperly() throws Exception {

        init(IPojos.class, "createDataObjects");

        method().will(returnValue(null));
        Object rv = invoke(null, null);
        assertNull(rv);

        method().will(returnValue(new ome.model.core.Image[] {}));
        rv = invoke(Collections.EMPTY_LIST, null);
        assertTrue(((List) rv).size() == 0);

        method()
                .will(
                        returnValue(new ome.model.core.Image[] { new ome.model.core.Image() }));
        rv = invoke(Collections.EMPTY_LIST, null);
        assertTrue(((List) rv).size() == 1);

        init(IPojos.class, "updateDataObjects");

        method().will(returnValue(null));
        rv = invoke(null, null);
        assertNull(rv);

        method().will(returnValue(new ome.model.IObject[] {}));
        rv = invoke(Collections.EMPTY_LIST, null);
        assertTrue(((List) rv).size() == 0);

        method()
                .will(
                        returnValue(new ome.model.core.Image[] { new ome.model.core.Image() }));
        rv = invoke(Collections.EMPTY_LIST, null);
        assertTrue(((List) rv).size() == 1);

        init(IPojos.class, "link");

        method()
                .will(
                        returnValue(new ome.model.containers.ProjectDatasetLink[] { new ome.model.containers.ProjectDatasetLink() }));
        rv = invoke(Arrays.asList(new omero.model.ProjectDatasetLinkI()), null);
        assertTrue(((List) rv).size() == 1);

    }

    // what happens if sth returns a prx
    // what happens

    // Invokers could use inheritance for special things (PlaneDef, e.g.)
    // method with: int, double, float, boolean, arrays, lists, etc.
    // input values:
    // long, int, double, Long, Integer, String, RString,
    // arrags

    // Search

    @Test
    public void testSearchWorks() throws Exception {

        Search s;

        init(Search.class, "next");
        method().will(returnValue(new ome.model.core.Image()));
        Object rv = invoke();
        Image img = (Image) rv;

        assertNotNull(img);

        init(Search.class, "onlyOwnedBy");
        method();
        rv = invoke(new omero.model.DetailsI());

        init(Search.class, "onlyIds");
        method();
        rv = invoke((Object) new Long[] { 1L, 2L });

        init(Search.class, "onlyIds");
        method();
        rv = invoke((Object) new Long[] { 1L });

        init(Search.class, "byAnnotatedWith");
        method();
        rv = invoke(Arrays.asList(new TagAnnotationI()));

        init(Search.class, "fetchAlso");
        method();
        rv = invoke(Arrays.asList("a", "b"));

        init(Search.class, "fetchAnnotations");
        method();
        rv = invoke(Arrays.asList("Image", "Project"));

        init(Search.class, "currentMetadataList");
        method().will(returnValue(Collections.singletonList(new HashMap())));
        rv = invoke();

    }

    // ~ Exceptions
    // =========================================================================

    public void testExceptionsDirect() throws Exception {
        omero.ServerError se = new omero.ServerError();
        Throwable t = mapper.handleException(se, ctx);
        assertEquals(t, se);

        t = mapper
                .handleException(new java.lang.IllegalThreadStateException(), ctx);
        assertTrue(t instanceof omero.InternalException);
    }

    public void testExceptionsWithHandler() throws Exception {
        // addApplicationListener currently does not work
        // instead we've subclassed OmeroContext to make
        // addListener public
        multicaster.addApplicationListener(new ApplicationListener() {
            public void onApplicationEvent(ApplicationEvent arg0) {
                int i = 0;
                i++;
                if (arg0 instanceof ConvertToBlitzExceptionMessage) {
                    ConvertToBlitzExceptionMessage msg = (ConvertToBlitzExceptionMessage) arg0;
                    if (msg.from instanceof NullPointerException) {
                        msg.to = new omero.ApiUsageException();
                    }
                }
            }
        });

        Throwable t = mapper.handleException(new NullPointerException(), ctx);
        assertTrue(t.getClass().toString(),
                t instanceof omero.InternalException);
    }

    public void testByteArraysAsMaValues() {

    }

    // ~ Helpers
    // =========================================================================

    protected ArgumentsMatchBuilder method() {
        return mock.expects(once()).method(current.operation);
    }
}
