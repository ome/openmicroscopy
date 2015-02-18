/*
 * org.openmicroscopy.shoola.env.ui.DataTransferActivity 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
import javax.swing.Icon;


import omero.cmd.GraphException;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.ProcessReport;
import org.openmicroscopy.shoola.env.data.model.TransferableActivityParam;

/** 
 * Activity to move data between groups.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class DataTransferActivity 
	extends ActivityComponent
{

	/** The description of the activity. */
	private static final String		DESCRIPTION_START = "Moving data ";
		
	/** The description of the activity when finished. */
	private static final String		DESCRIPTION_END = "Move completed";
	
	/** The description of the activity when error occurred. */
	private static final String		DESCRIPTION_ERROR =
		"Unable to transfer data";
	
	/** The description of the activity when cancelled. */
	private static final String		DESCRIPTION_CANCEL = "Move cancelled";
	
	/** The parameters hosting information about the transfer. */
    private TransferableActivityParam parameters;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer		The viewer this data loader is for.
     *               		Mustn't be <code>null</code>.
     * @param registry		Convenience reference for subclasses.
     * @param parameters  	The parameters used to delete.
     */
	public DataTransferActivity(UserNotifier viewer, Registry registry,
			TransferableActivityParam parameters)
	{
		super(viewer, registry, null);
		if (parameters == null)
			throw new IllegalArgumentException("Parameters not valid.");
		this.parameters = parameters;
		String name = parameters.getGroupName();
		StringBuffer buffer = new StringBuffer();
		buffer.append(DESCRIPTION_START);
		if (name != null && name.length() > 0) {
			buffer.append("to ");
			buffer.append(name);
		}
		initialize(buffer.toString(), parameters.getIcon());
		int n = parameters.getNumber();
		String end = "";
		if (n > 1) end = "s";
		messageLabel.setText("move "+n+" object"+end);
	}
	
	/**
	 * Creates a concrete loader.
	 * @see ActivityComponent#createLoader()
	 */
	protected UserNotifierLoader createLoader()
	{
		loader = new DataObjectTransfer(viewer, registry,
				parameters.getObject(), this);
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
