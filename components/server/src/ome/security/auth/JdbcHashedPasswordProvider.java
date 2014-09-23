/*
 *
 * Copyright (C) 2013 Glencoe Software, Inc.
 * All rights reserved.

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

package ome.security.auth;

/**
 * Central {@link PasswordProvider} which uses the "password" table in the
 * central OMERO database. When setting a password, stores password as-is
 * into database, so it must be properly hashed already.
 *
 * @author Andreas Knab, andreas at glencoesoftware.com
 */

public class JdbcHashedPasswordProvider extends JdbcPasswordProvider {

    public JdbcHashedPasswordProvider(PasswordUtil util) {
        super(util);
    }

    public JdbcHashedPasswordProvider(PasswordUtil util, boolean ignoreUnknown) {
        super(util);
    }

    @Override
    public void changePassword(String user, String md5password)
            throws PasswordChangeException {
        changePassword(user, md5password, PasswordUtil.METHOD.CLEAR);
    }
}
