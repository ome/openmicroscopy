package ome.services.blitz.repo;

import static omero.rtypes.rlong;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.io.File;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;

import org.hibernate.Session;
import org.springframework.aop.framework.Advised;
import org.springframework.transaction.annotation.Transactional;

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

    protected final Principal principal;
    protected final Executor executor;

    public RepositoryDaoImpl(Principal principal, Executor executor) {
        this.principal = principal;
        this.executor = executor;
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
            String mode, Principal currentUser) throws SecurityViolation {

        final RawFileStore proxy = executor.getContext()
                .getBean("managed-ome.api.RawFileStore", RawFileStore.class);
        final RawFileBean bean = unwrapRawFileBean(proxy);
        final FileBuffer buffer = new FileBuffer(checked.file.getAbsolutePath(),  mode);
        try {
            executor.execute(currentUser, new Executor.SimpleWork(this, "setFileIdWithBuffer") {
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
            final String mimetype, Principal currentUser)
            throws omero.ServerError {

        try {
            ome.model.core.OriginalFile ofile = (ome.model.core.OriginalFile) executor
                .execute(currentUser, new Executor.SimpleWork(this, "findRepoFile", uuid, checked, mimetype) {
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

    public boolean canUpdate(final omero.model.IObject obj, Principal currentUser) {
        return (Boolean)  executor
                .execute(currentUser, new Executor.SimpleWork(this, "canUpdate") {
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
            final Principal currentUser) throws SecurityViolation {

         try {
             ome.model.core.OriginalFile oFile = (ome.model.core.OriginalFile)  executor
                 .execute(currentUser, new Executor.SimpleWork(this, "getOriginalFile", repoId) {
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
            final Principal currentUser) throws SecurityViolation {

         try {
             List<ome.model.core.OriginalFile> oFiles = (List<ome.model.core.OriginalFile>)  executor
                 .execute(currentUser, new Executor.SimpleWork(this,
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

    public OriginalFile register(final String repoUuid, final CheckedPath checked,
            final String mimetype, final Principal currentUser) throws ServerError {

        final IceMapper mapper = new IceMapper();
        try {
            final ome.model.core.OriginalFile of = (ome.model.core.OriginalFile)
                    executor.execute(currentUser, new Executor.SimpleWork(
                    this, "register", repoUuid, checked) {
                @Transactional(readOnly = false)
                public Object doWork(Session session, ServiceFactory sf) {

                    Long fileId = getSqlAction().findRepoFile(
                            repoUuid, checked.getRelativePath(),
                            checked.getName(), null /*mimetype doesn't matter*/);

                    if (fileId == null) {
                        return createOriginalFile(sf, getSqlAction(),
                                repoUuid, checked, mimetype);
                    } else {
                        return sf.getQueryService().get(
                                ome.model.core.OriginalFile.class, fileId);
                    }
                }
            });

            return (OriginalFile) new IceMapper().map(of);
        } catch (Exception e) {
            throw (ServerError) mapper.handleException(e, executor.getContext());
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
    public File getFile(final long id, final Principal currentUser,
            final String repoUuid, final CheckedPath root) {
        return (File) executor.execute(currentUser, new Executor.SimpleWork(this, "getFile", id) {
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

        if (file.exists() && !file.isDirectory()) {
            ofile.setMtime(new Timestamp(file.lastModified()));
            ofile.setSha1(checked.sha1());
            ofile.setSize(file.length());
        } else {
            ofile.setMtime(new Timestamp(System.currentTimeMillis()));
            ofile.setSha1("");
            ofile.setSize(0L);
        }
        // atime/ctime??

        ofile = sf.getUpdateService().saveAndReturnObject(ofile);
        sql.setFileRepo(ofile.getId(), repoUuid);
        return ofile;
    }

}
