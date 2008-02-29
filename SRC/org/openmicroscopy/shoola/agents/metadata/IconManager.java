/*
 * org.openmicroscopy.shoola.agents.metadata.IconManager 
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
package org.openmicroscopy.shoola.agents.metadata;


//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.ui.AbstractIconManager;

/** 
 * Provides the icons used by the MetadataViewer.
 * <p>The icons are retrieved by first calling the 
 * {@link #getInstance() getInstance} method and then the 
 * {@link #getIcon(int) getIcon} method passing one of the icon ID's specified
 * by the static constants within this class &#151; icons will be retrieved
 * from the MetadataViewer's graphics bundle, which implies that its
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
 * @since OME3.0
 */
public class IconManager 
	extends AbstractIconManager
{

	/** The <code>Refresh</code> icon. */
    public static int           REFRESH = 0;
    
    /** The <code>Collapse</code> icon. */
    public static int           COLLAPSE = 1;
    
    /** The <code>Annotation</code> icon. */
    public static int           ANNOTATION = 2;
    
    /** The <code>Tag</code> icon. */
    public static int           TAG = 3;
    
    /** The <code>Attachment</code> icon. */
    public static int           ATTACHMENT = 4;
    
    /** The <code>Properties</code> icon. */
    public static int           PROPERTIES = 5;
    
    /** The <code>URL</code> icon. */
    public static int           URL = 6;
    
    /** The <code>Image</code> icon. */
    public static int           IMAGE = 7;
    
    /** The <code>Project</code> icon. */
    public static int           PROJECT = 8;
    
    /** The <code>Dataset</code> icon. */
    public static int           DATASET = 9;
    
    /** The <code>Add</code> icon. */
    public static int           ADD = 10;
    
    /** The <code>Remove</code> icon. */
    public static int           REMOVE = 11;
    
    /** The <code>Browse</code> icon. */
    public static int           BROWSE = 12;
    
    /** The <code>View</code> icon. */
    public static int           VIEW = 13;
    
    /** The <code>Owner</code> icon. */
    public static int           OWNER = 14;
    
    /** The <code>Root</code> icon. */
    public static int           ROOT = 15;
    
    /** The <code>List view</code> icon. */
    public static int           LIST_VIEW = 16;
    
    /** The <code>Grid view</code> icon. */
    public static int           GRID_VIEW = 17;
    
    /** The <code>Download</code> icon. */
    public static int           DOWNLOAD = 18;
    
    /** The <code>Info</code> icon. */
    public static int           INFO = 19;
    
    /** The <code>Right arrow</code> icon. */
    public static int           RIGHT_ARROW = 20;
    
    /** The <code>Left arrow</code> icon. */
    public static int           LEFT_ARROW = 21;
    
    /** The <code>Double right arrow</code> icon. */
    public static int           DOUBLE_RIGHT_ARROW = 22;
    
    /** The <code>Double left arrow</code> icon. */
    public static int           DOUBLE_LEFT_ARROW = 23;
    
    /** The <code>Tags 48</code> icon. */
    public static int           TAGS_48 = 24;
    
	/** 
	 * The maximum ID used for the icon IDs.
	 * Allows to correctly build arrays for direct indexing. 
	 */
	private static int          MAX_ID = 24;

	/** Paths of the icon files. */
	private static String[]     relPaths = new String[MAX_ID+1];

	static {
		relPaths[REFRESH] = "nuvola_reload16.png";
		relPaths[COLLAPSE] = "eclipse_collapseall16.png";
		relPaths[ANNOTATION] = "nuvola_kwrite16.png";
		relPaths[TAG] = "nuvola_knotes16.png";
		relPaths[ATTACHMENT] = "nuvola_attach16.png";
		relPaths[PROPERTIES] = "nuvola_kate16.png";
		relPaths[URL] = "nuvola_browser16.png";
		relPaths[PROJECT] = "nuvola_document16.png";
        relPaths[DATASET] = "nuvola_folder_image16.png";
        relPaths[IMAGE] = "nuvola_image16.png";
        relPaths[ADD] = "nuvola_image16.png";
        relPaths[REMOVE] = "nuvola_fileclose16.png";
        relPaths[VIEW] = "viewer16.png";
        relPaths[BROWSE] = "zoom16.png";
        relPaths[OWNER] = "nuvola_kdmconfig16.png";
        relPaths[ROOT] = "nuvola_trashcan_empty16.png";
        relPaths[LIST_VIEW] = "nuvola_mozilla16.png";
        relPaths[GRID_VIEW] = "mozilla_grey16.png";
        relPaths[DOWNLOAD] = "mozilla_grey16.png";
        relPaths[INFO] = "mozilla_grey16.png";
       
        relPaths[LEFT_ARROW] = "nuvola_1leftarrow16.png";
        relPaths[RIGHT_ARROW] = "nuvola_1rightarrow16.png";
        relPaths[DOUBLE_LEFT_ARROW] = "nuvola_2leftarrow16.png";
        relPaths[DOUBLE_RIGHT_ARROW] = "nuvola_2rightarrow16.png";
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
            singleton = new IconManager(MetadataViewerAgent.getRegistry());
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
