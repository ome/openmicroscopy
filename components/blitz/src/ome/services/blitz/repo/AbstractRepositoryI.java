/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.services.blitz.repo;

import java.io.File;
import java.nio.channels.OverlappingFileLockException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.springframework.context.ApplicationListener;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;
import Ice.ObjectAdapter;
import ome.services.blitz.fire.Registry;
import ome.services.messages.DeleteLogMessage;
import ome.services.messages.DeleteLogsMessage;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.util.SqlAction;
import ome.util.SqlAction.DeleteLog;
import ome.util.messages.InternalMessage;
import omero.ServerError;
import omero.api.RawFileStorePrx;
import omero.api.RawPixelsStorePrx;
import omero.api.RenderingEnginePrx;
import omero.api.ThumbnailStorePrx;
import omero.cmd.Response;
import omero.constants.SESSIONUUID;
import omero.grid.InternalRepositoryPrx;
import omero.grid.RawAccessRequest;
import omero.grid.RepositoryPrx;
import omero.grid.RepositoryPrxHelper;
import omero.grid._InternalRepositoryDisp;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.util.IceMapper;

/**
 * Base repository class responsible for properly handling directory
 * {@link #takeover() takeover} and other lifecycle tasks. Individual instances
 * will be responsible for providing the other service instances which are
 * returned from this service.
 *
 * @since Beta4.2
 */
