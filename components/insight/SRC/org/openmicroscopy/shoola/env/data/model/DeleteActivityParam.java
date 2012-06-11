/*
 * org.openmicroscopy.shoola.env.data.model.DeleteActivityParam 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.model;


//Java imports
import java.util.Arrays;
import java.util.List;
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies

/** 
 * Parameters required to delete collection of objects.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class DeleteActivityParam
{

    /** The icon associated to the parameters. */
    private Icon			icon;
    
    /** The icon associated to the parameters in case of failure. */
    private Icon			failureIcon;
    
    /** The collection of objects to delete. */
    private List<DeletableObject> objects;
    
    /** The collection of nodes to remove when the delete is completed. */
    private List<Object> 		nodes;
    
    /** The id of the image, only useful when deleting ROI. */
    private long 		imageID;
    
    /** Indicates to register in UI.*/
    private boolean		uiRegister;
    
    /**
     * Creates a new instance.
     * 
     * @param icon The icon associated to the parameters.
     * @param objects The collection of objects to delete.
     */
    public DeleteActivityParam(Icon icon, List<DeletableObject> objects)
    {
    	if (objects == null || objects.size() == 0)
    		throw new IllegalArgumentException("No Objects to delete.");
    	this.icon = icon;
    	this.objects = objects;
    	imageID = -1;
    	uiRegister = true;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param icon The icon associated to the parameters.
     * @param object The object to delete.
     */
    public DeleteActivityParam(Icon icon, DeletableObject object)
    {
    	if (object == null)
    		throw new IllegalArgumentException("No Object to delete.");
    	this.icon = icon;
    	objects = Arrays.asList(object);
    	imageID = -1;
    	uiRegister = true;
    }
    
    /**
     * Sets the image's identifier.
     * 
     * @param imageID The identifier of the image.
     */
    public void setImageID(long imageID) { this.imageID = imageID; }
    
    /**
     * Returns the identifier of the image.
     * 
     * @return See above.
     */
    public long getImageID() { return imageID; }
    
    /**
     * Sets the failure icon.
     * 
     * @param failureIcon The icon to set.
     */
    public void setFailureIcon(Icon failureIcon)
    { 
    	this.failureIcon = failureIcon;
    }
    
    /**
     * Returns the failure icon.
     * 
     * @return See above.
     */
    public Icon getFailureIcon() { return failureIcon; }
    
    /**
     * Sets the collection of nodes.
     * 
     * @param nodes The collection to set.
     */
    public void setNodes(List<Object> nodes) { this.nodes = nodes; }
    
    /**
     * Returns the collection of nodes.
     * 
     * @return See above.
     */
    public List<Object> getNodes() { return nodes; }
    
	/**
	 * Returns the icon if set or <code>null</code>.
	 * 
	 * @return See above.
	 */
	public Icon getIcon() { return icon; }
	
	/**
	 * Returns the collection of objects to delete.
	 * 
	 * @return See above.
	 */
	public List<DeletableObject> getObjects() { return objects; }
	
    /**
     * Sets to <code>true</code> if the component does not need to be
     * registered, <code>false</code> otherwise.
     * 
     * @param uiRegister Pass <code>true</code> to register, <code>false</code>
     * 					 otherwise.
     */
    public void setUIRegister(boolean uiRegister)
    { 
    	this.uiRegister = uiRegister; 
    }
    
    /**
     * Returns <code>true</code> if the component does not need to be
     * registered, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isUIRegister() { return uiRegister; }
	
}
