/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import ome.conditions.InternalException;
import ome.io.nio.OriginalFilesService;
import ome.model.enums.Format;
import ome.services.blitz.fire.Registry;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;
import omero.ServerError;
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
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;
import Ice.ObjectAdapter;

/**
 * 
 * @since Beta4.1
 */
public class LegacyRepositoryI extends _InternalRepositoryDisp {

    private final static Log log = LogFactory.getLog(LegacyRepositoryI.class);

    private final Ice.ObjectAdapter oa;

    private final Registry reg;

    private final Executor ex;

    private final Principal p;

    private final OriginalFilesService fs;

    private final FileMaker fileMaker;

    private OriginalFile description;

    private RepositoryPrx proxy;

    private String repoUuid;

    private volatile AtomicReference<State> state = new AtomicReference<State>();

    private enum State {
        ACTIVE, EAGER, WAITING, CLOSED;
    }

    public LegacyRepositoryI(Ice.ObjectAdapter oa, Registry reg, Executor ex,
            String sessionUuid, String repoDir) {
        this(oa, reg, ex, sessionUuid, new FileMaker(repoDir));
    }

    public LegacyRepositoryI(Ice.ObjectAdapter oa, Registry reg, Executor ex,
            String sessionUuid, FileMaker fileMaker) {

        this.state.set(State.EAGER);
        this.p = new Principal(sessionUuid, "system", "Internal");
        this.oa = oa;
        this.ex = ex;
        this.reg = reg;
        this.fileMaker = fileMaker;
        this.fs = new OriginalFilesService(fileMaker.getDir());
        log.info("Initializing repository in " + fileMaker.getDir());
    }

    /**
     * Method called in a background thread which may end up waiting
     * indefinitely on the repository lock file
     * ("${omero.data.dir}/.omero/repository/${omero.db.uuid}/repo_uuid").
     */
    public boolean takeover() {

        if (!state.compareAndSet(State.EAGER, State.WAITING)) {
            log.info("Skipping takeover");
            return false;
        }

        // All code paths after this point should guarantee that they set
        // the state to the proper code, since now no other thread can get
        // into this method.

        Object rv = null;
        try {
            rv = ex.execute(p, new GetOrCreateRepo(this));
            if (rv == null) {
                // Success
                if (!state.compareAndSet(State.WAITING, State.ACTIVE)) {
                    // But this may have been set to CLOSED
                    log.debug("Could not set state to ACTIVE");
                }
                return true;
            }
        } catch (Exception e) {
            log.error("Unexpected error in called executor", e);
        }

        log.error("Failed during repository takeover", (Exception) rv);
        state.compareAndSet(State.WAITING, State.EAGER);
        return false;

    }

    public void close() {
        state.set(State.CLOSED);
        log.info("Releasing " + fileMaker.getDir());
        fileMaker.close();
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

    /**
     * @DEV.TODO CACHING
     */
    public String getFilePath(final OriginalFile file, Current __current)
            throws ServerError {

        if (file == null || file.getId() == null) {
            throw new omero.ValidationException(null, null, "Unmanaged file");
        }

        Integer count = (Integer) ex
                .executeStateless(new Executor.SimpleStatelessWork(this,
                        "getFileUrl") {
                    @Transactional(readOnly = true)
                    public Object doWork(SimpleJdbcOperations jdbc) {
                        return jdbc.queryForInt(
                                "select count(*) from originalfile "
                                        + "where id = ? and url is null", file
                                        .getId().getValue());
                    }
                });

        if (count.intValue() == 0) {
            throw new omero.ValidationException(null, null,
                    "Does not belong to this repository");
        }

        return fs.getFilesPath(file.getId().getValue());

    }

    // UNIMPLEMENTED
    // =========================================================================

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

    // Helpers
    // =========================================================================

    /**
     * Action class for either looking up the repository for this instance, or
     * if it doesn't exist, creating it. This is the bulk of the logic for the
     * {@link LegacyRepositoryI#takeover()} method, but doesn't deal with the
     * atomic locking of {@link LegacyRepositoryI#state} nor error handling.
     * Instead it simple returns an {@link Exception} ("failure") or null
     * ("success").
     */
    class GetOrCreateRepo extends Executor.SimpleWork {

        LegacyRepositoryI repo;

        public GetOrCreateRepo(LegacyRepositoryI repo) {
            super(repo, "takeover");
            this.repo = repo;
        }

        @Transactional(readOnly = false)
        public Object doWork(Session session, ServiceFactory sf) {

            try {

                if (fileMaker.needsInit()) {
                    fileMaker.init(sf.getConfigService().getDatabaseUuid());
                }

                String line = fileMaker.getLine();

                if (line == null) {
                    repoUuid = UUID.randomUUID().toString();
                    ome.model.core.OriginalFile r = new ome.model.core.OriginalFile();
                    r.setSha1(repoUuid);
                    r.setName(fileMaker.getDir());
                    r.setPath("/");
                    Timestamp t = new Timestamp(System.currentTimeMillis());
                    r.setAtime(t);
                    r.setMtime(t);
                    r.setCtime(t);
                    r.setFormat(new Format("Directory"));
                    r.setSize(0L);
                    r = sf.getUpdateService().saveAndReturnObject(r);
                    description = new OriginalFileI(r.getId(), false);
                    fileMaker.writeLine(repoUuid);
                    log.info("Registered new repository: " + repoUuid);
                } else {
                    repoUuid = line;
                    ome.model.core.OriginalFile r = sf.getQueryService()
                            .findByString(ome.model.core.OriginalFile.class,
                                    "sha1", repoUuid);
                    if (r == null) {
                        throw new InternalException(
                                "Can't find repository object: " + line);
                    }
                    description = new OriginalFileI(r.getId(), false);
                    log.info("Opened repository: " + repoUuid);
                }

                //
                // Servants
                //

                PublicRepositoryI pr = new PublicRepositoryI(description
                        .getId().getValue(), ex, p);

                Ice.Identity internal = Ice.Util
                        .stringToIdentity("InternalRepository-" + repoUuid);
                Ice.Identity external = Ice.Util
                        .stringToIdentity("PublicRepository-" + repoUuid);

                Ice.ObjectPrx internalObj = oa.add(repo, internal);
                Ice.ObjectPrx externalObj = oa.add(pr, external);

                reg.addObject(internalObj);
                reg.addObject(externalObj);

                proxy = RepositoryPrxHelper.uncheckedCast(externalObj);

                //
                // Activation & Registration
                //
                oa.activate();
                log.info("Repository now active");
                return null;
            } catch (Exception e) {
                return e;
            }

        }
    }

}
