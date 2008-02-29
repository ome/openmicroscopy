/*
 * org.openmicroscopy.shoola.agents.metadata.editor.EditorComponent 
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
import java.awt.image.BufferedImage;
import java.util.Map;

import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.browser.Browser;
import org.openmicroscopy.shoola.env.data.util.StructuredDataResults;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;

import pojos.ImageData;

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
class EditorComponent 
	extends AbstractComponent
	implements Editor
{

	/** The Model sub-component. */
	private EditorModel		model;
	
	/** The Control sub-component. */
	private EditorControl	controller;
	
	/** The View sub-component. */
	private EditorUI		view;

	/**
	 * Creates a new instance.
	 * The {@link #initialize() initialize} method should be called straigh 
	 * after to complete the MVC set up.
	 * 
	 * @param model The Model sub-component. Mustn't be <code>null</code>.
	 */
	EditorComponent(EditorModel model)
	{
		if (model == null) throw new NullPointerException("No model.");
		this.model = model;
		view = new EditorUI();
		controller = new EditorControl();
	}
	
	/** Links up the MVC triad. */
	void initialize()
	{
		controller.initialize(this, view);
		view.initialize(model, controller);
	}
	
	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#getUI()
	 */
	public JComponent getUI() { return view; }

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setStructuredDataResults(StructuredDataResults)
	 */
	public void setStructuredDataResults(StructuredDataResults result)
	{
		if (result == null) return;
		model.setStructuredDataResults(result);
		view.layoutUI();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#setRootObject(Object)
	 */
	public void setRootObject(Object refObject)
	{
		if (refObject == null)
			throw new IllegalArgumentException("Root object not valid.");
		model.setRootObject(refObject);
		view.setRootObject();
	}

	/** 
	 * Implemented as specified by the {@link Browser} interface.
	 * @see Editor#loadThumbnails()
	 */
	public void loadThumbnails()
	{
		if (!model.isThumbnailsLoaded())
			model.loadThumbnails();
		
	}

	public void setThumbnails(Map<Long, BufferedImage> thumbnails, 
							long imageID)
	{
		Object ref = model.getRefObject();
		if (ref instanceof ImageData) {
			if (((ImageData) ref).getId() == imageID) {
				model.setThumbnails(thumbnails);
				view.setThumbnails();
			}
		}
		
	}
	
}
