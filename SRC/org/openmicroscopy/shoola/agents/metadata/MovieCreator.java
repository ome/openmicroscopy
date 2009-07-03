/*
 * org.openmicroscopy.shoola.agents.metadata.MovieCreator 
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
package org.openmicroscopy.shoola.agents.metadata;


//Java imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.metadata.view.MetadataViewer;
import org.openmicroscopy.shoola.env.data.model.MovieExportParam;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import pojos.FileAnnotationData;
import pojos.ImageData;

/** 
 * 
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
public class MovieCreator 
	extends MetadataLoader
{

    /** Handle to the asynchronous call so that we can cancel it. */
    private CallHandle  			handle;
    
    /** The image to create a movie from. */
    private ImageData 				image;
    
    /** The parameters to use.*/
    private MovieExportParam 		param;
    
    /** The select channels. */
    private List<Integer>			channels;
    
    /** The result. */
    private FileAnnotationData		data;
    
    /**
     * Creates a new instance.
     * 
     * @param viewer 	The view this loader is for.
     *               	Mustn't be <code>null</code>.
     * @param param  	The parameters used to create the movie.
     * @param channels	The selected channels.
     * @param image		The image.
     */
	public MovieCreator(MetadataViewer viewer, MovieExportParam param, 
			List<Integer> channels, ImageData image)
	{
		super(viewer);
		if (image == null)
			throw new IllegalArgumentException("Image Id not valid.");
		if (param == null)
			throw new IllegalArgumentException("Parameters cannot be null.");
		this.param = param;
		if (channels == null) 
			channels = new ArrayList<Integer>();
		this.channels = channels;
		this.image = image;
	}
	
	/**
     * Creates a movie of the selected image.
     * @see MetadataLoader#load()
     */
    public void load()
    {
        handle = ivView.createMovie(image.getId(), channels, param, this);
    }

    /**
     * Cancels the ongoing data retrieval.
     * @see MetadataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Notifies the user that it wasn't possible to retrieve the data and
     * and discards the {@link #viewer}.
     */
    public void handleNullResult() 
    {
    	viewer.uploadMovie(null, null);
    }
    
    /** 
     * Feeds the result back to the viewer. 
     * @see MetadataLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == MetadataViewer.DISCARDED) return;  //Async cancel.
        data = (FileAnnotationData) result;
        if (data == null) return;
        JFrame f = MetadataViewerAgent.getRegistry().getTaskBar().getFrame();
        MessageBox box = new MessageBox(f, "Movie Created",
		"The movie has been created. Do you want to download it?");
		if (box.centerMsgBox() == MessageBox.YES_OPTION) {
			FileChooser chooser = new FileChooser(f, FileChooser.FOLDER_CHOOSER, 
					"Download", "Select where to download the file.");
			chooser.addPropertyChangeListener(new PropertyChangeListener() {
			
				public void propertyChange(PropertyChangeEvent evt) {
					String name = evt.getPropertyName();
					if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
						File folder = (File) evt.getNewValue();
						viewer.uploadMovie(data, folder);
					} else if (FileChooser.CANCEL_SELECTION_PROPERTY.equals(
							name)) {
						viewer.uploadMovie(null, 
								UIUtilities.getDefaultFolder());
					}
				}
			});
			chooser.centerDialog();
		} else {
			viewer.uploadMovie(null, UIUtilities.getDefaultFolder());
		}
    }

}
