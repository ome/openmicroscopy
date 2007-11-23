/*
 * org.openmicroscopy.shoola.agents.hiviewer.IconManager
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

package org.openmicroscopy.shoola.agents.hiviewer;


//Java imports

//Third-party libraries
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.AbstractIconManager;


/** 
 * Provides the icons used by the HiViewer.
 * <p>The icons are retrieved by first calling the 
 * {@link #getInstance() getInstance} method and then the 
 * {@link #getIcon(int) getIcon} method passing one of the icon ID's specified
 * by the static constants within this class &#151; icons will be retrieved
 * from the HiViewer's graphics bundle, which implies that its
 * configuration has been read in (this happens during the initialization
 * procedure).</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 *              <a href="mailto:a.falconi@dundee.ac.uk">
 *                  a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class IconManager
    extends AbstractIconManager
{ 
    
    /** ID of the <code>Minus</code> icon. */
    public static final int     MINUS = 0;
    
    /** ID of the <code>Minus Over</code> icon. */
    public static final int     MINUS_OVER = 1;
  
    /** ID of the <code>Plus</code> icon. */
    public static final int     PLUS = 2;
    
    /** ID of the <code>Plus Over</code> icon. */
    public static final int     PLUS_OVER = 3;
    
    /** ID of the <code>Close</code> icon. */
    public static final int     CLOSE = 4;
    
    /** ID of the <code>Close over</code> icon. */
    public static final int     CLOSE_OVER = 5;
    
    /** ID of the <code>Properties</code> icon. */
    public static final int     PROPERTIES = 6;
    
    /** ID of the <code>Viewer</code> icon. */
    public static final int     VIEWER = 7;

    /** ID of the <code>Annotate</code> icon. */
    public static final int     ANNOTATE = 8;
    
    /** ID of the <code>Zoom In</code> icon. */
    public static final int     ZOOM_IN = 9;
    
    /** ID of the <code>Zoom Out</code> icon. */
    public static final int     ZOOM_OUT = 10;
    
    /** ID of the <code>Zoom Fit</code> icon. */
    public static final int     ZOOM_FIT = 11;
    
    /** ID of the <code>Exit</code> icon. */
    public static final int     EXIT = 12;
    
    /** ID of the <code>Save</code> icon. */
    public static final int     SAVE = 13;
    
    /** ID of the <code>Clear</code> icon. */
    public static final int     CLEAR = 14;
    
    /** ID of the <code>Classify</code> icon. */
    public static final int     CLASSIFY = 15;
    
    /** ID of the <code>Filter with annotation</code> icon. */
    public static final int     FILTER_W_ANNOTATION = 16;
    
    /** ID of the <code>Filter with Title</code> icon. */
    public static final int     FILTER_W_TITLE = 17;
    
    /** ID of the <code>Squary Layout</code> icon. */
    public static final int     SQUARY_LAYOUT = 18;
    
    /** ID of the <code>Tree Layout</code> icon. */
    public static final int     TREE_LAYOUT = 19;
    
    /** ID of the <code>Status Info</code> icon. */
    public static final int     STATUS_INFO = 20;
    
    /** ID of the <code>Root</code> icon. */
    public static final int     ROOT = 21;
    
    /** ID of the <code>Project</code> icon. */
    public static final int     PROJECT = 22;
    
    /** ID of the <code>Dataset</code> icon. */
    public static final int     DATASET = 23;
    
    /** ID of the <code>CategoryGroup</code> icon. */
    public static final int     CATEGORY_GROUP = 24;
    
    /** ID of the <code>Category</code> icon. */
    public static final int     CATEGORY = 25;
    
    /** ID of the <code>Image</code> icon. */
    public static final int     IMAGE = 26;
    
    /** ID of the big <code>Category</code> icon. */
    public static final int     CATEGORY_BIG = 27;
    
    /** ID of the <code>Refresh</code> icon. */
    public static final int     REFRESH = 28;
    
    /** ID of the big <code>Save As</code> icon. */
    public static final int     SAVE_AS_BIG = 29;
    
    /** ID of the <code>Question</code> icon. */
    public static final int     QUESTION = 30;
    
    /** ID of the <code>Image medium</code> icon. */
    public static final int     IMAGE_MEDIUM = 31;
    
    /** ID of the <code>Collapse</code> icon. */
    public static final int     COLLAPSE = 32;
    
    /** ID of the <code>Close View</code> icon. */
    public static final int     CLOSE_VIEW = 32;
    
    /** ID of the <code>Transparent</code> icon. */
    public static final int     TRANSPARENT = 34;
    
    /** ID of the <code>Find</code> icon. */
    public static final int     FIND = 35;
    
    /** ID of the <code>Annotated Dataset</code> icon. */
    public static final int     ANNOTATED_DATASET = 36;
    
    /** ID of the <code>Annotated Image</code> icon. */
    public static final int     ANNOTATED_IMAGE = 37;
    
    /** ID of the <code>Find</code> icon. */
    public static final int     CLASSIFIED_IMAGE = 38;
    
    /** ID of the <code>Find</code> icon. */
    public static final int     ANNOTATED_CLASSIFIED_IMAGE = 39;
    
    /** ID of the small <code>Annotated</code> icon. */
    public static final int     ANNOTATED_SMALL = 40;
    
    /** ID of the small <code>Annotated</code> icon. */
    public static final int     ANNOTATED_SMALL_OVER = 41;
    
    /** ID of the small <code>Classified</code> icon. */
    public static final int     CLASSIFIED_SMALL = 42;
    
    /** ID of the small <code>Annotated</code> icon. */
    public static final int     CLASSIFIED_SMALL_OVER = 43;
    
    /** The <code>Filter Menu</code> icon. */
    public static int           FILTER_MENU = 44;
    
    /** The <code>Warning</code> icon. */
    public static int           WARNING = 45;
    
    /** The <code>Highlight</code> icon. */
    public static int           HIGHLIGHT = 46;
    
    /** The <code>Info</code> icon. */
    public static int           INFO = 47;
    
    /** The <code>Pin</code> icon. */
    public static int           PIN = 48;
    
    /** The <code>DELETE</code> icon. */
    public static int           DELETE = 49;
    
    /** The <code>Tree View</code> icon. */
    public static int           TREE_VIEW = 50;
    
    /** The <code>ClipBoard View</code> icon. */
    public static int           CLIPBOARD_VIEW = 51;
    
    /** The <code>Sort by Name</code> icon. */
    public static int           SORT_BY_NAME = 52;
    
    /** The <code>Sort by Name</code> icon. */
    public static int           SORT_BY_DATE = 53;

    /** The <code>Lens</code> icon. */
    public static int           LENS = 54;
    
    /** The <code>File Manager 48</code> icon. */
    public static int           VIEWER_48 = 55;
    
    /** The <code>Pin small</code> icon. */
    public static int           PIN_SMALL = 56;
    
    /** The <code>Pin small over</code> icon. */
    public static int           PIN_SMALL_OVER = 57;
    
    /** The <code>Decategorise</code> icon. */
    public static int           DECATEGORISE = 58;
    
    /** The <code>Find Next</code> icon. */
    public static int           FIND_NEXT = 59;
    
    /** The <code>Find Previous</code> icon. */
    public static int           FIND_PREVIOUS = 60;
    
    /** The <code>Partial name</code> icon. */
    public static int           PARTIAL_NAME = 61;
    
    /** The <code>Flat Layout</code> icon. */
    public static int           FLAT_LAYOUT = 62;
    
    /** The <code>Hierarchy Layout</code> icon. */
    public static int           HIERARCHICAL_LAYOUT = 63;

    /** The <code>History</code> icon. */
    public static int           HISTORY = 64;

    /** The <code>Backward nav</code> icon. */
    public static int           BACKWARD_NAV = 65;
    
    /** The <code>Redo</code> icon. */
    public static int           REDO = 66;
    
    /** The <code>Mouse over</code> icon. */
    public static int           MOUSE_OVER = 67;

    /** The <code>Paste</code> icon. */
    public static int           PASTE = 68;
    
    /** The <code>Paste</code> icon. */
    public static int           COPY = 69;

    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private static int          MAX_ID = 69;
    
    /** Paths of the icon files. */
    private static String[]     relPaths = new String[MAX_ID+1];
    
    static {
        relPaths[MINUS] = "minus.png";
        relPaths[MINUS_OVER] = "minus_over.png";
        relPaths[PLUS] = "plus.png";
        relPaths[PLUS_OVER] = "plus_over.png";
        relPaths[CLOSE] = "cross.png";
        relPaths[CLOSE_OVER] = "cross_over.png";
        relPaths[PROPERTIES] = "nuvola_kate16.png";
        relPaths[VIEWER] = "viewer16.png";
        relPaths[ANNOTATE] = "nuvola_kwrite16.png";
        relPaths[ZOOM_IN] = "nuvola_viewmag+16.png";
        relPaths[ZOOM_OUT] = "nuvola_viewmag-16.png";
        relPaths[ZOOM_FIT] = "nuvola_viewmagfit16.png";
        relPaths[EXIT] = "nuvola_fileclose16.png";
        relPaths[SAVE] = "nuvola_save_all16.png";
        relPaths[CLEAR] = "nuvola_history_clear16.png";//"eclipse_clear_co16.png";
        relPaths[CLASSIFY] = "nuvola_knotes16.png";//"category16.png";
        relPaths[FILTER_W_ANNOTATION] = "eclipse_filter_ps16.png";
        relPaths[FILTER_W_TITLE] = "eclipse_filter_ps16.png";
        relPaths[SQUARY_LAYOUT] = "nuvola_view_multicolumn16.png";
        relPaths[TREE_LAYOUT] = "nuvola_view_tree16.png";
        relPaths[STATUS_INFO] = "nuvola_hwinfo16.png";
        relPaths[ROOT] = "nuvola_trashcan_empty16.png";
        relPaths[PROJECT] = "nuvola_document16.png";
        relPaths[DATASET] = "nuvola_folder_image16.png";
        relPaths[CATEGORY_GROUP] = "tag_folder16.png";//"category_group16.png";
        relPaths[CATEGORY] = "nuvola_knotes16.png";//"category16.png";
        relPaths[IMAGE] = "nuvola_image16.png";
        relPaths[CATEGORY_BIG] = "nuvola_knotes48.png";//"category48.png";
        relPaths[REFRESH] = "nuvola_reload16.png";
        relPaths[SAVE_AS_BIG] = "nuvola_filesaveas48.png";
        relPaths[QUESTION] = "nuvola_filetypes32.png";
        relPaths[IMAGE_MEDIUM] = "nuvola_image26.png";
        relPaths[CLOSE_VIEW] = "eclipse_close_view16.png";
        relPaths[COLLAPSE] = "eclipse_collapseall16.png";
        relPaths[TRANSPARENT] = "eclipse_transparent16.png";
        relPaths[FIND] = "eclipse_searchrecord16.png";
        relPaths[ANNOTATED_DATASET] = "annotated_dataset16.png";
        relPaths[ANNOTATED_IMAGE] = "annotated_image16.png";
        relPaths[CLASSIFIED_IMAGE] = "tagged_image16.png";//"classified_image16.png";
        relPaths[ANNOTATED_CLASSIFIED_IMAGE] = "annotated_tagged_image16.png";
                                    //"annotated_classified_image16.png";
        relPaths[ANNOTATED_SMALL] = "kwrite8.png";
        relPaths[ANNOTATED_SMALL_OVER] = "kwrite_over8.png";
        relPaths[CLASSIFIED_SMALL] = "knotes8.png";//"category8.png";
        relPaths[CLASSIFIED_SMALL_OVER] = "knotes8.png";//"category_over8.png";
        relPaths[FILTER_MENU] = "eclipse_view_menu16.png";  
        relPaths[HIGHLIGHT] = "eclipse_default_log_co16.png";
        relPaths[WARNING] = "eclipse_showwarn_tsk16.png";
        relPaths[INFO] = "nuvola_messagebox_info16.png";
        relPaths[PIN] = "nuvola_attach16.png";
        relPaths[DELETE] =  "eclipse_delete_edit16.png";
        relPaths[TREE_VIEW] =  "nuvola_view_tree16.png";
        relPaths[CLIPBOARD_VIEW] =  "nuvola_tab_new16.png";
        relPaths[SORT_BY_NAME] =  "eclipse_alphab_sort_co16.png";
        relPaths[SORT_BY_DATE] =  "eclipse_trace_persp16.png";
        relPaths[LENS] =  "nuvola_viewmag16.png";
        relPaths[VIEWER_48] =  "nuvola_file-manager48.png";
        relPaths[PIN_SMALL] =  "attach8.png";
        relPaths[PIN_SMALL_OVER] = "attach8.png";
        relPaths[DECATEGORISE] = "delete_knotes16.png";//"declassify16.png";
        relPaths[FIND_NEXT] = "eclipse_SelectNextBottomMappedObject16.png";
        relPaths[FIND_PREVIOUS] = 
            		"eclipse_SelectPreviousBottomMappedObject16.png";
        relPaths[PARTIAL_NAME] = "nuvola_kmessedwords16.png";
        relPaths[FLAT_LAYOUT] = "eclipse_flatLayout16.png";
        relPaths[HIERARCHICAL_LAYOUT] = "eclipse_hierarchicalLayout16.png";
        relPaths[HISTORY] = "eclipse_history_list16.png";
        relPaths[BACKWARD_NAV] = "eclipse_backward_nav16.png";
        relPaths[REDO] = "nuvola_redo16.png";
        relPaths[MOUSE_OVER] = "nuvola_mouse16.png";
        relPaths[PASTE] = "eclipse_paste_edit16.png";
        relPaths[COPY] = "eclipse_copy_edit16.png";
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
            singleton = new IconManager(HiViewerAgent.getRegistry());
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
