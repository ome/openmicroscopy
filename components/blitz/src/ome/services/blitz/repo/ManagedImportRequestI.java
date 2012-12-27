/*
 * Copyright (C) 2012 Glencoe Software, Inc. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package ome.services.blitz.repo;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import loci.common.DataTools;
import loci.formats.FormatException;
import loci.formats.FormatReader;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.MissingLibraryException;
import loci.formats.UnknownFormatException;
import loci.formats.UnsupportedCompressionException;
import loci.formats.in.MIASReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.OverlayMetadataStore;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.ImportSize;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.util.ErrorHandler;
import ome.io.nio.TileSizes;
import ome.services.blitz.fire.Registry;
import ome.util.Utils;

import omero.ServerError;
import omero.api.ServiceFactoryPrx;
import omero.cmd.ERR;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.OK;
import omero.cmd.Response;
import omero.grid.ImportLocation;
import omero.grid.ImportRequest;
import omero.grid.ImportResponse;
import omero.grid.ImportSettings;
import omero.model.Annotation;
import omero.model.FilesetJobLink;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Job;
import omero.model.MetadataImportJob;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.ThumbnailGenerationJob;

/**
 * Wrapper around {@link FilesetJobLink} instances which need to be handled
 * on the server-side. This will primarily provide the step-location required
 * by {@link omero.cmd.Handle} by calling back to the
 * {@link ManagedImportProcessI} object.
 *
 * @author Josh Moore, josh at glencoesoftware.com
 * @since 4.5.0
 */
public class ManagedImportRequestI extends ImportRequest implements IRequest {

    private static final long serialVersionUID = -303948503984L;

    private static Log log = LogFactory.getLog(ManagedImportRequestI.class);

    /**
     * Helper instance for this class. Will create a number of sub-helper
     * instances for each request.
     */
    private Helper helper;

    private byte[] arrayBuf = new byte[omero.constants.MESSAGESIZEMAX.value/8]; // 8 MB buffer

    private final Registry reg;

    private final TileSizes sizes;

    public ManagedImportRequestI(Registry reg, TileSizes sizes) {
        this.reg = reg;
        this.sizes = sizes;

    }

    //
    // IRequest methods
    //

    public Map<String, String> getCallContext() {
        return null;
    }

    public void init(Helper helper) {
        this.helper = helper;
        helper.setSteps(1);
    }

    public Object step(int step) {
        helper.assertStep(step);
        try {
            Job j = activity.getChild();
            if (j == null) {
                throw helper.cancel(new ERR(), null, "null-job");
            } else if (j instanceof MetadataImportJob) {
                return importMetadata();
            } else if (j instanceof ThumbnailGenerationJob) {
                throw helper.cancel(new ERR(), null, "NYI");
            } else {
                throw helper.cancel(new ERR(), null, "unknown-job-type",
                        "job-type", j.ice_id());
            }
        } catch (Throwable t) {
            throw helper.cancel(new ERR(), t, "import-request-failure");
        }
    }

