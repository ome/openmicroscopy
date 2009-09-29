/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.roi;

import java.io.File;
import java.sql.Timestamp;
import java.util.List;

import ome.api.RawFileStore;
import ome.model.core.OriginalFile;
import ome.model.enums.Format;
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
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.transaction.annotation.Transactional;

/**
 * Start-up task which guarantees that lib/python/populateroi.py is added as a
 * script to the server. Then, users like MetadataStoreI who would like to run
 * populateroi.py scripts, can use {@link #createJob()}
 * 
 * The strategy here is to not only check for name, but also for the sha1 of the
 * file, meaning that if a new version is published, it will be used.
 * 
 * @since Beta4.1
 */
public class PopulateRoiJob {

    protected final Log log = LogFactory.getLog(PopulateRoiJob.class);

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
     * Unloaded copy of the original file.
     */
    private/* final */OriginalFile file = null;

    private static File production() {
        File cwd = new File(".");
        File lib = new File(cwd, "lib");
        File py = new File(lib, "python");
        File populate = new File(py, "populateroi.py");
        return populate;
    }

    public PopulateRoiJob(String uuid, Executor executor) {
        this(uuid, executor, production());
    }

    public PopulateRoiJob(String uuid, Executor executor, File source) {
        this(new Principal(uuid, "system", "Internal"), executor, source);
    }

    public PopulateRoiJob(Principal principal, Executor executor, File source) {
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

    public void init() {

        synchronized (mutex) {

            if (file != null) {
                return; // Already set
            }

            try {
                final byte[] buf = FileUtils.readFileToByteArray(source);
                final String sha1 = Utils.bufferToSha1(buf);
                log.info("populateroi.py found: " + sha1);

                file = (OriginalFile) ex.execute(principal,
                        new Executor.SimpleWork(this, "populateroi.py") {
                            @Transactional(readOnly = false)
                            public Object doWork(Session session,
                                    ServiceFactory sf) {
                                Parameters p = new Parameters().addString(
                                        "sha1", sha1);
                                List<OriginalFile> files = loadScripts(sf, p);

                                if (files.size() < 1) {
                                    log.info("Adding populateroi.py");
                                    return createScript(buf, sha1, sf);
                                } else {
                                    if (files.size() > 1) {
                                        log
                                                .warn("Multiple populateroi.py scripts found");
                                    }
                                    return files.get(0);
                                }
                            }

                        });

            } catch (Exception e) {
                throw new RuntimeException("Failed to register populateroi.py",
                        e);
            }

        }

    }

    /**
     * Load all scripts that match the name, sha1, and format for populateroi.py
     */
    protected List<OriginalFile> loadScripts(ServiceFactory sf, Parameters p) {
        List<OriginalFile> files = sf.getQueryService().findAllByQuery(
                "select f from OriginalFile f where f.sha1 = :sha1 "
                        + "and f.name = 'populateroi.py' "
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
        file.setName("populateroi.py");
        file.setSha1(sha1);
        Timestamp t = new Timestamp(System.currentTimeMillis());
        file.setAtime(t);
        file.setCtime(t);
        file.setMtime(t);
        file.setPath("lib/python/populateroi.py");
        file.setSize(Long.valueOf(buf.length));
        file.setFormat(new Format("text/x-python"));
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
        job.setDescription(omero.rtypes.rstring("populateroi.py"));
        return job;
    }
}
