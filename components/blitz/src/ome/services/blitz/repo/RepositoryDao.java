package ome.services.blitz.repo;

import java.io.File;
import java.util.List;

import ome.api.RawFileStore;
import ome.io.nio.FileBuffer;
import ome.services.RawFileBean;
import ome.services.blitz.repo.path.ServerFilePathTransformer;
import ome.util.SqlAction.DeleteLog;

import omero.SecurityViolation;
import omero.ServerError;
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
     * Checks that the given {@link CheckedPath} object exists (via
     * {@link #findRepoFile(String, CheckedPath, String, Ice.Current)})
     * and is in the "user" group. If it doesn't exist, it is created; and if
     * it isn't in the "user" group, it is moved.
     */
    void createOrFixUserDir(String uuid, CheckedPath path, Ice.Current current)
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
    Fileset saveFileset(String repoUuid, Fileset fs, List<CheckedPath> paths,
            Ice.Current current) throws ServerError;

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
     * Get an {@link OriginalFile} object based on its id. Returns null if
     * the file does not exist or does not belong to this repo.
     *
     * @param id
     *            long, db id of original file.
     * @return OriginalFile object.
     *
     */
    File getFile(final long id, final Ice.Current current,
            final String repoUuid, final ServerFilePathTransformer serverPaths);

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

}