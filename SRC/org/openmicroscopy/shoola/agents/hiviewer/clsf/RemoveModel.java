/*
 * org.openmicroscopy.shoola.agents.hiviewer.clsf.RemoveModel
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

package org.openmicroscopy.shoola.agents.hiviewer.clsf;


//Java imports
import java.util.HashSet;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.ClassifLoader;
import org.openmicroscopy.shoola.agents.hiviewer.DeclassifPathsLoader;
import org.openmicroscopy.shoola.agents.hiviewer.HiViewerAgent;
import org.openmicroscopy.shoola.env.data.events.DSCallAdapter;
import org.openmicroscopy.shoola.env.data.views.HierarchyBrowsingView;
import pojos.CategoryData;

/** 
 * The concrete Model used by a {@link Classifier} component that was created
 * for declassification. 
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
class RemoveModel
    extends ClassifierModel
{

    /**
     * Returns the {@link Classifier#DECLASSIFICATION_MODE} constant.
     * 
     * @return See above.
     * @see ClassifierModel#getMode()
     */
    protected int getMode() { return Classifier.DECLASSIFICATION_MODE; }
    
    /**
     * Returns a new {@link ClassifLoader} to load all paths that are
     * available for declassification.
     * 
     * @return See above.
     * @see ClassifierModel#createClassifLoader()
     */
    protected ClassifLoader createClassifLoader()
    {
        return new DeclassifPathsLoader(component);
    }

    /**
     * Removes the Image this Model is working with from the specified
     * Category.
     * 
     * @param category Data object representing the Category.
     * @see ClassifierModel#save(CategoryData)
     */
    protected void save(CategoryData category)
    {
        HierarchyBrowsingView hbw = (HierarchyBrowsingView) 
            HiViewerAgent.getRegistry().getDataServicesView(
                    HierarchyBrowsingView.class);
        Set ids = new HashSet(1);
        ids.add(new Integer(imageID));
        hbw.declassify(category, ids, new DSCallAdapter() {});
    }
    
    /**
     * Creates a new instance.
     * 
     * @param imageID The id of the Image the component will be working with.
     */
    RemoveModel(int imageID) { super(imageID); }

}
