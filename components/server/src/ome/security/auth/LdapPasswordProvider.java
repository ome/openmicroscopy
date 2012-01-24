/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import java.security.Permissions;

import ome.api.local.LocalLdap;
import ome.conditions.ApiUsageException;
import ome.security.LdapUtil;
import ome.security.PasswordUtil;
import ome.security.SecuritySystem;

import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.util.Assert;

/**
 * LDAP {@link PasswordProvider} which can create users on
 * {@link #checkPassword(String, String) request} to synchronize with an LDAP
 * directory. Assuming that a user exists in the configured LDAP store but not
 * in the database, then a new user will be created. Authentication, however,
 * always takes place against LDAP, and changing passwords is not allowed.
 * 
 * Note: deleted LDAP users will not be removed from OMERO, but will not be able
 * to login.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @see SecuritySystem
 * @see Permissions
 * @since 4.0
 */

public class LdapPasswordProvider extends ConfigurablePasswordProvider {

    final protected LocalLdap ldap;

    final protected SimpleJdbcOperations jdbc;

    public LdapPasswordProvider(LocalLdap ldap, SimpleJdbcOperations jdbc) {
        super();
        Assert.notNull(ldap);
        Assert.notNull(jdbc);
        this.ldap = ldap;
        this.jdbc = jdbc;
    }

    public LdapPasswordProvider(LocalLdap ldap, SimpleJdbcOperations jdbc,
            boolean ignoreUnknown) {
        super(ignoreUnknown);
        Assert.notNull(ldap);
        Assert.notNull(jdbc);
        this.ldap = ldap;
        this.jdbc = jdbc;
    }

    /**
     * Only returns if the user is already in the database and has a DN value in
     * the password table. Note: after a call to
     * {@link #checkPassword(String, String)} with this same user value, this
     * method might begin to return true due to a call to
     * {@link LocalLdap#createUserFromLdap(String, String)}.
     */
    @Override
    public boolean hasPassword(String user) {
        if (ldap.getSetting()) {
            Long id = PasswordUtil.userId(jdbc, user);
            if (id != null) {
                String dn = LdapUtil.lookupLdapAuthExperimenter(jdbc, id);
                if (dn != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Boolean checkPassword(String user, String password) {

        if (!ldap.getSetting()) {
            return null; // EARLY EXIT!
        }

        Long id = PasswordUtil.userId(jdbc, user);

        // Note: LDAP simple authentication defaults to anonymous
        // binding if the password is blank:
        //
        // 5.1.2. Unauthenticated Authentication Mechanism of Simple Bind
        //
        //  An LDAP client may use the unauthenticated authentication mechanism
        //  of the simple Bind method to establish an anonymous authorization
        //  state by sending a Bind request with a name value (a distinguished
        //  name in LDAP string form [RFC4514] of non-zero length) and specifying
        //  the simple authentication choice containing a password value of zero
        //  length.
        //
        //  Since an anonymous bind proves nothing about the validity of this
        //  user, we disable all attempts to login with an empty password.
        //
        //  The same check takes place in LdapImpl.isAuthContext method.
        //
        if (password == null || password.equals("")) {
            log.warn("Empty password for user: " + user);
            return false;
        }


        // Unknown user. First try to create.
        if (null == id) {
            try {
                boolean login = ldap.createUserFromLdap(user, password);
                // Use default logic if the user creation did not exist,
                // because there may be another non-database login mechanism
                // which should also be given a chance.
                if (login) {
                    return true;
                }
            } catch (ApiUsageException e) {
                log.warn("Default choice on create user exception: " + user, e);
            }
        }

        // Known user
        else {
            try {
                String dn = LdapUtil.lookupLdapAuthExperimenter(jdbc, id);
                if (dn != null) {
                    return ldap.validatePassword(dn, password);
                }
            } catch (ApiUsageException e) {
                log.warn("Default choice on check ldap password: " + user, e);
            }
        }

        // If anything goes wrong, use the default (configurable) logic.
        return super.checkPassword(user, password);
    }
}
