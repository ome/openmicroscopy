/*
 * org.openmicroscopy.shoola.agents.datamng.IconManager
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

package org.openmicroscopy.shoola.agents.datamng;


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
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class IconManager
	extends AbstractIconManager
{ 
		
	/** ID of the project icon. */
	public static final int     PROJECT = 0;
		
	/** ID of the dataset icon. */
	public static final int     DATASET = 1;
		
	/** ID of the image icon. */
	public static final int     IMAGE = 2;
	
	/** ID of the icon of the save to DB button. */
	public static final int     SAVE_DB = 3;
		
	/** ID of the icon of the reload from DB button. */
	public static final int     RELOAD_DB = 4;
  
	/** ID of the information icon. */
	public static final int     INFO = 5;
	
	/** ID of the properties icon used by the popup menu. */
	public static final int		PROPERTIES = 6;
	
	/** ID of the viewer icon used by the popup menu. */
	public static final int		VIEWER = 7;

	/** ID of the browser icon used by the popup menu. */
	public static final int		BROWSER = 8;

	/** ID of the refresh icon used by the popup menu. */
	public static final int		REFRESH = 9;

	/** ID of the annotate icon used by the popup menu. */
	public static final int		ANNOTATE = 10;
	
	/** ID of the datamanager icon. */
	public static final int		DMANAGER = 11;
	
	/** ID of the datamanager icon. */
	public static final int		ROOT = 12;
	
	/** ID of the import image icon. */
	public static final int		IMPORT_IMAGE = 13;
	
	/** ID of the owner icon. */
	public static final int		OWNER = 14;
	
	/** ID of the explorer icon. */
	public static final int		EXPLORER = 15;	
	
	/** ID of the createProject icon. */
	public static final int		CREATE_PROJECT = 16;
	
	/** ID of the createDataset icon. */
	public static final int		CREATE_DATASET = 17;	
	
	/** ID of the createproject big icon. */
	public static final int		CREATE_PROJECT_BIG = 18;
	
	/** ID of the createDataset big icon. */
	public static final int		CREATE_DATASET_BIG = 19;
	
	/** ID of the project big icon. */
	public static final int		PROJECT_BIG = 20;
	
	/** ID of the dataset big icon. */
	public static final int		DATASET_BIG = 21;
		
	/** ID of the image big icon. */
	public static final int		IMAGE_BIG = 22;
	
	/** ID of the image big icon. */
	public static final int		IMPORT_IMAGE_BIG = 23;	
	
	/** ID of the orderByName icon. */
	public static final int		ORDER_BY_NAME_UP = 24;
	
	/** ID of the orderByName icon. */
	public static final int		ORDER_BY_NAME_DOWN = 25;
	
	/** ID of the orderByDate icon. */
	public static final int		ORDER_BY_DATE_UP = 26;
	
	/** ID of the orderByDate icon. */
	public static final int		ORDER_BY_DATE_DOWN = 27;
	
	public static final int		ORDER_BY_SELECTED_UP = 28;
	
	public static final int		ORDER_BY_SELECTED_DOWN = 29;
	
	/** ID of the filter icon. */
	public static final int		FILTER = 30;
	
    public static final int     SEND_TO_DB = 31;
    
	/** 
	 * The maximum ID used for the icon IDs.
	 * Allows to correctly build arrays for direct indexing. 
	 */
	private static int          MAX_ID = 31;
	
	/** Paths of the icon files. */
	private static String[]     relPaths = new String[MAX_ID+1];
	static {
		relPaths[PROJECT] = "nuvola_document16.png";
		relPaths[DATASET] = "nuvola_folder_image16.png";
		relPaths[IMAGE] = "nuvola_image16.png";
		relPaths[SAVE_DB] = "nuvola_save_all16.png";
		relPaths[RELOAD_DB] = "reload_DB30.png";
		relPaths[INFO] = "nuvola_info16.png";
		relPaths[PROPERTIES] = "nuvola_kate16.png";
		relPaths[VIEWER] = "viewer16.png";
		relPaths[BROWSER] = "browser-small.png";
		relPaths[REFRESH] = "nuvola_reload16.png";
		relPaths[ANNOTATE] = "nuvola_kwrite16.png";
		relPaths[DMANAGER] = "nuvola_file-manager16.png";
		relPaths[ROOT] = "nuvola_trashcan_empty16.png";
		relPaths[IMPORT_IMAGE] = "nuvola_digikam16.png";
		relPaths[OWNER] = "nuvola_kdmconfig16.png";
		relPaths[EXPLORER] = "eclipse_hierarchy_co16.png";	
		relPaths[CREATE_PROJECT] = "nuvola_filenew16.png";				
		relPaths[CREATE_DATASET] = "nuvola_folder_new16.png";
		relPaths[CREATE_PROJECT_BIG] = "nuvola_filenew48.png";
		relPaths[CREATE_DATASET_BIG] = "nuvola_folder_new48.png";
		relPaths[PROJECT_BIG] = "nuvola_document48.png";
		relPaths[DATASET_BIG] = "nuvola_folder_image48.png";
		relPaths[IMPORT_IMAGE_BIG] = "nuvola_digikam48.png";
		relPaths[IMAGE_BIG] = "nuvola_image48.png";
		relPaths[ORDER_BY_NAME_UP] = "alphab_sort_co_with_arrow_up.png";
		relPaths[ORDER_BY_NAME_DOWN] = "alphab_sort_co_with_arrow_down.png";
		relPaths[ORDER_BY_DATE_UP] = "clock_with_arrow_up.png";
		relPaths[ORDER_BY_DATE_DOWN] = "clock_with_arrow_down.png";
		relPaths[ORDER_BY_SELECTED_UP] = "endturn_with_arrow_up.png";
		relPaths[ORDER_BY_SELECTED_DOWN] = "endturn_with_arrow_down.png";
		relPaths[FILTER] = "eclipse_filter_ps16.png";		
        relPaths[SEND_TO_DB] = "nuvola_package_games_board32.png";
	}
	
	/** The sole instance. */
	private static IconManager	singleton;
	
	
	/** Returns the <code>IconManager</code> object. */
	public static IconManager getInstance(Registry registry)
	{
		if (singleton == null)	singleton = new IconManager(registry);
		return singleton;
	}
	
	/**
	 * Creates a new instance and configures the parameters.
	 * 
	 * @param registry	Reference to the registry.
	 */
	private IconManager(Registry registry)
	{
		super(registry, "/resources/icons/Factory", relPaths);
	}
	
}
