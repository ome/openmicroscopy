package ome.services.blitz.repo;

import static omero.rtypes.rlong;

import java.io.File;

import ome.api.local.LocalAdmin;
import ome.services.util.Executor;
import ome.system.EventContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import omero.ServerError;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.util.IceMapper;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;

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

    public OriginalFile getOriginalFile(final long repoId) {
        ome.model.core.OriginalFile oFile = (ome.model.core.OriginalFile)  executor
                .execute(principal, new Executor.SimpleWork(this, "root") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        return sf.getQueryService().find(ome.model.core.OriginalFile.class, repoId);
                    }
                });
            return (OriginalFileI) new IceMapper().map(oFile);
    }

    /**
     * Register an OriginalFile using its path
     *
     * @param checkedPath
     *            CheckedPath object wrapper around the path string.
     * @param path
     *            Absolute path of the file to be registered.
     * @param mimetype
     *            Mimetype as an RString.
     * @param __current
     *            ice context.
     * @return The OriginalFile with id set (unloaded)
     * @throws ServerError
     *
     */
    public OriginalFile register(OriginalFile omeroFile,
            omero.RString mimetype, Current __current) throws ServerError {
        IceMapper mapper = new IceMapper();
        final ome.model.core.OriginalFile omeFile = (ome.model.core.OriginalFile) mapper
                .reverse(omeroFile);
        Long id = (Long) executor.execute(principal, new Executor.SimpleWork(
                this, "register", omeroFile.getPath().getValue()) {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                return sf.getUpdateService().saveAndReturnObject(omeFile).getId();
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
            final String path, final String name, Principal currentUser)
                    throws omero.ApiUsageException {

        ome.model.core.OriginalFile of = (ome.model.core.OriginalFile)
                executor.execute(currentUser, new Executor.SimpleWork(this,
                        "createUserDirectory", repoUuid, path, name) {

                    @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                Long fileId = getSqlAction().findRepoFile(
                        repoUuid, path, name, null /*mimetype*/);
                if (fileId == null) {
                    ome.model.core.OriginalFile ofile =
                            new ome.model.core.OriginalFile();
                    ofile.setPath(path);
                    ofile.setName(name);
                    ofile.setMimetype("Directory");
                    ofile.setSha1("None");
                    ofile.setSize(0L);

                    ofile = sf.getUpdateService().saveAndReturnObject(ofile);
                    getSqlAction().setFileRepo(ofile.getId(), repoUuid);
                    return ofile;
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
            final String path, final String name, final long size, Principal currentUser)
                    throws omero.ApiUsageException {

        ome.model.core.OriginalFile of = (ome.model.core.OriginalFile)
                executor.execute(currentUser, new Executor.SimpleWork(this,
                        "createUserFile", repoUuid, path, name) {

                    @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                Long fileId = getSqlAction().findRepoFile(
                        repoUuid, path, name, null /*mimetype*/);
                if (fileId == null) {
                    ome.model.core.OriginalFile ofile =
                            new ome.model.core.OriginalFile();
                    ofile.setPath(path);
                    ofile.setName(name);
                    ofile.setMimetype("FSLiteMarkerFile");
                    ofile.setSha1("None");
                    ofile.setSize(size);

                    ofile = sf.getUpdateService().saveAndReturnObject(ofile);
                    getSqlAction().setFileRepo(ofile.getId(), repoUuid);
                    return ofile;
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

//    protected String getCurrentUserName(Ice.Current curr) {
//        return getEventContext(curr).userName;
//    }
}
