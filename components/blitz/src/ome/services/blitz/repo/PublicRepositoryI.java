/*
 * ome.services.blitz.repo.PublicRepositoryI
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang.StringUtils;
import org.hibernate.Session;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;

import ome.api.IQuery;
import ome.api.RawFileStore;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.OMEROWrapper;
import ome.services.blitz.impl.AbstractAmdServant;
import ome.services.blitz.impl.ServiceFactoryI;
import ome.services.blitz.repo.path.FilePathRestrictionInstance;
import ome.services.blitz.repo.path.FilePathRestrictions;
import ome.services.blitz.repo.path.FsFile;
import ome.services.blitz.repo.path.MakePathComponentSafe;
import ome.services.blitz.repo.path.ServerFilePathTransformer;
import ome.services.blitz.util.BlitzExecutor;
import ome.services.blitz.util.ChecksumAlgorithmMapper;
import ome.services.blitz.util.FindServiceFactoryMessage;
import ome.services.blitz.util.RegisterServantMessage;
import ome.services.util.Executor;
import ome.services.util.SleepTimer;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.util.SqlAction;
import ome.util.checksum.ChecksumProviderFactory;
import ome.util.messages.InternalMessage;

import omero.InternalException;
import omero.RLong;
import omero.RMap;
import omero.RType;
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
import omero.cmd.Delete2;
import omero.cmd.HandlePrx;
import omero.grid._RepositoryOperations;
import omero.grid._RepositoryTie;
import omero.model.ChecksumAlgorithm;
import omero.model.OriginalFile;
import omero.model.enums.ChecksumAlgorithmSHA1160;
import omero.util.IceMapper;

/**
 * An implementation of the PublicRepository interface.
 *
 * @author Colin Blackburn <cblackburn at dundee dot ac dot uk>
 * @author Josh Moore, josh at glencoesoftware.com
 * @author m.t.b.carroll@dundee.ac.uk
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

    /** key for finding the real session UUID under sudo */
    static final String SUDO_REAL_SESSIONUUID = "omero.internal.sudo.real:" + omero.constants.SESSIONUUID.value;

    /** key for finding the real group name under sudo */
    static final String SUDO_REAL_GROUP_NAME = "omero.internal.sudo.real:" + omero.constants.GROUP.value;

    private final static Logger log = LoggerFactory.getLogger(PublicRepositoryI.class);

    private final static IOFileFilter DEFAULT_SKIP =
            FileFilterUtils.notFileFilter(
                    FileFilterUtils.orFileFilter(new NameFileFilter(".omero"),
                            new NameFileFilter(".git")));

    /**
     * Mimetype used to connote a directory {@link OriginalFile} object.
     */
    public static final String DIRECTORY_MIMETYPE = "Directory";

    /** media type for import logs */
    public static final String IMPORT_LOG_MIMETYPE = "application/omero-log-file";

    private /*final*/ long id;

    protected /*final*/ ServerFilePathTransformer serverPaths;

    protected final RepositoryDao repositoryDao;

    protected final ChecksumProviderFactory checksumProviderFactory;

    /* in descending order of preference */
    protected final ImmutableList<ChecksumAlgorithm> checksumAlgorithms;

    protected /*final*/ FilePathRestrictions filePathRestrictions;

    protected OmeroContext context;

    private String repoUuid;

    public PublicRepositoryI(RepositoryDao repositoryDao,
            ChecksumProviderFactory checksumProviderFactory,
            String checksumAlgorithmSupported,
            String pathRules) throws ServerError {
        this.repositoryDao = repositoryDao;
        this.checksumProviderFactory = checksumProviderFactory;
        this.repoUuid = null;

        final Builder<ChecksumAlgorithm> checksumAlgorithmsBuilder = ImmutableList.builder();
        for (final String term : checksumAlgorithmSupported.split(",")) {
            if (StringUtils.isNotBlank(term)) {
                checksumAlgorithmsBuilder.add(ChecksumAlgorithmMapper.getChecksumAlgorithm(term.trim()));
            }
        }
        this.checksumAlgorithms = checksumAlgorithmsBuilder.build();
        if (this.checksumAlgorithms.isEmpty()) {
            throw new IllegalArgumentException("a checksum algorithm must be supported");
        }

        final Set<String> terms = new HashSet<String>();
        for (final String term : pathRules.split(",")) {
            if (StringUtils.isNotBlank(term)) {
                terms.add(term.trim());
            }
        }
        final String[] termArray = terms.toArray(new String[terms.size()]);
        try {
            this.filePathRestrictions = FilePathRestrictionInstance.getFilePathRestrictions(termArray);
        } catch (NullPointerException e) {
            throw new ServerError(null, null, "unknown rule set named in: " + pathRules);
        }
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
        this.serverPaths.setPathSanitizer(new MakePathComponentSafe(this.filePathRestrictions));
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
        final String delId = Delete2.ice_staticId();
        final Delete2 deleteRequest = (Delete2) getFactory(delId, adjustedCurr).create(delId);
        final List<Long> fileIds = new ArrayList<Long>();
        deleteRequest.targetObjects = new HashMap<String, List<Long>>();
        deleteRequest.targetObjects.put("OriginalFile", fileIds);

        for (String path : files) {
            // treeList() calls checkedPath
            RMap map = treeList(path, __current);
            _deletePaths(map, fileIds);
        }

        final FindServiceFactoryMessage msg
            = new FindServiceFactoryMessage(this, adjustedCurr);
        publishMessage(msg);
        final ServiceFactoryI sf = msg.getServiceFactory();

        AMD_submit submit = submitRequest(sf, deleteRequest, adjustedCurr);
        return submit.ret;
    }

    private void _deletePaths(RMap map, List<Long> fileIds) {
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
                        _deletePaths(files, fileIds);
                    }
                    // Now after we've recursed, note the actual delete.
                    RLong id = (RLong) val.getValue().get("id");
                    fileIds.add(id.getValue());
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
            // Is this ever used? No Memoizer is active here!
            // Perhaps better to use the PixelsService directly and
            // omit OMEROWrapper.
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

    /* TODO: The server should not have any hard-coded preference for the SHA-1 algorithm
     * (which may not be the setting of omero.checksum.supported) in such a generic code path.
     * Clients wishing to assume SHA-1 for checksumming files created using this method should
     * somehow specify this to the server via the API. This method can then be removed.
     */
    /**
     * Set the hasher of the original file of the given ID to SHA-1. Clears any previous hash.
     * @param id the ID of an original file
     * @param current the ICE method invocation context
     * @throws ServerError if there was a problem in executing this internal task
     */
    @Deprecated
    private void setOriginalFileHasherToSHA1(final long id, Current current) throws ServerError {
        final Executor executor = this.context.getBean("executor", Executor.class);
        final Map<String, String> ctx = current.ctx;
        final String session = ctx.get(omero.constants.SESSIONUUID.value);
        final String group = ctx.get(omero.constants.GROUP.value);
        final Principal principal = new Principal(session, group, null);

        try {
            executor.execute(ctx, principal, new Executor.SimpleWork(this, "setOriginalFileHasherToSHA1", id) {
                    @Transactional
                    public Object doWork(Session session, ServiceFactory sf) {
                        final IQuery iQuery = sf.getQueryService();
                        final ome.model.core.OriginalFile originalFile = iQuery.find(ome.model.core.OriginalFile.class, id);
                        final ome.model.enums.ChecksumAlgorithm sha1 = iQuery.findByString(ome.model.enums.ChecksumAlgorithm.class,
                                "value", ChecksumAlgorithmSHA1160.value);
                        originalFile.setHash(null);
                        originalFile.setHasher(sha1);
                        sf.getUpdateService().saveObject(originalFile);
                        return null;
                    }
                });
            } catch (Exception e) {
                throw (ServerError) new IceMapper().handleException(e, executor.getContext());
            }
        }

    /**
     * Set the repository of the given original file to be this one.
     * @param originalFile the ID of the log file
     * @param current the Ice method invocation context
     */
    //TODO: Should be refactored elsewhere.
    @Deprecated
    protected ome.model.core.OriginalFile persistLogFile(final ome.model.core.OriginalFile originalFile, Ice.Current current)
                throws ServerError {

        final Executor executor = this.context.getBean("executor", Executor.class);
        final Map<String, String> ctx = current.ctx;
        final String session = ctx.get(omero.constants.SESSIONUUID.value);
        final String group = ctx.get(omero.constants.GROUP.value);
        final Principal principal = new Principal(session, group, null);

        try {
            return (ome.model.core.OriginalFile) executor.execute(ctx, principal,
                    new Executor.SimpleWork(this, "persistLogFile", id) {
                @Transactional(readOnly = false)
                public ome.model.core.OriginalFile doWork(Session session, ServiceFactory sf) {
                    final ome.model.core.OriginalFile persisted = sf.getUpdateService().saveAndReturnObject(originalFile);
                    getSqlAction().setFileRepo(Collections.singleton(persisted.getId()), repoUuid);
                    return persisted;
                }
            });
        } catch (Exception e) {
            throw (ServerError) new IceMapper().handleException(e, executor.getContext());
        }
}

    protected OriginalFile findOrCreateInDb(CheckedPath checked, String mode,
            Ice.Current curr) throws ServerError {
        return findOrCreateInDb(checked, mode, null, curr);
    }

    protected OriginalFile findOrCreateInDb(CheckedPath checked, String mode,
            String mimetype, Ice.Current curr) throws ServerError {

        OriginalFile ofile = findInDb(checked, mode, curr);
        if (ofile != null) {
            return ofile;
        }

        if (checked.exists()) {
            omero.grid.UnregisteredFileException ufe
                = new omero.grid.UnregisteredFileException();
            ofile = (OriginalFile) new IceMapper().map(checked.asOriginalFile(mimetype));
            ufe.file = ofile;
            throw ufe;
        }

        ofile = repositoryDao.register(repoUuid, checked, null, curr);
        final long originalFileId = ofile.getId().getValue();
        setOriginalFileHasherToSHA1(originalFileId, curr);
        checked.setId(originalFileId);
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
     * Provide a {@link Ice.Current} like the given one, except with the request context session UUID replaced.
     * @param current an {@link Ice.Current} instance
     * @param sessionUuid a new session UUID for the instance
     * @return a new {@link Ice.Current} instance like the given one but with the new session UUID
     */
    protected Current sudo(Current current, String sessionUuid) {
        final Current sudoCurrent =  makeAdjustedCurrent(current);
        sudoCurrent.ctx = new HashMap<String, String>(current.ctx);
        sudoCurrent.ctx.put(SUDO_REAL_SESSIONUUID, current.ctx.get(omero.constants.SESSIONUUID.value));
        sudoCurrent.ctx.put(SUDO_REAL_GROUP_NAME, current.ctx.get(omero.constants.GROUP.value));
        sudoCurrent.ctx.put(omero.constants.SESSIONUUID.value, sessionUuid);
        return sudoCurrent;
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
     * @return See above.
     * @throws ServerError
     */
    Ice.ObjectPrx registerServant(Ice.Object tie,
            AbstractAmdServant servant, Ice.Current current)
                    throws ServerError {

        final RegisterServantMessage msg = new RegisterServantMessage(this, tie,
                servant.getClass().getSimpleName(), current);
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
     * @param parents
     *            Boolean switch like the "mkdir -p" flag in unix.
     * @param current
     *            ice context.
     */
    public void makeDir(String path, boolean parents, Current current) throws ServerError {
        CheckedPath checked = checkPath(path, null, current);
        repositoryDao.makeDirs(this, Arrays.asList(checked), parents, current);
    }

    public void makeDir(CheckedPath checked, boolean parents,
            Session s, ServiceFactory sf, SqlAction sql,
            ome.system.EventContext effectiveEventContext) throws ServerError {

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

        makeCheckedDirs(paths, parents, s, sf, sql, effectiveEventContext);

    }

    /**
     * Internal method to be used by subclasses to perform any extra checks on
     * the listed of {@link CheckedPath} instances before allowing the creation
     * of directories.
     *
     * @param paths Not null, not empty. (Will be emptied by this method.)
     * @param parents "mkdir -p" like flag.
     * @param s The session
     * @param sf
     * @param sql
     * @param effectiveEventContext
     */
    protected void makeCheckedDirs(final LinkedList<CheckedPath> paths,
            boolean parents, Session s, ServiceFactory sf, SqlAction sql,
            ome.system.EventContext effectiveEventContext) throws ServerError {

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
                assertFindDir(checked, s, sf, sql);

            } else {
                // This will fail if the directory already exists
                try {
                    repositoryDao.register(repoUuid, checked,
                            DIRECTORY_MIMETYPE, sf, sql);
                } catch (ValidationException ve) {
                    if (ve.getCause() instanceof PSQLException) {
                        // Could have collided with another thread also creating the directory.
                        // See Trac #11096 regarding originalfile table uniqueness of columns repo, path, name.
                        // So, give the other thread time to complete registration.
                        SleepTimer.sleepFor(1000);
                        if (checked.exists()) {
                            // The path now exists! It did not a moment ago.
                            // We are not going to rethrow the validation exception,
                            // so we otherwise note that something unexpected did occur.
                            log.warn("retrying after exception in registering directory " + checked + ": " + ve.getCause());
                            // Another thread may have succeeded where this one failed,
                            // so try this directory again.
                            paths.add(0, checked);
                            continue;
                        }
                    }
                    // We cannot recover from the validation exception.
                    throw ve;
                }
            }

        }

        // Now we are ready to work on the actual intended path.
        checked = paths.removeFirst(); // Size is now empty
        if (checked.exists()) {
            if (parents) {
                assertFindDir(checked, s, sf, sql);
            } else {
                throw new omero.ResourceError(null, null,
                    "Path exists on disk: " + checked.fsFile);
            }
        }
        repositoryDao.register(repoUuid, checked,
                DIRECTORY_MIMETYPE, sf, sql);
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
        // TODO: could getOriginalFile and getFile be reduced to a single call?
        final FsFile file = this.repositoryDao.getFile(id, curr, this.repoUuid);
        if (file == null) {
            throw new SecurityViolation(null, null, "FileNotFound: " + id);
        }
        final OriginalFile originalFile = this.repositoryDao.getOriginalFile(id, curr);
        if (originalFile == null) {
            /* reachable even if file != null because getFile uses SQL,
             * evading the filter on the HQL used here by getOriginalFile */
            throw new SecurityViolation(null, null, "FileNotAccessible: " + id);
        }
        final CheckedPath checked = new CheckedPath(this.serverPaths,file.toString(),
                checksumProviderFactory, originalFile.getHasher());
        checked.setId(id);
        return checked;
    }

    private void assertFindDir(final CheckedPath checked,
            Session s, ServiceFactory sf, SqlAction sql)
        throws omero.ServerError {
        if (null == repositoryDao.findRepoFile(sf, sql, repoUuid, checked, null)) {
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
