/*
 * org.openmicroscopy.shoola.agents.browser.datamodel.TSMFactory
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.browser.datamodel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openmicroscopy.ds.DataException;
import org.openmicroscopy.ds.dto.Dataset;
import org.openmicroscopy.ds.dto.Image;

/**
 * A factory that constructs ThumbnailSourceMaps from a particular data
 * source (in this case, a dataset; although future methods can be added
 * if necessary)
 * 
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class DatasetLoader
{
    private Set progressListeners;
    
    /**
     * Constructs a new DatasetLoader.
     */
    public DatasetLoader()
    {
        progressListeners = new HashSet();
    }
    
    /**
     * Loads the image contents of the Dataset DTO into the
     * ThumbnailSourceMap.  Will do nothing if either the Dataset or
     * ThumbnailSourceMap are null.
     * 
     * @param dto The dataset to analyze.
     * @param tsm The thumbnail source map in which to place image
     *            information (extracted from the DTO)
     */
    public void loadInto(Dataset dto, ThumbnailSourceMap tsm)
    {
        if(dto == null || tsm == null)
        {
            // do nothing
            return;
        }
        int count = 0; // TODO: replace with actual count here (line below)
        // int count = dto.countImages();
        
        List imageList;
        try
        {
            imageList = dto.getImages();
        }
        catch(DataException de)
        {
            // notify everyone that a DB read (expensive) is about to go down
            for(Iterator iter = progressListeners.iterator(); iter.hasNext();)
            {
                ProgressListener listener = (ProgressListener)iter.next();
                listener.processStarted(1);
            }
            // TODO: call factory fill methods: this could take some time
            // (and also throw more exceptions which will trigger a
            // processFailure)
            
            // OK, we're done; everybody go home
            for(Iterator iter = progressListeners.iterator(); iter.hasNext();)
            {
                ProgressListener listener = (ProgressListener)iter.next();
                listener.processAdvanced("Image metadata loaded.");
                listener.processSucceeded();
            }
            imageList = dto.getImages();
        }
        
        // now, suck the images into the TSM
        for(Iterator iter = imageList.iterator(); iter.hasNext();)
        {
            tsm.putImageData((Image)iter.next());
        }
        // OK, now the image data will have a data model backing, but
        // perhaps no thumbnails.
    }
    
    /**
     * Adds a progress listener to receive notifications of time-consuming
     * activities.
     * @param listener The listener to receive notifications.
     */
    public void addListener(ProgressListener listener)
    {
        if(listener != null)
        {
            progressListeners.add(listener);
        }
    }
    
    /**
     * Unsubscribes a listener from the DatasetLoader's notifications.
     * @param listener The listener to remove.
     */
    public void removeListener(ProgressListener listener)
    {
        if(listener != null)
        {
            progressListeners.remove(listener);
        }
    }
}