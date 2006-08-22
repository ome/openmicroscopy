/*
 * org.openmicroscopy.shoola.env.ui.IconManager
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
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class IconManager
	extends AbstractIconManager
{
	
	/** The pathname, relative to this class, of the splash screen. */
	private static final String		SPLASH_SCREEN = "graphx/splash.jpg";

	/** 
	 * The pathname, relative to this class, of the splash screen login button
	 * image. 
	 */
	private static final String		SPLASH_SCREEN_LOGIN = 
														"graphx/login_out.jpg";

	/** 
	 * The pathname, relative to this class, of the splash screen login button
	 * image displayed when the mouse is over the button. 
	 */
	private static final String		SPLASH_SCREEN_LOGIN_OVER = 
														"graphx/login_over.jpg";
		
    /** 
     * The pathname, relative to this class, of the splash screen cancel button
     * image. 
     */
    private static final String     SPLASH_SCREEN_CANCEL = 
                                                        "graphx/cancel_out.jpg";

    /** 
     * The pathname, relative to this class, of the splash screen cancel button
     * image displayed when the mouse is over the button. 
     */
    private static final String     SPLASH_SCREEN_CANCEL_OVER = 
                                                "graphx/cancel_over.jpg";
    
	/** 
	 * The pathname, relative to this class, of the default error icon to
	 * use for notification dialogs. 
	 */
	private static final String		DEFAULT_ERROR_ICON_PATH = 
												"graphx/nuvola_cancel32.png";
	
	/** 
	 * The pathname, relative to this class, of the default warning icon to
	 * use for notification dialogs.
	 */
	private static final String		DEFAULT_WARN_ICON_PATH = 
										"graphx/nuvola_important32.png";
													
	/** 
	 * The pathname, relative to this class, of the default info icon to
	 * use for notification dialogs.
	 */													
	private static final String		DEFAULT_INFO_ICON_PATH = 
											"graphx/stock_dialog-info-32.png";
	
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
    
	/** ID of the help icon. */
	static final int		HELP = 0;
	
	/** ID of the connect to DS icon. */
	static final int		CONNECT_DS = 1;
	
	/** ID of the disconnect from DS icon. */
	static final int		DISCONNECT_DS = 2;
	
	/** ID of the exit icon. */
	static final int		EXIT = 3;
		
	/** ID of the login icon. */
	static final int		LOGIN_INIT = 4;

	/** ID of the login icon. */
	static final int		LOGIN = 5;
	
	/** ID of the connect to DS icon. */
	static final int		CONNECT_DS_BIG = 6;
	
	/** ID of the welcome icon. */
	static final int		WELCOME = 7;
	
	/** ID of the how to icon. */
	static final int		HOW_TO = 8;
	
	/** ID of the software updates icon. */
	static final int		SW_UPDATES = 9;
	
			
	/** 
	 * The maximum ID used for the icon IDs.
	 * Allows to correctly build arrays for direct indexing. 
	 */
	private static int      MAX_ID = 9;
	
	/** Paths of the icon files. */
	private static String[]     relPaths = new String[MAX_ID+1];
	static {  
		relPaths[HELP] = "nuvola_help16.png";
		relPaths[CONNECT_DS] = "nuvola_server16.png";
		relPaths[DISCONNECT_DS] = "server_disconn16.png";
		relPaths[EXIT] = "OpenOffice_stock_exit-16.png";
		relPaths[LOGIN_INIT] = "nuvola_button_cancel16.png";
		relPaths[LOGIN] = "nuvola_apply16.png";
		relPaths[CONNECT_DS_BIG] = "nuvola_server48.png";
		relPaths[WELCOME] = "nuvola_background16.png";
		relPaths[HOW_TO] = "nuvola_artscontrol16.png";
		relPaths[SW_UPDATES] = "nuvola_download_manager16.png";
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
	 * Returns the image of the login button within the splash screen.
	 * 
	 * @return See above.
	 */
	static Icon getLoginButton()
	{
		return createIcon(SPLASH_SCREEN_LOGIN);
	}
	
	/**
	 * Returns the rollover image of the login button within the splash screen.
	 * 
	 * @return See above.
	 */
	static Icon getLoginButtonOver()
	{
		return createIcon(SPLASH_SCREEN_LOGIN_OVER);
	}
    
    /**
     * Returns the image of the cancel button within the splash screen.
     * 
     * @return See above.
     */
    static Icon getCancelButton()
    {
        return createIcon(SPLASH_SCREEN_CANCEL);
    }
    
    /**
     * Returns the rollover image of the cancel button within the splash screen.
     * 
     * @return See above.
     */
    static Icon getCancelButtonOver()
    {
        return createIcon(SPLASH_SCREEN_CANCEL_OVER);
    }

	/**
	 * Returns the default error icon to use for notification dialogs.
	 * 
	 * @return See above.
	 */
	static Icon getDefaultErrorIcon()
	{
		return createIcon(DEFAULT_ERROR_ICON_PATH);
	}
	
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
