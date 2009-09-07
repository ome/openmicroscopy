/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.UUID;

import ome.services.blitz.fire.Registry;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;
import omero.api.RawFileStorePrx;
import omero.api.RawPixelsStorePrx;
import omero.api.RenderingEnginePrx;
import omero.api.ThumbnailStorePrx;
import omero.grid.RepositoryPrx;
import omero.grid.RepositoryPrxHelper;
import omero.grid._InternalRepositoryDisp;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;
import Ice.ObjectAdapter;

/**
 * 
 * @since Beta4.1
 * @DEV.TODO This could be rewritten to use the Executr
 */
public class LegacyRepositoryI extends _InternalRepositoryDisp {

    private final static Log log = LogFactory.getLog(LegacyRepositoryI.class);

    private final Ice.ObjectAdapter oa;

    private final Registry reg;

    private final Executor ex;

    private final Principal p;

    private final String repoDir;

    private OriginalFile description;

    private RepositoryPrx proxy;

    private File repoLock;

    private RandomAccessFile raf;

    private FileChannel channel;

    private FileLock lock;

    private String dbUuid;

    private String repoUuid;

    private volatile State state;

    private enum State {
        ACTIVE, WAITING, CLOSED;
    }

    public LegacyRepositoryI(Ice.ObjectAdapter oa, Registry reg, Executor ex,
            String sessionUuid, String repoDir) {
        this.state = State.WAITING;
        this.p = new Principal(sessionUuid, "system", "Internal");
        this.oa = oa;
        this.ex = ex;
        this.reg = reg;
        this.repoDir = repoDir;
        log.info("Initializing repository in " + repoDir);

    }

    /**
     * Method called in a background thread which may end up waiting indefnitely
     * on the repository lock file
     * ("${omero.data.dir}/.omero/repository/${omero.db.uuid}/repo_uuid")
     */
    public boolean takeover() {

        if (!state.equals(State.WAITING)) {
            log.debug("Skipping takeover");
            return false;
        }

        try {

            ex.execute(p, new Executor.SimpleWork(this, "takeover") {

                @Transactional(readOnly = false)
                public Object doWork(Session session, ServiceFactory sf) {

                    dbUuid = sf.getConfigService().getDatabaseUuid();
                    File mountDir = new File(repoDir);
                    File omeroDir = new File(mountDir, ".omero");
                    File repoCfg = new File(omeroDir, "repository");
                    File uuidDir = new File(repoCfg, dbUuid);
                    if (!uuidDir.exists()) {
                        uuidDir.mkdirs();
                        log.info("Creating " + uuidDir);
                    }

                    try {
                        repoLock = new File(uuidDir, "repo_uuid");
                        raf = new RandomAccessFile(repoLock, "rw");
                        channel = raf.getChannel();
                        lock = channel.lock();
                        String line = null;
                        try {
                            raf.seek(0);
                            line = raf.readUTF();
                        } catch (EOFException eof) {
                            // pass
                        }

                        ome.model.core.OriginalFile r = null;
                        if (line == null || line.trim().equals("")) {
                            repoUuid = UUID.randomUUID().toString();
                            raf.seek(0);
                            raf.writeUTF(repoUuid);
                            r = new ome.model.core.OriginalFile();
                            r.setSha1(repoUuid);
                            r.setName(repoDir);
                            r.setType("legacy");
                            r = sf.getUpdateService().saveAndReturnObject(r);
                            log.info("Registered new repository: " + repoUuid);
                        } else {
                            repoUuid = line.trim();
                            r = sf.getQueryService().findByString(
                                    ome.model.core.OriginalFile.class, "sha1",
                                    repoUuid);
                            log.info("Opened repository: " + repoUuid);
                        }
                        if (r != null) {
                            description = new OriginalFileI(r.getId(), false);
                        }

                    } catch (IOException io) {
                        throw new RuntimeException("IO Exception", io);
                    }
                    
                    return null;
                    
                }
            });

            //
            // Servants
            //

            PublicRepositoryI pr = new PublicRepositoryI(description.getId()
                    .getValue(), ex, p);

            Ice.Identity internal = Ice.Util
                    .stringToIdentity("InternalRepository-" + repoUuid);
            Ice.Identity external = Ice.Util
                    .stringToIdentity("PublicRepository-" + repoUuid);

            Ice.ObjectPrx internalObj = oa.add(this, internal);
            Ice.ObjectPrx externalObj = oa.add(pr, external);

            reg.addObject(internalObj);
            reg.addObject(externalObj);

            proxy = RepositoryPrxHelper.uncheckedCast(externalObj);

            //
            // Activation & Registration
            //
            oa.activate();

            state = State.ACTIVE;
            log.info("Repository now active");

        } catch (Exception e) {
            log.error("Failed during repository takeover", e);
            throw new RuntimeException(e);
        }

        return true;

    }

    public void close() {

        state = State.CLOSED;

        log.info("Releasing " + repoLock.getAbsolutePath());
        try {
            lock.release();
        } catch (IOException e) {
            log.warn("Failed to release lock");
        }
        try {
            raf.close();
        } catch (IOException e) {
            log.warn("Failed to close RandomAccessFile");
        }
    }

    public Ice.Communicator getCommunicator() {
        return oa.getCommunicator();
    }

    public ObjectAdapter getObjectAdapter() {
        return oa;
    }

    public OriginalFile getDescription(Current __current) {
        return description;
    }

    public RepositoryPrx getProxy(Current __current) {
        return proxy;
    }

    public RawFileStorePrx createRawFileStore(OriginalFile file,
            Current __current) {
        return null;
    }

    public RawPixelsStorePrx createRawPixelsStore(OriginalFile file,
            Current __current) {
        // TODO Auto-generated method stub
        return null;
    }

    public RenderingEnginePrx createRenderingEngine(OriginalFile file,
            Current __current) {
        // TODO Auto-generated method stub
        return null;
    }

    public ThumbnailStorePrx createThumbnailStore(OriginalFile file,
            Current __current) {
        // TODO Auto-generated method stub
        return null;
    }

}