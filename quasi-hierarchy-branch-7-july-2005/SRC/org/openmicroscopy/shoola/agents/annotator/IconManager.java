/*
 * org.openmicroscopy.shoola.agents.annotator.IconManager
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

package org.openmicroscopy.shoola.agents.annotator;




//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.AbstractIconManager;


/** 
 * Provides the icons used by the Data Manager.
 * <p>The icons are retrieved by first calling the 
 * {@link #getInstance(Registry) getInstance} method and then the 
 * {@link #getIcon(int) getIcon} method passing one of the icon ID's specified
 * by the static constants within this class &#151; icons will be retrieved
 * from the Data Manager's graphics bundle, which implies that its
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
          
    
    /** ID of the icon of the save to DB button. */
    public static final int     SAVE = 0;
  
    public static final int     ANNOTATE_BIG = 1;
    
    public static final int     CANCEL = 2;
    
    public static final int     UP = 3;
    
    public static final int     DOWN = 4;
    
    public static final int     CREATE = 5;
    
    public static final int     DELETE = 6;
    
    public static final int     SAVEWITHRS = 7;
    
    public static final int     SEND_TO_DB = 8;
    
    public static final int     VIEWER = 9;
    
    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private static int          MAX_ID = 9;
    
    /** Paths of the icon files. */
    private static String[]     relPaths = new String[MAX_ID+1];
    
    static {
        relPaths[SAVE] = "nuvola_filesaveas16.png";
        relPaths[CANCEL] = "nuvola_cancel16.png";  
        relPaths[ANNOTATE_BIG] = "nuvola_kwrite48.png";
        relPaths[UP] = "nuvola_up16.png"; 
        relPaths[DOWN] = "nuvola_down16.png"; 
        relPaths[CREATE] = "nuvola_down16.png";
        relPaths[DELETE] = "nuvola_mail_delete16.png";
        relPaths[SAVEWITHRS] = "nuvola_save_all16.png";
        relPaths[SEND_TO_DB] = "nuvola_package_games_board32.png";
        relPaths[VIEWER] = "viewer16.png";
    }
    
    /** The sole instance. */
    private static IconManager  singleton;
    
    
    /** Returns the <code>IconManager</code> object. */
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

