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

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import Ice.Current;

import ome.api.RawFileStore;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.OMEROWrapper;
import ome.services.blitz.impl.AbstractAmdServant;
import ome.services.blitz.impl.ServiceFactoryI;
import ome.services.blitz.repo.path.FsFile;
import ome.services.blitz.repo.path.MakePathComponentSafe;
import ome.services.blitz.repo.path.ServerFilePathTransformer;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.FindServiceFactoryMessage;
import ome.services.blitz.util.RegisterServantMessage;
import ome.system.OmeroContext;
import ome.util.checksum.ChecksumProviderFactory;
import ome.util.messages.InternalMessage;

import omero.InternalException;
import omero.RLong;
import omero.RMap;
import omero.RType;
import omero.ResourceError;
import omero.SecurityViolation;
import omero.ServerError;
import omero.ValidationException;
import omero.api.RawFileStorePrx;
import omero.api.RawFileStorePrxHelper;
import omero.api.RawPixelsStorePrx;
import omero.api.RawPixelsStorePrxHelper;
import omero.api._RawFileStoreTie;
import omero.api._RawPixelsStoreTie;
import omero.cmd.AMD_Session_submit;
import omero.cmd.Delete;
import omero.cmd.DoAll;
import omero.cmd.HandlePrx;
import omero.cmd.Request;
import omero.grid._RepositoryOperations;
import omero.grid._RepositoryTie;
import omero.model.ChecksumAlgorithm;
import omero.model.OriginalFile;
import omero.util.IceMapper;

