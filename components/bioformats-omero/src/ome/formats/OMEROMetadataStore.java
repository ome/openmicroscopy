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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import loci.formats.meta.MetadataStore;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.api.RawPixelsStore;
import ome.model.IObject;
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
 * be logged into OMERO prior to use with the {@link #login()} method. While
 * attempts have been made to allow the caller to switch back and forth between 
 * Images and Pixels during metadata population it is <b>strongly</b> 
 * encouraged that at least Images and Pixels are populated in ascending order. 
 * For example: Image_1 --> Pixels_1, Pixels_2 followed by Image_2 --> Pixels_1,
 * Pixels2, Pixels_3.
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
    private List<Pixels> pixelsList = new ArrayList<Pixels>();
    
    /** 
     * PlaneInfo ordered cache which compensates for pixels.planeInfo being a
     * HashMap.
     */
    private Map<Pixels, List<PlaneInfo>> planeInfoCache = null;
    
    private Experimenter    exp;
    
    private RawFileStore    rawFileStore;
    
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
	    pixelsList = new ArrayList<Pixels>();
	    planeInfoCache = new HashMap<Pixels, List<PlaneInfo>>();
	}

	/*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setRoot(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
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
    
    public long getExperimenterID()
    {
        return exp.getId();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setChannelGlobalMinMax(int,
     *      java.lang.Double, java.lang.Double, java.lang.Integer)
     */
    @SuppressWarnings("unchecked")
	public void setChannelGlobalMinMax(int channelIdx, Double globalMin,
            Double globalMax, Integer pixelsIndex)
    {
        log.debug(String.format(
                "Setting Pixels[%d] Channel[%d] globalMin: '%f' globalMax: '%f'",
                pixelsIndex, channelIdx, globalMin, globalMax));
        if (globalMin != null)
        {
        	globalMin = new Double(Math.floor(globalMin.doubleValue()));
        }
        if (globalMax != null)
        {
        	globalMax = new Double(Math.ceil(globalMax.doubleValue()));
        }

        List<Channel> channels = getPixels(pixelsIndex).getChannels();
        StatsInfo statsInfo = new StatsInfo();
        statsInfo.setGlobalMin(globalMin);
        statsInfo.setGlobalMax(globalMax);
        channels.get(channelIdx).setStatsInfo(statsInfo);
    }

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
     * Adds an image to a dataset.
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
        pixelsList = new ArrayList<Pixels>();
        for (int i = 0; i < o.length; i++)
        {
            Image image = (Image) o[i];
            // FIXME: This assumes only *one* pixels set.
            pixelsList.add((Pixels) image.iteratePixels().next());
            imageList.set(i, image);
        }
        return pixelsList;
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
            for (Pixels pixels : pixelsList)
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
        for (int j=0; j < p.getSizeC(); j++)
        {
            Channel channel = channels.get(j);
            Channel readerChannel = readerChannels.get(j);
            channel.setStatsInfo(readerChannel.getStatsInfo());
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
	 * created as required. This also invalidates the PlaneInfo ordered cache
	 * if the pixelsIndex is different than the one currently stored. You 
	 * <b>must not</b> attempt to retrieve two different Pixels instances and 
	 * expect to have planeIndexes maintained.
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
    			// here we must insert a Pixels object. We also need to ensure
    			// that the OMERO specific "sha1" field is filled.
    			Pixels p = new Pixels();
    			// FIXME: We *really* should deal with this properly... finally.
    			p.setSha1("foo");
    			image.addPixels(p);
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
    			if (!pixelsList.contains(p))
    			{
    				pixelsList.add(p);
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
		return pixelsList.get(series);
	}
	
	/**
	 * Returns a PlaneInfo from a given Image's, Pixels' indexed  plane info
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
		Pixels pixels = getPixels(imageIndex, pixelsIndex);
        if (!planeInfoCache.containsKey(pixels))
        {
            planeInfoCache.put(pixels, new ArrayList<PlaneInfo>());
        }
        
        List<PlaneInfo> cache = planeInfoCache.get(pixels);
    	if (cache.size() < (planeIndex + 1))
    	{
    		for (int i = cache.size(); i <= planeIndex; i++)
    		{
    			// Since OMERO model objects prevent us from inserting nulls
    			// here we must insert a PlaneInfo object. Also, we need an
    			// ordered list of PlaneInfo objects for later reference so
    			// we're populating the cache here which will be invalidated
    			// upon a call to createRoot().
    			PlaneInfo info = new PlaneInfo();
                // FIXME: Time stamp needs fixing.
    			info.setTimestamp(0.0f);
    			cache.add(info);
    			pixels.addPlaneInfo(info);
    		}
    	}
    	return cache.get(planeIndex);
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
	@SuppressWarnings("unchecked")
	public void setPixelsSizeC(Integer sizeC, int imageIndex, int pixelsIndex)
	{
        log.debug(String.format(
        		"Setting Image[%d] Pixels[%d] sizeC: '%d'",
        		imageIndex, pixelsIndex, sizeC));
        Pixels p = getPixels(imageIndex, pixelsIndex);
        p.setSizeC(sizeC);
        List<Channel> channels = p.getChannels();
        if (channels.size() != 0)
        {
            channels.clear();
        }
        for (int i = 0; i < sizeC; i++)
        {
            Channel c = new Channel();
            c.setLogicalChannel(new LogicalChannel());
            channels.add(c);
        }
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
	    if (physicalSizeX == null || physicalSizeX <= 0.000001)
	    {
	        log.warn("physicalSizeX is <= 0.000001f, setting to 1.0f");
	        physicalSizeX = 1.0f;
	    } else {
	        log.debug(String.format(
	                "Setting Image[%d] Pixels[%d] physical size X: '%f'",
	                imageIndex, pixelsIndex, physicalSizeX));
	    }
        //if (physicalSizeX == null) return;
        Pixels p = getPixels(imageIndex, pixelsIndex);
        PixelsDimensions dims = p.getPixelsDimensions();
        if (dims == null)
        {
        	dims = new PixelsDimensions();
        	dims.setSizeX(0.0f);
            dims.setSizeY(0.0f);
            dims.setSizeZ(0.0f);
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
	    if (physicalSizeY == null || physicalSizeY <= 0.000001)
	    {
	        log.warn("physicalSizeY is <= 0.000001f, setting to 1.0f");
	        physicalSizeY = 1.0f;
	    } else {
	        log.debug(String.format(
	                "Setting Image[%d] Pixels[%d] physical size Y: '%f'",
	                imageIndex, pixelsIndex, physicalSizeY));
	    }
	    //if (physicalSizeY == null) return;
        Pixels p = getPixels(imageIndex, pixelsIndex);
        PixelsDimensions dims = p.getPixelsDimensions();
        if (dims == null)
        {
        	dims = new PixelsDimensions();
            dims.setSizeX(0.0f);
            dims.setSizeY(0.0f);
            dims.setSizeZ(0.0f);
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
	    if (physicalSizeZ == null || physicalSizeZ <= 0.000001)
	    {
	        log.warn("physicalSizeZ is <= 0.000001f, setting to 1.0f");
	        physicalSizeZ = 1.0f;
	    } else {
	        log.debug(String.format(
	                "Setting Image[%d] Pixels[%d] physical size Z: '%f'",
	                imageIndex, pixelsIndex, physicalSizeZ));
	    }
	    if (physicalSizeZ == null) return;
        Pixels p = getPixels(imageIndex, pixelsIndex);
        PixelsDimensions dims = p.getPixelsDimensions();
        if (dims == null)
        {
        	dims = new PixelsDimensions();
            dims.setSizeX(0.0f);
            dims.setSizeY(0.0f);
            dims.setSizeZ(0.0f);
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
        		"Setting Image[%d] Pixels[%d] wave increment: '%d'",
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
        		"Setting Image[%d] Pixels[%d] wave start: '%d'",
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
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] LogicalChannel[%d] name: '%s'",
            		imageIndex, p, logicalChannelIndex, name));
            List<Channel> channels = p.getChannels();
            LogicalChannel lc = 
            	channels.get(logicalChannelIndex).getLogicalChannel();
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
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] LogicalChannel[%d] samples per pixel: '%d'",
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
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] LogicalChannel[%d] illumination type: '%s'",
            		imageIndex, p, logicalChannelIndex, illuminationType));
            List<Channel> channels = p.getChannels();
            LogicalChannel lc = 
            	channels.get(logicalChannelIndex).getLogicalChannel();
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
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] LogicalChannel[%d] pinhole size: '%d'",
            		imageIndex, p, logicalChannelIndex, pinholeSize));
            List<Channel> channels = p.getChannels();
            LogicalChannel lc = 
            	channels.get(logicalChannelIndex).getLogicalChannel();
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
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] LogicalChannel[%d] " +
            		"photometric interpretation: '%s'",
            		imageIndex, p, logicalChannelIndex, photometricInterpretation));
            List<Channel> channels = p.getChannels();
            LogicalChannel lc = 
            	channels.get(logicalChannelIndex).getLogicalChannel();
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
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] LogicalChannel[%d] " +
            		"channel mode: '%s'",
            		imageIndex, p, logicalChannelIndex, mode));
            List<Channel> channels = p.getChannels();
            LogicalChannel lc = 
            	channels.get(logicalChannelIndex).getLogicalChannel();
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
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] LogicalChannel[%d] " +
            		"contrast method: '%s'",
            		imageIndex, p, logicalChannelIndex, contrastMethod));
            List<Channel> channels = p.getChannels();
            LogicalChannel lc = 
            	channels.get(logicalChannelIndex).getLogicalChannel();
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
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] LogicalChannel[%d] " +
            		"excitation wavelength: '%d'",
            		imageIndex, p, logicalChannelIndex, exWave));
            List<Channel> channels = p.getChannels();
            LogicalChannel lc = 
            	channels.get(logicalChannelIndex).getLogicalChannel();
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
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] LogicalChannel[%d] " +
            		"emission wavelength: '%d'",
            		imageIndex, p, logicalChannelIndex, emWave));
            List<Channel> channels = p.getChannels();
            LogicalChannel lc = 
            	channels.get(logicalChannelIndex).getLogicalChannel();
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
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] LogicalChannel[%d] " +
            		"fluor: '%s'",
            		imageIndex, p, logicalChannelIndex, fluor));
            List<Channel> channels = p.getChannels();
            LogicalChannel lc = 
            	channels.get(logicalChannelIndex).getLogicalChannel();
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
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] LogicalChannel[%d] " +
            		"ndFilter: '%f'",
            		imageIndex, p, logicalChannelIndex, ndFilter));
            List<Channel> channels = p.getChannels();
            LogicalChannel lc = 
            	channels.get(logicalChannelIndex).getLogicalChannel();
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
            log.debug(String.format(
            		"Setting Image[%d] Pixels[%s] LogicalChannel[%d] " +
            		"pockel cell setting: '%d'",
            		imageIndex, p, logicalChannelIndex, pockelCellSetting));
            List<Channel> channels = p.getChannels();
            LogicalChannel lc = 
            	channels.get(logicalChannelIndex).getLogicalChannel();
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
        log.debug(String.format(
                "Admin only function: Ignoring dataDirectory[%s] experimenterIndex[%d] ",
                dataDirectory, experimenterIndex)); 
	}

	public void setExperimenterEmail(String email, int experimenterIndex) {
        log.debug(String.format(
                "Admin only function: Ignoring email[%s] experimenterIndex[%d] ",
                email, experimenterIndex)); 
	}

	public void setExperimenterFirstName(String firstName, int experimenterIndex) {
        log.debug(String.format(
                "Admin only function: Ignoring firstName[%s] experimenterIndex[%d] ",
                firstName, experimenterIndex));	
	}

	public void setExperimenterInstitution(String institution,
			int experimenterIndex) {
	    log.debug(String.format(
                "Admin only function: Ignoring institution[%s] experimenterIndex[%d] ",
                institution, experimenterIndex));
	}

	public void setExperimenterLastName(String lastName, int experimenterIndex) {
        log.debug(String.format(
                "Admin only function: Ignoring lastName[%s] experimenterIndex[%d] ",
                lastName, experimenterIndex)); 
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
        log.debug(String.format(
                "FIXME: Ignoring laserMedium[%s] instrumentIndex[%d] lightsourceMedium[%d] ",
                laserMedium, instrumentIndex, lightSourceIndex));
        // FIXME: Needs to be implemented when the model is relaxed.	
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
        log.debug(String.format(
                "FIXME: Ignoring type[%s] instrumentIndex[%d] lightsourceMedium[%d] ",
        type, instrumentIndex, lightSourceIndex));
        // FIXME: Needs to be implemented when the model is relaxed.
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
        log.debug(String.format(
                "FIXME: Ignoring model[%s] instrumentIndex[%d] objectiveIndex[%d] ",
                model, instrumentIndex, objectiveIndex));
        // FIXME: Needs to be implemented when the model is relaxed.
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

    public void setDetectorSettingsDetector(Object detector, int imageIndex,
            int logicalChannelIndex)
    {
        throw new RuntimeException("Un-implemented.");
    }

    public void setLightSourceSettingsLightSource(Object lightSource,
            int imageIndex, int logicalChannelIndex)
    {
        throw new RuntimeException("Un-implemented.");
    }
}
