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
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.activation.MimetypesFileTypeMap;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.FormatException;
import loci.formats.IFormatReader;
import loci.formats.IFormatWriter;
import loci.formats.ImageReader;
import loci.formats.ImageWriter;
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;
import ome.conditions.InternalException;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.parameters.Parameters;
import ome.services.blitz.util.RegisterServantMessage;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;
import ome.util.SqlAction;
import ome.xml.model.primitives.PositiveInteger;
import omero.ServerError;
import omero.ValidationException;
import omero.api.RawFileStorePrx;
import omero.api.RawFileStorePrxHelper;
import omero.api.RawPixelsStorePrx;
import omero.api.RawPixelsStorePrxHelper;
import omero.api.RenderingEnginePrx;
import omero.api.ThumbnailStorePrx;
import omero.api._RawFileStoreTie;
import omero.api._RawPixelsStoreTie;
import omero.constants.data.NONAMESET;
import omero.grid.RepositoryImportContainer;
import omero.grid.RepositoryPrx;
import omero.grid._RepositoryDisp;
import omero.grid._RepositoryOperations;
import omero.grid._RepositoryTie;
import omero.model.DimensionOrder;
import omero.model.Experimenter;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.Pixels;
import omero.model.PixelsType;
import omero.util.IceMapper;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;

/**
 * An implementation of he PublicRepository interface
 *
 * @author Colin Blackburn <cblackburn at dundee dot ac dot uk>
 * @author Josh Moore, josh at glencoesoftware.com
 */
public class PublicRepositoryI implements _RepositoryOperations {

    private final static Log log = LogFactory.getLog(PublicRepositoryI.class);

    /* These two path elements make up the local thumbnail cache */
    private final static String OMERO_PATH = ".omero";

    private final static String THUMB_PATH = "thumbnails";

    /* String used as key in params field of db for indexing image series number */
    private final static String IMAGE_NO_KEY = "image_no";

    private /*final*/ long id;

    protected /*final*/ File root;

    protected final Executor executor;

    protected final Principal principal;

    private final Map<String,DimensionOrder> dimensionOrderMap =
        new ConcurrentHashMap<String, DimensionOrder>();

    private final Map<String,PixelsType> pixelsTypeMap =
        new ConcurrentHashMap<String, PixelsType>();

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
    public void initialize(FileMaker fileMaker, Long id) throws ValidationException {
        this.id = id;
        this.root = new File(fileMaker.getDir()).getAbsoluteFile();
        if (root == null || !root.isDirectory()) {
            throw new ValidationException(null, null,
                    "Root directory must be a existing, readable directory.");
        }
    }

    /**
     * Wrap the current instance with an {@link Ice.TieBase} so that it
     * can be turned into a proxy. This is required due to the subclassing
     * between public repo instances.
     */
    public Ice.Object tie() {
        return new _RepositoryTie(this);
    }

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

    public List<String> list(String path, Current __current) throws ServerError {
        File file = checkPath(path, true);
        List<String> contents = new ArrayList<String>();
        for (String child : file.list()) {
            contents.add(child);
        }
        return contents;
    }

