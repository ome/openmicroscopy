package ome.services.blitz.repo;

import static omero.rtypes.rlong;

import java.io.File;
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
            return (OriginalFile) new IceMapper().reverse(ofile);
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

                         Long id = getSqlAction().findRepoFile(repoUuid,
                                 checked.getRelativePath(), checked.getName(),
                                 null);

                         if (id == null) {
                             throw new ome.conditions.SecurityViolation(
                                     "No such parent dir: " + checked);
                         }
                         final IQuery q = sf.getQueryService();
                         // Load parent directory to possibly cause
                         // a read sec-vio.
                         q.get(ome.model.core.OriginalFile.class, id);

                         List<Long> ids = getSqlAction().findRepoFiles(repoUuid,
                                 checked.getRelativePath());

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

    /**
     * Register an OriginalFile object
     *
     * @param omeroFile
     *            OriginalFile object.
     * @param repoUuid
     *            uuid of the repository that the given file argument should be
     *            registered with. Cannot be null.
     * @param currentUser
     *            Not null.
     * @return The OriginalFile with id set (unloaded)
     * @throws ServerError
     *
     */
    public OriginalFile register(OriginalFile omeroFile, final String repoUuid, final Principal currentUser)
            throws ServerError {
        IceMapper mapper = new IceMapper();
        final ome.model.core.OriginalFile omeFile = (ome.model.core.OriginalFile) mapper
                .reverse(omeroFile);
        Long id = (Long) executor.execute(currentUser, new Executor.SimpleWork(
                this, "register", omeroFile.getPath().getValue()) {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                Long id =
                        sf.getUpdateService().saveAndReturnObject(omeFile).getId();
                getSqlAction().setFileRepo(id, repoUuid);
                return id;
            }
        });

        omeroFile.setId(rlong(id));
        omeroFile.unload();
        return omeroFile;

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


    /**
     * Create an {@link OriginalFile} in the given repository if it does
     * not exist. Otherwise, return the id.
     *
     * @param repoUuid Not null. sha1 of the repository
     * @param path Not null. {@link OriginalFile#getPath()}
     * @param name Not null. {@link OriginalFile#getName()}
     * @param currentUser Not null.
     * @return ID of the object.
     * @throws omero.ApiUsageException
     */
    public OriginalFile createUserDirectory(final String repoUuid,
            final CheckedPath checked, Principal currentUser)
                    throws omero.ApiUsageException {

        ome.model.core.OriginalFile of = (ome.model.core.OriginalFile)
                executor.execute(currentUser, new Executor.SimpleWork(this,
                        "createUserDirectory", repoUuid, checked) {

                    @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                Long fileId = getSqlAction().findRepoFile(
                        repoUuid, checked.getRelativePath(),
                        checked.getName(), null /*mimetype*/);
                if (fileId == null) {
                    return createOriginalFile(
                            sf, getSqlAction(),
                            repoUuid, checked, "Directory",
                            0L, "None");
                } else {
                    return sf.getQueryService().get(
                            ome.model.core.OriginalFile.class, fileId);
                }
            }
        });
        return (OriginalFile) new IceMapper().map(of);
    }

    // TODO: The follow method should clearly be refactored with the above method.
    /**
     * Create an {@link OriginalFile} in the given repository if it does
     * not exist. Otherwise, return the id.
     *
     * @param repoUuid Not null. sha1 of the repository
     * @param path Not null. {@link OriginalFile#getPath()}
     * @param name Not null. {@link OriginalFile#getName()}
     * @param currentUser Not null.
     * @return ID of the object.
     * @throws omero.ApiUsageException
     */
    public OriginalFile createUserFile(final String repoUuid,
            final CheckedPath checked, final long size, Principal currentUser)
                    throws omero.ApiUsageException {

        ome.model.core.OriginalFile of = (ome.model.core.OriginalFile)
                executor.execute(currentUser, new Executor.SimpleWork(this,
                        "createUserFile", repoUuid, checked) {

                    @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                Long fileId = getSqlAction().findRepoFile(
                        repoUuid, checked.getRelativePath(),
                        checked.getName(), null /*mimetype*/);
                if (fileId == null) {
                    return createOriginalFile(
                            sf, getSqlAction(),
                            repoUuid, checked, "FSLiteMarkerFile", size, "None");
                } else {
                    return sf.getQueryService().get(
                            ome.model.core.OriginalFile.class, fileId);
                }
            }
        });
        return (OriginalFile) new IceMapper().map(of);
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

    protected ome.model.core.OriginalFile createOriginalFile(
        ServiceFactory sf, SqlAction sql,
        String repoUuid, CheckedPath checked, String mimetype,
        long size, String sha1) {

        ome.model.core.OriginalFile ofile =
                new ome.model.core.OriginalFile();

        ofile.setPath(checked.getRelativePath());
        ofile.setName(checked.getName());
        ofile.setMimetype(mimetype);
        ofile.setSha1(sha1);
        ofile.setSize(size);

        ofile = sf.getUpdateService().saveAndReturnObject(ofile);
        sql.setFileRepo(ofile.getId(), repoUuid);
        return ofile;
    }
}
