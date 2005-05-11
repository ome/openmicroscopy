/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.PDIModel
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

package org.openmicroscopy.shoola.agents.hiviewer.view;


//Java imports
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.DataLoader;
import org.openmicroscopy.shoola.agents.hiviewer.PDILoader;
import org.openmicroscopy.shoola.env.data.model.ImageSummary;

/** 
 * A concrete Model for a P/D/I hierarchy consisting of possibly multiple
 * trees whose leaves are some specified Images.
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
class PDIModel
    extends HiViewerModel
{

    /**
     * The set of all the Images that sit at the bottom of the P/D/I
     * trees that this Model handles.  Every Image is represented by
     * an {@link ImageSummary} object.  
     */
    private Set     images;
    
    
    /**
     * Creates a new instance.
     * 
     * @param images The set of all the Images that sit at the bottom of the
     *               P/D/I trees that this Model will handle.  Every Image
     *               is represented by an {@link ImageSummary} object.
     *               Don't pass <code>null</code>.
     */
    PDIModel(Set images) 
    {
        super();
        if (images == null) throw new NullPointerException("No images.");
        this.images = images; 
    }
    
    /**
     * Implemented as specified by the superclass.
     * @see HiViewerModel#getHierarchyType()
     */
    protected int getHierarchyType() { return HiViewer.PDI_HIERARCHY; }

    /**
     * Implemented as specified by the superclass.
     * @see HiViewerModel#isSameDisplay(HiViewerModel)
     */
    protected boolean isSameDisplay(HiViewerModel other)
    {
        if (other == null || !(other instanceof PDIModel) ||
                other.getHierarchyType() != getHierarchyType()) 
            return false;
        PDIModel pdim = (PDIModel) other;
        if (images.size() != pdim.images.size()) return false;
        ImageSummary is;
        Map myImgs = new HashMap(), otherImgs = new HashMap();
        Iterator i = images.iterator(), j = pdim.images.iterator();
        while (i.hasNext()) {
            is = (ImageSummary) i.next();
            myImgs.put(new Integer(is.getID()), is);
        }
        while (j.hasNext()) {
            is = (ImageSummary) j.next();
            otherImgs.put(new Integer(is.getID()), is);
        }
        i = myImgs.keySet().iterator();
        while (i.hasNext())
            if (otherImgs.get(i.next()) == null) return false;
        return true;
    }//TODO: factor the above out. same code in CGCIModel
    
    /**
     * Implemented as specified by the superclass.
     * @see HiViewerModel#createHierarchyLoader()
     */
    protected DataLoader createHierarchyLoader()
    {
        return new PDILoader(component, images);
    }
    
    /**
     * Implemented as specified by the superclass.
     * @see HiViewerModel#reinstantiate()
     */
    protected HiViewerModel reinstantiate()
    {
        HashSet copy = new HashSet(images);
        return new PDIModel(copy);
    }

}
