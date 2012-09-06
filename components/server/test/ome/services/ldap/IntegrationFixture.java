/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.ldap;

import java.io.File;
import java.util.UUID;

import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.conditions.SecurityViolation;
import ome.logic.LdapImpl;
import ome.model.meta.Experimenter;
import ome.model.meta.Session;
import ome.security.auth.LdapPasswordProvider;
import ome.security.auth.PasswordUtil;
import ome.security.auth.RoleProvider;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.services.util.Executor.Work;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.Roles;
import ome.system.ServiceFactory;
import ome.tools.spring.InternalServiceFactory;
import ome.util.SqlAction;

import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.transaction.annotation.Transactional;

public class IntegrationFixture
    extends Fixture
{

    private final Object old;

    private final HotSwappableTargetSource hsts;

    protected final SessionManager sessionManager;

    protected final Executor executor;

    protected Principal p;

    public IntegrationFixture(File ctxFile, OmeroContext ctx)
    {
        super(ctxFile);

        hsts = (HotSwappableTargetSource) ctx.getBean("contextSourceSwapper");
        old = hsts.getTarget();
        sessionManager = (SessionManager) ctx.getBean("sessionManager");
        executor = (Executor) ctx.getBean("executor");

        // Now we take the found source from the test configuration
        // which points to an ApacheDS server, and we swap it into
        // the current configuration.
        hsts.swap(getSource());

        InternalServiceFactory isf = new InternalServiceFactory(ctx);
        SqlAction sql = (SqlAction) ctx.getBean("simpleSqlAction");
        ldap = new LdapImpl(data, new Roles(), config, provider(), sql);
        ldap.setQueryService((LocalQuery) isf.getQueryService());
        ldap.setUpdateService((LocalUpdate) isf.getUpdateService());

        provider = new LdapPasswordProvider(new PasswordUtil(sql), ldap);
        provider.setApplicationContext(ctx);
    }

    @Override
    public void createUserWithGroup(String dn, final String group)
    {
        executor.execute(p,
            new Executor.SimpleWork(this, "createUserWithGroup")
            {

                @Transactional(readOnly = false)
                public Object doWork(org.hibernate.Session session,
                    ServiceFactory sf)
                {
                    provider().createGroup(group, null, false);
                    return null;
                }
            });
    }

    @Override
    public boolean createUserFromLdap(final String user, final String password)
    {

        // To keep things simple, if a user already exists,
        // it gets renamed. Otherwise, it would be necessary to generate
        // the ldif on every execution.
        executor.execute(p, new Executor.SimpleWork(this, "renameUser")
        {

            @Transactional(readOnly = false)
            public Object doWork(org.hibernate.Session session,
                ServiceFactory sf)
            {

                Experimenter exp = null;
                try
                {
                    exp = sf.getAdminService().lookupExperimenter(user);
                }
                catch (Exception e)
                {
                    // good
                }

                if (exp != null)
                {
                    exp.setOmeName(UUID.randomUUID().toString());
                    sf.getAdminService().updateExperimenter(exp);
                }

                return null;
            }
        });

        return (Boolean) executor.execute(p, new Executor.SimpleWork(this,
            "createUserFromLdap")
        {

            @Transactional(readOnly = false)
            public Object doWork(org.hibernate.Session session,
                ServiceFactory sf)
            {
                return ldap.createUserFromLdap(user, "password");
            }
        });
    }

    @Override
    public EventContext login(String username, String group, String password)
    {
        newSession(username, group, password);
        EventContext ec = (EventContext) executor.execute(p,
            new Executor.SimpleWork(this, "simpleCall")
            {

                @Transactional(readOnly = false)
                public Object doWork(org.hibernate.Session session,
                    ServiceFactory sf)
                {
                    return sf.getAdminService().getEventContext();
                }
            });
        return ec;
    }

    @Override
    public Object execute(Work work)
    {
        return executor.execute(p, work);
    }

    public void newSession(final String username, final String group,
        final String password)
    {

        if (password != null)
        {
            // Will cause synchronization.
            Boolean check = (Boolean) executor.execute(p,
                new Executor.SimpleWork(this, "newSession")
                {

                    @Transactional(readOnly = false)
                    public Object doWork(org.hibernate.Session session,
                        ServiceFactory sf)
                    {
                        return provider.checkPassword(username, password, true);
                    }
                });

            if (check != null && Boolean.FALSE.equals(check.booleanValue()))
            {
                throw new SecurityViolation("false returned.");
            }

        }

        Principal tmp = new Principal(username, group, "Test");
        Session s = sessionManager.createWithAgent(tmp,
            "AbstractManagedContext");
        p = new Principal(s.getUuid(), group, "Test");
    }

    public RoleProvider provider()
    {
        return (RoleProvider) executor.getContext().getBean("ldapRoleProvider");
    }

    public RoleProvider nonLdapProvider()
    {
        return (RoleProvider) executor.getContext().getBean(
            "simpleRoleProvider");
    }

    @Override
    void close()
    {
        try
        {
            super.close();
            sessionManager.closeAll();
        }
        finally
        {
            if (old != null)
            {
                hsts.swap(old);
            }
        }
    }
}
