/*
 * ome.services.blitz.repo.PublicRepositoryI
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 *
 *
 */
package ome.services.blitz.repo;

import static omero.rtypes.rlong;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;

import ome.formats.importer.ImportConfig;
import ome.formats.importer.OMEROWrapper;
import ome.services.blitz.util.RegisterServantMessage;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;

import omero.InternalException;
import omero.ServerError;
import omero.ValidationException;
import omero.api.RawFileStorePrx;
import omero.api.RawFileStorePrxHelper;
import omero.api.RawPixelsStorePrx;
import omero.api.RawPixelsStorePrxHelper;
import omero.api._RawFileStoreTie;
import omero.api._RawPixelsStoreTie;
import omero.grid._RepositoryOperations;
import omero.grid._RepositoryTie;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.util.IceMapper;

/**
 * An implementation of he PublicRepository interface
 *
 * @author Colin Blackburn <cblackburn at dundee dot ac dot uk>
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class PublicRepositoryI implements _RepositoryOperations {

    private final static Log log = LogFactory.getLog(PublicRepositoryI.class);

    private final static IOFileFilter DEFAULT_SKIP =
            FileFilterUtils.notFileFilter(
                    FileFilterUtils.orFileFilter(new NameFileFilter(".omero"),
                            new NameFileFilter(".git")));

    private /*final*/ long id;

    protected /*final*/ File root;

    protected /*final*/ String normRoot;

    protected /*final*/ String normRootSlash;

    protected final Executor executor;

    protected final Principal principal;

    private String repoUuid;

    public PublicRepositoryI(Executor executor, Principal principal) throws Exception {
        this.executor = executor;
        this.principal = principal;
        this.repoUuid = null;
    }

    /**
     * Called by the internal repository once initialization has taken place.
     * @param fileMaker
     * @param id
     */
    public void initialize(FileMaker fileMaker, Long id, String repoUuid) throws ValidationException {
        this.id = id;
        this.root = new File(fileMaker.getDir()).getAbsoluteFile();
        this.normRoot = FilenameUtils.normalizeNoEndSeparator(root.getAbsolutePath());
        this.normRootSlash = this.normRoot + File.separator;
        if (root == null || !root.isDirectory()) {
            throw new ValidationException(null, null,
                    "Root directory must be a existing, readable directory.");
        }
        this.repoUuid = repoUuid;
    }

    /**
     * Wrap the current instance with an {@link Ice.TieBase} so that it
     * can be turned into a proxy. This is required due to the subclassing
     * between public repo instances.
     */
    public Ice.Object tie() {
        return new _RepositoryTie(this);
    }

    //
    // OriginalFile-based Interface methods
    //

    public OriginalFile root(Current __current) throws ServerError {
       final long repoId = this.id;
       ome.model.core.OriginalFile oFile = (ome.model.core.OriginalFile)  executor
           .execute(principal, new Executor.SimpleWork(this, "root") {
               @Transactional(readOnly = true)
               public Object doWork(Session session, ServiceFactory sf) {
                   return sf.getQueryService().find(ome.model.core.OriginalFile.class, repoId);
               }
           });
       return (OriginalFileI) new IceMapper().map(oFile);
    }

    //
    // Path-based Interface methods
    //

    public List<String> list(String path, Current __current) throws ServerError {
        File file = checkPath(path, __current).mustExist().file;
        List<String> contents = new ArrayList<String>();
        for (Object child : FileUtils.listFiles(file, DEFAULT_SKIP, null)) {
            contents.add(child.toString());
        }
        return contents;
    }

    public List<OriginalFile> listFiles(String path, Current __current) throws ServerError {
        File file = checkPath(path, __current).mustExist().file;
        List<OriginalFile> contents = new ArrayList<OriginalFile>();
        for (Object child_ : FileUtils.listFiles(file, DEFAULT_SKIP, null)) {
            File child = (File) child_;
            OriginalFile originalFile = new OriginalFileI();
            originalFile.setName(rstring(child.getName()));
            originalFile.setPath(rstring(path));
            originalFile.setSize(rlong(child.length()));
            originalFile.setMtime(rtime(child.lastModified()));
            contents.add(originalFile);
        }
        return contents;
    }

    /**
     * Register an OriginalFile using its path
     *
     * @param path
     *            Absolute path of the file to be registered.
     * @param mimetype
     *            Mimetype as an RString
     * @param __current
     *            ice context.
     * @return The OriginalFile with id set (unloaded)
     *
     */
    public OriginalFile register(String path, omero.RString mimetype, Current __current)
            throws ServerError {

        PathCheck check = checkPath(path, __current);
        OriginalFile omeroFile = check.createOriginalFile(mimetype);

        IceMapper mapper = new IceMapper();
        final ome.model.core.OriginalFile omeFile = (ome.model.core.OriginalFile) mapper
                .reverse(omeroFile);
        Long id = (Long) executor.execute(principal, new Executor.SimpleWork(
                this, "register", path) {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                return sf.getUpdateService().saveAndReturnObject(omeFile).getId();
            }
        });

        omeroFile.setId(rlong(id));
        omeroFile.unload();
        return omeroFile;

    }

    public boolean delete(String path, Current __current) throws ServerError {
        return checkPath(path, __current).mustEdit().delete();
    }

    public List<String> deleteFiles(String[] files, Current __current) throws ServerError {
        List<String> undeleted = new ArrayList<String>();
        for (String path : files) {
            PathCheck check = checkPath(path, __current).mustEdit();
            boolean deleted = check.delete();
            if (!deleted) {
                undeleted.add(path);
            } else {
                // Now we attempt to clean up any parent directories that are
                // 1) empty, 2) not the root directory and 3) editable.
                check = check.parent();
                while (check != null) {
                    if (check.isRoot || !check.canEdit()) {
                        break;
                    }
                    check.delete();
                    check = check.parent();
                }
            }
        }
        return undeleted;
    }

    /**
     * Get the mimetype for a file.
     *
     * @param path
     *            A path on a repository.
     * @param __current
     *            ice context.
     * @return mimetype
     *
     */
    public String mimetype(String path, Current __current) throws ServerError {
        return checkPath(path, __current).mustExist().getMimetype();
    }

    /**
     * Return true if a file exists in the repository.
     *
     * @param path
     *            A path on a repository.
     * @param __current
     *            ice context.
     * @return The existence of the file
     *
     */
    public boolean fileExists(String path, Current __current) throws ServerError {
        File file = checkPath(path, __current).file;
        return file.exists();
    }

    /**
     * Create a file in the repository if one doesn't already exist.
     *
     * @param path
     *            A path on a repository.
     * @param __current
     *            ice context.
     * @return The creation of the file (false means file already exists).
     *
     */
    public boolean create(String path, Current __current) throws ServerError {
        File file = checkPath(path, __current).file;
        try {
            FileUtils.touch(file);
            return true;
        } catch (Exception e) {
            throw new omero.InternalException(stackTraceAsString(e), null, e.getMessage());
        }
    }

    public RawPixelsStorePrx pixels(String path, Current __current) throws ServerError {
        checkPath(path, __current).mustDB();

        // See comment below in RawFileStorePrx
        Ice.Current adjustedCurr = new Ice.Current();
        adjustedCurr.ctx = __current.ctx;
        adjustedCurr.operation = __current.operation;
        String sessionUuid = __current.ctx.get(omero.constants.SESSIONUUID.value);
        adjustedCurr.id = new Ice.Identity(__current.id.name, sessionUuid);

        BfPixelsStoreI rps;
        try {
            // FIXME ImportConfig should be injected
            rps = new BfPixelsStoreI(path,
                    new OMEROWrapper(new ImportConfig()).getImageReader());
        } catch (Throwable t) {
            if (t instanceof ServerError) {
                throw (ServerError) t;
            } else {
                omero.InternalException ie = new omero.InternalException();
                IceMapper.fillServerError(ie, t);
                throw ie;
            }
        }

        // See comment below in RawFileStorePrx
        _RawPixelsStoreTie tie = new _RawPixelsStoreTie(rps);
        RegisterServantMessage msg = new RegisterServantMessage(this, tie, adjustedCurr);
        try {
            this.executor.getContext().publishMessage(msg);
        } catch (Throwable t) {
            if (t instanceof ServerError) {
                throw (ServerError) t;
            } else {
                omero.InternalException ie = new omero.InternalException();
                IceMapper.fillServerError(ie, t);
                throw ie;
            }
        }
        Ice.ObjectPrx prx = msg.getProxy();
        if (prx == null) {
            throw new omero.InternalException(null, null, "No ServantHolder for proxy.");
        }
        return RawPixelsStorePrxHelper.uncheckedCast(prx);
    }

    public RawFileStorePrx file(String path, String mode, Current __current) throws ServerError {
        PathCheck check = checkPath(path, __current);
        return createRepoRFS(check.file, mode, null, __current);
    }

    public RawFileStorePrx fileById(long fileId, Current __current) throws ServerError {
        Principal currentUser = currentUser(__current);
        File file = getFile(fileId, currentUser);
        if (file == null) {
            return null;
        }
        return createRepoRFS(file, null, fileId, __current);
    }

    /**
     * Create, initialize, and register an {@link RepoRawFileStoreI}
     * with the proper setting (read or write).
     *
     * @param file The file that will be read. Can't be null.
     * @param mode The mode for writing. If null, read-only.
     * @param fileId The id to read. Can't be null if mode is null.
     * @param __current The current user's session information.
     * @return A proxy ready to be returned to the user.
     * @throws ServerError
     * @throws InternalException
     */
    protected RawFileStorePrx createRepoRFS(File file, String mode, Long fileId,
            Current __current) throws ServerError, InternalException {

        // WORKAROUND: See the comment in RawFileStoreI.
        // The most likely correction of this
        // is to have PublicRepositories not be global objects, but be created
        // on demand for each session via SharedResourcesI
        final String sessionUuid = __current.ctx.get(omero.constants.SESSIONUUID.value);
        final Ice.Current adjustedCurr = new Ice.Current();
        adjustedCurr.ctx = __current.ctx;
        adjustedCurr.operation = __current.operation;
        adjustedCurr.id = new Ice.Identity(__current.id.name, sessionUuid);

        RepoRawFileStoreI rfs;
        try {
            if (mode != null) {
                rfs = new RepoRawFileStoreI(file.getAbsolutePath(), mode);
            } else {
                rfs = new RepoRawFileStoreI(fileId, file);
            }
            rfs.setApplicationContext(executor.getContext());
        } catch (Throwable t) {
            if (t instanceof ServerError) {
                throw (ServerError) t;
            } else {
                omero.InternalException ie = new omero.InternalException();
                IceMapper.fillServerError(ie, t);
                throw ie;
            }
        }

        // TODO: Refactor all this into a single helper method.
        // If there is no listener available who will take responsibility
        // for this servant, then we bail.
        final _RawFileStoreTie tie = new _RawFileStoreTie(rfs);
        final RegisterServantMessage msg = new RegisterServantMessage(this, tie, adjustedCurr);
        try {
            this.executor.getContext().publishMessage(msg);
            rfs.setHolder(msg.getHolder());
        } catch (Throwable t) {
            if (t instanceof ServerError) {
                throw (ServerError) t;
            } else {
                omero.InternalException ie = new omero.InternalException();
                IceMapper.fillServerError(ie, t);
                throw ie;
            }
        }
        Ice.ObjectPrx prx = msg.getProxy();
        if (prx == null) {
            throw new omero.InternalException(null, null, "No ServantHolder for proxy.");
        }
        return RawFileStorePrxHelper.uncheckedCast(prx);

    }

    /**
     * Create a nested path in the repository. Creates each directory
     * in the path is it doen't already exist. Silently returns if
     * the directory already exists.
     *
     * @param path
     *            A path on a repository.
     * @param __current
     *            ice context.
     */
    public void makeDir(String path, Current __current) throws ServerError {
        File file = checkPath(path, __current).file;
        try {
            FileUtils.forceMkdir(file);
        } catch (Exception e) {
            throw new omero.InternalException(stackTraceAsString(e), null, e.getMessage());
        }
    }

    //
    //
    // Utility methods
    //
    //

    /**
     * Create a new {@link PathCheck} object based on the given user input.
     * This method is included to allow subclasses a change to introduce their
     * own {@link PathCheck} implementations.
     *
     * @param path
     *            A path on a repository.
     *
     */
    protected PathCheck checkPath(final String path, final Ice.Current curr)
            throws ValidationException {
        return new PathCheck(executor, curr, path, normRoot, normRootSlash, root);
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
    private File getFile(final long id, final Principal currentUser) {
        return (File) executor.execute(currentUser, new Executor.SimpleWork(this, "getFile", id) {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                            String path = getSqlAction().findRepoFilePath(
                                    repoUuid, id);

                            if (path == null) {
                                return null;
                            }

                            return new File(root, path);
                    }
                });
    }

    // Utility function for passing stack traces back in exceptions.
    protected String stackTraceAsString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    protected Principal currentUser(Current __current) {
        return new Principal(__current.ctx.get(omero.constants.SESSIONUUID.value));
    }

}
