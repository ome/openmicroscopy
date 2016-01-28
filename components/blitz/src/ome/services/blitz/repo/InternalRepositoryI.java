/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.repo;

import ome.services.blitz.fire.Registry;
import ome.services.util.Executor;
import ome.system.Principal;
import omero.ServerError;
import omero.model.OriginalFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Ice.Current;
import Ice.ObjectAdapter;

/**
 * Standalone repository service.
 *
 * @since Beta4.1
 */
//TODO Better named "StandaloneRepositoryI"
public class InternalRepositoryI extends AbstractRepositoryI {

    private final static Logger log = LoggerFactory.getLogger(InternalRepositoryI.class);

    public InternalRepositoryI(ObjectAdapter oa, Registry reg, Executor ex,
            Principal p, String repoDir, PublicRepositoryI servant) {
        super(oa, reg, ex, p, repoDir, servant);
    }

    //TODO CACHING
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
