/*
 * org.openmicroscopy.shoola.agents.hiviewer.Declassifier
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
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard;
import org.openmicroscopy.shoola.agents.hiviewer.clsf.Classifier;
import org.openmicroscopy.shoola.env.data.views.CallHandle;
import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;

import pojos.ImageData;

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 	<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class Declassifier
    extends CBDataLoader
{

    /** The image to declassify. */
    private ImageData   image;
    
    /** The selected categories to remove the image. */
    private Set         categories;
    
    /** Handle to the async call so that we can cancel it. */
    private CallHandle  handle;
    
    /**
     * Creates a new instance.
     * 
     * @param clipBoard     The viewer this data loader is for.
     *                      Mustn't be <code>null</code>.
     * @param image         The image to declassify.
     *                      Mustn't be <code>null</code>.
     * @param categories    The categories to add to or remove from.
     *                      Mustn't be <code>null</code>.
     */
    public Declassifier(ClipBoard clipBoard, ImageData image, Set categories)
    {
        super(clipBoard);
        if (image ==  null) 
            throw new IllegalArgumentException("No image to declassify.");
        if (categories == null || categories.size() == 0) 
            throw new IllegalArgumentException("No category selected.");
        this.image = image; 
        this.categories = categories;
    }
    
    /**
     * Declassifies the specified image. 
     * @see DataLoader#load()
     */
    public void load()
    {
        Set objects = new HashSet(1);
        objects.add(image);
        handle = hiBrwView.declassify(objects, categories, this);
    }

    /** 
     * Cancels the data loading. 
     * @see DataLoader#cancel()
     */
    public void cancel() { handle.cancel(); }
    
    /**
     * Feeds the result back to the viewer.
     * @see DataLoader#handleResult(Object)
     */
    public void handleResult(Object result)
    {
        ///if (classifier.getState() == Classifier.DISCARDED) return; 
        clipBoard.saveClassification(((Set) result));
    }
}
