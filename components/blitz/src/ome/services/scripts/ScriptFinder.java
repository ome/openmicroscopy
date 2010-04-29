/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.scripts;

import java.io.File;
import java.util.List;

import ome.conditions.InternalException;
import ome.model.core.OriginalFile;
import ome.parameters.Parameters;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.Roles;
import ome.system.ServiceFactory;
import ome.util.Utils;
import omero.model.OriginalFileI;
import omero.model.ScriptJob;
import omero.model.ScriptJobI;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

/**
 * Looks up an official script based on path for internal use.
 * 
 * The strategy here is to not only check for name, but also for the sha1 of the
 * file, meaning that if a new version is published, it will be used.
 *
 * Also provides {@link #createJob()} method in order to allow simple creation
 * of processors.
 *
 * @since Beta4.1
 */
public abstract class ScriptFinder {

    protected final Log log = LogFactory.getLog(getClass());

    private final Principal principal;

    private final Executor ex;

    private final Roles roles;

    /**
     * Source file to use for uploading.
     */
    private final File source;

    public ScriptFinder(Roles roles, String uuid, Executor executor, File source) {
        this(roles, new Principal(uuid, "system", "Internal"), executor, source);
    }

    public ScriptFinder(Roles roles, Principal principal, Executor executor, File source) {
        this.roles = roles;
        this.principal = principal;
        this.ex = executor;
        this.source = source;

        // to permit testing, we are exiting early if the source cannot
        // be found at all. any exception after this point, however, will
        // prevent the server from stopping properly.
        if (!this.source.exists()) {
            log.error("SERVER IMPROPERLY CONFIGURED - CANNOT FIND "
                    + source.getAbsolutePath());
            return;
        }

    }

    /**
     * Subclasses should override to return the name of the file under scripts/
     * which should be uploaded.
     */
    public abstract String getName();

    public OriginalFileI getFile() {
        OriginalFile file = null;
        try {
            final byte[] buf = FileUtils.readFileToByteArray(source);
            final String sha1 = Utils.bufferToSha1(buf);
            log.debug("Loading script: " + sha1);

            file = (OriginalFile) ex.execute(principal,
                    new Executor.SimpleWork(this, getName()) {
                        @Transactional(readOnly = false)
                        public Object doWork(Session session,
                                ServiceFactory sf) {
                            Parameters p = new Parameters();
                            p.addString("sha1", sha1);
                            p.addString("name", getName());
                            List<OriginalFile> files = loadScripts(sf, p);

                            if (files.size() < 1) {
                                return null;
                            } else {
                                if (files.size() > 1) {
                                    log.warn("Multiple scripts found: " + files);
                                }
                                return files.get(0);
                            }
                        }

                    });
        } catch (Exception e) {
            // pass in order to throw
            log.warn(e);
        }

        if (file == null) {
            throw new InternalException("Failed to find script");
        } else {
            return new OriginalFileI(file.getId(), false);
        }
    }

    /**
     * Load all scripts that match the name, sha1, and format for script
     */
    protected List<OriginalFile> loadScripts(ServiceFactory sf, Parameters p) {
        p.addLong("gid", roles.getUserGroupId());
        List<OriginalFile> files = sf.getQueryService().findAllByQuery(
                "select f from OriginalFile f where f.sha1 = :sha1 "
                        + "and f.details.group.id = :gid "
                        + "and f.name = :name "
                        + "and f.format.value = 'text/x-python'", p);
        return files;
    }

    /**
     * Returns a fresh (unsaved) {@link ScriptJob} which can be passed to
     * acquireProcessor for background processing.
     */
    public ScriptJob createJob() {
        ScriptJob job = new ScriptJobI();
        job.linkOriginalFile(getFile());
        job.setDescription(omero.rtypes.rstring(getName()));
        return job;
    }
}
