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
import omero.api.ServiceFactoryPrx;
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
 * ImportHandler to support ImportFixture The general workflow
 * for this class (as seen in {@link ImportFixture} is: <code>
 *   ImportLibrary library = new ImportLibrary(store,reader,files);
 *   for (File file : files) {
 *     String fileName = file.getAbsolutePath();
 *     library.open(fileName);
 *     int count = library.calculateImageCount(fileName);
 *     long pixId = library.importMetadata();
 *     library.importData(pixId, fileName, new ImportLibrary.Step(){
 *       public void step(int i) {}});
 *   }
 * </code>
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

    /** Default arraybuf size for planar data transfer. (1MB) */
    public static final int DEFAULT_ARRAYBUF_SIZE = 1048576;

    private final ArrayList<IObserver> observers = new ArrayList<IObserver>();

    private final OMEROMetadataStoreClient store;

    private final OMEROWrapper reader;

    private byte[] arrayBuf = new byte[DEFAULT_ARRAYBUF_SIZE];

    /** Whether or not to import as metadata only. */
    private boolean isMetadataOnly = false;

    /** List of readers for which we have FS lite enabled. */
    private final Set<String> fsLiteReaders;

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
        String fsLiteReadersString =
            store.getConfigValue("omero.pixeldata.fs_lite_readers");
        if (fsLiteReadersString == null || fsLiteReadersString.length() == 0)
        {
            fsLiteReaders = new HashSet<String>();
            log.warn("Pre 4.3.2 server or empty " +
                     "omero.pixeldata.fs_lite_readers, using hard coded " +
                     "readers!");
            fsLiteReaders.add(loci.formats.in.SVSReader.class.getName());
            fsLiteReaders.add(loci.formats.in.APNGReader.class.getName());
            fsLiteReaders.add(loci.formats.in.JPEG2000Reader.class.getName());
            fsLiteReaders.add(loci.formats.in.JPEGReader.class.getName());
            fsLiteReaders.add(loci.formats.in.TiffDelegateReader.class.getName());
        }
        else
        {
            fsLiteReaders = new HashSet<String>(Arrays.asList(
                    fsLiteReadersString.split(",")));
        }
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
            log.debug("FS lite enabled readers: " +
                    Arrays.toString(fsLiteReaders.toArray(
                            new String[fsLiteReaders.size()])));
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
     */
    public boolean importCandidates(ImportConfig config, ImportCandidates candidates)
    {
        List<ImportContainer> containers = candidates.getContainers();
        if (containers != null) {
            int numDone = 0;
            for (int index = 0; index < containers.size(); index++) {
                ImportContainer ic = containers.get(index);
                if (config.targetClass.get() == "omero.model.Dataset")
                {
                    ic.setTarget(store.getTarget(
                            Dataset.class, config.targetId.get()));
                }
                else if (config.targetClass.get() == "omero.model.Screen")
                {
                    ic.setTarget(store.getTarget(
                            Screen.class, config.targetId.get()));
                }

                try {
                    importImage(ic, index, numDone, containers.size());
                    numDone++;
                } catch (Throwable t) {
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
     * Perform various specific operations on big image formats.
     * @param reader The base reader currently being used.
     * @param container The current import container we're to handle.
     */
    private void handleBigImageFormats(IFormatReader reader,
                                       ImportContainer container)
    {
        String readerName = reader.getClass().getName();
        if (fsLiteReaders.contains(readerName))
        {
            if (reader.getClass().equals(loci.formats.in.TiffDelegateReader.class)
                || reader.getClass().equals(loci.formats.in.APNGReader.class)
                || reader.getClass().equals(loci.formats.in.JPEGReader.class))
            {
                log.debug("Using TIFF/PNG/JPEG reader FS lite handling.");
                List<Pixels> pixelsList = store.getSourceObjects(Pixels.class);
                int maxPlaneSize = maxPlaneWidth * maxPlaneHeight;
                boolean doBigImage = false;
                for (Pixels pixels : pixelsList)
                {
                    if ((pixels.getSizeX().getValue()
                         * pixels.getSizeY().getValue()) > maxPlaneSize)
                    {
                        doBigImage = true;
                        log.debug("Image meets big image size criteria.");
                        break;
                    }
                }
                if (!doBigImage)
                {
                    log.debug("Image does not meet big image size criteria.");
                    return;
                }
            }
            log.info("Big image, enabling metadata only and archiving.");
            container.setMetadataOnly(true);
            container.setArchive(true);
        }
        else
        {
            log.debug("FS lite disabled for: " + readerName);
        }
    }

    /**
     * Perform an image import.  <em>Note: this method both notifies
     * {@link #observers} of error states AND throws the exception to cancel
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
     * @since OMERO Beta 4.2.1.
     */
    public List<Pixels> importImage(ImportContainer container, int index,
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
            notifyObservers(new ImportEvent.LOADING_IMAGE(
                    shortName, index, numDone, total));

            open(file.getAbsolutePath());
            format = reader.getFormat();
            domains = reader.getDomains();
            if (reader.getUsedFiles() != null)
            {
                usedFiles = reader.getUsedFiles();
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
            handleBigImageFormats(baseReader, container);
            // Setting these two variables here because handleBigImageFormats()
            // above may modify the container.
            boolean archive = container.getArchive();
            boolean useMetadataFile = container.getUseMetadataFile();
            if (log.isInfoEnabled())
            {
                log.info("File format: " + format);
                log.info("Base reader: " + baseReader.getClass().getName());
                log.info("Metadata only import? " + isMetadataOnly);
                log.info("Archiving enabled? " + archive);
                log.info("Container metadata only import? " +
                         container.getMetadataOnly());
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

                metadataFiles = store.setArchiveScreeningDomain(archive);
            }
            else
            {
                log.info("Reader is not of HCS domain, use metafile: "
                        + useMetadataFile);
                metadataFiles = store.setArchive(archive, useMetadataFile);
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
            // If we're metadata only, we don't want to perform any pixel I/O.
            if (!isMetadataOnly && !container.getMetadataOnly())
            {
                boolean success = false;
                try
                {
                    store.preparePixelsStore(pixelsIds);
                    int seriesCount = reader.getSeriesCount();
                    for (int series = 0; series < seriesCount; series++)
                    {
                        // Calculate the dimensions for import this single file.
                        ImportSize size = new ImportSize(fileName,
                                pixList.get(series), reader.getDimensionOrder());

                        Pixels pixels = pixList.get(series);
                        long pixId = pixels.getId().getValue();

                        notifyObservers(new ImportEvent.DATASET_STORED(
                                index, fileName, userSpecifiedTarget, pixId,
                                series, size, numDone, total));

                        MessageDigest md = importData(
                                pixId, fileName, series, size);
                        if (md != null)
                        {
                            String s = OMEROMetadataStoreClient.byteArrayToHexString(
                                    md.digest());
                            pixels.setSha1(store.toRType(s));
                            saveSha1 = true;
                        }

                        notifyObservers(new ImportEvent.DATA_STORED(
                                index, fileName, userSpecifiedTarget, pixId,
                                series, size));
                    }
                    success = true;
                }
                finally
                {
                    try
                    {
                        store.finalizePixelStore();
                    } catch (Throwable t) {
                        // ticket:5594
                        if (success) {
                            // We thought we were successful, so
                            // this exception can propagate outward
                            // cancelling the import.
                            throw t;
                        } else {
                            // We were not successful which could
                            // only happen if an exception was thrown,
                            // therefore log this new exception, so
                            // the first exception can propagate.
                            log.error("Close exception hidden by previous exception", t);
                        }

                    }
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

            List<File> fileNameList = new ArrayList<File>();
            if (archive)
            {
                for (String filename : reader.getUsedFiles())
                {
                    fileNameList.add(new File(filename));
                }
            }
            else
            {
                for (String filename : store.getFilteredCompanionFiles())
                {
                    fileNameList.add(new File(filename));
                }
            }

            fileNameList.addAll(metadataFiles);
            if (fileNameList.size() != originalFileMap.size())
            {
                log.warn(String.format("Original file number mismatch, %d!=%d.",
                        fileNameList.size(), originalFileMap.size()));
            }

            if (archive)
            {
                notifyObservers(new ImportEvent.IMPORT_ARCHIVING(
                        index, null, userSpecifiedTarget, null, 0, null));
            }
            store.writeFilesToFileStore(fileNameList, originalFileMap);
            // If we're in metadata only mode and archiving is on we need to
            // tell the server which Pixels set matches up to which series.
            if ((isMetadataOnly || container.getMetadataOnly()) && archive)
            {
                int series = 0;
                for (Long pixelsId : pixelsIds)
                {
                    store.setPixelsParams(pixelsId, series);
                    series++;
                }
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
            notifyObservers(new ImportEvent.IMPORT_DONE(
                    index, null, userSpecifiedTarget, null, 0, null, pixList));

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

    /**
     * saves the binary data to the server. After each successful save,
     * an {@link ImportEvent.IMPORT_STEP} is raised with the number of the
     * iteration just completed.
     * @param series
     * @return The SHA1 message digest for the Pixels saved.
     */
    public MessageDigest importData(Long pixId, String fileName,
                                    int series, ImportSize size)
        throws FormatException, IOException, ServerError
    {
        reader.setSeries(series);
        int bytesPerPixel = getBytesPerPixel(reader.getPixelType());

        MessageDigest md;
        try
        {
            md = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(
                "Required SHA-1 message digest algorithm unavailable.");
        }

        int planeNo = 1;
        int[] tileSize = store.getTileSize(pixId);
        if (log.isDebugEnabled())
        {
            log.debug("Server tile size: " + Arrays.toString(tileSize));
        }
        for (int t = 0; t < size.sizeT; t++)
        {
            for (int c = 0; c < size.sizeC; c++)
            {
                for (int z = 0; z < size.sizeZ; z++)
                {
                    writeDataTileBased(
                            pixId, size, z, c, t, tileSize[0], tileSize[1],
                            bytesPerPixel, fileName, md);
                    notifyObservers(new ImportEvent.IMPORT_STEP(
                            planeNo, series, reader.getSeriesCount()));
                    planeNo++;
                }
            }
        }
        return md;
    }

    // ~ Helpers
    // =========================================================================

    /**
     * Writes data to the server for a given plane in a tile based manner.
     * @param pixId Pixels ID to write to.
     * @param size Sizes of the Pixels set.
     * @param z The Z-section offset to write to.
     * @param c The channel offset to write to.
     * @param t The timepoint offset to write to.
     * @param tileWidth Width of the tiles to write.
     * @param tileHeight Height of the tiles to write.
     * @param bytesPerPixel Number of bytes per pixel.
     * @param fileName Name of the file.
     * @param md Current Pixels set message digest.
     * @return The new offset to use for the next plane.
     * @throws FormatException If there is an error reading Pixel data via
     * Bio-Formats.
     * @throws IOException If there is an I/O error reading Pixel data via
     * Bio-Formats.
     * @throws ServerError If there is an error writing the data to the
     * OMERO.server instance.
     */
    private void writeDataTileBased(long pixId, ImportSize size,
                                    int z, int c, int t, int tileWidth,
                                    int tileHeight, int bytesPerPixel,
                                    String fileName, MessageDigest md)
        throws FormatException, IOException, ServerError
    {
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
                ByteBuffer buf = ByteBuffer.wrap(arrayBuf);
                arrayBuf = swapIfRequired(buf, fileName);
                try
                {
                    md.update(arrayBuf, 0, arrayBuf.length);
                }
                catch (Exception e)
                {
                    // This better not happen. :)
                    throw new RuntimeException(e);
                }
                store.setTile(
                        pixId, arrayBuf, z, c, t, x, y, w, h);
            }
        }
    }

    /**
     * Writes data to the server for a given plane in a <i>full plane</i>
     * manner.
     * @param pixId Pixels ID to write to.
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
     * @throws ServerError If there is an error writing the data to the
     * OMERO.server instance.
     */
    private void writeDataPlanarBased(long pixId, ImportSize size,
                                      int z, int c, int t,
                                      int bytesPerPixel, String fileName,
                                      MessageDigest md)
        throws FormatException, IOException, ServerError
    {
        int bytesToRead = size.sizeX * size.sizeY * bytesPerPixel;
        if (arrayBuf.length != bytesToRead)
        {
            arrayBuf = new byte[bytesToRead];
        }
        int planeNumber = reader.getIndex(z, c, t);
        PixelData data = reader.openPlane2D(fileName, planeNumber, arrayBuf);
        ByteBuffer buf = data.getData();
        arrayBuf = swapIfRequired(buf, fileName);
        try
        {
            md.update(arrayBuf);
        }
        catch (Exception e)
        {
            // This better not happen. :)
            throw new RuntimeException(e);
        }
        store.setPlane(pixId, arrayBuf, z, c, t);
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
