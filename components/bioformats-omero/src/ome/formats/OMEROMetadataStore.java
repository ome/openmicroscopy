/*
 * ome.formats.OMEROMetadataStore
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import loci.formats.FormatReader;
import loci.formats.MetadataStore;
import loci.formats.MetadataStoreException;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.api.RawPixelsStore;
import ome.model.IObject;
import ome.model.acquisition.AcquisitionContext;
import ome.model.acquisition.StageLabel;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.LogicalChannel;
import ome.model.core.OriginalFile;
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import ome.model.core.PlaneInfo;
import ome.model.enums.AcquisitionMode;
import ome.model.enums.DimensionOrder;
import ome.model.enums.Format;
import ome.model.enums.PhotometricInterpretation;
import ome.model.enums.PixelsType;
import ome.model.meta.Experimenter;
import ome.model.stats.StatsInfo;
import ome.parameters.Parameters;
import ome.system.Login;
import ome.system.Server;
import ome.system.ServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An OMERO metadata store. This particular metadata store requires the user to
 * be logged into OMERO prior to use with the {@link #login()} method. NOTE: All
 * indexes are ignored by this metadata store as they don't make much real
 * sense.
 * 
 * @author Brian W. Loranger brain at lifesci.dundee.ac.uk
 * @author Chris Allan callan at blackcat.ca
 */
public class OMEROMetadataStore implements MetadataStore {

    /** Logger for this class. */
    private static Log log = LogFactory.getLog(OMEROMetadataStore.class);

    /** OMERO service factory; all other services are retrieved from here. */
    private ServiceFactory sf;

    /** OMERO raw pixels service */
    private RawPixelsStore pservice;

    /** OMERO query service */
    private IQuery iQuery;

    /** OMERO update service */
    private IUpdate iUpdate;

    /** The "root" pixels object */
    private Pixels pixels = new Pixels();

    private Experimenter exp;

    private RawFileStore rawFileStore;

    /**
     * Creates a new instance.
     * 
     * @param username
     *            the username to use to login to the OMERO server.
     * @param password
     *            the password to use to login to the OMERO server.
     * @param host
     *            the hostname of the OMERO server.
     * @param port
     *            the port the OMERO server is listening on.
     * @throws MetadataStoreException
     *             if the login credentials are incorrect or there is another
     *             error instantiating required services.
     */
    public OMEROMetadataStore(String username, String password, String host,
            String port) throws MetadataStoreException {
        // Mask the password information for display in the debug window
        String maskedPswd = "";
        if (password.length() > 0) {
            maskedPswd = "<" + password.length() + "chars>";
        } else {
            maskedPswd = "<empty>";
        }
        log.debug(String.format("Initializing store: %s/%s %s:%s", username,
                maskedPswd, host, port));

        // Attempt to log in
        try {
            Server server = new Server(host, Integer.parseInt(port));
            Login login = new Login(username, password);
            // Instantiate our service factory
            sf = new ServiceFactory(server, login);

            // Now initialize all our services
            iQuery = sf.getQueryService();
            iUpdate = sf.getUpdateService();
            pservice = sf.createRawPixelsStore();
            rawFileStore = sf.createRawFileStore();

            exp = iQuery.findByString(Experimenter.class, "omeName", username);
        } catch (Throwable t) {
            throw new MetadataStoreException(t);
        }
    }