public abstract class AbstractRepositoryI extends _InternalRepositoryDisp
    implements ApplicationListener<InternalMessage> {

    private final static Logger log = LoggerFactory.getLogger(AbstractRepositoryI.class);

    private final Ice.ObjectAdapter oa;

    private final Registry reg;

    private final Executor ex;

    private final Principal p;

    private final FileMaker fileMaker;

    private final PublicRepositoryI servant;

    private OriginalFile description;

    private RepositoryPrx proxy;

    private String repoUuid;

    private volatile AtomicReference<State> state = new AtomicReference<State>();

    private enum State {
        ACTIVE, EAGER, WAITING, CLOSED;
    }

    public AbstractRepositoryI(Ice.ObjectAdapter oa, Registry reg, Executor ex,
            Principal p, String repoDir, PublicRepositoryI servant) {
        this(oa, reg, ex, p, new FileMaker(repoDir), servant);
    }

    public AbstractRepositoryI(Ice.ObjectAdapter oa, Registry reg, Executor ex,
            Principal p, FileMaker fileMaker, PublicRepositoryI servant) {
        this.state.set(State.EAGER);
        this.p = p;
        this.oa = oa;
        this.ex = ex;
        this.reg = reg;
        this.fileMaker = fileMaker;
        this.servant = servant;
        log.info("Initializing repository in " + fileMaker.getDir());
    }

    /**
     * Called when this repository is creating a new {@link OriginalFile}
     * repository object.
     */
    public String generateRepoUuid() {
        return UUID.randomUUID().toString();
    }

    public void onApplicationEvent(InternalMessage im) {
        if (im instanceof DeleteLogMessage) {
            handleDLMs(Arrays.asList((DeleteLogMessage) im));
        } else if (im instanceof DeleteLogsMessage) {
            handleDLMs(((DeleteLogsMessage) im).getMessages());
        }
    }

    private void handleDLMs(List<DeleteLogMessage> dlms) {

        final Ice.Current rootCurrent = new Ice.Current();
        rootCurrent.ctx = new HashMap<String, String>();
        rootCurrent.ctx.put(SESSIONUUID.value, p.toString());
        final RepositoryDao dao = servant.repositoryDao;
        final List<DeleteLog> templates = new ArrayList<DeleteLog>();
        for (DeleteLogMessage dlm : dlms) {
            final DeleteLog template = new DeleteLog();
            template.repo = repoUuid; // Ourselves!
            template.fileId = dlm.getFileId();
            templates.add(template);
        }

        // Length matches that of dlms
        final List<List<DeleteLog>> logs = dao.findRepoDeleteLogs(templates,
                rootCurrent);

        final Map<DeleteLog, Integer> successes =
                new HashMap<DeleteLog, Integer>();

        for (int i = 0; i < dlms.size(); i++) {
            final DeleteLogMessage dlm = dlms.get(i);
            final List<DeleteLog> dls = logs.get(i);

            for (DeleteLog dl : dls) {
                // Copied from RawAccessRequestI.local
                String filename = dl.path + "/" + dl.name;
                if (filename.startsWith("/")) {
                    filename = "." + filename;
                }
                try {
                    final CheckedPath checked = servant.checkPath(filename, null, null /* i.e. as admin*/);
                    if (!checked.delete()) {
                        Throwable t = new omero.grid.FileDeleteException(
                                null, null, "Delete file failed: " + filename);
                        dlm.error(dl, t);
                    }
                } catch (Throwable t) {
                    log.warn("Failed to delete log " + dl, t);
                    dlm.error(dl, t);
                }
                if (!dlm.isError(dl)) {
                    successes.put(dl, i);
                }
            }
        }

        // Only remove the logs if req.local was successful
        List<DeleteLog> copies = new ArrayList<DeleteLog>(successes.keySet());
        List<Integer> counts = dao.deleteRepoDeleteLogs(copies, rootCurrent);
        for (int i = 0; i < copies.size(); i++) {
            DeleteLog copy = copies.get(i);
            Integer index = successes.get(copy);
            DeleteLogMessage dlm = dlms.get(index);
            int expected = logs.get(index).size();
            int actual = counts.get(i);
            if (actual != expected) {
                log.warn(String.format(
                    "Failed to remove all delete log entries: %s instead of %s",
                    actual, expected));
            }
            dlm.success(copy);
        }
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
            GetOrCreateRepo gorc = new GetOrCreateRepo(this);
            rv = ex.execute(p, gorc);
            if (rv instanceof ome.model.core.OriginalFile) {

                ome.model.core.OriginalFile r = (ome.model.core.OriginalFile) rv;
                description = getDescription(r.getId());
                proxy = gorc.publicPrx;

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

    public Response rawAccess(RawAccessRequest req, Current __current) throws ServerError {
        if (!(req instanceof RawAccessRequestI)) {
            return new omero.cmd.ERR();
        }
        try {
            ((RawAccessRequestI) req).local(this, servant, __current);
            return new omero.cmd.OK();
        } catch (Throwable t) {
            throw new IceMapper().handleServerError(t, servant.context);
        }
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

        RepositoryPrx publicPrx;

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

                String line = null;
                try {
                    line = fileMaker.getLine();
                } catch (OverlappingFileLockException ofle) {
                    InternalRepositoryPrx[] repos = reg.lookupRepositories();
                    InternalRepositoryPrx prx = null;
                    if (repos != null) {
                        for (int i = 0; i < repos.length; i++) {
                            if (repos[i] != null) {
                                if (repos[i].toString().contains(repoUuid)) {
                                    prx = repos[i];
                                }
                            }
                        }
                    }
                    if (prx == null) {
                        fileMaker.close();
                        FileMaker newFileMaker = new FileMaker(new File(
                                fileMaker.getDir()).getAbsolutePath());
                        fileMaker.init(sf.getConfigService().getDatabaseUuid());
                        line = newFileMaker.getLine();
                    }
                }

                if (line == null) {
                    repoUuid = repo.generateRepoUuid();
                } else {
                    repoUuid = line;
                }

                r = sf.getQueryService()
                .findByString(ome.model.core.OriginalFile.class,
                        "hash", repoUuid);

                final String path = FilenameUtils.normalize(
                        new File(fileMaker.getDir()).getAbsolutePath());
                final String pathName = FilenameUtils.getName(path);
                final String pathDir = FilenameUtils.getFullPath(path);
                if (r == null) {

                    if (line != null) {
                        log.warn("Couldn't find repository object: " + line);
                    }

                    r = new ome.model.core.OriginalFile();
                    r.setHash(repoUuid);
                    r.setName(pathName);
                    r.setPath(pathDir);
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
                } else if (!r.getPath().equals(pathDir) ||
                        !r.getName().equals(pathName)) {
                    final String oldPath = r.getPath();
                    final String oldName = r.getName();
                    r.setPath(pathDir);
                    r.setName(pathName);
                    r = sf.getUpdateService().saveAndReturnObject(r);
                    log.warn("Data directory moved: {}{} updated to {}{}",
                            oldPath, oldName, pathDir, pathName);
                }

                // ticket:1794 - only adds if necessary
                sf.getAdminService().moveToCommonSpace(r);


                log.info(String.format("Opened repository %s (uuid=%s)", r
                        .getName(), repoUuid));

                //
                // Servants
                //

                servant.initialize(fileMaker, r.getId(), repoUuid);

                LinkedList<Ice.ObjectPrx> objs = new LinkedList<Ice.ObjectPrx>();
                objs.add(addOrReplace("InternalRepository-", repo));
                objs.add(addOrReplace("PublicRepository-", servant.tie()));
                publicPrx = RepositoryPrxHelper.uncheckedCast(objs.getLast());

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
