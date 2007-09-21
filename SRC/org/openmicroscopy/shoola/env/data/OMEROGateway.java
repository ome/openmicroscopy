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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJBAccessException;
import javax.ejb.EJBException;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.util.PojoMapper;
import org.openmicroscopy.shoola.env.rnd.RenderingServiceException;
import ome.api.IAdmin;
import ome.api.IPojos;
import ome.api.IQuery;
import ome.api.IRenderingSettings;
import ome.api.IRepositoryInfo;
import ome.api.IUpdate;
import ome.api.RawFileStore;
import ome.api.RawPixelsStore;
import ome.api.ThumbnailStore;
import ome.conditions.ApiUsageException;
import ome.conditions.ValidationException;
import ome.model.IObject;
import ome.model.containers.Category;
import ome.model.containers.CategoryGroup;
import ome.model.containers.Dataset;
import ome.model.containers.Project;
import ome.model.core.Image;
import ome.model.core.OriginalFile;
import ome.model.core.Pixels;
import ome.model.core.PixelsDimensions;
import ome.model.meta.Experimenter;
import ome.model.meta.ExperimenterGroup;
import ome.parameters.Parameters;
import ome.parameters.QueryParameter;
import ome.system.Login;
import ome.system.Server;
import ome.system.ServiceFactory;
import ome.util.builders.PojoOptions;
import omeis.providers.re.RenderingEngine;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.GroupData;
import pojos.ImageData;
import pojos.ProjectData;

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
	private static final int		INC = 256000;
	
	/** 
	 * The maximum number of thumbnails retrieved before restarting the
	 * thumbnails service.
	 */
	private static final int		MAX_RETRIEVAL = 100;
	
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
  
  /** The raw pixels store. */
  private RawPixelsStore			pixelsStore;
  
  /**
   * Tells whether we're currently connected and logged into <i>OMERO</i>.
   */
  private boolean                 connected;
  
  /** 
   * Used whenever a broken link is detected to get the Login Service and
   * try reestabishing a valid link to <i>OMERO</i>. 
   */
  private DataServicesFactory     dsFactory;
  
  /** Server instance to log in. */
  private Server                  server;
  
  /** The port to use in order to connect. */
  private int                     port;
  
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
  	t.printStackTrace();
  	if (cause instanceof SecurityException) {
  		String s = "Cannot access data for security reasons \n"; 
  		throw new DSAccessException(s+message+"\n\n"+
  							printErrorText((Exception) cause));
  	} else if (cause instanceof EJBAccessException) {
  		String s = "Cannot access data for security reasons \n"; 
  		throw new DSAccessException(s+message+"\n\n"+
  				printErrorText((Exception) cause));
  	} else if (cause instanceof ApiUsageException) {
  		String s = "Cannot access data, specified parameters not valid \n"; 
  		throw new DSAccessException(s+message+"\n\n"+
  				printErrorText((Exception) cause));
  	} else if (cause instanceof ValidationException) {
  		String s = "Cannot access data, specified parameters not valid \n"; 
  		throw new DSAccessException(s+message+"\n\n"+
  					printErrorText((Exception) cause));
  	} else 
  		throw new DSOutOfServiceException(message+"\n\n"+
  					printErrorText((Exception) cause));
  }
  
  /**
   * Utility method to print the error message
   * 
   * @param e The exception to handle.
   * @return  See above.
   */
  private String printErrorText(Exception e) 
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
      return table;
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
   * Converts the specified POJO into a String corresponding model.
   *  
   * @param nodeType The POJO class.
   * @return The corresponding string.
   */
  private String convertPojosToString(Class nodeType)
  {
      if (nodeType.equals(ProjectData.class))
          return Project.class.getName();
      else if (nodeType.equals(DatasetData.class))
          return Dataset.class.getName();
      else if (nodeType.equals(ImageData.class))
          return Image.class.getName();
      else if (nodeType.equals(CategoryData.class)) 
          return Category.class.getName();
      else if (nodeType.equals(CategoryGroupData.class))
          return CategoryGroup.class.getName();
      throw new IllegalArgumentException("NodeType not supported");
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
   * Converts the specified POJO into the corresponding model.
   *  
   * @param nodeType The POJO class.
   * @return The corresponding class.
   */
  private Class convertPojos(Class nodeType)
  {
      if (nodeType.equals(ProjectData.class)) return Project.class;
      else if (nodeType.equals(DatasetData.class)) return Dataset.class;
      else if (nodeType.equals(ImageData.class)) return Image.class;
      else if (nodeType.equals(CategoryData.class)) return Category.class;
      else if (nodeType.equals(CategoryGroupData.class))
          return CategoryGroup.class;
      else throw new IllegalArgumentException("NodeType not supported");
  }
  
  /**
   * Returns the {@link IRenderingSettings} service.
   * 
   * @return See above.
   */
  public IRenderingSettings getRenderingSettingsService()
  {
  	return entry.getRenderingSettingsService();
  }
  
  /**
   * Returns the {@link IRepositoryInfo} service.
   * 
   * @return See above.
   */
  public IRepositoryInfo getRepositoryService()
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
  private IAdmin getAdmin() { return entry.getAdminService(); }
  
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
      if (thumbnailService == null) 
          thumbnailService = entry.createThumbnailService();
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
  	//if (fileStore == null) fileStore = entry.createRawFileStore();
  	//return fileStore;
  	return entry.createRawFileStore();
  }
  
  /**
   * Returns the {@link RenderingEngine Rendering service}.
   * 
   * @return See above.
   */
  private RenderingEngine getRenderingService()
  {
      return entry.createRenderingEngine();
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
  	return ("system".equals(n) || "user".equals(n) || "default".equals(n));
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
      entry = new ServiceFactory(server, new Login(userName, password));
      if (thumbnailService != null) thumbnailService.close();
      thumbnailService = null;
      thumbRetrieval = 0;
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
                  						printErrorText(e));
      }
  }
  
  /**
   * Tries to connect to <i>OMERO</i> and log in by using the supplied
   * credentials.
   * 
   * @param userName  The user name to be used for login.
   * @param password  The password to be used for login.
   * @param hostName  The name of the server.
   * @return The user's details.
   * @throws DSOutOfServiceException If the connection can't be established
   *                                  or the credentials are invalid.
   * @see #getUserDetails(String)
   */
  ExperimenterData login(String userName, String password, String hostName)
      throws DSOutOfServiceException
  {
      try {
          server = new Server(hostName, port);
          entry = new ServiceFactory(server, new Login(userName, password)); 
          connected = true;
          return getUserDetails(userName);
      } catch (Exception e) {
          connected = false;
          String s = "Can't connect to OMERO. OMERO info not valid.\n\n";
          s += printErrorText(e);
          throw new DSOutOfServiceException(s, e);  
      } 
  }
  
  /** Log out. */
  void logout()
  {
      //TODO
      connected = false;
      if (thumbnailService != null) thumbnailService.close();
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
   * @param nodeType      The type of the rootNodes. It can either be
   *                      <code>Dataset</code> or <code>Image</code>.
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
   * don’t end with the specified leaves, note that in that case because of 
   * the mutually exclusive constraint the categories which don’t contain a
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
      try {
          IPojos service = getPojosService();
          return PojoMapper.asDataObjects(service.findCGCPaths(imgIDs, 
                                  mapAlgorithmToString(algorithm),
                                  options));
      } catch (Throwable t) {
          handleException(t, "Cannot find CGC paths.");
      }
      new Long(1);
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
      try {
          IPojos service = getPojosService();
          return PojoMapper.asDataObjects(
                 service.getImages(convertPojos(nodeType), nodeIDs, options));
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
      try {
          IPojos service = getPojosService();
          String p = convertProperty(rootNodeType, property);
          if (p == null) return null;
          return PojoMapper.asDataObjects(service.getCollectionCount(
                  convertPojosToString(rootNodeType), p, rootNodeIDs, 
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
      try {
          //IPojos service = getIPojosService();
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
      try {
          IQuery service = getQueryService();
          Pixels pixs = service.get(Pixels.class, pixelsID);
          return service.get(PixelsDimensions.class,
                  pixs.getPixelsDimensions().getId().longValue());
      } catch (Throwable t) {
          handleException(t, "Cannot retrieve the dimension of "+
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
  List getChannelsData(long pixelsID)
      throws DSOutOfServiceException, DSAccessException
  {
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
          return pixs.getChannels();
      } catch (Throwable t) {
          handleException(t, "Cannot retrieve the channelsData for "+
                              "the pixels set "+pixelsID);
      }
      return null;
   }
  
  /**
   * Retrieves the thumbnail for the passed set of pixels.
   * 
   * @param pixelsID  The id of the pixels set the thumbnail is for.
   * @param sizeX     The size of the thumbnail along the X-axis.
   * @param sizeY     The size of the thumbnail along the Y-axis.
   * @return See above.
   * @throws RenderingServiceException If an error occured while trying to 
   *              retrieve data from the service. 
   * @throws DSOutOfServiceException If the connection is broken.
   */
  synchronized byte[] getThumbnail(long pixelsID, int sizeX, int sizeY)
      throws RenderingServiceException, DSOutOfServiceException
  {
      try {
      	ThumbnailStore service = getThumbService();
      	
      	needDefault(pixelsID, null);
          return service.getThumbnailDirect(new Integer(sizeX), 
                                              new Integer(sizeY));
      } catch (Throwable t) {
      	if (thumbnailService != null) thumbnailService.close();
      	thumbnailService = null;
      	if (t instanceof EJBException || 
      			t.getCause() instanceof IllegalStateException) {
      		throw new DSOutOfServiceException(
      				"Thumbnail service null for pixelsID: "+pixelsID+"\n\n"+
      				printErrorText((Exception) t));
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
      try {
      	ThumbnailStore service = getThumbService();
      	needDefault(pixelsID, null);
      	return service.getThumbnailByLongestSideDirect(maxLength);
      } catch (Throwable t) {
      	if (thumbnailService != null) thumbnailService.close();
      	thumbnailService = null;
      	if (t instanceof EJBException || 
      			t.getCause() instanceof IllegalStateException) {
      		throw new DSOutOfServiceException(
      				"Thumbnail service null for pixelsID: "+pixelsID+"\n\n"+
      				printErrorText((Exception) t));
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
      try {
          String table = getTableForLink(parent.getClass());
          if (table == null) return null;
          String sql = "select link from "+table+" as link where " +
                  "link.parent.id = :parentID and link.child.id in " +
                  "(:childIDs)";
          IQuery service = getQueryService();
          Parameters param = new Parameters();
          param.addLong("parentID", parent.getId());
          param.addList("childIDs", children);
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
      try {
          String table = getTableForLink(parentClass);
          if (table == null) return null;
          String sql = "select link from "+table+" as link where " +
                  "link.details.owner.id = :userID and link.child.id in " +
                  "(:childIDs)";
          IQuery service = getQueryService();
          Parameters param = new Parameters();
          param.addSet("childIDs", children);
          param.addLong("userID", userID);
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
      try {
          String table = getTableForLink(parentClass);
          if (table == null) return null;
          String sql = "select link from "+table+" as link where " +
                  "link.child.id = :childID and " +
                  "link.details.owner.id = :userID";
          IQuery service = getQueryService();
          Parameters param = new Parameters();
          param.addLong("childID", childID);
          param.addLong("userID", userID);
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
   * Retrieves the available experimenter groups.
   * 
   * @return See above.
   * @throws DSOutOfServiceException If the connection is broken, or logged in
   * @throws DSAccessException If an error occured while trying to 
   * retrieve data from OMERO service. 
   */
  Map<GroupData, Set> getAvailableGroups()
  	throws DSOutOfServiceException, DSAccessException
  {
  	try {
			IAdmin service = getAdmin();
			List<ExperimenterGroup> groups = service.lookupGroups();
			Iterator i = groups.iterator();
			ExperimenterGroup group;
			Experimenter[] experimenters;
			Map<GroupData, Set> pojos = new HashMap<GroupData, Set>();
			DataObject pojoGroup;
			while (i.hasNext()) {
				group = (ExperimenterGroup) i.next();
				if (!isSystemGroup(group)) {
					pojoGroup = PojoMapper.asDataObject(group);
					experimenters = service.containedExperimenters(
							group.getId());
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
  	throws DSOutOfServiceException, DSAccessException
  {
  	
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
  	while (i.hasNext()) {
			of = (OriginalFile) i.next();
			store.setFileId(of.getId()); 
			f = new File(path+of.getName());
			//TODO: review that code
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
				throw new DSAccessException("Cannot create the file", e);
			}
		}
  	result.put(files.size(), notDownloaded);
      return result;
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
		IAdmin service = getAdmin();
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
		IAdmin service = getAdmin();
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
	byte[] getPlane(long pixelsID, int z, int t, int c)
		throws DSOutOfServiceException, DSAccessException
	{
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
		IRepositoryInfo service = getRepositoryService();
		try {
			return service.getUsedSpaceInKilobytes();
		} catch (Throwable e) {
			handleException(e, "Cannot retrieve the free space");
		}
		return -1;
	}
	
	/**
	 * Returns a collection of images imported before the specified time
	 * by the specified user.
	 * 
	 * @param time		The reference time.
	 * @param userID	The user's id.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
   *                                  in.
   * @throws DSAccessException        If an error occured while trying to 
   *                                  retrieve data from OMEDS service.
	 */
	List getImagesBefore(Timestamp time, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			String sql = "from Image as i where i.details.owner.id = :userID " +
					"and i.details.creationEvent.time < :time";
			IQuery service = getQueryService();
			Parameters param = new Parameters();
			param.addLong("userID", userID);
			param.add(new QueryParameter("time", Timestamp.class, time));
			return service.findAllByQuery(sql, param);
		} catch (Throwable e) {
			handleException(e, "Cannot retrieve the images before " +
									"the passed time.");
		}
		return null;
	}
	
	/**
	 * Returns a collection of images imported after the specified time
	 * by the specified user.
	 * 
	 * @param time		The reference time.
	 * @param userID	The user's id.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
   *                                  in.
   * @throws DSAccessException        If an error occured while trying to 
   *                                  retrieve data from OMEDS service.
	 */
	List getImagesAfter(Timestamp time, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			String sql = "from Image as i where i.details.owner.id = :userID " +
					"and i.details.creationEvent.time > :time";
			IQuery service = getQueryService();
			Parameters param = new Parameters();
			param.addLong("userID", userID);
			param.add(new QueryParameter("time", Timestamp.class, time));
			return service.findAllByQuery(sql, param);
		} catch (Throwable e) {
			handleException(e, "Cannot retrieve the images after " +
			"the passed time.");
		}
		return null;
	}
	
	/**
	 * Returns a collection of images imported after the specified time
	 * by the specified user.
	 * 
	 * @param lowerTime	The reference time.
	 * @param time		The reference time.
	 * @param userID	The user's id.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
   *                                  in.
   * @throws DSAccessException        If an error occured while trying to 
   *                                  retrieve data from OMEDS service.
	 */
	List getImagesDuring(Timestamp lowerTime, Timestamp time, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			String sql = "from Image as i where i.details.owner.id = :userID " +
					"and i.details.creationEvent.time < :time and "+
					"i.details.creationEvent.time > :lowerTime";
			IQuery service = getQueryService();
			Parameters param = new Parameters();
			param.addLong("userID", userID);
			param.add(new QueryParameter("time", Timestamp.class, time));
			param.add(new QueryParameter("lowerTime", Timestamp.class, 
						lowerTime));
			return service.findAllByQuery(sql, param);
		} catch (Throwable e) {
			handleException(e, "Cannot retrieve the images imported during " +
								"the selected period.");
		}
		return null;
	}
	
	/**
	 * Returns a collection of images imported after the specified time
	 * by the specified user.
	 * 
	 * @param lowerTime	The reference time.
	 * @param time		The reference time.
	 * @param userID	The user's id.
	 * @return See above.
	 * @throws DSOutOfServiceException  If the connection is broken, or logged
   *                                  in.
   * @throws DSAccessException        If an error occured while trying to 
   *                                  retrieve data from OMEDS service.
	 */
	List getImagesFilledDuring(Timestamp lowerTime, Timestamp time, long userID)
		throws DSOutOfServiceException, DSAccessException
	{
		try {
			String sql = "select i from Image as i left outer join fetch " +
                  "i.details.creationEvent as c "+
                  "where i.details.owner.id = :userID " +
					"and i.details.creationEvent.time < :time and "+
					"i.details.creationEvent.time > :lowerTime";
			IQuery service = getQueryService();
			Parameters param = new Parameters();
			param.addLong("userID", userID);
			param.add(new QueryParameter("time", Timestamp.class, time));
			param.add(new QueryParameter("lowerTime", Timestamp.class, 
						lowerTime));
			return service.findAllByQuery(sql, param);
		} catch (Throwable e) {
			handleException(e, "Cannot retrieve the images imported during " +
								"the selected period.");
		}
		return null;
	}

	/**
   * Applies the rendering settings associated to the passed pixels set 
   * to the images contained in the specified datasets or categories
   * if the rootType is <code>DatasetData</code> or <code>CategoryData</code>.
   * Applies the settings to the passed images if the type is 
   * <code>ImageData</code>.
   * 
   * @param userID		The id of the user currently logged in.
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
	Map pasteRenderingSettings(long userID, long pixelsID, 
								Class rootNodeType, Set nodes) 
		throws DSOutOfServiceException, DSAccessException
	{
		Set<Long> success = new HashSet<Long>();
		Set<Long> failure = new HashSet<Long>();
		try {
			IRenderingSettings service = getRenderingSettingsService();
			
			long rndID = service.getRenderingSettings(pixelsID).getId();
			
			Class klass = convertPojos(rootNodeType);
			Iterator i = nodes.iterator();
			long id;
			boolean b = false;
			if (klass.equals(Image.class)) {
				while (i.hasNext()) {
					id = (Long) i.next();
					b = service.applySettingsToImage(rndID, id);
					if (b) success.add(id);
					else failure.add(id);
				}
			} else if (klass.equals(Dataset.class)) {
				Map m;
				List l;
				Iterator k;
				while (i.hasNext()) {
					id = (Long) i.next();
					m = service.applySettingsToDataset(rndID, id);
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
			} else if (klass.equals(Dataset.class)) {
				Map m;
				List l;
				Iterator k;
				while (i.hasNext()) {
					id = (Long) i.next();
					m = service.applySettingsToCategory(rndID, id);
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
  
}
