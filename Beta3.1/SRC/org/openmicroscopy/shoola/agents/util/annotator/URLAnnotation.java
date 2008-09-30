/*
 * org.openmicroscopy.shoola.agents.util.annotator.URLAnnotation 
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
package org.openmicroscopy.shoola.agents.util.annotator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

//Java imports

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
public class URLAnnotation 
	extends JPanel
{

	/** Text field hosting the url to enter. */
	private JTextField 		urlArea;
	
	private List<JButton> 	removeButtons;
	
	private List			urls;
	
	private void initialize()
	{
		if (urls != null) {
			removeButtons = new ArrayList<JButton>(urls.size());
			Iterator i = urls.iterator();
			while (i.hasNext()) {
				//type element = (//type) i.next();
				
			}
		}
	}
	
	private void buildGUI()
	{
		
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param urls Collection of existing urls.
	 */
	public URLAnnotation(List urls)
	{
		this.urls = urls;
		initialize(); 
	}
	
}
