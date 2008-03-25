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
    
    /** The <code>Order by date</code> icon. */
    public static int           ORDER_BY_DATE = 25;
    
    /** The <code>Order by user</code> icon. */
    public static int           ORDER_BY_USER = 26;
    
    /** The <code>Close</code> icon. */
    public static int           CLOSE = 27;
    
    /** The <code>Close</code> icon. */
    public static int           EDIT = 28;
    
    /** The <code>Info 48</code> icon. */
    public static int           INFO_48 = 29;
    
    /** The <code>PDF</code> icon. */
    public static int           PDF_DOC = 30;
    
    /** The <code>Text</code> icon. */
    public static int           TEXT_DOC = 32;
    
    /** The <code>Word</code> icon. */
    public static int           WORD_DOC = 33;
    
    /** The <code>Excel</code> icon. */
    public static int           EXCEL_DOC = 34;
    
    /** The <code>Power point</code> icon. */
    public static int           PPT_DOC = 35;
    
    /** The <code>XML</code> icon. */
    public static int           XML_DOC = 36;
    
    /** The <code>Save</code> icon. */
    public static int           SAVE = 37;
    
    /** The <code>Columns view</code> icon. */
    public static int           COLUMNS_VIEW = 38;
     
    /** The <code>Upload</code> icon. */
    public static int           UPLOAD = 39;
    
    /** The <code>Sort</code> icon. */
    public static int           SORT = 40;
    
    /** The <code>URL</code> 48 icon. */
    public static int           URL_48 = 41;
     
    /** The <code>Attachment 48</code> icon. */
    public static int           ATTACHMENT_48 = 42;
    
	/** 
	 * The maximum ID used for the icon IDs.
	 * Allows to correctly build arrays for direct indexing. 
	 */
	private static int          MAX_ID = 42;

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
		//relPaths[PROJECT] = "nuvola_document16.png";
		relPaths[PROJECT] = "nuvola_folder_blue_open16.png"; 
		relPaths[PROJECT] = "nuvola_folder_blue_open16.png";
        relPaths[DATASET] = "nuvola_folder_image16.png";
        relPaths[IMAGE] = "nuvola_image16.png";
        relPaths[ADD] = "nuvola_image16.png";
        relPaths[REMOVE] = "nuvola_fileclose16.png";
        relPaths[VIEW] = "viewer16.png";
        relPaths[BROWSE] = "zoom16.png";
        relPaths[OWNER] = "nuvola_kdmconfig16.png";
        relPaths[ROOT] = "nuvola_trashcan_empty16.png";
        relPaths[LIST_VIEW] = "nuvola_view_text16.png";
        relPaths[GRID_VIEW] = "nuvola_view_multicolumn16.png";
        relPaths[DOWNLOAD] = "nuvola_download_manager16.png";
        relPaths[LEFT_ARROW] = "nuvola_1leftarrow16.png";
        relPaths[RIGHT_ARROW] = "nuvola_1rightarrow16.png";
        relPaths[DOUBLE_LEFT_ARROW] = "nuvola_2leftarrow16.png";
        relPaths[DOUBLE_RIGHT_ARROW] = "nuvola_2rightarrow16.png";
        relPaths[ORDER_BY_DATE] = "eclipse_trace_persp16.png";
        relPaths[ORDER_BY_USER] = "nuvola_kdmconfig16.png";
        relPaths[CLOSE] = "eclipse_alphab_sort_co16.png";
        relPaths[EDIT] = "nuvola_editpaste16.png";
        relPaths[INFO] = "nuvola_messagebox_info16.png";
        relPaths[INFO_48] = "nuvola_messagebox_info48.png";
        relPaths[PDF_DOC] = "nuvola_acroread22.png";
        relPaths[TEXT_DOC] = "nuvola_acroread22.png";
        relPaths[WORD_DOC] = "alienOSX_Microsoft_Word22.png";
        relPaths[PPT_DOC] = "alienOSX_Microsoft_PowerPoint22.png";
        relPaths[EXCEL_DOC] = "alienOSX_Microsoft_Excel22.png";
        relPaths[XML_DOC] = "nuvola_acroread22.png";
        relPaths[SAVE] = "nuvola_filesaveas16.png";
        relPaths[COLUMNS_VIEW] = "nuvola_view_left_right16.png";
        relPaths[UPLOAD] = "nuvola_download_manager_green_flipped16.png";
        relPaths[SORT] = "nuvola_player_play_gray16.png";
        relPaths[ATTACHMENT_48] = "nuvola_attach48.png";
		relPaths[URL_48] = "nuvola_browser48.png";
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
