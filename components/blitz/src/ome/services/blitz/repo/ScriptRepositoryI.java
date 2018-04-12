/*
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.blitz.repo;

import java.util.Set;

import ome.services.blitz.fire.Registry;
import ome.services.scripts.ScriptRepoHelper;
import ome.services.util.Executor;
import ome.services.util.ReadOnlyStatus;
import ome.system.Principal;
import omero.ServerError;
import omero.model.OriginalFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Ice.Current;
import Ice.ObjectAdapter;

/**
 * Repository which makes the included script files available to users.
 *
 * @since Beta4.2
 */
public class ScriptRepositoryI extends AbstractRepositoryI {

    private final static Logger log = LoggerFactory.getLogger(ScriptRepositoryI.class);

    private final ScriptRepoHelper helper;

    @Deprecated
    public ScriptRepositoryI(ObjectAdapter oa, Registry reg, Executor ex,
            Principal p, ScriptRepoHelper helper, PublicRepositoryI servant, Set<String> scriptRepoUuids) {
        this(oa, reg, ex, p, helper, new ReadOnlyStatus(false, false), servant, scriptRepoUuids);
        log.info("assuming read-write repository");
    }

    public ScriptRepositoryI(ObjectAdapter oa, Registry reg, Executor ex, Principal p,
           ScriptRepoHelper helper, ReadOnlyStatus readOnly, PublicRepositoryI servant, Set<String> scriptRepoUuids) {
        super(oa, reg, ex, p, helper.getScriptDir(), readOnly, servant);
        this.helper = helper;
        scriptRepoUuids.add(helper.getUuid());
    }

    @Override
    public String generateRepoUuid() {
        return this.helper.getUuid();
    }

    /**
     */
    public String getFilePath(final OriginalFile file, Current __current)
            throws ServerError {

        String repo = getFileRepo(file);
        String uuid = getRepoUuid();

        if (repo == null || !repo.equals(uuid)) {
            throw new omero.ValidationException(null, null,repo
                    + " does not belong to this repository: " + uuid);
        }

        return file.getPath() == null ? null : file.getPath().getValue();

    }

}
