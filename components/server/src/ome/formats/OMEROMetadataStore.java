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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.api.IQuery;
import ome.api.IUpdate;
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
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.LogicalChannel;
import ome.model.core.OriginalFile;
import ome.model.core.Pixels;
import ome.model.core.PlaneInfo;
import ome.model.screen.Plate;
import ome.model.screen.Screen;
import ome.model.screen.Well;
import ome.model.screen.WellSample;
import ome.model.stats.StatsInfo;
import ome.system.ServiceFactory;
import ome.conditions.ApiUsageException;
import ome.util.LSID;

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
public class OMEROMetadataStore
{
    /** Logger for this class. */
    private static Log log = LogFactory.getLog(OMEROMetadataStore.class);

    /** OMERO service factory; all other services are retrieved from here. */
    private ServiceFactory sf;

    /** OMERO query service */
    private IQuery iQuery;

    /** OMERO update service */
    private IUpdate iUpdate;

    /** The "root" image object */
    private List<Image> imageList = new ArrayList<Image>();

    /** A list of Pixels that we have worked on ordered by first access. */
    private List<Pixels> pixelsList = new ArrayList<Pixels>();
    
    /** A list of Screens that we have worked on ordered by first access. */
    private List<Screen> screenList = new ArrayList<Screen>();

    /** A list of Plates that we have worked on ordered by first access. */
    private List<Plate> plateList = new ArrayList<Plate>();

    /** A list of Wells that we have worked on ordered by first access. */
    private List<Well> wellList = new ArrayList<Well>();
    
    /** A list of instrument objects */
    private List<Instrument> instrumentList = new ArrayList<Instrument>();

