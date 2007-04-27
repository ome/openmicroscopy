/*
 * measurement.component.ui.ChannelComboBox 
 *
  *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.measurement.ui.util;

//Java imports
import java.awt.Dimension;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.measurement.model.ChannelInfo;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ChannelComboBox
	extends JComboBox
{
	private DefaultComboBoxModel	listModel;
		
	public ChannelComboBox(ChannelInfo data)
	{ 
		ChannelComboBoxRenderer cellRenderer = new ChannelComboBoxRenderer();
		cellRenderer.setPreferredSize(new Dimension(200, 130));
		setRenderer(cellRenderer);
		setMaximumRowCount(4);
		listModel = new DefaultComboBoxModel();
		for( int cnt = 0 ; cnt < data.numChannels; cnt++)
			listModel.addElement(data.channels.get(cnt));
		setModel(listModel);
	}

	
	
	
}


