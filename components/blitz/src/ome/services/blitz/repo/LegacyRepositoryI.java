/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import ome.io.nio.OriginalFilesService;
import ome.services.blitz.fire.Registry;
import ome.services.util.Executor;
import ome.util.SqlAction;
import omero.ServerError;
import omero.model.OriginalFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.Current;

/**
 * Repository which makes the ${omero.data.dir} directory available
 * via the standard Repository API. Moving forward, this repository should
 * be phased out.
 *
 * @since Beta4.1
 */
public class LegacyRepositoryI extends AbstractRepositoryI {

    private final static Log log = LogFactory.getLog(LegacyRepositoryI.class);

    private final OriginalFilesService fs;

    public LegacyRepositoryI(Ice.ObjectAdapter oa, Registry reg, Executor ex,
            SqlAction sql, String sessionUuid, String repoDir, String template) {
        this(oa, reg, ex, sql, sessionUuid, new FileMaker(repoDir), template);
    }

    public LegacyRepositoryI(Ice.ObjectAdapter oa, Registry reg, Executor ex,
            SqlAction sql, String sessionUuid, FileMaker fileMaker, String template) {
        super(oa, reg, ex, sql, sessionUuid, fileMaker, template);
        this.fs = new OriginalFilesService(fileMaker.getDir());
    }

    /**
     * @DEV.TODO CACHING
     */
    public String getFilePath(final OriginalFile file, Current __current)
            throws ServerError {

        String repo = getFileRepo(file);

        if (repo != null) {
            throw new omero.ValidationException(null, null,
                    "Does not belong to this repository");
        }

        return fs.getFilesPath(file.getId().getValue());

    }

}
