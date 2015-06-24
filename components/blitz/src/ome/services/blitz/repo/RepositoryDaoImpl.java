package ome.services.blitz.repo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.api.IQuery;
import ome.api.JobHandle;
import ome.api.RawFileStore;
import ome.api.local.LocalAdmin;
import ome.conditions.InternalException;
import ome.io.nio.FileBuffer;
import ome.model.IObject;
import ome.model.fs.FilesetJobLink;
import ome.model.meta.Experimenter;
import ome.parameters.Parameters;
import ome.services.RawFileBean;
import ome.services.blitz.repo.path.FsFile;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.Roles;
import ome.system.ServiceFactory;
import ome.util.SqlAction;
import ome.util.SqlAction.DeleteLog;
import omero.RMap;
import omero.RType;
import omero.SecurityViolation;
import omero.ServerError;
import omero.ValidationException;
import omero.model.ChecksumAlgorithm;
import omero.model.Fileset;
import omero.model.Job;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.util.IceMapper;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.Session;
import org.perf4j.StopWatch;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.Advised;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;

/**
 * DAO class for encapsulating operations related to resource access inside the
 * repository. Methods return types already mapped from Ice. DAO is also used
 * in the form of a mock in unit tests.
 *
 * @author Blazej Pindelski <bpindelski at dundee dot ac dot uk>
 * @author Colin Blackburn <c.blackburn at dundee dot ac dot uk>
 * @since 4.5
 */
public class RepositoryDaoImpl implements RepositoryDao {

    private static class Rethrow extends InternalException {
        private final Throwable t;
        Rethrow(Throwable t) {
            super("rethrow!");
            this.t = t;
        }
    }

    private static abstract class StatefulWork
        extends Executor.SimpleWork
        implements Executor.StatefulWork {

        private final RawFileBean bean;
        StatefulWork(RawFileBean bean,
                Object self, String method, Object...args) {
            super(self, method, args);
            this.bean = bean;
        }

        public Object getThis() {
            return bean;
        }
    }

    /** Query to load the original file.*/
    private static final String LOAD_ORIGINAL_FILE =
    "select f from OriginalFile as f left outer join fetch f.hasher where ";

    /* query to load a user's institution */
    private static final String LOAD_USER_INSTITUTION =
            "SELECT institution FROM " + Experimenter.class.getName() + " WHERE id = :id";

    private final static Logger log = LoggerFactory.getLogger(RepositoryDaoImpl.class);

    private final IceMapper mapper = new IceMapper();

    protected final Principal principal;
    protected final Roles roles;
    protected final Executor executor;
    protected final Executor statefulExecutor;

    /**
     * Primary constructor which takes all final fields.
     *
     * @param principal
     * @param roles
     * @param executor
     * @param statefulExecutor
     */
    public RepositoryDaoImpl(Principal principal, Roles roles,
            Executor executor, Executor statefulExecutor) {
        this.principal = principal;
        this.roles = roles;
        this.executor = executor;
        this.statefulExecutor = statefulExecutor;
    }

    /**
     * Previous constructor which should no longer be used. Primarily for
     * simplicity of testing.
     *
     * @param principal
     * @param executor
     */
    public RepositoryDaoImpl(Principal principal, Executor executor) {
        this(principal, new Roles(), executor,
                executor.getContext().getBean(
                        "statefulExecutor", Executor.class));
    }

