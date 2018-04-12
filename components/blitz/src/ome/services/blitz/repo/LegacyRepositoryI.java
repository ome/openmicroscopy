/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.repo;

import ome.io.nio.OriginalFilesService;
import ome.services.blitz.fire.Registry;
import ome.services.util.Executor;
import ome.services.util.ReadOnlyStatus;
import ome.system.Principal;
import omero.ServerError;
import omero.model.OriginalFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Ice.Current;

/**
 * Repository which makes the ${omero.data.dir} directory available
 * via the standard Repository API. Moving forward, this repository should
 * be phased out.
 *
 * @since Beta4.1
 */
public class LegacyRepositoryI extends AbstractRepositoryI {

    private final static Logger log = LoggerFactory.getLogger(LegacyRepositoryI.class);

    private final OriginalFilesService fs;

    @Deprecated
    public LegacyRepositoryI(Ice.ObjectAdapter oa, Registry reg, Executor ex,
            Principal p, String repoDir, PublicRepositoryI servant) {
        this(oa, reg, ex, p, new FileMaker(repoDir), new ReadOnlyStatus(false, false), servant);
        log.info("assuming read-write repository");
    }

    public LegacyRepositoryI(Ice.ObjectAdapter oa, Registry reg, Executor ex,
            Principal p, String repoDir, ReadOnlyStatus readOnly, PublicRepositoryI servant) {
        this(oa, reg, ex, p, new FileMaker(repoDir), readOnly, servant);
    }

    @Deprecated
    public LegacyRepositoryI(Ice.ObjectAdapter oa, Registry reg, Executor ex,
            Principal p, FileMaker fileMaker, PublicRepositoryI servant) {
        this(oa, reg, ex, p, fileMaker, new ReadOnlyStatus(false, false), servant);
        log.info("assuming read-write repository");
    }

    public LegacyRepositoryI(Ice.ObjectAdapter oa, Registry reg, Executor ex,
            Principal p, FileMaker fileMaker, ReadOnlyStatus readOnly, PublicRepositoryI servant) {
        super(oa, reg, ex, p, fileMaker, readOnly, servant);
        this.fs = new OriginalFilesService(fileMaker.getDir(), readOnly.isReadOnlyRepo());
    }

    /**
     * TODO CACHING
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
