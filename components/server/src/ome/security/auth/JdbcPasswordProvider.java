/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.security.auth;


/**
 * Central {@link PasswordProvider} which uses the "password" table in the
 * central OMERO database.
 * 
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.0
 */

public class JdbcPasswordProvider extends ConfigurablePasswordProvider {

    public JdbcPasswordProvider(PasswordUtil util) {
        super(util);
    }

    public JdbcPasswordProvider(PasswordUtil util, boolean ignoreUnknown) {
        super(util);
    }

    @Override
    public boolean hasPassword(String user) {
        Long id = util.userId(user);
        return id != null;
    }

    /**
     * Retrieves password from the database and calls
     * {@link ConfigurablePasswordProvider#comparePasswords(String, String)}.
     * Uses default logic if user is unknown.
     */
    @Override
    public Boolean checkPassword(String user, String password, boolean readOnly) {
        Long id = util.userId(user);

        // If user doesn't exist, use the default settings for
        // #ignoreUnknown.

        Boolean b = null;
        if (id == null) {
            b = super.checkPassword(user, password, readOnly);
        } else {
            String trusted = util.getUserPasswordHash(id);
            b = comparePasswords(id, trusted, password);
        }
        loginAttempt(user, b);
        return b;
    }

    @Override
    public void changePassword(String user, String password)
            throws PasswordChangeException {
        Long id = util.userId(user);
        if (id == null) {
            throw new PasswordChangeException("Couldn't find id: " + user);
        }
        util.changeUserPasswordById(id, password);
    }

}
