package ome.services.blitz.repo;

import static omero.rtypes.rlong;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.aop.framework.Advised;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;

import ome.api.IQuery;
import ome.api.RawFileStore;
import ome.api.local.LocalAdmin;
import ome.io.nio.FileBuffer;
import ome.parameters.Parameters;
import ome.services.RawFileBean;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.util.SqlAction;

import omero.SecurityViolation;
import omero.ServerError;
import omero.grid.ImportLocation;
import omero.model.Fileset;
import omero.model.FilesetEntry;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.util.IceMapper;

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

    private final static Log log = LogFactory.getLog(RepositoryDaoImpl.class);

    protected final Principal principal;
    protected final Executor executor;
    protected final Executor statefulExecutor;

    public RepositoryDaoImpl(Principal principal, Executor executor) {
        this.principal = principal;
        this.executor = executor;
        this.statefulExecutor = executor.getContext().getBean("statefulExecutor",
                Executor.class);
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
        final FileBuffer buffer = new FileBuffer(checked.file.getAbsolutePath(),  mode);
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
                        Long id = getSqlAction().findRepoFile(uuid,
                                checked.getRelativePath(), checked.getName(),
                                mimetype);
                        if (id == null) {
                            return null;
                        } else {
                            return sf.getQueryService().get(
                                    ome.model.core.OriginalFile.class, id);
                        }
                    }
                });
            return (OriginalFile) new IceMapper().map(ofile);
        } catch (ome.conditions.SecurityViolation sv) {
            throw wrapSecurityViolation(sv);
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

    public OriginalFile getOriginalFile(final long repoId,
            final Ice.Current current) throws SecurityViolation {

         try {
             ome.model.core.OriginalFile oFile = (ome.model.core.OriginalFile)  executor
                 .execute(current.ctx, currentUser(current),
                         new Executor.SimpleWork(this, "getOriginalFile", repoId) {
                     @Transactional(readOnly = true)
                     public Object doWork(Session session, ServiceFactory sf) {
                         return sf.getQueryService().find(ome.model.core.OriginalFile.class, repoId);
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
             List<ome.model.core.OriginalFile> oFiles = (List<ome.model.core.OriginalFile>)  executor
                 .execute(current.ctx, currentUser(current),
                         new Executor.SimpleWork(this,
                         "getOriginalFiles", repoUuid, checked) {
                     @Transactional(readOnly = true)
                     public List<ome.model.core.OriginalFile> doWork(Session session, ServiceFactory sf) {

                         final IQuery q = sf.getQueryService();

                         Long id = null;
                         if (checked.isRoot) {
                             id = q.findByString(ome.model.core.OriginalFile.class,
                                     "sha1", repoUuid).getId();
                         } else {
                             id = getSqlAction().findRepoFile(repoUuid,
                                 checked.getRelativePath(), checked.getName(),
                                 null);

                             if (id == null) {
                                 throw new ome.conditions.SecurityViolation(
                                         "No such parent dir: " + checked);
                             }
                         }

                         // Load parent directory to possibly cause
                         // a read sec-vio.
                         q.get(ome.model.core.OriginalFile.class, id);

                         String dirname = null;
                         if (checked.isRoot) {
                             dirname = "/";
                         } else {
                             dirname = checked.getDirname();
                         }
                         List<Long> ids = getSqlAction().findRepoFiles(repoUuid,
                                 dirname);

                         if (ids == null || ids.size() == 0) {
                             return Collections.emptyList();
                         }
                         Parameters p = new Parameters();
                         p.addIds(ids);
                         return q.findAllByQuery(
                                 "select o from OriginalFile o where o.id in (:ids)", p);
                     }
                 });
             return (List<OriginalFile>) new IceMapper().map(oFiles);
         } catch (ome.conditions.SecurityViolation sv) {
             throw wrapSecurityViolation(sv);
         }
    }

    public Fileset saveFileset(final String repoUuid, final Fileset _fs,
            final List<CheckedPath> paths,
            final Principal currentUser) throws ServerError {

        final IceMapper mapper = new IceMapper();
        final List<CheckedPath> parents = new ArrayList<CheckedPath>();
        for (CheckedPath path : paths) {
            parents.add(path.parent());
        }

        final ome.model.fs.Fileset fs = (ome.model.fs.Fileset) mapper.reverse(_fs);

        try {
            return (Fileset) mapper.map((ome.model.fs.Fileset)
                    executor.execute(currentUser, new Executor.SimpleWork(
                    this, "saveFileset", paths) {
                @Transactional(readOnly = false)
                public Object doWork(Session session, ServiceFactory sf) {
                    int size = paths.size();
                    for (int i = 0; i < size; i++) {
                        CheckedPath checked = paths.get(i);
                        ome.model.core.OriginalFile of =
                                _internalRegister(repoUuid, checked, null,
                                        parents.get(i), sf, getSqlAction());
                        fs.getFilesetEntry(i).setOriginalFile(of);
                    }

                    return sf.getUpdateService().saveAndReturnObject(fs);
                }}));
        } catch (Exception e) {
            throw (ServerError) mapper.handleException(e, executor.getContext());
        }
    }

    public OriginalFile register(final String repoUuid, final CheckedPath checked,
            final String mimetype, final Ice.Current current) throws ServerError {

        if (checked.isRoot) {
            throw new ome.conditions.SecurityViolation(
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
                    return _internalRegister(repoUuid, checked, mimetype,
                            parent, sf, getSqlAction());
                }
            });

            return (OriginalFile) mapper.map(of);
        } catch (Exception e) {
            throw (ServerError) mapper.handleException(e, executor.getContext());
        }
    }

    /**
     * Internal file registration which must happen within a single tx.
     *
     * @param repoUuid
     * @param checked
     * @param mimetype
     * @param parent
     * @param sf non-null
     * @param sql non-null
     * @return
     */
    private ome.model.core.OriginalFile _internalRegister(final String repoUuid,
            final CheckedPath checked, final String mimetype,
            final CheckedPath parent, ServiceFactory sf, SqlAction sql) {
        Long fileId = sql.findRepoFile(
                repoUuid, checked.getRelativePath(),
                checked.getName(), null /*mimetype doesn't matter*/);

        if (fileId == null) {
            canWriteParentDirectory(sf, sql,
                    repoUuid, parent);
            return createOriginalFile(sf, sql,
                    repoUuid, checked, mimetype);
        } else {
            return sf.getQueryService().get(
                    ome.model.core.OriginalFile.class, fileId);
        }
    }

    /**
     * Get an {@link OriginalFile} object based on its id. Returns null if
     * the file does not exist or does not belong to this repo.
     *
     * @param id
     *            long, db id of original file.
     * @return OriginalFile object.
     *
     */
    public File getFile(final long id, final Ice.Current current,
            final String repoUuid, final CheckedPath root) {
        return (File) executor.execute(current.ctx, currentUser(current),
                new Executor.SimpleWork(this, "getFile", id) {
            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                    String path = getSqlAction().findRepoFilePath(
                            repoUuid, id);

                    if (path == null) {
                        return null;
                    }

                    return new File(root.file, path);
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
     * @param mimetype
     * @return
     */
    protected ome.model.core.OriginalFile createOriginalFile(
            ServiceFactory sf, SqlAction sql,
            String repoUuid, CheckedPath checked, String mimetype) {

        final File file = checked.file;

        ome.model.core.OriginalFile ofile =
                new ome.model.core.OriginalFile();

        // Only non conditional properties.
        ofile.setName(checked.getName());
        ofile.setMimetype(mimetype); // null takes DB default

        // This first case deals with registering the repos themselves.
        if (checked.isRoot) {
            ofile.setPath(file.getParent());
        } else { // Path should be relative to root?
            ofile.setPath(checked.getRelativePath());
        }

        final boolean mimeDir = PublicRepositoryI.DIRECTORY_MIMETYPE.equals(mimetype);
        final boolean actualDir = file.isDirectory();

        if (file.exists() && !actualDir) {
            ofile.setMtime(new Timestamp(file.lastModified()));
            ofile.setSha1(checked.sha1());
            ofile.setSize(file.length());
        } else {
            ofile.setMtime(new Timestamp(System.currentTimeMillis()));
            ofile.setSha1("");
            ofile.setSize(0L);
            if (actualDir && !mimeDir) {
                throw new ome.conditions.ValidationException(
                        "File is a directory but mimetype is: " + mimetype);
            }
        }
        // atime/ctime??

        ofile = sf.getUpdateService().saveAndReturnObject(ofile);
        sql.setFileRepo(ofile.getId(), repoUuid);

        if (mimeDir) {
            internalMkdir(checked.file);
        }

        return ofile;
    }

    /**
     * This method should only be used by the register public method in order to
     * guarantee that the DB is kept in sync with the file system.
     * @param file
     * @throws ome.conditions.ResourceError
     */
    protected void internalMkdir(File file) {
        try {
            FileUtils.forceMkdir(file);
        } catch (Exception e) {
            log.error(e);
            throw new ome.conditions.ResourceError("Cannot mkdir:" + e.getMessage());
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
                parent.getRelativePath(), parent.getName(),
                null);

        if (parentId == null) {
            throw new ome.conditions.SecurityViolation(
                    "Cannot find parent directory: " + parent);
        }

        final ome.model.core.OriginalFile parentObject
            = new ome.model.core.OriginalFile(parentId, false);

        final LocalAdmin admin = (LocalAdmin) sf.getAdminService();
        if (!admin.canAnnotate(parentObject)) {
            throw new ome.conditions.SecurityViolation(
                    "No annotate access for parent directory: "
                            + parentId);
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
        final Map<String, String> context = new HashMap<String, String>();
        if (current.ctx != null) {
            context.putAll(current.ctx);
        }
        context.put("omero.group",
                Long.toString(file.getDetails().getGroup().getId().getValue()));
        return context;
    }
}
