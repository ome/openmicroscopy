/*
 *   $Id$
 *
 *   Copyright 2006-2014 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import junit.framework.TestCase;
import ome.api.IConfig;
import ome.api.IContainer;
import ome.api.ILdap;
import ome.api.IMetadata;
import ome.api.IPixels;
import ome.api.ISession;
import ome.api.local.LocalAdmin;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.formats.MockedOMEROImportFixture;
import ome.model.IObject;
import ome.model.containers.Dataset;
import ome.model.core.Image;
import ome.model.core.Pixels;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.parameters.Parameters;
import ome.security.SecuritySystem;
import ome.security.basic.CurrentDetails;
import ome.security.basic.PrincipalHolder;
import ome.services.fulltext.FullTextThread;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.Roles;
import ome.system.ServiceFactory;
import ome.testing.InterceptingServiceFactory;
import ome.testing.OMEData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jmock.Mock;
import org.jmock.core.stub.DefaultResultStub;
import org.springframework.aop.interceptor.JamonPerformanceMonitorInterceptor;
import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.mail.MailSender;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.util.ResourceUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

@Test(groups = { "integration" })
public class AbstractManagedContextTest extends TestCase {

    // =========================================================================

    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    protected LoginInterceptor loginAop;

    /**
     * Factory which provides "wrapped" managed services which handles login as
     * would take place via ISession
     */
    protected ServiceFactory factory;

    protected LocalQuery iQuery;

    protected LocalUpdate iUpdate;

    protected LocalAdmin iAdmin;

    protected ILdap iLdap;

    protected IConfig iConfig;

    protected IContainer iContainer;

    protected IMetadata iMetadata;

    protected IPixels iPixels;

    protected ISession iSession;

    protected OMEData data;

    protected LdapTemplate ldapTemplate;

    protected HibernateTemplate hibernateTemplate;

    protected SecuritySystem securitySystem;

    protected Roles roles;

    protected PrincipalHolder holder;

    protected SessionManager sessionManager;

    protected Executor executor;
    
    protected OmeroContext applicationContext;

    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @BeforeClass
    protected final void onSetUp() throws Exception {
        this.applicationContext = OmeroContext.getManagedServerContext();
        applicationContext.refreshAllIfNecessary();

        DataSource dataSource = (DataSource) applicationContext
                .getBean("dataSource");

        ldapTemplate = (LdapTemplate) applicationContext
                .getBean("ldapTemplate");

        // data = new OMEData();
        // data.setDataSource(dataSource);

        securitySystem = (SecuritySystem) applicationContext
                .getBean("securitySystem");
        roles = securitySystem.getSecurityRoles();
        holder = (PrincipalHolder) applicationContext
                .getBean("principalHolder");
        sessionManager = (SessionManager) applicationContext
                .getBean("sessionManager");
        executor = (Executor) this.applicationContext.getBean("executor");

        // Service setup
        JamonPerformanceMonitorInterceptor jamon = new JamonPerformanceMonitorInterceptor();
        loginAop = new LoginInterceptor((CurrentDetails) holder);
        factory = new ServiceFactory((OmeroContext) applicationContext);
        factory = new InterceptingServiceFactory(factory, loginAop, jamon);
        iQuery = (LocalQuery) factory.getQueryService();
        iUpdate = (LocalUpdate) factory.getUpdateService();
        iAdmin = (LocalAdmin) factory.getAdminService();
        iLdap = factory.getLdapService();
        iConfig = factory.getConfigService();
        iContainer = factory.getContainerService();
        iMetadata = factory.getMetadataService();
        iPixels = factory.getPixelsService();
        iSession = factory.getSessionService();

        // Disabling the background indexer. If you want indexing then
        FullTextThread ftt = applicationContext
            .getBean("fullTextThread", FullTextThread.class);
        ftt.stop();

        loginRoot();

    }

    @AfterClass
    protected final void onTearDown() throws Exception {
        sessionManager.closeAll();
    }

    protected void loginRoot() {
        loginRoot(roles.getSystemGroupName());
    }

    protected void loginRootKeepGroup() {
        String name = iAdmin.getEventContext().getCurrentGroupName();
        loginRoot(name);
    }

    protected void loginRoot(String groupName) {
        login(roles.getRootName(), groupName, "Test");
    }

    protected Experimenter currentUser() {
        long gid = iAdmin.getEventContext().getCurrentUserId();
        return iAdmin.getExperimenter(gid);
    }

    protected ExperimenterGroup currentGroup() {
        long gid = iAdmin.getEventContext().getCurrentGroupId();
        return iAdmin.getGroup(gid);
    }

    protected void loginUserKeepGroup(String uname) {
        String gname = iAdmin.getEventContext().getCurrentGroupName();
        loginUser(uname, gname);
    }

    protected void loginUserKeepGroup(Experimenter e) {
        String gname = iAdmin.getEventContext().getCurrentGroupName();
        loginUser(e.getOmeName(), gname);
    }

    public Experimenter loginNewUser() {
        return loginNewUser(Permissions.PRIVATE);
    }

    public Experimenter loginNewUser(Permissions p) {
        loginRoot();
        String uuid;
        Experimenter user;
        String guid = uuid();
        ExperimenterGroup group = new ExperimenterGroup();
        group.getDetails().setPermissions(p);
        group.setName(guid);
        group.setLdap(false);

        iAdmin.createGroup(group);

        uuid = uuid();
        Experimenter e = new Experimenter();
        e.setFirstName("New");
        e.setLastName("User");
        e.setOmeName(uuid);
        e.setLdap(false);

        long uid = iAdmin.createUser(e, guid);
        user = iQuery.get(Experimenter.class, uid);

        loginUser(uuid);
        return user;
    }

    public Experimenter loginNewUserInOtherUsersGroup(Experimenter e1) {
        Experimenter e2 = loginNewUser();
        // Here we add the second user to the same
        // group to make sure s/he can see the image.
        // If we ever move to private permissions by
        // default, then we will need to do a chmod
        // on the whole image graph.
        loginRoot();
        ExperimenterGroup g1 = iAdmin.getDefaultGroup(e1.getId());
        iAdmin.addGroups(e2, g1);
        loginUser(e2.getOmeName(), g1.getName()); // ticket:1434, in same group
        return e2;
    }

    public ExperimenterGroup loginUserInNewGroup(Experimenter e1) {
        loginRoot();
        String name = uuid();
        ExperimenterGroup newGroup = new ExperimenterGroup();
        newGroup.setName(name);
        newGroup.setLdap(false);
        long gid = iAdmin.createGroup(newGroup);
        iAdmin.addGroups(e1, new ExperimenterGroup(gid, false));
        login(e1.getOmeName(), name, "Test");
        return iAdmin.lookupGroup(name);
    }

    protected void loginUser(String omeName) {
        loginUser(omeName, roles.getUserGroupName());
    }

    protected void loginUser(String omeName, String groupName) {
        login(omeName, groupName, "Test");
    }

    protected Principal login(EventContext ec) {
        return login(ec.getCurrentUserName(), ec.getCurrentGroupName(),
                ec.getCurrentEventType());
    }

    protected Principal login(String userName, String groupName,
            String eventType) {
        Principal p = new Principal(userName, groupName, eventType);
        Session s = sessionManager.createWithAgent(p, "AbstractManagedContext", "127.0.0.1");
        loginAop.p = new Principal(s.getUuid(), groupName, eventType);
        return loginAop.p;
    }

    protected void indexObject(IObject obj) {
        EventContext ec = iAdmin.getEventContext();
        loginRootKeepGroup();
        iUpdate.indexObject(obj);
        login(ec);
    }

    protected String uuid() {
        return uuid("DASH");
    }

    protected String uuid(String dashes) {
        return UUID.randomUUID().toString().replaceAll("-", dashes);
    }

    protected String getOmeroDataDir() {
        return ((OmeroContext) applicationContext)
                .getProperty("omero.data.dir");
    }

    protected Image new_Image(String name) {
        Image i = new Image();
        i.setName(name);
        return i;
    }

    protected Pixels makePixels() {
        try {
            MockedOMEROImportFixture fixture = new MockedOMEROImportFixture(
                    this.factory, "");

            List<omero.model.Pixels> pix = fixture.fullImport(ResourceUtils
                    .getFile("classpath:tinyTest.d3d.dv"), "tinyTest");
            if (pix == null) {
                throw new RuntimeException("No pixels returned.");
            }
            if (pix.size() != 1) {
                throw new RuntimeException("Expected 1, got: " + pix.size());
            }

            return new Pixels(pix.get(0).getId().getValue(), false);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    protected Image makeImage(boolean withDataset) throws Exception {
        return makeImage("classpath:tinyTest.d3d.dv", withDataset);
    }

    protected Image makeImage(String path, boolean withDataset) throws Exception {
        
        return makeImage(path, withDataset, 1);
        
    }
    
    protected Image makeImage(String path, boolean withDataset, Integer pixelCount) throws Exception {
        
        MockedOMEROImportFixture fixture = new MockedOMEROImportFixture(
                this.factory, "");

        File test = ResourceUtils.getFile(path);
        List<omero.model.Pixels> pixs = fixture.fullImport(test, "test");
        // fullImport calls tearDown

        if (pixelCount != null) {
            assertEquals(pixelCount.intValue(), pixs.size());
        }
        omero.model.Pixels p = pixs.get(0);
        assertNotNull(p);
        Image i = new Image(p.getImage().getId().getValue(), false);

        if (withDataset) {
            Dataset d = new Dataset();
            d.setName("test image");
            d.linkImage(i);
            iUpdate.saveObject(d);
        }

        i = this.factory.getQueryService().findByQuery(
                "select i from Image i "
                        + "left outer join fetch i.datasetLinks dil "
                        + "left outer join fetch dil.parent d "
                        + "left outer join fetch d.imageLinks "
                        + "left outer join fetch i.pixels p "
                        + "where p.id = :id",
                new Parameters().addId(pixs.get(0).getId().getValue()));

        assertNotNull(i);
        return i;
    }

    protected <T extends IObject> void assertWorldReadable(T t) {
        Permissions p = t.getDetails().getPermissions();
        assertTrue(p.isGranted(Role.GROUP, Right.READ));
        assertTrue(p.isGranted(Role.WORLD, Right.READ));
    }

    protected <T extends IObject> void assertWorldReadable(List<T> list) {
        for (T t : list) {
            if (t.getId().equals(0L) || t.getId().equals(1L)) {
                continue; // Skipping root and guest.
            }
            assertWorldReadable(t);
        }
    }

    protected MailSender setMailSender(MailSender ms) {
        HotSwappableTargetSource hsts =
            applicationContext.getBean("mailSenderTargetSource",
                    HotSwappableTargetSource.class);
        return (MailSender) hsts.swap(ms);
    }


    protected MailSender setNoopMailSender() {
        MailSender old;
        final Mock proxy = new Mock(MailSender.class);
        proxy.setDefaultStub(new DefaultResultStub());

        old = setMailSender((MailSender) proxy.proxy());
        return old;
    }
}
