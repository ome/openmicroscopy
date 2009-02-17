/*
 * org.openmicroscopy.shoola.env.data.OMEROGateway
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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

package org.openmicroscopy.shoola.env.data;


//Java imports
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;

import javax.swing.filechooser.FileSystemView;

//Third-party libraries

//Application-internal dependencies
import monitors.EventType;
import monitors.MonitorClientPrx;
import monitors.MonitorServerPrx;
import monitors.PathMode;
import org.openmicroscopy.shoola.env.data.model.EnumerationObject;
import org.openmicroscopy.shoola.env.data.util.PojoMapper;
import org.openmicroscopy.shoola.env.data.util.SearchDataContext;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import Ice.Communicator;
import Ice.ObjectAdapter;
import Ice.ObjectPrx;
import ome.formats.OMEROMetadataStore;
import ome.system.UpgradeCheck;
import omero.AuthenticationException;
import omero.ExpiredCredentialException;
import omero.InternalException;
import omero.RString;
import omero.RType;
import omero.SecurityViolation;
import omero.ServerError;
import omero.SessionException;
import omero.client;
import omero.api.IAdminPrx;
import omero.api.IContainerPrx;
import omero.api.IDeletePrx;
import omero.api.IMetadataPrx;
import omero.api.IPixelsPrx;
import omero.api.IProjectionPrx;
import omero.api.IQueryPrx;
import omero.api.IRenderingSettingsPrx;
import omero.api.IRepositoryInfoPrx;
import omero.api.ISessionPrx;
import omero.api.IUpdatePrx;
import omero.api.RawFileStorePrx;
import omero.api.RawPixelsStorePrx;
import omero.api.RenderingEnginePrx;
import omero.api.SearchPrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceInterfacePrx;
import omero.api.ThumbnailStorePrx;
import omero.constants.projection.ProjectionType;
import omero.model.Annotation;
import omero.model.AnnotationAnnotationLink;
import omero.model.BooleanAnnotation;
import omero.model.BooleanAnnotationI;
import omero.model.CommentAnnotation;
import omero.model.CommentAnnotationI;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.Details;
import omero.model.DetailsI;
import omero.model.Experimenter;
import omero.model.ExperimenterGroup;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Format;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.LogicalChannel;
import omero.model.LongAnnotation;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.Pixels;
import omero.model.PixelsI;
import omero.model.PixelsType;
import omero.model.Plate;
import omero.model.PlateI;
import omero.model.Project;
import omero.model.ProjectI;
import omero.model.RenderingDef;
import omero.model.Screen;
import omero.model.ScreenI;
import omero.model.ScreenPlateLink;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.TimestampAnnotation;
import omero.model.TimestampAnnotationI;
import omero.model.UriAnnotation;
import omero.model.UriAnnotationI;
import omero.model.Well;
import omero.model.WellSample;
import omero.sys.EventContext;
import omero.sys.Parameters;
import omero.sys.ParametersI;
import omero.sys.PojoOptions;
import omero.util.PojoOptionsI;
import pojos.ArchivedAnnotationData;
import pojos.BooleanAnnotationData;
import pojos.ChannelAcquisitionData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.ImageAcquisitionData;
import pojos.ImageData;
import pojos.LongAnnotationData;
import pojos.PixelsData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.RatingAnnotationData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;
import pojos.TimeAnnotationData;
import pojos.URLAnnotationData;
import pojos.WellData;
import pojos.WellSampleData;


/** 
* Unified access point to the various <i>OMERO</i> services.
*
* @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
*              <a href="mailto:a.falconi@dundee.ac.uk">
*                  a.falconi@dundee.ac.uk</a>
* @version 2.2
* <small>
* (<b>Internal version:</b> $Revision$ $Date$)
* </small>
* @since OME2.2
*/
class OMEROGateway
{

	/** Maximum size of pixels read at once. */
	private static final int				INC = 262144;//256000;
	
	/** 
	 * The maximum number of thumbnails retrieved before restarting the
	 * thumbnails service.
	 */
	private static final int				MAX_RETRIEVAL = 100;
	
	/** The collection of escaping characters we allow in the search. */
	private static final List<Character>	SUPPORTED_SPECIAL_CHAR;
	
	/** The collection of escaping characters we allow in the search. */
	private static final List<String>		WILD_CARDS;
	
	static {
		SUPPORTED_SPECIAL_CHAR = new ArrayList<Character>();
		SUPPORTED_SPECIAL_CHAR.add(new Character('-'));
		SUPPORTED_SPECIAL_CHAR.add(new Character('+'));
		SUPPORTED_SPECIAL_CHAR.add(new Character('['));
		SUPPORTED_SPECIAL_CHAR.add(new Character(']'));
		SUPPORTED_SPECIAL_CHAR.add(new Character(')'));
		SUPPORTED_SPECIAL_CHAR.add(new Character('('));
		SUPPORTED_SPECIAL_CHAR.add(new Character(':'));
		SUPPORTED_SPECIAL_CHAR.add(new Character('|'));
		SUPPORTED_SPECIAL_CHAR.add(new Character('!'));
		SUPPORTED_SPECIAL_CHAR.add(new Character('{'));
		SUPPORTED_SPECIAL_CHAR.add(new Character('}'));
		WILD_CARDS = new ArrayList<String>();
		WILD_CARDS.add("*");
		WILD_CARDS.add("?");
		WILD_CARDS.add("~");
	}
	
	/**
	 * The number of thumbnails already retrieved. Resets to <code>0</code>
	 * when the value equals {@link #MAX_RETRIEVAL}.
	 */
	private int										thumbRetrieval;
	
	/**
	 * The entry point provided by the connection library to access the various
	 * <i>OMERO</i> services.
	 */
	private ServiceFactoryPrx 						entry;

	/** The thumbnail service. */
	private ThumbnailStorePrx						thumbnailService;

	/** The raw file store. */
	private RawFileStorePrx							fileStore;
	
	/** The raw pixels store. */
	private RawPixelsStorePrx						pixelsStore;

	/** The projection service. */
	private IProjectionPrx							projService;
	
	/** The search stateful service. */
	private SearchPrx								searchService;
	
	/** The Admin service. */
	private IAdminPrx								adminService;
	
	/** The query service. */
	private IQueryPrx								queryService;
	
	/** The rendering settings service. */
	private IRenderingSettingsPrx					rndSettingsService;
	
	/** The repository service. */
	private IRepositoryInfoPrx						repInfoService;
	
	/** The delete service. */
	private IDeletePrx								deleteService;
	
	/** The pixels service. */
	private IPixelsPrx								pixelsService;
	
	/** The container service. */
	private IContainerPrx							pojosService;
	
	/** The update service. */
	private IUpdatePrx								updateService;
	
	/** The update service. */
	private IMetadataPrx							metadataService;
	
	/** Tells whether we're currently connected and logged into <i>OMERO</i>. */
	private boolean                 				connected;

	/** 
	 * Used whenever a broken link is detected to get the Login Service and
	 * try reestabishing a valid link to <i>OMERO</i>. 
	 */
	private DataServicesFactory     				dsFactory;

	/** The compression level. */
	private float									compression;
	
	/** The port to connect. */
	private int										port;
	
	/** The port to connect. */
	private String									hostName;
	
	/** 
	 * The Blitz client object, this is the entry point to the 
	 * OMERO.Blitz Server. 
	 */
	private client 									blitzClient;

	/** Map hosting the enumeration required for metadata. */
	private Map<String, List<EnumerationObject>>	enumerations;
	
	/** Collection of services to keep alive. */
	private List<ServiceInterfacePrx>				services;
	
	/** Collection of services to keep alive. */
	private Map<Long, ServiceInterfacePrx>			reServices;
	
	//fs Testing stuff
	/** The sole system view instance. */
	private FileSystemView							systemView;
	
	//tmp
	private static MonitorServerPrx					monitorPrx;
	
	/** Collection of monitors to end if any.*/
	private List<String>							monitorIDs;
	
	private OMEROMetadataStore						metadataStore;
	//
	
	/**
	 * Helper method to handle exceptions thrown by the connection library.
	 * Methods in this class are required to fill in a meaningful context
	 * message.
	 * This method is not supposed to be used in this class' constructor or in
	 * the login/logout methods.
	 *  
	 * @param t     	The exception.
	 * @param message	The context message.    
	 * @throws DSOutOfServiceException  A connection problem.
	 * @throws DSAccessException    A server-side error.
	 */
	private void handleException(Throwable t, String message) 
		throws DSOutOfServiceException, DSAccessException
	{
		Throwable cause = t.getCause();
		if (cause instanceof SecurityViolation) {
			String s = "For security reasons, cannot access data. \n"; 
			throw new DSAccessException(s+message, t);
		} else if (cause instanceof SessionException) {
			String s = "Session is not valid. \n"; 
			throw new DSOutOfServiceException(s+message, t);
		} else if (cause instanceof AuthenticationException) {
			String s = "Cannot initialize the session. \n"; 
			throw new DSOutOfServiceException(s+message, t);
		} else if (cause instanceof ExpiredCredentialException) {
			String s = "Cannot initialize the session. \n"; 
			throw new DSOutOfServiceException(s+message, t);
		}
		throw new DSAccessException("Cannot access data. \n"+message, t);
	}
	
	/**
	 * Utility method to print the error message
	 * 
	 * @param e The exception to handle.
	 * @return  See above.
	 */
	private String printErrorText(Throwable e) 
	{
		if (e == null) return "";
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		return sw.toString();
	}

	/**
	 * Determines the table name corresponding to the specified class.
	 * 
	 * @param klass The class to analyze.
	 * @return See above.
	 */
	private String getTableForLink(Class klass)
	{
		String table = null;
		if (Dataset.class.equals(klass)) table = "DatasetImageLink";
		else if (DatasetI.class.equals(klass)) table = "DatasetImageLink";
		else if (Project.class.equals(klass)) table = "ProjectDatasetLink";
		else if (ProjectI.class.equals(klass)) table = "ProjectDatasetLink";
		else if (Screen.class.equals(klass)) table = "ScreenPlateLink";
		else if (ScreenI.class.equals(klass)) table = "ScreenPlateLink";
		else if (TagAnnotation.class.equals(klass)) 
			table = "AnnotationAnnotationLink";
		else if (TagAnnotationI.class.equals(klass)) 
			table = "AnnotationAnnotationLink";
		return table;
	}

	/**
	 * Determines the table name corresponding to the specified class.
	 * 
	 * @param klass The class to analyze.
	 * @return See above.
	 */
	private String getTableForAnnotationLink(String klass)
	{
		String table = null;
		if (klass == null) return table;
		if (klass.equals(Dataset.class.getName())) 
			table = "DatasetAnnotationLink";
		else if (klass.equals(Project.class.getName())) 
			table = "ProjectAnnotationLink";
		else if (klass.equals(Image.class.getName())) 
			table = "ImageAnnotationLink";
		else if (klass.equals(Pixels.class.getName()))
			table = "PixelAnnotationLink";
		else if (klass.equals(Annotation.class.getName()))
			table = "AnnotationAnnotationLink";
		else if (klass.equals(DatasetData.class.getName())) 
			table = "DatasetAnnotationLink";
		else if (klass.equals(ProjectData.class.getName())) 
			table = "ProjectAnnotationLink";
		else if (klass.equals(ImageData.class.getName())) 
			table = "ImageAnnotationLink";
		else if (klass.equals(PixelsData.class.getName())) 
			table = "PixelAnnotationLink";
		else if (klass.equals(Screen.class.getName())) table = 
			"ScreenAnnotationLink";
		else if (klass.equals(Plate.class.getName())) 
			table = "PlateAnnotationLink";
		else if (klass.equals(ScreenData.class.getName())) 
			table = "ScreenAnnotationLink";
		else if (klass.equals(PlateData.class.getName())) 
			table = "PlateAnnotationLink";
		else if (klass.equals(DatasetI.class.getName())) 
			table = "DatasetAnnotationLink";
		else if (klass.equals(ProjectI.class.getName())) 
			table = "ProjectAnnotationLink";
		else if (klass.equals(ImageI.class.getName())) 
			table = "ImageAnnotationLink";
		else if (klass.equals(PixelsI.class.getName()))
			table = "PixelAnnotationLink";
		else if (klass.equals(ScreenI.class.getName())) 
			table = "ScreenAnnotationLink";
		else if (klass.equals(PlateI.class.getName())) 
			table = "PlateAnnotationLink";
		else if (klass.equals(ScreenData.class.getName())) 
			table = "ScreenAnnotationLink";
		else if (klass.equals(PlateData.class.getName())) 
			table = "PlateAnnotationLink";
		return table;
	}
	
	/**
	 * Determines the table name corresponding to the specified class.
	 * 
	 * @param klass The class to analyze.
	 * @return See above.
	 */
	private String getTableForClass(Class klass)
	{
		if (DatasetData.class.equals(klass)) return "Dataset";
		else if (ProjectData.class.equals(klass)) return "Project";
		else if (ImageData.class.equals(klass)) return "Image";
		else if (ScreenData.class.equals(klass)) return "Screen";
		else if (PlateData.class.equals(klass)) return "Plate";
		return null;
	}
	
	/**
	 * Transforms the specified <code>property</code> into the 
	 * corresponding server value.
	 * The transformation depends on the specified class.
	 * 
	 * @param nodeType The type of node this property corresponds to.
	 * @param property The name of the property.
	 * @return See above.
	 */
	private String convertProperty(Class nodeType, String property)
	{
		if (nodeType.equals(DatasetData.class)) {
			if (property.equals(OmeroDataService.IMAGES_PROPERTY))
				return DatasetData.IMAGE_LINKS;
		}  else throw new IllegalArgumentException("NodeType or " +
				"property not supported");
		return null;
	}
	
