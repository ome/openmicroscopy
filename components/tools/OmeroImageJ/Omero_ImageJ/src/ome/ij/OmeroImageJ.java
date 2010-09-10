/*
 * ome.ij.OmeroImageJ 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package ome.ij;


//Java imports
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

//Third-party libraries
import ij.IJ;
import ij.ImageJ;
import ij.plugin.PlugIn;

//Application-internal dependencies
import ome.ij.data.ServicesFactory;
import org.openmicroscopy.shoola.util.ui.MacOSMenuHandler;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * Entry point of the <code>OMERO ImageJ</code> plugin.
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
public class OmeroImageJ 
	implements PlugIn, PropertyChangeListener
{

	/** Version of ImageJ required. */
	private static final String IJ_VERSION = "1.39u";
	
	/** Reacts to the application closing. */
	private void onImageJClosing()
	{
		ServicesFactory.getInstance().exitPlugin();
	}
	
	/** Attaches listeners to the IJ instance. */
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
				view.addPropertyChangeListener(
						MacOSMenuHandler.QUIT_APPLICATION_PROPERTY, this);
			} catch (Throwable e) {
				IJ.log("Cannot listen to the Quit action of the menu.");
			}
		}
	}
	
	/**
	 * Implemented as specified by the application.
	 * @see PlugIn#run(String)
	 */
	public void run(String arg)
	{
		if (IJ.versionLessThan(IJ_VERSION))	 {
			IJ.showMessage(LoginScreenManager.TITLE,
					"This plugin requires ImageJ\n"+IJ_VERSION+
					"or later. Your version is "+IJ.getVersion()+
					"; you will need to upgrade.");
			return;
		}
		attachListeners();
		LoginScreenManager manager = new LoginScreenManager();
		manager.start();
	}

	/**
	 * Reacts to property change fired by the <code>Quit</code> action.
	 * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) { onImageJClosing(); }
	
}
