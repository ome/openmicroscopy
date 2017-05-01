/*
 * Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.util;

import java.util.UUID;

import ome.util.SqlAction;

/**
 * Initialize the <tt>_secret_keys</tt> table in the database.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.4.0
 */
public class DBInitSecretKeys {

    private SqlAction sql;
    private UUID fileRepoSecretKey;

    public DBInitSecretKeys(SqlAction sql, UUID fileRepoSecretKey) {
        this.sql = sql;
        this.fileRepoSecretKey = fileRepoSecretKey;
    }

    public void start() {
        sql.setSecretKeyRepoFile(fileRepoSecretKey.toString());
    }
}
