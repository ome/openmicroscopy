/*
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.test.utests;

import static omero.rtypes.rbool;
import static omero.rtypes.rdouble;
import static omero.rtypes.rint;
import static omero.rtypes.rinternal;
import static omero.rtypes.rlist;
import static omero.rtypes.rlong;
import static omero.rtypes.rmap;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.math.BigDecimal;
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
import ome.api.IContainer;
import ome.api.IMetadata;
import ome.api.IQuery;
import ome.api.ISession;
import ome.api.IShare;
import ome.api.ITypes;
import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.api.RawPixelsStore;
import ome.api.Search;
import ome.api.ServiceInterface;
import ome.api.ThumbnailStore;
import ome.conditions.SecurityViolation;
import ome.model.IObject;
import ome.model.acquisition.Objective;
import ome.model.enums.Family;
import ome.model.enums.FilterType;
import ome.model.internal.Permissions;
import ome.model.meta.ExperimenterGroup;
import ome.model.roi.Roi;
import ome.parameters.Parameters;
import ome.parameters.QueryParameter;
import ome.services.blitz.impl.RoiI;
import ome.services.blitz.util.ConvertToBlitzExceptionMessage;
import ome.services.blitz.util.IceMethodInvoker;
import ome.services.messages.GlobalMulticaster;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.system.Roles;
import omeis.providers.re.RGBBuffer;
import omeis.providers.re.RenderingEngine;
import omero.RMap;
import omero.RString;
import omero.RType;
import omero.UnloadedCollectionException;
import omero.api.RoiResult;
import omero.model.Experimenter;
import omero.model.ExperimenterGroupI;
import omero.model.ExperimenterI;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.TagAnnotationI;
import omero.romio.XY;
import omero.rtypes.Conversion;
import omero.sys.Filter;
import omero.sys.ParametersI;
import omero.util.IceMap;
import omero.util.IceMapper;

import org.jmock.Mock;
import org.jmock.MockObjectTestCase;
import org.jmock.builder.ArgumentsMatchBuilder;
import org.jmock.core.Constraint;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

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
    @BeforeMethod
    protected void setUp() throws Exception {
        new GlobalMulticaster().removeAllListeners(); // Static singleton.
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
            return String.format("%d closes", closed);
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
        
        public void createThumbnailsByLongestSideSet(Integer size,
                Set<Long> pixelsIds) {
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

        public boolean isInProgress() {
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

        public long getRenderingDefId() {
            return -1;
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
        imi.invoke(prx, current, new IceMapper(), (java.lang.Object[]) null);

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

    @Test(groups = "ticket:1321")
    public void testSessionsGetInputs() throws Exception {

        ISession s;

        RString val = rstring("value");

        init(ISession.class, "setInput");
        method().with(eq("sess"), eq("key"), eq("value"));
        invoke("sess", "key", rstring("value"));

        Map map = new HashMap();
        map.put("key", "value");

        init(ISession.class, "getInputs");
        method().will(returnValue(map));
        Object rv = invoke("sess");

    }

    @Test
    public void testRMapRecursion() throws Exception {
        RMap map = rmap();
        map.getValue().put("m", map);
        Conversion c = (Conversion) map;
        c.convert(new IceMapper());

        init(ISession.class, "getInput");
        map.put("recurse", map);
        method().will(returnValue(map));
        invoke("sess", "recursive");

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

            public Permissions getCurrentGroupPermissions() {
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

    @Test(groups = "ticket:1775")
    public void testPermissionsMapToNull() throws Exception {
        ExperimenterGroupI in = new ExperimenterGroupI();
        assertNull(in.getDetails().getPermissions());
        IceMapper mapper = new IceMapper();
        ExperimenterGroup out = (ExperimenterGroup)
            mapper.handleInput(ExperimenterGroup.class, in);
        assertNull(out.getDetails().getPermissions());
    }

    // Types

    @Test( groups = {"ticket:1436","broken"} )
    public void testTypesWorks() throws Exception {

        ITypes t;

        init(ITypes.class, "getEnumerationTypes");
        method().will(returnValue(Arrays.asList(FilterType.class)));

        Object rv = invoke();
        assertNotNull(rv);

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
                    if (!Long.valueOf(p.owner()).equals(2L)) {
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

    @Test
    public void testUpdateHanlesCollectionsProperly() throws Exception {

        init(IUpdate.class, "saveCollection");
        method(); // void

        Object rv = invoke(Arrays.asList(new omero.model.ImageI()));
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
        method().will(returnValue(Long.valueOf(1)));

        rv = invoke();
        assertTrue(1 == (Long) rv);

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

    // Container

    @Test
    public void testContainerWorks() throws Exception {

        IContainer p;

        Map<String, RType> paramMap = new HashMap<String, RType>();
        paramMap.put("foo", rstring("bar"));
        ParametersI param = new ParametersI(paramMap);

        init(IContainer.class, "loadContainerHierarchy");
        method().will(returnValue(new HashSet()));
        Object rv = invoke("Project", Arrays.asList(1L), param);

    }

    // Moved in 4.0
    @Test
    public void testContainerCanFindAnnotations() throws Exception {
        IMetadata p;

        Map<String, RType> paramMap = new HashMap<String, RType>();
        paramMap.put("foo", rbool(true));
        ParametersI param = new ParametersI(paramMap);

        Map<Long, Set<? extends IObject>> retVal = new HashMap<Long, Set<? extends IObject>>();

        init(IMetadata.class, "loadAnnotations");
        method().will(returnValue(retVal));
        Object rv = invoke("Image", Arrays.asList(1L, 2L), 
                Arrays.asList("CommentAnnotation"),
                Arrays.asList(1L, 2L), param);
        Map map = (Map) rv;
        assertNotNull(map);

    }

    @Test
    public void testContainerReceivesProperPojoOptions() throws Exception {
        IContainer p;

        ParametersI params = new ParametersI();
        params.leaves();
        params.exp(rlong(1));

        init(IContainer.class, "loadContainerHierarchy");
        method().with(ANYTHING, ANYTHING, new Constraint() {
            public StringBuffer describeTo(StringBuffer buffer) {
                buffer.append(" proper PojoOptions ");
                return buffer;
            }

            public boolean eval(Object o) {
                if (o instanceof Parameters) {
                    Parameters po = (Parameters) o;
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
        Object rv = invoke("Image", Arrays.asList(1L, 2L), params);
    }

    @Test
    public void testContainerHandlesArraysProperly() throws Exception {

        init(IContainer.class, "createDataObjects");

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

        init(IContainer.class, "updateDataObjects");

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

        init(IContainer.class, "link");

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

        t = mapper.handleException(new java.lang.IllegalThreadStateException(),
                ctx);
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
                t instanceof omero.ApiUsageException);
    }

    public void testByteArraysAsMaValues() {

    }

    @Test(groups = "ticket:1150")
    public void testRFloatDoesNotGetRounded() throws Exception {
        Objective o = new Objective();
        o.setLensNA(new Double(1.4));
        omero.model.Objective o2 = (omero.model.Objective) mapper.handleOutput(
                Objective.class, o);
        assertEquals(o.getLensNA().doubleValue(), o2.getLensNA().getValue());
    }
    
    @Test(groups = "ticket:1150")
    public void testFloatDoesNotGetRounded() throws Exception {
        
        float f = 1.4f;
        
        // These fail
        
        double dbl1 = rdouble(f).getValue();
        assertFalse("1.4".equals(Double.toString(dbl1)));
        
        double dbl2 = f;
        assertFalse("1.4".equals(Double.toString(dbl2)));
        
        double dbl3 = rdouble(dbl2).getValue();
        assertFalse("1.4".equals(Double.toString(dbl3)));
        
        double dbl4 = BigDecimal.valueOf(f).doubleValue();
        assertFalse("1.4".equals(Double.toString(dbl4)));
        
        // These work
        
        double dbl5 = new BigDecimal("" + f).doubleValue();
        assertTrue("1.4".equals(Double.toString(dbl5)));
        
        double dbl6 = Double.parseDouble(String.valueOf(f));
        assertTrue("1.4".equals(Double.toString(dbl6)));
    }
    
    @Test
    public void testNullFromGetPrimaryPixels() throws Exception {
        ome.model.core.Image i = new ome.model.core.Image();
        i.putAt(ome.model.core.Image.PIXELS, null);
        assertEquals(-1, i.sizeOfPixels());
        Image mapped = (Image) mapper.map(i);
        assertEquals(-1, mapped.sizeOfPixels());
        try {
            mapped.getPrimaryPixels();
            fail("must throw");
        } catch (UnloadedCollectionException uce) {
            // good
        }
        try {
            mapped.copyPixels();
            fail("must throw");
        } catch (UnloadedCollectionException uce) {
            // good
        }
    }

    // ~ RoiResults
    // =========================================================================

    @Test
    public void testRoiResultMaps() throws Exception {
        Map<Long, List<Roi>> rois = new HashMap<Long, List<Roi>>();
        rois.put(1L, Collections.singletonList(new Roi()));
        IceMapper mapper = new IceMapper();
        Map<Long, RoiResult> rrs = (Map<Long, RoiResult>)
            new RoiI.RoiResultMapReturnMapper(null).mapReturnValue(mapper, rois);
        RoiResult rr = rrs.get(1L);
        assertNotNull(rr);
        
    }
    
    // ~ Helpers
    // =========================================================================

    protected ArgumentsMatchBuilder method() {
        return mock.expects(once()).method(current.operation);
    }
}
