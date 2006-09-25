/*
 * org.openmicroscopy.shoola.agents.hiviewer.DeclassifPathsLoader
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

package org.openmicroscopy.shoola.agents.hiviewer;


//Java imports
import java.util.HashSet;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.clsf.Classifier;
import org.openmicroscopy.shoola.env.data.OmeroDataService;
import org.openmicroscopy.shoola.env.data.views.CallHandle;

import pojos.ImageData;

/** 
 * Loads, asynchronously, the metadata needed by a given {@link Classifier} to
 * declassify an Image.
 * That is, all the paths in the Category Group trees that contain the Image
 * the {@link Classifier} is working with, and so can be used to declassify
 * the Image. Every path is rooted by a Category Group object.
 * This class calls the <code>loadClassificationPaths</code> method in the
 * <code>HierarchyBrowsingView</code>.
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
public class DeclassifPathsLoader
    extends ClassifLoader
{
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle            handle;
    
    /**
     * Creates a new instance.
     * 
     * @param classifier The {@link Classifier} this data loader is for.
     *                   Mustn't be <code>null</code>.
     */
    public DeclassifPathsLoader(Classifier classifier) { super(classifier); }
    
    /**
     * Retrieves all the metadata needed by the {@link #classifier}.
     * @see DataLoader#load()
     */
    public void load()
    {
        ImageData[] images = classifier.getImages();
        Set ids = new HashSet(images.length);
        for (int i = 0; i < images.length; i++)
            ids.add(new Long(images[i].getId()));
        handle = hiBrwView.loadClassificationPaths(ids,
                             OmeroDataService.DECLASSIFICATION, this);
    }
    
    /** 
     * Cancels the data loading. 
     * @see DataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Feeds the result back to the viewer.
     * @see #handleResult(Object)
     */
    public void handleResult(Object result)
    {
        if (classifier.getState() == Classifier.DISCARDED) return; 
        classifier.setMetadata((Set) result);
    }
    
}
