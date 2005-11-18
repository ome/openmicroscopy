/*
 * org.openmicroscopy.shoola.env.data.OMEROGateway
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.data;






//Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import ome.adapters.pojos.Model2PojosMapper;
import ome.api.Pojos;
import ome.client.ServiceFactory;
import ome.model.Category;
import ome.model.CategoryGroup;
import ome.model.Dataset;
import ome.model.Image;
import ome.model.Project;
import pojos.CategoryData;
import pojos.CategoryGroupData;
import pojos.DatasetData;
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
    
    /**
     * The entry point provided by the connection library to access the various
     * <i>OMERO</i> services.
     */
    private ServiceFactory          entry;
    
    /**
     * Tells whether we're currently connected and logged into <i>OMEDS</i>.
     */
    private boolean                 connected;
    
    /** 
     * Used whenever a broken link is detected to get the Login Service and
     * try reestabishing a valid link to <i>OMEDS</i>. 
     */
    private DataServicesFactory     dsFactory;
    
    /** Reference to the pojos mapper. */
    private Model2PojosMapper       mapper;
    
    /**
     * Helper method to handle exceptions thrown by the connection library.
     * Methods in this class are required to fill in a meaningful context
     * message.
     * This method is not supposed to be used in this class' constructor or in
     * the login/logout methods.
     *  
     * @param e     The exception.
     * @param contextMessage    The context message.    
     * @throws DSOutOfServiceException  A connection problem.
     * @throws DSAccessException    A server-side error.
     */
    private void handleException(Exception e, String contextMessage) 
        throws DSOutOfServiceException, DSAccessException
    {
        //TODO: handle errors
        e.printStackTrace();
        throw new DSAccessException(contextMessage, e);
        //if (e instanceof AuthenticationE)
        //TODO
        /*
        if (e instanceof IllegalArgumentException) {
            //TMP, thrown by PixelsFactory.
            throw new DSAccessException(contextMessage, e);
        } else {
            //This should never be reached.  If so, there's a yet another
            //bug in OME-JAVA.
            logout();  //Will set connected=false.
            throw new RuntimeException("Internal error.", e);
        }
        */
    }
    
    /**
     * Utility method to print the contents of a list in a string.
     * 
     * @param l     The list.
     * @return  See above.
     */
    private String printList(List l) 
    {
        StringBuffer buf = new StringBuffer();
        if (l == null)  buf.append("<null> list");
        else if (l.size() == 0)     buf.append("empty list");
        else {
            Iterator i = l.iterator();
            while (i.hasNext()) {
                buf.append(i.next());
                buf.append(" ");
            }
        }
        return buf.toString();
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
     * Returns the {@link Pojos} service.
     * 
     * @return See above.
     */
    private Pojos getPojosService() { return entry.getPojosService(); }
    
    /**
     * Creates a new instance.
     * 
     * @param hostName The name of the server.
     * @param port The value of the port.
     * @param dsFactory A reference to the factory. Used whenever a broken link
     *                  is detected to get the Login Service and try 
     *                  reestabishing a valid link to <i>OMEDS</i>.
     *                  Mustn't be <code>null</code>.
     */
    OMEROGateway(String hostName, int port, DataServicesFactory dsFactory)
    {
        if (dsFactory == null) 
            throw new IllegalArgumentException("No Data service factory.");
        System.setProperty("server.host", ""+hostName);
        System.setProperty("server.port", ""+port);
        this.dsFactory = dsFactory;
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
     * Tries to connect to <i>OMERO</i> and log in by using the supplied
     * credentials.
     * 
     * @param userName  The user name to be used for login.
     * @param password  The password to be used for login.
     * @throws DSOutOfServiceException If the connection can't be established
     *                                  or the credentials are invalid.
     */
    void login(String userName, String password)
        throws DSOutOfServiceException
    {
        System.setProperty("omeds.user", userName);
        System.setProperty("omeds.pass", password);
        try {
            entry = new ServiceFactory();
            mapper = new Model2PojosMapper();
            connected = true;
        } catch (Exception e) {
            connected = false;
            String s = "Can't connect to OMERO. OMERO info not valid.";
            throw new DSOutOfServiceException(s, e);  
        } 
    }
    
    void logout()
    {
        connected = false;
    }
    
    /**
     * Retrieves hierarchy trees rooted by a given node.
     * i.e. the requested node as root and all of its descendants.
     * The annotation for the current user is also linked to the object.
     * Annotations are currently possible only for Image and Dataset.
     * Wraps the call to the 
     * {@link Pojos#loadContainerHierarchy(Class, Set, Map)}
     * and maps the result calling
     * {@link Model2PojosMapper#map(java.util.Collection)}.
     * 
     * @param rootNodeType top-most type which will be searched for 
     *          Can be <code>Project</code> or <code>CategoryGroup</code>. 
     *          Mustn't be <code>null</code>.
     * @param rootNodeIDs A set of the IDs of top-most containers.
     * @param options Options to retrieve the data.
     * @return  A set of hierarchy trees.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    Set loadContainerHierarchy(Class rootNodeType, Set rootNodeIDs, Map options)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            Pojos service = getPojosService();
            return (Set) mapper.map(service.loadContainerHierarchy(
                            convertPojos(rootNodeType), rootNodeIDs, options));
        } catch (Exception e) {
            handleException(e, "Cannot load hierarchy for "+rootNodeType);
        }
        return new HashSet();
    }
    
    /**
     * Retrieves hierarchy trees in various hierarchies that
     * contain the specified Images.
     * The annotation for the current user is also linked to the object.
     * Annotations are currently possible only for Image and Dataset.
     * Wraps the call to the 
     * {@link Pojos#findContainerHierarchies(Class, Set, Map)}
     * and maps the result calling
     * {@link Model2PojosMapper#map(java.util.Collection)}.
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
     * retrieve data from OMEDS service. 
     */
    Set findContainerHierarchy(Class rootNodeType, Set leavesIDs, Map options)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            Pojos service = getPojosService();
            return (Set) mapper.map(service.findContainerHierarchies(
                            convertPojos(rootNodeType), leavesIDs, options));
        } catch (Exception e) {
            handleException(e, "Cannot find hierarchy for "+rootNodeType);
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
     * Wraps the call to the {@link Pojos#findAnnotations(Class, Set, Map)}
     * and maps the result calling
     * {@link Model2PojosMapper#map(java.util.Collection)}.
     * 
     * @param nodeType  The type of the rootNodes. It can either be
     *                  <code>Dataset</code> or <code>Image</code>.
     *                  Mustn't be <code>null</code>. 
     * @param nodeIDs   TheIds of the objects of type <code>rootNodeType</code>.
     *                  Mustn't be <code>null</code>.
     * @param options   Options to retrieve the data.
     * @return A map whose key is rootNodeID and value the <code>Set</code> of
     *         all annotations for that node or <code>null</code>.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    Map findAnnotations(Class nodeType, Set nodeIDs, Map options)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            Pojos service = getPojosService();
            return mapper.map(service.findAnnotations(convertPojos(nodeType),
                            nodeIDs, options));
        } catch (Exception e) {
            handleException(e, "Cannot find annotations for "+nodeType);
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
     * Wraps the call to the {@link Pojos#findCGCPaths(Set, int, Map)}
     * and maps the result calling
     * {@link Model2PojosMapper#map(java.util.Collection)}.
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
     * retrieve data from OMEDS service. 
     */
    Set findCGCPaths(Set imgIDs, int algorithm, Map options)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            Pojos service = getPojosService();
            return (Set) mapper.map(service.findCGCPaths(imgIDs, algorithm,
                                    options));
        } catch (Exception e) {
            handleException(e, "Cannot find CGC paths");
        }
        return new HashSet();
    }
    
    /**
     * Retrieves the images contained in containers specified by the 
     * node type.
     * Wraps the call to the {@link Pojos#getImages(Class, Set, Map)}
     * and maps the result calling
     * {@link Model2PojosMapper#map(java.util.Collection)}.
     * 
     * @param nodeType  The type of container. Can be either Project, Dataset,
     *                  CategoryGroup, Category.
     * @param nodeIDs   Set of containers' IDS.
     * @param options   Options to retrieve the data.
     * @return A <code>Set</code> of retrieved images.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    Set getImages(Class nodeType, Set nodeIDs, Map options)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            Pojos service = getPojosService();
            return (Set) mapper.map(service.getImages(convertPojos(nodeType),
                                    nodeIDs, options));
        } catch (Exception e) {
            handleException(e, "Cannot find images for "+nodeType);
        }
        return new HashSet();
    }
    
    /**
     * Retrieves the images imported by the current user.
     * Wraps the call to the {@link Pojos#getUserImages(Map)}
     * and maps the result calling
     * {@link Model2PojosMapper#map(java.util.Collection)}.
     * 
     * @param options   Options to retrieve the data.
     * @return A <code>Set</code> of retrieved images.
     * @throws DSOutOfServiceException If the connection is broken, or logged in
     * @throws DSAccessException If an error occured while trying to 
     * retrieve data from OMEDS service. 
     */
    Set getUserImages(Map options)
        throws DSOutOfServiceException, DSAccessException
    {
        try {
            Pojos service = getPojosService();
            return (Set) mapper.map(service.getUserImages(options));
        } catch (Exception e) {
            handleException(e, "Cannot find user images ");
        }
        return new HashSet();
    }
    
}
