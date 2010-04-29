/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import ome.services.blitz.fire.Registry;
import ome.services.blitz.impl.SharedResourcesI;
import ome.services.scripts.ScriptRepoHelper;
import ome.services.util.Executor;
import omero.ServerError;
import omero.model.OriginalFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Ice.Current;
import Ice.ObjectAdapter;

/**
 * Repository which makes the included script files available to users.
 *
 * @since Beta4.2
 */
public class ScriptRepositoryI extends AbstractRepositoryI {

    private final static Log log = LogFactory.getLog(ScriptRepositoryI.class);

    private final ScriptRepoHelper helper;

    public ScriptRepositoryI(ObjectAdapter oa, Registry reg, Executor ex,
            String sessionUuid, ScriptRepoHelper helper) {
        super(oa, reg, ex, sessionUuid, helper.getScriptDir());
        this.helper = helper;
    }

    @Override
    public String generateRepoUuid() {
        return this.helper.getUuid();
    }

    /**
     */
    public String getFilePath(final OriginalFile file, Current __current)
            throws ServerError {

        String url = getFileUrl(file);
        String uuid = getRepoUuid();

        if (url == null || !url.equals(uuid)) {
            throw new omero.ValidationException(null, null, url
                    + " does not belong to this repository: " + uuid);
        }

        return file.getPath() == null ? null : file.getPath().getValue();

    }

}