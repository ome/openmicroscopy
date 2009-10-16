/*
 * org.openmicroscopy.shoola.env.ui.MovieCreator
 *
 *------------------------------------------------------------------------------
 * Copyright (C) 2006-2009 University of Dundee. All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.env.ui;



//Java imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import omero.model.OriginalFile;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DownloadActivityParam;
import org.openmicroscopy.shoola.env.data.model.MovieExportParam;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import pojos.FileAnnotationData;
import pojos.ImageData;

/**
 * Creates a movie of the passed image.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *         <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
public class MovieCreator 
	extends UserNotifierLoader
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
    
    /** Reference to the activity. */
    private ActivityComponent 		activity;
    
    /** Notifies the user that an error occurred. */
    protected void onException() { handleNullResult(); }
    
    /**
     * Creates a new instance.
     * 
     * @param viewer	The viewer this data loader is for.
     *               	Mustn't be <code>null</code>.
     * @param registry	Convenience reference for subclasses.
     * @param param  	The parameters used to create the movie.
     * @param channels	The selected channels.
     * @param image		The image.
     * @param activity 	The activity associated to this loader.
     */
	public MovieCreator(UserNotifier viewer,  Registry registry,
			MovieExportParam param, List<Integer> channels, 
			ImageData image, ActivityComponent activity)
	{
		super(viewer, registry);
		if (activity == null)
			throw new IllegalArgumentException("Activity valid.");
		if (image == null)
			throw new IllegalArgumentException("Image not valid.");
		if (param == null)
			throw new IllegalArgumentException("Parameters cannot be null.");
		this.param = param;
		if (channels == null) 
			channels = new ArrayList<Integer>();
		this.channels = channels;
		this.image = image;
		this.activity = activity;
	}
	
	/**
     * Creates a movie of the selected image.
     * @see UserNotifierLoader#load()
     */
    public void load()
    {
    	long pixelsID = image.getDefaultPixels().getId();
        handle = ivView.createMovie(image.getId(), pixelsID, channels, param, 
        		this);
    }
    
    /**
     * Cancels the ongoing data retrieval.
     * @see UserNotifierLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Notifies the user that it wasn't possible to create the movie.
     * @see UserNotifierLoader#handleNullResult()
     */
    public void handleNullResult()
    { 
    	activity.notifyError("Unable to create movie for ");
    }
 
    /** 
     * Feeds the result back to the viewer. 
     * @see UserNotifierLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        data = (FileAnnotationData) result;
        activity.endActivity();
        JFrame f = registry.getTaskBar().getFrame();
        MessageBox box = new MessageBox(f, "Movie Created",
		"The movie has been created. Do you want to download it?");
        
		if (box.centerMsgBox() == MessageBox.YES_OPTION) {
			FileChooser chooser = new FileChooser(f, FileChooser.SAVE, 
					"Download", "Select where to download the file.", null, 
					true);
			IconManager icons = IconManager.getInstance(registry);
			chooser.setTitleIcon(icons.getIcon(IconManager.DOWNLOAD_48));
			chooser.setSelectedFileFull(data.getFileName());
			chooser.setApproveButtonText("Download");
			chooser.addPropertyChangeListener(new PropertyChangeListener() {
			
				public void propertyChange(PropertyChangeEvent evt) {
					String name = evt.getPropertyName();
					if (FileChooser.APPROVE_SELECTION_PROPERTY.equals(name)) {
						File folder = (File) evt.getNewValue();
						if (data == null) return;
						OriginalFile f = (OriginalFile) data.getContent();
						IconManager icons = IconManager.getInstance(registry);
						
						DownloadActivityParam activity = 
							new DownloadActivityParam(f,
								folder, icons.getIcon(IconManager.DOWNLOAD_22));
						viewer.notifyActivity(activity);
						//viewer.notifyDownload(data, folder);
					}
				}
			});
			chooser.centerDialog();
		}
    }
	
}