    /**
     * Loads
     * @return
     */
    protected RawFileBean unwrapRawFileBean(RawFileStore proxy) {
        try {
            return (RawFileBean) ((Advised) proxy).getTargetSource().getTarget();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected SecurityViolation wrapSecurityViolation(
            ome.conditions.SecurityViolation sv) throws SecurityViolation {
        SecurityViolation copy = new SecurityViolation();
        IceMapper.fillServerError(copy, sv);
        throw copy;
    }

    public RawFileStore getRawFileStore(final long fileId, final CheckedPath checked,
            String mode, Ice.Current current) throws SecurityViolation {

        final RawFileStore proxy = executor.getContext()
                .getBean("managed-ome.api.RawFileStore", RawFileStore.class);
        final RawFileBean bean = unwrapRawFileBean(proxy);
        final FileBuffer buffer = checked.getFileBuffer(mode);
        try {
            Map<String, String> fileContext = fileContext(fileId, current);
            statefulExecutor.execute(fileContext, currentUser(current),
                new StatefulWork(bean, this,
                    "setFileIdWithBuffer", fileId, checked, mode) {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        bean.setFileIdWithBuffer(fileId, buffer);
                        return null;
                    }
                });
        } catch (ome.conditions.SecurityViolation sv) {
            throw wrapSecurityViolation(sv);
        }
        return proxy;
    }

    public OriginalFile findRepoFile(final String uuid, final CheckedPath checked,
            final String mimetype, Ice.Current current)
            throws omero.ServerError {

        try {
            ome.model.core.OriginalFile ofile = (ome.model.core.OriginalFile) executor
                    .execute(current.ctx, currentUser(current),
                            new Executor.SimpleWork(this, "findRepoFile", uuid, checked, mimetype) {
                        @Transactional(readOnly = true)
                        public ome.model.core.OriginalFile doWork(Session session, ServiceFactory sf) {
                        return findRepoFile(sf, getSqlAction(), uuid, checked, mimetype);
                    }
                });
            return (OriginalFile) new IceMapper().map(ofile);
        } catch (ome.conditions.SecurityViolation sv) {
            throw wrapSecurityViolation(sv);
        }

    }

    public ome.model.core.OriginalFile findRepoFile(ServiceFactory sf,
            SqlAction sql, final String uuid, final CheckedPath checked,
            final String mimetype) {

        Long id = sql.findRepoFile(uuid,
                checked.getRelativePath(), checked.getName(),
                mimetype);
        if (id == null) {
            return null;
        } else {
            return sf.getQueryService().get(
                    ome.model.core.OriginalFile.class, id);
        }
    }

    public RMap treeList(final String repoUuid, final CheckedPath checked,
            Current current) throws ServerError {

        final RMap map = omero.rtypes.rmap();
        executor.execute(current.ctx, currentUser(current),
                new Executor.SimpleWork(this,
                "treeList", repoUuid, checked) {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                _treeList(map, repoUuid, checked, sf, getSqlAction());
                return null;
            }
        });

        return map;
    }
    /**
     * Recursive descent for {@link PublicDirectoryI#treeList(String, Current)}.
     * This should only really be called on directories, but if it's accidentally
     * called on a non-directory, then we will treat it specially.
     *
     * @param rv The {@link RMap} which should be filled for a given level.
     * @param path
     * @param __current
     * @throws ServerError
     */
    private void _treeList(RMap rv, String repoUuid, CheckedPath checked,
            ServiceFactory sf, SqlAction sql) {

        final ome.model.core.OriginalFile file
            = findRepoFile(sf, sql, repoUuid, checked, null);

        if (file == null) {
            if (rv.getValue().size() == 0) {
                // This is likely the top-level search, and therefore
                // we can just exit.
                log.debug("No file found in _treeList: " + checked);
            } else {
                // In this case, we've been given data that's now
                // missing from the DB in the same transaction.
                // Shouldn't happen.
                log.warn("No file found in _treeList: " + checked);
            }
            return; // EARLY EXIT.
        }

        final String name = file.getName();
        final String mime = file.getMimetype();
        final Long size = file.getSize();
        final Long id = file.getId();

        final RMap subRv = omero.rtypes.rmap();
        final Map<String, RType> subVal = subRv.getValue();
        rv.put(name, subRv);
        subVal.put("id", omero.rtypes.rlong(id));
        subVal.put("mimetype", omero.rtypes.rstring(mime));
        if (size != null) {
            subVal.put("size", omero.rtypes.rlong(size));
        }

        if (file.getMimetype() != null && // FIXME: should be set!
                PublicRepositoryI.DIRECTORY_MIMETYPE.equals(file.getMimetype())) {

            // Now we recurse
            List<ome.model.core.OriginalFile> subFiles
                = getOriginalFiles(sf, sql, repoUuid, checked);
            final RMap subFilesRv = omero.rtypes.rmap();

            for (ome.model.core.OriginalFile subFile : subFiles) {
                CheckedPath child = null;
                try {
                    child = checked.child(subFile.getName());
                    if (child == null) {
                        // This should never happen.
                        throw new omero.ValidationException(null, null, "null child!");
                    }
                } catch (omero.ValidationException ve) {
                    // This can only really happen if the database has very odd
                    // information stored. Issuing a warning and then throwing
                    // an exception.
                    log.warn(String.format("Validation exception on %s.child(%s)",
                            checked, subFile.getName()), ve);
                    throw new ome.conditions.ValidationException(ve.getMessage());
                }

                _treeList(subFilesRv, repoUuid, child, sf, sql);
            }
            subVal.put("files", subFilesRv);
        }
    }

