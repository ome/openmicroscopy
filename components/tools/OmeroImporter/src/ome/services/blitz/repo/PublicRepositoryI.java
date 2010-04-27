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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.MimetypesFileTypeMap;

import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.IFormatWriter;
import loci.formats.ImageReader;
import loci.formats.ImageWriter;
import loci.formats.MetadataTools;
import loci.formats.meta.IMetadata;
import ome.formats.importer.ImportContainer;
import ome.services.util.Executor;
import ome.system.Principal;
import ome.system.ServiceFactory;
import omero.ServerError;
import omero.ValidationException;
import omero.api.RawFileStorePrx;
import omero.api.RawPixelsStorePrx;
import omero.api.RenderingEnginePrx;
import omero.api.ThumbnailStorePrx;
import omero.grid.RepositoryPrx;
import omero.grid._RepositoryDisp;
import omero.model.Format;
import omero.model.DimensionOrder;
import omero.model.PixelsType;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.Pixels;
import omero.model.PixelsI;
import omero.util.IceMapper;

import omero.grid.FileSet;
import omero.grid.RepositoryListConfig;
import ome.services.blitz.repo.FileSetI;
import ome.services.blitz.repo.RepositoryListConfigI;

import ome.formats.importer.ImportContainer;
import ome.services.blitz.repo.ImportableFiles;

import loci.common.services.DependencyException;
import loci.common.services.ServiceException;
import loci.formats.*; // need to close this down once the r/w are sorted.
import loci.formats.meta.IMetadata;
import loci.formats.services.OMEXMLService;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;

/**
 * An implementation of he PublicRepository interface
 * 
 * @author Colin Blackburn <cblackburn at dundee dot ac dot uk>
 */
public class PublicRepositoryI extends _RepositoryDisp {

    private final static Log log = LogFactory.getLog(PublicRepositoryI.class);

    private final long id;

    private final File root;

    private final Executor executor;

    private final Principal principal;
    
    private final static String OMERO_PATH = ".omero";
    private final static String THUMB_PATH = "thumbnails";

    public PublicRepositoryI(File root, long repoObjectId, Executor executor,
            Principal principal) throws Exception {
        this.id = repoObjectId;
        this.executor = executor;
        this.principal = principal;

        if (root == null || !root.isDirectory()) {
            throw new ValidationException(null, null,
                    "Root directory must be a existing, readable directory.");
        }
        this.root = root.getAbsoluteFile();

    }

    public OriginalFile root(Current __current) throws ServerError {
        return new OriginalFileI(this.id, false); // SHOULD BE LOADED.
    }