	/**
	 * Returns the {@link ISessionPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	public ISessionPrx getSessionService()
		throws DSAccessException, DSOutOfServiceException
	{
		try {
			return entry.getSessionService();
		} catch (Throwable e) {
			handleException(e, "Cannot access Session service.");
		}
		return null;
	}
	
	/**
	 * Returns the {@link IRenderingSettingsPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IRenderingSettingsPrx getRenderingSettingsService()
		throws DSAccessException, DSOutOfServiceException
	{
		try {
			if (rndSettingsService == null) {
				rndSettingsService = entry.getRenderingSettingsService(); 
				services.add(rndSettingsService);
			}
			return rndSettingsService;
		} catch (Throwable e) {
			handleException(e, "Cannot access RenderingSettings service.");
		}
		return null;
	}

	/**
	 * Returns the {@link IRepositoryInfoPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IRepositoryInfoPrx getRepositoryService()
		throws DSAccessException, DSOutOfServiceException
	{
		try {
			if (repInfoService == null) {
				repInfoService = entry.getRepositoryInfoService(); 
				services.add(repInfoService);
			}
			return repInfoService;
		} catch (Throwable e) {
			handleException(e, "Cannot access RepositoryInfo service.");
		}
		return null;
	}

	/**
	 * Returns the {@link IPojosPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IContainerPrx getPojosService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			if (pojosService == null) {
				pojosService = entry.getContainerService();
				services.add(pojosService);
			}
			return pojosService; 
		} catch (Throwable e) {
			handleException(e, "Cannot access Pojos service.");
		}
		return null;
	}

	/**
	 * Returns the {@link IQueryPrx} service.
	 *  
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IQueryPrx getQueryService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			if (queryService == null) {
				queryService = entry.getQueryService(); 
				services.add(queryService);
			}
			return queryService; 
		} catch (Throwable e) {
			handleException(e, "Cannot access Query service.");
		}
		return null;
	}
	
	/**
	 * Returns the {@link IUpdatePrx} service.
	 *  
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IUpdatePrx getUpdateService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			if (updateService == null) {
				updateService = entry.getUpdateService();
				services.add(updateService);
			}
			return updateService; 
		} catch (Throwable e) {
			handleException(e, "Cannot access Update service.");
		}
		return null;
	}

	/**
	 * Returns the {@link IMetadataPrx} service.
	 *  
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IMetadataPrx getMetadataService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			if (metadataService == null) {
				metadataService = entry.getMetadataService();
				services.add(metadataService);
			}
			return metadataService; 
		} catch (Throwable e) {
			handleException(e, "Cannot access Update service.");
		}
		return null;
	}
	
	/**
	 * Returns the {@link IAdminPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IAdminPrx getAdminService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			if (adminService == null) {
				adminService = entry.getAdminService(); 
				services.add(adminService);
			}
			return adminService; 
		} catch (Throwable e) {
			handleException(e, "Cannot access Admin service.");
		}
		return null;
	}
	
	/**
	 * Returns the {@link IDeletePrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IDeletePrx getDeleteService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			if (deleteService == null) {
				deleteService = entry.getDeleteService(); 
				services.add(deleteService);
			}
			return deleteService;
		} catch (Throwable e) {
			handleException(e, "Cannot access Delete service.");
		}
		return null;
	}
	
	/**
	 * Returns the {@link ThumbnailStorePrx} service.
	 *   
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	private ThumbnailStorePrx getThumbService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			if (thumbRetrieval == MAX_RETRIEVAL) {
				thumbRetrieval = 0;
				//to be on the save side
				if (thumbnailService != null) thumbnailService.close();
				services.remove(thumbnailService);
				thumbnailService = null;
			}
			if (thumbnailService == null) {
				thumbnailService = entry.createThumbnailStore();
				services.add(thumbnailService);
			}
			thumbRetrieval++;
			return thumbnailService; 
		} catch (Throwable e) {
			handleException(e, "Cannot access Thumbnail service.");
		}
		return null;
	}

	/**
	 * Returns the {@link RawFileStorePrx} service.
	 *  
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	private RawFileStorePrx getRawFileService()
		throws DSAccessException, DSOutOfServiceException
	{
		try {
			if (fileStore != null) {
				services.remove(fileStore);
				try {
					fileStore.close();
				} catch (Exception e) {}
			}
			fileStore = entry.createRawFileStore();
			services.add(fileStore);
			return fileStore;
		} catch (Throwable e) {
			handleException(e, "Cannot access RawFileStore service.");
		}
		return null;
	}

	/**
	 * Returns the {@link RenderingEnginePrx Rendering service}.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	private RenderingEnginePrx getRenderingService()
		throws DSAccessException, DSOutOfServiceException
	{
		try {
			RenderingEnginePrx engine = entry.createRenderingEngine();
			engine.setCompressionLevel(compression);
			return engine;
		} catch (Throwable e) {
			handleException(e, "Cannot access RawFileStore service.");
		}
		return null;
	}

	/**
	 * Returns the {@link RawPixelsStorePrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	private RawPixelsStorePrx getPixelsStore()
		throws DSAccessException, DSOutOfServiceException
	{
		try {
			if (pixelsStore != null) {
				services.remove(pixelsStore);
				try {
					pixelsStore.close();
				} catch (Exception e) {}
			}
			pixelsStore = entry.createRawPixelsStore();
			services.add(pixelsStore);
			return pixelsStore;
		} catch (Throwable e) {
			handleException(e, "Cannot access RawPixelsStore service.");
		}
		return null;
	}

	/**
	 * Returns the {@link IPixelsPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IPixelsPrx getPixelsService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			if (pixelsService == null) {
				pixelsService = entry.getPixelsService(); 
				services.add(pixelsService);
			}
			return pixelsService;
		} catch (Throwable e) {
			handleException(e, "Cannot access Pixels service.");
		}
		return null;
	}
	
	/**
	 * Returns the {@link SearchPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service.
	 */
	private SearchPrx getSearchService()
		throws DSAccessException, DSOutOfServiceException
	{
		try {
			if (searchService == null) {
				searchService = entry.createSearchService(); 
				services.add(searchService);
			}
		} catch (Throwable e) {
			handleException(e, "Cannot access Search service.");
		}
		return searchService;
	}
	
	/**
	 * Returns the {@link IProjectionPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service.
	 */
	private IProjectionPrx getProjectionService()
		throws DSAccessException, DSOutOfServiceException
	{
		try {
			if (projService == null) {
				projService = entry.getProjectionService(); 
				services.add(projService);
			}
			return projService;
		} catch (Throwable e) {
			handleException(e, "Cannot access Pixels service.");
		}
		return null;
	}
	
	/**
	 * Returns the ice communicator.
	 * 
	 * @return See above.
	 */
	private Communicator getIceCommunicator()
	{
		return entry.ice_getCommunicator();
	}
	
	/**
	 * Checks if some default rendering settings have to be created
	 * for the specified set of pixels.
	 * 
	 * @param pixelsID	The pixels ID.
	 * @param re		The rendering engine to load.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service.
	 */
	private synchronized void needDefault(long pixelsID, RenderingEnginePrx re)
		throws DSAccessException, DSOutOfServiceException
	{
		try {
			if (re == null) {
				ThumbnailStorePrx service = getThumbService();
				if (!(service.setPixelsId(pixelsID))) {
					service.resetDefaults();
					service.setPixelsId(pixelsID);
				}
			} else {
				if (!(re.lookupRenderingDef(pixelsID))) {
					//re.resetDefaultsNoSave();
					re.resetDefaults();
					re.lookupRenderingDef(pixelsID);
				}
			}
		} catch (Throwable e) {
			handleException(e, "Cannot set RE defaults.");
		}
		
	}

	/**
	 * Returns <code>true</code> if the passed group is an experimenter group
	 * internal to OMERO, <code>false</code> otherwise.
	 * 
	 * @param group The experimenter group to handle.
	 * @return See above.
	 */
	private boolean isSystemGroup(ExperimenterGroup group)
	{
		String n = group.getName() == null ? null : group.getName().getValue();
		return ("system".equals(n) || "user".equals(n) || "default".equals(n) ||
				"guest".equals(n));
	}

	/**
	 * Returns a collection of users contained in the specified group.
	 * 
	 * @param groupID	The id of the group.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service.
	 */
	private List<Experimenter> containedExperimenters(long groupID)
		throws DSAccessException, DSOutOfServiceException
	{
		IQueryPrx service = getQueryService();
		List<Experimenter> exps = new ArrayList<Experimenter>();
		Parameters p = new ParametersI();
		p.map = new HashMap<String, RType>();
		p.map.put("id", omero.rtypes.rlong(groupID));
		try {
			List<IObject> rv = service.findAllByQuery(
					"select e from Experimenter e "
			        + "left outer join fetch e.groupExperimenterMap m "
			         + "left outer join fetch m.parent g where g.id = :id",
			                p);
			
			for (IObject obj : rv) 
				exps.add((Experimenter) obj);
		} catch (Throwable e) {
			handleException(e, "Cannot retrieve experimenters.");
		}
		return exps;
	}
	
	/**
	 * Formats the terms to search for.
	 * 
	 * @param terms		The terms to search for.
	 * @param service	The search service.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service.
	 */
	private List<String> prepareTextSearch(String[] terms, SearchPrx service) 
		throws DSAccessException, DSOutOfServiceException
	{
		if (terms == null || terms.length == 0) return null;
		String value;
		int n;
		char[] arr;
		String v;
		List<String> formattedTerms = new ArrayList<String>(terms.length);
		String formatted;
		try {
			for (int j = 0; j < terms.length; j++) {
				value = terms[j];
				if (startWithWildCard(value)) 
					service.setAllowLeadingWildcard(true);
				//format string
				n = value.length();
				arr = new char[n];
				v = "";
				value.getChars(0, n, arr, 0);  
				for (int i = 0; i < arr.length; i++) {
					if (SUPPORTED_SPECIAL_CHAR.contains(arr[i])) 
						v += "\\"+arr[i];
					else v += arr[i];
				}
				if (value.contains(" ")) 
					formatted = "\""+v.toLowerCase()+"\"";
				else formatted = v.toLowerCase();
				formattedTerms.add(formatted);
			}
		} catch (Throwable e) {
			handleException(e, "Cannot format text for search.");
		}
		return formattedTerms;
	}
	
	/**
	 * Formats the terms to search for.
	 * 
	 * @param terms		The terms to search for.
	 * @param service	The search service.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service.
	 */
	private List<String> prepareTextSearch(Collection<String> terms, 
			SearchPrx service) 
		throws DSAccessException, DSOutOfServiceException
	{
		if (terms == null || terms.size() == 0) return null;
		if (terms == null || terms.size() == 0) return null;
		String[] values = new String[terms.size()];
		Iterator<String> i = terms.iterator();
		int index = 0;
		while (i.hasNext()) {
			values[index] = i.next();
			index++;
		}
		return prepareTextSearch(values, service);
	}

	/**
	 * Returns <code>true</code> if the specified value starts with a wild card,
	 * <code>false</code> otherwise.
	 * 
	 * @param value The value to handle.
	 * @return See above.
	 */
	private boolean startWithWildCard(String value)
	{
		if (value == null) return false;
		Iterator<String> i = WILD_CARDS.iterator();
		while (i.hasNext()) 
			if (value.startsWith(i.next())) return true;
		
		return false;
	}

	/**
	 * Converts the class to the specified model string.
	 * 
	 * @param pojo The class to convert.
	 * @return See above.
	 */
	private String convertAnnotation(Class pojo)
	{
		if (TextualAnnotationData.class.equals(pojo))
			return "ome.model.annotations.CommentAnnotation";
		else if (TagAnnotationData.class.equals(pojo))
			return "ome.model.annotations.TagAnnotation";
		else if (RatingAnnotationData.class.equals(pojo))
			return "ome.model.annotations.LongAnnotation";
		else if (LongAnnotationData.class.equals(pojo))
			return "ome.model.annotations.LongAnnotation";
		else if (FileAnnotationData.class.equals(pojo))
			return "ome.model.annotations.FileAnnotation"; 
		else if (URLAnnotationData.class.equals(pojo))
			return "ome.model.annotations.UriAnnotation"; 
		else if (TimeAnnotationData.class.equals(pojo))
			return "ome.model.annotations.UriAnnotation"; 
		else if (BooleanAnnotationData.class.equals(pojo))
			return "ome.model.annotations.UriAnnotation"; 
		return null;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param port			The port used to connect.
	 * @param dsFactory 	A reference to the factory. Used whenever a broken 
	 * 						link is detected to get the Login Service and try 
	 *                  	reestablishing a valid link to <i>OMEDS</i>.
	 *                  	Mustn't be <code>null</code>.
	 */
	OMEROGateway(int port, DataServicesFactory dsFactory)
	{
		if (dsFactory == null) 
			throw new IllegalArgumentException("No Data service factory.");
		this.dsFactory = dsFactory;
		this.port = port;
		thumbRetrieval = 0;
		enumerations = new HashMap<String, List<EnumerationObject>>();
		services = new ArrayList<ServiceInterfacePrx>();
		reServices = new HashMap<Long, ServiceInterfacePrx>();
	}
	
	/**
	 * Sets the port value.
	 * 
	 * @param port The value to set.
	 */
	void setPort(int port)
	{
		if (this.port != port) this.port = port;
	}
	
	/**
	 * Converts the specified POJO into the corresponding model.
	 *  
	 * @param nodeType The POJO class.
	 * @return The corresponding class.
	 */
	Class convertPojos(Class nodeType)
	{
		if (ProjectData.class.equals(nodeType)) 
			return Project.class;
		else if (DatasetData.class.equals(nodeType)) 
			return Dataset.class;
		else if (ImageData.class.equals(nodeType)) 
			return Image.class;
		else if (BooleanAnnotationData.class.equals(nodeType) ||
				ArchivedAnnotationData.class.equals(nodeType))
			return BooleanAnnotation.class;
		else if (RatingAnnotationData.class.equals(nodeType) ||
				LongAnnotationData.class.equals(nodeType)) 
			return LongAnnotation.class;
		else if (TagAnnotationData.class.equals(nodeType)) 
			return TagAnnotation.class;
		else if (TextualAnnotationData.class.equals(nodeType)) 
			return CommentAnnotation.class;
		else if (FileAnnotationData.class.equals(nodeType))
			return FileAnnotation.class;
		else if (URLAnnotationData.class.equals(nodeType))
			return UriAnnotation.class;
		else if (ScreenData.class.equals(nodeType)) 
			return Screen.class;
		else if (PlateData.class.equals(nodeType)) 
			return Plate.class;
		else if (WellData.class.equals(nodeType)) 
			return Well.class;
		else if (WellSampleData.class.equals(nodeType)) 
			return WellSample.class;
		throw new IllegalArgumentException("NodeType not supported");
	}
	
	/**
	 * Converts the specified type to its corresponding type for search.
	 * 
	 * @param nodeType The type to convert.
	 * @return See above.
	 */
	private String convertTypeForSearch(Class nodeType)
	{
		if (nodeType.equals(Image.class))
			return ImageI.class.getName();
		else if (nodeType.equals(TagAnnotation.class) ||
				nodeType.equals(TagAnnotationData.class))
			return TagAnnotationI.class.getName();
		else if (nodeType.equals(BooleanAnnotation.class) ||
				nodeType.equals(BooleanAnnotationData.class))
			return BooleanAnnotationI.class.getName();
		else if (nodeType.equals(UriAnnotation.class) ||
				nodeType.equals(URLAnnotationData.class))
			return UriAnnotationI.class.getName();
		else if (nodeType.equals(FileAnnotation.class) ||
				nodeType.equals(FileAnnotationData.class))
			return FileAnnotationI.class.getName();
		else if (nodeType.equals(CommentAnnotation.class) ||
				nodeType.equals(TextualAnnotationData.class))
			return CommentAnnotationI.class.getName();
		else if (nodeType.equals(TimestampAnnotation.class) ||
				nodeType.equals(TimeAnnotationData.class))
			return TimestampAnnotationI.class.getName();
		throw new IllegalArgumentException("type not supported");
	}
	
	/**
	 * Tells whether the communication channel to <i>OMERO</i> is currently
	 * connected.
	 * This means that we have established a connection and have sucessfully
	 * logged in.
	 * 
	 * @return  <code>true</code> if connected, <code>false</code> otherwise.
	 */
	boolean isConnected() { return connected; }

	/**
	 * Retrieves the details on the current user and maps the result calling
	 * {@link PojoMapper#asDataObjects(Map)}.
	 * 
	 * @param name  The user's name.
	 * @return The {@link ExperimenterData} of the current user.
	 * @throws DSOutOfServiceException If the connection is broken, or
	 * logged in.
	 * @see IPojosPrx#getUserDetails(Set, Map)
	 */
	ExperimenterData getUserDetails(String name)
		throws DSOutOfServiceException
	{
		try {
			IAdminPrx service = getAdminService();
			return (ExperimenterData) 
				PojoMapper.asDataObject(service.lookupExperimenter(name));
		} catch (Exception e) {
			throw new DSOutOfServiceException("Cannot retrieve user's data " +
					printErrorText(e), e);
		}
	}

	/**
	 * Returns <code>true</code> if an upgrade is required, <code>false</code>
	 * otherwise.
	 * 
	 * @return See above.
	 */
	boolean isUpgradeRequired()
	{
		ResourceBundle bundle = ResourceBundle.getBundle("omero");
	    String version = bundle.getString("omero.version");
	    String url = bundle.getString("omero.upgrades.url");
	    UpgradeCheck check = new UpgradeCheck(url, version, "insight"); 
	    check.run();
	    return check.isUpgradeNeeded();
	    
		//return false;
	}
	
	/**
	 * Tries to connect to <i>OMERO</i> and log in by using the supplied
	 * credentials.
	 * 
	 * @param userName  		The user name to be used for login.
	 * @param password  		The password to be used for login.
	 * @param hostName  		The name of the server.
	 * @param compressionLevel  The compression level used for images and 
	 * 							thumbnails depending on the connection speed.
	 * @return The user's details.
	 * @throws DSOutOfServiceException If the connection can't be established
	 *                                  or the credentials are invalid.
	 * @see #getUserDetails(String)
	 */
	ExperimenterData login(String userName, String password, String hostName,
							float compressionLevel)
		throws DSOutOfServiceException
	{
		try {
			//TMP
			
			compression = compressionLevel;
			this.hostName = hostName;
			if (port > 0) blitzClient = new client(hostName, port);
			else blitzClient = new client(hostName);
			entry = blitzClient.createSession(userName, password);
			blitzClient.getProperties().setProperty("Ice.Override.Timeout", ""+5000);
			//metadataStore = new OMEROMetadataStore(entry);
			connected = true;
			//fillEnumerations();
			return getUserDetails(userName);
		} catch (Throwable e) {
			e.printStackTrace();
			connected = false;
			String s = "Can't connect to OMERO. OMERO info not valid.\n\n";
			s += printErrorText(e);
			throw new DSOutOfServiceException(s, e);  
		} 
	}
	
	void startFS(Properties fsConfig)
	{
		monitorIDs = new ArrayList<String>();
		ObjectPrx base = getIceCommunicator().stringToProxy(
				fsConfig.getProperty("omerofs.MonitorServer"));
		monitorPrx = monitors.MonitorServerPrxHelper.uncheckedCast(
				base.ice_twoway());
		Iterator i = fsConfig.keySet().iterator();
		String key;
		while (i.hasNext()) {
			key = (String) i.next();
			if (!("omerofs.MonitorServer".equals(key)))
				blitzClient.getProperties().setProperty(key, 
						fsConfig.getProperty(key));
		}
	}
	
	/** 
	 * Tries to reconnect to the server.
	 * 
	 * @param userName	The user name to be used for login.
	 * @param password	The password to be used for login.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 */
	void reconnect(String userName, String password)
		throws DSOutOfServiceException
	{
		try {
			logout();
			thumbnailService = null;
			thumbRetrieval = 0;
			fileStore = null;
			if (port > 0) blitzClient = new client(hostName, port);
			else blitzClient = new client(hostName);
			entry = blitzClient.createSession(userName, password);
			metadataStore = new OMEROMetadataStore(entry);
			connected = true;
		} catch (Throwable e) {
			connected = false;
			String s = "Can't connect to OMERO. OMERO info not valid.\n\n";
			s += printErrorText(e);
			throw new DSOutOfServiceException(s, e);  
		} 
	}

	/** Logs out. */
	void logout()
	{
		connected = false;
		try {
			Iterator<String> i = monitorIDs.iterator();
			String id;
			while (i.hasNext()) {
				id = i.next();
				monitorPrx.stopMonitor(id);
				monitorPrx.destroyMonitor(id);
			}
			monitorIDs.clear();
			if (thumbnailService != null) thumbnailService.close();
			if (fileStore != null) fileStore.close();
			thumbnailService = null;
			fileStore = null;
			entry.destroy();
			blitzClient.closeSession();
			entry = null;
			blitzClient = null;
			metadataStore = null;
			pojosService = null;
			projService = null;
			searchService = null;
			adminService = null;
			queryService = null;
			rndSettingsService = null;
			repInfoService = null;
			deleteService = null;
			pixelsService = null;
			services.clear();
			reServices.clear();
		} catch (Exception e) {
			//session already dead.
		}
		return;
	}

	/**
	 * Retrieves hierarchy trees rooted by a given node.
	 * i.e. the requested node as root and all of its descendants.
	 * The annotation for the current user is also linked to the object.
	 * Annotations are currently possible only for Image and Dataset.
	 * Wraps the call to the 
	 * {@link IPojos#loadContainerHierarchy(Class, List, Map)}
	 * and maps the result calling {@link PojoMapper#asDataObjects(Set)}.
	 * 
	 * @param rootType  The top-most type which will be searched for 
	 *                  Can be <code>Project</code>. 
	 *                  Mustn't be <code>null</code>.
	 * @param rootIDs   A set of the IDs of top-most containers. 
	 *                  Passed <code>null</code> to retrieve all container
	 *                  of the type specified by the rootNodetype parameter.
	 * @param options   The Options to retrieve the data.
	 * @return  A set of hierarchy trees.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#loadContainerHierarchy(Class, List, Map)
	 */
	Set loadContainerHierarchy(Class rootType, List rootIDs, Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IContainerPrx service = getPojosService();
			return PojoMapper.asDataObjects(service.loadContainerHierarchy(
				convertPojos(rootType).getName(), rootIDs, options));
		} catch (Throwable t) {
			t.printStackTrace();
			handleException(t, "Cannot load hierarchy for " + rootType+".");
		}
		return new HashSet();
	}

