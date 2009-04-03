/*
 * org.openmicroscopy.shoola.agents.imviewer.util.proj.ProjectionDialogControl 
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
package org.openmicroscopy.shoola.agents.imviewer.util.proj;




//Java imports
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.Map;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.util.ChannelButton;
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;

/** 
 * The projection controller.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta3
 */
class ProjectionDialogControl 
	implements ActionListener, PropertyChangeListener
{
	
	/** Action id to project the selection and view the result. */
	static final int PREVIEW = 1;
	
	/** Action id to project the whole image. */
	static final int PROJECT = 2;

	/** Reference to the model. */
	private ProjectionDialog model;

	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	ProjectionDialogControl(ProjectionDialog model)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.model = model;
	}
	
	/**
	 * Previews, projects the image or sets the interval to project/preview.
	 * @see ActionListener#actionPerformed(ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		int index = Integer.parseInt(e.getActionCommand());
		switch (index) {
			case PREVIEW:
				model.preview();
				break;
			case PROJECT:
				model.loadDatasets();
		}
	}

	/**
	 * Resets the preview image if a preview has already been done.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt)
	{
		String name = evt.getPropertyName();
		if (ChannelButton.CHANNEL_SELECTED_PROPERTY.equals(name)) {
			Map map = (Map) evt.getNewValue();
			if (map == null) return;
			if (map.size() != 1) return;
			Iterator i = map.keySet().iterator();
			Integer index;
			while (i.hasNext()) {
				index = (Integer) i.next();
				model.selectChannel(index.intValue(), 
						((Boolean) map.get(index)).booleanValue());
			}
		} else if (ImViewer.CHANNEL_ACTIVE_PROPERTY.equals(name)) {
			int index = (Integer) evt.getNewValue();
			model.selectChannel(index);
		} else if (ImViewer.CHANNEL_COLOR_CHANGE_PROPERTY.equals(name)) {
			Map m = (Map) evt.getNewValue();
			if (m == null || m.size() != 1) return;
			Iterator i = m.keySet().iterator();
			int index;
			while (i.hasNext()) {
				index = (Integer) i.next();
				model.setChannelColor(index, (Color) m.get(index));
			}
		}
	}
    
}