    /**
     * Register an OriginalFile using its path
     *
     * @param path
     *            Absolute path of the file to be registered.
     * @param __current
     *            ice context.
     * @return The OriginalFile with id set (unloaded)
     *
     */
    public OriginalFile register(String path, Format fmt, Current __current)
            throws ServerError {

        if (path == null || fmt == null
                || (fmt.getId() == null && fmt.getValue() == null)) {
            throw new ValidationException(null, null,
                    "path and fmt are required arguments");
        }

        File file = new File(path).getAbsoluteFile();
        OriginalFile omeroFile = new OriginalFileI();
        omeroFile = createOriginalFile(file);
        omeroFile.setFormat(fmt);

        IceMapper mapper = new IceMapper();
        final ome.model.core.OriginalFile omeFile = (ome.model.core.OriginalFile) mapper
                .reverse(omeroFile);
        Long id = (Long) executor.execute(principal, new Executor.SimpleWork(
                this, "register") {
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
     * Register an IObject object
     * 
     * @param obj
     *            IObject object.
     * @param __current
     *            ice context.
     * @return The IObject with id set (unloaded)
     *
     */
    public IObject registerObjectWithName(IObject obj, String omeName, Current __current)
            throws ServerError {

        if (obj == null) {
            throw new ValidationException(null, null,
                    "obj is required argument");
        }
        if (omeName == "" || omeName == null) {
            throw new ValidationException(null, null,
                    "omeName is required argument");
        }
        
        IceMapper mapper = new IceMapper();
        final ome.model.IObject omeObj = (ome.model.IObject) mapper
                .reverse(obj);
        
        Long id = (Long) executor.execute(principal, new Executor.SimpleWork(
                this, "register") {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                return sf.getUpdateService().saveAndReturnObject(omeObj).getId();
            }
        });
        
        obj.setId(rlong(id));
        obj.unload();
        
        registerUserId(obj, omeName);

        return obj;
    }

    /**
     * Register an IObject object
     * 
     * @param obj
     *            IObject object.
     * @param __current
     *            ice context.
     * @return The IObject with id set (unloaded)
     *
     */
    public IObject registerObject(IObject obj, Current __current)
            throws ServerError {

        if (obj == null) {
            throw new ValidationException(null, null,
                    "obj is required argument");
        }

        IceMapper mapper = new IceMapper();
        final ome.model.IObject omeObj = (ome.model.IObject) mapper
                .reverse(obj);
        
        Long id = (Long) executor.execute(principal, new Executor.SimpleWork(
                this, "register") {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                return sf.getUpdateService().saveAndReturnObject(omeObj).getId();
            }
        });
        
        obj.setId(rlong(id));
        obj.unload();
        
        return obj;
    }

    private void registerUserId(IObject obj, String omeName) throws ServerError {
        
        IceMapper mapper = new IceMapper();
        final ome.model.IObject omeObj = (ome.model.IObject) mapper
                .reverse(obj);
        final String oName = omeName;
        Long id = (Long) executor.execute(principal, new Executor.SimpleWork(
                this, "registerUserId") {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                sf.getAdminService().changeOwner(omeObj, oName);
                return omeObj.getId();
            }
        });
        
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
     * @param __current
     *            ice context.
     * @return List of OriginalFile objects at path
     *
     */
    public List<OriginalFile> list(String path, RepositoryListConfig config, Current __current) throws ServerError {
        File file = checkPath(path);
        List<File> files;
        List<OriginalFile> oFiles;
        RepositoryListConfig conf;
        
        if(config == null) {
            conf = new RepositoryListConfigI();
        } else {
            conf = config;
        }
        files = filteredFiles(file, conf);
        oFiles = filesToOriginalFiles(files);
        if (conf.registered) {
            oFiles = knownOriginalFiles(oFiles);
        }
        return oFiles;
    }

    /**
     * Get a list of those files as importable and non-importable list.
     * 
     * @param path
     *            A path on a repository.
     * @param __current
     *            ice context.
     * @return A Map of IObjects keyed by filename
     * 
     * The map uses the object name as key. This is the file name but should be something
     * guaranteed to be unique. 
     *
     * The crude test for an image file in a list of importable objects is the extension.
     * If it is the same as the key's extension then it is treated as an image. A more
     * certain/elegant method should probably be used especially considee
     */
     public List<FileSet> listObjects(String path, RepositoryListConfig config, Current __current)
            throws ServerError {
        List<FileSet> rv = new ArrayList<FileSet>();
        // Use the same for all Pixels
        DimensionOrder dimOrder = getDimensionOrder("XYZCT");
        File file = checkPath(path);
        RepositoryListConfig conf;
        
        if(config == null) {
            conf = new RepositoryListConfigI();
        } else {
            conf = config;
        }
        List<File> files = filteredFiles(file, conf);
        List<String> names = filesToPaths(files);
        List<ImportContainer> containers = importableImageFiles(path, conf.depth);

        for (ImportContainer ic : containers) {
            FileSet set = new FileSetI();
            List<OriginalFile> ofList;
            
            set.importableImage = true;
            set.fileName = ic.getFile().getAbsolutePath();
            set.imageName = set.fileName; //ic.imageName seems to be empty
            set.reader = ic.getReader();
            set.imageCount = ic.getBfImageCount();
                        
            set.usedFiles = new ArrayList<IObject>();
            List<String> iFileList = Arrays.asList(ic.getUsedFiles());
            for (String iFile : iFileList)  {
                removeNameFromFileList(iFile, names);
                ofList = getOriginalFiles(iFile);
                if (ofList != null && ofList.size() != 0) {
                    set.usedFiles.add(ofList.get(0));
                } else {
                    set.usedFiles.add(createOriginalFile(new File(iFile)));   
                }
            }
            
            int i = 0;
            set.imageList = new ArrayList<Image>();
            List<String> iNames = ic.getBfImageNames();
            for (Pixels pix : ic.getBfPixels())  {
                Image image;
                String imageName;
                pix.setDimensionOrder(dimOrder);
                pix.setSha1(rstring("UNKNOWN"));
                pix.setPixelsType(getPixelsType(pix.getPixelsType().getValue().getValue()));
                if (set.imageCount == 1) {
                    imageName = set.imageName;
                } else {
                    imageName = iNames.get(i);
                    if (imageName == null) {
                        imageName = "";
                    }
                }
                
                if (imageName != "") {
                    List<Image> iList = getImages(imageName);
                    if (iList != null && iList.size() != 0) {
                        image = iList.get(0);
                    } else {
                        image = createImage(imageName, pix);   
                    }
                } else {
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
                FileSet set = new FileSetI();
                List<OriginalFile> ofList;
            
                set.importableImage = false;
                set.fileName = iFile;
                set.imageCount = 0;
                        
                set.usedFiles = new ArrayList<IObject>();
                ofList = getOriginalFiles(iFile);
                if (ofList != null && ofList.size() != 0) {
                    set.usedFiles.add(ofList.get(0));
                } else {
                    set.usedFiles.add(createOriginalFile(new File(iFile)));   
                }
                rv.add(set);
            }
        }
        
        return rv;
    }

    
    
    /**
     * Get the format object for a file.
     * 
     * @param path
     *            A path on a repository.
     * @param __current
     *            ice context.
     * @return Format object
     *
     */
    public Format format(String path, Current __current) throws ServerError {
        File file = checkPath(path);
        return getFileFormat(file);
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
        File file = checkPath(path);
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
        File file = checkPath(path);
        String tnPath;
        try {
            tnPath = createThumbnail(file, imageIndex);   
        } catch (ServerError exc) {
            throw exc;
        }
        return tnPath;
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

    public RawPixelsStorePrx pixels(String path, Current __current)
            throws ServerError {
        // TODO Auto-generated method stub
        return null;
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
        
        // If system is true list all files othersise only those files not starting with "."
        if (config.system) {
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
     * Get the Format for a file using its MIME content type
     * 
     * @param file
     *            A File in a repository.
     * @return A Format object
     *
     * TODO Return the correct Format object in place of a dummy one
     */
    private Format getFileFormat(File file) {

        final String contentType = new MimetypesFileTypeMap().getContentType(file);

        ome.model.enums.Format format = (ome.model.enums.Format) executor
                .execute(principal, new Executor.SimpleWork(this, "getFileFormat") {

                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        return sf.getQueryService().findByQuery(
                                "from Format as f where f.value='"
                                        + contentType + "'", null);
                    }
                });
                
        IceMapper mapper = new IceMapper();
        return (Format) mapper.map(format);

    }

    /**
     * Get the DimensionOrder
     * 
     * @param String
     *            A string representing the dimension order
     * @return A DimensionOrder object
     *
     */
    private DimensionOrder getDimensionOrder(String dimensionOrder) {
        final String dim = dimensionOrder;
        ome.model.enums.DimensionOrder dimOrder = (ome.model.enums.DimensionOrder) executor
                .execute(principal, new Executor.SimpleWork(this, "getDimensionOrder") {

                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        return sf.getQueryService().findByQuery(
                                "from DimensionOrder as d where d.value='"
                                        + dim + "'", null);
                    }
                });
                
        IceMapper mapper = new IceMapper();
        return (DimensionOrder) mapper.map(dimOrder);

    }

    /**
     * Get the PixelsType
     * 
     * @param String
     *            A string representing the pixels type
     * @return A PixelsType object
     *
     * TODO: This db look-up per Pixels object needs 
     * to be a local look-up from a HashMap built in
     * the constructor.
     */
    private PixelsType getPixelsType(String pixelsType) {
        final String pType = pixelsType;
        ome.model.enums.PixelsType pixType = (ome.model.enums.PixelsType) executor
                .execute(principal, new Executor.SimpleWork(this, "getPixelsType") {

                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        return sf.getQueryService().findByQuery(
                                "from PixelsType as p where p.value='"
                                        + pType + "'", null);
                    }
                });
                
        IceMapper mapper = new IceMapper();
        return (PixelsType) mapper.map(pixType);

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
        List rv = new ArrayList<OriginalFile>();
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
        List rv = new ArrayList<String>();
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
    private List<OriginalFile> knownOriginalFiles(Collection<OriginalFile> files)  {
        List rv = new ArrayList<OriginalFile>();
        for (OriginalFile f : files) {
            List<OriginalFile> fileList = getOriginalFiles(f.getPath().getValue());
            if (fileList.size() > 0) {
                rv.add(fileList.get(0));
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

    /**
     * Create an OriginalFile object corresponding to a File object.
     * 
     * @param f
     *            A File object.
     * @return An OriginalFile object
     *
     * TODO populate more attribute fields than the few set here.
     */
    private OriginalFile createOriginalFile(File f) {
        OriginalFile file = new OriginalFileI();
        file.setPath(rstring(f.getAbsolutePath()));
        file.setMtime(rtime(f.lastModified()));
        file.setSize(rlong(f.length()));
        file.setSha1(rstring("UNKNOWN"));
        // What more do I need to set here, more times, details?
        
        // This needs to be unique - see ticket #1753
        file.setName(rstring(f.getAbsolutePath()));
        
        file.setFormat(getFileFormat(f));
        
        return file;
    }
    
    /**
     * Create an Image object corresponding to a File object.
     * 
     * @param f
     *            A File object.
     * @return An Image object
     *
     * TODO populate more attribute fields than the few set here.
     */
    private Image createImage(String imageName, Pixels pix) {
        Image image = new ImageI();
        image.setName(rstring(imageName));        
        image.setAcquisitionDate(rtime(java.lang.System.currentTimeMillis()));
        image.addPixels(pix);
        return image;
    }
    
    /**
     * Get a list of OriginalFiles with path corresponding to the paramater path.
     * 
     * @param path
     *            A path to a file.
     * @return List of OriginalFile objects, empty if the query returned no values.
     *
     * TODO Weak at present, returns all matched files based on path.
     *      There should be further checking for uniqueness
     */
    private List<OriginalFile> getOriginalFiles(String path)  {
        
        List rv = new ArrayList<OriginalFile>();
        final String queryString = "from OriginalFile as o where o.path = '"
                    + path + "'";
        List<ome.model.core.OriginalFile> fileList = (List<ome.model.core.OriginalFile>) executor
                .execute(principal, new Executor.SimpleWork(this, "getOriginalFiles") {

                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        return sf.getQueryService().findAllByQuery(queryString,
                                null);
                    }
                });
            
        if (fileList == null || fileList.size() == 0) {
            return rv;
        }
        IceMapper mapper = new IceMapper();
        rv = (List<OriginalFile>) mapper.map(fileList);

        return rv;
    }

    /**
     * Get a list of Images with path corresponding to the paramater path.
     * 
     * @param path
     *            A path to a file.
     * @return List of Image objects, empty if the query returned no values.
     *
     * TODO Weak at present, returns all matched files based on path.
     *      There should be further checking for uniqueness
     */
    private List<Image> getImages(String path)  {
        
        List rv = new ArrayList<Image>();
        final String queryString = "from Image as i where i.name = '"
                    + path + "'";
        List<ome.model.core.Image> fileList = (List<ome.model.core.Image>) executor
                .execute(principal, new Executor.SimpleWork(this, "getImages") {

                    @Transactional(readOnly = true)
                    public Object doWork(Session session, ServiceFactory sf) {
                        return sf.getQueryService().findAllByQuery(queryString,
                                null);
                    }
                });
            
        if (fileList == null || fileList.size() == 0) {
            return rv;
        }
        IceMapper mapper = new IceMapper();
        rv = (List<Image>) mapper.map(fileList);

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
        int pixelType = FormatTools.UINT8;            
        meta.createRoot();
        meta.setPixelsBigEndian(Boolean.TRUE, 0, 0);
        meta.setPixelsDimensionOrder("XYZCT", 0, 0);
        meta.setPixelsPixelType(FormatTools.getPixelTypeString(pixelType), 0, 0);
        meta.setPixelsSizeX(thumbSizeX, 0, 0);
        meta.setPixelsSizeY(thumbSizeY, 0, 0);
        meta.setPixelsSizeZ(1, 0, 0);
        meta.setPixelsSizeC(1, 0, 0);
        meta.setPixelsSizeT(1, 0, 0);
        meta.setLogicalChannelSamplesPerPixel(1, 0, 0);
        
        // Finally try to create the jpeg file abd return the path.  
        IFormatWriter writer = new ImageWriter();
        writer.setMetadataRetrieve(meta);
        try {
            writer.setId(tnFile.getAbsolutePath());
            writer.saveBytes(thumb, true);
            writer.close();  
        } catch (FormatException exc) { 
            throw new ServerError(null, stackTraceAsString(exc), 
                    "Thumbnail error, write failed."); 
        } catch (IOException exc) { 
            throw new ServerError(null, stackTraceAsString(exc), 
                    "Thumbnail error, write failed."); 
        }
        
        return tnFile.getAbsolutePath();
	}

    // Utility function for passing stack traces back in exceptions.
    private String stackTraceAsString(Exception exception) {
        StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
    
    private void removeNameFromFileList(String sText, List<String> sList) {
        int index;
        for(index = 0; index < sList.size(); index ++) {
            if (sText.equals(sList.get(index))) break;
        }
        if (index < sList.size()) sList.remove(index);
    }
}
