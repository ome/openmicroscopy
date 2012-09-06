/*
 * Copyright 2010 Glencoe Software, Inc. All rights reserved. Use is subject to
 * license terms supplied in LICENSE.txt
 */

package ome.services.ldap;

import java.io.File;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import ome.conditions.ApiUsageException;
import ome.conditions.SecurityViolation;
import ome.conditions.ValidationException;
import ome.logic.LdapImpl;
import ome.security.auth.LdapConfig;
import ome.security.auth.LdapData;
import ome.security.auth.LdapPasswordProvider;
import ome.services.ldapsync.Modification;
import ome.services.util.Executor;
import ome.system.EventContext;

import org.jmock.MockObjectTestCase;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.DistinguishedName;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

/**
 * Fixtures which are {@link AbstractLdapTest#createFixture(File)} by the
 * various subclasses of {@link AbstractLdapTest}. Each represents a single
 * directory, containing a single Spring XML file as well as any number of LDIF
 * files arranged alpha-numerically.
 * 
 * Fixture extends {@link MockObjectTestCase} in order to have the static
 * methods like {@link #returnValue(boolean)} available locally.
 */
public abstract class Fixture
    extends MockObjectTestCase
{

    private final ConfigurableApplicationContext ctx;

    private final LdapTemplate template;

    private final LdapContextSource source;

    //
    // These are the primary classes under test. They are largely un-final
    // protected to simplify initialization by subclasses.
    //

    /**
     * The configuration is always static and shouldn't need to be modified.
     */
    protected final LdapConfig config;

    protected LdapData data;

    protected LdapImpl ldap;

    protected LdapPasswordProvider provider;

    public Fixture(File ctxFile)
    {
        ctx = new FileSystemXmlApplicationContext("file:"
            + ctxFile.getAbsolutePath());
        config = (LdapConfig) ctx.getBean("config");

        Map<String, LdapContextSource> sources = ctx
            .getBeansOfType(LdapContextSource.class);
        source = sources.values().iterator().next();
        String[] urls = source.getUrls();
        assertEquals(1, urls.length);

        template = new LdapTemplate(source);
        data = new LdapData(template, config);
    }

    /**
     * One of the primary actions performed by the LDAP plugin.
     * 
     * @param dn
     * @param group
     */
    public abstract void createUserWithGroup(String dn, String group);

    public ContextSource getSource()
    {
        return source;
    }

    public <T> Map<String, T> getBeansOfType(Class<T> k)
    {
        return ctx.getBeansOfType(k);
    }

    public void assertGoodPasses()
        throws Exception
    {
        @SuppressWarnings("unchecked")
        Map<String, List<String>> users = ctx.getBean("good", Map.class);

        for (String user : users.keySet())
        {

            // addMemberOf(fixture, template, user);

            String dn = null;
            assertTrue(1 <= users.get(user).size());
            try
            {
                dn = ldap.findDN(user);
            }
            catch (ApiUsageException aue)
            {
                // This will be one of the major errors: when we can't find a
                // user
                // that we expect to find. Adding a try/catch block for
                // debugging
                // purposes.
                throw aue;
            }
            assertNotNull(dn);
            assertEquals(user, ldap.findExperimenter(user).getOmeName());
            createUserWithGroup(dn, users.get(user).get(0));
            assertTrue(createUserFromLdap(user, "password"));
            login(user, users.get(user).get(0), "password");

            // Check that proper dn is passed to setDN
            // Check password
            // Get list of groups
            // List all users and get back the target user
            // List groups and find the good ones
            //
        }
    }

    protected void assertBadFails()
    {
        @SuppressWarnings("unchecked")
        Map<String, List<String>> users = ctx.getBean("bad", Map.class);

        for (String user : users.keySet())
        {
            assertEquals(1, users.get(user).size());
            try
            {
                String dn = ldap.findDN(user);
                assertNotNull(dn);
                createUserWithGroup(dn, users.get(user).get(0));
                assertTrue(createUserFromLdap(user, "password"));
                login(user, users.get(user).get(0), "password");
                // Parsing afterwards to force an explosion to reproduce #2557
                assertEquals(user, ldap.findExperimenter(user).getOmeName());
                fail("user didn't fail");
            }
            catch (ValidationException e)
            {
                if (e.getMessage().equals(
                    "No group found for: cn=user,ou=attributeFilter"))
                {
                    // good. This is the expected result for #8357
                }
                else
                {
                    throw e; // This means that we couldn't insert.
                    // See the thread on case-senitivty in #2557
                }
            }
            catch (ApiUsageException e)
            {
                // if not a ValidationException, but otherwise an
                // ApiUsageException,
                // then this will be the "Cannot find unique DN" which we are
                // looking for.
            }
            catch (SecurityViolation sv)
            {
                // e.g. User 466 is not a member of group 54 and cannot login
                // also good.
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void addMemberOf(Fixture fixture, LdapTemplate template,
        String user)
        throws NamingException
    {
        List<String> dns = template.search("",
            fixture.config.usernameFilter(user).encode(), new ContextMapper()
            {

                public Object mapFromContext(Object arg0)
                {
                    DirContextAdapter ctx = (DirContextAdapter) arg0;
                    return ctx.getNameInNamespace();
                }
            });
        assertEquals(dns.toString(), 1, dns.size());

        DistinguishedName name = new DistinguishedName(dns.get(0));
        DistinguishedName root = new DistinguishedName(template
            .getContextSource().getReadOnlyContext().getNameInNamespace());

        // Build a relative name
        for (int i = 0; i < root.size(); i++)
        {
            name.removeFirst();
        }

        DirContextOperations context = template.lookupContext(name);
        context.setAttributeValues("memberOf", new Object[] { "foo" });
        template.modifyAttributes(context);
    }

    public boolean createUserFromLdap(String user, String string)
    {
        return ldap.createUserFromLdap(user, "password");
    }

    public EventContext login(String username, String group, String password)
    {
        // no-op here.
        return null;
    }

    public Object execute(Executor.Work work)
    {
        throw new RuntimeException("Only in subclasses!");
    }

    void close()
    {
        ctx.close();
    }

}
