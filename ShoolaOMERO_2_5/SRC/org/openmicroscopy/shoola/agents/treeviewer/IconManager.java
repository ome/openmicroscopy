/*
 * org.openmicroscopy.shoola.agents.treemng.IconManager
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

package org.openmicroscopy.shoola.agents.treeviewer;

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
 * from the HiViewer's graphics bundle, which implies that its
 * configuration has been read in (this happens during the initialization
 * procedure).</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class IconManager
    extends AbstractIconManager
{

    /** The <code>Hierarchy Explorer</code> icon. */
    public static int           HIERARCHY_EXPLORER = 0;
    
    /** The <code>Category Explorer</code> icon. */
    public static int           CATEGORY_EXPLORER = 1;
    
    /** The <code>Images Explorer</code> icon. */
    public static int           IMAGES_EXPLORER = 2;
    
    /** The <code>Manager</code> icon. */
    public static int           MANAGER = 3;
    
    /** The <code>Project</code> icon. */
    public static int           PROJECT = 4;
    
    /** The <code>Dataset</code> icon. */
    public static int           DATASET = 5;
    
    /** The <code>Image</code> icon. */
    public static int           IMAGE = 6;
    
    /** The <code>Category</code> icon. */
    public static int           CATEGORY = 7;
    
    /** The <code>CategoryGroup</code> icon. */
    public static int           CATEGORY_GROUP = 8;
    
    /** The <code>Root</code> icon. */
    public static int           ROOT = 9;
    
    /** The <code>Properties</code> icon. */
    public static int           PROPERTIES = 10;
    
    /** The <code>Viewer</code> icon. */
    public static int           VIEWER = 11;
    
    /** The <code>Refresh</code> icon. */
    public static int           REFRESH = 12;
    
    /** The <code>Create</code> icon. */
    public static int           CREATE = 13;
    
    /** The <code>Close</code> icon. */
    public static int           CLOSE = 14;
    
    /** The <code>Collapse</code> icon. */
    public static int           COLLAPSE = 15;
    
    /** The <code>Sort</code> icon. */
    public static int           SORT = 16;
    
    /** The <code>Sort by Date</code> icon. */
    public static int           SORT_DATE = 17;
    
    /** The <code>Filter</code> icon. */
    public static int           FILTER = 18;
    
    /** The <code>Filter Menu</code> icon. */
    public static int           FILTER_MENU = 19;
    
    /** The <code>Annotated Dataset</code> icon. */
    public static int           ANNOTATED_DATASET = 20;
    
    /** The <code>Annotated Image</code> icon. */
    public static int           ANNOTATED_IMAGE = 21;
    
    /** A bigger version of the <code>Create</code> icon. */
    public static int           CREATE_BIG = 22;
    
    /** The <code>Error</code> icon. */
    public static int           ERROR = 23;
    
    /** A bigger version of the <code>Properties</code> icon. */
    public static int           PROPERTIES_BIG = 24;
    
    /** The <code>Owner</code> icon. */
    public static int           OWNER = 25;
    
    /** The <code>Copy</code> icon. */
    public static int           COPY = 26;
    
    /** The <code>Paste</code> icon. */
    public static int           PASTE = 27;
    
    /** The <code>Delete</code> icon. */
    public static int           DELETE = 28;
    
    /** The <code>Find Next</code> icon. */
    public static int           FIND_NEXT = 29;
    
    /** The <code>Find Previous</code> icon. */
    public static int           FIND_PREVIOUS = 30;
    
    /** The <code>Highlight</code> icon. */
    public static int           HIGHLIGHT = 31;
    
    /** The <code>Highlight</code> icon. */
    public static int           FINDER = 32;
    
    /** The <code>Warning</code> icon. */
    public static int           WARNING = 33;
    
    /** The <code>Blank</code> icon. */
    public static int           TRANSPARENT = 34;
    
    /** The <code>Annotation</code> icon. */
    public static int           ANNOTATION = 35;
    
    /** The <code>Classification</code> icon. */
    public static int           CLASSIFY = 36;
    
    /** The <code>Classification</code> icon. */
    public static int           CATEGORY_BIG = 37;
    
    /** The <code>Classified Image</code> icon. */
    public static int           CLASSIFIED_IMAGE = 38;
    
    /** The <code>Classified and Annotated Image</code> icon. */
    public static int           CLASSIFIED_ANNOTATED_IMAGE = 39;
    
    /** The <code>Exit Application</code> icon. */
    public static int           EXIT_APPLICATION = 40;
    
    /** The <code>Filter Big</code> icon. */
    public static int           FILTER_BIG = 41;
    
    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private static int          MAX_ID = 41;
    
    
    /** Paths of the icon files. */
    private static String[]     relPaths = new String[MAX_ID+1];
    
    static {
        relPaths[HIERARCHY_EXPLORER] = "eclipse_hierarchy_co16.png";
        relPaths[CATEGORY_EXPLORER] = "eclipse_hierarchicalLayout16.png";
        relPaths[IMAGES_EXPLORER] = "eclipse_outline_co16.png";
        relPaths[MANAGER] = "nuvola_file-manager16.png";
        relPaths[PROJECT] = "nuvola_document16.png";
        relPaths[DATASET] = "nuvola_folder_image16.png";
        relPaths[IMAGE] = "nuvola_image16.png";
        relPaths[CATEGORY] = "category16.png";
        relPaths[CATEGORY_GROUP] = "category_group16.png";
        relPaths[ROOT] = "nuvola_trashcan_empty16.png";
        relPaths[PROPERTIES] = "nuvola_kate16.png";
        relPaths[VIEWER] = "viewer16.png";
        relPaths[REFRESH] = "nuvola_reload16.png";
        relPaths[CREATE] = "nuvola_filenew16.png"; 
        relPaths[CLOSE] = "eclipse_close_view16.png";
        relPaths[COLLAPSE] = "eclipse_collapseall16.png";
        relPaths[SORT] = "eclipse_alphab_sort_co16.png";
        relPaths[SORT_DATE] = "eclipse_trace_persp16.png";
        relPaths[FILTER] = "nuvola_find16.png";
        relPaths[FILTER_MENU] = "eclipse_view_menu16.png"; 
        relPaths[ANNOTATED_DATASET] = "annotated_dataset16.png";
        relPaths[ANNOTATED_IMAGE] = "annotated_image16.png";
        relPaths[CREATE_BIG] = "nuvola_filenew48.png"; 
        relPaths[ERROR] = "eclipse_error_tsk16.png";
        relPaths[PROPERTIES_BIG] = "nuvola_filenew48.png";
        relPaths[OWNER] = "nuvola_kdmconfig16.png";
        relPaths[COPY] = "eclipse_copy_edit16.png";
        relPaths[PASTE] = "eclipse_paste_edit16.png";
        relPaths[DELETE] = "eclipse_delete_edit16.png";
        relPaths[FIND_NEXT] = "eclipse_SelectNextBottomMappedObject16.png";
        relPaths[FIND_PREVIOUS] = 
            		"eclipse_SelectPreviousBottomMappedObject16.png";
        relPaths[HIGHLIGHT] = "eclipse_default_log_co16.png";
        relPaths[FINDER] = "eclipse_searchrecord16.png";
        relPaths[WARNING] = "eclipse_showwarn_tsk16.png";
        relPaths[TRANSPARENT] = "eclipse_transparent16.png";
        //relPaths[ANNOTATION] = "nuvola_kwrite16.png";
        relPaths[ANNOTATION] = "eclipse_annotate16.png";
        relPaths[CLASSIFY] = "category16.png";
        relPaths[CATEGORY_BIG] = "nuvola_filenew48.png";
        relPaths[CLASSIFIED_ANNOTATED_IMAGE] = "nuvola_filenew48.png";
        relPaths[CLASSIFIED_IMAGE] = "nuvola_filenew48.png";
        relPaths[EXIT_APPLICATION] = "OpenOffice_stock_exit-16.png";
        relPaths[FILTER_BIG] = "nuvola_find48.png";
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
            singleton = new IconManager(TreeViewerAgent.getRegistry());
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
