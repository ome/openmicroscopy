/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import java.io.File;
import java.sql.Timestamp;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import ome.conditions.InternalException;
import ome.model.enums.Format;
import ome.model.internal.Permissions;
import ome.model.internal.Permissions.Right;
import ome.model.internal.Permissions.Role;
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
import omero.util.IceMapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.jdbc.core.simple.SimpleJdbcOperations;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;
import Ice.ObjectAdapter;

/**
 * Base repository class responsible for properly handling directory
 * {@link #takeover() takeover} and other lifecycle tasks. Individual instances
 * will be responsible for providing the other service instances which are
 * returned from this service.
 * 
 * @since Beta4.2
 */
public abstract class AbstractRepositoryI extends _InternalRepositoryDisp {

    private final static Log log = LogFactory.getLog(AbstractRepositoryI.class);

    private final Ice.ObjectAdapter oa;

    private final Registry reg;

    private final Executor ex;

    private final Principal p;

    private final FileMaker fileMaker;

    private OriginalFile description;

    private RepositoryPrx proxy;

    private String repoUuid;

    private volatile AtomicReference<State> state = new AtomicReference<State>();

    private enum State {
        ACTIVE, EAGER, WAITING, CLOSED;
    }

    public AbstractRepositoryI(Ice.ObjectAdapter oa, Registry reg, Executor ex,
            String sessionUuid, String repoDir) {
        this(oa, reg, ex, sessionUuid, new FileMaker(repoDir));
    }

    public AbstractRepositoryI(Ice.ObjectAdapter oa, Registry reg, Executor ex,
            String sessionUuid, FileMaker fileMaker) {
        this.state.set(State.EAGER);
        this.p = new Principal(sessionUuid, "system", "Internal");
        this.oa = oa;
        this.ex = ex;
        this.reg = reg;
        this.fileMaker = fileMaker;
        log.info("Initializing repository in " + fileMaker.getDir());
    }

    /**
     * Method called in a background thread which may end up waiting
     * indefinitely on the repository lock file
     * ("${omero.data.dir}/.omero/repository/${omero.db.uuid}/repo_uuid").
     */
    public boolean takeover() {

        if (!state.compareAndSet(State.EAGER, State.WAITING)) {
            log.debug("Skipping takeover");
            return false;
        }

        // All code paths after this point should guarantee that they set
        // the state to the proper code, since now no other thread can get
        // into this method.

        Object rv = null;
        try {
            rv = ex.execute(p, new GetOrCreateRepo(this));
            if (rv instanceof ome.model.core.OriginalFile) {

                ome.model.core.OriginalFile r = (ome.model.core.OriginalFile) rv;
                description = getDescription(r.getId());

                // Success
                if (!state.compareAndSet(State.WAITING, State.ACTIVE)) {
                    // But this may have been set to CLOSED
                    log.debug("Could not set state to ACTIVE");
                }
                return true;

            } else if (rv instanceof Exception) {
                log.error("Failed during repository takeover", (Exception) rv);
            } else {
                log.error("Unknown issue with repository takeover:" + rv);
            }
        } catch (Exception e) {
            log.error("Unexpected error in called executor on takeover", e);
        }

        state.compareAndSet(State.WAITING, State.EAGER);
        return false;

    }

    public void close() {
        state.set(State.CLOSED);
        log.info("Releasing " + fileMaker.getDir());
        fileMaker.close();
    }

    public final String getRepoUuid() {
        return repoUuid;
    }

    public final Ice.Communicator getCommunicator() {
        return oa.getCommunicator();
    }

    public final ObjectAdapter getObjectAdapter() {
        return oa;
    }

    public final OriginalFile getDescription(Current __current) {
        return description;
    }

    public final RepositoryPrx getProxy(Current __current) {
        return proxy;
    }

