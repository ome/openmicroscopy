/*
 *   Copyright (C) 2006-2014 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ome.model.IEnum;
import ome.model.IObject;
import ome.model.acquisition.Detector;
import ome.model.acquisition.DetectorSettings;
import ome.model.acquisition.Dichroic;
import ome.model.acquisition.Filter;
import ome.model.acquisition.FilterSet;
import ome.model.acquisition.ImagingEnvironment;
import ome.model.acquisition.Instrument;
import ome.model.acquisition.Laser;
import ome.model.acquisition.LightPath;
import ome.model.acquisition.LightSettings;
import ome.model.acquisition.LightSource;
import ome.model.acquisition.Microscope;
import ome.model.acquisition.OTF;
import ome.model.acquisition.Objective;
import ome.model.acquisition.ObjectiveSettings;
import ome.model.acquisition.StageLabel;
import ome.model.annotations.Annotation;
import ome.model.annotations.FileAnnotation;
import ome.model.containers.Dataset;
import ome.model.core.Channel;
import ome.model.core.Image;
import ome.model.core.LogicalChannel;
import ome.model.core.OriginalFile;
import ome.model.core.Pixels;
import ome.model.core.PlaneInfo;
import ome.model.enums.Format;
import ome.model.experiment.Experiment;
import ome.model.experiment.MicrobeamManipulation;
import ome.model.fs.Fileset;
import ome.model.fs.FilesetJobLink;
import ome.model.roi.Roi;
import ome.model.roi.Shape;
import ome.model.screen.Plate;
import ome.model.screen.Reagent;
import ome.model.screen.Screen;
import ome.model.screen.Well;
import ome.model.screen.WellSample;
import ome.model.screen.PlateAcquisition;
import ome.model.stats.StatsInfo;
import ome.system.ServiceFactory;
import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.util.LSID;
import ome.util.SqlAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.perf4j.slf4j.Slf4JStopWatch;
import org.perf4j.StopWatch;


/**
 * An OMERO metadata store. This particular metadata store requires the user to
 * be logged into OMERO prior to use with the
 * {@link ome.security.SecuritySystem#login(ome.system.Principal)} method. While
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
    /** List of graphics domains we are checking.*/
    private static String[] DOMAINS = {"jpeg", "png", "bmp", "gif", "tiff", "avi"};

    /** Logger for this class. */
    private static Logger log = LoggerFactory.getLogger(OMEROMetadataStore.class);

    /** OMERO service factory; all other services are retrieved from here. */
    private ServiceFactory sf;

    private SqlAction sql;

    /** A map of imageIndex vs. Image object ordered by first access. */
    private Map<Integer, Image> imageList = 
    	new LinkedHashMap<Integer, Image>();

    /** A map of pixelsIndex vs. Pixels object ordered by first access. */
    private Map<Integer, Pixels> pixelsList = 
    	new LinkedHashMap<Integer, Pixels>();
    
    /** A map of screenIndex vs. Screen object ordered by first access. */
    private Map<Integer, Screen> screenList = 
    	new LinkedHashMap<Integer, Screen>();

    /** A map of plateIndex vs. Plate object ordered by first access. */
    private Map<Integer, Plate> plateList = 
    	new LinkedHashMap<Integer, Plate>();

    /** A map of roiIndex vs. ROI object ordered by first access. */
    private Map<Integer, Roi> roiList =
        new LinkedHashMap<Integer, Roi>();

    /** A map of wellIndex vs. Well object ordered by first access. */
    private Map<Integer, Map<Integer, Well>> wellList = 
        new LinkedHashMap<Integer, Map<Integer, Well>>();

    /** A map of instrumentIndex vs. Instrument object ordered by first access. */
    private Map<Integer, Instrument> instrumentList = 
    	new LinkedHashMap<Integer, Instrument>();

    /** A map of experimentIndex vs. Experiment object ordered by first access. */
    private Map<Integer, Experiment> experimentList = 
    	new LinkedHashMap<Integer, Experiment>();

    /**
     * A map of Instrument vs. a map of otfIndex vs. OTF object ordered by
     * first access.
     */
    private Map<Instrument, Map<Integer, OTF>> otfList =
        new LinkedHashMap<Instrument, Map<Integer, OTF>>();

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
    	else if (sourceObject instanceof StageLabel)
    	{
    		handle(lsid, (StageLabel) sourceObject, indexes);
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
        else if (sourceObject instanceof Microscope)
        {
            handle(lsid, (Microscope) sourceObject, indexes);
        }
    	else if (sourceObject instanceof Objective)
    	{
    		handle(lsid, (Objective) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Detector)
    	{
    		handle(lsid, (Detector) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Dichroic)
    	{
    		handle(lsid, (Dichroic) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Filter)
    	{
    		handle(lsid, (Filter) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof FilterSet)
    	{
    		handle(lsid, (FilterSet) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof LightSource)
    	{
    		handle(lsid, (LightSource) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof OTF)
    	{
    		handle(lsid, (OTF) sourceObject, indexes);
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
        else if (sourceObject instanceof LightPath)
        {
            handle(lsid, (LightPath) sourceObject, indexes);
        }
    	else if (sourceObject instanceof Screen)
    	{
    	    handle(lsid, (Screen) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Plate)
    	{
    	    handle(lsid, (Plate) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof PlateAcquisition)
    	{
    		handle(lsid, (PlateAcquisition) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Well)
    	{
    	    handle(lsid, (Well) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Reagent)
    	{
    	    handle(lsid, (Reagent) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof WellSample)
    	{
    	    handle(lsid, (WellSample) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof OriginalFile)
        {
            handle(lsid, (OriginalFile) sourceObject, indexes);
        }
    	else if (sourceObject instanceof Annotation)
    	{
    		handle(lsid, (Annotation) sourceObject, indexes);
    	}
    	else if (sourceObject instanceof Experiment)
    	{
    		handle(lsid, (Experiment) sourceObject, indexes);
    	}
        else if (sourceObject instanceof MicrobeamManipulation)
        {
            handle(lsid, (MicrobeamManipulation) sourceObject, indexes);
        }
    	else if (sourceObject instanceof Roi)
    	{
    	    handle(lsid, (Roi) sourceObject, indexes);
    	}
        else if (sourceObject instanceof Shape)
        {
            handle(lsid, (Shape) sourceObject, indexes);
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
    public void updateReferences(Map<String, String[]> referenceCache)
    {
        // This function is mostly processing back-refrences. e.g. If the OME Schema
        // has a AnnotationRef in ROI the referenceObject is Annotation and the
        // targetObject is ROI.
        for (String target : referenceCache.keySet())
        {
            for (String reference : referenceCache.get(target))
            {
                LSID targetLSID = new LSID(target);
                IObject targetObject = lsidMap.get(targetLSID);
                LSID referenceLSID = new LSID(reference);
                IObject referenceObject = lsidMap.get(
                        new LSID(stripCustomSuffix(reference)));

                log.debug(String.format(
                        "Updating reference handler for %s(%s) --> %s(%s).",
                        reference, referenceObject, target, targetObject));

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
                    if (referenceObject instanceof Annotation)
                    {
                        handleReference((Image) targetObject,
                                        (Annotation) referenceObject);
                        continue;
                    }
                    if (referenceObject instanceof Roi)
                    {
                        handleReference((Image) targetObject,
                                        (Roi) referenceObject);
                        continue;
                    }
                    if (referenceObject instanceof Experiment)
                    {
                        handleReference((Image) targetObject,
                                        (Experiment) referenceObject);
                        continue;
                    }
                    if (referenceObject instanceof MicrobeamManipulation)
                    {
                        handleReference((Image) targetObject,
                                        (MicrobeamManipulation) referenceObject);
                        continue;
                    }
                    if (referenceLSID.toString().contains("DatasetI"))
                    {
                        int colonIndex = reference.indexOf(":");
                        long datasetId = Long.parseLong(
                                reference.substring(colonIndex + 1));
                        referenceObject = new Dataset(datasetId, false);
                        handleReference((Image) targetObject,
                                        (Dataset) referenceObject);
                        continue;
                    }
                }
                else if (targetObject instanceof PlaneInfo)
                {
                    if (referenceObject instanceof Annotation) {
                        handleReference((PlaneInfo) targetObject,
                                        (Annotation) referenceObject);
                        continue;
                    }
                }
                else if (targetObject instanceof LightSource)
                {
                    if (referenceObject instanceof LightSource)
                    {
                        handleReference((LightSource) targetObject,
                                        (LightSource) referenceObject);
                        continue;
                    }
                    if (referenceObject instanceof Annotation) {
                        handleReference((LightSource) targetObject,
                                        (Annotation) referenceObject);
                        continue;
                    }
                }
                else if (targetObject instanceof Detector)
                {
                    if (referenceObject instanceof Annotation) {
                        handleReference((Detector) targetObject,
                                        (Annotation) referenceObject);
                        continue;
                    }
                }
                else if (targetObject instanceof Dichroic)
                {
                    if (referenceObject instanceof Annotation) {
                        handleReference((Dichroic) targetObject,
                                        (Annotation) referenceObject);
                        continue;
                    }
                }
                else if (targetObject instanceof Filter)
                {
                    if (referenceObject instanceof Annotation) {
                        handleReference((Filter) targetObject,
                                        (Annotation) referenceObject);
                        continue;
                    }
                }
                else if (targetObject instanceof Instrument)
                {
                    if (referenceObject instanceof Annotation) {
                        handleReference((Instrument) targetObject,
                                        (Annotation) referenceObject);
                        continue;
                    }
                }
                else if (targetObject instanceof Objective)
                {
                    if (referenceObject instanceof Annotation) {
                        handleReference((Objective) targetObject,
                                        (Annotation) referenceObject);
                        continue;
                    }
                }
                else if (targetObject instanceof Shape)
                {
                    if (referenceObject instanceof Annotation) {
                        handleReference((Shape) targetObject,
                                        (Annotation) referenceObject);
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
                else if (targetObject instanceof LightPath)
                {
                    if (referenceObject instanceof Dichroic)
                    {
                        handleReference((LightPath) targetObject,
                                        (Dichroic) referenceObject);
                        continue;
                    }
                    if (referenceObject instanceof Filter)
                    {
                        handleReference((LightPath) targetObject,
                                        (Filter) referenceObject, referenceLSID);
                        continue;
                    }
                    if (referenceObject instanceof Annotation) {
                        handleReference((LightPath) targetObject,
                                        (Annotation) referenceObject);
                        continue;
                    }
                }
                else if (targetObject instanceof Channel)
                {
                    if (referenceObject instanceof OTF)
                    {
                        handleReference((Channel) targetObject,
                                        (OTF) referenceObject);
                        continue;
                    }
                    if (referenceObject instanceof Annotation) {
                        handleReference((Channel) targetObject,
                                        (Annotation) referenceObject);
                        continue;
                    }
                }
                else if (targetObject instanceof LogicalChannel)
                {
                    if (referenceObject instanceof Filter)
                    {
                        handleReference((LogicalChannel) targetObject,
                                        (Filter) referenceObject,
                                        referenceLSID);
                        continue;
                    }
                    if (referenceObject instanceof FilterSet)
                    {
                        handleReference((LogicalChannel) targetObject,
                                        (FilterSet) referenceObject);
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
                    if (referenceObject instanceof FilterSet)
                    {
                        handleReference((OTF) targetObject,
                                        (FilterSet) referenceObject);
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
                else if (targetObject instanceof PlateAcquisition)
                {
                    if (referenceObject instanceof WellSample)
                    {
                        handleReference((PlateAcquisition) targetObject,
                                        (WellSample) referenceObject);
                        continue;
                    }
                    if (referenceObject instanceof Annotation)
                    {
                        handleReference((PlateAcquisition) targetObject,
                                        (Annotation) referenceObject);
                        continue;
                    }
                }
                else if (targetObject instanceof Pixels)
                {
                    if (referenceObject instanceof OriginalFile)
                    {
                        handleReference((Pixels) targetObject,
                                        (OriginalFile) referenceObject);
                        continue;
                    }
                }
                else if (targetObject instanceof FilterSet)
                {
                    if (referenceObject instanceof Filter)
                    {
                        handleReference((FilterSet) targetObject,
                                        (Filter) referenceObject,
                                        referenceLSID);
                        continue;
                    }
                    if (referenceObject instanceof Dichroic)
                    {
                        handleReference((FilterSet) targetObject,
                                        (Dichroic) referenceObject);
                        continue;
                    }
                }
                else if (targetObject instanceof Plate)
                {
                    if (referenceLSID.toString().contains("ScreenI"))
                    {
                        int colonIndex = reference.indexOf(":");
                        long screenId = Long.parseLong(
                                reference.substring(colonIndex + 1));
                        referenceObject = new Screen(screenId, false);
                        handleReference((Plate) targetObject,
                                        (Screen) referenceObject);
                        continue;
                    }
                    if (referenceObject instanceof Screen)
                    {
                        handleReference((Plate) targetObject,
                                        (Screen) referenceObject);
                        continue;
                    }
                    if (referenceObject instanceof Annotation)
                    {
                        handleReference((Plate) targetObject,
                                        (Annotation) referenceObject);
                        continue;
                    }
                }
                else if (targetObject instanceof Screen)
                {
                    if (referenceObject instanceof Plate)
                    {
                        handleReference((Screen) targetObject,
                                        (Plate) referenceObject);
                        continue;
                    }
                    if (referenceObject instanceof Annotation) {
                            handleReference((Screen) targetObject,
                                            (Annotation) referenceObject);
                            continue;
                    }
                }
                else if (targetObject instanceof Well)
                {
                    if (referenceObject instanceof Reagent)
                    {
                        handleReference((Well) targetObject,
                                        (Reagent) referenceObject);
                        continue;
                    }
                }
                else if (targetObject instanceof Reagent)
                {
                    if (referenceObject instanceof Annotation) {
                        handleReference((Reagent) targetObject,
                                        (Annotation) referenceObject);
                        continue;
                    }
                }
                else if (targetObject instanceof FileAnnotation)
                {
                    if (referenceObject instanceof OriginalFile)
                    {
                        handleReference((FileAnnotation) targetObject,
                                        (OriginalFile) referenceObject);
                        continue;
                    }
                }
                else if (targetObject instanceof Annotation)
                {
                    if (referenceObject instanceof Annotation)
                    {
                        handleReference((Annotation) targetObject,
                                        (Annotation) referenceObject);
                        continue;
                    }
                }
                else if (targetObject instanceof MicrobeamManipulation)
                {
                    if (referenceObject instanceof Roi)
                    {
                        handleReference((MicrobeamManipulation) targetObject,
                                        (Roi) referenceObject);
                        continue;
                    }
                }
                else if (targetObject instanceof Roi)
                {
                    if (referenceObject instanceof Annotation) {
                        handleReference((Roi) targetObject,
                                        (Annotation) referenceObject);
                        continue;
                    }
                }
                else if (targetObject instanceof PlateAcquisition)
                {
                    if (referenceObject instanceof Annotation) {
                        handleReference((PlateAcquisition) targetObject,
                                        (Annotation) referenceObject);
                        continue;
                    }
                }
                throw new ApiUsageException(String.format(
                    "Missing reference handler for %s(%s) --> %s(%s) reference.",
                    reference, referenceObject, target, targetObject));
            }
        }
    }
    
    /**
     * Strips custom, reference only suffixes from LSID so that the object
     * may be correctly looked up.
     * @param LSID The LSID string to strip the suffix from.
     * @return A new LSID string with the suffix stripped or <code>LSID</code>.
     */
    private String stripCustomSuffix(String LSID)
    {
    	if (LSID.endsWith("OMERO_EMISSION_FILTER")
    		|| LSID.endsWith("OMERO_EXCITATION_FILTER"))
    	{
    		return LSID.substring(0, LSID.lastIndexOf(':'));
    	}
    	return LSID;
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
    	int imageIndex = indexes.get("imageIndex");
        imageList.put(imageIndex, sourceObject);
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
    	Channel c = getChannel(indexes.get("imageIndex"), indexes.get("channelIndex"));
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
    	Pixels p = imageList.get(imageIndex).getPrimaryPixels();
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
        int instrumentIndex = indexes.get("instrumentIndex");
        instrumentList.put(instrumentIndex, sourceObject);
    }

    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Microscope sourceObject,
                        Map<String, Integer> indexes)
    {
        int instrumentIndex = indexes.get("instrumentIndex");
        instrumentList.get(instrumentIndex).setMicroscope(sourceObject);
    }

    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, StageLabel sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Image i = getImage(indexes.get("imageIndex"));
    	i.setStageLabel(sourceObject);
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
    private void handle(String LSID, OTF sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Instrument i = instrumentList.get(indexes.get("instrumentIndex"));
    	i.addOTF(sourceObject);
    	Map<Integer, OTF> map = otfList.get(i);
    	if (map == null)
    	{
    	    map = new HashMap<Integer, OTF>();
    	    otfList.put(i, map);
    	}
    	map.put(indexes.get("otfIndex"), sourceObject);
    }

    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Dichroic sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Instrument i = instrumentList.get(indexes.get("instrumentIndex"));
    	i.addDichroic(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Filter sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Instrument i = instrumentList.get(indexes.get("instrumentIndex"));
    	i.addFilter(sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, FilterSet sourceObject,
    		            Map<String, Integer> indexes)
    {
    	Instrument i = instrumentList.get(indexes.get("instrumentIndex"));
    	i.addFilterSet(sourceObject);
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
    			                              indexes.get("channelIndex"));
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
        Integer imageIndex = indexes.get("imageIndex");
        Integer channelIndex = indexes.get("channelIndex");
        Integer experimentIndex = indexes.get("experimentIndex");
        Integer microbeamManipulationIndex = 
            indexes.get("microbeamManipulationIndex");
        if (experimentIndex != null)
        {
            Experiment e = experimentList.get(experimentIndex);
            Iterator<MicrobeamManipulation> iter = 
                e.iterateMicrobeamManipulation();
            for (int i = 0; i < e.sizeOfMicrobeamManipulation(); i++)
            {
                MicrobeamManipulation mm = iter.next();
                if (i == microbeamManipulationIndex)
                {
                    mm.addLightSettings(sourceObject);
                }
            }
        }
        else
        {
            LogicalChannel lc = getLogicalChannel(imageIndex, channelIndex);
            lc.setLightSourceSettings(sourceObject);
        }
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
        Integer instrumentIndex = indexes.get("instrumentIndex");
        Integer otfIndex = indexes.get("otfIndex");
        Integer imageIndex = indexes.get("imageIndex");
        if (instrumentIndex != null && otfIndex != null)
        {
            OTF o = getOTF(instrumentIndex, otfIndex);
            o.setObjective(sourceObject.getObjective());
        }
        else
        {
            Image i = getImage(imageIndex);
            i.setObjectiveSettings(sourceObject);
        }
    }

    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, LightPath sourceObject,
                        Map<String, Integer> indexes)
    {
        Channel c = getChannel(
                indexes.get("imageIndex"), indexes.get("channelIndex"));
        c.getLogicalChannel().setLightPath(sourceObject);
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
    	int plateIndex = indexes.get("plateIndex");
        wellList.put(plateIndex, new LinkedHashMap<Integer, Well>());
        plateList.put(plateIndex, sourceObject);
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
        int wellIndex = indexes.get("wellIndex");
        getPlate(plateIndex).addWell(sourceObject);
        wellList.get(plateIndex).put(wellIndex, sourceObject);
    }

    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Screen sourceObject,
                        Map<String, Integer> indexes)
    {
    	int screenIndex = indexes.get("screenIndex");
    	screenList.put(screenIndex, sourceObject);
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Reagent sourceObject,
                        Map<String, Integer> indexes)
    {
        int screenIndex = indexes.get("screenIndex");
        getScreen(screenIndex).addReagent(sourceObject);
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
    private void handle(String LSID, Roi sourceObject,
                        Map<String, Integer> indexes)
    {
        roiList.put(indexes.get("roiIndex"), sourceObject);
    }

    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Shape sourceObject,
                        Map<String, Integer> indexes)
    {
        int roiIndex = indexes.get("roiIndex");
        Roi r = getRoi(roiIndex);
        r.addShape(sourceObject);
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
        // No-op.
    }
    
    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Annotation sourceObject,
                        Map<String, Integer> indexes)
    {
        // No-op.
    }

    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, Experiment sourceObject,
                        Map<String, Integer> indexes)
    {
    	int experimentIndex = indexes.get("experimentIndex");
    	experimentList.put(experimentIndex, sourceObject);
    }

    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, MicrobeamManipulation sourceObject,
                        Map<String, Integer> indexes)
    {
        int experimentIndex = indexes.get("experimentIndex");
        Experiment e = experimentList.get(experimentIndex);
        e.addMicrobeamManipulation(sourceObject);
    }

    /**
     * Handles inserting a specific type of model object into our object graph.
     * @param LSID LSID of the model object.
     * @param sourceObject Model object itself.
     * @param indexes Any indexes that should be used to reference the model
     * object.
     */
    private void handle(String LSID, PlateAcquisition sourceObject,
                        Map<String, Integer> indexes)
    {
        int plateIndex = indexes.get("plateIndex");
        Plate p = getPlate(plateIndex);
        p.addPlateAcquisition(sourceObject);
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
    private void handleReference(Image target, Dataset reference)
    {
    	target.linkDataset(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(LightSource target, LightSource reference)
    {
    	// The only possible linkage at this point is a Laser's pump.
    	Laser laser = (Laser) target;
    	laser.setPump(reference);
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
    private void handleReference(LightPath target, Dichroic reference)
    {
        target.setDichroic(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(LightPath target, Filter reference,
                                 LSID referenceLSID)
    {
        if (referenceLSID.toString().endsWith("OMERO_EMISSION_FILTER"))
        {
            target.linkEmissionFilter(reference);
        }
        else if (referenceLSID.toString().endsWith("OMERO_EXCITATION_FILTER"))
        {
            target.linkExcitationFilter(reference);
        }
        else
        {
            throw new ApiUsageException(String.format(
                    "Unable to handle LightPath --> Filter reference: %s",
                    referenceLSID));
        }
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Channel target, OTF reference)
    {
        target.getLogicalChannel().setOtf(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Channel target, Annotation reference)
    {
        target.linkAnnotation(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(LogicalChannel target, FilterSet reference)
    {
    	target.setFilterSet(reference);
    }
    
    /**
     * Handles linking a specific reference object to a target object in our
     * object graph. This method handles <b>secondary</b> excitation and 
     * emission filters so requires the LSID be passed in as well.
     * @param target Target model object.
     * @param reference Reference model object.
     * @param referenceLSID LSID of the reference object.
     */
    private void handleReference(LogicalChannel target, Filter reference,
    		                     LSID referenceLSID)
    {
        LightPath lightPath = target.getLightPath();
        if (lightPath == null)
        {
            lightPath = new LightPath();
        }
        target.setLightPath(lightPath);
    	if (referenceLSID.toString().endsWith("OMERO_EMISSION_FILTER"))
    	{
    		lightPath.linkEmissionFilter(reference);
    	}
    	else if (referenceLSID.toString().endsWith("OMERO_EXCITATION_FILTER"))
    	{
    		lightPath.linkExcitationFilter(reference);
    	}
    	else
    	{
    		throw new ApiUsageException(String.format(
    				"Unable to handle LogicalChannel --> Filter reference: %s",
    				referenceLSID));
    	}
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
    private void handleReference(OTF target, FilterSet reference)
    {
        target.setFilterSet(reference);
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
    private void handleReference(PlateAcquisition target, WellSample reference)
    {
        target.addWellSample(reference);
    }

		/**
		 * Handles linking a specific reference object to a target object in our
		 * object graph.
		 * @param target Target model object.
		 * @param reference Reference model object.
		 */
		private void handleReference(PlateAcquisition target, Annotation reference)
		{
				target.linkAnnotation(reference);
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
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(FilterSet target, Dichroic reference)
    {
        target.setDichroic(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(FilterSet target, Filter reference,
    		                     LSID referenceLSID)
    {
    	if (referenceLSID.toString().endsWith("OMERO_EMISSION_FILTER"))
    	{
    		target.linkEmissionFilter(reference);
    	}
    	else if (referenceLSID.toString().endsWith("OMERO_EXCITATION_FILTER"))
    	{
    		target.linkExcitationFilter(reference);
    	}
    	else
    	{
    		throw new ApiUsageException(String.format(
    				"Unable to handle FilterSet --> Filter reference: %s",
    				referenceLSID));
    	}
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Image target, Annotation reference)
    {
        target.linkAnnotation(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Image target, Roi reference)
    {
        target.addRoi(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Image target, Experiment reference)
    {
        target.setExperiment(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Image target, MicrobeamManipulation reference)
    {
        // TODO: add code to handle this linking if needed
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Screen target, Plate reference)
    {
        if (!target.linkedPlateList().contains(reference))
        {
            target.linkPlate(reference);
        }
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Screen target, Annotation reference)
    {
        target.linkAnnotation(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Plate target, Annotation reference)
    {
        target.linkAnnotation(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Detector target, Annotation reference)
    {
        target.linkAnnotation(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Dichroic target, Annotation reference)
    {
        target.linkAnnotation(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Filter target, Annotation reference)
    {
        target.linkAnnotation(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Instrument target, Annotation reference)
    {
        target.linkAnnotation(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(LightPath target, Annotation reference)
    {
        target.linkAnnotation(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Objective target, Annotation reference)
    {
        target.linkAnnotation(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Shape target, Annotation reference)
    {
        target.linkAnnotation(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(LightSource target, Annotation reference)
    {
        target.linkAnnotation(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Reagent target, Annotation reference)
    {
        target.linkAnnotation(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Roi target, Annotation reference)
    {
        target.linkAnnotation(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(PlaneInfo target, Annotation reference)
    {
        target.linkAnnotation(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Plate target, Screen reference)
    {
        if (!target.linkedScreenList().contains(reference))
        {
            target.linkScreen(reference);
        }
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Well target, Reagent reference)
    {
        target.linkReagent(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(FileAnnotation target, OriginalFile reference)
    {
        target.setFile(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(Annotation target, Annotation reference)
    {
        target.linkAnnotation(reference);
    }

    /**
     * Handles linking a specific reference object to a target object in our
     * object graph.
     * @param target Target model object.
     * @param reference Reference model object.
     */
    private void handleReference(MicrobeamManipulation target, Roi reference)
    {
			// no-op as the ROIRef cannot be set on a MicrobeamManipulation
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
     * Returns an OTF model object based on its indexes within the OMERO
     * data model.
     * @param instrumentIndex Instrument index.
     * @param otfIndex OTF index.
     * @return See above.
     */
    private OTF getOTF(int instrumentIndex, int otfIndex)
    {
        Instrument i = getInstrument(instrumentIndex);
        return otfList.get(i).get(otfIndex);
    }

    /**
     * Returns a Channel model object based on its indexes within the
     * OMERO data model.
     * @param imageIndex Image index.
     * @param channelIndex channel index.
     * @return See above.
     */
    private Channel getChannel(int imageIndex, int channelIndex)
    {
    	return getPixels(imageIndex, 0).getChannel(channelIndex); 
    }
    
    /**
     * Returns a LogicalChannel model object based on its indexes within the
     * OMERO data model.
     * @param imageIndex Image index.
     * @param channelIndex channel index.
     * @return See above.
     */
    private LogicalChannel getLogicalChannel(int imageIndex,
    		                                 int channelIndex)
    {
    	return getChannel(imageIndex, channelIndex).getLogicalChannel();
    }

    /**
     * Returns a Screen model object based on its indexes within the
     * OMERO data model.
     * @param screenIndex Screen index.
     * @return See above.
     */ 
    private Screen getScreen(int screenIndex)
    {
        return screenList.get(screenIndex);
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
        return wellList.get(plateIndex).get(wellIndex);
    }

    /**
     * Returns a Roi model object based on its indexes within the
     * OMERO data model.
     * @param roiIndex Roi index.
     * @return See above.
     */
    private Roi getRoi(int roiIndex)
    {
        return roiList.get(roiIndex);
    }

    /**
     * Empty constructor for testing purposes.
     */
    public OMEROMetadataStore() {}
    
    /**
     * Creates a new instance.
     * 
     * @param factory a non-null, active {@link ServiceFactory}
     * @param sql the SQL action instance
     * @throws ValidationException if the factory is null or there
     *             is another error instantiating required services.
     */
    public OMEROMetadataStore(ServiceFactory factory, SqlAction sql)
            throws ValidationException
    {
        if (factory == null || sql == null)
            throw new ValidationException("arguments cannot be null");
        sf = factory;
        this.sql = sql;
    }

    /*
     * (non-Javadoc)
     * 
     * @see loci.formats.MetadataStore#createRoot()
     */
    public void createRoot()
    {
        imageList = new LinkedHashMap<Integer, Image>();
        pixelsList = new LinkedHashMap<Integer, Pixels>();
        screenList = new LinkedHashMap<Integer, Screen>();
        plateList = new LinkedHashMap<Integer, Plate>();
        roiList = new LinkedHashMap<Integer, Roi>();
        wellList = new LinkedHashMap<Integer, Map<Integer, Well>>();
        instrumentList = new LinkedHashMap<Integer, Instrument>();
        experimentList = new LinkedHashMap<Integer, Experiment>();
        otfList = new LinkedHashMap<Instrument, Map<Integer, OTF>>();
        lsidMap = new LinkedHashMap<LSID, IObject>();
    }
    
    /**
     * Compares two enumerations by reference and by ID.
     * @param a First OMERO model object.
     * @param b Second OMERO model object.
     * @return <code>true</code> if <code>a == null && b == null</code>, 
     * <code>a == b</code> or <code>a.getId() == b.getId()</code>.
     */
    private static boolean compare(IEnum a, IEnum b)
    {
    	if (a == null && b == null)
    	{
    		return true;
    	}
    	if (a == null || b == null)
    	{
    		return false;
    	}
    	return a.getId() == b.getId();
    }
    
    /**
     * Compares two objects by reference existence and by value.
     * @param a First object.
     * @param b Second object.
     * @return <code>true</code> if <code>a == null && b == null</code> or
     * <code>a.equals(b)</code>.
     */
    private static boolean compare(Object a, Object b)
    {
    	if (a == null && b == null)
    	{
    		return true;
    	}
    	if (a == null || b == null)
    	{
    		return false;
    	}
    	return a.equals(b);
    }
    
    /**
     * Checks the entire object graph for sections that may be collapsed if
     * the data is derived from a Plate. Collapsible points:
     * <ul>
     *   <li>Image --> ObjectiveSettings</li>
     *   <li>Image --> Channel --> LogicalChannel --> LightSettings</li>
     *   <li>Image --> Channel --> LogicalChannel --> LightPath</li>
     *   <li>Image --> Channel --> LogicalChannel --> DetectorSettings</li>
     * </ul>
     */
    public void checkAndCollapseGraph()
    {
    	// Ensure we're working with an SPW data set.
    	if (plateList.size() == 0)
    	{
    		return;
    	}
    	// Collapse down ObjectiveSettings by uniqueness
    	Set<ObjectiveSettings> objectiveSettings =
    		new HashSet<ObjectiveSettings>();
    	Set<LightSettings> lightSettings = new HashSet<LightSettings>();
    	Set<LightPath> lightPaths = new HashSet<LightPath>();
    	Set<DetectorSettings> detectorSettings = 
    		new HashSet<DetectorSettings>();
    	Set<LogicalChannel> logicalChannels = new HashSet<LogicalChannel>();
    	Pixels pixels;
    	Channel channel;
    	LogicalChannel lc;
    	for (Image image : imageList.values())
    	{
    		pixels = image.getPrimaryPixels();
    		image.setObjectiveSettings(
    				getUniqueObjectiveSettings(objectiveSettings, image));
    		for (int c = 0; c < pixels.sizeOfChannels(); c++)
    		{
    			channel = pixels.getChannel(c);
    			lc = channel.getLogicalChannel();
    			lc.setLightSourceSettings(
    					getUniqueLightSettings(lightSettings, lc));
    			lc.setDetectorSettings(
    					getUniqueDetectorSettings(detectorSettings, lc));
    			lc.setLightPath(
    					getUniqueLightPath(lightPaths, lc.getLightPath()));
    			channel.setLogicalChannel(
    					getUniqueLogicalChannel(logicalChannels, lc));
    		}
    	}
    	log.info("Unique objective settings: " + objectiveSettings.size());
    	log.info("Unique light settings: " + lightSettings.size());
    	log.info("Unique detector settings: " + detectorSettings.size());
    	log.info("Unique light paths: " + lightPaths.size());
    	log.info("Unique logical channels: " + logicalChannels.size());
    }

    /**
     * For all plates and all images which are not contained within a well,
     * create a link from the {@link Fileset} to the given object.
     */
    private void linkFileset(FilesetJobLink link)
    {
        final Fileset fs = link.parent().proxy(); // Unloaded

        for (Image image : imageList.values())
        {
            image.setFileset(fs.proxy());
        }
    }

    /**
     * Finds the matching unique settings for an image.
     * @param uniqueSettings Set of existing unique settings.
     * @param image Image to find unique settings for.
     * @return Matched unique settings or <code>null</code> if
     * <code>lc.getObjectiveSettings() == null</code>.
     */
    private ObjectiveSettings getUniqueObjectiveSettings(
    		Set<ObjectiveSettings> uniqueSettings, Image image)
    {
    	ObjectiveSettings s1 = image.getObjectiveSettings();
    	if (s1 == null)
    	{
    		return null;
    	}
    	for (ObjectiveSettings s2 : uniqueSettings)
    	{
    		if (compare(s1.getCorrectionCollar(), s2.getCorrectionCollar())
    			&& compare(s1.getMedium(), s2.getMedium())
    			&& s1.getObjective() == s2.getObjective()
    			&& compare(s1.getRefractiveIndex(), s2.getRefractiveIndex()))
    		{
    			return s2;
    		}
    	}
    	uniqueSettings.add(s1);
    	return s1;
    }
    
    /**
     * Finds the matching unique settings for a logical channel.
     * @param uniqueSettings Set of existing unique settings.
     * @param lc Logical channel to find unique settings for.
     * @return Matched unique settings or <code>null</code> if
     * <code>lc.getLightSourceSettings() == null</code>.
     */
    private LightSettings getUniqueLightSettings(
    		Set<LightSettings> uniqueSettings, LogicalChannel lc)
    {
    	LightSettings s1 = lc.getLightSourceSettings();
    	if (s1 == null)
    	{
    		return null;
    	}
    	for (LightSettings s2 : uniqueSettings)
    	{
    		if (compare(s1.getAttenuation(), s2.getAttenuation())
    			&& s1.getLightSource() == s2.getLightSource()
    			&& s1.getMicrobeamManipulation()
    			   == s2.getMicrobeamManipulation()
    			&& compare(s1.getWavelength(), s2.getWavelength()))
    		{
    			return s2;
    		}
    	}
    	uniqueSettings.add(s1);
    	return s1;
    }
    
    /**
     * Finds the matching unique settings for a logical channel.
     * @param uniqueSettings Set of existing unique settings.
     * @param lc Logical channel to find unique settings for.
     * @return Matched unique settings or <code>null</code> if
     * <code>lc.getDetectorSettings() == null</code>.
     */
    private DetectorSettings getUniqueDetectorSettings(
    		Set<DetectorSettings> uniqueSettings, LogicalChannel lc)
    {
    	DetectorSettings s1 = lc.getDetectorSettings();
    	if (s1 == null)
    	{
    		return null;
    	}
    	for (DetectorSettings s2 : uniqueSettings)
    	{
    		if (compare(s1.getBinning(), s2.getBinning())
    			&& s1.getDetector() == s2.getDetector()
    			&& compare(s1.getGain(), s2.getGain())
    			&& compare(s1.getOffsetValue(), s2.getOffsetValue())
    			&& compare(s1.getReadOutRate(), s2.getReadOutRate())
    			&& compare(s1.getVoltage(), s2.getVoltage()))
    		{
    			return s2;
    		}
    	}
    	uniqueSettings.add(s1);
    	return s1;
    }
    
    /**
     * Finds the matching unique logical channel.
     * @param uniqueChannels Set of existing unique logical channels.
     * @param lc Logical channel to compare for uniqueness.
     * @return Matched unique logical channel or <code>null</code> if
     * <code>lc == null</code>.
     */
    private LogicalChannel getUniqueLogicalChannel(
    		Set<LogicalChannel> uniqueChannels, LogicalChannel lc)
    {
    	if (lc == null)
    	{
    		return null;
    	}

    	for (LogicalChannel lc2 : uniqueChannels)
    	{
    		if (compare(lc.getMode(), lc2.getMode())
    			&& compare(lc.getContrastMethod(), lc2.getContrastMethod())
    			&& compare(lc.getIllumination(), lc2.getIllumination())
    			&& compare(lc.getPhotometricInterpretation(),
    					   lc2.getPhotometricInterpretation())
    			&& lc.getDetectorSettings() == lc2.getDetectorSettings()
    			&& compare(lc.getEmissionWave(), lc2.getEmissionWave())
    			&& compare(lc.getExcitationWave(), lc2.getExcitationWave())
    			&& lc.getFilterSet() == lc2.getFilterSet()
    			&& compare(lc.getFluor(), lc2.getFluor())
    			&& lc.getLightSourceSettings() == lc2.getLightSourceSettings()
    			&& compare(lc.getName(), lc2.getName())
    			&& compare(lc.getNdFilter(), lc2.getNdFilter())
    			&& lc.getOtf() == lc.getOtf()
    			&& compare(lc.getPinHoleSize(), lc2.getPinHoleSize())
    			&& compare(lc.getPockelCellSetting(), lc2.getPockelCellSetting())
    			&& compare(lc.getSamplesPerPixel(), lc2.getSamplesPerPixel())
    			&& lc.getLightPath() == lc2.getLightPath())
    		{
    			return lc2;
    		}
    	}
    	uniqueChannels.add(lc);
    	return lc;
    }

    /**
     * Finds the matching unique light path.
     * @param uniqueLightPaths Set of existing unique light paths.
     * @param lp Light path to compare for uniqueness.
     * @return Matched unique light path or <code>null</code> if
     * <code>lp == null</code>.
     */
    private LightPath getUniqueLightPath(
            Set<LightPath> uniqueLightPaths, LightPath lp)
    {
        if (lp == null)
        {
            return null;
        }

        for (LightPath lp2 : uniqueLightPaths)
        {
            if (lp.getDichroic() == lp2.getDichroic()) {
                // Excitation filters are ordered - no sorting required.
                List<Filter> exFilters = lp.linkedExcitationFilterList();
                List<Filter> exFilters2 = lp2.linkedExcitationFilterList();
                if (exFilters.equals(exFilters2)) {
                    List<Filter> emFilters = lp.linkedEmissionFilterList();
                    List<Filter> emFilters2 = lp2.linkedEmissionFilterList();
                    // Emission filters are un-ordered - sorting required.  If
                    // we do not sort out of order filters will cause
                    // List.equals() to fail.
                    Comparator<Filter> comparator = new ToStringComparator();
                    Collections.sort(emFilters, comparator);
                    Collections.sort(emFilters2, comparator);

                    if (emFilters.equals(emFilters2)) {
                        return lp2;
                    }
                }
            }
        }
        uniqueLightPaths.add(lp);
        return lp;
    }

    /**
     * Saves the current object graph to the database.
     * @param link a link from the fileset to be linked from
     * @return List of the Pixels objects with their attached object graphs
     * that have been saved.
     */
    public List<Pixels> saveToDB(FilesetJobLink link)
    {
    	// Check the entire object graph, optimizing and sections that may
    	// be collapsed.
    	checkAndCollapseGraph();
    	linkFileset(link);
    	
    	// Save the entire Image rooted graph using the "insert only"
    	// saveAndReturnIds(). DISABLED until we can find out what is causing
    	// the extreme memory usage on the graph reload.
    	StopWatch s1 = new Slf4JStopWatch("omero.saveImportGraph");
    	Image[] imageArray = 
    		imageList.values().toArray(new Image[imageList.size()]);
    	IObject[] saved = sf.getUpdateService().saveAndReturnArray(imageArray);
    	s1.stop();
    	
    	List<Pixels> toReturn = new ArrayList<Pixels>();
    	Image image;
    	Pixels pixels;
    	for (int i = 0; i < saved.length; i++)
    	{
    		image = (Image) saved[i];
    		pixels = image.getPrimaryPixels();
    		pixelsList.put(i, pixels);
    		toReturn.add(pixels);
    	}
    	//s2.stop();
   		return toReturn;
    }

    /**
     * Checks if the format is a graphics format or not.
     *
     * @param value The value to check
     * @return See above.
     */
    private boolean isRGB(String value)
    {
        if (value == null) return false;
        value = value.toLowerCase();
        for (int i = 0; i < DOMAINS.length; i++) {
            if (DOMAINS[i].equals(value)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Synchronize the minimum and maximum intensity values with those
     * specified by the client and save them in the DB.
     * @param imageChannelGlobalMinMax Minimums and maximums to update.
     */
    public void populateMinMax(double[][][] imageChannelGlobalMinMax)
    {
    	double[][] channelGlobalMinMax;
    	double[] globalMinMax;
    	Channel channel;
    	StatsInfo statsInfo;
    	Pixels pixels, unloadedPixels;
    	for (int i = 0; i < imageChannelGlobalMinMax.length; i++)
    	{
    		channelGlobalMinMax = imageChannelGlobalMinMax[i];
    		pixels = pixelsList.get(i);
    		Format f = pixels.getImage().getFormat();
    		String v = null;
    		if (f != null) {
    		    v = f.getValue();
    		}
    		boolean rgb = isRGB(v);
    		String type = pixels.getPixelsType().getValue();
    		unloadedPixels = new Pixels(pixels.getId(), false);
    		for (int c = 0; c < channelGlobalMinMax.length; c++)
    		{
    			globalMinMax = channelGlobalMinMax[c];
    			channel = pixels.getChannel(c);
    			statsInfo = new StatsInfo();
    			if (rgb && "uint8".equals(type)) {
    			    statsInfo.setGlobalMin(0.0);
                    statsInfo.setGlobalMax(255.0);
    			} else {
    			    statsInfo.setGlobalMin(globalMinMax[0]);
                    statsInfo.setGlobalMax(globalMinMax[1]);
    			}
    			sql.setStatsInfo(channel, statsInfo);
    		}
    	}
    }

    /**
     * Simple comparator that compares two filters by their stringified value.
     * @author Emil Rozbicki <emil@glencoesoftware.com>
     *
     */
    class ToStringComparator implements Comparator<Filter>
    {
        @Override
        public int compare(Filter a, Filter b) {
            return a.toString().compareTo(b.toString());
        }
    }
}