    /** A list of all objects we've received from the client and their LSIDs. */
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
    	else if (sourceObject instanceof Channel)
    	{
    		handle(lsid, (Channel) sourceObject, indexes);
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
    	else if (sourceObject instanceof Plate)
    	{
    	    handle(lsid, (Plate) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Well)
    	{
    	    handle(lsid, (Well) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof WellSample)
    	{
    	    handle(lsid, (WellSample) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof OriginalFile)
        {
            handle(lsid, (OriginalFile) sourceObject, indexes);
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
    		IObject referenceObject = lsidMap.get(new LSID(reference));
    		if (targetObject instanceof DetectorSettings)
    		{
    			if (referenceObject instanceof Detector)
    			{
    				handleReference((DetectorSettings) targetObject,
    						        (Detector) referenceObject);
    				continue;
    			}
    		}
    		else if (targetObject instanceof Image)
    		{
    			if (referenceObject instanceof Instrument)
    			{
    				handleReference((Image) targetObject,
    						        (Instrument) referenceObject);
    				continue;
    			}
    		}
    		else if (targetObject instanceof LightSettings)
    		{
    			if (referenceObject instanceof LightSource)
    			{
    				handleReference((LightSettings) targetObject,
    						        (LightSource) referenceObject);
    				continue;
    			}
    		}
    		else if (targetObject instanceof LogicalChannel)
    		{
    			if (referenceObject instanceof OTF)
    			{
    				handleReference((LogicalChannel) targetObject,
    						        (OTF) referenceObject);
    				continue;
    			}
    		}
    		else if (targetObject instanceof OTF)
    		{
    			if (referenceObject instanceof Objective)
    			{
    				handleReference((OTF) targetObject,
    						        (Objective) referenceObject);
    				continue;
    			}
    		}
    		else if (targetObject instanceof ObjectiveSettings)
    		{
    			if (referenceObject instanceof Objective)
    			{
    				handleReference((ObjectiveSettings) targetObject,
    						        (Objective) referenceObject);
    				continue;
    			}
    		}
    		else if (targetObject instanceof WellSample)
    		{
    		    if (referenceObject instanceof Image)
    		    {
    		        handleReference((WellSample) targetObject,
    		                        (Image) referenceObject);
    		        continue;
    		    }
    		}
            else if (targetObject instanceof Pixels)
            {
                if (referenceObject instanceof OriginalFile )
                {
                    handleReference((Pixels) targetObject,
                                    (OriginalFile) referenceObject);
                    continue;
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
    private void handle(String LSID, Channel sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Pixels p = getPixels(indexes.get("imageIndex"), 0);
    	p.addChannel(sourceObject);
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
    	Channel c = getChannel(indexes.get("imageIndex"),
    			               indexes.get("logicalChannelIndex"));
    	c.setLogicalChannel(sourceObject);
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
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Plate sourceObject,
                        Map<String, Integer> indexes)
    {
        wellList = new ArrayList<Well>();
        plateList.add(sourceObject);
    }

    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Well sourceObject,
                        Map<String, Integer> indexes)
    {
        int plateIndex = indexes.get("plateIndex");
        getPlate(plateIndex).addWell(sourceObject);  
        wellList.add(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, WellSample sourceObject,
                        Map<String, Integer> indexes)
    {
        int plateIndex = indexes.get("plateIndex");
        int wellIndex = indexes.get("wellIndex");
        Well w = getWell(plateIndex, wellIndex);
        w.addWellSample(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, OriginalFile sourceObject,
                        Map<String, Integer> indexes)
    {
        //Do nothing
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
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(WellSample target, Image reference)
    {
        reference.addWellSample(target);
    }
    
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Pixels target, OriginalFile reference)
    {
        target.linkOriginalFile(reference);
    }
    
    
    /**
     * Retrieves an object from the internal object graph by LSID.
     * @param lsid LSID of the object.
     * @return See above. <code>null</code> if the object is not in the
     * internal LSID map.
     */
    public IObject getObjectByLSID(LSID lsid)
    {
    	return lsidMap.get(lsid);
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
     * Returns a Channel model object based on its indexes within the
     * OMERO data model.
     * @param imageIndex Image index.
     * @param logicalChannelIndex Logical channel index.
     * @return See above.
     */
    private Channel getChannel(int imageIndex, int logicalChannelIndex)
    {
    	return getPixels(imageIndex, 0).getChannel(logicalChannelIndex); 
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
    	return getChannel(imageIndex, logicalChannelIndex).getLogicalChannel();
    }

    /**
     * Returns a Plate model object based on its indexes within the
     * OMERO data model.
     * @param plateIndex Plate index.
     * @return See above.
     */ 
    private Plate getPlate(int plateIndex)
    {
        return plateList.get(plateIndex);
    }

    /**
     * Returns a Well model object based on its indexes within the
     * OMERO data model.
     * @param plateIndex Plate index.
     * @param wellIndex Well index
     * @return See above.
     */ 
    private Well getWell(int plateIndex, int wellIndex)
    {
        return wellList.get(wellIndex);
 
    }
    
    /**
     * Empty constructor for testing purposes.
     */
    public OMEROMetadataStore() {}
    
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
            throw new Exception("Factory argument cannot be null.");
        sf = factory;
        // Now initialize all our services
        initializeServices(sf);
    }
    
    /**
     * Private class used by constructor to initialize the services of the 
     * service factory.
     * 
     * @param factory a non-null, active {@link ServiceFactory}
     */
    private void initializeServices(ServiceFactory sf)
    {
        // Now initialize all our services
        iQuery = sf.getQueryService();
        iUpdate = sf.getUpdateService();
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
        screenList = new ArrayList<Screen>();
        plateList = new ArrayList<Plate>();
        wellList = new ArrayList<Well>();
        instrumentList = new ArrayList<Instrument>();
        lsidMap = new HashMap<LSID, IObject>();
    }

    /**
     * Saves the current object graph to the database.
     * 
     * @return List of the Pixels objects with their attached object graphs
     * that have been saved.
     */
    public List<Pixels> saveToDB()
    {
    	Image[] imageArray = imageList.toArray(new Image[imageList.size()]);
   		IObject[] objectArray = iUpdate.saveAndReturnArray(imageArray);
   		pixelsList = new ArrayList<Pixels>(objectArray.length);
   		for (IObject object : objectArray)
   		{
   			Image image = (Image) object;
   			pixelsList.add(image.getPrimaryPixels());
   		}
   		return pixelsList;
    }
    
    /**
     * Synchronize the minimum and maximum intensity values with those
     * specified by the client and save them in the DB.
     * @param imageChannelGlobalMinMax Minimums and maximums to update.
     */
    public void populateMinMax(double[][][] imageChannelGlobalMinMax)
    {
    	List<Channel> channelList = new ArrayList<Channel>();
    	double[][] channelGlobalMinMax;
    	double[] globalMinMax;
    	Channel channel;
    	StatsInfo statsInfo;
    	Pixels pixels, unloadedPixels;
    	for (int i = 0; i < imageChannelGlobalMinMax.length; i++)
    	{
    		channelGlobalMinMax = imageChannelGlobalMinMax[i];
    		pixels = pixelsList.get(i);
    		unloadedPixels = new Pixels(pixels.getId(), false);
    		for (int c = 0; c < channelGlobalMinMax.length; c++)
    		{
    			globalMinMax = channelGlobalMinMax[c];
    			channel = pixels.getChannel(c);
    			statsInfo = new StatsInfo();
    			statsInfo.setGlobalMin(globalMinMax[0]);
    			statsInfo.setGlobalMax(globalMinMax[1]);
    			channel.setStatsInfo(statsInfo);
    			channel.setPixels(unloadedPixels);
    			channelList.add(channel);
    		}
    	}
    	Channel[] toSave = channelList.toArray(new Channel[channelList.size()]);
    	iUpdate.saveArray(toSave);
    }
}
