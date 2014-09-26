/*
 *   Copyright 2009-2014 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.util.Assert;

/**
 * Composite class which delegates to each of the configured providers in turn.
 * The first instance which is responible for a user name wins.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.0
 */
public class PasswordProviders implements PasswordProvider {

    final private PasswordProvider[] providers;

    private AtomicBoolean ignoreCaseLookup;

    public PasswordProviders(PasswordProvider... providers) {
        this(new AtomicBoolean(false), providers);
    }

    public PasswordProviders(AtomicBoolean ignoreCaseLookup,
            PasswordProvider... providers) {
        Assert.notNull(providers);
        int l = providers.length;
        this.providers = new PasswordProvider[l];
        System.arraycopy(providers, 0, this.providers, 0, l);
        this.ignoreCaseLookup = ignoreCaseLookup;
    }

    public boolean hasPassword(String user) {
        user = ignoreCaseLookup.get() ? user.toLowerCase() : user;
        for (PasswordProvider provider : providers) {
            boolean hasPassword = provider.hasPassword(user);
            if (hasPassword) {
                return true;
            }
        }
        return false;
    }

    public Boolean checkPassword(String user, String password, boolean readOnly) {
        user = ignoreCaseLookup.get() ? user.toLowerCase() : user;
        for (PasswordProvider provider : providers) {
            Boolean rv = provider.checkPassword(user, password, readOnly);
            if (rv != null) {
                return rv;
            }
        }
        return null;
    }

    public void changePassword(String user, String password)
            throws PasswordChangeException {
        user = ignoreCaseLookup.get() ? user.toLowerCase() : user;
        for (PasswordProvider provider : providers) {
            boolean hasPassword = provider.hasPassword(user);
            if (hasPassword) {
                provider.changePassword(user, password);
                return;
            }
        }
        throw new PasswordChangeException("No provider found: " + user);
    }

    public void changeDistinguisedName(String user, String dn)
            throws PasswordChangeException {

        for (PasswordProvider provider : providers) {
            boolean hasPassword = provider.hasPassword(user);
            if (hasPassword) {
                provider.changePassword(user, dn);
                return;
            }
        }
        throw new PasswordChangeException("No provider found:" + user);
    }
}
