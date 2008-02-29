/*
 * org.openmicroscopy.shoola.agents.metadata.editor.TextualAnnotationsUI 
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
import javax.swing.border.Border;


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.border.TitledLineBorder;


/** 
 * UI component displaying the textual annotations.
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
class TextualAnnotationsUI 
	extends AnnotationUI
{
    
	/** The title associated to this component. */
	static final String TITLE = "Annotations";
	
	/**
	 * Creates a new instance.
	 * 
	 * @param model Reference to the model. Mustn't be <code>null</code>.
	 */
	TextualAnnotationsUI(EditorModel model)
	{
		super(model);
	}
	
	/**
	 * Overridden to lay out the annotations.
	 * @see AnnotationUI#buildUI()
	 */
	protected void buildUI()
	{
		removeAll();
		int n = model.getTextualAnnotationCount();
		title = TITLE+LEFT+n+RIGHT;
		Border border = new TitledLineBorder(title, getBackground());
		setBorder(border);
		getCollapseComponent().setBorder(border);
	}
	
	/**
	 * Overridden to set the title of the component.
	 * @see AnnotationUI#getComponentTitle()
	 */
	protected String getComponentTitle() { return TITLE; }
	
}
