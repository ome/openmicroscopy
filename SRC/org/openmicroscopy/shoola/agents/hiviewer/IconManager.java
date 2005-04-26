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
 * {@link #getInstance(Registry) getInstance} method and then the 
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
    
    /** ID of the save icon. */
    public static final int     ANNOTATED = 14;
    
    /** ID of the save icon. */
    public static final int     CLEAR = 15;
    
    /** ID of the save icon. */
    public static final int     CLASSIFY = 16;
    
    /** ID of the save icon. */
    public static final int     FILTER_W_ANNOTATION = 17;
    
    /** ID of the save icon. */
    public static final int     FILTER_W_TITLE = 18;
    
    /** ID of the save icon. */
    public static final int     SQUARY_LAYOUT = 19;
    
    /** ID of the save icon. */
    public static final int     TREE_LAYOUT = 20;
    
    /** ID of the save icon. */
    public static final int     STATUS_INFO = 21;
    
    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private static int          MAX_ID = 21;
    
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
        relPaths[SQUARY_LAYOUT] = "OpenOffice_stock_3d-texture-16.png";
        relPaths[TREE_LAYOUT] = "eclipse_hierarchy_co16.png";
        relPaths[STATUS_INFO] = "nuvola_hwinfo16.png";
    }
    
    /** The sole instance. */
    private static IconManager  singleton;
    
    
    /** Returns the <code>IconManager</code> object. */
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
