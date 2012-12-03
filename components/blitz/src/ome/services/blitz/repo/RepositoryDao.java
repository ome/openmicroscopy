package ome.services.blitz.repo;

import java.io.File;
import java.util.List;

import ome.api.RawFileStore;
import ome.io.nio.FileBuffer;
import ome.services.RawFileBean;
import ome.system.Principal;

import omero.SecurityViolation;
import omero.ServerError;
import omero.model.IObject;
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
     * @param file {@link java.io.File} pointing to the given path.
     * @param mode FileChannel mode, "r", "rw", etc.
     * @return An instance with
     *      {@link RawFileBean#setFileIdWithBuffer(FileBuffer)} called.
     */
    RawFileStore getRawFileStore(final long fileId, final File file, String mode,
            final Principal currentUser) throws SecurityViolation;

    /**
     * Delegate to {@link ome.util.SqlAction#findRepoFile(String, String, String, String)}
     * @param uuid
     * @param dirname
     * @param basename
     * @param mimetype
     * @return
     */
    Long findRepoFile(String uuid, String dirname, String basename, String mimetype, Principal currentUser);

    /**
     * Delegates to IAdmin#canUpdate
     * @param fileId
     * @param currentUser
     * @throws an {@link omero.SecurityViolation} if the currentUser is not
     *      allowed to access the given file.
     * @return
     */
    boolean canUpdate(IObject obj, Principal currentUser);

    OriginalFile getOriginalFile(long fileId, Principal currentUser)
            throws SecurityViolation;


    /**
     * Return a non-null, possibly empty list of {@link OriginalFile} elements
     * which are accessible to the given user at the given path. If the
     * directory which they are associated with is not also readable by the
     * current user, then a {@link SecurityViolation} will be thrown.
     *
     * @param uuid for the repository in question.
     * @param path normalized path which can be found as the value of
     *      {@link OriginalFile#getPath()} in the database.
     * @param currentUser
     */
    List<OriginalFile> getOriginalFiles(String repoUuid, String path, Principal currentUser)
        throws SecurityViolation;

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
    OriginalFile register(OriginalFile omeroFile, String repoUuid,
            final Principal currentUser) throws ServerError;

    /**
     * Get an {@link OriginalFile} object based on its id. Returns null if
     * the file does not exist or does not belong to this repo.
     *
     * @param id
     *            long, db id of original file.
     * @return OriginalFile object.
     *
     */
    File getFile(final long id, final Principal currentUser,
            final String repoUuid, final CheckedPath root);

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
    OriginalFile createUserDirectory(final String repoUuid, final String path,
            final String name, Principal currentUser)
            throws omero.ApiUsageException;

    /**
     * Create an {@link OriginalFile} in the given repository if it does
     * not exist. Otherwise, return the id.
     *
     * @param repoUuid Not null. sha1 of the repository
     * @param path Not null. {@link OriginalFile#getPath()}
     * @param name Not null. {@link OriginalFile#getName()}
     * @param size {@link OriginalFile#getSize()}
     * @param currentUser Not null.
     * @return ID of the object.
     * @throws omero.ApiUsageException
     */
    OriginalFile createUserFile(final String repoUuid, final String path,
            final String name, final long size, Principal currentUser)
            throws omero.ApiUsageException;

    /**
     * Look up information for the current session as specified in the ctx
     * field of the current.
     */
    EventContext getEventContext(final Ice.Current current);

}