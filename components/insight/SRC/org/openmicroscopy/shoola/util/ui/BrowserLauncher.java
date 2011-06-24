/*
 * org.openmicroscopy.shoola.util.ui.BrowserLauncher 
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
package org.openmicroscopy.shoola.util.ui;



//Java imports
import java.awt.Image;
import java.lang.reflect.Method;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies

/** 
 * Launch the specified url.
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
public class BrowserLauncher
{

	/** Array of supported browsers. */
	private static final String[]	BROWSERS_UNIX;
	
	static {
		BROWSERS_UNIX = new String[6];
		BROWSERS_UNIX[0] = "firefox";
		BROWSERS_UNIX[1] = "opera";
		BROWSERS_UNIX[2] = "konqueror";
		BROWSERS_UNIX[3] = "epiphany";
		BROWSERS_UNIX[4] = "mozilla";
		BROWSERS_UNIX[5] = "netscape";
	}
	
	/** The icon displayed in the top-left corner of the message box. */
	private Image topLeftIcon;

	/** Creates a new instance. */
	public BrowserLauncher()
	{
		this(null);
	}
	
	/**
	 * Creates a new instance.
	 * 
	 * @param topLeftIcon The icon displayed in the top-left corner
	 */
	public BrowserLauncher(Image topLeftIcon)
	{
		this.topLeftIcon = topLeftIcon;
		if (this.topLeftIcon == null) {
			ImageIcon icon = 
				IconManager.getInstance().getImageIcon(IconManager.INFO);
			if (icon != null) this.topLeftIcon = icon.getImage();
		}
	}
	
	/**
	 * Opens the url. 
	 * 
	 * @param url The url to open.
	 */
	public void openURL(String url)
	{
		String osName = System.getProperty("os.name");
		try {
			if (osName.contains("Mac")) {
				Class fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL",
											new Class[] {String.class});
				openURL.invoke(null, new Object[] {url});
			} else if (osName.contains("Windows"))
				Runtime.getRuntime().exec(
						"rundll32 url.dll,FileProtocolHandler "+url);
			else { //assume Unix or Linux
				String browser = null;
				for (int count = 0; count < BROWSERS_UNIX.length && 
					browser == null; count++)
					if (Runtime.getRuntime().exec(
							new String[] {"which", 
										BROWSERS_UNIX[count]}).waitFor() == 0)
						browser = BROWSERS_UNIX[count];
				if (browser == null)
					throw new Exception("Could not find web browser");
				Runtime.getRuntime().exec(new String[] {browser, url});
			}
		} catch (Exception e) {
			JFrame f = new JFrame();
			if (topLeftIcon != null) {
				f.setIconImage((Image) topLeftIcon);
			}
			NotificationDialog dialog = new NotificationDialog(
                    f, "Launch Browser", 
                    "Cannot launch The web browser.", 
                    IconManager.getInstance().getIcon(IconManager.INFO_32));
			dialog.pack();  
			UIUtilities.centerAndShow(dialog);
		}
	}
	
}
