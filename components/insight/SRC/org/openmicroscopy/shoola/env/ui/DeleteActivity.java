/*
 * org.openmicroscopy.shoola.env.ui.DeleteActivity 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.ui;


//Java imports
import java.util.Iterator;
import java.util.List;

import javax.swing.Icon;


import omero.cmd.GraphException;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.ProcessReport;
import org.openmicroscopy.shoola.env.data.model.DeletableObject;
import org.openmicroscopy.shoola.env.data.model.DeleteActivityParam;

/** 
 * Activity to delete data.
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
public class DeleteActivity 
	extends ActivityComponent
{

	/** Indicates that the delete has been completed.*/
	public static final String DELETE_COMPLETE = "Delete completed";
	
	/** The description of the activity. */
	private static final String		DESCRIPTION_START = "Deleting ";
	
	/** The description of the activity when finished. */
	private static final String		DEFAULT_TYPE = "Object";
	
	/** The description of the activity when finished. */
	private static final String		DESCRIPTION_END = " deleted";
	
	/** The description of the activity when error occurred. */
	private static final String		DESCRIPTION_ERROR = "Unable to delete";
	
	/** The description of the activity when cancelled. */
	private static final String		DESCRIPTION_CANCEL = "Deletion cancelled";
	
	/** The parameters hosting information about the figure to make. */
    private DeleteActivityParam	parameters;
    
    /** 
     * Returns the text displayed for the delete.
     * 
     * @return See above.
     */
    private String getDeleteType()
    {
    	List<DeletableObject> objects = parameters.getObjects();
		DeletableObject o = objects.get(0);
		String s = "";
		if (objects.size() > 1) s = "s";
		String value = o.getType();
		if (value == null || value.trim().length() == 0)
			return DEFAULT_TYPE+s;
		return value+s;
    }
    
    /**
     * Creates a message indicating the objects to delete.
     * 
     * @return See above.
     */
    private String createMessage()
    {
    	List<DeletableObject> objects = parameters.getObjects();
    	if (objects.size() == 1) return objects.get(0).getMessage();
    	StringBuffer buffer = new StringBuffer();
    	DeletableObject o;
    	Iterator<DeletableObject> i = objects.iterator();
    	int index = 0;
    	int n = 1;
    	while (i.hasNext()) {
			o = i.next();
			if (index == n) break;
			buffer.append(o.getMessage());
			if (index < (n-1)) buffer.append(", ");
			index++;
		}
    	if (objects.size() > n)
    		buffer.append(" and "+(objects.size()-n)+" more");
    	return buffer.toString();
    }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer		The viewer this data loader is for.
     *               		Mustn't be <code>null</code>.
     * @param registry		Convenience reference for subclasses.
     * @param parameters  	The parameters used to delete.
     */
	public DeleteActivity(UserNotifier viewer, Registry registry,
			DeleteActivityParam parameters)
	{
		super(viewer, registry, null);
		if (parameters == null)
			throw new IllegalArgumentException("Parameters not valid.");
		this.parameters = parameters;
		initialize(DESCRIPTION_START+getDeleteType(), parameters.getIcon());
		messageLabel.setText(createMessage());
	}
	
	/**
	 * Returns the collection of nodes to remove.
	 * 
	 * @return See above.
	 */
	public List<Object> getNodes() { return parameters.getNodes(); }
	
    /**
     * Returns the identifier of the image.
     * 
     * @return See above.
     */
	public long getImageID() { return parameters.getImageID(); }
	
	/**
	 * Creates a concrete loader.
	 * @see ActivityComponent#createLoader()
	 */
	protected UserNotifierLoader createLoader()
	{
		List<DeletableObject> objects = parameters.getObjects();
		loader = new DataObjectRemover(viewer, registry, objects, this);
		return loader;
	}

	/**
	 * Modifies the text of the component. 
	 * @see ActivityComponent#notifyActivityEnd()
	 */
	protected void notifyActivityEnd()
	{
		if (result instanceof ProcessReport) {
			type.setText(DESCRIPTION_ERROR);
			GraphException ex = ((ProcessReport) result).getGraphException();
			if (ex != null) 
				messageLabel.setText(messageLabel.getText()+" - "+ex.message);
			notifyActivityError();
		} else {
			type.setText(DESCRIPTION_END);
			Icon icon = parameters.getIcon();
			if (icon != null) iconLabel.setIcon(icon);
		}
	}
	
	/**
	 * Modifies the text of the component. 
	 * @see ActivityComponent#notifyActivityCancelled()
	 */
	protected void notifyActivityCancelled()
	{
		type.setText(DESCRIPTION_CANCEL);
	}

	/**
	 * Modifies the text of the component. 
	 * @see ActivityComponent#notifyActivityError()
	 */
	protected void notifyActivityError()
	{
		Icon icon = parameters.getFailureIcon();
		if (icon != null) iconLabel.setIcon(icon);
	}

}
