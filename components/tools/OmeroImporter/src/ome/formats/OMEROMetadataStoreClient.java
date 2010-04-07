package ome.formats;

import static omero.rtypes.rbool;
import static omero.rtypes.rdouble;
import static omero.rtypes.rint;
import static omero.rtypes.rlong;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.meta.IMinMaxStore;
import loci.formats.meta.MetadataStore;
import ome.formats.enums.EnumerationProvider;
import ome.formats.enums.IQueryEnumProvider;
import ome.formats.importer.util.ClientKeepAlive;
import ome.formats.model.BlitzInstanceProvider;
import ome.formats.model.ChannelProcessor;
import ome.formats.model.IObjectContainerStore;
import ome.formats.model.InstanceProvider;
import ome.formats.model.InstrumentProcessor;
import ome.formats.model.MetaLightSource;
import ome.formats.model.MetaShape;
import ome.formats.model.ModelProcessor;
import ome.formats.model.PixelsProcessor;
import ome.formats.model.PlaneInfoProcessor;
import ome.formats.model.ReferenceProcessor;
import ome.formats.model.ShapeProcessor;
import ome.formats.model.TargetProcessor;
import ome.formats.model.WellProcessor;
import ome.util.LSID;
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
import omero.api.IRenderingSettingsPrx;
import omero.api.IRepositoryInfoPrx;
import omero.api.ITypesPrx;
import omero.api.IUpdatePrx;
import omero.api.MetadataStorePrx;
import omero.api.MetadataStorePrxHelper;
import omero.api.RawFileStorePrx;
import omero.api.RawPixelsStorePrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceInterfacePrx;
import omero.api.ThumbnailStorePrx;
import omero.constants.METADATASTORE;
import omero.grid.InteractiveProcessorPrx;
import omero.metadatastore.IObjectContainer;
import omero.model.AcquisitionMode;
import omero.model.Annotation;
import omero.model.Arc;
import omero.model.ArcType;
import omero.model.Binning;
import omero.model.ContrastMethod;
import omero.model.Correction;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.Detector;
import omero.model.DetectorSettings;
import omero.model.DetectorType;
import omero.model.Dichroic;
import omero.model.DimensionOrder;
import omero.model.Ellipse;
import omero.model.Experiment;
import omero.model.ExperimentType;
import omero.model.Filament;
import omero.model.FilamentType;
import omero.model.FileAnnotation;
import omero.model.Filter;
import omero.model.FilterSet;
import omero.model.FilterType;
import omero.model.Format;
import omero.model.IObject;
import omero.model.Illumination;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.ImagingEnvironment;
import omero.model.Immersion;
import omero.model.ImmersionI;
import omero.model.Instrument;
import omero.model.Laser;
import omero.model.LaserMedium;
import omero.model.LaserType;
import omero.model.LightSettings;
import omero.model.LightSource;
import omero.model.Line;
import omero.model.LogicalChannel;
import omero.model.Mask;
import omero.model.Medium;
import omero.model.Microscope;
import omero.model.MicroscopeI;
import omero.model.MicroscopeType;
import omero.model.OTF;
import omero.model.Objective;
import omero.model.ObjectiveSettings;
import omero.model.OriginalFile;
import omero.model.Path;
import omero.model.PhotometricInterpretation;
import omero.model.Pixels;
import omero.model.PixelsType;
import omero.model.PlaneInfo;
import omero.model.Plate;
import omero.model.Point;
import omero.model.Polygon;
import omero.model.Project;
import omero.model.ProjectI;
import omero.model.Pulse;
import omero.model.Reagent;
import omero.model.Rect;
import omero.model.Screen;
import omero.model.ScreenAcquisition;
import omero.model.ScreenI;
import omero.model.Shape;
import omero.model.StageLabel;
import omero.model.Text;
import omero.model.TransmittanceRange;
import omero.model.TransmittanceRangeI;
import omero.model.Well;
import omero.model.WellSample;
import omero.sys.ParametersI;
import omero.util.TempFileManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import Glacier2.CannotCreateSessionException;
import Glacier2.PermissionDeniedException;


