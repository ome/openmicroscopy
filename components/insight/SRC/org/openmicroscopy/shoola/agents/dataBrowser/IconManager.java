/*
 * org.openmicroscopy.shoola.agents.dataBrowser.IconManager 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2014 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
 * Provides the icons used by the TreeViewer.
 * <p>The icons are retrieved by first calling the 
 * {@link #getInstance() getInstance} method and then the 
 * {@link #getIcon(int) getIcon} method passing one of the icon ID's specified
 * by the static constants within this class &#151; icons will be retrieved
 * from the TreeViewer's graphics bundle, which implies that its
 * configuration has been read in (this happens during the initialization
 * procedure).</p>
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
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
    public static final int ZOOM_IN = 0;

    /** The <code>Zoom out</code> icon. */
    public static final int ZOOM_OUT = 1;

    /** The <code>Thumbnail view</code> icon. */
    public static final int THUMBNAIL_VIEW = 2;

    /** The <code>Column view</code> icon. */
    public static final int COLUMN_VIEW = 3;

    /** The 48x48 <code>Filtering</code> icon. */
    public static final int FILTERING_48 = 4;

    /** The <code>Filtering</code> icon. */
    public static final int FILTERING = 5;

    /** The <code>Slide Show View</code> icon. */
    public static final int SLIDE_SHOW_VIEW = 6;

    /** The <code>Metadata</code> icon. */
    public static final int METADATA = 7;

    /** The <code>Pin</code> icon. */
    public static final int PIN = 8;

    /** The <code>Pause</code> icon. */
    public static final int PAUSE = 9;

    /** The <code>Forward</code> icon. */
    public static final int FORWARD = 10;

    /** The <code>Backward</code> icon. */
    public static final int BACKWARD = 11;

    /** The <code>Previous</code> icon. */
    public static final int PREVIOUS = 12;

    /** The <code>Next</code> icon. */
    public static final int NEXT = 13;

    /** The <code>Image</code> icon. */
    public static final int IMAGE = 14;

    /** The <code>Dataset</code> icon. */
    public static final int DATASET = 15;

    /** The <code>Project</code> icon. */
    public static final int PROJECT = 16;

    /** The <code>Annotation</code> icon. */
    public static final int ANNOTATION = 17;

    /** The <code>Transparent</code> icon. */
    public static final int TRANSPARENT = 18;

    /** The <code>Annotation 8</code> icon. */
    public static final int ANNOTATION_8 = 19;

    /** The <code>Manager</code> icon. */
    public static final int MANAGER = 20;

    /** The <code>Roll Over</code> icon. */
    public static final int ROLL_OVER = 21;

    /** The <code>Create</code> icon. */
    public static final int CREATE = 22;

    /** The 48x48 <code>Create</code> icon. */
    public static final int CREATE_48 = 23;

    /** The <code>Sort by date</code> icon. */
    public static final int SORT_BY_DATE = 24;

    /** The <code>Sort by name</code> icon. */
    public static final int SORT_BY_NAME = 25;

    /** The <code>View</code> icon. */
    public static final int VIEWER = 26;

    /** The <code>Copy</code> icon. */
    public static final int COPY = 27;

    /** The <code>Paste</code> icon. */
    public static final int PASTE = 28;

    /** The <code>Remove</code> icon. */
    public static final int REMOVE = 29;

    /** The <code>Refresh</code> icon. */
    public static final int REFRESH = 30;

    /** The <code>Cut</code> icon. */
    public static final int CUT = 31;

    /** The <code>Undo</code> icon. */
    public static final int UNDO = 32;

    /** The <code>Tag</code> icon. */
    public static final int SET_ORIGINAL_RND_SETTINGS = 33;

    /** The 8x8 <code>Owner</code> icon. */
    public static final int OWNER_8 = 34;

    /** The <code>Image annotated</code> icon. */
    public static final int IMAGE_ANNOTATED = 35;

    /** The <code>Save as</code> icon. */
    public static final int SAVE_AS = 36;

    /** The <code>Report</code> icon. */
    public static final int REPORT = 37;

    /** The <48x48 code>Report</code> icon. */
    public static final int REPORT_48 = 38;

    /** The 48x48 <code>Save As</code> icon. */
    public static final int SAVE_AS_48 = 39;

    /** The <code>Tag</code> icon. */
    public static final int TAG = 40;

    /** The 8x8 <code>Edit</code> icon. */
    public static final int EDIT_8 = 41;

    /** The 48x48 <code>Dataset</code> icon. */
    public static final int DATASET_48 = 42;

    /** The <code>Field View</code> icon. */
    public static final int FIELDS_VIEW = 43;

    /** The <code>Rendering Settings redo</code> icon. */
    public static final int RND_REDO = 44;

    /** The <code>Rendering Settings Min-Max</code> icon. */
    public static final int RND_MIN_MAX = 45;

    /** The <code>Rendering Settings Owner</code> icon. */
    public static final int RND_OWNER = 46;

    /** The 48x48 <code>filter by tags</code> icon. */
    public static final int TAG_FILTER_48 = 47;

    /** The <code>filter by tags</code> icon. */
    public static final int TAG_FILTER = 48;

    /** The <code>filter by Menu</code> icon. */
    public static final int FILTER_BY_MENU = 49;

    /** The <code>Send comment</code> icon. */
    public static final int SEND_COMMENT = 50;

    /** The <code>Personal</code> icon. */
    public static final int PERSONAL = 51;

    /** The <code>Private Group</code> icon. */
    public static final int PRIVATE_GROUP = 52;

    /** The <code>Read Group</code> icon. */
    public static final int READ_GROUP = 53;

    /** The <code>Read Link Group</code> icon. */
    public static final int READ_LINK_GROUP = 54;

    /** The <code>Public Group</code> icon. */
    public static final int PUBLIC_GROUP = 55;

    /** The <code>ImageJ Viewer</code> icon. */
    public static final int VIEWER_IJ = 56;

    /** The <code>Read Link Group</code> icon. */
    public static final int READ_WRITE_GROUP = 57;

    /** The <code>KNIME Viewer</code> icon. */
    public static final int VIEWER_KNIME = 58;

    /** The 8x8 <code>Owner</code> icon. */
    public static final int NOT_OWNER_8 = 50;

    /** The <code>Owner</code> icon. */
    public static final int OWNER = 60;

    /** The <code>Owner Not Active</code> icon. */
    public static final int OWNER_NOT_ACTIVE = 61;

    /** The <code>Owner Not Active</code> icon. */
    public static final int PASSWORD = 62;

    /** The <code>Download</code> icon. */
    public static final int DOWNLOAD = 63;

    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing.
     */
    private static final int MAX_ID = 63;

    /** Paths of the icon files. */
    private static String[] relPaths = new String[MAX_ID+1];

    static {
        relPaths[ZOOM_IN] = "nuvola_viewmag+16.png";
        relPaths[ZOOM_OUT] = "nuvola_viewmag-16.png";
        relPaths[THUMBNAIL_VIEW] = "nuvola_view_multicolumn16.png";
        relPaths[COLUMN_VIEW] = "nuvola_view_text16.png";
        relPaths[FILTERING_48] = "nuvola_view_text16.png";
        relPaths[FILTERING] = "filter_grey16.png";
        relPaths[SLIDE_SHOW_VIEW] = "nuvola_background16.png";
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
        relPaths[MANAGER] = "eclipse_external_tools16.png";
        relPaths[ROLL_OVER] = "roll_over_image16.png";
        relPaths[CREATE] = "nuvola_filenew16.png"; 
        relPaths[CREATE_48] = "nuvola_filenew48.png"; 
        relPaths[SORT_BY_DATE] = "eclipse_trace_persp16.png";
        relPaths[SORT_BY_NAME] = "eclipse_alphab_sort_co16.png";
        relPaths[VIEWER] = "viewer16.png";
        relPaths[COPY] = "eclipse_copy_edit16.png";
        relPaths[PASTE] = "eclipse_paste_edit16.png";
        relPaths[REMOVE] = "eclipse_delete_edit16.png";
        relPaths[REFRESH] = "nuvola_reload16.png";
        relPaths[CUT] = "nuvola_editcut16.png";
        relPaths[UNDO] = "nuvola_undo16.png";
        relPaths[OWNER_8] = "nuvola_kdmconfig8.png";
        relPaths[IMAGE_ANNOTATED] = "tagged_image16.png";
        relPaths[SET_ORIGINAL_RND_SETTINGS] = "nuvola_redo16.png";
        relPaths[SAVE_AS] = "nuvola_filesaveas16.png";
        relPaths[REPORT] = "nuvola_style16.png";
        relPaths[REPORT_48] = "nuvola_style48.png";
        relPaths[SAVE_AS_48] = "nuvola_filesaveas48.png";
        relPaths[TAG] = "nuvola_knotes16.png";
        relPaths[EDIT_8] = "nuvola_ksig8.png";
        relPaths[DATASET_48] = "nuvola_folder_image48.png";
        relPaths[FIELDS_VIEW] = "nuvola_view_multicolumn16.png";
        relPaths[RND_REDO] = "nuvola_undo16.png";
        relPaths[RND_MIN_MAX] =  "nuvola_rendering_minmax16.png";
        relPaths[RND_OWNER] = "rendering_owner16.png";
        relPaths[TAG_FILTER_48] = "nuvola_knotes48.png";
        relPaths[TAG_FILTER] = "eclipse_filter_ps16.png";
        relPaths[FILTER_BY_MENU] = "eclipse_view_menu16.png";
        relPaths[SEND_COMMENT] = "nuvola_mail_send16.png";
        relPaths[PRIVATE_GROUP] = "nuvola_ledred16.png";
        relPaths[READ_GROUP] = "nuvola_ledorange_readOnly16.png";
        relPaths[READ_LINK_GROUP] = "nuvola_ledorange16.png";
        relPaths[READ_WRITE_GROUP] = "nuvola_ledorange16.png";
        relPaths[PUBLIC_GROUP] = "nuvola_ledgreen16.png";
        relPaths[PERSONAL] = "nuvola_personal16.png";
        relPaths[VIEWER_IJ] = "imageJ16.png";
        relPaths[VIEWER_KNIME] = "knimeIcon16.png";
        relPaths[NOT_OWNER_8] = "red_dot8.png";
        relPaths[OWNER] = "nuvola_kdmconfig_modified16.png";
        relPaths[OWNER_NOT_ACTIVE] = "nuvola_kdmconfig_modified_grey16.png";
        relPaths[PASSWORD] = "nuvola_download_manager_rotated16.png";
        relPaths[DOWNLOAD] = "nuvola_download_manager_rotated16.png";
    }

    /** The sole instance. */
    private static IconManager singleton;

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
