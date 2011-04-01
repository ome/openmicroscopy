/*
 * org.openmicroscopy.shoola.agents.fsimporter.IconManager 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.fsimporter;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.AbstractIconManager;

/** 
 * Provides the icons used by the FSImporter.
 * <p>The icons are retrieved by first calling the 
 * {@link #getInstance() getInstance} method and then the 
 * {@link #getIcon(int) getIcon} method passing one of the icon ID's specified
 * by the static constants within this class &#151; icons will be retrieved
 * from the ImViewer's graphics bundle, which implies that its
 * configuration has been read in (this happens during the initialization
 * procedure).</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class IconManager
	extends AbstractIconManager
{
    
	/** The <code>Status Info</code> icon. */
    public  static final int	STATUS_INFO = 0;
    
    /** The <code>Import</code> icon. */
    public  static final int	IMPORT = 1;
    
    /** The <code>Import</code> icon. */
    public  static final int	IMPORT_48 = 2;
    
    /** The <code>Plus 12x12</code> icon. */
    public static final int      PLUS_12 = 3;
    
    /** The <code>Minus 12x12</code> icon. */
    public static final int      MINUS_12 = 4;
    
    /** The <code>Tag 48x48</code> icon. */
    public static final int      TAGS_48 = 5;
    
    /** The <code>Minus 11x11</code> icon. */
    public static final int      MINUS_11 = 6;
    
    /** The <code>Image</code> icon. */
    public static final int      IMAGE = 7;
    
    /** The <code>Directory</code> icon. */
    public static final int      DIRECTORY = 8;
    
    /** The <code>Plate</code> icon. */
    public static final int      PLATE = 9;
    
    /** The <code>Delete</code> icon. */
    public static final int      DELETE = 10;
    
    /** The 22x22 <code>Delete</code> icon. */
    public static final int      DELETE_22 = 11;
    
    /** The 22x22 <code>Apply</code> icon. */
    public static final int 	APPLY_22 = 12;
    
    /** The <code>Project</code> icon. */
    public static final int 	PROJECT = 13;
    
    /** The <code>Dataset</code> icon. */
    public static final int 	DATASET = 14;
    
    /** The <code>Screen</code> icon. */
    public static final int 	SCREEN = 15;
    
    /** The <code>Apply</code> icon. */
    public static final int 	APPLY = 16;
    
    /** The <code>Apply Cancel</code> icon. */
    public static final int 	APPLY_CANCEL = 17;
    
    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private static final int	MAX_ID = 17;
    
    /** Paths of the icon files. */
    private static String[]     relPaths = new String[MAX_ID+1];
    
    static {
    	relPaths[STATUS_INFO] = "nuvola_messagebox_info16.png";
    	relPaths[IMPORT] = "omeroImporterLink16.png";
    	relPaths[IMPORT_48] = "omeroImporterLink48.png";
    	relPaths[PLUS_12] = "plus12.png";
        relPaths[MINUS_12] = "minus12.png";
        relPaths[MINUS_11] = "minus11.png";
        relPaths[TAGS_48] = "nuvola_knotes48.png";
        relPaths[IMAGE] = "nuvola_image16.png";
        relPaths[DIRECTORY] = "nuvola_folder_grey16.png";
        relPaths[PLATE] = "plate16.png";
        relPaths[DELETE] = "nuvola_cancel16.png";
        relPaths[DELETE_22] = "nuvola_cancel22.png";
        relPaths[APPLY_22] = "nuvola_apply22.png";
        relPaths[PROJECT] = "nuvola_folder_darkblue_open16.png";
        relPaths[DATASET] = "nuvola_folder_image16.png";
        relPaths[SCREEN] = "nuvola_folder_blue_open_modified_screen16.png";
        relPaths[APPLY] = "nuvola_apply16.png";
        relPaths[APPLY_CANCEL] = "nuvola_apply_cancel16.png";
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
            singleton = new IconManager(ImporterAgent.getRegistry());
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
