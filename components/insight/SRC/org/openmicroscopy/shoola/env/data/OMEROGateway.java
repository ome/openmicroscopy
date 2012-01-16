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
import java.nio.ByteBuffer;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Map.Entry;

//Third-party libraries
import loci.formats.FormatException;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.login.UserCredentials;
import org.openmicroscopy.shoola.env.data.model.AdminObject;
import org.openmicroscopy.shoola.env.data.model.EnumerationObject;
import org.openmicroscopy.shoola.env.data.model.ImportableObject;
import org.openmicroscopy.shoola.env.data.model.MovieExportParam;
import org.openmicroscopy.shoola.env.data.model.ROIResult;
import org.openmicroscopy.shoola.env.data.model.FigureParam;
import org.openmicroscopy.shoola.env.data.model.SaveAsParam;
import org.openmicroscopy.shoola.env.data.model.ScriptObject;
import org.openmicroscopy.shoola.env.data.model.TableParameters;
import org.openmicroscopy.shoola.env.data.model.TableResult;
import org.openmicroscopy.shoola.env.data.util.ModelMapper;
import org.openmicroscopy.shoola.env.data.util.PojoMapper;
import org.openmicroscopy.shoola.env.data.util.SearchDataContext;
import org.openmicroscopy.shoola.env.data.util.StatusLabel;
import org.openmicroscopy.shoola.env.rnd.PixelsServicesFactory;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import org.openmicroscopy.shoola.env.rnd.RndProxyDef;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import Ice.ConnectionLostException;
import Ice.ConnectionRefusedException;
import Ice.ConnectionTimeoutException;
import omero.ResourceError;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.system.UpgradeCheck;
import omero.ApiUsageException;
import omero.AuthenticationException;
import omero.ClientError;
import omero.ConcurrencyException;
import omero.InternalException;
import omero.LockTimeout;
import omero.MissingPyramidException;
import omero.RLong;
import omero.RString;
import omero.RType;
import omero.SecurityViolation;
import omero.ServerError;
import omero.SessionException;
import omero.client;
import omero.rtypes;
import omero.api.ExporterPrx;
import omero.api.IAdminPrx;
import omero.api.IConfigPrx;
import omero.api.IContainerPrx;
import omero.api.IDeletePrx;
import omero.api.IMetadataPrx;
import omero.api.IPixelsPrx;
import omero.api.IProjectionPrx;
import omero.api.IQueryPrx;
import omero.api.IRenderingSettingsPrx;
import omero.api.IRepositoryInfoPrx;
import omero.api.IRoiPrx;
import omero.api.IScriptPrx;
import omero.api.ITimelinePrx;
import omero.api.IUpdatePrx;
import omero.api.RawFileStorePrx;
import omero.api.RawPixelsStorePrx;
import omero.api.RenderingEnginePrx;
import omero.api.RoiOptions;
import omero.api.RoiResult;
import omero.api.SearchPrx;
import omero.api.ServiceFactoryPrx;
import omero.api.ServiceInterfacePrx;
import omero.api.StatefulServiceInterfacePrx;
import omero.api.ThumbnailStorePrx;
import omero.api.delete.DeleteCommand;
import omero.api.delete.DeleteHandlePrx;
import omero.constants.projection.ProjectionType;
import omero.grid.BoolColumn;
import omero.grid.Column;
import omero.grid.Data;
import omero.grid.DoubleColumn;
import omero.grid.ImageColumn;
import omero.grid.LongColumn;
import omero.grid.RepositoryMap;
import omero.grid.RepositoryPrx;
import omero.grid.RoiColumn;
import omero.grid.ScriptProcessPrx;
import omero.grid.SharedResourcesPrx;
import omero.grid.StringColumn;
import omero.grid.TablePrx;
import omero.grid.WellColumn;
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
import omero.model.ExperimenterGroupI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.GroupExperimenterMap;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageI;
import omero.model.Instrument;
import omero.model.Laser;
import omero.model.Line;
import omero.model.LogicalChannel;
import omero.model.LongAnnotation;
import omero.model.Namespace;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.Permissions;
import omero.model.Pixels;
import omero.model.PixelsI;
import omero.model.PixelsType;
import omero.model.Plate;
import omero.model.PlateAcquisition;
import omero.model.PlateAcquisitionI;
import omero.model.PlateI;
import omero.model.Polyline;
import omero.model.Project;
import omero.model.ProjectI;
import omero.model.RenderingDef;
import omero.model.Roi;
import omero.model.Screen;
import omero.model.ScreenI;
import omero.model.Shape;
import omero.model.TagAnnotation;
import omero.model.TagAnnotationI;
import omero.model.TermAnnotation;
import omero.model.TermAnnotationI;
import omero.model.TimestampAnnotation;
import omero.model.TimestampAnnotationI;
import omero.model.Well;
import omero.model.WellSample;
import omero.sys.Parameters;
import omero.sys.ParametersI;
import pojos.BooleanAnnotationData;
import pojos.ChannelAcquisitionData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.FileData;
import pojos.GroupData;
import pojos.ImageAcquisitionData;
import pojos.ImageData;
import pojos.InstrumentData;
import pojos.LightSourceData;
import pojos.LongAnnotationData;
import pojos.MultiImageData;
import pojos.PixelsData;
import pojos.PlateAcquisitionData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ROICoordinate;
import pojos.ROIData;
import pojos.RatingAnnotationData;
import pojos.ScreenData;
import pojos.ShapeData;
import pojos.TagAnnotationData;
import pojos.TermAnnotationData;
import pojos.TextualAnnotationData;
import pojos.TimeAnnotationData;
import pojos.WellData;
import pojos.WellSampleData;
import pojos.WorkflowData;

