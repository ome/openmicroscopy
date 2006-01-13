/*
 * org.openmicroscopy.shoola.agents.hiviewer.IconManager
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
    
    /** ID of the minus icon of the browser's internal frame. */
    public static final int     MINUS = 0;
    
    /** ID of the minus over icon of the browser's internal frame. */
    public static final int     MINUS_OVER = 1;
  
    /** ID of the plus icon of the browser's internal frame. */
    public static final int     PLUS = 2;
    
    /** ID of the plus over icon of the browser's internal frame. */
    public static final int     PLUS_OVER = 3;
    
    /** ID of the close over icon of the browser's window. */
    public static final int     CLOSE = 4;
    
    /** ID of the close over icon of the browser's window. */
    public static final int     CLOSE_OVER = 5;
    
    /** ID of the properties icon used by the popup menu. */
    public static final int     PROPERTIES = 6;
    
    /** ID of the viewer icon used by the popup menu. */
    public static final int     VIEWER = 7;

    /** ID of the annotate icon used by the popup menu. */
    public static final int     ANNOTATE = 8;
    
    /** ID of the zoomIn icon used by the popup menu. */
    public static final int     ZOOM_IN = 9;
    
    /** ID of the zoomOut icon used by the popup menu. */
    public static final int     ZOOM_OUT = 10;
    
    /** ID of the zoomOut icon used by the popup menu. */
    public static final int     ZOOM_FIT = 11;
    
    /** ID of the exit icon. */
    public static final int     EXIT = 12;
    
    /** ID of the save icon. */
    public static final int     SAVE = 13;
    
    /** ID of the find annotated icon. */
    public static final int     ANNOTATED = 14;
    
    /** ID of the clear icon. */
    public static final int     CLEAR = 15;
    
    /** ID of the classify icon. */
    public static final int     CLASSIFY = 16;
    
    /** ID of the find with annotation icon. */
    public static final int     FILTER_W_ANNOTATION = 17;
    
    /** ID of the find with title icon. */
    public static final int     FILTER_W_TITLE = 18;
    
    /** ID of the squary layout icon. */
    public static final int     SQUARY_LAYOUT = 19;
    
    /** ID of the tree layout icon. */
    public static final int     TREE_LAYOUT = 20;
    
    /** ID of the status icon. */
    public static final int     STATUS_INFO = 21;
    
    /** ID of the root icon. */
    public static final int     ROOT = 22;
    
    /** ID of the project icon. */
    public static final int     PROJECT = 23;
    
    /** ID of the dataset icon. */
    public static final int     DATASET = 24;
    
    /** ID of the category group icon. */
    public static final int     CATEGORY_GROUP = 25;
    
    /** ID of the category icon. */
    public static final int     CATEGORY = 26;
    
    /** ID of the image icon. */
    public static final int     IMAGE = 27;
    
    /** ID of the single-view icon in the browser's internal frame. */
    public static final int     SINGLE_VIEW_MODE = 28;
    
    /** ID of the single-view over icon in the browser's internal frame. */
    public static final int     SINGLE_VIEW_MODE_OVER = 29;

    /** ID of the multi-view icon in the browser's internal frame. */
    public static final int     MULTI_VIEW_MODE = 30;
    
    /** ID of the views list icon in the browser's internal frame. */
    public static final int     VIEWS_LIST = 31;
    
    /** ID of the views list over icon in the browser's internal frame. */
    public static final int     VIEWS_LIST_OVER = 32;
    
    /** ID of the category big icon. */
    public static final int     CATEGORY_BIG = 33;
    
    /** ID of the refresh icon. */
    public static final int     REFRESH = 34;
    
    /** ID of the save big icon. */
    public static final int     SAVE_AS_BIG = 35;
    
    /** ID of the question icon. */
    public static final int     QUESTION = 36;
    
    /** ID of the image medium icon. */
    public static final int     IMAGE_MEDIUM = 37;
    
    /** ID of the collapse icon. */
    public static final int     COLLAPSE = 38;
    
    /** ID of the cloase icon. */
    public static final int     CLOSE_VIEW = 39;
    
    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private static int          MAX_ID = 39;
    
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
        relPaths[EXIT] = "OpenOffice_stock_exit-16.png";
        relPaths[SAVE] = "nuvola_save_all16.png";
        relPaths[ANNOTATED] = "annotated_image16.png";
        relPaths[CLEAR] = "eclipse_clear_co16.png";
        relPaths[CLASSIFY] = "category16.png";
        relPaths[FILTER_W_ANNOTATION] = "eclipse_filter_ps16.png";
        relPaths[FILTER_W_TITLE] = "eclipse_filter_ps16.png";
        relPaths[SQUARY_LAYOUT] = "nuvola_view_multicolumn16.png";
        relPaths[TREE_LAYOUT] = "nuvola_view_tree16.png";
        relPaths[STATUS_INFO] = "nuvola_hwinfo16.png";
        relPaths[ROOT] = "nuvola_trashcan_empty16.png";
        relPaths[PROJECT] = "nuvola_document16.png";
        relPaths[DATASET] = "nuvola_folder_image16.png";
        relPaths[CATEGORY_GROUP] = "category_group16.png";
        relPaths[CATEGORY] = "category16.png";
        relPaths[IMAGE] = "nuvola_image16.png";
        relPaths[SINGLE_VIEW_MODE] = "sinlge_view_mode.png";
        relPaths[SINGLE_VIEW_MODE_OVER] = "sinlge_view_mode_over.png";
        relPaths[MULTI_VIEW_MODE] = "nuvola_view_multicolumn16.png";
        relPaths[VIEWS_LIST] = "frame_list.png";
        relPaths[VIEWS_LIST_OVER] = "frame_list_over.png";
        relPaths[CATEGORY_BIG] = "category48.png";
        relPaths[REFRESH] = "nuvola_reload16.png";
        relPaths[SAVE_AS_BIG] = "nuvola_filesaveas48.png";
        relPaths[QUESTION] = "nuvola_filetypes32.png";
        relPaths[IMAGE_MEDIUM] = "nuvola_image26.png";
        relPaths[CLOSE_VIEW] = "eclipse_close_view16.png";
        relPaths[COLLAPSE] = "eclipse_collapseall16.png";
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
