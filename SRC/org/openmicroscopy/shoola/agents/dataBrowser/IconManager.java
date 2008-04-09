/*
 * org.openmicroscopy.shoola.agents.dataBrowser.IconManager 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.dataBrowser;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.AbstractIconManager;

/** 
 * 
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
    
    /** The <code>Zoom in</code> icon. */
    public static int			ZOOM_IN = 0;

    /** The <code>Zoom out</code> icon. */
    public static int			ZOOM_OUT = 1;
    
    /** The <code>Thumbnail view</code> icon. */
    public static int			THUMBNAIL_VIEW = 2;
    
    /** The <code>Column view</code> icon. */
    public static int			COLUMN_VIEW = 3;
    
    /** The <code>Filtering 48</code> icon. */
    public static int			FILTERING_48 = 4;
    
    /** The <code>Filtering</code> icon. */
    public static int			FILTERING = 5;
    
    /** The <code>Slide Show View</code> icon. */
    public static int			SLIDE_SHOW_VIEW = 6;
    
    /** The <code>Metadata</code> icon. */
    public static int			METADATA = 7;
    
    /** The <code>Pin</code> icon. */
    public static int			PIN = 8;
    
    /** The <code>Pause</code> icon. */
    public static int			PAUSE = 9;
    
    /** The <code>Forward</code> icon. */
    public static int			FORWARD = 10;
    
    /** The <code>Backward</code> icon. */
    public static int			BACKWARD = 11;
    
    /** The <code>Previous</code> icon. */
    public static int			PREVIOUS = 12;
    
    /** The <code>Next</code> icon. */
    public static int			NEXT = 13;
    
    /** The <code>Image</code> icon. */
    public static int			IMAGE = 14;
    
    /** The <code>Dataset</code> icon. */
    public static int			DATASET = 15;
    
    /** The <code>Project</code> icon. */
    public static int			PROJECT = 16;
    
    /** The <code>Annotation</code> icon. */
    public static int			ANNOTATION = 17;
    
    /** The <code>Transparent</code> icon. */
    public static int			TRANSPARENT = 18;
    
    /** The <code>Annotation 8</code> icon. */
    public static int			ANNOTATION_8 = 19;
    
    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private static int          MAX_ID = 19;
    
    /** Paths of the icon files. */
    private static String[]     relPaths = new String[MAX_ID+1];
    
    static {
    	relPaths[ZOOM_IN] = "nuvola_viewmag+16.png";
        relPaths[ZOOM_OUT] = "nuvola_viewmag-16.png";
        relPaths[THUMBNAIL_VIEW] = "nuvola_view_multicolumn16.png";
        relPaths[COLUMN_VIEW] = "nuvola_view_text16.png";
        relPaths[FILTERING_48] = "nuvola_view_text16.png";
        relPaths[FILTERING] = "eclipse_filter_ps16.png";
        relPaths[SLIDE_SHOW_VIEW] = 
        					"openOffice_stock_insert-video-plugin-16.png";
        relPaths[METADATA] = "eclipse_filter_ps16.png";
        relPaths[PIN] = "nuvola_attach16.png";
        relPaths[PAUSE] = "nuvola_player_pause16.png";
        relPaths[FORWARD] = "nuvola_player_play16.png";
        relPaths[BACKWARD] = "nuvola_flipped_player_play16.png";
        relPaths[PREVIOUS] = "nuvola_1leftarrow16.png";
        relPaths[NEXT] = "nuvola_1rightarrow16.png";
        relPaths[IMAGE] = "nuvola_image16.png";
        relPaths[DATASET] = "nuvola_folder_image16.png";
        relPaths[PROJECT] = "nuvola_folder_blue_open16.png";
        relPaths[ANNOTATION] = "nuvola_knotes16.png";
        relPaths[TRANSPARENT] = "eclipse_transparent16.png";
        relPaths[ANNOTATION_8] = "nuvola_knotes8.png";
    }
    
    /** The sole instance. */
    private static IconManager  singleton;
    
    /**
     * Returns the <code>IconManager</code> object. 
     * 
     * @return See above.
     */
    public static IconManager getInstance() 
    { 
        if (singleton == null) 
            singleton = new IconManager(DataBrowserAgent.getRegistry());
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