/**
 * An implementation of the PublicRepository interface.
 *
 * @author Colin Blackburn <cblackburn at dundee dot ac dot uk>
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class PublicRepositoryI implements _RepositoryOperations, ApplicationContextAware {

    public static class AMD_submit implements AMD_Session_submit {

        HandlePrx ret;

        Exception ex;

        public void ice_response(HandlePrx __ret) {
            this.ret = __ret;
        }

        public void ice_exception(Exception ex) {
            this.ex = ex;
        }

    }

    private final static Logger log = LoggerFactory.getLogger(PublicRepositoryI.class);

    private final static IOFileFilter DEFAULT_SKIP =
            FileFilterUtils.notFileFilter(
                    FileFilterUtils.orFileFilter(new NameFileFilter(".omero"),
                            new NameFileFilter(".git")));

    /**
     * Mimetype used to connote a directory {@link OriginalFile} object.
     */
    public static String DIRECTORY_MIMETYPE = "Directory";

    private /*final*/ long id;

    protected /*final*/ ServerFilePathTransformer serverPaths;

    protected final RepositoryDao repositoryDao;

    protected final ChecksumProviderFactory checksumProviderFactory;

    protected final omero.model.ChecksumAlgorithm checksumAlgorithm;

    protected OmeroContext context;

    private String repoUuid;

    public PublicRepositoryI(RepositoryDao repositoryDao,
            ChecksumProviderFactory checksumProviderFactory,
            omero.model.ChecksumAlgorithm checksumAlgorithm) throws Exception {
        this.repositoryDao = repositoryDao;
        this.checksumProviderFactory = checksumProviderFactory;
        this.checksumAlgorithm = checksumAlgorithm;
        this.repoUuid = null;
    }

    /**
     * Called by the internal repository once initialization has taken place.
     * @param fileMaker
     * @param id
     */
    public void initialize(FileMaker fileMaker, Long id, String repoUuid) throws ValidationException {
        this.id = id;
        File root = new File(fileMaker.getDir());
        if (!root.isDirectory()) {
            throw new ValidationException(null, null,
                    "Root directory must be a existing, readable directory.");
        }
        this.repoUuid = repoUuid;
        this.serverPaths = new ServerFilePathTransformer();
        this.serverPaths.setBaseDirFile(root);
        this.serverPaths.setPathSanitizer(new MakePathComponentSafe());
    }

    /**
     * Wrap the current instance with an {@link Ice.TieBase} so that it
     * can be turned into a proxy. This is required due to the subclassing
     * between public repo instances.
     */
    public Ice.Object tie() {
        return new _RepositoryTie(this);
    }

    public String getRepoUuid() {
        return repoUuid;
    }

    //
    // OriginalFile-based Interface methods
    //

    public OriginalFile root(Current __current) throws ServerError {
        return this.repositoryDao.getOriginalFile(this.id, __current);
    }

    //
    // Path-based Interface methods
    //

    public boolean fileExists(String path, Current __current) throws ServerError {
        final CheckedPath checked = checkPath(path, null, __current);
        final OriginalFile ofile =
                repositoryDao.findRepoFile(repoUuid, checked, null, __current);
        return (ofile != null);
    }

    public List<String> list(String path, Current __current) throws ServerError {
        List<OriginalFile> ofiles = listFiles(path, __current);
        List<String> contents = new ArrayList<String>(ofiles.size());
        for (OriginalFile ofile : ofiles) {
            contents.add(ofile.getPath().getValue() + ofile.getName().getValue());
        }
        return contents;
    }

    public List<OriginalFile> listFiles(String path, Current __current) throws ServerError {
        final CheckedPath checked = checkPath(path, null, __current).mustExist();
        return repositoryDao.getOriginalFiles(repoUuid, checked, __current);
    }

    public RMap treeList(String path, Current __current) throws ServerError {
        final CheckedPath checked = checkPath(path, null, __current);
        return repositoryDao.treeList(repoUuid, checked, __current);
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
    public OriginalFile register(String path, omero.RString mimetype,
            Current __current) throws ServerError {
        final CheckedPath checked = checkPath(path, null, __current);
        return this.repositoryDao.register(repoUuid, checked,
                mimetype == null ? null : mimetype.getValue(), __current);
    }

    /**
     * Delete paths recursively as described in Repositories.ice. Internally
     * uses {@link #treeList(String, Ice.Current)} to build the recursive
     * list of files.
     *
     * @param files non-null, preferably non-empty list of files to check.
     * @param recursive See Repositories.ice for an explanation
     * @param force See Repositories.ice for an explanation
     * @param __current Non-null ice context.
     */
    public HandlePrx deletePaths(String[] files, boolean recursive, boolean force,
            Current __current) throws ServerError {

        // TODO: This could be refactored to be the default in shared servants
        final Ice.Current adjustedCurr = makeAdjustedCurrent(__current);
        final String allId = DoAll.ice_staticId();
        final String delId = Delete.ice_staticId();
        final DoAll all = (DoAll) getFactory(allId, adjustedCurr).create(allId);
        final Ice.ObjectFactory delFactory = getFactory(delId, adjustedCurr);
        final List<Request> commands = new ArrayList<Request>();
        all.requests = commands;

        for (String path : files) {
            // treeList() calls checkedPath
            RMap map = treeList(path, __current);
            _deletePaths(delFactory, map, commands);
        }

        final FindServiceFactoryMessage msg
            = new FindServiceFactoryMessage(this, adjustedCurr);
        publishMessage(msg);
        final ServiceFactoryI sf = msg.getServiceFactory();

        AMD_submit submit = submitRequest(sf, all, adjustedCurr);
        return submit.ret;
    }

    private void _deletePaths(Ice.ObjectFactory delFactory, RMap map, List<Request> commands) {
        if (map != null && map.getValue() != null) {
            // Each of the entries
            for (RType value : map.getValue().values()) {
                // We know that the value for any key at the
                // "top" level is going to be a RMap
                RMap val = (RMap) value;
                if (val != null && val.getValue() != null) {
                    if (val.getValue().containsKey("files")) {
                        // then we need to recurse. files points to the next
                        // "top" level.
                        RMap files = (RMap) val.getValue().get("files");
                        _deletePaths(delFactory, files, commands);
                    }
                    // Now after we've recursed, do the actual delete.
                    RLong id = (RLong) val.getValue().get("id");
                    Delete del = (Delete) delFactory.create(null);
                    del.type = "/OriginalFile";
                    del.id = id.getValue();
                    commands.add(del);
                }
            }
        }
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
        return checkPath(path, null, __current).mustExist().getMimetype();
    }

    public RawPixelsStorePrx pixels(String path, Current __current) throws ServerError {
        final CheckedPath checked = checkPath(path, null, __current);

        // See comment below in RawFileStorePrx
        Ice.Current adjustedCurr = makeAdjustedCurrent(__current);


        // Check that the file is in the DB and has minimally "r" permissions
        // Sets the ID value on the checked object.
        findInDb(checked, "r", adjustedCurr);

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
        publishMessage(msg);
        Ice.ObjectPrx prx = msg.getProxy();
        if (prx == null) {
            throw new omero.InternalException(null, null, "No ServantHolder for proxy.");
        }
        return RawPixelsStorePrxHelper.uncheckedCast(prx);
    }

    public RawFileStorePrx file(String path, String mode, Current __current) throws ServerError {
        final CheckedPath check = checkPath(path, null, __current);
        findOrCreateInDb(check, mode, __current);
        return createRepoRFS(check, mode, __current);
    }

    public RawFileStorePrx fileById(long fileId, Current __current) throws ServerError {
        CheckedPath checked = checkId(fileId, __current);
        return createRepoRFS(checked, "r", __current);
    }

    /**
     * Find the given path in the DB or create.
     *
     * "requiresWrite" is set to true unless the mode is "r". If requiresWrite
     * is true, then the caller needs the file to be modifiable (both on disk
     * and the DB). If this doesn't hold, then a SecurityViolation will be thrown.
     */
    protected OriginalFile findInDb(CheckedPath checked, String mode,
            Ice.Current current) throws ServerError {

        final OriginalFile ofile =
                repositoryDao.findRepoFile(repoUuid, checked, null, current);

        if (ofile == null) {
            return null; // EARLY EXIT!
        }

        boolean requiresWrite = true;
        if ("r".equals(mode)) {
            requiresWrite = false;
        }

        checked.setId(ofile.getId().getValue());
        boolean canUpdate = repositoryDao.canUpdate(ofile, current);
        if (requiresWrite && !canUpdate) {
            throw new omero.SecurityViolation(null, null,
                    "requiresWrite is true but cannot modify");
        }

        return ofile;
    }

    protected OriginalFile findOrCreateInDb(CheckedPath checked, String mode,
            Ice.Current curr) throws ServerError {

        OriginalFile ofile = findInDb(checked, mode, curr);
        if (ofile != null) {
            return ofile;
        }

        if (checked.exists()) {
            omero.grid.UnregisteredFileException ufe
                = new omero.grid.UnregisteredFileException();
            ofile = (OriginalFile) new IceMapper().map(checked.asOriginalFile(null));
            ufe.file = ofile;
            throw ufe;
        }

        ofile = repositoryDao.register(repoUuid, checked, null, curr);
        checked.setId(ofile.getId().getValue());
        return ofile;
    }

    protected Ice.Current makeAdjustedCurrent(Ice.Current __current) {
        // WORKAROUND: See the comment in RawFileStoreI.
        // The most likely correction of this
        // is to have PublicRepositories not be global objects, but be created
        // on demand for each session via SharedResourcesI
        final String sessionUuid = __current.ctx.get(omero.constants.SESSIONUUID.value);
        final Ice.Current adjustedCurr = new Ice.Current();
        adjustedCurr.ctx = __current.ctx;
        adjustedCurr.adapter = __current.adapter;
        adjustedCurr.operation = __current.operation;
        adjustedCurr.id = new Ice.Identity(__current.id.name, sessionUuid);
        return adjustedCurr;
    }

    /**
     * Create, initialize, and register an {@link RepoRawFileStoreI}
     * with the proper setting (read or write).
     *
     * @param checked The file that will be read. Can't be null,
     *          and must have ID set.
     * @param mode The mode for writing. If null, read-only.
     * @param __current The current user's session information.
     * @return A proxy ready to be returned to the user.
     * @throws ServerError
     * @throws InternalException
     */
    protected RawFileStorePrx createRepoRFS(CheckedPath checked, String mode,
            Current __current) throws ServerError, InternalException {


        final Ice.Current adjustedCurr = makeAdjustedCurrent(__current);
        final BlitzExecutor be =
                context.getBean("throttlingStrategy", BlitzExecutor.class);

        RepoRawFileStoreI rfs;
        try {
            final RawFileStore service = repositoryDao.getRawFileStore(
                    checked.getId(), checked, mode, __current);
            rfs = new RepoRawFileStoreI(be, service, adjustedCurr);
            rfs.setApplicationContext(this.context);
        } catch (Throwable t) {
            if (t instanceof ServerError) {
                throw (ServerError) t;
            } else {
                omero.InternalException ie = new omero.InternalException();
                IceMapper.fillServerError(ie, t);
                throw ie;
            }
        }

        final _RawFileStoreTie tie = new _RawFileStoreTie(rfs);
        Ice.ObjectPrx prx = registerServant(tie, rfs, adjustedCurr);
        return RawFileStorePrxHelper.uncheckedCast(prx);

    }

    /**
     * Registers the given tie/servant combo with the service factory connected
     * to the current connection. If none is found, and exception will be
     * thrown. Once the tie/servant pair is registered, cleanup by the client
     * will cause this servant to be closed, etc.
     *
     * @param tie
     * @param servant
     * @param current
     * @return
     * @throws ServerError
     */
    Ice.ObjectPrx registerServant(Ice.Object tie,
            AbstractAmdServant servant, Ice.Current current)
                    throws ServerError {

        final RegisterServantMessage msg = new RegisterServantMessage(this, tie, current);
        publishMessage(msg);
        Ice.ObjectPrx prx = msg.getProxy();
        if (prx == null) {
            throw new omero.InternalException(null, null, "No ServantHolder for proxy.");
        }
        return prx;
    }

    protected void publishMessage(final InternalMessage msg)
            throws ServerError, InternalException {
        try {
            this.context.publishMessage(msg);
        } catch (Throwable t) {
            if (t instanceof ServerError) {
                throw (ServerError) t;
            } else {
                omero.InternalException ie = new omero.InternalException();
                IceMapper.fillServerError(ie, t);
                throw ie;
            }
        }
    }

    protected AMD_submit submitRequest(final ServiceFactoryI sf,
            final omero.cmd.Request req,
            final Ice.Current current) throws ServerError, InternalException {

        final AMD_submit submit = new AMD_submit();
        sf.submit_async(submit, req, current);
        if (submit.ex != null) {
            IceMapper mapper = new IceMapper();
            throw mapper.handleServerError(submit.ex, context);
        } else if (submit.ret == null) {
            throw new omero.InternalException(null, null,
                    "No handle proxy found for: " + req);
        }
        return submit;
    }

    protected Ice.ObjectFactory getFactory(String id, Ice.Current current) {
        final Ice.Communicator ic = current.adapter.getCommunicator();
        return ic.findObjectFactory(id);
    }

    /**
     * Create a nested path in the repository. Creates each directory
     * in the path is it doen't already exist. Silently returns if
     * the directory already exists.
     *
     * @param path
     *            A path on a repository.
     * @param parent
     *            Boolean switch like the "mkdir -p" flag in unix.
     * @param __current
     *            ice context.
     */
    public void makeDir(String path, boolean parents, Current __current) throws ServerError {
        CheckedPath checked = checkPath(path, null, __current);

        final LinkedList<CheckedPath> paths = new LinkedList<CheckedPath>();
        while (!checked.isRoot) {
            paths.addFirst(checked);
            checked = checked.parent();
            if (!parents) {
                break; // Only include last element
            }
        }

        if (paths.size() == 0) {
            if (parents) {
                throw new omero.ResourceError(null, null, "Cannot re-create root!");
            } else{
                log.debug("Ignoring re-creation of root");
                return;
            }
        }

        makeCheckedDirs(paths, parents, __current);

    }

    /**
     * Internal method to be used by subclasses to perform any extra checks on
     * the listed of {@link CheckedPath} instances before allowing the creation
     * of directories.
     *
     * @param paths Not null, not empty.
     * @param parents "mkdir -p" like flag.
     * @param __current
     */
    protected void makeCheckedDirs(final LinkedList<CheckedPath> paths,
            boolean parents, Current __current) throws ResourceError,
            ServerError {

        CheckedPath checked;

        // Since we now have some number of elements, we start at the most
        // parent element and work our way down through all the parents.
        // If the file exists, then we check its permissions. If it doesn't
        // exist, it gets created.
        while (paths.size() > 1) { // Only possible if `parents`
            checked = paths.removeFirst();

            if (checked.exists()) {
                if (!checked.isDirectory()) {
                    throw new omero.ResourceError(null, null,
                            "Path is not a directory.");
                } else if (!checked.canRead()) {
                    throw new omero.ResourceError(null, null,
                            "Directory is not readable");
                }
                assertFindDir(checked, __current);

            } else {
                // This will fail if the file already exists in
                repositoryDao.register(repoUuid, checked,
                        DIRECTORY_MIMETYPE, __current);
            }

        }

        // Now we are ready to work on the actual intended path.
        checked = paths.removeFirst(); // Size is now empty
        if (checked.exists()) {
            if (parents) {
                assertFindDir(checked, __current);
            } else {
                throw new omero.ResourceError(null, null,
                    "Path exists on disk: " + checked.fsFile);
            }
        }
        repositoryDao.register(repoUuid, checked,
                DIRECTORY_MIMETYPE, __current);
    }

    //
    //
    // Utility methods
    //
    //

    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException {
        this.context = (OmeroContext) applicationContext;

    }

    /**
     * Create a new {@link CheckedPath} object based on the given user input.
     * This method is included to allow subclasses a chance to introduce their
     * own {@link CheckedPath} implementations.
     *
     * @param path
     *            A path on a repository.
     *
     */
    protected CheckedPath checkPath(final String path, ChecksumAlgorithm checksumAlgorithm, final Ice.Current curr)
            throws ValidationException {
        return new CheckedPath(this.serverPaths, path, this.checksumProviderFactory, checksumAlgorithm);
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
    private CheckedPath checkId(final long id, final Ice.Current curr)
        throws SecurityViolation, ValidationException {
        final ChecksumAlgorithm checksumAlgorithm = this.repositoryDao.getOriginalFile(id, curr).getHasher();
        final FsFile file = this.repositoryDao.getFile(id, curr, this.repoUuid);
        if (file == null) {
            throw new SecurityViolation(null, null, "FileNotFound: " + id);
        }
        final CheckedPath checked = new CheckedPath(this.serverPaths,file.toString(),
                checksumProviderFactory, checksumAlgorithm);
        checked.setId(id);
        return checked;
    }

    private void assertFindDir(final CheckedPath checked, final Ice.Current curr)
        throws omero.ServerError {
        if (null == repositoryDao.findRepoFile(repoUuid, checked, null, curr)) {
            omero.ResourceError re = new omero.ResourceError();
            IceMapper.fillServerError(re, new RuntimeException(
                    "Directory exists but is not registered: " + checked));
            throw re;
        }
    }

    // Utility function for passing stack traces back in exceptions.
    protected String stackTraceAsString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

}