/**
 * Client side implementation of the Bio-Formats {@link MetadataStore}. It is
 * responsible for handling metadata from Bio-Formats and maintaining
 * communication with an OMERO server.
 * @author Brian Loranger, brain at lifesci.dundee.ac.uk
 * @author Chris Allan, callan at lifesci.dundee.ac.uk
 */
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
    private Map<LSID, List<LSID>> referenceCache = 
    	new HashMap<LSID, List<LSID>>();
    
    /** Our authoritative LSID container cache. */
    private Map<Class<? extends IObject>, Map<String, IObjectContainer>>
    	authoritativeContainerCache = 
    		new HashMap<Class<? extends IObject>, Map<String, IObjectContainer>>();
    
    /** 
     * Our string based reference cache. This will be populated after all
     * model population has been completed by a ReferenceProcessor. 
     */
    private Map<String, String[]> referenceStringCache;

    /** Our model processors. Will be called on saveToDB(). */
    private List<ModelProcessor> modelProcessors = 
        new ArrayList<ModelProcessor>();
    
    /** Bio-Formats reader that's populating us. */
    private IFormatReader reader;
    
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
    private IRenderingSettingsPrx iSettings;
    private ThumbnailStorePrx thumbnailStore;
    
    /** Our enumeration provider. */
    private EnumerationProvider enumProvider;
    
    /** Our OMERO model object provider. */
    private InstanceProvider instanceProvider;

    /** Current pixels ID we're writing planes for. */
    private Long currentPixId;
    
    /** Annotations from the user for use by model processors. */
    private List<Annotation> userSpecifiedAnnotations;
    
    /** Image name the user specified for use by model processors. */
    private String userSpecifiedImageName;
    
    /** Image description the user specified for use by model processors. */
    private String userSpecifiedImageDescription;
    
    /** Linkage target for all Images/Plates for use by model processors. */
    private IObject userSpecifiedTarget;
    
    /** Physical pixel sizes the user specified for use by model processors. */
    private Double[] userSpecifiedPhysicalPixelSizes;
    
    /** Image channel minimums and maximums. */
    private double[][][] imageChannelGlobalMinMax;
    
    /** Keep alive runnable, pings all services. */
    private ClientKeepAlive keepAlive = new ClientKeepAlive();
    
    /** Executor that will run our keep alive task. */
    private ScheduledThreadPoolExecutor executor;
    
    /** Emission filter LSID suffix. */
    public static final String OMERO_EMISSION_FILTER_SUFFIX =
    	":OMERO_EMISSION_FILTER";
    
    /** Excitation filter LSID suffix. */
    public static final String OMERO_EXCITATION_FILTER_SUFFIX =
    	":OMERO_EXCITATION_FILTER";
    
    /** Companion file namespace */
    private static final String NS_COMPANION =
    	"openmicroscopy.org/omero/import/companionFile";
    
    /** The default longest side of a thumbnail in OMERO.insight. */
    private static final int DEFAULT_INSIGHT_THUMBNAIL_LONGEST_SIDE = 96;

    private void initializeServices()
        throws ServerError
    {
        // Blitz services
        iUpdate = serviceFactory.getUpdateService();
        iQuery = serviceFactory.getQueryService();
        iAdmin = serviceFactory.getAdminService();
        rawFileStore = serviceFactory.createRawFileStore();
        rawPixelStore = serviceFactory.createRawPixelsStore();
        thumbnailStore = serviceFactory.createThumbnailStore();
        iRepoInfo = serviceFactory.getRepositoryInfoService();
        iContainer = serviceFactory.getContainerService();
        iSettings = serviceFactory.getRenderingSettingsService();
        delegate = MetadataStorePrxHelper.checkedCast(serviceFactory.getByName(METADATASTORE.value));

        // Client side services
        enumProvider = new IQueryEnumProvider(iQuery);
        instanceProvider = new BlitzInstanceProvider(enumProvider);
        
        // Default model processors
        modelProcessors.add(new PixelsProcessor());
        modelProcessors.add(new ChannelProcessor());
        modelProcessors.add(new InstrumentProcessor());
        modelProcessors.add(new PlaneInfoProcessor());
        modelProcessors.add(new WellProcessor());
        modelProcessors.add(new ShapeProcessor());
        modelProcessors.add(new TargetProcessor());  // Should be second last
        modelProcessors.add(new ReferenceProcessor());  // Should be last
        
        // Fix check for broken 4.0 immersions table
        //checkImmersions();
        
        // Start our keep alive executor
        if (executor == null)
        {
            executor = new ScheduledThreadPoolExecutor(1);
            executor.scheduleWithFixedDelay(keepAlive, 60, 60, TimeUnit.SECONDS);
        }
        keepAlive.setClient(this);
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
     * Returns the currently active service factory.
     * @return See above.
     */
    public ServiceFactoryPrx getServiceFactory()
    {
    	return serviceFactory;
    }

    /**
     * Pings all registered OMERO Blitz proxies. 
     * 
     */
    public void ping()
    {
        serviceFactory.keepAllAlive(new ServiceInterfacePrx[] 
                {iQuery, iAdmin, rawFileStore, rawPixelStore, thumbnailStore,
        		 iRepoInfo, iContainer, iUpdate, iSettings, delegate});
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
     * Destroys the sessionFactory and closes the client.
     */
    public void logout()
    {
        if (executor != null)
        {
            log.debug("Logout called, shutting keep alive down.");
            executor.shutdown();
            executor = null;
            log.debug("keepalive shut down.");
        }
        if (c != null)
        {
            log.debug("closing client session.");
            c.closeSession();
            c = null;
            log.debug("client closed.");
        }

    }
    
    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#createRoot()
     */
    public void createRoot()
    {
        try
        {
            log.debug("Creating root!");
            authoritativeContainerCache = 
            	new HashMap<Class<? extends IObject>, Map<String, IObjectContainer>>();
            containerCache = 
                new TreeMap<LSID, IObjectContainer>(new OMEXMLModelComparator());
            referenceCache = new HashMap<LSID, List<LSID>>();
            referenceStringCache = null;
            imageChannelGlobalMinMax = null;
            userSpecifiedAnnotations = null;
            userSpecifiedImageName = null;
            userSpecifiedImageDescription = null;
            userSpecifiedTarget = null;
            userSpecifiedPhysicalPixelSizes = null;
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
    
    /**
     * Checks for duplicate authoritative LSIDs for a given class in the 
     * container cache.
     * @param klass Filter class for IObjectContainer types.
     * @param lsid LSID to check against.
     */
    private void checkDuplicateLSID(Class<? extends IObject> klass, String lsid)
    {
    	if (log.isTraceEnabled())
    	{
    		List<IObjectContainer> containers = getIObjectContainers(klass);
    		for (IObjectContainer container : containers)
    		{
    			if (container.LSID.equals(lsid))
    			{
    				log.trace(String.format("Duplicate LSID %s exists in %s,%s",
    						lsid, container.sourceObject, container.LSID));
    					return;
    			}
    		}
    	}
    }
    
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#getReader()
     */
    public IFormatReader getReader()
    {
        return reader;
    }
    
    
    public String getReaderType()
    {
        ImageReader imageReader = (ImageReader) reader;
        String formatString = imageReader.getReader().getClass().toString();
        formatString = formatString.replace("class loci.formats.in.", "");
        formatString = formatString.replace("Reader", "");
        return formatString;
    }
    
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#setReader(loci.formats.IFormatReader)
     */
    public void setReader(IFormatReader reader)
    {
        this.reader = reader;
    }

    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#getUserSpecifiedAnnotations()
     */
    public List<Annotation> getUserSpecifiedAnnotations()
    {
    	return userSpecifiedAnnotations;
    }
    
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#setUserSpecifiedAnnotations(java.util.List)
     */
    public void setUserSpecifiedAnnotations(List<Annotation> annotations)
    {
    	this.userSpecifiedAnnotations = annotations;
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
    
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#getUserSpecifiedImageDescription()
     */
    public String getUserSpecifiedImageDescription()
    {
        return userSpecifiedImageDescription;
    }

    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#setUserSpecifiedImageDescription(java.lang.String)
     */
    public void setUserSpecifiedImageDescription(String description)
    {
        this.userSpecifiedImageDescription = description;
    }
    
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#getUserSpecifiedTarget()
     */
    public IObject getUserSpecifiedTarget()
    {
        return userSpecifiedTarget;
    }

    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#setUserSpecifiedTarget(omero.model.IObject)
     */
    public void setUserSpecifiedTarget(IObject target)
    {
        this.userSpecifiedTarget = target;
    }
    
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#getUserSpecifiedPhysicalPixelSizes()
     */
    public Double[] getUserSpecifiedPhysicalPixelSizes()
    {
        return userSpecifiedPhysicalPixelSizes;
    }
    
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#setUserSpecifiedPhysicalPixelSizes(java.lang.Double, java.lang.Double, java.lang.Double)
     */
    public void setUserSpecifiedPhysicalPixelSizes(Double physicalSizeX,
                                                   Double physicalSizeY,
                                                   Double physicalSizeZ)
    {
        userSpecifiedPhysicalPixelSizes = 
            new Double[] { physicalSizeX, physicalSizeY, physicalSizeZ };
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
    public Map<LSID, List<LSID>> getReferenceCache()
    {
        return referenceCache;
    }
    
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#getAuthoritativeContainerCache()
     */
    public Map<Class<? extends IObject>, Map<String, IObjectContainer>>
    	getAuthoritativeContainerCache()
    {
    	return authoritativeContainerCache;
    }
    
    /**
     * Adds a container to the authoritative LSID cache.
     * @param klass Type of container we're adding.
     * @param lsid String LSID of the container.
     * @param container Container to add.
     */
    private void addAuthoritativeContainer(Class<? extends IObject> klass,
    		                               String lsid,
    		                               IObjectContainer container)
    {
    	Map<String, IObjectContainer> lsidContainerMap =
    		authoritativeContainerCache.get(klass);
    	if (lsidContainerMap == null)
    	{
    		lsidContainerMap = new HashMap<String, IObjectContainer>(); 
    		authoritativeContainerCache.put(klass, lsidContainerMap);
    	}
    	lsidContainerMap.put(lsid, container);
    }
    
    /**
     * Adds a reference to the reference cache.
     * @param source Source LSID to add.
     * @param target Target LSID to add.
     */
    public void addReference(LSID source, LSID target)
    {
    	List<LSID> targets = null;
    	if (referenceCache.containsKey(source))
    	{
    		targets = referenceCache.get(source);
    	}
    	else
    	{
    		targets = new ArrayList<LSID>();
    		referenceCache.put(source, targets);
    	}
    	targets.add(target);
    }
    
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#getReferenceStringCache()
     */
    public Map<String, String[]> getReferenceStringCache()
    {
        return referenceStringCache;
    }
    
    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#setReferenceStringCache(Map<String, String[]>)
     */
    public void setReferenceStringCache(Map<String, String[]> referenceStringCache)
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
     * @see ome.formats.model.IObjectContainerStore#getSourceObject(ome.util.LSID)
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
     * @see ome.formats.model.IObjectContainerStore#hasReference(ome.util.LSID, ome.util.LSID)
     */
    public boolean hasReference(LSID source, LSID target)
    {
        if (!referenceCache.containsKey(source)
            || !referenceCache.get(source).contains(target))
        {
            return false;
        }
        return true;
    }
    
    /**
     * Handles the upcast to the "real" concrete type and the correct LSID
     * mapping if required.
     * @param klass Class to retrieve a container for.
     * @param indexes Indexes into the OME-XML data model.
     * @param lsid LSID of the container.
     * @return Created container or <code>null</code> if the container cache
     * already contains <code>lsid</code>.
     */
    private IObjectContainer handleAbstractLightSource(
    		Class<? extends IObject> klass,
    		LinkedHashMap<String, Integer> indexes, LSID lsid)
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
        return null;
    }
    
    /**
     * Handles the upcast to the "real" concrete type and the correct LSID
     * mapping if required.
     * @param klass Class to retrieve a container for.
     * @param indexes Indexes into the OME-XML data model.
     * @param lsid LSID of the container.
     * @return Created container or <code>null</code> if the container cache
     * already does not contain <code>lsid</code>.
     */
    private IObjectContainer handleAbstractShape(
    		Class<? extends IObject> klass,
    		LinkedHashMap<String, Integer> indexes, LSID lsid)
    {
        LSID shapeLSID = new LSID(Shape.class,
                                  indexes.get("imageIndex"),
                                  indexes.get("roiIndex"),
                                  indexes.get("shapeIndex"));
        if (containerCache.containsKey(shapeLSID))
        {
            IObjectContainer container = containerCache.get(shapeLSID);
            MetaShape metaShape = 
                (MetaShape) container.sourceObject;
            Shape realInstance = 
                (Shape) getSourceObjectInstance(klass);
            metaShape.copyData(realInstance);
            container.sourceObject = realInstance;
            if (container.LSID == null
                || container.LSID.equals(shapeLSID.toString()))
            {
                container.LSID = lsid.toString();
            }
            containerCache.put(lsid, container);
            return container;
        }
        return null;
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
        	IObjectContainer toReturn = 
        		handleAbstractLightSource(klass, indexes, lsid);
        	if (toReturn != null)
        	{
        		return toReturn;
        	}
        }
        // Because of the Shape abstract type, here we need to handle
        // the upcast to the "real" concrete type and the correct LSID
        // mapping.
        if ((klass.equals(Text.class) || klass.equals(Rect.class)
            || klass.equals(Mask.class) || klass.equals(Ellipse.class)
            || klass.equals(Point.class) || klass.equals(Path.class)
            || klass.equals(Polygon.class) || klass.equals(Line.class))
            && !containerCache.containsKey(lsid))
        {
        	IObjectContainer toReturn = 
        		handleAbstractShape(klass, indexes, lsid);
        	if (toReturn != null)
        	{
        		return toReturn;
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
        if (klass.equals(Shape.class)
            && !containerCache.containsKey(lsid))
        {
            Class[] concreteClasses = 
                new Class[] { Text.class, Rect.class, Mask.class, Ellipse.class,
            		          Point.class, Path.class, Polygon.class,
            		          Line.class };
            for (Class concreteClass : concreteClasses)
            {
                LSID shapeLSID = new LSID(concreteClass,
                                          indexes.get("imageIndex"),
                                          indexes.get("roiIndex"),
                                          indexes.get("shapeIndex"));
                if (containerCache.containsKey(shapeLSID))
                {
                    return containerCache.get(shapeLSID);
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
     * @see ome.formats.model.IObjectContainerStore#removeIObjectContainer(ome.util.LSID)
     */
    public void removeIObjectContainer(LSID lsid)
    {
    	containerCache.remove(lsid);
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
                if (indexes == null)
                {
                	// We're just doing a class match, increment the count
                	count++;
                }
                else
                {
                	// We're doing a class and index match, loop over and
                	// check the indexes based on the shortest array.
                    int[] lsidIndexes = lsid.getIndexes();
                    int n = Math.min(indexes.length, lsidIndexes.length);
                    boolean match = true;
                    for (int i = 0; i < n; i++)
                    {
                        if (lsidIndexes[i] != indexes[i])
                        {
                            match = false;
                            break;
                        }
                    }
                    if (match)
                    {
                    	count++;
                    }
                }
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
        	int count = 0;
        	for (LSID key : referenceCache.keySet())
        	{
        		count += referenceCache.get(key).size();
        	}
        	return count;
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
        	for (LSID sourceLSID : referenceCache.keySet())
        	{
        		for (LSID targetLSID : referenceCache.get(sourceLSID))
        		{
        			Class containerClass = targetLSID.getJavaClass();
        			if (containerClass.equals(target))
        			{
        				count++;
        			}
        		}
        	}
            return count;
        }
        
        for (LSID sourceLSID : referenceCache.keySet())
        {
            Class sourceClass = sourceLSID.getJavaClass();
            if (sourceClass.equals(source))
            {
            	for (LSID targetLSID : referenceCache.get(sourceLSID))
            	{
            		Class targetClass = targetLSID.getJavaClass();
            		if (targetClass.equals(target))
            		{
            			count++;
            		}
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
    	checkDuplicateLSID(Detector.class, id);
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", instrumentIndex);
        indexes.put("detectorIndex", detectorIndex);
        IObjectContainer o = getIObjectContainer(Detector.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Detector.class, id, o);
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
        addReference(key, new LSID(detector));
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
    	checkDuplicateLSID(Image.class, id);
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("imageIndex", imageIndex);
        IObjectContainer o = getIObjectContainer(Image.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Image.class, id, o);
    }

    public void setImageInstrumentRef(String instrumentRef, int imageIndex)
    {
        LSID key = new LSID(Image.class, imageIndex);
        addReference(key, new LSID(instrumentRef));
    }

    public void setImageName(String name, int imageIndex)
    {
        // We don't want empty string image names
        // Thu Oct  8 11:05:37 BST 2009
        // Chris Allan <callan at lifesci dot dundee dot ac dot uk>
        // http://trac.openmicroscopy.org.uk/omero/ticket/1523
        if (name != null && name.length() == 0)
        {
            return;
        }
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
    
    private Instrument getInstrument(int instrumentIndex)
    {
        LinkedHashMap<String, Integer> indexes = 
        	new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", instrumentIndex);
        return getSourceObject(Instrument.class, indexes);
    }

    public void setInstrumentID(String id, int instrumentIndex)
    {
    	checkDuplicateLSID(Instrument.class, id);
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", instrumentIndex);
        IObjectContainer o = getIObjectContainer(Instrument.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Instrument.class, id, o);
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
    	checkDuplicateLSID(LightSource.class, id);
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", instrumentIndex);
        indexes.put("lightSourceIndex", lightSourceIndex);  
        IObjectContainer o = getIObjectContainer(LightSource.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(LightSource.class, id, o);
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
        addReference(key, new LSID(lightSource));
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
    	checkDuplicateLSID(LogicalChannel.class, id);
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("imageIndex", imageIndex);
        indexes.put("logicalChannelIndex", logicalChannelIndex);  
        IObjectContainer o = getIObjectContainer(LogicalChannel.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(LogicalChannel.class, id, o);
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
    	checkDuplicateLSID(OTF.class, id);
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", instrumentIndex);
        indexes.put("otfIndex", otfIndex);  
        IObjectContainer o = getIObjectContainer(OTF.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(OTF.class, id, o);
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
    	checkDuplicateLSID(Objective.class, id);
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", instrumentIndex);
        indexes.put("objectiveIndex", objectiveIndex);  
        IObjectContainer o = getIObjectContainer(Objective.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Objective.class, id, o);
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
    	checkDuplicateLSID(Pixels.class, id);
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("imageIndex", imageIndex);
        indexes.put("pixelsIndex", pixelsIndex);  
        IObjectContainer o = getIObjectContainer(Pixels.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Pixels.class, id, o);
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
    	checkDuplicateLSID(Plate.class, id);
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("plateIndex", plateIndex); 
        IObjectContainer o = getIObjectContainer(Plate.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Plate.class, id, o);
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
    	checkDuplicateLSID(Reagent.class, id);
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("screenIndex", screenIndex);
        indexes.put("reagentIndex", reagentIndex);  
        IObjectContainer o = getIObjectContainer(Reagent.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Reagent.class, id, o);
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
        // Disabled
    }

    public void setScreenAcquisitionID(String id, int screenIndex,
            int screenAcquisitionIndex)
    {
        // Disabled
    }

    public void setScreenAcquisitionStartTime(String startTime,
            int screenIndex, int screenAcquisitionIndex)
    {
        // Disabled
    }

    public void setScreenID(String id, int screenIndex)
    {
        // Disabled
    }

    public void setScreenName(String name, int screenIndex)
    {
        // Disabled
    }

    public void setScreenProtocolDescription(String protocolDescription,
            int screenIndex)
    {
        // Disabled
    }

    public void setScreenProtocolIdentifier(String protocolIdentifier,
            int screenIndex)
    {
        // Disabled
    }

    public void setScreenReagentSetDescription(String reagentSetDescription,
            int screenIndex)
    {
        // Disabled
    }

    public void setScreenType(String type, int screenIndex)
    {
        // Disabled
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
    	checkDuplicateLSID(Well.class, id);
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("plateIndex", plateIndex);
        indexes.put("wellIndex", wellIndex);  
        IObjectContainer o = getIObjectContainer(Well.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Well.class, id, o);
    }

    public void setWellRow(Integer row, int plateIndex, int wellIndex)
    {
        Well o = getWell(plateIndex, wellIndex);
        o.setRow(toRType(row));
    }

    public void setWellSampleID(String id, int plateIndex, int wellIndex,
            int wellSampleIndex)
    {
    	checkDuplicateLSID(WellSample.class, id);
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("plateIndex", plateIndex);
        indexes.put("wellIndex", wellIndex); 
        indexes.put("wellSampleIndex", wellSampleIndex);
        IObjectContainer o = getIObjectContainer(WellSample.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(WellSample.class, id, o);
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
    
    /**
     * Creates an original file object from a Java file object along with some
     * metadata specific to OMERO in the container cache or returns the
     * original file as already created in the supplied map.
     * @param file Java file object to pull metadata from.
     * @param indexes Container cache indexes to use.
     * @param formatString Original file format as a string.
     * @param existing Existing original files keyed by absolute path.
     * @return Created original file source object.
     */
    private OriginalFile createOriginalFileFromFile(
    		File file, LinkedHashMap<String, Integer> indexes,
    		String formatString)
    {
		OriginalFile o = (OriginalFile) 
			getSourceObject(OriginalFile.class, indexes);
		Format format = (Format) getEnumeration(Format.class, formatString);
		o.setName(toRType(file.getName()));
		o.setSize(toRType(file.length()));
		o.setFormat(format);
		o.setPath(toRType(file.getAbsolutePath()));
		o.setSha1(toRType("Pending"));
		return o;
    }
    
    /**
     * Creates a temporary file on disk containing all metadata in the
     * Bio-Formats metadata hash table for the current series.
     * @return Temporary file created.
     */
    private File createSeriesMetadataFile()
    {
    	Hashtable globalMetadata = reader.getGlobalMetadata();
    	Hashtable seriesMetadata = reader.getSeriesMetadata();
    	FileOutputStream stream = null;
    	OutputStreamWriter writer= null;
    	try
    	{
    		File metadataFile = TempFileManager.createTempFile("metadata", ".txt");
    		stream = new FileOutputStream(metadataFile);
    		writer = new OutputStreamWriter(stream);
    		metadataFile.deleteOnExit();
    		writer.write("[GlobalMetadata]\n");
    		for (Object key : globalMetadata.keySet())
    		{
    			String s = key.toString() + "="
    			           + globalMetadata.get(key).toString() + "\n";
    			writer.write(s);
    		}
    		writer.write("[SeriesMetadata]\n");
    		for (Object key : seriesMetadata.keySet())
    		{
    			String s = key.toString() + "="
    			           + seriesMetadata.get(key).toString() + "\n";
    			writer.write(s);
    		}
    		return metadataFile;
    	}
    	catch (IOException e)
    	{
    		log.error("Unable to create series metadata file.", e);
    		return null;
    	}
    	finally
    	{
    		try
    		{
    			if (writer != null)
    			{
    				writer.close();
    			}
    			if (stream != null)
    			{
    				stream.close();
    			}
    		}
    		catch (IOException e)
    		{
    			log.error("Unable to close writer or stream.", e);
    		}
    	}
    }
    
    /**
     * Populates archive flags on all images currently processed links
     * relevant original metadata files as requested and performs graph logic
     * to have the scafolding in place for later original file upload if
     * we are of the HCS domain.
     * @param archive Whether or not the user requested the original files to
     * be archived.
     * @return A list of the temporary metadata files created on local disk.
     */
    public List<File> setArchiveScreeningDomain(boolean archive)
    {
    	List<File> metadataFiles = new ArrayList<File>();
    	String[] usedFiles = reader.getUsedFiles();
    	List<String> companionFiles = getFilteredCompanionFiles();
    	ImageReader imageReader = (ImageReader) reader;
    	String formatString = 
    		imageReader.getReader().getClass().toString();
    	formatString = formatString.replace("class loci.formats.in.", "");
    	formatString = formatString.replace("Reader", "");
    	LSID plateKey = new LSID(Plate.class, 0);
		// Populate the archived flag on the image. This inadvertently
		// ensures that an Image object (and corresponding container)
		// exists.
    	for (int series = 0; series < reader.getSeriesCount(); series++)
    	{
        	LinkedHashMap<String, Integer> imageIndexes =
        		new LinkedHashMap<String, Integer>();
        	imageIndexes.put("imageIndex", series);
    		Image image = getSourceObject(Image.class, imageIndexes);
    		image.setArchived(toRType(archive));
    	}
    	// Create all original file objects for later population based on
    	// the existence or abscence of companion files and the archive
    	// flag. This increments the original file count by the number of
    	// files to actually be created.
    	int originalFileIndex = 0;
    	for (String usedFilename : usedFiles)
    	{
    		File usedFile = new File(usedFilename);
    		boolean isCompanionFile = companionFiles == null? false :
    			companionFiles.contains(usedFilename);
    		if (archive || isCompanionFile)
    		{
    			LinkedHashMap<String, Integer> indexes = 
    				new LinkedHashMap<String, Integer>();
    			indexes.put("originalFileIndex", originalFileIndex);
    			if (isCompanionFile)
    			{
    				// PATH 1: The file is a companion file, create it,
    				// and increment the next original file's index.
    				String format = "Companion/" + formatString;
    				createOriginalFileFromFile(usedFile, indexes, format);
    				addCompanionFileAnnotationTo(plateKey, indexes,
    						                     originalFileIndex);
    				originalFileIndex++;
    			}
    			else
    			{
    				// PATH 2: We're archiving and the file is not a
    				// companion file, create it, and increment the next
    				// original file's index.
    				createOriginalFileFromFile(usedFile, indexes,
    						formatString);
    				LSID originalFileKey = 
    					new LSID(OriginalFile.class, originalFileIndex);
    				addReference(plateKey, originalFileKey);
    				originalFileIndex++;
    			}
    		}
    	}
    	return metadataFiles;
    }

    /**
     * Populates archive flags on all images currently processed links
     * relevant original metadata files as requested and performs graph logic
     * to have the scafolding in place for later original file upload.
     * @param archive Whether or not the user requested the original files to
     * be archived.
     * @param useMetadataFile Whether or not to dump all metadata to a flat
     * file annotation on the server.
     * @return A list of the temporary metadata files created on local disk.
     */
    public List<File> setArchive(boolean archive, boolean useMetadataFile)
    {
    	List<File> metadataFiles = new ArrayList<File>();
    	int originalFileIndex = 0;
		Map<String, Integer> pathIndexMap = new HashMap<String, Integer>();
    	for (int series = 0; series < reader.getSeriesCount(); series++)
    	{
    		reader.setSeries(series);
    		String[] usedFiles = reader.getSeriesUsedFiles();
    		// Collection of companion files so we can use contains()
    		List<String> companionFiles = getFilteredSeriesCompanionFiles();
    		
    		ImageReader imageReader = (ImageReader) reader;
    		String formatString = 
    			imageReader.getReader().getClass().toString();
    		formatString = formatString.replace("class loci.formats.in.", "");
    		formatString = formatString.replace("Reader", "");
    		LSID pixelsKey = new LSID(Pixels.class, series, 0);
    		LSID imageKey = new LSID(Image.class, series);
    		LinkedHashMap<String, Integer> imageIndexes =
    			new LinkedHashMap<String, Integer>();
    		imageIndexes.put("imageIndex", series);

    		// Populate the archived flag on the image. This inadvertently
    		// ensures that an Image object (and corresponding container)
    		// exists.
    		Image image = getSourceObject(Image.class, imageIndexes);
    		image.setArchived(toRType(archive));
    		
    		// If we have been asked to create a metadata file with all the
    		// metadata dumped out, do so, add it to the collection we're to
    		// return and create an OriginalFile object to hold state on the
    		// server and a companion file annotation to link it to the pixels
    		// set itself. This increments the original file count if enabled.
    		if (useMetadataFile)
    		{
    			File metadataFile = createSeriesMetadataFile();
    			metadataFiles.add(metadataFile);
				LinkedHashMap<String, Integer> indexes = 
					new LinkedHashMap<String, Integer>();
				indexes.put("originalFileIndex", originalFileIndex);
				String format = "text/plain";
				OriginalFile originalFile = 
					createOriginalFileFromFile(metadataFile, indexes, format);
				originalFile.setName(toRType("original_metadata.txt"));
                indexes = new LinkedHashMap<String, Integer>();
                indexes.put("imageIndex", series);
                indexes.put("originalFileIndex", originalFileIndex);
                addCompanionFileAnnotationTo(imageKey, indexes,
                		                     originalFileIndex);
				originalFileIndex++;
    		}
    		
    		// Create all original file objects for later population based on
    		// the existence or abscence of companion files and the archive
    		// flag. This increments the original file count by the number of
    		// files to actually be created.
    		for (int i = 0; i < usedFiles.length; i++)
    		{
    			String usedFilename = usedFiles[i];
    			File usedFile = new File(usedFilename);
    			String absolutePath = usedFile.getAbsolutePath();
    			log.debug("Series: " + i + " file: " + absolutePath);
    			boolean isCompanionFile = companionFiles == null? false :
    				                      companionFiles.contains(usedFilename);
    			if (archive || isCompanionFile)
    			{
    				LinkedHashMap<String, Integer> indexes = 
    					new LinkedHashMap<String, Integer>();
    				indexes.put("originalFileIndex", originalFileIndex);
    				int usedFileIndex = originalFileIndex;
    				if (pathIndexMap.containsKey(absolutePath))
    				{
    					// PATH 1: We've already seen this path before, re-use
    					// the same original file index.
    					usedFileIndex = pathIndexMap.get(absolutePath);
    				}
    				else if (isCompanionFile)
    				{
    					// PATH 2: The file is a companion file, create it,
    					// put the new original file index into our cached map
    					// and increment the next original file's index.
    					String format = "Companion/" + formatString;
    					createOriginalFileFromFile(usedFile, indexes, format);
    					pathIndexMap.put(absolutePath, usedFileIndex);
    					originalFileIndex++;
    				}
    				else
    				{
    					// PATH 3: We're archiving and the file is not a
    					// companion file, create it, put the new original file
    					// index into our cached map, increment the next
    					// original file's index and link it to our pixels set.
    					createOriginalFileFromFile(usedFile, indexes,
    					                           formatString);
    					pathIndexMap.put(absolutePath, usedFileIndex);
    					originalFileIndex++;
    				}
    				
    				if (isCompanionFile)
    				{
                        indexes = new LinkedHashMap<String, Integer>();
                        indexes.put("imageIndex", series);
                        indexes.put("originalFileIndex", usedFileIndex);
                        addCompanionFileAnnotationTo(imageKey, indexes,
                        		                     usedFileIndex);
    				}
    				else
    				{
        				LSID originalFileKey = 
        					new LSID(OriginalFile.class, usedFileIndex);
        				addReference(pixelsKey, originalFileKey);
    				}
    			}
    		}
    	}
    	return metadataFiles;
    }
    
    /**
     * Filters a set of filenames.
     * @param files An array of the files to filter.
     * @return A collection of the filtered files.
     */
    private List<String> filterFilenames(String[] files)
    {
    	if (files == null)
    	{
    		return null;
    	}
    	List<String> filteredFiles = new ArrayList<String>();
    	for (String file : files)
    	{
    		if (!file.endsWith(".tif")
    			&& !file.endsWith(".tiff"))
    		{
    			filteredFiles.add(file);
    		}
    	}
    	return filteredFiles;
    }

    /**
     * Returns the current set of filtered companion files that the Bio-Formats
     * image reader contains.
     * @return See above.
     * @see getFilteredSeriesCompanionFiles()
     */
    public List<String> getFilteredCompanionFiles()
    {
    	return filterFilenames(reader.getUsedFiles(true));
    }
    
    /**
     * Returns the current set of filtered companion files that the Bio-Formats
     * image reader contains for the current series.
     * @return See above.
     * @see getFilteredCompanionFiles()
     */
    public List<String> getFilteredSeriesCompanionFiles()
    {
    	return filterFilenames(reader.getSeriesUsedFiles(true));
    }
    
    /**
     * Adds a file annotation and original file reference linked to a given
     * base LSID target.
     * @param target LSID of the target object.
     * @param indexes Indexes of the annotation.
     * @param originalFileIndex Index of the original file.
     */
    private void addCompanionFileAnnotationTo(
    		LSID target, LinkedHashMap<String, Integer> indexes,
    		int originalFileIndex)
    {
    	FileAnnotation a = (FileAnnotation) 
    	getSourceObject(FileAnnotation.class, indexes);
    	a.setNs(rstring(NS_COMPANION));

    	Collection<Integer> indexValues = indexes.values();
    	Integer[] integerValues = indexValues.toArray(
    			new Integer[indexValues.size()]);
    	int[] values = new int[integerValues.length];
    	for (int i = 0; i < integerValues.length; i++)
    	{
    		values[i] = integerValues[i].intValue();
    	}
    	LSID annotationKey = new LSID(FileAnnotation.class, values);
    	LSID originalFileKey = new LSID(OriginalFile.class,
    			originalFileIndex);
    	addReference(target, annotationKey);
    	addReference(annotationKey, originalFileKey);
    }
    
    /**
     * Writes binary original file data to the OMERO server.
     * @param files Files to populate against an original file list.
     * @param originalFileMap Map of absolute path against original file
     * objects that we are to populate.
     */
    public void writeFilesToFileStore(
    		List<File> files, Map<String, OriginalFile> originalFileMap)
    {
        // Lookup each source file in our hash map and write it to the
        // correct original file object server side.
        byte[] buf = new byte[1048576];  // 1 MB buffer
        for (File file : files)
        {
            OriginalFile originalFile = 
            	originalFileMap.get(file.getAbsolutePath());
            if (originalFile == null)
            {
                log.warn("Cannot lookup original file with path: "
                         + file.getAbsolutePath());
                continue;
            }

            FileInputStream stream = null;
            try
            {           
                stream = new FileInputStream(file);
                rawFileStore.setFileId(originalFile.getId().getValue());
                int rlen = 0;
                int offset = 0;
                while (stream.available() != 0)
                {
                    rlen = stream.read(buf);
                    rawFileStore.write(buf, offset, rlen);
                    offset += rlen;
                }
            }
            catch (Exception e)
            {
                log.error("I/O or server error populating file store.", e);
                break;
            }
            finally
            {
                if (stream != null)
                {
                    try
                    {
                        stream.close();
                    }
                    catch (Exception e)
                    {
                        log.error("I/O error closing stream.", e);
                    }
                }
            }
        }
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
            
            if (log.isDebugEnabled())
            {
            	log.debug("Starting containers....");
                for (LSID key : containerCache.keySet())
                {
                    String s = String.format("%s == %s,%s", 
                            key, containerCache.get(key).sourceObject,
                            containerCache.get(key).LSID);
                    log.debug(s);
                }

                log.debug("Starting references....");
                for (String key : referenceStringCache.keySet())
                {
                	for (String value : referenceStringCache.get(key))
                	{
                		String s = String.format("%s == %s", key, value);
                		log.debug(s);
                	}
                }
                
                log.debug("containerCache contains " + containerCache.size()
                          + " entries.");
                log.debug("referenceCache contains " 
                		  + countCachedReferences(null, null)
                          + " entries.");
            }
            
            delegate.updateObjects(containerArray);
            delegate.updateReferences(referenceStringCache);
            pixelsList = delegate.saveToDB();
            
            if (log.isDebugEnabled())
            {
                long pixelsId;
                for (Pixels pixels : pixelsList)
                {
                    pixelsId = pixels.getId().getValue();
                    log.debug("Saved Pixels with ID: "  + pixelsId);
                }
            }
            return pixelsList;
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public List<InteractiveProcessorPrx> launchProcessing()
    {
        try {
            return delegate.postProcess();
        } catch (Exception e) {
            // Becasuse this method is evolving, we're going to
            // permit an exception to not stop import. Eventually,
            // this could be dangerous. ~Josh.
            log.warn("Failed to launch post-processing", e);
            return null;
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

    @SuppressWarnings("unchecked")
    public <T extends IObject> T getTarget(Class<T> klass, long id)
    {
        try
        {
            return (T) iQuery.get(klass.getName(), id);
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

    public List<Screen> getScreens()
    {
        try
        {
            List<IObject> objects = 
                iContainer.loadContainerHierarchy(Screen.class.getName(), null, null);
            List<Screen> screens = new ArrayList<Screen>(objects.size());
            for (IObject object : objects)
            {
                screens.add((Screen) object);
            }
            return screens;
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
                iContainer.loadContainerHierarchy(Project.class.getName(), null, new ParametersI().exp(rlong(getExperimenterID())));
            List<Project> projects = new ArrayList<Project>(objects.size());
            for (IObject object : objects)
            {
                projects.add((Project) object);
            }
            
            Collections.sort(projects, new SortProjectsByName());
            
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
                
                List<Dataset> datasets = project.linkedDatasetList();
                Collections.sort(datasets, new SortDatasetsByName());
                return datasets;
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
    
    public Screen addScreen(String screenName, String screenDescription)
    {
        Screen screen = new ScreenI();
        if (screenName.length() != 0)
            screen.setName(toRType(screenName));
        if (screenDescription.length() != 0)
            screen.setDescription(toRType(screenDescription));

        try
        {
            return (Screen) iUpdate.saveAndReturnObject(screen);
        } catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Prepares the server side RawPixelsStore.
     * @param pixelsIds List of Pixels IDs we'll be populating.
     */
    public void preparePixelsStore(List<Long> pixelsIds)
    {
        try
        {
            rawPixelStore.prepare(pixelsIds);
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    public void setPlane(Long pixId, byte[] arrayBuf, int z, int c, int t)
        throws ServerError
    {
        if (currentPixId != pixId)
        {
            //rawPixelStore.close();
            //rawPixelStore = serviceFactory.createRawPixelsStore();
            rawPixelStore.setPixelsId(pixId, true);
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
            Image unloadedImage;
            for (Pixels pixels : pixelsList)
            {
                pixels.unloadCollections();
                pixels.unloadDetails();
                unloadedImage = new ImageI(pixels.getImage().getId(), false);
                pixels.setImage(unloadedImage);
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
    
    /**
     * Resets the defaults and generates thumbnails for a given set of Pixels 
     * IDs. 
     * @param plateIds Set of Plate IDs to reset defaults and thumbnails for.
     * @param pixelsIds Set of Pixels IDs to reset defaults and thumbnails for.
     */
    public void resetDefaultsAndGenerateThumbnails(List<Long> plateIds,
    		                                       List<Long> pixelsIds)
    {
    	try
    	{
    		if (plateIds.size() > 0)
    		{
    			iSettings.resetDefaultsInSet("Plate", plateIds);
    		}
    		else
    		{
    			iSettings.resetDefaultsInSet("Pixels", pixelsIds);
    		}
    		thumbnailStore.createThumbnailsByLongestSideSet(
    				rint(DEFAULT_INSIGHT_THUMBNAIL_LONGEST_SIDE), pixelsIds);
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
    	checkDuplicateLSID(Experiment.class, id);
        LinkedHashMap<String, Integer> indexes = new LinkedHashMap<String, Integer>();
        indexes.put("experimentIndex", experimentIndex);
        IObjectContainer o = getIObjectContainer(Experiment.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Experiment.class, id, o);
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
        addReference(key, new LSID(otf));
    }

    public void setOTFObjective(String objective, int instrumentIndex,
            int otfIndex)
    {
        LSID key = new LSID(OTF.class, instrumentIndex, otfIndex);
        addReference(key, new LSID(objective));
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
        addReference(key, new LSID(objective));
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
            
            // Handle the null class (one or more unparsable internal 
            // references) case.
            if (xClass == null || yClass == null)
            {
                return stringComparator.compare(x.toString(), y.toString()); 
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

        //

    }

    private Ellipse getCircle(int imageIndex, int roiIndex, int shapeIndex)
    {
        LinkedHashMap<String, Integer> indexes =
        	new LinkedHashMap<String, Integer>();
        indexes.put("imageIndex", imageIndex);
        indexes.put("roiIndex", roiIndex);
        indexes.put("shapeIndex", shapeIndex);
        return getSourceObject(Ellipse.class, indexes);
    }
    
    public void setCircleCx(String cx, int imageIndex, int roiIndex,
			int shapeIndex)
    {
    	// XXX: Disabled for now
    	//Ellipse o = getCircle(imageIndex, roiIndex, shapeIndex);
    	//o.setCx(toRType(Double.parseDouble(cx)));
	}

	public void setCircleCy(String cy, int imageIndex, int roiIndex,
			int shapeIndex)
	{
		// XXX: Disabled for now
    	//Ellipse o = getCircle(imageIndex, roiIndex, shapeIndex);
    	//o.setCy(toRType(Double.parseDouble(cy)));
	}

	public void setCircleID(String id, int imageIndex, int roiIndex,
			int shapeIndex)
	{
		// XXX: Disabled for now
    	//checkDuplicateLSID(Ellipse.class, id);
        //LinkedHashMap<String, Integer> indexes =
        //	new LinkedHashMap<String, Integer>();
        //indexes.put("imageIndex", imageIndex);
        //indexes.put("roiIndex", roiIndex);
        //indexes.put("shapeIndex", shapeIndex);
        //IObjectContainer o = getIObjectContainer(Ellipse.class, indexes);
        //o.LSID = id;
	}

	public void setCircleR(String r, int imageIndex, int roiIndex,
			int shapeIndex)
	{
		// XXX: Disabled for now
    	//Ellipse o = getCircle(imageIndex, roiIndex, shapeIndex);
    	//Double radius = Double.parseDouble(r);
    	//o.setRx(toRType(radius));
    	//o.setRy(toRType(radius));
	}

	public void setCircleTransform(String transform, int imageIndex,
			int roiIndex, int shapeIndex)
	{
		// XXX: Disabled for now
    	//Ellipse o = getCircle(imageIndex, roiIndex, shapeIndex);
    	//o.setTransform(toRType(transform));
	}

    public void setContactExperimenter(String arg0, int arg1)
    {

        //

    }

    public void setDatasetDescription(String arg0, int arg1)
    {

        //

    }

    public void setDatasetExperimenterRef(String arg0, int arg1)
    {

        //

    }

    public void setDatasetGroupRef(String arg0, int arg1)
    {

        //

    }

    public void setDatasetID(String arg0, int arg1)
    {

        //

    }

    public void setDatasetLocked(Boolean arg0, int arg1)
    {

        //

    }

    public void setDatasetName(String arg0, int arg1)
    {

        //

    }

    public void setDatasetRefID(String arg0, int arg1, int arg2)
    {

        //

    }

    public void setDetectorAmplificationGain(Float amplificationGain,
    		                                 int instrumentIndex,
                                             int detectorIndex)
    {
    	Detector o = getDetector(instrumentIndex, detectorIndex);
    	o.setAmplificationGain(toRType(amplificationGain));
    }

    public void setDetectorZoom(Float zoom, int instrumentIndex,
    		                    int detectorIndex)
    {
    	Detector o = getDetector(instrumentIndex, detectorIndex);
    	o.setZoom(toRType(zoom));
    }

    private Dichroic getDichroic(int instrumentIndex, int dichroicIndex)
	{
	    LinkedHashMap<String, Integer> indexes = 
	    	new LinkedHashMap<String, Integer>();
	    indexes.put("instrumentIndex", instrumentIndex);
	    indexes.put("dichroicIndex", dichroicIndex);
	    return getSourceObject(Dichroic.class, indexes);
	}

	public void setDichroicLotNumber(String lotNumber,
                                     int instrumentIndex,
                                     int dichroicIndex)
    {
    	Dichroic o = getDichroic(instrumentIndex, dichroicIndex);
    	o.setLotNumber(toRType(lotNumber));
    }
    
    public void setDichroicManufacturer(String manufacturer,
    		                            int instrumentIndex,
    		                            int dichroicIndex)
    {
    	Dichroic o = getDichroic(instrumentIndex, dichroicIndex);
    	o.setManufacturer(toRType(manufacturer));
    }

    public void setDichroicModel(String model,
                                 int instrumentIndex,
                                 int dichroicIndex)
    {
    	Dichroic o = getDichroic(instrumentIndex, dichroicIndex);
    	o.setModel(toRType(model));
    }

    public void setDisplayOptionsDisplay(String arg0, int arg1)
    {

        //

    }

    public void setEllipseID(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setEllipseCx(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setEllipseCy(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setEllipseRx(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setEllipseRy(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setEllipseTransform(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }
    
    private Filter getFilter(int instrumentIndex, int filterIndex)
    {
        LinkedHashMap<String, Integer> indexes = 
        	new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", instrumentIndex);
        indexes.put("filterIndex", filterIndex);
        return getSourceObject(Filter.class, indexes);
    }
    
    private TransmittanceRange getTransmittanceRange(int instrumentIndex,
    		                                         int filterIndex)
    {
    	Filter filter = getFilter(instrumentIndex, filterIndex);
        TransmittanceRange range = filter.getTransmittanceRange();
        if (range == null)
        {
        	range = new TransmittanceRangeI();
        	filter.setTransmittanceRange(range);
        }
        return range;
    }

    public void setEmFilterLotNumber(String arg0, int arg1, int arg2)
    {

        //

    }

    public void setEmFilterManufacturer(String arg0, int arg1, int arg2)
    {

        //

    }

    public void setEmFilterModel(String arg0, int arg1, int arg2)
    {

        //

    }

    public void setEmFilterType(String arg0, int arg1, int arg2)
    {

        //

    }

    public void setExFilterLotNumber(String arg0, int arg1, int arg2)
    {

        //

    }

    public void setExFilterManufacturer(String arg0, int arg1, int arg2)
    {

        //

    }

    public void setExFilterModel(String arg0, int arg1, int arg2)
    {

        //

    }

    public void setExFilterType(String arg0, int arg1, int arg2)
    {

        //

    }

    public void setExperimentExperimenterRef(String arg0, int arg1)
    {

        //

    }

    public void setExperimenterOMEName(String arg0, int arg1)
    {

        //

    }
    
    public void setFilterFilterWheel(String filterWheel, int instrumentIndex,
    		                         int filterIndex)
    {
    	Filter o = getFilter(instrumentIndex, filterIndex);
    	o.setFilterWheel(toRType(filterWheel));
    }

    public void setFilterLotNumber(String lotNumber, int instrumentIndex,
                                   int filterIndex)
    {
    	Filter o = getFilter(instrumentIndex, filterIndex);
    	o.setLotNumber(toRType(lotNumber));
    }

    public void setFilterManufacturer(String lotNumber, int instrumentIndex,
                                      int filterIndex)
    {
    	Filter o = getFilter(instrumentIndex, filterIndex);
    	o.setLotNumber(toRType(lotNumber));
    }

    public void setFilterModel(String model, int instrumentIndex,
                               int filterIndex)
    {
    	Filter o = getFilter(instrumentIndex, filterIndex);
    	o.setModel(toRType(model));
    }
    
    private FilterSet getFilterSet(int instrumentIndex,
    		                       int filterSetIndex)
    {
    	LinkedHashMap<String, Integer> indexes = 
    		new LinkedHashMap<String, Integer>();
    	indexes.put("instrumentIndex", instrumentIndex);
    	indexes.put("filterSetIndex", filterSetIndex);
    	return getSourceObject(FilterSet.class, indexes);
    }

    public void setFilterSetDichroic(String dichroic, int instrumentIndex,
    		                         int filterSetIndex)
    {
        LSID key = new LSID(FilterSet.class, instrumentIndex, filterSetIndex);
        addReference(key, new LSID(dichroic));
    }

    public void setFilterSetEmFilter(String emFilter, int instrumentIndex,
    		                         int filterSetIndex)
    {
    	// XXX: Using this suffix is kind of a gross hack but the reference
    	// processing logic does not easily handle multiple A --> B or B --> A 
    	// linkages of the same type so we'll compromise.
    	// Thu Jul 16 13:34:37 BST 2009 -- Chris Allan <callan@blackcat.ca>
    	emFilter += OMERO_EMISSION_FILTER_SUFFIX;
        LSID key = new LSID(FilterSet.class, instrumentIndex, filterSetIndex);
        addReference(key, new LSID(emFilter));
    }

    public void setFilterSetExFilter(String exFilter, int instrumentIndex,
    		                         int filterSetIndex)
    {
    	// XXX: Using this suffix is kind of a gross hack but the reference
    	// processing logic does not easily handle multiple A --> B or B --> A 
    	// linkages of the same type so we'll compromise.
    	// Thu Jul 16 13:34:37 BST 2009 -- Chris Allan <callan@blackcat.ca>
    	exFilter += OMERO_EXCITATION_FILTER_SUFFIX;
        LSID key = new LSID(FilterSet.class, instrumentIndex, filterSetIndex);
        addReference(key, new LSID(exFilter));
    }

    public void setFilterSetLotNumber(String lotNumber, int instrumentIndex,
    		                          int filterSetIndex)
    {
    	FilterSet o = getFilterSet(instrumentIndex, filterSetIndex);
    	o.setLotNumber(toRType(lotNumber));
    }

    public void setFilterSetManufacturer(String manufacturer,
    		                             int instrumentIndex,
    		                             int filterSetIndex)
    {
    	FilterSet o = getFilterSet(instrumentIndex, filterSetIndex);
    	o.setManufacturer(toRType(manufacturer));
    }

    public void setFilterSetModel(String model, int instrumentIndex,
    		                      int filterIndex)
    {
    	Filter o = getFilter(instrumentIndex, filterIndex);
    	o.setModel(toRType(model));
    }

    public void setFilterType(String type, int instrumentIndex, int filterIndex)
    {
    	Filter o = getFilter(instrumentIndex, filterIndex);
    	o.setType((FilterType) getEnumeration(FilterType.class, type));
    }

    public void setGroupName(String arg0, int arg1)
    {

        //

    }

    public void setImageAcquiredPixels(String arg0, int arg1)
    {

        //

    }

    public void setImageExperimentRef(String arg0, int arg1)
    {

        //

    }

    public void setImageExperimenterRef(String arg0, int arg1)
    {

        //

    }

    public void setImageGroupRef(String arg0, int arg1)
    {

        //

    }

    public void setLaserPockelCell(Boolean pockelCell, int instrumentIndex,
    		                       int lightSourceIndex)
    {
    	Laser o = getLaser(instrumentIndex, lightSourceIndex);
    	o.setPockelCell(toRType(pockelCell));
    }

    public void setLaserRepetitionRate(Boolean repetitionRate, 
    		                           int instrumentIndex,
    		                           int lightSourceIndex)
    {
    	Laser o = getLaser(instrumentIndex, lightSourceIndex);
    	//o.setRepetitionRate(toRType(repetitionRate));
    }
    
    private LightSettings getLightSettings(int imageIndex,
    		                               int microbeamManipulationIndex,
    		                               int lightSourceRefIndex)
    {
        LinkedHashMap<String, Integer> indexes = 
        	new LinkedHashMap<String, Integer>();
        indexes.put("imageIndex", imageIndex);
        indexes.put("microbeamManipulationIndex", microbeamManipulationIndex);
        indexes.put("lightSourceRefIndex", lightSourceRefIndex);
        return getSourceObject(LightSettings.class, indexes);
    }

    public void setLightSourceRefAttenuation(Float attenuation, int imageIndex,
    		                                 int microbeamManipulationIndex,
    		                                 int lightSourceRefIndex)
    {
    	LightSettings o = getLightSettings(imageIndex,
    			                           microbeamManipulationIndex,
    			                           lightSourceRefIndex);
    	o.setAttenuation(toRType(attenuation));
    }

    public void setLightSourceRefLightSource(String lightSource, int imageIndex,
                                             int microbeamManipulationIndex,
                                             int lightSourceRefIndex)
    {
        LSID key = new LSID(LightSettings.class, imageIndex,
                            microbeamManipulationIndex,
                            lightSourceRefIndex);
        addReference(key, new LSID(lightSource));
    }

    public void setLightSourceRefWavelength(Integer wavelength, int imageIndex,
                                            int microbeamManipulationIndex,
                                            int lightSourceRefIndex)
    {
    	LightSettings o = getLightSettings(imageIndex,
                                           microbeamManipulationIndex,
                                           lightSourceRefIndex);
    	o.setWavelength(toRType(wavelength));
    }

    public void setLineID(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setLineTransform(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setLineX1(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setLineX2(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setLineY1(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setLineY2(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setLogicalChannelDetector(
    		String detector, int imageIndex, int logicalChannelIndex)
    {
    	log.warn("Handling legacy LogicalChannel --> Detector reference.");
    	// Create the non-existant DetectorSettings object; it's unlikely
    	// we'll ever see method calls associated with it because the Reader
    	// that called us is likely the OMEXMLReader with a 2003FC OME-XML 
    	// instance document or OME-TIFF.
    	getDetectorSettings(imageIndex, logicalChannelIndex);
    	setDetectorSettingsDetector(detector, imageIndex, logicalChannelIndex);
    }

    public void setLogicalChannelFilterSet(
    		String filterSet, int imageIndex, int logicalChannelIndex)
    {
        LSID key = new LSID(LogicalChannel.class, imageIndex,
                            logicalChannelIndex);
        addReference(key, new LSID(filterSet));
    }

    public void setLogicalChannelLightSource(
    		String lightSource, int imageIndex, int logicalChannelIndex)
    {
    	log.warn("Handling legacy LogicalChannel --> LightSource reference.");
    	// Create the non-existant LightSettings object; it's unlikely
    	// we'll ever see method calls associated with it because the Reader
    	// that called us is likely the OMEXMLReader with a 2003FC OME-XML 
    	// instance document or OME-TIFF.
    	getLightSettings(imageIndex, logicalChannelIndex);
    	setLightSourceSettingsLightSource(lightSource, imageIndex,
    			                          logicalChannelIndex);
    }

    public void setLogicalChannelSecondaryEmissionFilter(
    		String secondaryEmissionFilter, int imageIndex,
    		int logicalChannelIndex)
    {
    	// XXX: Using this suffix is kind of a gross hack but the reference
    	// processing logic does not easily handle multiple A --> B or B --> A 
    	// linkages of the same type so we'll compromise.
    	// Thu Jul  2 12:08:19 BST 2009 -- Chris Allan <callan@blackcat.ca>
    	secondaryEmissionFilter += OMERO_EMISSION_FILTER_SUFFIX;
        LSID key = new LSID(LogicalChannel.class, imageIndex,
	                        logicalChannelIndex);
        addReference(key, new LSID(secondaryEmissionFilter));
    }

    public void setLogicalChannelSecondaryExcitationFilter(
    		String secondaryExcitationFilter, int imageIndex,
    		int logicalChannelIndex)
    {
    	// XXX: Using this suffix is kind of a gross hack but the reference
    	// processing logic does not easily handle multiple A --> B or B --> A 
    	// linkages of the same type so we'll compromise.
    	// Thu Jul  2 12:08:19 BST 2009 -- Chris Allan <callan@blackcat.ca>
    	secondaryExcitationFilter += OMERO_EXCITATION_FILTER_SUFFIX;
        LSID key = new LSID(LogicalChannel.class, imageIndex,
        		            logicalChannelIndex);
        addReference(key, new LSID(secondaryExcitationFilter));
    }
    
    public Mask getMask(int imageIndex, int roiIndex, int shapeIndex)
    {
        LinkedHashMap<String, Integer> indexes =
        	new LinkedHashMap<String, Integer>();
        indexes.put("imageIndex", imageIndex);
        indexes.put("roiIndex", roiIndex);
        indexes.put("shapeIndex", shapeIndex);
        return getSourceObject(Mask.class, indexes);
    }

    public void setMaskHeight(String height, int imageIndex, int roiIndex, int shapeIndex)
    {
    	// XXX: Disabled for now
    	//Mask o = getMask(imageIndex, roiIndex, shapeIndex);
    	//o.setHeight(toRType(Double.parseDouble(height)));
    }

    public void setMaskID(String id, int imageIndex, int roiIndex, int shapeIndex)
    {
    	// XXX: Disabled for now
    	//checkDuplicateLSID(Mask.class, id);
        //LinkedHashMap<String, Integer> indexes =
        //	new LinkedHashMap<String, Integer>();
        //indexes.put("imageIndex", imageIndex);
        //indexes.put("roiIndex", roiIndex);
        //indexes.put("shapeIndex", shapeIndex);
        //IObjectContainer o = getIObjectContainer(Mask.class, indexes);
        //o.LSID = id;
    }

    public void setMaskTransform(String transform, int imageIndex, int roiIndex, int shapeIndex)
    {
    	// XXX: Disabled for now
    	//Mask o = getMask(imageIndex, roiIndex, shapeIndex);
    	//o.setTransform(toRType(transform));
    }

    public void setMaskWidth(String width, int imageIndex, int roiIndex, int shapeIndex)
    {
    	// XXX: Disabled for now
    	//Mask o = getMask(imageIndex, roiIndex, shapeIndex);
    	//o.setWidth(toRType(Double.parseDouble(width)));
    }

    public void setMaskX(String x, int imageIndex, int roiIndex, int shapeIndex)
    {
    	// XXX: Disabled for now
    	//Mask o = getMask(imageIndex, roiIndex, shapeIndex);
    	//o.setX(toRType(Double.parseDouble(x)));
    }

    public void setMaskY(String y, int imageIndex, int roiIndex, int shapeIndex)
    {
    	// XXX: Disabled for now
    	//Mask o = getMask(imageIndex, roiIndex, shapeIndex);
    	//o.setY(toRType(Double.parseDouble(y)));
    }

    public void setMaskPixelsBigEndian(Boolean bigEndian, int imageIndex, int roiIndex, int shapeIndex)
    {
    }

    public void setMaskPixelsBinData(byte[] binData, int imageIndex, int roiIndex, int shapeIndex)
    {
    	// XXX: Disabled for now
    	//Mask o = getMask(imageIndex, roiIndex, shapeIndex);
    	//o.setBytes(binData);
    }

    public void setMaskPixelsExtendedPixelType(String extendedPixelType, int imageIndex, int roiIndex, int shapeIndex)
    {
    }

    public void setMaskPixelsID(String id, int imageIndex, int roiIndex, int shapeIndex)
    {
    }

    public void setMaskPixelsSizeX(Integer sizeX, int imageIndex, int roiIndex, int shapeIndex)
    {
    }

    public void setMaskPixelsSizeY(Integer sizeY, int imageIndex, int roiIndex, int shapeIndex)
    {
    }

    public void setMicrobeamManipulationExperimenterRef(String arg0, int arg1,
            int arg2)
    {

        //

    }

    public void setMicrobeamManipulationID(String arg0, int arg1, int arg2)
    {

        //

    }

    public void setMicrobeamManipulationRefID(String arg0, int arg1, int arg2)
    {

        //

    }

    public void setMicrobeamManipulationType(String arg0, int arg1, int arg2)
    {

        //

    }
    
    private Microscope getMicroscope(int instrumentIndex)
    {
    	Instrument instrument = getInstrument(instrumentIndex);
    	Microscope microscope = instrument.getMicroscope();
    	if (microscope == null)
    	{
    		microscope = new MicroscopeI();
    		instrument.setMicroscope(microscope);
    	}
    	return microscope;
    }

    public void setMicroscopeID(String id, int instrumentIndex)
    {
    	// TODO: Not in model, etc.
    }

    public void setMicroscopeManufacturer(String manufacturer,
    		                              int instrumentIndex)
    {
    	Microscope o = getMicroscope(instrumentIndex);
    	o.setManufacturer(toRType(manufacturer));
    }

    public void setMicroscopeModel(String model, int instrumentIndex)
    {
    	Microscope o = getMicroscope(instrumentIndex);
    	o.setModel(toRType(model));
    }

    public void setMicroscopeSerialNumber(String serialNumber,
    		                              int instrumentIndex)
    {
    	Microscope o = getMicroscope(instrumentIndex);
    	o.setSerialNumber(toRType(serialNumber));
    }

    public void setMicroscopeType(String type, int instrumentIndex)
    {
    	Microscope o = getMicroscope(instrumentIndex);
    	o.setType((MicroscopeType) getEnumeration(MicroscopeType.class, type));
    }

    public void setOTFBinaryFile(String arg0, int arg1, int arg2)
    {

        //

    }

    public void setPlaneHashSHA1(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setPlaneID(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setPlateRefSample(Integer arg0, int arg1, int arg2)
    {

        //

    }

    public void setPlateRefWell(String arg0, int arg1, int arg2)
    {

        //

    }

    public void setPointID(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setPointCx(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setPointCy(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setPointR(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setPointTransform(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setPolygonID(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setPolygonPoints(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setPolygonTransform(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setPolylineID(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setPolylinePoints(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setPolylineTransform(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setProjectDescription(String arg0, int arg1)
    {

        //

    }

    public void setProjectExperimenterRef(String arg0, int arg1)
    {

        //

    }

    public void setProjectGroupRef(String arg0, int arg1)
    {

        //

    }

    public void setProjectID(String arg0, int arg1)
    {

        //

    }

    public void setProjectName(String arg0, int arg1)
    {

        //

    }

    public void setProjectRefID(String arg0, int arg1, int arg2)
    {

        //

    }

    public void setPumpLightSource(String lightSource, int instrumentIndex,
    		                       int lightSourceIndex)
    {
        LSID key = new LSID(LightSource.class, instrumentIndex,
        		            lightSourceIndex);
        addReference(key, new LSID(lightSource));
    }

    public void setROIRefID(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setRectID(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setRectHeight(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setRectTransform(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setRectWidth(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setRectX(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setRectY(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setRegionID(String arg0, int arg1, int arg2)
    {

        //

    }

    public void setRegionName(String arg0, int arg1, int arg2)
    {

        //

    }

    public void setRegionTag(String arg0, int arg1, int arg2)
    {

        //

    }

    public void setScreenDescription(String description, int screenIndex)
    {
    	//Screen o = getScreen(screenIndex);
    	//o.setDescription(toRType(description));
    	// Disabled
    }

    public void setScreenExtern(String extern, int screenIndex)
    {
    	//
    }

    public void setScreenReagentSetIdentifier(String reagentSetIdentifier,
    		                                  int screenIndex)
    {
    	//Screen o = getScreen(screenIndex);
    	//o.setReagentSetIdentifier(toRType(reagentSetIdentifier));
    	// Disabled
    }

    public void setScreenRefID(String arg0, int arg1, int arg2)
    {

    }

    public void setThumbnailID(String arg0, int arg1)
    {

    }

    public void setThumbnailMIMEtype(String arg0, int arg1)
    {

    }

    public void setThumbnailHref(String arg0, int arg1)
    {

    }

    public void setTransmittanceRangeCutIn(Integer cutIn, int instrumentIndex,
    		                               int filterIndex)
    {
    	TransmittanceRange o = getTransmittanceRange(instrumentIndex,
                                                     filterIndex);
        o.setCutIn(toRType(cutIn));
    }

    public void setTransmittanceRangeCutInTolerance(Integer cutInTolerance,
    		                                        int instrumentIndex,
    		                                        int filterIndex)
    {
    	TransmittanceRange o = getTransmittanceRange(instrumentIndex,
                                                     filterIndex);
    	o.setCutInTolerance(toRType(cutInTolerance));
    }

    public void setTransmittanceRangeCutOut(Integer cutOut, 
    		                                int instrumentIndex,
    		                                int filterIndex)
    {
    	TransmittanceRange o = getTransmittanceRange(instrumentIndex,
                                                     filterIndex);
    	o.setCutOut(toRType(cutOut));
    }

    public void setTransmittanceRangeCutOutTolerance(Integer cutOutTolerance,
    		                                         int instrumentIndex,
    		                                         int filterIndex)
    {
    	TransmittanceRange o = getTransmittanceRange(instrumentIndex,
                                                     filterIndex);
    	o.setCutOutTolerance(toRType(cutOutTolerance));
    }

    public void setTransmittanceRangeTransmittance(Integer transmittance,
    		                                       int instrumentIndex,
    		                                       int filterIndex)
    {
    	TransmittanceRange o = getTransmittanceRange(instrumentIndex,
    			                                     filterIndex);
    	// TODO: Hack, model has integer, database has double
    	o.setTransmittance(toRType(new Double(transmittance)));
    }

    public void setWellReagent(String reagent, int plateIndex, int wellIndex)
    {
        LSID key = new LSID(Well.class, plateIndex, wellIndex);
        addReference(key, new LSID(reagent));
    }

    public void setWellSampleImageRef(String image, int plateIndex, 
            int wellIndex, int wellSampleIndex)
    {
        LSID key = new LSID(WellSample.class, plateIndex,
        		            wellIndex, wellSampleIndex);
        addReference(key, new LSID(image));
    }

    public void setWellSampleRefID(String arg0, int arg1, int arg2, int arg3)
    {
    	//
    }

    public void setPlateColumnNamingConvention(String columnNamingConvention,
    		                                   int plateIndex)
    {
        Plate o = getPlate(plateIndex);
        o.setColumnNamingConvention(toRType(columnNamingConvention));
    }

    public void setPlateRowNamingConvention(String rowNamingConvention,
    		                                int plateIndex)
    {
        Plate o = getPlate(plateIndex);
        o.setRowNamingConvention(toRType(rowNamingConvention));
    }

    public void setPlateWellOriginX(Double wellOriginX, int plateIndex)
    {
        Plate o = getPlate(plateIndex);
        o.setWellOriginX(toRType(wellOriginX));
    }

    public void setPlateWellOriginY(Double wellOriginY, int plateIndex)
    {
        Plate o = getPlate(plateIndex);
        o.setWellOriginX(toRType(wellOriginY));
    }

    public void setPathD(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }

    public void setPathID(String arg0, int arg1, int arg2, int arg3)
    {

        //

    }
    
    private Shape getShape(int imageIndex, int roiIndex, int shapeIndex)
    {
        LinkedHashMap<String, Integer> indexes =
        	new LinkedHashMap<String, Integer>();
        indexes.put("imageIndex", imageIndex);
        indexes.put("roiIndex", roiIndex);
        indexes.put("shapeIndex", shapeIndex);
        return getSourceObject(Shape.class, indexes);
    }
    
    public void setShapeBaselineShift(String baselineShift, int imageIndex,
			int roiIndex, int shapeIndex)
    {
	}

	public void setShapeDirection(String direction, int imageIndex,
			int roiIndex, int shapeIndex)
	{
	}

	public void setShapeFillColor(String fillColor, int imageIndex,
			int roiIndex, int shapeIndex) {
	}

	public void setShapeFillOpacity(String fillOpacity, int imageIndex,
			int roiIndex, int shapeIndex) {
	}

	public void setShapeFillRule(String fillRule, int imageIndex, int roiIndex,
			int shapeIndex) {
	}

	public void setShapeFontFamily(String fontFamily, int imageIndex,
			int roiIndex, int shapeIndex) {
	}

	public void setShapeFontSize(Integer fontSize, int imageIndex,
			int roiIndex, int shapeIndex) {
	}

	public void setShapeFontStretch(String fontStretch, int imageIndex,
			int roiIndex, int shapeIndex) {
	}

	public void setShapeFontStyle(String fontStyle, int imageIndex,
			int roiIndex, int shapeIndex) {
	}

	public void setShapeFontVariant(String fontVariant, int imageIndex,
			int roiIndex, int shapeIndex) {
	}

	public void setShapeFontWeight(String fontWeight, int imageIndex,
			int roiIndex, int shapeIndex) {
	}

	public void setShapeG(String g, int imageIndex, int roiIndex, int shapeIndex) {
	}

	public void setShapeGlyphOrientationVertical(
			Integer glyphOrientationVertical, int imageIndex, int roiIndex,
			int shapeIndex) {
	}

	public void setShapeID(String id, int imageIndex, int roiIndex,
			int shapeIndex) {
	}

	public void setShapeLocked(Boolean locked, int imageIndex, int roiIndex,
			int shapeIndex) {
	}

	public void setShapeStrokeAttribute(String strokeAttribute, int imageIndex,
			int roiIndex, int shapeIndex) {
	}

	public void setShapeStrokeColor(String strokeColor, int imageIndex,
			int roiIndex, int shapeIndex) {
	}

	public void setShapeStrokeDashArray(String strokeDashArray, int imageIndex,
			int roiIndex, int shapeIndex) {
	}

	public void setShapeStrokeLineCap(String strokeLineCap, int imageIndex,
			int roiIndex, int shapeIndex) {
	}

	public void setShapeStrokeLineJoin(String strokeLineJoin, int imageIndex,
			int roiIndex, int shapeIndex) {
	}

	public void setShapeStrokeMiterLimit(Integer strokeMiterLimit,
			int imageIndex, int roiIndex, int shapeIndex) {
	}

	public void setShapeStrokeOpacity(Float strokeOpacity, int imageIndex,
			int roiIndex, int shapeIndex) {
	}

	public void setShapeStrokeWidth(Integer strokeWidth, int imageIndex,
			int roiIndex, int shapeIndex) {
	}

	public void setShapeText(String text, int imageIndex, int roiIndex,
			int shapeIndex) {
	}

	public void setShapeTextAnchor(String textAnchor, int imageIndex,
			int roiIndex, int shapeIndex) {
	}

	public void setShapeTextDecoration(String textDecoration, int imageIndex,
			int roiIndex, int shapeIndex) {
	}

	public void setShapeTextFill(String textFill, int imageIndex, int roiIndex,
			int shapeIndex) {
	}

	public void setShapeTextStroke(String textStroke, int imageIndex,
			int roiIndex, int shapeIndex) {
	}

	public void setShapeTheT(Integer theT, int imageIndex, int roiIndex,
			int shapeIndex) {
		// XXx: Disabled for now
		//Shape o = getShape(imageIndex, roiIndex, shapeIndex);
		//o.setTheT(toRType(theT));
	}

	public void setShapeTheZ(Integer theZ, int imageIndex, int roiIndex,
			int shapeIndex) {
		// XXX: Disabled for now
		//Shape o = getShape(imageIndex, roiIndex, shapeIndex);
		//o.setTheZ(toRType(theZ));
	}

	public void setShapeVectorEffect(String vectorEffect, int imageIndex,
			int roiIndex, int shapeIndex) {
	}

	public void setShapeVisibility(Boolean visibility, int imageIndex,
			int roiIndex, int shapeIndex) {
	}

	public void setShapeWritingMode(String writingMode, int imageIndex,
			int roiIndex, int shapeIndex) {
	}


   /*-----------*/
    
    public class SortProjectsByName implements Comparator<Project>{
        public int compare(Project o1, Project o2)
        {
            return o1.getName().getValue().compareTo(o2.getName().getValue());
        }
     }
    
    
    public class SortDatasetsByName implements Comparator<Dataset>{
        public int compare(Dataset o1, Dataset o2)
        {
            return o1.getName().getValue().compareTo(o2.getName().getValue());
        }
     }
    

    /**
     * Based on immmersion table bug in 4.0 this is a hack to fix in code those enums missing/broken
     * 
     *  replace l1[0:3] (['Gly', 'Hl', 'Oel']) l2[0:3] (['Air', 'Glycerol', 'Multi'])
     *  insert l1[4:4] ([]) l2[4:5] (['Other'])
     *   delete l1[5:6] (['Wasser']) l2[6:6] ([])
     *  replace l1[7:8] (['Wl']) l2[7:8] (['WaterDipping'])
     */
    private void checkImmersions()
    {   
        String[] immersionAddStrings = {"Air", "Glycerol", "Multi", "WaterDipping", "Other"};
        List<String> immersionAdds = Arrays.asList(immersionAddStrings);
        
        String[] immersionDeleteStrings = {"Gly", "Hl", "Oel", "Wasser", "Wl"};
        List<String> immersionDeletes = Arrays.asList(immersionDeleteStrings);
        
        try
        {
            ITypesPrx types = serviceFactory.getTypesService();
            List<IObject> immersionList = types.allEnumerations("omero.model.Immersion");
            List<String> immersionStrings = new ArrayList<String>();
            
            for (IObject immersion: immersionList)
            {
                Immersion immersion2 = (Immersion) immersion;
                immersionStrings.add(immersion2.getValue().getValue());
                log.info("Found immersion: " + immersion2.getValue().getValue());
            }
            
            for (String i: immersionAdds)
            {
                if (!immersionStrings.contains(i))
                {
                    Immersion immersion  = new ImmersionI();
                    immersion.setValue(rstring(i));
                    //types.createEnumeration(immersion);
                    log.info("Adding missing immersion: " + i);
                }

            }
            
            for (String i: immersionDeletes)
            {
                int index = immersionStrings.indexOf(i);
                
                if (index != -1)
                {
                    //types.deleteEnumeration(immersionList.get(index));
                    log.info("Deleting bad immersion: " + i);
                }
            }
            
        } catch (ServerError e)
        {
            log.error("checkImmersions() failure", e);
        }
    }

	public void setDichroicID(String id, int instrumentIndex, int dichroicIndex)
	{
		checkDuplicateLSID(Dichroic.class, id);
        LinkedHashMap<String, Integer> indexes = 
        	new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", instrumentIndex);
        indexes.put("dichroicIndex", dichroicIndex);
        IObjectContainer o = getIObjectContainer(Dichroic.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Dichroic.class, id, o);
	}

	public void setFilterID(String id, int instrumentIndex, int filterIndex)
	{
		checkDuplicateLSID(Filter.class, id);
        LinkedHashMap<String, Integer> indexes = 
        	new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", instrumentIndex);
        indexes.put("filterIndex", filterIndex);
        IObjectContainer o = getIObjectContainer(Filter.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Filter.class, id, o);
	}

	public void setFilterSetID(String id, int instrumentIndex,
			                   int filterSetIndex)
	{
		checkDuplicateLSID(FilterSet.class, id);
        LinkedHashMap<String, Integer> indexes = 
        	new LinkedHashMap<String, Integer>();
        indexes.put("instrumentIndex", instrumentIndex);
        indexes.put("filterSetIndex", filterSetIndex);
        IObjectContainer o = getIObjectContainer(FilterSet.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(FilterSet.class, id, o);
	}

	public void setRoiLinkDirection(String arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	public void setRoiLinkName(String arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}

	public void setRoiLinkRef(String arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
	}
	
        public void setGroupID(String arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
}