    public abstract String getFilePath(final OriginalFile file,
            Current __current) throws ServerError;

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
     * {@link AbstractRepositoryI#takeover()} method, but doesn't deal with the
     * atomic locking of {@link AbstractRepositoryI#state} nor error handling.
     * Instead it simple returns an {@link Exception} ("failure") or null
     * ("success").
     */
    class GetOrCreateRepo extends Executor.SimpleWork {

        private final AbstractRepositoryI repo;

        public GetOrCreateRepo(AbstractRepositoryI repo) {
            super(repo, "takeover");
            this.repo = repo;
        }

        @Transactional(readOnly = false)
        public Object doWork(Session session, ServiceFactory sf) {

            ome.model.core.OriginalFile r = null;

            try {

                if (fileMaker.needsInit()) {
                    fileMaker.init(sf.getConfigService().getDatabaseUuid());
                }

                String line = fileMaker.getLine();

                if (line == null) {
                    repoUuid = UUID.randomUUID().toString();
                    r = new ome.model.core.OriginalFile();
                    r.setSha1(repoUuid);
                    r.setName(fileMaker.getDir());
                    r.setPath("/");
                    Timestamp t = new Timestamp(System.currentTimeMillis());
                    r.setAtime(t);
                    r.setMtime(t);
                    r.setCtime(t);
                    r.setFormat(new Format("Repository"));
                    r.setSize(0L);
                    r.getDetails().setPermissions(Permissions.WORLD_IMMUTABLE);
                    r = sf.getUpdateService().saveAndReturnObject(r);
                    fileMaker.writeLine(repoUuid);
                    log.info(String.format(
                            "Registered new repository %s (uuid=%s)", r
                                    .getName(), repoUuid));
                } else {
                    repoUuid = line;
                    r = sf.getQueryService()
                            .findByString(ome.model.core.OriginalFile.class,
                                    "sha1", repoUuid);
                    if (r == null) {
                        throw new InternalException(
                                "Can't find repository object: " + line);
                    } else {
                        if (!r.getDetails().getPermissions().isGranted(
                                Role.WORLD, Right.READ)) {
                            // TODO: See changes to SharedResources. The current
                            // repository usage is at odds to the security
                            // system and needs to be reviewed
                            log.warn("Making repository readable...");
                            r.getDetails().setPermissions(
                                    Permissions.WORLD_IMMUTABLE);
                            sf.getUpdateService().saveObject(r);
                        }
                    }
                    log.info(String.format("Opened repository %s (uuid=%s)", r
                            .getName(), repoUuid));
                }

                //
                // Servants
                //

                PublicRepositoryI pr = new PublicRepositoryI(new File(fileMaker
                        .getDir()), r.getId(), ex, p);

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
                return r;
            } catch (Exception e) {
                fileMaker.close(); // If anything goes awry, we release for
                // others!
                return e;
            }

        }

    }

    protected OriginalFileI getDescription(final long id) throws ServerError {
        ome.model.core.OriginalFile file = (ome.model.core.OriginalFile) ex
                .execute(p,
                        new Executor.SimpleWork(this, "getDescription", id) {
                            @Transactional(readOnly = true)
                            public Object doWork(Session session,
                                    ServiceFactory sf) {
                                return sf.getQueryService().findByQuery(
                                        "select o from OriginalFile o "
                                                + "join fetch o.format "
                                                + "where o.id = " + id, null);
                            }
                        });
        OriginalFileI rv = (OriginalFileI) new IceMapper().map(file);
        return rv;

    }

    protected String getFileUrl(final OriginalFile file) throws ServerError {

        if (file == null || file.getId() == null) {
            throw new omero.ValidationException(null, null, "Unmanaged file");
        }

        String url = (String) ex
                .executeStateless(new Executor.SimpleStatelessWork(this,
                        "getFileUrl") {
                    @Transactional(readOnly = true)
                    public Object doWork(SimpleJdbcOperations jdbc) {
                        return jdbc.queryForObject(
                                "select url from originalfile "
                                        + "where id = ?", String.class, file
                                        .getId().getValue());
                    }
                });

        return url;
    }

}