    public void createOrFixUserDir(final String repoUuid,
            final List<CheckedPath> checkedPaths,
            final Session s, final ServiceFactory sf, final SqlAction sql)
                    throws ServerError {

        final StopWatch outer = new Slf4JStopWatch();
        try {

            for (CheckedPath checked : checkedPaths) {

                CheckedPath parent;
                try {
                    parent = checked.parent();
                } catch (ValidationException ve) {
                    throw new RuntimeException(ve);
                }

                StopWatch sw = new Slf4JStopWatch();
                // Look for the dir in all groups (
                Long id = sql.findRepoFile(repoUuid, checked.getRelativePath(),
                        checked.getName());
                sw.stop("omero.repo.file.find");

                ome.model.core.OriginalFile f = null;
                if (id == null) {
                    // Doesn't exist. Create directory
                    sw = new Slf4JStopWatch();
                    // TODO: this whole method now looks quite similar to _internalRegister
                    f = _internalRegister(repoUuid,
                            Arrays.asList(checked), Arrays.asList(parent),
                            null, PublicRepositoryI.DIRECTORY_MIMETYPE,
                            sf, sql).get(0);
                    sw.stop("omero.repo.file.register");
                } else {
                    // Make sure the file is in the user group
                    try {
                        sw = new Slf4JStopWatch();
                        // Now that within one tx, likely cached.
                        f = sf.getQueryService().get(
                                ome.model.core.OriginalFile.class, id);
                        if (f != null) {
                            long groupId = f.getDetails().getGroup().getId();
                            if (roles.getUserGroupId() == groupId) {
                                // Null f, since it doesn't need to be reset.
                                f = null;
                            }
                        }
                        sw.stop("omero.repo.file.check_group");
                    }
                    catch (ome.conditions.SecurityViolation e) {
                        // If we aren't allowed to read the file, then likely
                        // it isn't in the user group so we will move it there.
                        f = new ome.model.core.OriginalFile(id, false);
                    }
                }

                if (f != null) {
                    sw = new Slf4JStopWatch();
                    ((LocalAdmin) sf.getAdminService())
                        .internalMoveToCommonSpace(f);
                    sw.stop("omero.repo.file.move_to_common");
                }

            }
        } catch (ome.conditions.SecurityViolation sv) {
            throw wrapSecurityViolation(sv);
        } finally {
            outer.stop("omero.repo.user_dir");
        }
    }

