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
	public static final int     PLAY = 0;   
  
	/** ID of the stop icon. */
	public static final int     STOP = 1; 
	
	/** ID of the rewind icon. */
	public static final int     REWIND = 2;
	
	/** ID of the rendering icon. */
	public static final int		RENDER = 3;
	
	/** ID of the viewer icon. */
	public static final int		VIEWER = 4;
	
	/** ID of the zoom in icon. */
	public static final int		ZOOMIN = 5;
	
	/** ID of the zoom out icon. */
	public static final int		ZOOMOUT = 6;
	
	/** ID of the zoom fit icon. */
	public static final int		ZOOMFIT = 7;
	
	/** ID of the inspector icon. */
	public static final int		INSPECTOR = 8;
	
	/** ID of the player pause icon. */
	public static final int		PAUSE = 9;
	
	/** ID of the save_image_as icon. */
	public static final int		SAVEAS = 10;	
	
	/** ID of the player fwd icon. */
	public static final int		FORWARD = 11;
	
	/** ID of the question icon. */
	public static final int		QUESTION = 12;
	
	/** ID of the save_image_as big icon. */
	public static final int		SAVEAS_BIG = 13;
	
	/** ID of the save_image_as big icon. */
	public static final int		VIEWER3D = 14;

	/** ID of the save_image_as big icon. */
	public static final int		LOAD = 15;
	
	/** ID of the player movie icon. */
	public static final int		MOVIE = 16;
    
    /** ID of the player end icon. */
    public static final int     PLAYER_END = 17;
	
    /** ID of the player end icon. */
    public static final int     PLAYER_START = 18;
	/** 
	 * The maximum ID used for the icon IDs.
	 * Allows to correctly build arrays for direct indexing. 
	 */
	private static int          MAX_ID = 18;
	
	/** Paths of the icon files. */
	private static String[]     relPaths = new String[MAX_ID+1];
		
	static {
		relPaths[PLAY] = "nuvola_player_play16.png";
		relPaths[STOP] = "nuvola_player_stop16.png";
		relPaths[REWIND] = "nuvola_player_rev16.png";
		relPaths[RENDER] = "render16.png";
		relPaths[VIEWER] = "viewer16.png";
		relPaths[ZOOMIN] = "nuvola_viewmag+16.png";
		relPaths[ZOOMOUT] = "nuvola_viewmag-16.png";
		relPaths[ZOOMFIT] = "nuvola_viewmagfit16.png";
		relPaths[INSPECTOR] = "nuvola_viewmag16.png";
		relPaths[PAUSE] = "nuvola_player_pause16.png";
		relPaths[SAVEAS] = "nuvola_filesaveas16.png";
		relPaths[FORWARD] = "nuvola_player_fwd16.png";
		relPaths[QUESTION] = "nuvola_filetypes32.png";
		relPaths[SAVEAS_BIG] = "nuvola_filesaveas48.png";
		relPaths[VIEWER3D] = "nuvola_kalzium16.png";
		relPaths[LOAD] = "nuvola_network48.png";
		relPaths[MOVIE] = "openOffice_stock_insert-video-plugin-16.png";
        relPaths[PLAYER_START] = "nuvola_player_start16.png";
        relPaths[PLAYER_END] = "nuvola_player_end16.png";
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
