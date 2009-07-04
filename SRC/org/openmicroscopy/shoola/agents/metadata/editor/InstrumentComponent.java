/*
 * org.openmicroscopy.shoola.agents.metadata.editor.InstrumentComponent
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies

/** 
 * Describes the instrument used to capture the image.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class InstrumentComponent 
	extends JPanel
{

	/** Reference to the Model. */
	private EditorModel							model;
	
	/** Reference to the parent of this component. */
	private AcquisitionDataUI					parent;
	
	/** Initiliases the components. */
	private void initComponents()
	{
		
	}
	
	/** Builds and lays out the UI. */
	private void buildGUI()
	{
		removeAll();
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param parent Reference to the Parent. Mustn't be <code>null</code>.
	 * @param model	 Reference to the Model. Mustn't be <code>null</code>.
	 */
	InstrumentComponent(AcquisitionDataUI parent, EditorModel model)
	{
		if (model == null)
			throw new IllegalArgumentException("No model.");
		if (parent == null)
			throw new IllegalArgumentException("No parent.");
		this.parent = parent;
		this.model = model;
		initComponents();
	}
}
