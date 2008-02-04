/*
 * ome.formats.OMEROMetadataStore
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
GPL'd. See License attached to this project
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.formats;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import loci.formats.FormatTools;
import loci.formats.meta.MetadataStore;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.api.RawPixelsStore;
import ome.model.IObject;
import ome.model.acquisition.StageLabel;
import ome.model.containers.Dataset;
import ome.model.containers.DatasetImageLink;
import ome.model.containers.Project;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.LogicalChannel;
import ome.model.core.OriginalFile;
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import ome.model.core.PlaneInfo;
import ome.model.enums.AcquisitionMode;
import ome.model.enums.ContrastMethod;
import ome.model.enums.DimensionOrder;
import ome.model.enums.Format;
import ome.model.enums.Illumination;
import ome.model.enums.PhotometricInterpretation;
import ome.model.enums.PixelsType;
import ome.model.meta.Experimenter;
import ome.model.stats.StatsInfo;
import ome.parameters.Parameters;
import ome.system.Login;
import ome.system.Server;
import ome.system.ServiceFactory;
import ome.api.IRepositoryInfo;

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
public class OMEROMetadataStore implements MetadataStore
{

    /** Logger for this class. */
    private static Log     log    = LogFactory.getLog(OMEROMetadataStore.class);

    /** OMERO service factory; all other services are retrieved from here. */
    private ServiceFactory sf;

    /** OMERO raw pixels service */
    private RawPixelsStore pservice;

    /** OMERO query service */
    private IQuery         iQuery;

    /** OMERO update service */
    private IUpdate        iUpdate;
    
    private IRepositoryInfo iInfo;

    /** The "root" image object */
    private List<Image> imageList = new ArrayList<Image>();
    
    /** An list of Pixels that we have worked on ordered by first access. */
    private List<Pixels> pList = new ArrayList<Pixels>();
    
    private Experimenter    exp;
    
    private RawFileStore    rawFileStore;

    //private List<Boolean> minMaxSet = new ArrayList<Boolean>();
    
    /**
     * Creates a new instance.
     * 
     * @param username the username to use to login to the OMERO server.
     * @param password the password to use to login to the OMERO server.
     * @param host the hostname of the OMERO server.
     * @param port the port the OMERO server is listening on.
     * @throws MetadataStoreException if the login credentials are
     *             incorrect or there is another error instantiating required
     *             services.
     */
    public OMEROMetadataStore(String username, String password, String host,
            String port) throws Exception
    {
        // Mask the password information for display in the debug window
        String maskedPswd = "";
        if (password == null) password = new String("");
        if (password.length() > 0) maskedPswd = "<" +password.length() + "chars>";
        else maskedPswd = "<empty>";
        log.debug(String.format("Initializing store: %s/%s %s:%s", 
                username, maskedPswd, host, port));
        
        // Attempt to log in
        try
        {
            Server server = new Server(host, Integer.parseInt(port));
            Login login = new Login(username, password);
            // Instantiate our service factory
            sf = new ServiceFactory(server, login);

            // Now initialize all our services
            iQuery = sf.getQueryService();
            iUpdate = sf.getUpdateService();
            pservice = sf.createRawPixelsStore();
            rawFileStore = sf.createRawFileStore();
            iInfo = sf.getRepositoryInfoService();
            
            exp = iQuery.findByString(Experimenter.class, "omeName", username);
        } catch (Throwable t)
        {
            throw new Exception(t);
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param factory a non-null, active {@link ServiceFactory}
     * @throws MetadataStoreException if the factory is null or there
     *             is another error instantiating required services.
     */
    public OMEROMetadataStore(ServiceFactory factory)
            throws Exception
    {
        if (factory == null)
            throw new Exception(
                    "Factory argument cannot be null.");

        sf = factory;

        try
        {
            // Now initialize all our services
            iQuery = sf.getQueryService();
            iUpdate = sf.getUpdateService();
            pservice = sf.createRawPixelsStore();
        } catch (Throwable t)
        {
            throw new Exception(t);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#getRoot()
     */
    public Object getRoot()
    {
        return imageList;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see loci.formats.MetadataStore#createRoot()
	 */
	public void createRoot()
	{
	    imageList = new ArrayList<Image>();
	}

	/*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setRoot(java.lang.Object)
     */
    public void setRoot(Object root) throws IllegalArgumentException
    {
        if (!(root instanceof List))
            throw new IllegalArgumentException("'root' object of type '"
                    + root.getClass()
                    + "' must be of type 'List<ome.model.core.Image>'");
        imageList = (List<Image>) root;
    }

    /**
     * Retrieves a server side enumeration.
     * 
     * @param klass the enumeration's class from <code>ome.model.enum</code>
     * @param value the enumeration's string value.
     * @return enumeration object.
     */
    private IObject getEnumeration(Class<? extends IObject> klass, String value)
    {
        if (klass == null)
            throw new NullPointerException("Expecting not-null klass.");
        if (value == null) return null;

        IObject enumeration = iQuery.findByString(klass, "value", value);

        if (enumeration == null)
            throw new EnumerationException("Problem finding enumeration: ",
                    klass, value);
        return enumeration;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setImage(java.lang.String,
     *      java.lang.String, java.lang.String, java.lang.Integer)
     */
    public void setImage(String name, String creationDate, String description,
            Integer series)
    {
        
        log.debug(String.format("Setting Image: name (%s), creationDate (%s), "
                + "description (%s)", name, creationDate, description));
        // FIXME: Image really needs to handle creation date somehow.
        Image image = new Image();
        image.setName(name);
        image.setDescription(description);

        getPixels(series).setImage(image);
    }

    public long getExperimenterID()
    {
        return exp.getId();
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
            Object group, Integer i)
    {
    // FIXME: To implement.
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setGroup(java.lang.String,
     *      java.lang.Object, java.lang.Object, java.lang.Integer)
     */
    public void setGroup(String name, Object leader, Object contact, Integer i)
    {
    // FIXME: To implement.
    }

    public void setInstrument(String manufacturer, String model,
            String serialNumber, String type, Integer i)
    {
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
            Float pixelSizeZ, Float pixelSizeC, Float pixelSizeT, Integer i)
    {
        log.debug(String.format(
                "Setting Dimensions: pixelSizeX (%f), pixelSizeY (%f), "
                        + "pixelSizeZ (%f), pixelSizeC (%f), pixelSizeT (%f)",
                pixelSizeX, pixelSizeY, pixelSizeZ, pixelSizeC, pixelSizeT));
        PixelsDimensions dimensions = new PixelsDimensions();
        
        if (pixelSizeX == null || pixelSizeX <= 0.000001)
        {
            log.warn("pixelSizeX is <= 0.000001f, setting to 1.0f");
            pixelSizeX = 1.0f;
        } else {
            log.warn("pixelSizeX is " + pixelSizeX);
        }

        if (pixelSizeY == null || pixelSizeY <= 0.000001)
        {
            log.warn("pixelSizeY is <= 0.000001f, setting to 1.0f");
            pixelSizeY = 1.0f;
        } else {
            log.warn("pixelSizeY is " + pixelSizeY);
        }
        
        if (pixelSizeZ == null || pixelSizeZ <= 0.000001)
        {
            log.warn("pixelSizeZ is <= 0.000001f, setting to 1.0f");
            pixelSizeZ = 1.0f;
        } else {
            log.warn("pixelSizeZ is " + pixelSizeZ);
        }

        dimensions.setSizeX(pixelSizeX);
        dimensions.setSizeY(pixelSizeY);
        dimensions.setSizeZ(pixelSizeZ);

        getPixels(i).setPixelsDimensions(dimensions);
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
            Object displayOptions, Integer i)
    {
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
            String dimensionOrder, Integer imageIndex, Integer pixelsIndex)
    {
        // Retrieve enumerations from the server               
        PixelsType type = null;
        String pixTypeString = "";
        switch(pixelType)
        {
            case FormatTools.INT8:
                type = (PixelsType) getEnumeration(PixelsType.class, "int8");
                pixTypeString = "int8";
                break;
            case FormatTools.UINT8:
                type = (PixelsType) getEnumeration(PixelsType.class, "uint8");
                pixTypeString = "uint8";
                break;
            case FormatTools.INT16:
                type = (PixelsType) getEnumeration(PixelsType.class, "int16");
                pixTypeString = "int16";
                break;
            case FormatTools.UINT16:
                type = (PixelsType) getEnumeration(PixelsType.class, "uint16");
                pixTypeString = "uint16";
                break;
            case FormatTools.INT32:
                type = (PixelsType) getEnumeration(PixelsType.class, "int32");
                pixTypeString = "int32";
                break;
            case FormatTools.UINT32:
                type = (PixelsType) getEnumeration(PixelsType.class, "uint32");
                pixTypeString = "uint32";
                break;
            case FormatTools.FLOAT:
                type = (PixelsType) getEnumeration(PixelsType.class, "float");
                pixTypeString = "float";
                break;
            case FormatTools.DOUBLE:
                type = (PixelsType) getEnumeration(PixelsType.class, "double");
                pixTypeString = "double";
                break;
            default: new RuntimeException("Unknown pixelType enumeration: " 
                    + pixelType);
        }
        
        log
        .debug(String
                .format(
                        "Setting Pixels: x (%d), y (%d), z (%d), c (%d), t (%d), "
                                + "PixelType (%s), BigEndian? (%b), dimemsionOrder (%s)",
                        sizeX, sizeY, sizeZ, sizeC, sizeT, pixTypeString,
                        bigEndian, dimensionOrder));
        
        
        DimensionOrder order = (DimensionOrder) getEnumeration(
                DimensionOrder.class, dimensionOrder);

        Pixels pixels = getPixels(imageIndex);
        
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
    public void setStageLabel(String name, Float x, Float y, Float z, Integer i)
    {
        log.debug(String.format(
                "Setting StageLabel: name (%s), x (%f), y (%f), z (%f)", name,
                x, y, z));

        // Checks to make sure we have no null values and returns with a warning
        // if we do.
        if (name == null || x == null || y == null || z == null)
        {
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

        getPixels(i).getImage().setPosition(stageLabel);
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setLogicalChannel(int, java.lang.String,
     *      float, int, int, java.lang.String, java.lang.String,
     *      java.lang.Integer)
     */
    public void setLogicalChannel(int channelIdx, String name, Integer samplesPerPixel,
    	    Integer filter, Integer lightSource, Float lightAttenuation,
    	    Integer lightWavelength, Integer otf, Integer detector, 
    	    Float detectorOffset, Float detectorGain, String illuminationType, 
    	    Integer pinholeSize, String photometricInterpretation, String mode, 
    	    String contrastMethod, Integer auxLightSource, Float auxLightAttenuation, 
    	    String auxTechnique, Integer auxLightWavelength, Integer emWave, 
    	    Integer exWave, String fluor, Float ndFilter, Integer series)
    {
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

        
        Pixels pixels = getPixels(series);
        
        List<Channel> channels = pixels.getChannels();
        Channel channel = new Channel();

        if (channels.size() == 0)
        {
            channels = initChannels(series);
        } 
        else if (channels.size() != pixels.getSizeC())
        {
            log.warn(String.format("channels.size() (%d) is not equal to " +
                    "pixels.getChannels().size() (%d) Resetting channel array.", 
                    channels.size(), pixels.getSizeC()));
            channels = initChannels(series);
        }
        
//        try {
//            channels.set(channelIdx, channel);
//        } catch (IndexOutOfBoundsException e) {
//            channels.add(channelIdx, channel);
//        }

        channels.set(channelIdx, channel);
        
        LogicalChannel lchannel = new LogicalChannel();
        lchannel.setEmissionWave(emWave);
        lchannel.setExcitationWave(exWave);
        lchannel.setName(name);
        lchannel.setNdFilter(ndFilter);
        lchannel.setPhotometricInterpretation(pi);
        lchannel.setMode(acquisitionMode);

        channel.setLogicalChannel(lchannel);
        pixels.setChannels(channels);
    }

    private List<Channel> initChannels(Integer i)
    {
        Pixels pixels = getPixels(i);
        
        List<Channel> channels = new ArrayList<Channel>(pixels.getSizeC());
        for (int j = 0; j < pixels.getSizeC(); j++)
        {
            channels.add(null);
        }
        return channels;
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setChannelGlobalMinMax(int,
     *      java.lang.Double, java.lang.Double, java.lang.Integer)
     */
    public void setChannelGlobalMinMax(int channelIdx, Double globalMin,
            Double globalMax, Integer i)
    {
//       try { minMaxSet.get(channelIdx); }
//        catch (IndexOutOfBoundsException e) 
//        { minMaxSet.add(channelIdx, false); }
//
//        if (minMaxSet.get(channelIdx) == true)
//            return;
        log.debug(String.format(
                "Setting GlobalMin: '%f' GlobalMax: '%f' for channel: '%d'",
                globalMin, globalMax, channelIdx));
        if (globalMin != null)
        {
        	globalMin = new Double(Math.floor(globalMin.doubleValue()));
        }
        if (globalMax != null)
        {
        	globalMax = new Double(Math.ceil(globalMax.doubleValue()));
        }

        List<Channel> channels = getPixels(i).getChannels();

        if (channels.size() < channelIdx)
            throw new IndexOutOfBoundsException("No such channel index: "
                    + channelIdx);

        Channel channel = channels.get(channelIdx);
        StatsInfo statsInfo = new StatsInfo();
        statsInfo.setGlobalMin(globalMin);
        statsInfo.setGlobalMax(globalMax);
        
        channel.setStatsInfo(statsInfo);
        //minMaxSet.set(channelIdx, true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setPlaneInfo(int, int, int,
     *      java.lang.Float, java.lang.Float, java.lang.Integer)
     */
    public void setPlaneInfo(int z, int c, int t, Float timestamp,
            Float exposureTime, Integer i)
    {
        PlaneInfo pi = new PlaneInfo();
        pi.setTheZ(z);
        pi.setTheC(c);
        pi.setTheT(t);
        pi.setTimestamp(timestamp);
        pi.setExposureTime(exposureTime);
        getPixels(i).addPlaneInfo(pi);
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setDefaultDisplaySettings(java.lang.Integer)
     */
    public void setDefaultDisplaySettings(Integer i) {}

    /**
     * Writes a set of bytes as a plane in the OMERO image repository.
     * 
     * @param id the primary <i>id</i> of the pixels set.
     * @param pixels an array of bytes (sizeX * sizeY * bytesPerPixel)
     * @param theZ the optical section in the pixels array.
     * @param theC the channel in the pixels array.
     * @param theT the timepoint in the pixels array.
     */
    public void setPlane(Long id, byte[] pixels, int theZ, int theC, int theT)
    {
        if (pservice == null) pservice = sf.createRawPixelsStore();

        pservice.setPixelsId(id);
        pservice.setPlane(pixels, theZ, theC, theT);
    }

    /**
     * Writes a set of bytes as a stack in the OMERO image repository.
     * 
     * @param id he primary <i>id</i> of the pixels set.
     * @param pixels an array of bytes (sizeX * sizeY * sizeZ * bytesPerPixel)
     * @param theC the channel in the pixels array.
     * @param theT the timepoint in the pixels array.
     */
    public void setStack(Long id, byte[] pixels, int theC, int theT)
    {
        if (pservice == null) pservice = sf.createRawPixelsStore();

        pservice.setPixelsId(id);
        pservice.setStack(pixels, theT, theC, theT);
    }

    /**
     * Adds an image) to a dataset.
     * 
     * @param image The image to link to <code>dataset</code>.
     * @param dataset The dataset to link to <code>image</code>.
     */
    public void addImageToDataset(Image image, Dataset dataset)
    {
        Image unloadedImage = new Image(image.getId());
        unloadedImage.unload();

        Dataset unloadedDataset = new Dataset(dataset.getId());
        unloadedDataset.unload();
        DatasetImageLink link = new DatasetImageLink();
        link.setParent(unloadedDataset);
        link.setChild(unloadedImage);

        // Now update the dataset object in the database
        iUpdate.saveObject(link);
    }

    /**
     * Retrieves dataset names of the current user from the active OMERO
     * instance.
     * @param project the project to retireve datasets from. 
     * @return an array of dataset names.
     */
    public List<Dataset> getDatasets(Project project)
    {
        List<Dataset> l = iQuery.findAllByQuery(
                "from Dataset where id in " +
                "(select link.child.id from ProjectDatasetLink link where " +
                "link.parent.id = :id)", new Parameters().addId(project.getId()));
       return (List<Dataset>) l;
       
       // Use this for M3 build till it gets fixed if this is needed.
       //return new ArrayList();
    }

    public String getDatasetName(long datasetID)
    {
        Dataset dataset = iQuery.get(Dataset.class, datasetID);
        return dataset.getName();
    }

    public String getProjectName(long projectID)
    {
        Project project = iQuery.get(Project.class, projectID);
        return project.getName();
    }
    
    /**
     * Retrieves dataset names of the current user from the active OMERO
     * instance.
     * 
     * @return an array of dataset names.
     */
    public List<Project> getProjects()
    {
        List<Project> l = iQuery.findAllByQuery(
                "from Project as p left join fetch p.datasetLinks " +
                "where p.details.owner.id = :id", 
                new Parameters().addId(exp.getId()));
        return (List<Project>) l;
    }
    
    
    /**
     * Saves the current <i>root</i> pixelsList to the database.
     */
    public List<Pixels> saveToDB()
    {
        IUpdate update = sf.getUpdateService();
        Image[] imageArray = imageList.toArray(new Image[imageList.size()]);
        IObject[] o = update.saveAndReturnArray(imageArray);
        for (int i = 0; i < o.length; i++)
        {
            imageList.set(i, (Image) o[i]);
        }
        return pList;
    }
    
    public ServiceFactory getSF()
    {
        return sf;
    }

    public IUpdate getIUpdate()
    {
        return iUpdate;
    }
    
    /**
     * Links a set of original files to all Pixels that the metadata store
     * currently knows about. NOTE: Ensure that you call this <b>after</b>
     * fully populating the metadata store.
     * @param files The list of File objects to translate to OriginalFile
     * objects and link.
     */
    public void setOriginalFiles(File[] files)
    {
        for (File file: files)
        {
        	// FIXME: This is **incorrect** quite obviously, not every file
        	// is of format "DV".
            Format f = iQuery.findByString(Format.class, "value", "DV");
            OriginalFile oFile = new OriginalFile();
            oFile.setName(file.getName());
            oFile.setPath(file.getAbsolutePath());
            oFile.setSize(file.length());
            oFile.setSha1("pending");
            oFile.setFormat(f);
            for (Pixels pixels : pList)
            {
                pixels.linkOriginalFile(oFile);
            }
        }
    }
    
    public void writeFilesToFileStore(File[] files, long pixelsId)
    {
        try
        {
            for (File file : files)
            {
                Parameters p = new Parameters();
                p.addId(pixelsId);
                p.addString("path", file.getAbsolutePath());
                OriginalFile o = iQuery.findByQuery(
                        "select ofile from OriginalFile as ofile left join " +
                        "ofile.pixelsFileMaps as pfm left join pfm.child as child " +
                        "where child.id = :id and ofile.path =:path", p);
                
                if (o == null) throw 
                    new FileNotFoundException("Unable to look up originalFile");
                
                rawFileStore.setFileId(o.getId());
                
                byte[] buf = new byte[262144];            
                FileInputStream stream = new FileInputStream(file);

                long time = System.currentTimeMillis();
                long pos = 0;
                int rlen;
                while((rlen = stream.read(buf)) > 0)
                {
                    rawFileStore.write(buf, pos, rlen);
                    pos += rlen;
                    ByteBuffer nioBuffer = ByteBuffer.wrap(buf);
                    nioBuffer.limit(rlen);
                }
            }
            
        } catch (Exception e)
        {
            e.printStackTrace();   
        }
           
    }

	/* (non-Javadoc)
	 * @see loci.formats.MetadataStore#setArc(java.lang.String, java.lang.Float, java.lang.Integer, java.lang.Integer)
	 */
	public void setArc(String type, Float power, Integer lightNdx,
	                   Integer arcNdx)
	{
		// TODO: Unsupported.
	}



	/* (non-Javadoc)
	 * @see loci.formats.MetadataStore#setDetector(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Float, java.lang.Float, java.lang.Float, java.lang.Integer, java.lang.Integer)
	 */
	public void setDetector(String manufacturer, String model,
	                        String serialNumber, String type, Float gain,
	                        Float voltage, Float offset, Integer instrumentNdx,
	                        Integer detectorNdx)
	{
		// TODO: Unsupported.
	}

	/* (non-Javadoc)
	 * @see loci.formats.MetadataStore#setDichroic(java.lang.String, java.lang.String, java.lang.String, java.lang.Integer)
	 */
	public void setDichroic(String manufacturer, String model, String lotNumber,
	                        Integer dichroicNdx)
	{
		// TODO: Unsupported.
	}

	/* (non-Javadoc)
	 * @see loci.formats.MetadataStore#setDisplayChannel(java.lang.Integer, java.lang.Double, java.lang.Double, java.lang.Float, java.lang.Integer)
	 */
	public void setDisplayChannel(Integer channelNumber, Double blackLevel,
			                      Double whiteLevel, Float gamma, Integer i)
	{
		// TODO: Unsupported.
	}

	/* (non-Javadoc)
	 * @see loci.formats.MetadataStore#setDisplayOptions(java.lang.Float, java.lang.Boolean, java.lang.Boolean, java.lang.Boolean, java.lang.Boolean, java.lang.String, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	public void setDisplayOptions(Float zoom, Boolean redChannelOn,
	                              Boolean greenChannelOn, Boolean blueChannelOn,
	                              Boolean displayRGB, String colorMap,
	                              Integer zstart, Integer zstop, Integer tstart,
	                              Integer tstop, Integer imageNdx,
	                              Integer pixelsNdx, Integer redChannel,
	                              Integer greenChannel, Integer blueChannel,
	                              Integer grayChannel)
	{
		// TODO: Unsupported.
	}

	/* (non-Javadoc)
	 * @see loci.formats.MetadataStore#setEmissionFilter(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer)
	 */
	public void setEmissionFilter(String manufacturer, String model,
			                      String lotNumber, String type,
			                      Integer filterNdx)
	{
		// TODO: Unsupported.
	}

	/* (non-Javadoc)
	 * @see loci.formats.MetadataStore#setExcitationFilter(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.Integer)
	 */
	public void setExcitationFilter(String manufacturer, String model,
			                        String lotNumber, String type,
			                        Integer filterNdx)
	{
		// TODO: Unsupported.
	}

	/* (non-Javadoc)
	 * @see loci.formats.MetadataStore#setFilament(java.lang.String, java.lang.Float, java.lang.Integer, java.lang.Integer)
	 */
	public void setFilament(String type, Float power, Integer lightNdx,
			                Integer filamentNdx)
	{
		// TODO: Unsupported.
	}

	/* (non-Javadoc)
	 * @see loci.formats.MetadataStore#setFilterSet(java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, java.lang.Integer)
	 */
	public void setFilterSet(String manufacturer, String model,
			                 String lotNumber, Integer filterSetNdx,
			                 Integer filterNdx)
	{
		// TODO: Unsupported.
	}

	/* (non-Javadoc)
	 * @see loci.formats.MetadataStore#setImagingEnvironment(java.lang.Float, java.lang.Float, java.lang.Float, java.lang.Float, java.lang.Integer)
	 */
	public void setImagingEnvironment(Float temperature, Float airPressure,
			                          Float humidity, Float co2Percent,
			                          Integer i)
	{
		// TODO: Unsupported.
	}

	/* (non-Javadoc)
	 * @see loci.formats.MetadataStore#setLaser(java.lang.String, java.lang.String, java.lang.Integer, java.lang.Boolean, java.lang.Boolean, java.lang.String, java.lang.Float, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	public void setLaser(String type, String medium, Integer wavelength,
			             Boolean frequencyDoubled, Boolean tunable,
			             String pulse, Float power, Integer instrumentNdx,
			             Integer lightNdx, Integer pumpNdx, Integer laserNdx)
	{
		// TODO: Unsupported.
	}

	/* (non-Javadoc)
	 * @see loci.formats.MetadataStore#setLightSource(java.lang.String, java.lang.String, java.lang.String, java.lang.Integer, java.lang.Integer)
	 */
	public void setLightSource(String manufacturer, String model,
			                   String serialNumber, Integer instrumentIndex,
			                   Integer lightIndex)
	{
		// TODO: Unsupported.
	}

	/* (non-Javadoc)
	 * @see loci.formats.MetadataStore#setOTF(java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String, java.lang.Boolean, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	public void setOTF(Integer sizeX, Integer sizeY, String pixelType,
			           String path, Boolean opticalAxisAverage,
			           Integer instrumentNdx, Integer otfNdx, Integer filterNdx,
			           Integer objectiveNdx)
	{
		// TODO: Unsupported.
	}

	/* (non-Javadoc)
	 * @see loci.formats.MetadataStore#setObjective(java.lang.String, java.lang.String, java.lang.String, java.lang.Float, java.lang.Float, java.lang.Integer, java.lang.Integer)
	 */
	public void setObjective(String manufacturer, String model,
			                 String serialNumber, Float lensNA,
			                 Float magnification, Integer instrumentNdx,
			                 Integer objectiveNdx)
	{
		// TODO: Unsupported.
	}

	/**
     * Check the MinMax values stored in the DB and sync them with the new values
     * we generate in the channelMinMax reader, then save them to the DB. 
	 * @param id The <code>Pixels</code> id.
	 */
	@SuppressWarnings("unchecked")
	public void populateMinMax(Long id, Integer i)
    {
        Pixels p = iQuery.findByQuery(
                "select p from Pixels as p left join fetch p.channels " +
                "where p.id = :id", new Parameters().addId(id));
        List<Channel> channels = p.getChannels();
        List<Channel> readerChannels = getPixels(i).getChannels();
        
        for (int j=0; j < channels.size(); j++)
        {
            channels.get(j).setStatsInfo(readerChannels.get(j).getStatsInfo());
        }
        iUpdate.saveObject(p);
    }
    
    public IRepositoryInfo getRepositoryInfo()
    {
        return iInfo;
    }
    
    public long getRepositorySpace()
    {
        return iInfo.getFreeSpaceInKilobytes();
    }
    
    /**
     * Returns an Image from the internal indexed image list. The indexed image
     * list is extended as required and the Image object itself is created as 
     * required.
     * 
     * @param imageIndex The image index.
     * @return See above.
     */
    public Image getImage(int imageIndex)
    {
    	if (imageList.size() < (imageIndex + 1))
    	{
    		for (int i = imageList.size(); i < imageIndex; i++)
    		{
    			// Inserting null here so that we don't place potentially bogus
    			// Images into the list which will eventually be saved into
    			// the database.
    			imageList.add(null);
    		}
    		imageList.add(new Image());
    	}
    	
    	// We're going to check to see if the image list has a null value and
    	// update it as required.
    	Image i = imageList.get(imageIndex);
    	if (i == null)
    	{
    		i = new Image();
    		imageList.set(imageIndex, i);
    	}
    	return i;
    }

	/**
	 * Returns a Pixels from a given Image's indexed pixels list. The indexed
	 * pixels list is extended as required and the Pixels object itself is
	 * created as required.
	 * 
	 * @param imageIndex The image index.
	 * @param pixelsIndex The pixels index within <code>imageIndex</code>.
	 * @return See above.
	 */
	@SuppressWarnings("unchecked")
	public Pixels getPixels(int imageIndex, int pixelsIndex)
	{
		Image image = getImage(imageIndex);
		
    	if (image.sizeOfPixels() < (pixelsIndex + 1))
    	{
    		for (int i = image.sizeOfPixels(); i <= pixelsIndex; i++)
    		{
    			// Since OMERO model objects prevent us from inserting nulls
    			// here we must insert a Pixels object.
    			image.addPixels(new Pixels());
    		}
    	}
    	
    	Iterator<Pixels> i = image.iteratePixels();
    	int j = 0;
    	while (i.hasNext())
    	{
    		Pixels p = i.next();
    		if (j == pixelsIndex)
    		{
    			// Ensure that we have at least one default pixels
    			if (pixelsIndex == 0)
    			{
    				p.setDefaultPixels(Boolean.TRUE);
    			}
    			// This ensures that we can lookup the Pixels set at a later
    			// time based upon its "series" in Bio-Formats terms.
    			// FIXME: Note that there is no way to really ensure that
    			// "series" acurately maps to index in the List.
    			if (!pList.contains(p))
    			{
    				pList.add(p);
    			}
    			return p;
    		}
    		j++;
    	}
    	throw new RuntimeException(
    			"Unable to locate pixels index: " + pixelsIndex);
	}
	
	/**
	 * Returns a Pixels from the internal "series" indexed pixels list. FIXME: 
	 * Note that there is no way to really ensure that <code>series</code> 
	 * accurately maps to index in the List.
	 * @param series The Bio-Formats series to lookup.
	 * @return See above.
	 */
	@SuppressWarnings("unchecked")
	public Pixels getPixels(int series)
	{
		return pList.get(series);
	}
	
	/**
	 * Returns a PlaneInfo from a given Image's, Pixels' indexed plane info
	 * list. The indexed plane info list is extended as required and the 
	 * PlaneInfo object itself is created as required.
	 * 
	 * @param imageIndex The image index.
	 * @param pixelsIndex The pixels index within <code>imageIndex</code>.
	 * @param planeIndex The plane info index within <code>pixelsIndex</code>.
	 * @return See above.
	 */
	@SuppressWarnings("unchecked")
	public PlaneInfo getPlaneInfo(int imageIndex, int pixelsIndex,
			int planeIndex)
	{
		Pixels p = getPixels(imageIndex, pixelsIndex);
    	if (p.sizeOfPlaneInfo() < (planeIndex + 1))
    	{
    		for (int i = p.sizeOfPlaneInfo(); i <= planeIndex; i++)
    		{
    			// Since OMERO model objects prevent us from inserting nulls
    			// here we must insert a PlaneInfo object.
    			p.addPlaneInfo(new PlaneInfo());
    		}
    	}
    	
    	Iterator<PlaneInfo> i = p.iteratePlaneInfo();
    	int j = 0;
    	while (i.hasNext())
    	{
    		PlaneInfo info = i.next();
    		if (j == pixelsIndex)
    		{
    			return info;
    		}
    		j++;
    	}
    	throw new RuntimeException(
    			"Unable to locate plane info index: " + planeIndex);
	}
	
	/**
	 * Returns a Channel from a given Pixels' indexed channel list. The OMERO
	 * Channel is analogous to the OME-XML Schema ChannelComponent.
	 * 
	 * @param pixels The pixels object to retrieve from.
	 * @param channelIndex The logical channel index within 
	 * <code>pixels</code>.
	 * @return See above.
	 */
	@SuppressWarnings("unchecked")
	public Channel getChannel(Pixels pixels, int channelIndex)
	{
		List<Channel> channels = pixels.getChannels();
    	if (channels.size() < (channelIndex + 1))
    	{
    		for (int i = channels.size(); i <= channelIndex; i++)
    		{
    			// Since OMERO model objects prevent us from inserting nulls
    			// here we must insert a Channel object.
    			channels.add(new Channel());
    		}
    	}
    	return channels.get(channelIndex);
	}
	
	/**
	 * Returns a given Channel's LogicalChannel creating it if it does not
	 * exist.
	 * 
	 * @param channel The channel object to retrieve from.
	 * @return See above.
	 */
	public LogicalChannel getLogicalChannel(Channel channel)
	{
		LogicalChannel lc = channel.getLogicalChannel();
		if (lc == null)
		{
			lc = new LogicalChannel();
			channel.setLogicalChannel(lc);
		}
		return lc;
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setImageName(java.lang.String, int)
	 */
	public void setImageName(String name, int imageIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] name: '%s'", imageIndex, name));
        Image i = getImage(imageIndex);
		i.setName(name);
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setImageCreationDate(java.lang.String, int)
	 */
	public void setImageCreationDate(String creationDate, int imageIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] creation date: '%s'", imageIndex, creationDate));
        log.debug("FIXME: Creation date is ignored.");
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setImageDescription(java.lang.String, int)
	 */
	public void setImageDescription(String description, int imageIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] description: '%s'", imageIndex, description));
        Image i = getImage(imageIndex);
        i.setDescription(description);
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setPixelsSizeX(java.lang.Integer, int, int)
	 */
	public void setPixelsSizeX(Integer sizeX, int imageIndex, int pixelsIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] sizeX: '%d'",
        		imageIndex, pixelsIndex, sizeX));
		Pixels p = getPixels(imageIndex, pixelsIndex);
		p.setSizeX(sizeX);
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setPixelsSizeY(java.lang.Integer, int, int)
	 */
	public void setPixelsSizeY(Integer sizeY, int imageIndex, int pixelsIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] sizeY: '%d'",
        		imageIndex, pixelsIndex, sizeY));
		Pixels p = getPixels(imageIndex, pixelsIndex);
		p.setSizeY(sizeY);
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setPixelsSizeZ(java.lang.Integer, int, int)
	 */
	public void setPixelsSizeZ(Integer sizeZ, int imageIndex, int pixelsIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] sizeZ: '%d'",
        		imageIndex, pixelsIndex, sizeZ));
		Pixels p = getPixels(imageIndex, pixelsIndex);
		p.setSizeZ(sizeZ);
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setPixelsSizeC(java.lang.Integer, int, int)
	 */
	public void setPixelsSizeC(Integer sizeC, int imageIndex, int pixelsIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] sizeC: '%d'",
        		imageIndex, pixelsIndex, sizeC));
		Pixels p = getPixels(imageIndex, pixelsIndex);
		p.setSizeC(sizeC);
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setPixelsSizeT(java.lang.Integer, int, int)
	 */
	public void setPixelsSizeT(Integer sizeT, int imageIndex, int pixelsIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] sizeT: '%d'",
        		imageIndex, pixelsIndex, sizeT));
		Pixels p = getPixels(imageIndex, pixelsIndex);
		p.setSizeT(sizeT);
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setPixelsPixelType(java.lang.String, int, int)
	 */
	public void setPixelsPixelType(String pixelType, int imageIndex,
			int pixelsIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] pixel type: '%s'",
        		imageIndex, pixelsIndex, pixelType));
        
        // Retrieve enumerations from the server               
        PixelsType type =
        	(PixelsType) getEnumeration(PixelsType.class, pixelType);
        
        Pixels p = getPixels(imageIndex, pixelsIndex);
        p.setPixelsType(type);
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setPixelsBigEndian(java.lang.Boolean, int, int)
	 */
	public void setPixelsBigEndian(Boolean bigEndian, int imageIndex,
			int pixelsIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] big-endian?: '%s'",
        		imageIndex, pixelsIndex, bigEndian));
        log.debug("NOTE: This field is unsupported/unused.");
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setPixelsDimensionOrder(java.lang.String, int, int)
	 */
	public void setPixelsDimensionOrder(String dimensionOrder, int imageIndex,
			int pixelsIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] dimension order: '%s'",
        		imageIndex, pixelsIndex, dimensionOrder));
        DimensionOrder order =
        	(DimensionOrder) getEnumeration(DimensionOrder.class, dimensionOrder);
        Pixels p = getPixels(imageIndex, pixelsIndex);
        p.setDimensionOrder(order);
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setDimensionsPhysicalSizeX(java.lang.Float, int, int)
	 */
	public void setDimensionsPhysicalSizeX(Float physicalSizeX, int imageIndex,
			int pixelsIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] physical size X: '%f'",
        		imageIndex, pixelsIndex, physicalSizeX));
        Pixels p = getPixels(imageIndex, pixelsIndex);
        PixelsDimensions dims = p.getPixelsDimensions();
        if (dims == null)
        {
        	dims = new PixelsDimensions();
        	p.setPixelsDimensions(dims);
        }
        dims.setSizeX(physicalSizeX);
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setDimensionsPhysicalSizeY(java.lang.Float, int, int)
	 */
	public void setDimensionsPhysicalSizeY(Float physicalSizeY, int imageIndex,
			int pixelsIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] physical size Y: '%f'",
        		imageIndex, pixelsIndex, physicalSizeY));
        Pixels p = getPixels(imageIndex, pixelsIndex);
        PixelsDimensions dims = p.getPixelsDimensions();
        if (dims == null)
        {
        	dims = new PixelsDimensions();
        	p.setPixelsDimensions(dims);
        }
        dims.setSizeY(physicalSizeY);
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setDimensionsPhysicalSizeZ(java.lang.Float, int, int)
	 */
	public void setDimensionsPhysicalSizeZ(Float physicalSizeZ, int imageIndex,
			int pixelsIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] physical size Z: '%f'",
        		imageIndex, pixelsIndex, physicalSizeZ));
        Pixels p = getPixels(imageIndex, pixelsIndex);
        PixelsDimensions dims = p.getPixelsDimensions();
        if (dims == null)
        {
        	dims = new PixelsDimensions();
        	p.setPixelsDimensions(dims);
        }
        dims.setSizeZ(physicalSizeZ);
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setDimensionsTimeIncrement(java.lang.Float, int, int)
	 */
	public void setDimensionsTimeIncrement(Float timeIncrement, int imageIndex,
			int pixelsIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] time increment: '%f'",
        		imageIndex, pixelsIndex, timeIncrement));
        log.debug("NOTE: This field is unsupported/unused.");
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setDimensionsWaveIncrement(java.lang.Integer, int, int)
	 */
	public void setDimensionsWaveIncrement(Integer waveIncrement,
			int imageIndex, int pixelsIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] wave increment: '%f'",
        		imageIndex, pixelsIndex, waveIncrement));
        log.debug("NOTE: This field is unsupported/unused.");
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setDimensionsWaveStart(java.lang.Integer, int, int)
	 */
	public void setDimensionsWaveStart(Integer waveStart, int imageIndex,
			int pixelsIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] wave start: '%f'",
        		imageIndex, pixelsIndex, waveStart));
        log.debug("NOTE: This field is unsupported/unused.");
	}

	public void setImagingEnvironmentTemperature(Float temperature,
			int imageIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setImagingEnvironmentAirPressure(Float airPressure,
			int imageIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setImagingEnvironmentHumidity(Float humidity, int imageIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setImagingEnvironmentCO2Percent(Float percent, int imageIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setPlaneTheZ(java.lang.Integer, int, int, int)
	 */
	public void setPlaneTheZ(Integer theZ, int imageIndex, int pixelsIndex,
			int planeIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] PlaneInfo[%d] theZ: '%d'",
        		imageIndex, pixelsIndex, planeIndex, theZ));
        PlaneInfo p = getPlaneInfo(imageIndex, pixelsIndex, planeIndex);
        p.setTheZ(theZ);
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setPlaneTheC(java.lang.Integer, int, int, int)
	 */
	public void setPlaneTheC(Integer theC, int imageIndex, int pixelsIndex,
			int planeIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] PlaneInfo[%d] theC: '%d'",
        		imageIndex, pixelsIndex, planeIndex, theC));
        PlaneInfo p = getPlaneInfo(imageIndex, pixelsIndex, planeIndex);
        p.setTheC(theC);
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setPlaneTheT(java.lang.Integer, int, int, int)
	 */
	public void setPlaneTheT(Integer theT, int imageIndex, int pixelsIndex,
			int planeIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] PlaneInfo[%d] theT: '%d'",
        		imageIndex, pixelsIndex, planeIndex, theT));
        PlaneInfo p = getPlaneInfo(imageIndex, pixelsIndex, planeIndex);
        p.setTheT(theT);
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setPlaneTimingDeltaT(java.lang.Float, int, int, int)
	 */
	public void setPlaneTimingDeltaT(Float deltaT, int imageIndex,
			int pixelsIndex, int planeIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] PlaneInfo[%d] deltaT: '%f'",
        		imageIndex, pixelsIndex, planeIndex, deltaT));
        PlaneInfo p = getPlaneInfo(imageIndex, pixelsIndex, planeIndex);
        p.setTimestamp(deltaT);
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setPlaneTimingExposureTime(java.lang.Float, int, int, int)
	 */
	public void setPlaneTimingExposureTime(Float exposureTime, int imageIndex,
			int pixelsIndex, int planeIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] PlaneInfo[%d] exposure time: '%f'",
        		imageIndex, pixelsIndex, planeIndex, exposureTime));
        PlaneInfo p = getPlaneInfo(imageIndex, pixelsIndex, planeIndex);
        p.setExposureTime(exposureTime);
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setStagePositionPositionX(java.lang.Float, int, int, int)
	 */
	public void setStagePositionPositionX(Float positionX, int imageIndex,
			int pixelsIndex, int planeIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] PlaneInfo[%d] position X: '%f'",
        		imageIndex, pixelsIndex, planeIndex, positionX));
        PlaneInfo p = getPlaneInfo(imageIndex, pixelsIndex, planeIndex);
        p.setPositionX(positionX);
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setStagePositionPositionY(java.lang.Float, int, int, int)
	 */
	public void setStagePositionPositionY(Float positionY, int imageIndex,
			int pixelsIndex, int planeIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] PlaneInfo[%d] position Y: '%f'",
        		imageIndex, pixelsIndex, planeIndex, positionY));
        PlaneInfo p = getPlaneInfo(imageIndex, pixelsIndex, planeIndex);
        p.setPositionY(positionY);
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setStagePositionPositionZ(java.lang.Float, int, int, int)
	 */
	public void setStagePositionPositionZ(Float positionZ, int imageIndex,
			int pixelsIndex, int planeIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] PlaneInfo[%d] position Z: '%f'",
        		imageIndex, pixelsIndex, planeIndex, positionZ));
        PlaneInfo p = getPlaneInfo(imageIndex, pixelsIndex, planeIndex);
        p.setPositionZ(positionZ);
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setLogicalChannelName(java.lang.String, int, int)
	 */
	@SuppressWarnings("unchecked")
	public void setLogicalChannelName(String name, int imageIndex,
			int logicalChannelIndex)
	{
        Image image = getImage(imageIndex);
        Iterator<Pixels> i = image.iteratePixels();
        while (i.hasNext())
        {
        	Pixels p = i.next();
        	Channel c = getChannel(p, logicalChannelIndex);
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] Channel[%s] LogicalChannel[%d] name: '%s'",
            		imageIndex, p, logicalChannelIndex, name));
            LogicalChannel lc = getLogicalChannel(c);
            lc.setName(name);
        }
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setLogicalChannelSamplesPerPixel(java.lang.Integer, int, int)
	 */
	@SuppressWarnings("unchecked")
	public void setLogicalChannelSamplesPerPixel(Integer samplesPerPixel,
			int imageIndex, int logicalChannelIndex)
	{
        Image image = getImage(imageIndex);
        Iterator<Pixels> i = image.iteratePixels();
        while (i.hasNext())
        {
        	Pixels p = i.next();
        	Channel c = getChannel(p, logicalChannelIndex);
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] Channel[%s] LogicalChannel[%d] samples per pixel: '%d'",
            		imageIndex, p, logicalChannelIndex, samplesPerPixel));
            log.debug("NOTE: This field is unsupported/unused.");
        }
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setLogicalChannelIlluminationType(java.lang.String, int, int)
	 */
	@SuppressWarnings("unchecked")
	public void setLogicalChannelIlluminationType(String illuminationType,
			int imageIndex, int logicalChannelIndex)
	{
        Image image = getImage(imageIndex);
        Iterator<Pixels> i = image.iteratePixels();
        while (i.hasNext())
        {
        	Pixels p = i.next();
        	Channel c = getChannel(p, logicalChannelIndex);
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] Channel[%s] LogicalChannel[%d] illumination type: '%s'",
            		imageIndex, p, logicalChannelIndex, illuminationType));
            LogicalChannel lc = getLogicalChannel(c);
            Illumination iType = (Illumination) getEnumeration(
                    AcquisitionMode.class, illuminationType);
            lc.setIllumination(iType);
        }
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setLogicalChannelPinholeSize(java.lang.Integer, int, int)
	 */
	@SuppressWarnings("unchecked")
	public void setLogicalChannelPinholeSize(Integer pinholeSize,
			int imageIndex, int logicalChannelIndex)
	{
        Image image = getImage(imageIndex);
        Iterator<Pixels> i = image.iteratePixels();
        while (i.hasNext())
        {
        	Pixels p = i.next();
        	Channel c = getChannel(p, logicalChannelIndex);
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] Channel[%s] LogicalChannel[%d] pinhole size: '%d'",
            		imageIndex, p, logicalChannelIndex, pinholeSize));
            LogicalChannel lc = getLogicalChannel(c);
            lc.setPinHoleSize(pinholeSize);
        }
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setLogicalChannelPhotometricInterpretation(java.lang.String, int, int)
	 */
	@SuppressWarnings("unchecked")
	public void setLogicalChannelPhotometricInterpretation(
			String photometricInterpretation, int imageIndex,
			int logicalChannelIndex)
	{
        Image image = getImage(imageIndex);
        Iterator<Pixels> i = image.iteratePixels();
        while (i.hasNext())
        {
        	Pixels p = i.next();
        	Channel c = getChannel(p, logicalChannelIndex);
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] Channel[%s] LogicalChannel[%d] " +
            		"photometric interpretation: '%s'",
            		imageIndex, p, logicalChannelIndex, photometricInterpretation));
            LogicalChannel lc = getLogicalChannel(c);
            PhotometricInterpretation pi = 
            	(PhotometricInterpretation) getEnumeration(
                    PhotometricInterpretation.class, photometricInterpretation);
            lc.setPhotometricInterpretation(pi);
        }
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setLogicalChannelMode(java.lang.String, int, int)
	 */
	@SuppressWarnings("unchecked")
	public void setLogicalChannelMode(String mode, int imageIndex,
			int logicalChannelIndex)
	{
        Image image = getImage(imageIndex);
        Iterator<Pixels> i = image.iteratePixels();
        while (i.hasNext())
        {
        	Pixels p = i.next();
        	Channel c = getChannel(p, logicalChannelIndex);
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] Channel[%s] LogicalChannel[%d] " +
            		"channel mode: '%s'",
            		imageIndex, p, logicalChannelIndex, mode));
            LogicalChannel lc = getLogicalChannel(c);
            AcquisitionMode m = 
            	(AcquisitionMode) getEnumeration(AcquisitionMode.class, mode);
            lc.setMode(m);
        }
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setLogicalChannelContrastMethod(java.lang.String, int, int)
	 */
	@SuppressWarnings("unchecked")
	public void setLogicalChannelContrastMethod(String contrastMethod,
			int imageIndex, int logicalChannelIndex)
	{
        Image image = getImage(imageIndex);
        Iterator<Pixels> i = image.iteratePixels();
        while (i.hasNext())
        {
        	Pixels p = i.next();
        	Channel c = getChannel(p, logicalChannelIndex);
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] Channel[%s] LogicalChannel[%d] " +
            		"contrast method: '%s'",
            		imageIndex, p, logicalChannelIndex, contrastMethod));
            LogicalChannel lc = getLogicalChannel(c);
            ContrastMethod m = (ContrastMethod) 
            	getEnumeration(ContrastMethod.class, contrastMethod);
            lc.setContrastMethod(m);
        }
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setLogicalChannelExWave(java.lang.Integer, int, int)
	 */
	@SuppressWarnings("unchecked")
	public void setLogicalChannelExWave(Integer exWave, int imageIndex,
			int logicalChannelIndex)
	{
        Image image = getImage(imageIndex);
        Iterator<Pixels> i = image.iteratePixels();
        while (i.hasNext())
        {
        	Pixels p = i.next();
        	Channel c = getChannel(p, logicalChannelIndex);
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] Channel[%s] LogicalChannel[%d] " +
            		"excitation wavelength: '%d'",
            		imageIndex, p, logicalChannelIndex, exWave));
            LogicalChannel lc = getLogicalChannel(c);
            lc.setExcitationWave(exWave);
        }
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setLogicalChannelEmWave(java.lang.Integer, int, int)
	 */
	@SuppressWarnings("unchecked")
	public void setLogicalChannelEmWave(Integer emWave, int imageIndex,
			int logicalChannelIndex)
	{
        Image image = getImage(imageIndex);
        Iterator<Pixels> i = image.iteratePixels();
        while (i.hasNext())
        {
        	Pixels p = i.next();
        	Channel c = getChannel(p, logicalChannelIndex);
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] Channel[%s] LogicalChannel[%d] " +
            		"emission wavelength: '%d'",
            		imageIndex, p, logicalChannelIndex, emWave));
            LogicalChannel lc = getLogicalChannel(c);
            lc.setEmissionWave(emWave);
        }
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setLogicalChannelFluor(java.lang.String, int, int)
	 */
	@SuppressWarnings("unchecked")
	public void setLogicalChannelFluor(String fluor, int imageIndex,
			int logicalChannelIndex)
	{
        Image image = getImage(imageIndex);
        Iterator<Pixels> i = image.iteratePixels();
        while (i.hasNext())
        {
        	Pixels p = i.next();
        	Channel c = getChannel(p, logicalChannelIndex);
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] Channel[%s] LogicalChannel[%d] " +
            		"fluor: '%s'",
            		imageIndex, p, logicalChannelIndex, fluor));
            LogicalChannel lc = getLogicalChannel(c);
            lc.setFluor(fluor);
        }
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setLogicalChannelNdFilter(java.lang.Float, int, int)
	 */
	@SuppressWarnings("unchecked")
	public void setLogicalChannelNdFilter(Float ndFilter, int imageIndex,
			int logicalChannelIndex)
	{
        Image image = getImage(imageIndex);
        Iterator<Pixels> i = image.iteratePixels();
        while (i.hasNext())
        {
        	Pixels p = i.next();
        	Channel c = getChannel(p, logicalChannelIndex);
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] Channel[%s] LogicalChannel[%d] " +
            		"ndFilter: '%d'",
            		imageIndex, p, logicalChannelIndex, ndFilter));
            LogicalChannel lc = getLogicalChannel(c);
            lc.setNdFilter(ndFilter);
        }
	}

	/* (non-Javadoc)
	 * @see loci.formats.meta.MetadataStore#setLogicalChannelPockelCellSetting(java.lang.Integer, int, int)
	 */
	@SuppressWarnings("unchecked")
	public void setLogicalChannelPockelCellSetting(Integer pockelCellSetting,
			int imageIndex, int logicalChannelIndex)
	{
        Image image = getImage(imageIndex);
        Iterator<Pixels> i = image.iteratePixels();
        while (i.hasNext())
        {
        	Pixels p = i.next();
        	Channel c = getChannel(p, logicalChannelIndex);
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] Channel[%s] LogicalChannel[%d] " +
            		"pockel cell setting: '%d'",
            		imageIndex, p, logicalChannelIndex, pockelCellSetting));
            LogicalChannel lc = getLogicalChannel(c);
            lc.setPockelCellSetting(pockelCellSetting.toString());
            // FIXME: Should pockel cell be String or Integer?
        }
	}

	public void setArcPower(Float power, int instrumentIndex,
			int lightSourceIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setArcType(String type, int instrumentIndex,
			int lightSourceIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setDetectorGain(Float gain, int instrumentIndex,
			int detectorIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setDetectorManufacturer(String manufacturer,
			int instrumentIndex, int detectorIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setDetectorModel(String model, int instrumentIndex,
			int detectorIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setDetectorOffset(Float offset, int instrumentIndex,
			int detectorIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setDetectorSerialNumber(String serialNumber,
			int instrumentIndex, int detectorIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setDetectorSettingsGain(Float gain, int imageIndex,
			int logicalChannelIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setDetectorSettingsOffset(Float offset, int imageIndex,
			int logicalChannelIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setDetectorType(String type, int instrumentIndex,
			int detectorIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setDetectorVoltage(Float voltage, int instrumentIndex,
			int detectorIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setExperimenterDataDirectory(String dataDirectory,
			int experimenterIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setExperimenterEmail(String email, int experimenterIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setExperimenterFirstName(String firstName, int experimenterIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setExperimenterInstitution(String institution,
			int experimenterIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setExperimenterLastName(String lastName, int experimenterIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setFilamentPower(Float power, int instrumentIndex,
			int lightSourceIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setFilamentType(String type, int instrumentIndex,
			int lightSourceIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setLaserFrequencyMultiplication(
			Integer frequencyMultiplication, int instrumentIndex,
			int lightSourceIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setLaserLaserMedium(String laserMedium, int instrumentIndex,
			int lightSourceIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setLaserPower(Float power, int instrumentIndex,
			int lightSourceIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setLaserPulse(String pulse, int instrumentIndex,
			int lightSourceIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setLaserTuneable(Boolean tuneable, int instrumentIndex,
			int lightSourceIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setLaserType(String type, int instrumentIndex,
			int lightSourceIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setLaserWavelength(Integer wavelength, int instrumentIndex,
			int lightSourceIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setLightSourceManufacturer(String manufacturer,
			int instrumentIndex, int lightSourceIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setLightSourceModel(String model, int instrumentIndex,
			int lightSourceIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setLightSourceSerialNumber(String serialNumber,
			int instrumentIndex, int lightSourceIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setLightSourceSettingsAttenuation(Float attenuation,
			int imageIndex, int logicalChannelIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setLightSourceSettingsWavelength(Integer wavelength,
			int imageIndex, int logicalChannelIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setOTFOpticalAxisAveraged(Boolean opticalAxisAveraged,
			int instrumentIndex, int otfIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setOTFPath(String path, int instrumentIndex, int otfIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setOTFPixelType(String pixelType, int instrumentIndex,
			int otfIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setOTFSizeX(Integer sizeX, int instrumentIndex, int otfIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setOTFSizeY(Integer sizeY, int instrumentIndex, int otfIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setObjectiveCalibratedMagnification(
			Float calibratedMagnification, int instrumentIndex,
			int objectiveIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setObjectiveCorrection(String correction, int instrumentIndex,
			int objectiveIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setObjectiveImmersion(String immersion, int instrumentIndex,
			int objectiveIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setObjectiveLensNA(Float lensNA, int instrumentIndex,
			int objectiveIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setObjectiveManufacturer(String manufacturer,
			int instrumentIndex, int objectiveIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setObjectiveModel(String model, int instrumentIndex,
			int objectiveIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setObjectiveNominalMagnification(Integer nominalMagnification,
			int instrumentIndex, int objectiveIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setObjectiveSerialNumber(String serialNumber,
			int instrumentIndex, int objectiveIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setObjectiveWorkingDistance(Float workingDistance,
			int instrumentIndex, int objectiveIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setROIT0(Integer t0, int imageIndex, int roiIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setROIT1(Integer t1, int imageIndex, int roiIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setROIX0(Integer x0, int imageIndex, int roiIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setROIX1(Integer x1, int imageIndex, int roiIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setROIY0(Integer y0, int imageIndex, int roiIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setROIY1(Integer y1, int imageIndex, int roiIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setROIZ0(Integer z0, int imageIndex, int roiIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setROIZ1(Integer z1, int imageIndex, int roiIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setStageLabelName(String name, int imageIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setStageLabelX(Float x, int imageIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setStageLabelY(Float y, int imageIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}

	public void setStageLabelZ(Float z, int imageIndex) {
		throw new RuntimeException("Un-implemented.");
		
	}
}