    /**
     * Creates a new instance.
     * 
     * @param factory
     *            a non-null, active {@link ServiceFactory}
     * @throws MetadataStoreException
     *             if the factory is null or there is another error
     *             instantiating required services.
     */
    public OMEROMetadataStore(ServiceFactory factory)
            throws MetadataStoreException {
        if (factory == null) {
            throw new MetadataStoreException("Factory argument cannot be null.");
        }

        sf = factory;

        try {
            // Now initialize all our services
            iQuery = sf.getQueryService();
            iUpdate = sf.getUpdateService();
            pservice = sf.createRawPixelsStore();
        } catch (Throwable t) {
            throw new MetadataStoreException(t);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#getRoot()
     */
    public Object getRoot() {
        return pixels;
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setRoot(java.lang.Object)
     */
    public void setRoot(Object root) throws IllegalArgumentException {
        if (!(root instanceof Pixels)) {
            throw new IllegalArgumentException("'root' object of type '"
                    + root.getClass()
                    + "' must be of type 'ome.model.core.Pixels'");
        }
        pixels = (Pixels) root;
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#createRoot()
     */
    public void createRoot() {
        pixels = new Pixels();
    }

    /**
     * Retrieves a server side enumeration.
     * 
     * @param klass
     *            the enumeration's class from <code>ome.model.enum</code>
     * @param value
     *            the enumeration's string value.
     * @return enumeration object.
     */
    private IObject getEnumeration(Class<? extends IObject> klass, String value) {
        if (klass == null) {
            throw new NullPointerException("Expecting not-null klass.");
        }
        if (value == null) {
            return null;
        }

        IObject enumeration = iQuery.findByString(klass, "value", value);

        if (enumeration == null) {
            throw new EnumerationException("Problem finding enumeration: ",
                    klass, value);
        }
        return enumeration;
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setImage(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.Integer)
     */
    public void setImage(String name, String creationDate, String description,
            Integer i) {
        log.debug(String.format("Setting Image: name (%s), creationDate (%s), "
                + "description (%s)", name, creationDate, description));
        // FIXME: Image really needs to handle creation date somehow.
        Image image = new Image();
        image.setName(name);
        image.setDescription(description);

        pixels.setImage(image);
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setExperimenter(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.String,
     *      java.lang.String, java.lang.Object, java.lang.Integer)
     */
    public void setExperimenter(String firstName, String lastName,
            String email, String institution, String dataDirectory,
            Object group, Integer i) {
        // FIXME: To implement.
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setGroup(java.lang.String,
     *      java.lang.Object, java.lang.Object, java.lang.Integer)
     */
    public void setGroup(String name, Object leader, Object contact, Integer i) {
        // FIXME: To implement.
    }

    public void setInstrument(String manufacturer, String model,
            String serialNumber, String type, Integer i) {
        // FIXME: To implement.
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setDimensions(java.lang.Float,
     *      java.lang.Float, java.lang.Float, java.lang.Float, java.lang.Float,
     *      java.lang.Integer)
     */
    public void setDimensions(Float pixelSizeX, Float pixelSizeY,
            Float pixelSizeZ, Float pixelSizeC, Float pixelSizeT, Integer i) {
        log.debug(String.format(
                "Setting Dimensions: pixelSizeX (%f), pixelSizeY (%f), "
                        + "pixelSizeZ (%f), pixelSizeC (%f), pixelSizeT (%f)",
                pixelSizeX, pixelSizeY, pixelSizeZ, pixelSizeC, pixelSizeT));
        PixelsDimensions dimensions = new PixelsDimensions();

        if (pixelSizeX <= 0) {
            log.warn("pixelSizeX is <= 0.0f, setting to 1.0f");
            pixelSizeX = 1.0f;
        }

        if (pixelSizeY <= 0) {
            log.warn("pixelSizeY is <= 0.0f, setting to 1.0f");
            pixelSizeY = 1.0f;
        }

        if (pixelSizeZ <= 0) {
            log.warn("pixelSizeZ is <= 0.0f, setting to 1.0f");
            pixelSizeZ = 1.0f;
        }

        dimensions.setSizeX(pixelSizeX);
        dimensions.setSizeY(pixelSizeY);
        dimensions.setSizeZ(pixelSizeZ);

        pixels.setPixelsDimensions(dimensions);
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setDisplayROI(java.lang.Integer,
     *      java.lang.Integer, java.lang.Integer, java.lang.Integer,
     *      java.lang.Integer, java.lang.Integer, java.lang.Integer,
     *      java.lang.Integer, java.lang.Object, java.lang.Integer)
     */
    public void setDisplayROI(Integer x0, Integer y0, Integer z0, Integer x1,
            Integer y1, Integer z1, Integer t0, Integer t1,
            Object displayOptions, Integer i) {
        // FIXME: We have no real type for this.
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setPixels(java.lang.Integer,
     *      java.lang.Integer, java.lang.Integer, java.lang.Integer,
     *      java.lang.Integer, java.lang.String, java.lang.Boolean,
     *      java.lang.String, java.lang.Integer)
     */
    public void setPixels(Integer sizeX, Integer sizeY, Integer sizeZ,
            Integer sizeC, Integer sizeT, Integer pixelType, Boolean bigEndian,
            String dimensionOrder, Integer i) {
        log
                .debug(String
                        .format(
                                "Setting Pixels: x (%d), y (%d), z (%d), c (%d), t (%d), "
                                        + "PixelType (%s), BigEndian? (%b), dimemsionOrder (%s)",
                                sizeX, sizeY, sizeZ, sizeC, sizeT, pixelType,
                                bigEndian, dimensionOrder));
        // Retrieve enumerations from the server

        PixelsType type = null;
        switch (pixelType) {
            case FormatReader.INT8:
                type = (PixelsType) getEnumeration(PixelsType.class, "int8");
                break;
            case FormatReader.UINT8:
                type = (PixelsType) getEnumeration(PixelsType.class, "uint8");
                break;
            case FormatReader.INT16:
                type = (PixelsType) getEnumeration(PixelsType.class, "int16");
                break;
            case FormatReader.UINT16:
                type = (PixelsType) getEnumeration(PixelsType.class, "uint16");
                break;
            case FormatReader.INT32:
                type = (PixelsType) getEnumeration(PixelsType.class, "int32");
                break;
            case FormatReader.UINT32:
                type = (PixelsType) getEnumeration(PixelsType.class, "uint32");
                break;
            case FormatReader.FLOAT:
                type = (PixelsType) getEnumeration(PixelsType.class, "float");
                break;
            case FormatReader.DOUBLE:
                type = (PixelsType) getEnumeration(PixelsType.class, "double");
                break;
            default:
                new RuntimeException("Unknown pixelType enumeration: "
                        + pixelType);
        }
        DimensionOrder order = (DimensionOrder) getEnumeration(
                DimensionOrder.class, dimensionOrder);

        pixels.setSha1("foo"); // FIXME: needs to be fixed!
        pixels.setSizeX(sizeX);
        pixels.setSizeY(sizeY);
        pixels.setSizeZ(sizeZ);
        pixels.setSizeC(sizeC);
        pixels.setSizeT(sizeT);
        pixels.setPixelsType(type);
        pixels.setDimensionOrder(order);
        pixels.setDefaultPixels(Boolean.TRUE); // *Very* important
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setStageLabel(java.lang.String,
     *      java.lang.Float, java.lang.Float, java.lang.Float,
     *      java.lang.Integer)
     */
    public void setStageLabel(String name, Float x, Float y, Float z, Integer i) {
        log.debug(String.format(
                "Setting StageLabel: name (%s), x (%f), y (%f), z (%f)", name,
                x, y, z));

        // Checks to make sure we have no null values and returns with a warning
        // if we do.
        if (name == null || x == null || y == null || z == null) {
            log
                    .warn(String
                            .format(
                                    "StageLabel has null value(s): name (%s), x (%f), y (%f), z (%f)",
                                    name, x, y, z));
            return;
        }

        StageLabel stageLabel = new StageLabel();
        stageLabel.setName(name);
        stageLabel.setPositionX(x);
        stageLabel.setPositionY(y);
        stageLabel.setPositionZ(z);

        pixels.getImage().setPosition(stageLabel);
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setLogicalChannel(int, java.lang.String,
     *      float, int, int, java.lang.String, java.lang.String,
     *      java.lang.Integer)
     */
    public void setLogicalChannel(int channelIdx, String name, Float ndFilter,
            Integer emWave, Integer exWave, String photometricInterpretation,
            String mode, Integer i) {
        log
                .debug(String
                        .format(
                                "Setting LogicalChannel: channelIdx (%d), name (%s), ndFilter (%f), "
                                        + "emWave (%d), exWave (%d), photometicInterpretation (%s), mode (%s)",
                                channelIdx, name, ndFilter, emWave, exWave,
                                photometricInterpretation, mode));
        // Retrieve enumerations from the server
        PhotometricInterpretation pi = (PhotometricInterpretation) getEnumeration(
                PhotometricInterpretation.class, photometricInterpretation);
        AcquisitionMode acquisitionMode = (AcquisitionMode) getEnumeration(
                AcquisitionMode.class, mode);

        List<Channel> channels = pixels.getChannels();

        AcquisitionContext ctx = new AcquisitionContext();
        ctx.setPhotometricInterpretation(pi);
        ctx.setMode(acquisitionMode);

        Channel channel;
        if (channels.size() == 0) {
            channels = new ArrayList<Channel>(pixels.getSizeC());
        }

        channel = new Channel();
        channels.add(channelIdx, channel);

        LogicalChannel lchannel = new LogicalChannel();
        lchannel.setEmissionWave(emWave);
        lchannel.setExcitationWave(exWave);
        lchannel.setName(name);
        lchannel.setNdFilter(ndFilter);

        channel.setLogicalChannel(lchannel);
        pixels.setChannels(channels);
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setChannelGlobalMinMax(int,
     *      java.lang.Double, java.lang.Double, java.lang.Integer)
     */
    public void setChannelGlobalMinMax(int channelIdx, Double globalMin,
            Double globalMax, Integer i) {
        log.debug(String.format(
                "Setting GlobalMin: '%f' GlobalMax: '%f' for channel: '%d'",
                globalMin, globalMax, channelIdx));
        if (globalMin == null) {
            throw new NullPointerException("globalMin should not be null.");
        }

        if (globalMax == null) {
            throw new NullPointerException("globalMax should not be null.");
        }

        List<Channel> channels = pixels.getChannels();

        if (channels.size() < channelIdx) {
            throw new IndexOutOfBoundsException("No such channel index: "
                    + channelIdx);
        }

        Channel channel = channels.get(channelIdx);
        StatsInfo statsInfo = new StatsInfo();
        statsInfo.setGlobalMin(Math.floor(globalMin));
        statsInfo.setGlobalMax(Math.floor(globalMax));

        channel.setStatsInfo(statsInfo);
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setPlaneInfo(int, int, int,
     *      java.lang.Float, java.lang.Float, java.lang.Integer)
     */
    public void setPlaneInfo(int z, int c, int t, Float timestamp,
            Float exposureTime, Integer i) {
        PlaneInfo pi = new PlaneInfo();
        pi.setTheZ(z);
        pi.setTheC(c);
        pi.setTheT(t);
        pi.setTimestamp(timestamp);
        pi.setExposureTime(exposureTime);
        pixels.addPlaneInfo(pi);
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setDefaultDisplaySettings(java.lang.Integer)
     */
    public void setDefaultDisplaySettings(Integer i) {
    }

    /**
     * Writes a set of bytes as a plane in the OMERO image repository.
     * 
     * @param id
     *            the primary <i>id</i> of the pixels set.
     * @param pixels
     *            an array of bytes (sizeX * sizeY * bytesPerPixel)
     * @param theZ
     *            the optical section in the pixels array.
     * @param theC
     *            the channel in the pixels array.
     * @param theT
     *            the timepoint in the pixels array.
     */
    public void setPlane(Long id, byte[] pixels, int theZ, int theC, int theT) {
        if (pservice == null) {
            pservice = sf.createRawPixelsStore();
        }

        pservice.setPixelsId(id);
        pservice.setPlane(pixels, theZ, theC, theT);
    }

    /**
     * Writes a set of bytes as a stack in the OMERO image repository.
     * 
     * @param id
     *            he primary <i>id</i> of the pixels set.
     * @param pixels
     *            an array of bytes (sizeX * sizeY * sizeZ * bytesPerPixel)
     * @param theC
     *            the channel in the pixels array.
     * @param theT
     *            the timepoint in the pixels array.
     */
    public void setStack(Long id, byte[] pixels, int theC, int theT) {
        if (pservice == null) {
            pservice = sf.createRawPixelsStore();
        }

        pservice.setPixelsId(id);
        pservice.setStack(pixels, theT, theC, theT);
    }

    /**
     * Adds a pixels set (really its image) to a dataset.
     * 
     * @param pixId
     *            the primary <i>id</i> of the pixels set.
     * @param dataset
     *            the dataset.
     */
    public void addPixelsToDataset(Long pixId, Dataset dataset) {
        Pixels pixels = iQuery.get(Pixels.class, pixId);

        // We need a special dataset query because of the nature of the call
        // we're going to do next needing the Dataset --> Image links.
        Dataset d2 = iQuery.findByQuery(
                "select d from Dataset as d left join fetch "
                        + "d.imageLinks where d.id = :id", new Parameters()
                        .addId(dataset.getId()));

        // Link the image to the dataset
        d2.linkImage(pixels.getImage());

        // Now update the dataset object in the database
        iUpdate.saveObject(d2);
    }

    /**
     * Retrieves dataset names of the current user from the active OMERO
     * instance.
     * 
     * @param project
     *            the project to retireve datasets from.
     * @return an array of dataset names.
     */
    public List<Dataset> getDatasets(Project project) {
        List l = iQuery.findAllByQuery("from Dataset where id in "
                + "(select link.child.id from ProjectDatasetLink link where "
                + "link.parent.id = :id)", new Parameters().addId(project
                .getId()));
        return l;

        // Use this for M3 build till it gets fixed if this is needed.
        // return new ArrayList();
    }

    /**
     * Retrieves dataset names of the current user from the active OMERO
     * instance.
     * 
     * @return an array of dataset names.
     */
    public List<Project> getProjects() {
        List l = iQuery.findAllByQuery(
                "from Project as p where p.details.owner.id = :id",
                new Parameters().addId(exp.getId()));
        return l;
    }

    /**
     * Saves the current <i>root</i> pixels to the database.
     * 
     * @return the primary <i>id</i> of the pixels set saved.
     */
    public Long saveToDB() {
        IUpdate update = sf.getUpdateService();
        pixels = update.saveAndReturnObject(pixels);

        return pixels.getId();
    }

    public void setOriginalFiles(File[] files) {
        for (File file : files) {
            Format f = iQuery.findByString(Format.class, "value", "DV");
            OriginalFile oFile = new OriginalFile();
            oFile.setName(file.getName());
            oFile.setPath(file.getAbsolutePath());
            oFile.setSize(new Integer((int) file.length())); // FIXME this
                                                                // needs to be
                                                                // long
            oFile.setSha1("pending");
            oFile.setFormat(f);
            pixels.linkOriginalFile(oFile);
        }
    }

    public void writeFilesToFileStore(File[] files, long pixelsId) {
        try {
            for (File file : files) {
                System.err.println(file + "  " + pixelsId);
                Parameters p = new Parameters();
                p.addId(pixelsId);
                p.addString("path", file.getAbsolutePath());
                OriginalFile o = iQuery
                        .findByQuery(
                                "select ofile from OriginalFile as ofile left join "
                                        + "ofile.pixelsFileMaps as pfm left join pfm.child as child "
                                        + "where child.id = :id and ofile.path =:path",
                                p);

                if (o == null) {
                    throw new FileNotFoundException(
                            "Unable to look up originalFile");
                }

                rawFileStore.setFileId(o.getId());

                byte[] buf = new byte[262144];
                FileInputStream stream = new FileInputStream(file);

                long time = System.currentTimeMillis();
                long pos = 0;
                int rlen;
                while ((rlen = stream.read(buf)) > 0) {
                    rawFileStore.write(buf, pos, rlen);
                    pos += rlen;
                    ByteBuffer nioBuffer = ByteBuffer.wrap(buf);
                    nioBuffer.limit(rlen);
                }

                System.err.println(System.currentTimeMillis() - time);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