    @SuppressWarnings("unchecked")
    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        if (object instanceof List) {
            helper.setResponseIfNull(new ImportResponse((List<Pixels>) object));
        } else {
            helper.setResponseIfNull(new OK());
        }
    }

    public Response getResponse() {
        return helper.getResponse();
    }

    //
    // ACTIONS
    //


    /** Now an internal, trusted method */
    public List<Pixels> importMetadata() throws Throwable {

        ServiceFactoryPrx sf = null;
        OMEROMetadataStoreClient store = null;
        OMEROWrapper reader = null;
        List<Pixels> pix = null;
        try {
            final ImportConfig config = new ImportConfig();
            final String sessionUuid = helper.getEventContext().getCurrentSessionUuid();
            final String clientUuid = UUID.randomUUID().toString();

            sf = reg.getInternalServiceFactory(
                    sessionUuid, "unused", 3, 1, clientUuid);
            reader = new OMEROWrapper(config);
            store = new OMEROMetadataStoreClient();
            store.initialize(sf);

            if (!(location instanceof ManagedImportLocationI)) {
                throw new RuntimeException("Bad location type: " +
                        location.getClass().getName());
            }
            File file = ((ManagedImportLocationI) location).getTarget();
            pix = importImageInternal(store, reader, settings, 0, 0, 1, file);

        }
        finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (Throwable e){
                log.error(e);
            }
            try {
                if (store != null) {
                    store.logout();
                }
            } catch (Throwable e) {
                log.error(e);
            }
            try {
                if (sf != null) {
                    sf.destroy();
                }
            } catch (Throwable e) {
                log.error(e);
            }
        }
        return pix;
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
    public List<Pixels> importImageInternal(
            OMEROMetadataStoreClient store, OMEROWrapper reader,
            ImportSettings data, int index,
            int numDone, int total,
            final File file)
            throws FormatException, IOException, Throwable
    {

        final IObject userSpecifiedTarget = data.userSpecifiedTarget;
        final String userSpecifiedName = data.userSpecifiedName == null ? null :
            data.userSpecifiedName.getValue();
        final String userSpecifiedDescription = data.userSpecifiedDescription == null ? null :
            data.userSpecifiedDescription.getValue();
        final double[] userPixels = data.userSpecifiedPixels;
        final List<Annotation> annotationList = data.userSpecifiedAnnotationList;
        final boolean doThumbnails = data.doThumbnails == null ? true :
            data.doThumbnails.getValue();

        String fileName = file.getAbsolutePath();
        String shortName = file.getName();
        String format = null;
        String[] usedFiles = new String[1];

        usedFiles[0] = file.getAbsolutePath();

        try {
            //TODO: must check that files exist in repository
            notifyObservers(new ImportEvent.LOADING_IMAGE(
                    shortName, index, numDone, total));

            open(reader, store, file.getAbsolutePath());
            format = reader.getFormat();
            if (reader.getUsedFiles() != null)
            {
                usedFiles = reader.getUsedFiles();
            }
            if (usedFiles == null) {
                throw new NullPointerException(
                        "usedFiles must be non-null");
            }

            IFormatReader baseReader = reader.getImageReader().getReader();
            if (log.isInfoEnabled())
            {
                log.info("File format: " + format);
                log.info("Base reader: " + baseReader.getClass().getName());
            }
            notifyObservers(new ImportEvent.LOADED_IMAGE(
                    shortName, index, numDone, total));

            String formatString = baseReader.getClass().toString();
            formatString = formatString.replace("class loci.formats.in.", "");
            formatString = formatString.replace("Reader", "");

            List<Pixels> pixList = importMetadata(store, index, userSpecifiedTarget,
                    userSpecifiedName, userSpecifiedDescription, userPixels,
                    annotationList);

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
                MessageDigest md = parseData(reader, store, fileName, series, size);
                if (md != null) {
                    String s = Utils.bytesToHex(md.digest());
                    pixels.setSha1(store.toRType(s));
                    saveSha1 = true;
                }
            }

            // As we're in metadata only mode  on we need to
            // tell the server which Pixels set matches up to which series.
            String targetName = file.getAbsolutePath();
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
            importOverlays(reader, store, pixList, plateIds);

            notifyObservers(new ImportEvent.IMPORT_PROCESSING(
                    index, null, userSpecifiedTarget, null, 0, null));
            if (doThumbnails)
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


    /** opens the file using the {@link FormatReader} instance */
    private void open(OMEROWrapper reader, OMEROMetadataStoreClient store,
            String fileName) throws IOException, FormatException
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
     * Parse the binary data to generate min/max values and
     * allow an md to be calculated.
     *
     * @param series
     * @return The SHA1 message digest for the binary data.
     */
    public MessageDigest parseData(
            OMEROWrapper reader,
            OMEROMetadataStoreClient store,
            String fileName, int series,
            ImportSize size)
        throws FormatException, IOException, ServerError
    {
        reader.setSeries(series);
        int maxPlaneSize = sizes.getMaxPlaneWidth() * sizes.getMaxPlaneHeight();
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
                    parseDataByPlane(reader, size, z, c, t,
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
    private void parseDataByPlane(
            OMEROWrapper reader,
            ImportSize size, int z, int c, int t,
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
            OMEROMetadataStoreClient store,
            final int index,
            final IObject target, final String userSpecifiedName,
            final String userSpecifiedDescription,
            final double[] userPixels,
            final List<Annotation> annotationList)
            throws FormatException, IOException
    {
        // 1st we post-process the metadata that we've been given.
        notifyObservers(new ImportEvent.BEGIN_POST_PROCESS(
                index, null, target, null, 0, null));
        store.setUserSpecifiedName(userSpecifiedName);
        store.setUserSpecifiedDescription(userSpecifiedDescription);
        if (userPixels != null && userPixels.length >= 3)
            // The array could be empty due to Ice-non-null semantics.
            store.setUserSpecifiedPhysicalPixelSizes(
                    userPixels[0], userPixels[1], userPixels[2]);
        store.setUserSpecifiedTarget(target);
        store.setUserSpecifiedAnnotations(annotationList);
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
    private void importOverlays(OMEROWrapper reader,
            OMEROMetadataStoreClient store,
            List<Pixels> pixelsList, List<Long> plateIds)
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

    private void notifyObservers(Object...args) {
        // TEMPORARY REPLACEMENT. FIXME
    }
}
