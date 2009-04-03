/*
 * org.openmicroscopy.shoola.agents.hiviewer.clsf.ClassifierComponent
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
import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.HiTranslator;
import org.openmicroscopy.shoola.util.ui.component.AbstractComponent;
import pojos.ImageData;

/** 
 * Implements the {@link Classifier} interface to provide the functionality
 * required of the classifier component.
 * This class is the component hub and embeds the component's MVC triad.
 * It manages the component's state machine and fires state change 
 * notifications as appropriate, but delegates actual functionality to the
 * MVC sub-components.
 * 
 * @see ClassifierModel
 * @see ClassifierControl
 * @see ClassifierWin
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
class ClassifierComponent
    extends AbstractComponent
    implements Classifier
{

    /** The Model sub-component. */
    private ClassifierModel     model;
    
    /** The Controller sub-component. */
    private ClassifierControl   controller;

    
    /**
     * Creates a new instance.
     * The {@link #initialize(JFrame) initialize} method should be called
     * straight after to complete the MVC set up.
     * 
     * @param model The Model sub-component.
     */
    ClassifierComponent(ClassifierModel model)
    {
        if (model == null) throw new NullPointerException("No model.");
        this.model = model;
        controller = new ClassifierControl();
    }
    
    /**
     * Links up the MVC triad.
     * 
     * @param owner The window from which the component is invoked.
     *              Mustn't be <code>null</code>.
     */
    void initialize(JFrame owner)
    {
        model.initialize(this);
        controller.initialize(this, owner);
    }
    
    /**
     * Implemented as specified by the {@link Classifier} interface.
     * @see Classifier#getMode()
     */
    public int getMode() { return model.getMode(); }
    
    /**
     * Implemented as specified by the {@link Classifier} interface.
     * @see Classifier#getState()
     */
    public int getState() { return model.getState(); }

    /**
     * Implemented as specified by the {@link Classifier} interface.
     * @see Classifier#getImage()
     */
    public ImageData getImage() { return model.getImage(); }

    /**
     * Implemented as specified by the {@link Classifier} interface.
     * @see Classifier#activate()
     */
    public void activate()
    {
        switch (model.getState()) {
            case DISCARDED:
                throw new IllegalStateException(
                        "This method can't be invoked in the DISCARDED state.");
            case NEW:
                model.fireMetadataLoading();
                fireStateChange();
        }
    }

    /**
     * Implemented as specified by the {@link Classifier} interface.
     * @see Classifier#setMetadata(Set)
     */
    public void setMetadata(Set availablePaths)
    {
        if (model.getState() != LOADING_METADATA)
            throw new IllegalStateException(
                    "This method can only be invoked in the LOADING_METADATA "+
                    "state.");
        Set paths = HiTranslator.transformClassificationPaths(availablePaths);
        model.setMetadata(paths);
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link Classifier} interface.
     * @see Classifier#getMetadata()
     */
    public Set getMetadata()
    {
        if (model.getState() != READY)
            throw new IllegalStateException(
                    "This method can only be invoked in the READY state.");
        return model.getMetadata();
    }
    
    /**
     * Implemented as specified by the {@link Classifier} interface.
     * @see Classifier#save(Set)
     */
    public void save(Set categories)
    {
        if (model.getState() != READY)
            throw new IllegalStateException(
                    "This method can only be invoked in the READY state.");
        model.save(categories);
        fireStateChange();
    }

    /**
     * Implemented as specified by the {@link Classifier} interface.
     * @see Classifier#discard()
     */
    public void discard()
    {
        if (model.getState() != DISCARDED) {
            model.discard();
            fireStateChange();
        }
    }

    /**
     * Implemented as specified by the {@link Classifier} interface.
     * @see Classifier#saveClassification(Set)
     */
    public void saveClassification(Set categories)
    {
        if (model.getState() != SAVING_METADATA)
            throw new IllegalStateException("This method should be invoked " +
                    "in the SAVING_METADATA state.");
        if (categories == null) 
            throw new IllegalArgumentException("Categories shouldn't be null.");
        controller.closeWindow();
    }
    
}
