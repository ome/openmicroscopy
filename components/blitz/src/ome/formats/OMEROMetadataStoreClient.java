/*
 * ome.formats.OMEROMetadataStoreClient
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
 *
 */
package ome.formats;

import static omero.rtypes.rbool;
import static omero.rtypes.rdouble;
import static omero.rtypes.rint;
import static omero.rtypes.rlong;
import static omero.rtypes.rstring;
import static omero.rtypes.rtime;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import loci.formats.IFormatReader;
import loci.formats.ImageReader;
import loci.formats.meta.IMinMaxStore;
import loci.formats.meta.MetadataStore;
import ome.formats.enums.EnumerationProvider;
import ome.formats.enums.IQueryEnumProvider;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.util.ClientKeepAlive;
import ome.formats.model.BlitzInstanceProvider;
import ome.formats.model.ChannelProcessor;
import ome.formats.model.IObjectContainerStore;
import ome.formats.model.InstanceProvider;
import ome.formats.model.InstrumentProcessor;
import ome.formats.model.ModelProcessor;
import ome.formats.model.PixelsProcessor;
import ome.formats.model.PlaneInfoProcessor;
import ome.formats.model.ReferenceProcessor;
import ome.formats.model.ShapeProcessor;
import ome.formats.model.TargetProcessor;
import ome.formats.model.WellProcessor;
import ome.system.UpgradeCheck;
import ome.util.LSID;
import ome.xml.model.enums.IlluminationType;
import ome.xml.model.enums.NamingConvention;
import ome.xml.model.enums.PixelType;
import ome.xml.model.primitives.NonNegativeInteger;
import ome.xml.model.primitives.NonNegativeLong;
import ome.xml.model.primitives.PercentFraction;
import ome.xml.model.primitives.PositiveInteger;
import ome.xml.model.primitives.PositiveFloat;
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
import omero.model.BooleanAnnotation;
import omero.model.Channel;
import omero.model.CommentAnnotation;
import omero.model.ContrastMethod;
import omero.model.Correction;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.Detector;
import omero.model.DetectorSettings;
import omero.model.DetectorType;
import omero.model.Dichroic;
import omero.model.DimensionOrder;
import omero.model.DoubleAnnotation;
import omero.model.Ellipse;
import omero.model.Experiment;
import omero.model.ExperimentType;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
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
import omero.model.Label;
import omero.model.Laser;
import omero.model.LaserMedium;
import omero.model.LaserType;
import omero.model.LightEmittingDiode;
import omero.model.LightPath;
import omero.model.LightSettings;
import omero.model.LightSource;
import omero.model.Line;
import omero.model.ListAnnotation;
import omero.model.LogicalChannel;
import omero.model.LongAnnotation;
import omero.model.Mask;
import omero.model.Medium;
import omero.model.MicrobeamManipulation;
import omero.model.MicrobeamManipulationType;
import omero.model.Microscope;
import omero.model.MicroscopeI;
import omero.model.MicroscopeType;
import omero.model.OTF;
import omero.model.Objective;
import omero.model.ObjectiveSettings;
import omero.model.OriginalFile;
import omero.model.Path;
import omero.model.Permissions;
import omero.model.Pixels;
import omero.model.PixelsType;
import omero.model.PlaneInfo;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.Point;
import omero.model.Polyline;
import omero.model.Project;
import omero.model.ProjectI;
import omero.model.Pulse;
import omero.model.Reagent;
import omero.model.Rect;
import omero.model.Roi;
import omero.model.Screen;
import omero.model.ScreenI;
import omero.model.StageLabel;
import omero.model.TagAnnotation;
import omero.model.TermAnnotation;
import omero.model.TimestampAnnotation;
import omero.model.TransmittanceRange;
import omero.model.TransmittanceRangeI;
import omero.model.Well;
import omero.model.WellSample;
import omero.model.XmlAnnotation;
import omero.sys.EventContext;
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

    /**
     * Begins empty to allow access to all groups. Once a target object
     * has been chosen, the id will be set to reflect the target.
     */
    private Long groupID = null;

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
    private List<ModelProcessor> modelProcessors;

    /** Bio-Formats reader that's populating us. */
    private IFormatReader reader;

    private List<Pixels> pixelsList;

    private boolean encryptedConnection = false;

    private client c;
    private ServiceFactoryPrx serviceFactory;
    private EventContext eventContext;
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

    /** Plate name the user specified for use by model processors. */
    private String userSpecifiedPlateName;

    /** Plate description the user specified for use by model processors. */
    private String userSpecifiedPlateDescription;

    /** Linkage target for all Images/Plates for use by model processors. */
    private IObject userSpecifiedTarget;

    /** Physical pixel sizes the user specified for use by model processors. */
    private Double[] userSpecifiedPhysicalPixelSizes;

    /** Image channel minimums and maximums. */
    private double[][][] imageChannelGlobalMinMax;

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
	omero.constants.namespaces.NSCOMPANIONFILE.value;

    /** Original metadata text file key */
    private static final String ORIGINAL_METADATA_KEY =
        "ORIGINAL_METADATA_TXT";

    /** The default longest side of a thumbnail in OMERO.insight. */
    private static final int DEFAULT_INSIGHT_THUMBNAIL_LONGEST_SIDE = 96;

    /** Keep alive runnable, pings all services. */
    private ClientKeepAlive keepAlive = new ClientKeepAlive();

    /**
     * Map of series vs. populated Image graph as set by <code>prepare</code>.
     * This map is valid for a single execution of <code>importImage()</code>
     * only.
     */
    private Map<Integer, Image> existingMetadata;

    /**
     * Returns clientKeepAlive created in store
     *
     * @return current ClientKeepAlive
     */
    public ClientKeepAlive getKeepAlive() {
		return keepAlive;
	}

    private void resetPixelsId(Long pixId) throws ServerError
    {
        if (pixId != null && !pixId.equals(currentPixId))
        {
            rawPixelStore.setPixelsId(pixId, true);
            currentPixId = pixId;
        }
    }

    public void logVersionInfo(String clientVersion) throws ServerError {
    	if (serviceFactory != null)
    		log.info("Server: " + serviceFactory.getConfigService().getVersion());
    	else
    		log.info("Unknown server version (no service factory)");
    	if (clientVersion != null)
    		log.info("Client: " + clientVersion);
    	else
    		log.info("Unknown client version (null sent)");
        log.info("Java Version: " + System.getProperty("java.version"));
        log.info("OS Name: " + System.getProperty("os.name"));
        log.info("OS Arch: " + System.getProperty("os.arch"));
        log.info("OS Version: " + System.getProperty("os.version"));
    }

    /**
     * Initialize all services needed
     *
     * @param manageLifecylce
     *
     *            Whether or not to call the {@link Thread#start()} method on
     *            the {@link #keepAlive} instance. This will be set to false
     *            when an {@link omero.client} or a {@link ServiceFactoryPrx}
     *            instance is provided to {@link #initialize(client)} since the
     *            assumption is that the consumer will take care of the keep
     *            alive. In that case, {@link #closeServices()} should be called
     *            when importing is finished.
     *
     *  @param group
     *
     *            Value to pass set in {@link #callCtx}
     *
     * @throws ServerError
     */
    private void initializeServices(boolean manageLifecycle)
        throws ServerError
    {

        closeServices();
        Map<String, String> callCtx = new HashMap<String, String>();
        if (groupID != null) {
            callCtx.put("omero.group", groupID.toString());
            log.info(String.format("Call context: {omero.group:%s}", groupID));
        }

        // Blitz services
        iAdmin = (IAdminPrx) serviceFactory.getAdminService().ice_context(callCtx);
        iQuery = (IQueryPrx) serviceFactory.getQueryService().ice_context(callCtx);
        eventContext = iAdmin.getEventContext();
        iUpdate = (IUpdatePrx) serviceFactory.getUpdateService().ice_context(callCtx);
        rawFileStore = (RawFileStorePrx) serviceFactory.createRawFileStore().ice_context(callCtx);
        rawPixelStore = (RawPixelsStorePrx) serviceFactory.createRawPixelsStore().ice_context(callCtx);
        thumbnailStore = (ThumbnailStorePrx) serviceFactory.createThumbnailStore().ice_context(callCtx);
        iRepoInfo = (IRepositoryInfoPrx) serviceFactory.getRepositoryInfoService().ice_context(callCtx);
        iContainer = (IContainerPrx) serviceFactory.getContainerService().ice_context(callCtx);
        iSettings = (IRenderingSettingsPrx) serviceFactory.getRenderingSettingsService().ice_context(callCtx);
        delegate = (MetadataStorePrx) MetadataStorePrxHelper.checkedCast(
                serviceFactory.getByName(METADATASTORE.value)).ice_context(callCtx);

        // Client side services
        enumProvider = new IQueryEnumProvider(iQuery);
        instanceProvider = new BlitzInstanceProvider(enumProvider);

        // Default model processors
        modelProcessors = new ArrayList<ModelProcessor>();
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
        if (manageLifecycle)
        {
            if (executor == null)
            {
                executor = new ScheduledThreadPoolExecutor(1);
                executor.scheduleWithFixedDelay(keepAlive, 60, 60, TimeUnit.SECONDS);
            }
        }
        keepAlive.setClient(this); // This is used elsewhere.
    }

    /**
     * @return IQuery proxy
     */
    public IQueryPrx getIQuery()
    {
        return iQuery;
    }


	public void setEncryptedConnection(boolean encryptedConnection) {
		this.encryptedConnection = encryptedConnection;
	}

	public boolean isEncryptedConnection() {
		return encryptedConnection;
	}

	/**
	 * Sets the id which will be used by {@link #initializeServices(boolean)}
	 * to set the call context for all services. If null, the call context
	 * will be left which will then use the context of the session.
	 *
	 * @param groupID
	 * @return
	 */
	public Long setGroup(Long groupID) {
	    Long old = this.groupID;
	    this.groupID = groupID;
	    return old;
	}

	/**
     * Initializes the MetadataStore with an already logged in, ready to go
     * service factory. When finished with this instance, close stateful
     * services via {@link #closeServices()}.
     * @param serviceFactory The factory. Mustn't be <code>null</code>.
     */
    public void initialize(ServiceFactoryPrx serviceFactory)
        throws ServerError
    {
        if (serviceFactory == null)
            throw new IllegalArgumentException("No factory.");
        this.serviceFactory = serviceFactory;
        initializeServices(false);
    }

    /**
     * Initializes the MetadataStore with an already logged in, ready to go
     * service factory. When finished with this instance, close stateful
     * services via {@link #closeServices()}.
     *
     * @param c The client. Mustn't be <code>null</code>.
     */
    public void initialize(omero.client c)
        throws ServerError
    {
        this.c = c;
        c.setAgent("OMERO.importer");
        serviceFactory = c.getSession();
        initializeServices(false);
    }

    /**
     * Initializes the MetadataStore taking string parameters to feed to the
     * OMERO Blitz client object. Using this method creates an unsecure
     * session. When finished with this instance, close all resources via
     * {@link #logout}
     *
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
	// Always make this an unsecure session
        initialize(username, password, server, port, false);
    }

    /**
     * Initializes the MetadataStore taking string parameters to feed to the
     * OMERO Blitz client object. Using this method to create either secure
     * or unsecure sessions. When finished with this instance, close all resources via
     * {@link #logout}
     *
     * @param username User's omename.
     * @param password User's password.
     * @param server Server hostname.
     * @param port Server port.
     * @param isSecure is this session secure
     * @throws CannotCreateSessionException If there is a session error when
     * creating the OMERO Blitz client object.
     * @throws PermissionDeniedException If there is a problem logging the user
     * in.
     * @throws ServerError If there is a critical error communicating with the
     * server.
     */
	public void initialize(String username, String password,
            String server, int port, boolean isSecure)
	throws CannotCreateSessionException, PermissionDeniedException, ServerError
	{
        secure(server, port);
        c.createSession(username, password);
	if (!isSecure)
	{
	    unsecure();
	}
        initializeServices(true);
	}

    /**
     * Initializes the MetadataStore taking string parameters to feed to the
     * OMERO Blitz client object. Using this method to create either secure
     * or unsecure sessions and sets the user's group to supplied group.
     * When finished with this instance, close all resources via
     * {@link #logout}
     *
     * @param username User's omename.
     * @param password User's password.
     * @param server Server hostname.
     * @param port Server port.
     * @param group User's current group.
     * @param isSecure is this session secure
     * @throws CannotCreateSessionException If there is a session error when
     * creating the OMERO Blitz client object.
     * @throws PermissionDeniedException If there is a problem logging the user
     * in.
     * @throws ServerError If there is a critical error communicating with the
     * server.
     */
	public void initialize(String username, String password,
            String server, int port, Long group, boolean isSecure)
	throws CannotCreateSessionException, PermissionDeniedException, ServerError
	{
        secure(server, port);
        serviceFactory = c.createSession(username, password);
	if (!isSecure)
	{
	    unsecure();
	}
	    setGroup(group);
        initializeServices(true);
	}

    /**
     * Initializes the MetadataStore by joining an existing session.
     * Use this method only with unsecure sessions. When finished with this
     * instance, close all resources via {@link #logout}
     *
     * @param server Server hostname.
     * @param port Server port.
     * @param sessionKey Bind session key.
     */
    public void initialize(String server, int port, String sessionKey)
        throws CannotCreateSessionException, PermissionDeniedException, ServerError
    {
	// Always make this an 'unsecure' session
        initialize(server, port, sessionKey, false);
    }

    /**
     * Initializes the MetadataStore by joining an existing session.
     * Use this method only with unsecure sessions. When finished with this
     * instance, close all resources via {@link #logout}
     *
     * @param server Server hostname.
     * @param port Server port.
     * @param sessionKey Bind session key.
     */
    public void initialize(String server, int port, String sessionKey, boolean isSecure)
        throws CannotCreateSessionException, PermissionDeniedException, ServerError
    {
        secure(server, port);
        serviceFactory = c.joinSession(sessionKey);
	if (!isSecure)
	{
            unsecure();
	}
        initializeServices(true);
    }

    /**
     * First phase of login is to make an SSL connection. Creates an
     * {@link omero.client} instance and calls {@link omero.client#setAgent(String)}
     * @param server
     * @param port
     * @throws CannotCreateSessionException
     * @throws PermissionDeniedException
     * @throws ServerError
     */
    private void secure(String server, int port) throws CannotCreateSessionException,
            PermissionDeniedException, ServerError {
        log.info(String.format(
                    "Attempting initial SSL connection to %s:%d",
                    server, port));
        c = new client(server, port);
        c.setAgent("OMERO.importer");
    }

    /**
     * Second phase of login is to drop down to a non-SSL connection. Uses
     * {@link omero.client#createClient(boolean)} to create a new instance and
     * closes the old.
     *
     * @throws ServerError
     * @throws CannotCreateSessionException
     * @throws PermissionDeniedException
     */
    private void unsecure() throws ServerError, CannotCreateSessionException,
            PermissionDeniedException {
        log.info("Insecure connection requested, falling back");
        omero.client tmp = c.createClient(false);
        logout();
        c = tmp;
        serviceFactory = c.getSession();
    }

    /**
     * Returns the currently active service factory.
     *
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
        try {
            serviceFactory.keepAllAlive(new ServiceInterfacePrx[]
                    {iQuery, iAdmin, rawFileStore, rawPixelStore, thumbnailStore,
			 iRepoInfo, iContainer, iUpdate, iSettings, delegate});
            log.debug("KeepAlive ping.");

        } catch (Exception e) {
		log.debug("KeepAlive failed.");
            throw new RuntimeException(e);
        }
    }


    /**
     * Sets the active enumeration provider.
     *
     * @param enumProvider Enumeration provider to use.
     */
    public void setEnumerationProvider(EnumerationProvider enumProvider)
    {
        this.enumProvider = enumProvider;
    }

    /**
     * Retrieves the active enumeration provider.
     *
     * @return See above.
     */
    public EnumerationProvider getEnumerationProvider()
    {
        return enumProvider;
    }

    /**
     * Sets the active instance provider.
     *
     * @param enumProvider Enumeration provider to use.
     */
    public void setInstanceProvider(InstanceProvider instanceProvider)
    {
        this.instanceProvider = instanceProvider;
    }

    /**
     * Retrieves the active enumeration provider.
     *
     * @return See above.
     */
    public InstanceProvider getInstanceProvider()
    {
        return instanceProvider;
    }

    /**
     * Transforms a Java type into the corresponding OMERO RType.
     *
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
     *
     * @param value Java concrete type value.
     * @return RType or <code>null</code> if <code>value</code> is
     * <code>null</code>.
     */
    public RInt toRType(NonNegativeInteger value)
    {
        return value == null? null : rint(value.getValue());
    }

    /**
     * Transforms a Java type into the corresponding OMERO RType.
     *
     * @param value Java concrete type value.
     * @return RType or <code>null</code> if <code>value</code> is
     * <code>null</code>.
     */
    public RLong toRType(NonNegativeLong value)
    {
        return value == null? null : rlong(value.getValue());
    }

    /**
     * Transforms a Java type into the corresponding OMERO RType.
     *
     * @param value Java concrete type value.
     * @return RType or <code>null</code> if <code>value</code> is
     * <code>null</code>.
     */
    public RDouble toRType(PositiveFloat value)
    {
        return value == null? null : rdouble(value.getValue());
    }

    /**
     * Transforms a Java type into the corresponding OMERO RType.
     *
     * @param value Java concrete type value.
     * @return RType or <code>null</code> if <code>value</code> is
     * <code>null</code>.
     */
    public RDouble toRType(PercentFraction value)
    {
        return value == null? null : rdouble(value.getValue());
    }

    /**
     * Transforms a Java type into the corresponding OMERO RType.
     *
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
     * Transforms a Java type into the corresponding OMERO RType.
     * @param value Java concrete type value.
     * @return RType or <code>null</code> if <code>value</code> is
     * <code>null</code>.
     */
    public RString toRType(NamingConvention value)
    {
        return value == null ? null : rstring(value.getValue());
    }

    /**
     * Attempts to create a Java timestamp from an XML date/time string.
     * @param value An <i>xsd:dateTime</i> string.
     * @return A value Java timestamp for <code>value</code> or
     * <code>null</code> if timestamp parsing failed. The error will be logged
     * at the <code>ERROR</code> log level.
     */
    private Timestamp timestampFromXmlString(String value)
    {
        if (value == null)
        {
            return null;
        }
        try
        {
            SimpleDateFormat sdf =
                new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
            return new Timestamp(sdf.parse(value).getTime());
        }
        catch (ParseException e)
        {
            log.error(String.format(
                    "Parsing timestamp '%s' failed!", value), e);
        }
        return null;
    }

    private void closeQuietly(omero.api.StatefulServiceInterfacePrx prx)
    {
        if (prx != null) {
            try {
                prx.close();
            } catch (Ice.CommunicatorDestroyedException cde) {
                log.debug("Communicator already closed; cannot close " + prx);
            } catch (Exception e) {
                log.warn("Exception closing " + prx, e);
                log.debug(e);
            }
        }
    }

    /**
     * Closes all stateful services.
     *
     * This method should be preferred over {@link #logout()} when initialized
     * via an {@link omero.client} or a {@link ServiceFactoryPrx} instance.
     *
     * @see #initialize(client)
     * @see #initialize(ServiceFactoryPrx)
     */
    public void closeServices()
    {
        closeQuietly(rawFileStore);
        rawFileStore = null;

        closeQuietly(rawPixelStore);
        rawPixelStore = null;

        closeQuietly(thumbnailStore);
        thumbnailStore = null;

        closeQuietly(delegate);
        delegate = null;

    }

    /**
     * Destroys the sessionFactory and closes the client.
     *
     * This method should not be called when initialized via an
     * {@link omero.client} or a {@link ServiceFactoryPrx} instance. * @see
     * #initialize(client)
     *
     * @see #initialize(ServiceFactoryPrx)
     * @see #closeServices()
     */
    public void logout()
    {
        closeServices();
        if (c != null)
        {
            log.debug("closing client session.");
            c.closeSession();
            c = null;
            log.debug("client closed.");
        }
        if (executor != null)
        {
            log.debug("Logout called, shutting keep alive down.");
            executor.shutdown();
            executor = null;
            log.debug("keepalive shut down.");
        }
    }

    /**
     * Prepares the metadata store using existing metadata that has been
     * pre-registered by OMERO.fs. The expected graph should be fully loaded:
     * <ul>
     *   <li>Image</li>
     *   <li>Pixels</li>
     * </ul>
     * <b>NOTE:</b> An execution of <code>prepare()</code> is only valid for
     * a <b>SINGLE</b> <code>importImage()</code> execution. Following
     * <code>importImage()</code> the existing metadata map will be reset
     * regardless of success or failure.
     * @param existingMetadata Map of imageIndex or series vs. populated Image
     * source graph with the fetched objects defined above.
     */
    public void prepare(Map<Integer, Image> existingMetadata)
    {
        this.existingMetadata = existingMetadata;
    }

    /**
     * Actually performs the preparation logic during createRoot().
     */
    private void prepare()
    {
        // Sanity check
        if (existingMetadata == null)
        {
            return;
        }

        IObjectContainer container;
        for (Entry<Integer, Image> entry : existingMetadata.entrySet())
        {
            Image image = entry.getValue();
            Integer series = entry.getKey();
            // Reset the image acquisition date as it has been inserted
            // erroneously by the OMERO.fs infrastructure.
            image.setAcquisitionDate(null);
            Pixels pixels = image.getPrimaryPixels();
            LinkedHashMap<Index, Integer> indexes =
                new LinkedHashMap<Index, Integer>();
            indexes.put(Index.IMAGE_INDEX, series);
            container = getIObjectContainer(Image.class, indexes);
            container.sourceObject = image;
            if (log.isDebugEnabled())
            {
                log.debug(String.format("Prepared(%d) == %s,%s",
                        series, container.sourceObject, container.LSID));
            }
            container = getIObjectContainer(Pixels.class, indexes);
            container.sourceObject = pixels;
            if (log.isDebugEnabled())
            {
                log.debug(String.format("Prepared(%d) == %s,%s",
                        series, container.sourceObject, container.LSID));
            }
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
            initializeServices(false); // Reset group
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
            // Ensures that any prepared objects go into the container cache
            prepare();
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            existingMetadata = null;
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

    /**
     * Retrieves a Format enumeration for the current reader's type.
     * @return See above.
     */
    private Format getImageFormat()
    {
        ImageReader imageReader = (ImageReader) reader;
        String value = imageReader.getReader().getClass().toString();
        value = value.replace("class loci.formats.in.", "");
        value = value.replace("Reader", "");
        return (Format) getEnumeration(Format.class, value);
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
     * @see ome.formats.model.IObjectContainerStore#getUserSpecifiedPlateName()
     */
    public String getUserSpecifiedPlateName()
    {
        return userSpecifiedPlateName;
    }

    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#setUserSpecifiedPlateName(java.lang.String)
     */
    public void setUserSpecifiedPlateName(String name)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Using user specified plate name: " + name);
        }
        userSpecifiedPlateName = name;
    }

    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#getUserSpecifiedPlateDescription()
     */
    public String getUserSpecifiedPlateDescription()
    {
        return userSpecifiedPlateDescription;
    }

    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#setUserSpecifiedPlateDescription(java.lang.String)
     */
    public void setUserSpecifiedPlateDescription(String description)
    {
        if (log.isDebugEnabled())
        {
            log.debug("Using user specified plate description: " + description);
        }
        userSpecifiedPlateDescription = description;
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
        if (log.isDebugEnabled())
        {
            log.debug("Using user specified image name: " + name);
        }
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
        if (log.isDebugEnabled())
        {
            log.debug("Using user specified image description: " + description);
        }
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
	if (!targets.contains(target))
	{
	    targets.add(target);
	}
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
    private <T extends IObject> T getSourceObject(Class<T> klass, LinkedHashMap<Index, Integer> indexes)
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
    @SuppressWarnings("unchecked")
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
     * Creates a temporary file on disk containing all metadata in the
     * Bio-Formats metadata hash table for the current series.
     * @param suffix String that will be appended to the end of the temporary
     * file path.
     * @return Temporary file created.
     */
    private File createSeriesMetadataFile(String suffix)
    {
	Hashtable<?,?> globalMetadata = reader.getGlobalMetadata();
	Hashtable<?,?> seriesMetadata = reader.getSeriesMetadata();
        if (globalMetadata.size() == 0 && seriesMetadata.size() == 0)
        {
            return null;
        }
	FileOutputStream stream = null;
	OutputStreamWriter writer= null;
	try
	{
		File metadataFile = TempFileManager.createTempFile(
		        ORIGINAL_METADATA_KEY, suffix);
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
		LinkedHashMap<Index, Integer> imageIndexes =
			new LinkedHashMap<Index, Integer>();
		imageIndexes.put(Index.IMAGE_INDEX, series);
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
			LinkedHashMap<Index, Integer> indexes =
				new LinkedHashMap<Index, Integer>();
			indexes.put(Index.ORIGINAL_FILE_INDEX, originalFileIndex);
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
     * to have the scaffolding in place for later original file upload.
     * @param archive Whether or not the user requested the original files to
     * be archived.
     * @param useMetadataFile Whether or not to dump all metadata to a flat
     * file annotation on the server.
     * @return A list of the temporary metadata files created on local disk.
     */
    public List<File> setArchive(boolean archive, boolean useMetadataFile)
    {
	List<File> metadataFiles = new ArrayList<File>();
	int originalFileIndex = countCachedContainers(OriginalFile.class, null);
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
		LSID pixelsKey = new LSID(Pixels.class, series);
		LSID imageKey = new LSID(Image.class, series);
		LinkedHashMap<Index, Integer> imageIndexes =
			new LinkedHashMap<Index, Integer>();
		imageIndexes.put(Index.IMAGE_INDEX, series);

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
		    String uuid = UUID.randomUUID().toString();
			File metadataFile = createSeriesMetadataFile(uuid);
			if (metadataFile != null)
			{
				metadataFiles.add(metadataFile);
				LinkedHashMap<Index, Integer> indexes =
					new LinkedHashMap<Index, Integer>();
				indexes.put(Index.ORIGINAL_FILE_INDEX, originalFileIndex);
				String format = "text/plain";
				OriginalFile originalFile = createOriginalFileFromFile(
				        metadataFile, indexes, format);
				log.debug("originalFile created");
				originalFile.setPath(toRType(String.format("%s%s/",
				        omero.constants.annotation.file.ORIGINALMETADATAPREFIX.value,
				        uuid)));
				originalFile.setName(toRType(omero.constants.annotation.file.ORIGINALMETADATA.value));
				log.debug("originalFile path created");
                    indexes = new LinkedHashMap<Index, Integer>();
                    indexes.put(Index.IMAGE_INDEX, series);
                    indexes.put(Index.ORIGINAL_FILE_INDEX, originalFileIndex);
                    addCompanionFileAnnotationTo(imageKey, indexes,
                                                 originalFileIndex);
                    originalFileIndex++;
                }
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
			boolean isCompanionFile = companionFiles == null? false :
				                      companionFiles.contains(usedFilename);
			if (archive || isCompanionFile)
			{
				LinkedHashMap<Index, Integer> indexes =
					new LinkedHashMap<Index, Integer>();
				indexes.put(Index.ORIGINAL_FILE_INDEX, originalFileIndex);
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
                        // Add a companion file annotation to the Image.
                        indexes = new LinkedHashMap<Index, Integer>();
                        indexes.put(Index.IMAGE_INDEX, series);
                        indexes.put(Index.ORIGINAL_FILE_INDEX, usedFileIndex);
                        addCompanionFileAnnotationTo(imageKey, indexes,
                                                     usedFileIndex);
                    }
                    if (archive)
                    {
                        // Always link the original file to the Image even if
                        // it is a companion file when we are archiving.
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
     * @return The LSID of the original file.
     */
    private void addCompanionFileAnnotationTo(
		LSID target, LinkedHashMap<Index, Integer> indexes,
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
     * Looks up an original file by UUID if the requested path contains the
     * <code>ORIGINAL_METADATA_KEY</code>.
     * @param path Absolute path to locate an original file for.
     * @param originalFileMap Original file path vs. original file map.
     * @return An original file if one can be looked up by UUID and
     * <code>null</code> otherwise.
     */
    private OriginalFile byUUID(
		String path, Map<String, OriginalFile> originalFileMap)
    {
	for (Entry<String, OriginalFile> entry : originalFileMap.entrySet())
	{
		try {
			if (path.contains(ORIGINAL_METADATA_KEY))
			{
				String[] tokens = entry.getKey().split("/");
				if (tokens.length > 1 && path.endsWith(tokens[tokens.length - 2]))
				{
					return entry.getValue();
				}
			}
		} catch (ArrayIndexOutOfBoundsException e)
		{
			log.error("byUUID error, path: " + path + ", entry.key: " + entry.getKey(), e);
			throw e;
		}
	}

	return null;
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
            String path = file.getAbsolutePath();
            OriginalFile originalFile = originalFileMap.get(path);
            if (originalFile == null)
            {
                originalFile = byUUID(path, originalFileMap);
            }
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

    /**
     * Sets extended the properties on a pixel set.
     * @param pixelsId The pixels set identifier.
     * @param series The series number to populate.
     */
    public void setPixelsParams(long pixelsId, int series)
    {
        try
        {
            Map<String, String> params = new HashMap<String, String>();
            params.put("image_no", Integer.toString(series));
            delegate.setPixelsParams(pixelsId, true, params);
        }
        catch (Exception e)
        {
            log.error("Server error setting extended properties for Pixels:" +
                      pixelsId);
        }
    }

	/**
	 * Changes the default group of the currently logged in user.
	 *
	 * @param groupID The id of the group.
	 * @throws Exception If an error occurred while trying to
	 * retrieve data from OMERO service.
	 */
	public void setCurrentGroup(long groupID)
		throws ServerError
	{
	    setGroup(groupID);
	    initializeServices(false);
	}

	/**
	 * Retrieves the groups visible by the current experimenter.
	 *
	 * @return List of ExperimenterGroups the user is in
	 * @throws Exception If an error occurred while trying to
	 * retrieve data from OMERO service.
	 */
	List<ExperimenterGroup> getUserGroups()
		throws ServerError
	{
		List<ExperimenterGroup> myGroups = new ArrayList<ExperimenterGroup>();
			//Need method server side.
			ParametersI p = new ParametersI();
			p.addId(eventContext.userId);
			List<IObject> groups = iQuery.findAllByQuery(
                    "select distinct g from ExperimenterGroup as g "
                    + "join fetch g.groupExperimenterMap as map "
                    + "join fetch map.parent e "
                    + "left outer join fetch map.child u "
                    + "left outer join fetch u.groupExperimenterMap m2 "
                    + "left outer join fetch m2.parent p "
                    + "where g.id in "
                    + "  (select m.parent from GroupExperimenterMap m "
                    + "  where m.child.id = :id )", p);

			ExperimenterGroup group;
			Iterator<IObject> i = groups.iterator();
			while (i.hasNext()) {
				group = (ExperimenterGroup) i.next();
				myGroups.add(group);
			}
		return myGroups;
	}

	/**
	 * Maps the user's groups for use by ScreenLogin.registerGroup()
	 * Also strips system groups from this map
	 *
	 * @return map of group id & name
	 * @throws ServerError
	 */
	public Map<Long, String> mapUserGroups() throws ServerError
	{
		List<String> systemGroups = new ArrayList<String>();
		systemGroups.add("system");
		systemGroups.add("user");
		systemGroups.add("guest");

		Map<Long, String> names = new LinkedHashMap<Long, String>();

		List<ExperimenterGroup> groups = getUserGroups();

		if (groups == null || groups.size() == 0)
			return null;

		ExperimenterGroup currentDefaultGroup =
			iAdmin.getDefaultGroup(eventContext.userId);

		Iterator<ExperimenterGroup> i = groups.iterator();
		ExperimenterGroup group = null;

		// Add all groups excluding the default group
		while (i.hasNext()) {
			group = i.next();

			String n = group.getName() == null ? null : group.getName().getValue();

			if (!systemGroups.contains(n) && group.getId().getValue() != currentDefaultGroup.getId().getValue()) {
				names.put(group.getId().getValue(), group.getName().getValue());
			}
		}

		String dn = currentDefaultGroup.getName() == null ? null
				: currentDefaultGroup.getName().getValue();

		// Add the default group last (unless its a system group)
		if (!systemGroups.contains(dn))
			names.put(currentDefaultGroup.getId().getValue(),
					currentDefaultGroup.getName().getValue());

		if (names.size() == 0) names = null;
		return names;
	}

	/**
	 * Retrieve the default group's name
	 *
	 * @return name
	 * @throws ServerError
	 */
	public String getDefaultGroupName() throws ServerError
	{
		ExperimenterGroup currentDefaultGroup =
			iAdmin.getDefaultGroup(eventContext.userId);

		String dn = currentDefaultGroup.getName() == null ? ""
				: currentDefaultGroup.getName().getValue();

		return dn;
	}

	/**
	 * Retrieve teh default groups permission 'level'.
	 *
	 * @return ImportEvent's group level
	 * @throws ServerError
	 */
	public int getDefaultGroupLevel() throws ServerError {

		int groupLevel = 0;

		ExperimenterGroup currentDefaultGroup =
			iAdmin.getDefaultGroup(eventContext.userId);

		Permissions perm = currentDefaultGroup.getDetails().getPermissions();

		if (perm.isGroupRead()) {
			if (perm.isGroupWrite())  groupLevel = ImportEvent.GROUP_COLLAB_READ_LINK;
			else groupLevel = ImportEvent.GROUP_COLLAB_READ;
		}
		else if (perm.isWorldRead()) {
			if (perm.isWorldWrite())  groupLevel = ImportEvent.GROUP_PUBLIC;
			else groupLevel = ImportEvent.GROUP_PUBLIC;
		} else {
			groupLevel = ImportEvent.GROUP_PRIVATE;
		}

		/* TODO: add private icon
		//Check if the user is owner of the group.
		Set leaders = group.getLeaders();
		if (leaders == null || leaders.size() == 0)
			return AdminObject.PERMISSIONS_PRIVATE;
		Iterator j = leaders.iterator();
		long id = exp.getId();
		while (j.hasNext()) {
			exp = (ExperimenterData) j.next();
			if (exp.getId() == id)
				return AdminObject.PERMISSIONS_GROUP_READ;
		}
		return AdminObject.PERMISSIONS_PRIVATE;
		*/
		return groupLevel;
	}

    /**
     * @return repository space as a long
     */
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

    /**
     * Helper method to retrieve an object from iQuery
     *
     * @param <T>
     * @param klass
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T extends IObject> T getTarget(Class<T> klass, long id)
    {
        try
        {
            Map<String, String> allGroups = new HashMap<String, String>();
            allGroups.put("omero.group", "-1");
            T obj = (T) iQuery.get(klass.getName(), id, allGroups);
            if (obj == null) {
                throw new RuntimeException(String.format("Cannot find target: %s:%s",
                            klass.getName(), id));
            }
            long grpID = obj.getDetails().getGroup().getId().getValue();
            setCurrentGroup(grpID);
            return obj;
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param projectId
     * @return
     */
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

    /**
     * @param datasetName
     * @param datasetDescription
     * @param project
     * @return
     */
    public Dataset addDataset(String datasetName, String datasetDescription,
            Project project)
    {
        Dataset dataset = new DatasetI();
        if (datasetName.length() != 0)
            dataset.setName(toRType(datasetName));
        if (datasetDescription.length() != 0)
            dataset.setDescription(toRType(datasetDescription));
        if (project.getId() != null) {
		Project p = new ProjectI(project.getId().getValue(), false);
		dataset.linkProject(p);
        }

        try
        {
            return (Dataset) iUpdate.saveAndReturnObject(dataset);
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return - experimenter id
     */
    public long getExperimenterID()
    {
        return eventContext.userId;
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
		File file, LinkedHashMap<Index, Integer> indexes,
		String formatString)
    {
		OriginalFile o = (OriginalFile)
			getSourceObject(OriginalFile.class, indexes);
		o.setName(toRType(file.getName()));
		o.setSize(toRType(file.length()));
		o.setMimetype(toRType(formatString));
		o.setPath(toRType(file.getParent() + File.separator));
		o.setSha1(toRType("Pending"));
		return o;
    }

    /**
     * Retrieves a configuration value from the <code>IConfig</code> service.
     * @param key Key for the string encoded value.
     * @return String encoded configuration value.
     */
    public String getConfigValue(String key)
    {
        try
        {
            return serviceFactory.getConfigService().getConfigValue(key);
        }
        catch (omero.SecurityViolation sv)
        {
            return null;
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return
     */
    public List<Screen> getScreens()
    {
        try
        {
            List<IObject> objects =
                iContainer.loadContainerHierarchy(Screen.class.getName(), null, new ParametersI().exp(rlong(getExperimenterID())));
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


    /**
     * @return
     */
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

    /**
     * @param p
     * @return
     */
    public List<Dataset> getDatasets(Project p)
    {
	if (p.getId() == null || p.getId().getValue() == 0)
		return getDatasetsWithoutProjects();
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

    /**
     * @return
     */
    public List<Dataset> getDatasetsWithoutProjects()
    {
        try
        {
		ParametersI param = new ParametersI();
		param.exp(rlong(getExperimenterID()));
		param.orphan();
            List<IObject> objects =
                iContainer.loadContainerHierarchy(Project.class.getName(), null, param);
            List<Dataset> datasets = new ArrayList<Dataset>(0);
            for (IObject object : objects)
            {
		if (object instanceof DatasetI)
			datasets.add((Dataset) object);
            }
            return datasets;
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param projectName
     * @param projectDescription
     * @return
     */
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

    /**
     * @param screenName
     * @param screenDescription
     * @return
     */
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

    /**
     * Closes the active raw pixels store. Finalizing any open server side
     * resources.
     *
     * The call to close on the RawPixelsStorePrx may throw, in which case
     * the current import should be considered failed, since the saving of
     * the pixels server-side will have not completed successfully.
     *
     * @see ticket:5594
     */
    public void finalizePixelStore() throws ServerError
    {
        if (rawPixelStore != null)
        {
            try
            {
                rawPixelStore.close();
            } finally
            {
                rawPixelStore = null;
            }
        }
        rawPixelStore = serviceFactory.createRawPixelsStore();
    }

    /**
     * Retrieves the suggested tile size for a pixels set.
     * @param pixId Pixels set to write to.
     * @return Width and height of the tile as an array.
     * @throws ServerError If there is an error writing this tile to the
     * server.
     */
    public int[] getTileSize(Long pixId)
        throws ServerError
    {
        resetPixelsId(pixId);
        return rawPixelStore.getTileSize();
    }

    /**
     * Writes a tile of pixels to the server.
     * @param pixId Pixels set to write to.
     * @param arrayBuf Byte array containing all pixels for this plane.
     * @param z Z offset within the Pixels set.
     * @param c Channel offset within the Pixels set.
     * @param t Timepoint offset within the Pixels set.
     * @param x X offset of the tile.
     * @param y Y offset of the tile.
     * @param w Width of the tile.
     * @param h Height of the tile.
     * @throws ServerError If there is an error writing this tile to the
     * server.
     * @see #setPlane(Long, byte[], int, int, int)
     */
    public void setTile(Long pixId, byte[] arrayBuf, int z, int c, int t,
                        int x, int y, int w, int h)
        throws ServerError
    {
        resetPixelsId(pixId);
        rawPixelStore.setTile(arrayBuf, z, c, t, x, y, w, h);
    }

    /**
     * Writes a plane to the server.
     * @param pixId Pixels set to write to.
     * @param arrayBuf Byte array containing all pixels for this plane.
     * @param z Z offset within the Pixels set.
     * @param c Channel offset within the Pixels set.
     * @param t Timepoint offset within the Pixels set.
     * @throws ServerError If there is an error writing this plane to the
     * server.
     */
    public void setPlane(Long pixId, byte[] arrayBuf, int z, int c, int t)
        throws ServerError
    {
        resetPixelsId(pixId);
        rawPixelStore.setPlane(arrayBuf, z, c, t);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.IMinMaxStore#setChannelGlobalMinMax(int, double, double, int)
     */
    public void setChannelGlobalMinMax(int channel, double minimum,
            double maximum, int series)
    {
        Pixels pixels =
            (Pixels) getSourceObject(new LSID(Pixels.class, series));
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

    /**
     * @param in
     * @return
     */
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


    /*-------------------*/

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


    //////////////////////////////////////////////

    /* (non-Javadoc)
     * @see ome.formats.model.IObjectContainerStore#getIObjectContainer(java.lang.Class, java.util.LinkedHashMap)
     */
    public IObjectContainer getIObjectContainer(Class<? extends IObject> klass,
                                                LinkedHashMap<Index, Integer> indexes)
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

        Map<String, Integer> asString = new HashMap<String, Integer>();
        for (Entry<Index, Integer> v : indexes.entrySet())
        {
            asString.put(v.getKey().toString(), v.getValue());
        }

        if (!containerCache.containsKey(lsid))
        {
            IObjectContainer c = new IObjectContainer();
            c.indexes = asString;
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
    @SuppressWarnings("unchecked")
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
    @SuppressWarnings("unchecked")
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
                Class<?> containerClass = lsid.getJavaClass();
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
				Class<?> containerClass = targetLSID.getJavaClass();
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
            Class<?> sourceClass = sourceLSID.getJavaClass();
            if (sourceClass.equals(source))
            {
		for (LSID targetLSID : referenceCache.get(sourceLSID))
		{
			Class<?> targetClass = targetLSID.getJavaClass();
			if (targetClass.equals(target))
			{
				count++;
			}
		}
            }
        }
        return count;
    }




    /*
     *
     * Bio-formats method calls start here
     *
     */


    //////// Arc /////////

    /**
     * Retrieve Arc
     * @param instrumentIndex
     * @param lightSourceIndex
     * @return
     */
    private Arc getArc(int instrumentIndex, int lightSourceIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        indexes.put(Index.LIGHT_SOURCE_INDEX, lightSourceIndex);
        return getSourceObject(Arc.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setArcID(java.lang.String, int, int)
     */
    public void setArcID(String id, int instrumentIndex, int lightSourceIndex)
    {
        checkDuplicateLSID(Arc.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        indexes.put(Index.LIGHT_SOURCE_INDEX, lightSourceIndex);
        IObjectContainer o = getIObjectContainer(Arc.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Arc.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setArcLotNumber(java.lang.String, int, int)
     */
    public void setArcLotNumber(String lotNumber, int instrumentIndex,
            int lightSourceIndex)
    {
    	Arc o = getArc(instrumentIndex, lightSourceIndex);
        o.setLotNumber(toRType(lotNumber));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setArcManufacturer(java.lang.String, int, int)
     */
    public void setArcManufacturer(String manufacturer, int instrumentIndex,
            int lightSourceIndex)
    {
        Arc o = getArc(instrumentIndex, lightSourceIndex);
        o.setManufacturer(toRType(manufacturer));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setArcModel(java.lang.String, int, int)
     */
    public void setArcModel(String model, int instrumentIndex,
            int lightSourceIndex)
    {
        Arc o = getArc(instrumentIndex, lightSourceIndex);
        o.setModel(toRType(model));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setArcPower(java.lang.Double, int, int)
     */
    public void setArcPower(Double power, int instrumentIndex,
            int lightSourceIndex)
    {
        Arc o = getArc(instrumentIndex, lightSourceIndex);
        o.setPower(toRType(power));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setArcSerialNumber(java.lang.String, int, int)
     */
    public void setArcSerialNumber(String serialNumber, int instrumentIndex,
            int lightSourceIndex)
    {
        Arc o = getArc(instrumentIndex, lightSourceIndex);
        o.setSerialNumber(toRType(serialNumber));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setArcType(ome.xml.model.enums.ArcType, int, int)
     */
    public void setArcType(ome.xml.model.enums.ArcType type,
            int instrumentIndex, int lightSourceIndex)
    {
        Arc o = getArc(instrumentIndex, lightSourceIndex);
        o.setType((ArcType) getEnumeration(ArcType.class, type.toString()));
    }


    //////// BooleanAnnotation /////////

    /**
     * @param booleanAnnotationIndex
     * @return
     */
    private BooleanAnnotation getBooleanAnnotation(int booleanAnnotationIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.BOOLEAN_ANNOTATION_INDEX, booleanAnnotationIndex);
        return getSourceObject(BooleanAnnotation.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setBooleanAnnotationID(java.lang.String, int)
     */
    public void setBooleanAnnotationID(String id, int booleanAnnotationIndex)
    {
        checkDuplicateLSID(BooleanAnnotation.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.BOOLEAN_ANNOTATION_INDEX, booleanAnnotationIndex);
        IObjectContainer o = getIObjectContainer(BooleanAnnotation.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(BooleanAnnotation.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setBooleanAnnotationNamespace(java.lang.String, int)
     */
    public void setBooleanAnnotationNamespace(String namespace,
            int booleanAnnotationIndex)
    {
        BooleanAnnotation o = getBooleanAnnotation(booleanAnnotationIndex);
        o.setNs(toRType(namespace));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setBooleanAnnotationValue(java.lang.Boolean, int)
     */
    public void setBooleanAnnotationValue(Boolean value,
            int booleanAnnotationIndex)
    {
        BooleanAnnotation o = getBooleanAnnotation(booleanAnnotationIndex);
        o.setBoolValue(toRType(value));
    }

    public void setBinaryOnlyUUID(String uuid)
    {
        // XXX: Not handled by OMERO.
    }

    public void setBinaryOnlyMetadataFile(String metadataFile)
    {
        // XXX: Not handled by OMERO.
    }

    //////// Channel /////////

    public Channel getChannel(int imageIndex, int channelIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.IMAGE_INDEX, imageIndex);
        indexes.put(Index.CHANNEL_INDEX, channelIndex);
        Channel c = getSourceObject(Channel.class, indexes);
        c.setLogicalChannel(getSourceObject(LogicalChannel.class, indexes));
        return getSourceObject(Channel.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelID(java.lang.String, int, int)
     */
    public void setChannelID(String id, int imageIndex, int channelIndex)
    {
        checkDuplicateLSID(Channel.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.IMAGE_INDEX, imageIndex);
        indexes.put(Index.CHANNEL_INDEX, channelIndex);
        IObjectContainer o = getIObjectContainer(Channel.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Channel.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelAcquisitionMode(ome.xml.model.enums.AcquisitionMode, int, int)
     */
    public void setChannelAcquisitionMode(
            ome.xml.model.enums.AcquisitionMode acquisitionMode,
            int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        o.getLogicalChannel().setMode((AcquisitionMode) getEnumeration(AcquisitionMode.class, acquisitionMode.toString()));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelColor(java.lang.Integer, int, int)
     */
    public void setChannelColor(Integer color, int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        // RGBA --> ARGB
        Color c = new Color((color >>> 8) | (color << (32-8)));
        o.setRed(toRType(c.getRed()));
        o.setGreen(toRType(c.getGreen()));
        o.setBlue(toRType(c.getBlue()));
        o.setAlpha(toRType(c.getAlpha()));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelContrastMethod(ome.xml.model.enums.ContrastMethod, int, int)
     */
    public void setChannelContrastMethod(
            ome.xml.model.enums.ContrastMethod contrastMethod,
            int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        o.getLogicalChannel().setContrastMethod((ContrastMethod) getEnumeration(ContrastMethod.class, contrastMethod.toString()));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelEmissionWavelength(ome.xml.model.primitives.PositiveInteger, int, int)
     */
    public void setChannelEmissionWavelength(
            PositiveInteger emissionWavelength, int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        o.getLogicalChannel().setEmissionWave(toRType(emissionWavelength));
    }

    /** (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelExcitationWavelength(ome.xml.model.primitives.PositiveInteger, int, int)
     */
    public void setChannelExcitationWavelength(
            PositiveInteger excitationWavelength, int imageIndex,
            int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        o.getLogicalChannel().setExcitationWave(toRType(excitationWavelength));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelFilterSetsjava.lang.String, int, int)
     */
    public void setChannelFilterSetRef(String filterSet, int imageIndex,
            int channelIndex)
    {
        LSID key = new LSID(LogicalChannel.class, imageIndex, channelIndex);
        addReference(key, new LSID(filterSet));

    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelFluor(java.lang.String, int, int)
     */
    public void setChannelFluor(String fluor, int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        o.getLogicalChannel().setFluor(toRType(fluor));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelIlluminationType(ome.xml.model.enums.IlluminationType, int, int)
     */
    public void setChannelIlluminationType(IlluminationType illuminationType,
            int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        o.getLogicalChannel().setIllumination((Illumination) getEnumeration(Illumination.class, illuminationType.toString()));
    }


    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelNDFilter(java.lang.Double, int, int)
     */
    public void setChannelNDFilter(Double ndfilter, int imageIndex,
            int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        o.getLogicalChannel().setNdFilter(toRType(ndfilter));
    }


    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelName(java.lang.String, int, int)
     */
    public void setChannelName(String name, int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        o.getLogicalChannel().setName(toRType(name));
    }


    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelOTFRef(java.lang.String, int, int)
     */
    public void setChannelOTFRef(String otf, int imageIndex, int channelIndex)
    {
        LSID key = new LSID(Channel.class, imageIndex, channelIndex);
        addReference(key, new LSID(otf));
    }


    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelPinholeSize(java.lang.Double, int, int)
     */
    public void setChannelPinholeSize(Double pinholeSize, int imageIndex,
            int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        o.getLogicalChannel().setPinHoleSize(toRType(pinholeSize));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelPockelCellSetting(java.lang.Integer, int, int)
     */
    public void setChannelPockelCellSetting(Integer pockelCellSetting,
            int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        o.getLogicalChannel().setPockelCellSetting(toRType(pockelCellSetting));
    }


    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelSamplesPerPixel(java.lang.Integer, int, int)
     */
    public void setChannelSamplesPerPixel(PositiveInteger samplesPerPixel,
            int imageIndex, int channelIndex)
    {
        Channel o = getChannel(imageIndex, channelIndex);
        o.getLogicalChannel().setSamplesPerPixel(toRType(samplesPerPixel));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelAnnotationRef(java.lang.String, int, int, int)
     */
    public void setChannelAnnotationRef(String annotation, int imageIndex,
            int channelIndex, int annotationRefIndex)
    {
        LSID key = new LSID(Channel.class, imageIndex, channelIndex);
        addReference(key, new LSID(annotation));
    }

     ////////Lightsource Settings/////////

    /**
     * Logical Channel and Channel combined in the new model
     *
     * @param imageIndex
     * @param logicalChannelIndex
     * @return
     */
    private LightSettings getChannelLightSourceSettings(int imageIndex, int channelIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.IMAGE_INDEX, imageIndex);
        indexes.put(Index.CHANNEL_INDEX, channelIndex);
        return getSourceObject(LightSettings.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelLightSourceSettingsID(java.lang.String, int, int)
     */
    public void setChannelLightSourceSettingsID(String id, int imageIndex,
            int channelIndex)
    {
        getChannelLightSourceSettings(imageIndex, channelIndex);
        LSID key = new LSID(LightSettings.class, imageIndex, channelIndex);
        addReference(key, new LSID(id));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelLightSourceSettingsAttenuation(ome.xml.model.primitives.PercentFraction, int, int)
     */
    public void setChannelLightSourceSettingsAttenuation(
            PercentFraction attenuation, int imageIndex, int channelIndex)
    {
        LightSettings o = getChannelLightSourceSettings(imageIndex, channelIndex);
        o.setAttenuation(toRType(attenuation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setChannelLightSourceSettingsWavelength(ome.xml.model.primitives.PositiveInteger, int, int)
     */
    public void setChannelLightSourceSettingsWavelength(
            PositiveInteger wavelength, int imageIndex, int channelIndex)
    {
        LightSettings o = getChannelLightSourceSettings(imageIndex, channelIndex);
        o.setWavelength(toRType(wavelength));
    }

    ////////Dataset/////////

    public void setDatasetID(String id, int datasetIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDatasetAnnotationRef(java.lang.String, int, int)
     */
    public void setDatasetAnnotationRef(String annotation, int datasetIndex,
            int annotationRefIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDatasetDescription(java.lang.String, int)
     */
    public void setDatasetDescription(String description, int datasetIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDatasetExperimenterRef(java.lang.String, int)
     */
    public void setDatasetExperimenterRef(String experimenter, int datasetIndex)
    {
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDatasetGroupRef(java.lang.String, int)
     */
    public void setDatasetGroupRef(String group, int datasetIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDatasetName(java.lang.String, int)
     */
    public void setDatasetName(String name, int datasetIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDatasetProjectRef(java.lang.String, int, int)
     */
    public void setDatasetProjectRef(String project, int datasetIndex,
            int projectRefIndex)
    {
        // XXX: Not handled by OMERO.
    }

    ////////Detector/////////

    /**
     * @param instrumentIndex
     * @param detectorIndex
     * @return
     */
    public Detector getDetector(int instrumentIndex, int detectorIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        indexes.put(Index.DETECTOR_INDEX, detectorIndex);
        return getSourceObject(Detector.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorID(java.lang.String, int, int)
     */
    public void setDetectorID(String id, int instrumentIndex, int detectorIndex)
    {
        checkDuplicateLSID(Detector.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        indexes.put(Index.DETECTOR_INDEX, detectorIndex);
        IObjectContainer o = getIObjectContainer(Detector.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Detector.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorAmplificationGain(java.lang.Double, int, int)
     */
    public void setDetectorAmplificationGain(Double amplificationGain,
            int instrumentIndex, int detectorIndex)
    {
        Detector o = getDetector(instrumentIndex, detectorIndex);
        o.setAmplificationGain(toRType(amplificationGain));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorGain(java.lang.Double, int, int)
     */
    public void setDetectorGain(Double gain, int instrumentIndex,
            int detectorIndex)
    {
        Detector o = getDetector(instrumentIndex, detectorIndex);
        o.setGain(toRType(gain));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorLotNumber(java.lang.String, int, int)
     */
    public void setDetectorLotNumber(String lotNumber, int instrumentIndex,
            int detectorIndex)
    {
    	Detector o = getDetector(instrumentIndex, detectorIndex);
        o.setLotNumber(toRType(lotNumber));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorManufacturer(java.lang.String, int, int)
     */
    public void setDetectorManufacturer(String manufacturer,
            int instrumentIndex, int detectorIndex)
    {
        Detector o = getDetector(instrumentIndex, detectorIndex);
        o.setManufacturer(toRType(manufacturer));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorModel(java.lang.String, int, int)
     */
    public void setDetectorModel(String model, int instrumentIndex,
            int detectorIndex)
    {
        Detector o = getDetector(instrumentIndex, detectorIndex);
        o.setModel(toRType(model));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorOffset(java.lang.Double, int, int)
     */
    public void setDetectorOffset(Double offset, int instrumentIndex,
            int detectorIndex)
    {
        Detector o = getDetector(instrumentIndex, detectorIndex);
        o.setOffsetValue(toRType(offset));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorSerialNumber(java.lang.String, int, int)
     */
    public void setDetectorSerialNumber(String serialNumber,
            int instrumentIndex, int detectorIndex)
    {
        Detector o = getDetector(instrumentIndex, detectorIndex);
        o.setSerialNumber(toRType(serialNumber));
    }


    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorType(ome.xml.model.enums.DetectorType, int, int)
     */
    public void setDetectorType(ome.xml.model.enums.DetectorType type,
            int instrumentIndex, int detectorIndex)
    {
        Detector o = getDetector(instrumentIndex, detectorIndex);
        o.setType((DetectorType) getEnumeration(DetectorType.class, type.toString()));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorVoltage(java.lang.Double, int, int)
     */
    public void setDetectorVoltage(Double voltage, int instrumentIndex,
            int detectorIndex)
    {
        Detector o = getDetector(instrumentIndex, detectorIndex);
        o.setVoltage(toRType(voltage));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorZoom(java.lang.Double, int, int)
     */
    public void setDetectorZoom(Double zoom, int instrumentIndex,
            int detectorIndex)
    {
        Detector o = getDetector(instrumentIndex, detectorIndex);
        o.setZoom(toRType(zoom));
    }

    ////////Detector Settings/////////

    /**
     * @param instrumentIndex
     * @param detectorIndex
     * @return
     */
    private DetectorSettings getDetectorSettings(int imageIndex, int channelIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.IMAGE_INDEX, imageIndex);
        indexes.put(Index.CHANNEL_INDEX, channelIndex);
        return getSourceObject(DetectorSettings.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorSettingsID(java.lang.String, int, int)
     */
    public void setDetectorSettingsID(String id, int imageIndex,
            int channelIndex)
    {
        getDetectorSettings(imageIndex, channelIndex);
        LSID key = new LSID(DetectorSettings.class, imageIndex, channelIndex);
        addReference(key, new LSID(id));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorSettingsBinning(ome.xml.model.enums.Binning, int, int)
     */
    public void setDetectorSettingsBinning(
            ome.xml.model.enums.Binning binning, int imageIndex,
            int channelIndex)
    {
        DetectorSettings o = getDetectorSettings(imageIndex, channelIndex);
        o.setBinning((Binning) getEnumeration(Binning.class, binning.toString()));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorSettingsGain(java.lang.Double, int, int)
     */
    public void setDetectorSettingsGain(Double gain, int imageIndex,
            int channelIndex)
    {
        DetectorSettings o = getDetectorSettings(imageIndex, channelIndex);
        o.setGain(toRType(gain));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorSettingsOffset(java.lang.Double, int, int)
     */
    public void setDetectorSettingsOffset(Double offset, int imageIndex,
            int channelIndex)
    {
        DetectorSettings o = getDetectorSettings(imageIndex, channelIndex);
        o.setOffsetValue(toRType(offset));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorSettingsReadOutRate(java.lang.Double, int, int)
     */
    public void setDetectorSettingsReadOutRate(Double readOutRate,
            int imageIndex, int channelIndex)
    {
        DetectorSettings o = getDetectorSettings(imageIndex, channelIndex);
        o.setReadOutRate(toRType(readOutRate));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDetectorSettingsVoltage(java.lang.Double, int, int)
     */
    public void setDetectorSettingsVoltage(Double voltage, int imageIndex,
            int channelIndex)
    {
        DetectorSettings o = getDetectorSettings(imageIndex, channelIndex);
        o.setVoltage(toRType(voltage));
    }

    ////////Dichroic/////////

    /**
     * @param instrumentIndex
     * @param dichroicIndex
     * @return
     */
    private Dichroic getDichroic(int instrumentIndex, int dichroicIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        indexes.put(Index.DICHROIC_INDEX, dichroicIndex);
        return getSourceObject(Dichroic.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDichroicID(java.lang.String, int, int)
     */
    public void setDichroicID(String id, int instrumentIndex, int dichroicIndex)
    {
        checkDuplicateLSID(Dichroic.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        indexes.put(Index.DICHROIC_INDEX, dichroicIndex);
        IObjectContainer o = getIObjectContainer(Dichroic.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Dichroic.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDichroicLotNumber(java.lang.String, int, int)
     */
    public void setDichroicLotNumber(String lotNumber, int instrumentIndex,
            int dichroicIndex)
    {
        Dichroic o = getDichroic(instrumentIndex, dichroicIndex);
        o.setLotNumber(toRType(lotNumber));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDichroicManufacturer(java.lang.String, int, int)
     */
    public void setDichroicManufacturer(String manufacturer,
            int instrumentIndex, int dichroicIndex)
    {
        Dichroic o = getDichroic(instrumentIndex, dichroicIndex);
        o.setManufacturer(toRType(manufacturer));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDichroicModel(java.lang.String, int, int)
     */
    public void setDichroicModel(String model, int instrumentIndex,
            int dichroicIndex)
    {
        Dichroic o = getDichroic(instrumentIndex, dichroicIndex);
        o.setModel(toRType(model));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDichroicSerialNumber(java.lang.String, int, int)
     */
    public void setDichroicSerialNumber(String serialNumber,
            int instrumentIndex, int dichroicIndex)
    {
    	Dichroic o = getDichroic(instrumentIndex, dichroicIndex);
        o.setSerialNumber(toRType(serialNumber));
    }

    ////////Double Annotation/////////

    private DoubleAnnotation getDoubleAnnotation(int doubleAnnotationIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.DOUBLE_ANNOTATION_INDEX, doubleAnnotationIndex);
        return getSourceObject(DoubleAnnotation.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDoubleAnnotationID(java.lang.String, int)
     */
    public void setDoubleAnnotationID(String id, int doubleAnnotationIndex)
    {
        checkDuplicateLSID(DoubleAnnotation.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.DOUBLE_ANNOTATION_INDEX, doubleAnnotationIndex);
        IObjectContainer o = getIObjectContainer(DoubleAnnotation.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(DoubleAnnotation.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDoubleAnnotationNamespace(java.lang.String, int)
     */
    public void setDoubleAnnotationNamespace(String namespace,
            int doubleAnnotationIndex)
    {
        DoubleAnnotation o = getDoubleAnnotation(doubleAnnotationIndex);
        o.setNs(toRType(namespace));
    }


    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDoubleAnnotationValue(java.lang.Double, int)
     */
    public void setDoubleAnnotationValue(Double value, int doubleAnnotationIndex)
    {
        DoubleAnnotation o = getDoubleAnnotation(doubleAnnotationIndex);
        o.setDoubleValue(toRType(value));
    }

    ////////Eclipse/////////

    /**
     * @param ROIIndex
     * @param shapeIndex
     * @return
     */
    private Ellipse getEllipse(int ROIIndex, int shapeIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.ROI_INDEX, ROIIndex);
        indexes.put(Index.SHAPE_INDEX, shapeIndex);
        return getSourceObject(Ellipse.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseID(java.lang.String, int, int)
     */
    public void setEllipseID(String id, int ROIIndex, int shapeIndex)
    {
        checkDuplicateLSID(Ellipse.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.ROI_INDEX, ROIIndex);
        indexes.put(Index.SHAPE_INDEX, shapeIndex);
        IObjectContainer o = getIObjectContainer(Ellipse.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Ellipse.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseDescription(java.lang.String, int, int)
     */
    public void setEllipseDescription(String description, int ROIIndex,
            int shapeIndex)
    {
        //TODO: not in the OMERO model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseFill(java.lang.Integer, int, int)
     */
    public void setEllipseFill(Integer fill, int ROIIndex, int shapeIndex)
    {
        //TODO: Which is this in the OMERO model?
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseFontSize(java.lang.Integer, int, int)
     */
    public void setEllipseFontSize(NonNegativeInteger fontSize, int ROIIndex,
            int shapeIndex)
    {
        Ellipse o = getEllipse(ROIIndex, shapeIndex);
        o.setFontSize(toRType(fontSize));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseLabel(java.lang.String, int, int)
     */
    public void setEllipseLabel(String label, int ROIIndex, int shapeIndex)
    {
        //TODO: not in the OMERO model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseName(java.lang.String, int, int)
     */
    public void setEllipseName(String name, int ROIIndex, int shapeIndex)
    {
        //TODO: not in the OMERO model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseRadiusX(java.lang.Double, int, int)
     */
    public void setEllipseRadiusX(Double radiusX, int ROIIndex, int shapeIndex)
    {
        Ellipse o = getEllipse(ROIIndex, shapeIndex);
        o.setRx(toRType(radiusX));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseRadiusY(java.lang.Double, int, int)
     */
    public void setEllipseRadiusY(Double radiusY, int ROIIndex, int shapeIndex)
    {
        Ellipse o = getEllipse(ROIIndex, shapeIndex);
        o.setRy(toRType(radiusY));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseStroke(java.lang.Integer, int, int)
     */
    public void setEllipseStroke(Integer stroke, int ROIIndex, int shapeIndex)
    {
        //TODO: Which in OMERO model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseStrokeDashArray(java.lang.String, int, int)
     */
    public void setEllipseStrokeDashArray(String strokeDashArray, int ROIIndex,
            int shapeIndex)
    {
        Ellipse o = getEllipse(ROIIndex, shapeIndex);
        o.setStrokeDashArray(toRType(strokeDashArray));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseStrokeWidth(java.lang.Double, int, int)
     */
    public void setEllipseStrokeWidth(Double strokeWidth, int ROIIndex,
            int shapeIndex)
    {
        Ellipse o = getEllipse(ROIIndex, shapeIndex);
        o.setStrokeWidth(toRType(strokeWidth.intValue()));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseTheC(java.lang.Integer, int, int)
     */
    public void setEllipseTheC(NonNegativeInteger theC, int ROIIndex, int shapeIndex)
    {
        Ellipse o = getEllipse(ROIIndex, shapeIndex);
        o.setTheC(toRType(theC));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseTheT(java.lang.Integer, int, int)
     */
    public void setEllipseTheT(NonNegativeInteger theT, int ROIIndex, int shapeIndex)
    {
        Ellipse o = getEllipse(ROIIndex, shapeIndex);
        o.setTheT(toRType(theT));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseTheZ(java.lang.Integer, int, int)
     */
    public void setEllipseTheZ(NonNegativeInteger theZ, int ROIIndex, int shapeIndex)
    {
        Ellipse o = getEllipse(ROIIndex, shapeIndex);
        o.setTheZ(toRType(theZ));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseTransform(java.lang.String, int, int)
     */
    public void setEllipseTransform(String transform, int ROIIndex,
            int shapeIndex)
    {
        Ellipse o = getEllipse(ROIIndex, shapeIndex);
        o.setTransform(toRType(transform));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseX(java.lang.Double, int, int)
     */
    public void setEllipseX(Double x, int ROIIndex, int shapeIndex)
    {
        Ellipse o = getEllipse(ROIIndex, shapeIndex);
        o.setCx(toRType(x));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setEllipseY(java.lang.Double, int, int)
     */
    public void setEllipseY(Double y, int ROIIndex, int shapeIndex)
    {
        Ellipse o = getEllipse(ROIIndex, shapeIndex);
        o.setCy(toRType(y));
    }

    ////////Experiment/////////

    private Experiment getExperiment(int experimentIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.EXPERIMENT_INDEX, experimentIndex);
        return getSourceObject(Experiment.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimentID(java.lang.String, int)
     */
    public void setExperimentID(String id, int experimentIndex)
    {
        checkDuplicateLSID(Experiment.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.EXPERIMENT_INDEX, experimentIndex);
        IObjectContainer o = getIObjectContainer(Experiment.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Experiment.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimentDescription(java.lang.String, int)
     */
    public void setExperimentDescription(String description, int experimentIndex)
    {
        Experiment o = getExperiment(experimentIndex);
        o.setDescription(toRType(description));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimentExperimenterRef(java.lang.String, int)
     */
    public void setExperimentExperimenterRef(String experimenter,
            int experimentIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimentType(ome.xml.model.enums.ExperimentType, int)
     */
    public void setExperimentType(ome.xml.model.enums.ExperimentType type,
            int experimentIndex)
    {
        Experiment o = getExperiment(experimentIndex);
        o.setType((ExperimentType) getEnumeration(ExperimentType.class, type.toString()));
    }

    ////////Experimenter/////////

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterID(java.lang.String, int)
     */
    public void setExperimenterID(String id, int experimenterIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterAnnotationRef(java.lang.String, int, int)
     */
    public void setExperimenterAnnotationRef(String annotation,
            int experimenterIndex, int annotationRefIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterDisplayName(java.lang.String, int)
     */
    public void setExperimenterDisplayName(String displayName,
            int experimenterIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterEmail(java.lang.String, int)
     */
    public void setExperimenterEmail(String email, int experimenterIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterFirstName(java.lang.String, int)
     */
    public void setExperimenterFirstName(String firstName, int experimenterIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterGroupRef(java.lang.String, int, int)
     */
    public void setExperimenterGroupRef(String group, int experimenterIndex,
            int groupRefIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterInstitution(java.lang.String, int)
     */
    public void setExperimenterInstitution(String institution,
            int experimenterIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterLastName(java.lang.String, int)
     */
    public void setExperimenterLastName(String lastName, int experimenterIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterMiddleName(java.lang.String, int)
     */
    public void setExperimenterMiddleName(String middleName,
            int experimenterIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setExperimenterUserName(java.lang.String, int)
     */
    public void setExperimenterUserName(String userName, int experimenterIndex)
    {
        // XXX: Not handled by OMERO.
    }

    ////////Filament/////////

    public Filament getFilament(int instrumentIndex, int lightSourceIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        indexes.put(Index.LIGHT_SOURCE_INDEX, lightSourceIndex);
        return getSourceObject(Filament.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilamentID(java.lang.String, int, int)
     */
    public void setFilamentID(String id, int instrumentIndex,
            int lightSourceIndex)
    {
        checkDuplicateLSID(Filament.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        indexes.put(Index.LIGHT_SOURCE_INDEX, lightSourceIndex);
        IObjectContainer o = getIObjectContainer(Filament.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Filament.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilamentLotNumber(java.lang.String, int, int)
     */
    public void setFilamentLotNumber(String lotNumber, int instrumentIndex,
            int lightSourceIndex)
    {
    	Filament o = getFilament(instrumentIndex, lightSourceIndex);
        o.setLotNumber(toRType(lotNumber));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilamentManufacturer(java.lang.String, int, int)
     */
    public void setFilamentManufacturer(String manufacturer,
            int instrumentIndex, int lightSourceIndex)
    {
        Filament o = getFilament(instrumentIndex, lightSourceIndex);
        o.setManufacturer(toRType(manufacturer));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilamentModel(java.lang.String, int, int)
     */
    public void setFilamentModel(String model, int instrumentIndex,
            int lightSourceIndex)
    {
        Filament o = getFilament(instrumentIndex, lightSourceIndex);
        o.setModel(toRType(model));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilamentPower(java.lang.Double, int, int)
     */
    public void setFilamentPower(Double power, int instrumentIndex,
            int lightSourceIndex)
    {
        Filament o = getFilament(instrumentIndex, lightSourceIndex);
        o.setPower(toRType(power));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilamentSerialNumber(java.lang.String, int, int)
     */
    public void setFilamentSerialNumber(String serialNumber,
            int instrumentIndex, int lightSourceIndex)
    {
        Filament o = getFilament(instrumentIndex, lightSourceIndex);
        o.setSerialNumber(toRType(serialNumber));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilamentType(ome.xml.model.enums.FilamentType, int, int)
     */
    public void setFilamentType(ome.xml.model.enums.FilamentType type,
            int instrumentIndex, int lightSourceIndex)
    {
        Filament o = getFilament(instrumentIndex, lightSourceIndex);
        o.setType((FilamentType) getEnumeration(FilamentType.class, type.toString()));
    }

    ////////FileAnnotation/////////

    private FileAnnotation getFileAnnotation(int fileAnnotationIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.FILE_ANNOTATION_INDEX, fileAnnotationIndex);
        return getSourceObject(FileAnnotation.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFileAnnotationID(java.lang.String, int)
     */
    public void setFileAnnotationID(String id, int fileAnnotationIndex)
    {
        checkDuplicateLSID(FileAnnotation.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.FILE_ANNOTATION_INDEX, fileAnnotationIndex);
        IObjectContainer o = getIObjectContainer(FileAnnotation.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(FileAnnotation.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFileAnnotationNamespace(java.lang.String, int)
     */
    public void setFileAnnotationNamespace(String namespace,
            int fileAnnotationIndex)
    {
        FileAnnotation o = getFileAnnotation(fileAnnotationIndex);
        o.setNs(toRType(namespace));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFileAnnotationBinaryFileFileName(java.lang.String, int)
     */
    public void setFileAnnotationBinaryFileFileName(String fileName,
            int fileAnnotationIndex)
    {
        //TODO: not in OMERO model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFileAnnotationBinaryFileMIMEType(java.lang.String, int)
     */
    public void setFileAnnotationBinaryFileMIMEType(String mimetype,
            int fileAnnotationIndex)
    {
        //TODO: not in OMERO model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFileAnnotationBinaryFileSize(java.lang.Integer, int)
     */
    public void setFileAnnotationBinaryFileSize(NonNegativeLong size,
            int fileAnnotationIndex)
    {
        //TODO: not in OMERO model
    }


    ////////Filter/////////

    private Filter getFilter(int instrumentIndex, int filterIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        indexes.put(Index.FILTER_INDEX, filterIndex);
        return getSourceObject(Filter.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterID(java.lang.String, int, int)
     */
    public void setFilterID(String id, int instrumentIndex, int filterIndex)
    {
        checkDuplicateLSID(Filter.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        indexes.put(Index.FILTER_INDEX, filterIndex);
        IObjectContainer o = getIObjectContainer(Filter.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Filter.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterFilterWheel(java.lang.String, int, int)
     */
    public void setFilterFilterWheel(String filterWheel, int instrumentIndex,
            int filterIndex)
    {
        Filter o = getFilter(instrumentIndex, filterIndex);
        o.setFilterWheel(toRType(filterWheel));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterLotNumber(java.lang.String, int, int)
     */
    public void setFilterLotNumber(String lotNumber, int instrumentIndex,
            int filterIndex)
    {
        Filter o = getFilter(instrumentIndex, filterIndex);
        o.setLotNumber(toRType(lotNumber));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterManufacturer(java.lang.String, int, int)
     */
    public void setFilterManufacturer(String manufacturer, int instrumentIndex,
            int filterIndex)
    {
        Filter o = getFilter(instrumentIndex, filterIndex);
        o.setManufacturer(toRType(manufacturer));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterModel(java.lang.String, int, int)
     */
    public void setFilterModel(String model, int instrumentIndex,
            int filterIndex)
    {
        Filter o = getFilter(instrumentIndex, filterIndex);
        o.setModel(toRType(model));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSerialNumber(java.lang.String, int, int)
     */
    public void setFilterSerialNumber(String serialNumber, int instrumentIndex,
            int filterIndex)
    {
    	Filter o = getFilter(instrumentIndex, filterIndex);
        o.setSerialNumber(toRType(serialNumber));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterType(ome.xml.model.enums.FilterType, int, int)
     */
    public void setFilterType(ome.xml.model.enums.FilterType type,
            int instrumentIndex, int filterIndex)
    {
        Filter o = getFilter(instrumentIndex, filterIndex);
        o.setType((FilterType) getEnumeration(FilterType.class, type.toString()));
    }

    ////////Filter Set/////////

    public FilterSet getFilterSet(int instrumentIndex, int filterSetIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        indexes.put(Index.FILTER_SET_INDEX, filterSetIndex);
        return getSourceObject(FilterSet.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetID(java.lang.String, int, int)
     */
    public void setFilterSetID(String id, int instrumentIndex,
            int filterSetIndex)
    {
        checkDuplicateLSID(FilterSet.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        indexes.put(Index.FILTER_SET_INDEX, filterSetIndex);
        IObjectContainer o = getIObjectContainer(FilterSet.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(FilterSet.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetDichroicRef(java.lang.String, int, int)
     */
    public void setFilterSetDichroicRef(String dichroic, int instrumentIndex,
            int filterSetIndex)
    {
        LSID key = new LSID(FilterSet.class, instrumentIndex, filterSetIndex);
        addReference(key, new LSID(dichroic));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetEmissionFilterRef(java.lang.String, int, int, int)
     */
    public void setFilterSetEmissionFilterRef(String emissionFilter,
            int instrumentIndex, int filterSetIndex, int emissionFilterRefIndex)
    {
        // XXX: Using this suffix is kind of a gross hack but the reference
        // processing logic does not easily handle multiple A --> B or B --> A
        // linkages of the same type so we'll compromise.
        // Thu Jul 16 13:34:37 BST 2009 -- Chris Allan <callan@blackcat.ca>
        emissionFilter += OMERO_EMISSION_FILTER_SUFFIX;
        LSID key = new LSID(FilterSet.class, instrumentIndex, filterSetIndex);
        addReference(key, new LSID(emissionFilter));
        // TODO EmissionFilter.class not in OMERO model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetExcitationFilterRef(java.lang.String, int, int, int)
     */
    public void setFilterSetExcitationFilterRef(String excitationFilter,
            int instrumentIndex, int filterSetIndex,
            int excitationFilterRefIndex)
    {
        // XXX: Using this suffix is kind of a gross hack but the reference
        // processing logic does not easily handle multiple A --> B or B --> A
        // linkages of the same type so we'll compromise.
        // Thu Jul 16 13:34:37 BST 2009 -- Chris Allan <callan@blackcat.ca>
        excitationFilter += OMERO_EXCITATION_FILTER_SUFFIX;
        LSID key = new LSID(FilterSet.class, instrumentIndex, filterSetIndex);
        addReference(key, new LSID(excitationFilter));
        // TODO ExcitationFilter.class not in OMERO model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetLotNumber(java.lang.String, int, int)
     */
    public void setFilterSetLotNumber(String lotNumber, int instrumentIndex,
            int filterSetIndex)
    {
        FilterSet o = getFilterSet(instrumentIndex, filterSetIndex);
        o.setLotNumber(toRType(lotNumber));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetManufacturer(java.lang.String, int, int)
     */
    public void setFilterSetManufacturer(String manufacturer,
            int instrumentIndex, int filterSetIndex)
    {
        FilterSet o = getFilterSet(instrumentIndex, filterSetIndex);
        o.setManufacturer(toRType(manufacturer));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetModel(java.lang.String, int, int)
     */
    public void setFilterSetModel(String model, int instrumentIndex,
            int filterSetIndex)
    {
        FilterSet o = getFilterSet(instrumentIndex, filterSetIndex);
        o.setModel(toRType(model));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFilterSetSerialNumber(java.lang.String, int, int)
     */
    public void setFilterSetSerialNumber(String serialNumber,
            int instrumentIndex, int filterSetIndex)
    {
    	 FilterSet o = getFilterSet(instrumentIndex, filterSetIndex);
         o.setSerialNumber(toRType(serialNumber));
    }

    ////////Group/////////

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setGroupID(java.lang.String, int)
     */
    public void setGroupID(String id, int groupIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setGroupContact(java.lang.String, int)
     */
    public void setGroupContact(String contact, int groupIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setGroupDescription(java.lang.String, int)
     */
    public void setGroupDescription(String description, int groupIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setGroupLeader(java.lang.String, int)
     */
    public void setGroupLeader(String leader, int groupIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setGroupName(java.lang.String, int)
     */
    public void setGroupName(String name, int groupIndex)
    {
        // XXX: Not handled by OMERO.
    }

    //////// Image /////////

    /**
     * Retrieve Image
     * @param imageIndex
     * @return
     */
    private Image getImage(int imageIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.IMAGE_INDEX, imageIndex);
        Image o = getSourceObject(Image.class, indexes);
        o.setFormat(getImageFormat());
        return o;
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageID(java.lang.String, int)
     */
    public void setImageID(String id, int imageIndex)
    {
        checkDuplicateLSID(Image.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.IMAGE_INDEX, imageIndex);
        IObjectContainer o = getIObjectContainer(Image.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Image.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageAcquiredDate(java.lang.String, int)
     */
    public void setImageAcquiredDate(String acquiredDate, int imageIndex)
    {
        if (acquiredDate == null)
        {
            return;
        }
        Image o = getImage(imageIndex);
        o.setAcquisitionDate(toRType(timestampFromXmlString(acquiredDate)));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageAnnotationRef(java.lang.String, int, int)
     */
    public void setImageAnnotationRef(String annotation, int imageIndex,
            int annotationRefIndex)
    {
        LSID key = new LSID(Image.class, imageIndex);
        addReference(key, new LSID(annotation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageDatasetRef(java.lang.String, int, int)
     */
    public void setImageDatasetRef(String dataset, int imageIndex,
            int datasetRefIndex)
    {
        //LSID key = new LSID(Image.class, imageIndex);
        //addReference(key, new LSID(dataset));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageDescription(java.lang.String, int)
     */
    public void setImageDescription(String description, int imageIndex)
    {
        Image o = getImage(imageIndex);
        o.setDescription(toRType(description));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#/erimentRef(java.lang.String, int)
     */
    public void setImageExperimentRef(String experiment, int imageIndex)
    {
        LSID key = new LSID(Image.class, imageIndex);
        addReference(key, new LSID(experiment));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageExperimenterRef(java.lang.String, int)
     */
    public void setImageExperimenterRef(String experimenter, int imageIndex)
    {
        //LSID key = new LSID(Image.class, imageIndex);
        //addReference(key, new LSID(experimenter));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageGroupRef(java.lang.String, int)
     */
    public void setImageGroupRef(String group, int imageIndex)
    {
        //LSID key = new LSID(Image.class, imageIndex);
        //addReference(key, new LSID(group));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageInstrumentRef(java.lang.String, int)
     */
    public void setImageInstrumentRef(String instrument, int imageIndex)
    {
        LSID key = new LSID(Image.class, imageIndex);
        addReference(key, new LSID(instrument));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageMicrobeamManipulationRef(java.lang.String, int, int)
     */
    public void setImageMicrobeamManipulationRef(String microbeamManipulation,
            int imageIndex, int microbeamManipulationRefIndex)
    {
        LSID key = new LSID(Image.class, imageIndex);
        addReference(key, new LSID(microbeamManipulation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageName(java.lang.String, int)
     */
    public void setImageName(String name, int imageIndex)
    {
        Image o = getImage(imageIndex);
        o.setName(toRType(name));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageROIRef(java.lang.String, int, int)
     */
    public void setImageROIRef(String roi, int imageIndex, int ROIRefIndex)
    {
        LSID key = new LSID(Image.class, imageIndex);
        addReference(key, new LSID(roi));
    }

    //////// Image Objective Settings /////////

    public ObjectiveSettings getImageObjectiveSettings(int imageIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.IMAGE_INDEX, imageIndex);
        return getSourceObject(ObjectiveSettings.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageObjectiveSettingsID(java.lang.String, int)
     */
    public void setImageObjectiveSettingsID(String id, int imageIndex)
    {
        getImageObjectiveSettings(imageIndex);
        LSID key = new LSID(ObjectiveSettings.class, imageIndex);
        addReference(key, new LSID(id));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageObjectiveSettingsCorrectionCollar(java.lang.Double, int)
     */
    public void setImageObjectiveSettingsCorrectionCollar(
            Double correctionCollar, int imageIndex)
    {
        ObjectiveSettings o = getImageObjectiveSettings(imageIndex);
        o.setCorrectionCollar(toRType(correctionCollar));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageObjectiveSettingsMedium(ome.xml.model.enums.Medium, int)
     */
    public void setImageObjectiveSettingsMedium(
            ome.xml.model.enums.Medium medium, int imageIndex)
    {
        ObjectiveSettings o = getImageObjectiveSettings(imageIndex);
        o.setMedium((Medium) getEnumeration(Medium.class, medium.toString()));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImageObjectiveSettingsRefractiveIndex(java.lang.Double, int)
     */
    public void setImageObjectiveSettingsRefractiveIndex(
            Double refractiveIndex, int imageIndex)
    {
        ObjectiveSettings o = getImageObjectiveSettings(imageIndex);
        o.setRefractiveIndex(toRType(refractiveIndex));
    }

    //////// Imaging Environment /////////

    public ImagingEnvironment getImagingEnvironment(int imageIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.IMAGE_INDEX, imageIndex);
        return getSourceObject(ImagingEnvironment.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImagingEnvironmentAirPressure(java.lang.Double, int)
     */
    public void setImagingEnvironmentAirPressure(Double airPressure,
            int imageIndex)
    {
        ImagingEnvironment o = getImagingEnvironment(imageIndex);
        o.setAirPressure(toRType(airPressure));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImagingEnvironmentCO2Percent(ome.xml.model.primitives.PercentFraction, int)
     */
    public void setImagingEnvironmentCO2Percent(PercentFraction co2percent,
            int imageIndex)
    {
        ImagingEnvironment o = getImagingEnvironment(imageIndex);
        o.setCo2percent(toRType(co2percent));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImagingEnvironmentHumidity(ome.xml.model.primitives.PercentFraction, int)
     */
    public void setImagingEnvironmentHumidity(PercentFraction humidity,
            int imageIndex)
    {
        ImagingEnvironment o = getImagingEnvironment(imageIndex);
        o.setHumidity(toRType(humidity));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setImagingEnvironmentTemperature(java.lang.Double, int)
     */
    public void setImagingEnvironmentTemperature(Double temperature,
            int imageIndex)
    {
        ImagingEnvironment o = getImagingEnvironment(imageIndex);
        o.setTemperature(toRType(temperature));
    }

    //////// Instrument /////////

    private Instrument getInstrument(int instrumentIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        return getSourceObject(Instrument.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setInstrumentID(java.lang.String, int)
     */
    public void setInstrumentID(String id, int instrumentIndex)
    {
        checkDuplicateLSID(Instrument.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        IObjectContainer o = getIObjectContainer(Instrument.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Instrument.class, id, o);
    }

    //////// Laser /////////

    public Laser getLaser(int instrumentIndex, int lightSourceIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        indexes.put(Index.LIGHT_SOURCE_INDEX, lightSourceIndex);
        return getSourceObject(Laser.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserID(java.lang.String, int, int)
     */
    public void setLaserID(String id, int instrumentIndex, int lightSourceIndex)
    {
        checkDuplicateLSID(Laser.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        indexes.put(Index.LIGHT_SOURCE_INDEX, lightSourceIndex);
        IObjectContainer o = getIObjectContainer(Laser.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Laser.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserFrequencyMultiplication(ome.xml.model.primitives.PositiveInteger, int, int)
     */
    public void setLaserFrequencyMultiplication(
            PositiveInteger frequencyMultiplication, int instrumentIndex,
            int lightSourceIndex)
    {
        Laser o = getLaser(instrumentIndex, lightSourceIndex);
        o.setFrequencyMultiplication(toRType(frequencyMultiplication));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserLaserMedium(ome.xml.model.enums.LaserMedium, int, int)
     */
    public void setLaserLaserMedium(
            ome.xml.model.enums.LaserMedium laserMedium, int instrumentIndex,
            int lightSourceIndex)
    {
        Laser o = getLaser(instrumentIndex, lightSourceIndex);
        o.setLaserMedium((LaserMedium) getEnumeration(LaserMedium.class, laserMedium.toString()));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserLotNumber(java.lang.String, int, int)
     */
    public void setLaserLotNumber(String lotNumber, int instrumentIndex,
            int lightSourceIndex)
    {
    	Laser o = getLaser(instrumentIndex, lightSourceIndex);
        o.setLotNumber(toRType(lotNumber));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserManufacturer(java.lang.String, int, int)
     */
    public void setLaserManufacturer(String manufacturer, int instrumentIndex,
            int lightSourceIndex)
    {
        Laser o = getLaser(instrumentIndex, lightSourceIndex);
        o.setManufacturer(toRType(manufacturer));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserModel(java.lang.String, int, int)
     */
    public void setLaserModel(String model, int instrumentIndex,
            int lightSourceIndex)
    {
        Laser o = getLaser(instrumentIndex, lightSourceIndex);
        o.setModel(toRType(model));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserPockelCell(java.lang.Boolean, int, int)
     */
    public void setLaserPockelCell(Boolean pockelCell, int instrumentIndex,
            int lightSourceIndex)
    {
        Laser o = getLaser(instrumentIndex, lightSourceIndex);
        o.setPockelCell(toRType(pockelCell));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserPower(java.lang.Double, int, int)
     */
    public void setLaserPower(Double power, int instrumentIndex,
            int lightSourceIndex)
    {
        Laser o = getLaser(instrumentIndex, lightSourceIndex);
        o.setPower(toRType(power));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserPulse(ome.xml.model.enums.Pulse, int, int)
     */
    public void setLaserPulse(ome.xml.model.enums.Pulse pulse,
            int instrumentIndex, int lightSourceIndex)
    {
        Laser o = getLaser(instrumentIndex, lightSourceIndex);
        o.setPulse((Pulse) getEnumeration(Pulse.class, pulse.toString()));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserPump(java.lang.String, int, int)
     */
    public void setLaserPump(String pump, int instrumentIndex,
            int lightSourceIndex)
    {
        LSID key = new LSID(Laser.class, instrumentIndex, lightSourceIndex);
        addReference(key, new LSID(pump));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserRepetitionRate(java.lang.Double, int, int)
     */
    public void setLaserRepetitionRate(Double repetitionRate,
            int instrumentIndex, int lightSourceIndex)
    {
        Laser o = getLaser(instrumentIndex, lightSourceIndex);
        o.setRepetitionRate(toRType(repetitionRate));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserSerialNumber(java.lang.String, int, int)
     */
    public void setLaserSerialNumber(String serialNumber, int instrumentIndex,
            int lightSourceIndex)
    {
        Laser o = getLaser(instrumentIndex, lightSourceIndex);
        o.setSerialNumber(toRType(serialNumber));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserTuneable(java.lang.Boolean, int, int)
     */
    public void setLaserTuneable(Boolean tuneable, int instrumentIndex,
            int lightSourceIndex)
    {
        Laser o = getLaser(instrumentIndex, lightSourceIndex);
        o.setTuneable(toRType(tuneable));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserType(ome.xml.model.enums.LaserType, int, int)
     */
    public void setLaserType(ome.xml.model.enums.LaserType type,
            int instrumentIndex, int lightSourceIndex)
    {
        Laser o = getLaser(instrumentIndex, lightSourceIndex);
        o.setType((LaserType) getEnumeration(LaserType.class, type.toString()));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLaserWavelength(ome.xml.model.primitives.PositiveInteger, int, int)
     */
    public void setLaserWavelength(PositiveInteger wavelength,
            int instrumentIndex, int lightSourceIndex)
    {
        Laser o = getLaser(instrumentIndex, lightSourceIndex);
        o.setWavelength(toRType(wavelength));
    }

    //////// Laser Emitting Diode /////////

    public LightEmittingDiode getLightEmittingDiode(int instrumentIndex, int lightSourceIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        indexes.put(Index.LIGHT_SOURCE_INDEX, lightSourceIndex);
        return getSourceObject(LightEmittingDiode.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightEmittingDiodeID(java.lang.String, int, int)
     */
    public void setLightEmittingDiodeID(String id, int instrumentIndex,
            int lightSourceIndex)
    {
        checkDuplicateLSID(LightEmittingDiode.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        indexes.put(Index.LIGHT_SOURCE_INDEX, lightSourceIndex);
        IObjectContainer o = getIObjectContainer(LightEmittingDiode.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(LightEmittingDiode.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightEmittingDiodeLotNumber(java.lang.String, int, int)
     */
    public void setLightEmittingDiodeLotNumber(String lotNumber,
            int instrumentIndex, int lightSourceIndex)
    {
        LightEmittingDiode o = getLightEmittingDiode(instrumentIndex, 
        		lightSourceIndex);
        o.setLotNumber(toRType(lotNumber));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightEmittingDiodeManufacturer(java.lang.String, int, int)
     */
    public void setLightEmittingDiodeManufacturer(String manufacturer,
            int instrumentIndex, int lightSourceIndex)
    {
        LightEmittingDiode o = getLightEmittingDiode(instrumentIndex, lightSourceIndex);
        o.setManufacturer(toRType(manufacturer));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightEmittingDiodeModel(java.lang.String, int, int)
     */
    public void setLightEmittingDiodeModel(String model, int instrumentIndex,
            int lightSourceIndex)
    {
        LightEmittingDiode o = getLightEmittingDiode(instrumentIndex, lightSourceIndex);
        o.setModel(toRType(model));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightEmittingDiodePower(java.lang.Double, int, int)
     */
    public void setLightEmittingDiodePower(Double power, int instrumentIndex,
            int lightSourceIndex)
    {
        LightEmittingDiode o = getLightEmittingDiode(instrumentIndex, lightSourceIndex);
        o.setPower(toRType(power));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightEmittingDiodeSerialNumber(java.lang.String, int, int)
     */
    public void setLightEmittingDiodeSerialNumber(String serialNumber,
            int instrumentIndex, int lightSourceIndex)
    {
        LightEmittingDiode o = getLightEmittingDiode(instrumentIndex, lightSourceIndex);
        o.setSerialNumber(toRType(serialNumber));
    }


    //////// Light Path /////////

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightPathDichroicRef(java.lang.String, int, int)
     */
    public void setLightPathDichroicRef(String dichroic, int imageIndex,
            int channelIndex)
    {
        LSID key = new LSID(LightPath.class, imageIndex, channelIndex);
        addReference(key, new LSID(dichroic));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightPathEmissionFilterRef(java.lang.String, int, int, int)
     */
    public void setLightPathEmissionFilterRef(String emissionFilter,
            int imageIndex, int channelIndex, int emissionFilterRefIndex)
    {
        // XXX: Using this suffix is kind of a gross hack but the reference
        // processing logic does not easily handle multiple A --> B or B --> A
        // linkages of the same type so we'll compromise.
        // Tue 18 May 2010 17:07:51 BST -- Chris Allan <callan@blackcat.ca>
        emissionFilter += OMERO_EMISSION_FILTER_SUFFIX;
        LSID key = new LSID(LightPath.class, imageIndex, channelIndex);
        addReference(key, new LSID(emissionFilter));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLightPathExcitationFilterRef(java.lang.String, int, int, int)
     */
    public void setLightPathExcitationFilterRef(String excitationFilter,
            int imageIndex, int channelIndex, int excitationFilterRefIndex)
    {
        // XXX: Using this suffix is kind of a gross hack but the reference
        // processing logic does not easily handle multiple A --> B or B --> A
        // linkages of the same type so we'll compromise.
        // Tue 18 May 2010 17:07:51 BST -- Chris Allan <callan@blackcat.ca>
        excitationFilter += OMERO_EXCITATION_FILTER_SUFFIX;
        LSID key = new LSID(LightPath.class, imageIndex, channelIndex);
        addReference(key, new LSID(excitationFilter));
    }


    //////// Line /////////

    public Line getLine(int ROIIndex, int shapeIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.ROI_INDEX,ROIIndex);
        indexes.put(Index.SHAPE_INDEX, shapeIndex);
        return getSourceObject(Line.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineID(java.lang.String, int, int)
     */
    public void setLineID(String id, int ROIIndex, int shapeIndex)
    {
        checkDuplicateLSID(Line.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.ROI_INDEX,ROIIndex);
        indexes.put(Index.SHAPE_INDEX, shapeIndex);
        IObjectContainer o = getIObjectContainer(Line.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Line.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineDescription(java.lang.String, int, int)
     */
    public void setLineDescription(String description, int ROIIndex,
            int shapeIndex)
    {
        //Line o = getLine(ROIIndex, shapeIndex);
        //o.setDescription()
        // TODO not in OMERO model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineFill(java.lang.Integer, int, int)
     */
    public void setLineFill(Integer fill, int ROIIndex, int shapeIndex)
    {
        //Line o = getLine(ROIIndex, shapeIndex);
        //o.setFill(toRType(fill));
        // TODO not in OMERO model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineFontSize(java.lang.Integer, int, int)
     */
    public void setLineFontSize(NonNegativeInteger fontSize, int ROIIndex, int shapeIndex)
    {
        Line o = getLine(ROIIndex, shapeIndex);
        o.setFontSize(toRType(fontSize));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineLabel(java.lang.String, int, int)
     */
    public void setLineLabel(String label, int ROIIndex, int shapeIndex)
    {
        //Line o = getLine(ROIIndex, shapeIndex);
        //o.setLineLabel(toRType(fontSize));
        // TODO not in OMERO model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineName(java.lang.String, int, int)
     */
    public void setLineName(String name, int ROIIndex, int shapeIndex)
    {
        //Line o = getLine(ROIIndex, shapeIndex);
        //o.setName(toRType(fontSize));
        // TODO not in OMERO model

    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineStroke(java.lang.Integer, int, int)
     */
    public void setLineStroke(Integer stroke, int ROIIndex, int shapeIndex)
    {
        Line o = getLine(ROIIndex, shapeIndex);
        o.setStrokeWidth(toRType(stroke));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineStrokeDashArray(java.lang.String, int, int)
     */
    public void setLineStrokeDashArray(String strokeDashArray, int ROIIndex,
            int shapeIndex)
    {
        Line o = getLine(ROIIndex, shapeIndex);
        o.setStrokeDashArray(toRType(strokeDashArray));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineStrokeWidth(java.lang.Double, int, int)
     */
    public void setLineStrokeWidth(Double strokeWidth, int ROIIndex,
            int shapeIndex)
    {
        Line o = getLine(ROIIndex, shapeIndex);
        o.setStrokeWidth(toRType(strokeWidth.intValue()));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineTheC(java.lang.Integer, int, int)
     */
    public void setLineTheC(NonNegativeInteger theC, int ROIIndex, int shapeIndex)
    {
        Line o = getLine(ROIIndex, shapeIndex);
        o.setTheC(toRType(theC));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineTheT(java.lang.Integer, int, int)
     */
    public void setLineTheT(NonNegativeInteger theT, int ROIIndex, int shapeIndex)
    {
        Line o = getLine(ROIIndex, shapeIndex);
        o.setTheT(toRType(theT));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineTheZ(java.lang.Integer, int, int)
     */
    public void setLineTheZ(NonNegativeInteger theZ, int ROIIndex, int shapeIndex)
    {
        Line o = getLine(ROIIndex, shapeIndex);
        o.setTheZ(toRType(theZ));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineTransform(java.lang.String, int, int)
     */
    public void setLineTransform(String transform, int ROIIndex, int shapeIndex)
    {
        Line o = getLine(ROIIndex, shapeIndex);
        o.setTransform(toRType(transform));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineX1(java.lang.Double, int, int)
     */
    public void setLineX1(Double x1, int ROIIndex, int shapeIndex)
    {
        Line o = getLine(ROIIndex, shapeIndex);
        o.setX1(toRType(x1));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineX2(java.lang.Double, int, int)
     */
    public void setLineX2(Double x2, int ROIIndex, int shapeIndex)
    {
        Line o = getLine(ROIIndex, shapeIndex);
        o.setX2(toRType(x2));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineY1(java.lang.Double, int, int)
     */
    public void setLineY1(Double y1, int ROIIndex, int shapeIndex)
    {
        Line o = getLine(ROIIndex, shapeIndex);
        o.setY1(toRType(y1));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLineY2(java.lang.Double, int, int)
     */
    public void setLineY2(Double y2, int ROIIndex, int shapeIndex)
    {
        Line o = getLine(ROIIndex, shapeIndex);
        o.setY2(toRType(y2));
    }


    //////// List Annotation /////////

    public ListAnnotation getListAnnotation(int listAnnotationIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.LIST_ANNOTATION_INDEX, listAnnotationIndex);
        return getSourceObject(ListAnnotation.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setListAnnotationID(java.lang.String, int)
     */
    public void setListAnnotationID(String id, int listAnnotationIndex)
    {
        checkDuplicateLSID(ListAnnotation.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.LIST_ANNOTATION_INDEX, listAnnotationIndex);
        IObjectContainer o = getIObjectContainer(ListAnnotation.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(ListAnnotation.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setListAnnotationAnnotationRef(java.lang.String, int, int)
     */
    public void setListAnnotationAnnotationRef(String annotation,
            int listAnnotationIndex, int annotationRefIndex)
    {
        LSID key = new LSID(Annotation.class, listAnnotationIndex);
        addReference(key, new LSID(annotation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setListAnnotationNamespace(java.lang.String, int)
     */
    public void setListAnnotationNamespace(String namespace,
            int listAnnotationIndex)
    {
        ListAnnotation o = getListAnnotation(listAnnotationIndex);
        o.setNs(toRType(namespace));
    }


    //////// Long Annotation /////////

    public LongAnnotation getLongAnnotation(int longAnnotationIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.LONG_ANNOTATION_INDEX, longAnnotationIndex);
        return getSourceObject(LongAnnotation.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLongAnnotationID(java.lang.String, int)
     */
    public void setLongAnnotationID(String id, int longAnnotationIndex)
    {
        checkDuplicateLSID(LongAnnotation.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.LONG_ANNOTATION_INDEX, longAnnotationIndex);
        IObjectContainer o = getIObjectContainer(LongAnnotation.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(LongAnnotation.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLongAnnotationNamespace(java.lang.String, int)
     */
    public void setLongAnnotationNamespace(String namespace,
            int longAnnotationIndex)
    {
        LongAnnotation o = getLongAnnotation(longAnnotationIndex);
        o.setNs(toRType(namespace));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLongAnnotationValue(java.lang.Long, int)
     */
    public void setLongAnnotationValue(Long value, int longAnnotationIndex)
    {
        LongAnnotation o = getLongAnnotation(longAnnotationIndex);
        o.setLongValue(toRType(value));
    }

    ///////// Mask ////////

    public Mask getMask(int ROIIndex, int shapeIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.ROI_INDEX, ROIIndex);
        indexes.put(Index.SHAPE_INDEX, shapeIndex);
        return getSourceObject(Mask.class, indexes);
    }

    public void setMaskBinData(byte[] binData, int roiIndex, int shapeIndex)
    {
        Mask o = getMask(roiIndex, shapeIndex);
        if (o != null)
        {
            o.setBytes(binData);
        }
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskDescription(java.lang.String, int, int)
     */
    public void setMaskDescription(String description, int ROIIndex,
            int shapeIndex)
    {
        // TODO : not in OMERO model
        //Mask o = getMask(ROIIndex, shapeIndex);
        //o.setDescription(toRType(description));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskFill(java.lang.Integer, int, int)
     */
    public void setMaskFill(Integer fill, int ROIIndex, int shapeIndex)
    {
        // TODO : not in OMERO model
        //Mask o = getMask(ROIIndex, shapeIndex);
        //o.setFill(toRType(fill));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskFontSize(java.lang.Integer, int, int)
     */
    public void setMaskFontSize(NonNegativeInteger fontSize, int ROIIndex, int shapeIndex)
    {
        Mask o = getMask(ROIIndex, shapeIndex);
        o.setFontSize(toRType(fontSize));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskID(java.lang.String, int, int)
     */
    public void setMaskID(String id, int ROIIndex, int shapeIndex)
    {
        checkDuplicateLSID(Mask.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.ROI_INDEX, ROIIndex);
        indexes.put(Index.SHAPE_INDEX, shapeIndex);
        IObjectContainer o = getIObjectContainer(Mask.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Mask.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskLabel(java.lang.String, int, int)
     */
    public void setMaskLabel(String label, int ROIIndex, int shapeIndex)
    {
        // TODO : not in OMERO model
        //Mask o = getMask(Mask.class, id);
        //o.setLabel(toRType(label));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskName(java.lang.String, int, int)
     */
    public void setMaskName(String name, int ROIIndex, int shapeIndex)
    {
        // TODO : not in OMERO model
        // Mask o = getMask(Mask.class, id);
        // o.setName(toRType(name));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskStroke(java.lang.Integer, int, int)
     */
    public void setMaskStroke(Integer stroke, int ROIIndex, int shapeIndex)
    {
        // TODO : not in OMERO model
        // Mask o = getMask(ROIIndex, shapeIndex);
        // o.setStroke(toRType(stroke));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskStrokeDashArray(java.lang.String, int, int)
     */
    public void setMaskStrokeDashArray(String strokeDashArray, int ROIIndex,
            int shapeIndex)
    {
        Mask o = getMask(ROIIndex, shapeIndex);
        o.setStrokeDashArray(toRType(strokeDashArray));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskStrokeWidth(java.lang.Double, int, int)
     */
    public void setMaskStrokeWidth(Double strokeWidth, int ROIIndex,
            int shapeIndex)
    {
        // TODO : not in OMERO model
        //Mask o = getMask(ROIIndex, shapeIndex);
        //o.setStrokeWidth(toRType(strokeWidth));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskTheC(java.lang.Integer, int, int)
     */
    public void setMaskTheC(NonNegativeInteger theC, int ROIIndex, int shapeIndex)
    {
        Mask o = getMask(ROIIndex, shapeIndex);
        o.setTheC(toRType(theC));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskTheT(java.lang.Integer, int, int)
     */
    public void setMaskTheT(NonNegativeInteger theT, int ROIIndex, int shapeIndex)
    {
        Mask o = getMask(ROIIndex, shapeIndex);
        o.setTheT(toRType(theT));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskTheZ(java.lang.Integer, int, int)
     */
    public void setMaskTheZ(NonNegativeInteger theZ, int ROIIndex, int shapeIndex)
    {
        Mask o = getMask(ROIIndex, shapeIndex);
        o.setTheZ(toRType(theZ));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskTransform(java.lang.String, int, int)
     */
    public void setMaskTransform(String transform, int ROIIndex, int shapeIndex)
    {
        Mask o = getMask(ROIIndex, shapeIndex);
        o.setTransform(toRType(transform));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskX(java.lang.Double, int, int)
     */
    public void setMaskX(Double x, int ROIIndex, int shapeIndex)
    {
        Mask o = getMask(ROIIndex, shapeIndex);
        o.setX(toRType(x));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskY(java.lang.Double, int, int)
     */
    public void setMaskY(Double y, int ROIIndex, int shapeIndex)
    {
        Mask o = getMask(ROIIndex, shapeIndex);
        o.setY(toRType(y));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskHeight(java.lang.Double, int, int)
     */
    public void setMaskHeight(Double height, int ROIIndex, int shapeIndex)
    {
        Mask o = getMask(ROIIndex, shapeIndex);
        o.setHeight(toRType(height));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMaskWidth(java.lang.Double, int, int)
     */
    public void setMaskWidth(Double width, int ROIIndex, int shapeIndex)
    {
        Mask o = getMask(ROIIndex, shapeIndex);
        o.setWidth(toRType(width));
    }

    //////// Microbean Manipulation /////////

    public MicrobeamManipulation getMicrobeamManipulation(int experimentIndex, int microbeamManipulationIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.EXPERIMENT_INDEX, experimentIndex);
        indexes.put(Index.MICROBEAM_MANIPULATION_INDEX, microbeamManipulationIndex);
        return getSourceObject(MicrobeamManipulation.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicrobeamManipulationID(java.lang.String, int, int)
     */
    public void setMicrobeamManipulationID(String id, int experimentIndex,
            int microbeamManipulationIndex)
    {
        checkDuplicateLSID(MicrobeamManipulation.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.EXPERIMENT_INDEX, experimentIndex);
        indexes.put(Index.MICROBEAM_MANIPULATION_INDEX, microbeamManipulationIndex);
        IObjectContainer o = getIObjectContainer(MicrobeamManipulation.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(MicrobeamManipulation.class, id, o);
    }

    public void setMicrobeamManipulationDescription(String description, int experimentIndex, int microbeamManipulationIndex)
    {
        MicrobeamManipulation o = getMicrobeamManipulation(experimentIndex, microbeamManipulationIndex);
        o.setDescription(toRType(description));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicrobeamManipulationExperimenterRef(java.lang.String, int, int)
     */
    public void setMicrobeamManipulationExperimenterRef(String experimenter,
            int experimentIndex, int microbeamManipulationIndex)
    {
        //LSID key = new LSID(MicrobeamManipulation.class, experimentIndex, microbeamManipulationIndex);
        //addReference(key, new LSID(experimenter));
    }


    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicrobeamManipulationROIRef(java.lang.String, int, int, int)
     */
    public void setMicrobeamManipulationROIRef(String roi, int experimentIndex,
            int microbeamManipulationIndex, int ROIRefIndex)
    {
        LSID key = new LSID(MicrobeamManipulation.class, experimentIndex, microbeamManipulationIndex);
        addReference(key, new LSID(roi));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicrobeamManipulationType(ome.xml.model.enums.MicrobeamManipulationType, int, int)
     */
    public void setMicrobeamManipulationType(ome.xml.model.enums.MicrobeamManipulationType type,
            int experimentIndex, int microbeamManipulationIndex)
    {
        MicrobeamManipulation o = getMicrobeamManipulation(experimentIndex, microbeamManipulationIndex);
        o.setType((MicrobeamManipulationType) getEnumeration(MicrobeamManipulationType.class, type.toString()));
    }

    ////////Microbeam Manipulation Light Source Settings /////////

    public LightSettings getMicrobeamManipulationLightSourceSettings(int experimentIndex, int microbeamManipulationIndex,
            int lightSourceSettingsIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.EXPERIMENT_INDEX, experimentIndex);
        indexes.put(Index.MICROBEAM_MANIPULATION_INDEX, microbeamManipulationIndex);
        indexes.put(Index.LIGHT_SOURCE_SETTINGS_INDEX, lightSourceSettingsIndex);
        return getSourceObject(LightSettings.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicrobeamManipulationLightSourceSettingsID(java.lang.String, int, int, int)
     */
    public void setMicrobeamManipulationLightSourceSettingsID(String id,
            int experimentIndex, int microbeamManipulationIndex,
            int lightSourceSettingsIndex)
    {
        getMicrobeamManipulationLightSourceSettings(
                experimentIndex, microbeamManipulationIndex,
                lightSourceSettingsIndex);
        LSID key = new LSID(LightSettings.class, experimentIndex,
			microbeamManipulationIndex, lightSourceSettingsIndex);
        addReference(key, new LSID(id));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicrobeamManipulationLightSourceSettingsAttenuation(ome.xml.model.primitives.PercentFraction, int, int, int)
     */
    public void setMicrobeamManipulationLightSourceSettingsAttenuation(
            PercentFraction attenuation, int experimentIndex,
            int microbeamManipulationIndex, int lightSourceSettingsIndex)
    {
        LightSettings o = getMicrobeamManipulationLightSourceSettings(experimentIndex,
                microbeamManipulationIndex, lightSourceSettingsIndex);
        o.setAttenuation(toRType(attenuation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicrobeamManipulationLightSourceSettingsWavelength(ome.xml.model.primitives.PositiveInteger, int, int, int)
     */
    public void setMicrobeamManipulationLightSourceSettingsWavelength(
            PositiveInteger wavelength, int experimentIndex,
            int microbeamManipulationIndex, int lightSourceSettingsIndex)
    {
        LightSettings o = getMicrobeamManipulationLightSourceSettings(experimentIndex,
                microbeamManipulationIndex, lightSourceSettingsIndex);
        o.setWavelength(toRType(wavelength));
    }

    //////// Microscope ////////

    private Microscope getMicroscope(int instrumentIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        return getSourceObject(Microscope.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicroscopeLotNumber(java.lang.String, int)
     */
    public void setMicroscopeLotNumber(String lotNumber, int instrumentIndex)
    {
        Microscope o = getMicroscope(instrumentIndex);
        o.setLotNumber(toRType(lotNumber));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicroscopeManufacturer(java.lang.String, int)
     */
    public void setMicroscopeManufacturer(String manufacturer,
            int instrumentIndex)
    {
        Microscope o = getMicroscope(instrumentIndex);
        o.setManufacturer(toRType(manufacturer));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicroscopeModel(java.lang.String, int)
     */
    public void setMicroscopeModel(String model, int instrumentIndex)
    {
        Microscope o = getMicroscope(instrumentIndex);
        o.setModel(toRType(model));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicroscopeSerialNumber(java.lang.String, int)
     */
    public void setMicroscopeSerialNumber(String serialNumber,
            int instrumentIndex)
    {
        Microscope o = getMicroscope(instrumentIndex);
        o.setSerialNumber(toRType(serialNumber));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setMicroscopeType(ome.xml.model.enums.MicroscopeType, int)
     */
    public void setMicroscopeType(ome.xml.model.enums.MicroscopeType type,
            int instrumentIndex)
    {
        Microscope o = getMicroscope(instrumentIndex);
        o.setType((MicroscopeType)
                getEnumeration(MicroscopeType.class, type.toString()));
    }

    //////// OTF /////////

    public OTF getOTF(int instrumentIndex, int OTFIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        indexes.put(Index.OTF_INDEX, OTFIndex);
        return getSourceObject(OTF.class, indexes);
    }


    private OriginalFile getOriginalFile(int instrumentIndex, int OTFIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        indexes.put(Index.OTF_INDEX, OTFIndex);
        return getSourceObject(OriginalFile.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFID(java.lang.String, int, int)
     */
    public void setOTFID(String id, int instrumentIndex, int OTFIndex)
    {
        checkDuplicateLSID(OTF.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        indexes.put(Index.OTF_INDEX, OTFIndex);
        IObjectContainer o = getIObjectContainer(OTF.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(OTF.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFBinaryFileFileName(java.lang.String, int, int)
     */
    public void setOTFBinaryFileFileName(String fileName, int instrumentIndex,
            int OTFIndex)
    {
        OriginalFile o = getOriginalFile(instrumentIndex, OTFIndex);
        o.setName(toRType(fileName));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFBinaryFileMIMEType(java.lang.String, int, int)
     */
    public void setOTFBinaryFileMIMEType(String mimetype, int instrumentIndex,
            int OTFIndex)
    {
        OriginalFile o = getOriginalFile(instrumentIndex, OTFIndex);
        o.setMimetype(toRType(mimetype));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFBinaryFileSize(java.lang.Integer, int, int)
     */
    public void setOTFBinaryFileSize(NonNegativeLong size, int instrumentIndex,
            int OTFIndex)
    {
        OriginalFile o = getOriginalFile(instrumentIndex, OTFIndex);
        o.setSize(toRType(size));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFFilterSetRef(java.lang.String, int, int)
     */
    public void setOTFFilterSetRef(String filterSet, int instrumentIndex,
            int OTFIndex)
    {
        LSID key = new LSID(OTF.class, instrumentIndex, OTFIndex);
        addReference(key, new LSID(filterSet));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFOpticalAxisAveraged(java.lang.Boolean, int, int)
     */
    public void setOTFOpticalAxisAveraged(Boolean opticalAxisAveraged,
            int instrumentIndex, int OTFIndex)
    {
        OTF o = getOTF(instrumentIndex, OTFIndex);
        o.setOpticalAxisAveraged(toRType(opticalAxisAveraged));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFSizeX(ome.xml.model.primitives.PositiveInteger, int, int)
     */
    public void setOTFSizeX(PositiveInteger sizeX, int instrumentIndex,
            int OTFIndex)
    {
        OTF o = getOTF(instrumentIndex, OTFIndex);
        o.setSizeX(toRType(sizeX));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFSizeY(ome.xml.model.primitives.PositiveInteger, int, int)
     */
    public void setOTFSizeY(PositiveInteger sizeY, int instrumentIndex,
            int OTFIndex)
    {
        OTF o = getOTF(instrumentIndex, OTFIndex);
        o.setSizeY(toRType(sizeY));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFType(ome.xml.model.enums.PixelType, int, int)
     */
    public void setOTFType(ome.xml.model.enums.PixelType type, int instrumentIndex, int OTFIndex)
    {
        OTF o = getOTF(instrumentIndex, OTFIndex);
        o.setPixelsType((PixelsType) getEnumeration(PixelsType.class, type.toString()));
    }

    ////////OTF Objective Settings /////////

    private ObjectiveSettings getOTFObjectiveSettings(int instrumentIndex, int OTFIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        indexes.put(Index.OTF_INDEX, OTFIndex);
        return getSourceObject(ObjectiveSettings.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFObjectiveSettingsID(java.lang.String, int, int)
     */
    public void setOTFObjectiveSettingsID(String id, int instrumentIndex,
            int OTFIndex)
    {
        getOTFObjectiveSettings(instrumentIndex, OTFIndex);
        LSID key = new LSID(OTF.class, instrumentIndex, OTFIndex);
        addReference(key, new LSID(id));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFObjectiveSettingsCorrectionCollar(java.lang.Double, int, int)
     */
    public void setOTFObjectiveSettingsCorrectionCollar(
            Double correctionCollar, int instrumentIndex, int OTFIndex)
    {
        ObjectiveSettings o = getOTFObjectiveSettings(instrumentIndex, OTFIndex);
        o.setCorrectionCollar(toRType(correctionCollar));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFObjectiveSettingsMedium(ome.xml.model.enums.Medium, int, int)
     */
    public void setOTFObjectiveSettingsMedium(
            ome.xml.model.enums.Medium medium, int instrumentIndex,
            int OTFIndex)
    {
        ObjectiveSettings o = getOTFObjectiveSettings(instrumentIndex, OTFIndex);
        o.setMedium((Medium) getEnumeration(Medium.class, medium.toString()));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setOTFObjectiveSettingsRefractiveIndex(java.lang.Double, int, int)
     */
    public void setOTFObjectiveSettingsRefractiveIndex(Double refractiveIndex,
            int instrumentIndex, int OTFIndex)
    {
        ObjectiveSettings o = getOTFObjectiveSettings(instrumentIndex, OTFIndex);
        o.setRefractiveIndex(toRType(refractiveIndex));
    }


    //////// Objective /////////

    public Objective getObjective(int instrumentIndex, int objectiveIndex)
    {
      LinkedHashMap<Index, Integer> indexes =
          new LinkedHashMap<Index, Integer>();
      indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
      indexes.put(Index.OBJECTIVE_INDEX, objectiveIndex);
      return getSourceObject(Objective.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveCalibratedMagnification(java.lang.Double, int, int)
     */
    public void setObjectiveCalibratedMagnification(
            Double calibratedMagnification, int instrumentIndex,
            int objectiveIndex)
    {
        Objective o = getObjective(instrumentIndex, objectiveIndex);
        o.setCalibratedMagnification(toRType(calibratedMagnification));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveCorrection(ome.xml.model.enums.Correction, int, int)
     */
    public void setObjectiveCorrection(
            ome.xml.model.enums.Correction correction, int instrumentIndex,
            int objectiveIndex)
    {
        Objective o = getObjective(instrumentIndex, objectiveIndex);
        o.setCorrection((Correction) getEnumeration(
            Correction.class, correction.toString()));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveID(java.lang.String, int, int)
     */
    public void setObjectiveID(String id, int instrumentIndex,
            int objectiveIndex)
    {
        checkDuplicateLSID(Objective.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.INSTRUMENT_INDEX, instrumentIndex);
        indexes.put(Index.OBJECTIVE_INDEX, objectiveIndex);
        IObjectContainer o = getIObjectContainer(Objective.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Objective.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveImmersion(ome.xml.model.enums.Immersion, int, int)
     */
    public void setObjectiveImmersion(
            ome.xml.model.enums.Immersion immersion, int instrumentIndex,
            int objectiveIndex)
    {
        Objective o = getObjective(instrumentIndex, objectiveIndex);
        o.setImmersion(
            (Immersion) getEnumeration(Immersion.class, immersion.toString()));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveIris(java.lang.Boolean, int, int)
     */
    public void setObjectiveIris(Boolean iris, int instrumentIndex,
            int objectiveIndex)
    {
        Objective o = getObjective(instrumentIndex, objectiveIndex);
        o.setIris(toRType(iris));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveLensNA(java.lang.Double, int, int)
     */
    public void setObjectiveLensNA(Double lensNA, int instrumentIndex,
            int objectiveIndex)
    {
        Objective o = getObjective(instrumentIndex, objectiveIndex);
        o.setLensNA(toRType(lensNA));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveLotNumber(java.lang.String, int, int)
     */
    public void setObjectiveLotNumber(String lotNumber, int instrumentIndex,
            int objectiveIndex)
    {
        Objective o = getObjective(instrumentIndex, objectiveIndex);
        o.setLotNumber(toRType(lotNumber));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveManufacturer(java.lang.String, int, int)
     */
    public void setObjectiveManufacturer(String manufacturer,
            int instrumentIndex, int objectiveIndex)
    {
        Objective o = getObjective(instrumentIndex, objectiveIndex);
        o.setManufacturer(toRType(manufacturer));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveModel(java.lang.String, int, int)
     */
    public void setObjectiveModel(String model, int instrumentIndex,
            int objectiveIndex)
    {
        Objective o = getObjective(instrumentIndex, objectiveIndex);
        o.setModel(toRType(model));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveNominalMagnification(java.lang.Integer, int, int)
     */
    public void setObjectiveNominalMagnification(PositiveInteger nominalMagnification,
            int instrumentIndex, int objectiveIndex)
    {
        Objective o = getObjective(instrumentIndex, objectiveIndex);
        o.setNominalMagnification(toRType(nominalMagnification));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveSerialNumber(java.lang.String, int, int)
     */
    public void setObjectiveSerialNumber(String serialNumber,
            int instrumentIndex, int objectiveIndex)
    {
        Objective o = getObjective(instrumentIndex, objectiveIndex);
        o.setSerialNumber(toRType(serialNumber));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setObjectiveWorkingDistance(java.lang.Double, int, int)
     */
    public void setObjectiveWorkingDistance(Double workingDistance,
            int instrumentIndex, int objectiveIndex)
    {
        Objective o = getObjective(instrumentIndex, objectiveIndex);
        o.setWorkingDistance(toRType(workingDistance));
    }

    //////// Path /////////

    private Path getPath(int ROIIndex, int shapeIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.ROI_INDEX, ROIIndex);
        indexes.put(Index.SHAPE_INDEX, shapeIndex);
        return getSourceObject(Path.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathDefinition(java.lang.String, int, int)
     */
    public void setPathDefinition(String definition, int ROIIndex,
            int shapeIndex)
    {
        // TODO : double-check that this is correct
        Path o = getPath(ROIIndex, shapeIndex);
        o.setD(toRType(definition));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathDescription(java.lang.String, int, int)
     */
    public void setPathDescription(String description, int ROIIndex,
            int shapeIndex)
    {
        // TODO : not in OMERO model
        // Path o = getPath(ROIIndex, shapeIndex);
        // o.setDescription(toRType(description));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathFill(java.lang.Integer, int, int)
     */
    public void setPathFill(Integer fill, int ROIIndex, int shapeIndex)
    {
        // TODO : not in OMERO model
        // Path o = getPath(ROIIndex, shapeIndex);
        // o.setFill(toRType(fill));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathFontSize(java.lang.Integer, int, int)
     */
    public void setPathFontSize(NonNegativeInteger fontSize, int ROIIndex, int shapeIndex)
    {
        Path o = getPath(ROIIndex, shapeIndex);
        o.setFontSize(toRType(fontSize));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathID(java.lang.String, int, int)
     */
    public void setPathID(String id, int ROIIndex, int shapeIndex)
    {
        checkDuplicateLSID(Path.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.ROI_INDEX, ROIIndex);
        indexes.put(Index.SHAPE_INDEX, shapeIndex);
        IObjectContainer o = getIObjectContainer(Path.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Path.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathLabel(java.lang.String, int, int)
     */
    public void setPathLabel(String label, int ROIIndex, int shapeIndex)
    {
        // TODO : not in OMERO model
        // Path o = getPath(ROIIndex, shapeIndex);
        // o.setLabel(toRType(label));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathName(java.lang.String, int, int)
     */
    public void setPathName(String name, int ROIIndex, int shapeIndex)
    {
        // TODO : not in OMERO model
        // Path o = getPath(ROIIndex, shapeIndex);
        // o.setName(toRType(name));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathStroke(java.lang.Integer, int, int)
     */
    public void setPathStroke(Integer stroke, int ROIIndex, int shapeIndex)
    {
        // TODO : not in OMERO model
        // Path o = getPath(ROIIndex, shapeIndex);
        // o.setStroke(toRType(stroke));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathStrokeDashArray(java.lang.String, int, int)
     */
    public void setPathStrokeDashArray(String strokeDashArray, int ROIIndex,
            int shapeIndex)
    {
        Path o = getPath(ROIIndex, shapeIndex);
        o.setStrokeDashArray(toRType(strokeDashArray));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathStrokeWidth(java.lang.Double, int, int)
     */
    public void setPathStrokeWidth(Double strokeWidth, int ROIIndex,
            int shapeIndex)
    {
        // TODO : not in OMERO model
        //Path o = getPath(ROIIndex, shapeIndex);
        //o.setStrokeWidth(toRType(strokeWidth));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathTheC(java.lang.Integer, int, int)
     */
    public void setPathTheC(NonNegativeInteger theC, int ROIIndex, int shapeIndex)
    {
        Path o = getPath(ROIIndex, shapeIndex);
        o.setTheC(toRType(theC));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathTheT(java.lang.Integer, int, int)
     */
    public void setPathTheT(NonNegativeInteger theT, int ROIIndex, int shapeIndex)
    {
        Path o = getPath(ROIIndex, shapeIndex);
        o.setTheT(toRType(theT));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathTheZ(java.lang.Integer, int, int)
     */
    public void setPathTheZ(NonNegativeInteger theZ, int ROIIndex, int shapeIndex)
    {
        Path o = getPath(ROIIndex, shapeIndex);
        o.setTheZ(toRType(theZ));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPathTransform(java.lang.String, int, int)
     */
    public void setPathTransform(String transform, int ROIIndex, int shapeIndex)
    {
        Path o = getPath(ROIIndex, shapeIndex);
        o.setTransform(toRType(transform));
    }

    //////// Pixels /////////

    private Pixels getPixels(int imageIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.IMAGE_INDEX, imageIndex);
        Pixels p = getSourceObject(Pixels.class, indexes);
        p.setSha1(rstring("Foo"));
        return p;

    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsID(java.lang.String, int)
     */
    public void setPixelsID(String id, int imageIndex)
    {
        checkDuplicateLSID(Pixels.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.IMAGE_INDEX, imageIndex);
        IObjectContainer o = getIObjectContainer(Pixels.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Pixels.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsAnnotationRef(java.lang.String, int, int)
     */
    public void setPixelsAnnotationRef(String annotation, int imageIndex,
            int annotationRefIndex)
    {
        LSID key = new LSID(Pixels.class, imageIndex);
        addReference(key, new LSID(annotation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsBinDataBigEndian(java.lang.Boolean, int, int)
     */
    public void setPixelsBinDataBigEndian(Boolean bigEndian, int imageIndex,
            int binDataIndex)
    {
        // TODO : not in OMERO model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsDimensionOrder(ome.xml.model.enums.DimensionOrder, int)
     */
    public void setPixelsDimensionOrder(
            ome.xml.model.enums.DimensionOrder dimensionOrder, int imageIndex)
    {
        Pixels o = getPixels(imageIndex);
        // We're always the same dimension order in the server; force it to
        // "XYZCT" (ticket:3124, ticket:3718, ticket:3668)
        o.setDimensionOrder((DimensionOrder) getEnumeration(
            DimensionOrder.class, "XYZCT"));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsPhysicalSizeX(ome.xml.model.primitives.PositiveFloat, int)
     */
    public void setPixelsPhysicalSizeX(PositiveFloat physicalSizeX, int imageIndex)
    {
        Pixels o = getPixels(imageIndex);
        o.setPhysicalSizeX(toRType(physicalSizeX));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsPhysicalSizeY(ome.xml.model.primitives.PositiveFloat, int)
     */
    public void setPixelsPhysicalSizeY(PositiveFloat physicalSizeY, int imageIndex)
    {
        Pixels o = getPixels(imageIndex);
        o.setPhysicalSizeY(toRType(physicalSizeY));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsPhysicalSizeZ(ome.xml.model.primitives.PositiveFloat, int)
     */
    public void setPixelsPhysicalSizeZ(PositiveFloat physicalSizeZ, int imageIndex)
    {
        Pixels o = getPixels(imageIndex);
        o.setPhysicalSizeZ(toRType(physicalSizeZ));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsSizeC(ome.xml.model.primitives.PositiveInteger, int)
     */
    public void setPixelsSizeC(PositiveInteger sizeC, int imageIndex)
    {
        Pixels o = getPixels(imageIndex);
        o.setSizeC(toRType(sizeC));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsSizeT(ome.xml.model.primitives.PositiveInteger, int)
     */
    public void setPixelsSizeT(PositiveInteger sizeT, int imageIndex)
    {
        Pixels o = getPixels(imageIndex);
        o.setSizeT(toRType(sizeT));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsSizeX(ome.xml.model.primitives.PositiveInteger, int)
     */
    public void setPixelsSizeX(PositiveInteger sizeX, int imageIndex)
    {
        Pixels o = getPixels(imageIndex);
        o.setSizeX(toRType(sizeX));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsSizeY(ome.xml.model.primitives.PositiveInteger, int)
     */
    public void setPixelsSizeY(PositiveInteger sizeY, int imageIndex)
    {
        Pixels o = getPixels(imageIndex);
        o.setSizeY(toRType(sizeY));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsSizeZ(ome.xml.model.primitives.PositiveInteger, int)
     */
    public void setPixelsSizeZ(PositiveInteger sizeZ, int imageIndex)
    {
        Pixels o = getPixels(imageIndex);
        o.setSizeZ(toRType(sizeZ));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsTimeIncrement(java.lang.Double, int)
     */
    public void setPixelsTimeIncrement(Double timeIncrement, int imageIndex)
    {
        Pixels o = getPixels(imageIndex);
        o.setTimeIncrement(toRType(timeIncrement));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPixelsType(ome.xml.model.enums.PixelType, int)
     */
    public void setPixelsType(PixelType type, int imageIndex)
    {
        Pixels o = getPixels(imageIndex);
        o.setPixelsType(
            (PixelsType) getEnumeration(PixelsType.class, type.toString()));
    }

    //////// Plane /////////

    private PlaneInfo getPlane(int imageIndex, int planeIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.IMAGE_INDEX, imageIndex);
        indexes.put(Index.PLANE_INDEX, planeIndex);
        return getSourceObject(PlaneInfo.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlaneAnnotationRef(java.lang.String, int, int, int)
     */
    public void setPlaneAnnotationRef(String annotation, int imageIndex,
            int planeIndex, int annotationRefIndex)
    {
        LSID key = new LSID(
            PlaneInfo.class, imageIndex, planeIndex);
        addReference(key, new LSID(annotation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlaneDeltaT(java.lang.Double, int, int)
     */
    public void setPlaneDeltaT(Double deltaT, int imageIndex, int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        o.setDeltaT(toRType(deltaT));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlaneExposureTime(java.lang.Double, int, int)
     */
    public void setPlaneExposureTime(Double exposureTime, int imageIndex,
            int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        o.setExposureTime(toRType(exposureTime));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlaneHashSHA1(java.lang.String, int, int)
     */
    public void setPlaneHashSHA1(String hashSHA1, int imageIndex, int planeIndex)
    {
        // TODO : not in the OMERO model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlanePositionX(java.lang.Double, int, int)
     */
    public void setPlanePositionX(Double positionX, int imageIndex,
            int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        o.setPositionX(toRType(positionX));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlanePositionY(java.lang.Double, int, int)
     */
    public void setPlanePositionY(Double positionY, int imageIndex,
            int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        o.setPositionY(toRType(positionY));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlanePositionZ(java.lang.Double, int, int)
     */
    public void setPlanePositionZ(Double positionZ, int imageIndex,
            int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        o.setPositionZ(toRType(positionZ));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlaneTheC(java.lang.Integer, int, int)
     */
    public void setPlaneTheC(NonNegativeInteger theC, int imageIndex, int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        o.setTheC(toRType(theC));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlaneTheT(java.lang.Integer, int, int)
     */
    public void setPlaneTheT(NonNegativeInteger theT, int imageIndex, int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        o.setTheT(toRType(theT));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlaneTheZ(java.lang.Integer, int, int)
     */
    public void setPlaneTheZ(NonNegativeInteger theZ, int imageIndex, int planeIndex)
    {
        PlaneInfo o = getPlane(imageIndex, planeIndex);
        o.setTheZ(toRType(theZ));
    }

    //////// PlateAcquisition /////////

    private PlateAcquisition getPlateAcquisition(
        int plateIndex, int plateAcquisitionIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.PLATE_INDEX, plateIndex);
        indexes.put(Index.PLATE_ACQUISITION_INDEX, plateAcquisitionIndex);
        return getSourceObject(PlateAcquisition.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateAcquisitionAnnotationRef(java.lang.String, int, int, int)
     */
    public void setPlateAcquisitionAnnotationRef(String annotation,
            int plateIndex, int plateAcquisitionIndex, int annotationRefIndex)
    {
        LSID key = new LSID(PlateAcquisition.class, plateIndex,
            plateAcquisitionIndex);
        addReference(key, new LSID(annotation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateAcquisitionDescription(java.lang.String, int, int)
     */
    public void setPlateAcquisitionDescription(String description,
            int plateIndex, int plateAcquisitionIndex)
    {
        PlateAcquisition o =
            getPlateAcquisition(plateIndex, plateAcquisitionIndex);
        o.setDescription(toRType(description));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateAcquisitionEndTime(java.lang.String, int, int)
     */
    public void setPlateAcquisitionEndTime(String endTime, int plateIndex,
            int plateAcquisitionIndex)
    {
        PlateAcquisition o =
            getPlateAcquisition(plateIndex, plateAcquisitionIndex);
        o.setEndTime(toRType(timestampFromXmlString(endTime)));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateAcquisitionID(java.lang.String, int, int)
     */
    public void setPlateAcquisitionID(String id, int plateIndex,
            int plateAcquisitionIndex)
    {
        checkDuplicateLSID(PlateAcquisition.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.PLATE_INDEX, plateIndex);
        indexes.put(Index.PLATE_ACQUISITION_INDEX, plateAcquisitionIndex);
        IObjectContainer o =
            getIObjectContainer(PlateAcquisition.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(PlateAcquisition.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateAcquisitionMaximumFieldCount(java.lang.Integer, int, int)
     */
    public void setPlateAcquisitionMaximumFieldCount(PositiveInteger maximumFieldCount,
            int plateIndex, int plateAcquisitionIndex)
    {
        PlateAcquisition o =
            getPlateAcquisition(plateIndex, plateAcquisitionIndex);
        o.setMaximumFieldCount(toRType(maximumFieldCount));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateAcquisitionName(java.lang.String, int, int)
     */
    public void setPlateAcquisitionName(String name, int plateIndex,
            int plateAcquisitionIndex)
    {
        PlateAcquisition o =
            getPlateAcquisition(plateIndex, plateAcquisitionIndex);
        o.setName(toRType(name));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateAcquisitionStartTime(java.lang.String, int, int)
     */
    public void setPlateAcquisitionStartTime(String startTime, int plateIndex,
            int plateAcquisitionIndex)
    {
        PlateAcquisition o =
            getPlateAcquisition(plateIndex, plateAcquisitionIndex);
        o.setStartTime(toRType(timestampFromXmlString(startTime)));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateAcquisitionWellSampleRef(java.lang.String, int, int, int)
     */
    public void setPlateAcquisitionWellSampleRef(String wellSample,
            int plateIndex, int plateAcquisitionIndex, int wellSampleRefIndex)
    {
        LSID key = new LSID(PlateAcquisition.class, plateIndex,
            plateAcquisitionIndex);
        addReference(key, new LSID(wellSample));
    }

    //////// Plate /////////

    private Plate getPlate(int plateIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.PLATE_INDEX, plateIndex);
        return getSourceObject(Plate.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateAnnotationRef(java.lang.String, int, int)
     */
    public void setPlateAnnotationRef(String annotation, int plateIndex,
            int annotationRefIndex)
    {
        LSID key = new LSID(Plate.class, plateIndex);
        addReference(key, new LSID(annotation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateColumnNamingConvention(ome.xml.model.enums.NamingConvention, int)
     */
    public void setPlateColumnNamingConvention(
            NamingConvention columnNamingConvention, int plateIndex)
    {
        Plate o = getPlate(plateIndex);
        o.setColumnNamingConvention(toRType(columnNamingConvention));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateColumns(java.lang.Integer, int)
     */
    public void setPlateColumns(PositiveInteger columns, int plateIndex)
    {
        Plate o = getPlate(plateIndex);
        o.setColumns(toRType(columns));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateDescription(java.lang.String, int)
     */
    public void setPlateDescription(String description, int plateIndex)
    {
        Plate o = getPlate(plateIndex);
        o.setDescription(toRType(description));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateExternalIdentifier(java.lang.String, int)
     */
    public void setPlateExternalIdentifier(String externalIdentifier,
            int plateIndex)
    {
        Plate o = getPlate(plateIndex);
        o.setExternalIdentifier(toRType(externalIdentifier));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateID(java.lang.String, int)
     */
    public void setPlateID(String id, int plateIndex)
    {
        checkDuplicateLSID(Plate.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.PLATE_INDEX, plateIndex);
        IObjectContainer o = getIObjectContainer(Plate.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Plate.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateName(java.lang.String, int)
     */
    public void setPlateName(String name, int plateIndex)
    {
        Plate o = getPlate(plateIndex);
        o.setName(toRType(name));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateRowNamingConvention(ome.xml.model.enums.NamingConvention, int)
     */
    public void setPlateRowNamingConvention(
            NamingConvention rowNamingConvention, int plateIndex)
    {
        Plate o = getPlate(plateIndex);
        o.setRowNamingConvention(toRType(rowNamingConvention));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateRows(java.lang.Integer, int)
     */
    public void setPlateRows(PositiveInteger rows, int plateIndex)
    {
        Plate o = getPlate(plateIndex);
        o.setRows(toRType(rows));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateScreenRef(java.lang.String, int, int)
     */
    public void setPlateScreenRef(String screen, int plateIndex,
            int screenRefIndex)
    {
        LSID key = new LSID(Plate.class, plateIndex);
        addReference(key, new LSID(screen));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateStatus(java.lang.String, int)
     */
    public void setPlateStatus(String status, int plateIndex)
    {
        Plate o = getPlate(plateIndex);
        o.setStatus(toRType(status));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateWellOriginX(java.lang.Double, int)
     */
    public void setPlateWellOriginX(Double wellOriginX, int plateIndex)
    {
        Plate o = getPlate(plateIndex);
        o.setWellOriginX(toRType(wellOriginX));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPlateWellOriginY(java.lang.Double, int)
     */
    public void setPlateWellOriginY(Double wellOriginY, int plateIndex)
    {
        Plate o = getPlate(plateIndex);
        o.setWellOriginY(toRType(wellOriginY));
    }

    //////// Point /////////

    private Point getPoint(int ROIIndex, int shapeIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.ROI_INDEX, ROIIndex);
        indexes.put(Index.SHAPE_INDEX, shapeIndex);
        return getSourceObject(Point.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointDescription(java.lang.String, int, int)
     */
    public void setPointDescription(String description, int ROIIndex,
            int shapeIndex)
    {
        // TODO : not in OMERO model
        //Point o = getPoint(ROIIndex, shapeIndex);
        //o.setDescription(toRType(description));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointFill(java.lang.Integer, int, int)
     */
    public void setPointFill(Integer fill, int ROIIndex, int shapeIndex)
    {
        // TODO : not in OMERO model
        //Point o = getPoint(ROIIndex, shapeIndex);
        //o.setFill(toRType(fill));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointFontSize(java.lang.Integer, int, int)
     */
    public void setPointFontSize(NonNegativeInteger fontSize, int ROIIndex, int shapeIndex)
    {
        Point o = getPoint(ROIIndex, shapeIndex);
        o.setFontSize(toRType(fontSize));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointID(java.lang.String, int, int)
     */
    public void setPointID(String id, int ROIIndex, int shapeIndex)
    {
        checkDuplicateLSID(Point.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.ROI_INDEX, ROIIndex);
        indexes.put(Index.SHAPE_INDEX, shapeIndex);
        IObjectContainer o = getIObjectContainer(Point.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Point.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointLabel(java.lang.String, int, int)
     */
    public void setPointLabel(String label, int ROIIndex, int shapeIndex)
    {
        // TODO : not in OMERO model
        //Point o = getPoint(ROIIndex, shapeIndex);
        //o.setLabel(toRType(label));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointName(java.lang.String, int, int)
     */
    public void setPointName(String name, int ROIIndex, int shapeIndex)
    {
        // TODO : not in OMERO model
        //Point o = getPoint(ROIIndex, shapeIndex);
        //o.setName(toRType(name));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointStroke(java.lang.Integer, int, int)
     */
    public void setPointStroke(Integer stroke, int ROIIndex, int shapeIndex)
    {
        // TODO : not in OMERO model
        //Point o = getPoint(ROIIndex, shapeIndex);
        //o.setStroke(toRType(stroke));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointStrokeDashArray(java.lang.String, int, int)
     */
    public void setPointStrokeDashArray(String strokeDashArray, int ROIIndex,
            int shapeIndex)
    {
        Point o = getPoint(ROIIndex, shapeIndex);
        o.setStrokeDashArray(toRType(strokeDashArray));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointStrokeWidth(java.lang.Double, int, int)
     */
    public void setPointStrokeWidth(Double strokeWidth, int ROIIndex,
            int shapeIndex)
    {
        // TODO : not in OMERO model
        //Point o = getPoint(ROIIndex, shapeIndex);
        //o.setStrokeWidth(toRType(strokeWidth.intValue()));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointTheC(java.lang.Integer, int, int)
     */
    public void setPointTheC(NonNegativeInteger theC, int ROIIndex, int shapeIndex)
    {
        Point o = getPoint(ROIIndex, shapeIndex);
        o.setTheC(toRType(theC));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointTheT(java.lang.Integer, int, int)
     */
    public void setPointTheT(NonNegativeInteger theT, int ROIIndex, int shapeIndex)
    {
        Point o = getPoint(ROIIndex, shapeIndex);
        o.setTheT(toRType(theT));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointTheZ(java.lang.Integer, int, int)
     */
    public void setPointTheZ(NonNegativeInteger theZ, int ROIIndex, int shapeIndex)
    {
        Point o = getPoint(ROIIndex, shapeIndex);
        o.setTheZ(toRType(theZ));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointTransform(java.lang.String, int, int)
     */
    public void setPointTransform(String transform, int ROIIndex, int shapeIndex)
    {
        Point o = getPoint(ROIIndex, shapeIndex);
        o.setTransform(toRType(transform));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointX(java.lang.Double, int, int)
     */
    public void setPointX(Double x, int ROIIndex, int shapeIndex)
    {
        Point o = getPoint(ROIIndex, shapeIndex);
        o.setCx(toRType(x));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPointY(java.lang.Double, int, int)
     */
    public void setPointY(Double y, int ROIIndex, int shapeIndex)
    {
        Point o = getPoint(ROIIndex, shapeIndex);
        o.setCy(toRType(y));
    }

    //////// Polyline /////////

    private Polyline getPolyline(int ROIIndex, int shapeIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.ROI_INDEX, ROIIndex);
        indexes.put(Index.SHAPE_INDEX, shapeIndex);
        return getSourceObject(Polyline.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineID(java.lang.String, int, int)
     */
    public void setPolylineID(String id, int ROIIndex, int shapeIndex)
    {
        checkDuplicateLSID(Polyline.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.ROI_INDEX, ROIIndex);
        indexes.put(Index.SHAPE_INDEX, shapeIndex);
        IObjectContainer o = getIObjectContainer(Polyline.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Polyline.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineClosed(java.lang.Boolean, int, int)
     */
    public void setPolylineClosed(Boolean closed, int ROIIndex, int shapeIndex)
    {
//      Polyline o = getPolyline(ROIIndex, shapeIndex);
//      o.set???(toRType(closed));
      // TODO not in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineDescription(java.lang.String, int, int)
     */
    public void setPolylineDescription(String description, int ROIIndex,
            int shapeIndex)
    {
//      Polyline o = getPolyline(ROIIndex, shapeIndex);
//      o.set???(toRType(description));
      // TODO not in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineFill(java.lang.Integer, int, int)
     */
    public void setPolylineFill(Integer fill, int ROIIndex, int shapeIndex)
    {
//      Polyline o = getPolyline(ROIIndex, shapeIndex);
//      o.set???(toRType(fill));
      // TODO which in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineFontSize(java.lang.Integer, int, int)
     */
    public void setPolylineFontSize(NonNegativeInteger fontSize, int ROIIndex,
            int shapeIndex)
    {
        Polyline o = getPolyline(ROIIndex, shapeIndex);
        o.setFontSize(toRType(fontSize));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineLabel(java.lang.String, int, int)
     */
    public void setPolylineLabel(String label, int ROIIndex, int shapeIndex)
    {
//      Polyline o = getPolyline(ROIIndex, shapeIndex);
//      o.set???(toRType(label));
      // TODO not in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineName(java.lang.String, int, int)
     */
    public void setPolylineName(String name, int ROIIndex, int shapeIndex)
    {
//      Polyline o = getPolyline(ROIIndex, shapeIndex);
//      o.set???(toRType(name));
      // TODO not in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylinePoints(java.lang.String, int, int)
     */
    public void setPolylinePoints(String points, int ROIIndex, int shapeIndex)
    {
      Polyline o = getPolyline(ROIIndex, shapeIndex);
      o.setPoints(toRType(points));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineStroke(java.lang.Integer, int, int)
     */
    public void setPolylineStroke(Integer stroke, int ROIIndex, int shapeIndex)
    {
//      Polyline o = getPolyline(ROIIndex, shapeIndex);
//      o.set???(toRType(stroke));
      // TODO which in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineStrokeDashArray(java.lang.String, int, int)
     */
    public void setPolylineStrokeDashArray(String strokeDashArray,
            int ROIIndex, int shapeIndex)
    {
        Polyline o = getPolyline(ROIIndex, shapeIndex);
        o.setStrokeDashArray(toRType(strokeDashArray));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineStrokeWidth(java.lang.Double, int, int)
     */
    public void setPolylineStrokeWidth(Double strokeWidth, int ROIIndex,
            int shapeIndex)
    {
//      Polyline o = getPolyline(ROIIndex, shapeIndex);
//      o.setStrokeWidth(toRType(strokeWidth));
      // TODO incorrect type in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineTheC(java.lang.Integer, int, int)
     */
    public void setPolylineTheC(NonNegativeInteger theC, int ROIIndex, int shapeIndex)
    {
        Polyline o = getPolyline(ROIIndex, shapeIndex);
        o.setTheC(toRType(theC));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineTheT(java.lang.Integer, int, int)
     */
    public void setPolylineTheT(NonNegativeInteger theT, int ROIIndex, int shapeIndex)
    {
        Polyline o = getPolyline(ROIIndex, shapeIndex);
        o.setTheT(toRType(theT));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineTheZ(java.lang.Integer, int, int)
     */
    public void setPolylineTheZ(NonNegativeInteger theZ, int ROIIndex, int shapeIndex)
    {
        Polyline o = getPolyline(ROIIndex, shapeIndex);
        o.setTheZ(toRType(theZ));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setPolylineTransform(java.lang.String, int, int)
     */
    public void setPolylineTransform(String transform, int ROIIndex,
            int shapeIndex)
    {
        Polyline o = getPolyline(ROIIndex, shapeIndex);
        o.setTransform(toRType(transform));
    }

    //////// Project /////////

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setProjectID(java.lang.String, int)
     */
    public void setProjectID(String id, int projectIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setProjectAnnotationRef(java.lang.String, int, int)
     */
    public void setProjectAnnotationRef(String annotation, int projectIndex,
            int annotationRefIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setProjectDescription(java.lang.String, int)
     */
    public void setProjectDescription(String description, int projectIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setProjectExperimenterRef(java.lang.String, int)
     */
    public void setProjectExperimenterRef(String experimenter, int projectIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setProjectGroupRef(java.lang.String, int)
     */
    public void setProjectGroupRef(String group, int projectIndex)
    {
        // XXX: Not handled by OMERO.
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setProjectName(java.lang.String, int)
     */
    public void setProjectName(String name, int projectIndex)
    {
        // XXX: Not handled by OMERO.
    }

    //////// ROI /////////

    private Roi getROI(int ROIIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.ROI_INDEX, ROIIndex);
        return getSourceObject(Roi.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setROIID(java.lang.String, int)
     */
    public void setROIID(String id, int ROIIndex)
    {
        checkDuplicateLSID(Roi.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.ROI_INDEX, ROIIndex);
        IObjectContainer o = getIObjectContainer(Roi.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Roi.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setROIAnnotationRef(java.lang.String, int, int)
     */
    public void setROIAnnotationRef(String annotation, int ROIIndex,
            int annotationRefIndex)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setROIDescription(java.lang.String, int)
     */
    public void setROIDescription(String description, int ROIIndex)
    {
        Roi o = getROI(ROIIndex);
        o.setDescription(toRType(description));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setROIName(java.lang.String, int)
     */
    public void setROIName(String name, int ROIIndex)
    {
//        Roi o = getROI(ROIIndex);
//        o.set(toRType(name));
        // TODO not in OMERO model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setROINamespace(java.lang.String, int)
     */
    public void setROINamespace(String namespace, int ROIIndex)
    {
        // TODO Auto-generated method stub

    }

    //////// Reagent /////////

    /**
     * Retrieve Reagent
     * @param screenIndex
     * @param reagentIndex
     * @return
     */
    private Reagent getReagent(int screenIndex, int reagentIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.SCREEN_INDEX, screenIndex);
        indexes.put(Index.REAGENT_INDEX, reagentIndex);
        return getSourceObject(Reagent.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setReagentID(java.lang.String, int, int)
     */
    public void setReagentID(String id, int screenIndex, int reagentIndex)
    {
        checkDuplicateLSID(Reagent.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.SCREEN_INDEX, screenIndex);
        indexes.put(Index.REAGENT_INDEX, reagentIndex);
        IObjectContainer o = getIObjectContainer(Reagent.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Reagent.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setReagentAnnotationRef(java.lang.String, int, int, int)
     */
    public void setReagentAnnotationRef(String annotation, int screenIndex,
            int reagentIndex, int annotationRefIndex)
    {
        LSID key = new LSID(Reagent.class, screenIndex, reagentIndex);
        addReference(key, new LSID(annotation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setReagentDescription(java.lang.String, int, int)
     */
    public void setReagentDescription(String description, int screenIndex,
            int reagentIndex)
    {
        Reagent o = getReagent(screenIndex, reagentIndex);
        o.setDescription(toRType(description));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setReagentName(java.lang.String, int, int)
     */
    public void setReagentName(String name, int screenIndex, int reagentIndex)
    {
        Reagent o = getReagent(screenIndex, reagentIndex);
        o.setName(toRType(name));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setReagentReagentIdentifier(java.lang.String, int, int)
     */
    public void setReagentReagentIdentifier(String reagentIdentifier,
            int screenIndex, int reagentIndex)
    {
        Reagent o = getReagent(screenIndex, reagentIndex);
        o.setReagentIdentifier(toRType(reagentIdentifier));
    }


    //////// Rectangle /////////

    /**
     * Retrieve the Rectangle object (as a Rect object)
     * @param ROIIndex
     * @param shapeIndex
     * @return
     */
    private Rect getRectangle(int ROIIndex, int shapeIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.ROI_INDEX, ROIIndex);
        indexes.put(Index.SHAPE_INDEX, shapeIndex);
        return getSourceObject(Rect.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleID(java.lang.String, int, int)
     */
    public void setRectangleID(String id, int ROIIndex, int shapeIndex)
    {
        checkDuplicateLSID(Rect.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.ROI_INDEX, ROIIndex);
        indexes.put(Index.SHAPE_INDEX, shapeIndex);
        IObjectContainer o = getIObjectContainer(Rect.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Rect.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleDescription(java.lang.String, int, int)
     */
    public void setRectangleDescription(String description, int ROIIndex,
            int shapeIndex)
    {
//        Rect o = getRectangle(ROIIndex, shapeIndex);
//        o.set???(toRType(description));
        // TODO not in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleFill(java.lang.Integer, int, int)
     */
    public void setRectangleFill(Integer fill, int ROIIndex, int shapeIndex)
    {
//        Rect o = getRectangle(ROIIndex, shapeIndex);
//        o.set???(toRType(fill));
        // TODO which in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleFontSize(java.lang.Integer, int, int)
     */
    public void setRectangleFontSize(NonNegativeInteger fontSize, int ROIIndex,
            int shapeIndex)
    {
        Rect o = getRectangle(ROIIndex, shapeIndex);
        o.setFontSize(toRType(fontSize));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleHeight(java.lang.Double, int, int)
     */
    public void setRectangleHeight(Double height, int ROIIndex, int shapeIndex)
    {
        Rect o = getRectangle(ROIIndex, shapeIndex);
        o.setHeight(toRType(height));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleLabel(java.lang.String, int, int)
     */
    public void setRectangleLabel(String label, int ROIIndex, int shapeIndex)
    {
//        Rect o = getRectangle(ROIIndex, shapeIndex);
//        o.set???(toRType(label));
        // TODO not in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleName(java.lang.String, int, int)
     */
    public void setRectangleName(String name, int ROIIndex, int shapeIndex)
    {
//        Rect o = getRectangle(ROIIndex, shapeIndex);
//        o.set???(toRType(name));
        // TODO not in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleStroke(java.lang.Integer, int, int)
     */
    public void setRectangleStroke(Integer stroke, int ROIIndex, int shapeIndex)
    {
//        Rect o = getRectangle(ROIIndex, shapeIndex);
//        o.set???(toRType(stroke));
        // TODO which in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleStrokeDashArray(java.lang.String, int, int)
     */
    public void setRectangleStrokeDashArray(String strokeDashArray,
            int ROIIndex, int shapeIndex)
    {
        Rect o = getRectangle(ROIIndex, shapeIndex);
        o.setStrokeDashArray(toRType(strokeDashArray));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleStrokeWidth(java.lang.Double, int, int)
     */
    public void setRectangleStrokeWidth(Double strokeWidth, int ROIIndex,
            int shapeIndex)
    {
//        Rect o = getRectangle(ROIIndex, shapeIndex);
//        o.setStrokeWidth(toRType(strokeWidth));
        // TODO incorrect type in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleTheC(java.lang.Integer, int, int)
     */
    public void setRectangleTheC(NonNegativeInteger theC, int ROIIndex, int shapeIndex)
    {
        Rect o = getRectangle(ROIIndex, shapeIndex);
        o.setTheC(toRType(theC));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleTheT(java.lang.Integer, int, int)
     */
    public void setRectangleTheT(NonNegativeInteger theT, int ROIIndex, int shapeIndex)
    {
        Rect o = getRectangle(ROIIndex, shapeIndex);
        o.setTheT(toRType(theT));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleTheZ(java.lang.Integer, int, int)
     */
    public void setRectangleTheZ(NonNegativeInteger theZ, int ROIIndex, int shapeIndex)
    {
        Rect o = getRectangle(ROIIndex, shapeIndex);
        o.setTheZ(toRType(theZ));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleTransform(java.lang.String, int, int)
     */
    public void setRectangleTransform(String transform, int ROIIndex,
            int shapeIndex)
    {
        Rect o = getRectangle(ROIIndex, shapeIndex);
        o.setTransform(toRType(transform));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleWidth(java.lang.Double, int, int)
     */
    public void setRectangleWidth(Double width, int ROIIndex, int shapeIndex)
    {
        Rect o = getRectangle(ROIIndex, shapeIndex);
        o.setWidth(toRType(width));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleX(java.lang.Double, int, int)
     */
    public void setRectangleX(Double x, int ROIIndex, int shapeIndex)
    {
        Rect o = getRectangle(ROIIndex, shapeIndex);
        o.setX(toRType(x));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRectangleY(java.lang.Double, int, int)
     */
    public void setRectangleY(Double y, int ROIIndex, int shapeIndex)
    {
        Rect o = getRectangle(ROIIndex, shapeIndex);
        o.setY(toRType(y));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setRoot(java.lang.Object)
     */
    public void setRoot(Object root)
    {
        // TODO Auto-generated method stub

    }

    //////// Screen /////////

    /**
     * Retrieve Screen
     * @param screenIndex
     * @return
     */
    private Screen getScreen(int screenIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.SCREEN_INDEX, screenIndex);
        return getSourceObject(Screen.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenID(java.lang.String, int)
     */
    public void setScreenID(String id, int screenIndex)
    {
        checkDuplicateLSID(Screen.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.SCREEN_INDEX, screenIndex);
        IObjectContainer o = getIObjectContainer(Screen.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Screen.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenAnnotationRef(java.lang.String, int, int)
     */
    public void setScreenAnnotationRef(String annotation, int screenIndex,
            int annotationRefIndex)
    {
        LSID key = new LSID(Screen.class, screenIndex);
        addReference(key, new LSID(annotation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenDescription(java.lang.String, int)
     */
    public void setScreenDescription(String description, int screenIndex)
    {
        Screen o = getScreen(screenIndex);
        o.setDescription(toRType(description));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenName(java.lang.String, int)
     */
    public void setScreenName(String name, int screenIndex)
    {
        Screen o = getScreen(screenIndex);
        o.setName(toRType(name));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenPlateRef(java.lang.String, int, int)
     */
    public void setScreenPlateRef(String plate, int screenIndex,
            int plateRefIndex)
    {
        LSID key = new LSID(Screen.class, screenIndex);
        addReference(key, new LSID(plate));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenProtocolDescription(java.lang.String, int)
     */
    public void setScreenProtocolDescription(String protocolDescription,
            int screenIndex)
    {
        Screen o = getScreen(screenIndex);
        o.setProtocolDescription(toRType(protocolDescription));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenProtocolIdentifier(java.lang.String, int)
     */
    public void setScreenProtocolIdentifier(String protocolIdentifier,
            int screenIndex)
    {
        Screen o = getScreen(screenIndex);
        o.setProtocolIdentifier(toRType(protocolIdentifier));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenReagentSetDescription(java.lang.String, int)
     */
    public void setScreenReagentSetDescription(String reagentSetDescription,
            int screenIndex)
    {
        Screen o = getScreen(screenIndex);
        o.setReagentSetDescription(toRType(reagentSetDescription));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenReagentSetIdentifier(java.lang.String, int)
     */
    public void setScreenReagentSetIdentifier(String reagentSetIdentifier,
            int screenIndex)
    {
        Screen o = getScreen(screenIndex);
        o.setReagentSetIdentifier(toRType(reagentSetIdentifier));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setScreenType(java.lang.String, int)
     */
    public void setScreenType(String type, int screenIndex)
    {
        Screen o = getScreen(screenIndex);
        o.setType(toRType(type));
    }

    //////// StageLabel /////////

    /**
     * Retrieve StageLabel
     * @param imageIndex
     * @return
     */
    private StageLabel getStageLabel(int imageIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.IMAGE_INDEX, imageIndex);
        return getSourceObject(StageLabel.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setStageLabelName(java.lang.String, int)
     */
    public void setStageLabelName(String name, int imageIndex)
    {
        StageLabel o = getStageLabel(imageIndex);
        o.setName(toRType(name));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setStageLabelX(java.lang.Double, int)
     */
    public void setStageLabelX(Double x, int imageIndex)
    {
        StageLabel o = getStageLabel(imageIndex);
        o.setPositionX(toRType(x));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setStageLabelY(java.lang.Double, int)
     */
    public void setStageLabelY(Double y, int imageIndex)
    {
        StageLabel o = getStageLabel(imageIndex);
        o.setPositionY(toRType(y));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setStageLabelZ(java.lang.Double, int)
     */
    public void setStageLabelZ(Double z, int imageIndex)
    {
        StageLabel o = getStageLabel(imageIndex);
        o.setPositionZ(toRType(z));
    }

    //////// String Annotation /////////

    /**
     * Retrieve CommentAnnotation object
     * @param commentAnnotationIndex
     * @return
     */
    private CommentAnnotation getCommentAnnotation(int commentAnnotationIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.COMMENT_ANNOTATION_INDEX, commentAnnotationIndex);
        return getSourceObject(CommentAnnotation.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setCommentAnnotationID(java.lang.String, int)
     */
    public void setCommentAnnotationID(String id, int commentAnnotationIndex)
    {
        checkDuplicateLSID(CommentAnnotation.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.COMMENT_ANNOTATION_INDEX, commentAnnotationIndex);
        IObjectContainer o = getIObjectContainer(CommentAnnotation.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(CommentAnnotation.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setCommentAnnotationNamespace(java.lang.String, int)
     */
    public void setCommentAnnotationNamespace(String namespace,
            int commentAnnotationIndex)
    {
        CommentAnnotation o = getCommentAnnotation(commentAnnotationIndex);
        o.setNs(toRType(namespace));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setCommentAnnotationValue(java.lang.String, int)
     */
    public void setCommentAnnotationValue(String value, int commentAnnotationIndex)
    {
        CommentAnnotation o = getCommentAnnotation(commentAnnotationIndex);
        o.setTextValue(toRType(value));
    }

    //////// Text /////////

    /**
     * Retrieve the Text object (as a Label object)
     * @param ROIIndex
     * @param shapeIndex
     * @return
     */
    private Label getText(int ROIIndex, int shapeIndex)
    {

        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.ROI_INDEX, ROIIndex);
        indexes.put(Index.SHAPE_INDEX, shapeIndex);
        return getSourceObject(Label.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextID(java.lang.String, int, int)
     */
    public void setTextID(String id, int ROIIndex, int shapeIndex)
    {
        checkDuplicateLSID(Label.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.ROI_INDEX, ROIIndex);
        indexes.put(Index.SHAPE_INDEX, shapeIndex);
        IObjectContainer o = getIObjectContainer(Label.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Label.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextDescription(java.lang.String, int, int)
     */
    public void setTextDescription(String description, int ROIIndex,
            int shapeIndex)
    {
//        Label o = getText(ROIIndex, shapeIndex);
//        o.set???(toRType(description));
     // TODO not in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextFill(java.lang.Integer, int, int)
     */
    public void setTextFill(Integer fill, int ROIIndex, int shapeIndex)
    {
//        Label o = getText(ROIIndex, shapeIndex);
//        o.set???(toRType(fill));
     // TODO which in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextFontSize(java.lang.Integer, int, int)
     */
    public void setTextFontSize(NonNegativeInteger fontSize, int ROIIndex, int shapeIndex)
    {
        Label o = getText(ROIIndex, shapeIndex);
        o.setFontSize(toRType(fontSize));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextLabel(java.lang.String, int, int)
     */
    public void setTextLabel(String label, int ROIIndex, int shapeIndex)
    {
//        Label o = getText(ROIIndex, shapeIndex);
//        o.set???(toRType(label));
        // TODO not in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextName(java.lang.String, int, int)
     */
    public void setTextName(String name, int ROIIndex, int shapeIndex)
    {
//        Label o = getText(ROIIndex, shapeIndex);
//        o.set???(toRType(name));
     // TODO not in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextStroke(java.lang.Integer, int, int)
     */
    public void setTextStroke(Integer stroke, int ROIIndex, int shapeIndex)
    {
//        Label o = getText(ROIIndex, shapeIndex);
//        o.set???(toRType(stroke));
        // TODO not in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextStrokeDashArray(java.lang.String, int, int)
     */
    public void setTextStrokeDashArray(String strokeDashArray, int ROIIndex,
            int shapeIndex)
    {
        Label o = getText(ROIIndex, shapeIndex);
        o.setStrokeDashArray(toRType(strokeDashArray));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextStrokeWidth(java.lang.Double, int, int)
     */
    public void setTextStrokeWidth(Double strokeWidth, int ROIIndex,
            int shapeIndex)
    {
//        Label o = getText(ROIIndex, shapeIndex);
//        o.setStrokeWidth(toRType(strokeWidth));
        // TODO incorrect type in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextTheC(java.lang.Integer, int, int)
     */
    public void setTextTheC(NonNegativeInteger theC, int ROIIndex, int shapeIndex)
    {
        Label o = getText(ROIIndex, shapeIndex);
        o.setTheC(toRType(theC));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextTheT(java.lang.Integer, int, int)
     */
    public void setTextTheT(NonNegativeInteger theT, int ROIIndex, int shapeIndex)
    {
        Label o = getText(ROIIndex, shapeIndex);
        o.setTheT(toRType(theT));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextTheZ(java.lang.Integer, int, int)
     */
    public void setTextTheZ(NonNegativeInteger theZ, int ROIIndex, int shapeIndex)
    {
        Label o = getText(ROIIndex, shapeIndex);
        o.setTheZ(toRType(theZ));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextTransform(java.lang.String, int, int)
     */
    public void setTextTransform(String transform, int ROIIndex, int shapeIndex)
    {
        Label o = getText(ROIIndex, shapeIndex);
        o.setTransform(toRType(transform));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextValue(java.lang.String, int, int)
     */
    public void setTextValue(String value, int ROIIndex, int shapeIndex)
    {
        Label o = getText(ROIIndex, shapeIndex);
        o.setTextValue(toRType(value));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextX(java.lang.Double, int, int)
     */
    public void setTextX(Double x, int ROIIndex, int shapeIndex)
    {
        Label o = getText(ROIIndex, shapeIndex);
        o.setX(toRType(x));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTextY(java.lang.Double, int, int)
     */
    public void setTextY(Double y, int ROIIndex, int shapeIndex)
    {
        Label o = getText(ROIIndex, shapeIndex);
        o.setY(toRType(y));
    }

    //////// TiffData /////////

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTiffDataFirstC(java.lang.Integer, int, int)
     */
    public void setTiffDataFirstC(NonNegativeInteger firstC, int imageIndex,
            int tiffDataIndex)
    {
        // TODO not in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTiffDataFirstT(java.lang.Integer, int, int)
     */
    public void setTiffDataFirstT(NonNegativeInteger firstT, int imageIndex,
            int tiffDataIndex)
    {
        // TODO not in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTiffDataFirstZ(java.lang.Integer, int, int)
     */
    public void setTiffDataFirstZ(NonNegativeInteger firstZ, int imageIndex,
            int tiffDataIndex)
    {
        // TODO not in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTiffDataIFD(java.lang.Integer, int, int)
     */
    public void setTiffDataIFD(NonNegativeInteger ifd, int imageIndex, int tiffDataIndex)
    {
        // TODO not in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTiffDataPlaneCount(java.lang.Integer, int, int)
     */
    public void setTiffDataPlaneCount(NonNegativeInteger planeCount, int imageIndex,
            int tiffDataIndex)
    {
        // TODO not in OMERO Model
    }

    //////// Timestamp /////////

    /**
     * Retrieve TimestampAnnotation
     * @param timestampAnnotationIndex
     * @return
     */
    private TimestampAnnotation getTimestampAnnotation(int timestampAnnotationIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.TIMESTAMP_ANNOTATION_INDEX, timestampAnnotationIndex);
        return getSourceObject(TimestampAnnotation.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTimestampAnnotationID(java.lang.String, int)
     */
    public void setTimestampAnnotationID(String id, int timestampAnnotationIndex)
    {
        checkDuplicateLSID(TimestampAnnotation.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.TIMESTAMP_ANNOTATION_INDEX, timestampAnnotationIndex);
        IObjectContainer o = getIObjectContainer(TimestampAnnotation.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(TimestampAnnotation.class, id, o);       }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTimestampAnnotationNamespace(java.lang.String, int)
     */
    public void setTimestampAnnotationNamespace(String namespace,
            int timestampAnnotationIndex)
    {
        TimestampAnnotation o = getTimestampAnnotation(timestampAnnotationIndex);
        o.setNs(toRType(namespace));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTimestampAnnotationValue(java.lang.String, int)
     */
    public void setTimestampAnnotationValue(String value,
            int timestampAnnotationIndex)
    {
        TimestampAnnotation o = getTimestampAnnotation(timestampAnnotationIndex);
        o.setTimeValue(toRType(timestampFromXmlString(value)));
    }

    //////// TransmittanceRange /////////

    private TransmittanceRange getTransmittanceRange(int instrumentIndex, int filterIndex)
    {
        Filter filter = getFilter(instrumentIndex, filterIndex);
        TransmittanceRange tm = filter.getTransmittanceRange();
        if (tm == null)
        {
            tm = new TransmittanceRangeI();
            filter.setTransmittanceRange(tm);
        }
        return tm;
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTransmittanceRangeCutIn(java.lang.Integer, int, int)
     */
    public void setTransmittanceRangeCutIn(PositiveInteger cutIn, int instrumentIndex,
            int filterIndex)
    {
        TransmittanceRange o = getTransmittanceRange(instrumentIndex, filterIndex);
        o.setCutIn(toRType(cutIn));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTransmittanceRangeCutInTolerance(java.lang.Integer, int, int)
     */
    public void setTransmittanceRangeCutInTolerance(NonNegativeInteger cutInTolerance,
            int instrumentIndex, int filterIndex)
    {
        TransmittanceRange o = getTransmittanceRange(instrumentIndex, filterIndex);
        o.setCutInTolerance(toRType(cutInTolerance));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTransmittanceRangeCutOut(java.lang.Integer, int, int)
     */
    public void setTransmittanceRangeCutOut(PositiveInteger cutOut,
            int instrumentIndex, int filterIndex)
    {
        TransmittanceRange o = getTransmittanceRange(instrumentIndex, filterIndex);
        o.setCutOut(toRType(cutOut));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTransmittanceRangeCutOutTolerance(java.lang.Integer, int, int)
     */
    public void setTransmittanceRangeCutOutTolerance(NonNegativeInteger cutOutTolerance,
            int instrumentIndex, int filterIndex)
    {
        TransmittanceRange o = getTransmittanceRange(instrumentIndex, filterIndex);
        o.setCutOutTolerance(toRType(cutOutTolerance));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTransmittanceRangeTransmittance(ome.xml.model.primitives.PercentFraction, int, int)
     */
    public void setTransmittanceRangeTransmittance(
            PercentFraction transmittance, int instrumentIndex, int filterIndex)
    {
        TransmittanceRange o = getTransmittanceRange(instrumentIndex, filterIndex);
        o.setTransmittance(toRType(transmittance));
    }

    //////// UUID /////////

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setUUID(java.lang.String)
     */
    public void setUUID(String uuid)
    {
        // TODO not in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setUUIDFileName(java.lang.String, int, int)
     */
    public void setUUIDFileName(String fileName, int imageIndex,
            int tiffDataIndex)
    {
        // TODO not in OMERO Model
    }

    public void setUUIDValue(String fileName, int imageIndex,
            int tiffDataIndex)
    {
        // TODO not in OMERO Model
    }

    //////// Well /////////

    /**
     * Retrieve Well
     * @param plateIndex
     * @param wellIndex
     * @return
     */
    private Well getWell(int plateIndex, int wellIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.PLATE_INDEX, plateIndex);
        indexes.put(Index.WELL_INDEX, wellIndex);
        return getSourceObject(Well.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellID(java.lang.String, int, int)
     */
    public void setWellID(String id, int plateIndex, int wellIndex)
    {
        checkDuplicateLSID(Well.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.PLATE_INDEX, plateIndex);
        indexes.put(Index.WELL_INDEX, wellIndex);
        IObjectContainer o = getIObjectContainer(Well.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(Well.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellAnnotationRef(java.lang.String, int, int, int)
     */
    public void setWellAnnotationRef(String annotation, int plateIndex,
            int wellIndex, int annotationRefIndex)
    {
        LSID key = new LSID(Well.class, plateIndex, wellIndex);
        addReference(key, new LSID(annotation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellColor(java.lang.Integer, int, int)
     */
    public void setWellColor(Integer color, int plateIndex, int wellIndex)
    {
        Well o = getWell(plateIndex, wellIndex);
        Color c = new Color(color);
        o.setRed(toRType(c.getRed()));
        o.setGreen(toRType(c.getGreen()));
        o.setBlue(toRType(c.getBlue()));
        o.setAlpha(toRType(c.getAlpha()));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellColumn(ome.xml.model.primitives.NonNegativeInteger, int, int)
     */
    public void setWellColumn(NonNegativeInteger column, int plateIndex,
            int wellIndex)
    {
        Well o = getWell(plateIndex, wellIndex);
        o.setColumn(toRType(column));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellExternalDescription(java.lang.String, int, int)
     */
    public void setWellExternalDescription(String externalDescription,
            int plateIndex, int wellIndex)
    {
        Well o = getWell(plateIndex, wellIndex);
        o.setExternalDescription(toRType(externalDescription));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellExternalIdentifier(java.lang.String, int, int)
     */
    public void setWellExternalIdentifier(String externalIdentifier,
            int plateIndex, int wellIndex)
    {
        Well o = getWell(plateIndex, wellIndex);
        o.setExternalIdentifier(toRType(externalIdentifier));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellReagentRef(java.lang.String, int, int)
     */
    public void setWellReagentRef(String reagent, int plateIndex, int wellIndex)
    {
        LSID key = new LSID(Well.class, plateIndex, wellIndex);
        addReference(key, new LSID(reagent));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellRow(ome.xml.model.primitives.NonNegativeInteger, int, int)
     */
    public void setWellRow(NonNegativeInteger row, int plateIndex, int wellIndex)
    {
        Well o = getWell(plateIndex, wellIndex);
        o.setRow(toRType(row));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellStatus(java.lang.String, int, int)
     */
    public void setWellStatus(String status, int plateIndex, int wellIndex)
    {
        //TODO missing from omero model
    }

    //////// WellSample /////////

    /**
     * Retrieve WellSample
     * @param plateIndex
     * @param wellIndex
     * @param wellSampleIndex
     * @return
     */
    private WellSample getWellSample(int plateIndex, int wellIndex, int wellSampleIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.PLATE_INDEX, plateIndex);
        indexes.put(Index.WELL_INDEX, wellIndex);
        indexes.put(Index.WELL_SAMPLE_INDEX, wellSampleIndex);

        WellSample ws = getSourceObject(WellSample.class, indexes);
        return ws;
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellSampleID(java.lang.String, int, int, int)
     */
    public void setWellSampleID(String id, int plateIndex, int wellIndex,
            int wellSampleIndex)
    {
        checkDuplicateLSID(WellSample.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.PLATE_INDEX, plateIndex);
        indexes.put(Index.WELL_INDEX, wellIndex);
        indexes.put(Index.WELL_SAMPLE_INDEX, wellSampleIndex);
        IObjectContainer o = getIObjectContainer(WellSample.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(WellSample.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellSampleAnnotationRef(java.lang.String, int, int, int, int)
     */
    public void setWellSampleAnnotationRef(String annotation, int plateIndex,
            int wellIndex, int wellSampleIndex, int annotationRefIndex)
    {
        LSID key = new LSID(WellSample.class, plateIndex, wellIndex, wellSampleIndex);
        addReference(key, new LSID(annotation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellSampleImageRef(java.lang.String, int, int, int)
     */
    public void setWellSampleImageRef(String image, int plateIndex,
            int wellIndex, int wellSampleIndex)
    {
        LSID key = new LSID(WellSample.class, plateIndex, wellIndex, wellSampleIndex);
        addReference(key, new LSID(image));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellSampleIndex(ome.xml.model.primitives.NonNegativeInteger, int, int, int)
     */
    public void setWellSampleIndex(NonNegativeInteger index, int plateIndex,
            int wellIndex, int wellSampleIndex)
    {
        // TODO not in OMERO Model
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellSamplePositionX(java.lang.Double, int, int, int)
     */
    public void setWellSamplePositionX(Double positionX, int plateIndex,
            int wellIndex, int wellSampleIndex)
    {
        WellSample o = getWellSample(plateIndex, wellIndex, wellSampleIndex);
        o.setPosX(toRType(positionX));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellSamplePositionY(java.lang.Double, int, int, int)
     */
    public void setWellSamplePositionY(Double positionY, int plateIndex,
            int wellIndex, int wellSampleIndex)
    {
        WellSample o = getWellSample(plateIndex, wellIndex, wellSampleIndex);
        o.setPosY(toRType(positionY));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setWellSampleTimepoint(java.lang.Integer, int, int, int)
     */
    public void setWellSampleTimepoint(String timepoint, int plateIndex,
            int wellIndex, int wellSampleIndex)
    {
        if (timepoint == null)
        {
            return;
        }
        WellSample o =
            getWellSample(plateIndex, wellIndex, wellSampleIndex);
        o.setTimepoint(toRType(timestampFromXmlString(timepoint)));
    }

    //////// XMLAnnotation /////////

    /**
     * Retrieve XMLAnnotation
     * @param XMLAnnotationIndex
     * @return
     */

    private XmlAnnotation getXMLAnnotation(int XMLAnnotationIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.XML_ANNOTATION_INDEX, XMLAnnotationIndex);
        return getSourceObject(XmlAnnotation.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setXMLAnnotationID(java.lang.String, int)
     */
    public void setXMLAnnotationID(String id, int XMLAnnotationIndex)
    {
        checkDuplicateLSID(XmlAnnotation.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.XML_ANNOTATION_INDEX, XMLAnnotationIndex);
        IObjectContainer o = getIObjectContainer(XmlAnnotation.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(XmlAnnotation.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setXMLAnnotationNamespace(java.lang.String, int)
     */
    public void setXMLAnnotationNamespace(String namespace,
            int XMLAnnotationIndex)
    {
        XmlAnnotation o = getXMLAnnotation(XMLAnnotationIndex);
        o.setNs(toRType(namespace));
   }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setXMLAnnotationValue(java.lang.String, int)
     */
    public void setXMLAnnotationValue(String value, int XMLAnnotationIndex)
    {
        XmlAnnotation o = getXMLAnnotation(XMLAnnotationIndex);
        o.setTextValue(toRType(value));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setBooleanAnnotationAnnotationRef(java.lang.String, int, int)
     */
    public void setBooleanAnnotationAnnotationRef(String annotation,
            int booleanAnnotationIndex, int annotationRefIndex)
    {
        LSID key = new LSID(BooleanAnnotation.class, booleanAnnotationIndex);
        addReference(key, new LSID(annotation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setBooleanAnnotationDescription(java.lang.String, int)
     */
    public void setBooleanAnnotationDescription(String description,
            int booleanAnnotationIndex)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setCommentAnnotationAnnotationRef(java.lang.String, int, int)
     */
    public void setCommentAnnotationAnnotationRef(String annotation,
            int commentAnnotationIndex, int annotationRefIndex)
    {
        LSID key = new LSID(CommentAnnotation.class, commentAnnotationIndex);
        addReference(key, new LSID(annotation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setCommentAnnotationDescription(java.lang.String, int)
     */
    public void setCommentAnnotationDescription(String description,
            int commentAnnotationIndex)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDoubleAnnotationAnnotationRef(java.lang.String, int, int)
     */
    public void setDoubleAnnotationAnnotationRef(String annotation,
            int doubleAnnotationIndex, int annotationRefIndex)
    {
        LSID key = new LSID(DoubleAnnotation.class, doubleAnnotationIndex);
        addReference(key, new LSID(annotation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setDoubleAnnotationDescription(java.lang.String, int)
     */
    public void setDoubleAnnotationDescription(String description,
            int doubleAnnotationIndex)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFileAnnotationAnnotationRef(java.lang.String, int, int)
     */
    public void setFileAnnotationAnnotationRef(String annotation,
            int fileAnnotationIndex, int annotationRefIndex)
    {
        LSID key = new LSID(FileAnnotation.class, fileAnnotationIndex);
        addReference(key, new LSID(annotation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setFileAnnotationDescription(java.lang.String, int)
     */
    public void setFileAnnotationDescription(String description,
            int fileAnnotationIndex)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setListAnnotationDescription(java.lang.String, int)
     */
    public void setListAnnotationDescription(String description,
            int listAnnotationIndex)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLongAnnotationAnnotationRef(java.lang.String, int, int)
     */
    public void setLongAnnotationAnnotationRef(String annotation,
            int longAnnotationIndex, int annotationRefIndex)
    {
        LSID key = new LSID(LongAnnotation.class, longAnnotationIndex);
        addReference(key, new LSID(annotation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setLongAnnotationDescription(java.lang.String, int)
     */
    public void setLongAnnotationDescription(String description,
            int longAnnotationIndex)
    {
        // TODO Auto-generated method stub

    }

    /**
     * Retrieve TagAnnotation object
     * @param tagAnnotationIndex
     * @return
     */
    private TagAnnotation getTagAnnotation(int tagAnnotationIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.TAG_ANNOTATION_INDEX, tagAnnotationIndex);
        return getSourceObject(TagAnnotation.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTagAnnotationAnnotationRef(java.lang.String, int, int)
     */
    public void setTagAnnotationAnnotationRef(String annotation,
            int tagAnnotationIndex, int annotationRefIndex)
    {
        LSID key = new LSID(TagAnnotation.class, tagAnnotationIndex);
        addReference(key, new LSID(annotation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTagAnnotationDescription(java.lang.String, int)
     */
    public void setTagAnnotationDescription(String description,
            int tagAnnotationIndex)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTagAnnotationID(java.lang.String, int)
     */
    public void setTagAnnotationID(String id, int tagAnnotationIndex)
    {
        checkDuplicateLSID(TagAnnotation.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.TAG_ANNOTATION_INDEX, tagAnnotationIndex);
        IObjectContainer o = getIObjectContainer(TagAnnotation.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(TagAnnotation.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTagAnnotationNamespace(java.lang.String, int)
     */
    public void setTagAnnotationNamespace(String namespace,
            int tagAnnotationIndex)
    {
        TagAnnotation o = getTagAnnotation(tagAnnotationIndex);
        o.setNs(toRType(namespace));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTagAnnotationValue(java.lang.String, int)
     */
    public void setTagAnnotationValue(String value, int tagAnnotationIndex)
    {
        TagAnnotation o = getTagAnnotation(tagAnnotationIndex);
        o.setTextValue(toRType(value));
    }

    /**
     * Retrieve TermAnnotation object
     * @param termAnnotationIndex
     * @return
     */
    private TermAnnotation getTermAnnotation(int termAnnotationIndex)
    {
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.TERM_ANNOTATION_INDEX, termAnnotationIndex);
        return getSourceObject(TermAnnotation.class, indexes);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTermAnnotationAnnotationRef(java.lang.String, int, int)
     */
    public void setTermAnnotationAnnotationRef(String annotation,
            int termAnnotationIndex, int annotationRefIndex)
    {
        LSID key = new LSID(TermAnnotation.class, termAnnotationIndex);
        addReference(key, new LSID(annotation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTermAnnotationDescription(java.lang.String, int)
     */
    public void setTermAnnotationDescription(String description,
            int termAnnotationIndex)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTermAnnotationID(java.lang.String, int)
     */
    public void setTermAnnotationID(String id, int termAnnotationIndex)
    {
        checkDuplicateLSID(TermAnnotation.class, id);
        LinkedHashMap<Index, Integer> indexes =
            new LinkedHashMap<Index, Integer>();
        indexes.put(Index.TERM_ANNOTATION_INDEX, termAnnotationIndex);
        IObjectContainer o = getIObjectContainer(TermAnnotation.class, indexes);
        o.LSID = id;
        addAuthoritativeContainer(TermAnnotation.class, id, o);
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTermAnnotationNamespace(java.lang.String, int)
     */
    public void setTermAnnotationNamespace(String namespace,
            int termAnnotationIndex)
    {
        TermAnnotation o = getTermAnnotation(termAnnotationIndex);
        o.setNs(toRType(namespace));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTermAnnotationValue(java.lang.String, int)
     */
    public void setTermAnnotationValue(String value, int termAnnotationIndex)
    {
        TermAnnotation o = getTermAnnotation(termAnnotationIndex);
        o.setTermValue(toRType(value));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTimestampAnnotationAnnotationRef(java.lang.String, int, int)
     */
    public void setTimestampAnnotationAnnotationRef(String annotation,
            int timestampAnnotationIndex, int annotationRefIndex)
    {
        LSID key = new LSID(TimestampAnnotation.class, timestampAnnotationIndex);
        addReference(key, new LSID(annotation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setTimestampAnnotationDescription(java.lang.String, int)
     */
    public void setTimestampAnnotationDescription(String description,
            int timestampAnnotationIndex)
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setXMLAnnotationAnnotationRef(java.lang.String, int, int)
     */
    public void setXMLAnnotationAnnotationRef(String annotation,
            int XMLAnnotationIndex, int annotationRefIndex)
    {
        LSID key = new LSID(XmlAnnotation.class, XMLAnnotationIndex);
        addReference(key, new LSID(annotation));
    }

    /* (non-Javadoc)
     * @see loci.formats.meta.MetadataStore#setXMLAnnotationDescription(java.lang.String, int)
     */
    public void setXMLAnnotationDescription(String description,
            int XMLAnnotationIndex)
    {
        // TODO Auto-generated method stub

    }

}
