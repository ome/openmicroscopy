/*
 * org.openmicroscopy.shoola.agents.roi.IconManager 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.measurement;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.AbstractIconManager;

/** 
 * Provides the icons used by the MeasurementTool.
 * <p>The icons are retrieved by first calling the 
 * {@link #getInstance() getInstance} method and then the 
 * {@link #getIcon(int) getIcon} method passing one of the icon ID's specified
 * by the static constants within this class &#151; icons will be retrieved
 * from the Measurement tool graphics bundle, which implies that its
 * configuration has been read in (this happens during the initialization
 * procedure).</p>
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
public class IconManager 
	extends AbstractIconManager
{

	/** The <code>Save</code> icon. */
	public static final int		SAVE = 0;
	
	/** The <code>Inspector</code> icon. */
	public static final int		INSPECTOR = 1;
	
	/** The <code>Manager</code> icon. */
	public static final int		MANAGER = 2;
	
	/** The <code>Results</code> icon. */
	public static final int		RESULTS = 3;
	
	/** The <code>Load</code> icon. */
	public static final int		LOAD = 4;
	
	/** The <code>Refresh</code> icon. */
	public static final int		REFRESH = 5;
	
	/** The <code>Left Arrow</code> icon. */
	public static final int		LEFT_ARROW = 6;
	
	/** The <code>Right Arrow</code> icon. */
	public static final int		RIGHT_ARROW = 7;
	
	/** The <code>Double Left Arrow</code> icon. */
	public static final int		DOUBLE_LEFT_ARROW = 8;
	
	/** The <code>Double Right Arrow</code> icon. */
	public static final int		DOUBLE_RIGHT_ARROW = 9;

	/** The <code>Wizard</code> icon. */
	public static final int		WIZARD = 10;
	
	/** The <code>Wizard16</code> icon. */
	public static final int		WIZARD16 = 11;
	
	/** The <code>Save16</code> icon. */
	public static final int		SAVE16 = 12;
	
	/** The <code>Refresh16</code> icon. */
	public static final int		REFRESH16 = 13;
	
	/** The <code>Status Info</code> icon. */
	public static final int		STATUS_INFO = 14;
	
	/** The <code>Square</code> icon. */
	public static final int		SQUARE = 15;
	
	/** The <code>Ellipse</code> icon. */
	public static final int	 	ELLIPSE = 16;
	
	/** The <code>Polyline</code> icon. */
	public static final int		POLYLINE = 17;

	/** The <code>Polygon</code> icon. */
	public static final int		POLYGON = 18;

	/** The <code>Point</code> icon. */
	public static final int		POINT = 19;

	/** The <code>Line</code> icon. */
	public static final int		LINE = 20;

	/** The <code>Lineconnection</code> icon. */
	public static final int		LINECONNECTION = 21;

	/** The <code>Text</code> icon. */
	public static final int		TEXT = 22;
	
	/** The <code>PointIcon</code> icon. */
	public static final int		POINTICON = 23;

	 /** The <code>Progress</code> icon. */
    public static int           PROGRESS = 24;

    /** The <code>Progress</code> icon. */
    public static int           GRAPHPANE = 25;
    
	/** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private static final int    MAX_ID = 25;
    
    /** Paths of the icon files. */
    private static String[]     relPaths = new String[MAX_ID+1];
    
    /** The sole instance. */
    private static IconManager  singleton;
    
    static {
        relPaths[SAVE] = "nuvola_filesaveas22.png";
        relPaths[INSPECTOR] = "nuvola_viewmag16.png";
        relPaths[MANAGER] = "nuvola_find16.png";
        relPaths[RESULTS] = "nuvola_view_text16.png";
        relPaths[LOAD] = "nuvola_revert22.png";
        relPaths[REFRESH] = "nuvola_reload16.png";
        relPaths[LEFT_ARROW] = "nuvola_1leftarrow22.png";
        relPaths[RIGHT_ARROW] = "nuvola_1rightarrow22.png";
        relPaths[DOUBLE_LEFT_ARROW] = "nuvola_2leftarrow22.png";
        relPaths[DOUBLE_RIGHT_ARROW] = "nuvola_2rightarrow22.png";
        relPaths[WIZARD] = "nuvola_wizard48.png";
        relPaths[WIZARD16] = "nuvola_wizard16.png";
        relPaths[SAVE16] = "nuvola_filesaveas16.png";
        relPaths[REFRESH16] = "nuvola_reload16.png";
        relPaths[STATUS_INFO] = "nuvola_messagebox_info16.png";
        relPaths[SQUARE] = "square.png";
        relPaths[ELLIPSE] = "ellipse.png";
        relPaths[POLYLINE] = "polyline.png";
        relPaths[SQUARE] = "square.png";
        relPaths[LINE] = "line.png";
        relPaths[LINECONNECTION] = "lineconnection.png";
        relPaths[POLYGON] = "polygon.png";
        relPaths[POINT] = "point.png";
        relPaths[TEXT] = "text.png";
        relPaths[POINTICON] = "point22.png";
        relPaths[PROGRESS] = "eclipse_progress_none16.png";
        relPaths[GRAPHPANE] = "nuvola_kmplot16.png";
    }
    
    /** 
     * Returns the <code>IconManager</code> object. 
     * 
     * @return See above.
     */
    public static IconManager getInstance() 
    { 
        if (singleton == null) 
            singleton = new IconManager(MeasurementAgent.getRegistry());
        return singleton; 
    }
    
    /**
     * Creates a new instance and configures the parameters.
     * 
     * @param registry  Reference to the registry.
     */
    private IconManager(Registry registry)
    {
        super(registry, "/resources/icons/Factory", relPaths);
    }

}
