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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
	implements ActionListener
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
	 * Sets the datasets containing the image to project.
	 * 
	 * @param datasets The datasets containing the image.
	 */
	public void setContainers(Collection datasets)
	{
		ProjectionSavingDialog d = new ProjectionSavingDialog(model, datasets);
		UIUtilities.centerAndShow(d);
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
				break;
		}
	}
    
}
