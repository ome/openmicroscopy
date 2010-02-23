/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.scripts;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;

import ome.api.RawFileStore;
import ome.model.core.OriginalFile;
import ome.model.enums.Format;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
import ome.parameters.Parameters;
import ome.services.util.Executor;
import ome.system.Principal;
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
 * Start-up task which guarantees a script is added as a script to the server.
 * The strategy here is to not only check for name, but also for the sha1 of the
 * file, meaning that if a new version is published, it will be used.
 *
 * Also provides {@link #createJob()} method in order to allow simple creation
 * of processors.
 *
 * @since Beta4.1
 */
public abstract class ScriptUploader {

    protected final Log log = LogFactory.getLog(getClass());

    private final Principal principal;

    private final Executor ex;

    /**
     * Source file to use for uploading.
     */
    private final File source;

    /**
     * Lock for updating {@link #file}
     */
    private final Object[] mutex = new Object[0];

    /**
     * Unloaded copy of the original file to be uploaded.
     */
    private/* final */OriginalFile file = null;

    public ScriptUploader(String uuid, Executor executor, File source) {
        this(new Principal(uuid, "system", "Internal"), executor, source);
    }

    public ScriptUploader(Principal principal, Executor executor, File source) {
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

        init();

    }

    /**
     * Subclasses should override to return the name of the file under scripts/
     * which should be uploaded.
     */
    public abstract String getName();

    public void init() {

        synchronized (mutex) {

            if (file != null) {
                return; // Already set
            }

            try {
                final byte[] buf = FileUtils.readFileToByteArray(source);
                final String sha1 = Utils.bufferToSha1(buf);
                log.info("Script found: " + sha1);

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
                                    log.info("Creating script");
                                    return createScript(buf, sha1, sf);
                                } else {
                                    if (files.size() > 1) {
                                        log.warn("Multiple scripts found");
                                    }
                                    return files.get(0);
                                }
                            }

                        });

                if (!file.getDetails().getPermissions().isGranted(Role.WORLD,
                        Right.READ)) {
                    log.warn("Making script readable...");
                    ex.execute(principal, new Executor.SimpleWork(this,
                            "chmodScript") {
                        @Transactional(readOnly = false)
                        public Object doWork(Session session, ServiceFactory sf) {
                            sf.getAdminService().changePermissions(file,
                                    Permissions.WORLD_IMMUTABLE);
                            return null;
                        }
                    });
                }

            } catch (Exception e) {
                throw new RuntimeException("Failed to register script", e);
            }

        }

    }

    /**
     * Load all scripts that match the name, sha1, and format for script
     */
    protected List<OriginalFile> loadScripts(ServiceFactory sf, Parameters p) {
        List<OriginalFile> files = sf.getQueryService().findAllByQuery(
                "select f from OriginalFile f where f.sha1 = :sha1 "
                        + "and f.name = :name "
                        + "and f.format.value = 'text/x-python'", p);
        return files;
    }

    /**
     * Create the original file and upload the bytes we've already read into
     * memory.
     */
    protected OriginalFile createScript(final byte[] buf, final String sha1,
            ServiceFactory sf) {
        OriginalFile file = new OriginalFile();
        file.setName(getName());
        file.setSha1(sha1);
        Timestamp t = new Timestamp(System.currentTimeMillis());
        file.setAtime(t);
        file.setCtime(t);
        file.setMtime(t);
        file.setPath("lib/python/" + getName());
        file.setSize(Long.valueOf(buf.length));
        file.setFormat(new Format("text/x-python"));
        // ticket:1794 - currently perms == group.perms only!
        // file.getDetails().setPermissions(Permissions.WORLD_IMMUTABLE);
        file = sf.getUpdateService().saveAndReturnObject(file);

        RawFileStore rfs = sf.createRawFileStore();
        rfs.setFileId(file.getId());
        rfs.write(buf, 0, buf.length);
        rfs.close();
        return file;
    }

    /**
     * Returns a fresh (unsaved) {@link ScriptJob} which can be passed to
     * acquireProcessor for background processing.
     */
    public ScriptJob createJob() {
        ScriptJob job = new ScriptJobI();
        job.linkOriginalFile(new OriginalFileI(file.getId(), false));
        job.setDescription(omero.rtypes.rstring(getName()));
        return job;
    }
}
