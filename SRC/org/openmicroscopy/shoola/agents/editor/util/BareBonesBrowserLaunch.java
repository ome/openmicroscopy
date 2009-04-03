/*
 * org.openmicroscopy.shoola.agents.editor.util.BareBonesBrowserLaunch 
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

package org.openmicroscopy.shoola.agents.editor.util;

// Java Imports

import java.lang.reflect.Method;
import javax.swing.JOptionPane;

//Third-party libraries

//Application-internal dependencies

/** 
 * A class with static method for opening URLs in a web-browser.
 * 
 * It also seems to work for opening local files in their default 
 * application. E.g. word, excell, pdf files etc, as long as the
 * URL is valid "file:///Users/will/myWordDoc.doc"
 * 
 * The code comes from here:
 * /////////////////////////////////////////////////////////
 * //Bare Bones Browser Launch                          //
 * //Version 1.5                                        //
 * //December 10, 2005                                  //
 * //Supports: Mac OS X, GNU/Linux, Unix, Windows XP    //
 * //Example Usage:                                     //
 * // String url = "http://www.centerkey.com/";       //
 * // BareBonesBrowserLaunch.openURL(url);            //
 * //Public Domain Software -- Free to Use as You Like  //
 * /////////////////////////////////////////////////////////
 *
 * @author  William Moore &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:will@lifesci.dundee.ac.uk">will@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class BareBonesBrowserLaunch 
{

	/**
	 * Launches a Browser (if url is a URL)
	 * If url is a path to local file, e.g. "file:///Users/will/myWordDoc.doc"
	 * this method seems to open it in the default application.
	 * 
	 * @param url	A URL (or file path).
	 */
	public static void openURL(String url) {
	
		String osName = System.getProperty("os.name");
		try {
			if (osName.startsWith("Mac OS")) {
				Class fileMgr = Class.forName("com.apple.eio.FileManager");
				Method openURL = fileMgr.getDeclaredMethod("openURL",
						new Class[] {String.class});
				openURL.invoke(null, new Object[] {url});
			}
			else if (osName.startsWith("Windows"))
				Runtime.getRuntime().exec(
						"rundll32 url.dll,FileProtocolHandler " + url);
			else { //assume Unix or Linux
				String[] browsers = {
						"firefox", "opera", "konqueror", "epiphany", 
						"mozilla", "netscape" };
				String browser = null;
				for (int count = 0; 
						count < browsers.length && browser == null; 
						count++) {
					if (Runtime.getRuntime().exec(new String[] 
					                {"which", browsers[count]}).waitFor() == 0)
						browser = browsers[count];
				}
        	if (browser == null) {
        		throw new Exception("Could not find web browser");
        	}
        	Runtime.getRuntime().exec(new String[] {browser, url});
        }
     }
  catch (Exception e) {
     JOptionPane.showMessageDialog(null, "Could not open "
    		 + url + "\n" + e.getLocalizedMessage());
     }
  }

}

