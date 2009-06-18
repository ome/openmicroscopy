/*
 * org.openmicroscopy.shoola.agents.metadata.editor.InstrumentUI 
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
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;


//Third-party libraries

//Application-internal dependencies
import org.jdesktop.swingx.JXTaskPane;

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
class InstrumentUI 
	extends JPanel
{

	/** Collection of detectors. */
	private List<JXTaskPane> 	detectors;
	
	/** Collection of light sources. */
	private List<JXTaskPane> 	lightSources;
	 
	/** The microscope used. */
	private JXTaskPane			microscope;
	
	/** Collection of objectives. */
	private List<JXTaskPane> 	objectives; 
	
	/** Collection of filters. */
	private List<JXTaskPane> 	filters; 
	
	/** Collection of dichroic. */
	private List<JXTaskPane> 	dichroics; 
	
	/** Initializes the components composing the display. */
	private void initComponents()
	{
		detectors = new ArrayList<JXTaskPane>();
		objectives = new ArrayList<JXTaskPane>();
		lightSources = new ArrayList<JXTaskPane>();
		filters = new ArrayList<JXTaskPane>();
		dichroics = new ArrayList<JXTaskPane>();
	}
	
}
