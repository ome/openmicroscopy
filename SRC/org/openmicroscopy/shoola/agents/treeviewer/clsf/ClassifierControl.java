/*
 * org.openmicroscopy.shoola.agents.treeviewer.clsf.ClassifierControl
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
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewer;
import org.openmicroscopy.shoola.agents.treeviewer.view.TreeViewerFactory;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

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
class ClassifierControl
    implements ChangeListener, PropertyChangeListener
{
 
    /** 
     * Reference to the {@link Classifier} component, which, in this context,
     * is regarded as the Model.
     */
    private Classifier      model;
    
    /** Reference to the View. */
    private ClassifierUI    view;
    
    /**
     * Creates a new instance.
     * The {@link #initialize(ClassifierUI) initialize} method 
     * should be called straight 
     * after to link this Controller to the other MVC components.
     * 
     * @param model  Reference to the {@link Classifier} component, which, in 
     *               this context, is regarded as the Model.
     *               Mustn't be <code>null</code>.
     */
    ClassifierControl(Classifier model)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
    }

    /** 
     * Links it with the View. 
     * 
     * @param view The reference to the View.
     */
    void initialize(ClassifierUI view)
    {
        if (view == null) throw new IllegalArgumentException("No view.");
        this.view = view;
    }

    /**
     * Closes the {@link Classifier}.
     * 
     * @param b Passed <code>true</code> to close the {@link Classifier}. 
     */
    void closeClassifier(boolean b)
    {
        if (b == true) model.close();
    }

    /** 
     * Reacts to state changes in the {@link Classifier}.
     * @see ChangeListener#stateChanged(ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        switch (model.getState()) {
            case Classifier.SAVING_CLASSIFICATION:   
                TreeViewerFactory.getLoadingWindow().setTitle(
                        TreeViewer.SAVING_TITLE);
            case Classifier.LOADING_CLASSIFICATION:
                UIUtilities.centerAndShow(TreeViewerFactory.getLoadingWindow());
                break;
            case Classifier.READY:
                TreeViewerFactory.getLoadingWindow().setVisible(false);
                break;
        }
    }
    
    /**
     * Reacts to the <code>THUMBNAIL_LOADED_PROPERTY</code> changes fired by the
     * <code>TreeViewer</code>.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        model.setThumbnail((BufferedImage) pce.getNewValue());
    }
    
}