    public List<OriginalFile> listFiles(String path, Current __current) throws ServerError {
        File file = checkPath(path, true);
        List<OriginalFile> contents = new ArrayList<OriginalFile>();
        for (File child : file.listFiles()) {
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

        File file = new File(path).getAbsoluteFile();
        OriginalFile omeroFile = new OriginalFileI();
        omeroFile = createOriginalFile(file, mimetype);

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

    public void delete(String path, Current __current) throws ServerError {
        File file = checkPath(path);
        FileUtils.deleteQuietly(file);
    }

    public List<String> deleteFiles(String[] files, Current __current) throws ServerError {
        List<String> undeleted = new ArrayList<String>();
        for (String path : files) {
            File file = checkPath(path);
            if (file.delete()) {
                file = file.getParentFile();
                while(!file.equals(root) && file.delete()) {
                    file = file.getParentFile();
                }
            }
            else {
                undeleted.add(path);
            }
        }
        return undeleted;
    }

    @SuppressWarnings("unchecked")

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
        File file = checkPath(path);
        if (!file.exists()) {
            throw new ValidationException(null, null, "Path does not exist");
        }
        return getMimetype(file);
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
        File file = checkPath(path);
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
        File file = checkPath(path);
        try {
            FileUtils.touch(file);
            return true;
        } catch (Exception e) {
            throw new omero.InternalException(stackTraceAsString(e), null, e.getMessage());
        }
    }

    /**
     *
     * Interface methods yet TODO
     *
     */
    public OriginalFile load(String path, Current __current) throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public RawPixelsStorePrx pixels(String path, Current __current) throws ServerError {
        Principal currentUser = currentUser(__current);

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
        // See comment below in RawFileStorePrx
        Ice.Current adjustedCurr = new Ice.Current();
        adjustedCurr.ctx = __current.ctx;
        adjustedCurr.operation = __current.operation;
        String sessionUuid = __current.ctx.get(omero.constants.SESSIONUUID.value);
        adjustedCurr.id = new Ice.Identity(__current.id.name, sessionUuid);

        RepoRawFileStoreI rfs;
        try {
            rfs = new RepoRawFileStoreI(path, mode);
        } catch (Throwable t) {
            if (t instanceof ServerError) {
                throw (ServerError) t;
            } else {
                omero.InternalException ie = new omero.InternalException();
                IceMapper.fillServerError(ie, t);
                throw ie;
            }
        }

        // See comment below in fileById
        _RawFileStoreTie tie = new _RawFileStoreTie(rfs);
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
        return RawFileStorePrxHelper.uncheckedCast(prx);
    }

    public RawFileStorePrx fileById(long fileId, Current __current) throws ServerError {
        Principal currentUser = currentUser(__current);
        File file = getFile(fileId, currentUser);
        if (file == null) {
            return null;
        }

        // WORKAROUND: See the comment in RawFileStoreI.
        // The most likely correction of this
        // is to have PublicRepositories not be global objects, but be created
        // on demand for each session via SharedResourcesI
        Ice.Current adjustedCurr = new Ice.Current();
        adjustedCurr.ctx = __current.ctx;
        adjustedCurr.operation = __current.operation;
        String sessionUuid = __current.ctx.get("omero.session");
        adjustedCurr.id = new Ice.Identity(__current.id.name, sessionUuid);

        // TODO: Refactor all this into a single helper method.
        // If there is no listener available who will take responsibility
        // for this servant, then we bail.
        RepoRawFileStoreI rfs = new RepoRawFileStoreI(fileId, file);
        _RawFileStoreTie tie = new _RawFileStoreTie(rfs);
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
        File file = checkPath(path);
        try {
            FileUtils.forceMkdir(file);
        } catch (Exception e) {
            throw new omero.InternalException(stackTraceAsString(e), null, e.getMessage());
        }
    }

    public void rename(String path, Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }


    public RenderingEnginePrx render(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public ThumbnailStorePrx thumbs(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
    }

    public void transfer(String srcPath, RepositoryPrx target,
            String targetPath, Current __current) throws ServerError {
        // TODO Auto-generated method stub

    }

    /**
     *
     * Utility methods
     *
     */

    /**
     * Get the file object at a path.
     *
     * @param path
     *            A path on a repository.
     * @return File object
     *
     */
    private File checkPath(String path) throws ValidationException {
        return checkPath(path, false);
    }

    /**
     * Get the file object at a path.
     *
     * @param path
     *            A path on a repository.
     * @param mustExist
     *            Whether or not the file must currently exist.
     * @return File object
     *
     */
    private File checkPath(String path, boolean mustExist) throws ValidationException {

        if (path == null || path.length() == 0) {
            throw new ValidationException(null, null, "Path is empty");
        }

        final String root_ = FilenameUtils.normalizeNoEndSeparator(
                root.getAbsolutePath()) + File.separator;
        final String abspath = FilenameUtils.normalizeNoEndSeparator(path);
        // Could be replaced by commons-io 2.4 directoryContains.
        if (!abspath.regionMatches(
                true, 0, root_, 0, root_.length())) {
            throw new ValidationException(null, null, path + " is not within "
                    + root.getAbsolutePath());
        }

        final File f = new File(abspath);
        if (mustExist && !f.exists()) {
            throw new ValidationException(null, null, path + " does not exist");
        }
        return f;
    }

    /**
     * Get the mimetype for a file.
     *
     * @param file
     *            A File in a repository.
     * @return A String representing the mimetype.
     *
     * TODO Return the correct Format object in place of a dummy one
     */
    private String getMimetype(File file) {

        final String contentType = new MimetypesFileTypeMap().getContentType(file);
        return contentType;

    }

    /**
     * Create an OriginalFile object corresponding to a File object
     *
     * @param f
     *            A File object.
     * @return An OriginalFile object
     *
     */
    private OriginalFile createOriginalFile(File f) {
        String mimetype = getMimetype(f);
        return createOriginalFile(f, rstring(mimetype));
    }

    /**
     * Create an OriginalFile object corresponding to a File object
     * using the user supplied mimetype string
     *
     * @param f
     *            A File object.
     * @param mimetype
     *            Mimetype as an RString
     * @return An OriginalFile object
     *
     * TODO populate more attribute fields than the few set here?
     */
    private OriginalFile createOriginalFile(File f, omero.RString mimetype) {
        OriginalFile file = new OriginalFileI();
        file.setName(rstring(f.getName()));
        // This first case deals with registerng the repos themselves.
        if (f.getAbsolutePath().equals(root.getAbsolutePath())) {
            file.setPath(rstring(f.getParent()));
        } else { // Path should be relative to root?
            file.setPath(rstring(getRelativePath(f)));
        }
        file.setSha1(rstring("UNKNOWN"));
        file.setMimetype(mimetype);
        file.setMtime(rtime(f.lastModified()));
        file.setSize(rlong(f.length()));
        // Any other fields?

        return file;
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
        final String uuid = getRepoUuid();
        return (File) executor.execute(currentUser, new Executor.SimpleWork(this, "getFile", id) {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                            String path = getSqlAction().findRepoFilePath(uuid, id);

                            if (path == null) {
                                return null;
                            }

                            return new File(root, path);
                    }
                });
    }

    /**
     * A getter for the repoUuid.
     * This is run once by getRepoUuid() when first needed,
     * thereafter lookups are local.
     *
     * TODO: this should probably be done in the constructor?
     */
	private String getRepoUuid() {
	    if (this.repoUuid == null) {
            final long repoId = this.id;
            ome.model.core.OriginalFile oFile = (ome.model.core.OriginalFile)  executor
                .execute(principal, new Executor.SimpleWork(this, "getRepoUuid") {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        return sf.getQueryService().find(ome.model.core.OriginalFile.class, repoId);
                    }
                });
            OriginalFileI file = (OriginalFileI) new IceMapper().map(oFile);
            this.repoUuid = file.getSha1().getValue();
        }
        return this.repoUuid;
    }

    private String getRelativePath(File f) {
        String path = f.getParent()
                .substring(root.getAbsolutePath().length(), f.getParent().length());
        // The parent doesn't contain a trailing slash.
        path = path + "/";
        return path;
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
