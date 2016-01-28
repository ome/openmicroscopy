/*
 *   $Id$
 *
 *   Copyright 2009-2011 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import java.security.Permissions;

import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.logic.LdapImpl;
import ome.model.meta.Experimenter;
import ome.security.SecuritySystem;

import org.springframework.util.Assert;

/**
 * LDAP {@link PasswordProvider} which can create users on
 * {@link #checkPassword(String, String, boolean) request} to synchronize with an LDAP
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

    final protected LdapImpl ldapUtil;

    public LdapPasswordProvider(PasswordUtil util, LdapImpl ldap) {
        super(util);
        Assert.notNull(ldap);
        this.ldapUtil = ldap;
    }

    public LdapPasswordProvider(PasswordUtil util,
            LdapImpl ldap,
            boolean ignoreUnknown) {
        super(util, ignoreUnknown);
        Assert.notNull(ldap);
        this.ldapUtil = ldap;
    }

    /**
     * Only returns if the user is already in the database and has a DN value in
     * the password table. Note: after a call to
     * {@link #checkPassword(String, String, boolean)} with this same user value, this
     * method might begin to return {@code true} due to a call to
     * {@link LdapImpl#createUser(String, String)}.
     */
    @Override
    public boolean hasPassword(String user) {
        if (ldapUtil.getSetting()) {
            Long id = util.userId(user);
            if (id != null) {
                String dn = ldapUtil.lookupLdapAuthExperimenter(id);
                if (dn != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Boolean checkPassword(String user, String password, boolean readOnly) {

        if (!ldapUtil.getSetting()) {
            return null; // EARLY EXIT!
        }

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
            loginAttempt(user, false);
            return false;
        }

        Long id = util.userId(user);

        // Unknown user. First try to create.
        if (null == id) {
            try {
                if (readOnly == true) {
                    throw new IllegalStateException("Cannot create user!");
                }
                Experimenter experimenter = ldapUtil.createUser(user, password);
                // Use default logic if the user creation did not exist,
                // because there may be another non-database login mechanism
                // which should also be given a chance.
                if (experimenter != null) {
                    loginAttempt(user, true);
                    return true;
                }
            } catch (ApiUsageException e) {
                log.info(String.format(
                        "Default choice on create user: %s (%s)", user, e));
            }
        }

        // Known user, preventing special users by checking for a null dn
        // in which case we ignore any information from LDAP.
        // See ticket:6702
        final String dn1 = (id == null) ? null : getOmeroDN(id);
        if (dn1 != null) {

            // If LDAP doesn't return a DN for a user that expects one
            // then assume that they've been locked out. ticket:6248
            final String dn2 = getLdapDN(user);
            if (dn2 == null) {
                log.info(String.format(
                        "User not found in LDAP: {username=%s, dn=%s}",
                        user, dn1));
                return loginAttempt(user, false);
            } else if (!dn1.equals(dn2)) {
                String msg = String.format("DNs don't match: '%s' and '%s'",
                        dn1, dn2);
                log.warn(msg);
                loginAttempt(user, false);
                // Throwing an exception so that the permissions verifier
                // will state an "InternalException: Please contact your admin"
                // We will need to find another way to handle this.
                // Perhaps a hard-coded value in "password"."dn"
                throw new ValidationException(msg);
            } else {
                ldapUtil.synchronizeLdapUser(user);
                return loginAttempt(user,
                        ldapUtil.validatePassword(dn1, password));
            }
        }

        // If anything goes wrong or no LDAP is found in OMERO,
        // then use the default (configurable) logic, which will
        // probably return null in order to check JDBC for the password.
        return super.checkPassword(user, password, readOnly);
    }

    private String getOmeroDN(long id) {
        try {
            String dn = ldapUtil.lookupLdapAuthExperimenter(id);
            if (log.isDebugEnabled()) {
                log.debug(String.format("lookupLdap(%s)=%s", id, dn));
            }
            return dn;
        } catch (ApiUsageException e) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("lookupLdap(%s) is empty", id));
            }
            return null;
        }
    }

    private String getLdapDN(String user) {
        try {
            String dn = ldapUtil.findDN(user);
            if (log.isDebugEnabled()) {
                log.debug(String.format("findDN(%s)=%s", user, dn));
            }
            return dn;
        } catch (ApiUsageException e) {
            if (log.isDebugEnabled()) {
                log.debug(String.format("findDN(%s) is empty", user));
            }
            return null;
        }
    }
}
