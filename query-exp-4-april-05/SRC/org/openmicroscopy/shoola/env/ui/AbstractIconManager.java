/*
 * org.openmicroscopy.shoola.env.ui.AbstractIconManager
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2004 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package org.openmicroscopy.shoola.env.ui;

//Java imports
import java.awt.Image;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.IconFactory;
import org.openmicroscopy.shoola.env.config.Registry;

/** 
 * Factors out all functionality which is common to a concrete icon manager.
 * This class is a tiny wrapper around an {@link IconFactory} to manage the
 * retrieval of icons from a graphics bundle &#151; usually files within a
 * given directory in a <i>jar</i> file.
 * <p>A concrete subclass has to provide:
 * <ul>
 *  <li>The {@link Registry} to use to look up the {@link IconFactory},
 * 		along with the lookup name.</li>
 *  <li>An array containing the names of the icon files to manage.</li>
 * </ul>
 * The names of the icon files are relative to the directory the
 * {@link IconFactory} is working against.  The <code>getIcon</code> methods
 * provide the means to retrieve an icon by specifying either an id or the
 * name of the icon file.  The id is the index of the file name in the array
 * of file names provided by subclasses.</p>
 * <p>Typically, a concrete icon manager subclass follows this pattern:
 *  <ul>
 *   <li>Define a <code>private static</code> array containing the names of the
 * 	 icon files.  This array is then passed to this class' constructor.</li>
 *   <li>For each file name, define a <code>public static final</code> id.
 * 	 This is the index of the file name in the file names array and is
 *   used by client classes to retrieve the icon via the
 *   {@link #getIcon(int) getIcon} method.</li>
 *   <li>Define a singleton instance which is accessed through a
 *   <code>public static getInstance</code> method to retrieve the singleton.
 *   This method gets a {@link Registry} from the clients so that the singleton
 *   can be created.</li>
 *  </ul>
 * </p>
 * <p>Finally, as the <i>OME</i> icon is virtually needed for every title-bar,
 * a public class methods are exposed to retrieve it &#151; so agents needn't
 * include that icon in their graphics bundle.</p>
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
public abstract class AbstractIconManager 
{
	
	/** 
	 * The <i>OME</i> logo to be used for title-bars.
	 * We cache it as this icon is used in basically every top-level UI.
	 */
	private static final Icon	OME_ICON = createIcon("graphx/OME16.png");
	
	
	/** 
	 * Utility factory method to create an icon from a file.
	 *
	 * @param path    The path of the icon file relative to this class.
	 * @return  An instance of {@link javax.swing.Icon Icon} or
	 * 			<code>null</code> if the path was invalid.
	 */
	protected static Icon createIcon(String path)
	{
		URL location = IconManager.class.getResource(path);
		ImageIcon icon = null;
		if (location != null)	icon = new ImageIcon(location);
		return icon;
	}
	
	/**
	 * Returns the <i>OME</i> logo to be used for title-bars.
	 * 
	 * @return See above.
	 */
	public static Image getOMEImageIcon()
	{
		//This type cast is OK, see implementation of createIcon.
		return ((ImageIcon) OME_ICON).getImage();
	}
	
	/**
	 * Returns the <i>OME</i> logo.
	 * 
	 * @return See above.
	 */
	public static Icon getOMEIcon()
	{
		return OME_ICON;
	}
	
	
	/**
	 * The factory retrieved from the configuration held by {@link #registry}.
	 * It can instantiate any icon whose file is contained in the graphics
	 * bundle specified by the configuration file that was used to build the
	 * {@link #registry}.
	 */
	private IconFactory 	factory;
	
	/** The names of the icon files. */
	private String[]		iconFiles;
	
	/** The registry to use to lookup the {@link #factory}. */
	private Registry 		registry;
	
	
	/**
	 * Creates a new instance.
	 * Subclasses are forced to use this constructor.
	 * 
	 * @param registry	The registry to use to lookup the icon factory.
	 * @param lookupName The name to use for the lookup.
	 * @param iconFiles The names of the icon files.
	 */
	protected AbstractIconManager(Registry registry, 
									String lookupName, String[] iconFiles) 
	{
		if (registry == null) throw new NullPointerException("No registry.");
		if (iconFiles == null || iconFiles.length == 0) 
			throw new IllegalArgumentException("No icon files.");
		Object f = registry.lookup(lookupName);
		if (f == null || !(f instanceof IconFactory))
			throw new IllegalArgumentException("Wrong lookup name: "+
												lookupName+".");
		this.factory = (IconFactory) f;
		this.iconFiles = iconFiles;
		this.registry = registry;
	}
	
	/** 
	 * Retrieves the icon specified by <code>name</code>.
	 * If the icon can't be retrieved, then this method will log the error and
	 * return <code>null</code>.
	 *
	 * @param name    Must be one a valid icon file name within the directory
	 * 					used by the {@link IconFactory} instance specified via
	 * 					this class' constructor.
	 * @return  An {@link Icon} object created from the image file.  The return
	 * 			value will be <code>null</code> if the file couldn't be found
	 * 			or an image icon couldn't be created from that file.
	 */ 
	public Icon getIcon(String name)
	{
		Icon icon = factory.getIcon(name);
		if (icon == null) {
			StringBuffer buf = new StringBuffer("Failed to retrieve icon: ");
			buf.append("<classpath>");
			buf.append(factory.getResourcePathname(name));
			buf.append(".");
			registry.getLogger().error(this, buf.toString());
		}
		return icon;
	}
	
	/** 
	 * Retrieves the icon specified by <code>id</code>.
	 * If the icon can't be retrieved, then this method will log the error and
	 * return <code>null</code>.
	 *
	 * @param id	The index of the file name in the array of file names 
	 * 				specified to this class' constructor.
	 * @return  An {@link Icon} object created from the image file.  The return
	 * 			value will be <code>null</code> if the file couldn't be found
	 * 			or an image icon couldn't be created from that file.
	 */ 
	public Icon getIcon(int id)
	{
		if (id < 0 || iconFiles.length <= id) {
			registry.getLogger().error(this, "Icon id out of range: "+id+".");
			return null;
		}
		return getIcon(iconFiles[id]);
	}

}
