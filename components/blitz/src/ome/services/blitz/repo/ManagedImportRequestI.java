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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.ImportSize;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.util.ErrorHandler;
import ome.io.nio.TileSizes;
import ome.services.blitz.fire.Registry;
import ome.util.Utils;

import omero.ServerError;
import omero.api.ServiceFactoryPrx;
import omero.cmd.ERR;
import omero.cmd.HandleI.Cancel;
import omero.cmd.Helper;
import omero.cmd.IRequest;
import omero.cmd.OK;
import omero.cmd.Response;
import omero.grid.ImportRequest;
import omero.grid.ImportResponse;
import omero.model.Annotation;
import omero.model.FilesetJobLink;
import omero.model.IObject;
import omero.model.Image;
import omero.model.IndexingJob;
import omero.model.Job;
import omero.model.MetadataImportJob;
import omero.model.PixelDataJob;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.ScriptJob;
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

    private byte[] arrayBuf = new byte[omero.constants.DEFAULTBLOCKSIZE.value];  // not yet server-configurable

    private final Registry reg;

    private final TileSizes sizes;

    //
    // Import items. Initialized in init(Helper)
    //

    private ServiceFactoryPrx sf = null;

    private OMEROMetadataStoreClient store = null;

    private OMEROWrapper reader = null;

    private File file = null;

    private IObject userSpecifiedTarget = null; // TODO: remove?

    private String userSpecifiedName = null;

    private String userSpecifiedDescription = null;

    private double[] userPixels = null;

    private List<Annotation> annotationList = null;

    private boolean doThumbnails = true;

    private String fileName = null;

    private String shortName = null;

    private String format = null;

    private String formatString = null;

    private String[] usedFiles = null;

    private Map<String, List<IObject>> objects;

    private List<Pixels> pixList;

    private List<Image> imageList;

    private List<Plate> plateList;

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
        helper.setSteps(5);

        final ImportConfig config = new ImportConfig();
        final String sessionUuid = helper.getEventContext().getCurrentSessionUuid();
        final String clientUuid = UUID.randomUUID().toString();

        if (!(location instanceof ManagedImportLocationI)) {
            throw helper.cancel(new ERR(), null, "bad-location",
                    "location-type", location.getClass().getName());
        }

        file = ((ManagedImportLocationI) location).getTarget();

        try {
            sf = reg.getInternalServiceFactory(
                    sessionUuid, "unused", 3, 1, clientUuid);
            reader = new OMEROWrapper(config);
            store = new OMEROMetadataStoreClient();
            store.initialize(sf);

            userSpecifiedTarget = settings.userSpecifiedTarget;
            userSpecifiedName = settings.userSpecifiedName == null ? null :
                settings.userSpecifiedName.getValue();
            userSpecifiedDescription = settings.userSpecifiedDescription == null ? null :
                settings.userSpecifiedDescription.getValue();
            userPixels = settings.userSpecifiedPixels;
            annotationList = settings.userSpecifiedAnnotationList;
            doThumbnails = settings.doThumbnails == null ? true :
                settings.doThumbnails.getValue();

            fileName = file.getAbsolutePath();
            shortName = file.getName();
            format = null;
            usedFiles = new String[1];
            usedFiles[0] = file.getAbsolutePath();

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
                    shortName, 0, 0, 0));

            formatString = baseReader.getClass().getSimpleName();
            formatString = formatString.replace("Reader", "");

        } catch (Cancel c) {
            throw c;
        } catch (Throwable t) {
            throw helper.cancel(new ERR(), t, "error-on-init");
        }


    }

    /**
     * Called during {@link #getResponse()}.
     */
    private void cleanup() {
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

    public Object step(int step) {
        helper.assertStep(step);
        try {
            Job j = activity.getChild();
            if (j == null) {
                throw helper.cancel(new ERR(), null, "null-job");
            } else if (!(j instanceof MetadataImportJob)) {
                throw helper.cancel(new ERR(), null, "unexpected-job-type",
                        "job-type", j.ice_id());
            }

            if (step == 0) {
                return importMetadata((MetadataImportJob) j);
            } else if (step == 1) {
                return pixelData(null);//(ThumbnailGenerationJob) j);
            } else if (step == 2) {
                return generateThumbnails(null);//(PixelDataJob) j); Nulls image
            } else if (step == 3) {
                // TODO: indexing and scripting here as well.
                store.launchProcessing();
                return null;
            } else if (step == 4) {
                return objects;
            } else {
                throw helper.cancel(new ERR(), null, "bad-step",
                        "step", ""+step);
            }
        } catch (MissingLibraryException mle) {
            notifyObservers(new ErrorHandler.MISSING_LIBRARY(
                    fileName, mle, usedFiles, format));
            throw helper.cancel(new ERR(), mle, "import-missing-library",
                    "filename", fileName);
        } catch (UnsupportedCompressionException uce) {
            // Handling as UNKNOWN_FORMAT for 4.3.0
            notifyObservers(new ErrorHandler.UNKNOWN_FORMAT(
                    fileName, uce, this));
            throw helper.cancel(new ERR(), uce, "import-unknown-format",
                    "filename", fileName);
        } catch (UnknownFormatException ufe) {
            notifyObservers(new ErrorHandler.UNKNOWN_FORMAT(
                    fileName, ufe, this));
            throw helper.cancel(new ERR(), ufe, "import-unknown-format",
                    "filename", fileName);
        } catch (IOException io) {
            notifyObservers(new ErrorHandler.FILE_EXCEPTION(
                    fileName, io, usedFiles, format));
            throw helper.cancel(new ERR(), io, "import-file-exception",
                    "filename", fileName);
        } catch (FormatException fe) {
            notifyObservers(new ErrorHandler.FILE_EXCEPTION(
                    fileName, fe, usedFiles, format));
            throw helper.cancel(new ERR(), fe, "import-file-exception",
                    "filename", fileName);
        } catch (Cancel c) {
            throw c;
        } catch (Throwable t) {
            notifyObservers(new ErrorHandler.INTERNAL_EXCEPTION(
                    fileName, new RuntimeException(t), usedFiles, format));
            throw helper.cancel(new ERR(), t, "import-request-failure");
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void buildResponse(int step, Object object) {
        helper.assertResponse(step);
        if (step == 4) {
            ImportResponse rsp = new ImportResponse();
            Map<String, List<IObject>> rv = (Map<String, List<IObject>>) object;
            rsp.pixels = (List) rv.get(Pixels.class.getSimpleName());
            rsp.objects = new ArrayList<IObject>();
            addObjects(rsp.objects, rv, Plate.class.getSimpleName());
            addObjects(rsp.objects, rv, Image.class.getSimpleName());
            helper.setResponseIfNull(rsp);
        }
    }

    private void addObjects(List<IObject> objects,
            Map<String, List<IObject>> rv, String simpleName) {
        List<IObject> list = rv.get(simpleName);
        if (list != null) {
            objects.addAll(list);
        }
    }

    public Response getResponse() {
        cleanup();
        return helper.getResponse();
    }

    //
    // ACTIONS
    //

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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Map<String, List<IObject>> importMetadata(MetadataImportJob mij) throws Throwable {
        notifyObservers(new ImportEvent.LOADING_IMAGE(
                shortName, 0, 0, 0));

        // 1st we post-process the metadata that we've been given.
        notifyObservers(new ImportEvent.BEGIN_POST_PROCESS(
                0, null, null, null, 0, null));
        store.setUserSpecifiedName(userSpecifiedName);
        store.setUserSpecifiedDescription(userSpecifiedDescription);
        if (userPixels != null && userPixels.length >= 3)
            // The array could be empty due to Ice-non-null semantics.
            store.setUserSpecifiedPhysicalPixelSizes(
                    userPixels[0], userPixels[1], userPixels[2]);
        store.setUserSpecifiedTarget(userSpecifiedTarget);
        store.setUserSpecifiedAnnotations(annotationList);
        store.postProcess();
        notifyObservers(new ImportEvent.END_POST_PROCESS(
                0, null, userSpecifiedTarget, null, 0, null));

        notifyObservers(new ImportEvent.BEGIN_SAVE_TO_DB(
                0, null, userSpecifiedTarget, null, 0, null));
        objects = store.saveToDB(activity);
        pixList = (List) objects.get(Pixels.class.getSimpleName());
        imageList = (List) objects.get(Image.class.getSimpleName());
        plateList = (List) objects.get(Plate.class.getSimpleName());
        notifyObservers(new ImportEvent.END_SAVE_TO_DB(
                0, null, userSpecifiedTarget, null, 0, null));

        return objects;

    }

    public Object pixelData(PixelDataJob pdj) throws Throwable {

        boolean saveSha1 = false;
        // Parse the binary data to generate min/max values
        int seriesCount = reader.getSeriesCount();
        for (int series = 0; series < seriesCount; series++) {
            ImportSize size = new ImportSize(fileName,
                    pixList.get(series), reader.getDimensionOrder());
            Pixels pixels = pixList.get(series);
            MessageDigest md = parseData(fileName, series, size);
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
        for (Long pixelsId : pixelIds())
        {
            store.setPixelsParams(pixelsId, series, targetName, repoUuid);
            series++;
        }

        if (saveSha1)
        {
            store.updatePixels(pixList);
        }

        if (!reader.isMinMaxSet())
        {
            store.populateMinMax();
        }


        return null;
    }


    public Object generateThumbnails(ThumbnailGenerationJob tgj) throws Throwable {

        List<Long> plateIds = new ArrayList<Long>();
        Image image = pixList.get(0).getImage();
        if (image.sizeOfWellSamples() > 0)
        {
            Plate plate =
                image.copyWellSamples().get(0).getWell().getPlate();
            plateIds.add(plate.getId().getValue());
        }

        notifyObservers(new ImportEvent.IMPORT_OVERLAYS(
                0, null, userSpecifiedTarget, null, 0, null));
        importOverlays(pixList, plateIds);

        notifyObservers(new ImportEvent.IMPORT_PROCESSING(
                0, null, userSpecifiedTarget, null, 0, null));
        if (doThumbnails)
        {
            store.resetDefaultsAndGenerateThumbnails(plateIds, pixelIds());
        }
        else
        {
            log.warn("Not creating thumbnails at user request!");
        }

        return null;
    }

    public Object index(IndexingJob ij) {
        return null;
    }

    public Object script(ScriptJob sj) {
        return null;
    }

    //
    // HELPERS
    //

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
    private void parseDataByPlane(
            ImportSize size, int z, int c, int t,
            int bytesPerPixel, String fileName,
            MessageDigest md)
        throws FormatException, IOException
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
     * If available, populates overlays for a given set of pixels objects.
     * @param pixelsList Pixels objects to populate overlays for.
     * @param plateIds Plate object IDs to populate overlays for.
     */
    private void importOverlays(
            List<Pixels> pixelsList, List<Long> plateIds)
        throws FormatException, IOException
    {
        IFormatReader baseReader = reader.getImageReader().getReader();
        if (baseReader instanceof MIASReader)
        {
            try
            {
                MIASReader miasReader = (MIASReader) baseReader;
                ServiceFactoryPrx sf = store.getServiceFactory();
                OverlayMetadataStore s = new OverlayMetadataStore();
                s.initialize(sf, pixelsList, plateIds);
                miasReader.parseMasks(s);
                s.complete();
            }
            catch (ServerError e)
            {
                log.warn("Error while populating MIAS overlays.", e);
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

    private List<Long> pixelIds() {
        List<Long> pixelsIds = new ArrayList<Long>(pixList.size());
        for (Pixels pixels : pixList)
        {
            pixelsIds.add(pixels.getId().getValue());
        }
        return pixelsIds;
    }

    private void notifyObservers(Object...args) {
        // TEMPORARY REPLACEMENT. FIXME
    }

}
