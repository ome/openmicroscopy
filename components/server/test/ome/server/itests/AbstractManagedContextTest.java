/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.server.itests;

import java.io.File;
import java.util.List;
import java.util.UUID;

import javax.sql.DataSource;

import ome.api.IConfig;
import ome.api.IPixels;
import ome.api.IPojos;
import ome.api.ISession;
import ome.api.local.LocalAdmin;
import ome.api.local.LocalLdap;
import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.formats.OMEROMetadataStore;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.model.containers.Dataset;
import ome.model.core.Pixels;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.meta.Session;
import ome.security.SecuritySystem;
import ome.security.basic.PrincipalHolder;
import ome.services.sessions.SessionManager;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.Roles;
import ome.system.ServiceFactory;
import ome.testing.InterceptingServiceFactory;
import ome.testing.OMEData;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;
import org.springframework.util.ResourceUtils;
import org.testng.annotations.Configuration;
import org.testng.annotations.Test;

@Test(groups = { "integration" })
public class AbstractManagedContextTest extends
        AbstractDependencyInjectionSpringContextTests {

    // ~ Testng Adapter
    // =========================================================================
    @Configuration(beforeTestMethod = true)
    public void adaptSetUp() throws Exception {
        setUp();
    }

    @Configuration(afterTestMethod = true)
    public void adaptTearDown() throws Exception {
        tearDown();
    }

    // =========================================================================

    protected LoginInterceptor loginAop;

    protected ServiceFactory factory;

    protected LocalQuery iQuery;

    protected LocalUpdate iUpdate;

    protected LocalAdmin iAdmin;

    protected LocalLdap iLdap;

    protected IConfig iConfig;

    protected IPojos iPojos;

    protected IPixels iPixels;

    protected ISession iSession;

    protected OMEData data;

    protected SimpleJdbcTemplate jdbcTemplate;

    protected LdapTemplate ldapTemplate;

    protected HibernateTemplate hibernateTemplate;

    protected SecuritySystem securitySystem;

    protected Roles roles;

    protected PrincipalHolder holder;

    protected SessionManager sessionManager;

    /**
     * @see org.springframework.test.AbstractDependencyInjectionSpringContextTests#onSetUp()
     */
    @Override
    protected void onSetUp() throws Exception {
        this.applicationContext = createApplicationContext(null);

        DataSource dataSource = (DataSource) applicationContext
                .getBean("dataSource");
        jdbcTemplate = (SimpleJdbcTemplate) applicationContext
                .getBean("simpleJdbcTemplate");

        ldapTemplate = (LdapTemplate) applicationContext
                .getBean("ldapTemplate");

        data = new OMEData();
        data.setDataSource(dataSource);

        hibernateTemplate = (HibernateTemplate) applicationContext
                .getBean("hibernateTemplate");

        securitySystem = (SecuritySystem) applicationContext
                .getBean("securitySystem");
        roles = securitySystem.getSecurityRoles();
        holder = (PrincipalHolder) applicationContext
                .getBean("principalHolder");
        sessionManager = (SessionManager) applicationContext
                .getBean("sessionManager");

        // Service setup
        loginAop = new LoginInterceptor(holder);
        factory = new ServiceFactory((OmeroContext) applicationContext);
        factory = new InterceptingServiceFactory(factory, loginAop);
        iQuery = (LocalQuery) factory.getQueryService();
        iUpdate = (LocalUpdate) factory.getUpdateService();
        iAdmin = (LocalAdmin) factory.getAdminService();
        iLdap = (LocalLdap) factory.getLdapService();
        iConfig = factory.getConfigService();
        iPojos = factory.getPojosService();
        iPixels = factory.getPixelsService();
        iSession = factory.getSessionService();

        loginRoot();

    }

    @Override
    protected void onTearDown() throws Exception {
        sessionManager.closeAll();
    }

    protected void loginRoot() {

        login(roles.getRootName(), roles.getSystemGroupName(), "Test");

    }

    public Experimenter loginNewUser() {
        loginRoot();
        String uuid;
        Experimenter user;
        String guid = uuid();
        ExperimenterGroup group = new ExperimenterGroup();
        group.setName(guid);

        iAdmin.createGroup(group);

        uuid = uuid();
        Experimenter e = new Experimenter();
        e.setFirstName("New");
        e.setLastName("User");
        e.setOmeName(uuid);

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
        loginUser(e2.getOmeName());
        return e2;
    }

    protected void loginUser(String omeName) {
        login(omeName, roles.getUserGroupName(), "Test");
    }

    @Override
    protected ConfigurableApplicationContext createApplicationContext(
            String[] locations) {
        return OmeroContext.getManagedServerContext();
    }

    protected Principal login(String userName, String groupName,
            String eventType) {
        Principal p = new Principal(userName, groupName, eventType);
        Session s = sessionManager.create(p);
        loginAop.p = new Principal(s.getUuid(), groupName, eventType);
        return loginAop.p;
    }

    protected String uuid() {
        return UUID.randomUUID().toString().replaceAll("-", "DASH");
    }

    protected String getOmeroDataDir() {
        return ((OmeroContext) applicationContext)
                .getProperty("omero.data.dir");
    }

    protected Pixels makePixels() {
        try {
            final File file = ResourceUtils
                    .getFile("classpath:tinyTest.d3d.dv");

            Dataset d = new Dataset("rendering-session-test");
            d = iUpdate.saveAndReturnObject(d);

            final OMEROMetadataStore store = new OMEROMetadataStore(
                    this.factory);
            final ImportLibrary library = new ImportLibrary(store,
                    new OMEROWrapper());

            library.setDataset(d);

            String fileName = file.getAbsolutePath();
            library.open(fileName);
            library.calculateImageCount(fileName, 0);

            final List<Pixels> pixels = library.importMetadata(fileName);
            library.importData(pixels.get(0).getId(), fileName, 0,
                    new ImportLibrary.Step() {

                        @Override
                        public void step(int series, int step) {
                        }
                    });
            return pixels.get(0);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
