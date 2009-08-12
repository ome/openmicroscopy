/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.StatusLabel
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 *     This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.data.util;


//Java imports
import javax.swing.JLabel;

import org.openmicroscopy.shoola.util.ui.UIUtilities;

//Third-party libraries

//Application-internal dependencies
import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.util.Actions;

/**
 * Component displaying the status of a specific import.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author    Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *     <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class StatusLabel 
	extends JLabel
	implements IObserver
{

	/** The number of planes. This value is used only for some file formats. */
	private int maxPlanes;
	
	/** Creates a new instance. */
	public StatusLabel()
	{
		setForeground(UIUtilities.LIGHT_GREY);
		maxPlanes = 0;
	}

	/** 
	 * Sets the status of the import.
	 * 
	 * @param value The value to set.
	 */
	public void setStatus(String value)
	{
		if (value == null) value = "";
		setText(value);
	}
	
	/**
	 * Displays the status of an on-going import.
	 * @see IObserver#update(IObservable, Object, Object[])
	 */
	public void update(IObservable arg0, Object message, Object[] responses) 
	{
		if (Actions.LOADING_IMAGE.equals(message)) {
			setText("prepping");
		} else if (Actions.LOADED_IMAGE.equals(message)) {
			setText("analyzing");
		} else if (Actions.IMPORT_DONE.equals(message)) {
			setText("");
		} else if (Actions.IMPORT_ARCHIVING.equals(message)) {
			setText("archiving");
		} else if (Actions.DATASET_STORED.equals(message)) {
			if (responses != null && responses.length >= 7)
				maxPlanes = (Integer) responses[6];
		} else if (Actions.IMPORT_STEP.equals(message)) {
			if (responses != null && responses.length >= 3) {
				int value = (Integer) responses[1];
				if (value <= maxPlanes) {
					String text;
					int count = (Integer) responses[2];
					int series = (Integer) responses[0];
					if (count > 1)
						text = (series+1)+"/"+count+": "+value+"/"+maxPlanes;
					else
						text = value+"/"+maxPlanes;
					setText(text);
				}
			}
		} 
	}
	
}
