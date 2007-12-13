/*
 * org.openmicroscopy.shoola.agents.util.archived.view.DownloaderControl 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2007 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.archived.view;



//Java imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JDialog;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;



//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.TinyLoadingWin;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;

/** 
 * The Downloader's Controller.
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
class DownloaderControl
	implements ChangeListener, PropertyChangeListener
{

	/** 
     * Reference to the {@link Downloader} component, which, in this context,
     * is regarded as the Model.
     */
	private Downloader 		model;
	
	/** Loading window. */
	private TinyLoadingWin	loadinWindow;
	
	/**
     * Creates a new instance.
     * The {@link #initialize() initialize} method 
     * should be called straight 
     * after to link this Controller to the other MVC components.
     * 
     * @param model  Reference to the {@link Classifier} component, which, in 
     *               this context, is regarded as the Model.
     *               Mustn't be <code>null</code>.
     */
	DownloaderControl(Downloader model)
	{
		if (model == null) throw new NullPointerException("No model.");
        this.model = model;
	}
    
	/**
     * Links this Controller to its View.
     * 
     * @param view   Reference to the View. Mustn't be <code>null</code>.
     */
    void initialize(JDialog view)
    {
       if (view == null)
    	   throw new NullPointerException("No view.");
       view.addPropertyChangeListener(FileChooser.LOCATION_PROPERTY, 
				this);
       loadinWindow = new TinyLoadingWin(DownloaderFactory.getOwner(),
    		   "Downloading...");
       model.addChangeListener(this);
    }
    
    /**
     * Reacts to property changed. 
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
	public void propertyChange(PropertyChangeEvent evt) 
	{
		String name = evt.getPropertyName();
		if (FileChooser.LOCATION_PROPERTY.equals(name)) {
			String dir = (String) evt.getNewValue();
			model.download(dir);
		}
	}

    /**
     * Reacts to state changes in the {@link Downloader}.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
	public void stateChanged(ChangeEvent e)
	{
		switch (model.getState()) {
			case Downloader.LOADING:
				loadinWindow.setOnScreen();
				break;
			default:
				loadinWindow.setVisible(false);
				break;
		}
		
	}

}
