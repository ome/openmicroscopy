/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import Ice.Current;

/**
 * 
 * @since Beta4.1
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
     * Register an OriginalFile object
     * 
     * @param file
     *            OriginalFile object.
     * @param __current
     *            ice context.
     * @return The OriginalFile with id set (unloaded)
     *
     */
    public OriginalFile registerOriginalFile(OriginalFile file, Current __current)
            throws ServerError {

        if (file == null) {
            throw new ValidationException(null, null,
                    "file is required argument");
        }

        IceMapper mapper = new IceMapper();
        final ome.model.core.OriginalFile omeFile = (ome.model.core.OriginalFile) mapper
                .reverse(file);
        Long id = (Long) executor.execute(principal, new Executor.SimpleWork(
                this, "registerOriginalFile") {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                return sf.getUpdateService().saveAndReturnObject(omeFile).getId();
            }
        });
        
        file.setId(rlong(id));
        file.unload();
        return file;
    }


    /**
     * Register an Image object
     * 
     * @param file
     *            Image object.
     * @param __current
     *            ice context.
     * @return The Image with id set (unloaded)
     *
     */
    public Image registerImage(Image image, Current __current)
            throws ServerError {

        if (image == null) {
            throw new ValidationException(null, null,
                    "image is required argument");
        }

        IceMapper mapper = new IceMapper();
        final ome.model.core.Image omeImage = (ome.model.core.Image) mapper
                .reverse(image);
        Long id = (Long) executor.execute(principal, new Executor.SimpleWork(
                this, "registerImage") {
            @Transactional(readOnly = false)
            public Object doWork(Session session, ServiceFactory sf) {
                return sf.getUpdateService().saveAndReturnObject(omeImage).getId();
            }
        });
        
        image.setId(rlong(id));
        image.unload();
        return image;
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
        
        if (conf.registered) {
            oFiles = knownOriginalFiles(files);
        } else {
            oFiles = filesToOriginalFiles(files);
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
        File file = checkPath(path);
        RepositoryListConfig conf;
        
        if(config == null) {
            conf = new RepositoryListConfigI();
        } else {
            conf = config;
        }
        List<File> files = filteredFiles(file, conf);
        List<String> names = filesToPaths(files);
        Map<String, List<String>> importableFiles = importableImageFiles(files, conf.depth);
        
        // Add the importable files as Image/OriginalFiles
        for (String keyFile : importableFiles.keySet()) {
            FileSet set = new FileSetI();
            set.usedFiles = new ArrayList<IObject>();
            set.importableImage = true;
            List<String> iFileList = importableFiles.get(keyFile);
            for (String iFile : iFileList)  {
                removeNameFromFileList(iFile, names);
                // Primary file as Image object
                if (keyFile.equals(iFile)) {
                    set.name = iFile;
                    List<OriginalFile> ofList = getOriginalFiles(iFile);
                    if (ofList != null && ofList.size() != 0) {
                        set.file = ofList.get(0);
                    } else {
                        set.file = createOriginalFile(new File(iFile));   
                    }
                // Remaining used files as OriginalFile objects
                } else {
                    List<OriginalFile> ofList = getOriginalFiles(iFile);
                    if (ofList != null && ofList.size() != 0) {
                        set.usedFiles.add(ofList.get(0));
                    } else {
                        set.usedFiles.add(createOriginalFile(new File(iFile)));   
                    }
                }
            }
            rv.add(set);
        }
        
        // Add the left over files in the directory as OrignalFile objects
        if (names.size() > 0) {
            for (String iFile : names) {
                FileSet set = new FileSetI();
                set.usedFiles = new ArrayList<IObject>();
                set.importableImage = false;
                set.name = iFile;
                List<OriginalFile> ofList = getOriginalFiles(iFile);
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
     * Get IObject objects corresponding to a collection of File objects.
     * 
     * @param files
     *            A collection of File objects.
     * @return A list of new OriginalFile objects
     *
     */
    private Map<String, List<IObject>> filesToIObjects(Collection<File> files) {
        Map<String, List<IObject>> rv = new HashMap<String, List<IObject>>();
        for (File f : files) {
            List iObjList = new ArrayList<IObject>();
            OriginalFile iObj = createOriginalFile(f);
            iObjList.add(iObj);
            rv.put(f.getAbsolutePath(), iObjList);
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
     * Get files corresponding to a collection paths.
     * 
     * @param paths
     *            A collection of Strings.
     * @return A list of File objects
     *
     */
    private List<File> pathsToFiles(Collection<String> paths) {
        List rv = new ArrayList<File>();
        for (String p : paths) {
            rv.add(new File(p));
        }
        return rv;
    }

    
    /**
     * Get Image objects corresponding to a collection of File objects.
     * 
     * @param files
     *            A collection of File objects.
     * @return A list of new Image objects
     *
     */
    private List<Image> filesToImages(Collection<File> files) {
        List rv = new ArrayList<Image>();
        for (File f : files) {
            rv.add(createImage(f));
        }
        return rv;
    }
    
    /**
     * Get Pixels objects corresponding to a collection of File objects.
     * 
     * @param files
     *            A collection of File objects.
     * @return A list of new Image objects
     *
     */
    private List<Pixels> filesToPixels(Collection<File> files) {
        List rv = new ArrayList<Image>();
        for (File f : files) {
            rv.add(createPixels(f));
        }
        return rv;
    }
    
    /**
     * Get registered OriginalFile objects corresponding to a collection of File objects.
     * 
     * @param files
     *            A collection of File objects.
     * @return A list of registered OriginalFile objects. 
     *
     */
    private List<OriginalFile> knownOriginalFiles(Collection<File> files)  {
        List rv = new ArrayList<OriginalFile>();
        for (File f : files) {
            List<OriginalFile> fileList = getOriginalFiles(f.getAbsolutePath());
            rv.addAll(fileList);
        }
        return rv;
    }

    /**
     * Get registered Image objects corresponding to a collection of File objects.
     * 
     * @param files
     *            A collection of File objects.
     * @return A list of registered Image objects. 
     *
     */
    private List<Image> knownImages(Collection<File> files)  {
        List rv = new ArrayList<Image>();
        for (File f : files) {
            List<Image> fileList = getImages(f.getAbsolutePath());
            rv.addAll(fileList);
        }
        return rv;
    }
    
    private  Map<String, List<String>> importableImageFiles(Collection<File> files, int depth) {
        List<String> pathList = filesToPaths(files);
        String paths [] = (String []) pathList.toArray (new String [pathList.size()]);        
        Map<String, List<String>> importableFiles = new  HashMap<String, List<String>>();

        ImportableFiles imp = new ImportableFiles(paths, depth);
        List<ImportContainer> containers = imp.getContainers();

        for (ImportContainer ic : containers) {
            String name = ic.file.getAbsolutePath();
            importableFiles.put(name, Arrays.asList(ic.usedFiles));
        }
        return importableFiles;
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
    private Image createImage(File f) {
        Image image = new ImageI();
        // This needs to be unique ala ticket #1753
        image.setName(rstring(f.getAbsolutePath()));        
        image.setAcquisitionDate(rtime(java.lang.System.currentTimeMillis()));
        return image;
    }
    
    private Pixels createPixels(File f) {
        Pixels pixels = new PixelsI();
        // This needs to be unique ala ticket #1753
        //pixels.setName(rstring(f.getAbsolutePath()));        
        //image.setAcquisitionDate(rtime(java.lang.System.currentTimeMillis()));
        return pixels;
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
     * Create a jpeg thumbnail from an image file 
     * 
     * @param path
     *            A path to a file.
     * @return The path of the thumbnail
     *
     * TODO Weak at present, no caching
     */
     private String createThumbnail(File file)  throws ServerError {
        
        // Build a path to the thumbnail
        File parent = file.getParentFile();
        File tnParent = new File(new File(parent, OMERO_PATH), THUMB_PATH);
        tnParent.mkdirs(); // Need to check if this is created?
        File tnFile = new File(tnParent, file.getName() + "_tn.jpg");
        
        // Very basic caching... if a file exists return it.
        if (tnFile.exists()) {
            return tnFile.getAbsolutePath();
        }
        // As it doesn't exist, create it.  
        
        // First get the thumb bytes from the image file  
        IFormatReader reader = new ImageReader();
        byte[] thumb;
        reader.setNormalized(true);
        try {
            reader.setId(file.getAbsolutePath());
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
        IMetadata meta = MetadataTools.createOMEXMLMetadata();
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
            return tnFile.getAbsolutePath();  
        } catch (FormatException exc) { 
            throw new ServerError(null, stackTraceAsString(exc), 
                    "Thumbnail error, write failed."); 
        } catch (IOException exc) { 
            throw new ServerError(null, stackTraceAsString(exc), 
                    "Thumbnail error, write failed."); 
        }
        
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