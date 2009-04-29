/*
 * org.openmicroscopy.shoola.agents.imviewer.MovieCreator 
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
package org.openmicroscopy.shoola.agents.imviewer;

//Java imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.List;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.imviewer.view.ImViewer;
import org.openmicroscopy.shoola.env.data.model.MovieExportParam;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.ui.UserNotifier;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import pojos.FileAnnotationData;
import pojos.ImageData;

/** 
 * Creates a movie of the image. 
 * This class calls the <code>createMovie</code> in the
 * <code>ImViewerView</code>. 
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
	extends DataLoader
{

    /** Handle to the async call so that we can cancel it. */
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
	public MovieCreator(ImViewer viewer, MovieExportParam param, 
			List<Integer> channels, ImageData image)
	{
		super(viewer);
		if (image == null)
			throw new IllegalArgumentException("Image Id not valid.");
		if (param == null)
			throw new IllegalArgumentException("Parameters cannot be null.");
		this.param = param;
		this.channels = channels;
		this.image = image;
	}
	
	/**
     * Creates a movie of the selected image.
     * @see DataLoader#load()
     */
    public void load()
    {
        handle = ivView.createMovie(image.getId(), channels, param, this);
    }

    /**
     * Cancels the ongoing data retrieval.
     * @see DataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /** 
     * Feeds the result back to the viewer. 
     * @see DataLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (viewer.getState() == ImViewer.DISCARDED) return;  //Async cancel.
        data = (FileAnnotationData) result;
        if (data == null) return;
        MessageBox box = new MessageBox(viewer.getUI(), "Movie Created",
		"The movie has been created. Do you want to download it?");
		if (box.centerMsgBox() == MessageBox.YES_OPTION) {
			FileChooser chooser = new FileChooser(viewer.getUI(), 
					FileChooser.FOLDER_CHOOSER, 
					"Download", "Select where to download the file.");
			chooser.addPropertyChangeListener(new PropertyChangeListener() {
			
				public void propertyChange(PropertyChangeEvent evt) {
					String name = evt.getPropertyName();
					if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
						File folder = (File) evt.getNewValue();
						if (folder == null)
							folder = UIUtilities.getDefaultFolder();
						UserNotifier un = registry.getUserNotifier();
						un.notifyDownload(data, folder);
					}
				}
			});
			chooser.centerDialog();
		}
    }

}

