/*
 * org.openmicroscopy.shoola.util.ui.filechooser.OMEFileChooser 
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
package org.openmicroscopy.shoola.util.ui.filechooser;


//Java imports
import java.io.File;

//Third-party libraries

//Application-internal dependencies

/** 
 * File chooser indicating location of the <code>Volumes</code> directory
 * depending on the platform.
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
public class OMEFileChooser 
	extends GenericFileChooser
{

	/** Identifies the windows platform. */
	private static final int WINDOWS = 0;
	
	/** Identifies the windows platform. */
	private static final int MAC = 1;
	
	/** Identifies the windows platform. */
	private static final int UNIX = 2;
	
	/** The index identifying the platform. */
	private int platform;
	
	/** The directory identifying the <code>Volumes</code> directory. */
	private File volumesDir;
	
	/** Sets the properties of the file chooser. */
	private void setProperties()
	{
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Mac OS")) platform = MAC;
		else if (osName.startsWith("Windows")) platform = WINDOWS;
		else platform = UNIX;
	}
	
	/** Creates a default instance. */
	public OMEFileChooser()
	{
		super();
		setProperties();
	}
	
	/**
	 * Returns the System volume depending on the platform.
	 * 
	 * @return See above.
	 */
	public File getVolumeFolder()
	{
		if (volumesDir != null) return volumesDir;
		switch (platform) {
			case UNIX:
			default:	
			case MAC:
				volumesDir = new File("/Volumes");
				break;
			case WINDOWS:
				volumesDir = 
					getFileSystemView().getParentDirectory(new File("C:\\"));
		}
		return volumesDir;
	}
	
}
