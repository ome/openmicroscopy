package ome.services.blitz.repo;

import java.io.File;

import ome.system.Principal;

import omero.ServerError;
import omero.model.OriginalFile;
import omero.sys.EventContext;

import Ice.Current;

public interface RepositoryDao {

    OriginalFile getOriginalFile(final long repoId);

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
    OriginalFile register(OriginalFile omeroFile, omero.RString mimetype,
            Current __current) throws ServerError;

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