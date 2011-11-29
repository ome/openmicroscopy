/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import ome.services.blitz.fire.Registry;
import ome.services.util.Executor;
import ome.util.SqlAction;
import omero.ServerError;
import omero.model.OriginalFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.Current;
import Ice.ObjectAdapter;

/**
 * Standalone repository service.
 *
 * @DEV.TODO Better named "StandaloneRepositoryI"
 * @since Beta4.1
 */
public class InternalRepositoryI extends AbstractRepositoryI {

    private final static Log log = LogFactory.getLog(InternalRepositoryI.class);

    public InternalRepositoryI(ObjectAdapter oa, Registry reg, Executor ex,
            SqlAction sql, String sessionUuid, String repoDir) {
        super(oa, reg, ex, sql, sessionUuid, repoDir);
    }

    /**
     * @DEV.TODO CACHING
     */
    public String getFilePath(final OriginalFile file, Current __current)
            throws ServerError {

        String repo = getFileRepo(file);
        String uuid = getRepoUuid();

        if (repo == null || !repo.equals(uuid)) {
            throw new omero.ValidationException(null, null, repo
                    + " does not belong to this repository: " + uuid);
        }

        return file.getPath() == null ? null : file.getPath().getValue();

    }

}
