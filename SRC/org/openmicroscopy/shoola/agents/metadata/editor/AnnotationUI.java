/*
 * org.openmicroscopy.shoola.agents.metadata.editor.AnnotationUI 
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
import java.util.List;
import javax.swing.JPanel;

//Third-party libraries

//Application-internal dependencies
import pojos.AnnotationData;

/** 
 * Super class that all UI components displaying annotation should extend.
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
abstract class AnnotationUI 
	extends JPanel
{

	/** Bound property indicating to delete the annotation. */
	static final String	DELETE_ANNOTATION_PROPERTY = "deleteAnnotation";
	
	/** The symbol inserted before the number of annotations. */
	static final String	LEFT = "[";
	
	/** The symbol inserted after the number of annotations. */
	static final String	RIGHT = "]";

	/** The length of a column. */
	static final int	COLUMN_WIDTH = 200;
	
	/** The length of a column. */
	static final int	DEFAULT_WIDTH = COLUMN_WIDTH+100;
	
	/** The default height of a component displaying text. */
	static final int	DEFAULT_HEIGHT = 100;
	
	/** Default text if no data entered. */
	static final String DEFAULT_TEXT = "None";

	
	/** Reference to the model. */
	protected EditorModel 	model;
	
	/** The title associated to the component. */
	protected String		title;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	AnnotationUI(EditorModel model)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		this.model = model;
	}

	/** Builds and lays out the UI. */
	protected abstract void buildUI();
	
	/** 
	 * Returns the title of the component.
	 * 
	 * @return See above.
	 */
	protected abstract String getComponentTitle();
	
	/**
	 * Returns <code>true</code> if there is some annotation to save,
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	protected abstract boolean hasDataToSave();
	
	/**
	 * Returns the collection of annotations to remove, or <code>null</code>
	 * if any.
	 * 
	 * @return See above.
	 */
	protected abstract List<AnnotationData> getAnnotationToRemove();
	
	/**
	 * Returns the collection of annotations to save, or <code>null</code>
	 * if any.
	 * 
	 * @return See above.
	 */
	protected abstract List<AnnotationData> getAnnotationToSave();
	
	/** Lays out components and clears the display. */
	protected abstract void clearDisplay();
	
	/** Clears the data to save. */
	protected abstract void clearData();
	
	/** Sets the title of the component. */
	protected abstract void setComponentTitle();

}
