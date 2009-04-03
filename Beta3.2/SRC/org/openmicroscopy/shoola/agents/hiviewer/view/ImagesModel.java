/*
 * org.openmicroscopy.shoola.agents.hiviewer.view.ImagesModel
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.hiviewer.view;




//Java imports
import java.util.Iterator;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.DataLoader;
import org.openmicroscopy.shoola.agents.hiviewer.ImagesLoader;

/** 
 * A concrete Model for a collection of images.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
class ImagesModel
    extends HiViewerModel
{

    /** A collection of images' id to browse. */
    private Set 		imagesID;
    
    /**
     * Creates a new instance.
     * 
     * @param imagesID  The colllection of images' ID.
     */
    ImagesModel(Set imagesID)
    {
        super();
        this.imagesID = imagesID;
    }
    
    /**
     * Implemented as specified by the superclass. 
     * @see HiViewerModel#getHierarchyType()
     */
    protected int getHierarchyType() { return HiViewer.IMAGES_HIERARCHY; }

    /**
     * Implemented as specified by the superclass. 
     * @see HiViewerModel#isSameDisplay(HiViewerModel)
     */
    protected boolean isSameDisplay(HiViewerModel other)
    {
        if (other == null || !(other instanceof ImagesModel)) return false;
        ImagesModel im = (ImagesModel) other;
        if (im.getHierarchyType() != getHierarchyType()) return false;
        if (im.imagesID.size() != imagesID.size()) return false;
        Iterator i = im.imagesID.iterator(), j;
        Long id;
        int index = imagesID.size();
        while (i.hasNext()) {
            id = (Long) i.next();
            j = imagesID.iterator();
            while (j.hasNext()) {
                if (id.longValue() == ((Long) j.next()).longValue()) index--;
            }
        }
        return (index == 0);
    }

    /** 
     * Implemented as specified by the superclass. 
     * @see HiViewerModel#createHierarchyLoader(boolean)
     */
    protected DataLoader createHierarchyLoader(boolean refresh)
    {
        return new ImagesLoader(component, imagesID, refresh);
    }

    /**
     * Implemented as specified by the superclass.
     * @see HiViewerModel#reinstantiate()
     */
    protected HiViewerModel reinstantiate()
    {
        HiViewerModel model = new ImagesModel(imagesID);
        model.setRootLevel(getExperimenter());
        return model;
    }

}
