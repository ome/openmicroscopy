/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.HierarchyModel
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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.openmicroscopy.shoola.agents.hiviewer.CGCILoader;
import org.openmicroscopy.shoola.agents.hiviewer.DataLoader;
import org.openmicroscopy.shoola.agents.hiviewer.PDILoader;

import pojos.ImageData;


//Java imports

//Third-party libraries

//Application-internal dependencies

/** 
 * 
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
class HierarchyModel
    extends HiViewerModel
{

    /**
     * The set of all the Images that sit at the bottom of the P/D/I
     * trees that this Model handles.  Every Image is represented by
     * an {@link ImageData} object.  
     */
    private Set     images;
    
    /**
     * The type of the model either {@link HiViewer#PDI_HIERARCHY}
     * or {@link HiViewer#CGCI_HIERARCHY}.
     */
    private int     type;
    
    /**
     * Checks if the type passed is valid.
     * 
     * @param v The type to check.
     * @return  <code>true</code> if the type is valid,
     *          <code>false</code> otherwise.
     */
    private boolean checkType(int v)
    {
        switch (v) {
            case HiViewer.PDI_HIERARCHY:
            case HiViewer.CGCI_HIERARCHY:    
                return true;
            default: return false;
        }
    }
    
    /**
     * Creates a new instance.
     * 
     * @param images The set of all the Images that sit at the bottom of the
     *               P/D/I trees that this Model will handle.  Every Image
     *               is represented by an {@link ImageData} object.
     *               Don't pass <code>null</code>.
     * @param type   The hierarchy type. 
     */
    HierarchyModel(Set images, int type)
    {
        super();
        if (images == null) throw new NullPointerException("No images.");
        if (!checkType(type))
            throw new IllegalArgumentException("Hierarchy not supported");
        this.images = images; 
        this.type = type;
    }

    /**
     * Implemented as specified by the superclass.
     * @see HiViewerModel#getHierarchyType()
     */
    protected int getHierarchyType() { return type; }

    /**
     * Implemented as specified by the superclass.
     * @see HiViewerModel#isSameDisplay(HiViewerModel)
     */
    protected boolean isSameDisplay(HiViewerModel other)
    {
        if (other == null || !(other instanceof HierarchyModel) ||
                other.getHierarchyType() != getHierarchyType()) 
            return false;
        HierarchyModel hm = (HierarchyModel) other;
        if (images.size() != hm.images.size()) return false;
        ImageData data;
        Map myImgs = new HashMap(), otherImgs = new HashMap();
        Iterator i = images.iterator(), j = hm.images.iterator();
        while (i.hasNext()) {
            data = (ImageData) i.next();
            myImgs.put(new Integer(data.getId()), data);
        }
        while (j.hasNext()) {
            data = (ImageData) j.next();
            otherImgs.put(new Integer(data.getId()), data);
        }
        i = myImgs.keySet().iterator();
        while (i.hasNext())
            if (otherImgs.get(i.next()) == null) return false;
        return true;
    }

    /**
     * Implemented as specified by the superclass.
     * @see HiViewerModel#createHierarchyLoader()
     */
    protected DataLoader createHierarchyLoader()
    {
        Set ids = new HashSet(images.size());
        Iterator i = images.iterator();
        while (i.hasNext())
            ids.add(new Integer(((ImageData) i.next()).getId()));
        
        switch (type) {
            case HiViewer.PDI_HIERARCHY: 
                return new PDILoader(component, images);
            case HiViewer.CGCI_HIERARCHY:
                return new CGCILoader(component, ids);
        }
        return null;
    }

    /**
     * Implemented as specified by the superclass.
     * @see HiViewerModel#reinstantiate()
     */
    protected HiViewerModel reinstantiate()
    {
        HashSet copy = new HashSet(images);
        return new HierarchyModel(copy, type);
    }
    
}
