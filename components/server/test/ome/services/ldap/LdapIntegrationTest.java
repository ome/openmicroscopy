/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.ldap;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import ome.logic.LdapImpl;
import ome.model.meta.Experimenter;
import ome.model.meta.Session;
import ome.security.auth.LdapConfig;
import ome.security.auth.RoleProvider;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.Roles;
import ome.system.ServiceFactory;
import ome.util.SqlAction;

import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Extends {@link LdapTest} to use a real db.
 */
@Test(groups = "integration")
public class LdapIntegrationTest extends LdapTest {

    Object old;
    OmeroContext mCtx;
    HotSwappableTargetSource hsts;
    SessionManager sessionManager;
    Executor executor;
    Principal p;

    @BeforeClass
    public void swap() {
        mCtx = OmeroContext.getManagedServerContext();
        hsts = (HotSwappableTargetSource) mCtx.getBean("contextSourceSwapper");
        old = hsts.getTarget();
        sessionManager = (SessionManager) mCtx.getBean("sessionManager");
        executor = (Executor) mCtx.getBean("executor");
    }

    RoleProvider provider() {
        return (RoleProvider) mCtx.getBean("roleProvider");
    }

    @BeforeMethod
    public void login() {
        p = newSession("root", "system");
    }

    public Principal newSession(String username, String group) {
        Principal tmp = new Principal(username, group, "Test");
        Session s = sessionManager.createWithAgent(tmp,
                "AbstractManagedContext");
        return new Principal(s.getUuid(), group, "Test");
    }

    @AfterMethod
    public void logout() {
        sessionManager.closeAll();
    }

    @Override
    // Critical that this is overriding.
    protected Fixture createFixture(File ctxFile) throws Exception {

        Fixture fixture = new Fixture() {
            @Override
            protected void createUserWithGroup(final LdapTest t, String dn,
                    final String group) {
                executor.execute(p, new Executor.SimpleWork(this,
                        "createUserWithGroup") {
                    @Transactional(readOnly = false)
                    public Object doWork(org.hibernate.Session session,
                            ServiceFactory sf) {
                        ((LdapIntegrationTest) t).provider().createGroup(group,
                                null, false);
                        return null;
                    }
                });
            }

            @Override
            protected boolean createUserFromLdap(final String user,
                    final String password) {

                // To keep things simple, if a user already exists,
                // it gets renamed. Otherwise, it would be necessary to generate
                // the ldif on every execution.
                executor.execute(p,
                        new Executor.SimpleWork(this, "renameUser") {
                            @Transactional(readOnly = false)
                            public Object doWork(org.hibernate.Session session,
                                    ServiceFactory sf) {

                                Experimenter exp = null;
                                try {
                                    exp = sf.getAdminService()
                                            .lookupExperimenter(user);
                                } catch (Exception e) {
                                    // goodl
                                }

                                if (exp != null) {
                                    exp.setOmeName(UUID.randomUUID().toString());
                                    sf.getAdminService()
                                            .updateExperimenter(exp);
                                }

                                return null;
                            }
                        });

                return (Boolean) executor.execute(p, new Executor.SimpleWork(
                        this, "createUserFromLdap") {
                    @Transactional(readOnly = false)
                    public Object doWork(org.hibernate.Session session,
                            ServiceFactory sf) {
                        return ldap.createUserFromLdap(user, "password");
                    }
                });
            }

            @Override
            protected void login(String username, String group) {
                Principal user = newSession(username, group);
                EventContext ec = (EventContext) executor.execute(user,
                        new Executor.SimpleWork(this, "simpleCall") {
                            @Transactional(readOnly = false)
                            public Object doWork(org.hibernate.Session session,
                                    ServiceFactory sf) {
                                return sf.getAdminService().getEventContext();
                            }
                        });
            }

        };

        fixture.ctx = new FileSystemXmlApplicationContext("file:"
                + ctxFile.getAbsolutePath());
        fixture.config = (LdapConfig) fixture.ctx.getBean("config");

        Map<String, LdapContextSource> sources = fixture.ctx
                .getBeansOfType(LdapContextSource.class);

        LdapContextSource source = sources.values().iterator().next();
        String[] urls = source.getUrls();
        assertEquals(1, urls.length);

        hsts.swap(source);

        fixture.template = (LdapTemplate) mCtx.getBean("ldapTemplate");
        fixture.template.setContextSource(source);

        fixture.ldap = new LdapImpl(source, fixture.template, new Roles(),
                fixture.config, provider(),
                (SqlAction) mCtx.getBean("simpleSqlAction"));
        return fixture;
    }

    @AfterClass
    public void unswap() {
        if (old != null && mCtx != null) {
            hsts.swap(old);
        }
    }

}
