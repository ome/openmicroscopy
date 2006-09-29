/*
 * org.openmicroscopy.shoola.agents.imviewer.IconManager
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
    public  static int          STATUS_INFO = 0;
    
    /** The <code>Renderer</code> icon. */
    public  static int          RENDERER = 1;
    
    /** The <code>Save</code> icon. */
    public  static int          SAVE = 2;
    
    /** The <code>Movie</code> icon. */
    public  static int          MOVIE = 3;
    
    /** The <code>Lens</code> icon. */
    public  static int          LENS = 4;
    
    /** The tiny <code>Info</code> icon. */
    public  static int          TINY_INFO = 5;
    
    /** The tiny <code>Info</code> icon. */
    public  static int          DOMAIN = 6;
    
    /** The tiny <code>Info</code> icon. */
    public  static int          CODOMAIN = 7;
    
    /** The <code>Contrast Stretching</code> icon. */
    public static  int          CONTRAST_STRETCHING = 8;
    
    /** The <code>Plane slicing</code> icon. */
    public static  int          PLANE_SLICING = 9;
    
    /** The big <code>Contrast Stretching</code> icon. */
    public static  int          CONTRAST_STRETCHING_BIG = 10;
    
    /** The big <code>Plane slicing</code> icon. */
    public static  int          PLANE_SLICING_BIG = 11;
    
    /** The <code>Save settings</code> icon. */
    public static  int          SAVE_SETTINGS = 12;
    
    /** The <code>Reset settings</code> icon. */
    public static  int          RESET_SETTINGS = 13;
    
    /** The <code>Histogram</code> icon. */
    public static  int          HISTOGRAM = 14;
   
    /** The <code>Play</code> icon. */
    public static  int          PLAY = 15;
    
    /** The <code>Stop</code> icon. */
    public static  int          STOP = 16;
    
    /** The <code>Pause</code> icon. */
    public static  int          PAUSE = 17;
    
    /** The <code>Histogram big</code> icon. */
    public static  int          HISTOGRAM_BIG = 18;
    
    /** The <code>Save As big</code> icon. */
    public static  int          SAVE_BIG = 19;
    
    /** The <code>Question</code> icon. */
    public static  int          QUESTION = 20;
    
    /** The <code>Cancel</code> icon. */
    public static  int          CANCEL = 21;
    
    /** The <code>Viewer</code> icon. */
    public static  int          VIEWER = 22;
    
    /** The <code>GreyScale</code> icon. */
    public static  int          GRAYSCALE = 23;
    
    /** The <code>RGB</code> icon. */
    public static  int          RGB = 24;
    
    /** The <code>HSB</code> icon. */
    public static  int          HSB = 25;
    
    /** The <code>Plus</code> icon. */
    public static  int          PLUS = 26;
    
    /** The <code>MINUS</code> icon. */
    public static  int          MINUS = 27;
    
    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private static int          MAX_ID = 27;
    
    /** Paths of the icon files. */
    private static String[]     relPaths = new String[MAX_ID+1];
    
    static {
        relPaths[STATUS_INFO] = "nuvola_messagebox_info16.png";
        relPaths[RENDERER] = "render16.png";
        relPaths[MOVIE] = "openOffice_stock_insert-video-plugin-16.png";
        relPaths[SAVE] = "nuvola_filesaveas16.png";
        relPaths[LENS] = "nuvola_viewmag16.png";
        relPaths[TINY_INFO] = "messagebox_info8.png";
        relPaths[DOMAIN] = "nuvola_kmplot16.png";
        relPaths[CODOMAIN] = "codomain16.png";
        relPaths[CONTRAST_STRETCHING] = "openOffice_stock_new-drawing-16.png";
        relPaths[PLANE_SLICING] = "openOffice_stock_new-labels-16.png";
        relPaths[CONTRAST_STRETCHING_BIG] = 
                                "openOffice_stock_new-drawing-48.png";
        relPaths[PLANE_SLICING_BIG] = "openOffice_stock_new-labels-48.png";
        relPaths[SAVE_SETTINGS] = "nuvola_viewmag16.png";
        relPaths[RESET_SETTINGS] = "nuvola_undo16.png";
        relPaths[HISTOGRAM] = "histogram16.png";
        relPaths[PLAY] = "nuvola_player_play16.png";
        relPaths[PAUSE] = "nuvola_player_pause16.png";
        relPaths[STOP] = "nuvola_player_stop16.png";
        relPaths[HISTOGRAM_BIG] = "histogram16.png";
        relPaths[SAVE_BIG] = "histogram16.png";
        relPaths[QUESTION] = "nuvola_filetypes32.png";
        relPaths[CANCEL] = "eclipse_delete_edit16.png";
        relPaths[VIEWER] = "viewer16.png";
        relPaths[GRAYSCALE] = "grayscale16.png";
        relPaths[RGB] = "rgb16.png";
        relPaths[HSB] = "hsb16.png";
        relPaths[PLUS] = "nuvola_edit_add16.png";
        relPaths[MINUS] = "nuvola_edit_remove16.png";
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
