/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import ome.services.blitz.fire.Registry;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.util.SqlAction;
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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
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

    private final SqlAction sql;

    private final FileMaker fileMaker;

    private OriginalFile description;

    private RepositoryPrx proxy;

    private String repoUuid;

    private String template = "";

    private volatile AtomicReference<State> state = new AtomicReference<State>();

    private enum State {
        ACTIVE, EAGER, WAITING, CLOSED;
    }

    public AbstractRepositoryI(Ice.ObjectAdapter oa, Registry reg, Executor ex,
            SqlAction sql, String sessionUuid, String repoDir) {
        this(oa, reg, ex, sql, sessionUuid, new FileMaker(repoDir));
    }

    public AbstractRepositoryI(Ice.ObjectAdapter oa, Registry reg, Executor ex,
            SqlAction sql, String sessionUuid, String repoDir, String template) {
        this(oa, reg, ex, sql, sessionUuid, new FileMaker(repoDir), template);
    }

    public AbstractRepositoryI(Ice.ObjectAdapter oa, Registry reg, Executor ex,
            SqlAction sql, String sessionUuid, FileMaker fileMaker, String template) {
        this(oa, reg, ex, sql, sessionUuid, fileMaker);
        this.template = template;
    }

    public AbstractRepositoryI(Ice.ObjectAdapter oa, Registry reg, Executor ex,
            SqlAction sql, String sessionUuid, FileMaker fileMaker) {
        this.state.set(State.EAGER);
        this.p = new Principal(sessionUuid, "system", "Internal");
        this.oa = oa;
        this.ex = ex;
        this.reg = reg;
        this.sql = sql;
        this.fileMaker = fileMaker;
        log.info("Initializing repository in " + fileMaker.getDir());
    }

    /**
     * Called when this repository is creating a new {@link OriginalFile}
     * repository object.
     */
    public String generateRepoUuid() {
        return UUID.randomUUID().toString();
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

                final String line = fileMaker.getLine();

                if (line == null) {
                    repoUuid = repo.generateRepoUuid();
                } else {
                    repoUuid = line;
                }

                r = sf.getQueryService()
                .findByString(ome.model.core.OriginalFile.class,
                        "sha1", repoUuid);

                if (r == null) {

                    if (line != null) {
                        log.warn("Couldn't find repository object: " + line);
                    }

                    String path = FilenameUtils.normalize(new File(fileMaker.getDir()).getAbsolutePath());
                    r = new ome.model.core.OriginalFile();
                    r.setSha1(repoUuid);
                    r.setName(FilenameUtils.getName(path));
                    r.setPath(FilenameUtils.getFullPath(path));
                    Timestamp t = new Timestamp(System.currentTimeMillis());
                    r.setAtime(t);
                    r.setMtime(t);
                    r.setCtime(t);
                    r.setMimetype("Repository"); // ticket:2211
                    r.setSize(0L);
                    r = sf.getUpdateService().saveAndReturnObject(r);
                    // ticket:1794
                    sf.getAdminService().moveToCommonSpace(r);
                    fileMaker.writeLine(repoUuid);
                    log.info(String.format(
                            "Registered new repository %s (uuid=%s)", r
                                    .getName(), repoUuid));
                }

                // ticket:1794 - only adds if necessary
                sf.getAdminService().moveToCommonSpace(r);


                log.info(String.format("Opened repository %s (uuid=%s)", r
                        .getName(), repoUuid));

                //
                // Servants
                //

                PublicRepositoryI pr = new PublicRepositoryI(new File(fileMaker
                        .getDir()), r.getId(), ex, sql, p);
                ManagedRepositoryI mr = null;
                if (template != null) {
                    mr = new ManagedRepositoryI(new File(fileMaker
                        .getDir()), template, r.getId(), ex, sql, p);
                }

                LinkedList<Ice.ObjectPrx> objs = new LinkedList<Ice.ObjectPrx>();
                objs.add(addOrReplace("InternalRepository-", repo));
                objs.add(addOrReplace("PublicRepository-", pr));
                proxy = RepositoryPrxHelper.uncheckedCast(objs.getLast());
                if (mr != null) {
                    objs.add(addOrReplace("ManagedRepository-", pr));
                }

                //
                // Activation & Registration
                //
                oa.activate(); // Must happen before the registry tries to connect

                for (Ice.ObjectPrx prx : objs) {
                    reg.addObject(prx);
                }

                log.info("Repository now active");
                return r;
            } catch (Exception e) {
                fileMaker.close(); // If anything goes awry, we release for
                // others!
                return e;
            }

        }

        private Ice.ObjectPrx addOrReplace(String prefix, Ice.Object obj) {
            Ice.Identity id = Ice.Util.stringToIdentity(prefix + repoUuid);
            Object old = oa.find(id);
            if (old != null) {
                oa.remove(id);
                log.warn(String.format("Found %s; removing: %s", id, old));
            }
            oa.add(obj, id);
            return oa.createDirectProxy(id);
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
                                                + "where o.id = " + id, null);
                            }
                        });
        OriginalFileI rv = (OriginalFileI) new IceMapper().map(file);
        return rv;

    }

    @SuppressWarnings("unchecked")
    protected String getFileRepo(final OriginalFile file) throws ServerError {

        if (file == null || file.getId() == null) {
            throw new omero.ValidationException(null, null, "Unmanaged file");
        }

        Map<String, Object> map = (Map<String, Object>) ex
                .executeSql(new Executor.SimpleSqlWork(this,
                        "getFileRepo") {
                    @Transactional(readOnly = true)
                    public Object doWork(SqlAction sql) {
                        return sql.repoFile(file.getId().getValue());
                    }
                });

        if (map.size() == 0) {
            throw new omero.ValidationException(null, null, "Unknown file: "
                    + file.getId().getValue());
        }

        return (String) map.get("repo");

    }

}
