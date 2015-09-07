/*
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
import ome.util.checksum.ChecksumProviderFactory;
import ome.util.checksum.ChecksumProviderFactoryImpl;
import ome.util.checksum.ChecksumType;
import omero.model.OriginalFileI;
import omero.model.ScriptJob;
import omero.model.ScriptJobI;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    protected final Logger log = LoggerFactory.getLogger(getClass());

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

    public OriginalFileI getFile(ServiceFactory sf) {
        OriginalFile file = null;
        ChecksumProviderFactory cpf = new ChecksumProviderFactoryImpl();
        try {
            final byte[] buf = FileUtils.readFileToByteArray(source);
            final String sha1 = cpf.getProvider(ChecksumType.SHA1)
                    .putBytes(buf).checksumAsString();
            log.debug("Loading script: " + sha1);

            Parameters p = new Parameters();
            p.addString("hash", sha1);
            p.addString("name", getName());
            List<OriginalFile> files = loadScripts(sf, p);

            if (files.size() < 1) {
                return null;
            } else {
                if (files.size() > 1) {
                    log.warn("Multiple scripts found: " + files);
                }
                file = files.get(0);
            }

        } catch (Exception e) {
            // pass in order to throw
            log.warn(e.toString()); // slf4j migration: toString()
        }

        if (file == null) {
            throw new InternalException("Failed to find script");
        } else {
            return new OriginalFileI(file.getId(), false);
        }
    }

    /**
     * Load all scripts that match the name, hash, and format for script
     */
    protected List<OriginalFile> loadScripts(ServiceFactory sf, Parameters p) {
        p.addLong("gid", roles.getUserGroupId());
        List<OriginalFile> files = sf.getQueryService().findAllByQuery(
                "select f from OriginalFile f where f.hash = :hash "
                        + "and f.details.group.id = :gid "
                        + "and f.name = :name "
                        + "and f.mimetype = 'text/x-python'", p);
        return files;
    }

    /**
     * Returns a fresh (unsaved) {@link ScriptJob} which can be passed to
     * acquireProcessor for background processing.
     */
    public ScriptJob createJob(ServiceFactory sf) {
        ScriptJob job = new ScriptJobI();
        job.linkOriginalFile(getFile(sf));
        job.setDescription(omero.rtypes.rstring(getName()));
        return job;
    }
}
