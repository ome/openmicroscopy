package ome.formats;

import java.io.File;
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
import ome.formats.model.ChannelProcessor;
import ome.formats.model.IObjectContainerStore;
import ome.formats.model.InstrumentProcessor;
import ome.formats.model.PixelsProcessor;
import ome.formats.model.InstanceProvider;
import ome.formats.model.ModelProcessor;
import ome.formats.model.ReferenceProcessor;
import omero.RBool;
import omero.RDouble;
import omero.RInt;
import omero.RLong;
import omero.RString;
import omero.RTime;
import omero.ServerError;
import omero.client;
import omero.api.IAdminPrx;
import omero.api.IContainerPrx;
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
import omero.model.BooleanAnnotationI;
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

import loci.formats.IFormatReader;
import loci.formats.meta.IMinMaxStore;
import loci.formats.meta.MetadataStore;


public class OMEROMetadataStoreClient
	implements MetadataStore, IMinMaxStore, IObjectContainerStore
{
	/** Logger for this class */
	private Log log = LogFactory.getLog(OMEROMetadataStoreClient.class);
	
    private MetadataStorePrx delegate;
    
    /** Our IObject container cache. */
    private Map<LSID, IObjectContainer> containerCache = 
    	new TreeMap<LSID, IObjectContainer>(new OMEXMLModelComparator());
    
    /** Our LSID reference cache. */
    private Map<LSID, LSID> referenceCache = new HashMap<LSID, LSID>();
    
    /** 
     * Our string based reference cache. This will be populated after all
     * model population has been completed by a ReferenceProcessor. 
     */
    private Map<String, String> referenceStringCache;

    /** Our model processors. Will be called on saveToDB(). */
    private List<ModelProcessor> modelProcessors = 
    	new ArrayList<ModelProcessor>();
    
    /** Bio-Formats reader that's populating us. */
    private IFormatReader reader;
    
    /** Namespace for our archival annotation. */
    private static final String ARCHIVE_ANN_NS = 
    	"openmicroscopy.org/omero/importer/archived";
    
    private List<Pixels> pixelsList;
    
    private client c;
    private ServiceFactoryPrx serviceFactory;
    private IUpdatePrx iUpdate;
    private IQueryPrx iQuery;
    private IAdminPrx iAdmin;
    private RawFileStorePrx rawFileStore;
    private RawPixelsStorePrx rawPixelStore;
    private IRepositoryInfoPrx iRepoInfo;
    private IContainerPrx iContainer;
    
    /** Our enumeration provider. */
    private EnumerationProvider enumProvider;
    
    /** Our OMERO model object provider. */
    private InstanceProvider instanceProvider;

    /** Current pixels ID we're writing planes for. */
    private Long currentPixId;
    
    /** Image name that the user specified for use by model processors. */
    private String userSpecifiedImageName;
    
    /** Image channel minimums and maximums. */
    private double[][][] imageChannelGlobalMinMax;

    private void initializeServices()
    	throws ServerError
    {
    	// Blitz services
    	iUpdate = serviceFactory.getUpdateService();
    	iQuery = serviceFactory.getQueryService();
    	iAdmin = serviceFactory.getAdminService();
    	rawFileStore = serviceFactory.createRawFileStore();
    	rawPixelStore = serviceFactory.createRawPixelsStore();
    	iRepoInfo = serviceFactory.getRepositoryInfoService();
    	iContainer = serviceFactory.getContainerService();
    	delegate = MetadataStorePrxHelper.checkedCast(serviceFactory.getByName(METADATASTORE.value));

    	// Client side services
    	enumProvider = new IQueryEnumProvider(iQuery);
    	instanceProvider = new BlitzInstanceProvider(enumProvider);
    	
    	// Default model processors
    	modelProcessors.add(new ReferenceProcessor());
        modelProcessors.add(new PixelsProcessor());
    	modelProcessors.add(new ChannelProcessor());
    	modelProcessors.add(new InstrumentProcessor());
    }
    
    public IQueryPrx getIQuery()
    {
        return iQuery;
    }
    
    /**
     * Initializes the MetadataStore with an already logged in, ready to go
     * service factory.
     * @param serviceFactory The factory. Mustn't be <code>null</code>.
     */
    public void initialize(ServiceFactoryPrx serviceFactory)
    	throws ServerError
    {
    	if (serviceFactory == null)
    		throw new IllegalArgumentException("No factory.");
    	this.serviceFactory = serviceFactory;
    	initializeServices();
    }
    
    /**
     * Initializes the MetadataStore taking string parameters to feed to the 
     * OMERO Blitz client object.
     * @param username User's omename.
     * @param password User's password.
     * @param server Server hostname.
     * @param port Server port.
     * @throws CannotCreateSessionException If there is a session error when
     * creating the OMERO Blitz client object.
     * @throws PermissionDeniedException If there is a problem logging the user
     * in.
     * @throws ServerError If there is a critical error communicating with the
     * server.
     */
    public void initialize(String username, String password,
                           String server, int port)
        throws CannotCreateSessionException, PermissionDeniedException, ServerError
    {
        c = new client(server, port);
        serviceFactory = c.createSession(username, password);
        initializeServices();
    }
    
    /**
     * Initializes the MetadataStore by joining an existing session.
     * @param server Server hostname.
     * @param port Server port.
     * @param sessionKey Bind session key.
     */
    public void initialize(String server, int port, String sessionKey)
        throws CannotCreateSessionException, PermissionDeniedException, ServerError
    {
        c = new client(server, port);
        serviceFactory = c.joinSession(sessionKey);
        initializeServices();
    }

    /**
     * Pings all registered OMERO Blitz proxies. 
     */
    public void ping()
    {
        serviceFactory.keepAllAlive(new ServiceInterfacePrx[] 
                {iQuery, iAdmin, rawFileStore, rawPixelStore, iRepoInfo,
                 iContainer, iUpdate, delegate});
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
    public RDouble toRType(Float value)
    {
        return value == null? null : rdouble(Double.parseDouble(value.toString()));
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
    
    /**
     * Destroys the sessionFactor and closes the client
     * @return <code>null</code>
     */
    public void logout()
    {
        serviceFactory.destroy();
        c.close();
    }
    
    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#createRoot()
     */
    public void createRoot()
    {
        try
        {
            System.err.println("Creating root!");
            containerCache = 
                new TreeMap<LSID, IObjectContainer>(new OMEXMLModelComparator());
            referenceCache = new HashMap<LSID, LSID>();
            referenceStringCache = null;
            delegate.createRoot();
        }
        catch (ServerError e)
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
    
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#getReader()
     */
    public IFormatReader getReader()
    {
    	return reader;
    }
    
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#setReader(loci.formats.IFormatReader)
     */
    public void setReader(IFormatReader reader)
    {
    	this.reader = reader;
    }

    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#getUserSpecifiedImageName()
     */
    public String getUserSpecifiedImageName()
    {
        return userSpecifiedImageName;
    }

    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#setUserSpecifiedImageName(java.lang.String)
     */
    public void setUserSpecifiedImageName(String name)
    {
        this.userSpecifiedImageName = name;
    }
    
    /**
     * Retrieves the current list of model processors the metadata store is
     * using.
     * @return See above. 
     */
    public List<ModelProcessor> getModelProcessors()
    {
    	return modelProcessors;
    }
    
    /**
     * Sets the current set of model processors.
     * @param modelProcessors List of model processors to use.
     */
    public void setModelProcessors(List<ModelProcessor> modelProcessors)
    {
    	this.modelProcessors = modelProcessors;
    }
    
    /**
     * Removes a model processor from use.
     * @param processor Model processor to remove.
     */
    public void removeModelProcessor(ModelProcessor processor)
    {
    	modelProcessors.remove(processor);
    }
    
    /**
     * Adds a model processor to the end of the processing chain.
     * @param processor Model processor to add.
     * @return <code>true</code> as specified by {@link Collection.add(E)}.
     */
    public boolean addModelProcessor(ModelProcessor processor)
    {
    	return modelProcessors.add(processor);
    }

    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#getContainerCache()
     */
    public Map<LSID, IObjectContainer> getContainerCache()
    {
    	return containerCache;
    }
    
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#getReferenceCache()
     */
    public Map<LSID, LSID> getReferenceCache()
    {
    	return referenceCache;
    }
    
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#getReferenceStringCache()
     */
    public Map<String, String> getReferenceStringCache()
    {
    	return referenceStringCache;
    }
    
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#setReferenceStringCache(java.util.Map)
     */
    public void setReferenceStringCache(Map<String, String> referenceStringCache)
    {
    	this.referenceStringCache = referenceStringCache;
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
    
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#getSourceObject(ome.formats.LSID)
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
    
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#getSourceObjects(java.lang.Class)
     */
    public <T extends IObject> List<T> getSourceObjects(Class<T> klass)
    {
        List<IObjectContainer> containers = getIObjectContainers(klass);
    	List<T> toReturn = new ArrayList<T>(containers.size());
    	for (IObjectContainer container: containers)
    	{
    	    toReturn.add((T) container.sourceObject);
    	}
    	return toReturn;
    }
    
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#hasReference(ome.formats.LSID, ome.formats.LSID)
     */
    public boolean hasReference(LSID source, LSID target)
    {
        if (!referenceCache.containsKey(source)
            || !referenceCache.containsValue(target))
        {
            return false;
        }
        return true;
    }
    
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#getIObjectContainer(java.lang.Class, java.util.LinkedHashMap)
     */
    public IObjectContainer getIObjectContainer(Class<? extends IObject> klass,
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
        LSID lsid = new LSID(klass, indexesArray);
        
        // Because of the LightSource abstract type, here we need to handle
        // the upcast to the "real" concrete type and the correct LSID
        // mapping.
        if ((klass.equals(Arc.class) || klass.equals(Laser.class)
            || klass.equals(Filament.class))
            && !containerCache.containsKey(lsid))
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
                if (container.LSID == null
                    || container.LSID.equals(lsLSID.toString()))
                {
                    container.LSID = lsid.toString();
                }
                containerCache.put(lsid, container);
                return container;
            }
        }
        // We may have first had a concrete method call request, put the object
        // in a container and in the cache. Now we have a request with only the
        // abstract type's class to give us LSID resolution and must handle 
        // that as well.
        if (klass.equals(LightSource.class)
        	&& !containerCache.containsKey(lsid))
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
        
        if (!containerCache.containsKey(lsid))
        {
            IObjectContainer c = new IObjectContainer();
            c.indexes = indexes;
            c.LSID = lsid.toString();
            c.sourceObject = getSourceObjectInstance(klass);
            containerCache.put(lsid, c);
        }
        
        return containerCache.get(lsid);
    }
    
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#getIObjectContainers(java.lang.Class)
     */
    public List<IObjectContainer> getIObjectContainers(Class<? extends IObject> klass)
    {
        Set<LSID> keys = containerCache.keySet();
        List<IObjectContainer> toReturn = new ArrayList<IObjectContainer>();
        for (LSID key : keys)
        {
            Class<? extends IObject> keyClass = key.getJavaClass();
            if (keyClass != null && keyClass.equals(klass))
            {
                toReturn.add(containerCache.get(key));
            }
        }
        return toReturn;
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
        
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#countCachedContainers(java.lang.Class, int[])
     */
    public int countCachedContainers(Class<? extends IObject> klass,
    		                         int... indexes)
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
            	if (indexes != null)
            	{
            		int[] lsidIndexes = lsid.getIndexes();
            		for (int i = 0; i < indexes.length; i++)
            		{
            			if (lsidIndexes[i] != indexes[i])
            			{
            				continue;
            			}
            		}
            	}
                count++;
            }
        }
        return count;
    }
    
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#countCachedReferences(java.lang.Class, java.lang.Class)
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
            for (LSID lsid : referenceCache.keySet())
            {
                Class containerClass = lsid.getJavaClass();
                if (containerClass.equals(source))
                {
                    count++;
                }
            }
            return count;
        }
        
        if (source == null)
        {
            for (LSID lsid : referenceCache.values())
            {
                Class containerClass = lsid.getJavaClass();
                if (containerClass.equals(target))
                {
                    count++;
                }
            }
            return count;
        }
        
        for (LSID lsid : referenceCache.keySet())
        {
            Class containerClass = lsid.getJavaClass();
            if (containerClass.equals(source.getName()))
            {
                Class targetClass = referenceCache.get(lsid).getJavaClass();
                if (targetClass.equals(target.getName()))
                {
                    count++;
                }
            }
        }
        return count;
    }
    
    /**
     * Populates archive flags on all images currently processed. This method
     * should only be called <b>after</b> a full Bio-Formats metadata parsing
     * cycle. 
     * @param archive Whether or not the user requested the original files to
     * be archived.
     */
    public void setArchive(boolean archive)
    {
    	// First create our annotation for linkage
    	LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
    	indexes.put("annotationIndex", 0);
    	BooleanAnnotation annotation = 
    		getSourceObject(BooleanAnnotation.class, indexes);
        annotation.setBoolValue(rbool(archive));
        annotation.setNs(rstring(ARCHIVE_ANN_NS));
        
        // Now link this annotation to all images we have in cache
        int imageCount = countCachedContainers(Image.class);
        for (int i = 0; i < imageCount; i++)
        {
        	LSID key = new LSID(Image.class, i);
        	referenceCache.put(key, new LSID(BooleanAnnotation.class, 0));
        }
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
        referenceCache.put(key, new LSID(detector));
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
        o.setPhysicalSizeY(toRType(physicalSizeY));
    }

    public void setDimensionsPhysicalSizeZ(Float physicalSizeZ, int imageIndex,
            int pixelsIndex)
    {
        Pixels o = getPixels(imageIndex, pixelsIndex);
        o.setPhysicalSizeZ(toRType(physicalSizeZ));
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
    }

    public void setImageInstrumentRef(String instrumentRef, int imageIndex)
    {
        LSID key = new LSID(Image.class, imageIndex);
        referenceCache.put(key, new LSID(instrumentRef));
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
        referenceCache.put(key, new LSID(lightSource));
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
        indexes.put("wellSampleIndex", wellSampleIndex);
        IObjectContainer o = getIObjectContainer(WellSample.class, indexes);
        o.LSID = id;
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
    
    /**
     * Post processes the internal structure of the client side MetadataStore.
     * Should be called before {@link saveToDB()}.
     */
    public void postProcess()
    {
		// Perform model processing
		for (ModelProcessor processor : modelProcessors)
		{
			processor.process(this);
		}
    }

    /**
     * Updates the server side MetadataStore with a list of our objects and
     * references and saves them into the database.
     * @return List of Pixels after database commit.
     */
    public List<Pixels> saveToDB()
    {
    	try
    	{
        	Collection<IObjectContainer> containers = containerCache.values();
        	IObjectContainer[] containerArray = 
        		containers.toArray(new IObjectContainer[containers.size()]);
            
            for (LSID key : containerCache.keySet())
            {
                System.err.println(key + " == " + containerCache.get(key).sourceObject
                        + "," + containerCache.get(key).LSID);
            }
            
            System.err.println("\nStarting references....");

            for (String key : referenceStringCache.keySet())
            {
                System.err.println(key + " == " + referenceStringCache.get(key));
            }
            
            System.err.println("\ncontainerCache contains " + containerCache.size() + " entries.");
            System.err.println("referenceCache contains " + referenceCache.size() + " entries.");
            
        	delegate.updateObjects(containerArray);
        	delegate.updateReferences(referenceStringCache);
        	pixelsList = delegate.saveToDB();
        	
        	for (Pixels pixels : pixelsList)
        	{
        	    System.err.println("Saving pixels id: "  + pixels.getId().getValue());
        	}
        	return pixelsList;
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Links the Image objects of Pixels returned after a {@link saveToDb()}
     * action to a particular dataset.
     * @param pixelsList List of Pixels objects whose Images we are to link.
     * @param dataset Dataset to link to.
     */
    public void addImagesToDataset(List<Pixels> pixelsList, Dataset dataset)
    {   
        try
        {
        	List<IObject> links = 
        		new ArrayList<IObject>(pixelsList.size());
        	Dataset unloadedDataset = new DatasetI(dataset.getId(), false);
        	for (int i = 0; i < pixelsList.size(); i++)
        	{
        		RLong imageId = pixelsList.get(i).getImage().getId();
        		Image unloadedImage = new ImageI(imageId, false);
                DatasetImageLink l = new DatasetImageLinkI();
                l.setChild(unloadedImage);
                l.setParent(unloadedDataset);
        		links.add(l);
        	}
        	iUpdate.saveArray(links);
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

    public List<Project> getProjects()
    {
    	try
    	{
    		List<IObject> objects = 
    			iContainer.loadContainerHierarchy(Project.class.getName(), null, null);
    		List<Project> projects = new ArrayList<Project>(objects.size());
    		for (IObject object : objects)
    		{
    			projects.add((Project) object);
    		}
    		return projects;
    	}
    	catch (ServerError e)
    	{
    		throw new RuntimeException(e);
    	}
    }

    public List<Dataset> getDatasets(Project p)
    {
    	try
    	{
    		List<Long> ids = new ArrayList<Long>(1);
    		ids.add(p.getId().getValue());
    		List<IObject> objects = 
    			iContainer.loadContainerHierarchy(Project.class.getName(), ids, null);
    		if (objects.size() > 0)
    		{
    		    Project project = (Project) objects.get(0);
    		    return project.linkedDatasetList();
    		}
    		return null;
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

    }

    public void setPlane(Long pixId, byte[] arrayBuf, int z, int c, int t) throws ServerError
    {
            if (currentPixId != pixId)
            {
                //rawPixelStore.close();
                //rawPixelStore = serviceFactory.createRawPixelsStore();
                rawPixelStore.setPixelsId(pixId);
                currentPixId = pixId;
            }
            rawPixelStore.setPlane(arrayBuf, z, c, t);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.IMinMaxStore#setChannelGlobalMinMax(int, double, double, int)
     */
    public void setChannelGlobalMinMax(int channel, double minimum,
            double maximum, int series)
    {
    	Pixels pixels = 
    		(Pixels) getSourceObject(new LSID(Pixels.class, series, 0));
    	if (imageChannelGlobalMinMax == null)
    	{
    		int imageCount = countCachedContainers(Image.class);
    		imageChannelGlobalMinMax = new double[imageCount][][];
    	}
    	double[][] channelGlobalMinMax = imageChannelGlobalMinMax[series];
    	if (channelGlobalMinMax == null)
    	{
    		imageChannelGlobalMinMax[series] = channelGlobalMinMax =
    			new double[pixels.getSizeC().getValue()][];
    	}
    	double[] globalMinMax = channelGlobalMinMax[channel];
    	if (globalMinMax == null)
    	{
    		imageChannelGlobalMinMax[series][channel] = globalMinMax =
    			new double[2];
    	}
    	globalMinMax[0] = minimum;
    	globalMinMax[1] = maximum;
    }

    /**
     * Updates a list of Pixels.
     * @param pixelsList List of Pixels to update.
     */
    public void updatePixels(List<Pixels> pixelsList)
    {
        try
        {
        	List<IObject> objectList = new ArrayList<IObject>(pixelsList.size());
        	for (Pixels pixels : pixelsList)
        	{
        		objectList.add(pixels);
        	}
        	iUpdate.saveArray(objectList);
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public static String byteArrayToHexString(byte in[]) {

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

    /**
     * Sends all the minimums and maximums for all images processed so far to
     * the server.
     */
    public void populateMinMax()
    {
        try
        {
            delegate.populateMinMax(imageChannelGlobalMinMax);
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
        //FIXME: make this work!
        //LSID key = new LSID(Image.class, imageIndex);
        //referenceCache.put(key, new LSID(defaultPixels));
    }

    public void setLogicalChannelOTF(String otf, int imageIndex,
            int logicalChannelIndex)
    {
        LSID key = new LSID(LogicalChannel.class, imageIndex, logicalChannelIndex);
        referenceCache.put(key, new LSID(otf));
    }

    public void setOTFObjective(String objective, int instrumentIndex,
            int otfIndex)
    {
        LSID key = new LSID(OTF.class, instrumentIndex, otfIndex);
        referenceCache.put(key, new LSID(objective));
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
        referenceCache.put(key, new LSID(objective));
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
			    int stringDifference = 
			        stringComparator.compare(x.toString(), y.toString());
				if (yClass == null || stringDifference == 0)
				{
					// Handle different supplied LSIDs by string difference.
				    return stringDifference;
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
			    || klass.equals(LightSettings.class))
			{
			    return 3;
			}
			
			return indexes;
		}
    }

    public void setChannelComponentPixels(String arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setCircleID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setCirclecx(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setCirclecy(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setCircler(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setCircletransform(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setContactExperimenter(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setDatasetDescription(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setDatasetExperimenterRef(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setDatasetGroupRef(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setDatasetID(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setDatasetLocked(Boolean arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setDatasetName(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setDatasetRefID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setDetectorAmplificationGain(Float arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setDetectorZoom(Float arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setDichroicLotNumber(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setDichroicManufacturer(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setDichroicModel(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setDisplayOptionsDisplay(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setEllipseID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setEllipsecx(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setEllipsecy(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setEllipserx(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setEllipsery(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setEllipsetransform(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setEmFilterLotNumber(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setEmFilterManufacturer(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setEmFilterModel(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setEmFilterType(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setExFilterLotNumber(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setExFilterManufacturer(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setExFilterModel(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setExFilterType(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setExperimentExperimenterRef(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setExperimenterOMEName(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setFilterFilterWheel(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setFilterLotNumber(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setFilterManufacturer(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setFilterModel(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setFilterSetDichroic(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setFilterSetEmFilter(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setFilterSetExFilter(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setFilterSetLotNumber(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setFilterSetManufacturer(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setFilterSetModel(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setFilterType(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setGreyChannelBlackLevel(Float arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setGreyChannelChannelNumber(Integer arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setGreyChannelGamma(Float arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setGreyChannelMapColorMap(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setGreyChannelWhiteLevel(Float arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setGreyChannelisOn(Boolean arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setGroupName(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setImageAcquiredPixels(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setImageExperimentRef(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setImageExperimenterRef(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setImageGroupRef(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setImageObjective(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setLaserPockelCell(Boolean arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setLaserRepetitionRate(Boolean arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setLightSourceRefAttenuation(Float arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setLightSourceRefLightSource(String arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setLightSourceRefWavelength(Integer arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setLineID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setLinetransform(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setLinex1(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setLinex2(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setLiney1(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setLiney2(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setLogicalChannelDetector(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setLogicalChannelFilterSet(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setLogicalChannelLightSource(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setLogicalChannelSecondaryEmissionFilter(String arg0, int arg1,
            int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setLogicalChannelSecondaryExcitationFilter(String arg0,
            int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setMaskID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setMaskPixelsBigEndian(Boolean arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setMaskPixelsBinData(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setMaskPixelsExtendedPixelType(String arg0, int arg1, int arg2,
            int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setMaskPixelsID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setMaskPixelsSizeX(Integer arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setMaskPixelsSizeY(Integer arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setMaskheight(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setMasktransform(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setMaskwidth(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setMaskx(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setMasky(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setMicrobeamManipulationExperimenterRef(String arg0, int arg1,
            int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setMicrobeamManipulationID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setMicrobeamManipulationRefID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setMicrobeamManipulationType(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setMicroscopeID(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setMicroscopeManufacturer(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setMicroscopeModel(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setMicroscopeSerialNumber(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setMicroscopeType(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setOTFBinaryFile(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setPlaneHashSHA1(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setPlaneID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setPlateRefSample(Integer arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setPlateRefWell(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setPointID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setPointcx(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setPointcy(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setPointr(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setPointtransform(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setPolygonID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setPolygonpoints(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setPolygontransform(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setPolylineID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setPolylinepoints(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setPolylinetransform(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setProjectDescription(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setProjectExperimenterRef(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setProjectGroupRef(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setProjectID(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setProjectName(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setProjectRefID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setPumpLightSource(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setROIRefID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setRectID(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setRectheight(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setRecttransform(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setRectwidth(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setRectx(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setRecty(String arg0, int arg1, int arg2, int arg3)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setRegionID(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setRegionName(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setRegionTag(String arg0, int arg1, int arg2)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setScreenDescription(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setScreenExtern(String arg0, int arg1)
    {
        // TODO Auto-generated method stub
        //

    }

    public void setScreenReagentSetIdentifier(String arg0, int arg1)
    {

    }

    public void setScreenRefID(String arg0, int arg1, int arg2)
    {

    }

    public void setShapeID(String arg0, int arg1, int arg2, int arg3)
    {

    }

    public void setShapetheT(Integer arg0, int arg1, int arg2, int arg3)
    {

    }

    public void setShapetheZ(Integer arg0, int arg1, int arg2, int arg3)
    {

    }

    public void setThumbnailID(String arg0, int arg1)
    {

    }

    public void setThumbnailMIMEtype(String arg0, int arg1)
    {

    }

    public void setThumbnailhref(String arg0, int arg1)
    {

    }

    public void setTransmittanceRangeCutIn(Integer arg0, int arg1, int arg2)
    {

    }

    public void setTransmittanceRangeCutInTolerance(Integer arg0, int arg1,
            int arg2)
    {

    }

    public void setTransmittanceRangeCutOut(Integer arg0, int arg1, int arg2)
    {

    }

    public void setTransmittanceRangeCutOutTolerance(Integer arg0, int arg1,
            int arg2)
    {

    }

    public void setTransmittanceRangeTransmittance(Integer arg0, int arg1,
            int arg2)
    {

    }

    public void setWellReagent(String reagent, int plateIndex, int wellIndex)
    {
        LSID key = new LSID(Well.class, plateIndex, wellIndex);
        referenceCache.put(key, new LSID(reagent));
    }

    public void setWellSampleImageRef(String image, int plateIndex, 
            int wellIndex, int wellSampleIndex)
    {
        LSID key = new LSID(WellSample.class, plateIndex, wellIndex, wellSampleIndex);
        referenceCache.put(key, new LSID(image));
    }

    public void setWellSampleRefID(String arg0, int arg1, int arg2, int arg3)
    {

    }
}
