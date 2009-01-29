package ome.formats;

import java.io.File;
import java.lang.reflect.Constructor;
import java.security.MessageDigest;
import java.sql.Timestamp;
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;

import static omero.rtypes.*;
import ome.formats.enums.EnumerationProvider;
import ome.formats.enums.IQueryEnumProvider;
import ome.formats.importer.MetaLightSource;
import ome.formats.model.BlitzInstanceProvider;
import ome.formats.model.InstanceProvider;
import omero.RBool;
import omero.RDouble;
import omero.RFloat;
import omero.RInt;
import omero.RLong;
import omero.RString;
import omero.RTime;
import omero.ServerError;
import omero.client;
import omero.api.IAdminPrx;
import omero.api.IPojosPrx;
import omero.api.IQueryPrx;
import omero.api.IRepositoryInfoPrx;
import omero.api.IUpdatePrx;
import omero.api.MetadataStorePrx;
import omero.api.MetadataStorePrxHelper;
import omero.api.RawFileStorePrx;
import omero.api.RawPixelsStorePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceInterfacePrx;
import omero.constants.METADATASTORE;
import omero.metadatastore.IObjectContainer;
import omero.model.AcquisitionMode;
import omero.model.Arc;
import omero.model.ArcType;
import omero.model.Binning;
import omero.model.BooleanAnnotation;
import omero.model.ContrastMethod;
import omero.model.Correction;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.Detector;
import omero.model.DetectorSettings;
import omero.model.DetectorType;
import omero.model.DimensionOrder;
import omero.model.Experiment;
import omero.model.ExperimentType;
import omero.model.Filament;
import omero.model.FilamentType;
import omero.model.IObject;
import omero.model.Illumination;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.ImagingEnvironment;
import omero.model.Immersion;
import omero.model.Instrument;
import omero.model.Laser;
import omero.model.LaserMedium;
import omero.model.LaserType;
import omero.model.LightSettings;
import omero.model.LightSource;
import omero.model.LogicalChannel;
import omero.model.Medium;
import omero.model.OTF;
import omero.model.Objective;
import omero.model.ObjectiveSettings;
import omero.model.PhotometricInterpretation;
import omero.model.Pixels;
import omero.model.PixelsAnnotationLink;
import omero.model.PixelsAnnotationLinkI;
import omero.model.PixelsI;
import omero.model.PixelsType;
import omero.model.PlaneInfo;
import omero.model.Plate;
import omero.model.ProjectI;
import omero.model.Project;
import omero.model.Pulse;
import omero.model.Reagent;
import omero.model.Screen;
import omero.model.ScreenAcquisition;
import omero.model.StageLabel;
import omero.model.Well;
import omero.model.WellSample;

import loci.formats.meta.IMinMaxStore;
import loci.formats.meta.MetadataStore;


public class OMEROMetadataStoreClient implements MetadataStore, IMinMaxStore
{
	/** Logger for this class */
	private Log log = LogFactory.getLog(OMEROMetadataStoreClient.class);
	
    private MetadataStorePrx delegate;
    
    private Map<LSID, IObjectContainer> containerCache = 
    	new TreeMap<LSID, IObjectContainer>(new OMEXMLModelComparator());
    private Map<String, String> referenceCache = new HashMap<String, String>();
    
    private List<Pixels> pixelsList;
    
    private ServiceFactoryPrx serviceFactory;
    private IUpdatePrx iUpdate;
    private IQueryPrx iQuery;
    private IAdminPrx iAdmin;
    private RawFileStorePrx rawFileStore;
    private RawPixelsStorePrx rawPixelStore;
    private IRepositoryInfoPrx iRepoInfo;
    private IPojosPrx iPojos;
    
    /** Our enumeration provider. */
    private EnumerationProvider enumProvider;
    
    /** Our OMERO model object provider. */
    private InstanceProvider instanceProvider;

    private Long currentPixId;

    private void initialize()
    	throws ServerError
    {
    	 iUpdate = serviceFactory.getUpdateService();
         iQuery = serviceFactory.getQueryService();
         iAdmin = serviceFactory.getAdminService();
         rawFileStore = serviceFactory.createRawFileStore();
         rawPixelStore = serviceFactory.createRawPixelsStore();
         iRepoInfo = serviceFactory.getRepositoryInfoService();
         iPojos = serviceFactory.getPojosService();
         enumProvider = new IQueryEnumProvider(iQuery);
         instanceProvider = new BlitzInstanceProvider(enumProvider);

         delegate = MetadataStorePrxHelper.checkedCast(serviceFactory.getByName(METADATASTORE.value));        
    }
    
