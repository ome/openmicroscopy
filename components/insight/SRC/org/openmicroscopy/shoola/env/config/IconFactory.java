/*
 * org.openmicroscopy.shoola.env.config.IconFactory
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

package org.openmicroscopy.shoola.env.config;

//Java imports
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;

//Third-party libraries

//Application-internal dependencies

/** 
 * Factory to build {@link Icon}s from image files within a given directory.
 * An entry of type <i>"icons"</i> in a configuration file will be turned into
 * an instance of this class.  The content of the <i>location</i> tag is used
 * for locating the directory where the image files are.  This directory can
 * be anywhere within the application code and is specified in terms of a 
 * fully qualified package name.
 * <p>For example, the icons used by the code within the container are located
 * in <code>org/openmicroscopy/shoola/env/ui/graphx</code>, so the
 * corresponding location has to be specified as
 * <code>org.openmicroscopy.shoola.env.ui.graphx</code> in the container's 
 * configuration file &#151; note that the contents of this directory will be
 * part of a <i>jar</i> file.</p>
 * <p>So after application start-up, the container's registry will contain an
 * <code>IconFactory</code> object which is configured to read any image file
 * (file type must be one supported by <i>Swing</i>) within the specified
 * directory: <br>
 * <code>
 * IconFactory factory = (IconFactory)<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;registry.lookup("/resources/icons/DefaultFactory");
 * <br>Icon logo = factory.getIcon("OME16.png");
 * </code> 
 * </p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class IconFactory
{

	/** 
	 * Points to the directory specified by the <i>location</i> tag.
	 * The path is relative to the application classpath.
	 */
	private String	location;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param location	The FQN of the package containing the icons.
	 */
	IconFactory(String location)
	{
		location = (location == null ? "" : location);
		this.location = "/"+location.replace('.', '/');
	}
	
	/**
	 * Returns the pathname of the specified file.
	 * The returned pathname is relative to the application classpath.
	 *  
	 * @param iconFileName	The file name.
	 * @return See above.
	 */
	public String getResourcePathname(String iconFileName)
	{
		return location+"/"+iconFileName;
	}

	/** 
	 * Creates an {@link Icon} from the specified file.
	 * 
	 * @param name	The file name.  Must be a valid name within the location
	 * 				specified in the configuration file.
	 * @return	    An {@link Icon} object created from the image file. 
     *              The return value will be <code>null</code> if the file 
     *              couldn't be found or an image icon couldn't be created 
     *              from that file.
	 */
	public Icon getIcon(String name) { return getImageIcon(name); }
	
	/** 
	 * Creates an {@link ImageIcon} from the specified file.
	 * 
	 * @param name	The file name.  Must be a valid name within the location
	 * 				specified in the configuration file.
	 * @return	    An {@link ImageIcon} object created from the image file. The 
	 * 			    return value will be <code>null</code> if the file couldn't 
     *              be found or an image icon couldn't be created from 
     *              that file.
	 */
	public ImageIcon getImageIcon(String name)
	{
		ImageIcon icon = null;
		try {
			String path = getResourcePathname(name);
			URL url = IconFactory.class.getResource(path);
			icon = new ImageIcon(url);
		} catch (Exception e) {} 
		return icon;
	}
	
	/** 
	 * Creates an {@link URL} from the specified file.
	 * 
	 * @param name	The file name.  Must be a valid name within the location
	 * 				specified in the configuration file.
	 * @return	    See above.
	 */
	public URL getImageURL(String name)
	{
		try {
			String path = getResourcePathname(name);
			return IconFactory.class.getResource(path);
		} catch (Exception e) {} 
		return null;
	}

}
