package ome.services.blitz.repo;

import java.util.List;

import org.hibernate.Session;
import Ice.Current;

import ome.api.RawFileStore;
import ome.io.nio.FileBuffer;
import ome.services.RawFileBean;
import ome.services.blitz.repo.path.FsFile;
import ome.system.ServiceFactory;
import ome.util.SqlAction;
import ome.util.SqlAction.DeleteLog;

import omero.RMap;
import omero.SecurityViolation;
import omero.ServerError;
import omero.model.ChecksumAlgorithm;
import omero.model.Fileset;
import omero.model.IObject;
import omero.model.Job;
import omero.model.OriginalFile;
import omero.sys.EventContext;

public interface RepositoryDao {

    /**
     * Create a {@link RawFileBean} (i.e. an implementation of
     * {@link ome.api.RawFileStore} which can be passed to
     * {@link RepoRawFileStore} for performing internal functions. The primary
     * difference to an instance created via the ServiceFactory is that
     * the {@link RawFileBean#setFileIdWithBuffer(FileBuffer)} method is called
     * pointing to a non-romio file path, e.g. /OMERO/Files/x.
     *
     * @param fileId ID of an {@link OriginalFile}
     * @param checked Not null. Normalized path from the repository.
     * @param mode FileChannel mode, "r", "rw", etc.
     * @return An instance with
     *      {@link RawFileBean#setFileIdWithBuffer(FileBuffer)} called.
     */
    RawFileStore getRawFileStore(long fileId, CheckedPath checked, String mode,
            final Ice.Current current) throws SecurityViolation;

    /**
     * Delegate to {@link ome.util.SqlAction#findRepoFile(String, String, String, String)}
     * for looking up the id of the file, and then load it normally via
     * IQuery. This will enforce any read security checks.
     * @param uuid
     * @param dirname
     * @param basename
     * @param mimetype
     * @return
     */
    OriginalFile findRepoFile(String uuid, CheckedPath checked,
            String mimetype, Ice.Current current) throws ServerError;

    /**
     * As {@link #findRepoFile(String, CheckedPath, String, Current)} but
     * can be called from within a transaction.
     */
    ome.model.core.OriginalFile findRepoFile(ServiceFactory sf,
            SqlAction sql, final String uuid, final CheckedPath checked,
            final String mimetype);

    /*
     * Look up all original files at a given path, recursively, in a single
     * transaction.
     *
     * @param repoUuid
     * @param checked
     * @param __current
     * @return
     * @throws ServerError
     */
    RMap treeList(String repoUuid, CheckedPath checked, Current __current)
            throws ServerError;

    /**
     * Checks that the given {@link CheckedPath} objects exist (via
     * {@link #findRepoFile(String, CheckedPath, String, Ice.Current)})
     * and are in the "user" group. If they don't exist, they are created; and
     * if they aren't in the "user" group, they are moved.
     */
    void createOrFixUserDir(String uuid,
            List<CheckedPath> path, Session s, ServiceFactory sf, SqlAction sql)
        throws ServerError;

    /**
     * Delegates to IAdmin#canUpdate
     * @param fileId
     * @param current
     * @throws an {@link omero.SecurityViolation} if the currentUser is not
     *      allowed to access the given file.
     * @return
     */
    boolean canUpdate(IObject obj, Ice.Current current);

    /**
     * Find the original file IDs among those given that are in the given repository.
     * @param repo a repository UUID
     * @param ids IDs of original files
     * @param Ice method invocation context
     * @return those IDs among those given whose original files are in the given repository
     */
    List<Long> filterFilesByRepository(String repo, List<Long> ids, Ice.Current current);

    /**
     * Gets the original file instance for a given file ID.
     * @param fileId a file ID
     * @param current applicable ICE context
     * @return the original file corresponding to the given file ID
     * @throws SecurityViolation if the query threw a security violation
     */
    OriginalFile getOriginalFile(long fileId, Ice.Current current)
            throws SecurityViolation;

    /**
     * Return a non-null, possibly empty list of {@link OriginalFile} elements
     * which are accessible to the given user at the given path. If the
     * directory which they are associated with is not also readable by the
     * current user, then a {@link SecurityViolation} will be thrown.
     *
     * @param uuid for the repository in question.
     * @param checked normalized path which can be found as the value of
     *      {@link OriginalFile#getPath()} in the database.
     * @param current
     */
    List<OriginalFile> getOriginalFiles(String repoUuid, CheckedPath checked,
            Ice.Current current) throws SecurityViolation;

    /**
     * Fill the various fields of the {@link Fileset} and then save the
     * entire instance into the database.
     *
     * @param repoUuid for the repository in question.
     * @param fs a user provided {@link Fileset} that must minimally have the
     *    {@link FilesetEntry} objects present with their clientPath set. The rest
     *    of the fields will be filled here.
     * @param paths a List of the same size as the number of entries in fs
     *    one per {@link FilesetEntry}.
     * @param currentUser
     */
    Fileset saveFileset(String repoUuid, Fileset fs, ChecksumAlgorithm checksumAlgorithm,
            List<CheckedPath> paths, Ice.Current current) throws ServerError;

