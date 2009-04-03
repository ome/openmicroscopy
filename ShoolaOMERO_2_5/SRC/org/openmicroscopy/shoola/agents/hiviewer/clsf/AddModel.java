/*
 * org.openmicroscopy.shoola.agents.hiviewer.clsf.AddModel
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
import java.util.Set;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.ClassifLoader;
import org.openmicroscopy.shoola.agents.hiviewer.ClassifPathsLoader;
import org.openmicroscopy.shoola.agents.hiviewer.ClassificationSaver;


/** 
 * The concrete Model used by a {@link Classifier} component that was created
 * for classification. 
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
class AddModel
    extends ClassifierModel
{

    /**
     * Returns the {@link Classifier#CLASSIFICATION_MODE} constant.
     * 
     * @return See above.
     * @see ClassifierModel#getMode()
     */
    protected int getMode() { return Classifier.CLASSIFICATION_MODE; }
    
    /**
     * Returns a new {@link ClassifPathsLoader} to load all paths that are
     * available for classification.
     * 
     * @return See above.
     */
    protected ClassifLoader createClassifLoader()
    {
        return new ClassifPathsLoader(component);
    }

    /**
     * Classifies the Image this Model is working with under the specified
     * categories
     * @see ClassifierModel#save(Set)
     */
    protected void save(Set categories)
    {
        state = Classifier.SAVING_METADATA;
        loader = new ClassificationSaver(component, 
                    ClassificationSaver.CLASSIFY, imageID, categories);
        loader.load();
    }

    /**
     * Creates a new instance.
     * 
     * @param imageID The id of the Image the component will be working with.
     */
    AddModel(int imageID) { super(imageID); }

}
