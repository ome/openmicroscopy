/*
 * org.openmicroscopy.shoola.agents.treeviewer.clsf.ClassifierFactory
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

package org.openmicroscopy.shoola.agents.treeviewer.clsf;


//Java imports
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import pojos.ImageData;


/** 
 *  Factory to create and keep track of the {@link Classifier classifer}.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class ClassifierFactory
    implements PropertyChangeListener
{

    /** The sole instance. */
    private static final ClassifierFactory singleton = new ClassifierFactory();
    
    /**
     * Creates a classifier depending on the specified mode.
     * 
     * @param model     Reference to {@link TreeViewer}.
     *                  Mustn't be <code>null</code>.
     * @param m         The type of classifier. One of the following constants:
     *                  {@link Classifier#CLASSIFY_MODE}, 
     *                  {@link Classifier#DECLASSIFY_MODE}.
     * @param images    The images to classify or declassify.
     * @return See above.
     */
    public static Classifier getClassifier(TreeViewer model, int m,
                                            ImageData[] images)
    {
        return singleton.createClassifier(model, m, images);
    }
    
    /** The tracked component. */
    private Classifier classifier;
    
    /** Creates a new instance. */
    private ClassifierFactory()
    {
        classifier = null;
    }
    
    /**
     * Creates a classifier depending on the specified mode.
     * 
     * @param model     Reference to {@link TreeViewer}.
     *                  Mustn't be <code>null</code>
     * @param mode      The type of classifier. One of the following constants:
     *                  {@link Classifier#CLASSIFY_MODE}, 
     *                  {@link Classifier#DECLASSIFY_MODE}.
     * @param images    The image to classify or declassify.
     * @return See above.
     */
    private Classifier createClassifier(TreeViewer model, int mode,
                                        ImageData[] images)
    {
        if (classifier != null) {
            if (mode == classifier.getMode()) return classifier;
            classifier.discard();
            classifier = null;
        }
        model.addPropertyChangeListener(this);
        ClassifierModel m = new ClassifierModel(model, mode, images);
        ClassifierComponent component = new ClassifierComponent(m);
        m.initialize(component);
        component.initialize();
        classifier = component;
        return classifier;
    }
    
    /** 
     * Listens to property changed fired by the {@link TreeViewer}. 
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        String name = pce.getPropertyName();
        if (name.equals(TreeViewer.REMOVE_EDITOR_PROPERTY)) {
            if (classifier != null) {
                classifier.discard();
                classifier = null;
            }
        }
    }

}
