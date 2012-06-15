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
    
    /** The <code>Refresh</code> icon. */
    public static final int 	REFRESH = 18;
    
    /** The <code>Switch location</code> icon. */
    public static final int 	SWITCH_LOCATION = 19;
    
    /** The <code>Delete 48x48</code> icon. */
    public static final int 	DELETE_48 = 20;
    
    /** The 16x16 <code>Personal</code> icon. */
    public static final int		PERSONAL = 21;
    
    /** The <code>Private Group</code> icon. */
    public static final int		PRIVATE_GROUP = 22;
    
    /** The <code>Read Group</code> icon. */
    public static final int		READ_GROUP = 23;
    
    /** The <code>Read Link Group</code> icon. */
    public static final int		READ_LINK_GROUP = 24;
    
    /** The <code>Public Group</code> icon. */
    public static final int		PUBLIC_GROUP = 25;
    
    /** The <code>Up down</code> icon. */
    public static final int		UP_DOWN_9_12 = 26;
    
    /** The 12x12 <code>Private Group Drop Down</code> icon. */
    public static final int		PRIVATE_GROUP_DD_12 = 27;
    
    /** The 12x12 <code>Read Group Drop Down</code> icon. */
    public static final int		READ_GROUP_DD_12 = 28;
    
    /** The 12x12 <code>Read Link Group Drop Down</code> icon. */
    public static final int		READ_LINK_GROUP_DD_12 = 29;
    
    /** The 12x12 <code>Public Group Drop Down</code> icon. */
    public static final int		PUBLIC_GROUP_DD_12 = 30;
    
    /** The <code>Exit</code> icon. */
    public static final int		EXIT_APPLICATION = 31;
    
    /** The <code>Debug</code> icon. */
    public static final int		DEBUG = 32;
    
    /** The <code>Log in</code> icon. */
    public static final int		LOGIN = 33;
    
    /** The <code>Read Link Group</code> icon. */
    public static final int		READ_WRITE_GROUP = 34;
    
    /** The 12x12 <code>Read Link Group Drop Down</code> icon. */
    public static final int		READ_WRITE_GROUP_DD_12 = 35;
    
    /** 
     * The maximum ID used for the icon IDs.
     * Allows to correctly build arrays for direct indexing. 
     */
    private static final int	MAX_ID = 35;
    
    /** Paths of the icon files. */
    private static String[]     relPaths = new String[MAX_ID+1];
    
    static {
    	relPaths[STATUS_INFO] = "nuvola_messagebox_info16.png";
    	relPaths[IMPORT] = "omeroImporter16.png";
    	relPaths[IMPORT_48] = "omeroImporter48.png";
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
        relPaths[REFRESH] = "nuvola_reload16.png";
        relPaths[SWITCH_LOCATION] = "eclipse_hierarchy_co16.png";
        relPaths[DELETE_48] = "nuvola_cancel48.png";
        relPaths[PERSONAL] = "nuvola_personal16.png";
        relPaths[PRIVATE_GROUP] = "private16.png";//"nuvola_ledred16.png";
        relPaths[READ_GROUP] = "group_read16.png";//"nuvola_ledorange_readOnly16.png";
        relPaths[READ_LINK_GROUP] = "group_read_annotate16.png";//"nuvola_ledorange16.png";
        relPaths[PUBLIC_GROUP] = "public_read16.png";//"nuvola_ledgreen16.png";
        relPaths[READ_WRITE_GROUP] = "group_read_write16.png";//"nuvola_ledorange16.png";
        
        relPaths[UP_DOWN_9_12] = "upDown.png";
        relPaths[PRIVATE_GROUP_DD_12] = "nuvola_permission_private_dd12.png";
        relPaths[READ_GROUP_DD_12] = "nuvola_permission_readOnly_dd12.png";
        relPaths[READ_LINK_GROUP_DD_12] = "nuvola_permission_read_dd12.png";//"nuvola_permission_readLink_dd12.png";
        relPaths[PUBLIC_GROUP_DD_12] = "nuvola_permission_public_dd12.png";
        relPaths[READ_WRITE_GROUP_DD_12] = "nuvola_permission_read_dd12.png";
        relPaths[EXIT_APPLICATION] = "nuvola_exit16.png";
        relPaths[DEBUG] = "nuvola_bug16.png";
        relPaths[LOGIN] = "nuvola_login16.png";
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
