/*
 * org.openmicroscopy.shoola.agents.imviewer.IconManager
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

package org.openmicroscopy.shoola.agents.imviewer;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.AbstractIconManager;

/** 
 * Provides the icons used by the ImViewer.
 * <p>The icons are retrieved by first calling the 
 * {@link #getInstance() getInstance} method and then the 
 * {@link #getIcon(int) getIcon} method passing one of the icon ID's specified
 * by the static constants within this class &#151; icons will be retrieved
 * from the ImViewer's graphics bundle, which implies that its
 * configuration has been read in (this happens during the initialization
 * procedure).</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">a.falconi@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class IconManager
    extends AbstractIconManager
{

    /** The <code>Status Info</code> icon. */
    public  static final int          STATUS_INFO = 0;
    
    /** The <code>Renderer</code> icon. */
    public  static final int          RENDERER = 1;
    
    /** The <code>Save</code> icon. */
    public  static final int          SAVE = 2;
    
    /** The <code>Movie</code> icon. */
    public  static final int          MOVIE = 3;
    
    /** The <code>Lens</code> icon. */
    public  static final int          LENS = 4;
    
    /** The tiny <code>Info</code> icon. */
    public  static final int          TINY_INFO = 5;
    
    /** The  <code>domain</code> icon. */
    public  static final int          DOMAIN = 6;
    
    /** The  <code>codomain</code> icon. */
    public  static final int          CODOMAIN = 7;
    
    /** The <code>Contrast Stretching</code> icon. */
    public static  final int          CONTRAST_STRETCHING = 8;
    
    /** The <code>Plane slicing</code> icon. */
    public static  final int          PLANE_SLICING = 9;
    
    /** The big <code>Contrast Stretching</code> icon. */
    public static  final int          CONTRAST_STRETCHING_48 = 10;
    
    /** The big <code>Plane slicing</code> icon. */
    public static  final int          PLANE_SLICING_48 = 11;
    
    /** The <code>Save settings</code> icon. */
    public static  final int          SAVE_SETTINGS = 12;
    
    /** The <code>Reset settings</code> icon. */
    public static  final int          RESET_SETTINGS = 13;
    
    /** The <code>Histogram</code> icon. */
    public static  final int          HISTOGRAM = 14;
   
    /** The <code>Play</code> icon. */
    public static  final int          PLAY = 15;
    
    /** The <code>Stop</code> icon. */
    public static  final int          STOP = 16;
    
    /** The <code>Pause</code> icon. */
    public static  final int          PAUSE = 17;
    
    /** The <code>Histogram big</code> icon. */
    public static  final int          HISTOGRAM_48 = 18;
    
    /** The <code>Save As big</code> icon. */
    public static  final int          SAVE_48 = 19;
    
    /** The <code>Question</code> icon. */
    public static  final int          QUESTION = 20;
    
    /** The <code>Cancel</code> icon. */
    public static  final int          CANCEL = 21;
    
    /** The <code>Viewer</code> icon. */
    public static  final int          VIEWER = 22;
    
    /** The <code>GreyScale</code> icon. */
    public static  final int          GRAYSCALE = 23;
    
    /** The <code>RGB</code> icon. */
    public static  final int          RGB = 24;
    
    /** The <code>HSB</code> icon. */
    public static  final int          HSB = 25;
    
    /** The <code>Plus</code> icon. */
    public static  final int          PLUS = 26;
    
    /** The <code>MINUS</code> icon. */
    public static  final int          MINUS = 27;
    
    /** The <code>Histogram</code> icon. */
    public static final int 		  TEMPORARY_HISTOGRAM = 28;

    /** The <code>Color Picker</code> icon. */
    public static final int           COLOR_PICKER = 29;

    /** The <code>Info</code> icon. */
    public static final int           INFO = 30;
    
    /** The <code>Info</code> icon 48 by 48. */
    public static final int           INFO_48 = 31;
    
    /** The <code>Download</code> icon. */
    public static final int           DOWNLOAD = 32;
    
    /** The <code>Annotation</code> icon. */
    public static final int           ANNOTATION = 33;
    
    /** The <code>Grid View</code> icon. */
    public static final int           GRIDVIEW = 34;
    
    /** The <code>Channel Split</code> icon. */
    public static final int           CHANNEL_SPLIT = 35;
    
    /** The <code>Text</code> icon. */
    public static final int           TEXT = 36;
    
    /** The <code>Measurement tool</code> icon. */
    public static final int           MEASUREMENT_TOOL = 37;
    
    /** The <code>Zoom in</code> icon. */
    public static final int           ZOOM_IN = 38;
    
    /** The <code>Zoom out</code> icon. */
    public static final int           ZOOM_OUT = 39;
    
    /** The <code>Zoom fit</code> icon. */
    public static final int           ZOOM_FIT = 40;
    
    /** The <code>Clear</code> icon. */
    public static final int           HISTORY_CLEAR = 41;

    /** The <code>Create big</code> icon. */
    public static final int           CREATE_48 = 43;
    
    /** The <code>Filter menu</code> icon. */
    public static final int           FILTER_MENU = 44;

    /** The <code>Browse</code> icon. */
    public static final int           BROWSE = 46;
    
    /** The <code>Transparent</code> icon. */
    public static final int           TRANSPARENT = 47;
    
    /** The <code>Ratio min</code> icon. */
    public static final int           RATIO_MIN = 48;
    
    /** The <code>Ratio max</code> icon. */
    public static final int           RATIO_MAX = 49;
    
    /** The <code>Copy</code> icon. */
    public static final int           COPY = 50;
    
    /** The <code>Paste</code> icon. */
    public static final int           PASTE = 51;
    
    /** The <code>Preferences</code> 48x48 icon. */
    public static final int           PREFERENCES_48 = 52;
    
    /** The <code>User</code> icon. */
    public static final int           USER = 53;
    
    /** The <code>Search</code> icon. */
    public static final int           SEARCH = 54;
    
    /** The <code>History</code> icon. */
    public static final int           HISTORY = 55;
    
    /** The <code>Clear 12x12</code> icon. */
    public static final int           HISTORY_CLEAR_12 = 56;
    
    /** The <code>Set original Rendering settings</code> icon. */
    public static final int           SET_RND_SETTINGS = 57;
    
    /** The <code>Projection</code> icon. */
    public static final int           PROJECTION = 58;
    
    /** The <code>Projection 48</code> icon. */
    public static final int           PROJECTION_48 = 59;
    
    /** The <code>Projection 12</code> icon. */
    public static final int           PROJECTION_12 = 60;
    
    /** The <code>Publishing</code> icon. */
    public static final int           PUBLISHING = 61;
    
    /** The <code>Close</code> icon. */
    public static final int           CLOSE = 62;
    
    /** The <code>Detach</code> icon. */
    public static final int           DETACH = 63;
    
    /** The <code>Rendering Settings redo</code> icon. */
    public static final int          RND_REDO = 64;
    
    /** The <code>Rendering Settings Min-Max</code> icon. */
    public static final int          RND_MIN_MAX = 65;
    
    /** The <code>Rendering Settings Owner</code> icon. */
    public static final int          RND_OWNER = 66;
    
    /** The <code>Rendering Settings Undo</code> icon. */
    public static final int          RND_UNDO = 67;
    
    /** The <code>Refresh</code> icon. */
    public static final int          REFRESH = 68;
    
    /** The 48x48 <code>FLIM</code> icon. */
    public static final int          FLIM_48 = 69;
    
    /** The <code>Ratio min</code> icon. */
    public static final int           RATIO_MIN_DISABLED = 70;
    
    /** The <code>Ratio max</code> icon. */
    public static final int           RATIO_MAX_DISABLED = 71;
    
    /** The 48x48 <code>Download</code> icon. */
    public static final int           DOWNLOAD_48 = 72;
    
    /** The 22x22 <code>Download</code> icon. */
    public static final int           DOWNLOAD_22 = 73;
    
    /** The 22x22 <code>Split view figure</code> icon. */
    public static final int           SPLIT_VIEW_FIGURE_22 = 74;
    
    /** The <code>Private Group</code> icon. */
    public static final int           PRIVATE_GROUP = 75;
    
    /** The <code>Read Group</code> icon. */
    public static final int           READ_GROUP = 76;
    
    /** The <code>Read Link Group</code> icon. */
    public static final int           READ_LINK_GROUP = 77;
    
    /** The <code>Public Group</code> icon. */
    public static final int           PUBLIC_GROUP = 78;
    
    /** The <code>Read Write Group</code> icon. */
    public static final int           READ_WRITE_GROUP = 79;

    /** The <code>Analysis</code> icon. */
    public static final int ANALYSIS_RUN = 80;

    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private static final int          MAX_ID = 80;

    /** Paths of the icon files. */
    private static String[]     relPaths = new String[MAX_ID+1];
    
    static {
        relPaths[STATUS_INFO] = "nuvola_messagebox_info16.png";
        relPaths[RENDERER] = "render16.png";
        relPaths[MOVIE] = "nuvola_kaboodle16.png";
        relPaths[SAVE] = "download_image16.png";//"nuvola_filesaveas16.png";
        relPaths[LENS] = "nuvola_viewmag16.png";
        relPaths[TINY_INFO] = "messagebox_info8.png";
        relPaths[DOMAIN] = "nuvola_kmplot16.png";
        relPaths[CODOMAIN] = "codomain16.png";
        relPaths[CONTRAST_STRETCHING] = "openOffice_stock_new-drawing-16.png";
        relPaths[PLANE_SLICING] = "openOffice_stock_new-labels-16.png";
        relPaths[CONTRAST_STRETCHING_48] = 
                                "openOffice_stock_new-drawing-48.png";
        relPaths[PLANE_SLICING_48] = "openOffice_stock_new-labels-48.png";
        relPaths[SAVE_SETTINGS] = "nuvola_filesave16.png";
        relPaths[RESET_SETTINGS] = "nuvola_undo16.png";
        relPaths[HISTOGRAM] = "histogram16.png";
        relPaths[PLAY] = "nuvola_player_play16.png";
        relPaths[PAUSE] = "nuvola_player_pause16.png";
        relPaths[STOP] = "nuvola_player_stop16.png";
        relPaths[HISTOGRAM_48] = "histogram16.png";
        relPaths[SAVE_48] = "download_image48.png";//"nuvola_filesaveas48.png";
        relPaths[QUESTION] = "nuvola_filetypes32.png";
        relPaths[CANCEL] = "eclipse_delete_edit16.png";
        relPaths[VIEWER] = "viewer16.png";
        relPaths[GRAYSCALE] = "grayscale16.png";
        relPaths[RGB] = "rgb16.png";
        relPaths[HSB] = "hsb16.png";
        relPaths[PLUS] = "nuvola_edit_add16.png";
        relPaths[MINUS] = "nuvola_edit_remove16.png";
        relPaths[TEMPORARY_HISTOGRAM] = "histogram_temporary.png";
        relPaths[COLOR_PICKER] = "nuvola_colorpicker16.png";
        relPaths[INFO] = "nuvola_messagebox_info16.png";
        relPaths[INFO_48] = "nuvola_messagebox_info48.png";
        relPaths[DOWNLOAD] = "nuvola_download_manager16.png";
        relPaths[ANNOTATION] = "nuvola_kwrite16.png";
        relPaths[GRIDVIEW] = "gridView16.png";
        relPaths[CHANNEL_SPLIT] = "gridView16.png";
        relPaths[TEXT] = "nuvola_font_truetype16.png";
        relPaths[MEASUREMENT_TOOL] = "nuvola_designer16.png";//crystal_roi16.png";
        relPaths[ZOOM_IN] = "nuvola_viewmag+16.png";
        relPaths[ZOOM_OUT] = "nuvola_viewmag-16.png";
        relPaths[ZOOM_FIT] = "nuvola_viewmagfit16.png";
        relPaths[HISTORY_CLEAR] = "nuvola_history_clear16.png";
        relPaths[CREATE_48] = "nuvola_filenew48.png"; 
        relPaths[FILTER_MENU] = "eclipse_view_menu16.png"; 
        relPaths[BROWSE] = "zoom16.png";
        relPaths[TRANSPARENT] = "eclipse_transparent16.png";
        relPaths[RATIO_MIN] = relPaths[ZOOM_OUT];//"image8.png";
        relPaths[RATIO_MAX] = relPaths[ZOOM_IN];//"image14.png";
        relPaths[COPY] = "eclipse_copy_edit16.png";
        relPaths[PASTE] = "eclipse_paste_edit16.png";
        relPaths[PREFERENCES_48] = "nuvola_messagebox_info48.png";
        relPaths[USER] = "nuvola_kdmconfig16.png";
        relPaths[SEARCH] = "nuvola_find16.png";
        relPaths[HISTORY] = "nuvola_history16.png";
        relPaths[HISTORY_CLEAR_12] = "nuvola_history_clear12.png";
        relPaths[SET_RND_SETTINGS] = "nuvola_redo16.png";
        relPaths[PROJECTION] = "projBlack16.png";//"projection16.png";
        relPaths[PROJECTION_48] = "projBlack48.png"; 
        relPaths[PROJECTION_12] = "projection12.png"; 
        relPaths[PUBLISHING] = "splitViewFigure16.png";//"nuvola_kcmsystem16.png";
        relPaths[CLOSE] = "nuvola_cancel16.png";
        relPaths[DETACH] = "nuvola_cancel16.png";
        relPaths[RND_REDO] = "nuvola_undo16.png";
        relPaths[RND_MIN_MAX] = "nuvola_rendering_minmax16.png";
        relPaths[RND_OWNER] = "rendering_owner16.png";
        relPaths[RND_UNDO] = "nuvola_undo16.png";
        relPaths[REFRESH] = "nuvola_reload16.png";
        relPaths[FLIM_48] = "nuvola_messagebox_info48.png";
        relPaths[RATIO_MIN_DISABLED] = "nuvola_disabled_viewmag-16.png";
        relPaths[RATIO_MAX_DISABLED] = "nuvola_disabled_viewmag+16.png";
        relPaths[DOWNLOAD_48] = "nuvola_download_manager48.png";
        relPaths[DOWNLOAD_22] = "nuvola_download_manager22.png";
        relPaths[SPLIT_VIEW_FIGURE_22] = "splitViewFigure22.png";
        relPaths[PRIVATE_GROUP] = "private16.png";
        relPaths[READ_GROUP] = "group_read16.png";
        relPaths[READ_LINK_GROUP] = "group_read_annotate16.png";
        relPaths[PUBLIC_GROUP] = "group_public_read16.png";
        relPaths[READ_WRITE_GROUP] = "group_read_write16.png";
        relPaths[ANALYSIS_RUN] = "nuvola_script_run16.png";
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
            singleton = new IconManager(ImViewerAgent.getRegistry());
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