    /**
     * Load filesets by id.
     * @param ids
     * @param current
     * @return
     */
    List<Fileset> loadFilesets(List<Long> ids, Ice.Current current)
            throws ServerError;

    /**
     * Register an OriginalFile object
     *
     * @param repoUuid
     *            uuid of the repository that the given file argument should be
     *            registered with. Cannot be null.
     * @param checked
     *            Normalized path provided by the repository. Not null.
     * @param mimetype
     *            Mimetype for use with the OriginalFile. May be null in which
     *            case a default will be chosen.
     * @param current
     *            Not null.
     * @return The OriginalFile with id set (unloaded)
     * @throws ServerError
     *
     */
    OriginalFile register(String repoUuid, CheckedPath checked, String mimetype,
            final Ice.Current current) throws ServerError;

    /**
     * Like {@link #register(String, CheckedPath, String, Ice.Current) but
     * does not create a new transaction. Instead, the {@link ServiceFactory}
     * and {@link SqlAction} must be passed in. The returned OriginalFile
     * instance is still connected to Hibernate.
     *
     * @param repoUuid
     * @param checked
     * @param mimetype
     * @param sf
     * @param sql
     * @return
     * @throws ServerError
     */
    public ome.model.core.OriginalFile register(String repoUuid, CheckedPath checked,
            final String mimetype, final ServiceFactory sf, final SqlAction sql)
                    throws ServerError;

    /**
     * Get an {@link FsFile} object based on its ID. Returns null if
     * the file does not exist or does not belong to this repository.
     *
     * @param id database ID of the sought original file
     * @param current current applicable ICE context
     * @param repoUuid the UUID of the repository containing the original file
     * @return the requested FsFile object
     *
     */
    FsFile getFile(final long id, final Ice.Current current,
            final String repoUuid);

    /**
     * Create a job from an instance provided by either the client or the
     * server. Only those fields which are modifiable by the user will be
     * copied.
     *
     * @param job Not null.
     * @param current Not null.
     * @return
     * @throws ServerError
     */
    <T extends Job> T saveJob(T job, Ice.Current current) throws ServerError;

    /**
     * Set both the message and the status of the given job.
     *
     * @param job Not null.
     * @param message If null, no modification will be made for the message
     * @param status If null, no modification will be made for the status.
     */
    void updateJob(Job job, String message, String status, Ice.Current current)
        throws ServerError;

    /**
     * Look up information for the current session as specified in the ctx
     * field of the current.
     */
    EventContext getEventContext(final Ice.Current current);

    /**
     * Get the current user's institution.
     * @param userId the ID of the user whose institution is to be fetched
     * @param current the current ICE method invocation context
     * @return the institution, may be {@code null}
     */
    String getUserInstitution(long userId, Ice.Current current);

    /**
     * Get the current user's institution.
     * @param userId the ID of the user whose institution is to be fetched
     * @param sf the service factory to use for the query
     * @return the institution, may be {@code null}
     */
    String getUserInstitution(long userId, ServiceFactory sf);

    /**
     * Call {@link SqlAction.findRepoDeleteLogs(DeleteLog)} with the current
     * context.
     *
     * @param template not null.
     * @param current not null.
     * @return all the rows found which match the non-null fields on templates
     */
    List<DeleteLog> findRepoDeleteLogs(DeleteLog template, final Ice.Current current);

    /**
     * Call {@link SqlAction.deleteRepoDeleteLogs(DeleteLog)} with the current
     * context.
     *
     * @param template not null.
     * @param current not null.
     * @return the number of rows deleted
     */
    int deleteRepoDeleteLogs(DeleteLog template, final Ice.Current current);

    /**
     * Create a number of directories in a single transaction, using the
     * {@link PublicRepositoryI} instance as a callback for implementation
     * specific logic. Applies the <q>real user</q>'s event context when
     * within a {@link PublicRepositoryI#sudo(Current, String)}.
     */
    void makeDirs(PublicRepositoryI repo, List<CheckedPath> dirs, boolean parents,
            Ice.Current c) throws ServerError;

    /**
     * Retrieve the checksum algorithm of the given name.
     * @param name a checksum algorithm name, must exist
     * @param Ice method invocation context
     * @return the corresponding checksum algorithm model object
     */
    ome.model.enums.ChecksumAlgorithm getChecksumAlgorithm(String name, Ice.Current current);

    /**
     * Retrieve the original file of the given ID.
     * @param id the ID of an original file, must exist
     * @param Ice method invocation context
     * @return the corresponding original file model object
     */
    ome.model.core.OriginalFile getOriginalFileWithHasher(long id, Ice.Current current);

    /**
     * Save the given model object.
     * @param object a model object
     * @param Ice method invocation context
     * @return {@code null}
     */
    void saveObject(ome.model.IObject object, Ice.Current current);
}
