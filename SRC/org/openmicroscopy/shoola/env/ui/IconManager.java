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
import java.awt.Image;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.config.IconFactory;
import org.openmicroscopy.shoola.env.config.Registry;

/** 
 * Provides the icons used by the container.
 * <p>This is an utility primarily meant to serve the other classes in this
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
 * <p>Finally, as the <i>OME</i> icon is virtually needed for every title-bar,
 * a public class method is exposed to retrieve it &#151; so agents needn't
 * include that icon in their graphics bundle.</p>
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
{
	
	/** 
	 * The <i>OME</i> logo to be used for title-bars.
	 * We cache it as this icon is used in basically every top-level UI.
	 */
	private static final Icon		OME_ICON = createIcon("graphx/OME16.png");
	
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
	 * The pathname, relative to this class, of the default error icon to
	 * use for notification dialogs. 
	 */
	private static final String		DEFAULT_ERROR_ICON_PATH = 
													"graphx/stock_stop-32.png";
	
	/** 
	 * The pathname, relative to this class, of the default warning icon to
	 * use for notification dialogs.
	 */
	private static final String		DEFAULT_WARN_ICON_PATH = 
										"graphx/stock_dialog-warning-32.png";
													
	/** 
	 * The pathname, relative to this class, of the default info icon to
	 * use for notification dialogs.
	 */													
	private static final String		DEFAULT_INFO_ICON_PATH = 
											"graphx/stock_dialog-info-32.png";
	
	/** ID of the help icon. */
	static final int		HELP = 0;
	
	/** ID of the connect to DS icon. */
	static final int		CONNECT_DS = 1;
	
	/** ID of the exit icon. */
	static final int		EXIT = 2;
		
	/** ID of the login icon. */
	static final int		LOGIN = 3;	
		
	/** 
	 * The maximum ID used for the icon IDs.
	 * Allows to correctly build arrays for direct indexing. 
	 */
	private static int      MAX_ID = 3;
	
	/** Paths of the icon files. */
	private static String[]     relPaths = new String[MAX_ID+1];
	static {  //TODO: modify when icons are ready.
		relPaths[HELP] = "information16.png";
		relPaths[CONNECT_DS] = "information16.png";
		relPaths[EXIT] = "information16.png";
		relPaths[LOGIN] = "information16.png";
	}
	
	/** The sole instance. */
	private static IconManager	singleton;
	
	
	/**
	 * Returns the <i>OME</i> logo to be used for title-bars.
	 * 
	 * @return See above.
	 */
	public static Image getOMEImageIcon()
	{
		//This type cast is OK, see implementation of createIcon.
		return ((ImageIcon) OME_ICON).getImage();
	}
	
	/**
	 * Returns the <i>OME</i> logo.
	 * 
	 * @return See above.
	 */
	public static Icon getOMEIcon()
	{
		return OME_ICON;
	}
	
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
	 * Utility factory method to create an icon from a file.
	 *
	 * @param path    The path of the icon file relative to this class.
	 * @return  An instance of {@link javax.swing.Icon Icon} or
	 * 			<code>null</code> if the path was invalid.
	 */
	private static Icon createIcon(String path)
	{
		URL location = IconManager.class.getResource(path);
		ImageIcon icon = null;
		if (location != null)	icon = new ImageIcon(location);
		return icon;
	}
	
	/**
	 * Returns the <code>IconManager</code> object. 
	 * 
	 * @return	See above.
	 */
	static IconManager getInstance(Registry registry)
	{
		if (singleton == null)	singleton = new IconManager(registry);
		return singleton;
	}
	
	/**
	 * The factory retrieved from the container's configuration.
	 * It can instantiate any icon whose file is contaied in the container's
	 * graphics bundle.
	 */
	private IconFactory 	factory;
	
	/**
	 * Creates a new instance and configures the parameters.
	 * 
	 * @param registry	Reference to the registry.
	 */
	private IconManager(Registry registry)
	{
		factory = (IconFactory) registry.lookup(LookupNames.ICONS_FACTORY);
		if (factory == null) {
			String summary = "Can't retrieve container's icons. ";
			StringBuffer buf = new StringBuffer();
			buf.append("The container's configuration file is probably ");
			buf.append("corrupted.  Please make sure that it contains an ");
			buf.append("entry for the icon factory: ");
			buf.append(LookupNames.ICONS_FACTORY);
			buf.append(".");			
			UserNotifier un = registry.getUserNotifier();
			un.notifyWarning(null, summary, buf.toString());
			registry.getLogger().warn(this, summary + buf.toString());
		}
	}

	/** 
	 * Retrieves the icon specified by the icon <code>id</code>.
	 *
	 * @param   id    Must be one of the ID's defined by this class.
	 * @return  The specified icon or <code>null</code> if the icon couldn't
	 * 			be retrieved or if <code>id</code> is not one of the ID's
	 * 			defined by this class.
	 */    
	Icon getIcon(int id)
	{
		if (factory == null || id < 0 || MAX_ID < id)	return null;
		return factory.getIcon(relPaths[id]);
	}
	
}
