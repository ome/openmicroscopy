/*
 * org.openmicroscopy.shoola.MainIJPlugin 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola;


//Java imports
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URL;
import java.security.CodeSource;

//Third-party libraries
import ij.IJ;
import ij.ImageJ;
import ij.plugin.PlugIn;

//Application-internal dependencies
import org.openmicroscopy.shoola.env.Container;
import org.openmicroscopy.shoola.env.LookupNames;
import org.openmicroscopy.shoola.env.data.DataServicesFactory;
import org.openmicroscopy.shoola.env.log.LogMessage;
import org.openmicroscopy.shoola.util.ui.MacOSMenuHandler;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Starts the application as an <code>ImageJ</code> plugin.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since Beta4.4
 */
public class MainIJPlugin 
	implements PlugIn
{

	/** Minimum version of ImageJ required. */
	private static final String IJ_VERSION = "1.39u";
	
	/** The title of the splash screens. */
	private static final String	TITLE = "Open Microscopy Environment";
	
	/** Reference to the container.*/
	private Container container;
	
	/** Notifies that <code>ImageJ</code> is closing.*/
	private void onImageJClosing()
	{
		if (container == null) return;
		try {
			DataServicesFactory.getInstance(container).exitApplication(true,
					true);
		} catch (Exception e) {
			LogMessage msg = new LogMessage();
			msg.println("Exit Plugin:"+UIUtilities.printErrorText(e));
			container.getRegistry().getLogger().info(this, msg);
		}
	}
	
	/** Attaches listeners to the IJ instance.*/
	private void attachListeners()
	{
		ImageJ view = IJ.getInstance();
		view.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				onImageJClosing();
			}
		});
		if (UIUtilities.isMacOS()) {
			try {
				MacOSMenuHandler handler = new MacOSMenuHandler(view);
				handler.initialize();
				view.addPropertyChangeListener(new PropertyChangeListener() {
					
					public void propertyChange(PropertyChangeEvent evt) {
						String name = evt.getPropertyName();
						if (MacOSMenuHandler.QUIT_APPLICATION_PROPERTY.equals(
								name))
							onImageJClosing();
					}
				});
			} catch (Throwable e) {
				container.getRegistry().getLogger().info(this, 
						"Cannot listen to the Quit action of the menu.");
			}
		}
	}
	
	/**
	 * Runs the application as an <code>ImageJ</code> plugin.
	 * @see PlugIn#run(String)
	 */
	public void run(String args)
	{
		if (IJ.versionLessThan(IJ_VERSION))	 {
			IJ.showMessage(TITLE,
					"This plugin requires ImageJ\n"+IJ_VERSION+
					"or later. Your version is "+IJ.getVersion()+
					"; you will need to upgrade.");
			return;
		}
		String homeDir = "";
		String configFile = null;
		
		if (args != null) {
			String[] values = args.split(" ");
			if (values.length > 0) configFile = values[0];
			if (values.length > 1) homeDir = values[1];
		}
		if (homeDir.length() == 0) {
			CodeSource src = 
				MainIJPlugin.class.getProtectionDomain().getCodeSource();
			try {
				File jarFile = new File(src.getLocation().toURI().getPath());
			    homeDir = jarFile.getParentFile().getPath();
			} catch (Exception e) {}
		}
		attachListeners();
		container = Container.startupInPluginMode(homeDir, configFile,
				LookupNames.IMAGE_J);
	}

}
