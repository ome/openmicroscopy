/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import ome.conditions.InternalException;
import ome.system.PreferenceContext;
import ome.util.SqlAction;

/**
 * Checks that the database contains correctly encoded enumerations for units of measure.
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.1.2
 *
 */
public class DBUnicodeUnitsCheck extends BaseDBCheck {

    public final static Logger LOGGER = LoggerFactory.getLogger(DBUnicodeUnitsCheck.class);

    protected DBUnicodeUnitsCheck(Executor executor, PreferenceContext preferences) {
        super(executor, preferences);
    }

    @Override
    protected void doCheck() {
        final boolean hasUnicodeUnits;
        try {
            hasUnicodeUnits = (Boolean) executor.executeSql(new Executor.SimpleSqlWork(this, "DBUnicodeUnitsCheck") {
                @Transactional(readOnly = true)
                public Boolean doWork(SqlAction sql) {
                    return sql.hasUnicodeUnits();
                }
            });
        } catch (Exception e) {
            final String message = "Error while checking the encoding of units of measure.";
            LOGGER.error(message, e);
            throw new InternalException(message);
        }
        if (hasUnicodeUnits) {
            LOGGER.info("Database has the correctly encoded units of measure.");
        } else {
            final String message = "Database does not contain correctly encoded units of measure.";
            LOGGER.error(message);
            throw new InternalException(message);
        }
    }
}
