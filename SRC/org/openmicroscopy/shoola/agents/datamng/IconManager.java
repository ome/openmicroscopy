/*
 * org.openmicroscopy.shoola.agents.datamng.IconManager
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

package org.openmicroscopy.shoola.agents.datamng;


//Java imports
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.IconFactory;
import org.openmicroscopy.shoola.env.config.Registry;

/** 
 * 
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
public class IconManager
{
	/** Contains icon objects to be retrieved through the icon IDs. */
	private Icon[]				icons;

	/** ID of the OME logo icon. */
	public static final int     OME = 0;   
		
	/** ID of the project icon. */
	public static final int     PROJECT = 1;
		
	/** ID of the dataset icon. */
	public static final int     DATASET = 2;
		
	/** ID of the image icon. */
	public static final int     IMAGE = 3;
	
	/** ID of the icon of the save to DB button. */
	public static final int     SAVE_DB = 4;
		
	/** ID of the icon of the reload from DB button. */
	public static final int     RELOAD_DB = 5;
  
	/** ID of the information icon. */
	public static final int     INFO = 6;
	
	/** ID of the properties icon used by the popup menu. */
	public static final int		PROPERTIES = 7;
	
	/** ID of the viewer icon used by the popup menu. */
	public static final int		VIEWER = 8;

	/** ID of the browser icon used by the popup menu. */
	public static final int		BROWSER = 9;

	/** ID of the refresh icon used by the popup menu. */
	public static final int		REFRESH = 10;

	/** 
	 * The maximum ID used for the icon IDs.
	 * Allows to correctly build arrays for direct indexing. 
	 */
	private static int          MAX_ID = 10;
	
	/** Paths of the icon files. */
	private static String[]     relPaths = new String[MAX_ID+1];
		
	static {
		relPaths[OME] = "OME16.png";
		relPaths[PROJECT] = "project16.png";
		relPaths[DATASET] = "dataset16.png";
		relPaths[IMAGE] = "image16.png";
		relPaths[SAVE_DB] = "save_DB30.png";
		relPaths[RELOAD_DB] = "reload_DB30.png";
		relPaths[INFO] = "information16.png";
		relPaths[PROPERTIES] = "properties16.png";
		relPaths[VIEWER] = "viewer16.png";
		relPaths[BROWSER] = "browser-small.png";
		relPaths[REFRESH] = "refresh16.png";
	}
	
	/** The sole instance that provides. */
	private static IconManager	singleton;
	
	/** Returns the <code>IconManager</code> object. */
	public static IconManager getInstance(Registry registry)
	{
		if (singleton == null) {
			try {	
				singleton = new IconManager(registry);
			} catch (Exception e) {
				throw new RuntimeException("Can't create the IconManager", e);
			}
		}
		
		return singleton;
	}
	
	private IconFactory 	factory;
	
	/**
	 * Creates a new instance and configures the parameters.
	 * 
	 * @param registry	Reference to the registry.
	 */
	private IconManager(Registry registry)
	{
		factory = (IconFactory) registry.lookup("/resources/icons/Factory");
		icons = new Icon[MAX_ID+1];
	}

	/** 
	 * Retrieves the icon specified by the icon <code>ID</code>.
	 *
	 * @param   ID    Must be one of the IDs defined by this class.
	 * @return  The specified icon. The retuned value is meant to be READ-ONLY.
	 */    
	public Icon getIcon(int ID)
	{
		if (icons[ID] == null) icons[ID] = factory.getIcon(relPaths[ID]);
		return icons[ID];
	}
	
}
