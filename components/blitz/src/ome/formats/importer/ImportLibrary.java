/*
 * ome.formats.importer.ImportLibrary
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import loci.common.DataTools;
import loci.formats.FormatException;
import loci.formats.FormatReader;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.MissingLibraryException;
import loci.formats.UnknownFormatException;
import loci.formats.UnsupportedCompressionException;
import loci.formats.in.MIASReader;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.OverlayMetadataStore;
import ome.formats.importer.util.ErrorHandler;
import ome.formats.model.InstanceProvider;
import ome.util.PixelData;
import omero.ServerError;
import omero.api.RawFileStorePrx;
import omero.api.ServiceFactoryPrx;
import omero.grid.Import;
import omero.grid.ManagedRepositoryPrx;
import omero.grid.ManagedRepositoryPrxHelper;
import omero.grid.RepositoryImportContainer;
import omero.grid.RepositoryMap;
import omero.grid.RepositoryPrx;
import omero.model.Annotation;
import omero.model.Dataset;
import omero.model.FileAnnotation;
import omero.model.IObject;
import omero.model.Image;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.Screen;
import omero.model.WellSample;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * support class for the proper usage of {@link OMEROMetadataStoreClient} and
 * {@link FormatReader} instances. This library was factored out of
 * ImportHandler to support ImportFixture
 *
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision: 1167 $, $Date: 2006-12-15 10:39:34 +0000 (Fri, 15 Dec 2006) $
 * @see FormatReader
 * @see OMEROMetadataStoreClient
 * @see ImportFixture
 * @see IObservable
 * @see IObserver
 * @since 3.0-M3
 */
public class ImportLibrary implements IObservable
{
    private static Log log = LogFactory.getLog(ImportLibrary.class);

    /** The class used to identify the dataset target.*/
    private static final String DATASET_CLASS = "omero.model.Dataset";
    
    /** The class used to identify the screen target.*/
    private static final String SCREEN_CLASS = "omero.model.Screen";
    
    /** Default arraybuf size for planar data transfer. (1MB) */
    public static final int DEFAULT_ARRAYBUF_SIZE = 1048576;

    private final ArrayList<IObserver> observers = new ArrayList<IObserver>();

    private final OMEROMetadataStoreClient store;

    private final OMEROWrapper reader;

    private final ManagedRepositoryPrx repo;

    private byte[] arrayBuf = new byte[DEFAULT_ARRAYBUF_SIZE];

    /** Whether or not to import as metadata only. */
    private boolean isMetadataOnly = false;

    /** Maximum plane width. */
    private int maxPlaneWidth;

    /** Maximum plane height. */
    private int maxPlaneHeight;

    /**
     * The library will not close the client instance. The reader will be closed
     * between calls to import.
     *
     * @param store not null
     * @param reader not null
     */
    public ImportLibrary(OMEROMetadataStoreClient client, OMEROWrapper reader)
    {
        if (client == null || reader == null)
        {
            throw new NullPointerException(
            "All arguments to ImportLibrary() must be non-null.");
        }

        this.store = client;
        this.reader = reader;
        repo = lookupManagedRepository();
        try
        {
            maxPlaneWidth = Integer.parseInt(store.getConfigValue(
                    "omero.pixeldata.max_plane_width"));
        }
        catch (NumberFormatException e)
        {
            log.warn("Pre 4.3.2 server or empty missing " +
                     "omero.pixeldata.max_plane_width, using hard coded " +
                     "maximum plane width!");
            maxPlaneWidth = 3192;
        }
        try
        {
            maxPlaneHeight = Integer.parseInt(store.getConfigValue(
                    "omero.pixeldata.max_plane_height"));
        }
        catch (NumberFormatException e)
        {
            log.warn("Pre 4.3.2 server or empty missing " +
                     "omero.pixeldata.max_plane_height, using hard coded " +
                     "maximum plane height!");
            maxPlaneHeight = 3192;
        }
        if (log.isDebugEnabled())
        {
            log.debug("Maximum plane width: " + maxPlaneWidth);
            log.debug("Maximum plane height: " + maxPlaneHeight);
        }
    }

