/*
 * ome.formats.OMEROMetadataStore
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
 */

package ome.formats;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import loci.formats.meta.IMinMaxStore;
import loci.formats.meta.MetadataStore;
import ome.api.IQuery;
import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.api.RawPixelsStore;
import ome.model.IEnum;
import ome.model.IObject;
import ome.model.acquisition.Arc;
import ome.model.acquisition.Detector;
import ome.model.acquisition.DetectorSettings;
import ome.model.acquisition.Filament;
import ome.model.acquisition.ImagingEnvironment;
import ome.model.acquisition.Instrument;
import ome.model.acquisition.Laser;
import ome.model.acquisition.LightSettings;
import ome.model.acquisition.LightSource;
import ome.model.acquisition.OTF;
import ome.model.acquisition.Objective;
import ome.model.acquisition.ObjectiveSettings;
import ome.model.acquisition.StageLabel;
import ome.model.annotations.BooleanAnnotation;
import ome.model.annotations.PixelsAnnotationLink;
import ome.model.containers.Dataset;
import ome.model.containers.DatasetImageLink;
import ome.model.containers.Project;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.LogicalChannel;
import ome.model.core.OriginalFile;
import ome.model.core.Pixels;
import ome.model.core.PlaneInfo;
import ome.model.enums.AcquisitionMode;
import ome.model.enums.ArcType;
import ome.model.enums.Binning;
import ome.model.enums.Correction;
import ome.model.enums.ContrastMethod;
import ome.model.enums.DetectorType;
import ome.model.enums.DimensionOrder;
import ome.model.enums.FilamentType;
import ome.model.enums.Format;
import ome.model.enums.Illumination;
import ome.model.enums.Immersion;
import ome.model.enums.LaserMedium;
import ome.model.enums.LaserType;
import ome.model.enums.Medium;
import ome.model.enums.PhotometricInterpretation;
import ome.model.enums.PixelsType;
import ome.model.enums.Pulse;
import ome.model.meta.Experimenter;
import ome.model.screen.Plate;
import ome.model.screen.Screen;
import ome.model.screen.ScreenAcquisition;
import ome.model.screen.Well;
import ome.model.screen.WellSample;
import ome.model.stats.StatsInfo;
import ome.parameters.Parameters;
import ome.system.Login;
import ome.system.Server;
import ome.system.ServiceFactory;
import ome.api.IRepositoryInfo;
import ome.conditions.ApiUsageException;
import ome.conditions.SessionException;
import ome.formats.enums.EnumerationProvider;
import ome.formats.enums.IQueryEnumProvider;
import ome.formats.importer.MetaLightSource;

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
public class OMEROMetadataStore implements MetadataStore, IMinMaxStore
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

    /** A list of Pixels that we have worked on ordered by first access. */
    private List<Pixels> pixelsList = new ArrayList<Pixels>();
    
    /** A list of Screens that we have worked on ordered by first access. */
    private List<Screen> screenList = new ArrayList<Screen>();

    /** A list of Plates that we have worked on ordered by first access. */
    private List<Plate> plateList = new ArrayList<Plate>();
    
    /** A list of lightsource objects */
    private List<Instrument> instrumentList = new ArrayList<Instrument>();

    /** A list of all objects we've recieved from the client and their LSIDs. */
    private Map<LSID, IObject> lsidMap = new HashMap<LSID, IObject>();
        
    /**
     * Updates a given model object in our object graph.
     * @param lsid LSID of model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should are used to describe the model
     * object's graph location.
     */
    public void updateObject(String lsid, IObject sourceObject,
    		                 Map<String, Integer> indexes)
    {
    	lsidMap.put(new LSID(lsid), sourceObject);
    	if (sourceObject instanceof Image)
    	{
    		handle(lsid, (Image) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Pixels)
    	{
    		handle(lsid, (Pixels) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof LogicalChannel)
    	{
    		handle(lsid, (LogicalChannel) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof PlaneInfo)
    	{
    		handle(lsid, (PlaneInfo) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Instrument)
    	{
    		handle(lsid, (Instrument) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Objective)
    	{
    		handle(lsid, (Objective) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Detector)
    	{
    		handle(lsid, (Detector) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Laser)
    	{
    		handle(lsid, (LightSource) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Filament)
    	{
    		handle(lsid, (LightSource) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Arc)
    	{
    		handle(lsid, (LightSource) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof ImagingEnvironment)
    	{
    		handle(lsid, (ImagingEnvironment) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof DetectorSettings)
    	{
    		handle(lsid, (DetectorSettings) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof LightSettings)
    	{
    		handle(lsid, (LightSettings) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof ObjectiveSettings)
    	{
    		handle(lsid, (ObjectiveSettings) sourceObject, indexes);
    	}
    	else
    	{
    		throw new ApiUsageException(
    			"Missing object handler for object type: "
    				+ sourceObject.getClass());
    	}
    }
    
    /**
     * Updates our object graph references.
     * @param referenceCache Client side LSID reference cache.
     */
    public void updateReferences(Map<String, String> referenceCache)
    {
    	for (String target : referenceCache.keySet())
    	{
    		IObject targetObject = lsidMap.get(new LSID(target));
    		String reference = referenceCache.get(target);
    		IObject referenceObject = lsidMap.get(new LSID(target));
    		if (targetObject instanceof DetectorSettings)
    		{
    			if (referenceObject instanceof Detector)
    			{
    				handleReference((DetectorSettings) targetObject,
    						        (Detector) referenceObject);
    				return;
    			}
    		}
    		else if (targetObject instanceof Image)
    		{
    			if (referenceObject instanceof Instrument)
    			{
    				handleReference((Image) targetObject,
    						        (Instrument) referenceObject);
    				return;
    			}
    		}
    		else if (targetObject instanceof LightSettings)
    		{
    			if (referenceObject instanceof LightSource)
    			{
    				handleReference((LightSettings) targetObject,
    						        (LightSource) referenceObject);
    				return;
    			}
    		}
    		else if (targetObject instanceof LogicalChannel)
    		{
    			if (referenceObject instanceof OTF)
    			{
    				handleReference((LogicalChannel) targetObject,
    						        (OTF) referenceObject);
    				return;
    			}
    		}
    		else if (targetObject instanceof OTF)
    		{
    			if (referenceObject instanceof Objective)
    			{
    				handleReference((OTF) targetObject,
    						        (Objective) referenceObject);
    				return;
    			}
    		}
    		else if (targetObject instanceof ObjectiveSettings)
    		{
    			if (referenceObject instanceof Objective)
    			{
    				handleReference((ObjectiveSettings) targetObject,
    						        (Objective) referenceObject);
    				return;
    			}
    		}
			throw new ApiUsageException(String.format(
					"Missing reference handler for %s(%s) --> %s(%s) reference.",
					reference, referenceObject, target, targetObject));
    	}
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should are used to describe the model
     * object's graph location.
     */
    private void handle(String LSID, Image sourceObject,
    		            Map<String, Integer> indexes)
    {
        imageList.add(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Pixels sourceObject,
    		            Map<String, Integer> indexes)
    {
    	int imageIndex = indexes.get("imageIndex");
    	imageList.get(imageIndex).addPixels(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, LogicalChannel sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Pixels p = getPixels(indexes.get("imageIndex"), 0);
    	Channel c = new Channel();
    	c.setLogicalChannel(sourceObject);
    	p.addChannel(c);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, PlaneInfo sourceObject,
    		            Map<String, Integer> indexes)
    {
    	int imageIndex = indexes.get("imageIndex");
    	int pixelsIndex = indexes.get("pixelsIndex");
    	Pixels p = imageList.get(imageIndex).getPixels(pixelsIndex);
    	p.addPlaneInfo(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Instrument sourceObject,
    		            Map<String, Integer> indexes)
    {
    	instrumentList.add(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Objective sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Instrument i = getInstrument(indexes.get("instrumentIndex"));
    	i.addObjective(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Detector sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Instrument i = getInstrument(indexes.get("instrumentIndex"));
    	i.addDetector(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, LightSource sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Instrument i = instrumentList.get(indexes.get("instrumentIndex"));
    	i.addLightSource(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, ImagingEnvironment sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Image i = imageList.get(indexes.get("imageIndex"));
    	i.setImagingEnvironment(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, DetectorSettings sourceObject,
    		            Map<String, Integer> indexes)
    {
    	LogicalChannel lc = getLogicalChannel(indexes.get("imageIndex"),
    			                              indexes.get("logicalChannelIndex"));
    	lc.setDetectorSettings(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, LightSettings sourceObject,
    		            Map<String, Integer> indexes)
    {
    	LogicalChannel lc = getLogicalChannel(indexes.get("imageIndex"),
    			                              indexes.get("logicalChannelIndex"));
    	lc.setLightSourceSettings(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, ObjectiveSettings sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Image i = getImage(indexes.get("imageIndex"));
    	i.setObjectiveSettings(sourceObject);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(DetectorSettings target, Detector reference)
    {
    	target.setDetector(reference);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Image target, Instrument reference)
    {
    	target.setInstrument(reference);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(LightSettings target, LightSource reference)
    {
    	target.setLightSource(reference);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(LogicalChannel target, OTF reference)
    {
    	target.setOtf(reference);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(OTF target, Objective reference)
    {
    	target.setObjective(reference);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(ObjectiveSettings target, Objective reference)
    {
    	target.setObjective(reference);
    }
    
    /**
     * Returns an Image model object based on its indexes within the OMERO data
     * model.
     * @param imageIndex Image index.
     * @return See above.
     */
    private Image getImage(int imageIndex)
    {
    	return imageList.get(imageIndex);
    }
    
    /**
     * Returns a Pixels model object based on its indexes within the OMERO data
     * model.
     * @param imageIndex Image index.
     * @param pixelsIndex Pixels index.
     * @return See above.
     */
    private Pixels getPixels(int imageIndex, int pixelsIndex)
    {
    	return getImage(imageIndex).getPixels(pixelsIndex);
    }
    
    /**
     * Returns an Instrument model object based on its indexes within the OMERO
     * data model.
     * @param instrumentIndex Instrument index.
     * @return See above.
     */
    private Instrument getInstrument(int instrumentIndex)
    {
    	return instrumentList.get(instrumentIndex);
    }
    
    /**
     * Returns a LogicalChannel model object based on its indexes within the
     * OMERO data model.
     * @param imageIndex Image index.
     * @param logicalChannelIndex Logical channel index.
     * @return See above.
     */
    private LogicalChannel getLogicalChannel(int imageIndex,
    		                                 int logicalChannelIndex)
    {
    	Channel c = getPixels(imageIndex, 0).getChannel(logicalChannelIndex); 
    	return c.getLogicalChannel();
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
            InitializeServices(sf);

        } catch (Throwable t)
        {
            throw new Exception(t);
        }
    }
    
    /**
     * Private class used by constructor to initialze the services of the service factory.
     * 
     * @param factory a non-null, active {@link ServiceFactory}
     */
    private void InitializeServices(ServiceFactory sf)
    {
        // Now initialize all our services
        iQuery = sf.getQueryService();
        iUpdate = sf.getUpdateService();
        pservice = sf.createRawPixelsStore();
        iInfo = sf.getRepositoryInfoService();
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
        instrumentList = new ArrayList<Instrument>();
        plateList = new ArrayList<Plate>();
        screenList = new ArrayList<Screen>();
        lsidMap = new HashMap<LSID, IObject>();
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setRoot(java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void setRoot(Object root) throws IllegalArgumentException
    {
        if (root == null) {
            imageList = new ArrayList<Image>();
        } else if (!(root instanceof List))
            throw new IllegalArgumentException("'root' object of type '"
                    + root.getClass()
                    + "' must be of type 'List<ome.model.core.Image>'");
        imageList = (List<Image>) root;
    }
    
    public long getExperimenterID()
    {
        return sf.getAdminService().getEventContext().getCurrentUserId();
    }

    
    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#setChannelGlobalMinMax(int,
     *      java.lang.Double, java.lang.Double, java.lang.Integer)
     */
    @SuppressWarnings("unchecked")
    public void setChannelGlobalMinMax(int channelIdx, Double globalMin,
            Double globalMax, Integer imageIndex)
    {
        log.debug(String.format(
                "Setting Image[%d] Channel[%d] globalMin: '%f' globalMax: '%f'",
                imageIndex, channelIdx, globalMin, globalMax));
        if (globalMin != null)
        {
            globalMin = new Double(Math.floor(globalMin.doubleValue()));
        }
        if (globalMax != null)
        {
            globalMax = new Double(Math.ceil(globalMax.doubleValue()));
        }
        StatsInfo statsInfo = new StatsInfo();
        statsInfo.setGlobalMin(globalMin);
        statsInfo.setGlobalMax(globalMax);
        getPixels(imageIndex, 0).getChannel(channelIdx).setStatsInfo(statsInfo);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.IMinMaxStore#setChannelGlobalMinMax(int, double, double, int)
     */
    public void setChannelGlobalMinMax(int channel, double minimum,
            double maximum, int imageIndex)
    {
        log.debug(String.format(
                "Setting Image[%d] Channel[%d] globalMin: '%f' globalMax: '%f'",
                imageIndex, channel, minimum, maximum));
        StatsInfo statsInfo = new StatsInfo();
        statsInfo.setGlobalMin(minimum);
        statsInfo.setGlobalMax(maximum);
        getPixels(imageIndex, 0).getChannel(channel).setStatsInfo(statsInfo);
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

        pservice.setPlane(pixels, theZ, theC, theT);
    }


    /**
     * Sets the pixels id in the OMERO image repository.
     * 
     * @param id the primary <i>id</i> of the pixels set.
     */
    public void setPixelsId(Long id)
    {
        if (pservice == null) pservice = sf.createRawPixelsStore();

        pservice.setPixelsId(id);
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
        Image unloadedImage = new Image(image.getId(), false);
        Dataset unloadedDataset = new Dataset(dataset.getId(), false);
        DatasetImageLink link = new DatasetImageLink();
        link.setParent(unloadedDataset);
        link.setChild(unloadedImage);

        // Now update the dataset object in the database
        iUpdate.saveObject(link);
    }

    public void addBooleanAnnotationToPixels(BooleanAnnotation ba, Pixels p)
    {
        Pixels unloadedPixels = new Pixels(p.getId(), false);
        PixelsAnnotationLink link = new PixelsAnnotationLink();
        link.setParent(unloadedPixels);
        link.setChild(ba);
        iUpdate.saveObject(link);

    }

    /**
     * Creates a new dataset, adding it to the mentioned project
     * @param name
     * @param description
     * @param project
     * @return returns the new dataset created
     */
    public Dataset addDataset(String name, String description, Project project)
    {
        Dataset dataset = new Dataset();
        if (name.length() != 0)
            dataset.setName(name);
        if (description.length() != 0)
            dataset.setDescription(description);
        Project p = new Project(project.getId(), false);
        dataset.linkProject(p);
        
        Dataset storedDataset = null;
        storedDataset = iUpdate.saveAndReturnObject(dataset);
        return storedDataset;
    }
    
    /**
     * Create a new project, adding it to the mentioned project
     * @param name
     * @param description
     * @return
     */
    public Project addProject(String name, String description)
    {
        Project project = new Project();
        if (name.length() != 0)
            project.setName(name);
        if (description.length() != 0)
            project.setDescription(description);
        
        Project storedProject = null;
        storedProject = iUpdate.saveAndReturnObject(project);
        return storedProject;
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
                "from Dataset d where id in " +
                "(select link.child.id from ProjectDatasetLink link where " +
                "link.parent.id = :id) order by d.name", new Parameters().addId(project.getId()));
        return (List<Dataset>) l;

        // Use this for M3 build till it gets fixed if this is needed.
        //return new ArrayList();
    }

    public Dataset getDataset(long datasetID)
    {
        Dataset dataset = iQuery.get(Dataset.class, datasetID);
        return dataset;
    }

    public Project getProject(long projectID)
    {
        Project project = iQuery.get(Project.class, projectID);
        return project;
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
                "where p.details.owner.id = :id order by name", 
                new Parameters().addId(getExperimenterID()));
        return (List<Project>) l;
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
        for (int j=0; j < p.getSizeC(); j++)
        {
            Channel channel = p.getChannel(j);
            Channel readerChannel = getPixels(i, 0).getChannel(j);
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

	@Override
	public void setArcType(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setChannelComponentColorDomain(String arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setChannelComponentIndex(Integer arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDetectorGain(Float arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDetectorID(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDetectorManufacturer(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDetectorModel(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDetectorOffset(Float arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDetectorSerialNumber(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDetectorSettingsBinning(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDetectorSettingsDetector(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDetectorSettingsGain(Float arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDetectorSettingsOffset(Float arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDetectorSettingsReadOutRate(Float arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDetectorSettingsVoltage(Float arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDetectorType(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDetectorVoltage(Float arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDimensionsPhysicalSizeX(Float arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDimensionsPhysicalSizeY(Float arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDimensionsPhysicalSizeZ(Float arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDimensionsTimeIncrement(Float arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDimensionsWaveIncrement(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDimensionsWaveStart(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDisplayOptionsID(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDisplayOptionsProjectionZStart(Integer arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDisplayOptionsProjectionZStop(Integer arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDisplayOptionsTimeTStart(Integer arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDisplayOptionsTimeTStop(Integer arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDisplayOptionsZoom(Float arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setExperimentDescription(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setExperimentID(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setExperimentType(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setExperimenterEmail(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setExperimenterFirstName(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setExperimenterID(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setExperimenterInstitution(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setExperimenterLastName(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setExperimenterMembershipGroup(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFilamentType(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setImageCreationDate(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setImageDefaultPixels(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setImageDescription(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setImageID(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setImageInstrumentRef(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setImageName(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setImagingEnvironmentAirPressure(Float arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setImagingEnvironmentCO2Percent(Float arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setImagingEnvironmentHumidity(Float arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setImagingEnvironmentTemperature(Float arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setInstrumentID(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLaserFrequencyMultiplication(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLaserLaserMedium(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLaserPulse(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLaserTuneable(Boolean arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLaserType(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLaserWavelength(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLightSourceID(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLightSourceManufacturer(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLightSourceModel(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLightSourcePower(Float arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLightSourceSerialNumber(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLightSourceSettingsAttenuation(Float arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLightSourceSettingsLightSource(String arg0, int arg1,
			int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLightSourceSettingsWavelength(Integer arg0, int arg1,
			int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLogicalChannelContrastMethod(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLogicalChannelEmWave(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLogicalChannelExWave(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLogicalChannelFluor(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLogicalChannelID(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLogicalChannelIlluminationType(String arg0, int arg1,
			int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLogicalChannelMode(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLogicalChannelName(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLogicalChannelNdFilter(Float arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLogicalChannelOTF(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLogicalChannelPhotometricInterpretation(String arg0,
			int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLogicalChannelPinholeSize(Float arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLogicalChannelPockelCellSetting(Integer arg0, int arg1,
			int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setLogicalChannelSamplesPerPixel(Integer arg0, int arg1,
			int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOTFID(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOTFObjective(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOTFOpticalAxisAveraged(Boolean arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOTFPixelType(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOTFSizeX(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setOTFSizeY(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setObjectiveCalibratedMagnification(Float arg0, int arg1,
			int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setObjectiveCorrection(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setObjectiveID(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setObjectiveImmersion(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setObjectiveIris(Boolean arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setObjectiveLensNA(Float arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setObjectiveManufacturer(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setObjectiveModel(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setObjectiveNominalMagnification(Integer arg0, int arg1,
			int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setObjectiveSerialNumber(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setObjectiveSettingsCorrectionCollar(Float arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setObjectiveSettingsMedium(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setObjectiveSettingsObjective(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setObjectiveSettingsRefractiveIndex(Float arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setObjectiveWorkingDistance(Float arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPixelsBigEndian(Boolean arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPixelsDimensionOrder(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPixelsID(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPixelsPixelType(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPixelsSizeC(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPixelsSizeT(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPixelsSizeX(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPixelsSizeY(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPixelsSizeZ(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPlaneTheC(Integer arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPlaneTheT(Integer arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPlaneTheZ(Integer arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPlaneTimingDeltaT(Float arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPlaneTimingExposureTime(Float arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPlateDescription(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPlateExternalIdentifier(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPlateID(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPlateName(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPlateRefID(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPlateStatus(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setROIID(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setROIT0(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setROIT1(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setROIX0(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setROIX1(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setROIY0(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setROIY1(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setROIZ0(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setROIZ1(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setReagentDescription(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setReagentID(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setReagentName(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setReagentReagentIdentifier(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setScreenAcquisitionEndTime(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setScreenAcquisitionID(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setScreenAcquisitionStartTime(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setScreenID(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setScreenName(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setScreenProtocolDescription(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setScreenProtocolIdentifier(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setScreenReagentSetDescription(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setScreenType(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStageLabelName(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStageLabelX(Float arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStageLabelY(Float arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStageLabelZ(Float arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStagePositionPositionX(Float arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStagePositionPositionY(Float arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setStagePositionPositionZ(Float arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTiffDataFileName(String arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTiffDataFirstC(Integer arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTiffDataFirstT(Integer arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTiffDataFirstZ(Integer arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTiffDataIFD(Integer arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTiffDataNumPlanes(Integer arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTiffDataUUID(String arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUUID(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWellColumn(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWellExternalDescription(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWellExternalIdentifier(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWellID(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWellRow(Integer arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWellSampleID(String arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWellSampleIndex(Integer arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWellSamplePosX(Float arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWellSamplePosY(Float arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWellSampleTimepoint(Integer arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWellType(String arg0, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}
}
