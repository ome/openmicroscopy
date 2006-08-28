/*
 * org.openmicroscopy.shoola.agents.hiviewer.clsf.ClassifierFactory
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
import javax.swing.JFrame;

import pojos.ImageData;

//Third-party libraries

//Application-internal dependencies

/** 
 * Factory to create {@link Classifier} components.
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
public class ClassifierFactory
{
    
    /**
     * Creates a {@link Classifier} component to classify the specified
     * images.
     * 
     * @param images    The images to classify.
     * @param owner     The window from which the component is invoked.
     *                  Mustn't be <code>null</code>.
     * @return A {@link Classifier} to classify the given Image.
     */
    public static Classifier createClassifComponent(ImageData[] images,
                                                    JFrame owner)
    {
        AddModel model = new AddModel(images);
        ClassifierComponent comp = new ClassifierComponent(model);
        comp.initialize(owner);
        return comp;
    }
    
    /**
     * Creates a {@link Classifier} component to declassify the specified
     * images.
     * 
     * @param images    The images to declassify.
     * @param owner     The window from which the component is invoked.
     *                  Mustn't be <code>null</code>.
     * @return A {@link Classifier} to declassify the given Image.
     */
    public static Classifier createDeclassifComponent(ImageData[] images,
                                                    JFrame owner)
    {
        RemoveModel model = new RemoveModel(images);
        ClassifierComponent comp = new ClassifierComponent(model);
        comp.initialize(owner);
        return comp;
    }
    
    /**
     * Creates a {@link Classifier} component to classify/declassify the
     * specified Image, depending on <code>mode</code>.
     * 
     * @param mode      One of the classification mode constants defined by the 
     *                  {@link Classifier} interface.
     * @param images    The images to classify or declassify.
     * @param owner     The window from which the component is invoked.
     *                  Mustn't be <code>null</code>.
     * @return          A {@link Classifier} to classify or declassify the given 
     *                  images, depending on the value of the <code>mode</code>
     *                  constant.
     */
    public static Classifier createComponent(int mode, ImageData[] images,
                                             JFrame owner)
    {
        switch (mode) {
            case Classifier.CLASSIFICATION_MODE:
                return createClassifComponent(images, owner);
            case Classifier.DECLASSIFICATION_MODE:
                return createDeclassifComponent(images, owner);
            default:
                throw new IllegalArgumentException(
                        "Unsupported classification mode: "+mode+".");
        }
    }
    
}