    public boolean canUpdate(final omero.model.IObject obj, Ice.Current current) {
        return (Boolean)  executor
                .execute(current.ctx, currentUser(current),
                        new Executor.SimpleWork(this, "canUpdate") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        try {
                            ome.model.IObject iobj = (ome.model.IObject)
                                new IceMapper().reverse(obj);
                            return sf.getAdminService().canUpdate(iobj);
                        } catch (Exception e) {
                            return false;
                        }
                    }
                });
    }

    public List<Long> filterFilesByRepository(final String repo, List<Long> ids, Ice.Current current) {
        final List<Long> inRepo = new ArrayList<Long>();
        for (final List<Long> idBatch : Iterables.partition(ids, 256)) {
            inRepo.addAll((Collection<Long>) executor
                    .execute(current.ctx, currentUser(current),
                            new Executor.SimpleWork(this, "filterFilesByRepository") {
                        @Override
                        @Transactional(readOnly = true)
                        public List<Long> doWork(Session session, ServiceFactory sf) {
                            return getSqlAction().filterFileIdsByRepo(repo, idBatch);
                        }
                    }));
        }
        return inRepo;
    }

    public OriginalFile getOriginalFile(final long repoId,
            final Ice.Current current) throws SecurityViolation {

         try {
             ome.model.core.OriginalFile oFile = (ome.model.core.OriginalFile) executor
                 .execute(current.ctx, currentUser(current),
                         new Executor.SimpleWork(this, "getOriginalFile", repoId) {
                     @Transactional(readOnly = true)
                     public Object doWork(Session session, ServiceFactory sf) {
                        return sf.getQueryService().findByQuery(
                        LOAD_ORIGINAL_FILE+" f.id = :id",
                        new Parameters().addId(repoId));
                     }
                 });
             return (OriginalFileI) new IceMapper().map(oFile);
         } catch (ome.conditions.SecurityViolation sv) {
             throw wrapSecurityViolation(sv);
         }
     }

    @SuppressWarnings("unchecked")
    public List<OriginalFile> getOriginalFiles(final String repoUuid, final CheckedPath checked,
            final Ice.Current current) throws SecurityViolation {

         try {
             List<ome.model.core.OriginalFile> oFiles = (List<ome.model.core.OriginalFile>) executor
                .execute(current.ctx, currentUser(current),
                        new Executor.SimpleWork(this,
                        "getOriginalFiles", repoUuid, checked) {
                    @Transactional(readOnly = true)
                     public List<ome.model.core.OriginalFile> doWork(Session session, ServiceFactory sf) {
                         return getOriginalFiles(sf, getSqlAction(), repoUuid, checked);
                     }

                 });
             return (List<OriginalFile>) new IceMapper().map(oFiles);
         } catch (ome.conditions.SecurityViolation sv) {
             throw wrapSecurityViolation(sv);
         }
    }

    protected List<ome.model.core.OriginalFile> getOriginalFiles(
            ServiceFactory sf, SqlAction sql,
            final String repoUuid,
            final CheckedPath checked) {

            final IQuery q = sf.getQueryService();
            final Long id;

            if (checked.isRoot) {
                id = q.findByString(ome.model.core.OriginalFile.class,
                        "hash", repoUuid).getId();

                if (id == null) {
                    throw new ome.conditions.SecurityViolation(
                            "No repository with UUID: " + repoUuid);
                }
            } else {
                id = sql.findRepoFile(repoUuid,
                        checked.getRelativePath(), checked.getName());

                if (id == null) {
                    throw new ome.conditions.SecurityViolation(
                            "No such parent dir: " + checked);
                }
            }

            // Load parent directory to possibly cause
            // a read sec-vio.
            //
            q.get(ome.model.core.OriginalFile.class, id);

            List<Long> ids = sql.findRepoFiles(repoUuid,
                    checked.getDirname());

            if (CollectionUtils.isEmpty(ids)) {
                return Collections.emptyList();
            }
            Parameters p = new Parameters();
            p.addIds(ids);
            return q.findAllByQuery(LOAD_ORIGINAL_FILE+"f.id in (:ids)", p);

    }

    public Fileset saveFileset(final String repoUuid, final Fileset _fs,
            final ChecksumAlgorithm checksumAlgorithm, final List<CheckedPath> paths,
            final Ice.Current current) throws ServerError {

        final IceMapper mapper = new IceMapper();
        final List<CheckedPath> parents = new ArrayList<CheckedPath>();
        for (CheckedPath path : paths) {
            parents.add(path.parent());
        }

        final ome.model.fs.Fileset fs = (ome.model.fs.Fileset) mapper.reverse(_fs);

        final StopWatch outer = new Slf4JStopWatch();
        try {
            return (Fileset) mapper.map((ome.model.fs.Fileset)
                    executor.execute(current.ctx, currentUser(current),
                            new Executor.SimpleWork(
                    this, "saveFileset", repoUuid, fs, paths) {
                @Transactional(readOnly = false)
                public Object doWork(Session session, ServiceFactory sf) {
                    // Pre-save all the jobs.
                    for (int i = 0; i < fs.sizeOfJobLinks(); i++) {
                        FilesetJobLink link = fs.getFilesetJobLink(i);
                        JobHandle jh = sf.createJobHandle();
                        try {
                            jh.submit(link.child());
                            link.setChild(jh.getJob());
                        } finally {
                            jh.close();
                        }
                    }

                    StopWatch sw = new Slf4JStopWatch();
                    final int size = paths.size();
                    List<ome.model.core.OriginalFile> ofs =
                                _internalRegister(repoUuid, paths, parents,
                                        checksumAlgorithm, null,
                                        sf, getSqlAction());
                    sw.stop("omero.repo.save_fileset.register");

                    sw = new Slf4JStopWatch();
                    for (int i = 0; i < size; i++) {
                        CheckedPath checked = paths.get(i);
                        ome.model.core.OriginalFile of = ofs.get(i);
                        fs.getFilesetEntry(i).setOriginalFile(of);
                    }
                    sw.stop("omero.repo.save_fileset.update_fileset_entries");

                    sw = new Slf4JStopWatch();
                    try {
                        return sf.getUpdateService().saveAndReturnObject(fs);
                    } finally {
                        sw.stop("omero.repo.save_fileset.save");
                    }

                }}));
        } catch (Exception e) {
            throw (ServerError) mapper.handleException(e, executor.getContext());
        } finally {
            outer.stop("omero.repo.save_fileset");
        }
    }

    @SuppressWarnings("unchecked")
    public List<Fileset> loadFilesets(final List<Long> ids,
            final Ice.Current current) throws ServerError {

        if (ids == null || ids.size() == 0) {
            return new ArrayList<Fileset>(); // EARLY EXIT
        }

        final IceMapper mapper = new IceMapper();

        try {
            return (List<Fileset>) mapper.map((List<ome.model.fs.Fileset>)
                    executor.execute(current.ctx, currentUser(current),
                            new Executor.SimpleWork(
                    this, "loadFilesets", ids) {
                @Transactional(readOnly = true)
                public Object doWork(Session session, ServiceFactory sf) {
                    return sf.getQueryService().findAllByQuery(
                            "select fs from Fileset fs where fs.id in (:ids)",
                            new Parameters().addIds(ids));
                }}));
        } catch (Exception e) {
            throw (ServerError) mapper.handleException(e, executor.getContext());
        }
    }

    public OriginalFile register(final String repoUuid, final CheckedPath checked,
            final String mimetype, final Ice.Current current) throws ServerError {

        if (checked.isRoot) {
            throw new omero.SecurityViolation(null, null,
                    "Can't re-register the repository");
        }

        final CheckedPath parent = checked.parent();
        final IceMapper mapper = new IceMapper();

        try {
            final ome.model.core.OriginalFile of = (ome.model.core.OriginalFile)
                    executor.execute(current.ctx, currentUser(current),
                            new Executor.SimpleWork(
                    this, "register", repoUuid, checked, mimetype) {
                @Transactional(readOnly = false)
                public Object doWork(Session session, ServiceFactory sf) {
                    return _internalRegister(repoUuid,
                            Arrays.asList(checked), Arrays.asList(parent),
                            null, mimetype, sf, getSqlAction()).get(0);
                }
            });

            return (OriginalFile) mapper.map(of);
        } catch (Exception e) {
            throw (ServerError) mapper.handleException(e, executor.getContext());
        }
    }

    /**
     * Returned original file object is "live" within the Hibernate session.
     *
     * @param repoUuid
     * @param checked
     * @param mimetype
     * @param sf
     * @param sql
     * @return
     * @throws ServerError
     */
    public ome.model.core.OriginalFile register(final String repoUuid, final CheckedPath checked,
            final String mimetype, final ServiceFactory sf, final SqlAction sql)
                    throws ServerError {

        if (checked.isRoot) {
            throw new omero.SecurityViolation(null, null,
                    "Can't re-register the repository");
        }

        final CheckedPath parent = checked.parent();

        return _internalRegister(repoUuid,
                Arrays.asList(checked), Arrays.asList(parent),
                null, mimetype, sf, sql).get(0);

    }

    public Job saveJob(final Job job, final Ice.Current current)
            throws ServerError {

        if (job == null) {
            throw new omero.ValidationException(null, null,
                    "Job is null!");
        }

        final IceMapper mapper = new IceMapper();

        try {
            final ome.model.jobs.Job in = (ome.model.jobs.Job) mapper.reverse(job);
            final ome.model.jobs.Job out = (ome.model.jobs.Job)
                    executor.execute(current.ctx, currentUser(current),
                            new Executor.SimpleWork(
                    this, "saveJob", in) {
                @Transactional(readOnly = false)
                public Object doWork(Session session, ServiceFactory sf) {
                    JobHandle jh = sf.createJobHandle();
                    jh.submit(in);
                    return jh.getJob();
                }
            });

            return (Job) mapper.map(out);
        } catch (Exception e) {
            throw (ServerError) mapper.handleException(e, executor.getContext());
        }
    }


    public void updateJob(final Job job, final String message, final String status,
            final Ice.Current current) throws ServerError {

        if (job == null || job.getId() == null) {
            throw new omero.ValidationException(null, null,
                    "Job is null!");
        }

        final IceMapper mapper = new IceMapper();

        try {
            final ome.model.jobs.Job in = (ome.model.jobs.Job) mapper.reverse(job);
            executor.execute(current.ctx, currentUser(current),
                            new Executor.SimpleWork(
                    this, "updateJob", in, message, status) {
                @Transactional(readOnly = false)
                public Object doWork(Session session, ServiceFactory sf) {
                    JobHandle jh = sf.createJobHandle();
                    jh.attach(in.getId());
                    jh.setStatusAndMessage(status, message);
                    return null;
                }
            });

        } catch (Exception e) {
            throw (ServerError) mapper.handleException(e, executor.getContext());
        }
    }

    /*
     * See api.
     */
    public void makeDirs(final PublicRepositoryI repo,
            final List<CheckedPath> dirs,
            final boolean parents,
            final Ice.Current __current) throws ServerError {
        try {
            /* first check for sudo to find real user's event context */
            final EventContext effectiveEventContext;
            final String realSessionUuid = __current.ctx.get(PublicRepositoryI.SUDO_REAL_SESSIONUUID);
            if (realSessionUuid != null) {
                final String realGroupName = __current.ctx.get(PublicRepositoryI.SUDO_REAL_GROUP_NAME);
                final Principal realPrincipal = new Principal(realSessionUuid, realGroupName, null);
                final Map<String, String> realCtx = new HashMap<String, String>(__current.ctx);
                realCtx.put(omero.constants.SESSIONUUID.value, realSessionUuid);
                if (realGroupName == null) {
                    realCtx.remove(omero.constants.GROUP.value);
                } else {
                    realCtx.put(omero.constants.GROUP.value, realGroupName);
                }
                effectiveEventContext = (EventContext) executor.execute(realCtx, realPrincipal,
                        new Executor.SimpleWork(this, "makeDirs", dirs) {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        return ((LocalAdmin) sf.getAdminService()).getEventContextQuiet();
                    }
                });
            } else {
                effectiveEventContext = null;
            }
            /* now actually make the directories */
            executor.execute(__current.ctx, currentUser(__current),
                new Executor.SimpleWork(this, "makeDirs", dirs) {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                final ome.system.EventContext eventContext;
                if (effectiveEventContext == null) {
                    eventContext =
                        ((LocalAdmin) sf.getAdminService()).getEventContextQuiet();
                } else {
                    eventContext = effectiveEventContext;  /* sudo */
                }
                for (CheckedPath checked : dirs) {
                    try {
                        repo.makeDir(checked, parents,
                            session, sf, getSqlAction(), eventContext);
                    } catch (ServerError se) {
                        throw new Rethrow(se);
                    }
                }
                return null;
            }
        });
        } catch (Rethrow rt) {
            throw (ServerError) rt.t;
        } catch (Exception e) {
            throw (ServerError) mapper.handleException(e, executor.getContext());
        }
    }

    /**
     * Internal file registration which must happen within a single tx. All
     * files in the same directory are loaded in one block.
     *
     * @param repoUuid
     * @param checked
     * @param checksumAlgorithm 
     * @param mimetype
     * @param parent
     * @param sf non-null
     * @param sql non-null
     * @return
     */
    private List<ome.model.core.OriginalFile> _internalRegister(final String repoUuid,
            final List<CheckedPath> checked, final List<CheckedPath> parents,
            ChecksumAlgorithm checksumAlgorithm, final String mimetype,
            ServiceFactory sf, SqlAction sql) {

        final List<ome.model.core.OriginalFile> toReturn = new ArrayList<ome.model.core.OriginalFile>();
        final ListMultimap<CheckedPath, CheckedPath> levels = ArrayListMultimap.create();
        for (int i = 0; i < checked.size(); i++) {
            levels.put(parents.get(i), checked.get(i));
        }

        for (CheckedPath parent : levels.keySet()) {
            List<CheckedPath> level = levels.get(parent);
            List<String> basenames = new ArrayList<String>(checked.size());
            for (CheckedPath path: level) {
                basenames.add(path.getName());
            }

            StopWatch sw = new Slf4JStopWatch();
            Map<String, Long> fileIds = sql.findRepoFiles(repoUuid,
                level.get(0).getRelativePath(), /* all the same */
                basenames, null /*mimetypes*/);
            sw.stop("omero.repo.internal_register.find_repo_files");

            List<Long> toLoad = new ArrayList<Long>();
            List<CheckedPath> toCreate = new ArrayList<CheckedPath>();
            for (int i = 0; i < level.size(); i++) {
                CheckedPath path = level.get(i);
                Long fileId = fileIds.get(path.getName());
                if (fileId == null) {
                    toCreate.add(path);
                } else {
                    toLoad.add(fileId);
                }
            }
            
            if (toCreate.size() > 0) {
                canWriteParentDirectory(sf, sql,
                    repoUuid, parent);
                List<ome.model.core.OriginalFile> created = createOriginalFile(sf, sql,
                    repoUuid, toCreate, checksumAlgorithm, mimetype);
                toReturn.addAll(created);
            }

            sw = new Slf4JStopWatch();
            if (toLoad.size() > 0) {
                List<ome.model.core.OriginalFile> loaded = 
                    sf.getQueryService().findAllByQuery("select o from OriginalFile o " +
                        "where o.id in (:ids)", new Parameters().addIds(toLoad));
                toReturn.addAll(loaded);
            }
            sw.stop("omero.repo.internal_register.load");
        }
        return toReturn;
    }

    public FsFile getFile(final long id, final Ice.Current current,
            final String repoUuid) {
        return (FsFile) executor.execute(current.ctx, currentUser(current),
                new Executor.SimpleWork(this, "getFile", id) {
            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                    String path = getSqlAction().findRepoFilePath(
                            repoUuid, id);

                    if (path == null) {
                        return null;
                    }

                    return new FsFile(path);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public List<DeleteLog> findRepoDeleteLogs(final DeleteLog template, Current current) {
        return (List<DeleteLog>) executor.execute(current.ctx, currentUser(current),
                new Executor.SimpleWork(this, "findRepoDeleteLogs", template) {
            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                return getSqlAction().findRepoDeleteLogs(template);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public List<List<DeleteLog>> findRepoDeleteLogs(final List<DeleteLog> templates, Current current) {
        return (List<List<DeleteLog>>) executor.execute(current.ctx, currentUser(current),
                new Executor.SimpleWork(this, "findRepoDeleteLogs", templates) {
            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                List<List<DeleteLog>> rv = new ArrayList<List<DeleteLog>>();
                for (DeleteLog template : templates) {
                    rv.add(getSqlAction().findRepoDeleteLogs(template));
                }
                return rv;
            }
        });
    }

    public int deleteRepoDeleteLogs(final DeleteLog template, Current current) {
        return (Integer) executor.execute(current.ctx, currentUser(current),
                new Executor.SimpleWork(this, "deleteRepoDeleteLogs", template) {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                return getSqlAction().deleteRepoDeleteLogs(template);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public List<Integer> deleteRepoDeleteLogs(final List<DeleteLog> templates, Current current) {
        return (List<Integer>) executor.execute(current.ctx, currentUser(current),
                new Executor.SimpleWork(this, "deleteRepoDeleteLogs", templates) {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                List<Integer> rv = new ArrayList<Integer>();
                for (DeleteLog template : templates) {
                    Integer i = getSqlAction().deleteRepoDeleteLogs(template);
                    rv.add(i);
                }
                return rv;
            }
        });
    }

    public omero.sys.EventContext getEventContext(Ice.Current curr) {
        EventContext ec = this.currentContext(new Principal(curr.ctx.get(
                omero.constants.SESSIONUUID.value)));
        return IceMapper.convert(ec);
    }

    protected EventContext currentContext(Principal currentUser) {
        return (EventContext) executor.execute(currentUser,
                new Executor.SimpleWork(this, "getEventContext") {
            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                return ((LocalAdmin) sf.getAdminService()).getEventContextQuiet();
            }
        });
    }

    public String getUserInstitution(final long userId, Ice.Current current) {
        return (String) executor.execute(current.ctx, currentUser(current),
                new Executor.SimpleWork(this, "getUserInstitution") {
            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                return getUserInstitution(userId, sf);
            }
        });
    }

    public String getUserInstitution(long userId, ServiceFactory sf) {
        final Parameters parameters = new Parameters().addId(userId);
        final List<Object[]> results = sf.getQueryService().projection(LOAD_USER_INSTITUTION, parameters);
        if (results instanceof List && results.get(0) instanceof Object[]) {
            final Object[] firstResult = (Object[]) results.get(0);
            if (firstResult.length > 0 && firstResult[0] instanceof String) {
                return (String) firstResult[0];
            }
        }
        return null;
    }

    //
    // HELPERS
    //

    /**
     * Primary location for creating original files from a {@link CheckedPath}
     * instance. This will use access to the actual {@link java.io.File}
     * object in order to calculate size, timestamps, etc.
     *
     * @param sf
     * @param sql
     * @param repoUuid
     * @param checked
     * @param checksumAlgorithm 
     * @param mimetype
     * @return
     */
    protected List<ome.model.core.OriginalFile> createOriginalFile(
            ServiceFactory sf, SqlAction sql, String repoUuid,
            List<CheckedPath> checked, ChecksumAlgorithm checksumAlgorithm, String mimetype) {

        ome.model.enums.ChecksumAlgorithm ca = null;
        if (checksumAlgorithm != null) {
             ca = new ome.model.enums.ChecksumAlgorithm(checksumAlgorithm.getValue().getValue());
        }
        List<ome.model.core.OriginalFile> rv = new ArrayList<ome.model.core.OriginalFile>();
        for (CheckedPath path : checked) {
            ome.model.core.OriginalFile ofile = path.asOriginalFile(mimetype);
            rv.add(ofile);
            ofile.setHasher(ca);
        }


        StopWatch sw = new Slf4JStopWatch();
        IObject[] saved = sf.getUpdateService().saveAndReturnArray(rv.toArray(new IObject[rv.size()]));
        sw.stop("omero.repo.create_original_file.save");
        final List<Long> ids = new ArrayList<Long>(saved.length);
        sw = new Slf4JStopWatch();
        for (int i = 0; i < saved.length; i++) {
            final CheckedPath path = checked.get(i);
            final ome.model.core.OriginalFile ofile = (ome.model.core.OriginalFile) saved[i];
            rv.set(i, ofile);
            ids.add(ofile.getId());
            if (PublicRepositoryI.DIRECTORY_MIMETYPE.equals(ofile.getMimetype())) {
                internalMkdir(path);
            }
        }
        sw.stop("omero.repo.create_original_file.internal_mkdir");

        sw = new Slf4JStopWatch();
        sql.setFileRepo(ids, repoUuid);
        sw.stop("omero.repo.create_original_file.set_file_repo");
        return rv;
    }

    /**
     * This method should only be used by the register public method in order to
     * guarantee that the DB is kept in sync with the file system.
     * @param checked the path to ensure exists as a directory
     * @throws ome.conditions.ResourceError
     */
    protected void internalMkdir(CheckedPath file) {
        if (file.exists()) {
            if (file.isRoot || file.isDirectory()) {
                return;
            } else {
                throw new ome.conditions.ResourceError("Cannot mkdir " + file + 
                        " because it is already a file");
            }
        }
        try {
            if (!file.mkdirs()) {
                throw new ome.conditions.ResourceError("Cannot mkdir " + file);
            }
        } catch (Exception e) {
            log.error(e.toString()); // slf4j migration: toString()
            throw new ome.conditions.ResourceError("Cannot mkdir " + file + ":" + e.getMessage());
        }
    }

    /**
     * Throw a {@link ome.conditions.SecurityViolation} if the current
     * context cannot write to the parent directory.
     *
     * @param sf
     * @param sql
     * @param repoUuid
     * @param parent
     */
    protected void canWriteParentDirectory(ServiceFactory sf, SqlAction sql,
            final String repoUuid, final CheckedPath parent) {

        if (parent.isRoot) {
            // Allow whatever (for the moment) at the top-level
            return; // EARLY EXIT!
        }

        // Now we check whether or not the current user has
        // write permissions for the *parent* directory.
        final Long parentId = sql.findRepoFile(repoUuid,
                parent.getRelativePath(), parent.getName());

        if (parentId == null) {
            throw new ome.conditions.SecurityViolation(
                    "Cannot find parent directory: " + parent);
        }

        final ome.model.core.OriginalFile parentObject
            = new ome.model.core.OriginalFile(parentId, false);

        long parentObjectOwnerId = -1;
        long parentObjectGroupId = -1;
        try {
            final String query = "SELECT details.owner.id, details.group.id FROM OriginalFile WHERE id = :id";
            final Parameters parameters = new Parameters().addId(parentId);
            final Object[] results = sf.getQueryService().projection(query, parameters).get(0);
            parentObjectOwnerId = (Long) results[0];
            parentObjectGroupId = (Long) results[1];
        } catch (Exception e) {
            log.warn("failed to retrieve owner and group details for original file #" + parentId, e);
        }

        if (parentObjectOwnerId != roles.getRootId() || parentObjectGroupId != roles.getUserGroupId()) {
            final LocalAdmin admin = (LocalAdmin) sf.getAdminService();
            if (!admin.canAnnotate(parentObject)) {
                throw new ome.conditions.SecurityViolation(
                        "No annotate access for parent directory: "
                                + parentId);
            }
        }
    }

    protected Principal currentUser(Current __current) {
        final Map<String, String> ctx = __current.ctx;
        final String session = ctx.get(omero.constants.SESSIONUUID.value);
        final String group = ctx.get(omero.constants.GROUP.value);
        return new Principal(session, group, null);
    }

    /**
     * Create a String-String map which can be used as the context for a call
     * to Executor.execute based on the group of the file object.
     * @throws SecurityViolation if the file can't be read.
     */
    protected Map<String, String> fileContext(long fileId, Ice.Current current)
        throws omero.SecurityViolation {

        // TODO: we should perhaps pass "-1" here regardless of what group is
        // passed by the client, but that violates the current working of the
        // API, so using the standard behavior at the moment.
        final OriginalFile file = getOriginalFile(fileId, current);
        return groupContext(file.getDetails().getGroup().getId().getValue(),
                current);
    }

    /**
     * Creates a copy of the {@link Ice.Current#ctx} map and if groupId is
     * not null, sets the "omero.group" key to be a string version of the
     * id.
     *
     * @param groupId
     * @param current
     * @return
     */
    protected Map<String, String> groupContext(Long groupId, Ice.Current current) {
        final Map<String, String> context = new HashMap<String, String>();
        if (current.ctx != null) {
            context.putAll(current.ctx);
        }
        if (groupId != null) {
            context.put("omero.group", Long.toString(groupId));
        }
        return context;
    }

    @Override
    public ome.model.enums.ChecksumAlgorithm getChecksumAlgorithm(final String name, Ice.Current current) {
        return (ome.model.enums.ChecksumAlgorithm) executor.execute(current.ctx, currentUser(current),
                new Executor.Work<ome.model.enums.ChecksumAlgorithm>() {

            @Override
            public String description() {
                return "get a checksum algorithm by name " + name;
            }

            @Override
            @Transactional(readOnly = true)
            public ome.model.enums.ChecksumAlgorithm doWork(Session session, ServiceFactory sf) {
                final String query = "FROM ChecksumAlgorithm WHERE value = :name";
                final Parameters params = new Parameters().addString("name", name);
                final List<Object[]> results = sf.getQueryService().projection(query, params);
                return (ome.model.enums.ChecksumAlgorithm) results.get(0)[0];
            }
        });
    }

    @Override
    public ome.model.core.OriginalFile getOriginalFileWithHasher(final long id, Ice.Current current) {
        return (ome.model.core.OriginalFile) executor.execute(current.ctx, currentUser(current),
                new Executor.Work<ome.model.core.OriginalFile>() {

            @Override
            public String description() {
                return "get an original file #" + id + ", with hasher joined";
            }

            @Override
            @Transactional(readOnly = true)
            public ome.model.core.OriginalFile doWork(Session session, ServiceFactory sf) {
                final String query = "FROM OriginalFile o LEFT OUTER JOIN FETCH o.hasher WHERE o.id = :id";
                final Parameters params = new Parameters().addId(id);
                final List<Object[]> results = sf.getQueryService().projection(query, params);
                return (ome.model.core.OriginalFile) results.get(0)[0];
            }
        });
    }

    @Override
    public void saveObject(final IObject object, Ice.Current current) {
        executor.execute(current.ctx, currentUser(current),
                new Executor.Work<Object>() {

            @Override
            public String description() {
                return "save the model object " + object;
            }

            @Override
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                sf.getUpdateService().saveObject(object);
                return null;
            }
        });
    }

}