    /**
     * Sets the metadata only flag.
     * @param isMetadataOnly Whether or not to perform metadata only imports
     * with this import library.
     */
    public void setMetadataOnly(boolean isMetadataOnly)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Setting metadata only flag: " + isMetadataOnly);
        }
        this.isMetadataOnly = isMetadataOnly;
    }

    /**
     * Retrieves the metadata only flag.
     * @return See above.
     */
    public boolean isMetadataOnly()
    {
        return isMetadataOnly;
    }

    //
    // Delegation methods
    //

    public long getExperimenterID()
    {
        return store.getExperimenterID();
    }

    public InstanceProvider getInstanceProvider()
    {
        return store.getInstanceProvider();
    }

    /**
     * Prepares the metadata store using existing metadata that has been
     * pre-registered by OMERO.fs. The expected graph should be fully loaded:
     * <ul>
     *   <li>Image</li>
     *   <li>Pixels</li>
     * </ul>
     * @param existingMetadata Map of imageIndex or series vs. populated Image
     * source graph with the fetched objects defined above.
     */
    public void prepare(Map<Integer, Image> existingMetadata)
    {
        store.prepare(existingMetadata);
    }

    //
    // Observable methods
    //

    public boolean addObserver(IObserver object)
    {
        return observers.add(object);
    }

    public boolean deleteObserver(IObserver object)
    {
        return observers.remove(object);

    }

    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#notifyObservers(ome.formats.importer.ImportEvent)
     */
    public void notifyObservers(ImportEvent event)
    {
        for (IObserver observer : observers) {
            observer.update(this, event);
        }
    }


    // ~ Actions
    // =========================================================================

    /**
     * Primary user method for importing a number
     * 
     * @param config The configuration information.
     * @param candidates Hosts information about the files to import.
     */
    public boolean importCandidates(ImportConfig config, ImportCandidates candidates)
    {
        List<ImportContainer> containers = candidates.getContainers();
        if (containers != null) {
            int numDone = 0;
            for (int index = 0; index < containers.size(); index++) {
                ImportContainer ic = containers.get(index);
                if (DATASET_CLASS.equals(config.targetClass.get()))
                {
                    ic.setTarget(store.getTarget(
                            Dataset.class, config.targetId.get()));
                }
                else if (SCREEN_CLASS.equals(config.targetClass.get()))
                {
                    ic.setTarget(store.getTarget(
                            Screen.class, config.targetId.get()));
                }

                try {
                    importImage(ic,index,numDone,containers.size());
                    numDone++;
                } catch (Throwable t) {
                    log.error("Error on import", t);
                    if (!config.contOnError.get()) {
                        log.info("Exiting on error");
                        return false;
                    } else {
                        log.info("Continuing after error");
                    }
                }
            }
        }
        return true;
    }

    /**
     * Create a RepositoryImportContainer from an ImportContainer.
     * Currently public static for testing purposes.
     * @TODO This method and the RIC return type should be removed post-haste.
     */
    public static RepositoryImportContainer createRepositoryImportContainer(ImportContainer ic) {
        RepositoryImportContainer repoIC = new RepositoryImportContainer();
        repoIC.file = ic.getUsedFiles()[0];
        repoIC.projectId = (ic.getProjectID() == null) ? -1L : ic.getProjectID().longValue();
        repoIC.target = ic.getTarget();
        repoIC.reader = ic.getReader();
        repoIC.usedFiles = ic.getUsedFiles();
        repoIC.isSPW = (ic.getIsSPW() == null) ? false :
        	ic.getIsSPW().booleanValue();
        repoIC.bfImageCount = (ic.getBfImageCount() == null) ? 1 :
        	ic.getBfImageCount().intValue();
        repoIC.bfPixels = ic.getBfPixels();
        repoIC.bfImageNames = ic.getBfImageNames();
        // Assuming that if the array is not null all values are not null.
        if (ic.getUserPixels() == null || ic.getUserPixels().length == 0) {
            repoIC.userPixels = null;
        }
        else {
            double[] userPixels = new double[ic.getUserPixels().length];
            for (int i=0; i < userPixels.length; i++) {
                userPixels[i] = ic.getUserPixels()[i].doubleValue();
            }
            repoIC.userPixels = userPixels;
        }
        repoIC.customImageName = ic.getCustomImageName();
        repoIC.customImageDescription = ic.getCustomImageDescription();
        repoIC.customPlateName = ic.getCustomPlateName();
        repoIC.customPlateDescription = ic.getCustomPlateDescription();
        repoIC.doThumbnails = ic.getDoThumbnails();
        repoIC.customAnnotationList = ic.getCustomAnnotationList();
        return repoIC;
    }

    /** opens the file using the {@link FormatReader} instance */
    private void open(String fileName) throws IOException, FormatException
    {
        reader.close();
        reader.setMetadataStore(store);
        reader.setMinMaxStore(store);
        store.setReader(reader.getImageReader());
        reader.setId(fileName);
        //reset series count
        if (log.isDebugEnabled())
        {
            log.debug("Image Count: " + reader.getImageCount());
        }
    }

    /**
     * Uses the {@link OMEROMetadataStoreClient} to save the current all
     * image metadata provided.
     *
     * @param index Index of the file being imported.
     * @param container The import container which houses all the configuration
     * values for the import.
     * @return the newly created {@link Pixels} id.
     * @throws FormatException if there is an error parsing metadata.
     * @throws IOException if there is an error reading the file.
     */
    private List<Pixels> importMetadata(
            int index, ImportContainer container)
            throws FormatException, IOException
    {
        // 1st we post-process the metadata that we've been given.
        IObject target = container.getTarget();
        notifyObservers(new ImportEvent.BEGIN_POST_PROCESS(
                index, null, target, null, 0, null));
        store.setUserSpecifiedPlateName(container.getCustomPlateName());
        store.setUserSpecifiedPlateDescription(
                container.getCustomPlateDescription());
        store.setUserSpecifiedImageName(container.getCustomImageName());
        store.setUserSpecifiedImageDescription(
                container.getCustomImageDescription());
        Double[] userPixels = container.getUserPixels();
        if (userPixels != null)
            store.setUserSpecifiedPhysicalPixelSizes(
                    userPixels[0], userPixels[1], userPixels[2]);
        store.setUserSpecifiedTarget(container.getTarget());
        store.setUserSpecifiedAnnotations(container.getCustomAnnotationList());
        store.postProcess();
        notifyObservers(new ImportEvent.END_POST_PROCESS(
                index, null, target, null, 0, null));

        notifyObservers(new ImportEvent.BEGIN_SAVE_TO_DB(
                index, null, target, null, 0, null));
        List<Pixels> pixelsList = store.saveToDB();
        notifyObservers(new ImportEvent.END_SAVE_TO_DB(
                index, null, target, null, 0, null));
        return pixelsList;
    }

    /**
     * If available, populates overlays for a given set of pixels objects.
     * @param pixelsList Pixels objects to populate overlays for.
     * @param plateIds Plate object IDs to populate overlays for.
     */
    private void importOverlays(List<Pixels> pixelsList, List<Long> plateIds)
        throws ServerError, FormatException, IOException
    {
        IFormatReader baseReader = reader.getImageReader().getReader();
        if (baseReader instanceof MIASReader)
        {
            try
            {
                MIASReader miasReader = (MIASReader) baseReader;
                String currentFile = miasReader.getCurrentFile();
                reader.close();
                miasReader.setAutomaticallyParseMasks(true);
                ServiceFactoryPrx sf = store.getServiceFactory();
                OverlayMetadataStore s = new OverlayMetadataStore();
                s.initialize(sf, pixelsList, plateIds);
                reader.setMetadataStore(s);
                miasReader.close();
                miasReader.setAutomaticallyParseMasks(true);
                miasReader.setId(currentFile);
                s.complete();
            }
            catch (ServerError e)
            {
                log.warn("Error while populating MIAS overlays.", e);
            }
            finally
            {
                reader.close();
                reader.setMetadataStore(store);
            }
        }
    }

    /**
     * Delete files from the managed repository.
     * @param container The current import container containing usedFiles to be
     * deleted.
     * @return List of files that could not be deleted.
     */
    public List<String> deleteFilesFromRepository(ImportContainer container)
            throws ServerError
    {
        checkManagedRepo();
        List<String> undeleted = repo.deleteFiles(container.getUsedFiles());
        return undeleted;
    }

    /**
     * Upload files to the managed repository.
     *
     * This is done by first passing in the possibly absolute local file paths.
     * A common selection of those are chosen and passed back to the client.
     *
     * @param container The current import container we're to handle.
     * @return Rewritten container after files have been copied to repository.
     */
    public Import uploadFilesToRepository(final ImportContainer container)
            throws ServerError
    {
        checkManagedRepo();
        String[] usedFiles = container.getUsedFiles();
        File target = container.getFile();
        if (log.isDebugEnabled()) {
            log.debug("Main file: " + target.getAbsolutePath());
            log.debug("Used files before:");
            for (String f : usedFiles) {
                log.debug(f);
            }
        }
        final byte[] buf = new byte[DEFAULT_ARRAYBUF_SIZE];  // 1 MB buffer
        final List<String> srcFiles = Arrays.asList(usedFiles);
        final int fileTotal = srcFiles.size();
        Import data = repo.prepareImport(srcFiles);

        notifyObservers(new ImportEvent.FILE_UPLOAD_STARTED(
                null, 0, fileTotal, null, null, null));

        for (int i = 0; i < fileTotal; i++) {
            File file = new File(srcFiles.get(i));
            long length = file.length();
            FileInputStream stream = null;
            RawFileStorePrx rawFileStore = null;
            try {
                stream = new FileInputStream(file);
                rawFileStore = repo.uploadUsedFile(data, data.usedFiles.get(i));
                int rlen = 0;
                long offset = 0;
                notifyObservers(new ImportEvent.FILE_UPLOAD_BYTES(
                        file.getAbsolutePath(), i, fileTotal, offset, length, null));
                while (stream.available() != 0) {
                    rlen = stream.read(buf);
                    rawFileStore.write(buf, offset, rlen);
                    offset += rlen;
                    notifyObservers(new ImportEvent.FILE_UPLOAD_BYTES(
                            file.getAbsolutePath(), i, fileTotal, offset, length, null));
                }
                OriginalFile ofile = repo.createOriginalFile(data.usedFiles.get(i));
                String absolutePath = repo.getAbsolutePath(data.usedFiles.get(i));
                data.originalFileMap.put(absolutePath, ofile);
                notifyObservers(new ImportEvent.FILE_UPLOAD_COMPLETE(
                        file.getAbsolutePath(), i, fileTotal, offset, length, null));
            }
            catch (Exception e) {
                notifyObservers(new ImportEvent.FILE_UPLOAD_ERROR(
                        file.getAbsolutePath(), i, fileTotal, null, null, e));
                log.error("I/O or server error uploading file.", e);
                break;
            }
            finally {
                cleanupUpload(rawFileStore, stream);
            }
        }
        notifyObservers(new ImportEvent.FILE_UPLOAD_FINISHED(
                null, fileTotal, fileTotal, null, null, null));

        usedFiles = data.usedFiles.toArray(new String[data.usedFiles.size()]);
        if (log.isDebugEnabled()) {
            log.debug("Used files after:");
            for (String f : usedFiles) {
                log.debug(f);
            }
        }
        container.setUsedFiles(usedFiles);
        container.setFile(new File(usedFiles[0]));
        return data;
    }

    private void cleanupUpload(RawFileStorePrx rawFileStore,
            FileInputStream stream) throws ServerError {
        try {
            if (rawFileStore != null) {
                rawFileStore.close();
            }
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                }
                catch (Exception e) {
                    log.error("I/O error closing stream.", e);
                }
            }
        }

    }

    /**
     * Perform an image import uploading files if necessary.
     * @param container The import container which houses all the configuration
     * values and target for the import.
     * @param index Index of the import in a set. <code>0</code> is safe if
     * this is a singular import.
     * @param numDone Number of imports completed in a set. <code>0</code> is
     * safe if this is a singular import.
     * @param total Total number of imports in a set. <code>1</code> is safe
     * if this is a singular import.
     * @return List of Pixels that have been imported.
     * @throws FormatException If there is a Bio-Formats image file format
     * error during import.
     * @throws IOException If there is an I/O error.
     * @throws ServerError If there is an error communicating with the OMERO
     * server we're importing into.
     * @since OMERO Beta 4.2.1.
     */
    public List<Pixels> importImage(ImportContainer container, int index,
                                    int numDone, int total)
            throws FormatException, IOException, Throwable
    {
        if(container.getMetadataOnly()) {
            throw new RuntimeException("Unsupported");
        }

        Import data = uploadFilesToRepository(container);
        RepositoryImportContainer repoIc = createRepositoryImportContainer(container);
        List<Pixels> pixList = repo.importMetadata(data, repoIc);
        notifyObservers(new ImportEvent.IMPORT_DONE(
                index, container.getFile().getAbsolutePath(),
                null, null, 0, null, pixList));
        return pixList;
    }

    /**
     * Perform an image import on already uploaded files. <em>Note: this method both
     *notifies {@link #observers} of error states AND throws the exception to cancel
     * processing.</em>
     * {@link #importCandidates(ImportConfig, ImportCandidates)}
     * uses {@link ImportConfig#contOnError} to act on these exceptions.
     * @param container The import container which houses all the configuration
     * values and target for the import.
     * @param index Index of the import in a set. <code>0</code> is safe if
     * this is a singular import.
     * @param numDone Number of imports completed in a set. <code>0</code> is
     * safe if this is a singular import.
     * @param total Total number of imports in a set. <code>1</code> is safe
     * if this is a singular import.
     * @return List of Pixels that have been imported.
     * @throws FormatException If there is a Bio-Formats image file format
     * error during import.
     * @throws IOException If there is an I/O error.
     * @throws ServerError If there is an error communicating with the OMERO
     * server we're importing into.
     * @since OMERO Beta 4.5.
     */
    public List<Pixels> importImageInternal(ImportContainer container, Import data, int index,
                                    int numDone, int total)
            throws FormatException, IOException, Throwable
    {
        File file = container.getFile();
        String fileName = file.getAbsolutePath();
        String shortName = file.getName();
        String format = null;
        String[] domains = null;
        String[] usedFiles = new String[1];
        boolean isScreeningDomain = false;

        IObject userSpecifiedTarget = container.getTarget();

        usedFiles[0] = file.getAbsolutePath();

        try {
            //TODO: must check that files exist in repository
            notifyObservers(new ImportEvent.LOADING_IMAGE(
                    shortName, index, numDone, total));

            open(file.getAbsolutePath());
            format = reader.getFormat();
            domains = reader.getDomains();
            if (reader.getUsedFiles() != null)
            {
                usedFiles = reader.getUsedFiles();
            }
            if (usedFiles == null) {
                throw new NullPointerException(
                        "usedFiles must be non-null");
            }
            for (String domain : domains)
            {
                if (domain.equals(FormatTools.HCS_DOMAIN))
                {
                    isScreeningDomain = true;
                    break;
                }
            }
            IFormatReader baseReader = reader.getImageReader().getReader();
            // Forcing these to false for now but remove completely once tested?
            boolean useMetadataFile = false;
            if (log.isInfoEnabled())
            {
                log.info("File format: " + format);
                log.info("Base reader: " + baseReader.getClass().getName());
                log.info("Metadata only import? " + isMetadataOnly);
                log.info("Big image? " + container.getBigImage());
            }
            notifyObservers(new ImportEvent.LOADED_IMAGE(
                    shortName, index, numDone, total));

            String formatString = baseReader.getClass().toString();
            formatString = formatString.replace("class loci.formats.in.", "");
            formatString = formatString.replace("Reader", "");

            // Save metadata and prepare the RawPixelsStore for our arrival.
            List<File> metadataFiles;
            if (isScreeningDomain)
            {
                log.info("Reader is of HCS domain, disabling metafile.");
                metadataFiles = store.setArchiveScreeningDomain(data);
            }
            else
            {
                log.info("Reader is not of HCS domain, use metafile: "
                        + useMetadataFile);
                metadataFiles = store.setArchive(useMetadataFile, data);
            }
            List<Pixels> pixList = importMetadata(index, container);
            List<Long> plateIds = new ArrayList<Long>();
            Image image = pixList.get(0).getImage();
            if (image.sizeOfWellSamples() > 0)
            {
                Plate plate =
                    image.copyWellSamples().get(0).getWell().getPlate();
                plateIds.add(plate.getId().getValue());
            }
            List<Long> pixelsIds = new ArrayList<Long>(pixList.size());
            for (Pixels pixels : pixList)
            {
                pixelsIds.add(pixels.getId().getValue());
            }
            boolean saveSha1 = false;
            // Parse the binary data to generate min/max values
            int seriesCount = reader.getSeriesCount();
            for (int series = 0; series < seriesCount; series++) {
                ImportSize size = new ImportSize(fileName,
                        pixList.get(series), reader.getDimensionOrder());
                Pixels pixels = pixList.get(series);
                long pixId = pixels.getId().getValue();
                MessageDigest md = parseData(fileName, series, size);
                if (md != null) {
                    String s = OMEROMetadataStoreClient.byteArrayToHexString(
                            md.digest());
                    pixels.setSha1(store.toRType(s));
                    saveSha1 = true;
                }
            }
            // Original file absolute path to original file map for uploading
            Map<String, OriginalFile> originalFileMap =
                new HashMap<String, OriginalFile>();
            for (Pixels pixels : pixList)
            {
                Image i = pixels.getImage();
                for (Annotation annotation : i.linkedAnnotationList())
                {
                    if (annotation instanceof FileAnnotation)
                    {
                        FileAnnotation fa = (FileAnnotation) annotation;
                        OriginalFile of = fa.getFile();
                        String fullPath =
                            of.getPath().getValue() + of.getName().getValue();
                        originalFileMap.put(fullPath, of);
                    }
                }
                for (OriginalFile of : pixels.linkedOriginalFileList())
                {
                    String fullPath =
                        of.getPath().getValue() + of.getName().getValue();
                    originalFileMap.put(fullPath, of);
                }
                for (WellSample ws : i.copyWellSamples())
                {
                    Plate plate = ws.getWell().getPlate();
                    for (Annotation annotation : plate.linkedAnnotationList())
                    {
                        if (annotation instanceof FileAnnotation)
                        {
                            FileAnnotation fa = (FileAnnotation) annotation;
                            OriginalFile of = fa.getFile();
                            String fullPath =
                                of.getPath().getValue() + of.getName().getValue();
                            originalFileMap.put(fullPath, of);
                        }
                    }
                }
            }

            // As we're in metadata only mode  on we need to
            // tell the server which Pixels set matches up to which series.
            String targetName = container.getFile().getAbsolutePath();
            int series = 0;
            for (Long pixelsId : pixelsIds)
            {
                store.setPixelsParams(pixelsId, series, targetName);
                series++;
            }

            if (saveSha1)
            {
                store.updatePixels(pixList);
            }

            if (reader.isMinMaxSet() == false)
            {
                store.populateMinMax();
            }

            notifyObservers(new ImportEvent.IMPORT_OVERLAYS(
                    index, null, userSpecifiedTarget, null, 0, null));
            importOverlays(pixList, plateIds);

            notifyObservers(new ImportEvent.IMPORT_PROCESSING(
                    index, null, userSpecifiedTarget, null, 0, null));
            if (container.getDoThumbnails())
            {
                store.resetDefaultsAndGenerateThumbnails(plateIds, pixelsIds);
            }
            else
            {
                log.warn("Not creating thumbnails at user request!");
            }

            store.launchProcessing(); // Use or return value here later. TODO

            return pixList;

        } catch (MissingLibraryException mle) {
            notifyObservers(new ErrorHandler.MISSING_LIBRARY(
                    fileName, mle, usedFiles, format));
            throw mle;
        } catch (IOException io) {
            notifyObservers(new ErrorHandler.FILE_EXCEPTION(
                    fileName, io, usedFiles, format));
            throw io;
        } catch (UnsupportedCompressionException uce) {
            // Handling as UNKNOWN_FORMAT for 4.3.0
            notifyObservers(new ErrorHandler.UNKNOWN_FORMAT(
                    fileName, uce, this));
            throw uce;
        } catch (UnknownFormatException ufe) {
            notifyObservers(new ErrorHandler.UNKNOWN_FORMAT(
                    fileName, ufe, this));
            throw ufe;
        } catch (FormatException fe) {
            notifyObservers(new ErrorHandler.FILE_EXCEPTION(
                    fileName, fe, usedFiles, format));
            throw fe;
        } catch (NullPointerException npe) {
            notifyObservers(new ErrorHandler.INTERNAL_EXCEPTION(
                    fileName, npe, usedFiles, format));
            throw npe;
        } catch (Exception e) {
            notifyObservers(new ErrorHandler.INTERNAL_EXCEPTION(
                    fileName, e, usedFiles, format));
            throw e;
        } catch (Throwable t) {
            notifyObservers(new ErrorHandler.INTERNAL_EXCEPTION(
                    fileName, new RuntimeException(t), usedFiles, format));
            throw t;
        } finally {
            store.setGroup(null);
            store.createRoot(); // CLEAR MetadataStore
        }
    }

    // ~ Helpers
    // =========================================================================

    /**
     * Retrieves the first managed repository from the list of current active
     * repositories.
     * @return Active proxy for the legacy repository.
     */
    private ManagedRepositoryPrx lookupManagedRepository()
    {
        try
        {
            ManagedRepositoryPrx rv = null;
            ServiceFactoryPrx sf = store.getServiceFactory();
            RepositoryMap map = sf.sharedResources().repositories();
            for (int i = 0; i < map.proxies.size(); i++)
            {
                RepositoryPrx proxy = map.proxies.get(i);
                if (proxy != null) {
                    rv = ManagedRepositoryPrxHelper.checkedCast(proxy);
                    if (rv != null) {
                        return rv;
                    }
                }
            }
            return null;
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    private void checkManagedRepo() {
        if (repo == null) {
            throw new RuntimeException("No FS! Cannot proceed");
        }
    }

    /**
     * Parse the binary data to generate min/max values and 
     * allow an md to be calculated.
     *
     * @param series
     * @return The SHA1 message digest for the binary data.
     */
    public MessageDigest parseData(String fileName, int series,
                                        ImportSize size)
        throws FormatException, IOException, ServerError
    {
        reader.setSeries(series);
        int maxPlaneSize = maxPlaneWidth * maxPlaneHeight;
        if (((long) reader.getSizeX()
             * (long) reader.getSizeY()) > maxPlaneSize) {
            int pixelType = reader.getPixelType();
            long[] minMax = FormatTools.defaultMinMax(pixelType);
            for (int c = 0; c < reader.getSizeC(); c++) {
                store.setChannelGlobalMinMax(
                        c, minMax[0], minMax[1], series);
            }
            return null;
        }
        int bytesPerPixel = getBytesPerPixel(reader.getPixelType());
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(
                "Required SHA-1 message digest algorithm unavailable.");
        }
        int planeNo = 1;
        for (int t = 0; t < size.sizeT; t++) {
            for (int c = 0; c < size.sizeC; c++) {
                for (int z = 0; z < size.sizeZ; z++) {
                    parseDataByPlane(size, z, c, t,
                            bytesPerPixel, fileName, md);
                    notifyObservers(new ImportEvent.IMPORT_STEP(
                            planeNo, series, reader.getSeriesCount()));
                    planeNo++;
                }
            }
        }
        return md;
    }

    /**
     * Read a plane to cause min/max valus to be calculated.
     *
     * @param size Sizes of the Pixels set.
     * @param z The Z-section offset to write to.
     * @param c The channel offset to write to.
     * @param t The timepoint offset to write to.
     * @param bytesPerPixel Number of bytes per pixel.
     * @param fileName Name of the file.
     * @param md Current Pixels set message digest.
     * @throws FormatException If there is an error reading Pixel data via
     * Bio-Formats.
     * @throws IOException If there is an I/O error reading Pixel data via
     * Bio-Formats.
     */
    private void parseDataByPlane(ImportSize size, int z, int c, int t,
                                      int bytesPerPixel, String fileName,
                                      MessageDigest md)
        throws FormatException, IOException, ServerError
    {
        int tileHeight = reader.getOptimalTileHeight();
        int tileWidth = reader.getOptimalTileWidth();
        int planeNumber, x, y, w, h;
        for (int tileOffsetY = 0;
             tileOffsetY < (size.sizeY + tileHeight - 1) / tileHeight;
             tileOffsetY++)
        {
            for (int tileOffsetX = 0;
                 tileOffsetX < (size.sizeX + tileWidth - 1) / tileWidth;
                 tileOffsetX++)
            {
                x = tileOffsetX * tileWidth;
                y = tileOffsetY * tileHeight;
                w = tileWidth;
                h = tileHeight;
                if ((x + tileWidth) > size.sizeX)
                {
                    w = size.sizeX - x;
                }
                if ((y + tileHeight) > size.sizeY)
                {
                    h = size.sizeY - y;
                }
                int bytesToRead = w * h * bytesPerPixel;
                if (arrayBuf.length != bytesToRead)
                {
                    arrayBuf = new byte[bytesToRead];
                }
                planeNumber = reader.getIndex(z, c, t);
                if (log.isDebugEnabled())
                {
                    log.debug(String.format(
                            "Plane:%d X:%d Y:%d TileWidth:%d TileHeight:%d " +
                            "arrayBuf.length:%d", planeNumber, x, y, w, h,
                            arrayBuf.length));
                }
                arrayBuf = reader.openBytes(
                        planeNumber, arrayBuf, x, y, w, h);
                try {
                    md.update(arrayBuf);
                }
                catch (Exception e) {
                    // This better not happen. :)
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Examines a byte array to see if it needs to be byte swapped and modifies
     * the byte array directly.
     * @param byteArray The byte array to check and modify if required.
     * @return the <i>byteArray</i> either swapped or not for convenience.
     * @throws IOException if there is an error read from the file.
     * @throws FormatException if there is an error during metadata parsing.
     */
    private byte[] swapIfRequired(ByteBuffer buffer, String fileName)
        throws FormatException, IOException
    {
        int pixelType = reader.getPixelType();
        boolean isLittleEndian = reader.isLittleEndian();
        int bytesPerPixel = getBytesPerPixel(pixelType);

        // We've got nothing to do if the samples are only 8-bits wide.
        if (bytesPerPixel == 1)
            return buffer.array();

        int length;
        if (isLittleEndian) {
            if (bytesPerPixel == 2) { // short/ushort
                ShortBuffer buf = buffer.asShortBuffer();
                length = buffer.limit() / 2;
                for (int i = 0; i < length; i++) {
                    buf.put(i, DataTools.swap(buf.get(i)));
                }
            } else if (bytesPerPixel == 4) { // int/uint/float
                IntBuffer buf = buffer.asIntBuffer();
                length = buffer.limit() / 4;
                for (int i = 0; i < length; i++) {
                    buf.put(i, DataTools.swap(buf.get(i)));
                }
            } else if (bytesPerPixel == 8) // long/double
            {
                LongBuffer buf = buffer.asLongBuffer();
                length = buffer.limit() / 8;
                for (int i = 0; i < length ; i++) {
                    buf.put(i, DataTools.swap(buf.get(i)));
                }
            } else {
                throw new FormatException(String.format(
                        "Unsupported sample bit width: %d", bytesPerPixel));
            }
        }
        // We've got a big-endian file with a big-endian byte array.
        return buffer.array();
    }


    /**
     * Retrieves how many bytes per pixel the current plane or section has.
     * @return the number of bytes per pixel.
     */
    private int getBytesPerPixel(int type)
    {
        switch(type) {
            case 0:
            case 1:
                return 1;  // INT8 or UINT8
            case 2:
            case 3:
                return 2;  // INT16 or UINT16
            case 4:
            case 5:
            case 6:
                return 4;  // INT32, UINT32 or FLOAT
            case 7:
                return 8;  // DOUBLE
        }
        throw new RuntimeException("Unknown type with id: '" + type + "'");
    }

    public void clear()
    {
        store.setGroup(null);
        store.createRoot();
    }

}
