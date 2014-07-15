/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
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

import ome.conditions.InternalException;
import ome.util.SqlAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

/**
 * Spring bean run on start-up to make sure that the file size checksum algorithm is available.
 * A SQL upgrade script handles this issue for 5.1 and beyond.
 * 
 * @author m.t.b.carroll@dundee.ac.uk
 * @since 5.0.3
 */
public class DBFileSizeChecksumCheck {

    private static final Logger log = LoggerFactory.getLogger(DBFileSizeChecksumCheck.class);

    /* omero.model.enums.ChecksumAlgorithmFileSize64.value */
    private static final String ALGORITHM_NAME = "File-Size-64";

    private final Executor executor;

    public DBFileSizeChecksumCheck(Executor executor) {
        this.executor = executor;
    }

    public void start() {
        try {
            final Long algorithmId =
                    (Long) executor.executeSql(new Executor.SimpleSqlWork(this, "DBFileSizeChecksumCheck") {
                @Transactional(readOnly = true)
                public Long doWork(SqlAction sql) {
                    return sql.getChecksumAlgorithmId(ALGORITHM_NAME);
                }
            });
            if (algorithmId == null) {
                executor.executeSql(new Executor.SimpleSqlWork(this, "DBFileSizeChecksumCheck") {
                    @Transactional(readOnly = false)
                    public Object doWork(SqlAction sql) {
                        sql.addChecksumAlgorithm(ALGORITHM_NAME);
                        return null;
                    }
                });
                log.info("made the file-size checksum algorithm available");
            } else if (log.isDebugEnabled()) {
                log.debug("verified that the file-size checksum algorithm is available, ID is " + algorithmId);
            }
        } catch (Exception e) {
            final String message = "error in verifying that the file-size checksum algorithm is available";
            log.error(message, e);
            throw new InternalException(message);
        }
    }
}
