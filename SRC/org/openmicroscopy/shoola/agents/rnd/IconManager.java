/*
 * org.openmicroscopy.shoola.agents.rnd.IconManager
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

package org.openmicroscopy.shoola.agents.rnd;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.AbstractIconManager;

/** 
 * Provides the icons used by the Rendering Agent.
 * <p>The icons are retrieved by first calling the 
 * {@link #getInstance(Registry) getInstance} method and then the 
 * {@link #getIcon(int) getIcon} method passing one of the icon ID's specified
 * by the static constants within this class &#151; icons will be retrieved
 * from the Rendering Agent's graphics bundle, which implies that its
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
	
	/** ID of the information icon. */
	public static final int     INFO = 0;   
  
	/** ID of the histogram icon. */
	public static final int     HISTOGRAM = 1;
	
	/** ID of the contrast stretching icon. */
	public static final int     STRETCHING = 2;
	
	/** ID of the plane slicing icon. */
	public static final int     SLICING = 3;
	
	/** ID of the greyscale icon. */
	public static final int		GREYSCALE = 4;
	
	/** ID of the greyscale icon. */
	public static final int		RGB = 5;
		
	/** ID of the greyscale icon. */
	public static final int		HSB = 6;
	
	/** ID of the codomain icon. */
	public static final int		CODOMAIN = 7;
	
	/** ID of the save icon. */
	public static final int		SAVE_SETTINGS = 8;
	
	/** ID of the save icon. */
	public static final int		RENDER = 9;
	
	/** ID of the mapping icon. */
	public static final int		MAPPING = 10;
	
	/** ID of the channel big icon. */
	public static final int		CHANNEL_BIG = 11;	
	
	/** ID of the channel big icon. */
	public static final int		CHANNEL = 12;	
	
	/** 
	 * The maximum ID used for the icon IDs.
	 * Allows to correctly build arrays for direct indexing. 
	 */
	private static int          MAX_ID = 12;
	
	/** Paths of the icon files. */
	private static String[]     relPaths = new String[MAX_ID+1];
		
	static {
		relPaths[INFO] = "nuvola_info16.png";
		relPaths[HISTOGRAM] = "histogram16.png";
		relPaths[STRETCHING] = "contrastStretching16.png";
		relPaths[SLICING] = "planeSlicing16.png";
		relPaths[GREYSCALE] = "grayscale.png";
		relPaths[RGB] = "rgb16.png";
		relPaths[HSB] = "hsb16.png";
		relPaths[CODOMAIN] = "codomain16.png";
		relPaths[SAVE_SETTINGS] = "nuvola_filesave16.png";
		relPaths[RENDER] = "render16.png";
		relPaths[MAPPING] = "nuvola_kmplot16.png";
		relPaths[CHANNEL_BIG] = "nuvola_log48.png";
		relPaths[CHANNEL] = "nuvola_log16.png";
	}
	
	/** The sole instance that provides. */
	private static IconManager	singleton;
	
	/** Returns the <code>IconManager</code> object. */
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