/** 
 * Unified access point to the various <i>OMERO</i> services.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class OMEROGateway
{
	
	/** Identifies the image as root. */
	private static final String REF_IMAGE = "/Image";
	
	/** Identifies the dataset as root. */
	private static final String REF_DATASET = "/Dataset";
	
	/** Identifies the project as root. */
	private static final String REF_PROJECT = "/Project";
	
	/** Identifies the screen as root. */
	private static final String REF_SCREEN = "/Screen";
	
	/** Identifies the plate as root. */
	private static final String REF_PLATE = "/Plate";
	
	/** Identifies the ROI as root. */
	private static final String REF_ROI = "/Roi";
	
	/** Identifies the PlateAcquisition as root. */
	private static final String REF_PLATE_ACQUISITION = "/PlateAcquisition";

	/** Identifies the PlateAcquisition as root. */
	private static final String REF_WELL = "/Well";
	
	/** Identifies the Tag. */
	private static final String REF_ANNOTATION = "/Annotation";
	
	/** Identifies the Tag. */
	private static final String REF_TAG = "/TagAnnotation";
	
	/** Identifies the Term. */
	private static final String REF_TERM = "/TermAnnotation";
	
	/** Identifies the File. */
	private static final String REF_FILE= "/FileAnnotation";
	
	/** Indicates to keep a certain type of annotations. */
	static final String KEEP = "KEEP";
	
	/** The default MIME type. */
	private static final String				DEFAULT_MIMETYPE = 
		"application/octet-stream";
	
	/** String used to identify the overlays. */
	private static final String				OVERLAYS = "Overlays";
	
	/** Maximum size of pixels read at once. */
	private static final int				INC = 262144;//256000;
	
	/** The maximum number read at once. */
	private static final int				MAX_BYTES = 1024;
	
	/** 
	 * The maximum number of thumbnails retrieved before restarting the
	 * thumbnails service.
	 */
	static final int						MAX_RETRIEVAL = 50;//100;

	/** Maximum number of rows to retrieve at one time from a table. */
	private static final int				MAX_TABLE_ROW_RETRIEVAL = 100000;

	/** The collection of escaping characters we allow in the search. */
	private static final List<Character>	SUPPORTED_SPECIAL_CHAR;
	
	/** The collection of escaping characters we allow in the search. */
	private static final List<String>		WILD_CARDS;
	
	/** Identifies the client. */
	private static final String				AGENT = "OMERO.insight";
	
	/** The collection of system groups. */
	private static final List<String>		SYSTEM_GROUPS;

	/** The collection of scripts that have a UI available. */
	private static final List<String>		SCRIPTS_UI_AVAILABLE;
	
	/** The collection of scripts that have a UI available. */
	private static final List<String>		SCRIPTS_NOT_AVAILABLE_TO_USER;

	static {
		SUPPORTED_SPECIAL_CHAR = new ArrayList<Character>();
		SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('-'));
		SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('+'));
		SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('['));
		SUPPORTED_SPECIAL_CHAR.add(Character.valueOf(']'));
		SUPPORTED_SPECIAL_CHAR.add(Character.valueOf(')'));
		SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('('));
		SUPPORTED_SPECIAL_CHAR.add(Character.valueOf(':'));
		SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('|'));
		SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('!'));
		SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('{'));
		SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('}'));
		SUPPORTED_SPECIAL_CHAR.add(Character.valueOf('^'));
		WILD_CARDS = new ArrayList<String>();
		WILD_CARDS.add("*");
		WILD_CARDS.add("?");
		WILD_CARDS.add("~");
		SYSTEM_GROUPS = new ArrayList<String>();
		SYSTEM_GROUPS.add(GroupData.SYSTEM);
		SYSTEM_GROUPS.add(GroupData.USER);
		SYSTEM_GROUPS.add(GroupData.GUEST);
		
		//script w/ a UI.
		SCRIPTS_UI_AVAILABLE = new ArrayList<String>();
		
		SCRIPTS_UI_AVAILABLE.add(FigureParam.ROI_SCRIPT);
		SCRIPTS_UI_AVAILABLE.add(FigureParam.THUMBNAIL_SCRIPT);
		SCRIPTS_UI_AVAILABLE.add(FigureParam.MOVIE_SCRIPT);
		SCRIPTS_UI_AVAILABLE.add(FigureParam.SPLIT_VIEW_SCRIPT);
		SCRIPTS_UI_AVAILABLE.add(MovieExportParam.MOVIE_SCRIPT);
		
		SCRIPTS_NOT_AVAILABLE_TO_USER = new ArrayList<String>();
		SCRIPTS_NOT_AVAILABLE_TO_USER.add(
				ScriptObject.REGION_PATH+"Populate_ROI.py");
		SCRIPTS_NOT_AVAILABLE_TO_USER.add(
				ScriptObject.ANALYSIS_PATH+"FLIM.py");
		SCRIPTS_NOT_AVAILABLE_TO_USER.add(
				ScriptObject.ANALYSIS_PATH+"flim-omero.py");
		SCRIPTS_NOT_AVAILABLE_TO_USER.add(
				ScriptObject.SETUP_PATH+"FLIM_initialise.py");
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
	private ServiceFactoryPrx 						entryEncrypted;
	
	/**
	 * The entry point provided by the connection library to access the various
	 * <i>OMERO</i> services.
	 */
	private ServiceFactoryPrx 						entryUnencrypted;

	/** The thumbnail service. */
	private ThumbnailStorePrx						thumbnailService;

	/** The raw file store. */
	private RawFileStorePrx							fileStore;
	
	/** The raw pixels store. */
	private RawPixelsStorePrx						pixelsStore;

	/** The projection service. */
	private IProjectionPrx							projService;

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
	
	/** The metadata service. */
	private IMetadataPrx							metadataService;
	
	/** The scripting service. */
	private IScriptPrx								scriptService;
	
	/** The ROI (Region of Interest) service. */
	private IRoiPrx									roiService;
	
	/** The time service. */
	private ITimelinePrx							timeService;
	
	/** The shared resources. */
	private SharedResourcesPrx						sharedResources;
	
	/** Tells whether we're currently connected and logged into <i>OMERO</i>. */
	private boolean                 				connected;

	/** 
	 * Used whenever a broken link is detected to get the Login Service and
	 * try re-establishing a valid link to <i>OMERO</i>. 
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
	 * OMERO Server using a secure connection. 
	 */
	private client 									secureClient;

	/** 
	 * The Blitz client object, this is the entry point to the 
	 * OMERO Server using non secure data transfer
	 */
	private client 									unsecureClient;
	
	/** Map hosting the enumeration required for metadata. */
	private Map<String, List<EnumerationObject>>	enumerations;
	
	/** Collection of services to keep alive. */
	private Set<ServiceInterfacePrx>				services;
	
	/** Collection of services to keep alive. */
	private Map<Long, StatefulServiceInterfacePrx>	reServices;

	/** The service to import files. */
	private OMEROMetadataStoreClient				importStore;
	
	/** The collection of system groups. */
	private List<ExperimenterGroup>					systemGroups;
	
	/** Keep track of the file system view. */
	private Map<Long, FSFileSystemView>				fsViews;
	
	/** Checks if the session is still alive. */
	synchronized void isSessionAlive()
	{
		if (!connected) return;
		try {
			getAdminService().getEventContext();
		} catch (Exception e) {
			Throwable cause = e.getCause();
			int index = DataServicesFactory.SERVER_OUT_OF_SERVICE;
			if (cause instanceof ConnectionLostException ||
				e instanceof ConnectionLostException)
				index = DataServicesFactory.LOST_CONNECTION;
			connected = false;
			dsFactory.sessionExpiredExit(index, cause);
		}
	}

	/**
	 * Returns the identifier of the specified script.
	 * 
	 * @param name The name of the script.
	 * @param message The error message.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException       If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	private long getScriptID(String name, String message)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			IScriptPrx svc = getScriptService();
			if (svc == null) svc = getScriptService();
			return svc.getScriptID(name);
		} catch (Exception e) {
			handleException(e, message);
		}
		return -1;
	}

	/**
	 * Returns the specified script.
	 * 
	 * @param scriptID The identifier of the script to run.
	 * @param parameters The parameters to pass to the script.
	 * @return See above.
	 * @throws ProcessException If an error occurred while running the script.
	 */
	private ScriptCallback runScript(long scriptID, 
			Map<String, RType> parameters)
		throws ProcessException
	{
		ScriptCallback cb = null;
		try {
	         IScriptPrx svc = getScriptService();
	         if (svc == null) svc = getScriptService();
	         //scriptID, parameters, timeout (5s if null)
	         ScriptProcessPrx prx = svc.runScript(scriptID, parameters, null);
	         cb = new ScriptCallback(scriptID, secureClient, prx);
		} catch (Exception e) {
			throw new ProcessException("Cannot run script with ID:"+scriptID, 
					e);
		}
		return cb;
	}
	
	/**
	 * Retrieves the system groups.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	private List<ExperimenterGroup> getSystemGroups()
		throws DSOutOfServiceException, DSAccessException
	{
		if (systemGroups != null) return systemGroups;
		isSessionAlive();
		try {
			List<RType> names = new ArrayList<RType>();
			Iterator<String> j = SYSTEM_GROUPS.iterator();
			while (j.hasNext()) {
				names.add(omero.rtypes.rstring(j.next()));
			}
			systemGroups = new ArrayList<ExperimenterGroup>();
			ParametersI params = new ParametersI();
			params.map.put("names", omero.rtypes.rlist(names));
			String sql = "select g from ExperimenterGroup as g ";
			sql += "where g.name in (:names)";
			IQueryPrx service = getQueryService();
			if (service == null) service = getQueryService(); 
			List<IObject> l = getQueryService().findAllByQuery(sql, params);
			Iterator<IObject> i = l.iterator();
			ExperimenterGroup group;
			String name;
			while (i.hasNext()) {
				group = (ExperimenterGroup) i.next();
				name = group.getName().getValue();
				if (SYSTEM_GROUPS.contains(name)) //to be on the save side
					systemGroups.add(group);
			}
			/*
			IAdminPrx svc = getAdminService();
			List<ExperimenterGroup> groups = svc.lookupGroups();
			Iterator<ExperimenterGroup> i = groups.iterator();
			ExperimenterGroup group;
			String name;
			while (i.hasNext()) {
				group =  i.next();
				name = group.getName().getValue();
				if (SYSTEM_GROUPS.contains(name))
					systemGroups.add(group);
			}
			*/
		} catch (Exception e) {
			handleException(e, "Cannot retrieve the system groups.");
		}
		return systemGroups;
	}
	
	/**
	 * Returns the system group corresponding to the passed name.
	 * 
	 * @param name The name to handle.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	private ExperimenterGroup getSystemGroup(String name)
		throws DSOutOfServiceException, DSAccessException
	{
		getSystemGroups();
		Iterator<ExperimenterGroup> i = systemGroups.iterator();
		
		ExperimenterGroup g = null;
		while (i.hasNext()) {
			g = (ExperimenterGroup) i.next();
			if (g.getName() != null && name.equals(g.getName().getValue()))
				return g;
		}
		return g;
	}
	
	/**
	 * Creates a table with the overlays.
	 * 
	 * @param imageID The id of the image.
	 * @param table   The table to handle.
	 * @return See above
	 * @throws DSAccessException If an error occurred while trying to 
	 *                           retrieve data from OMEDS service.
	 */
	private TableResult createOverlay(long imageID, TablePrx table)
		throws DSAccessException
	{
		if (table == null) return null;
		try {
			Column[] cols = table.getHeaders();
			int imageIndex = -1;
			int roiIndex = -1;
			int colorIndex = -1;
			int size = 0;
			for (int i = 0; i < cols.length; i++) {
				if (cols[i] instanceof ImageColumn) {
					imageIndex = i;
					size++;
				} else if (cols[i] instanceof RoiColumn) {
					roiIndex = i;
					size++;
				} else if (cols[i] instanceof LongColumn) {
					if ("Color".equals(cols[i].name)) {
						colorIndex = i;
						size++;
					}
				}
			}
			if (imageIndex == -1 || roiIndex == -1) return null;;
			String[] headers = new String[size];
			String[] headersDescriptions = new String[size];
			headers[0] = cols[imageIndex].name;
			headersDescriptions[0] = cols[imageIndex].description;
			
			headers[1] = cols[roiIndex].name;
			headersDescriptions[1] = cols[roiIndex].description;
			
			headers[1] = cols[roiIndex].name;
			headersDescriptions[1] = cols[roiIndex].description;
			
			int n = (int) table.getNumberOfRows();
			Data d;
			Column column;
			long[] a = {imageIndex, roiIndex, colorIndex};
			long[] b = new long[0];
	
			d = table.slice(a, b);
			List<Integer> rows = new ArrayList<Integer>();
			column = d.columns[imageIndex];
			Long value;
			if (column instanceof ImageColumn) {
				for (int j = 0; j < n; j++) {
					value = ((ImageColumn) column).values[j];
					if (value == imageID)
						rows.add(j);
				}
			}
			
			Integer row;
			Object[][] data = new Object[rows.size()][size];
			int k = 0;
			Iterator<Integer> r = rows.iterator();
			column = d.columns[roiIndex];
			Column columnColor = null;
			if (colorIndex != -1) columnColor = d.columns[colorIndex];
			while (r.hasNext()) {
				row = r.next();
				data[k][0] = row;
				data[k][1] = ((RoiColumn) column).values[row];
				if (columnColor != null) 
					data[k][2] = ((LongColumn) columnColor).values[row];
				k++;
			}
			table.close();
			return new TableResult(data, headers);
		} catch (Exception e) {
			try {
				if (table != null) table.close();
			} catch (Exception ex) {
				//Digest exception
			}
			throw new DSAccessException("Unable to read the table.");
		}
	}

	/**
	 * Translates a set of table results into an array.
	 * @param src Source data from the table.
	 * @param dst Destination array.
	 * @param offset Offset within the destination array from which to copy
	 * data into.
	 * @param length Number of rows of data to be copied.
	 */
	private void translateTableResult(Data src, Object[][] dst, int offset,
	                                  int length, Map<Integer, Integer> indexes)
	{
		Column[] cols = src.columns;
		Column column;
		for (int i = 0; i < cols.length; i++) {
			column = cols[i];
			if (column instanceof LongColumn) {
				for (int j = 0; j < length; j++) {
					dst[j + offset][i] =
						((LongColumn) column).values[j];
				}
			} else if (column instanceof DoubleColumn) {
				for (int j = 0; j < length; j++) {
					dst[j + offset][i] =
						((DoubleColumn) column).values[j];
				}
			} else if (column instanceof StringColumn) {
				for (int j = 0; j < length; j++) {
					dst[j + offset][i] =
						((StringColumn) column).values[j];
				}
			} else if (column instanceof BoolColumn) {
				for (int j = 0; j < length; j++) {
					dst[j + offset][i] =
						((BoolColumn) column).values[j];
				}
			} else if (column instanceof RoiColumn) {
				indexes.put(TableResult.ROI_COLUMN_INDEX, i);
				for (int j = 0; j < length; j++) {
					dst[j + offset][i] =
						((RoiColumn) column).values[j];
				}
			} else if (column instanceof ImageColumn) {
				indexes.put(TableResult.IMAGE_COLUMN_INDEX, i);
				for (int j = 0; j < length; j++) {
					dst[j + offset][i] =
						((ImageColumn) column).values[j];
				}
			} else if (column instanceof WellColumn) { 
				indexes.put(TableResult.WELL_COLUMN_INDEX, i);
				for (int j = 0; j < length; j++) {
					dst[j + offset][i] =
						((WellColumn) column).values[j];
				}
			} 
		}
	}

	/**
	 * Transforms a set of rows for the passed table.
	 * 
	 * @param table The table to convert.
	 * @param rows The rows of the table to convert.
	 * @return See above
	 * @throws DSAccessException If an error occurred while trying to 
	 *                           retrieve data from OMEDS service.
	 */
	private TableResult createTableResult(TablePrx table, long[] rows)
		throws DSAccessException
	{
		if (table == null) return null;
		try {
			Column[] cols = table.getHeaders();
			String[] headers = new String[cols.length];
			String[] headersDescriptions = new String[cols.length];
			for (int i = 0; i < cols.length; i++) {
				headers[i] = cols[i].name;
				headersDescriptions[i] = cols[i].description;
			}
			int totalRowCount = rows.length;
			Object[][] data = new Object[totalRowCount][cols.length];
			Data d;
			long[] columns = new long[cols.length];
			for (int i = 0; i < cols.length; i++) {
				columns[i] = i;
			}

			int rowOffset = 0;
			int rowCount = 0;
			int rowsToGo = totalRowCount;
			long[] rowSubset;
			Map<Integer, Integer> indexes = new HashMap<Integer, Integer>();
			while (rowsToGo > 0) {
				rowCount = (int) Math.min(MAX_TABLE_ROW_RETRIEVAL,
				                          totalRowCount - rowOffset);
				rowSubset = new long[rowCount];
				System.arraycopy(rows, rowOffset, rowSubset, 0, rowCount);
				d = table.slice(columns, rowSubset);
				for (int i = 0; i < cols.length; i++) {
					translateTableResult(d, data, rowOffset, rowCount, indexes);
				}
				rowOffset += rowCount;
				rowsToGo -= rowCount;
			}
			table.close();
			TableResult tr = new TableResult(data, headers);
			tr.setIndexes(indexes);
			return tr;
		} catch (Exception e) {
			try {
				if (table != null) table.close();
			} catch (Exception ex) {
				//Digest exception
			}
			throw new DSAccessException("Unable to read the table.", e);
		}
	}

	/**
	 * Transforms the passed table data for a given image.
	 * 
	 * @param table The table to convert.
	 * @param key The key of the <code>where</code> clause.
	 * @param id The identifier of the object to retrieve rows for.
	 * @return See above
	 * @throws DSAccessException If an error occurred while trying to 
	 *                           retrieve data from OMEDS service.
	 */
	private TableResult createTableResult(TablePrx table, String key, long id)
		throws DSAccessException
	{
		if (table == null) return null;
		try {
			key = "("+key+"==%d)";
			long totalRowCount = table.getNumberOfRows();
			long[] rows = table.getWhereList(String.format(key, id), null, 0,
					totalRowCount, 1L);
			return createTableResult(table, rows);
		} catch (Exception e) {
			try {
				if (table != null) table.close();
			} catch (Exception ex) {
				//Digest exception
			}
			throw new DSAccessException("Unable to read the table.", e);
		}
	}

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
		if (!connected) return;
		Throwable cause = t.getCause();
		if (cause instanceof SecurityViolation) {
			String s = "For security reasons, cannot access data. \n"; 
			throw new DSAccessException(s+message, cause);
		} else if (cause instanceof SessionException) {
			String s = "Session is not valid. \n"; 
			throw new DSOutOfServiceException(s+message, cause);
		} else if (cause instanceof AuthenticationException) {
			String s = "Cannot initialize the session. \n"; 
			throw new DSOutOfServiceException(s+message, cause);
		} else if (cause instanceof ResourceError) {
			String s = "Fatal error. Please contact the administrator. \n"; 
			throw new DSOutOfServiceException(s+message, t);
		} else if (cause instanceof ConnectionRefusedException || 
				t instanceof ConnectionRefusedException ||
				cause instanceof ConnectionTimeoutException || 
				t instanceof ConnectionTimeoutException) {
			/*
			if (!connected) return;
			connected = false;
			dsFactory.sessionExpiredExit(
					DataServicesFactory.SERVER_OUT_OF_SERVICE);
					*/
			return;
		} else if (cause instanceof ConnectionLostException ||
				t instanceof ConnectionLostException) {
			/*
			if (!connected) return;
			connected = false;
			dsFactory.sessionExpiredExit(DataServicesFactory.LOST_CONNECTION);
			*/
			return;
		}
		throw new DSAccessException("Cannot access data. \n"+message, t);
	}
	
	/**
	 * Helper method to handle exceptions thrown by the connection library.
	 * Methods in this class are required to fill in a meaningful context
	 * message.
	 * This method is not supposed to be used in this class' constructor or in
	 * the login/logout methods.
	 *  
	 * @param t     	The exception.
	 * @param message	The context message.
	 * @throws FSAccessException    A server-side error.
	 */
	private void handleFSException(Throwable t, String message) 
		throws FSAccessException
	{
		Throwable cause = t.getCause();
		String s = "\nImage not ready. Please try again later.";
		if (cause instanceof ConcurrencyException) {
			ConcurrencyException mpe = (ConcurrencyException) cause;
			//s += ", ready in approximately ";
			//s += UIUtilities.calculateHMSFromMilliseconds(mpe.backOff);
			FSAccessException fsa = new FSAccessException(message+s, cause);
			if (mpe instanceof MissingPyramidException || 
					mpe instanceof LockTimeout)
				fsa.setIndex(FSAccessException.PYRAMID);
			fsa.setBackOffTime(mpe.backOff);
			throw fsa;
		} else if (t instanceof ConcurrencyException) {
			ConcurrencyException mpe = (ConcurrencyException) t;
			s += UIUtilities.calculateHMSFromMilliseconds(mpe.backOff);
			FSAccessException fsa = new FSAccessException(message+s, t);
			if (mpe instanceof MissingPyramidException || 
					mpe instanceof LockTimeout)
				fsa.setIndex(FSAccessException.PYRAMID);
			fsa.setBackOffTime(mpe.backOff);
			throw fsa;
		}
	}
	
	/**
	 * Returns the message corresponding to the error thrown while importing the
	 * files.
	 * 
	 * @param t The exception to handle.
	 * @return See above.
	 */
	private String getImportFailureMessage(Throwable t)
	{
		String message;
		Throwable cause = t.getCause();
		if (cause instanceof FormatException) {
			message = cause.getMessage();
			cause.printStackTrace();
			if (message == null) return null;
			if (message.contains("ome-xml.jar"))
				return "Missing ome-xml.jar required to read OME-TIFF files";
			String[] s = message.split(":");
			if (s.length > 0) return s[0];
		}
		return null;
	}

	/**
	 * Utility method to print the error message
	 * 
	 * @param e The exception to handle.
	 * @return  See above.
	 */
	private String printErrorText(Throwable e) 
	{
		return UIUtilities.printErrorText(e);
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
			return Integer.valueOf(size);
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
		if (field == null || field.length() == 0) return terms;
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
			value = firstField+":"+v+" "+sep+" "+secondField+":"+v;
			formatted.add(value);
		}
		return formatted;
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
		else if (PlateAcquisitionData.class.equals(klass))
			table = "PlateAcquisitionWellSampleLink";
		else if (PlateAcquisitionI.class.equals(klass))
			table = "PlateAcquisitionWellSampleLink";
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
	private String getAnnotationTableLink(Class klass)
	{
		String table = null;
		if (Dataset.class.equals(klass) ||
			DatasetData.class.equals(klass)) 
			table = "DatasetAnnotationLink";
		else if (Project.class.equals(klass) ||
				ProjectData.class.equals(klass)) 
			table = "ProjectAnnotationLink";
		else if (Image.class.equals(klass) ||
				ImageData.class.equals(klass)) table = "ImageAnnotationLink";
		else if (Screen.class.equals(klass) ||
				ScreenData.class.equals(klass)) 
			table = "ScreenAnnotationLink";
		else if (Plate.class.equals(klass) ||
				PlateData.class.equals(klass)) 
			table = "ScreenAnnotationLink";
		else if (WellSample.class.equals(klass) ||
				WellSampleData.class.equals(klass)) 
			table = "ScreenAnnotationLink";
		else table = "AnnotationAnnotationLink";
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
		else if (klass.equals(TagAnnotationData.class.getName()))
			table = "AnnotationAnnotationLink";
		else if (klass.equals(PlateAcquisitionData.class.getName()))
			table = "PlateAcquisitionAnnotationLink";
		else if (klass.equals(PlateAcquisitionI.class.getName())) 
			table = "PlateAcquisitionAnnotationLink";
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
	 * Loads the links.
	 * 
	 * @param table 	The table's link.
	 * @param childID	The annotation's identifier
	 * @param userID	The user's identifier.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private List loadLinks(String table, long childID, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			if (table == null) return new ArrayList();
			ParametersI param = new ParametersI();
			param.map.put("id", omero.rtypes.rlong(childID));
			StringBuffer sb = new StringBuffer();
			sb.append("select link from "+table+" as link ");
			sb.append("left outer join fetch link.child as child ");
			sb.append("left outer join fetch link.parent parent ");
			if (childID >= 0) {
				sb.append("where link.child.id = :id");
				param.addId(childID);
				if (userID >= 0) {
					sb.append(" and link.details.owner.id = :userID");
					param.map.put("userID", omero.rtypes.rlong(userID));
				}
			} else {
				if (userID >= 0) {
					sb.append("where link.details.owner.id = :userID");
					param.map.put("userID", omero.rtypes.rlong(userID));
				}
			}
			IQueryPrx service = getQueryService();
			if (service == null) service = getQueryService();
			return getQueryService().findAllByQuery(sb.toString(), param);
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the requested link for "+
					"child ID: "+childID);
		}
		return new ArrayList();
	}
	
	/**
	 * Returns the {@link SharedResourcesPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */

	private SharedResourcesPrx getSharedResources()
		throws DSAccessException, DSOutOfServiceException
	{
	
		try {
			if (sharedResources == null) {
				sharedResources = entryEncrypted.sharedResources();
				if (sharedResources == null)
					throw new DSOutOfServiceException(
					"Cannot access the Shared Resources."); 
			}
			return sharedResources;
		} catch (Exception e) {
			handleException(e, "Cannot access the Shared Resources.");
		}
		return null;
	}
	
	/**
	 * Returns the {@link IRenderingSettingsPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IRenderingSettingsPrx getRenderingSettingsService()
		throws DSAccessException, DSOutOfServiceException
	{
		try {
			if (rndSettingsService == null) {
				if (entryUnencrypted != null)
					rndSettingsService = 
						entryUnencrypted.getRenderingSettingsService();
				else 
					rndSettingsService = 
						entryEncrypted.getRenderingSettingsService();
				
				if (rndSettingsService == null)
					throw new DSOutOfServiceException(
							"Cannot access the RenderingSettings service.");
				services.add(rndSettingsService);
			}
			return rndSettingsService;
		} catch (Throwable e) {
			handleException(e, "Cannot access the RenderingSettings service.");
		}
		return null;
	}

	/**
	 * Creates or recycles the import store.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.
	 */
	private OMEROMetadataStoreClient getImportStore()
		throws DSAccessException, DSOutOfServiceException
	{
		try {
			if (importStore == null) {
				importStore = new OMEROMetadataStoreClient();
				importStore.initialize(entryEncrypted);
			}
			return importStore;
		} catch (Throwable e) {
			handleException(e, "Cannot access Import service.");
		}
		return null;
	}
	
	/**
	 * Returns the {@link IRepositoryInfoPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IRepositoryInfoPrx getRepositoryService()
		throws DSAccessException, DSOutOfServiceException
	{
		try {
			if (repInfoService == null) {
				if (entryUnencrypted != null)
					repInfoService = 
						entryUnencrypted.getRepositoryInfoService();
				else repInfoService = 
						entryEncrypted.getRepositoryInfoService();
				if (repInfoService == null)
					throw new DSOutOfServiceException(
							"Cannot access the RepositoryInfo service.");
				services.add(repInfoService);
			}
			return repInfoService;
		} catch (Throwable e) {
			handleException(e, "Cannot access the RepositoryInfo service.");
		}
		return null;
	}

	/**
	 * Returns the {@link IScriptPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IScriptPrx getScriptService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			if (scriptService == null) {
				if (entryUnencrypted != null)
					scriptService = entryUnencrypted.getScriptService();
				else scriptService = entryEncrypted.getScriptService();
				if (scriptService == null)
					throw new DSOutOfServiceException(
							"Cannot access the Scripting service.");
				services.add(scriptService);
			}
			return scriptService; 
		} catch (Throwable e) {
			handleException(e, "Cannot access the Scripting service.");
		}
		return null;
	}
	
	/**
	 * Returns the {@link IContainerPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IContainerPrx getPojosService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			if (pojosService == null) {
				if (entryUnencrypted != null)
					pojosService = entryUnencrypted.getContainerService();
				else pojosService = entryEncrypted.getContainerService();
				if (pojosService == null)
					throw new DSOutOfServiceException(
							"Cannot access the Container service.");
				services.add(pojosService);
			}
			return pojosService; 
		} catch (Throwable e) {
			handleException(e, "Cannot access the Container service.");
		}
		return null;
	}

	/**
	 * Returns the {@link IQueryPrx} service.
	 *  
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IQueryPrx getQueryService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			if (queryService == null) {
				if (entryUnencrypted != null)
					queryService = entryUnencrypted.getQueryService();
				else queryService = entryEncrypted.getQueryService();
				if (queryService == null)
					throw new DSOutOfServiceException(
							"Cannot access the Query service.");
				services.add(queryService);
			}
			return queryService; 
		} catch (Throwable e) {
			handleException(e, "Cannot access the Query service.");
		}
		return null;
	}
	
	/**
	 * Returns the {@link IUpdatePrx} service.
	 *  
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IUpdatePrx getUpdateService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			if (updateService == null) {
				if (entryUnencrypted != null)
					updateService = entryUnencrypted.getUpdateService();
				else updateService = entryEncrypted.getUpdateService();
				if (updateService == null)
					throw new DSOutOfServiceException(
							"Cannot access Update service.");
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
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IMetadataPrx getMetadataService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			if (metadataService == null) {
				if (entryUnencrypted != null)
					metadataService = entryUnencrypted.getMetadataService();
				else metadataService = entryEncrypted.getMetadataService();
				if (metadataService == null)
					throw new DSOutOfServiceException(
							"Cannot access the Metadata service.");
				services.add(metadataService);
			}
			return metadataService; 
		} catch (Throwable e) {
			handleException(e, "Cannot access the Metadata service.");
		}
		return null;
	}

	/**
	 * Returns the {@link IRoiPrx} service.
	 *  
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IRoiPrx getROIService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			if (roiService == null) {
				if (entryUnencrypted != null)
					roiService = entryUnencrypted.getRoiService();
				else roiService = entryEncrypted.getRoiService();
				if (roiService == null)
					throw new DSOutOfServiceException(
							"Cannot access the ROI service.");
				services.add(roiService);
			}
			return roiService; 
		} catch (Throwable e) {
			handleException(e, "Cannot access th ROI service.");
		}
		return null;
	}
	
	/**
	 * Returns the {@link IAdminPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IAdminPrx getAdminService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			if (adminService == null) {
				adminService = entryEncrypted.getAdminService();
				if (adminService == null)
					throw new DSOutOfServiceException(
							"Cannot access the Admin service.");
				services.add(adminService);
			}
			return adminService; 
		} catch (Throwable e) {
			handleException(e, "Cannot access the Admin service.");
		}
		return null;
	}
	
	/**
	 * Returns the {@link IConfigPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IConfigPrx getConfigService()
		throws DSAccessException, DSOutOfServiceException
	{
		try {
			if (entryUnencrypted != null)
				return entryUnencrypted.getConfigService();
			return entryEncrypted.getConfigService();
		} catch (Throwable e) {
			handleException(e, "Cannot access Configuration service.");
		}
		return null;
	}
	
	/**
	 * Returns the {@link IDeletePrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IDeletePrx getDeleteService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			if (deleteService == null) {
				if (entryUnencrypted != null)
					deleteService = entryUnencrypted.getDeleteService(); 
				else 
					deleteService = entryEncrypted.getDeleteService();
				if (deleteService == null)
					throw new DSOutOfServiceException(
							"Cannot access the Delete service.");
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
	 * @throws DSAccessException If an error occurred while trying to 
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
				if (entryUnencrypted != null)
					thumbnailService = entryUnencrypted.createThumbnailStore();
				else 
					thumbnailService = entryEncrypted.createThumbnailStore();
				if (thumbnailService == null)
					throw new DSOutOfServiceException(
							"Cannot access Thumbnail service.");
				services.add(thumbnailService);
			}
			return thumbnailService; 
		} catch (Throwable e) {
			handleException(e, "Cannot access Thumbnail service.");
		}
		return null;
	}

	/**
	 * Returns the {@link ExporterPrx} service.
	 *   
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private ExporterPrx getExporterService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			ExporterPrx store = null;
			if (entryUnencrypted != null)
				store = entryUnencrypted.createExporter();
			else store = entryEncrypted.createExporter();
			if (store == null)
				throw new DSOutOfServiceException(
						"Cannot access the Exporter service.");
			return store;
		} catch (Throwable e) {
			handleException(e, "Cannot access the Exporter service.");
		}
		return null;
	}
	
	/**
	 * Returns the {@link RawFileStorePrx} service.
	 *  
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private RawFileStorePrx getRawFileService()
		throws DSAccessException, DSOutOfServiceException
	{
		try {
			/*
			if (fileStore != null) {
				services.remove(fileStore);
				try {
					fileStore.close();
				} catch (Exception e) {}
			}
			fileStore = entry.createRawFileStore();
			services.add(fileStore);
			*/
			if (entryUnencrypted != null)
				fileStore = entryUnencrypted.createRawFileStore();
			else 
				fileStore = entryEncrypted.createRawFileStore();
			if (fileStore == null)
				throw new DSOutOfServiceException(
						"Cannot access the RawFileStore Engine.");
			return fileStore;
		} catch (Throwable e) {
			handleException(e, "Cannot access the RawFileStore service.");
		}
		return null;
	}

	/**
	 * Returns the {@link RenderingEnginePrx Rendering service}.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private RenderingEnginePrx getRenderingService()
		throws DSAccessException, DSOutOfServiceException
	{
		try {
			RenderingEnginePrx engine;
			if (entryUnencrypted != null)
				engine = entryUnencrypted.createRenderingEngine();
			else engine = entryEncrypted.createRenderingEngine();
			if (engine == null)
				throw new DSOutOfServiceException(
						"Cannot access the Rendering Engine.");
			engine.setCompressionLevel(compression);
			return engine;
		} catch (Throwable e) {
			handleException(e, "Cannot access the Rendering Engine.");
		}
		return null;
	}

	/**
	 * Returns the {@link RawPixelsStorePrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private RawPixelsStorePrx getPixelsStore()
		throws DSAccessException, DSOutOfServiceException
	{
		try {
			if (entryUnencrypted != null)
				pixelsStore = entryUnencrypted.createRawPixelsStore();
			else 
				pixelsStore = entryEncrypted.createRawPixelsStore();
			if (pixelsStore == null)
				throw new DSOutOfServiceException(
						"Cannot access the RawPixelsStore service.");
			return pixelsStore;
		} catch (Throwable e) {
			handleException(e, "Cannot access the RawPixelsStore service.");
		}
		return null;
	}

	/**
	 * Returns the {@link IPixelsPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	private IPixelsPrx getPixelsService()
		throws DSAccessException, DSOutOfServiceException
	{ 
		try {
			if (pixelsService == null) {
				if (entryUnencrypted != null)
					pixelsService = entryUnencrypted.getPixelsService();
				else 
					pixelsService = entryEncrypted.getPixelsService();
				if (pixelsService == null)
					throw new DSOutOfServiceException(
							"Cannot access the Pixels service.");
				services.add(pixelsService);
			}
			return pixelsService;
		} catch (Throwable e) {
			handleException(e, "Cannot access the Pixels service.");
		}
		return null;
	}
	
	/**
	 * Returns the {@link SearchPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.
	 */
	private SearchPrx getSearchService()
		throws DSAccessException, DSOutOfServiceException
	{
		try {
			SearchPrx prx = null;
			if (entryUnencrypted != null)
				prx = entryUnencrypted.createSearchService();
			else prx = entryEncrypted.createSearchService();
			if (prx == null)
				throw new DSOutOfServiceException(
					"Cannot access the Search service.");
			return prx;
		} catch (Throwable e) {
			handleException(e, "Cannot access the Search service.");
		}
		return null;
	}
	
	/**
	 * Returns the {@link IProjectionPrx} service.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.
	 */
	private IProjectionPrx getProjectionService()
		throws DSAccessException, DSOutOfServiceException
	{
		try {
			if (projService == null) {
				if (entryUnencrypted != null)
					projService = entryUnencrypted.getProjectionService();
				else projService = entryEncrypted.getProjectionService();
				if (projService == null)
					throw new DSOutOfServiceException(
							"Cannot access the Projection service.");
				services.add(projService);
			}
			return projService;
		} catch (Throwable e) {
			handleException(e, "Cannot access the Projection service.");
		}
		return null;
	}
	
	/**
	 * Checks if some default rendering settings have to be created
	 * for the specified set of pixels.
	 * 
	 * @param pixelsID	The pixels ID.
	 * @param re		The rendering engine to load.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
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
					re.resetDefaults();
					re.lookupRenderingDef(pixelsID);
				}
			}
		} catch (Throwable e) {
			handleException(e, "Cannot set RE defaults.");
		}
	}
	
	/**
	 * Formats the terms to search for.
	 * 
	 * @param terms		The terms to search for.
	 * @param service	The search service.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
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
	 * @throws DSAccessException If an error occurred while trying to 
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
		String card = null;
		while (i.hasNext()) {
			card = i.next();
			if (value.startsWith(card)) {
				return true;
			}
		}
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
		else if (TermAnnotationData.class.equals(pojo))
			return "ome.model.annotations.UriAnnotation"; 
		else if (TimeAnnotationData.class.equals(pojo))
			return "ome.model.annotations.TimeAnnotation"; 
		else if (BooleanAnnotationData.class.equals(pojo))
			return "ome.model.annotations.BooleanAnnotation"; 
		return null;
	}
	
	/** Clears the data. */
	private void clear()
	{
		services.clear();
		reServices.clear();
		thumbnailService = null;
		fileStore = null;
		metadataService = null;
		pojosService = null;
		projService = null;
		adminService = null;
		queryService = null;
		rndSettingsService = null;
		repInfoService = null;
		deleteService = null;
		pixelsService = null;
		roiService = null;
		pixelsStore = null;
		updateService = null;
		scriptService = null;
		timeService = null;
		sharedResources = null;
		importStore = null;
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
		else if (nodeType.equals(TermAnnotation.class) ||
				nodeType.equals(TermAnnotationData.class))
			return TermAnnotationI.class.getName();
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
	 * Creates a new instance.
	 * 
	 * @param port			The port used to connect.
	 * @param dsFactory 	A reference to the factory. Used whenever a broken 
	 * 						link is detected to get the Login Service and try 
	 *                  	reestablishing a valid link to <i>OMERO</i>.
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
		services = new HashSet<ServiceInterfacePrx>();
		reServices = new HashMap<Long, StatefulServiceInterfacePrx>();
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
	 * Returns <code>true</code> if the passed group is an experimenter group
	 * internal to OMERO, <code>false</code> otherwise.
	 * 
	 * @param group The experimenter group to handle.
	 * @return See above.
	 */
	boolean isSystemGroup(ExperimenterGroup group)
	{
		String n = group.getName() == null ? null : group.getName().getValue();
		return (SYSTEM_GROUPS.contains(n));
	}
	
	/**
	 * Converts the specified POJO into the corresponding model.
	 *  
	 * @param nodeType The POJO class.
	 * @return The corresponding class.
	 */
	Class convertPojos(DataObject node)
	{
		if (node instanceof FileData || node instanceof MultiImageData)
			return OriginalFile.class;
		return convertPojos(node.getClass());
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
		else if (BooleanAnnotationData.class.equals(nodeType))
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
		else if (TermAnnotationData.class.equals(nodeType))
			return TermAnnotation.class;
		else if (ScreenData.class.equals(nodeType)) 
			return Screen.class;
		else if (PlateData.class.equals(nodeType)) 
			return Plate.class;
		else if (WellData.class.equals(nodeType)) 
			return Well.class;
		else if (WellSampleData.class.equals(nodeType)) 
			return WellSample.class;
		else if (PlateAcquisitionData.class.equals(nodeType))
			return PlateAcquisition.class;
		else if (FileData.class.equals(nodeType) || 
				MultiImageData.class.equals(nodeType))
			return OriginalFile.class;
		else if (GroupData.class.equals(nodeType))
			return ExperimenterGroup.class;
		else if (ExperimenterData.class.equals(nodeType))
			return Experimenter.class;
		throw new IllegalArgumentException("NodeType not supported");
	}

	/**
	 * Creates the string corresponding to the object to delete.
	 * 
	 * @param data The object to handle.
	 * @return See above.
	 */
	String createDeleteCommand(String data)
	{
		if (ImageData.class.getName().equals(data)) return REF_IMAGE;
		else if (DatasetData.class.getName().equals(data)) return REF_DATASET;
		else if (ProjectData.class.getName().equals(data)) return REF_PROJECT;
		else if (ScreenData.class.getName().equals(data)) return REF_SCREEN;
		else if (PlateData.class.getName().equals(data)) return REF_PLATE;
		else if (ROIData.class.getName().equals(data)) return REF_ROI;
		else if (PlateAcquisitionData.class.getName().equals(data)) 
			return REF_PLATE_ACQUISITION;
		else if (WellData.class.getName().equals(data)) 
			return REF_WELL;
		else if (PlateAcquisitionData.class.getName().equals(data)) 
			return REF_PLATE_ACQUISITION;
		else if (TagAnnotationData.class.getName().equals(data) || 
				TermAnnotationData.class.getName().equals(data) ||
				FileAnnotationData.class.getName().equals(data) ||
				TextualAnnotationData.class.getName().equals(data)) 
			return REF_ANNOTATION;
		throw new IllegalArgumentException("Cannot delete the speficied type.");
	}
	
	/**
	 * Creates the string corresponding to the object to delete.
	 * 
	 * @param data The object to handle.
	 * @return See above.
	 */
	String createDeleteOption(String data)
	{
		if (TagAnnotationData.class.getName().equals(data))
			return REF_TAG;
		else if (TermAnnotationData.class.getName().equals(data)) 
			return REF_TERM;
		else if (FileAnnotationData.class.getName().equals(data)) 
			return REF_FILE;
		throw new IllegalArgumentException("Cannot delete the speficied type.");
	}
	
	/**
	 * Tells whether the communication channel to <i>OMERO</i> is currently
	 * connected.
	 * This means that we have established a connection and have successfully
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
			if (service == null) service = getAdminService();
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
	 * @param groupID			The id of the group or <code>-1</code>.
	 * @param encrypted  		Pass <code>true</code> to encrypt data transfer,
     * 					 		<code>false</code> otherwise.
	 * @return The user's details.
	 * @throws DSOutOfServiceException If the connection can't be established
	 *                                  or the credentials are invalid.
	 * @see #getUserDetails(String)
	 */
	ExperimenterData login(String userName, String password, String hostName,
							float compressionLevel, long groupID, boolean
							encrypted)
		throws DSOutOfServiceException
	{
		try {
			compression = compressionLevel;
			this.hostName = hostName;
			if (port > 0) secureClient = new client(hostName, port);
			else secureClient = new client(hostName);
			entryEncrypted = secureClient.createSession(userName, password);
			secureClient.setAgent(AGENT);
			if (!encrypted) {
				unsecureClient = secureClient.createClient(false);
				entryUnencrypted = unsecureClient.getSession();
			}
			connected = true;
			ExperimenterData exp = getUserDetails(userName);
			if (groupID >= 0) {
				long defaultID = -1;
				try {
					defaultID = exp.getDefaultGroup().getId();
				} catch (Exception e) {
					// no default group
				}
				if (defaultID == groupID) return exp;
				try {
					changeCurrentGroup(exp, groupID);
					exp = getUserDetails(userName);
				} catch (Exception e) {
					/*
					connected = false;
					String s = "Can't connect to OMERO. Group not valid.\n\n";
					throw new DSOutOfServiceException(s, e);
					*/
				}
			}
			
			return exp;
		} catch (Throwable e) {
			connected = false;
			String s = "Can't connect to OMERO. OMERO info not valid.\n\n";
			s += printErrorText(e);
			throw new DSOutOfServiceException(s, e);  
		} 
	}
	
	/**
	 * Retrieves the system view hosting the repositories.
	 * 
	 * @param userID The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in.
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.
	 */
	FSFileSystemView getFSRepositories(long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		if (fsViews == null) fsViews = new HashMap<Long, FSFileSystemView>();
		if (fsViews.containsKey(userID)) return fsViews.get(userID);
		//Review that code
		FSFileSystemView view = null;
		try {
			RepositoryMap m = getSharedResources().repositories();
			List proxys = m.proxies;
			List names = m.descriptions;
			Iterator i = names.iterator();
			int index = 0;
			FileData f;
			RepositoryPrx proxy;
			Map<FileData, RepositoryPrx> 
				repositories = new HashMap<FileData, RepositoryPrx>();
			while (i.hasNext()) {
				f = new FileData((OriginalFile) i.next(), true);
				if (!f.getName().contains("Tmp")) {
					proxy = (RepositoryPrx) proxys.get(index);
					repositories.put(f, proxy);
				}
				index++;
			}
			view = new FSFileSystemView(userID, repositories);
		} catch (Throwable e) {
			handleException(e, "Cannot load the repositories");
		}
		if (view != null) fsViews.put(userID, view);
		return view;
	}
	
	/**
	 * Changes the default group of the currently logged in user.
	 * 
	 * @param exp The experimenter to handle
	 * @param groupID The id of the group.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in.
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	void changeCurrentGroup(ExperimenterData exp, long groupID)
		throws DSOutOfServiceException, DSAccessException
	{
		List<GroupData> groups = exp.getGroups();
		Iterator<GroupData> i = groups.iterator();
		GroupData group = null;
		boolean in = false;
		while (i.hasNext()) {
			group = i.next();
			if (group.getId() == groupID) {
				in = true;
				break;
			}
		}
		String s = "Can't modify the current group.\n\n";
		if (!in) {
			throw new DSOutOfServiceException(s);  
		}
		try {
			shutDownServices(true);
			clear();
			IAdminPrx service = getAdminService();
			if (service == null) service = getAdminService();
			service.setDefaultGroup(exp.asExperimenter(), 
					group.asGroup());
			entryEncrypted.setSecurityContext(
					new ExperimenterGroupI(groupID, false));
		} catch (Exception e) {
			handleException(e, s);
		}
	}
	
	/**
	 * Changes the default group of the currently logged in user.
	 * 
	 * @param exp The experimenter to handle
	 * @param groupID The id of the group.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in.
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	/*
	void changeCurrentGroup(ExperimenterData exp, long groupID, 
			String userName, String password)
		throws DSOutOfServiceException, DSAccessException
	{
		List<GroupData> groups = exp.getGroups();
		Iterator<GroupData> i = groups.iterator();
		GroupData group = null;
		boolean in = false;
		while (i.hasNext()) {
			group = i.next();
			if (group.getId() == groupID) {
				in = true;
				break;
			}
		}
		String s = "Can't modify the current group.\n\n";
		if (!in) return;
		try {
			clear();
			secureClient.closeSession();
			if (unsecureClient != null) secureClient.closeSession();
			entryEncrypted = secureClient.createSession(userName, password);
			if (unsecureClient != null) {
				unsecureClient = secureClient.createClient(false);
				entryUnencrypted = unsecureClient.getSession();
			}
			getAdminService().setDefaultGroup(exp.asExperimenter(), 
					group.asGroup());
			entryEncrypted.setSecurityContext(
					new ExperimenterGroupI(groupID, false));
		} catch (Exception e) {
			handleException(e, s);
		} 
	}
*/
	/**
	 * Returns the version of the server.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in.
	 */
	String getServerVersion()
		throws DSOutOfServiceException
	{
		try {
			return getConfigService().getVersion();
		} catch (Exception e) {
			String s = "Can't retrieve the server version.\n\n";
			s += printErrorText(e);
			throw new DSOutOfServiceException(s, e);  
		}
	}
	
	/**
	 * Returns the LDAP details or an empty string.
	 * 
	 * @param userID The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection can't be established
	 *                                  or the credentials are invalid.
	 */
	String lookupLdapAuthExperimenter(long userID)
		throws DSOutOfServiceException
	{
		isSessionAlive();
		try {
			IAdminPrx svc = getAdminService();
			if (svc == null) svc = getAdminService();
			return svc.lookupLdapAuthExperimenter(userID);
		} catch (Throwable e) {
			String s = "Can't find the LDAP information.\n\n";
			s += printErrorText(e);
			throw new DSOutOfServiceException(s, e); 
		}
	}
	
	void startFS(Properties fsConfig)
	
	{
		/*
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
		*/
	}
	
	/**
	 * Returns the pixels id corresponding to the Rendering engine to
	 * reactivate.
	 * 
	 * @return See above.
	 */
	List<Long> getRenderingServices()
	{
		List<Long> l = new ArrayList<Long>();
		if (reServices == null || reServices.size() == 0) return l;
		Entry entry;
		Iterator i = reServices.entrySet().iterator();
		while (i.hasNext()) {
			entry = (Entry) i.next();
			l.add((Long) entry.getKey());
		}
		return l;
	}
	
	/** 
	 * Tries to reconnect to the server. Returns <code>true</code>
	 * if it was possible to reconnect, <code>false</code>
	 * otherwise.
	 * 
	 * @param userName	The user name to be used for login.
	 * @param password	The password to be used for login.
	 * @return See above.
	 */
	boolean reconnect(String userName, String password)
	{
		boolean b = entryUnencrypted != null;
		//sList
		clear();
		try {
			//first to rejoin the session.
			connected = true;
			try {
				secureClient.closeSession();
			} catch (Exception e) {
				
			}
			entryEncrypted = secureClient.joinSession(
					secureClient.getSessionId());
			if (b) {
				unsecureClient = secureClient.createClient(false);
				entryUnencrypted = unsecureClient.getSession();
			}
		} catch (Throwable t) {
			Throwable cause = t.getCause();
			connected = false;
			//joining the session did not work so trying to create a session
			if (cause instanceof ConnectionLostException ||
					t instanceof ConnectionLostException || 
					cause instanceof ClientError ||
					t instanceof ClientError) {
				try {
					connected = true;
					entryEncrypted =  secureClient.createSession(
							userName, password);
					if (b) {
						unsecureClient = secureClient.createClient(false);
						entryUnencrypted = unsecureClient.getSession();
					}
				} catch (Throwable e) {
					connected = false;
				}
			}
		}
		try {
			if (connected) {
				if (unsecureClient != null)
					unsecureClient = secureClient.createClient(false);
			}
		} catch (Exception e) {
			return false;
		}

		return connected;
	}
	
	/** Logs out. */
	void logout()
	{
		connected = false;
		try {
			shutDownServices(true);
			clear();
			secureClient.closeSession();
			secureClient = null;
			entryEncrypted = null;
		} catch (Exception e) {
			//session already dead.
		} finally {
			secureClient = null;
			entryEncrypted = null;
		}
		try {
			if (unsecureClient != null) unsecureClient.closeSession();
			unsecureClient = null;
			entryUnencrypted = null;
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			unsecureClient = null;
			entryUnencrypted = null;
		}
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
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#loadContainerHierarchy(Class, List, Map)
	 */
	Set loadContainerHierarchy(Class rootType, List rootIDs, Parameters options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IContainerPrx service = getPojosService();
			if (service == null) service = getPojosService();
			return PojoMapper.asDataObjects(
					service.loadContainerHierarchy(
					convertPojos(rootType).getName(), rootIDs, options));
		} catch (Throwable t) {
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
	 *                      Can be <code>Project</code>
	 *                      Mustn't be <code>null</code>.
	 * @param leavesIDs     Set of identifiers of the Images that sit at the 
	 * 						bottom of the trees. Mustn't be <code>null</code>.
	 * @param options Options to retrieve the data.
	 * @return A <code>Set</code> with all root nodes that were found.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#findContainerHierarchies(Class, List, Map)
	 */
	Set findContainerHierarchy(Class rootNodeType, List leavesIDs, 
			Parameters options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IContainerPrx service = getPojosService();
			if (service == null) service = getPojosService();
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
	 * and maps the result calling {@link PojoMapper#asDataObjects(Parameters)}.
	 * 
	 * @param nodeType      The type of the rootNodes.
	 *                      Mustn't be <code>null</code>. 
	 * @param nodeIDs       TheIds of the objects of type
	 *                      <code>rootNodeType</code>. 
	 *                      Mustn't be <code>null</code>.
	 * @param annotationTypes The collection of annotations to retrieve or 
	 * 						  passed an empty list if we retrieve all the 
	 * 						  annotations. 
	 * @param annotatorIDs  The identifiers of the users for whom annotations 
	 * 						should be retrieved. If <code>null</code>, 
	 * 						all annotations are returned.
	 * @param options       Options to retrieve the data.
	 * @return A map whose key is rootNodeID and value the <code>Set</code> of
	 *         all annotations for that node or <code>null</code>.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#findAnnotations(Class, List, List, Map)
	 */
	Map loadAnnotations(Class nodeType, List nodeIDs, 
			List<Class> annotationTypes, List annotatorIDs, Parameters options)
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
			if (service == null) service = getMetadataService();
			return PojoMapper.asDataObjects(
					service.loadAnnotations(convertPojos(nodeType).getName(), 
							nodeIDs, types, annotatorIDs, options));
		} catch (Throwable t) {
			handleException(t, "Cannot find annotations for "+nodeType+".");
		}
		return new HashMap();
	}
	
	/**
	 * Loads the specified annotations.
	 * 
	 * @param annotationIds The annotation to load.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.s
	 */
	Set<DataObject> loadAnnotation(List<Long> annotationIds)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		if (annotationIds == null || annotationIds.size() == 0)
			return new HashSet<DataObject>();
		try {
			IMetadataPrx service = getMetadataService();
			if (service == null) service = getMetadataService();
			return PojoMapper.asDataObjects(
					service.loadAnnotation(annotationIds));
					
		} catch (Throwable t) {
			handleException(t, "Cannot find the annotations.");
		}
		return new HashSet<DataObject>();
	}
	
	/**
	 * Finds the links if any between the specified parent and child.
	 * 
	 * @param type    The type of parent to handle.
	 * @param userID  The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	Collection findAllAnnotations(Class type, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQueryPrx service = getQueryService();
			if (service == null) service = getQueryService();
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
		return new ArrayList();
	}
	
	/**
	 * Retrieves the images contained in containers specified by the 
	 * node type.
	 * Wraps the call to the {@link IPojos#getImages(Class, List, Parameters)}
	 * and maps the result calling {@link PojoMapper#asDataObjects(Set)}.
	 * 
	 * @param nodeType  The type of container. Can be either Project, Dataset.
	 * @param nodeIDs   Set of containers' IDS.
	 * @param options   Options to retrieve the data.
	 * @return A <code>Set</code> of retrieved images.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#getImages(Class, List, Map)
	 */
	Set getContainerImages(Class nodeType, List nodeIDs, Parameters options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IContainerPrx service = getPojosService();
			if (service == null) service = getPojosService();
			return PojoMapper.asDataObjects(service.getImages(
					convertPojos(nodeType).getName(), nodeIDs, options));
		} catch (Throwable t) {
			handleException(t, "Cannot find images for "+nodeType+".");
		}
		return new HashSet();
	}

	/**
	 * Retrieves the images imported by the current user.
	 * Wraps the call to the {@link IPojos#getUserImages(Parameters)}
	 * and maps the result calling {@link PojoMapper#asDataObjects(Set)}.
	 * 
	 * @param options   Options to retrieve the data.
	 * @return A <code>Set</code> of retrieved images.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#getUserImages(Map)
	 */
	Set getUserImages(Parameters options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IContainerPrx service = getPojosService();
			if (service == null) service = getPojosService();
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
	 * @param rootNodeType 	The type of container. Can either be Dataset.
	 * @param property		One of the properties defined by this class.
	 * @param ids           The ids of the objects.
	 * @param options		Options to retrieve the data.		
	 * @param rootNodeIDs	Set of root node IDs.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#getCollectionCount(String, String, List, Map)
	 */
	Map getCollectionCount(Class rootNodeType, String property, List ids,
			Parameters options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			if (TagAnnotationData.class.equals(rootNodeType)) {
				IMetadataPrx service = getMetadataService();
				if (service == null) service = getMetadataService();
				return service.getTaggedObjectsCount(ids, options);
			}
			IContainerPrx service = getPojosService();
			if (service == null) service = getPojosService();
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
	 * Creates the specified object.
	 * 
	 * @param object    The object to create.
	 * @param options   Options to create the data.  
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#createDataObject(IObject, Map)
	 */
	IObject createObject(IObject object)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			isSessionAlive();
			return saveAndReturnObject(object, null);
		} catch (Throwable t) {
			handleException(t, "Cannot update the object.");
		}
		return null;
	}

	/**
	 * Creates the specified objects.
	 * 
	 * @param objects   The objects to create.
	 * @param options   Options to create the data.  
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#createDataObjects(IObject[], Map)
	 */
	List<IObject> createObjects(List<IObject> objects)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			return saveAndReturnObject(objects, null);
		} catch (Throwable t) {
			handleException(t, "Cannot create the objects.");
		}
		return new ArrayList<IObject>();
	}

	/**
	 * Deletes the specified object.
	 * 
	 * @param object    The object to delete.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 * @see IUpdate#deleteObject(IObject)
	 */
	void deleteObject(IObject object)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			if (object instanceof Plate) {
				IDeletePrx svc = getDeleteService();
				if (svc == null) svc = getDeleteService();
				svc.deletePlate(object.getId().getValue());
			} else {
				IUpdatePrx service = getUpdateService();
				if (service == null) service = getUpdateService();
				service.deleteObject(object);
			}
		} catch (Throwable t) {
			handleException(t, "Cannot delete the object.");
		}
	}

	/**
	 * Deletes the specified objects.
	 * 
	 * @param objects                  The objects to delete.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException       If an error occurred while trying to 
	 *                                 retrieve data from OMERO service. 
	 * @see IUpdate#deleteObject(IObject) 
	 */
	void deleteObjects(List<IObject> objects)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IUpdatePrx service = getUpdateService();
			if (service == null) service = getUpdateService();
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
	 * @param object    The object to update.
	 * @param options   Options to update the data.   
	 * @return          The updated object.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#updateDataObject(IObject, Map)
	 */
	IObject saveAndReturnObject(IObject object, Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IUpdatePrx service = getUpdateService();
			if (service == null) service = getUpdateService();
			if (options == null) return service.saveAndReturnObject(object);
			return service.saveAndReturnObject(object, options);
		} catch (Throwable t) {
			handleException(t, "Cannot update the object.");
		}
		return null;
	}
	
	/**
	 * Updates the specified object.
	 * 
	 * @param objects   The objects to update.
	 * @param options   Options to update the data.   
	 * @return          The updated object.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#updateDataObject(IObject, Map)
	 */
	List<IObject> saveAndReturnObject(List<IObject> objects, Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IUpdatePrx service = getUpdateService();
			if (service == null) service = getUpdateService();
			return service.saveAndReturnArray(objects);
		} catch (Throwable t) {
			handleException(t, "Cannot update the object.");
		}
		return new ArrayList<IObject>();
	}
	
	/**
	 * Updates the specified object.
	 * 
	 * @param object    The object to update.
	 * @param options   Options to update the data.   
	 * @return          The updated object.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#updateDataObject(IObject, Map)
	 */
	IObject updateObject(IObject object, Parameters options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IContainerPrx service = getPojosService();
			if (service == null) service = getPojosService();
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
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.
	 * @see IPojos#updateDataObjects(IObject[], Map) 
	 */
	List<IObject> updateObjects(List<IObject> objects, Parameters options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IContainerPrx service = getPojosService();
			if (service == null) service = getPojosService();
			List<IObject> l = service.updateDataObjects(objects, options);
			if (l == null) return l;
			Iterator<IObject> i = l.iterator();
			List<IObject> r = new ArrayList<IObject>(l.size());
			IObject io;
			while (i.hasNext()) {
				io = findIObject(i.next());
				if (io != null) r.add(io);
			}
			return r;
		} catch (Throwable t) {
			handleException(t, "Cannot update the object.");
		}
		return new ArrayList<IObject>();
	}

	/**
	 * Retrieves the dimensions in microns of the specified pixels set.
	 * 
	 * @param pixelsID  The pixels set ID.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	Pixels getPixels(long pixelsID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IPixelsPrx service = getPixelsService();
			if (service == null) service = getPixelsService();
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
	 * @throws RenderingServiceException If an error occurred while trying to 
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
			if (service == null) service = getThumbService();
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
	 * @param maxLength	The maximum length of the thumbnail width or height
	 * 					depending on the pixel size.
	 * @return See above.
	 * @throws RenderingServiceException If an error occurred while trying to 
	 *              retrieve data from the service. 
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	synchronized byte[] getThumbnailByLongestSide(long pixelsID, int maxLength)
		throws RenderingServiceException, DSOutOfServiceException
	{
		isSessionAlive();
		try {
			ThumbnailStorePrx service = getThumbService();
			if (service == null) service = getThumbService();
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
	 * @param maxLength	The maximum length of the thumbnail width or height
	 * 					depending on the pixel size.
	 * @param reset     Pass <code>true</code> to reset the thumbnail store,
	 *                  <code>false</code> otherwise.
	 * @return See above.
	 * @throws RenderingServiceException If an error occurred while trying to 
	 *              retrieve data from the service. 
	 * @throws DSOutOfServiceException If the connection is broken.
	 */
	synchronized Map getThumbnailSet(List<Long> pixelsID, int maxLength, boolean
			reset)
		throws RenderingServiceException, DSOutOfServiceException
	{
		isSessionAlive();
		try {
			if (reset) thumbRetrieval = MAX_RETRIEVAL;
			else thumbRetrieval += pixelsID.size();
			ThumbnailStorePrx service = getThumbService();
			if (service == null) service = getThumbService();
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
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.
	 * @throws FSAccessException If an error occurred when trying to build a 
	 * pyramid or access file not available.
	 */
	synchronized RenderingEnginePrx createRenderingEngine(long pixelsID)
		throws DSOutOfServiceException, DSAccessException, FSAccessException
	{
		isSessionAlive();
		RenderingEnginePrx service = null;
		try {
			service = getRenderingService();
			if (service == null) service = getRenderingService();
			service.lookupPixels(pixelsID);
			needDefault(pixelsID, service);
			reServices.put(pixelsID, service);
			service.load();
			return service;
		} catch (Throwable t) {
			String s = "Cannot start the Rendering Engine.";
			if (service != null) {
				try {
					service.close();
				} catch (Exception e) {}
			}
			handleFSException(t, s);
			handleException(t, s);
		}
		return null;
	}

	/**
	 * Finds the link if any between the specified parent and child.
	 * 
	 * @param type    The type of annotation to handle.
	 * @param parentID The id of the parent.
	 * @param childID   The id of the child, or <code>-1</code> if no 
	 * 					child specified.
	 * @param userID   The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	IObject findAnnotationLink(Class type, long parentID, long childID, 
			long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQueryPrx service = getQueryService();
			if (service == null) service = getQueryService();
			String table = getTableForAnnotationLink(type.getName());
			if (table == null) return null;
			String sql = "select link from "+table+" as link where " +
			"link.parent.id = :parentID and link.details.owner.id = :userID"; 
			Parameters p = new ParametersI();
			p.map = new HashMap<String, RType>();
			p.map.put("parentID", omero.rtypes.rlong(parentID));
			p.map.put("userID", omero.rtypes.rlong(userID));
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
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	List findAnnotationLinks(String parentType, long parentID, 
								List<Long> children)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQueryPrx service = getQueryService();
			if (service == null) service = getQueryService();
			String table = getTableForAnnotationLink(parentType);
			if (table == null) return null;
			StringBuffer sb = new StringBuffer();
			sb.append("select link from "+table+" as link");
			sb.append(" left outer join fetch link.details.owner as owner");
			sb.append(" left outer join fetch link.child as child");
			sb.append(" left outer join fetch link.parent as parent");
			ParametersI p = new ParametersI();
			if (parentID > 0) {
				sb.append(" where link.parent.id = :parentID");
				if (children != null && children.size() > 0) {
					sb.append(" and link.child.id in (:childIDs)");
					p.addLongs("childIDs", children);
				}
				p.map.put("parentID", omero.rtypes.rlong(parentID));
			} else {
				if (children != null && children.size() > 0) {
					sb.append(" where link.child.id in (:childIDs)");
					p.addLongs("childIDs", children);
				}
			}
			return service.findAllByQuery(sb.toString(), p);
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the annotation links for "+
					"parent ID: "+parentID);
		}
		return new ArrayList();
	}		
	
	/**
	 * Finds the link if any between the specified parent and child.
	 * 
	 * @param parent    The parent.
	 * @param child     The child.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
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
			IQueryPrx service = getQueryService();
			if (service == null) service = getQueryService();
			return service.findByQuery(sql, param);
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
	 * @throws DSAccessException If an error occurred while trying to 
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
			IQueryPrx service = getQueryService();
			if (service == null) service = getQueryService();
			return service.findAllByQuery(sql, param);
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the requested link for "+
					"parent ID: "+parent.getId());
		}
		return new ArrayList();
	}

	/**
	 * Finds the links if any between the specified parent and children.
	 * 
	 * @param parentClass	The parent.
	 * @param children  	Collection of children as children ids.
	 * @param userID		The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
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
			IQueryPrx service = getQueryService();
			if (service == null) service = getQueryService();
			return service.findAllByQuery(sql, param);
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the requested link for "+
			"the specified children");
		}
		return new ArrayList();
	}

	/**
	 * Finds all the links.
	 * 
	 * @param node The type of node to handle.
	 * @param nodeID The id of the node if any.
	 * @param children The collection of annotations' identifiers 
	 * @param userID The user's identifier or <code>-1</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	List findAnnotationLinks(Class node, long nodeID, List children, 
			long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			String table = getAnnotationTableLink(node);
			if (table == null) return null;
			StringBuffer sb = new StringBuffer();
			sb.append("select link from "+table+" as link ");
			sb.append("left outer join fetch link.child child ");
			sb.append("left outer join fetch child.details.owner ");
			sb.append("left outer join fetch link.details.owner ");
			sb.append("where link.child.id in (:childIDs)");

			ParametersI param = new ParametersI();
			param.addLongs("childIDs", children);
			if (nodeID > 0) {
				sb.append(" and link.parent.id = :parentID");
				param.map.put("parentID", omero.rtypes.rlong(nodeID));
			}
			if (userID >= 0) {
				sb.append(" and link.details.owner.id = :userID");
				param.map.put("userID", omero.rtypes.rlong(userID));
			}
			IQueryPrx service = getQueryService();
			if (service == null) service = getQueryService();
			return service.findAllByQuery(sb.toString(), param);
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the requested link for "+
			"the specified children");
		}
		return new ArrayList();
	}
	
	/**
	 * Finds the links if any between the specified parent and children.
	 * 
	 * @param parentClass   The parent.
	 * @param childID  		The id of the child.
	 * @param userID		The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	List findLinks(Class parentClass, long childID, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		if (FileAnnotation.class.equals(parentClass)) {
			List results = new ArrayList();
			results.addAll(loadLinks("ProjectAnnotationLink", childID, userID));
			results.addAll(loadLinks("DatasetAnnotationLink", childID, userID));
			results.addAll(loadLinks("ImageAnnotationLink", childID, userID));
			return results;
		}
		return loadLinks(getTableForLink(parentClass), childID, userID);
	}

	/**
	 * Retrieves an updated version of the specified object.
	 * 
	 * @param o	The object to retrieve.
	 * @return The last version of the object.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	IObject findIObject(IObject o)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQueryPrx service = getQueryService();
			if (service == null) service = getQueryService();
			return service.find(o.getClass().getName(), 
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
	 * @param dataObjectThe object to retrieve.
	 * @param name The name of the object.
	 * @param 
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	IObject findIObjectByName(Class dataObject, String name, long ownerID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			ParametersI param = new ParametersI();
			param.map.put("name", rtypes.rstring(name));
			param.map.put("ownerID", rtypes.rlong(ownerID));
			IQueryPrx service = getQueryService();
			if (service == null) service = getQueryService();
			String table = getTableForClass(dataObject);
			String sql = "select o from "+table+" as o";
			sql += " where o.name = :name";
			sql += " and o.details.owner.id = :ownerID";
			return service.findByQuery(sql, param);
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the requested object.");
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
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	IObject findIObject(String klassName, long id)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQueryPrx service = getQueryService();
			if (service == null) service = getQueryService();
			return service.find(klassName, id);
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
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	Set<GroupData> getAvailableGroups(ExperimenterData user)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		Set<GroupData> pojos = new HashSet<GroupData>();
		try {
			//Need method server side.
			ParametersI p = new ParametersI();
			p.addId(user.getId());
			IQueryPrx service = getQueryService();
			if (service == null) service = getQueryService();
			List<IObject> groups = service.findAllByQuery(
                    "select distinct g from ExperimenterGroup as g "
                    + "join fetch g.groupExperimenterMap as map "
                    + "join fetch map.parent e "
                    + "left outer join fetch map.child u "
                    + "left outer join fetch u.groupExperimenterMap m2 "
                    + "left outer join fetch m2.parent p "
                    + "where g.id in "
                    + "  (select m.parent from GroupExperimenterMap m "
                    + "  where m.child.id = :id )", p);

			//List<ExperimenterGroup> groups = service.containedGroups(
			//		user.getId());
			
			ExperimenterGroup group;
			//GroupData pojoGroup;
			Iterator<IObject> i = groups.iterator();
			while (i.hasNext()) {
				group = (ExperimenterGroup) i.next();
				pojos.add((GroupData) PojoMapper.asDataObject(group));
			}
			return pojos;
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the available groups ");
		}
		return pojos;
	}
	
	/**
	 * Retrieves the archived files if any for the specified set of pixels.
	 * 
	 * @param path		The location where to save the files.
	 * @param pixelsID 	The ID of the pixels set.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.  
	 */
	synchronized Map<Boolean, Object> getArchivedFiles(String path, long pixelsID) 
		throws DSAccessException, DSOutOfServiceException
	{
		isSessionAlive();
		IQueryPrx service = getQueryService();
		if (service == null) service = getQueryService();
		List files = null;
		try {
			ParametersI param = new ParametersI();
			param.map.put("id", omero.rtypes.rlong(pixelsID));
			files = service.findAllByQuery(
					"select ofile from OriginalFile as ofile left join " +
					"ofile.pixelsFileMaps as pfm left join pfm.child as " +
					"child where child.id = :id", param);
		} catch (Exception e) {
			throw new DSAccessException("Cannot retrieve original file", e);
		}

		Map<Boolean, Object> result = new HashMap<Boolean, Object>();
		if (files == null || files.size() == 0) return result;
		RawFileStorePrx store;
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
			store = getRawFileService();
			try {
				store.setFileId(of.getId().getValue()); 
			} catch (Exception e) {
				e.printStackTrace();
				handleException(e, "Cannot set the file's id.");
			}
			fullPath = path+of.getName().getValue();
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
					closeService(store);
				}
			} catch (IOException e) {
				if (f != null) f.delete();
				notDownloaded.add(of.getName().getValue());
				closeService(store);
				throw new DSAccessException("Cannot create file with path " +
											fullPath, e);
			}
			closeService(store);
		}
		result.put(Boolean.valueOf(true), files);
		result.put(Boolean.valueOf(false), notDownloaded);
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
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.  
	 */
	synchronized File downloadFile(File file, long fileID, long size)
		throws DSAccessException, DSOutOfServiceException
	{
		if (file == null) return null;
		isSessionAlive();
		if (size <= 0) {
			OriginalFile of = getOriginalFile(fileID);
			if (of != null) size = of.getSize().getValue();
		}
		RawFileStorePrx store = getRawFileService();
		try {
			store.setFileId(fileID);
		} catch (Throwable e) {
			closeService(store);
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
			closeService(store);
			throw new DSAccessException("Cannot create file  " +path, e);
		}
		closeService(store);
		
		return file;
	}
	
	/**
	 * Closes the specified service.
	 * 
	 * @param svc The service to handle.
	 */
	private void closeService(StatefulServiceInterfacePrx svc)
	{
		try {
			svc.close();
		} catch (Exception e) { //ignore
		}
	}
	
	/** 
	 * Shuts downs the stateful services. 
	 * 
	 * @param rendering Pass <code>true</code> to shut down the rendering 
	 * 					services, <code>false</code> otherwise.
	 */
	private void shutDownServices(boolean rendering)
	{
		if (thumbnailService != null)
			closeService(thumbnailService);
		if (pixelsStore != null)
			closeService(pixelsStore);
		if (fileStore != null)
			closeService(fileStore);
		if (importStore != null) {
			importStore.closeServices();
			importStore = null;
		}
		Collection<StatefulServiceInterfacePrx> l = reServices.values();
		if (l != null && rendering) {
			Iterator<StatefulServiceInterfacePrx> i = l.iterator();
			while (i.hasNext()) {
				closeService(i.next());
			}
			reServices.clear();
		}
		thumbnailService = null;
		pixelsStore = null;
		fileStore = null;
	}
	
	/**
	 * Returns the original file corresponding to the passed id.
	 * 
	 * @param id	The id identifying the file.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
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
			IQueryPrx svc = getQueryService();
			if (svc == null) svc = getQueryService();
			of = (OriginalFile) svc.findByQuery(
					"select p from OriginalFile as p " +
					"where p.id = :id", param);
		} catch (Exception e) {
			handleException(e, "Cannot retrieve original file");
		}
		return of;
	}
	
	/**
	 * Returns the collection of original files related to the specified 
	 * pixels set.
	 * 
	 * @param pixelsID The ID of the pixels set.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.  
	 */
	List getOriginalFiles(long pixelsID)
		throws DSAccessException, DSOutOfServiceException
	{
		isSessionAlive();
		List files = null;
		try {
			ParametersI param = new ParametersI();
			param.map.put("id", omero.rtypes.rlong(pixelsID));
			IQueryPrx svc = getQueryService();
			if (svc == null) svc = getQueryService();
			files = svc.findAllByQuery(
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
	 * @param mimeType		 The mimeType of the file.
	 * @param originalFileID The id of the file or <code>-1</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.  
	 */
	synchronized OriginalFile uploadFile(File file, String mimeType, 
			long originalFileID)
		throws DSAccessException, DSOutOfServiceException
	{
		if (file == null)
			throw new IllegalArgumentException("No file to upload");
		if (mimeType == null || mimeType.length() == 0)
			mimeType =  DEFAULT_MIMETYPE;
		isSessionAlive();
		RawFileStorePrx store = null;
		OriginalFile save = null;
		boolean fileCreated = false;
		try {
			store = getRawFileService();
			if (store == null) store = getRawFileService();
			OriginalFile oFile;
			if (originalFileID <= 0) {
				oFile = new OriginalFileI();
				String name = file.getName();
				oFile.setName(omero.rtypes.rstring(name));
				String absolutePath = file.getAbsolutePath();
				String path = absolutePath.substring(0, 
						absolutePath.length()-name.length());
				oFile.setPath(omero.rtypes.rstring(path));
				oFile.setSize(omero.rtypes.rlong(file.length()));
				//Need to be modified
				oFile.setSha1(omero.rtypes.rstring("pending"));
				oFile.setMimetype(omero.rtypes.rstring(mimeType));
				save = 
					(OriginalFile) getUpdateService().saveAndReturnObject(
							oFile);
				store.setFileId(save.getId().getValue());
				fileCreated = true;
			} else {
				oFile = (OriginalFile) findIObject(OriginalFile.class.getName(), 
					originalFileID);
				if (oFile == null) {
					oFile = new OriginalFileI();
					String name = file.getName();
					oFile.setName(omero.rtypes.rstring(name));
					String absolutePath = file.getAbsolutePath();
					String path = absolutePath.substring(0, 
							absolutePath.length()-name.length());
					oFile.setPath(omero.rtypes.rstring(path));
					oFile.setSize(omero.rtypes.rlong(file.length()));
					//Need to be modified
					oFile.setSha1(omero.rtypes.rstring("pending"));
					oFile.setMimetype(omero.rtypes.rstring(mimeType));
					save = 
						(OriginalFile) getUpdateService().saveAndReturnObject(
								oFile);
					store.setFileId(save.getId().getValue());
					fileCreated = true;
				} else {
					OriginalFile newFile = new OriginalFileI();
					newFile.setId(omero.rtypes.rlong(originalFileID));
					newFile.setName(omero.rtypes.rstring(file.getName()));
					newFile.setPath(omero.rtypes.rstring(
							file.getAbsolutePath()));
					newFile.setSize(omero.rtypes.rlong(file.length()));
					newFile.setSha1(omero.rtypes.rstring("pending"));
					oFile.setMimetype(oFile.getMimetype());
					save = (OriginalFile) 
						getUpdateService().saveAndReturnObject(newFile);
					store.setFileId(save.getId().getValue());
				}
			}
		} catch (Exception e) {
			closeService(store);
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
			save = store.save();
			closeService(store);
		} catch (Exception e) {
			try {
				if (fileCreated) deleteObject(save);
				if (stream != null) stream.close();
				closeService(store);
			} catch (Exception ex) {}
			closeService(store);
			throw new DSAccessException("Cannot upload the file with path " +
					file.getAbsolutePath(), e);
		}
		return save;
	}
	
	/**
	 * Modifies the password of the currently logged in user.
	 * 
	 * @param password	The new password.
	 * @param oldPassword The old password.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	void changePassword(String password, String oldPassword)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IAdminPrx service = getAdminService();
			if (service == null) service = getAdminService();
			service.changePasswordWithOldPassword(
					omero.rtypes.rstring(oldPassword), 
					omero.rtypes.rstring(password));
		} catch (Throwable t) {
			handleException(t, "Cannot modify password. ");
		}
	}

	/**
	 * Updates the profile of the specified experimenter.
	 * 
	 * @param exp	The experimenter to handle.
	 * @param currentUserID The identifier of the user currently logged in.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	void updateExperimenter(Experimenter exp, long currentUserID) 
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();		
		try {
			if (exp == null) return;
			IAdminPrx svc = getAdminService();
			if (svc == null) svc = getAdminService();
			if (exp.getId().getValue() == currentUserID)
				svc.updateSelf(exp);
			else svc.updateExperimenter(exp);
		} catch (Throwable t) {
			handleException(t, "Cannot update the user. ");
		}
	}

	/**
	 * Updates the specified group.
	 * 
	 * @param group	The group to update.
	 * @param permissions The new permissions.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	GroupData updateGroup(ExperimenterGroup group, 
			Permissions permissions) 
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IAdminPrx svc = getAdminService();
			if (svc == null) svc = getAdminService();
			svc.updateGroup(group);
			if (permissions != null) {
				svc.changePermissions(findIObject(group), permissions);
			}
			return (GroupData) PojoMapper.asDataObject(
					(ExperimenterGroup) findIObject(group));
		} catch (Throwable t) {
			handleException(t, "Cannot update the group. ");
		}
		return null;
	}

	/**
	 * Adds or removes the passed experimenters from the specified system group.
	 * 
	 * @param toAdd Pass <code>true</code> to add the experimenters as owners,
	 * 				<code>false</code> otherwise.
	 * @param experimenters The experimenters to add or remove.
	 * @param systemGroup	The roles to handle.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	void modifyExperimentersRoles(boolean toAdd, 
			List<ExperimenterData> experimenters, String systemGroup)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IAdminPrx svc = getAdminService();
			if (svc == null) svc = getAdminService();
			if (toAdd) {
				Iterator<ExperimenterData> i = experimenters.iterator();
				ExperimenterData exp;
				List<GroupData> list;
				Iterator<GroupData> j;
				GroupExperimenterMap gMap;
				GroupData group;
				List<ExperimenterGroup> groups;
				boolean added = false;
				ExperimenterGroup gs = svc.lookupGroup(systemGroup);
				while (i.hasNext()) {
					exp = i.next();
					list = exp.getGroups();
					j = list.iterator();
					while (j.hasNext()) {
						group = j.next();
						if (group.getName().equals(systemGroup))
							added = true;
						
					}
					if (!added) {
						groups = new ArrayList<ExperimenterGroup>();
						groups.add(gs);
						svc.addGroups(exp.asExperimenter(), groups);
					}		
				}
			} else {
				Iterator<ExperimenterData> i = experimenters.iterator();
				ExperimenterData exp;
				List<GroupData> list;
				Iterator<GroupData> j;
				GroupExperimenterMap gMap;
				GroupData group;
				List<ExperimenterGroup> groups;
				while (i.hasNext()) {
					exp = i.next();
					list = exp.getGroups();
					groups = new ArrayList<ExperimenterGroup>();
					j = list.iterator();
					while (j.hasNext()) {
						group = j.next();
						if (group.getName().equals(systemGroup)) {
							groups.add(group.asGroup());
						}
					}
					if (groups.size() > 0)
						svc.removeGroups(exp.asExperimenter(), groups);
				}
			}
		} catch (Throwable t) {
			handleException(t, "Cannot modify the roles of the experimenters.");
		}
	}
	
	/**
	 * Adds the passed experimenters as owner of the group if the flag is
	 * <code>true</code>, removes them otherwise.
	 * 
	 * @param toAdd Pass <code>true</code> to add the experimenters as owners,
	 * 				<code>false</code> otherwise.
	 * @param group	The group to handle.
	 * @param experimenters The experimenters to add or remove.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service. 
	 */
	void handleGroupOwners(boolean toAdd, ExperimenterGroup group, 
			List<Experimenter> experimenters)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IAdminPrx svc = getAdminService();
			if (toAdd) svc.addGroupOwners(group, experimenters);
			else svc.removeGroupOwners(group, experimenters);
		} catch (Throwable t) {
			handleException(t, "Cannot handle the group ownership. ");
		}
	}
	
	/**
	 * Returns the XY-plane identified by the passed z-section, time-point 
	 * and wavelength.
	 * 
	 * @param pixelsID 	The id of pixels containing the requested plane.
	 * @param z			The selected z-section.
	 * @param t			The selected time-point.
	 * @param c			The selected wavelength.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	synchronized byte[] getPlane(long pixelsID, int z, int t, int c)
		throws DSOutOfServiceException, DSAccessException, FSAccessException
	{
		isSessionAlive();
		try {
			RawPixelsStorePrx service = getPixelsStore();
			if (service == null) service = getPixelsStore();
			service.setPixelsId(pixelsID, false);
			byte[] plane = service.getPlane(z, c, t);
			service.close();
			return plane;
		} catch (Throwable e) {
			String s = "Cannot retrieve the plane " +
			"(z="+z+", t="+t+", c="+c+") for pixelsID: "+pixelsID;
			handleFSException(e, s);
			handleException(e, s);
		}
		return null;
	}

	/**
	 * Returns the free or available space (in Kilobytes) on the file system
	 * including nested sub-directories.
	 * 
	 * @param Either a group or a user.
	 * @param id The identifier of the user or group or <code>-1</code> 
	 * 			 if not specified.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	long getFreeSpace(Class type, long id)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IRepositoryInfoPrx service = getRepositoryService();
			if (service == null) service = getRepositoryService();
			return service.getFreeSpaceInKilobytes();
		} catch (Throwable e) {
			handleException(e, "Cannot retrieve the free space");
		}
		return -1;
	}

	/**
	 * Returns the used space (in Kilobytes) on the file system
	 * including nested sub-directories.
	 * 
	 * @param Either a group or a user.
	 * @param id The identifier of the user or group or <code>-1</code> 
	 * 			 if not specified.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	long getUsedSpace(Class type, long id)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			if (id < 0)
				return getRepositoryService().getUsedSpaceInKilobytes();
			StringBuffer buffer = new StringBuffer();
			buffer.append("select f from OriginalFile as f ");
			buffer.append("left outer join fetch f.details.owner as o ");
			buffer.append("where o.id = :userID");
			ParametersI param = new ParametersI();
			param.addLong("userID", id);
			List<IObject> result = 
				getQueryService().findAllByQuery(buffer.toString(), param);
			if (result == null) return -1;
			Iterator<IObject> i = result.iterator();
			OriginalFile f;
			long count = 0;
			while (i.hasNext()) {
				f = (OriginalFile) i.next();
				if (f.getSize() != null) count += f.getSize().getValue();
			}
			return count;
		} catch (Throwable e) {
			handleException(e, "Cannot retrieve the free space");
		}
		return -1;
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
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Collection getImages(Parameters map, boolean asDataObject)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IContainerPrx service = getPojosService();
			if (service == null) service = getPojosService();
			List result = service.getImagesByOptions(map);
			if (asDataObject) return PojoMapper.asDataObjects(result);
			return result;
		} catch (Exception e) {
			handleException(e, "Cannot retrieve the images imported during " +
							"the specified period.");
		}
		return new HashSet();
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
	 * 						<code>PlateData</code>.
	 * @param nodes			The nodes to apply settings to. 
	 * @return <true> if the call was successful, <code>false</code> otherwise.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
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
			if (service == null) service = getRenderingSettingsService();
			String klass = convertPojos(rootNodeType).getName();
			if (klass.equals(Image.class.getName())) failure.addAll(nodes);
			success = service.resetDefaultsInSet(klass, nodes);
		} catch (Exception e) {
			handleException(e, "Cannot reset the rendering settings.");
		}
		Iterator<Long> i = success.iterator(); 
		Long id;
		while (i.hasNext()) {
			id = i.next();
			if (failure.contains(id)) failure.remove(id);
		}
		Map<Boolean, List> result = new HashMap<Boolean, List>(2);
		result.put(Boolean.valueOf(true), success);
		result.put(Boolean.valueOf(false), failure);
		return result;
	}
  
	/**
	 * Resets the rendering settings for the images contained in the 
	 * specified datasets if the rootType is <code>DatasetData</code>.
	 * Resets the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param rootNodeType	The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code>.
	 * @param nodes			The nodes to apply settings to. 
	 * @return <true> if the call was successful, <code>false</code> otherwise.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Map setMinMaxSettings(Class rootNodeType, List nodes) 
		throws DSOutOfServiceException, DSAccessException
	{
		List<Long> success = new ArrayList<Long>();
		List<Long> failure = new ArrayList<Long>();
		
		isSessionAlive();
		try {
			IRenderingSettingsPrx service = getRenderingSettingsService();
			if (service == null) service = getRenderingSettingsService();
			String klass = convertPojos(rootNodeType).getName();
			if (klass.equals(Image.class.getName())) failure.addAll(nodes);
			success = service.resetMinMaxInSet(klass, nodes);
		} catch (Exception e) {
			handleException(e, "Cannot reset the rendering settings.");
		}
		Iterator<Long> i = success.iterator(); 
		Long id;
		while (i.hasNext()) {
			id = i.next();
			if (failure.contains(id)) failure.remove(id);
		}
		Map<Boolean, List> result = new HashMap<Boolean, List>(2);
		result.put(Boolean.valueOf(true), success);
		result.put(Boolean.valueOf(false), failure);
		return result;
	}
	
	/**
	 * Resets the rendering settings, used by the owner of the images contained 
	 * in the specified datasets if the rootType is <code>DatasetData</code>.
	 * Resets the settings to the passed images if the type is 
	 * <code>ImageData</code>.
	 * 
	 * @param rootNodeType	The type of nodes. Can either be 
	 * 						<code>ImageData</code>, <code>DatasetData</code>.
	 * @param nodes			The nodes to apply settings to. 
	 * @return <true> if the call was successful, <code>false</code> otherwise.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Map setOwnerRenderingSettings(Class rootNodeType, List nodes) 
		throws DSOutOfServiceException, DSAccessException
	{
		List<Long> success = new ArrayList<Long>();
		List<Long> failure = new ArrayList<Long>();
		
		isSessionAlive();
		try {
			IRenderingSettingsPrx service = getRenderingSettingsService();
			if (service == null) service = getRenderingSettingsService();
			String klass = convertPojos(rootNodeType).getName();
			if (klass.equals(Image.class.getName())) failure.addAll(nodes);
			success = service.resetDefaultsByOwnerInSet(klass, nodes);
		} catch (Exception e) {
			handleException(e, "Cannot reset the rendering settings.");
		}
		Iterator<Long> i = success.iterator(); 
		Long id;
		while (i.hasNext()) {
			id = i.next();
			if (failure.contains(id)) failure.remove(id);
		}
		Map<Boolean, List> result = new HashMap<Boolean, List>(2);
		result.put(Boolean.valueOf(true), success);
		result.put(Boolean.valueOf(false), failure);
		return result;
	}
	
	/**
	 * Applies the rendering settings associated to the passed pixels set 
	 * to the images contained in the specified datasets or plate.
	 * if the rootType is <code>DatasetData</code> or <code>PlateData</code>.
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
	 * @throws DSAccessException        If an error occurred while trying to 
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
			if (service == null) service = getRenderingSettingsService();
			Map m  = service.applySettingsToSet(pixelsID, 
					convertPojos(rootNodeType).getName(),
					nodes);
			success = (List) m.get(Boolean.valueOf(true));
			failure = (List) m.get(Boolean.valueOf(false));
		} catch (Exception e) {
			handleException(e, "Cannot paste the rendering settings.");
		}
		Map<Boolean, List> result = new HashMap<Boolean, List>(2);
		result.put(Boolean.valueOf(true), success);
		result.put(Boolean.valueOf(false), failure);
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
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Map getRenderingSettings(long pixelsID, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		Map map = new HashMap();
		isSessionAlive();
		try {
			IPixelsPrx service = getPixelsService();
			if (service == null) service = getPixelsService();
			List results = service.retrieveAllRndSettings(pixelsID, userID);
			
			if (results == null || results.size() == 0) return map;
			Iterator i = results.iterator();
			RenderingDef rndDef;
			Experimenter exp;
			while (i.hasNext()) {
				rndDef = (RenderingDef) i.next();
				exp = rndDef.getDetails().getOwner();
				map.put(PojoMapper.asDataObject(exp), 
						PixelsServicesFactory.convert(rndDef));
			}
			return map;
		} catch (Exception e) {
			handleException(e, "Cannot retrieve the rendering settings " +
								"for: "+pixelsID);
		}
		return map;
	}
	
	/**
	 * Retrieves all the rendering settings linked to the specified set
	 * of pixels.
	 * 
	 * @param pixelsID	The pixels ID.
	 * @param userID	The id of the user.
	 * @param convert   Pass <code>true</code> to convert the object,
	 * 					<code>false</code> otherwise.
	 * @return Map whose key is the experimenter who set the settings,
	 * 		  and the value is the rendering settings itself.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<RndProxyDef> getRenderingSettingsFor(long pixelsID, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IPixelsPrx service = getPixelsService();
			if (service == null) service = getPixelsService();
			List results = service.retrieveAllRndSettings(pixelsID, userID);
			List<RndProxyDef> l = new ArrayList<RndProxyDef>();
			if (results == null || results.size() == 0) return l;
			Iterator i = results.iterator();
			while (i.hasNext()) {
				l.add(PixelsServicesFactory.convert((RenderingDef) i.next()));
			}
			return l;
		} catch (Exception e) {
			handleException(e, "Cannot retrieve the rendering settings " +
								"for: "+pixelsID);
		}
		return new ArrayList();
	}
	
	/**
	 * Retrieves the rendering settings for the specified pixels set.
	 * 
	 * @param pixelsID  The pixels ID.
	 * @param userID	The id of the user who set the rendering settings.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	RenderingDef getRenderingDef(long pixelsID, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IPixelsPrx service = getPixelsService();
			if (service == null) service = getPixelsService();
			return service.retrieveRndSettingsFor(pixelsID, userID);
		} catch (Exception e) {
			handleException(e, "Cannot retrieve the rendering settings");
		}
		return null;
	}

	/**
	 * Retrieves the annotations of the passed type.
	 * 
	 * @param type The type of annotations to include.
	 * @param toInclude The collection of name space to include.
	 * @param toExclude The collection of name space to exclude.
	 * @param options The options.
	 * @return See above.
	 *@throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Set loadSpecificAnnotation(Class type, List<String> toInclude, 
			List<String> toExclude, Parameters options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IMetadataPrx service = getMetadataService();
			if (service == null) service = getMetadataService();
			return PojoMapper.asDataObjects(
					service.loadSpecifiedAnnotations(
							convertPojos(type).getName(), toInclude, 
							toExclude, options));
		} catch (Exception e) {
			handleException(e, "Cannot retrieve the annotations");
		}
		return new HashSet();
	}
	
	/**
	 * Counts the annotations of the passed type.
	 * 
	 * @param type The type of annotations to include.
	 * @param toInclude The collection of name space to include.
	 * @param toExclude The collection of name space to exclude.
	 * @param options The options.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	long countSpecificAnnotation(Class type, List<String> toInclude, 
			List<String> toExclude, Parameters options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IMetadataPrx service = getMetadataService();
			if (service == null) service = getMetadataService();
			RLong value = service.countSpecifiedAnnotations(
					convertPojos(type).getName(), toInclude, 
					toExclude, options);
			if (value == null) return -1;
			return value.getValue();
		} catch (Exception e) {
			handleException(e, "Cannot retrieve the annotations");
		}
		return -1;
	}
	
	/**
	 * Returns the number of annotations used by the passed user but not
	 * owned.
	 * 
	 * @param annotationType The type of annotation.
	 * @param userID The identifier of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	long countAnnotationsUsedNotOwned(Class annotationType, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		long count = 0;
		try {
			IMetadataPrx service = getMetadataService();
			if (service == null) service = getMetadataService();
			RLong value = service.countAnnotationsUsedNotOwned(
					convertAnnotation(annotationType), userID);
			if (value != null)
				count = value.getValue();
			if (count < 0) count = 0;
		} catch (Exception e) {
			handleException(e, "Cannot count the type of annotation " +
					"used by the specified user");
		}
		return count;
	}
	
	/**
	 * Loads the tag Sets and the orphaned tags, if requested.
	 * 
	 * @param userID 
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Collection loadAnnotationsUsedNotOwned(Class annotationType, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		Set result = new HashSet();
		try {
			IMetadataPrx service = getMetadataService();
			if (service == null) service = getMetadataService();
			List<IObject> set = service.loadAnnotationsUsedNotOwned(
					convertAnnotation(annotationType), userID);
			Iterator<IObject> i = set.iterator();
			IObject o;
			while (i.hasNext()) {
				o = i.next();
				if (TagAnnotationData.class.equals(annotationType)) {
					result.add(new TagAnnotationData((TagAnnotation) o));
				}
			}
			return result;
		} catch (Exception e) {
			handleException(e, "Cannot find the Used Tags.");
		}
		return result;
	}
	
	/** 
	 * Searches the images acquired or created during a given period of time.
	 * 
	 * @param context The context of the search.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Object searchByTime(SearchDataContext context)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		ParametersI param = new ParametersI();
		param.map = new HashMap<String, RType>();
		StringBuffer buf = new StringBuffer();
		buf.append("select img from Image as img ");
		buf.append("left outer join fetch img.pixels as pix ");
		buf.append("left outer join fetch pix.pixelsType as pt ");
		buf.append("left outer join fetch img.details.owner as owner ");
		boolean condition = false;
		Timestamp start = context.getStart();
		Timestamp end = context.getEnd();
		//Sets the time
		switch (context.getTimeIndex()) {
			case SearchDataContext.CREATION_TIME:
				if (start != null) {
					condition = true;
					buf.append("where img.acquisitionDate > :start ");
					param.map.put("start", omero.rtypes.rtime(start.getTime()));
					if (end != null) {
						param.map.put("end", omero.rtypes.rtime(end.getTime()));
						buf.append("and img.acquisitionDate < :end ");
					}
				} else {
					if (end != null) {
						condition = true;
						param.map.put("end", omero.rtypes.rtime(end.getTime()));
						buf.append("where img.acquisitionDate < :end ");
					}
				}
				break;
			case SearchDataContext.MODIFICATION_TIME:
				if (start != null) {
					condition = true;
					param.map.put("start", omero.rtypes.rtime(start.getTime()));
					buf.append("where img.details.creationEvent.time > :start ");
					if (end != null) {
						param.map.put("end", omero.rtypes.rtime(end.getTime()));
						buf.append("and img.details.creationEvent.time < :end ");
					}
				} else {
					if (end != null) {
						condition = true;
						param.map.put("end", omero.rtypes.rtime(end.getTime()));
						buf.append("where img.details.creationEvent.time < :end ");
					}
				}
				break;
			case SearchDataContext.ANNOTATION_TIME:
		}
		try {
			List<ExperimenterData> l = context.getOwners();
			List<Long> ids = new ArrayList<Long>();
			if (l != null) {
				Iterator<ExperimenterData> i = l.iterator();
				while (i.hasNext()) {
					ids.add(i.next().getId());
				}
			}
			param.addLongs("ids", ids);
			if (condition) {
				buf.append(" and owner.id in (:ids)");
			} else 
				buf.append("where owner.id in (:ids)");
			
			IQueryPrx service = getQueryService();
			if (service == null) service = getQueryService();
			return PojoMapper.asDataObjects(
					service.findAllByQuery(buf.toString(), param));
		} catch (Throwable e) {
			handleException(e, "Cannot retrieve the images.");
		}
		
		return new HashSet();
	}
	
	/**
	 * Searches for data.
	 * 
	 * @param context The context of search.
	 * @return The found objects.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Object performSearch(SearchDataContext context)
		throws DSOutOfServiceException, DSAccessException
	{
		Map<Integer, Object> results = new HashMap<Integer, Object>();
		List<Class> types = context.getTypes();
		List<Integer> scopes = context.getScope();
		if (types == null || types.size() == 0) return new HashMap();
		//if (scopes == null || scopes.size() == 0) return new HashMap();
		isSessionAlive();
		SearchPrx service = getSearchService();
		if (service == null) service = getSearchService();
		try {
			service.setAllowLeadingWildcard(true);
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
				} else {
					fSome = formatText(some, "");
					fMust = formatText(must, "");
					fNone = formatText(none, "");
				}
				owner = owners.iterator();
				//if (fSome != null) {
				//while (owner.hasNext()) {
					//d = owner.next();
					//service.onlyOwnedBy(d);
					service.bySomeMustNone(fSome, fMust, fNone);
					size = handleSearchResult(
							convertTypeForSearch(Image.class), rType, 
							service);
					if (size instanceof Integer)
						results.put(key, size);
					service.clearQueries();
					if (!(size instanceof Integer) && fSomeSec != null &&
							fSomeSec.size() > 0) {
						service.bySomeMustNone(fSomeSec, fMustSec, 
								fNoneSec);
						size = handleSearchResult(
								convertTypeForSearch(Image.class), 
								rType, service);
						if (size instanceof Integer) 
							results.put(key, size);
						service.clearQueries();
					}
				//}
				//}
			}
			service.close();
			return results;
		} catch (Throwable e) {
			try {
				service.close();
			} catch (Exception ex) {
				//digest the exception
			}
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
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List filterBy(Class annotationType, List<String> terms,
				Timestamp start, Timestamp end, ExperimenterData exp)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			SearchPrx service = getSearchService();
			if (service == null) service = getSearchService();
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
			List rType = new ArrayList();
			//service.bySomeMustNone(fSome, fMust, fNone);
			service.bySomeMustNone(t, null, null);
			Object size = handleSearchResult(
					convertTypeForSearch(annotationType), rType, service);
			if (size instanceof Integer) rType = new ArrayList();
			return rType;
		} catch (Exception e) {
			handleException(e, "Filtering by annotation not valid");
		}
		return new ArrayList();
	}
	
	/**
	 * Retrieves all containers of a given type.
	 * The containers are not linked to any of their children.
	 * 
	 * @param type		The type of container to retrieve.
	 * @param userID	The id of the owner of the container.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Set fetchContainers(Class type, long userID) 
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQueryPrx service = getQueryService();
			if (service == null) service = getQueryService();
			Parameters p = new ParametersI();
			p.map = new HashMap<String, RType>();
			p.map.put("id", omero.rtypes.rlong(userID));
			String table = getTableForClass(type);
			return PojoMapper.asDataObjects(service.findAllByQuery(
	                "from "+table+" as p where p.details.owner.id = :id", p));
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the containers.");
		}
		return new HashSet();
	}
	
	/**
	 * 
	 * @param type
	 * @param annotationIds
	 * @param ownerIds
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Set getAnnotatedObjects(Class type, Set<Long> annotationIds, 
			Set<Long> ownerIds)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQueryPrx service = getQueryService();
			if (service == null) service = getQueryService();
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
		} catch (Exception e) {
			handleException(e, "Cannot retrieve the annotated objects");
		}
		return new HashSet();
	}
	
	/**
	 * Returns the number of images related to a given tag.
	 * 
	 * @param rootNodeIDs
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Map getDataObjectsTaggedCount(List rootNodeIDs)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQueryPrx service = getQueryService();
			if (service == null) service = getQueryService();
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
					m.put(id, Long.valueOf(l.size()));
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
	 * Removes the description linked to the tags.
	 * 
	 * @param tagID  The id of tag to handle.
	 * @param userID The id of the user who annotated the tag.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	void removeTagDescription(long tagID, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			String type = "ome.model.annotations.TextAnnotation";
			IQueryPrx service = getQueryService();
			if (service == null) service = getQueryService();
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
						(child instanceof TermAnnotation)))  {
						deleteObject(link);
						deleteObject(child);
					}
				}
			}
		} catch (Exception e) {
			handleException(e, "Cannot remove the tag description.");
		}
	}
	
	/** Keeps the services alive. */
	void keepSessionAlive()
	{
		Collection<ServiceInterfacePrx> 
			all = new HashSet<ServiceInterfacePrx>();
		if (services.size() > 0) all.addAll(services);
		if (reServices.size() > 0) all.addAll(reServices.values());
		/*
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
		*/
		if (all.size() == 0) return;
		ServiceInterfacePrx[] entries = (ServiceInterfacePrx[]) 
			all.toArray(new ServiceInterfacePrx[all.size()]);
		try {
			entryEncrypted.keepAllAlive(entries);
		} catch (Exception e) {
			// TODO: handle exception
		}
		try {
			if (entryUnencrypted != null)
				entryUnencrypted.keepAllAlive(entries);
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	/**
	 * Projects the specified set of pixels according to the projection's 
	 * parameters. Adds the created image to the passed dataset.
	 * 
	 * @param pixelsID  The id of the pixels set.
	 * @param startT	The time-point to start projecting from.
	 * @param endT		The time-point to end projecting.
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
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	ImageData projectImage(long pixelsID, int startT, int endT, int startZ, 
						int endZ, int stepping, ProjectionType algorithm, 
						List<Integer> channels, String name, String pixType)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IProjectionPrx service = getProjectionService();
			if (service == null) service = getProjectionService();
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
			
			return getImage(imageID, new Parameters());
		} catch (Exception e) {
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
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	ImageData getImage(long imageID, Parameters options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			List<Long> ids = new ArrayList<Long>(1);
			ids.add(imageID);
			Set result = getContainerImages(ImageData.class, ids, options);
			if (result != null && result.size() == 1) {
				Iterator i = result.iterator();
				while (i.hasNext())
					return (ImageData) i.next();
			}
			return null;
		} catch (Exception e) {
			handleException(e, "Cannot retrieve the image.");
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
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	RenderingDef createRenderingDef(long pixelsID)
		throws DSOutOfServiceException, DSAccessException
	{
		//TODO: add method to server so that we don't have to make 2 calls.
		isSessionAlive();
		try {
			IPixelsPrx svc = getPixelsService();
			if (svc == null) svc = getPixelsService();
			Pixels pixels = svc.retrievePixDescription(pixelsID);
			if (pixels == null) return null;
			IRenderingSettingsPrx service = getRenderingSettingsService();
			return service.createNewRenderingDef(pixels);
		} catch (Exception e) {
			handleException(e, "Cannot create settings for: "+pixelsID);
		}
		
		return null;
	}
	
	/**
	 * Returns the plate where the specified image has been imported.
	 * 
	 * @param imageID The identifier of the image.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	PlateData getImportedPlate(long imageID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			List results = null;
			Iterator i;
			IQueryPrx service = getQueryService();
			if (service == null) service = getQueryService();
			StringBuilder sb = new StringBuilder();
			ParametersI param = new ParametersI();
			param.addLong("imageID", imageID);
			sb.append("select well from Well as well ");
			sb.append("left outer join fetch well.plate as pt ");
			sb.append("left outer join fetch well.wellSamples as ws ");
			sb.append("left outer join fetch ws.image as img ");
			sb.append("left outer join fetch img.pixels as pix ");
            sb.append("left outer join fetch pix.pixelsType as pt ");
            sb.append("where img.id = :imageID");
            results = service.findAllByQuery(sb.toString(), param);
            if (results.size() > 0) {
            	Well well = (Well) results.get(0);
            	if (well.getPlate() != null)
            		return new PlateData(well.getPlate());
            	return null;
            }
			return null;
		} catch (Exception e) {
			handleException(e, "Cannot load plate");
		}
		return null;
	}
	
	//TMP: 
	Set loadPlateWells(long plateID, long acquisitionID, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			
			List results = null;
			Set<DataObject> wells = new HashSet<DataObject>();
			Iterator i;
			IQueryPrx service = getQueryService();
			if (service == null) service = getQueryService();
			//if no acquisition set. First try to see if we have a id.
			ParametersI param = new ParametersI();
			param.addLong("plateID", plateID);
			StringBuilder sb;
			if (acquisitionID < 0) {
				sb = new StringBuilder();
				sb.append("select pa from PlateAcquisition as pa ");
				sb.append("where pa.plate.id = :plateID");
				results = service.findAllByQuery(sb.toString(), param);
				if (results != null && results.size() > 0)
					acquisitionID = 
						((PlateAcquisition) results.get(0)).getId().getValue();
			}
			
			sb = new StringBuilder();
			
			sb.append("select well from Well as well ");
			sb.append("left outer join fetch well.plate as pt ");
			sb.append("left outer join fetch well.wellSamples as ws ");
			sb.append("left outer join fetch ws.plateAcquisition as pa ");
			sb.append("left outer join fetch ws.image as img ");
			sb.append("left outer join fetch img.pixels as pix ");
            sb.append("left outer join fetch pix.pixelsType as pt ");
            sb.append("where well.plate.id = :plateID");
            if (acquisitionID > 0) {
            	sb.append(" and pa.id = :acquisitionID");
            	param.addLong("acquisitionID", acquisitionID);
            } 
            results = service.findAllByQuery(sb.toString(), param);
			i = results.iterator();
			while (i.hasNext()) {
				wells.add((WellData) PojoMapper.asDataObject((Well) i.next()));
			}
			return wells;
		} catch (Exception e) {
			handleException(e, "Cannot load plate");
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
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Object loadImageAcquisitionData(long imageID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		ParametersI po = new ParametersI();
		po.acquisitionData();
		List<Long> ids = new ArrayList<Long>(1);
		ids.add(imageID);
		IContainerPrx service = getPojosService();
		if (service == null) service = getPojosService();
        try {
        	List images = service.getImages(Image.class.getName(), ids, po);
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
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Object loadChannelAcquisitionData(long channelID)
		throws DSOutOfServiceException, DSAccessException
	{
		//stage Label
		isSessionAlive();
		try {
			IMetadataPrx service = getMetadataService();
			if (service == null) service = getMetadataService();
			List<Long> ids = new ArrayList<Long>(1);
			ids.add(channelID);
			List l = service.loadChannelAcquisitionData(ids);
			if (l != null && l.size() == 1) {
				LogicalChannel lc = (LogicalChannel) l.get(0);
				ChannelAcquisitionData data = new ChannelAcquisitionData(lc);
				LightSourceData src = data.getLightSource();
				if (src == null || src.isLoaded()) return data;
				//Not loaded so need to load
				IObject io = src.asIObject();
				if (io instanceof Laser) { //only case to handle.
					StringBuilder sb = new StringBuilder();
					sb.append("select l from Laser as l ");
					sb.append("left outer join fetch l.type ");
					sb.append("left outer join fetch l.laserMedium ");
					sb.append("left outer join fetch l.pulse as pulse ");
					sb.append("left outer join fetch l.pump as pump ");
					sb.append("left outer join fetch pump.type as pt ");
					sb.append("where l.id = :id");
					ParametersI param = new ParametersI();
					param.addId(src.getId());
					Laser laser = (Laser) getQueryService().findByQuery(
							sb.toString(), param);
					if (laser != null)
						data.setLightSource(new LightSourceData(laser));
				}
				return data;
			}
			return null;
		} catch (Exception e) {
			handleException(e, "Cannot load channel acquisition data.");
		}
		return null;
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
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	IObject getEnumeration(Class klass, String value)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQueryPrx service = getQueryService();
			if (service == null) service = getQueryService();
			return service.findByString(klass.getName(), "value", value);
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
	 * @throws DSAccessException        If an error occurred while trying to 
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
			if (service == null)
				service = getPixelsService();
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
	
	/**
	 * Loads the tags.
	 * 
	 * @param id  The id of the tags.
	 * @param options
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Collection loadTags(Long id, Parameters options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IMetadataPrx service = getMetadataService();
			if (service == null) service = getMetadataService();
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
	
	/**
	 * Loads the tag Sets and the orphaned tags, if requested.
	 * 
	 * @param options
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Collection loadTagSets(Parameters options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IMetadataPrx service = getMetadataService();
			if (service == null) service = getMetadataService();
			List<IObject> list = service.loadTagSets(options);
			return PojoMapper.asDataObjects(list);
		} catch (Exception e) {
			handleException(e, "Cannot find the Tags.");
		}
		return new HashSet();
	}
	
	/**
	 * Returns the collection of plane info object related to the specified
	 * pixels set.
	 * 
	 * @param pixelsID  The id of the pixels set.
	 * @param z 		The selected z-section or <code>-1</code>.
     * @param t 		The selected time-point or <code>-1</code>.
     * @param channel 	The selected time-point or <code>-1</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<IObject> loadPlaneInfo(long pixelsID, int z, int t, int channel)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		IQueryPrx service = getQueryService();
		if (service == null) service = getQueryService();
		StringBuilder sb;
		ParametersI param;
		sb = new StringBuilder();
		param = new ParametersI();
		sb.append("select info from PlaneInfo as info ");
        sb.append("where pixels.id = :id");
        param.addLong("id", pixelsID);
        if (z >= 0) {
        	 sb.append(" and info.theZ = :z");
        	 param.map.put("z", omero.rtypes.rint(z));
        }
        if (t >= 0) {
        	sb.append(" and info.theT = :t");
        	 param.map.put("t", omero.rtypes.rint(t));
        }
        if (channel >= 0) {
        	sb.append(" and info.theC = :c");
        	param.map.put("c", omero.rtypes.rint(channel));
        }
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
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	void fillEnumerations()
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		getEnumerations(OmeroMetadataService.IMMERSION);
		getEnumerations(OmeroMetadataService.CORRECTION);
		getEnumerations(OmeroMetadataService.MEDIUM);
		getEnumerations(OmeroMetadataService.FORMAT);
		getEnumerations(OmeroMetadataService.DETECTOR_TYPE);
		getEnumerations(OmeroMetadataService.BINNING);
		getEnumerations(OmeroMetadataService.CONTRAST_METHOD);
		getEnumerations(OmeroMetadataService.ILLUMINATION_TYPE);
		getEnumerations(OmeroMetadataService.PHOTOMETRIC_INTERPRETATION);
		getEnumerations(OmeroMetadataService.ACQUISITION_MODE);
		getEnumerations(OmeroMetadataService.LASER_MEDIUM);
		getEnumerations(OmeroMetadataService.LASER_TYPE);
		getEnumerations(OmeroMetadataService.LASER_PULSE);
		getEnumerations(OmeroMetadataService.ARC_TYPE);
		getEnumerations(OmeroMetadataService.FILAMENT_TYPE);
		getEnumerations(OmeroMetadataService.FILTER_TYPE);
		getEnumerations(OmeroMetadataService.MICROSCOPE_TYPE);
	}

	/**
	 * Deletes the passed object using the {@link IDeletePrx} service.
	 * Returns an empty list of nothing prevent the delete to happen,
	 * otherwise returns a list of objects preventing the delete to happen.
	 * 
	 * @param objectType The type of object to delete.
	 * @param objectID   The id of the object to delete.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<IObject> removeObject(Class objectType, Long objectID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IDeletePrx service = getDeleteService();
			if (service == null) service = getDeleteService();
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
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Object deleteImage(Image object)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IDeletePrx service = getDeleteService();
			if (service == null) service = getDeleteService();
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
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<IObject> checkImage(Image object)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IDeletePrx service = getDeleteService();
			if (service == null) service = getDeleteService();
			return service.checkImageDelete(object.getId().getValue(), true);
		} catch (Exception e) {
			handleException(e, "Cannot delete the image: "+object.getId());
		}
		return new ArrayList<IObject>();
	}
	
	/**
	 * Creates a movie. Returns the id of the annotation hosting the movie.
	 * 
	 * @param imageID 	The id of the image.	
	 * @param pixelsID	The id of the pixels.
	 * @param userID	The id of the user.
     * @param channels 	The channels to map.
     * @param param 	The parameters to create the movie.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws ProcessException If an error occurred while running the script.
	 */
	ScriptCallback saveAs(long userID, SaveAsParam param)
		throws ProcessException, DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		long id = getScriptID(SaveAsParam.SAVE_AS_SCRIPT,
				"Cannot start "+SaveAsParam.SAVE_AS_SCRIPT);
		if (id <= 0) return null;
		List<DataObject> objects = param.getObjects();
		List<RType> ids = new ArrayList<RType>();
		Iterator<DataObject> i = objects.iterator();
		String type = "Image";
		DataObject data;
		while (i.hasNext()) {
			data = i.next();
			if (data instanceof DatasetData) {
				type = "Dataset";
			}
			ids.add(omero.rtypes.rlong(data.getId()));
		}
		Map<String, RType> map = new HashMap<String, RType>();
		map.put("IDs", omero.rtypes.rlist(ids));
		map.put("Data_Type", omero.rtypes.rstring(type));
		map.put("Format", omero.rtypes.rstring(param.getIndexAsString()));
		return runScript(id, map);
	}
	
	/**
	 * Creates a movie. Returns the id of the annotation hosting the movie.
	 * 
	 * @param imageID 	The id of the image.	
	 * @param pixelsID	The id of the pixels.
	 * @param userID	The id of the user.
     * @param channels 	The channels to map.
     * @param param 	The parameters to create the movie.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws ProcessException If an error occurred while running the script.
	 */
	ScriptCallback createMovie(long imageID, long pixelsID, long userID, 
			List<Integer> channels, MovieExportParam param)
		throws ProcessException, DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		long id = getScriptID(param.getScriptName(), 
				"Cannot start "+param.getScriptName());
		if (id <= 0) return null;
		List<RType> set = new ArrayList<RType>(channels.size());
		Iterator<Integer> i = channels.iterator();
		while (i.hasNext()) 
			set.add(omero.rtypes.rint(i.next()));

		RenderingDef def = null;
		int startZ = param.getStartZ();
		int endZ = param.getEndZ();
		if (!param.isZSectionSet()) {
			def = getRenderingDef(pixelsID, userID);
			startZ = def.getDefaultZ().getValue();
			endZ = def.getDefaultZ().getValue();
		}
		int startT = param.getStartT();
		int endT = param.getEndT();
		if (!param.isTimeIntervalSet()) {
			if (def == null) def = getRenderingDef(pixelsID, userID);
			startT = def.getDefaultT().getValue();
			endT = def.getDefaultT().getValue();
		}

		Map<String, RType> map = new HashMap<String, RType>();
		map.put("Image_ID", omero.rtypes.rlong(imageID));
		map.put("Movie_Name", omero.rtypes.rstring(param.getName()));
		map.put("Z_Start", omero.rtypes.rint(startZ));
		map.put("Z_End", omero.rtypes.rint(endZ));
		map.put("T_Start", omero.rtypes.rint(startT));
		map.put("T_End", omero.rtypes.rint(endT));
		map.put("Channels", omero.rtypes.rlist(set));
		map.put("FPS", omero.rtypes.rint(param.getFps()));
		map.put("Show_Plane_Info", 
				omero.rtypes.rbool(param.isLabelVisible()));
		map.put("Show_Time", 
				omero.rtypes.rbool(param.isLabelVisible()));
		map.put("Split_View", omero.rtypes.rbool(false));
		map.put("Scalebar", omero.rtypes.rint(param.getScaleBar()));
		map.put("Format", omero.rtypes.rstring(param.getFormatAsString()));
		if (param.getColor() != null)
			map.put("Overlay_Colour", omero.rtypes.rstring(
					param.getColor()));
		return runScript(id, map);
	}
	
	/**
	 * Returns all the scripts that the user can run.
	 * 
	 * @param experimenter The experimenter or <code>null</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<ScriptObject> loadRunnableScripts()
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		List<ScriptObject> scripts = new ArrayList<ScriptObject>();
		try {
			IScriptPrx svc = getScriptService();
			if (svc == null) svc = getScriptService();
			List<OriginalFile> storedScripts = svc.getScripts();
		
			if (storedScripts == null || storedScripts.size() == 0) 
				return scripts;
			Entry en;
			Iterator<OriginalFile> j = storedScripts.iterator();
			ScriptObject script;
			OriginalFile of;
			RString value;
			String v = null;
			while (j.hasNext()) {
				of = j.next();
				value = of.getName();
				v = of.getPath().getValue()+ value.getValue();
				if (!SCRIPTS_NOT_AVAILABLE_TO_USER.contains(v)) { 
					//&&!SCRIPTS_UI_AVAILABLE.contains(v)) {
					script = new ScriptObject(of.getId().getValue(), 
							of.getPath().getValue(), of.getName().getValue());
					value = of.getMimetype();
					if (value != null) script.setMIMEType(value.getValue());
					scripts.add(script);
				}
			}
			storedScripts = svc.getUserScripts(new ArrayList());
			j = storedScripts.iterator();
			while (j.hasNext()) {
				of = j.next();
				value = of.getName();
				script = new ScriptObject(of.getId().getValue(), 
						of.getPath().getValue(), of.getName().getValue());
				value = of.getMimetype();
				if (value != null) script.setMIMEType(value.getValue());
				script.setOfficial(false);
				scripts.add(script);
			}
		} catch (Exception e) {
			handleException(e, "Cannot load the scripts. ");
		}
		return scripts;
	}
	
	/**
	 * Returns all the official scripts with a UI.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<ScriptObject> loadRunnableScriptsWithUI()
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		List<ScriptObject> scripts = new ArrayList<ScriptObject>();
		try {
			IScriptPrx svc = getScriptService();
			if (svc == null) svc = getScriptService();
			List<OriginalFile> storedScripts = svc.getScripts();
		
			if (storedScripts == null || storedScripts.size() == 0) 
				return scripts;
			Entry en;
			Iterator<OriginalFile> j = storedScripts.iterator();
			ScriptObject script;
			OriginalFile of;
			RString value;
			String v = null;
			while (j.hasNext()) {
				of = j.next();
				value = of.getName();
				v = of.getPath().getValue()+ value.getValue();
				if (SCRIPTS_UI_AVAILABLE.contains(v)) {
					script = new ScriptObject(of.getId().getValue(), 
							of.getPath().getValue(), of.getName().getValue());
					value = of.getMimetype();
					if (value != null) script.setMIMEType(value.getValue());
					scripts.add(script);
				}
			}
		} catch (Exception e) {
			handleException(e, "Cannot load the scripts with UI. ");
		}
		return scripts;
	}
	
	/**
	 * Loads and returns the script w/ parameters corresponding to the passed
	 * identifier.
	 * 
	 * @param scriptID The id of the script.
	 * @return See above.
	 * @throws ProcessException  If the script could not be loaded.
	 */
	ScriptObject loadScript(long scriptID)
		throws ProcessException
	{
		isSessionAlive();
		ScriptObject script = null;
		try {
			IScriptPrx svc = getScriptService();
			if (svc == null) svc = getScriptService();
			script = new ScriptObject(scriptID, "", "");
			script.setJobParams(svc.getParams(scriptID));
		} catch (Exception e) {
			throw new ProcessException("Cannot load the script: "+scriptID, 
					e);
		}
		return script;
	}
	
	/**
	 * Returns all the scripts currently stored into the system.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Map<Long, String> getScriptsAsString()
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IScriptPrx svc = getScriptService();
			if (svc == null) svc = getScriptService();
			List<OriginalFile> scripts = svc.getScripts();
			Map<Long, String> m = new HashMap<Long, String>();
			if (scripts != null) {
				Iterator<OriginalFile> i = scripts.iterator();
				OriginalFile of;
				String name = null;
				RString v;
				while (i.hasNext()) {
					of = i.next();
					v = of.getName();
					if (v != null)
						name = v.getValue();
					if (name != null)
						m.put(of.getId().getValue(), name);
				}
			}
			return m;
		} catch (Exception e) {
			handleException(e, "Cannot load the scripts. ");
		}
		return new HashMap<Long, String>();
	}
	

	/**
	 * Returns all the scripts currently stored into the system.
	 * 
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<OriginalFile> getScripts()
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IScriptPrx svc = getScriptService();
			if (svc == null) svc = getScriptService();
			return svc.getScripts();
		} catch (Exception e) {
			handleException(e, "Cannot load the scripts. ");
		}
		return new ArrayList<OriginalFile>();
	}
	
	/**
	 * Returns the <code>RType</code> corresponding to the passed value.
	 * 
	 * @param value The value to convert.
	 * @return See above.
	 */
	private RType convertValue(Object value)
	{
		Iterator i;
		if (value instanceof String) 
			return omero.rtypes.rstring((String) value);
		else if (value instanceof Boolean) 
			return omero.rtypes.rbool((Boolean) value);
		else if (value instanceof Long) 
			return omero.rtypes.rlong((Long) value);
		else if (value instanceof Integer) 
			return omero.rtypes.rint((Integer) value);
		else if (value instanceof Float) 
			return omero.rtypes.rfloat((Float) value);
		else if (value instanceof List) {
			List l = (List) value;
			i = l.iterator();
			List<RType> list = new ArrayList<RType>(l.size());
			while (i.hasNext()) {
				list.add(convertValue(i.next()));
			}
			return omero.rtypes.rlist(list);
		} else if (value instanceof Map) {
			Map map = (Map) value;
			Map<String, RType> m = new HashMap<String, RType>();
			Entry entry;
			i = map.entrySet().iterator();
			while (i.hasNext()) {
				entry = (Entry) i.next();
				m.put((String) entry.getKey(), convertValue(entry.getValue())); 
			}
			return omero.rtypes.rmap(m);
		}
		return null;
	}
	
	
	/**
	 * Creates a split view figure. 
	 * Returns the id of the annotation hosting the figure.
	 * 
	 * @param objectIDs	The id of the objects composing the figure.
	 * @param type		The type of objects.
	 * @param param 	The parameters to use.	
	 * @param userID		The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws ProcessException If an error occurred while running the script.
	 */
	ScriptCallback createFigure(List<Long> objectIDs, Class type,
			FigureParam param, long userID)
		throws ProcessException, DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		long id = getScriptID(param.getScriptName(), 
				"Cannot start "+param.getScriptName());
		if (id <= 0) return null;
		int scriptIndex = param.getIndex();
		List<RType> ids = new ArrayList<RType>(objectIDs.size());
		Iterator<Long> i = objectIDs.iterator();
		while (i.hasNext())
			ids.add(omero.rtypes.rlong(i.next()));

		Map<String, RType>  map = new HashMap<String, RType>();
		RString dataType;
		dataType = omero.rtypes.rstring("Image");
		map.put("Data_Type", dataType);
		if (scriptIndex == FigureParam.THUMBNAILS) {
			DataObject d = (DataObject) param.getAnchor();
			long parentID = -1;
			if (d instanceof DatasetData ||
					d instanceof ProjectData) parentID = d.getId();
			if (DatasetData.class.equals(type)) {
				dataType = omero.rtypes.rstring("Dataset");
			} 
			map.put("Data_Type", dataType);
			map.put("IDs", omero.rtypes.rlist(ids));
			List<Long> tags = param.getTags();
			if (tags != null && tags.size() > 0) {
				ids = new ArrayList<RType>(tags.size());
				i = tags.iterator();
				while (i.hasNext()) 
					ids.add(omero.rtypes.rlong(i.next()));
				map.put("Tag_IDs", omero.rtypes.rlist(ids));
			}

			if (parentID > 0)
				map.put("Parent_ID", omero.rtypes.rlong(parentID));
			map.put("Show_Untagged_Images", 
					omero.rtypes.rbool(param.isIncludeUntagged()));

			map.put("Thumbnail_Size", omero.rtypes.rint(param.getWidth()));
			map.put("Max_Columns", omero.rtypes.rint(param.getHeight()));
			map.put("Format", 
					omero.rtypes.rstring(param.getFormatAsString()));
			map.put("Figure_Name", 
					omero.rtypes.rstring(param.getName()));
			return runScript(id, map);	
		} 
		//merge channels
		Iterator j;
		Map<String, RType> merge = new LinkedHashMap<String, RType>();
		Entry entry;
		Map<Integer, Integer> mergeChannels = param.getMergeChannels();
		if (mergeChannels != null) {
			j = mergeChannels.entrySet().iterator();
			while (j.hasNext()) {
				entry = (Entry) j.next();
				merge.put(""+(Integer) entry.getKey(), 
						omero.rtypes.rlong((Integer) entry.getValue()));
			}
		}

		//split
		Map<String, RType> split = new LinkedHashMap<String, RType>();

		Map<Integer, String> splitChannels = param.getSplitChannels();
		if (splitChannels != null) {
			j = splitChannels.entrySet().iterator();
			while (j.hasNext()) {
				entry = (Entry) j.next();
				split.put(""+(Integer) entry.getKey(), 
						omero.rtypes.rstring((String) entry.getValue()));
			}
		}
		List<Integer> splitActive = param.getSplitActive();
		if (splitActive != null && splitActive.size() > 0) {
			List<RType> sa = new ArrayList<RType>(splitActive.size());
			Iterator<Integer> k = splitActive.iterator();
			while (k.hasNext()) {
				sa.add(omero.rtypes.rint(k.next()));
			}
			map.put("Split_Indexes", omero.rtypes.rlist(sa));
		}
		map.put("Merged_Names", omero.rtypes.rbool(
				param.getMergedLabel()));
		map.put("IDs", omero.rtypes.rlist(ids));
		if (param.getStartZ() >= 0)
			map.put("Z_Start", omero.rtypes.rint(param.getStartZ()));
		if (param.getEndZ() >= 0)
			map.put("Z_End", omero.rtypes.rint(param.getEndZ()));
		if (split.size() > 0) 
			map.put("Channel_Names", omero.rtypes.rmap(split));
		if (merge.size() > 0)
			map.put("Merged_Colours", omero.rtypes.rmap(merge));
		if (scriptIndex == FigureParam.MOVIE) {
			List<Integer> times = param.getTimepoints();
			List<RType> ts = new ArrayList<RType>(objectIDs.size());
			Iterator<Integer> k = times.iterator();
			while (k.hasNext()) 
				ts.add(omero.rtypes.rint(k.next()));
			map.put("T_Indexes", omero.rtypes.rlist(ts));
			map.put("Time_Units", 
					omero.rtypes.rstring(param.getTimeAsString()));
		} else 
			map.put("Split_Panels_Grey", 
					omero.rtypes.rbool(param.isSplitGrey()));
		if (param.getScaleBar() > 0)
			map.put("Scalebar", omero.rtypes.rint(param.getScaleBar()));
		map.put("Overlay_Colour", omero.rtypes.rstring(param.getColor()));
		map.put("Width", omero.rtypes.rint(param.getWidth()));
		map.put("Height", omero.rtypes.rint(param.getHeight()));
		map.put("Stepping", omero.rtypes.rint(param.getStepping()));
		map.put("Format", omero.rtypes.rstring(param.getFormatAsString()));
		map.put("Algorithm", 
				omero.rtypes.rstring(param.getProjectionTypeAsString()));
		map.put("Figure_Name", 
				omero.rtypes.rstring(param.getName()));
		map.put("Image_Labels", 
				omero.rtypes.rstring(param.getLabelAsString()));
		if (scriptIndex == FigureParam.SPLIT_VIEW_ROI) {
			map.put("ROI_Zoom", omero.rtypes.rfloat((float)
					param.getMagnificationFactor()));
		}
		return runScript(id, map);
	}
	
	/**
	 * Imports the specified file. Returns the image.
	 * 
	 * @param object Information about the file to import.
	 * @param container The folder to import the image.
	 * @param name		The name to give to the imported image.
	 * @param archived  Pass <code>true</code> if the image has to be archived,
	 * 					<code>false</code> otherwise.
     * @param Pass <code>true</code> to close the import,
     * 		<code>false</code> otherwise.
	 * @return See above.
	 * @throws ImportException If an error occurred while importing.
	 */
	Object importImage(ImportableObject object, IObject container, 
			File file, StatusLabel status, boolean archived, boolean close)
		throws ImportException
	{
		isSessionAlive();
		OMEROMetadataStoreClient omsc = null;
		try {
			omsc = getImportStore();
			ImportLibrary library = new ImportLibrary(omsc,
					new OMEROWrapper(new ImportConfig()));
			library.addObserver(status);
			ImportContainer ic = new ImportContainer(file, -1L, container, 
					archived, object.getPixelsSize(), null, null, null);
			ic.setUseMetadataFile(true);
			if (object.isOverrideName()) {
				int depth = object.getDepthForName();
				ic.setCustomImageName(UIUtilities.getDisplayedFileName(
						file.getAbsolutePath(), depth));
			}
			
			List<Pixels> pixels = library.importImage(ic, 0, 0, 1);
			Iterator<Pixels> j;
			Pixels p;
			Image image;
			if (pixels != null && pixels.size() > 0) {
				int n = pixels.size();
				long id;
				List<Long> ids;
				Parameters params = new Parameters();
				p = pixels.get(0);
				image = p.getImage();
				id = image.getId().getValue();
				//if (isLargeImage(p)) {
					//return new ThumbnailData(getImage(id, params), true);
				//}
				if (ImportableObject.isHCSFile(file)) {
					PlateData plate = getImportedPlate(id);
					if (plate != null) return plate;
					return getImage(id, params);
				}
				if (n == 1) {
					return getImage(id, params);
				} else if (n == 2) {
					ids = new ArrayList<Long>();
					ids.add(id);
					p = pixels.get(1);
					id = p.getImage().getId().getValue();
					ids.add(id);
					return getContainerImages(ImageData.class, ids, params);
				} else if (n >= 3) {
					j = pixels.iterator();
					int index = 0;
					ids = new ArrayList<Long>();
					while (j.hasNext()) {
						p = j.next();
						id = p.getImage().getId().getValue();
						ids.add(id);
						index++;
						if (index == 3) break;
					}
					return getContainerImages(ImageData.class, ids, params);
				}
			}
		} catch (Throwable e) {
			closeImport();
			throw new ImportException(getImportFailureMessage(e), e);
		} finally {
			if (omsc != null && close) {
				closeImport();
			}
		}
		return null;
	}
	
	/**
	 * Returns the import candidates.
	 * 
	 * @param object Host information about the file to import.
	 * @param file 		The file to import.
	 * @param archived 	Pass <code>true</code> to archived the files, 
	 * 					<code>false</code> otherwise.
	 * @param depth		The depth used to set the name. This will be taken into
	 * 					account if the file is a directory.
	 * @return See above.
	 * @throws ImportException If an error occurred while importing.
	 */
	ImportCandidates getImportCandidates(ImportableObject object, File file, 
			StatusLabel status)
		throws ImportException
	{
		isSessionAlive();
		try {
			ImportConfig config = new ImportConfig();
			OMEROWrapper reader = new OMEROWrapper(config);
			String[] paths = new String[1];
			paths[0] = file.getAbsolutePath();
			ImportCandidates candidates = new ImportCandidates(reader, 
					paths, status);
			return candidates;
			//return candidates.getPaths();
		} catch (Throwable e) {
			throw new ImportException(getImportFailureMessage(e), e);
		}
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

	/**
	 * Loads the folder identified by its absolute path.
	 * 
	 * @param absolutePath The absolute path.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	DataObject loadFolder(String absolutePath) 
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			
		} catch (Exception e) {
			handleException(e, "Cannot find the folder with path: "
					+absolutePath);
		}
		return null;
	}
	 
	/**
	 * Loads the instrument and its components.
	 * 
	 * @param id The id of the instrument.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Object loadInstrument(long id)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IMetadataPrx service = getMetadataService();
			if (service == null) service = getMetadataService();
			Instrument instrument = service.loadInstrument(id);
			if (instrument == null) return null;
			return new InstrumentData(instrument);
			
		} catch (Exception e) {
			handleException(e, "Cannot load the instrument: "+id);
		}
		return null;
	}
	
	
	/**
	 * Loads the table associated to a given node.
	 * 
	 * @param parameters The parameters used to retrieve the table.
	 * @param userID     The user's identifier.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<TableResult> loadTabularData(TableParameters parameters, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		TablePrx tablePrx = null;
		long id = -1;
		List<TableResult> results = new ArrayList<TableResult>();
		try {
			long[] rows;
			TableResult result;
			List<Long> ids;
			if (parameters.getNodeType() != null) {
				//TMP solution
				List<Long> objects = new ArrayList<Long>(1);
				objects.add(parameters.getNodeID());
				Map map = loadAnnotations(parameters.getNodeType(), objects, 
						null, null, new Parameters());
				Collection list = (Collection) map.get(parameters.getNodeID());
				Iterator j = list.iterator();
				FileAnnotationData fa;
				ids = new ArrayList<Long>();
				Object k;
				while (j.hasNext()) {
					k = j.next();
					if (k instanceof FileAnnotationData) {
						fa = (FileAnnotationData) k;
						if (FileAnnotationData.BULK_ANNOTATIONS_NS.equals(
								fa.getNameSpace()))
							ids.add(fa.getFileID());
					}
				}
			} else ids = parameters.getOriginalFileIDs();
			if (ids != null && ids.size() > 0) {
				Iterator<Long> i = ids.iterator();
				while (i.hasNext()) {
					id = i.next();
					tablePrx = getSharedResources().openTable(
							new OriginalFileI(id, false));
					if (tablePrx != null) {
						rows = new long[(int) tablePrx.getNumberOfRows()];
						for (int j = 0; j < rows.length; j++)
							rows[j] = j;
						result = createTableResult(tablePrx, rows);
						if (result != null)
							results.add(result);
					}
				}
			}
		} catch (Exception e) {
			handleException(e, "Cannot load the table: "+id);
		}
		return results;
	}
	
	/**
	 * Loads the ROI related to the specified image.
	 * 
	 * @param imageID 	The image's ID.
	 * @param userID	The user's ID.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<ROIResult> loadROI(long imageID, List<Long> measurements, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		List<ROIResult> results = new ArrayList<ROIResult>();
		try {
			IRoiPrx svc = getROIService();
			if (svc == null) svc = getROIService();
			RoiOptions options = new RoiOptions();
			options.userId = omero.rtypes.rlong(userID);
			RoiResult r;
			ROIResult result;
			if (measurements == null || measurements.size() == 0) {
				options = new RoiOptions();
				r = svc.findByImage(imageID, options);
				if (r == null) return results;
				results.add(new ROIResult(PojoMapper.asDataObjects(r.rois)));
			} else { //measurements
				Map<Long, RoiResult> map = svc.getMeasuredRoisMap(imageID, 
						measurements, options);
				if (map == null) return results;
				Iterator i = map.entrySet().iterator();
				Long id;
				Entry entry;
				while (i.hasNext()) {
					entry = (Entry) i.next();
					id = (Long) entry.getKey();
					r = (RoiResult) entry.getValue();
					//get the table
					result = new ROIResult(PojoMapper.asDataObjects(r.rois), 
							id);
					result.setResult(createTableResult(
							svc.getTable(id), "Image", imageID));
					results.add(result);
				}
			}
		} catch (Exception e) {
			handleException(e, "Cannot load the ROI for image: "+imageID);
		}
		return results;
	}
	
	/**
	 * Save the ROI for the image to the server.
	 * 
	 * @param imageID 	The image's ID.
	 * @param userID	The user's ID.
	 * @param roiList	The list of ROI to save.
	 * @return updated list of ROIData objects.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<ROIData> saveROI(long imageID,  long userID, List<ROIData> roiList)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try 
		{
			IUpdatePrx updateService = getUpdateService();
			if (updateService == null) updateService = getUpdateService();
			IRoiPrx svc = getROIService();
			RoiOptions options = new RoiOptions();
			options.userId = omero.rtypes.rlong(userID);
			RoiResult serverReturn;
			serverReturn = svc.findByImage(imageID, new RoiOptions());
			Map<Long, Roi> roiMap = new HashMap<Long, Roi>();
			List<Roi> serverRoiList = serverReturn.rois;

			/* Create a map of all the client roi with id as key */
			Map<Long, ROIData> clientROIMap = new HashMap<Long, ROIData>();
			for (ROIData roi : roiList) {
				if (roi != null)
					clientROIMap.put(roi.getId(), roi);
			}
				
			
			/* Create a map of the <id, serverROI>, but remove any roi from 
			 * the server that should be deleted, before creating map.
			 * To delete an roi we first must delete all the roiShapes in 
			 * the roi. */
			for (Roi roi : serverRoiList) {
				if (roi != null) {
					if (!clientROIMap.containsKey(roi.getId().getValue()))
					{
						/* rois are now deleted using the roi service.
						if (roi.getDetails().getOwner().getId().getValue()
								== userID) {
							for (int i = 0 ; i < roi.sizeOfShapes() ; i++)
								updateService.deleteObject(roi.getShape(i));
							updateService.deleteObject(roi);
						}
						*/
					} else roiMap.put(roi.getId().getValue(), roi);
				}
			}
			
			/* For each roi in the client, see what should be done:
			 * 1. Create a new roi if it does not exist. 
			 * 2. build a map of the roiShapes in the clientROI with ROICoordinate 
			 * as a key.
			 * 3. as above but for server roiShapes.
			 * 4. iterate through the maps to see if the shapes have been deleted
			 * in the roi on the client, if so then delete the shape on the server.
			 * 5. Somehow the server roi becomes stale on the client so we have 
			 * to retrieve the roi again from the server before updating it.
			 * 6. Check to see if the roi in the cleint has been updated
			 */
			List<ShapeData> shapeList;
			ShapeData shape;
			Map<ROICoordinate, ShapeData> clientCoordMap;
			Roi serverRoi;
			Iterator<List<ShapeData>> shapeIterator;
			Iterator<ROICoordinate> serverIterator;
			Map<ROICoordinate, Shape>serverCoordMap;
			Shape s;
			ROICoordinate coord;
			long id;
			RoiResult tempResults;
			int shapeIndex;
	
			List<Long> deleted = new ArrayList<Long>();
			Image unloaded = new ImageI(imageID, false);
			for (ROIData roi : roiList)
			{
				/*
				 * Step 1. Add new ROI to the server.
				 */
				if (!roiMap.containsKey(roi.getId()))
				{
					Roi r = (Roi) roi.asIObject();
					r.setImage(unloaded);
					updateService.saveAndReturnObject(r);
					continue;
				}	
				
				/*
				 * Step 2. create the client roiShape map. 
				 */
				serverRoi = roiMap.get(roi.getId());
				shapeIterator  = roi.getIterator();

				clientCoordMap = new HashMap<ROICoordinate, ShapeData>();
				while (shapeIterator.hasNext())
				{
					shapeList = shapeIterator.next();
					shape = shapeList.get(0);
					if (shape != null)
						clientCoordMap.put(shape.getROICoordinate(), shape);
				}
				
				/*
				 * Step 3. create the server roiShape map.
				 */
				serverCoordMap  = new HashMap<ROICoordinate, Shape>();
				if (serverRoi != null) {
					for (int i = 0 ; i < serverRoi.sizeOfShapes(); i++)
					{
						s = serverRoi.getShape(i);
						if (s != null) {
							serverCoordMap.put(new ROICoordinate(
								s.getTheZ().getValue(), s.getTheT().getValue()),
								s);
						}
					}
				}
				/*
				 * Step 4. delete any shapes in the server that have been deleted
				 * in the client.
				 */
				Iterator si = serverCoordMap.entrySet().iterator();
				Entry entry;
				List<ROICoordinate> removed = new ArrayList<ROICoordinate>();
				List<IObject> toDelete = new ArrayList<IObject>();
				while (si.hasNext())
				{
					entry = (Entry) si.next();
					coord = (ROICoordinate) entry.getKey();
					if (!clientCoordMap.containsKey(coord))
					{
						s = (Shape) entry.getValue();
						if (s != null) {
							updateService.deleteObject(s);
						}
					} else {
						s = (Shape) entry.getValue();
						if (s instanceof Line || s instanceof Polyline) {
							shape = clientCoordMap.get(coord);
							if ((s instanceof Line && 
									shape.asIObject() instanceof Polyline) ||
								(s instanceof Polyline && 
									shape.asIObject() instanceof Line)) {
								removed.add(coord);
								updateService.deleteObject(s);
								deleted.add(shape.getId());
								System.err.println("deleted: "+shape.getId());
							}
						}
					}
				}
				/*
				 * Step 5. retrieve new roi as some are stale.
				 */
				if (serverRoi != null) {
					id = serverRoi.getId().getValue();
					tempResults = svc.findByImage(imageID, new RoiOptions());
					for (Roi r : tempResults.rois)
					{
						if (r.getId().getValue() == id)
							serverRoi = r;
					}
				}
				
				/*
				 * Step 6. Check to see if the roi in the client has been updated
				 * if so replace the server roiShape with the client one.
				 */
				si = clientCoordMap.entrySet().iterator();
				Shape serverShape;
				long sid;
				while (si.hasNext())
				{
					entry = (Entry) si.next();
					coord = (ROICoordinate) entry.getKey();
					shape = (ShapeData) entry.getValue();
					
					if (shape != null) {
						if (!serverCoordMap.containsKey(coord))
							serverRoi.addShape((Shape) shape.asIObject());
						else if (shape.isDirty())
						{
							shapeIndex = -1;
							if (deleted.contains(shape.getId())) {
								serverRoi.addShape((Shape) shape.asIObject());
								break;
							}
							for (int j = 0 ; j < serverRoi.sizeOfShapes() ; j++)
							{
								if (serverRoi != null) {
									serverShape = serverRoi.getShape(j);
									if (serverShape != null) 
									{
										if (serverShape.getId() != null)
										{
											sid = serverShape.getId().getValue();
											if (sid == shape.getId())
											{
												shapeIndex = j;
												break;
											}
										}
									}
								}
							}
							
							if (shapeIndex == -1)
							{
								serverShape = null;
								shapeIndex = -1;
								for (int j = 0 ; j < serverRoi.sizeOfShapes() ; j++)
								{
									if (serverRoi != null) 
									{
										serverShape = serverRoi.getShape(j);
										if (serverShape != null) 
										{
											if (serverShape.getTheT().getValue()
												== shape.getT() && 
												serverShape.getTheZ().getValue()
												== shape.getZ())
											{
												shapeIndex = j;
												break;
											}
										}
									}
								}
								if (shapeIndex !=-1)
								{
									if (!removed.contains(coord))
										updateService.deleteObject(serverShape);
									serverRoi.addShape((Shape) shape.asIObject());
								}
								else
								{
									throw new Exception("serverRoi.shapeList " +
										"is corrupted");
								}
							}
							else
								serverRoi.setShape(shapeIndex,
									(Shape) shape.asIObject());
						}
					}
				}
				
				/* 
				 * Step 7. update properties of ROI, if they are changed.
				 * 
				 */
				if (serverRoi != null) {
					Roi ri = (Roi) roi.asIObject();
					serverRoi.setDescription(ri.getDescription());
					serverRoi.setNamespaces(ri.getNamespaces());
					serverRoi.setKeywords(ri.getKeywords());
					serverRoi.setImage(unloaded);
					updateService.saveAndReturnObject(serverRoi);
				}
				
			}
			return roiList;
		} catch (Exception e) {
			handleException(e, "Cannot Save the ROI for image: "+imageID);
		}
		return new ArrayList<ROIData>();
	}
	
	
	/**
	 * Loads the <code>FileAnnotationData</code>s for the passed image.
	 * 
	 * @param imageID 	The image's id.
	 * @param userID	The id of the user.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Collection loadROIMeasurements(long imageID, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IRoiPrx svc = getROIService();
			if (svc == null) svc = getROIService();
			RoiOptions options = new RoiOptions();
			options.userId = omero.rtypes.rlong(userID);
			Collection files = PojoMapper.asDataObjects(
					svc.getRoiMeasurements(imageID, options));
			List results = new ArrayList();
			if (files != null) {
				Iterator i = files.iterator();
				FileAnnotationData fa;
				long tableID;
				TableResult table;
				while (i.hasNext()) {
					fa = (FileAnnotationData) i.next();
					if (OVERLAYS.equals(fa.getDescription())) {
						//load the table
						tableID = fa.getId();
						table = createOverlay(imageID, svc.getTable(tableID));
						if (table != null) {
							table.setTableID(tableID);
							results.add(table);
						}
					} else
						results.add(fa);
				}
			}
			return results;
			
		} catch (Exception e) {
			handleException(e, "Cannot load the ROI measurements for image: "+
					imageID);
		}
		return new ArrayList<Object>();
	}
	
	/**
	 * Returns the file 
	 * 
	 * @param file		The file to write the bytes.
	 * @param imageID	The id of the image.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	File exportImageAsOMETiff(File f, long imageID)
		throws DSAccessException, DSOutOfServiceException
	{
		isSessionAlive();
		FileOutputStream stream = null;
		DSAccessException exception = null;
		try {
			ExporterPrx store = null;
			stream = new FileOutputStream(f);
			
			try {
				//synchronized(new Object()) {
					store = getExporterService();
					if (store == null) store = getExporterService();
					store.addImage(imageID);
					
					try {
						long size = store.generateTiff();
						long offset = 0;
						try {
							for (offset = 0; (offset+INC) < size;) {
								stream.write(store.read(offset, INC));
								offset += INC;
							}	
						} finally {
							stream.write(store.read(offset, (int)(size-offset))); 
							stream.close();
						}
					} catch (Exception e) {
						if (stream != null) stream.close();
						if (f != null) f.delete();
						exception = new DSAccessException(
								"Cannot export the image as an OME-TIFF ", e);
					}
				//}
			} finally {
				try {
					if (store != null) store.close();
				} catch (Exception e) {}
				if (exception != null) throw exception;
				return f;
			}
		} catch (Throwable t) {
			if (f != null) f.delete();
			throw new DSAccessException(
					"Cannot export the image as an OME-TIFF", t);
		}
	}
	
	/**
	 * Performs a basic fit. Returns the file hosting the results.
	 * 
	 * @param ids   	 The objects to analyze.
	 * @param objectType The type of objects to analyze.
	 * @param param		 The parameters.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	long analyseFRAP(List<Long> ids, Class objectType, Object param)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			/*
			IScriptPrx svc = getScripService();
			
			Map<Long, String> scripts = svc.getScripts();
			if (scripts == null) return -1;
			long id = -1;
			Entry en;
			Iterator j = scripts.entrySet().iterator();
			long value;
			String scriptName = "frapFigure.py";
			while (j.hasNext()) {
				en = (Entry) j.next();
				if (en.getValue().equals(scriptName)) {
					value = (Long) en.getKey();
					if (value > id) id = value;
				}
			}
			if (id <= 0) return -1;
			Map<String, RType> map = new HashMap<String, RType>();
			map.put("imageId", omero.rtypes.rlong(ids.get(0)));
			
			runScript(id, map);
			 */
		} catch (Exception e) {
			handleException(e, "Cannot analyze the data.");
		}
		return -1;
	}

	/**
	 * Runs the script.
	 * 
	 * @param script The script to run.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 * @throws ProcessException If an error occurred while running the script.
	 */
	ScriptCallback runScript(ScriptObject script)
		throws ProcessException, DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		long id = -1;
		try {
			id = script.getScriptID();
			if (id < 0) return null;
		} catch (Exception e) {
			handleException(e, "Cannot run the script.");
		}
		return runScript(id, script.getValueToPass());
	}
	
	/**
	 * Runs the script.
	 * 
	 * @param script The script to run.
	 * @param official Pass <code>true</code> to indicate that the script will
	 * 				   be uploaded as an official script, <code>false</code>
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Object uploadScript(ScriptObject script, boolean official)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		FileInputStream stream = null;
		try {
			IScriptPrx svc = getScriptService(); 
			if (svc == null) svc = getScriptService(); 
			StringBuffer buf = new StringBuffer("");
			try {
				File file = new File(script.getPath());
				stream = new FileInputStream(file);
				int c;
				while ((c = stream.read()) != -1)
					buf.append((char) c);
				try {
					if (stream != null) stream.close();
				} catch (Exception e) {}
			} catch (Exception e) {
				try {
					stream.close();
				} catch (Exception ex) {
					//n
				}
				handleException(e, 
						"Cannot upload the script: "+script.getName()+".");
				return -1;
			}
			String path = script.getFolder();
			List<OriginalFile> scripts = getScripts();
			if (scripts.size() > 0) {
				Iterator<OriginalFile> i = scripts.iterator();
				OriginalFile of;
				StringBuffer buffer = new StringBuffer();
				RString v;
				while (i.hasNext()) {
					of = i.next();
					v = of.getPath();
					if (v != null) buffer.append(v.getValue());
					v = of.getName();
					if (v != null) buffer.append(v.getValue());
					//check if the script already exists.
					if (buffer.toString().equals(path)) {
						svc.editScript(of, buf.toString());
						return of.getId().getValue();
					}
				}
			}
			if (official)
				return svc.uploadOfficialScript(path, buf.toString());
			return svc.uploadScript(path, buf.toString());
		} catch (Exception e) {
			handleException(e, 
					"Cannot upload the script: "+script.getName()+".");
		}
		try {
			if (stream != null) stream.close();
		} catch (Exception e) {
		}
		return -1;
	}
	
	//Admin 
	
	/**
	 * Creates the experimenters.
	 * 
	 * @param object The object hosting information about the experimenters 
	 * to create. 
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<ExperimenterData> createExperimenters(AdminObject object)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		List<ExperimenterData> results = new ArrayList<ExperimenterData>();
		try {
			IAdminPrx svc = getAdminService();
			if (svc == null) svc = getAdminService();
			Map<ExperimenterData, UserCredentials> 
				m = object.getExperimenters();
			Entry entry;
			Iterator i = m.entrySet().iterator();
			Experimenter exp;
			UserCredentials uc;
			String password;
			List<GroupData> groups = object.getGroups();
			ExperimenterGroup g = null;
			List<ExperimenterGroup> l = new ArrayList<ExperimenterGroup>();
			if (groups != null && groups.size() >= 1) {
				g = groups.get(0).asGroup();
				Iterator<GroupData> j = groups.iterator();
				while (j.hasNext()) 
					l.add(((GroupData) j.next()).asGroup());
			}
			long id;
			Experimenter value;
			boolean systemGroup = false;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				exp = (Experimenter) ModelMapper.createIObject(
						(DataObject) entry.getKey());
				uc = (UserCredentials) entry.getValue();
				value = lookupExperimenter(uc.getUserName());
				if (value == null) {
					if (uc.isAdministrator()) {
						l.add(getSystemGroup(GroupData.USER));
						l.add(getSystemGroup(GroupData.SYSTEM));
					} else l.add(getSystemGroup(GroupData.USER));
					if (g == null) {
						g = l.get(0);
						systemGroup = true;
					}
					exp.setOmeName(omero.rtypes.rstring(uc.getUserName()));
					password = uc.getPassword();
					if (password != null && password.length() > 0) {
						id = svc.createExperimenterWithPassword(exp, 
								omero.rtypes.rstring(password), g, l);			
					} else
						id = svc.createExperimenter(exp, g, l);
					exp = svc.getExperimenter(id);
					if (uc.isOwner() && !systemGroup)
						svc.setGroupOwner(g, exp);
					results.add((ExperimenterData) 
							PojoMapper.asDataObject(exp));
				}
			}
		} catch (Exception e) {
			handleException(e, "Cannot create the experimenters.");
		}
		return results;
	}

	/**
	 * Creates the experimenters.
	 * 
	 * @param object The object hosting information about the experimenters 
	 * to create. 
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	GroupData createGroup(AdminObject object)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			IAdminPrx svc = getAdminService();
			if (svc == null) svc = getAdminService();
			Map<ExperimenterData, UserCredentials> 
				m = object.getExperimenters();
			Entry entry;
			Iterator i = m.entrySet().iterator();
			Experimenter exp;
			UserCredentials uc;
			String password;
			GroupData groupData = (GroupData) object.getGroup();
			ExperimenterGroup g = lookupGroup(groupData.getName());
			
			if (g != null) return null; 
				
			g = (ExperimenterGroup) ModelMapper.createIObject(groupData);
			long groupID = svc.createGroup(g);
			g = svc.getGroup(groupID);
			int level = object.getPermissions();
			if (level != AdminObject.PERMISSIONS_PRIVATE) {
				Permissions p = g.getDetails().getPermissions();
				setPermissionsLevel(p, level);
				getAdminService().changePermissions(g, p);
			}
			
			List<ExperimenterGroup> list = new ArrayList<ExperimenterGroup>();
			list.add(g);

			List<ExperimenterGroup> l = new ArrayList<ExperimenterGroup>();
			long id;
			Experimenter value;
			GroupData defaultGroup = null;
			ExperimenterData expData;
			while (i.hasNext()) {
				entry = (Entry) i.next();
				uc = (UserCredentials) entry.getValue();
				//Check if the experimenter already exist
				value = lookupExperimenter(uc.getUserName());
				if (value != null) {
					exp = value;
					expData = new ExperimenterData(exp);
					defaultGroup = expData.getDefaultGroup();
					if (isSystemGroup(defaultGroup.asGroup()))
						defaultGroup = null;
				} else {
					exp = (Experimenter) ModelMapper.createIObject(
							(ExperimenterData) entry.getKey());
					if (uc.isAdministrator()) {
						l.add(getSystemGroup(GroupData.SYSTEM));
						l.add(getSystemGroup(GroupData.USER));
					} else l.add(getSystemGroup(GroupData.USER));
					exp.setOmeName(omero.rtypes.rstring(uc.getUserName()));
					password = uc.getPassword();
					if (password != null && password.length() > 0) {
						id = svc.createExperimenterWithPassword(exp, 
								omero.rtypes.rstring(password), g, l);			
					} else
						id = svc.createExperimenter(exp, g, l);
					exp = svc.getExperimenter(id);
				}

				svc.setGroupOwner(g, exp);
				if (defaultGroup == null) {
					svc.setDefaultGroup(exp, g);
				}
			}
			return (GroupData) PojoMapper.asDataObject(g);
		} catch (Exception e) {
			handleException(e, "Cannot create group and owner.");
		}
		return null;
	}
	
	/**
	 * Counts the number of experimenters within the specified groups.
	 * Returns a map whose keys are the group identifiers and the values the 
	 * number of experimenters in the group.
	 * 
	 * @param ids The group identifiers.
	 * @return See above
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.
	 */
	Map<Long, Long> countExperimenters(List<Long> groupIds)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		Map<Long, Long> r = new HashMap<Long, Long>();
		try {
			IQueryPrx svc = getQueryService();
			if (svc == null) svc = getQueryService();
			ParametersI p = new ParametersI();
			p.addLongs("gids", groupIds);
			List list = (List) svc.findAllByQuery("select m " +
					"from GroupExperimenterMap as m"
	                + " left outer join fetch m.parent"
	                		+" where m.parent.id in (:gids)", p);
			Iterator i = list.iterator();
			GroupExperimenterMap g;
			long id;
			Long count;
			ExperimenterGroup group;
			while (i.hasNext()) {
				g = (GroupExperimenterMap) i.next();
				group = g.getParent();
				if (!isSystemGroup(group)) {
					id = group.getId().getValue();
					groupIds.remove(id);
					count = r.get(id);
					if (count == null) count = 0L;
					count++;
					r.put(id, count);
				} else {
					if (GroupData.SYSTEM.equals(group.getName().getValue())) {
						id = group.getId().getValue();
						groupIds.remove(id);
						count = r.get(id);
						if (count == null) count = 0L;
						count++;
						r.put(id, count);
					}
				}
			}
			if (groupIds.size() > 0) {
				i = groupIds.iterator();
				while (i.hasNext()) {
					r.put((Long) i.next(), 0L);
				}
			}
		} catch (Throwable t) {
			handleException(t, "Cannot count the experimenters.");
		}
		
		return r;
	}
	
	/**
	 * Returns the collection of groups the user is a member of.
	 * 
	 * @param experimenterID The experimenter's identifier.
	 * @return See above.
	 *  @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<GroupData> getGroups(long experimenterID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		List<GroupData> pojos = new ArrayList<GroupData>();
		if (experimenterID < 0) return pojos;
		try {
			IQueryPrx svc = getQueryService();
			if (svc == null) svc = getQueryService();
			//IAdminPrx svc = getAdminService();
			List<ExperimenterGroup> groups = null;
			ParametersI p = new ParametersI();
			p.addId(experimenterID);
			groups = (List) svc.findAllByQuery("select distinct g " +
					"from ExperimenterGroup g "
	                + "left outer join fetch g.groupExperimenterMap m "
	                + "left outer join fetch m.child u " +
	                		" where u.id = :id", p);
			ExperimenterGroup group;
			//GroupData pojoGroup;
			Iterator<ExperimenterGroup> i = groups.iterator();
			while (i.hasNext()) {
				group = i.next();
				if (!isSystemGroup(group)) 
					pojos.add((GroupData) PojoMapper.asDataObject(group));	
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return pojos;
	}
	
	/**
	 * Loads the groups the experimenters.
	 * 
	 * @param id The group identifier or <code>-1</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<GroupData> loadGroups(long id)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		List<GroupData> pojos = new ArrayList<GroupData>();
		try {
			IQueryPrx svc = getQueryService();
			if (svc == null) svc = getQueryService();
			List<ExperimenterGroup> groups = null;
			if (id < 0) {
				groups = (List)
				svc.findAllByQuery("select distinct g from ExperimenterGroup g "
		               // + "left outer join fetch g.groupExperimenterMap m "
		                , null);
			} else {
				ParametersI p = new ParametersI();
				p.addId(id);
				groups = (List) svc.findAllByQuery("select distinct g " +
						"from ExperimenterGroup g "
		                + "left outer join fetch g.groupExperimenterMap m "
		                + "left outer join fetch m.child u "
		                + "left outer join fetch u.groupExperimenterMap m2 "
		                + "left outer join fetch m2.parent" +
		                		" where g.id = :id", p);
			}
			ExperimenterGroup group;
			GroupData pojoGroup;
			Iterator<ExperimenterGroup> i = groups.iterator();
			while (i.hasNext()) {
				group = i.next();
				pojoGroup = (GroupData) PojoMapper.asDataObject(group);
				if (!isSystemGroup(group)) 
					pojos.add(pojoGroup);	
				else {
					if (GroupData.SYSTEM.equals(pojoGroup.getName()))
						pojos.add(pojoGroup);	
				}
			}
			return pojos;
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the available groups ");
		}
		return pojos;
	}
	
	/**
	 * Loads the experimenters contained in the specified group or all
	 * experimenters if the value passed is <code>-1</code>.
	 * 
	 * @param id The group identifier or <code>-1</code>.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<ExperimenterData> loadExperimenters(long groupID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		List<ExperimenterData> pojos = new ArrayList<ExperimenterData>();
		try {
			IAdminPrx service = getAdminService();
			if (service == null) service = getAdminService();
			List<Experimenter> l = service.lookupExperimenters();
			pojos.addAll(PojoMapper.asDataObjects(l));
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the existing groups.");
		}
		return pojos;
	}
	
	
	
	/**
	 * Deletes the specified experimenters. Returns the experimenters 
	 * that could not be deleted.
	 * 
	 * @param experimenters The experimenters to delete.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<ExperimenterData> deleteExperimenters(
			List<ExperimenterData> experimenters)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		List<ExperimenterData> r = new ArrayList<ExperimenterData>();
		IAdminPrx svc = getAdminService();
		if (svc == null) svc = getAdminService();
		Iterator<ExperimenterData> i = experimenters.iterator();
		ExperimenterData exp;
		while (i.hasNext()) {
			exp = i.next();
			try {
				svc.deleteExperimenter(exp.asExperimenter());
			} catch (Exception e) {
				r.add(exp);
			}
		}
		return r;
	}
	
	/**
	 * Copies the experimenter to the specified group.
	 * Returns the experimenters that could not be copied.
	 * 
	 * @param group The group to add the experimenters to.
	 * @param experimenters The experimenters to add.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<ExperimenterData> copyExperimenters(GroupData group, 
			Collection experimenters)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		List<ExperimenterData> r = new ArrayList<ExperimenterData>();
		IAdminPrx svc = getAdminService();
		if (svc == null) svc = getAdminService();
		Iterator<ExperimenterData> i = experimenters.iterator();
		ExperimenterData exp;
		List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
		groups.add(group.asGroup());
		while (i.hasNext()) {
			exp = i.next();
			try {
				svc.addGroups(exp.asExperimenter(), groups);
			} catch (Exception e) {
				r.add(exp);
			}
		}
		return r;
	}
	
	/**
	 * Removes the experimenters from the specified group.
	 * Returns the experimenters that could not be removed.
	 * 
	 * @param group The group to add the experimenters to.
	 * @param experimenters The experimenters to add.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<ExperimenterData> removeExperimenters(GroupData group, 
			Collection experimenters)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		List<ExperimenterData> r = new ArrayList<ExperimenterData>();
		IAdminPrx svc = getAdminService();
		if (svc == null) svc = getAdminService();
		Iterator<ExperimenterData> i = experimenters.iterator();
		ExperimenterData exp;
		List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
		groups.add(group.asGroup());
		while (i.hasNext()) {
			exp = i.next();
			try {
				svc.removeGroups(exp.asExperimenter(), groups);
			} catch (Exception e) {
				r.add(exp);
			}
		}
		return r;
	}
	
	/**
	 * Deletes the specified groups. Returns the groups that could not be 
	 * deleted.
	 * 
	 * @param groups The groups to delete.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<GroupData> deleteGroups(List<GroupData> groups)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		List<GroupData> r = new ArrayList<GroupData>();
		IAdminPrx svc = getAdminService();
		if (svc == null) svc = getAdminService();
		Iterator<GroupData> i = groups.iterator();
		GroupData g;
		while (i.hasNext()) {
			g = i.next();
			try {
				svc.deleteGroup(g.asGroup());
			} catch (Exception e) {
				r.add(g);
			}
		}
		return r;
	}
	
	/**
	 * Resets the password of the specified user.
	 * 
	 * @param userName 	The login name.
	 * @param userID 	The id of the user.
	 * @param password 	The password to set.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	void resetPassword(String userName, long userID, String password)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		IAdminPrx svc = getAdminService();
		if (svc == null) svc = getAdminService();
		try {
			svc.changeUserPassword(userName, omero.rtypes.rstring(password));
		} catch (Throwable t) {
			handleException(t, "Cannot modify the password for:"+userName);
		}
	}
	
	/**
	 * Resets the login name of the specified user.
	 * Returns <code>true</code> if the user name could be reset,
	 * <code>false</code> otherwise.
	 * 
	 * @param userName 		The login name.
	 * @param experimenter 	The experimenter to handle.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	boolean resetUserName(String userName, ExperimenterData experimenter)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			//First check that no user with the name already exists

			Experimenter value = lookupExperimenter(userName);
			if (value == null) {
				Experimenter exp = experimenter.asExperimenter();
				exp.setOmeName(omero.rtypes.rstring(userName));
				IAdminPrx service = getAdminService();
				if (service == null) service = getAdminService();
				service.updateExperimenter(exp);
				return true;
			}
		} catch (Throwable t) {
			handleException(t, "Cannot modify the loginName for:"+
					experimenter.getId());
		}
		return false;
	}
	
	/**
	 * Invokes when the user has forgotten his/her password.
	 * 
	 * @param userName The login name.
	 * @param email The e-mail if set.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	void reportForgottenPassword(String userName, String email)
		throws DSOutOfServiceException, DSAccessException
	{
		//root need to login and send an e-mail.
		
	}
	
	/**
	 * Sets the permissions level.
	 * 
	 * @param p		The permissions of the object.
	 * @param level The permissions to set.
	 */
	void setPermissionsLevel(Permissions p, int level)
	{
		switch (level) {
			case AdminObject.PERMISSIONS_GROUP_READ:
				p.setGroupRead(true);
				break;
			case AdminObject.PERMISSIONS_GROUP_READ_LINK:
				p.setGroupRead(true);
				p.setGroupWrite(true);
				break;
			case AdminObject.PERMISSIONS_PUBLIC_READ:
				p.setWorldRead(true);
				break;
			case AdminObject.PERMISSIONS_PUBLIC_READ_WRITE:
				p.setWorldRead(true);
				p.setWorldWrite(true);
		}
	}
	
	/**
	 * Returns the group corresponding to the passed name or <code>null</code>.
	 * 
	 * @param name The name of the group.
	 * @return See above
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	ExperimenterGroup lookupGroup(String name)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		IAdminPrx svc = getAdminService();
		try {
			if (svc == null) svc = getAdminService();
			return svc.lookupGroup(name);
		} catch (Exception e) {
			if (e instanceof ApiUsageException) 
				return null;
			handleException(e, "Cannot loade the required group.");
		}
		return null;
	}
	
	/**
	 * Returns the experimenter corresponding to the passed name or 
	 * <code>null</code>.
	 * 
	 * @param name The name of the experimenter.
	 * @return See above
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Experimenter lookupExperimenter(String name)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		IAdminPrx svc = getAdminService();
		try {
			if (svc == null) svc = getAdminService();
			return svc.lookupExperimenter(name);
		} catch (Exception e) {
			if (e instanceof ApiUsageException) 
				return null;
			handleException(e, "Cannot load the required group.");
		}
		return null;
	}
	
	/**
	 * Get the list of available workflows on the server. 
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List<WorkflowData> retrieveWorkflows(long userID)
			throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		IQueryPrx svc = getQueryService();
		if (svc == null) svc = getQueryService();
		try
		{
			ParametersI param = new ParametersI();
			param.map.put("userID", omero.rtypes.rlong(userID));
			List<Namespace> serverWorkflows = 
				(List) svc.findAllByQuery("from Namespace as n", param);
			return PojoMapper.asDataObjectsAsList(serverWorkflows);
		} catch(Throwable t)
		{
			return new ArrayList<WorkflowData>();
		}
	}
	
	/**
	 * Get the list of available workflows on the server. 
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Object storeWorkflows(List<WorkflowData> workflows, long userID)
			throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		IUpdatePrx updateService = getUpdateService();
		if (updateService == null) updateService = getUpdateService();
		for (WorkflowData workflow : workflows)
			if (workflow.isDirty())
			{
				try
				{
					updateService.saveObject(workflow.asIObject());
				} catch (ServerError e)
				{
					handleException(e, "Unable to save Object : "+ workflow);
				}
			}
		return Boolean.valueOf(true);
	}
	
	/**
	 * Reads the file hosting the user photo.
	 * 
	 * @param fileID The id of the file.
	 * @param size   The size of the file.
	 * @return See above
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	synchronized byte[] getUserPhoto(long fileID, long size)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		
		RawFileStorePrx store = getRawFileService();
		if (store == null) store = getRawFileService();
		try {
			store.setFileId(fileID);
		} catch (Throwable e) {
			closeService(store);
			handleException(e, "Cannot set the file's id.");
		}
		try {
			return store.read(0, (int) size);
		} catch (Exception e) {
			closeService(store);
			throw new DSAccessException("Cannot read the file" +fileID, e);
		}
	}
	
	/**
	 * Uploads the photo hosting the user photo.
	 * 
	 * @param fileID The id of the file.
	 * @param size   The size of the file.
	 * @return See above
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	long uploadExperimenterPhoto(File file, String format, long experimenterID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		IAdminPrx svc = getAdminService();
		if (svc == null) svc = getAdminService();
		try {
			FileInputStream stream = new FileInputStream(file); 
			long length = file.length(); 
			//Make sure the file is not too big.
			byte[] bytes = new byte[(int) length]; 
			int offset = 0; int r = 0; 
			while (offset < bytes.length && 
					(r = stream.read(bytes, offset, bytes.length-offset)) >= 0)
				offset += r; 
			if (offset < bytes.length)
				throw new IOException("Could not completely read file "+
						file.getName()); 
			stream.close();
			return svc.uploadMyUserPhoto(file.getName(), format, bytes);
		} catch (Exception e) {
			handleException(e, "Cannot upload the photo.");
		}
		return -1;
	}
	
	/**
	 * Returns the specified script.
	 * 
	 * @param commands The object to delete.
	 * @return See above.
	 * @throws ProcessException If an error occurred while running the script.
	 */
	DeleteCallback deleteObject(DeleteCommand[] commands)
		throws ProcessException
	{
		isSessionAlive();
		DeleteCallback cb = null;
		shutDownServices(false);
		try {
	         IDeletePrx svc = getDeleteService();
	         if (svc == null) svc = getDeleteService();
	         //scriptID, parameters, timeout (5s if null)
	         DeleteHandlePrx prx = svc.queueDelete(commands);
	         cb = new DeleteCallback(secureClient, prx);
		} catch (Exception e) {
			throw new ProcessException("Cannot delete the speficied objects.", 
					e);
		}
		return cb;
	}

	/**
	 * Returns the back-off time if it requires a pyramid to be built, 
	 * <code>null</code> otherwise.
	 * 
	 * @param pixelsId The identifier of the pixels set to handle.
	 * @return See above
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occurred while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Boolean isLargeImage(long pixelsId)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {	
			RawPixelsStorePrx store = getPixelsStore();
			if (store == null) store = getPixelsStore();
			store.setPixelsId(pixelsId, true);
			boolean b = store.requiresPixelsPyramid();
			store.close();
			return b;
		} catch (Exception e) {
			handleException(e, "Cannot start the Raw pixels store.");
		}
		return null;
	}
	
	/** Closes the services initialized by the importer.*/
	void closeImport()
	{
		if (importStore != null) {
			importStore.closeServices();
			importStore = null;
		}
	}
	
	/**
	 * Adds the experimenters to the specified group.
	 * 
	 * @param group The group to add the experimenters to.
	 * @param experimenters The experimenters to add.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occurred while trying to 
	 * retrieve data from OMERO service.
	 */
	void addExperimenters(GroupData group, List<ExperimenterData>
	experimenters)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		IAdminPrx svc = getAdminService();
		if (svc == null) svc = getAdminService();
		Iterator<ExperimenterData> i = experimenters.iterator();
		try {
			ExperimenterData exp;
			List<ExperimenterGroup> groups = new ArrayList<ExperimenterGroup>();
			groups.add(group.asGroup());
			while (i.hasNext()) {
				exp = i.next();
				svc.addGroups(exp.asExperimenter(), groups);
			}
		} catch (Exception e) {
			handleException(e, "Cannot add the experimenters.");
		}
	}

}
