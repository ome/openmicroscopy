/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

import java.awt.Color;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.AnnotationData;

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

	/** Color indicating that a value needs to be highlighted to the user. */
	static final Color	WARNING = UIUtilities.REQUIRED_FIELDS_COLOR;
	
	/** The border of a field that can be edited. */
	static final Border EDIT_BORDER = BorderFactory.createLineBorder(
			Color.LIGHT_GRAY);
	
	/** The border of a field that can be edited. */
	static final Border EDIT_BORDER_BLACK = BorderFactory.createLineBorder(
			Color.BLACK);
	
	/** Bound property indicating to remove the annotation from the view. */
	static final String	REMOVE_ANNOTATION_PROPERTY = "removeAnnotation";
	
	/** Bound property indicating to remove the annotation from the view. */
	static final String	EDIT_TAG_PROPERTY = "editTag";
	
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

	/** 
	 * The default text for enumeration if not set the cardinality is 0 or 1.
	 */
	static final String	NO_SET_TEXT = "Not Set";
	
	/** The default text to display manufactured details. */
	static final String	MANUFACTURER = "Manufacturer";
	
	/** The Details or the manufacturer.  */
	static final String	MANUFACTURER_DETAILS = "Details";
	
	/** The Details or the manufacturer.  */
	static final String	MANUFACTURER_TOOLTIP = "Display the details " +
			"of the manufacturer.";
	
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
		setBackground(UIUtilities.BACKGROUND_COLOR);
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
	protected abstract List<Object> getAnnotationToRemove();
	
	/**
	 * Returns the collection of annotations to save, or <code>null</code>
	 * if any.
	 * 
	 * @return See above.
	 */
	protected abstract List<AnnotationData> getAnnotationToSave();
	
	/** Lays out components and clears the display. */
	protected abstract void clearDisplay();
	
	/** 
	 * Clears the data to save.
	 * 
	 * @param oldObject The previously selected object
	 */
	protected abstract void clearData(Object oldObject);
	
	/** Sets the title of the component. */
	protected abstract void setComponentTitle();

}
