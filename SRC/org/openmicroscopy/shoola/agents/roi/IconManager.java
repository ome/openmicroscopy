/*
 * org.openmicroscopy.shoola.agents.roi.IconManager
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

package org.openmicroscopy.shoola.agents.roi;


///Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.AbstractIconManager;

/** 
* Provides the icons used by the ROIAgent.
* <p>The icons are retrieved by first calling the 
* {@link #getInstance(Registry) getInstance} method and then the 
* {@link #getIcon(int) getIcon} method passing one of the icon ID's specified
* by the static constants within this class &#151; icons will be retrieved
* from the Viewer's graphics bundle, which implies that its
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
  
    /** ID of the erase icon. */
    public static final int     ERASE = 0;   

    /** ID of the erase all shapes icon. */
    public static final int     ERASE_ALL = 1;
  
    /** ID of the analyse icon. */
    public static final int     ANALYSE = 3; 
  
    /** ID of the rectangle icon. */
    public static final int     RECTANGLE = 4;
  
    /** ID of the ellipse icon. */
    public static final int     ELLIPSE = 5;
  
    /** ID of the ROI icon. */
    public static final int     MOVE_ROI = 6;
  
    /** ID of the ROI icon. */
    public static final int     SIZE_ROI = 7;
  
    public static final int     CLOSE = 8;
    
    public static final int     SAVE = 9;
    
    public static final int     ROISHEET_BIG = 10;
    
    public static final int     SAVEAS_BIG = 11;
    
    public static final int     UNDO_ERASE = 12;
    
    public static final int     QUESTION = 13;
    
    public static final int     ANNOTATE_BIG = 14;
    
    public static final int     UP = 15;
    
    public static final int     DOWN = 16;
    
    public static final int     UNDO = 18;
    
    public static final int     CREATE_ROI = 19;
    
    public static final int     ASSISTANT = 20;
    
    public static final int     COPY = 21;
    
    public static final int     COPY_STACK = 22;
    
    public static final int     COPY_SEGMENT = 23;
    
    public static final int     ZOOM_IN = 24;
    
    public static final int     ZOOM_OUT = 25;
    
    public static final int     ZOOM_FIT = 26;
    
    public static final int     VIEWER = 27;
    
    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private static int          MAX_ID = 27;
  
    /** Paths of the icon files. */
    private static String[]     relPaths = new String[MAX_ID+1];
          
    static {
        relPaths[ERASE] = "eclipse_clear_co16.png"; 
        relPaths[ERASE_ALL] = "eraser16.png";
        relPaths[ANALYSE] = "nuvola_edu_mathematics16.png";
        relPaths[RECTANGLE] = "openOffice_stock_draw-rectangle-unfilled-16.png";
        relPaths[ELLIPSE] = "openOffice_stock_draw-ellipse-unfilled-16.png";
        relPaths[MOVE_ROI] = "nuvola_window_list16.png";
        relPaths[SIZE_ROI] = "eclipse_bundle_fragment_obj16.png";
        relPaths[CLOSE] = "nuvola_cancel16.png";
        relPaths[SAVE] = "nuvola_filesaveas16.png";
        relPaths[ROISHEET_BIG] = "nuvola_spreadsheet48.png";
        relPaths[SAVEAS_BIG] = "nuvola_filesaveas48.png";
        relPaths[UNDO_ERASE] = "eclipse_clear_co16.png";
        relPaths[QUESTION] = "nuvola_filetypes32.png";
        relPaths[ANNOTATE_BIG] = "nuvola_template_source48.png";
        relPaths[UP] = "nuvola_up16.png";
        relPaths[DOWN] = "nuvola_down16.png";
        relPaths[UNDO] = "nuvola_undo16.png";
        relPaths[ASSISTANT] = "openOffice_stock_form-table-control-16.png";
        relPaths[COPY] = "nuvola_editcopy16.png";
        relPaths[COPY_STACK] = "openOffice_stock_3d-perspective-16.png";
        relPaths[CREATE_ROI] = "nuvola_kpovmodeler16.png";
        relPaths[COPY_SEGMENT] = "nuvola_editpaste16.png";
        relPaths[ZOOM_IN] = "nuvola_viewmag+16.png";
        relPaths[ZOOM_OUT] = "nuvola_viewmag-16.png";
        relPaths[ZOOM_FIT] = "nuvola_viewmagfit16.png";
        relPaths[VIEWER] = "ome_eye16.png";
    }
      
    /** The sole instance. */
    private static IconManager  singleton;
      
    /**
     * Returns the <code>IconManager</code> object. 
     * 
     * @return  See above.
     */
    public static IconManager getInstance(Registry registry)
    {
        if (singleton == null)  singleton = new IconManager(registry);
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
