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
import java.util.ResourceBundle;
import java.util.Set;
import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.util.PojoMapper;
import org.openmicroscopy.shoola.env.data.util.SearchDataContext;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import ome.api.IAdmin;
import ome.api.IDelete;
import ome.api.IPojos;
import ome.api.IQuery;
import ome.api.IRenderingSettings;
import ome.api.IRepositoryInfo;
import ome.api.ISession;
import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.api.RawPixelsStore;
import ome.api.Search;
import ome.api.ThumbnailStore;
import ome.conditions.ApiUsageException;
import ome.conditions.InternalException;
import ome.conditions.ValidationException;
import ome.model.ILink;
import ome.model.IObject;
import ome.model.annotations.Annotation;
import ome.model.annotations.BooleanAnnotation;
import ome.model.annotations.FileAnnotation;
import ome.model.annotations.TagAnnotation;
import ome.model.annotations.TextAnnotation;
import ome.model.annotations.UrlAnnotation;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.OriginalFile;
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import ome.model.display.RenderingDef;
import ome.model.enums.Format;
import ome.model.internal.Details;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.model.screen.Plate;
import ome.model.screen.Screen;
import ome.model.screen.Well;
import ome.model.screen.WellSample;
import ome.parameters.Parameters;
import ome.parameters.QueryParameter;
import ome.system.EventContext;
import ome.system.Login;
import ome.system.Server;
import ome.system.ServiceFactory;
import ome.system.UpgradeCheck;
import ome.util.builders.PojoOptions;
import omeis.providers.re.RenderingEngine;
import pojos.ArchivedAnnotationData;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FileAnnotationData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.PixelsData;
import pojos.PlateData;
import pojos.ProjectData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.TextualAnnotationData;
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
	private static final int				INC = 256000;
	
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
	private int						thumbRetrieval;
	
	/**
	 * The entry point provided by the connection library to access the various
	 * <i>OMERO</i> services.
	 */
	private ServiceFactory          entry;

	/** The thumbnail service. */
	private ThumbnailStore          thumbnailService;

	/** The raw file store. */
	private RawFileStore			fileStore;
	
	/** The search stateful service. */
	private Search					searchService;
	
	/** Tells whether we're currently connected and logged into <i>OMERO</i>. */
	private boolean                 connected;

	/** 
	 * Used whenever a broken link is detected to get the Login Service and
	 * try reestabishing a valid link to <i>OMERO</i>. 
	 */
	private DataServicesFactory     dsFactory;

	/** Server instance to log in. */
	private Server                  server;
	
	/** The login instance. */
	private Login 					login;

	/** The port to use in order to connect. */
	private int                     port;

	/** The compression level. */
	private float					compression;
	
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
		if (cause instanceof SecurityException) {
			String s = "Cannot access data for security reasons \n"; 
			throw new DSAccessException(s+message, t);
		} else if (cause instanceof EJBAccessException) {
			String s = "Cannot access data for security reasons \n"; 
			throw new DSAccessException(s+message, t);
		} else if (cause instanceof ApiUsageException) {
			String s = "Cannot access data, specified parameters not valid \n"; 
			throw new DSAccessException(s+message, t);
		} else if (cause instanceof ValidationException) {
			String s = "Cannot access data, specified parameters not valid \n"; 
			throw new DSAccessException(s+message, t);
		} else
			throw new DSOutOfServiceException(message, t);
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
		if (klass.equals(Category.class)) table = "CategoryImageLink";
		else if (klass.equals(Dataset.class)) table = "DatasetImageLink";
		else if (klass.equals(Project.class)) table = "ProjectDatasetLink";
		else if (klass.equals(CategoryGroup.class)) 
			table = "CategoryGroupCategoryLink";
		else if (klass.equals(Screen.class)) table = "ScreenPlateLink";
		return table;
	}

	/**
	 * Determines the table name corresponding to the specified class.
	 * 
	 * @param klass The class to analyze.
	 * @return See above.
	 */
	private String getTableForAnnotationLink(Class klass)
	{
		String table = null;
		if (klass.equals(Dataset.class)) table = "DatasetAnnotationLink";
		else if (klass.equals(Project.class)) table = "ProjectAnnotationLink";
		else if (klass.equals(Image.class)) table = "ImageAnnotationLink";
		else if (klass.equals(Pixels.class)) table = "PixelAnnotationLink";
		else if (klass.equals(Annotation.class))
			table = "AnnotationAnnotationLink";
		else if (klass.equals(DatasetData.class)) 
			table = "DatasetAnnotationLink";
		else if (klass.equals(ProjectData.class)) 
			table = "ProjectAnnotationLink";
		else if (klass.equals(ImageData.class)) table = "ImageAnnotationLink";
		else if (klass.equals(PixelsData.class)) table = "PixelAnnotationLink";
		else if (klass.equals(Screen.class)) table = "ScreenAnnotationLink";
		else if (klass.equals(Plate.class)) table = "PlateAnnotationLink";
		else if (klass.equals(ScreenData.class)) table = "ScreenAnnotationLink";
		else if (klass.equals(PlateData.class)) table = "PlateAnnotationLink";
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
		if (klass.equals(CategoryData.class)) return "Category";
		else if (klass.equals(DatasetData.class)) return "Dataset";
		else if (klass.equals(ProjectData.class)) return "Project";
		else if (klass.equals(CategoryGroupData.class)) return "CategoryGroup";
		else if (klass.equals(ImageData.class)) return "Image";
		else if (klass.equals(ScreenData.class)) return "Screen";
		else if (klass.equals(PlateData.class)) return "Plate";
		return null;
	}
	
	/**
	 * Maps the constant defined by {@link OmeroDataService}
	 * to the corresponding value defined by {@link IPojos}.
	 * 
	 * @param algorithm One of the constant defined by {@link OmeroDataService}.
	 * @return See above.
	 */
	private String mapAlgorithmToString(int algorithm)
	{
		switch (algorithm) {
			case OmeroDataService.CLASSIFICATION_ME:
				return IPojos.CLASSIFICATION_ME;
			case OmeroDataService.CLASSIFICATION_NME:
				return IPojos.CLASSIFICATION_NME;
			case OmeroDataService.DECLASSIFICATION:
				return IPojos.DECLASSIFICATION;
		}
		throw new IllegalArgumentException("Algorithm not valid.");
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
		} else if (nodeType.equals(CategoryData.class)) {
			if (property.equals(OmeroDataService.IMAGES_PROPERTY))
				return CategoryData.IMAGES;
		}
		else throw new IllegalArgumentException("NodeType or " +
				"property not supported");
		return null;
	}

	/**
	 * Formats the specified string.
	 * 
	 * @param term			The value to format.
	 * @param caseSensitive	Pass <code>true</code> if the case sensitivity has
	 * 						to be taken into account.
	 * @return See above.
	 */
	private String formatTerm(String term, boolean caseSensitive)
	{
		if (term == null) return null;
		String v = term;
		if (!caseSensitive) v = term.toLowerCase();
		if (v.contains("*")) v = v.replace("*", "%");
		if (v.contains("?")) v = v.replace("?", "_");
		return v;
	}
	
	/**
	 * Returns the {@link ISession} service.
	 * 
	 * @return See above.
	 */
	public ISession getSessionService()
	{
		return entry.getSessionService();
	}
	
	/**
	 * Returns the {@link IRenderingSettings} service.
	 * 
	 * @return See above.
	 */
	private IRenderingSettings getRenderingSettingsService()
	{
		return entry.getRenderingSettingsService();
	}

	/**
	 * Returns the {@link IRepositoryInfo} service.
	 * 
	 * @return See above.
	 */
	private IRepositoryInfo getRepositoryService()
	{
		return entry.getRepositoryInfoService();
	}

	/**
	 * Returns the {@link IPojos} service.
	 * 
	 * @return See above.
	 */
	private IPojos getPojosService() { return entry.getPojosService(); }

	/**
	 * Returns the {@link IQuery} service.
	 *  
	 * @return See above.
	 */
	private IQuery getQueryService() { return entry.getQueryService(); }

	/**
	 * Returns the {@link IUpdate} service.
	 *  
	 * @return See above.
	 */
	private IUpdate getUpdateService() { return entry.getUpdateService(); }

	/**
	 * Returns the {@link IAdmin} service.
	 * 
	 * @return See above.
	 */
	private IAdmin getAdminService() { return entry.getAdminService(); }
	
	/**
	 * Returns the {@link IDelete} service.
	 * 
	 * @return See above.
	 */
	private IDelete getDeleteService() { return entry.getDeleteService(); }
	
	/**
	 * Returns the {@link ThumbnailStore} service.
	 *  
	 * @return See above.
	 */
	private ThumbnailStore getThumbService()
	{ 
		if (thumbRetrieval == MAX_RETRIEVAL) {
			thumbRetrieval = 0;
			//to be on the save side
			if (thumbnailService != null) thumbnailService.close();
			thumbnailService = null;
		}
		if (thumbnailService == null) {
			thumbnailService = entry.createThumbnailService();
		}
			
		thumbRetrieval++;
		return thumbnailService; 
	}

	/**
	 * Returns the {@link RawFileStore} service.
	 *  
	 * @return See above.
	 */
	private RawFileStore getRawFileService()
	{
		if (fileStore == null) {
			fileStore = entry.createRawFileStore();
			try {
				fileStore.close();
			} catch (Exception e) {
				// Ignore the exception.
			}
		}
		fileStore = entry.createRawFileStore();
		return fileStore;
	}

	/**
	 * Returns the {@link RenderingEngine Rendering service}.
	 * 
	 * @return See above.
	 */
	private RenderingEngine getRenderingService()
	{
		RenderingEngine engine = entry.createRenderingEngine();
		engine.setCompressionLevel(compression);
		return engine;
	}

	/**
	 * Returns the {@link RawPixelsStore} service.
	 * 
	 * @return See above.
	 */
	private RawPixelsStore getPixelsStore()
	{
		return entry.createRawPixelsStore();
	}

	/**
	 * Returns the {@link Search} service.
	 * 
	 * @return See above.
	 */
	private Search getSearchService()
	{
		//if (searchService == null) 
			searchService = entry.createSearchService();
		//searchService.resetDefaults();
		return searchService;
	}
	
	/**
	 * Checks if some default rendering settings have to be created
	 * for the specified set of pixels.
	 * 
	 * @param pixelsID	The pixels ID.
	 * @param re		The rendering engine to load.
	 */
	private synchronized void needDefault(long pixelsID, RenderingEngine re)
	{
		if (re == null) {
			ThumbnailStore service = getThumbService();
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
		String n = group.getName();
		return ("system".equals(n) || "user".equals(n) || "default".equals(n) ||
				"guest".equals(n));
	}

	/**
	 * Returns a collection of users contained in the specified group.
	 * 
	 * @param groupID	The id of the group.
	 * @return See above.
	 */
	private List<Experimenter> containedExperimenters(long groupID)
	{
		IQuery service = getQueryService();
		return service.findAllByQuery("select e from Experimenter e "
	                + "left outer join fetch e.groupExperimenterMap m "
	                + "left outer join fetch m.parent g where g.id = :id", 
	                new Parameters().addId(groupID));
	}
	
	/**
	 * Reconnects to server. This method should be invoked when the password
	 * is reset.
	 * 
	 * @param userName	The name of the user who modifies his/her password.
	 * @param password 	The new password.
	 */
	private void resetFactory(String userName, String password)
	{
		//First close the previous session
		logout();
		login = new Login(userName, password);
		entry = new ServiceFactory(server, new Login(userName, password));
		//if (thumbnailService != null) thumbnailService.close();
		//thumbnailService = null;
		thumbRetrieval = 0;
	}

	/**
	 * Creates a query.
	 * 
	 * @param type			Identifies the table to search on.
	 * @param names			The terms to search for.
	 *@param separator		The separator between words, either <code>and</code>
	 * 						or <code>or</code>.
	 * @param caseSensitive Pass <code>true</code> to take into account the
	 * 						case sensitivity while searching, 
	 * 						<code>false</code> otherwise.
	 * @return The query.
	 */
	private String createSearchQuery(Class type, String[] names, 
									String separator, boolean caseSensitive)
	{
		String sql = null;
		String table;
		separator = " "+separator+" ";
		/*
		if (ImageAnnotation.class.equals(type)) {
			sql = "select obj from ImageAnnotation as obj " +
					"left outer join fetch obj.details.creationEvent as d " +
					"where (";
			for (int j = 0; j < names.length; j++) {
				if (j != 0) sql += separator;
				if (caseSensitive)
					sql += "obj.content like :"+names[j];
				else sql += "lower(obj.content) like :"+names[j];
			}
		} else if (DatasetAnnotation.class.equals(type)) {
			sql = "select obj from DatasetAnnotation as obj " +
			"left outer join fetch obj.details.creationEvent as d where (";
			for (int j = 0; j < names.length; j++) {
				if (j != 0) sql += separator;
				if (caseSensitive)
					sql += "obj.content like :"+names[j];
				else sql += "lower(obj.content) like :"+names[j];
			}
		} else 
		*/
		if (CategoryData.class.equals(type)) {
			table = getTableForLink(Category.class);
			sql = "select obj from "+table+" as obj " +
					"left outer join fetch obj.details.creationEvent " +
					"as d where (";
			//"lower(link.parent.name) like :name or " +
            // "lower(link.parent.description) like :name";
			for (int j = 0; j < names.length; j++) {
				if (j != 0) sql += separator;
				if (caseSensitive)
					sql += "obj.parent.name like :"+names[j];
				else
					sql += "lower(obj.parent.name)  like :"+names[j];
			}
		} else if (ImageData.class.equals(type)) {
			sql =  "select obj from Image as obj left outer join fetch " +
					"obj.details.creationEvent as d where (";
			for (int j = 0; j < names.length; j++) {
				if (j != 0) sql += separator;
				if (caseSensitive) {
					sql += "(obj.name like :"+names[j];
					sql += " or obj.description like :"+names[j]+")";
				} else {
					sql += "(lower(obj.name) like :"+names[j];
					sql += " or lower(obj.description) like :"+names[j]+")";
				}
			}
		} else if (CategoryGroupData.class.equals(type)) {
			table = getTableForLink(CategoryGroup.class);
			sql = "select obj from "+table+" as obj left outer join fetch " +
					"obj.details.creationEvent " +
					"as d where (";
					//"where lower(link.parent.name) like :name or " +
					//"lower(link.parent.description) like :name";
			for (int j = 0; j < names.length; j++) {
				if (j != 0) sql += separator;
				if (caseSensitive)
					sql += "obj.parent.name like :"+names[j];
				else
					sql += "lower(obj.parent.name) like :"+names[j];
			}
		}
		sql += ")";
		return sql;
	}
	
	/**
	 * Formats the terms to search for.
	 * 
	 * @param terms		The terms to search for.
	 * @param service	The search service.
	 * @return See above.
	 */
	private String[] prepareTextSearch(String[] terms, Search service) 
	{
		if (terms == null || terms.length == 0) return null;
		String value;
		int n;
		char[] arr;
		String v;
		String[] formattedTerms = new String[terms.length];
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
				formattedTerms[j] = "\""+v.toLowerCase()+"\"";
			else formattedTerms[j] = v.toLowerCase();
		}
		return formattedTerms;
	}
	
	/**
	 * Formats the terms to search for.
	 * 
	 * @param terms		The terms to search for.
	 * @param service	The search service.
	 * @return See above.
	 */
	private String[] prepareTextSearch(Collection<String> terms, Search service) 
	{
		if (terms == null || terms.size() == 0) return null;
		String value;
		int n;
		char[] arr;
		String v;
		String[] formattedTerms = new String[terms.size()];
		Iterator<String> j = terms.iterator();
		int k = 0;
		while (j.hasNext()) {
			value = j.next();
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
			formattedTerms[k] = v.toLowerCase() ;
		}

		return formattedTerms;
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
		while (i.hasNext()) {
			if (value.startsWith(i.next())) return true;
		}
		
		return false;
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param port      The value of the port.
	 * @param dsFactory A reference to the factory. Used whenever a broken link
	 *                  is detected to get the Login Service and try 
	 *                  reestabishing a valid link to <i>OMEDS</i>.
	 *                  Mustn't be <code>null</code>.
	 */
	OMEROGateway(int port, DataServicesFactory dsFactory)
	{
		if (dsFactory == null) 
			throw new IllegalArgumentException("No Data service factory.");
		this.dsFactory = dsFactory;
		this.port = port;
		thumbRetrieval = 0;
	}

	/**
	 * Converts the specified POJO into the corresponding model.
	 *  
	 * @param nodeType The POJO class.
	 * @return The corresponding class.
	 */
	Class convertPojos(Class nodeType)
	{
		if (nodeType.equals(ProjectData.class)) return Project.class;
		else if (nodeType.equals(DatasetData.class)) return Dataset.class;
		else if (nodeType.equals(ImageData.class)) return Image.class;
		else if (nodeType.equals(CategoryData.class)) return Category.class;
		else if (nodeType.equals(CategoryGroupData.class))
			return CategoryGroup.class;
		else if (nodeType.equals(TagAnnotationData.class)) 
			return TagAnnotation.class;
		else if (nodeType.equals(TextualAnnotationData.class)) 
			return TextAnnotation.class;
		else if (nodeType.equals(FileAnnotationData.class))
			return FileAnnotation.class;
		else if (nodeType.equals(URLAnnotationData.class))
			return UrlAnnotation.class;
		else if (nodeType.equals(ScreenData.class)) return Screen.class;
		else if (nodeType.equals(PlateData.class)) return Plate.class;
		else if (nodeType.equals(WellData.class)) return Well.class;
		else throw new IllegalArgumentException("NodeType not supported");
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
	 * @see IPojos#getUserDetails(Set, Map)
	 */
	ExperimenterData getUserDetails(String name)
		throws DSOutOfServiceException
	{
		//isSessionAlive();
		try {
			IPojos service = getPojosService();
			Set<String> set = new HashSet<String>(1);
			set.add(name);
			Map m = PojoMapper.asDataObjects(service.getUserDetails(set, 
					(new PojoOptions()).map()));
			ExperimenterData data = (ExperimenterData) m.get(name);
			if (data == null) {
				throw new DSOutOfServiceException("Cannot retrieve user's " +
						"data");
			}
			return data;
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
			compression = compressionLevel;
			server = new Server(hostName, port);
			login = new Login(userName, password);
			entry = new ServiceFactory(server, login); 
			connected = true;
			return getUserDetails(userName);
		} catch (Throwable e) {
			connected = false;
			String s = "Can't connect to OMERO. OMERO info not valid.\n\n";
			s += printErrorText(e);
			throw new DSOutOfServiceException(s, e);  
		} 
	}

	/** 
	 * Tries to reconnect to the server.
	 * 
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 */
	void reconnect()
		throws DSOutOfServiceException
	{
		try {
			logout();
			thumbnailService = null;
			thumbRetrieval = 0;
			fileStore = null;
			entry = new ServiceFactory(server, login); 
			connected = true;
		} catch (Throwable e) {
			connected = false;
			String s = "Can't connect to OMERO. OMERO info not valid.\n\n";
			s += printErrorText(e);
			throw new DSOutOfServiceException(s, e);  
		} 
	}

	/** Log out. */
	void logout()
	{
		connected = false;
		try {
			if (thumbnailService != null) thumbnailService.close();
			if (fileStore != null) fileStore.close();
			thumbnailService = null;
			fileStore = null;
			entry.closeSession();
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
	 * {@link IPojos#loadContainerHierarchy(Class, Set, Map)}
	 * and maps the result calling {@link PojoMapper#asDataObjects(Set)}.
	 * 
	 * @param rootNodeType  The top-most type which will be searched for 
	 *                      Can be <code>Project</code> or 
	 *                      <code>CategoryGroup</code>. 
	 *                      Mustn't be <code>null</code>.
	 * @param rootNodeIDs   A set of the IDs of top-most containers. 
	 *                      Passed <code>null</code> to retrieve all container
	 *                      of the type specified by the rootNodetype parameter.
	 * @param options       The Options to retrieve the data.
	 * @return  A set of hierarchy trees.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#loadContainerHierarchy(Class, Set, Map)
	 */
	Set loadContainerHierarchy(Class rootNodeType, Set rootNodeIDs, Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IPojos service = getPojosService();
			return PojoMapper.asDataObjects(service.loadContainerHierarchy(
					convertPojos(rootNodeType), rootNodeIDs, options));
		} catch (Throwable t) {
			handleException(t, "Cannot load hierarchy for "+rootNodeType+".");
		}
		return new HashSet();
	}

	/**
	 * Retrieves hierarchy trees in various hierarchies that
	 * contain the specified Images.
	 * The annotation for the current user is also linked to the object.
	 * Annotations are currently possible only for Image and Dataset.
	 * Wraps the call to the 
	 * {@link IPojos#findContainerHierarchies(Class, Set, Map)}
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
	 * @see IPojos#findContainerHierarchies(Class, Set, Map)
	 */
	Set findContainerHierarchy(Class rootNodeType, Set leavesIDs, Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IPojos service = getPojosService();
			return PojoMapper.asDataObjects(service.findContainerHierarchies(
					convertPojos(rootNodeType), leavesIDs, options));
		} catch (Throwable t) {
			handleException(t, "Cannot find hierarchy for "+rootNodeType+".");
		}
		return new HashSet();
	}

	/**
	 * Finds all the annotations that have been attached to the specified
	 * <code>rootNodes</code>. This method looks for all the <i>valid</i>
	 * annotations that have been attached to each of the specified objects. It
	 * then maps each <code>rootNodeID</code> onto the set of all annotations
	 * that were found for that node. If no annotations were found for that
	 * node, then the entry will be <code>null</code>. Otherwise it will be a
	 * <code>Set</code> containing <code>Annotation</code> objects.
	 * Wraps the call to the 
	 * {@link IPojos#findAnnotations(Class, Set, Set, Map)}
	 * and maps the result calling {@link PojoMapper#asDataObjects(Map)}.
	 * 
	 * @param nodeType      The type of the rootNodes.
	 *                      Mustn't be <code>null</code>. 
	 * @param nodeIDs       TheIds of the objects of type
	 *                      <code>rootNodeType</code>. 
	 *                      Mustn't be <code>null</code>.
	 * @param annotatorIDs  The Ids of the users for whom annotations should be 
	 *                      retrieved. If <code>null</code>, all annotations 
	 *                      are returned.
	 * @param options       Options to retrieve the data.
	 * @return A map whose key is rootNodeID and value the <code>Set</code> of
	 *         all annotations for that node or <code>null</code>.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#findAnnotations(Class, Set, Set, Map)
	 */
	Map findAnnotations(Class nodeType, Set nodeIDs, Set annotatorIDs, 
			Map options)
	throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IPojos service = getPojosService();
			return PojoMapper.asDataObjects(
					service.findAnnotations(convertPojos(nodeType), nodeIDs, 
							annotatorIDs, options));
		} catch (Throwable t) {
			handleException(t, "Cannot find annotations for "+nodeType+".");
		}
		return new HashMap();
	}

	/**
	 * Retrieves paths in the Category Group/Category/Image (CG/C/I) hierarchy.
	 * <p>
	 * Because of the mutually exclusive rule of CG/C hierarchy, this method
	 * is quite tricky.
	 * We want to retrieve all Category Group/Category paths that end with
	 * the specified leaves.
	 * </p>
	 * <p> 
	 * We also want to retrieve the all Category Group/Category paths that
	 * don't end with the specified leaves, note that in that case because of 
	 * the mutually exclusive constraint the categories which don't contain a
	 * specified leaf but which is itself contained in a group which already
	 * has a category ending with the specified leaf is excluded.
	 * </p>
	 * <p>  
	 * This is <u>more</u> restrictive than may be imagined. The goal is to 
	 * find CGC paths to which an Image <B>MAY</b> be attached.
	 * </p>
	 * Wraps the call to the {@link IPojos#findCGCPaths(Set, String, Map)}
	 * and maps the result calling {@link PojoMapper#asDataObjects(Set)}.
	 * 
	 * @param imgIDs    Set of ids of the images that sit at the bottom of the
	 *                  CGC trees. Mustn't be <code>null</code>.
	 * @param algorithm The search algorithm for finding paths. One of the 
	 *                  following constants: 
	 * @param options   Options to retrieve the data.
	 * @return A <code>Set</code> of hierarchy trees with all root nodes 
	 * that were found.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#findCGCPaths(Set, String, Map)
	 */
	Set findCGCPaths(Set imgIDs, int algorithm, Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IPojos service = getPojosService();
			return PojoMapper.asDataObjects(service.findCGCPaths(imgIDs, 
					mapAlgorithmToString(algorithm),
					options));
		} catch (Throwable t) {
			handleException(t, "Cannot find CGC paths.");
		}
		return new HashSet();
	}

	/**
	 * Retrieves the images contained in containers specified by the 
	 * node type.
	 * Wraps the call to the {@link IPojos#getImages(Class, Set, Map)}
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
	 * @see IPojos#getImages(Class, Set, Map)
	 */
	Set getContainerImages(Class nodeType, Set nodeIDs, Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IPojos service = getPojosService();
			return PojoMapper.asDataObjects(service.getImages(
					convertPojos(nodeType), nodeIDs, options));
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
			IPojos service = getPojosService();
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
	 * @param options		Options to retrieve the data.		
	 * @param rootNodeIDs	Set of root node IDs.
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#getCollectionCount(String, String, Set, Map)
	 */
	Map getCollectionCount(Class rootNodeType, String property, Set rootNodeIDs,
			Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IPojos service = getPojosService();
			String p = convertProperty(rootNodeType, property);
			if (p == null) return null;
			return PojoMapper.asDataObjects(service.getCollectionCount(
					convertPojos(rootNodeType).getName(), p, rootNodeIDs, 
					options));
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
			IPojos service = getPojosService();
			return service.createDataObject(object, options);
		} catch (Throwable t) {
			handleException(t, "Cannot update the object.");
		}
		return null;
	}

	/**
	 * Creates the speficied objects.
	 * 
	 * @param objects   The object to create.
	 * @param options   Options to create the data.  
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 * @see IPojos#createDataObjects(IObject[], Map)
	 */
	IObject[] createObjects(IObject[] objects, Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IPojos service = getPojosService();
			IObject[] results = service.createDataObjects(objects, options);
			return results;
		} catch (Throwable t) {
			handleException(t, "Cannot update the object.");
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
			IUpdate service = getUpdateService();
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
	void deleteObjects(IObject[] objects)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IUpdate service = getUpdateService();
			for (int i = 0; i < objects.length; i++) {
				service.deleteObject(objects[i]);
			}
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
	IObject updateObject(IObject object, Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IPojos service = getPojosService();
			return service.updateDataObject(object, options);
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
	IObject[] updateObjects(IObject[] objects, Map options)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IPojos service = getPojosService();
			return service.updateDataObjects(objects, options);
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
	PixelsDimensions getPixelsDimensions(long pixelsID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			Pixels pixs = getPixels(pixelsID);
			if (pixs == null) return null;
			return pixs.getPixelsDimensions();
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the dimension of " +
								"the pixels set.");
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
			IQuery service = getQueryService();
			Pixels pixs = (Pixels) service.findByQuery(
					"select p from Pixels as p " +
					"left outer join fetch p.pixelsType as pt " +
					"left outer join fetch p.pixelsDimensions " +
					"where p.id = :id",
					new Parameters().addId(new Long(pixelsID)));

			return pixs;
		} catch (Throwable t) {
			t.printStackTrace();
			handleException(t, "Cannot retrieve the pixels set of "+
			"the pixels set.");
		}
		return null;
	}
	/**
	 * Retrieves the channel information related to the given pixels set.
	 * 
	 * @param pixelsID  The id of the pixels set.
	 * @return A list of <code>Channel</code> Objects.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	Collection getChannelsData(long pixelsID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQuery service = getQueryService();
			Pixels pixs = (Pixels) service.findByQuery(
					"select p from Pixels as p " +
					"left outer join fetch p.pixelsType as pt " +
					"left outer join fetch p.channels as c " +
					"left outer join fetch p.pixelsDimensions " +
					"left outer join fetch c.logicalChannel as lc " +
					"left outer join fetch c.statsInfo where p.id = :id",
					new Parameters().addId(new Long(pixelsID)));
			return pixs.unmodifiableChannels();//pixs.getChannels();
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the channelsData for "+
					"the pixels set "+pixelsID);
		}
		return new ArrayList();
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
			ThumbnailStore service = getThumbService();
			needDefault(pixelsID, null);
			//getRendering Def for a given pixels set.
			if (userID >= 0) {
				RenderingDef def = getRenderingDef(pixelsID, userID);
				if (def != null) service.setRenderingDefId(def.getId());
			}
			return service.getThumbnail(new Integer(sizeX), 
					                         new Integer(sizeY));
			//return service.getThumbnailDirect(new Integer(sizeX), 
			//		new Integer(sizeY));
		} catch (Throwable t) {
			if (thumbnailService != null) thumbnailService.close();
			thumbnailService = null;
			if (t instanceof EJBException || 
					t.getCause() instanceof IllegalStateException) {
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
			ThumbnailStore service = getThumbService();
			needDefault(pixelsID, null);
			return service.getThumbnailByLongestSide(maxLength);
		} catch (Throwable t) {
			if (thumbnailService != null) thumbnailService.close();
			thumbnailService = null;
			if (t instanceof EJBException || 
					t.getCause() instanceof IllegalStateException) {
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
			ThumbnailStore service = getThumbService();
			Set<Long> ids = new HashSet<Long>();
			Iterator<Long> i = pixelsID.iterator();
			while (i.hasNext())  {
				long id = i.next();
				
				ids.add(id);
			}
				
			return service.getThumbnailByLongestSideSet(maxLength, ids);
		} catch (Throwable t) {
			if (thumbnailService != null) thumbnailService.close();
			thumbnailService = null;
			if (t instanceof EJBException || 
					t.getCause() instanceof IllegalStateException) {
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
	synchronized RenderingEngine createRenderingEngine(long pixelsID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			RenderingEngine service = getRenderingService();
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
			IQuery service = getQueryService();
			String table = getTableForAnnotationLink(type);
			if (table == null) return null;
			String sql = "select link from "+table+" as link where " +
			"link.parent.id = :parentID"; 
			
			Parameters param = new Parameters();
			param.addLong("parentID", parentID);
			if (childID >= 0) {
				sql += " and link.child.id = :childID";
				param.addLong("childID", childID);
			}
			
			return service.findByQuery(sql, param);
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
	List findAnnotationLinks(Class parentType, long parentID, 
								List<Long> children)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQuery service = getQueryService();
			String table = getTableForAnnotationLink(parentType);
			if (table == null) return null;
			String sql = "select link from "+table+" as link";
			Parameters param = new Parameters();
			if (parentID > 0) {
				sql += " where link.parent.id = :parentID";
				if (children != null && children.size() > 0) {
					sql += " and link.child.id in (:childIDs)";
					param.addList("childIDs", children);
				}
				param.addLong("parentID", parentID);
			} else {
				if (children != null && children.size() > 0) {
					sql += " where link.child.id in (:childIDs)";
					param.addList("childIDs", children);
				}
			}
			return service.findAllByQuery(sql, param);
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the annotation links for "+
					"parent ID: "+parentID);
		}
		return null;
	}	
	
	/**
	 * Finds the link if any between the specified parent and child.
	 * 
	 * @param ids   The ids of either the parent or the child.
	 * @param union
	 * @return See above.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	List findlinkedTags(List<Long> ids, boolean union)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			if (ids == null) return null;
			IQuery service = getQueryService();
			String table = getTableForAnnotationLink(Annotation.class);
			if (table == null) return null;
			Parameters param = new Parameters();
			param.addList("ids", ids);
			List r = null;
			String sql = "select link from "+table+" as link";
			if (union) {
				sql += " where link.child.id in (:ids)";
				r = service.findAllByQuery(sql, param);
				sql = "select link from "+table+" as link";
				sql += " where link.parent.id in (:ids)";
				r.addAll(service.findAllByQuery(sql, param));
			} else {
				//param.addList("parentIds", ids);
				sql += " where link.child.id in (:ids) and";
				sql += " link.parent.id in (:ids)";
				r = service.findAllByQuery(sql, param);
			}
			
			return r;
		} catch (Throwable t) {
			t.printStackTrace();
			handleException(t, "Cannot retrieve the annotation links");
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
			IQuery service = getQueryService();
			Parameters param = new Parameters();
			param.addLong("parentID", parent.getId());
			param.addLong("childID", child.getId());
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
			Parameters param = new Parameters();
			param.addLong("parentID", parent.getId());
			String sql = "select link from "+table+" as link where " +
			"link.parent.id = :parentID"; 
			if (children != null && children.size() > 0) {
				sql += " and link.child.id in (:childIDs)";
				param.addList("childIDs", children);
			}
			IQuery service = getQueryService();
			
			
			return service.findAllByQuery(sql, param);
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
	List findLinks(Class parentClass, Set children, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			String table = getTableForLink(parentClass);
			if (table == null) return null;
			String sql = "select link from "+table+" as link where " +
			"link.child.id in (:childIDs)";
			IQuery service = getQueryService();
			Parameters param = new Parameters();
			param.addSet("childIDs", children);
			if (userID >= 0) {
				sql += " and link.details.owner.id = :userID";
				param.addLong("userID", userID);
			}
			
			return service.findAllByQuery(sql, param);
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
			Parameters param = new Parameters();
			param.addLong("childID", childID);
			String table = getTableForLink(parentClass);
			if (table == null) return null;
			String sql = "select link from "+table+" as link where " +
			"link.child.id = :childID";
			if (userID >= 0) {
				sql += " and link.details.owner.id = :userID";
				param.addLong("userID", userID);
			}
			IQuery service = getQueryService();
			return service.findAllByQuery(sql, param);
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
			IQuery service = getQueryService();
			return service.find(o.getClass(), o.getId().longValue());
		} catch (Throwable t) {
			handleException(t, "Cannot retrieve the requested object with "+
					"object ID: "+o.getId());
		}
		return null;
	} 

	/**
	 * Retrieves an updated version of the specified object.
	 * 
	 * @param klass	The type of object to retrieve.
	 * @param id 	The object's id.
	 * @return The last version of the object.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	IObject findIObject(Class klass, long id)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQuery service = getQueryService();
			return service.find(klass, id);
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
			IAdmin service = getAdminService();
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
			
			while (i.hasNext()) {
				group = (ExperimenterGroup) i.next();
				if (!isSystemGroup(group) && groupIds.contains(group.getId())) {
					pojoGroup = PojoMapper.asDataObject(group);
					experimenters = containedExperimenters(group.getId());
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
		Set<Long> ids = new HashSet<Long>();
		ids.add(pixelsID);
		isSessionAlive();
		try {
			IPojos service = getPojosService();
			Map m = service.findAnnotations(Pixels.class, ids, null, 
									(new PojoOptions().map()));
			Collection c = (Collection) m.get(pixelsID);
			if (c == null || c.size() == 0) return false;
			Iterator i = c.iterator();
			Annotation data;
			while (i.hasNext()) {
				data = (Annotation) i.next();
				if (data instanceof BooleanAnnotation) {
					BooleanAnnotation ann = (BooleanAnnotation) data;
					if (ArchivedAnnotationData.IMPORTER_ARCHIVED_NS.equals(
						ann.getNs()))
						return ann.getBoolValue();
				}
			}
		} catch (Exception e) {
			handleException(e, "Cannot retrieve the annotations related " +
								"to "+pixelsID);
		}
		
		return false;
	}
	
	/**
	 * Retrieves the archived files if any for the specified set of pixels.
	 * 
	 * @param path		The location where to save the files.
	 * @param pixelsID 	The ID of the pixels set.
	 * @return See above.
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	Map<Integer, List> getArchivedFiles(String path, long pixelsID) 
		throws DSAccessException
	{
		isSessionAlive();
		IQuery service = getQueryService();
		List files = null;
		try {
			files = service.findAllByQuery(
					"select ofile from OriginalFile as ofile left join " +
					"ofile.pixelsFileMaps as pfm left join pfm.child as " +
					"child where child.id = :id",
					new Parameters().addId(new Long(pixelsID)));
		} catch (Exception e) {
			throw new DSAccessException("Cannot retrieve original file", e);
		}

		Map<Integer, List> result = new HashMap<Integer, List>();
		if (files == null || files.size() == 0) return result;
		RawFileStore store = getRawFileService();
		Iterator i = files.iterator();
		OriginalFile of;

		int size;	
		FileOutputStream stream = null;
		int offset = 0;
		File f;
		List<String> notDownloaded = new ArrayList<String>();
		String fullPath;
		while (i.hasNext()) {
			of = (OriginalFile) i.next();
			store.setFileId(of.getId()); 
			fullPath = path+of.getName();
			f = new File(fullPath);
			try {
				stream = new FileOutputStream(f);
				size = of.getSize().intValue(); 
				try {
					try {
						for (offset = 0; (offset+INC) < size;) {
							stream.write(store.read(offset, INC));
							offset += INC;
						}	
					} finally {
						stream.write(store.read(offset, size-offset)); 
						stream.close();
					}
				} catch (Exception e) {
					if (stream != null) stream.close();
					if (f != null) f.delete();
					notDownloaded.add(of.getName());
				}
			} catch (IOException e) {
				if (f != null) f.delete();
				notDownloaded.add(of.getName());
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
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	File downloadFile(File file, long fileID, long size)
		throws DSAccessException
	{
		isSessionAlive();
		return file;
		/*
		RawFileStore store = getRawFileService();
		store.setFileId(fileID);
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
		*/
	}
	
	/**
	 * Returns the original file corresponding to the passed id.
	 * 
	 * @param id	The id identifying the file.
	 * @return See above.
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	OriginalFile getOriginalFile(long id)
		throws DSAccessException
	{
		isSessionAlive();
		IQuery service = getQueryService();
		OriginalFile of;
		try {
			of = (OriginalFile) service.findByQuery(
					"select p from OriginalFile as p " +
					"left outer join fetch p.format " +
					"where p.id = :id", 
					new Parameters().addId(new Long(id)));
		} catch (Exception e) {
			throw new DSAccessException("Cannot retrieve original file", e);
		}
		return of;
	}
	
	/**
	 * Returns the collection of original files related to the specifed 
	 * pixels set.
	 * 
	 * @param pixelsID The ID of the pixels set.
	 * @return See above.
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	List getOriginalFiles(long pixelsID)
		throws DSAccessException
	{
		isSessionAlive();
		IQuery service = getQueryService();
		List files = null;
		try {
			files = service.findAllByQuery(
					"select ofile from OriginalFile as ofile left join " +
					"ofile.pixelsFileMaps as pfm left join pfm.child as " +
					"child where child.id = :id",
					new Parameters().addId(new Long(pixelsID)));
		} catch (Exception e) {
			throw new DSAccessException("Cannot retrieve original file", e);
		}
		return files;
	}
	
	/**
	 * Uploads the passed file to the server and returns the 
	 * original file i.e. the server object.
	 * 
	 * @param file		The file to upload.
	 * @param format	The format of the file.
	 * @return See above.
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service.
	 */
	OriginalFile uploadFile(File file, String format)
		throws DSAccessException
	{
		if (file == null)
			throw new IllegalArgumentException("No file to upload");
		isSessionAlive();
		Format f = getQueryService().findByString(Format.class, "value", 
												format);
		OriginalFile oFile = new OriginalFile();
		oFile.setName(file.getName());
		oFile.setPath(file.getAbsolutePath());
		oFile.setSize(file.length());
		oFile.setSha1("pending");
		oFile.setFormat(f);
		IUpdate service = getUpdateService();
		OriginalFile save = service.saveAndReturnObject(oFile);
		RawFileStore store = getRawFileService();
		store.setFileId(save.getId());
		byte[] buf = new byte[INC]; 
		try {
			FileInputStream stream = new FileInputStream(file);
			long pos = 0;
			int rlen;
			while((rlen = stream.read(buf)) > 0) {
				store.write(buf, pos, rlen);
				pos += rlen;
				ByteBuffer.wrap(buf).limit(rlen);
			}
		} catch (Exception e) {
			throw new DSAccessException("Cannot upload the file with path " +
					file.getAbsolutePath(), e);
		}
		return save;
	}
	
	/**
	 * Modifies the password of the currently logged in user.
	 * 
	 * @param userName	The name of the user whose password has not be changed.
	 * @param password	The new password.
	 * @throws DSOutOfServiceException If the connection is broken, or logged in
	 * @throws DSAccessException If an error occured while trying to 
	 * retrieve data from OMERO service. 
	 */
	void changePassword(String userName, String password)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		IAdmin service = getAdminService();
		try {
			service.changePassword(password);
			resetFactory(userName, password);
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
		IAdmin service = getAdminService();
		try {
			service.updateSelf(exp);
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
		RawPixelsStore service = getPixelsStore();
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
		IRepositoryInfo service = getRepositoryService();
		try {
			return service.getFreeSpaceInKilobytes();
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
		IRepositoryInfo service = getRepositoryService();
		try {
			return service.getUsedSpaceInKilobytes();
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
			IQuery service = getQueryService();
			Parameters param = new Parameters();
			param.addLong("userID", userID);
			if (startTime != null && endTime != null) {
				sql += "c.time < :endTime and c.time > :startTime";
				param.add(new QueryParameter("startTime", Timestamp.class, 
						startTime));
				param.add(new QueryParameter("endTime", Timestamp.class, 
						endTime));
			} else if (startTime == null && endTime != null) {
				sql += "c.time < :endTime";
				param.add(new QueryParameter("endTime", Timestamp.class, 
					endTime));
			} else if (startTime != null && endTime == null) {
				sql += "c.time  > :startTime";
				param.add(new QueryParameter("startTime", Timestamp.class, 
						startTime));
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
	 * @param map The options. 
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Set getImages(Map map)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IPojos service = getPojosService();
			return PojoMapper.asDataObjects(service.getImagesByOptions(map));
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
	Map resetRenderingSettings(Class rootNodeType, Set nodes) 
		throws DSOutOfServiceException, DSAccessException
	{
		Set<Long> success = new HashSet<Long>();
		Set<Long> failure = new HashSet<Long>();
		isSessionAlive();
		try {
			IRenderingSettings service = getRenderingSettingsService();
			Class klass = convertPojos(rootNodeType);
			if (klass.equals(Image.class) || klass.equals(Dataset.class) ||
					klass.equals(Category.class))
				success = service.resetDefaultsInSet(klass, nodes);
		} catch (Exception e) {
			handleException(e, "Cannot reset the rendering settings.");
		}
		Map<Boolean, Set> result = new HashMap<Boolean, Set>(2);
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
	Map setOriginalRenderingSettings(Class rootNodeType, Set nodes) 
		throws DSOutOfServiceException, DSAccessException
	{
		Set<Long> success = new HashSet<Long>();
		Set<Long> failure = new HashSet<Long>();
		isSessionAlive();
		try {
			IRenderingSettings service = getRenderingSettingsService();
			Class klass = convertPojos(rootNodeType);
			if (klass.equals(Image.class) || klass.equals(Dataset.class))
				success = service.setOriginalSettingsInSet(klass, nodes);
		} catch (Exception e) {
			handleException(e, "Cannot reset the rendering settings.");
		}
		Map<Boolean, Set> result = new HashMap<Boolean, Set>(2);
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
	 * @param pixelsID		The id of the pixels set of reference.
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
	Map pasteRenderingSettings(long pixelsID, Class rootNodeType, Set nodes) 
		throws DSOutOfServiceException, DSAccessException
	{
		Set<Long> success = new HashSet<Long>();
		Set<Long> failure = new HashSet<Long>();
		isSessionAlive();
		try {
			IRenderingSettings service = getRenderingSettingsService();
			Class klass = convertPojos(rootNodeType);
			Iterator i = nodes.iterator();
			long id;
			boolean b = false;
			if (klass.equals(Image.class)) {
				while (i.hasNext()) {
					id = (Long) i.next();
					b = service.applySettingsToImage(pixelsID, id);
					if (b) success.add(id);
					else failure.add(id);
				}
			} else if (klass.equals(Dataset.class)) {
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
			} else if (klass.equals(Category.class)) {
				Map m;
				List l;
				Iterator k;
				while (i.hasNext()) {
					id = (Long) i.next();
					m = service.applySettingsToCategory(pixelsID, id);
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
		Map<Boolean, Set> result = new HashMap<Boolean, Set>(2);
		result.put(Boolean.TRUE, success);
		result.put(Boolean.FALSE, failure);
		return result;
	}
	
	/**
	 * Retrieves all the rendering settings linked to the specified set
	 * of pixels.
	 * 
	 * @param pixelsID	The pixels ID.
	 * @return Map whose key is the experimenter who set the settings,
	 * 		  and the value is the rendering settings itself.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Map getRenderingSettings(long pixelsID)
		throws DSOutOfServiceException, DSAccessException
	{
		Map map = new HashMap();
		isSessionAlive();
		try {
			String sql =  "select rdef from RenderingDef as rdef "
                + "left outer join fetch rdef.quantization "
                + "left outer join fetch rdef.model "
                + "left outer join fetch rdef.waveRendering as cb "
                + "left outer join fetch cb.color "
                + "left outer join fetch cb.family "
                + "left outer join fetch rdef.spatialDomainEnhancement " 
                + "left outer join fetch rdef.details.owner "
                + "where rdef.pixels.id = :pixid";
			IQuery service = getQueryService();
			Parameters param = new Parameters();
			param.addLong("pixid", pixelsID);
			List results = service.findAllByQuery(sql, param);
			if (results == null || results.size() == 0) return map;
			Iterator i = results.iterator();
			RenderingDef rndDef;
			Experimenter exp;
			while (i.hasNext()) {
				rndDef = (RenderingDef) i.next();
				exp = rndDef.getDetails().getOwner();
				//if (exp.getId() != userID) {
				map.put(PojoMapper.asDataObject(exp), rndDef);
				//}
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
			String sql =  "select rdef from RenderingDef as rdef "
                + "left outer join fetch rdef.quantization "
                + "left outer join fetch rdef.model "
                + "left outer join fetch rdef.waveRendering as cb "
                + "left outer join fetch cb.color "
                + "left outer join fetch cb.family "
                + "left outer join fetch rdef.spatialDomainEnhancement " 
                + "left outer join fetch rdef.details.owner "
                + "where rdef.pixels.id = :pixid and " +
                	"rdef.details.owner.id = :userid";
			IQuery service = getQueryService();
			Parameters param = new Parameters();
			param.addLong("pixid", pixelsID);
			param.addLong("userid", userID);
			return service.findByQuery(sql, param);
		} catch (Exception e) {
			handleException(e, "Cannot retrieve the rendering settings");
		}
		
		return null;
	}

	/**
	 * Searches for the categories whose name contains the passed term.
	 * Returns a collection of objects.
	 * 
	 * @param type 			The class identify the object to search for.
	 * @param terms			The terms to search for.
	 * @param start			The start value of a time interval.
	 * @param end			The end value of a time interval.
	 * @param user 			The user to exclude from the search.
	 * @param separator		The separator between words, either <code>and</code>
	 * 						or <code>or</code>.
	 * @param caseSensitive Pass <code>true</code> to take into account the
	 * 						case sensitivity while searching, 
	 * 						<code>false</code> otherwise.	
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List searchFor(Class type, List<String> terms, Timestamp start, 
					Timestamp end, ExperimenterData user, String separator,
					boolean caseSensitive)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		IQuery service = getQueryService();
		Parameters param = new Parameters();
		Iterator i = terms.iterator();
		String term;
		String[] names = new String[terms.size()];
		int index = 0;
		while (i.hasNext()) {
			term = (String)  i.next();
			if (term != null) {
				names[index] = "name"+index;
				/*
				t = term;
				if (!caseSensitive) t = term.toLowerCase();
				if (CategoryData.class.equals(type) || 
						CategoryGroupData.class.equals(type)) 
					param.addString(names[index], t);
				else
				
					param.addString(names[index], "%"+t+"%");
					*/
				param.addString(names[index], formatTerm(term, caseSensitive));
				index++;
			}
		}
		try {
			String sql = createSearchQuery(type, names, separator, 
											caseSensitive);
			if (start != null && end != null) {
				sql += " and d.time > :startTime and d.time < :endTime";
				param.add(new QueryParameter("startTime", Timestamp.class, 
						start));
				param.add(new QueryParameter("endTime", Timestamp.class, end));
			} else if (start == null && end != null) {
				sql += " and (d.time < :endTime)";
				param.add(new QueryParameter("endTime", Timestamp.class, end));
			} else if (start != null && end == null) {
				sql += " and d.time > :startTime";
				param.add(new QueryParameter("startTime", Timestamp.class, 
						start));
			}
			
			//No users so retrieve the data of all members of a group.
			param.addLong("userID", user.getId());
			List groups = user.getGroups();//user.getGroups();
			Set<Long> groupIDs = new HashSet<Long>(groups.size());
			i = groups.iterator();
			while (i.hasNext()) 
				groupIDs.add(((DataObject) i.next()).getId());
			
			param.addSet("groupIDs", groupIDs);
			sql += " and obj.details.owner.id != :userID";
			String table;
			if (sql == null) return null;
			if (CategoryGroupData.class.equals(type)) {
				List l = service.findAllByQuery(sql, param);
				if (l != null && l.size() > 0) {
					i = l.iterator();
					Set<Long> ids = new HashSet<Long>();
					while (i.hasNext()) {
						ids.add(((ILink) i.next()).getChild().getId());
					}
					table = getTableForLink(Category.class);
					sql = "select link from "+table+" as link where " +
					"link.parent.id in (:parentIDs)";
					param = new Parameters();
					param.addSet("parentIDs", ids);
					return service.findAllByQuery(sql, param);
				}
			}
			return service.findAllByQuery(sql, param);
			
		} catch (Exception e) {
			handleException(e, "Search not valid");
		}
		return new ArrayList();
	}
	
	/**
	 * Searches for the categories whose name contains the passed term.
	 * Returns a collection of objects.
	 * 
	 * @param type 	The class identify the object to search for.
	 * @param term	The term to search for.
	 * @param start	The start value of a time interval.
	 * @param end	The end value of a time interval.
	 * @param user 	The user to exclude from the search.
	 * @param separator		The separator between words, either <code>and</code>
	 * 						or <code>or</code>.
	 * @param caseSensitive Pass <code>true</code> to take into account the
	 * 						case sensitivity while searching, 
	 * 						<code>false</code> otherwise.	
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List searchFor(Class type, String term, Timestamp start, 
					Timestamp end, ExperimenterData user, String separator,
					boolean caseSensitive)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		IQuery service = getQueryService();
		Parameters param = new Parameters();
		
		String[] names = new String[1];
		
		names[0] = "name"+0;
		param.addString(names[0], formatTerm(term, caseSensitive));
		
		try {
			String sql = createSearchQuery(type, names, separator, 
					caseSensitive);
			if (start != null && end != null) {
				sql += " and d.time > :startTime and d.time < :endTime";
				param.add(new QueryParameter("startTime", Timestamp.class, 
						start));
				param.add(new QueryParameter("endTime", Timestamp.class, end));
			} else if (start == null && end != null) {
				sql += " and (d.time < :endTime)";
				param.add(new QueryParameter("endTime", Timestamp.class, end));
			} else if (start != null && end == null) {
				sql += " and d.time > :startTime";
				param.add(new QueryParameter("startTime", Timestamp.class, 
						start));
			}
			
			//No users so retrieve the data of all members of a group.
			param.addLong("userID", user.getId());
			List groups = user.getGroups();
			Set<Long> groupIDs = new HashSet<Long>(groups.size());
			Iterator i = groups.iterator();
			while (i.hasNext()) {
				groupIDs.add(((DataObject) i.next()).getId());
			}
			
			param.addSet("groupIDs", groupIDs);
			sql += " and obj.details.owner.id != :userID";
			String table;
			if (sql == null) return null;
			if (CategoryGroupData.class.equals(type)) {
				List l = service.findAllByQuery(sql, param);
				if (l != null && l.size() > 0) {
					i = l.iterator();
					Set<Long> ids = new HashSet<Long>();
					while (i.hasNext()) {
						ids.add(((ILink) i.next()).getChild().getId());
					}
					table = getTableForLink(Category.class);
					sql = "select link from "+table+" as link where " +
					"link.parent.id in (:parentIDs)";
					param = new Parameters();
					param.addSet("parentIDs", ids);
					return service.findAllByQuery(sql, param);
				}
			}
			return service.findAllByQuery(sql, param);
			
		} catch (Exception e) {
			handleException(e, "Search not valid");
		}
		return new ArrayList();
	}

	/**
	 * Searches for the categories whose name contains the passed term.
	 * Returns a collection of objects.
	 * 
	 * @param type 			The class identify the object to search for.
	 * @param terms			The terms to search for.
	 * @param start			The lower bound of the time interval.
	 * @param end			The upper bound of the time interval.
	 * @param users			The collection of potential users.
	 * @param separator		The separator between words, either <code>and</code>
	 * 						or <code>or</code>.
	 * @param caseSensitive Pass <code>true</code> to take into account the
	 * 						case sensitivity while searching, 
	 * 						<code>false</code> otherwise.	
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List searchFor(Class type, List<String> terms, Timestamp start, 
					Timestamp end, List<ExperimenterData> users, 
					String separator, boolean caseSensitive)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		IQuery service = getQueryService();
		Parameters param = new Parameters();
		Iterator i = terms.iterator();
		String term;
		String[] names = new String[terms.size()];
		int index = 0;
		while (i.hasNext()) {
			term = (String)  i.next();
			if (term != null) {
				names[index] = "name"+index;
				/*
				if (caseSensitive) t = term;
				else t = term.toLowerCase();
				if (t.contains("*")) t = t.replace("*", "%");
				if (t.contains("?")) t = t.replace("?", "_");
				
				if (CategoryData.class.equals(type) || 
					CategoryGroupData.class.equals(type)) 
					param.addString(names[index], t);
				else
					param.addString(names[index], "%"+t+"%");
				
				*/
				param.addString(names[index], formatTerm(term, caseSensitive));
				index++;
			}
		}
		
		try {
			String sql = createSearchQuery(type, names, separator, 
											caseSensitive);
			if (start != null && end != null) {
				sql += " and d.time > :startTime and d.time < :endTime";
				param.add(new QueryParameter("startTime", Timestamp.class, 
						start));
				param.add(new QueryParameter("endTime", Timestamp.class, end));
			} else if (start == null && end != null) {
				sql += " and d.time < :endTime";
				param.add(new QueryParameter("endTime", Timestamp.class, end));
			} else if (start != null && end == null) {
				sql += " and d.time > :startTime";
				param.add(new QueryParameter("startTime", Timestamp.class, 
						start));
			}
			Set<Long> ids = null;
			if (users != null && users.size() > 0) {
				i = users.iterator();
				ids = new HashSet<Long>(users.size());
				while (i.hasNext()) 
					ids.add(((ExperimenterData) i.next()).getId());
				
				param.addSet("userids", ids);
				sql += " and obj.details.owner.id in (:userids)";
			}
			Project p = new Project();
			p.getDetails().getOwner();
			String table;
			if (sql == null) return null;
			if (CategoryGroupData.class.equals(type)) {
				List l = service.findAllByQuery(sql, param);
				if (l != null && l.size() > 0) {
					i = l.iterator();
					ids = new HashSet<Long>();
					while (i.hasNext()) {
						ids.add(((ILink) i.next()).getChild().getId());
					}
					table = getTableForLink(Category.class);
					sql = "select o from "+table+" as o where " +
					"o.parent.id in (:parentIDs)";
					param = new Parameters();
					param.addSet("parentIDs", ids);
					return service.findAllByQuery(sql, param);
				}
			}
			//param.add
			return service.findAllByQuery(sql, param);
			
		} catch (Exception e) {
			handleException(e, "Search not valid");
		}
		return new ArrayList();
	}

	/**
	 * Searches for the categories whose name contains the passed term.
	 * Returns a collection of objects.
	 * 
	 * @param type 			The class identifying the object to search for.
	 * @param term			The term to search for.
	 * @param start			The lower bound of the time interval.
	 * @param end			The upper bound of the time interval.
	 * @param users			The collection of potential users.
	 * @param separator		The separator between words, either <code>and</code>
	 * 						or <code>or</code>.
	 * @param caseSensitive Pass <code>true</code> to take into account the
	 * 						case sensitivity while searching, 
	 * 						<code>false</code> otherwise.	
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List searchFor(Class type, String term, Timestamp start, 
					Timestamp end, List<ExperimenterData> users, 
					String separator, boolean caseSensitive)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		IQuery service = getQueryService();
		Parameters param = new Parameters();
		
		String[] names = new String[1];
		names[0] = "name"+0;
		/*
		String t = term;
		if (!caseSensitive) t = term.toLowerCase();
		if (type.equals(CategoryData.class) || 
				type.equals(CategoryGroupData.class))
			param.addString(names[0], t);
		else 
			param.addString(names[0], "%"+t+"%");
			*/
		param.addString(names[0], formatTerm(term, caseSensitive));
		try {
			String sql = createSearchQuery(type, names, separator, 
										caseSensitive);
			if (start != null && end != null) {
				sql += " and d.time > :startTime and d.time < :endTime";
				param.add(new QueryParameter("startTime", Timestamp.class, 
						start));
				param.add(new QueryParameter("endTime", Timestamp.class, end));
			} else if (start == null && end != null) {
				sql += " and d.time < :endTime";
				param.add(new QueryParameter("endTime", Timestamp.class, end));
			} else if (start != null && end == null) {
				sql += " and d.time > :startTime";
				param.add(new QueryParameter("startTime", Timestamp.class, 
						start));
			}
			Set<Long> ids = null;
			Iterator i;
			if (users != null && users.size() > 0) {
				i = users.iterator();
				ids = new HashSet<Long>(users.size());
				while (i.hasNext()) 
					ids.add(((ExperimenterData) i.next()).getId());
				
				param.addSet("userids", ids);
				sql += " and obj.details.owner.id in (:userids)";
			}
			if (ids != null) {
				param.addSet("userids", ids);
				sql += " and obj.details.owner.id in (:userids)";
			}
			String table;
			if (sql == null) return null;
			if (CategoryGroupData.class.equals(type)) {
				List l = service.findAllByQuery(sql, param);
				if (l != null && l.size() > 0) {
					i = l.iterator();
					ids = new HashSet<Long>();
					while (i.hasNext()) {
						ids.add(((ILink) i.next()).getChild().getId());
					}
					table = getTableForLink(Category.class);
					sql = "select o from "+table+" as o where " +
					"o.parent.id in (:parentIDs)";
					param = new Parameters();
					param.addSet("parentIDs", ids);
					return service.findAllByQuery(sql, param);
				}
			}
			//param.add
			return service.findAllByQuery(sql, param);
			
		} catch (Exception e) {
			handleException(e, "Search not valid");
		}
		return new ArrayList();
	}
	
	/**
	 * Searches for the categories whose name contains the passed term.
	 * Returns a collection of objects.
	 * 
	 * @param id				The id of the annotation.
	 * @param userID			The id of the user the annotations are for.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Set fetchAnnotation(long id, long userID)
	    throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQuery service = getQueryService();
			Parameters param = new Parameters();
			String sql =  "select ann from Annotation as ann "
                + "left outer join fetch ann.details.creationEvent "
                + "left outer join fetch ann.details.owner";
			if (id >= 0 && userID >= 0) {
				sql += " where ann.id = :id and ann.details.owner.id = :uid";
				param.addLong("id", id);
				param.addLong("uid", userID);
			} else if (id < 0 && userID >= 0) {
				sql += " where ann.details.owner.id = :uid";
				param.addLong("uid", userID);
			} else if (id >= 0 && userID < 0) {
				sql += " where ann.id = :id";
				param.addLong("id", id);
			}
			return PojoMapper.asDataObjects(service.findAllByQuery(sql, param));
		} catch (Exception e) {
			handleException(e, "Cannot retrieve the annotations");
		}
		return null;
	}
	
	/**
	 * Retrieves the annotations.
	 * 
	 * @param ids
	 * @param userID
	 * @param asChild
	 * @return
	 * @throws DSOutOfServiceException
	 * @throws DSAccessException
	 */
	Set fetchAnnotations(List<Long> ids, long userID, boolean asChild)
	    throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			if (ids == null || ids.size() == 0) return null;
			IQuery service = getQueryService();
			Parameters param = new Parameters();
			param.addList("ids", ids);
			String sql =  "select ann from Annotation as ann "
				+ "left outer join ann.annotationLinks link "
                + "left outer join fetch ann.details.creationEvent "
                + "left outer join fetch ann.details.owner";
			
			if (asChild) sql += " where link.child.id in (:ids)";
			else sql += " where link.parent.id in (:ids)";

			if (userID >= 0) {
				sql += "and link.details.owner.id = :uid";
				param.addLong("uid", userID);
			}
			return PojoMapper.asDataObjects(service.findAllByQuery(sql, param));
		} catch (Exception e) {
			e.printStackTrace();
			handleException(e, "Cannot retrieve the annotations");
		}
		return null;
	}
	
	/**
	 * Handles the result of the search.
	 * 
	 * @param type
	 * @param r
	 * @param service
	 * @return See above.
	 */
	private Object handleSearchResult(Class type, Collection r, Search service)
	{
		//First get object of a given type.
		boolean hasNext = false;
		try {
			hasNext = service.hasNext();
		} catch (Exception e) {
			int size = 0;
			if (e instanceof InternalException) size = -1;
			else service.getBatchSize();
			return new Integer(size);
		}
		if (!hasNext) return r;
		List l = service.results();
		Iterator k = l.iterator();
		IObject object;
		long id;
		while (k.hasNext()) {
			object = (IObject) k.next();
			if (type.equals(object.getClass())) {
				id = object.getId();
				if (!r.contains(id)) 
					r.add(id); //Retrieve the object of a given type.
			}
		}
		return r;
	}
	
	/**
	 * Formats the elements of the passed array, the field to use.
	 * 
	 * @param terms
	 * @param field
	 * @return See above.
	 */
	private String[] formatText(String[] terms, String field)
	{
		if (terms == null || terms.length == 0) return null;
		String[] formatted = new String[terms.length];
		for (int i = 0; i < terms.length; i++) {
			formatted[i] = field+":"+terms[i];
		}
		return formatted;
	}
	
	/**
	 * 
	 * @param terms
	 * @param firstField
	 * @param sep
	 * @param secondField
	 * @return
	 */
	private String[] formatText(String[] terms, String firstField, String sep,
			String secondField)
	{
		if (terms == null || terms.length == 0) return null;
		String[] formatted = new String[terms.length];
		for (int i = 0; i < terms.length; i++) {
			formatted[i] = firstField+":"+terms[i]+" "+sep+" ";
			formatted[i] += secondField+":"+terms[i];
			System.err.println(formatted[i]);
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
	{
		List<Class> types = context.getTypes();
		List<Integer> scopes = context.getScope();
		if (types == null || types.size() == 0) return new HashMap();
		if (scopes == null || scopes.size() == 0) return new HashMap();
		isSessionAlive();
		Search service = getSearchService();
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
					service.onlyCreatedBetween(start, end);
					break;
				case SearchDataContext.MODIFICATION_TIME:
					service.onlyModifiedBetween(start, end);
					break;
				case SearchDataContext.ANNOTATION_TIME:
					service.onlyAnnotatedBetween(start, end);
					break;
						
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
				d = Details.create();
		        d.setOwner(exp.asExperimenter());
		        owners.add(d);
			}
		//}
		
		
		String[] some = prepareTextSearch(context.getSome(), service);
		String[] must = prepareTextSearch(context.getMust(), service);
		String[] none = prepareTextSearch(context.getNone(), service);
			
		List<Class> supportedTypes = new ArrayList<Class>();
		i = types.iterator();
		int index = 0;
		Class k;
		while (i.hasNext()) {
			k = (Class) i.next();
			supportedTypes.add(convertPojos(k));
			index++;
		}

		Set rType;
		Map<Integer, Object> results = new HashMap<Integer, Object>();
		Object size;
		Integer key;
		i = scopes.iterator();
		while (i.hasNext()) 
			results.put((Integer) i.next(), new HashSet());
		
		Iterator<Details> owner;
		i = scopes.iterator();
		String[] fSome = null, fMust = null, fNone = null;
		String[] fSomeSec = null, fMustSec = null, fNoneSec = null;
		service.onlyType(Image.class);
		while (i.hasNext()) {
			key = (Integer) i.next();
			rType = (HashSet) results.get(key);
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
					size = handleSearchResult(Image.class, rType, service);
					if (size instanceof Integer) 
						results.put(key, size);
					service.clearQueries();
					if (!(size instanceof Integer) && fSomeSec != null) {
						service.bySomeMustNone(fSomeSec, fMustSec, 
								fNoneSec);
						size = handleSearchResult(Image.class, rType, 
								service);
						if (size instanceof Integer) 
							results.put(key, size);
						service.clearQueries();
					}
				}
			}
		}
		service.close();
		return results;
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
			Search service = getSearchService();
			if (start != null || end != null)
				service.onlyAnnotatedBetween(start, end);
			if (exp != null) {
				Details d = Details.create();
		        d.setOwner(exp.asExperimenter());
				//service.onlyAnnotatedBy(d);
			}
			String[] t = prepareTextSearch(terms, service);
			
			
			Class k = convertPojos(annotationType);
			service.onlyType(k);
			Set rType = new HashSet();
			service.bySomeMustNone(t, null, null);
			Object size = handleSearchResult(k, rType, service);
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
			IQuery service = getQueryService();
			String table = getTableForClass(type);
			return PojoMapper.asDataObjects(service.findAllByQuery(
	                "from "+table+" as p where p.details.owner.id = :id", 
	                new Parameters().addId(userID)));
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
	 * @param tagID 			The class identifying the object to search for.
	 * @param images			Pass <code>true</code> to load the images,
	 * 							<code>false</code> otherwise.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	List loadTagAndImages(long tagID, boolean images)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQuery service = getQueryService();
			Parameters param = new Parameters();
			param.addLong("tagID", tagID);
			StringBuilder sb = new StringBuilder();
			
			sb.append("select ann from Annotation as ann ");
			sb.append("left outer join fetch ann.details.creationEvent ");
			sb.append("left outer join fetch ann.details.owner ");

			sb.append("where ann.id = :tagID");
			IObject object = service.findByQuery(sb.toString(), param);
			TagAnnotationData tag = 
						(TagAnnotationData) PojoMapper.asDataObject(object);
			//Condition
			if (images) {
				sb = new StringBuilder();
				sb.append("select img from Image as img ");
				sb.append("left outer join fetch "
	                    + "img.annotationLinksCountPerOwner img_a_c ");
				sb.append("left outer join fetch img.annotationLinks ail ");
				sb.append("left outer join fetch img.pixels as pix ");
	            sb.append("left outer join fetch pix.pixelsType as pt ");
	            sb.append("left outer join fetch pix.pixelsDimensions as pd ");
	            sb.append("where ail.child.id = :tagID");
	            Set imgs = PojoMapper.asDataObjects(
	            			service.findAllByQuery(sb.toString(), param));
	            //Retrieve the projects.
	            
	            
	            
	            
	            tag.setImages(imgs);
			}
			//sb.append("where ann.id = :tagID");
			//Test
			//
			List r = new ArrayList();
			r.add(tag);
			return r;
		} catch (Exception e) {
			handleException(e, "Cannot retrieve the annotations");
		}
		return null;
		//return service.results();
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
			IQuery service = getQueryService();
			Parameters param = new Parameters();
			param.addSet("ids", annotationIds);
			StringBuilder sb = new StringBuilder();
			sb = new StringBuilder();
			if (type.equals(ImageData.class)) {
				sb.append("select img from Image as img ");
				sb.append("left outer join fetch "
	                    + "img.annotationLinksCountPerOwner img_a_c ");
				sb.append("left outer join fetch img.annotationLinks ail ");
				sb.append("left outer join fetch img.pixels as pix ");
	            sb.append("left outer join fetch pix.pixelsType as pt ");
	            sb.append("left outer join fetch pix.pixelsDimensions as pd ");
	            sb.append("where ail.child.id in (:ids)");
	            if (ownerIds != null && ownerIds.size() > 0) {
	            	sb.append(" and img.details.owner.id in (:ownerIds)");
	            	param.addSet("ownerIds", ownerIds);
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
	 * @param tagID 			The class identifying the object to search for.
	 * @param images			Pass <code>true</code> to load the images,
	 * 							<code>false</code> otherwise.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	Collection loadTagSetsAndImages(long tagID, boolean images)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			String type = "ome.model.annotations.TagAnnotation";
			IQuery service = getQueryService();
			Parameters param = new Parameters();
			param.addLong("tagID", tagID);
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
			param = new Parameters();
			param.addLong("id", tag.getId());
			List r = service.findAllByQuery(sb.toString(), param);
			if (r != null) {
				Iterator j = r.iterator();
				ILink link;
				Set<TagAnnotationData> 
				    children = new HashSet<TagAnnotationData>();
				TagAnnotationData child;
				while (j.hasNext()) {
					link = (ILink) j.next();
					child = (TagAnnotationData) 
							PojoMapper.asDataObject(link.getChild());
					children.add(child);
					if (images) { //load images 
						sb = new StringBuilder();
						sb.append("select img from Image as img ");
						sb.append("left outer join fetch "
			                    + "img.annotationLinksCountPerOwner " +
			                    		"img_a_c ");
						sb.append("left outer join fetch img.annotationLinks " +
								"ail ");
						sb.append("left outer join fetch img.pixels as pix ");
			            sb.append("left outer join fetch pix.pixelsType as pt ");
			            sb.append("left outer join fetch pix.pixelsDimensions " +
			            		"as pd ");
			            sb.append("where ail.child.id = :id");
			            param = new Parameters();
						param.addLong("id", child.getId());
			            Set imgs = PojoMapper.asDataObjects(
			            		service.findAllByQuery(sb.toString(), param));
			            child.setImages(imgs);
					}
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
	Map getImagesTaggedCount(Set rootNodeIDs)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQuery service = getQueryService();
			Parameters param = new Parameters();
			
			StringBuilder sb = new StringBuilder();
			
			sb.append("select img from Image as img ");
			sb.append("left outer join fetch img.annotationLinks ail ");
            sb.append("where ail.child.id = :tagID");
			String query = sb.toString();
            Iterator i = rootNodeIDs.iterator();
            Long id;
            Map<Long, Integer> m = new HashMap<Long, Integer>();
            
            while (i.hasNext()) {
				id = (Long) i.next();
				param.addLong("tagID", id);
				m.put(id, service.findAllByQuery(query, param).size());
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
			IQuery service = getQueryService();
			Parameters param = new Parameters();
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
					param = new Parameters();
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
						ILink link;
						while (j.hasNext()) {
							link = (ILink) j.next();
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
			IQuery service = getQueryService();
			Parameters param = new Parameters();
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
				ILink link;
				IObject child;
				while (i.hasNext()) {
					link = (ILink) i.next();
					child = link.getChild();
					if (!((child instanceof TagAnnotation) || 
						(child instanceof UrlAnnotation)))  {
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
			getSessionService().getSession(ctx.getCurrentSessionUuid());
		} catch (Exception e) {
			dsFactory.sessionExpiredExit();
		}
	}
	
	//tmp
	List getFileAnnotations(Set<Long> originalFiles) 
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			IQuery service = getQueryService();
			Parameters param = new Parameters();
			param.addSet("ids", originalFiles);
			String sql =  "select link from Annotation as link ";
			sql += "where link.file.id in (:ids)";
			return service.findAllByQuery(sql, param);
			
		} catch (Exception e) {
			handleException(e, "Cannot remove the tag description.");
		}
		return new ArrayList();
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
			IDelete service = getDeleteService();
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
	 * Projects the specified set of pixels according to the projection's 
	 * parameters. Adds the created image to the passed dataset.
	 * 
	 * @param pixelsID The id of the pixels set.
	 * @param startZ   The first optical section.
	 * @param endZ     The last optical section.
	 * @param stepping The stepping used to project. Default is <code>1</code>.
	 * @param type     The projection's type.
	 * @param channels The channels to project.
	 * @param datasets The collection of datasets to add the image to.
	 * @param name     The name of the projected image.
	 * @return The newly created image.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
	 *                                  in.
	 * @throws DSAccessException        If an error occured while trying to 
	 *                                  retrieve data from OMEDS service.
	 */
	ImageData projectImage(long pixelsID, int startZ, int endZ, 
			int stepping, int type, List<Integer> channels, 
			List<DatasetData> datasets, String name)
		throws DSOutOfServiceException, DSAccessException
	{
		
		
		return null;
	}
	
	//TMP: 
	Set loadPlateWells(long plateID, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			List results = null;
			IQuery service = getQueryService();
			StringBuilder sb = new StringBuilder();
			Parameters param = new Parameters();
			param.addLong("plateID", plateID);
			
			sb.append("select well from Well as well ");
			sb.append("left outer join fetch well.wellSamples as ws ");
			sb.append("left outer join fetch ws.image as img ");
			
			sb.append("left outer join fetch img.pixels as pix ");
            sb.append("left outer join fetch pix.pixelsType as pt ");
            sb.append("left outer join fetch pix.pixelsDimensions as pd ");
			
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
			e.printStackTrace();
			handleException(e, "Cannot load plate");
		}
		return new HashSet();
	}
	
	Set loadScreenPlate(Class rootType, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		isSessionAlive();
		try {
			List results;
			IQuery service = getQueryService();
			StringBuilder sb = new StringBuilder();
			Parameters param = new Parameters();
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
				Set<Plate> plates = new HashSet<Plate>();
				Screen s;
				for (IObject o : plates) {
	                s = (Screen) o;
	                plates.addAll(s.linkedPlateList());
	            }
	            if (plates.size() > 0) {
	            	String sql = "select p from Plate p "
	                + "left outer join fetch p.annotationLinksCountPerOwner " +
	                "where p in (:list)";
	            	param.addSet("list", plates);
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
			e.printStackTrace();
			handleException(e, "Cannot load screen");
		}
		return new HashSet();
	}
	
}
