/*
 * org.openmicroscopy.shoola.agents.viewer.IconManager
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

package org.openmicroscopy.shoola.agents.viewer;

//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.AbstractIconManager;

/** 
 * Provides the icons used by the Viewer.
 * <p>The icons are retrieved by first calling the 
 * {@link #getInstance(Registry) getInstance} method and then the 
 * {@link #getIcon(int) getIcon} method passing one of the icon ID's specified
 * by the static constants within this class &#151; icons will be retrieved
 * from the Viewer's graphics bundle, which implies that its
 * configuration has been read in (this happens during the initialization
 * procedure).</p>
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
	extends AbstractIconManager
{
	
	/** ID of the movie icon. */
	public static final int     MOVIE = 0;   
  
	/** ID of the stop icon. */
	public static final int     STOP = 1; 
	
	/** ID of the rewind icon. */
	public static final int     REWIND = 2;
	
	/** ID of the question mark icon. */
	public static final int		QUESTION = 3;
	
	/** ID of the rendering icon. */
	public static final int		RENDER = 4;
	
	/** ID of the viewer icon. */
	public static final int		VIEWER = 5;
	
	/** ID of the zoom in icon. */
	public static final int		ZOOMIN = 6;
	
	/** ID of the zoom out icon. */
	public static final int		ZOOMOUT = 7;
	
	/** ID of the zoom fit icon. */
	public static final int		ZOOMFIT = 8;
	
	/** ID of the inspector icon. */
	public static final int		INSPECTOR = 9;
		
	/** 
	 * The maximum ID used for the icon IDs.
	 * Allows to correctly build arrays for direct indexing. 
	 */
	private static int          MAX_ID = 9;
	
	/** Paths of the icon files. */
	private static String[]     relPaths = new String[MAX_ID+1];
		
	static {
		relPaths[MOVIE] = "movie16.png";
		relPaths[STOP] = "stop16.png";
		relPaths[REWIND] = "rewind16.png";
		relPaths[QUESTION] = "question24.png";	
		relPaths[RENDER] = "render16.png";
		relPaths[VIEWER] = "viewer16.png";
		relPaths[ZOOMIN] = "zoomin16.png";
		relPaths[ZOOMOUT] = "zoomout16.png";
		relPaths[ZOOMFIT] = "zoomfit16.png";
		relPaths[INSPECTOR] = "render16.png";
	}
	
	/** The sole instance. */
	private static IconManager	singleton;
	
	/**
	 * Returns the <code>IconManager</code> object. 
	 * 
	 * @return	See above.
	 */
	public static IconManager getInstance(Registry registry)
	{
		if (singleton == null)	singleton = new IconManager(registry);
		return singleton;
	}
	
	
	/**
	 * Creates a new instance and configures the parameters.
	 * 
	 * @param registry	Reference to the registry.
	 */
	private IconManager(Registry registry)
	{
		super(registry, "/resources/icons/Factory", relPaths);
	}
	
}
