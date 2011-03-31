/*
 * ome.services.blitz.repo.PublicRepositoryI
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
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
import ome.formats.importer.ImportContainer;
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
import omero.grid.FileSet;
import omero.grid.RepositoryListConfig;
import omero.grid.RepositoryPrx;
import omero.grid._RepositoryDisp;
import omero.model.DimensionOrder;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.Pixels;
import omero.model.PixelsType;
import omero.util.IceMapper;

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
public class PublicRepositoryI extends _RepositoryDisp {

    private final static Log log = LogFactory.getLog(PublicRepositoryI.class);

    /* These two path elements make up the local thumbnail cache */
    private final static String OMERO_PATH = ".omero";

    private final static String THUMB_PATH = "thumbnails";

    /* String used as key in params field of db for indexing image series number */
    private final static String IMAGE_NO_KEY = "image_no";

    /* String to use when there is no image name */
    public final static String NO_NAME_SET = "NO_NAME_SET";

    private final long id;

    private final File root;

    private final Executor executor;

    private final SqlAction sql;

    private final Principal principal;

    private final Map<String,DimensionOrder> dimensionOrderMap =
        new ConcurrentHashMap<String, DimensionOrder>();

    private final Map<String,PixelsType> pixelsTypeMap =
        new ConcurrentHashMap<String, PixelsType>();

    private String repoUuid;

    public PublicRepositoryI(File root, long repoObjectId, Executor executor,
            SqlAction sql, Principal principal) throws Exception {
        this.id = repoObjectId;
        this.executor = executor;
        this.sql = sql;
        this.principal = principal;

        if (root == null || !root.isDirectory()) {
            throw new ValidationException(null, null,
                    "Root directory must be a existing, readable directory.");
        }
        this.root = root.getAbsoluteFile();
        this.repoUuid = null;
    }

    public OriginalFile root(Current __current) throws ServerError {
        return new OriginalFileI(this.id, false); // SHOULD BE LOADED.
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

    /**
     * Register an OriginalFile object
     *
     * @param obj
     *            OriginalFile object.
     * @param __current
     *            ice context.
     * @return The OriginalFile with id set
     *
     */
    public OriginalFile registerOriginalFile(OriginalFile omeroFile, Current __current)
            throws ServerError {

        if (omeroFile == null) {
            throw new ValidationException(null, null,
                    "obj is required argument");
        }

        Principal currentUser = currentUser(__current);
        IceMapper mapper = new IceMapper();
        final ome.model.core.OriginalFile omeFile = (ome.model.core.OriginalFile) mapper
			.reverse(omeroFile);
        final String repoId = getRepoUuid();

        Long id = (Long) executor.execute(currentUser, new Executor.SimpleWork(
                this, "registerOriginalFile", repoId) {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                long id = sf.getUpdateService().saveAndReturnObject(omeFile).getId();
                sql.setFileRepo(id, repoId);
                return id;
            }
        });

        omeroFile.setId(rlong(id));
        return omeroFile;
    }

    /**
     * Register the Images in a list of Images, a single image will be a one-element list
     *
     * @param filename
     *            The absolute path of the parent file.
     * @param imageList
     *            A list of Image objects.
     * @param params
     *            Map<String, String>
     * @param __current
     *            ice context.
     * @return A List of Images with ids set
     *
     */
    public List<IObject> registerFileSet(OriginalFile keyFile, List<Image> imageList, Current __current)
            throws ServerError {

        if (keyFile == null) {
            throw new ValidationException(null, null,
                    "keyFile is a required argument");
        }
        Principal currentUser = currentUser(__current);

        List<IObject> objList = new ArrayList<IObject>();

        IceMapper mapper = new IceMapper();
        final ome.model.IObject omeFile = (ome.model.IObject) mapper.reverse(keyFile);
        final String repoId = getRepoUuid();
        final String clientSessionUuid = __current.ctx.get(omero.constants.SESSIONUUID.value);

        Long ofId = (Long) executor.execute(currentUser, new Executor.SimpleWork(
                this, "registerParentFile", repoId) {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                long id = sf.getUpdateService().saveAndReturnObject(omeFile).getId();
                sql.setFileRepo(id, repoId);
                return id;
            }
        });
        keyFile.setId(rlong(ofId));
        objList.add(keyFile);

        if (imageList == null || imageList.size() == 0) {
            return objList;
        }

        final String path = keyFile.getPath().getValue();
        final String name = keyFile.getName().getValue();

        int imageCount = 0;
        Map<String, String> params = new HashMap<String, String>();
        for (IObject obj : imageList) {

            params.put("image_no", Integer.toString(imageCount));
            final Map<String, String> paramMap = params;
            final ome.model.IObject omeObj = (ome.model.IObject) mapper.reverse(obj);

            Long id = (Long) executor.execute(currentUser, new Executor.SimpleWork(
                    this, "registerImageList", repoId) {
                @Transactional(readOnly = false)
                public Object doWork(Session session, ServiceFactory sf) {
                    long id = sf.getUpdateService().saveAndReturnObject(omeObj).getId();
                    ome.model.IObject result = sf.getQueryService().findByQuery("select p from Pixels p where p.image = " + id, null);
                    long pixId = result.getId();
                    sql.setPixelsNamePathRepo(pixId, name, path, repoId);
                    sql.setPixelsParams(pixId, paramMap);
                    return id;
                }
            });
            obj.setId(rlong(id));
            objList.add(obj);
            imageCount++;
        }
        return objList;
    }

    /**
     * Import an image set's metadata.
     *
     * @param id
     *            The id of the parent original file.
     * @param __current
     *            ice context.
     * @return
     *
     */
    public List<Image> importFileSet(OriginalFile keyFile, Current __current) throws ServerError {

        Principal currentUser = currentUser(__current);
	final String name = keyFile.getName().getValue();
	final String path = keyFile.getPath().getValue();
	final File file = new File(new File(root, path), name);
	final String repoId = getRepoUuid();
        final String clientSessionUuid = __current.ctx.get(omero.constants.SESSIONUUID.value);

        @SuppressWarnings("unchecked")
        Map<Integer, ome.model.core.Image> returnMap = (Map<Integer, ome.model.core.Image>) executor
        .execute(currentUser, new Executor.SimpleWork(this, "importFileSetMetadata") {

            @Transactional(readOnly = true)
            public Object doWork(Session session, ServiceFactory sf) {

		Map<Integer, ome.model.core.Image> iMap = new HashMap<Integer, ome.model.core.Image>();

                List<Long> pixIds = sql.findRepoPixels(repoId, path, name);
                if (pixIds == null || pixIds.size() == 0) {
                    return iMap;
                }

                for (Long pId : pixIds) {

                    Map<String, String> params = sql.getPixelsParams(pId.longValue());

                    long pixelsId = pId.longValue();
                    Long imageId = sql.findRepoImageFromPixels(pixelsId);

                    Parameters p = new Parameters();
                    p.addId(new Long(imageId.longValue()));
                    ome.model.core.Image image = sf.getQueryService().findByQuery(
				"select i from Image i " +
				"join fetch i.pixels as p " +
				"join fetch p.pixelsType " +
				"join fetch p.dimensionOrder " +
				"left outer join fetch p.channels " +
				"left outer join fetch p.planeInfo " +
				"left outer join fetch p.pixelsFileMaps " +
				"left outer join fetch p.settings " +
				"left outer join fetch p.thumbnails " +
				"left outer join fetch i.instrument " +
				"left outer join fetch i.imagingEnvironment " +
				"left outer join fetch i.experiment " +
				"left outer join fetch i.format " +
				"left outer join fetch i.objectiveSettings " +
				"left outer join fetch i.stageLabel " +
				"left outer join fetch i.annotationLinks " +
				"left outer join fetch i.wellSamples " +
				"left outer join fetch i.rois " +
				"where i.id = :id", p);

                    iMap.put(new Integer(params.get(IMAGE_NO_KEY)),image);
                }
                return iMap;
            }
        });

        if (returnMap == null)
        {
            return null;
        }

        IceMapper mapper = new IceMapper();
        Map<Integer, Image> imageMap = mapper.map(returnMap);

        // Temporary logging
        for (Map.Entry<Integer, Image> entry : imageMap.entrySet() ) {
		log.info("Image: " + entry.getValue().getName().getValue()
				+ ", series=" + entry.getKey().toString()
				+ ", id=" + Long.toString(entry.getValue().getId().getValue()));
        }
        // End logging

        List<Pixels> pix = importFile(file, clientSessionUuid, imageMap);
        if (pix == null) {
            return null;
        }

        List<Image> images = new ArrayList<Image>();
        int count = 0;
        for (Image im : imageMap.values()) {
		im.clearPixels(); // Do I need to do this first?
		im.addPixels(pix.get(count));
		images.add(im);
		count++;
        }

        return images;
    }

    private List<Pixels> importFile(final File file, final String clientSessionUuid, Map<Integer,Image> imageMap) {
        return null; // FIXME
        /* FIXME
        OMEROMetadataStoreClient store;
        ImportConfig config = new ImportConfig();
        // TODO: replace hard-wired host and port
        config.hostname.set("localhost");
        config.port.set(new Integer(4064));
        config.sessionKey.set(clientSessionUuid);

        try {
            store = config.createStore();
        } catch (Exception e) {
            log.error("Failed to create OMEROMetadataStoreClient: ", e);
            return null;
        }

        OMEROWrapper reader = new OMEROWrapper(config);
        ImportLibrary library = new ImportLibrary(store, reader);
        library.setMetadataOnly(true);
        library.prepare(imageMap);

        List<Pixels> pix = new ArrayList<Pixels>();
        try {
             pix = library.importImage(file, 0, 0, 1, null, null, false, false, null, null);
        } catch (Throwable t) {
             log.error("Faled to importImage: ", t);
             return null;
        }
        */
    }

    public void delete(String path, Current __current) throws ServerError {
        File file = checkPath(path);
        FileUtils.deleteQuietly(file);
    }

    @SuppressWarnings("unchecked")

    /**
     * Get a list of all files and directories at path.
     *
     * @param path
     *            A path on a repository.
     * @param config
     *            A RepositoryListConfig defining the listing config.
     * @param __current
     *            ice context.
     * @return List of OriginalFile objects at path
     *
     */
    public List<OriginalFile> listFiles(String path, RepositoryListConfig config, Current __current) throws ServerError {
        Principal currentUser = currentUser(__current);
        File file = checkPath(path);
        if (!file.exists()) {
            throw new ValidationException(null, null, "Path does not exist");
        }
        if (!file.isDirectory()) {
            throw new ValidationException(null, null, "Path is not a directory");
        }

        List<File> files;
        List<OriginalFile> oFiles;
        RepositoryListConfig conf;

        if(config == null) {
            conf = new RepositoryListConfig(1, true, true, false, true, false);
        } else {
            conf = config;
        }
        files = filteredFiles(file, conf);
        oFiles = filesToOriginalFiles(files);
        if (conf.registered) {
            oFiles = knownOriginalFiles(oFiles, currentUser);
        }
        return oFiles;
    }

    /**
     * Get a list of those files as importable and non-importable list.
     *
     * @param path
     *            A path on a repository
     * @param config
     *            A RepositoryListConfig defining the listing config.
     * @param __current
     *            ice context.
     * @return A List of FileSet objects.
     *
     * The map uses the object name as key. This is the file name but should be something
     * guaranteed to be unique.
     *
     */
     public List<FileSet> listFileSets(String path, RepositoryListConfig config, Current __current)
            throws ServerError {
        Principal currentUser = currentUser(__current);
        File file = checkPath(path);
        if (!file.exists()) {
            throw new ValidationException(null, null, "Path does not exist");
        }
        if (!file.isDirectory()) {
            throw new ValidationException(null, null, "Path is not a directory");
        }
        RepositoryListConfig conf;

        if(config == null) {
            conf = new RepositoryListConfig(1, true, true, false, true, false);
        } else {
            conf = config;
        }
        List<File> files = filteredFiles(file, conf);
        List<String> names = filesToPaths(files);
        List<ImportContainer> containers = importableImageFiles(path, conf.depth);
        List<FileSet> rv;
        try {
            rv = processImportContainers(containers, names, conf.showOriginalFiles, currentUser);
        } catch (InternalException e) {
            throw new omero.InternalException(stackTraceAsString(e), null, e.getMessage());
        }
        return rv;
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
        File file = checkPath(path);
        if (!file.exists()) {
            throw new ValidationException(null, null, "Path does not exist");
        }
        return getMimetype(file);
    }

    /**
     * Get (the path of) the thumbnail image for an image file on the repository.
     *
     * @param path
     *            A path on a repository.
     * @param __current
     *            ice context.
     * @return The path of the thumbnail
     *
     */
    public String getThumbnail(String path, Current __current)  throws ServerError {
        Principal currentUser = currentUser(__current);
        File file = checkPath(path);
        if (!file.exists()) {
            throw new ValidationException(null, null, "Path does not exist");
        }
        if (!file.isFile()) {
            throw new ValidationException(null, null, "Path is not a file");
        }
        String tnPath;
        try {
            tnPath = createThumbnail(file);
        } catch (ServerError exc) {
            throw exc;
        }
        return tnPath;
    }

    /**
     * Get (the path of) the thumbnail image for an image file on the repository.
     *
     * @param path
     *            A path on a repository.
     * @param imageIndex
     *            The index of an image in a multi-image file set.
     * @param __current
     *            ice context.
     * @return The path of the thumbnail
     *
     */
    public String getThumbnailByIndex(String path, int imageIndex, Current __current)  throws ServerError {
        Principal currentUser = currentUser(__current);
        File file = checkPath(path);
        if (!file.exists()) {
            throw new ValidationException(null, null, "Path does not exist");
        }
        if (!file.isFile()) {
            throw new ValidationException(null, null, "Path is not a file");
        }
        String tnPath;
        try {
            tnPath = createThumbnail(file, imageIndex);
        } catch (ServerError exc) {
            throw exc;
        }
        return tnPath;
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
            rps = new BfPixelsStoreI(path);
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

    public RawFileStorePrx file(long fileId, Current __current) throws ServerError {
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

    public RawFileStorePrx read(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
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

    public RawFileStorePrx write(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
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

        if (path == null || path.length() == 0) {
            throw new ValidationException(null, null, "Path is empty");
        }

        boolean found = false;
        File file = new File(path).getAbsoluteFile();
        while (true) {
            if (file.equals(root)) {
                found = true;
                break;
            }
            file = file.getParentFile();
            if (file == null) {
                break;
            }
        }

        if (!found) {
            throw new ValidationException(null, null, path + " is not within "
                    + root.getAbsolutePath());
        }

        return new File(path).getAbsoluteFile();
    }

   /**
     * Get a filtered file listing based on the config options.
     *
     * @param file
     *            A File object representing the directory to be listed.
     * @param config
     *            A RepositoryListConfig object holding the filter options.
     * @return A list of File objects
     *
     */
    private List<File> filteredFiles(File file, RepositoryListConfig config) throws ServerError {
        List<File> files;
        IOFileFilter filter;

        // If hidden is true list all files otherwise only those files not starting with "."
        if (config.hidden) {
            filter = FileFilterUtils.trueFileFilter();
        } else {
            filter = FileFilterUtils.notFileFilter(FileFilterUtils.prefixFileFilter("."));
        }

        // Now decorate the filter to restrict to files or directories,
        // the else case is for a bizarre config of wanting nothing returned!
        if (!(config.dirs && config.files)) {
            if (config.dirs) {
                filter = FileFilterUtils.makeDirectoryOnly(filter);
            } else if (config.files) {
                filter = FileFilterUtils.makeFileOnly(filter);
            } else {
                filter = FileFilterUtils.falseFileFilter();
            }
        }

        files = Arrays.asList(file.listFiles((FileFilter)filter));

        return files;
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
     * Get the DimensionOrder
     *
     * @param String
     *            A string representing the dimension order
     * @return A DimensionOrder object
     *
     * The HashMap is built on the first call.
     * TODO: Move that build to constructor?
     */
    private DimensionOrder getDimensionOrder(String dimensionOrder) {
        if (dimensionOrderMap.size() == 0) {
            buildDimensionOrderMap(dimensionOrderMap);
        }
        return dimensionOrderMap.get(dimensionOrder);
    }

    /**
     * Get the PixelsType
     *
     * @param String
     *            A string representing the pixels type
     * @return A PixelsType object
     *
     * The HashMap is built on the first call.
     * TODO: Move that build to constructor?
     */
    private PixelsType getPixelsType(String pixelsType) {
        if (pixelsTypeMap.size() == 0) {
            buildPixelsTypeMap(pixelsTypeMap);
        }
        return pixelsTypeMap.get(pixelsType);
    }

    /**
     * Get OriginalFile objects corresponding to a collection of File objects.
     *
     * @param files
     *            A collection of File objects.
     * @return A list of new OriginalFile objects
     *
     */
    private List<OriginalFile> filesToOriginalFiles(Collection<File> files) {
        List<OriginalFile> rv = new ArrayList<OriginalFile>();
        for (File f : files) {
            rv.add(createOriginalFile(f));
        }
        return rv;
    }

    /**
     * Get file paths corresponding to a collection of File objects.
     *
     * @param files
     *            A collection of File objects.
     * @return A list of path Strings
     *
     */
    private List<String> filesToPaths(Collection<File> files) {
        List<String> rv = new ArrayList<String>();
        for (File f : files) {
            rv.add(f.getAbsolutePath());
        }
        return rv;
    }

    /**
     * Get registered OriginalFile objects corresponding to a collection of File objects.
     *
     * @param files
     *            A collection of OriginalFile objects.
     * @return A list of registered OriginalFile objects.
     *
     */
    private List<OriginalFile> knownOriginalFiles(Collection<OriginalFile> files, Principal currentUser)  {
        List<OriginalFile> rv = new ArrayList<OriginalFile>();
        for (OriginalFile f : files) {
            OriginalFile oFile = getOriginalFile(f.getPath().getValue(), f.getName().getValue(), currentUser);
            if (oFile != null) {
                rv.add(oFile);
            } else {
                rv.add(f);
            }
        }
        return rv;
    }


    private  List<ImportContainer> importableImageFiles(String path, int depth) {
        String paths [] = {path};
        ImportableFiles imp = new ImportableFiles(paths, depth);
        List<ImportContainer> containers = imp.getContainers();
        return containers;
    }

    private List<FileSet> processImportContainers(List<ImportContainer> containers,
            List<String> names, boolean showOriginalFiles, Principal currentUser) {
        List<FileSet> rv = new ArrayList<FileSet>();

        for (ImportContainer ic : containers) {
            FileSet set = new FileSet();
            OriginalFile oFile;

            set.importableImage = true;
            set.fileName = ic.getFile().getAbsolutePath();

            set.parentFile = getOriginalFile(getRelativePath(ic.getFile()),ic.getFile().getName(), currentUser);
            if (set.parentFile == null) {
                set.parentFile = createOriginalFile(ic.getFile());
            }

            set.hidden = ic.getFile().isHidden();
            set.dir = ic.getFile().isDirectory();
            set.reader = ic.getReader();
            set.imageCount = ic.getBfImageCount().intValue();

            set.usedFiles = new ArrayList<IObject>();
            List<String> iFileList = Arrays.asList(ic.getUsedFiles());
            for (String iFile : iFileList)  {
                removeNameFromFileList(iFile, names);
                if (showOriginalFiles) {
                    File f = new File(iFile);
                    oFile = getOriginalFile(getRelativePath(f),f.getName(), currentUser);
                    if (oFile != null) {
                        set.usedFiles.add(oFile);
                    } else {
                        set.usedFiles.add(createOriginalFile(f));
                    }
                }
            }

            int i = 0;
            set.imageList = new ArrayList<Image>();
            List<String> iNames = ic.getBfImageNames();
            for (Pixels pix : ic.getBfPixels())  {
                Image image;
                String imageName;
                pix = createPixels(pix);
                imageName = iNames.get(i);
                if (imageName == null) {
                    imageName = NO_NAME_SET;
                }
                else if (imageName.equals("")) {
                    imageName = NO_NAME_SET;
                }
                image = getImage(set.fileName, i, currentUser);
                if (image == null) {
                    image = createImage(imageName, pix);
                }
                set.imageList.add(image);
                i++;
            }
            rv.add(set);
        }

        // Add the left over files in the directory as OrignalFile objects
        if (names.size() > 0) {
            for (String iFile : names) {
                File f = new File(iFile);
                FileSet set = new FileSet();
                OriginalFile oFile;

                set.importableImage = false;
                set.fileName = iFile;

                set.parentFile = getOriginalFile(getRelativePath(f),f.getName(), currentUser);
                if (set.parentFile == null) {
                    set.parentFile = createOriginalFile(f);
                }

                set.hidden = f.isHidden();
                set.dir = f.isDirectory();
                set.imageCount = 0;

                set.usedFiles = new ArrayList<IObject>();
                if (showOriginalFiles) {
                    oFile = getOriginalFile(getRelativePath(f),f.getName(), currentUser);
                    if (oFile != null) {
                        set.usedFiles.add(oFile);
                    } else {
                        set.usedFiles.add(createOriginalFile(f));
                    }
                }
                set.imageList = new ArrayList<Image>();

                rv.add(set);
            }
        }

        return rv;
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
     * Create an Image object corresponding to an imagename and pixels object.
     *
     * @param imageName
     *            A String.
     * @param pix
     *            A Pixels object.
     * @return An Image object
     *
     */
    private Image createImage(String imageName, Pixels pix) {
        Image image = new ImageI();
        image.setName(rstring(imageName));
        // Property must be not-null but will be overwritten on metadat import.
        image.setAcquisitionDate(rtime(java.lang.System.currentTimeMillis()));
        image.addPixels(pix);
        return image;
    }

    /**
     * Create a fuller Pixels object from a Pixels object.
     *
     * @param pix
     *            A Pixels object.
     * @return An Pixels object
     *
     */
    private Pixels createPixels(Pixels pix) {
        // Use the same for all Pixels for now.
        DimensionOrder dimOrder = getDimensionOrder("XYZCT");
        pix.setDimensionOrder(dimOrder);
        pix.setPixelsType(getPixelsType(pix.getPixelsType().getValue().getValue()));
        pix.setSha1(rstring("UNKNOWN"));
        return pix;
    }


    /**
     * Get an {@link OriginalFile} object at the given path and name. Returns null if
     * the OriginalFile does not exist or does not belong to this repo.
     *
     * @param path
     *            A path to a file.
     * @return OriginalFile object.
     *
     */
    private OriginalFile getOriginalFile(final String path, final String name, final Principal currentUser)  {
        final String uuid = getRepoUuid();
        ome.model.core.OriginalFile oFile = (ome.model.core.OriginalFile) executor
                .execute(currentUser, new Executor.SimpleWork(this, "getOriginalFile", uuid, path, name) {
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        try {
                            Long id = sql.findRepoFile(uuid, path, name, null);
                            return sf.getQueryService().find(ome.model.core.OriginalFile.class, id.longValue());
                        } catch (EmptyResultDataAccessException e) {
                            return null;
                        }
                    }
                });

        if (oFile == null)
        {
            return null;
        }
        IceMapper mapper = new IceMapper();
        OriginalFile rv = (OriginalFile) mapper.map(oFile);
        return rv;
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
                            String path = sql.findRepoFilePath(uuid, id);

                            if (path == null) {
                                return null;
                            }

                            return new File(root, path);
                    }
                });
    }

    /**
     * Get an Image with path corresponding to the parameter path and the count.
     *
     * @param path
     *            A path to a file.
     * @return List of Image objects, empty if the query returned no values.
     *
     */
    private Image getImage(String fullPath, final int count, final Principal currentUser)  {

        File f = new File(fullPath);
        final String uuid = getRepoUuid();
        final String path = getRelativePath(f);
        final String name = f.getName();

        ome.model.core.Image image = (ome.model.core.Image) executor
                .execute(currentUser, new Executor.SimpleWork(this, "getImage") {

                    @SuppressWarnings("unchecked")
                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        List<Long> pixIds = sql.findRepoPixels(uuid, path, name);
                        if (pixIds == null || pixIds.size() == 0) {
                            return null;
                        }

                        long pixelsId = 0;
                        for (Long pId : pixIds) {
                            Map<String, String> params = sql.getPixelsParams(pId.longValue());
                            if (Integer.parseInt(params.get(IMAGE_NO_KEY)) == count) {
                                pixelsId = pId.longValue();
                                break;
                            }
                        }
                        if (pixelsId == 0) {
                            return null;
                        }
                        Long imageId = sql.findRepoImageFromPixels(pixelsId);

                        return sf.getQueryService().find(ome.model.core.Image.class, imageId.longValue());
                    }
                });

        if (image == null)
        {
            return null;
        }
        IceMapper mapper = new IceMapper();
        Image rv = (Image) mapper.map(image);
        return rv;
    }
    /**
     * Get an Image with path corresponding to the parameter path and the count.
     *
     * @param path
     *            A path to a file.
     * @return List of Image objects, empty if the query returned no values.
     *
     */
    @SuppressWarnings("unchecked")
    private Map<Integer, Image> getImageMap(OriginalFile keyFile, Principal currentUser)  {

        final String uuid = getRepoUuid();
        final String path = keyFile.getPath().getValue();
        final String name = keyFile.getName().getValue();

        Map<Integer, ome.model.core.Image> imageMap = (Map<Integer, ome.model.core.Image>) executor
                .execute(currentUser, new Executor.SimpleWork(this, "getImageMap") {

                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {

			Map<Integer, ome.model.core.Image> iMap = new HashMap<Integer, ome.model.core.Image>();

                        List<Long> pixIds = sql.findRepoPixels(uuid, path, name);
                        if (pixIds == null || pixIds.size() == 0) {
                            return iMap;
                        }

                        for (Long pId : pixIds) {
                            Map<String, String> params = sql.getPixelsParams(pId.longValue());
                            long pixelsId = pId.longValue();
                            Long imageId = sql.findRepoImageFromPixels(pixelsId);
                            ome.model.core.Image image = sf.getQueryService().find(ome.model.core.Image.class, imageId.longValue());
                            iMap.put(new Integer(params.get(IMAGE_NO_KEY)),image);
                        }
                        return iMap;
                    }
                });

        IceMapper mapper = new IceMapper();
        Map<Integer, Image> rv = mapper.map(imageMap);
        return rv;
    }

    /**
     * Create a jpeg thumbnail from an image file using the zeroth image
     *
     * @param path
     *            A path to a file.
     * @return The path of the thumbnail
     *
     */
    private String createThumbnail(File file) throws ServerError {
        return createThumbnail(file, 0);
    }

    /**
     * Create a jpeg thumbnail from an image file using the nth image
     *
     * @param path
     *            A path to a file.
     * @param imageIndex
     *            the image index in a multi-image file.
     * @return The path of the thumbnail
     *
     * TODO Weak at present, no caching
     */
    private String createThumbnail(File file, int imageIndex) throws ServerError {
        // Build a path to the thumbnail
        File parent = file.getParentFile();
        File tnParent = new File(new File(parent, OMERO_PATH), THUMB_PATH);
        tnParent.mkdirs(); // Need to check if this is created?
        File tnFile = new File(tnParent, file.getName() + "_" + Integer.toString(imageIndex) + "_tn.jpg");
        // Very basic caching... if a file exists return it.
        if (tnFile.exists()) {
            return tnFile.getAbsolutePath();
        }

        // First get the thumb bytes from the image file
        IFormatReader reader = new ImageReader();
        byte[] thumb;
        reader.setNormalized(true);
        try {
            reader.setId(file.getAbsolutePath());
            reader.setSeries(imageIndex);
            // open middle image thumbnail
            int z = reader.getSizeZ() / 2;
            int t = reader.getSizeT() / 2;
            int ndx = reader.getIndex(z, 0, t);
            thumb = reader.openThumbBytes(ndx);
        } catch (FormatException exc) {
            throw new ServerError(null, stackTraceAsString(exc),
                    "Thumbnail error, read failed.");
        } catch (IOException exc) {
            throw new ServerError(null, stackTraceAsString(exc),
                    "Thumbnail error, read failed.");
        }

        // Next create the metadata for the thumbnail image file.
        // How much of this is needed for a jpeg?
        // At present provides monochrome images for some formats, need to provide colour?
        IMetadata meta = null;
        try {
            // Fully qualified to avoid collisions with OMERO service factory
            loci.common.services.ServiceFactory sf =
                new loci.common.services.ServiceFactory();
            meta = sf.getInstance(OMEXMLService.class).createOMEXMLMetadata();
        } catch (DependencyException e) {
            throw new ServerError(null, stackTraceAsString(e),
                    "Thumbnail error, could not create OME-XML service.");
        } catch (ServiceException e) {
            throw new ServerError(null, stackTraceAsString(e),
                    "Thumbnail error, could not create OME-XML metadata.");
        }
        int thumbSizeX = reader.getThumbSizeX();
        int thumbSizeY = reader.getThumbSizeY();
        meta.createRoot();
        meta.setImageID("Image:0", 0);
        meta.setPixelsID("Pixels:0", 0);
        meta.setPixelsBinDataBigEndian(Boolean.TRUE, 0, 0);
        meta.setPixelsDimensionOrder(ome.xml.model.enums.DimensionOrder.XYZCT, 0);
        meta.setPixelsType(ome.xml.model.enums.PixelType.UINT8, 0);
        meta.setPixelsSizeX(new PositiveInteger(thumbSizeX), 0);
        meta.setPixelsSizeY(new PositiveInteger(thumbSizeY), 0);
        meta.setPixelsSizeZ(new PositiveInteger(1), 0);
        meta.setPixelsSizeC(new PositiveInteger(1), 0);
        meta.setPixelsSizeT(new PositiveInteger(1), 0);
        meta.setChannelID("Channel:0:0", 0, 0);
        meta.setChannelSamplesPerPixel(new PositiveInteger(1), 0, 0);

        // Finally try to create the jpeg file abd return the path.
        IFormatWriter writer = new ImageWriter();
        writer.setMetadataRetrieve(meta);
        try {
            writer.setId(tnFile.getAbsolutePath());
            writer.saveBytes(0, thumb);
            writer.close();
        } catch (FormatException exc) {
            throw new ServerError(null, stackTraceAsString(exc),
                    "Thumbnail error, write failed.\n File id: " + tnFile.getAbsolutePath());
        } catch (IOException exc) {
            throw new ServerError(null, stackTraceAsString(exc),
                    "Thumbnail error, write failed.\n File id: " + tnFile.getAbsolutePath());
        }

        return tnFile.getAbsolutePath();
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

    /**
     * Utility to a build map of DimensionOrder objects keyed by value.
     * This is run once by getDimensionOrder() when first needed,
     * thereafter lookups are local.
     *
     * TODO: this should probably be done in the constructor?
     */
    private Map<String, DimensionOrder> buildDimensionOrderMap(Map<String, DimensionOrder> dimensionOrderMap) {
        List <DimensionOrder> dimensionOrderList;
        List<ome.model.enums.DimensionOrder> dimOrderList = (List<ome.model.enums.DimensionOrder>) executor
                .execute(principal, new Executor.SimpleWork(this, "buildDimensionOrderMap") {

                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        return sf.getQueryService().findAllByQuery("from DimensionOrder as d",
                                null);
                    }
                });

        IceMapper mapper = new IceMapper();
        dimensionOrderList = (List<DimensionOrder>) mapper.map(dimOrderList);

        for (DimensionOrder dimensionOrder : dimensionOrderList) {
            dimensionOrderMap.put(dimensionOrder.getValue().getValue(), dimensionOrder);
        }
        return dimensionOrderMap;
    }

    /**
     * Utility to a build map of PixelsType objects keyed by value.
     * This is run once by getPixelsType() when first needed,
     * thereafter lookups are local.
     *
     * TODO: this should probably be done in the constructor?
     */
    private Map<String, PixelsType> buildPixelsTypeMap(Map<String, PixelsType> pixelsTypeMap) {
        List <PixelsType> pixelsTypeList;
        List<ome.model.enums.PixelsType> pixTypeList = (List<ome.model.enums.PixelsType>) executor
                .execute(principal, new Executor.SimpleWork(this, "buildPixelsTypeMap") {

                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        return sf.getQueryService().findAllByQuery("from PixelsType as p",
                                null);
                    }
                });

        IceMapper mapper = new IceMapper();
        pixelsTypeList = (List<PixelsType>) mapper.map(pixTypeList);

        for (PixelsType pixelsType : pixelsTypeList) {
            pixelsTypeMap.put(pixelsType.getValue().getValue(), pixelsType);
        }
        return pixelsTypeMap;
    }

    private String getRelativePath(File f) {
        String path = f.getParent()
                .substring(root.getAbsolutePath().length(), f.getParent().length());
        // The parent doesn't contain a trailing slash.
        path = path + "/";
        return path;
    }

    /**
     * Utility to remove a string from a list of strings if it exists.
     *
     */
    private void removeNameFromFileList(String sText, List<String> sList) {
        int index;
        for(index = 0; index < sList.size(); index ++) {
            if (sText.equals(sList.get(index))) break;
        }
        if (index < sList.size()) sList.remove(index);
    }

    // Utility function for passing stack traces back in exceptions.
    private String stackTraceAsString(Throwable t) {
        StringWriter sw = new StringWriter();
        t.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    private Principal currentUser(Current __current) {
        return new Principal(__current.ctx.get(omero.constants.SESSIONUUID.value));
    }

}
