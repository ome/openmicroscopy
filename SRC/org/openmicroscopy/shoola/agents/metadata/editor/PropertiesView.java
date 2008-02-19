/*
 * org.openmicroscopy.shoola.agents.util.editor.PropertiesView 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.metadata.editor;


//Java imports
import java.awt.Dimension;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.ImageData;
import pojos.PermissionData;
import pojos.ProjectData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class PropertiesView 
	extends JPanel
{

	/** Indicates that the view is to create a new object. */
	public static final int CREATION = 0;

	/** Indicates that the view is to edit an existing object. */
	public static final int EDITION = 1;

	 /**
     * A reduced size for the invisible components used to separate widgets
     * vertically.
     */
    static final Dimension      SMALL_V_SPACER_SIZE = new Dimension(1, 6);
    
	private static final String DETAILS = "Details";

	private static final String INFO = "Info";

	/** The object of this view is for. */
	private Object				refObject;
	
	/** One of the constants defined by this class. */
	private int					type;
	
	/**
	 * Checks if the type is valid.
	 * 
	 * @param t The value to check.
	 */
	private void checkType(int t)
	{
		switch (t) {
			case CREATION:
			case EDITION:
				break;
			default:
				throw new IllegalArgumentException("Type not supported.");
		}
	}
	/**
	 * Creates a new instance.
	 * 
	 * @param refObject	The object of reference.
	 * @param type		One of the constants defined by this class.
	 */
	public PropertiesView(Object refObject, int type)
	{
		checkType(type);
		this.type = type;
		this.refObject = refObject;
	}

	/**
	 * Returns the name of the object if any.
	 * 
	 * @return See above.
	 */
	String getRefObjectName() 
	{
		if (refObject instanceof ImageData)
			return ((ImageData) refObject).getName();
		else if (refObject instanceof DatasetData)
			return ((DatasetData) refObject).getName();
		else if (refObject instanceof ProjectData)
		return ((ProjectData) refObject).getName();
		return "";
	}
	
	/**
	 * Returns the description of the object if any.
	 * 
	 * @return See above.
	 */
	String getRefObjectDescription() 
	{
		if (refObject instanceof ImageData)
			return ((ImageData) refObject).getDescription();
		else if (refObject instanceof DatasetData)
			return ((DatasetData) refObject).getDescription();
		else if (refObject instanceof ProjectData)
		return ((ProjectData) refObject).getDescription();
		return "";
	}
	
	/**
	 * Returns the type of the view.
	 * 
	 * @return See above.
	 */
	int getType() { return type; }

	/**
     * Returns <code>true</code> if the edited object is writable,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
	boolean isWritable()
	{
		return false;
	}

	/**
     * Returns <code>true</code> if the edited object is readable,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
	boolean isReadable()
	{
		return false;
	}

	/**
	 * Returns the object hosted by this view.
	 * 
	 * @return See above.
	 */
	Object getRefObject() { return refObject; }
	
    /**
     * Returns the information on the owner of the ref object. 
     * 
     * @return See above.
     */
	ExperimenterData getRefObjectOwner()
	{
		if (refObject == null) return null;
		if (refObject instanceof DataObject) {
			ExperimenterData exp =  ((DataObject) refObject).getOwner();
	    	if (exp == null) return null;
	    	if (exp.isLoaded()) return exp;
		}
		return null; 
	}

	/**
	 * Returns the id of the object hosted by this view.
	 * 
	 * @return See above.
	 */
	long getRefObjectID()
	{
		if (refObject instanceof DataObject)
			return ((DataObject) refObject).getId();
		return -1;
	}

    /**
     * Returns <code>true</code> if the permissions can be shown,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
	boolean isPermissionsShowable()
	{
		if (refObject == null) return false;
    	return ((refObject instanceof ProjectData) || 
    			(refObject instanceof DatasetData));
	}

	/**
     * Returns the permission of the currently edited object. 
     * Returns <code>null</code> if no permission associated. (This should never
     * happens).
     * 
     * @return See above.
     */
	PermissionData getRefObjectPermissions()
	{
		if (refObject instanceof DataObject)
			return ((DataObject) refObject).getPermissions();
		return null;
	}

}
