/*
 * org.openmicroscopy.shoola.env.ui.IconManager
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2013 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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

package org.openmicroscopy.shoola.env.ui;

//Java imports
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.Registry;

/** 
 * Provides the icons used by the container.
 * <p>This is an utility only meant to serve the other classes in this
 * package.  The icons are normally retrieved by first calling the 
 * {@link #getInstance(Registry) getInstance} method and then the 
 * {@link #getIcon(int) getIcon} method passing one of the icon ID's specified
 * by the static constants within this class &#151; icons will be retrieved
 * from the container's graphics bundle, which implies that the container
 * configuration has been read in (this happens during the initialization
 * procedure).</p>
 * <p>However, some components (the user notifier and the splash screen) need
 * be independent from the container's initialization (we need to display the
 * splash screen concurrently to the initialization sequence and the user
 * notifier may be needed to tell the user about an error occurred at an
 * arbitrary time during initialization).  For this reason, some class methods
 * are available to retrieve the icons needed by those components in a way that
 * is independent from the container's initialization procedure.</p>
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author  <br>Andrea Falconi &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:a.falconi@dundee.ac.uk">
 * 					a.falconi@dundee.ac.uk</a>
 * @version 2.2 
 * @since OME2.2
 */
public class IconManager
	extends AbstractIconManager
{
	
	/** The pathname, relative to this class, of the splash screen. */
	private static final String		SPLASH_SCREEN = 
									"graphx/client_splashscreen.png";
	
	/** The pathname, relative to this class, of the editor splash screen. */
	private static final String		SPLASH_SCREEN_IMPORTER = 
									"graphx/importer_splashscreen.png";
	
	/** The pathname, relative to this class, of the login splash screen. */
	private static final String		LOGIN_BACKGROUND = 
										"graphx/login_background.png";
	
	/**  The pathname, relative to this class, of the config logo. */
	private static final String		SPLASH_SCREEN_CONFIG_LOGO= 
												"graphx/nuvola_configure48.png";
	
	/** 
	 * The pathname, relative to this class, of the splash screen config button
	 * image. 
	 */
	private static final String		SPLASH_SCREEN_CONFIG = "graphx/config.png";
	
	/** 
	 * The pathname, relative to this class, of the splash screen config button
	 * image when pressed.
	 */
	private static final String		SPLASH_SCREEN_CONFIG_PRESSED= 
													"graphx/config_pressed.png";
	
    
    /** The pathname, relative to this class, of the server icon. */ 
    private static final String     SERVER_LOGO = "graphx/nuvola_server22.png";
    
    /** The pathname, relative to this class, of the arrow right icon. */ 
    private static final String     ARROW_RIGHT = 
    									"graphx/nuvola_1rightarrow16.png";
    
    /** The pathname, relative to this class, of the arrow down icon. */ 
    private static final String     ARROW_DOWN = 
    										"graphx/nuvola_1downarrow16.png";
    
    /** The pathname, relative to this class, of the minus icon. */ 
    private static final String     MINUS = "graphx/nuvola_edit_remove16.png";
    
	/** 
	 * The pathname, relative to this class, of the default error icon to
	 * use for notification dialogs. 
	 */
	private static final String		DEFAULT_ERROR_ICON_PATH = 
												"graphx/nuvola_filetypes32.png";
	
	/** 
	 * The pathname, relative to this class, of the default warning icon to
	 * use for notification dialogs.
	 */
	private static final String		DEFAULT_WARN_ICON_PATH = 
									"graphx/nuvola_messagebox_warning32.png";

	/** 
	 * The pathname, relative to this class, of the default info icon to
	 * use for notification dialogs.
	 */
	private static final String		DEFAULT_INFO_ICON_PATH = 
										"graphx/nuvola_messagebox_info32.png";
	
    /** 
     * The pathname, relative to this class, of the default close icon to
     * use for tiny window dialogs.
     */ 
    private static final String     DEFAULT_CLOSE_PATH = "graphx/cross.png";
    
    /** 
     * The pathname, relative to this class, of the default close icon to
     * use for tiny window dialogs.
     */ 
    private static final String     DEFAULT_CLOSE_OVER_PATH = 
                                            "graphx/cross_over.png";
    
    /** 
     * The pathname, relative to this class, of the default minus icon to
     * use for tiny window dialogs.
     */ 
    private static final String     DEFAULT_MINUS_PATH = "graphx/minus.png";
    
    /** 
     * The pathname, relative to this class, of the default minus_over icon to
     * use for tiny window dialogs.
     */ 
    private static final String     DEFAULT_MINUS_OVER_PATH = 
                                            "graphx/minus_over.png";
    
    /** 
     * The pathname, relative to this class, of the default plus icon to
     * use for tiny window dialogs.
     */ 
    private static final String     DEFAULT_PLUS_PATH = "graphx/plus.png";
    
    /** 
     * The pathname, relative to this class, of the default plus_over icon to
     * use for tiny window dialogs.
     */ 
    private static final String     DEFAULT_PLUS_OVER_PATH = 
                                        "graphx/plus_over.png";
    	
    /** 
     * The pathname, relative to this class, of the logo icon to
     * use for the about software dialog.
     */  
    private static final String		LOGO_ABOUT = "graphx/ome64.png";
    
    /** 
     * The pathname, relative to this class, of the upgrade icon.
     */  
    private static final String		UPGRADE = "graphx/ome64.png";
    
    /** 
     * The pathname, relative to this class, of the upgrade icon.
     */  
    private static final String		RESULTS = "graphx/nuvola_kig48.png";
    
	/** ID of the <code>help</code> icon. */
	static final int		HELP = 0;
	
	/** ID of the <code>connect to DS</code> icon. */
	static final int		CONNECT_DS = 1;
	
	/** ID of the <code>disconnect from DS</code> icon. */
	static final int		DISCONNECT_DS = 2;
	
	/** ID of the <code>exit</code> icon. */
	static final int		EXIT = 3;
		
	/** ID of the <code>login init</code> icon. */
	static final int		LOGIN_INIT = 4;

	/** ID of the <code>login</code> icon. */
	static final int		LOGIN = 5;
	
	/** ID of the <code>connect to DS</code> big icon. */
	static final int		CONNECT_DS_BIG = 6;
	
	/** ID of the <code>welcome</code> icon. */
	static final int		WELCOME = 7;
	
	/** ID of the <code>how to</code> icon. */
	static final int		HOW_TO = 8;
	
	/** ID of the <code>software updates</code> icon. */
	static final int		SW_UPDATES = 9;
	
    /** ID of the <code>question</code> icon. */
    static final int        QUESTION = 10;
    
    /** ID of the <code>comment</code> icon. */
    static final int        COMMENT = 11;
    
    /** ID of the <code>document</code> icon. */
    static final int        DOCUMENT_12 = 12;
    
    /** ID of the <code>document</code> icon. */
    static final int        DOCUMENT_32 = 13;
    
    /** ID of the <code>cancel</code> icon. */
    static final int        CANCEL = 14;
    
    /** ID of the <code>remove</code> icon. */
    static final int        REMOVE = 15;
    
    /** ID of the <code>forum</code> icon. */
    static final int        FORUM = 16;
    
    /** A 22x22 <code>Download</code> icon. */
    static final int        DOWNLOAD_22 = 17;
    
    /** A 48x48 <code>Download</code> icon. */
    static final int        DOWNLOAD_48 = 18;
    
    /** ID of the <code>Activity</code> icon. */
    static final int        ACTIVITY = 19;
    
    /** ID of the <code>Log File</code> icon. */
    static final int        LOG_FILE = 20;
    
    /** ID of the <code>Plot</code> icon. */
    static final int        PLOT_48 = 21;
    
	/** 
	 * The maximum ID used for the icon IDs.
	 * Allows to correctly build arrays for direct indexing. 
	 */
	private static int      MAX_ID = 21;
	
	/** Paths of the icon files. */
	private static String[]     relPaths = new String[MAX_ID+1];
	static {  
		relPaths[HELP] = "nuvola_help16.png";
		relPaths[CONNECT_DS] = "nuvola_server16.png";
		relPaths[DISCONNECT_DS] = "server_disconn16.png";
		relPaths[EXIT] = "nuvola_exit16.png";
		relPaths[LOGIN_INIT] = "nuvola_button_cancel16.png";
		relPaths[LOGIN] = "nuvola_apply16.png";
		relPaths[CONNECT_DS_BIG] = "nuvola_server48.png";
		relPaths[WELCOME] = "nuvola_background16.png";
		relPaths[HOW_TO] = "nuvola_artscontrol16.png";
		relPaths[SW_UPDATES] = "nuvola_messagebox_info16.png";
        relPaths[QUESTION] = "nuvola_filetypes48.png";
        relPaths[COMMENT] = "nuvola_mail_send16.png";
        relPaths[DOCUMENT_12] = "nuvola_fileexport12.png";
        relPaths[DOCUMENT_32] = "nuvola_fileexport32.png";
        relPaths[CANCEL] = "nuvola_background16.png";
		relPaths[REMOVE] = "nuvola_button_cancel16.png";
		relPaths[DOWNLOAD_22] = "nuvola_download_manager22.png";
        relPaths[DOWNLOAD_48] = "nuvola_download_manager48.png";
        relPaths[FORUM] = "nuvola_chat16.png";
        relPaths[ACTIVITY] = "nuvola_kbounce16.png";
        relPaths[LOG_FILE] = "nuvola_kaddressbook16.png";
        relPaths[PLOT_48] = "nuvola_kmplot48.png";
	}
	
	/** The sole instance. */
	private static IconManager	singleton;

	/**
	 * Returns the splash screen.
	 * 
	 * @return See above.
	 */
	static Icon getSplashScreen()
	{
		return createIcon(SPLASH_SCREEN);
	}
	
	/**
	 * Returns the splash screen when the import is a standalone application.
	 * 
	 * @return See above.
	 */
	static Icon getImporterSplashScreen()
	{
		return createIcon(SPLASH_SCREEN_IMPORTER);
	}
	
	/**
	 * Returns the image of the server icon within the server dialog.
	 * 
	 * @return See above.
	 */
	static Icon getServer()
	{
		return createIcon(SERVER_LOGO);
	}
	
	/**
	 * Returns the image of the login button within the splash screen.
	 * 
	 * @return See above.
	 */
	static Icon getConfigButton()
	{
		return createIcon(SPLASH_SCREEN_CONFIG);
	}
	
	/**
	 * Returns the image of the login button within the splash screen.
	 * 
	 * @return See above.
	 */
	static Icon getConfigLogo()
	{
		return createIcon(SPLASH_SCREEN_CONFIG_LOGO);
	}
	
	/**
	 * Returns the image of the login button within the splash screen.
	 * 
	 * @return See above.
	 */
	static Icon getConfigButtonPressed()
	{
		return createIcon(SPLASH_SCREEN_CONFIG_PRESSED);
	}
	
	/**
	 * Returns the image of the options button within the server dialog.
	 * 
	 * @return See above.
	 */
	static Icon getArrowDown() { return createIcon(ARROW_DOWN); }
	
	/**
	 * Returns the image of the options button within the server dialog.
	 * 
	 * @return See above.
	 */
	static Icon getArrowRight() { return createIcon(ARROW_RIGHT); }

	/**
	 * Returns the image of the remove button within the server dialog.
	 * 
	 * @return See above.
	 */
	static Icon getMinus() { return createIcon(MINUS); }
	
	/**
	 * Returns the icon to use for the software update dialog.
	 * 
	 * @return See above.
	 */
	static Icon getLogoAbout() { return createIcon(LOGO_ABOUT); }
	
	/**
	 * Returns the default warning icon to use for notification dialogs.
	 * 
	 * @return See above.
	 */
	static Icon getDefaultWarnIcon()
	{
		return createIcon(DEFAULT_WARN_ICON_PATH);
	}
	
	/**
	 * Returns the default info icon to use for notification dialogs.
	 * 
	 * @return See above.
	 */
	static Icon getDefaultInfoIcon()
	{
		return createIcon(DEFAULT_INFO_ICON_PATH);
	}

	/**
	 * Returns the image of the options button within the server dialog.
	 * 
	 * @return See above.
	 */
	static Icon getResults() { return createIcon(RESULTS); }

	/**
	 * Returns the default error icon to use for notification dialogs.
	 * 
	 * @return See above.
	 */
	public static Icon getDefaultErrorIcon()
	{
		return createIcon(DEFAULT_ERROR_ICON_PATH);
	}
	
	/**
	 * Returns the image of the login button within the splash screen.
	 * 
	 * @return See above.
	 */
	public static Icon getLoginBackground()
	{
		return createIcon(LOGIN_BACKGROUND);
	}
	
    /**
     * Returns the default close icon to use for tiny window dialog.
     * 
     * @return See above.
     */
    public static Icon getDefaultCloseIcon()
    {
        return createIcon(DEFAULT_CLOSE_PATH);
    }
    
    /**
     * Returns the default close_over icon to use for tiny window dialog.
     * 
     * @return See above.
     */
    public static Icon getDefaultCloseOverIcon()
    {
        return createIcon(DEFAULT_CLOSE_OVER_PATH);
    }
    
    /**
     * Returns the default minus icon to use for tiny window dialog.
     * 
     * @return See above.
     */
    public static Icon getDefaultMinusIcon()
    {
        return createIcon(DEFAULT_MINUS_PATH);
    }
    
    /**
     * Returns the default size icon to use for tiny window dialog.
     * 
     * @return See above.
     */
    public static Icon getDefaultMinusOverIcon()
    {
        return createIcon(DEFAULT_MINUS_OVER_PATH);
    }
    
    /**
     * Returns the default minus icon to use for tiny window dialog.
     * 
     * @return See above.
     */
    public static Icon getDefaultPlusIcon()
    {
        return createIcon(DEFAULT_PLUS_PATH);
    }
    
    /**
     * Returns the default size icon to use for tiny window dialog.
     * 
     * @return See above.
     */
    public static Icon getDefaultPlusOverIcon()
    {
        return createIcon(DEFAULT_PLUS_OVER_PATH);
    }
    
    /**
	 * Returns the upgrade icon to use for notification dialogs.
	 * 
	 * @return See above.
	 */
	public static Icon getUpgradeIcon() { return createIcon(UPGRADE); }
	
	/**
	 * Returns the <code>IconManager</code> object. 
	 * 
     * @param registry Reference to the {@link Registry}.
	 * @return	See above.
	 */
	static IconManager getInstance(Registry registry)
	{
		if (singleton == null)	singleton = new IconManager(registry);
		return singleton;
	}
	
    /**
     * Creates a new instance and configures the parameters.
     * 
     * @param registry  Reference to the registry.
     */
    private IconManager(Registry registry)
    {
        super(registry, LookupNames.ICONS_FACTORY, relPaths);
    }

}
