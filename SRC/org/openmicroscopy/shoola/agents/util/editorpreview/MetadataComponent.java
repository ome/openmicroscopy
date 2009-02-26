/*
 * org.openmicroscopy.shoola.agents.metadata.editor.AcquisitionComponent 
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
package org.openmicroscopy.shoola.agents.util.editorpreview;


//Java imports
import javax.swing.JComponent;
import javax.swing.JLabel;

//Third-party libraries

//Application-internal dependencies

/** 
 * Utility class where UI component about metadata are stored.
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
public class MetadataComponent
{

	/** Component displaying the name of the field. */
	private JLabel 		label;
	
	/** Component displaying the value of the field. */
	private JComponent 	area;
	
	/** Flag indicating if the field has been set or not. */
	private boolean		setField;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param label	Component displaying the name of the field.
	 * @param area	Component displaying the value of the field.
	 */
	public MetadataComponent(JLabel label, JComponent area)
	{
		this.label = label;
		this.area = area;
		setField = true;
		label.setLabelFor(area);
	}
	
	/**
	 * Sets to <code>true</code> if the value has been set, <code>false</code> 
	 * otherwise.
	 * 
	 * @param setField The value to set.
	 */
	public void setSetField(boolean setField) { this.setField = setField; }
	
	/**
	 * Returns <code>true</code> if the field has been set, 
	 * <code>false</code> otherwise.
	 * 
	 * @return See above.
	 */
	public boolean isSetField() { return setField; }
	
	/**
	 * Returns the component displaying the name of the field.
	 * 
	 * @return See above.
	 */
	public JLabel getLabel() { return label; }
	
	/**
	 * Returns the component displaying the value of the field.
	 * 
	 * @return See above.
	 */
	public JComponent getArea() { return area; }
	
}