    public IQueryPrx getIQuery()
    {
        return iQuery;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param serviceFactory The factory. Mustn't be <code>null</code>.
     */
    public OMEROMetadataStoreClient(ServiceFactoryPrx serviceFactory)
    	throws ServerError
    {
    	if (serviceFactory == null)
    		throw new IllegalArgumentException("No factory.");
    	this.serviceFactory = serviceFactory;
    	initialize();
    }
    
    public OMEROMetadataStoreClient(String username, String password, String server,
            String port) throws CannotCreateSessionException, PermissionDeniedException, ServerError
    {
        client c = new client(server);
        serviceFactory = c.createSession(username, password);
        initialize();
    }

    public void ping()
    {
        serviceFactory.keepAllAlive(new ServiceInterfacePrx[] 
                {iQuery, iAdmin, rawFileStore, rawPixelStore, iRepoInfo, iPojos, iUpdate, delegate});
        log.debug("KeepAlive ping");
    }
    
    /**
     * Transforms a Java type into the corresponding OMERO RType.
     * @param value Java concrete type value.
     * @return RType or <code>null</code> if <code>value</code> is 
     * <code>null</code>.
     */
    public RInt toRType(Integer value)
    {
        return value == null? null : rint(value);
    }
    
    /**
     * Transforms a Java type into the corresponding OMERO RType.
     * @param value Java concrete type value.
     * @return RType or <code>null</code> if <code>value</code> is 
     * <code>null</code>.
     */
    public RLong toRType(Long value)
    {
        return value == null? null : rlong(value);
    }
    
    /**
     * Transforms a Java type into the corresponding OMERO RType.
     * @param value Java concrete type value.
     * @return RType or <code>null</code> if <code>value</code> is 
     * <code>null</code>.
     */
    public RString toRType(String value)
    {
        return value == null? null : rstring(value);
    }
    
    /**
     * Transforms a Java type into the corresponding OMERO RType.
     * @param value Java concrete type value.
     * @return RType or <code>null</code> if <code>value</code> is 
     * <code>null</code>.
     */
    public RBool toRType(Boolean value)
    {
        return value == null? null : rbool(value);
    }
    
    /**
     * Transforms a Java type into the corresponding OMERO RType.
     * @param value Java concrete type value.
     * @return RType or <code>null</code> if <code>value</code> is 
     * <code>null</code>.
     */
    public RDouble toRType(Double value)
    {
        return value == null? null : rdouble(value);
    }
    
    /**
     * Transforms a Java type into the corresponding OMERO RType.
     * @param value Java concrete type value.
     * @return RType or <code>null</code> if <code>value</code> is 
     * <code>null</code>.
     */
    public RFloat toRType(Float value)
    {
        return value == null? null : rfloat(value);
    }
    
    /**
     * Transforms a Java type into the corresponding OMERO RType.
     * @param value Java concrete type value.
     * @return RType or <code>null</code> if <code>value</code> is 
     * <code>null</code>.
     */
    public RTime toRType(Timestamp value)
    {
        return value == null? null : rtime(value);
    }
    
    public void logout()
    {
        serviceFactory.destroy();
    }
    
    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#createRoot()
     */
    public void createRoot()
    {
        try
        {
            containerCache = 
                new TreeMap<LSID, IObjectContainer>(new OMEXMLModelComparator());
            referenceCache = new HashMap<String, String>();
            delegate.createRoot();
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#getRoot()
     */
    public Object getRoot()
    {
    	return pixelsList;
    }

    /**
     * Retrieves a given enumeration from the current enumeration provider.
     * @param klass Enumeration type.
     * @param value Enumeration value.
     * @return See above.
     */
    private IObject getEnumeration(Class<? extends IObject> klass, String value)
    {
        return enumProvider.getEnumeration(klass, value, false);
    }

    /**
     * Returns the current container cache.
     * @return See above.
     */
    public Map<LSID, IObjectContainer> getContainerCache()
    {
    	return containerCache;
    }
    
    /**
     * Retrieves an OMERO Blitz source object for a given Java class and
     * indexes. 
     * @param klass Source object class.
     * @param indexes Indexes into the OME-XML data model.
     * @return See above.
     */
    private <T extends IObject> T getSourceObject(Class<T> klass, LinkedHashMap<String, Integer> indexes)
    {
        return (T) getIObjectContainer(klass, indexes).sourceObject;
    }
    
    /**
     * Retrieves an OMERO Blitz source object for a given LSID.
     * @param LSID LSID to retrieve a source object for.
     * @return See above.
     */
    public IObject getSourceObject(LSID LSID)
    {
        IObjectContainer o = containerCache.get(LSID);
        if (o == null)
        {
            return null;
        }
        return o.sourceObject;
    }
    
    /**
     * Retrieves all OMERO Blitz source objects of a given class.
     * @param klass Class to retrieve source objects for.
     * @return See above.
     */
    public <T extends IObject> List<T> getSourceObjects(Class<T> klass)
    {
    	Set<LSID> keys = containerCache.keySet();
    	List<T> toReturn = new ArrayList<T>();
    	for (LSID key : keys)
    	{
    		Class<? extends IObject> keyClass = key.getJavaClass();
    		if (keyClass != null && keyClass.equals(klass))
    		{
    			toReturn.add((T) containerCache.get(key).sourceObject);
    		}
    	}
    	return toReturn;
    }
    
    
    /**
     * Checks to see if there is currently an active reference for two LSIDs.
     * @param source LSID of the source object.
     * @param target LSID of the target object.
     * @return <code>true</code> if a reference exists, <code>false</code>
     * otherwise.
     */
    public boolean hasReference(LSID source, LSID target)
    {
        if (!referenceCache.containsKey(source.toString())
            || !referenceCache.containsValue(target.toString()))
        {
            return false;
        }
        return true;
    }
    
    /**
     * Retrieves an IObject container for a given class and location within the
     * OME-XML data model.
     * @param klass Class to retrieve a container for.
     * @param indexes Indexes into the OME-XML data model.
     * @return See above.
     */
    private IObjectContainer getIObjectContainer(Class<? extends IObject> klass,
    		                                     LinkedHashMap<String, Integer> indexes)
    {
    	// Transform an integer collection into an integer array without using
    	// wrapper objects.
    	Collection<Integer> indexValues = indexes.values();
    	int[] indexesArray = new int[indexValues.size()];
    	int i = 0;
    	for (Integer index : indexValues)
    	{
    		indexesArray[i] = index;
    		i++;
    	}
    	
    	// Create a new LSID.
        LSID LSID = new LSID(klass, indexesArray);
        
        // Because of the LightSource abstract type, here we need to handle
        // the upcast to the "real" concrete type and the correct LSID
        // mapping.
        if (klass.equals(Arc.class) || klass.equals(Laser.class)
            || klass.equals(Filament.class)
            && !containerCache.containsKey(LSID))
        {
            LSID lsLSID = new LSID(LightSource.class,
                                   indexes.get("instrumentIndex"),
                                   indexes.get("lightSourceIndex"));
            if (containerCache.containsKey(lsLSID))
            {
                IObjectContainer container = containerCache.get(lsLSID);
                MetaLightSource mls = 
                    (MetaLightSource) container.sourceObject;
                LightSource realInstance = 
                    (LightSource) getSourceObjectInstance(klass);
                mls.copyData(realInstance);
                container.sourceObject = realInstance;
                container.LSID = LSID.toString();
                containerCache.put(LSID, container);
                return container;
            }
        }
        // We may have first had a concrete method call request, put the object
        // in a container and in the cache. Now we have a request with only the
        // abstract type's class to give us LSID resolution and must handle 
        // that as well.
        if (klass.equals(LightSource.class)
        	&& !containerCache.containsKey(LSID))
        {
        	Class[] concreteClasses = 
        		new Class[] { Arc.class, Laser.class, Filament.class };
        	for (Class concreteClass : concreteClasses)
        	{
                LSID lsLSID = new LSID(concreteClass,
                                       indexes.get("instrumentIndex"),
                                       indexes.get("lightSourceIndex"));
                if (containerCache.containsKey(lsLSID))
                {
                	return containerCache.get(lsLSID);
                }
        	}
        }
        
        if (!containerCache.containsKey(LSID))
        {
            IObjectContainer c = new IObjectContainer();
            c.indexes = indexes;
            c.LSID = LSID.toString();
            c.sourceObject = getSourceObjectInstance(klass);
            containerCache.put(LSID, c);
        }
        
        return containerCache.get(LSID);
    }
    
    /**
     * Performs the task of actual source object instantiation using
     * reflection.
     * @param klass Class to instantiate a source object for.
     * @return An OMERO Blitz model object.
     */
    private <T extends IObject> T getSourceObjectInstance(Class<T> klass)
    {
    	return instanceProvider.getInstance(klass);
    }
        
    /**
     * Counts the number of containers the MetadataStore has of a given class.
     * @param klass Class to count containers of.
     * @return See above.
     */
    public int countCachedContainers(Class<? extends IObject> klass)
    {
        if (klass == null)
        {
            return new HashSet<IObjectContainer>(containerCache.values()).size();
        }
        
        int count = 0;
        for (LSID lsid : containerCache.keySet())
        {
        	Class<? extends IObject> lsidClass = lsid.getJavaClass();
            if (lsidClass != null && lsidClass.equals(klass))
            {
                count++;
            }
        }
        return count;
    }
    
    /**
     * Counts the number of references the MetadataStore has between objects
     * of two classes.
     * @param source Class of the source object. If <code>null</code> it is
     * treated as a wild card, all references whose target match
     * <code>target</code> will be counted. 
     * @param target Class of the target object. If <code>null</code> it is
     * treated as a wild card, all references whose source match
     * <code>source</code> will be counted. 
     * @return See above.
     */
    public int countCachedReferences(Class<? extends IObject> source,
                                     Class<? extends IObject> target)
    {
        if (source == null && target == null)
        {
            return referenceCache.size();
        }
        
        int count = 0;
        if (target == null)
        {
            for (String lsid : referenceCache.keySet())
            {
                String containerClass = lsid.split(":")[0];
                if (containerClass.equals(source.getName()))
                {
                    count++;
                }
            }
            return count;
        }
        
        if (source == null)
        {
            for (String lsid : referenceCache.values())
            {
                String containerClass = lsid.split(":")[0];
                if (containerClass.equals(target.getName()))
                {
                    count++;
                }
            }
            return count;
        }
        
        for (String lsid : referenceCache.keySet())
        {
            String containerClass = lsid.split(":")[0];
            if (containerClass.equals(source.getName()))
            {
                String targetClass = referenceCache.get(lsid).split(":")[0];
                if (targetClass.equals(target.getName()))
                {
                    count++;
                }
            }
        }
        return count;
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setArcType(java.lang.String, int, int)
     */
    public void setArcType(String type, int instrumentIndex,
            int lightSourceIndex)
    {
        Arc o = getArc(instrumentIndex, lightSourceIndex);
        
        o.setType((ArcType) getEnumeration(ArcType.class, type));
    }

    private Arc getArc(int instrumentIndex, int lightSourceIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        
        indexes.put("instrumentIndex", instrumentIndex);
        indexes.put("lightSourceIndex", lightSourceIndex);
        
        return(Arc) getSourceObject(Arc.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelComponentColorDomain(java.lang.String, int, int, int)
     */
    public void setChannelComponentColorDomain(String colorDomain,
            int imageIndex, int logicalChannelIndex, int channelComponentIndex)
    {
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelComponentIndex(java.lang.Integer, int, int, int)
     */
    public void setChannelComponentIndex(Integer index, int imageIndex,
            int logicalChannelIndex, int channelComponentIndex)
    {
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorGain(java.lang.Float, int, int)
     */
    public void setDetectorGain(Float gain, int instrumentIndex,
            int detectorIndex)
    {
        Detector o = getDetector(instrumentIndex, detectorIndex);
        o.setGain(toRType(gain));
    }

    public Detector getDetector(int instrumentIndex, int detectorIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", instrumentIndex);
        indexes.put("detectorIndex", detectorIndex);
        return getSourceObject(Detector.class, indexes);
    }

    public void setDetectorID(String id, int instrumentIndex, int detectorIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", instrumentIndex);
        indexes.put("detectorIndex", detectorIndex);
        IObjectContainer o = getIObjectContainer(Detector.class, indexes);
        
        o.LSID = id;
        containerCache.put(new LSID(id), o);
    }

    public void setDetectorManufacturer(String manufacturer,
            int instrumentIndex, int detectorIndex)
    {        
        Detector o = getDetector(instrumentIndex, detectorIndex);
        o.setManufacturer(toRType(manufacturer));
    }

    public void setDetectorModel(String model, int instrumentIndex,
            int detectorIndex)
    {
        Detector o = getDetector(instrumentIndex, detectorIndex);
        o.setModel(toRType(model));
    }

    public void setDetectorOffset(Float offset, int instrumentIndex,
            int detectorIndex)
    {
        Detector o = getDetector(instrumentIndex, detectorIndex);
        o.setOffsetValue(toRType(offset));
    }

    public void setDetectorSerialNumber(String serialNumber,
            int instrumentIndex, int detectorIndex)
    {
        Detector o = getDetector(instrumentIndex, detectorIndex);
        o.setSerialNumber(toRType(serialNumber));
    }

    public void setDetectorSettingsDetector(String detector, int imageIndex,
            int logicalChannelIndex)
    {
        LSID key = new LSID(DetectorSettings.class, imageIndex, logicalChannelIndex);
        referenceCache.put(key.toString(), detector);
    }

    private DetectorSettings getDetectorSettings(int imageIndex,
            int logicalChannelIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("imageIndex", imageIndex);
        indexes.put("logicalChannelIndex", logicalChannelIndex);
        return getSourceObject(DetectorSettings.class, indexes);
    }

    public void setDetectorSettingsBinning(String binning, int imageIndex,
            int logicalChannelIndex)
    {
        DetectorSettings o = getDetectorSettings(imageIndex, logicalChannelIndex);
        o.setBinning((Binning) getEnumeration(Binning.class, binning));
    }

    public void setDetectorSettingsReadOutRate(Float readOutRate,
            int imageIndex, int logicalChannelIndex)
    {
        DetectorSettings o = getDetectorSettings(imageIndex, logicalChannelIndex);
        o.setReadOutRate(toRType(readOutRate));
    }

    public void setDetectorSettingsVoltage(Float voltage, int imageIndex,
            int logicalChannelIndex)
    {
        DetectorSettings o = getDetectorSettings(imageIndex, logicalChannelIndex);
        o.setVoltage(toRType(voltage));
    }

    public void setDetectorSettingsGain(Float gain, int imageIndex,
            int logicalChannelIndex)
    {
        DetectorSettings o = getDetectorSettings(imageIndex, logicalChannelIndex);
        o.setGain(toRType(gain));
    }

    public void setDetectorSettingsOffset(Float offset, int imageIndex,
            int logicalChannelIndex)
    {
        DetectorSettings o = getDetectorSettings(imageIndex, logicalChannelIndex);
        o.setOffsetValue(toRType(offset));
    }

    public void setDetectorType(String type, int instrumentIndex,
            int detectorIndex)
    {
        Detector o = getDetector(instrumentIndex, detectorIndex);
        o.setType((DetectorType) getEnumeration(DetectorType.class, type));
    }

    public void setDetectorVoltage(Float voltage, int instrumentIndex,
            int detectorIndex)
    {
        Detector o = getDetector(instrumentIndex, detectorIndex);
        o.setVoltage(toRType(voltage));
    }

    public void setDimensionsPhysicalSizeX(Float physicalSizeX, int imageIndex,
            int pixelsIndex)
    {
        Pixels o = getPixels(imageIndex, pixelsIndex);
        o.setPhysicalSizeX(toRType(physicalSizeX));
    }

    private Pixels getPixels(int imageIndex, int pixelsIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("imageIndex", imageIndex);
        indexes.put("pixelsIndex", pixelsIndex);
        Pixels p = getSourceObject(Pixels.class, indexes);
        p.setSha1(rstring("Foo"));
        return p;
    }

    public void setDimensionsPhysicalSizeY(Float physicalSizeY, int imageIndex,
            int pixelsIndex)
    {
        Pixels o = getPixels(imageIndex, pixelsIndex);
        o.setPhysicalSizeX(toRType(physicalSizeY));
    }

    public void setDimensionsPhysicalSizeZ(Float physicalSizeZ, int imageIndex,
            int pixelsIndex)
    {
        Pixels o = getPixels(imageIndex, pixelsIndex);
        o.setPhysicalSizeX(toRType(physicalSizeZ));
    }

    public void setDimensionsTimeIncrement(Float timeIncrement, int imageIndex,
            int pixelsIndex)
    {
    }

    public void setDimensionsWaveIncrement(Integer waveIncrement,
            int imageIndex, int pixelsIndex)
    {
    }

    public void setDimensionsWaveStart(Integer waveStart, int imageIndex,
            int pixelsIndex)
    {
    }

    public void setDisplayOptionsID(String id, int imageIndex)
    {
    }

    public void setDisplayOptionsProjectionZStart(Integer start, int imageIndex)
    {
    }

    public void setDisplayOptionsProjectionZStop(Integer stop, int imageIndex)
    {
    }

    public void setDisplayOptionsTimeTStart(Integer start, int imageIndex)
    {
    }

    public void setDisplayOptionsTimeTStop(Integer stop, int imageIndex)
    {
    }

    public void setDisplayOptionsZoom(Float zoom, int imageIndex)
    {
    }

    public void setExperimenterEmail(String email, int experimenterIndex)
    {
    }

    public void setExperimenterFirstName(String firstName, int experimenterIndex)
    {
    }

    public void setExperimenterID(String id, int experimenterIndex)
    {
    }

    public void setExperimenterInstitution(String institution,
            int experimenterIndex)
    {
    }

    public void setExperimenterLastName(String lastName, int experimenterIndex)
    {
    }

    public void setFilamentType(String type, int instrumentIndex,
            int lightSourceIndex)
    {
        Filament o = getFilament(instrumentIndex, lightSourceIndex);
        o.setType((FilamentType) getEnumeration(FilamentType.class, type));
    }

    private Filament getFilament(int instrumentIndex, int lightSourceIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", instrumentIndex);
        indexes.put("lightSourceIndex", lightSourceIndex);
        return getSourceObject(Filament.class, indexes);
    }

    public void setImageCreationDate(String creationDate, int imageIndex)
    {
        if (creationDate != null)
        {
            try
            {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
                java.util.Date date = sdf.parse(creationDate);
                Timestamp creationTimestamp = new Timestamp(date.getTime());
                Image i = getImage(imageIndex);
                i.setAcquisitionDate(toRType(creationTimestamp));
            }
            catch (ParseException e)
            {
                log.error(String.format("Parsing start time failed!"), e);
            }
        }
    }

    private Image getImage(int imageIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("imageIndex", imageIndex);
        return getSourceObject(Image.class, indexes);
    }

    public void setImageDescription(String description, int imageIndex)
    {
        Image o = getImage(imageIndex);
        o.setDescription(toRType(description));
    }

    public void setImageID(String id, int imageIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("imageIndex", imageIndex);
        IObjectContainer o = getIObjectContainer(Image.class, indexes);
        o.LSID = id;
        containerCache.put(new LSID(id), o);
    }

    public void setImageInstrumentRef(String instrumentRef, int imageIndex)
    {
        LSID key = new LSID(Image.class, imageIndex);
        referenceCache.put(key.toString(), instrumentRef);
    }

    public void setImageName(String name, int imageIndex)
    {
        Image o = getImage(imageIndex);
        o.setName(toRType(name));
    }

    public void setImagingEnvironmentAirPressure(Float airPressure,
            int imageIndex)
    {
        ImagingEnvironment o = getImagingEnvironment(imageIndex);
        o.setAirPressure(toRType(airPressure));
    }

    private ImagingEnvironment getImagingEnvironment(int imageIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("imageIndex", imageIndex);
        return getSourceObject(ImagingEnvironment.class, indexes);
    }

    public void setImagingEnvironmentCO2Percent(Float percent, int imageIndex)
    {
        ImagingEnvironment o = getImagingEnvironment(imageIndex);
        o.setCo2percent(toRType(percent));
    }

    public void setImagingEnvironmentHumidity(Float humidity, int imageIndex)
    {
        ImagingEnvironment o = getImagingEnvironment(imageIndex);
        o.setHumidity(toRType(humidity));
    }

    public void setImagingEnvironmentTemperature(Float temperature,
            int imageIndex)
    {
        ImagingEnvironment o = getImagingEnvironment(imageIndex);
        o.setTemperature(toRType(temperature));
    }

    public void setInstrumentID(String id, int instrumentIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", instrumentIndex);
        IObjectContainer o = getIObjectContainer(Instrument.class, indexes);
        o.LSID = id;
        containerCache.put(new LSID(id), o);
    }

    public void setLaserFrequencyMultiplication(
            Integer frequencyMultiplication, int instrumentIndex,
            int lightSourceIndex)
    {
        Laser o = getLaser(instrumentIndex, lightSourceIndex);
        o.setFrequencyMultiplication(toRType(frequencyMultiplication));
    }

    private Laser getLaser(int instrumentIndex, int lightSourceIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", instrumentIndex);
        indexes.put("lightSourceIndex", lightSourceIndex);
        return getSourceObject(Laser.class, indexes);
    }

    public void setLaserLaserMedium(String laserMedium, int instrumentIndex,
            int lightSourceIndex)
    {
        Laser o = getLaser(instrumentIndex, lightSourceIndex);
        o.setLaserMedium((LaserMedium) getEnumeration(LaserMedium.class, laserMedium));
    }

    public void setLaserPulse(String pulse, int instrumentIndex,
            int lightSourceIndex)
    {
        Laser o = getLaser(instrumentIndex, lightSourceIndex);
        o.setPulse((Pulse) getEnumeration(Pulse.class, pulse));  
    }

    public void setLaserTuneable(Boolean tuneable, int instrumentIndex,
            int lightSourceIndex)
    {
        Laser o = getLaser(instrumentIndex, lightSourceIndex);
        o.setTuneable(toRType(tuneable));  
    }

    public void setLaserType(String type, int instrumentIndex,
            int lightSourceIndex)
    {
        Laser o = getLaser(instrumentIndex, lightSourceIndex);
        o.setType((LaserType) getEnumeration(LaserType.class, type)); 
    }

    public void setLaserWavelength(Integer wavelength, int instrumentIndex,
            int lightSourceIndex)
    {
        Laser o = getLaser(instrumentIndex, lightSourceIndex);
        o.setWavelength(toRType(wavelength));  
    }

    public void setLightSourceID(String id, int instrumentIndex,
            int lightSourceIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", instrumentIndex);
        indexes.put("lightSourceIndex", lightSourceIndex);  
        IObjectContainer o = getIObjectContainer(LightSource.class, indexes);
        o.LSID = id;
        containerCache.put(new LSID(id), o);
    }

    public void setLightSourceManufacturer(String manufacturer,
            int instrumentIndex, int lightSourceIndex)
    {
        LightSource o = getLightSource(instrumentIndex, lightSourceIndex);
        o.setManufacturer(toRType(manufacturer)); 
    }

    public LightSource getLightSource(int instrumentIndex, int lightSourceIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", instrumentIndex);
        indexes.put("lightSourceIndex", lightSourceIndex);
        return getSourceObject(LightSource.class, indexes);
    }

    public void setLightSourceModel(String model, int instrumentIndex,
            int lightSourceIndex)
    {
        LightSource o = getLightSource(instrumentIndex, lightSourceIndex);
        o.setModel(toRType(model)); 
    }

    public void setLightSourcePower(Float power, int instrumentIndex,
            int lightSourceIndex)
    {
        LightSource o = getLightSource(instrumentIndex, lightSourceIndex);
        o.setPower(toRType(power)); 
    }

    public void setLightSourceSerialNumber(String serialNumber,
            int instrumentIndex, int lightSourceIndex)
    {
        LightSource o = getLightSource(instrumentIndex, lightSourceIndex);
        o.setSerialNumber(toRType(serialNumber)); 
    }

    public void setLightSourceSettingsAttenuation(Float attenuation,
            int imageIndex, int logicalChannelIndex)
    {
        LightSettings o = getLightSettings(imageIndex, logicalChannelIndex);
        o.setAttenuation(toRType(attenuation)); 
    }

    private LightSettings getLightSettings(int imageIndex,
            int logicalChannelIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("imageIndex", imageIndex);
        indexes.put("logicalChannelIndex", logicalChannelIndex);
        return getSourceObject(LightSettings.class, indexes);
    }

    public void setLightSourceSettingsLightSource(String lightSource,
            int imageIndex, int logicalChannelIndex)
    {
        LSID key = new LSID(LightSettings.class, imageIndex, logicalChannelIndex);
        referenceCache.put(key.toString(), lightSource);
    }

    public void setLightSourceSettingsWavelength(Integer wavelength,
            int imageIndex, int logicalChannelIndex)
    {
         LightSettings o = getLightSettings(imageIndex, logicalChannelIndex);
        o.setWavelength(toRType(wavelength)); 
    }

    public void setLogicalChannelContrastMethod(String contrastMethod,
            int imageIndex, int logicalChannelIndex)
    {
        LogicalChannel o = getLogicalChannel(imageIndex, logicalChannelIndex);
        o.setContrastMethod((ContrastMethod) 
            getEnumeration(ContrastMethod.class, contrastMethod));
    }

    public LogicalChannel getLogicalChannel(int imageIndex,
            int logicalChannelIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("imageIndex", imageIndex);
        indexes.put("logicalChannelIndex", logicalChannelIndex);
        return getSourceObject(LogicalChannel.class, indexes);
    }

    public void setLogicalChannelEmWave(Integer emWave, int imageIndex,
            int logicalChannelIndex)
    {
        LogicalChannel o = getLogicalChannel(imageIndex, logicalChannelIndex);
        o.setEmissionWave(toRType(emWave));
    }

    public void setLogicalChannelExWave(Integer exWave, int imageIndex,
            int logicalChannelIndex)
    {
        LogicalChannel o = getLogicalChannel(imageIndex, logicalChannelIndex);
        o.setExcitationWave(toRType(exWave));
    }

    public void setLogicalChannelFluor(String fluor, int imageIndex,
            int logicalChannelIndex)
    {
        LogicalChannel o = getLogicalChannel(imageIndex, logicalChannelIndex);
        o.setFluor(toRType(fluor));
    }

    public void setLogicalChannelID(String id, int imageIndex,
            int logicalChannelIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("imageIndex", imageIndex);
        indexes.put("logicalChannelIndex", logicalChannelIndex);  
        IObjectContainer o = getIObjectContainer(LogicalChannel.class, indexes);
        o.LSID = id;
        containerCache.put(new LSID(id), o);
    }

    public void setLogicalChannelIlluminationType(String illuminationType,
            int imageIndex, int logicalChannelIndex)
    {
        LogicalChannel o = getLogicalChannel(imageIndex, logicalChannelIndex);
        o.setIllumination((Illumination) 
            getEnumeration(Illumination.class, illuminationType));
    }

    public void setLogicalChannelMode(String mode, int imageIndex,
            int logicalChannelIndex)
    {
        LogicalChannel o = getLogicalChannel(imageIndex, logicalChannelIndex);
        o.setMode((AcquisitionMode) 
            getEnumeration(AcquisitionMode.class, mode));
    }

    public void setLogicalChannelName(String name, int imageIndex,
            int logicalChannelIndex)
    {
        LogicalChannel o = getLogicalChannel(imageIndex, logicalChannelIndex);
        o.setName(toRType(name));
    }

    public void setLogicalChannelNdFilter(Float ndFilter, int imageIndex,
            int logicalChannelIndex)
    {
        LogicalChannel o = getLogicalChannel(imageIndex, logicalChannelIndex);
        o.setNdFilter(toRType(ndFilter));
    }

    public void setLogicalChannelPhotometricInterpretation(
            String photometricInterpretation, int imageIndex,
            int logicalChannelIndex)
    {
        LogicalChannel o = getLogicalChannel(imageIndex, logicalChannelIndex);
        o.setPhotometricInterpretation((PhotometricInterpretation) getEnumeration(
                    PhotometricInterpretation.class, photometricInterpretation));
    }

    public void setLogicalChannelPinholeSize(Float pinholeSize,
            int imageIndex, int logicalChannelIndex)
    {
        LogicalChannel o = getLogicalChannel(imageIndex, logicalChannelIndex);
        o.setPinHoleSize(toRType(pinholeSize));
    }

    public void setLogicalChannelPockelCellSetting(Integer pockelCellSetting,
            int imageIndex, int logicalChannelIndex)
    {
        LogicalChannel o = getLogicalChannel(imageIndex, logicalChannelIndex);
        o.setPockelCellSetting(toRType(pockelCellSetting));
    }

    public void setLogicalChannelSamplesPerPixel(Integer samplesPerPixel,
            int imageIndex, int logicalChannelIndex)
    {
    }

    public void setOTFID(String id, int instrumentIndex, int otfIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", instrumentIndex);
        indexes.put("otfIndex", otfIndex);  
        IObjectContainer o = getIObjectContainer(OTF.class, indexes);
       
        o.LSID = id;
        containerCache.put(new LSID(id), o);
    }

    public void setOTFOpticalAxisAveraged(Boolean opticalAxisAveraged,
            int instrumentIndex, int otfIndex)
    {
        OTF o = getOTF(instrumentIndex, otfIndex);
        o.setOpticalAxisAveraged(toRType(opticalAxisAveraged));
    }

    private OTF getOTF(int instrumentIndex, int otfIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", instrumentIndex);
        indexes.put("otfIndex", otfIndex);
        return getSourceObject(OTF.class, indexes);
    }

    public void setOTFPixelType(String pixelType, int instrumentIndex,
            int otfIndex)
    {
        OTF o = getOTF(instrumentIndex, otfIndex);
        o.setPixelsType((PixelsType) getEnumeration(PixelsType.class, pixelType));
    }

    public void setOTFSizeX(Integer sizeX, int instrumentIndex, int otfIndex)
    {
        OTF o = getOTF(instrumentIndex, otfIndex);
        o.setSizeX(toRType(sizeX));
    }

    public void setOTFSizeY(Integer sizeY, int instrumentIndex, int otfIndex)
    {
        OTF o = getOTF(instrumentIndex, otfIndex);
        o.setSizeY(toRType(sizeY));
    }

    public void setObjectiveIris(Boolean iris, int instrumentIndex,
            int objectiveIndex)
    {
        Objective o = getObjective(instrumentIndex, objectiveIndex);
        o.setIris(toRType(iris));
    }
    
    public Objective getObjective(int instrumentIndex, int objectiveIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", instrumentIndex);
        indexes.put("objectiveIndex", objectiveIndex);
        return getSourceObject(Objective.class, indexes);
    }

    public void setObjectiveCalibratedMagnification(
            Float calibratedMagnification, int instrumentIndex,
            int objectiveIndex)
    {
        Objective o = getObjective(instrumentIndex, objectiveIndex);
        o.setCalibratedMagnification(toRType(calibratedMagnification));
    }

    public void setObjectiveCorrection(String correction, int instrumentIndex,
            int objectiveIndex)
    {
        Objective o = getObjective(instrumentIndex, objectiveIndex);
        o.setCorrection((Correction) getEnumeration(Correction.class, correction));
    }

    public void setObjectiveID(String id, int instrumentIndex,
            int objectiveIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", instrumentIndex);
        indexes.put("objectiveIndex", objectiveIndex);  
        IObjectContainer o = getIObjectContainer(Objective.class, indexes);
        
        o.LSID = id;
        containerCache.put(new LSID(id), o);
    }

    public void setObjectiveImmersion(String immersion, int instrumentIndex,
            int objectiveIndex)
    {
        Objective o = getObjective(instrumentIndex, objectiveIndex);
        o.setImmersion((Immersion) getEnumeration(Immersion.class, immersion));
    }

    public void setObjectiveLensNA(Float lensNA, int instrumentIndex,
            int objectiveIndex)
    {
        Objective o = getObjective(instrumentIndex, objectiveIndex);
        o.setLensNA(toRType(lensNA));
    }

    public void setObjectiveManufacturer(String manufacturer,
            int instrumentIndex, int objectiveIndex)
    {
        Objective o = getObjective(instrumentIndex, objectiveIndex);
        o.setManufacturer(toRType(manufacturer));
    }

    public void setObjectiveModel(String model, int instrumentIndex,
            int objectiveIndex)
    {
        Objective o = getObjective(instrumentIndex, objectiveIndex);
        o.setModel(toRType(model));
    }

    public void setObjectiveNominalMagnification(Integer nominalMagnification,
            int instrumentIndex, int objectiveIndex)
    {
        Objective o = getObjective(instrumentIndex, objectiveIndex);
        o.setNominalMagnification(toRType(nominalMagnification));
    }

    public void setObjectiveSerialNumber(String serialNumber,
            int instrumentIndex, int objectiveIndex)
    {
        Objective o = getObjective(instrumentIndex, objectiveIndex);
        o.setSerialNumber(toRType(serialNumber));
    }

    public void setObjectiveWorkingDistance(Float workingDistance,
            int instrumentIndex, int objectiveIndex)
    {
        Objective o = getObjective(instrumentIndex, objectiveIndex);
        o.setWorkingDistance(toRType(workingDistance));
    }

    public void setPixelsBigEndian(Boolean bigEndian, int imageIndex,
            int pixelsIndex)
    {
    }

    public void setPixelsDimensionOrder(String dimensionOrder, int imageIndex,
            int pixelsIndex)
    {
        Pixels o = getPixels(imageIndex, pixelsIndex);
        o.setDimensionOrder((DimensionOrder) getEnumeration(DimensionOrder.class, dimensionOrder));
    }

    public void setPixelsID(String id, int imageIndex, int pixelsIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("imageIndex", imageIndex);
        indexes.put("pixelsIndex", pixelsIndex);  
        IObjectContainer o = getIObjectContainer(Pixels.class, indexes);
       
        o.LSID = id;
        containerCache.put(new LSID(id), o);
    }

    public void setPixelsPixelType(String pixelType, int imageIndex,
            int pixelsIndex)
    {
        Pixels o = getPixels(imageIndex, pixelsIndex);
        o.setPixelsType((PixelsType) getEnumeration(PixelsType.class, pixelType));
    }

    public void setPixelsSizeC(Integer sizeC, int imageIndex, int pixelsIndex)
    {
        Pixels o = getPixels(imageIndex, pixelsIndex);
        o.setSizeC(toRType(sizeC));
    }

    public void setPixelsSizeT(Integer sizeT, int imageIndex, int pixelsIndex)
    {
        Pixels o = getPixels(imageIndex, pixelsIndex);
        o.setSizeT(toRType(sizeT));
    }

    public void setPixelsSizeZ(Integer sizeZ, int imageIndex, int pixelsIndex)
    {
        Pixels o = getPixels(imageIndex, pixelsIndex);
        o.setSizeZ(toRType(sizeZ));
    }

    public void setPixelsSizeX(Integer sizeX, int imageIndex, int pixelsIndex)
    {       
        Pixels o = getPixels(imageIndex, pixelsIndex);
        o.setSizeX(toRType(sizeX));
    }

    public void setPixelsSizeY(Integer sizeY, int imageIndex, int pixelsIndex)
    {
        Pixels o = getPixels(imageIndex, pixelsIndex);
        o.setSizeY(toRType(sizeY));
    }

    public void setPlaneTheC(Integer theC, int imageIndex, int pixelsIndex,
            int planeIndex)
    {    
        PlaneInfo o = getPlaneInfo(imageIndex, pixelsIndex, planeIndex);
        o.setTheC(toRType(theC));
    }

    private PlaneInfo getPlaneInfo(int imageIndex, int pixelsIndex,
            int planeIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("imageIndex", imageIndex);
        indexes.put("pixelsIndex", pixelsIndex);
        indexes.put("planeIndex", planeIndex);
        return getSourceObject(PlaneInfo.class, indexes);
    }

    public void setPlaneTheT(Integer theT, int imageIndex, int pixelsIndex,
            int planeIndex)
    {
        PlaneInfo o = getPlaneInfo(imageIndex, pixelsIndex, planeIndex);
        o.setTheT(toRType(theT));
    }

    public void setPlaneTheZ(Integer theZ, int imageIndex, int pixelsIndex,
            int planeIndex)
    {
        PlaneInfo o = getPlaneInfo(imageIndex, pixelsIndex, planeIndex);
        o.setTheZ(toRType(theZ));
    }

    public void setPlaneTimingDeltaT(Float deltaT, int imageIndex,
            int pixelsIndex, int planeIndex)
    {
        PlaneInfo o = getPlaneInfo(imageIndex, pixelsIndex, planeIndex);
        o.setDeltaT(toRType(deltaT));
    }

    public void setPlaneTimingExposureTime(Float exposureTime, int imageIndex,
            int pixelsIndex, int planeIndex)
    {
        PlaneInfo o = getPlaneInfo(imageIndex, pixelsIndex, planeIndex);
        o.setExposureTime(toRType(exposureTime));
    }

    public void setPlateDescription(String description, int plateIndex)
    {
        Plate o = getPlate(plateIndex);
        o.setDescription(toRType(description));
    }

    private Plate getPlate(int plateIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("plateIndex", plateIndex);
        return getSourceObject(Plate.class, indexes);
    }

    public void setPlateExternalIdentifier(String externalIdentifier,
            int plateIndex)
    {
        Plate o = getPlate(plateIndex);
        o.setExternalIdentifier(toRType(externalIdentifier));
    }

    public void setPlateID(String id, int plateIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("plateIndex", plateIndex); 
        IObjectContainer o = getIObjectContainer(Plate.class, indexes);
       
        o.LSID = id;
        containerCache.put(new LSID(id), o);
    }

    public void setPlateName(String name, int plateIndex)
    {
        Plate o = getPlate(plateIndex);
        o.setName(toRType(name));
    }

    public void setPlateRefID(String id, int screenIndex, int plateRefIndex)
    {
    }

    public void setPlateStatus(String status, int plateIndex)
    {
        Plate o = getPlate(plateIndex);
        o.setStatus(toRType(status));
    }

    public void setROIID(String id, int imageIndex, int roiIndex)
    {
    }

    public void setROIT0(Integer t0, int imageIndex, int roiIndex)
    {
    }

    public void setROIT1(Integer t1, int imageIndex, int roiIndex)
    {
    }

    public void setROIX0(Integer x0, int imageIndex, int roiIndex)
    {
    }

    public void setROIX1(Integer x1, int imageIndex, int roiIndex)
    {
    }

    public void setROIY0(Integer y0, int imageIndex, int roiIndex)
    {
    }

    public void setROIY1(Integer y1, int imageIndex, int roiIndex)
    {
    }

    public void setROIZ0(Integer z0, int imageIndex, int roiIndex)
    {
    }

    public void setROIZ1(Integer z1, int imageIndex, int roiIndex)
    {
    }

    public void setReagentDescription(String description, int screenIndex,
            int reagentIndex)
    {
        Reagent o = getReagent(screenIndex, reagentIndex);
        o.setDescription(toRType(description));
    }

    private Reagent getReagent(int screenIndex, int reagentIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("screenIndex", screenIndex);
        indexes.put("reagentIndex", reagentIndex);
        return getSourceObject(Reagent.class, indexes);
    }

    public void setReagentID(String id, int screenIndex, int reagentIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("screenIndex", screenIndex);
        indexes.put("reagentIndex", reagentIndex);  
        IObjectContainer o = getIObjectContainer(Reagent.class, indexes);
       
        o.LSID = id;
        containerCache.put(new LSID(id), o);
    }

    public void setReagentName(String name, int screenIndex, int reagentIndex)
    {
        Reagent o = getReagent(screenIndex, reagentIndex);
        o.setName(toRType(name));
    }

    public void setReagentReagentIdentifier(String reagentIdentifier,
            int screenIndex, int reagentIndex)
    {
        Reagent o = getReagent(screenIndex, reagentIndex);
        o.setReagentIdentifier(toRType(reagentIdentifier));
    }

    public void setRoot(Object root)
    {
        log.debug(String.format("IGNORING: setRoot[%s]", root));
    }

    public void setScreenAcquisitionEndTime(String endTime, int screenIndex,
            int screenAcquisitionIndex)
    {
        ScreenAcquisition o = 
            getScreenAcquisition(screenIndex, screenAcquisitionIndex);
        
        try
        {
            SimpleDateFormat parser =
                new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ssZ");
            Timestamp ts = new Timestamp(parser.parse(endTime).getTime());
            o.setEndTime(toRType(ts));
        }
        catch (ParseException e)
        {
            log.error(String.format("Parsing start time failed!"), e);
        }
    }

    private ScreenAcquisition getScreenAcquisition(int screenIndex,
            int screenAcquisitionIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("screenIndex", screenIndex);
        indexes.put("screenAcquisitionIndex", screenAcquisitionIndex);
        return getSourceObject(ScreenAcquisition.class, indexes);
    }

    public void setScreenAcquisitionID(String id, int screenIndex,
            int screenAcquisitionIndex)
    {
        LSID lsid = 
            new LSID(ScreenAcquisition.class, screenIndex, screenAcquisitionIndex);
        IObjectContainer o = containerCache.get(lsid);
        o.LSID = id;
        containerCache.put(new LSID(id), o);
    }

    public void setScreenAcquisitionStartTime(String startTime,
            int screenIndex, int screenAcquisitionIndex)
    {
        ScreenAcquisition o = 
            getScreenAcquisition(screenIndex, screenAcquisitionIndex);
        
        try
        {
            SimpleDateFormat parser =
                new SimpleDateFormat("yyyy-MM-d'T'HH:mm:ssZ");
            Timestamp ts = new Timestamp(parser.parse(startTime).getTime());
            o.setStartTime(toRType(ts));
        }
        catch (ParseException e)
        {
            log.error(String.format("Parsing start time failed!"), e);
        }
    }

    public void setScreenID(String id, int screenIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("screenIndex", screenIndex); 
        IObjectContainer o = getIObjectContainer(Screen.class, indexes);
       
        o.LSID = id;
        containerCache.put(new LSID(id), o);
    }

    public void setScreenName(String name, int screenIndex)
    {
        Screen o = getScreen(screenIndex);
        o.setName(toRType(name));
    }

    private Screen getScreen(int screenIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("screenIndex", screenIndex);
        return getSourceObject(Screen.class, indexes);
    }

    public void setScreenProtocolDescription(String protocolDescription,
            int screenIndex)
    {
        Screen o = getScreen(screenIndex);
        o.setProtocolDescription(toRType(protocolDescription));
    }

    public void setScreenProtocolIdentifier(String protocolIdentifier,
            int screenIndex)
    {
        Screen o = getScreen(screenIndex);
        o.setProtocolIdentifier(toRType(protocolIdentifier));
    }

    public void setScreenReagentSetDescription(String reagentSetDescription,
            int screenIndex)
    {
        Screen o = getScreen(screenIndex);
        o.setReagentSetDescription(toRType(reagentSetDescription));
    }

    public void setScreenType(String type, int screenIndex)
    {
        Screen o = getScreen(screenIndex);
        o.setType(toRType(type));
    }

    public void setStageLabelName(String name, int imageIndex)
    {
        StageLabel o = getStageLabel(imageIndex);
        o.setName(toRType(name));
    }

    private StageLabel getStageLabel(int imageIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("imageIndex", imageIndex);
        return getSourceObject(StageLabel.class, indexes);
    }

    public void setStageLabelX(Float x, int imageIndex)
    {
        StageLabel o = getStageLabel(imageIndex);
        o.setPositionX(toRType(x));
    }

    public void setStageLabelY(Float y, int imageIndex)
    {
        StageLabel o = getStageLabel(imageIndex);
        o.setPositionY(toRType(y));
    }

    public void setStageLabelZ(Float z, int imageIndex)
    {
        StageLabel o = getStageLabel(imageIndex);
        o.setPositionZ(toRType(z));
    }

    public void setStagePositionPositionX(Float positionX, int imageIndex,
            int pixelsIndex, int planeIndex)
    {    
    	PlaneInfo o = getPlaneInfo(imageIndex, pixelsIndex, planeIndex);
    	o.setPositionX(toRType(positionX));
    }

    public void setStagePositionPositionY(Float positionY, int imageIndex,
            int pixelsIndex, int planeIndex)
    {
    	PlaneInfo o = getPlaneInfo(imageIndex, pixelsIndex, planeIndex);
    	o.setPositionY(toRType(positionY));
    }

    public void setStagePositionPositionZ(Float positionZ, int imageIndex,
            int pixelsIndex, int planeIndex)
    {
    	PlaneInfo o = getPlaneInfo(imageIndex, pixelsIndex, planeIndex);
    	o.setPositionZ(toRType(positionZ));
    }

    public void setTiffDataFileName(String fileName, int imageIndex,
            int pixelsIndex, int tiffDataIndex)
    {
    }

    public void setTiffDataFirstC(Integer firstC, int imageIndex,
            int pixelsIndex, int tiffDataIndex)
    {
    }

    public void setTiffDataFirstT(Integer firstT, int imageIndex,
            int pixelsIndex, int tiffDataIndex)
    {
    }

    public void setTiffDataFirstZ(Integer firstZ, int imageIndex,
            int pixelsIndex, int tiffDataIndex)
    {
    }

    public void setTiffDataIFD(Integer ifd, int imageIndex, int pixelsIndex,
            int tiffDataIndex)
    {
    }

    public void setTiffDataNumPlanes(Integer numPlanes, int imageIndex,
            int pixelsIndex, int tiffDataIndex)
    {
    }

    public void setTiffDataUUID(String uuid, int imageIndex, int pixelsIndex,
            int tiffDataIndex)
    {
    }

    public void setUUID(String uuid)
    {
    }

    public void setWellColumn(Integer column, int plateIndex, int wellIndex)
    {
        Well o = getWell(plateIndex, wellIndex);
        o.setColumn(toRType(column));
    }

    private Well getWell(int plateIndex, int wellIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("plateIndex", plateIndex);
        indexes.put("wellIndex", wellIndex);
        return getSourceObject(Well.class, indexes);
    }

    public void setWellExternalDescription(String externalDescription,
            int plateIndex, int wellIndex)
    {
        Well o = getWell(plateIndex, wellIndex);
        o.setExternalDescription(toRType(externalDescription));
    }

    public void setWellExternalIdentifier(String externalIdentifier,
            int plateIndex, int wellIndex)
    {
        Well o = getWell(plateIndex, wellIndex);
        o.setExternalIdentifier(toRType(externalIdentifier));
    }

    public void setWellID(String id, int plateIndex, int wellIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("plateIndex", plateIndex);
        indexes.put("wellIndex", wellIndex);  
        IObjectContainer o = getIObjectContainer(Well.class, indexes);
       
        o.LSID = id;
        containerCache.put(new LSID(id), o);
    }

    public void setWellRow(Integer row, int plateIndex, int wellIndex)
    {
        Well o = getWell(plateIndex, wellIndex);
        o.setRow(toRType(row));
    }

    public void setWellSampleID(String id, int plateIndex, int wellIndex,
            int wellSampleIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("plateIndex", plateIndex);
        indexes.put("wellIndex", wellIndex);  
        IObjectContainer o = getIObjectContainer(WellSample.class, indexes);
       
        o.LSID = id;
        containerCache.put(new LSID(id), o);
    }

    public void setWellSampleIndex(Integer index, int plateIndex,
            int wellIndex, int wellSampleIndex)
    {
    }

    public void setWellSamplePosX(Float posX, int plateIndex, int wellIndex,
            int wellSampleIndex)
    {
        WellSample o = getWellSample(plateIndex, wellIndex, wellSampleIndex);
        o.setPosX(toRType(posX));
    }

    private WellSample getWellSample(int plateIndex, int wellIndex,
            int wellSampleIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("plateIndex", plateIndex);
        indexes.put("wellIndex", wellIndex);
        indexes.put("wellSampleIndex", wellSampleIndex);
        return getSourceObject(WellSample.class, indexes);
    }

    public void setWellSamplePosY(Float posY, int plateIndex, int wellIndex,
            int wellSampleIndex)
    {
        WellSample o = getWellSample(plateIndex, wellIndex, wellSampleIndex);
        o.setPosY(toRType(posY));
    }

    public void setWellSampleTimepoint(Integer timepoint, int plateIndex,
            int wellIndex, int wellSampleIndex)
    {
        WellSample o = getWellSample(plateIndex, wellIndex, wellSampleIndex);
        o.setTimepoint(toRType(timepoint));
    }

    public void setWellType(String type, int plateIndex, int wellIndex)
    {
        Well o = getWell(plateIndex, wellIndex);
        o.setType(toRType(type));
    }
    
    public long getExperimenterID()
    {
        try
        {
            return iAdmin.getEventContext().userId;
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void writeFilesToFileStore(File[] files, Long pixId)
    {
        // TODO Auto-generated method stub
        //
        throw new RuntimeException("Not implemented yet.");
    }
    
    public long getRepositorySpace()
    {
        try
        {
            return iRepoInfo.getFreeSpaceInKilobytes();
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public List<Pixels> saveToDB()
    {
    	Collection<IObjectContainer> containers = containerCache.values();
    	IObjectContainer[] containerArray = 
    		containers.toArray(new IObjectContainer[containers.size()]);
        try
        {
        	delegate.updateObjects(containerArray);
        	delegate.updateReferences(referenceCache);
        	pixelsList = delegate.saveToDB();
        	return pixelsList;
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void addImageToDataset(Image image, Dataset dataset)
    {   
        try
        {
            Image unloadedImage = new ImageI(image.getId(), false);
            Dataset unloadedDataset = new DatasetI(dataset.getId(), false);
            DatasetImageLink l = new DatasetImageLinkI();
            l.setChild(unloadedImage);
            l.setParent(unloadedDataset);
            iUpdate.saveObject(l);
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public Project getProject(long projectId)
    {
        try
        {
            return (Project) iQuery.get("Project", projectId);
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public Dataset getDataset(long datasetId)
    {
        try
        {
            return (Dataset) iQuery.get("Dataset", datasetId);
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void addBooleanAnnotationToPixels(BooleanAnnotation annotation,
            Pixels pixels)
    {
        try
        {
            Pixels unloadedPixels = new PixelsI(pixels.getId(), false);
            PixelsAnnotationLink l = new PixelsAnnotationLinkI();
            l.setChild(annotation);
            l.setParent(unloadedPixels);
            iUpdate.saveObject(l);
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public Dataset addDataset(String datasetName, String datasetDescription,
            Project project)
    {
        Dataset dataset = new DatasetI();
        if (datasetName.length() != 0)
            dataset.setName(toRType(datasetName));
        if (datasetDescription.length() != 0)
            dataset.setDescription(toRType(datasetDescription));
        Project p = new ProjectI(project.getId().getValue(), false);
        dataset.linkProject(p);

        try
        {
            return (Dataset) iUpdate.saveAndReturnObject(dataset);
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

 // FIXME: change to iQuery
    public List<Project> getProjects()
    {
        try
        {
            return delegate.getProjects();
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

 // FIXME: change to iQuery
    public List<Dataset> getDatasets(Project p)
    {
        try
        {
            return delegate.getDatasets(p);
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public Project addProject(String projectName, String projectDescription)
    {
        Project project = new ProjectI();
        if (projectName.length() != 0)
            project.setName(toRType(projectName));
        if (projectDescription.length() != 0)
            project.setDescription(toRType(projectDescription));

        try
        {
            return (Project) iUpdate.saveAndReturnObject(project);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public void setOriginalFiles(File[] files, String formatString)
    {
        // TODO Implement
        //
        throw new RuntimeException("Not implemented yet.");
    }

    public void setPlane(Long pixId, byte[] arrayBuf, int z, int c, int t) throws ServerError
    {
            if (currentPixId != pixId)
            {
                rawPixelStore.setPixelsId(pixId);
                currentPixId = pixId;
            }
            rawPixelStore.setPlane(arrayBuf, z, c, t);
    }

    public void setChannelGlobalMinMax(int channel, double minimum,
            double maximum, int series)
    {
        try
        {
            delegate.setChannelGlobalMinMax(channel, toRType(minimum), toRType(maximum), toRType(series));
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void populateSHA1(MessageDigest md, Long id)
    {
        Pixels p;
        try
        {
            p = (Pixels) iQuery.get("Pixels", id);
            p.setSha1(toRType(byteArrayToHexString(md.digest())));
            iUpdate.saveObject(p);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    static String byteArrayToHexString(byte in[]) {

        byte ch = 0x00;
        int i = 0;

        if (in == null || in.length <= 0) {
            return null;
        }

        String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                "a", "b", "c", "d", "e", "f" };

        StringBuffer out = new StringBuffer(in.length * 2);

        while (i < in.length) {

            ch = (byte) (in[i] & 0xF0);
            ch = (byte) (ch >>> 4);
            ch = (byte) (ch & 0x0F);
            out.append(pseudo[ch]);
            ch = (byte) (in[i] & 0x0F);
            out.append(pseudo[ch]);
            i++;

        }

        String rslt = new String(out);
        return rslt;
    }

    public void populateMinMax(Long id, Integer i)
    {
        try
        {
            delegate.populateMinMax(toRType(id), toRType(i));
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setExperimentDescription(String description, int experimentIndex)
    {
        Experiment o = getExperiment(experimentIndex);
        o.setDescription(toRType(description));
    }

    private Experiment getExperiment(int experimentIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("experimentIndex", experimentIndex);
        return getSourceObject(Experiment.class, indexes);
    }

    public void setExperimentID(String id, int experimentIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("experimentIndex", experimentIndex);
        IObjectContainer o = getIObjectContainer(Experiment.class, indexes);
        
        o.LSID = id;
        containerCache.put(new LSID(id), o);
    }

    public void setExperimentType(String type, int experimentIndex)
    {
        Experiment o = getExperiment(experimentIndex);
        o.setType((ExperimentType) getEnumeration(ExperimentType.class, type));
    }

    public void setExperimenterMembershipGroup(String group,
            int experimenterIndex, int groupRefIndex)
    {
    }

    public void setImageDefaultPixels(String defaultPixels, int imageIndex)
    {
        LSID key = new LSID(Image.class, imageIndex);
        referenceCache.put(key.toString(), defaultPixels);
    }

    public void setLogicalChannelOTF(String otf, int imageIndex,
            int logicalChannelIndex)
    {
        LSID key = new LSID(LogicalChannel.class, imageIndex, logicalChannelIndex);
        referenceCache.put(key.toString(), otf);
    }

    public void setOTFObjective(String objective, int instrumentIndex,
            int otfIndex)
    {
        LSID key = new LSID(OTF.class, instrumentIndex, otfIndex);
        referenceCache.put(key.toString(), objective);
    }

    /* ---- Objective Settings ---- */
    
    public void setObjectiveSettingsCorrectionCollar(Float correctionCollar,
            int imageIndex)
    {
        ObjectiveSettings o = getObjectiveSettings(imageIndex);
        o.setCorrectionCollar(toRType(correctionCollar));
    }

    private ObjectiveSettings getObjectiveSettings(int imageIndex)
    {
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("imageIndex", imageIndex);
        return getSourceObject(ObjectiveSettings.class, indexes);
    }

    public void setObjectiveSettingsMedium(String medium, int imageIndex)
    {
        ObjectiveSettings o = getObjectiveSettings(imageIndex);
        o.setMedium((Medium) getEnumeration(Medium.class, medium));
    }

    public void setObjectiveSettingsObjective(String objective, int imageIndex)
    {
        LSID key = new LSID(ObjectiveSettings.class, imageIndex);
        referenceCache.put(key.toString(), objective);
    }

    public void setObjectiveSettingsRefractiveIndex(Float refractiveIndex,
            int imageIndex)
    {
        ObjectiveSettings o = getObjectiveSettings(imageIndex);
        o.setRefractiveIndex(toRType(refractiveIndex));
    }
    
    /**
     * Sets the active enumeration provider.
     * @param enumProvider Enumeration provider to use.
     */
    public void setEnumerationProvider(EnumerationProvider enumProvider)
    {
        this.enumProvider = enumProvider;
    }
    
    /**
     * Retriives the active enumeration provider.
     * @return See above.
     */
    public EnumerationProvider getEnumerationProvider()
    {
        return enumProvider;
    }
    
    /**
     * Sets the active instance provider.
     * @param enumProvider Enumeration provider to use.
     */
    public void setInstanceProvider(InstanceProvider instanceProvider)
    {
        this.instanceProvider = instanceProvider;
    }
    
    /**
     * Retrieves the active enumeration provider.
     * @return See above.
     */
    public InstanceProvider getInstanceProvider()
    {
        return instanceProvider;
    }
    
    /**
     * This comparator takes into account the OME-XML data model hierarchy
     * and uses that to define equivalence.
     * 
     * @author Chris Allan <callan at blackcat dot ca>
     *
     */
    public class OMEXMLModelComparator implements Comparator<LSID>
    {
    	/** 
    	 * The collator that we use to alphabetically sort by class name
    	 * within a given level of the OME-XML hierarchy.
    	 */
    	private RuleBasedCollator stringComparator = 
    		(RuleBasedCollator) Collator.getInstance(Locale.ENGLISH);
    	
		public int compare(LSID x, LSID y)
		{
			// Handle identical LSIDs
			if (x.equals(y))
			{
				return 0;
			}
			
			// Parse the LSID for hierarchical equivalence tests.
			Class<? extends IObject> xClass = x.getJavaClass();
			Class<? extends IObject> yClass = y.getJavaClass();
			int[] xIndexes = x.getIndexes();
			int[] yIndexes = y.getIndexes();
			
			// Handle the null class (unparsable internal reference) case.
			if (xClass == null)
			{
				if (yClass == null)
				{
					// Handle different supplied LSIDs by string difference.
					return stringComparator.compare(x.toString(), y.toString());
				}
				return 1;
			}
			if (yClass == null)
			{
				return -1;
			}

			// Assign values to the classes
			int xVal = getValue(xClass, xIndexes.length);
			int yVal = getValue(yClass, yIndexes.length);
			
			int retval = xVal - yVal;
			if (retval == 0)
			{
				// Handle different classes at the same level in the hierarchy
				// by string difference. They need to still be different.
				if (!xClass.equals(yClass))
				{
					return stringComparator.compare(x.toString(), y.toString());
				}
				for (int i = 0; i < xIndexes.length; i++)
				{
					int difference = xIndexes[i] - yIndexes[i];
					if (difference != 0)
					{
						return difference;
					}
				}
				return 0;
			}
			return retval;
		}
		
		/**
		 * Assigns a value to a particular class based on its location in the
		 * OME-XML hierarchy.
		 * @param klass Class to assign a value to.
		 * @param indexed Number of class indexes that were present in its LSID.
		 * @return The value.
		 */
		public int getValue(Class<? extends IObject> klass, int indexes)
		{
			// Top-level (Pixels is a special case due to Channel and
			// LogicalChannel containership weirdness).
			if (klass.equals(Pixels.class))
			{
				return 1;
			}
			
			if (klass.equals(DetectorSettings.class) 
			    || klass.equals(LightSettings.class)
			    || klass.equals(LogicalChannel.class))
			{
			    return 3;
			}
			
			return indexes;
		}
    }
}
