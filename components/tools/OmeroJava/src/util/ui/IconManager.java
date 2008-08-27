/*
 * org.openmicroscopy.shoola.util.ui.IconManager
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

package util.ui;


//Java imports
import javax.swing.Icon;
import javax.swing.ImageIcon;


//Third-party libraries

//Application-internal dependencies

/** 
 * Provides the icons used by the util.ui package.
 * <p>The icons are retrieved by first calling the 
 * {@link #getInstance() getInstance} method and then the 
 * {@link #getIcon(int) getIcon} method passing one of the icon ID's specified
 * by the static constants within this class.</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class IconManager
{

    /** The <code>Hierarchy Explorer</code> icon. */
    public final static int           HIERARCHY_EXPLORER = 0;
    
    /** The <code>Category Explorer</code> icon. */
    public final static int           CATEGORY_EXPLORER = 1;
    
    /** The <code>Images Explorer</code> icon. */
    public final static int           IMAGES_EXPLORER = 2;
    
    /** The <code>Manager</code> icon. */
    public final static int           MANAGER = 3;
    
    /** The <code>Project</code> icon. */
    public final static int           PROJECT = 4;
    
    /** The <code>Dataset</code> icon. */
    public final static int           DATASET = 5;
    
    /** The <code>Image</code> icon. */
    public final static int           IMAGE = 6;
    
    /** The <code>Category</code> icon. */
    public final static int           CATEGORY = 7;
    
    /** The <code>CategoryGroup</code> icon. */
    public final static int           CATEGORY_GROUP = 8;
    
    /** The <code>Root</code> icon. */
    public final static int           ROOT = 9;
    
    /** The <code>Properties</code> icon. */
    public final static int           PROPERTIES = 10;
    
    /** The <code>Viewer</code> icon. */
    public final static int           VIEWER = 11;
    
    /** The <code>Refresh</code> icon. */
    public final static int           REFRESH = 12;
    
    /** The <code>Create</code> icon. */
    public final static int           CREATE = 13;
    
    /** The <code>Close</code> icon. */
    public final static int           CLOSE = 14;
    
    /** The <code>Collapse</code> icon. */
    public final static int           COLLAPSE = 15;
    
    /** The <code>Sort</code> icon. */
    public final static int           SORT = 16;
    
    /** The <code>Sort by Date</code> icon. */
    public final static int           SORT_DATE = 17;
    
    /** The <code>Filter</code> icon. */
    public final static int           FILTER = 18;
    
    /** The <code>Filter Menu</code> icon. */
    public final static int           FILTER_MENU = 19;
    
    /** The <code>Annotated Dataset</code> icon. */
    public final static int           ANNOTATED_DATASET = 20;
    
    /** The <code>Annotated Image</code> icon. */
    public final static int           ANNOTATED_IMAGE = 21;
    
    /** A bigger version of the <code>Create</code> icon. */
    public final static int           CREATE_BIG = 22;
    
    /** The <code>Error</code> icon. */
    public final static int           ERROR = 23;
    
    /** A bigger version of the <code>Properties</code> icon. */
    public final static int           PROPERTIES_BIG = 24;
    
    /** The <code>Owner</code> icon. */
    public final static int           OWNER = 25;
    
    /** The <code>Copy</code> icon. */
    public final static int           COPY = 26;
    
    /** The <code>Paste</code> icon. */
    public final static int           PASTE = 27;
    
    /** The <code>Delete</code> icon. */
    public final static int           DELETE = 28;
    
    /** The <code>Find Next</code> icon. */
    public final static int           FIND_NEXT = 29;
    
    /** The <code>Find Previous</code> icon. */
    public final static int           FIND_PREVIOUS = 30;
    
    /** The <code>Highlight</code> icon. */
    public final static int           HIGHLIGHT = 31;
    
    /** The <code>Highlight</code> icon. */
    public final static int           FINDER = 32;
    
    /** The <code>Warning</code> icon. */
    public final static int           WARNING = 33;
    
    /** The <code>Blank</code> icon. */
    public final static int           TRANSPARENT = 34;
    
    /** The <code>Annotation</code> icon. */
    public final static int           ANNOTATION = 35;
    
    /** The <code>Classification</code> icon. */
    public final static int           CLASSIFY = 36;
    
    /** The <code>Classification</code> icon. */
    public final static int           CATEGORY_BIG = 37;
    
    /** The <code>Classified Image</code> icon. */
    public final static int           CLASSIFIED_IMAGE = 38;
    
    /** The <code>Classified and Annotated Image</code> icon. */
    public final static int           ANNOTATED_CLASSIFIED_IMAGE = 39;
    
    /** The <code>Exit Application</code> icon. */
    public final static int           EXIT_APPLICATION = 40;
    
    /** The <code>Filter Big</code> icon. */
    public final static int           FILTER_BIG = 41;
    
    /** The <code>Navigation Forward</code> icon. */
    public final static int           FORWARD_NAV = 42;
    
    /** The <code>Navigation Forward</code> icon. */
    public final static int           BACKWARD_NAV = 43;
    
    /** The <code>Status Info</code> icon. */
    public final static int           STATUS_INFO = 44;
    
    /** The <code>Cancel</code> icon. */
    public final static int           CANCEL = 45;
    
    /** The <code>Data Manager</code> icon. */
    public final static int           DATA_MANAGER = 46;
    
    /** The <code>Classifier</code> icon. */
    public final static int           CLASSIFIER = 47;
    
    /** The <code>Add top container</code> icon. */
    public final static int           ADD_CONTAINER = 48;
    
    /** The <code>Add existing</code> icon. */
    public final static int           ADD_EXISTING = 49;
    
    /** The <code>Progress</code> icon. */
    public final static int           PROGRESS = 50;
    
    /** The <code>Declassify</code> icon. */
    public final static int           DECLASSIFY = 51;
    
    /** The <code>Declassify</code> icon. */
    public final static int           CUT = 52;
    
    /** The <code>Question</code> icon. */
    public final static int           QUESTION = 53;
    
    /** The <code>Manager 48</code> icon. */
    public final static int           MANAGER_48 = 54;
        
    /** The <code>Partial name</code> icon. */
    public final static int           PARTIAL_NAME = 55;
    
    /** The <code>Partial name</code> icon. */
    public final static int           IMAGE_48 = 56;
    
    /** The <code>User Group</code> icon. */
    public final static int           USER_GROUP = 57;
    
    /** The <code>Owner 48</code> icon. */
    public final static int           OWNER_48 = 58;
    
    /** The <code>Server</code> icon. */
    public final static int           SERVER = 59;
    
    /** The <code>history</code> icon. */
    public final static int           HISTORY = 60;
    
    /** The <code>roll over</code> icon. */
    public final static int           ROLL_OVER = 61;
    
    /** The <code>browser</code> icon. */
    public final static int           BROWSER = 62;
    
    /** The <code>disk space</code> icon. */
    public final static int           DISK_SPACE = 63;
    
    /** The <code>disk space</code> icon. */
    public final static int           DATE = 64;
    
    /** The <code>Redo</code> icon. */
    public final static int           REDO = 65;
    
    /** The <code>Search</code> icon. */
    public final static int           SEARCH = 66;
    
    /** The <code>remove edit</code> icon. */
    public final static int           EDIT_REMOVE = 67;
    
    /** The <code>add_12</code> icon. */
    public final static int           ADD_12 = 68;
    
    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private final static int          MAX_ID = 68;
    
    /** Paths of the icon files. */
    private final static String[]     relPaths = new String[MAX_ID+1];
    
    static {
        relPaths[HIERARCHY_EXPLORER] = "eclipse_hierarchy_co16.png";
        relPaths[CATEGORY_EXPLORER] = "eclipse_hierarchicalLayout16.png";
        relPaths[IMAGES_EXPLORER] = "eclipse_outline_co16.png";
        relPaths[MANAGER] = "nuvola_file-manager16.png";
        relPaths[PROJECT] = "nuvola_document16.png";
        relPaths[DATASET] = "nuvola_folder_image16.png";
        relPaths[IMAGE] = "nuvola_image16.png";
        relPaths[CATEGORY] = "nuvola_knotes16.png";
        relPaths[CATEGORY_GROUP] = "tag_folder16.png";//"category_group16.png";
        relPaths[ROOT] = "nuvola_trashcan_empty16.png";
        relPaths[PROPERTIES] = "nuvola_kate16.png";
        relPaths[VIEWER] = "viewer16.png";
        relPaths[REFRESH] = "nuvola_reload16.png";
        relPaths[CREATE] = "nuvola_filenew16.png"; 
        relPaths[CLOSE] = "nuvola_editdelete16.png";
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
        relPaths[ANNOTATION] = "nuvola_kwrite16.png";
        relPaths[CLASSIFY] = "nuvola_knotes16.png";
        relPaths[CATEGORY_BIG] = "nuvola_filenew48.png";
        relPaths[ANNOTATED_CLASSIFIED_IMAGE] = 
                                    "annotated_tagged_image16.png";
        relPaths[CLASSIFIED_IMAGE] = "tagged_image16.png";
        relPaths[EXIT_APPLICATION] = "nuvola_exit16.png";
        relPaths[FILTER_BIG] = "nuvola_find48.png";
        relPaths[FORWARD_NAV] = "eclipse_forward_nav16.png";
        relPaths[BACKWARD_NAV] = "eclipse_backward_nav16.png";
        relPaths[STATUS_INFO] = "nuvola_messagebox_info16.png";
        relPaths[CANCEL] = "nuvola_cancel16.png";
        relPaths[DATA_MANAGER] = "eclipse_external_tools16.png";
        relPaths[CLASSIFIER] = "tag_folder_open16.png";//"eclipse_sroot_obj16.png";
        relPaths[ADD_CONTAINER] = "eclipse_newpack_wiz16.png";
        relPaths[ADD_EXISTING] = "eclipse_newdatapool_wiz16.png";
        relPaths[PROGRESS] = "eclipse_progress_none16.png";
        relPaths[DECLASSIFY] = "delete_knotes16.png";
        relPaths[CUT] = "nuvola_editcut16.png";
        relPaths[QUESTION] = "nuvola_filetypes32.png";
        relPaths[MANAGER_48] = "nuvola_file-manager48.png";
        relPaths[PARTIAL_NAME] = "nuvola_kmessedwords16.png";
        relPaths[IMAGE_48] = "nuvola_thumbnail48.png";
        relPaths[USER_GROUP] = "nuvola_kgpg_photo16.png";
        relPaths[OWNER_48] = "nuvola_kdmconfig48.png";
        relPaths[SERVER] = "nuvola_server16.png";
        relPaths[HISTORY] = "eclipse_history_list16.png";
        relPaths[ROLL_OVER] = "nuvola_mouse16.png";
        relPaths[BROWSER] = "zoom16.png";
        relPaths[DISK_SPACE] = "nuvola_kcmpartitions16.png";
        relPaths[DATE] = "nuvola_date16.png";
        relPaths[REDO] = "nuvola_redo16.png";
        relPaths[SEARCH] = "nuvola_find16.png";
        relPaths[EDIT_REMOVE] = "remove12.png";
        relPaths[ADD_12] = "add12.png";
    }
    
    /** 
     * Retrieves the icon specified by <code>id</code>.
     * If the icon can't be retrieved, then this method will log the error and
     * return <code>null</code>.
     *
     * @param id    The index of the file name in the array of file names 
     *              specified to this class' constructor.
     * @return  An {@link Icon} object created from the image file.  The return
     *          value will be <code>null</code> if the file couldn't be found
     *          or an image icon couldn't be created from that file.
     */ 
    public Icon getIcon(int id)
    {
        if (id < 0 || relPaths.length <= id) return null;
        return getIcon(relPaths[id]);
    }
    
    /** 
     * Retrieves the icon specified by <code>name</code>.
     * If the icon can't be retrieved, then this method will log the error and
     * return <code>null</code>.
     *
     * @param name    Must be one a valid icon file name within the directory
     *                  used by the {@link IconFactory} instance specified via
     *                  this class' constructor.
     * @return  An {@link Icon} object created from the image file.  The return
     *          value will be <code>null</code> if the file couldn't be found
     *          or an image icon couldn't be created from that file.
     */ 
    public Icon getIcon(String name)
    {
        Icon icon = factory.getIcon(name);
        if (icon == null) {
            StringBuffer buf = new StringBuffer("Failed to retrieve icon: ");
            buf.append("<classpath>");
            buf.append(factory.getResourcePathname(name));
            buf.append(".");
        }
        return icon;
    }
    
    /** 
     * Retrieves the icon specified by <code>id</code>.
     * If the icon can't be retrieved, then this method will log the error and
     * return <code>null</code>.
     *
     * @param id    The index of the file name in the array of file names 
     *              specified to this class' constructor.
     * @return  An {@link Icon} object created from the image file.  The return
     *          value will be <code>null</code> if the file couldn't be found
     *          or an image icon couldn't be created from that file.
     */ 
    public ImageIcon getImageIcon(int id)
    {
        if (id < 0 || relPaths.length <= id) return null;
        return getImageIcon(relPaths[id]);
    }
    
    /** 
     * Retrieves the icon specified by <code>name</code>.
     * If the icon can't be retrieved, then this method will log the error and
     * return <code>null</code>.
     *
     * @param name    Must be one a valid icon file name within the directory
     *                  used by the {@link IconFactory} instance specified via
     *                  this class' constructor.
     * @return  An {@link Icon} object created from the image file.  The return
     *          value will be <code>null</code> if the file couldn't be found
     *          or an image icon couldn't be created from that file.
     */ 
    public ImageIcon getImageIcon(String name)
    {
        ImageIcon icon = factory.getImageIcon(name);
        if (icon == null) {
            StringBuffer buf = new StringBuffer("Failed to retrieve icon: ");
            buf.append("<classpath>");
            buf.append(factory.getResourcePathname(name));
            buf.append(".");
        }
        return icon;
    }
    
    /** The sole instance. */
    private static IconManager  singleton;
    
    /** The factory. */
    private IconFactory         factory;
 
    /** 
     * Returns the <code>IconManager</code> object. 
     * 
     * @return See above.
     */
    public static IconManager getInstance()
    {
        if (singleton == null) singleton = new IconManager();
        return singleton;
    }
    
    /** Creates a new instance and configures the parameters. */
    private IconManager()
    {
        factory = new IconFactory();
    }
    
}
