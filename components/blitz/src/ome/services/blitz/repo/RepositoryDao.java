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
 * @since 4.5
 */
public class RepositoryDao {

    protected final Principal principal;
    protected final Executor executor;

    public RepositoryDao(Principal principal, Executor executor) {
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

    protected EventContext currentContext(Principal currentUser) {
        return (EventContext) executor.execute(currentUser,
                new Executor.SimpleWork(this, "getEventContext") {
            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {
                return ((LocalAdmin) sf.getAdminService()).getEventContextQuiet();
            }
        });
    }

}
