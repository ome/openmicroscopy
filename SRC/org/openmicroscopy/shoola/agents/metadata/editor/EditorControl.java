/*
 * org.openmicroscopy.shoola.agents.metadata.editor.EditorControl 
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;

//Third-party libraries

//Application-internal dependencies

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
class EditorControl
	implements PropertyChangeListener
{

	/** Bound property indicating that the save status has been modified. */
	static final String SAVE_PROPERTY = "save";
	
    /** Reference to the Model. */
    private Editor		model;
    
    /** Reference to the View. */
    private EditorUI	view;
    
	/**
     * Links this Controller to its Model and its View.
     * 
     * @param model	Reference to the Model. Mustn't be <code>null</code>.
     * @param view	Reference to the View. Mustn't be <code>null</code>.
     */
    void initialize(Editor model, EditorUI view)
    {
        if (view == null) throw new NullPointerException("No view.");
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        this.view = view;
    }

    /** Loads the thumbnails, forwards call the model. */
	void loadThumbnails() { model.loadThumbnails(); }

	/**
	 * Reacts to property change.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		String name = evt.getPropertyName();
		if (SAVE_PROPERTY.equals(name)) {
			view.setDataToSave(view.hasDataToSave());
		} else if (MetadataViewer.SAVE_DATA_PROPERTY.equals(name)) {
			view.saveData();
		} else if (MetadataViewer.CLEAR_SAVE_DATA_PROPERTY.equals(name)) {
			view.clearData();
		}
	}

}