	/**
	 * Retrieves hierarchy trees in various hierarchies that
	 * contain the specified Images.
	 * The annotation for the current user is also linked to the object.
	 * Annotations are currently possible only for Image and Dataset.
	 * Wraps the call to the 
	 * {@link IPojos#findContainerHierarchies(Class, List, Map)}
	 * and maps the result calling {@link PojoMapper#asDataObjects(Set)}.
	 * 
	 * @param rootNodeType  top-most type which will be searched for 
	 *                      Can be <code>Project</code> or
	 *                      <code>CategoryGroup</code>. 
	 *                      Mustn't be <code>null</code>.
	 * @param leavesIDs     Set of ids of the Images that sit at the bottom of
	 *                      the trees. Mustn't be <code>null</code>.
	 * @param options Options to retrieve the data.
	 * @return A <code>Set</code> with all root nodes that were found.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#findContainerHierarchies(Class, List, Map)
	 */
	Set findContainerHierarchy(Class rootNodeType, List leavesIDs, Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IContainerPrx service = getPojosService();
			return PojoMapper.asDataObjects(service.findContainerHierarchies(
					convertPojos(rootNodeType).getName(), leavesIDs, options));
		} catch (Throwable t) {
			handleException(t, "Cannot find hierarchy for "+rootNodeType+".");
		}
		return new HashSet();
	}
	
	/**
	 * Loads all the annotations that have been attached to the specified
	 * <code>rootNodes</code>. This method looks for all the <i>valid</i>
	 * annotations that have been attached to each of the specified objects. It
	 * then maps each <code>rootNodeID</code> onto the set of all annotations
	 * that were found for that node. If no annotations were found for that
	 * node, then the entry will be <code>null</code>. Otherwise it will be a
	 * <code>Set</code> containing <code>Annotation</code> objects.
	 * Wraps the call to the 
	 * {@link IMetadataPrx#loadAnnotations(String, List, List, List)}
	 * and maps the result calling {@link PojoMapper#asDataObjects(Map)}.
	 * 
	 * @param nodeType      The type of the rootNodes.
	 *                      Mustn't be <code>null</code>. 
	 * @param nodeIDs       TheIds of the objects of type
	 *                      <code>rootNodeType</code>. 
	 *                      Mustn't be <code>null</code>.
	 * @param annotationTypes The collection of annotations to retrieve or 
	 * 						  passed an empty list if we retrieve all the 
	 * 						  annotations. 
	 * @param annotatorIDs  The Ids of the users for whom annotations should be 
	 *                      retrieved. If <code>null</code>, all annotations 
	 *                      are returned.
	 * @param options       Options to retrieve the data.
	 * @return A map whose key is rootNodeID and value the <code>Set</code> of
	 *         all annotations for that node or <code>null</code>.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#findAnnotations(Class, List, List, Map)
	 */
	Map loadAnnotations(Class nodeType, List nodeIDs, 
			List<Class> annotationTypes, List annotatorIDs, Map options)
	throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		List<String> types = new ArrayList<String>();
		if (annotationTypes != null && annotationTypes.size() > 0) {
			types = new ArrayList<String>(annotationTypes.size());
			Iterator<Class> i = annotationTypes.iterator();
			String k;
			while (i.hasNext()) {
				k = convertAnnotation(i.next());
				if (k != null)
					types.add(k);
			}
		}
		try {
			IMetadataPrx service = getMetadataService();
			return PojoMapper.asDataObjects(
					service.loadAnnotations(convertPojos(nodeType).getName(), 
							nodeIDs, types, annotatorIDs, options));
		} catch (Throwable t) {
			handleException(t, "Cannot find annotations for "+nodeType+".");
		}
		return new HashMap();
	}
	
	/**
	 * Finds the links if any between the specified parent and child.
	 * 
	 * @param type    The type of parent to handle.
	 * @param userID  The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	Collection findAllAnnotations(Class type, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQueryPrx service = getQueryService();
			String table = getTableForAnnotationLink(type.getName());
			if (table == null) return null;
			String sql = "select link from "+table+" as link";
			sql +=" left outer join link.child as child";
			Parameters p = new ParametersI();
			p.map = new HashMap<String, RType>();
			p.map.put("uid", omero.rtypes.rlong(userID));
			sql += " where link.details.owner.id = :uid";
			return service.findAllByQuery(sql, p);
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the requested link for "+
					"userID: "+userID);
		}
		return null;
	}
	
	/**
	 * Retrieves the images contained in containers specified by the 
	 * node type.
	 * Wraps the call to the {@link IPojos#getImages(Class, List, Map)}
	 * and maps the result calling {@link PojoMapper#asDataObjects(Set)}.
	 * 
	 * @param nodeType  The type of container. Can be either Project, Dataset,
	 *                  CategoryGroup, Category.
	 * @param nodeIDs   Set of containers' IDS.
	 * @param options   Options to retrieve the data.
	 * @return A <code>Set</code> of retrieved images.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#getImages(Class, List, Map)
	 */
	Set getContainerImages(Class nodeType, List nodeIDs, Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IContainerPrx service = getPojosService();
			return PojoMapper.asDataObjects(service.getImages(
					convertPojos(nodeType).getName(), nodeIDs, options));
		} catch (Throwable t) {
			handleException(t, "Cannot find images for "+nodeType+".");
		}
		return new HashSet();
	}

	/**
	 * Retrieves the images imported by the current user.
	 * Wraps the call to the {@link IPojos#getUserImages(Map)}
	 * and maps the result calling {@link PojoMapper#asDataObjects(Set)}.
	 * 
	 * @param options   Options to retrieve the data.
	 * @return A <code>Set</code> of retrieved images.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#getUserImages(Map)
	 */
	Set getUserImages(Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IContainerPrx service = getPojosService();
			return PojoMapper.asDataObjects(service.getUserImages(options));
		} catch (Throwable t) {
			handleException(t, "Cannot find user images.");
		}
		return new HashSet();
	}

	/**
	 * Counts the number of items in a collection for a given object.
	 * Returns a map which key is the passed rootNodeID and the value is 
	 * the number of items contained in this object and
	 * maps the result calling {@link PojoMapper#asDataObjects(Map)}.
	 * 
	 * @param rootNodeType 	The type of container. Can either be Dataset 
	 * 						and Category.
	 * @param property		One of the properties defined by this class.
	 * @param ids           The ids of the objects.
	 * @param options		Options to retrieve the data.		
	 * @param rootNodeIDs	Set of root node IDs.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#getCollectionCount(String, String, List, Map)
	 */
	Map getCollectionCount(Class rootNodeType, String property, List ids,
			Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			if (TagAnnotationData.class.equals(rootNodeType)) {
				return getMetadataService().getTaggedObjectsCount(ids, options);
			}
			IContainerPrx service = getPojosService();
			String p = convertProperty(rootNodeType, property);
			if (p == null) return null;
			return PojoMapper.asDataObjects(service.getCollectionCount(
					convertPojos(rootNodeType).getName(), p, ids, options));
		} catch (Throwable t) {
			handleException(t, "Cannot count the collection.");
		}
		return new HashMap();
	}
	
	/**
	 * Creates the speficied object.
	 * 
	 * @param object    The object to create.
	 * @param options   Options to create the data.  
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#createDataObject(IObject, Map)
	 */
	IObject createObject(IObject object, Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			isSessionAlive();
			return saveAndReturnObject(object, options);
		} catch (Throwable t) {
			t.printStackTrace();
			handleException(t, "Cannot update the object.");
		}
		return null;
	}

	/**
	 * Creates the speficied objects.
	 * 
	 * @param objects   The objects to create.
	 * @param options   Options to create the data.  
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#createDataObjects(IObject[], Map)
	 */
	List<IObject> createObjects(List<IObject> objects, Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			return saveAndReturnObject(objects, options);
		} catch (Throwable t) {
			handleException(t, "Cannot create the objects.");
		}
		return null;
	}

	/**
	 * Deletes the specified object.
	 * 
	 * @param object    The object to delete.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 * @see IUpdate#deleteObject(IObject)
	 */
	void deleteObject(IObject object)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IUpdatePrx service = getUpdateService();
			service.deleteObject(object);
		} catch (Throwable t) {
			handleException(t, "Cannot delete the object.");
		}
	}

	/**
	 * Deletes the specified objects.
	 * 
	 * @param objects                  The objects to delete.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException       If an error occured while trying to 
	 *                                 retrieve data from OMERO service. 
	 * @see IUpdate#deleteObject(IObject) 
	 */
	void deleteObjects(List<IObject> objects)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IUpdatePrx service = getUpdateService();
			Iterator<IObject> i = objects.iterator();
			//TODO: need method
			while (i.hasNext()) 
				service.deleteObject(i.next());
			
		} catch (Throwable t) {
			handleException(t, "Cannot delete the object.");
		}
	}

	/**
	 * Updates the specified object.
	 * 
	 * @param object    The objet to update.
	 * @param options   Options to update the data.   
	 * @return          The updated object.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#updateDataObject(IObject, Map)
	 */
	IObject saveAndReturnObject(IObject object, Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IUpdatePrx service = getUpdateService();
			IObject r;
			if (options == null) r = service.saveAndReturnObject(object);
			r = service.saveAndReturnObject(object, options);
			return r;//findIObject(r);
		} catch (Throwable t) {
			t.printStackTrace();
			handleException(t, "Cannot update the object.");
		}
		return null;
	}
	
	/**
	 * Updates the specified object.
	 * 
	 * @param objects   The objets to update.
	 * @param options   Options to update the data.   
	 * @return          The updated object.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#updateDataObject(IObject, Map)
	 */
	List<IObject> saveAndReturnObject(List<IObject> objects, Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IUpdatePrx service = getUpdateService();
			return service.saveAndReturnArray(objects);
		} catch (Throwable t) {
			handleException(t, "Cannot update the object.");
		}
		return null;
	}
	
	/**
	 * Updates the specified object.
	 * 
	 * @param object    The objet to update.
	 * @param options   Options to update the data.   
	 * @return          The updated object.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#updateDataObject(IObject, Map)
	 */
	IObject updateObject(IObject object, Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IContainerPrx service = getPojosService();
			IObject r = service.updateDataObject(object, options);
			return findIObject(r);
		} catch (Throwable t) {
			handleException(t, "Cannot update the object.");
		}
		return null;
	}

	/**
	 * Updates the specified <code>IObject</code>s and returned the 
	 * updated <code>IObject</code>s.
	 * 
	 * @param objects   The array of objects to update.
	 * @param options   Options to update the data.   
	 * @return  See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service.
	 * @see IPojos#updateDataObjects(IObject[], Map) 
	 */
	List<IObject> updateObjects(List<IObject> objects, Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IContainerPrx service = getPojosService();
			List<IObject> l = service.updateDataObjects(objects, options);
			if (l == null) return l;
			Iterator<IObject> i = l.iterator();
			List<IObject> r = new ArrayList<IObject>(l.size());
			while (i.hasNext()) 
				r.add(findIObject(i.next()));

			return r;
			//return service.updateDataObjects(objects, options);
		} catch (Throwable t) {
			handleException(t, "Cannot update the object.");
		}
		return null;
	}

	/**
	 * Retrieves the dimensions in microns of the specified pixels set.
	 * 
	 * @param pixelsID  The pixels set ID.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	Pixels getPixels(long pixelsID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IPixelsPrx service = getPixelsService();
			return service.retrievePixDescription(pixelsID);
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the pixels set of "+
			"the pixels set.");
		}
		return null;
	}
	
	/**
	 * Retrieves the thumbnail for the passed set of pixels.
	 * 
	 * @param pixelsID  The id of the pixels set the thumbnail is for.
	 * @param sizeX     The size of the thumbnail along the X-axis.
	 * @param sizeY     The size of the thumbnail along the Y-axis.
	 * @param userID	The id of the user the thumbnail is for.
	 * @return See above.
	 * @throws RenderingServiceException If an error occured while trying to 
	 *              retrieve data from the service. 
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	synchronized byte[] getThumbnail(long pixelsID, int sizeX, int sizeY, 
									long userID)
		throws RenderingServiceException, DSOutOfServiceException
	{
		isSessionAlive();
		try {
			ThumbnailStorePrx service = getThumbService();
			
			needDefault(pixelsID, null);
			//getRendering Def for a given pixels set.
			if (userID >= 0) {
				RenderingDef def = getRenderingDef(pixelsID, userID);
				if (def != null) service.setRenderingDefId(
						def.getId().getValue());
			}
			return service.getThumbnail(omero.rtypes.rint(sizeX), 
					omero.rtypes.rint(sizeY));
		} catch (Throwable t) {
			if (thumbnailService != null) {
				try {
					thumbnailService.close();
				} catch (Exception e) {
					//nothing we can do
				}
			}
			thumbnailService = null;
			if (t instanceof ServerError) {
				throw new DSOutOfServiceException(
						"Thumbnail service null for pixelsID: "+pixelsID, t);
			}
			throw new RenderingServiceException("Cannot get thumbnail", t);
		}
	}

	/**
	 * Retrieves the thumbnail for the passed set of pixels.
	 * 
	 * @param pixelsID	The id of the pixels set the thumbnail is for.
	 * @param maxLength	The maximum length of the thumbnail width or heigth
	 * 					depending on the pixel size.
	 * @return See above.
	 * @throws RenderingServiceException If an error occured while trying to 
	 *              retrieve data from the service. 
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	synchronized byte[] getThumbnailByLongestSide(long pixelsID, int maxLength)
		throws RenderingServiceException, DSOutOfServiceException
	{
		isSessionAlive();
		try {
			ThumbnailStorePrx service = getThumbService();
			needDefault(pixelsID, null);
			return service.getThumbnailByLongestSide(
					omero.rtypes.rint(maxLength));
		} catch (Throwable t) {
			if (thumbnailService != null) {
				try {
					thumbnailService.close();
				} catch (Exception e) {
					//nothing we can do
				}
			}
			thumbnailService = null;
			if (t instanceof ServerError) {
				throw new DSOutOfServiceException(
						"Thumbnail service null for pixelsID: "+pixelsID, t);
			}
			throw new RenderingServiceException("Cannot get thumbnail", t);
		}
	}

	/**
	 * Retrieves the thumbnail for the passed collection of pixels set.
	 * 
	 * @param pixelsID	The collection of pixels set.
	 * @param maxLength	The maximum length of the thumbnail width or heigth
	 * 					depending on the pixel size.
	 * @return See above.
	 * @throws RenderingServiceException If an error occured while trying to 
	 *              retrieve data from the service. 
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	synchronized Map getThumbnailSet(List<Long> pixelsID, int maxLength)
		throws RenderingServiceException, DSOutOfServiceException
	{
		isSessionAlive();
		try {
			ThumbnailStorePrx service = getThumbService();
			return service.getThumbnailByLongestSideSet(
					omero.rtypes.rint(maxLength), pixelsID);
		} catch (Throwable t) {
			if (thumbnailService != null) {
				try {
					thumbnailService.close();
				} catch (Exception e) {
					//nothing we can do
				}
			}
			thumbnailService = null;
			if (t instanceof ServerError) {
				throw new DSOutOfServiceException(
						"Thumbnail service null for pixelsID: "+pixelsID, t);
			}
			throw new RenderingServiceException("Cannot get thumbnail", t);
		}
	}
	
	/**
	 * Creates a new rendering service for the specified pixels set.
	 * 
	 * @param pixelsID  The pixels set ID.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	synchronized RenderingEnginePrx createRenderingEngine(long pixelsID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			RenderingEnginePrx service = getRenderingService();
			reServices.put(pixelsID,  service);
			service.lookupPixels(pixelsID);
			needDefault(pixelsID, service);
			service.load();
			return service;
		} catch (Throwable t) {
			handleException(t, "Cannot start the Rendering Engine.");
		}
		return null;
	}

	/**
	 * Finds the link if any between the specified parent and child.
	 * 
	 * @param type    
	 * @param parentID
	 * @param childID   The id of the child, or <code>-1</code> if no 
	 * 					child specified.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	IObject findAnnotationLink(Class type, long parentID, long childID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQueryPrx service = getQueryService();
			String table = getTableForAnnotationLink(type.getName());
			if (table == null) return null;
			String sql = "select link from "+table+" as link where " +
			"link.parent.id = :parentID"; 
			Parameters p = new ParametersI();
			p.map = new HashMap<String, RType>();
			p.map.put("parentID", omero.rtypes.rlong(parentID));
			if (childID >= 0) {
				sql += " and link.child.id = :childID";
				p.map.put("childID", omero.rtypes.rlong(childID));
			}

			return service.findByQuery(sql, p);
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the requested link for "+
					"parent ID: "+parentID+" and child " +
					"ID: "+childID);
		}
		return null;
	}
	
	/**
	 * Finds the link if any between the specified parent and child.
	 * 
	 * @param parentType    The type of parent to handle.
	 * @param parentID		The id of the parent to handle.
	 * @param children     	Collection of the ids.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	List findAnnotationLinks(String parentType, long parentID, 
								List<Long> children)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQueryPrx service = getQueryService();
			String table = getTableForAnnotationLink(parentType);
			if (table == null) return null;
			String sql = "select link from "+table+" as link";
			sql +=" left outer join link.child as child";
			sql +=" left outer join link.parent as parent";
			ParametersI p = new ParametersI();
			if (parentID > 0) {
				sql += " where link.parent.id = :parentID";
				if (children != null && children.size() > 0) {
					sql += " and link.child.id in (:childIDs)";
					p.addLongs("childIDs", children);
				}
				p.map.put("parentID", omero.rtypes.rlong(parentID));
			} else {
				if (children != null && children.size() > 0) {
					sql += " where link.child.id in (:childIDs)";
					p.addLongs("childIDs", children);
				}
			}
			return service.findAllByQuery(sql, p);
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the annotation links for "+
					"parent ID: "+parentID);
		}
		return null;
	}		
	
	/**
	 * Finds the link if any between the specified parent and child.
	 * 
	 * @param parent    The parent.
	 * @param child     The child.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	IObject findLink(IObject parent, IObject child)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			String table = getTableForLink(parent.getClass());
			if (table == null) return null;
			String sql = "select link from "+table+" as link where " +
			"link.parent.id = :parentID and link.child.id = :childID";

			ParametersI param = new ParametersI();
			param.map = new HashMap<String, RType>();
			param.map.put("parentID", parent.getId());
			param.map.put("childID", child.getId());

			return getQueryService().findByQuery(sql, param);
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the requested link for "+
					"parent ID: "+parent.getId()+" and child " +
					"ID: "+child.getId());
		}
		return null;
	}

	/**
	 * Finds the links if any between the specified parent and children.
	 * 
	 * @param parent    The parent.
	 * @param children  Collection of children as children ids.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	List findLinks(IObject parent, List children)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			String table = getTableForLink(parent.getClass());
			if (table == null) return null;

			ParametersI param = new ParametersI();
			param.map.put("parentID", parent.getId());

			String sql = "select link from "+table+" as link where " +
			"link.parent.id = :parentID"; 
			if (children != null && children.size() > 0) {
				sql += " and link.child.id in (:childIDs)";
				param.addLongs("childIDs", children);

			}

			return getQueryService().findAllByQuery(sql, param);
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the requested link for "+
					"parent ID: "+parent.getId());
		}
		return null;
	}

	/**
	 * Finds the links if any between the specified parent and children.
	 * 
	 * @param parentClass	The parent.
	 * @param children  	Collection of children as children ids.
	 * @param userID		The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	List findLinks(Class parentClass, List children, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			String table = getTableForLink(parentClass);
			if (table == null) return null;
			String sql = "select link from "+table+" as link where " +
			"link.child.id in (:childIDs)";
			ParametersI param = new ParametersI();
			param.addLongs("childIDs", children);

			if (userID >= 0) {
				sql += " and link.details.owner.id = :userID";
				param.map.put("userID", omero.rtypes.rlong(userID));
			}
			
			return getQueryService().findAllByQuery(sql, param);
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the requested link for "+
			"the specified children");
		}
		return null;
	}

	/**
	 * Finds the links if any between the specified parent and children.
	 * 
	 * @param parentClass   The parent.
	 * @param childID  		The id of the child.
	 * @param userID		The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	List findLinks(Class parentClass, long childID, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			String table = getTableForLink(parentClass);
			if (table == null) return null;
			ParametersI param = new ParametersI();
			param.map.put("childID", omero.rtypes.rlong(childID));
			StringBuffer sb = new StringBuffer();
			sb.append("select link from "+table+" as link ");
			sb.append("left outer join fetch link.child as child ");
			sb.append("left outer join fetch link.parent parent ");
			sb.append("where link.child.id = :id");
			if (childID >= 0) {
				param.addId(childID);
			}
			
			if (userID >= 0) {
				sb.append(" and link.details.owner.id = :userID");
				param.map.put("userID", omero.rtypes.rlong(userID));
			}
			return getQueryService().findAllByQuery(sb.toString(), param);
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the requested link for "+
					"child ID: "+childID);
		}
		return null;
	}

	/**
	 * Retrieves an updated version of the specified object.
	 * 
	 * @param o	The object to retrieve.
	 * @return The last version of the object.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	IObject findIObject(IObject o)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			return getQueryService().find(o.getClass().getName(), 
									o.getId().getValue());
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the requested object with "+
					"object ID: "+o.getId());
		}
		return null;
	} 

	/**
	 * Retrieves an updated version of the specified object.
	 * 
	 * @param klassName	The type of object to retrieve.
	 * @param id 		The object's id.
	 * @return The last version of the object.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	IObject findIObject(String klassName, long id)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			return getQueryService().find(klassName, id);
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the requested object with "+
					"object ID: "+id);
		}
		return null;
	} 
	
	/**
	 * Retrieves the groups visible by the current experimenter.
	 * 
	 * @param loggedInUser The user currently logged in.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	Map<GroupData, Set> getAvailableGroups(ExperimenterData loggedInUser)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IAdminPrx service = getAdminService();
			//Need method server side.
			List<ExperimenterGroup> groups = service.lookupGroups();
			Iterator i = groups.iterator();
			ExperimenterGroup group;
			//Experimenter[] experimenters;
			List<Experimenter> experimenters;
			Map<GroupData, Set> pojos = new HashMap<GroupData, Set>();
			DataObject pojoGroup;
			//
			List<GroupData> l = loggedInUser.getGroups();
			Iterator<GroupData> k = l.iterator();
			List<Long> groupIds = new ArrayList<Long>();
			while (k.hasNext()) {
				groupIds.add(k.next().getId());
			}
			long gpId;
			while (i.hasNext()) {
				group = (ExperimenterGroup) i.next();
				gpId = group.getId().getValue();
				if (!isSystemGroup(group) && groupIds.contains(gpId)) {
					pojoGroup = PojoMapper.asDataObject(group);
					experimenters = containedExperimenters(gpId);
					pojos.put((GroupData) pojoGroup, 
							PojoMapper.asDataObjects(experimenters));
				}
			}
			return pojos;
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the available groups ");
		}
		return null;
	}

	/**
	 * Returns <code>true</code> if the imported set of pixels has 
	 * been archived, <code>false</code> otherwise.
	 * 
	 * @param pixelsID The id of the pixels set.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	boolean hasArchivedFiles(long pixelsID)
		throws DSAccessException, DSOutOfServiceException
	{
		List<Long> ids = new ArrayList<Long>();
		ids.add(pixelsID);
		isSessionAlive();
		try {
			IContainerPrx service = getPojosService();
			Map m = service.findAnnotations(Pixels.class.getName(), ids, null, 
									(new PojoOptions().map()));
			Collection c = (Collection) m.get(pixelsID);
			if (c == null || c.size() == 0) return false;
			Iterator i = c.iterator();
			Annotation data;
			while (i.hasNext()) {
				data = (Annotation) i.next();
				if (data instanceof BooleanAnnotation) {
					BooleanAnnotation ann = (BooleanAnnotation) data;
					RString nameSpace = ann.getNs();
					String ns = null;
					if (nameSpace != null) ns = nameSpace.getValue();
					if (ArchivedAnnotationData.IMPORTER_ARCHIVED_NS.equals(ns))
						return ann.getBoolValue().getValue();
				}
			}
		} catch (Exception e) {
			handleException(e, "Cannot retrieve the annotations related " +
								"to "+pixelsID);
		}
		
		return false;
	}
	
	/**
	 * 
	 * Retrieves the archived files if any for the specified set of pixels.
	 * 
	 * @param path		The location where to save the files.
	 * @param pixelsID 	The ID of the pixels set.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service.  
	 */
	Map<Integer, List> getArchivedFiles(String path, long pixelsID) 
		throws DSAccessException, DSOutOfServiceException
	{
		isSessionAlive();
		IQueryPrx service = getQueryService();
		List files = null;
		try {
			ParametersI param = new ParametersI();
			param.map.put("childID", omero.rtypes.rlong(pixelsID));
			files = service.findAllByQuery(
					"select ofile from OriginalFile as ofile left join " +
					"ofile.pixelsFileMaps as pfm left join pfm.child as " +
					"child where child.id = :id", param);
		} catch (Exception e) {
			throw new DSAccessException("Cannot retrieve original file", e);
		}

		Map<Integer, List> result = new HashMap<Integer, List>();
		if (files == null || files.size() == 0) return result;
		RawFileStorePrx store = getRawFileService();
		Iterator i = files.iterator();
		OriginalFile of;

		long size;	
		FileOutputStream stream = null;
		long offset = 0;
		File f;
		List<String> notDownloaded = new ArrayList<String>();
		String fullPath;
		while (i.hasNext()) {
			of = (OriginalFile) i.next();
			try {
				store.setFileId(of.getId().getValue()); 
			} catch (Exception e) {
				handleException(e, "Cannot set the file's id.");
			}
			
			fullPath = path+of.getName();
			f = new File(fullPath);
			try {
				stream = new FileOutputStream(f);
				size = of.getSize().getValue(); 
				try {
					try {
						for (offset = 0; (offset+INC) < size;) {
							stream.write(store.read(offset, INC));
							offset += INC;
						}	
					} finally {
						stream.write(store.read(offset, (int) (size-offset))); 
						stream.close();
					}
				} catch (Exception e) {
					if (stream != null) stream.close();
					if (f != null) f.delete();
					notDownloaded.add(of.getName().getValue());
				}
			} catch (IOException e) {
				if (f != null) f.delete();
				notDownloaded.add(of.getName().getValue());
				throw new DSAccessException("Cannot create file with path " +
											fullPath, e);
			}
		}
		result.put(files.size(), notDownloaded);
		return result;
	}

	/**
	 * Downloads a file previously uploaded to the server.
	 * 
	 * @param file		The file to copy the data into.	
	 * @param fileID	The id of the file to download.
	 * @param size		The size of the file.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service.  
	 */
	File downloadFile(File file, long fileID, long size)
		throws DSAccessException, DSOutOfServiceException
	{
		isSessionAlive();
		RawFileStorePrx store = getRawFileService();
		try {
			store.setFileId(fileID);
		} catch (Throwable e) {
			handleException(e, "Cannot set the file's id.");
		}
		
		String path = file.getAbsolutePath();
		int offset = 0;
		int length = (int) size;
		try {
			FileOutputStream stream = new FileOutputStream(file);
			try {
				try {
					for (offset = 0; (offset+INC) < size;) {
						stream.write(store.read(offset, INC));
						offset += INC;
					}	
				} finally {
					stream.write(store.read(offset, length-offset)); 
					stream.close();
				}
			} catch (Exception e) {
				if (stream != null) stream.close();
				if (file != null) file.delete();
			}
		} catch (IOException e) {
			if (file != null) file.delete();
			throw new DSAccessException("Cannot create file  " +path, e);
		}
		//store.close();
		return file;
	}
	
	/**
	 * Returns the original file corresponding to the passed id.
	 * 
	 * @param id	The id identifying the file.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	OriginalFile getOriginalFile(long id)
		throws DSAccessException, DSOutOfServiceException
	{
		isSessionAlive();
		OriginalFile of = null;
		try {
			ParametersI param = new ParametersI();
			param.map.put("id", omero.rtypes.rlong(id));
			of = (OriginalFile) getQueryService().findByQuery(
					"select p from OriginalFile as p " +
					"left outer join fetch p.format " +
					"where p.id = :id", param);
		} catch (Exception e) {
			handleException(e, "Cannot retrieve original file");
		}
		return of;
	}
	
	/**
	 * Returns the collection of original files related to the specifed 
	 * pixels set.
	 * 
	 * @param pixelsID The ID of the pixels set.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service.  
	 */
	List getOriginalFiles(long pixelsID)
		throws DSAccessException, DSOutOfServiceException
	{
		isSessionAlive();
		List files = null;
		try {
			ParametersI param = new ParametersI();
			param.map.put("childID", omero.rtypes.rlong(pixelsID));
			files = getQueryService().findAllByQuery(
					"select ofile from OriginalFile as ofile left join " +
					"ofile.pixelsFileMaps as pfm left join pfm.child as " +
					"child where child.id = :id", param);
		} catch (Exception e) {
			handleException(e, "Cannot retrieve original file");
		}
		return files;
	}
	
	/**
	 * Uploads the passed file to the server and returns the 
	 * original file i.e. the server object.
	 * 
	 * @param file		     The file to upload.
	 * @param format		 The format of the file.
	 * @param originalFileID The id of the file or <code>-1</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service.  
	 */
	OriginalFile uploadFile(File file, String format, long originalFileID)
		throws DSAccessException, DSOutOfServiceException
	{
		if (file == null)
			throw new IllegalArgumentException("No file to upload");
		isSessionAlive();
		RawFileStorePrx store = getRawFileService();
		OriginalFile save = null;
		try {
			OriginalFile oFile;
			if (originalFileID <= 0) {
				Format f = (Format) getQueryService().findByString(
						Format.class.getName(), "value", format);
				oFile = new OriginalFileI();
				oFile.setName(omero.rtypes.rstring(file.getName()));
				oFile.setPath(omero.rtypes.rstring(file.getAbsolutePath()));
				oFile.setSize(omero.rtypes.rlong(file.length()));
				oFile.setSha1(omero.rtypes.rstring("pending"));
				oFile.setFormat(f);
				
				save = (OriginalFile) saveAndReturnObject(oFile, null);
				//service.saveAndReturnObject(oFile);
				store.setFileId(save.getId().getValue());
			} else {
				oFile = (OriginalFile) findIObject(OriginalFile.class.getName(), 
					originalFileID);
				
				OriginalFile newFile = new OriginalFileI();
				newFile.setId(omero.rtypes.rlong(originalFileID));
				newFile.setName(omero.rtypes.rstring(file.getName()));
				newFile.setPath(omero.rtypes.rstring(file.getAbsolutePath()));
				newFile.setSize(omero.rtypes.rlong(file.length()));
				newFile.setSha1(omero.rtypes.rstring("pending"));
				newFile.setFormat(oFile.getFormat());
				save = (OriginalFile) saveAndReturnObject(newFile, null);
				store.setFileId(save.getId().getValue());
			}
			
		} catch (Exception e) {
			handleException(e, "Cannot set the file's id.");
		}
		byte[] buf = new byte[INC]; 
		FileInputStream stream = null;
		try {
			stream = new FileInputStream(file);
			long pos = 0;
			int rlen;
			ByteBuffer bbuf;
			while ((rlen = stream.read(buf)) > 0) {
				store.write(buf, pos, rlen);
				pos += rlen;
				bbuf = ByteBuffer.wrap(buf);
				bbuf.limit(rlen);
			}
			stream.close();
		} catch (Exception e) {
			try {
				if (stream != null) stream.close();
			} catch (Exception ex) {}
			
			throw new DSAccessException("Cannot upload the file with path " +
					file.getAbsolutePath(), e);
		}
		return save;
	}
	
	/**
	 * Modifies the password of the currently logged in user.
	 * 
	 * @param password	The new password.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	void changePassword(String password)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			getAdminService().changePassword(omero.rtypes.rstring(password));
		} catch (Throwable t) {
			handleException(t, "Cannot modify password. ");
		}
	}

	/**
	 * Updates the profile of the specified experimenter.
	 * 
	 * @param exp	The experimenter to handle.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	void updateExperimenter(Experimenter exp) 
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			getAdminService().updateSelf(exp);
		} catch (Throwable t) {
			handleException(t, "Cannot update the user. ");
		}
	}

	/**
	 * Returns the XY-plane identified by the passed z-section, timepoint 
	 * and wavelength.
	 * 
	 * @param pixelsID 	The id of pixels containing the requested plane.
	 * @param z			The selected z-section.
	 * @param t			The selected timepoint.
	 * @param c			The selected wavelength.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	synchronized byte[] getPlane(long pixelsID, int z, int t, int c)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		RawPixelsStorePrx service = getPixelsStore();
		try {
			service.setPixelsId(pixelsID);
			return service.getPlane(z, c, t);
		} catch (Throwable e) {
			handleException(e, "Cannot retrieve the plane " +
					"(z="+z+", t="+t+", c="+c+") for pixelsID: "+pixelsID);
		}
		return null;
	}

	/**
	 * Returns the free or available space (in Kilobytes) on the file system
	 * including nested subdirectories.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	long getFreeSpace()
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			return getRepositoryService().getFreeSpaceInKilobytes();
		} catch (Throwable e) {
			handleException(e, "Cannot retrieve the free space");
		}
		return -1;
	}

	/**
	 * Returns the used space (in Kilobytes) on the file system
	 * including nested subdirectories.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	long getUsedSpace()
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			return getRepositoryService().getUsedSpaceInKilobytes();
		} catch (Throwable e) {
			handleException(e, "Cannot retrieve the free space");
		}
		return -1;
	}

	/**
	 * Returns a collection of images imported after the specified time
	 * by the specified user.
	 * 
	 * @param startTime	The reference time.
	 * @param endTime	The reference time.
	 * @param userID	The user's id.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List getImagesDuring(Timestamp startTime, Timestamp endTime, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		
		try {
			String  sql = "select i from Image as i left outer join fetch " +
			"i.details.creationEvent as c where " +
			"i.details.owner.id = :userID and ";
			IQueryPrx service = getQueryService();
			ParametersI param = new ParametersI();
			param.map.put("userID", omero.rtypes.rlong(userID));
			if (startTime != null && endTime != null) {
				sql += "c.time < :endTime and c.time > :startTime";
				param.add("startTime", omero.rtypes.rtime(startTime.getTime()));
				param.add("endTime", omero.rtypes.rtime(endTime.getTime()));
			} else if (startTime == null && endTime != null) {
				sql += "c.time < :endTime";
				param.add("endTime", omero.rtypes.rtime(endTime.getTime()));
			} else if (startTime != null && endTime == null) {
				sql += "c.time  > :startTime";
				param.add("startTime", omero.rtypes.rtime(startTime.getTime()));
			} 
			
			return service.findAllByQuery(sql, param);
		} catch (Throwable e) {
			handleException(e, "Cannot retrieve the images imported during " +
			"the selected period.");
		}
		return null;
	}

	/**
	 * Retrieves the images specified by a set of parameters
	 * e.g. imported during a given period of time by a given user.
	 * 
	 * @param map 			The options. 
	 * @param asDataObject 	Pass <code>true</code> to convert the 
	 * 						<code>IObject</code>s into the corresponding 
	 * 						<code>DataObject</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Collection getImages(Map map, boolean asDataObject)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IContainerPrx service = getPojosService();
			List result = service.getImagesByOptions(map);
			if (asDataObject) return PojoMapper.asDataObjects(result);
			return result;
		} catch (Exception e) {
			handleException(e, "Cannot retrieve the images imported " +
							"the specified period.");
		}
		return null;
	}

	/**
	 * Resets the rendering settings for the images contained in the 
	 * specified datasets or categories
	 * if the rootType is <code>DatasetData</code> or <code>CategoryData</code>.
	 * Resets the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param rootNodeType	The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code> or 
	 * 						<code>CategoryData</code>.
	 * @param nodes			The nodes to apply settings to. 
	 * @return <true> if the call was successful, <code>false</code> otherwise.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Map resetRenderingSettings(Class rootNodeType, List nodes) 
		throws DSOutOfServiceException, DSAccessException
	{
		List<Long> success = new ArrayList<Long>();
		List<Long> failure = new ArrayList<Long>();
		isSessionAlive();
		try {
			IRenderingSettingsPrx service = getRenderingSettingsService();
			String klass = convertPojos(rootNodeType).getName();
			if (klass.equals(Image.class.getName()) 
					|| klass.equals(Dataset.class.getName()))
				success = service.resetDefaultsInSet(klass, nodes);
		} catch (Exception e) {
			handleException(e, "Cannot reset the rendering settings.");
		}
		Map<Boolean, List> result = new HashMap<Boolean, List>(2);
		result.put(Boolean.TRUE, success);
		result.put(Boolean.FALSE, failure);
		return result;
	}
  
	/**
	 * Resets the rendering settings for the images contained in the 
	 * specified datasets or categories
	 * if the rootType is <code>DatasetData</code> or <code>CategoryData</code>.
	 * Resets the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param rootNodeType	The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code> or 
	 * 						<code>CategoryData</code>.
	 * @param nodes			The nodes to apply settings to. 
	 * @return <true> if the call was successful, <code>false</code> otherwise.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Map setOriginalRenderingSettings(Class rootNodeType, List nodes) 
		throws DSOutOfServiceException, DSAccessException
	{
		List<Long> success = new ArrayList<Long>();
		List<Long> failure = new ArrayList<Long>();
		isSessionAlive();
		try {
			IRenderingSettingsPrx service = getRenderingSettingsService();
			String klass = convertPojos(rootNodeType).getName();
			if (klass.equals(Image.class.getName()) 
				|| klass.equals(Dataset.class.getName()))
				success = service.setOriginalSettingsInSet(klass, nodes);
		} catch (Exception e) {
			handleException(e, "Cannot reset the rendering settings.");
		}
		Map<Boolean, List> result = new HashMap<Boolean, List>(2);
		result.put(Boolean.TRUE, success);
		result.put(Boolean.FALSE, failure);
		return result;
	}
	
	/**
	 * Applies the rendering settings associated to the passed pixels set 
	 * to the images contained in the specified datasets or categories
	 * if the rootType is <code>DatasetData</code> or <code>CategoryData</code>.
	 * Applies the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param pixelsID		The id of the pixels set to copy the settings from.
	 * @param rootNodeType	The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code> or 
	 * 						<code>CategoryData</code>.
	 * @param nodes			The nodes to apply settings to. 
	 * @return <true> if the call was successful, <code>false</code> otherwise.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Map pasteRenderingSettings(long pixelsID, Class rootNodeType, List nodes) 
		throws DSOutOfServiceException, DSAccessException
	{
		List<Long> success = new ArrayList<Long>();
		List<Long> failure = new ArrayList<Long>();
		isSessionAlive();
		try {
			IRenderingSettingsPrx service = getRenderingSettingsService();
			//String klass = convertPojos(rootNodeType).getName();
			Iterator i = nodes.iterator();
			long id;
			if (ImageData.class.equals(rootNodeType)) {
				Map m = service.applySettingsToImages(pixelsID, nodes);
				success = (List<Long>) m.get(Boolean.TRUE);
				failure = (List<Long>) m.get(Boolean.FALSE);
			} else if (DatasetData.class.equals(rootNodeType)) {
				Map m;
				List l;
				Iterator k;
				while (i.hasNext()) {
					id = (Long) i.next();
					m = service.applySettingsToDataset(pixelsID, id);
					l = (List) m.get(Boolean.TRUE);
					if (l != null && l.size() > 0) {
						k = l.iterator();
						while (k.hasNext()) {
							success.add((Long) k.next());
						}
					}
					l = (List) m.get(Boolean.FALSE);
					if (l != null && l.size() > 0) {
						k = l.iterator();
						while (k.hasNext()) {
							failure.add((Long) k.next());
						}
					}
				}
			}
		} catch (Exception e) {
			handleException(e, "Cannot paste the rendering settings.");
		}
		Map<Boolean, List> result = new HashMap<Boolean, List>(2);
		result.put(Boolean.TRUE, success);
		result.put(Boolean.FALSE, failure);
		return result;
	}
	
	/**
	 * Retrieves all the rendering settings linked to the specified set
	 * of pixels.
	 * 
	 * @param pixelsID	The pixels ID.
	 * @param userID	The id of the user.
	 * @return Map whose key is the experimenter who set the settings,
	 * 		  and the value is the rendering settings itself.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Map getRenderingSettings(long pixelsID, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		Map map = new HashMap();
		isSessionAlive();
		try {
			/*
		}
			Parameters param = new ParametersI();
			param.map.put("pixid", omero.rtypes.rlong(pixelsID));
			
			String sql =  "select rdef from RenderingDef as rdef "
                + "left outer join fetch rdef.quantization "
                + "left outer join fetch rdef.model "
                + "left outer join fetch rdef.waveRendering as cb "
                + "left outer join fetch cb.family "
                + "left outer join fetch rdef.spatialDomainEnhancement " 
                + "left outer join fetch rdef.details.owner "
                + "where rdef.pixels.id = :pixid";
			if (userID >= 0) {
				sql += " and rdef.details.owner.id = :userid";
				param.map.put("userid", omero.rtypes.rlong(userID));
			}
			IQueryPrx service = getQueryService();
			
			List results = service.findAllByQuery(sql, param);
			*/
			IPixelsPrx service = getPixelsService();
			List results = service.retrieveAllRndSettings(pixelsID, userID);
			
			if (results == null || results.size() == 0) return map;
			Iterator i = results.iterator();
			RenderingDef rndDef;
			Experimenter exp;
			while (i.hasNext()) {
				rndDef = (RenderingDef) i.next();
				exp = rndDef.getDetails().getOwner();
				map.put(PojoMapper.asDataObject(exp), rndDef);
			}
			return map;
		} catch (Exception e) {
			handleException(e, "Cannot retrieve the rendering settings " +
								"for.");
		}
		return map;
	}
	
	/**
	 * Retrieves the rendering settings for the specified pixels set.
	 * 
	 * @param pixelsID  The pixels ID.
	 * @param userID	The id of the user who set the rendering settings.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	RenderingDef getRenderingDef(long pixelsID, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		//This method should be pushed server side.
		isSessionAlive();
		try {
			IPixelsPrx service = getPixelsService();
			return service.retrieveRndSettingsFor(pixelsID, userID);
		} catch (Exception e) {
			handleException(e, "Cannot retrieve the rendering settings");
		}
		
		return null;
	}

	/**
	 * 
	 * @param type
	 * @param nameSpace
	 * @param options
	 * @return
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	Set loadSpecifiedAnnotation(Class type, String nameSpace, Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IMetadataPrx service = getMetadataService();
			
			return PojoMapper.asDataObjects(
					service.loadSpecifiedAnnotations(
							convertPojos(type).getName(), nameSpace, options));
		} catch (Exception e) {
			e.printStackTrace();
			handleException(e, "Cannot retrieve the annotations");
		}
		return null;
	}
	
	/**
	 * Handles the result of the search.
	 * 
	 * @param type 	The supported type.
	 * @param r		The collection to fill.
	 * @param svc	Helper reference to the service.
	 * @return See above.
	 * @throws ServerError If an error occurs while reading the results.
	 */
	private Object handleSearchResult(String type, Collection r, SearchPrx svc)
		throws ServerError
	{
		//First get object of a given type.
		boolean hasNext = false;
		try {
			hasNext = svc.hasNext();
		} catch (Exception e) {
			int size = 0;
			if (e instanceof InternalException) size = -1;
			else svc.getBatchSize();
			return new Integer(size);
		}
		if (!hasNext) return r;
		List l = svc.results();
		Iterator k = l.iterator();
		IObject object;
		long id;
		while (k.hasNext()) {
			object = (IObject) k.next();
			if (type.equals(object.getClass().getName())) {
				id = object.getId().getValue();
				if (!r.contains(id)) 
					r.add(id); //Retrieve the object of a given type.
			}
		}
		return r;
	}
	
	/**
	 * Formats the elements of the passed array. Adds the 
	 * passed field in front of each term.
	 * 
	 * @param terms	The terms to format.
	 * @param field	The string to add in front of the terms.
	 * @return See above.
	 */
	private List<String> formatText(List<String> terms, String field)
	{
		if (terms == null || terms.size() == 0) return null;
		List<String> formatted = new ArrayList<String>(terms.size());
		Iterator<String> j = terms.iterator();
		while (j.hasNext()) 
			formatted.add(field+":"+j.next());
		
		return formatted;
	}
	
	/**
	 * Formats the elements of the passed array. Adds the 
	 * passed field in front of each term.
	 * @param terms			The terms to format.
	 * @param firstField	The string to add in front of the terms.
	 * @param sep			Separator used to join, exclude etc.
	 * @param secondField	The string to add in front of the terms.
	 * @return See above.
	 */
	private List<String> formatText(List<String> terms, String firstField, 
								String sep, String secondField)
	{
		if (terms == null || terms.size() == 0) return null;
		List<String> formatted = new ArrayList<String>(terms.size());
		String value;
		Iterator<String> j = terms.iterator();
		String v;
		while (j.hasNext()) {
			v = j.next();
			value = firstField+":"+v+" "+sep+" ";
			value += secondField+":"+v;
			formatted.add(value);
		}
		return formatted;
	}
	
	/**
	 * Searches for data.
	 * 
	 * @param context The context of search.
	 * @return The found objects.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Object performSearch(SearchDataContext context)
		throws DSOutOfServiceException, DSAccessException
	{
		List<Class> types = context.getTypes();
		List<Integer> scopes = context.getScope();
		if (types == null || types.size() == 0) return new HashMap();
		if (scopes == null || scopes.size() == 0) return new HashMap();
		isSessionAlive();
		try {
			SearchPrx service = getSearchService();
			//service.clearQueries();
			//service.resetDefaults();
			service.setAllowLeadingWildcard(false);
			
			service.setCaseSentivice(context.isCaseSensitive());
			
			Timestamp start = context.getStart();
			Timestamp end = context.getEnd();
			//Sets the time
			if (start != null || end != null) {
				switch (context.getTimeIndex()) {
					case SearchDataContext.CREATION_TIME:
						service.onlyCreatedBetween(
								omero.rtypes.rtime(start.getTime()), 
								omero.rtypes.rtime(end.getTime()));
						break;
					case SearchDataContext.MODIFICATION_TIME:
						service.onlyModifiedBetween(
								omero.rtypes.rtime(start.getTime()), 
								omero.rtypes.rtime(end.getTime()));
						break;
					case SearchDataContext.ANNOTATION_TIME:
						service.onlyAnnotatedBetween(
								omero.rtypes.rtime(start.getTime()), 
								omero.rtypes.rtime(end.getTime()));	
				}
			}
			List<ExperimenterData> users = context.getOwners();
			Iterator i;
			ExperimenterData exp;
			Details d;
			//owner
			List<Details> owners = new ArrayList<Details>();
			//if (users != null && users.size() > 0) {
				i = users.iterator();
				while (i.hasNext()) {
					exp = (ExperimenterData) i.next();
					d = new DetailsI();
					d.setOwner(exp.asExperimenter());
			        owners.add(d);
				}
			//}
			
			
			List<String> some = prepareTextSearch(context.getSome(), service);
			List<String> must = prepareTextSearch(context.getMust(), service);
			List<String> none = prepareTextSearch(context.getNone(), service);
			
			List<String> supportedTypes = new ArrayList<String>();
			i = types.iterator();
			while (i.hasNext()) 
				supportedTypes.add(convertPojos((Class) i.next()).getName());

			List rType;
			Map<Integer, Object> results = new HashMap<Integer, Object>();
			Object size;
			Integer key;
			i = scopes.iterator();
			while (i.hasNext()) 
				results.put((Integer) i.next(), new ArrayList());
			
			Iterator<Details> owner;
			i = scopes.iterator();
			List<String> fSome = null, fMust = null, fNone = null;
			List<String> fSomeSec = null, fMustSec = null, fNoneSec = null;
			service.onlyType(Image.class.getName());
			while (i.hasNext()) {
				key = (Integer) i.next();
				rType = (List) results.get(key);
				size = null;
				if (key == SearchDataContext.TAGS) {
					fSome = formatText(some, "tag");
					fMust = formatText(must, "tag");
					fNone = formatText(none, "tag");
				} else if (key == SearchDataContext.NAME) {
					fSome = formatText(some, "name");
					fMust = formatText(must, "name");
					fNone = formatText(none, "name");
				} else if (key == SearchDataContext.DESCRIPTION) {
					fSome = formatText(some, "description");
					fMust = formatText(must, "description");
					fNone = formatText(none, "description");
				} else if (key == SearchDataContext.FILE_ANNOTATION) {
					fSome = formatText(some, "file.name");
					fMust = formatText(must, "file.name");
					fNone = formatText(none, "file.name");
					fSomeSec = formatText(some, "file.contents");
					fMustSec = formatText(must, "file.contents");
					fNoneSec = formatText(none, "file.contents");
				} else if (key == SearchDataContext.TEXT_ANNOTATION) {
					fSome = formatText(some, "annotation", "NOT", "tag");
					fMust = formatText(must, "annotation", "NOT", "tag");
					fNone = formatText(none, "annotation", "NOT", "tag");
				} else if (key == SearchDataContext.URL_ANNOTATION) {
					fSome = formatText(some, "url");
					fMust = formatText(must, "url");
					fNone = formatText(none, "url");
				}
				
				owner = owners.iterator();
				if (fSome != null) {
					while (owner.hasNext()) {
						d = owner.next();
						service.onlyOwnedBy(d);
						service.bySomeMustNone(fSome, fMust, fNone);
						size = handleSearchResult(
								convertTypeForSearch(Image.class), rType, 
								service);
						if (size instanceof Integer)
							results.put(key, size);
						service.clearQueries();
						if (!(size instanceof Integer) && fSomeSec != null) {
							service.bySomeMustNone(fSomeSec, fMustSec, 
									fNoneSec);
							size = handleSearchResult(Image.class.getName(), 
									rType, service);
							if (size instanceof Integer) 
								results.put(key, size);
							service.clearQueries();
						}
					}
				}
			}
			service.close();
			return results;
		} catch (Throwable e) {
			handleException(e, "Cannot perform the search.");
		}
		return null;
	}
	
	/**
	 * Returns the collection of annotations of a given type.
	 * 
	 * @param annotationType	The type of annotation.
	 * @param terms				The terms to search for.
	 * @param start				The lower bound of the time interval 
	 * 							or <code>null</code>.
	 * @param end				The lower bound of the time interval 
	 * 							or <code>null</code>.
	 * @param exp				The experimenter who annotated the object.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Set filterBy(Class annotationType, List<String> terms,
				Timestamp start, Timestamp end, ExperimenterData exp)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			SearchPrx service = getSearchService();
			if (start != null && end != null)
				service.onlyAnnotatedBetween(
						omero.rtypes.rtime(start.getTime()), 
						omero.rtypes.rtime(end.getTime()));
			if (exp != null) {
				Details d = new DetailsI();
				d.setOwner(exp.asExperimenter());
			}
			List<String> t = prepareTextSearch(terms, service);

			service.onlyType(convertPojos(annotationType).getName());
			Set rType = new HashSet();
			service.bySomeMustNone(t, null, null);
			Object size = handleSearchResult(
					convertTypeForSearch(annotationType), rType, service);
			if (size instanceof Integer) new HashSet();
			return rType;
		} catch (Exception e) {
			handleException(e, "Filtering by annotation not valid");
		}
		return new HashSet();
	}
	
	/**
	 * Retrieves all containers of a given type.
	 * The containers are not linked to any of their children.
	 * 
	 * @param type		The type of container to retrieve.
	 * @param userID	The id of the owne of the container.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Set fetchContainers(Class type, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQueryPrx service = getQueryService();
			Parameters p = new ParametersI();
			p.map = new HashMap<String, RType>();
			p.map.put("id", omero.rtypes.rlong(userID));
			String table = getTableForClass(type);
			return PojoMapper.asDataObjects(service.findAllByQuery(
	                "from "+table+" as p where p.details.owner.id = :id", p));
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the containers.");
		}
		return null;
	}
	
	
	//TMP solution. Need to move the call to server and optimize them
	/**
	 * Loads the tags linked to images, returns the images linked to the tag
	 * if the passed value is <code>true</code>.
	 * 
	 * @param tagID 		The id of the tag.
	 * @param withLeaves    Pass <code>true</code> to load the images
	 * 						related to the object linked to the tag, 
	 * 						<code>false</code> otherwise.
     * @param userID		The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List loadTagAndDataObjects(long tagID, boolean withLeaves)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQueryPrx service = getQueryService();
			Parameters p = new ParametersI();
			p.map = new HashMap<String, RType>();
			p.map.put("tagID", omero.rtypes.rlong(tagID));
			StringBuilder sb = new StringBuilder();
			
			sb.append("select ann from Annotation as ann ");
			sb.append("left outer join fetch ann.details.creationEvent ");
			sb.append("left outer join fetch ann.details.owner ");

			sb.append("where ann.id = :tagID");
			IObject object = service.findByQuery(sb.toString(), p);
			TagAnnotationData tag = 
						(TagAnnotationData) PojoMapper.asDataObject(object);
			//Condition
			List r = new ArrayList();
			if (TagAnnotationData.INSIGHT_TAGSET_NS.equals(tag.getNameSpace()))
				return r;
			tag.setDataObjects(loadRelatedObjects(tagID, withLeaves));
			r.add(tag);
			return r;
		} catch (Exception e) {
			handleException(e, "Cannot retrieve the annotations");
		}
		return null;
	}
	
	private Set<DataObject> loadRelatedObjects(long tagID, boolean withLeaves)
		throws Exception
	{
		IQueryPrx service = getQueryService();
		Parameters p = new ParametersI();
		p.map = new HashMap<String, RType>();
		p.map.put("tagID", omero.rtypes.rlong(tagID));

		Set<DataObject> objects = new HashSet<DataObject>();
		StringBuilder sb = new StringBuilder();
		sb.append("select img from Image as img ");
		sb.append("left outer join fetch "
				+ "img.annotationLinksCountPerOwner img_a_c ");
		sb.append("left outer join fetch img.annotationLinks ail ");
		sb.append("left outer join fetch img.pixels as pix ");
		sb.append("left outer join fetch pix.pixelsType as pt ");
		sb.append("where ail.child.id = :tagID");
		Set l = PojoMapper.asDataObjects(
				service.findAllByQuery(sb.toString(), p));
		if (l != null && l.size() > 0)
			objects.addAll(l);
		sb = new StringBuilder();
		sb.append("select d from Dataset as d ");
		sb.append("left outer join fetch "
				+ "d.annotationLinksCountPerOwner d_a_c ");
		sb.append("left outer join fetch d.annotationLinks ail ");
		sb.append("where ail.child.id = :tagID");
		List list = service.findAllByQuery(sb.toString(), p);
		
		if (list != null && list.size() > 0) {
			if (withLeaves) {
				List<Long> ids = new ArrayList<Long>();
				Iterator k = list.iterator();
				IObject o;
				while (k.hasNext()) {
					o = (IObject) k.next();
					ids.add(o.getId().getValue());
				}
				PojoOptions po = new PojoOptions();
				po.leaves();
				l = loadContainerHierarchy(DatasetData.class, ids, po.map());
				if (l != null && l.size() > 0)
					objects.addAll(l);
			} else {
				objects.addAll(PojoMapper.asDataObjects(list));
			}
		}
			
		//Retrieve the projects.
		sb = new StringBuilder();
		sb.append("select p from Project as p ");
		sb.append("left outer join fetch "
				+ "p.annotationLinksCountPerOwner p_a_c ");
		sb.append("left outer join fetch p.annotationLinks ail ");
		sb.append("where ail.child.id = :tagID");
		list = service.findAllByQuery(sb.toString(), p);
		if (list != null && list.size() > 0) {
			List<Long> ids = new ArrayList<Long>();
			Iterator k = list.iterator();
			IObject o;
			while (k.hasNext()) {
				o = (IObject) k.next();
				ids.add(o.getId().getValue());
			}
			PojoOptions po = new PojoOptions();
			po.noLeaves();
			if (withLeaves) po.leaves();
			l = loadContainerHierarchy(ProjectData.class, ids, po.map());
			if (l != null && l.size() > 0)
				objects.addAll(l);
		}
		return objects;
	}
	
	/**
	 * 
	 * @param type
	 * @param annotationIds
	 * @param ownerIds
	 * @return
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	Set getAnnotatedObjects(Class type, Set<Long> annotationIds, 
			Set<Long> ownerIds)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQueryPrx service = getQueryService();
			ParametersI param = new ParametersI();
			param.addLongs("ids", annotationIds);
			StringBuilder sb = new StringBuilder();
			
			if (type.equals(ImageData.class)) {
				sb.append("select img from Image as img ");
				sb.append("left outer join fetch "
	                    + "img.annotationLinksCountPerOwner img_a_c ");
				sb.append("left outer join fetch img.annotationLinks ail ");
				sb.append("left outer join fetch img.pixels as pix ");
	            sb.append("left outer join fetch pix.pixelsType as pt ");
	            sb.append("where ail.child.id in (:ids)");
	            if (ownerIds != null && ownerIds.size() > 0) {
	            	sb.append(" and img.details.owner.id in (:ownerIds)");
	            	param.addLongs("ownerIds", ownerIds);
	            }
	            return PojoMapper.asDataObjects(
	         			service.findAllByQuery(sb.toString(), param));
			}
			return new HashSet();
			
		} catch (Exception e) {
			handleException(e, "Cannot retrieve the annotated objects");
		}
		return null;
	}
	
	/**
	 * Loads the tags linked to images, returns the images linked to the tag
	 * if the passed value is <code>true</code>.
	 * 
	 * @param id 		The id of the tag set.
     * @param userID	The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Collection loadTagSetsAndDataObjects(long id)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			String type = "ome.model.annotations.TagAnnotation";
			IQueryPrx service = getQueryService();
			ParametersI param = new ParametersI();
			param.addLong("tagID", id);
			StringBuilder sb = new StringBuilder();
			
			sb.append("select ann from Annotation as ann ");
			sb.append("left outer join fetch ann.details.creationEvent ");
			sb.append("left outer join fetch ann.details.owner ");

			sb.append("where ann.id = :tagID");
			//Tag to retrieve
			IObject object = service.findByQuery(sb.toString(), param);
			TagAnnotationData tag = 
				(TagAnnotationData) PojoMapper.asDataObject(object);
			sb = new StringBuilder();
			sb.append("select link from AnnotationAnnotationLink as link ");
			sb.append("left outer join link.child ann ");
			sb.append(" where ann member of "+type);
			sb.append(" and link.parent.id = :id");
			param = new ParametersI();
			param.addLong("id", tag.getId());

			List r = service.findAllByQuery(sb.toString(), param);
			if (r != null) {
				Iterator j = r.iterator();
				AnnotationAnnotationLink link;
				Set<TagAnnotationData> 
				    children = new HashSet<TagAnnotationData>();
				TagAnnotationData child;
				while (j.hasNext()) {
					link = (AnnotationAnnotationLink) j.next();
					child = (TagAnnotationData) 
							PojoMapper.asDataObject(link.getChild());
					children.add(child);
					/*
					if (dataObject) { //load images 
			            child.setDataObjects(
			            		loadRelatedObjects(child.getId(), false));
					}
					*/
				}
				tag.setTags(children);
			}
			Set result = new HashSet();
			result.add(tag);
			
			return result;
		} catch (Exception e) {
			handleException(e, "Cannot retrieve the tags");
		}
		return new HashSet();
	}
	
	/**
	 * Returns the number of images related to a given tag.
	 * 
	 * @param rootNodeIDs
	 * @return See above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	Map getDataObjectsTaggedCount(List rootNodeIDs)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQueryPrx service = getQueryService();
			ParametersI param;
			StringBuilder sb = new StringBuilder();
			
			sb.append("select img from Image as img ");
			sb.append("left outer join fetch img.annotationLinks ail ");
            sb.append("where ail.child.id = :tagID");
            Iterator i = rootNodeIDs.iterator();
            Long id;
            Map<Long, Long> m = new HashMap<Long, Long>();
            //Image first
            List l;
            while (i.hasNext()) {
				id = (Long) i.next();
				param = new ParametersI();
				param.addLong("tagID", id);
				l = service.findAllByQuery(sb.toString(), param);
				if (l != null) 
					m.put(id, new Long(l.size()));
			}
            //Dataset
            sb = new StringBuilder();
			sb.append("select d from Dataset as d ");
			sb.append("left outer join fetch d.annotationLinks ail ");
            sb.append("where ail.child.id = :tagID");
            i = rootNodeIDs.iterator();
            Long value;
            long r;
            while (i.hasNext()) {
				id = (Long) i.next();
				param = new ParametersI();
				param.addLong("tagID", id);
				value = m.get(id);
				l = service.findAllByQuery(sb.toString(), param);
				if (l != null) {
					r = l.size();
					if (value == null) value = r;
					else value += r;
				}
				m.put(id, value);
			}
            //Project
            sb = new StringBuilder();
			sb.append("select d from Project as d ");
			sb.append("left outer join fetch d.annotationLinks ail ");
            sb.append("where ail.child.id = :tagID");
            i = rootNodeIDs.iterator();
            while (i.hasNext()) {
				id = (Long) i.next();
				param = new ParametersI();
				param.addLong("tagID", id);
				value = m.get(id);
				l = service.findAllByQuery(sb.toString(), param);
				if (l != null) {
					r = l.size();
					if (value == null) value = r;
					else value += r;
				}
				m.put(id, value);
			}
			return m;
		} catch (Throwable t) {
			handleException(t, "Cannot count the collection.");
		}
		return new HashMap();
	}
	
	/**
	 * 
	 * @param userID
	 * @return
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	Set loagTagSets(long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			String type = "ome.model.annotations.TagAnnotation";
			IQueryPrx service = getQueryService();
			ParametersI param = new ParametersI();
			param.addLong("uid", userID);
			String sql =  "select ann from Annotation as ann "
				+ "left outer join ann.annotationLinks link "
                + "left outer join fetch ann.details.creationEvent "
                + "left outer join fetch ann.details.owner ";
			sql += " where link.parent member of "+type;
			sql += " and link.child member of "+type;
			sql += " and link.details.owner.id = :uid";
			List l = service.findAllByQuery(sql, param);
			Set results = new HashSet();
			if (l != null) {
				Iterator<IObject> i = l.iterator();
				TagAnnotationData tag;
				List<Long> ids = new ArrayList<Long>();
				while (i.hasNext()) {
					param = new ParametersI();
					tag = (TagAnnotationData) PojoMapper.asDataObject(i.next());
					if (!ids.contains(tag.getId())) {
						param.addLong("id", tag.getId());
						sql =  "select link from AnnotationAnnotationLink " +
								"as link left outer join link.child ann ";
						sql += " where ann member of "+type;
						sql += " and link.parent.id = :id";
						Set children = new HashSet();
						List r = service.findAllByQuery(sql, param);
						Iterator j = r.iterator();
						AnnotationAnnotationLink link;
						while (j.hasNext()) {
							link = (AnnotationAnnotationLink) j.next();
							children.add(
									PojoMapper.asDataObject(link.getChild()));
						}
						tag.setTags(children);
				 		results.add(tag);
				 		ids.add(tag.getId());
					}
				}
			}
			
			return results;
		} catch (Exception e) {
			e.printStackTrace();
			handleException(e, "Cannot retrieve the annotations");
		}
		return null;
	}
	
	/**
	 * Removes the description linked to the tags.
	 * 
	 * @param tagID  The id of tag to handle.
	 * @param userID The id of the user who annotated the tag.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	void removeTagDescription(long tagID, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			String type = "ome.model.annotations.TextAnnotation";
			IQueryPrx service = getQueryService();
			ParametersI param = new ParametersI();
			param.addLong("uid", userID);
			param.addLong("id", tagID);

			String sql =  "select link from AnnotationAnnotationLink as link ";
			sql += "where link.parent.id = :id";
			sql += " and link.child member of "+type;
			sql += " and link.details.owner.id = :uid";
			
			List l = service.findAllByQuery(sql, param);
			//remove all the links if any
			if (l != null) {
				Iterator i = l.iterator();
				AnnotationAnnotationLink link;
				IObject child;
				while (i.hasNext()) {
					link = (AnnotationAnnotationLink) i.next();
					child = link.getChild();
					if (!((child instanceof TagAnnotation) || 
						(child instanceof UriAnnotation)))  {
						deleteObject(link);
						deleteObject(child);
					}
				}
			}
		} catch (Exception e) {
			handleException(e, "Cannot remove the tag description.");
		}
	}
	
	/** Checks if the session is still alive. */
	void isSessionAlive()
	{
		try {
			EventContext ctx = getAdminService().getEventContext();
			getSessionService().getSession(ctx.sessionUuid);
		} catch (Exception e) {
			dsFactory.sessionExpiredExit();
		}
	}
	
	/** Keeps the services alive. */
	void keepSessionAlive()
	{
		int n = services.size()+reServices.size();
		ServiceInterfacePrx[] entries = new ServiceInterfacePrx[n];
		Iterator<ServiceInterfacePrx> i = services.iterator();
		int index = 0;
		while (i.hasNext()) {
			entries[index] = i.next();
			index++;
		}
		Iterator<Long> j = reServices.keySet().iterator();
		while (j.hasNext()) {
			entries[index] = reServices.get(j.next());
			index++;
		}
		entry.keepAllAlive(entries);
	}
	
	//tmp
	List getFileAnnotations(Set<Long> originalFiles) 
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQueryPrx service = getQueryService();
			ParametersI param = new ParametersI();
			param.addLongs("ids", originalFiles);
			String sql =  "select link from Annotation as link ";
			sql += "where link.file.id in (:ids)";
			return service.findAllByQuery(sql, param);
			
		} catch (Exception e) {
			handleException(e, "Cannot remove the tag description.");
		}
		return new ArrayList();
	}
	
	/**
	 * Projects the specified set of pixels according to the projection's 
	 * parameters. Adds the created image to the passed dataset.
	 * 
	 * @param pixelsID  The id of the pixels set.
	 * @param startT	The timepoint to start projecting from.
	 * @param endT		The timepoint to end projecting.
	 * @param startZ    The first optical section.
	 * @param endZ      The last optical section.
	 * @param stepping  The stepping used to project. Default is <code>1</code>.
	 * @param algorithm The projection's algorithm.
	 * @param channels  The channels to project.
	 * @param datasets  The collection of datasets to add the image to.
	 * @param name      The name of the projected image.
	 * @param pixType   The destination Pixels type. If <code>null</code>, the
     * 					source Pixels set pixels type will be used.
	 * @return The newly created image.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	ImageData projectImage(long pixelsID, int startT, int endT, int startZ, 
						int endZ, int stepping, ProjectionType algorithm, 
						List<Integer> channels, String name, String pixType)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			IProjectionPrx service = getProjectionService();
			PixelsType type = null;
			if (pixType != null) {
				IQueryPrx svc = getQueryService();
				List<IObject> l = svc.findAll(PixelsType.class.getName(), 
										null);
				Iterator<IObject> i = l.iterator();
				PixelsType pt;
				String value;
				while (i.hasNext()) {
					pt = (PixelsType) i.next();
					value = pt.getValue().getValue();
					if (value.equals(pixType)) {
						type = pt;
						break;
					}
				}
			}
			long imageID = service.projectPixels(pixelsID, type, algorithm, 
					startT, endT, channels, stepping, startZ, endZ, name);
			
			return getImage(imageID, new PojoOptionsI().map());
		} catch (Exception e) {
			e.printStackTrace();
			handleException(e, "Cannot project the image.");
		}
		return null;
	}
	
	/**
	 * Returns the image and loaded pixels.
	 * 
	 * @param imageID The id of the image to load.
	 * @param options The options.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	ImageData getImage(long imageID, Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			List<Long> ids = new ArrayList<Long>(1);
			ids.add(imageID);
			Set result = getContainerImages(ImageData.class, ids, options);
			if (result != null && result.size() == 1) {
				Iterator i = result.iterator();
				while (i.hasNext()) {
					return (ImageData) PojoMapper.asDataObject(
							(IObject) i.next());
				}
			}
			return null;
			/*
			StringBuilder sb = new StringBuilder();
			sb.append("select img from Image as img ");
			sb.append("left outer join fetch img.pixels as pix ");
	        sb.append("left outer join fetch pix.pixelsType as pt ");
	        sb.append("where img.id = :id");
	        ParametersI param = new ParametersI();
			param.addLong("id", imageID);
	        return (ImageData) PojoMapper.asDataObject(
	        		getQueryService().findByQuery(sb.toString(), param));
	        		*/
		} catch (Exception e) {
			handleException(e, "Cannot project the image.");
		}
		return null;
	}
	
	/**
	 * Creates default rendering setting for the passed pixels set.
	 * 
	 * @param pixelsID The id of the pixels set to handle.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	RenderingDef createRenderingDef(long pixelsID)
		throws DSOutOfServiceException, DSAccessException
	{
		//TODO: add method to server so that we don't have to make 2 calls.
		try {
			IPixelsPrx svc = getPixelsService();
			Pixels pixels = svc.retrievePixDescription(pixelsID);
			if (pixels == null) return null;
			IRenderingSettingsPrx service = getRenderingSettingsService();
			return service.createNewRenderingDef(pixels);
		} catch (Exception e) {
			handleException(e, "Cannot create settings for: "+pixelsID);
		}
		
		return null;
	}
	
	//TMP: 
	Set loadPlateWells(long plateID, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			List results = null;
			IQueryPrx service = getQueryService();
			StringBuilder sb = new StringBuilder();
			ParametersI param = new ParametersI();
			param.addLong("plateID", plateID);
			
			sb.append("select well from Well as well ");
			sb.append("left outer join fetch well.wellSamples as ws ");
			sb.append("left outer join fetch ws.image as img ");
			
			sb.append("left outer join fetch img.pixels as pix ");
            sb.append("left outer join fetch pix.pixelsType as pt ");
			
            sb.append("where well.plate.id = :plateID");
            results = service.findAllByQuery(sb.toString(), param);
			Iterator i;
			Well well;
			/*
			results = service.findAllByQuery(sb.toString(), param);
			i = results.iterator();
			
			
			List<Long> ids = new ArrayList<Long>();
			while (i.hasNext()) {
				well = (Well) i.next();
				ids.add(well.getId());
			}
			*/
			/*
			sb = new StringBuilder();
			param = new Parameters();
			//param.addLong("plateID", plateID);
			//param.addList("wellIDs", ids);
			sb.append("select ws from WellSample ws ");
			sb.append("left outer join fetch ws.well well ");
			sb.append("left outer join fetch ws.image img ");
			//sb.append("left outer join fetch well.plate plate ");
			//sb.append("left outer join fetch well.wellSamples ");
			
			//sb.append("left outer join fetch ws.imageLinks wsil ");
			//sb.append("left outer join fetch wsil.child img ");
			/*
			sb.append("left outer join fetch ws.image img ");
			sb.append("left outer join fetch img.pixels as pix ");
            sb.append("left outer join fetch pix.pixelsType as pt ");
            sb.append("left outer join fetch pix.pixelsDimensions as pd ");
            sb.append("where ws.well.id in (:wellIDs)");
            */
			/*
			List samples = service.findAllByQuery(sb.toString(), param);
			i = samples.iterator();
			WellSample ws;
			WellSampleData data;
			Map<Long, List<WellSampleData>> 
				map = new HashMap<Long, List<WellSampleData>>();
			List<WellSampleData> list;
			while (i.hasNext()) {
				ws = (WellSample) i.next();
				data = (WellSampleData) PojoMapper.asDataObject(ws);
				well = ws.getWell();
				list = map.get(well.getId());
				if (list == null) {
					list = new ArrayList<WellSampleData>();
					map.put(well.getId(), list);
				}
				list.add(data);
			}
			*/
			Set<DataObject> wells = new HashSet<DataObject>();
			i = results.iterator();
			WellData wellData;
			List<WellSampleData> list;
			Map<Long, List<WellSampleData>> 
				map = new HashMap<Long, List<WellSampleData>>();
			Iterator<WellSample> j;
			WellSample ws;
			
			while (i.hasNext()) {
				well = (Well) i.next();
				wellData = (WellData) PojoMapper.asDataObject(well);
				/*
				list = new ArrayList<WellSampleData>();
				j = well.iterateWellSamples();
				while (j.hasNext()) {
					ws = j.next();
					list.add((WellSampleData) PojoMapper.asDataObject(ws));
				}
				wellData.setWellSamples(list);
				*/
				wells.add(wellData);
			}

			
			return wells;
		} catch (Exception e) {
			handleException(e, "Cannot load plate");
		}
		return new HashSet();
	}
	
	//Should be moved to server
	Set loadScreenPlate(Class rootType, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			List<IObject> results;
			IQueryPrx service = getQueryService();
			StringBuilder sb = new StringBuilder();
			ParametersI param = new ParametersI();
			if (ScreenData.class.equals(rootType)) {
				sb.append("select screen from Screen screen ");
				sb.append("left outer join fetch "
		                   + "screen.annotationLinksCountPerOwner s_a_c ");
	            sb.append("left outer join fetch screen.plateLinks spl ");
	            sb.append("left outer join fetch spl.child plate ");
	            if (userID >= 0) {
	            	sb.append("where screen.details.owner.id = :uid");
	            	param.addLong("uid", userID);
	            }
				results = service.findAllByQuery(sb.toString(), param);
				Set<Long> plates = new HashSet<Long>();
				Screen s;
				for (IObject o : results) {
					s = (Screen) o;
					for (ScreenPlateLink link : s.copyPlateLinks()) {
						plates.add(link.getChild().getId().getValue());
					}
				}

	            if (plates.size() > 0) {
	            	String sql = "select p from Plate p "
	                + "left outer join fetch p.annotationLinksCountPerOwner " +
	                "where p.id in (:list)";
	            	param.addLongs("list", plates);
	            	service.findAllByQuery(sql, param);
	            }
	            return PojoMapper.asDataObjects(results);
			} else if (PlateData.class.equals(rootType)) {
				sb.append("select p from Plate p ");
				sb.append("left outer join fetch "
		                   + "p.annotationLinksCountPerOwner p_a_c ");
				if (userID >= 0) {
	            	sb.append("where p.details.owner.id = :uid");
	            	param.addLong("uid", userID);
	            }
				results = service.findAllByQuery(sb.toString(), param);
				return PojoMapper.asDataObjects(results);
			}
			
		} catch (Exception e) {
			handleException(e, "Cannot load screen");
		}
		return new HashSet();
	}
	
	/**
	 * Loads the acquisition object related to the passed image.
	 * 
	 * @param imageID The id of image object to handle.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Object loadImageAcquisitionData(long imageID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		PojoOptions po = new PojoOptions();
		po.acquisitionData();
		List<Long> ids = new ArrayList<Long>(1);
		ids.add(imageID);
		IContainerPrx service = getPojosService();
        try {
        	List images = service.getImages(Image.class.getName(), ids, 
        			po.map());
        	if (images != null && images.size() == 1)
        		return new ImageAcquisitionData((Image) images.get(0));
		} catch (Exception e) {
			handleException(e, "Cannot load image acquisition data.");
		}
       return null;
	}
	
	/**
	 * Loads the acquisition metadata related to the specified channel.
	 * 
	 * @param channelID The id of the channel.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Object loadChannelAcquisitionData(long channelID)
		throws DSOutOfServiceException, DSAccessException
	{
		//stage Label
		isSessionAlive();
		try {
			IMetadataPrx service = getMetadataService();
			List<Long> ids = new ArrayList<Long>(1);
			ids.add(channelID);
			List l = service.loadChannelAcquisitionData(ids);
			if (l != null && l.size() == 1) {
				LogicalChannel lc = (LogicalChannel) l.get(0);
				return new ChannelAcquisitionData(lc);
			}
			return null;
		} catch (Exception e) {
			handleException(e, "Cannot load channel acquisition data.");
		}
		return null;
		/*
		IQueryPrx service = getQueryService();
		StringBuilder sb;
		ParametersI param;
		sb = new StringBuilder();
		param = new ParametersI();
		sb.append("select channel from LogicalChannel as channel ");
		sb.append("left outer join fetch channel.detectorSettings as ds ");
        sb.append("left outer join fetch channel.lightSourceSettings as lss ");
        sb.append("left outer join fetch ds.detector as detector ");
        sb.append("left outer join fetch detector.type as dt ");
        sb.append("left outer join fetch ds.binning as binning ");
        sb.append("left outer join fetch lss.lightSource as light ");
        sb.append("left outer join fetch light.type as lt ");
        sb.append("where channel.id = :id");
        param.addLong("id", channelID);
		try {
			IObject r = service.findByQuery(sb.toString(), param);
			ChannelAcquisitionData data = new ChannelAcquisitionData(
					(LogicalChannel) r);
			String kind = data.getLightSourceKind();
			sb = new StringBuilder();
			param = new ParametersI();
			if (ChannelAcquisitionData.LASER.equals(kind)) {
				sb.append("select laser from Laser as laser ");
				sb.append("left outer join fetch laser.type as type ");
				sb.append("left outer join fetch laser.laserMedium as medium ");
				sb.append("left outer join fetch laser.pulse as pulse ");
		        sb.append("where laser.id = :id");
		        param.addLong("id", data.getLightSourceId());
		        r = service.findByQuery(sb.toString(), param);
		        data.setLightSource((LightSource) r);
			} else if (ChannelAcquisitionData.ARC.equals(kind)) {
				sb.append("select arc from Arc as arc ");
				sb.append("left outer join fetch arc.type as type ");
		        sb.append("where arc.id = :id");
		        param.addLong("id", data.getLightSourceId());
		        r = service.findByQuery(sb.toString(), param);
		        data.setLightSource((LightSource) r);
			} else if (ChannelAcquisitionData.FILAMENT.equals(kind)) {
				sb.append("select filament from Filament as filament ");
				sb.append("left outer join fetch filament.type as type ");
		        sb.append("where filament.id = :id");
		        param.addLong("id", data.getLightSourceId());
		        r = service.findByQuery(sb.toString(), param);
		        data.setLightSource((LightSource) r);
			} 
            return data;
		} catch (Exception e) {
			handleException(e, "Cannot load channel acquisition data.");
		}
		return null;
		*/
		
	}
	
	/**
	 * Returns the enumeration corresponding to the passed string or 
	 * <code>null</code> if none found.
	 * 
	 * @param klass The class the enumeration is for.
	 * @param value The value of the enumeration.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	IObject getEnumeration(Class klass, String value)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			//IPixelsPrx service = getPixelsService();
			IQueryPrx service = getQueryService();
			return service.findByString(klass.getName(), "value", value);
			//return service.getEnumeration(klass.getName(), value);
		} catch (Exception e) {
			handleException(e, "Cannot find the enumeration's value.");
		}
		return null;
	}
	
	/**
	 * Returns the enumerations corresponding to the passed type or 
	 * <code>null</code> if none found.
	 * 
	 * @param klassName The name of the class the enumeration is for.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<EnumerationObject> getEnumerations(String klassName)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		List<EnumerationObject> r;
		try {
			r = enumerations.get(klassName);
			if (r != null) return r;
			IPixelsPrx service = getPixelsService();
			List<IObject> l = service.getAllEnumerations(klassName);
			r = new ArrayList<EnumerationObject>(); 
			if (l == null) return r;
			Iterator<IObject> i = l.iterator();
			while (i.hasNext()) {
				r.add(new EnumerationObject(i.next()));
			}
			enumerations.put(klassName, r);
			return r;
		} catch (Exception e) {
			handleException(e, "Cannot find the enumeration's value.");
		}
		return new ArrayList<EnumerationObject>();
	}
	
	Collection loadTags(Long id, Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		
		isSessionAlive();
		try {
			IMetadataPrx service = getMetadataService();
			List<Long> ids = new ArrayList<Long>(1);
			ids.add(id);
			Map m = service.loadTagContent(ids, options);
			if (m == null || m.size() == 0)
				return new ArrayList();
			return PojoMapper.asDataObjects((Collection) m.get(id));
		} catch (Exception e) {
			handleException(e, "Cannot find the Tags.");
		}
		return new ArrayList();
	}
	
	Collection loadTagSets(Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		
		isSessionAlive();
		try {
			IMetadataPrx service = getMetadataService();
			List<IObject> list = service.loadTagSets(options);
			List result = new ArrayList();
			if (list == null) return result;
			Iterator<IObject> i = list.iterator();
			AnnotationAnnotationLink link;
			Annotation parent, child;
			TagAnnotationData tagSet;
			Map<Long, TagAnnotationData> 
				sets = new HashMap<Long, TagAnnotationData>();
			Set<TagAnnotationData> tags;
			List<Long> ids = new ArrayList<Long>();
			IObject object;
			while (i.hasNext()) {
				object = i.next();
				if (object instanceof TagAnnotation) {
					result.add(new TagAnnotationData((TagAnnotation) object));
				} else if (object instanceof AnnotationAnnotationLink) {
					link = (AnnotationAnnotationLink) object;
					parent = link.getParent();
					child = link.getChild();
					if (sets.get(parent.getId()) == null) {
						tagSet = new TagAnnotationData((TagAnnotation) parent);
						sets.put(parent.getId().getValue(), tagSet);
						result.add(tagSet);
						tagSet.setTags(new HashSet<TagAnnotationData>());
					} else 
						tagSet = sets.get(parent.getId().getValue());
					tags = tagSet.getTags();
					tags.add(new TagAnnotationData((TagAnnotation) child));
					ids.add(child.getId().getValue());
				}
			}
			return result;
		} catch (Exception e) {
			handleException(e, "Cannot find the Tags.");
		}
		return new ArrayList();
	}
	
	/**
	 * Returns the collection of plane info object related to the specified
	 * pixels set.
	 * 
	 * @param pixelsID The id of the pixels set.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<IObject> loadPlaneInfo(long pixelsID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		IQueryPrx service = getQueryService();
		StringBuilder sb;
		ParametersI param;
		sb = new StringBuilder();
		param = new ParametersI();
		sb.append("select info from PlaneInfo as info ");
        sb.append("where pixels.id = :id");
        param.addLong("id", pixelsID);
        try {
        	return service.findAllByQuery(sb.toString(), param);
		} catch (Exception e) {
			handleException(e, 
					"Cannot load the plane info for pixels: "+pixelsID);
		}
		return new ArrayList<IObject>();
	}
	
	/**
	 * Fills the enumerations.
	 * 
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	void fillEnumerations()
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		List<EnumerationObject> l = getEnumerations(
				OmeroMetadataService.IMMERSION);
		l = getEnumerations(OmeroMetadataService.CORRECTION);
		l = getEnumerations(OmeroMetadataService.MEDIUM);
		l = getEnumerations(OmeroMetadataService.DETECTOR_TYPE);
		l = getEnumerations(OmeroMetadataService.BINNING);
		l = getEnumerations(OmeroMetadataService.CONTRAST_METHOD);
		l = getEnumerations(OmeroMetadataService.ILLUMINATION_TYPE);
		l = getEnumerations(OmeroMetadataService.PHOTOMETRIC_INTERPRETATION);
		l = getEnumerations(OmeroMetadataService.ACQUISITION_MODE);
		l = getEnumerations(OmeroMetadataService.LASER_MEDIUM);
		l = getEnumerations(OmeroMetadataService.LASER_TYPE);
		l = getEnumerations(OmeroMetadataService.LASER_PULSE);
		l = getEnumerations(OmeroMetadataService.ARC_TYPE);
		l = getEnumerations(OmeroMetadataService.FILAMENT_TYPE);
	}
	
	
	/**
	 * Deletes the passed object using the {@link IDelete} service.
	 * Returns an emtpy list of nothing prevent the delete to happen,
	 * otherwise returns a list of objects preventing the delete to happen.
	 * 
	 * @param objectType The type of object to delete.
	 * @param objectID   The id of the object to delete.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<IObject> removeObject(Class objectType, Long objectID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IDeletePrx service = getDeleteService();
			if (ImageData.class.equals(objectType)) {
				List r = service.checkImageDelete(objectID, false);
				if (r == null || r.size() == 0) {
					service.deleteImage(objectID, true);
					return r;
				}
				return r;
			}
		} catch (Exception e) {
			handleException(e, "Cannot delete: "+objectType+" "+objectID);
		}
		
		return new ArrayList<IObject>();
	}
	
	/**
	 * Deletes the specified image.
	 * 
	 * @param object The image to delete.
	 * @return See above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	Object deleteImage(Image object)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IDeletePrx service = getDeleteService();
			service.deleteImage(object.getId().getValue(), true);
		} catch (Exception e) {
			handleException(e, "Cannot delete the image: "+object.getId());
		}
		
		return new ArrayList<IObject>();
	}
	
	/** 
	 * Returns the list of object than can prevent the delete.
	 * 
	 * @param object The object to handle.
	 * @return See above.
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	List<IObject> checkImage(Image object)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IDeletePrx service = getDeleteService();
			return service.checkImageDelete(object.getId().getValue(), true);
		} catch (Exception e) {
			handleException(e, "Cannot delete the image: "+object.getId());
		}
		return new ArrayList<IObject>();
	}
	
	/**
	 * Returns the fs file system view.
	 * 
	 * @param defaultPath The default directory.
	 * @return See above.
	 */
	FileSystemView getFSFileSystemView(String defaultPath)
	{
		if (systemView != null) return systemView;
		systemView = new FSFileSystemView(defaultPath, monitorPrx);
		return systemView;
	}
	
	/**
	 * Monitors the specified directory.
	 * 
	 * @param directory The directory to watch.
	 * @param whiteList	The types of images to watch.
	 * @return See above.
	 */
	Object monitor(String directory, String[] whiteList, DataObject container)
	{
		String[] blackList = new String[1];
		blackList[0] = "";
		MonitorClientImpl mClient = new MonitorClientImpl(metadataStore, 
				container);
		Communicator c = getIceCommunicator();
		String name = "monitorClient";
		ObjectAdapter adapter = c.createObjectAdapter("omerofs.MonitorClient");
		adapter.add(mClient, c.stringToIdentity(name));
		adapter.activate();

		MonitorClientPrx mClientProxy =
			monitors.MonitorClientPrxHelper.uncheckedCast(                
				adapter.createProxy(c.stringToIdentity(name)));
		try {
			System.err.println(directory);
			String id = monitorPrx.createMonitor(EventType.Create, directory, 
					whiteList, blackList, PathMode.Flat, mClientProxy);
			monitorIDs.add(id);
			monitorPrx.startMonitor(id);
			System.err.println(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Removes the rendering service corresponding to the pixels set ID.
	 * 
	 * @param pixelsID The pixels set Id to handle.
	 */
	void removeREService(long pixelsID)
	{
		reServices.remove(pixelsID);
	}
	
	//tmp
	static MonitorServerPrx getMonitorServer() { return monitorPrx; }
	
}
