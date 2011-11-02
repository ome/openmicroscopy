/*
 * org.openmicroscopy.shoola.MainIJPlugin 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola;


//Java imports

//Third-party libraries
import ij.plugin.PlugIn;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;

/** 
 * Starts the application as an <code>ImageJ</code> plugin.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class MainIJPlugin 
	implements PlugIn
{

	public void run(String args)
	{
		String homeDir = "";
		String configFile = null;
		
		if (args != null) {
			String[] values = args.split(" ");
			if (values.length > 0) configFile = values[0];
			if (values.length > 1) homeDir = values[1];
		}
		Container.startupInPluginMode(homeDir, configFile, LookupNames.IMAGE_J);
	}

}
