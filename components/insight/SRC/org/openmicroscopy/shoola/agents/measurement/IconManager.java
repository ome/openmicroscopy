/*
 * org.openmicroscopy.shoola.agents.roi.IconManager 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
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

	/** The <code>Save As 22</code> icon. */
	public static final int		SAVE_AS_22 = 0;
	
	/** The <code>Inspector</code> icon. */
	public static final int		INSPECTOR = 1;
	
	/** The <code>Manager</code> icon. */
	public static final int		MANAGER = 2;
	
	/** The <code>Results</code> icon. */
	public static final int		RESULTS = 3;
	
	/** The <code>Load</code> icon. */
	public static final int		LOAD_22 = 4;
	
	/** The <code>Refresh</code> icon. */
	public static final int		REFRESH = 5;
	
	/** The <code>Left Arrow 22</code> icon. */
	public static final int		LEFT_ARROW_22 = 6;
	
	/** The <code>Right Arrow 22</code> icon. */
	public static final int		RIGHT_ARROW_22 = 7;
	
	/** The <code>Double Left Arrow 22</code> icon. */
	public static final int		DOUBLE_LEFT_ARROW_22 = 8;
	
	/** The <code>Double Right Arrow 22</code> icon. */
	public static final int		DOUBLE_RIGHT_ARROW_22 = 9;

	/** The <code>Wizard 48</code> icon. */
	public static final int		WIZARD_48 = 10;
	
	/** The <code>Wizard</code> icon. */
	public static final int		WIZARD = 11;
	
	/** The <code>Save As</code> icon. */
	public static final int		SAVE_AS = 12;
	
	/** The <code>Status Info</code> icon. */
	public static final int		STATUS_INFO = 13;
	
	/** The <code>Square</code> icon. */
	public static final int		SQUARE = 14;
	
	/** The <code>Ellipse</code> icon. */
	public static final int	 	ELLIPSE = 15;
	
	/** The <code>Polyline</code> icon. */
	public static final int		POLYLINE = 16;

	/** The <code>Polygon</code> icon. */
	public static final int		POLYGON = 17;

	/** The <code>Point</code> icon. */
	public static final int		POINT = 20;

	/** The <code>Line</code> icon. */
	public static final int		LINE = 21;

	/** The <code>Line connection</code> icon. */
	public static final int		LINECONNECTION = 22;

	/** The <code>Text</code> icon. */
	public static final int		TEXT = 23;
	
	/** The <code>Point Icon 22</code> icon. */
	public static final int		POINTICON_22 = 24;

	 /** The <code>Progress</code> icon. */
    public static final int     PROGRESS = 25;

    /** The <code>GraphPane</code> icon. */
    public static final int		GRAPHPANE = 26;
    
    /** The <code>Intensity View</code> icon. */
    public static final int		INTENSITYVIEW = 27;
    
    /** The <code>ROI stack</code> icon. */
    public static final int		ROISTACK = 28;
    
    /** The <code>ROI Shape</code> icon. */
    public static final int		ROISHAPE = 29;
    
    /** The <code>Line 16</code> icon. */
    public static final int		LINE_16 = 30;
    
    /** The <code>Ellipse 16</code> icon. */
    public static final int		ELLIPSE_16 = 31;
    
    /** The <code>Rectangle 16</code> icon. */
    public static final int		RECTANGLE = 32;
    
    /** The <code>Connection 16</code> icon. */
    public static final int		CONNECTION = 33;
    
    /** The <code>Polygon 16</code> icon. */
    public static final int		POLYGON_16 = 34;
    
    /** The <code>Scribble 16</code> icon. */
    public static final int		SCRIBBLE = 35;

    /** The <code>Point 16</code> icon. */
    public static final int		POINT_16 = 36;

    /** The <code>Text</code> icon. */
    public static final int		TEXT_16 = 37;

    /** The <code>Question</code> icon. */
    public static final int		QUESTION_32 = 38;

    /** The <code>CornerIcon</code> icon. */
    public static final int		CORNERICON = 39;
    
    /** The <code>Load</code> icon. */
    public static final int		LOAD = 40;
    
    /** The <code>Measurement tool</code> icon. */
    public static final int		MEASUREMENT_TOOL = 41;

    /** The <code>Measurement tool</code> icon. */
	public static final int MASK = 42;

    /** The <code>Delete</code> icon. */
	public static final int DELETE = 43;
	
    /** The 22x22 <code>Delete</code> icon. */
    public static final int DELETE_22 = 44;
    
    /** The 22x22 <code>Apply</code> icon. */
    public static final int APPLY_22 = 45;
    
    /** The <code>ROI stack</code> icon if ROI is owned by other users. */
    public static final int ROISTACK_OTHER_OWNER = 46;
    
    /** Icon for ROI folders */
    public static final int ROIFOLDER = 47;
    
    /** The <code>Filter Menu</code> icon. */
    public static final int           FILTER_MENU = 48;
    
    /** 16px plus icon */
    public static final int           ADD_16 = 49;
   
    /** Icon for ROI folders owned by other user */
    public static final int ROIFOLDERUSER = 50;
    
	/** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private static final int    MAX_ID = 50;
    
    /** Paths of the icon files. */
    private static String[]     relPaths = new String[MAX_ID+1];
    
    /** The sole instance. */
    private static IconManager  singleton;
    
    static {
        relPaths[SAVE_AS_22] = "nuvola_filesaveas22.png";
        relPaths[INSPECTOR] = "nuvola_viewmag16.png";
        relPaths[MANAGER] = "nuvola_find16.png";
        relPaths[RESULTS] = "nuvola_view_text16.png";
        relPaths[LOAD_22] = "nuvola_revert22.png";
        relPaths[REFRESH] = "nuvola_reload16.png";
        relPaths[LEFT_ARROW_22] = "nuvola_1leftarrow22.png";
        relPaths[RIGHT_ARROW_22] = "nuvola_1rightarrow22.png";
        relPaths[DOUBLE_LEFT_ARROW_22] = "nuvola_2leftarrow22.png";
        relPaths[DOUBLE_RIGHT_ARROW_22] = "nuvola_2rightarrow22.png";
        relPaths[WIZARD_48] = "nuvola_wizard48.png";
        relPaths[WIZARD] = "nuvola_wizard16.png";
        relPaths[SAVE_AS] = "nuvola_filesaveas16.png";
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
        relPaths[POINTICON_22] = "point22.png";
        relPaths[PROGRESS] = "eclipse_progress_none16.png";
        relPaths[GRAPHPANE] = "nuvola_kmplot16.png";
        relPaths[INTENSITYVIEW] = "nuvola_kig16.png";
        relPaths[ROISTACK] = "roistack16.png";
        relPaths[ROISHAPE] = "roishape16.png";
        relPaths[RECTANGLE] = "rectangle16.png";
        relPaths[ELLIPSE_16] = "ellipse16.png";
        relPaths[CONNECTION] = "connection16.png";
        relPaths[LINE_16] = "line16.png";
        relPaths[SCRIBBLE] = "scribble16.png";
        relPaths[POLYGON_16] = "polygon16.png";
        relPaths[POINT_16] = "point16.png";
        relPaths[TEXT_16] = "text16.png";
        relPaths[QUESTION_32] = "nuvola_filetype32.png";
        relPaths[CORNERICON] = "cornericon.png";
        relPaths[LOAD] = "nuvola_revert16.png";
        relPaths[MEASUREMENT_TOOL] = "nuvola_designer16.png";
        relPaths[MASK] = "mask16.png";
        relPaths[DELETE] = "nuvola_cancel16.png";
        relPaths[DELETE_22] = "nuvola_cancel22.png";
        relPaths[APPLY_22] = "nuvola_apply22.png";
        relPaths[ROISTACK_OTHER_OWNER] = "roistack_owner_16.png";
        relPaths[ROIFOLDER] = "roi_folder_icon.png";
        relPaths[FILTER_MENU] = "eclipse_view_menu16.png";
        relPaths[ADD_16] = "nuvola_edit_add16.png";
        relPaths[ROIFOLDERUSER] = "roi_folder_user_icon.png";
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
