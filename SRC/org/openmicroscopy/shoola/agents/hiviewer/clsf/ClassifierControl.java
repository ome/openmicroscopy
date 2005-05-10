/*
 * org.openmicroscopy.shoola.agents.hiviewer.clsf.ClassifierControl
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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.env.data.model.CategoryData;

/** 
 * The Controller component in the {@link Classifier} MVC triad.
 * It manages the UI workflow according to state transitions in the Model and
 * reacts to user inputs by asking the Model to classify/declassify its Image.
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
class ClassifierControl
    implements PropertyChangeListener, ChangeListener
{

    /** 
     * Reference to the {@link ClassifierComponent} component, which, in this 
     * context, is regarded as the Model.
     */
    private ClassifierComponent     model;
    
    /** Progress window shown on screen as the metadata is loading. */
    private LoadingWin              loadingWin;
    
    /** The dialog that lets the user classify/declassify. */
    private ClassifierWin           classifWin;
     
    
    /**
     * Creates the progress window, registers for event notification, and
     * finally sets the window on screen.
     */
    private void createLoadingWin()
    {
        loadingWin = new LoadingWin();
        loadingWin.addPropertyChangeListener(LoadingWin.CLOSED_PROPERTY, this);
        loadingWin.setOnScreen();
    }
    
    /**
     * Gets rid of the progress window.
     */
    private void discardLoadingWin()
    {
        if (loadingWin == null) return;
        loadingWin.removePropertyChangeListener(LoadingWin.CLOSED_PROPERTY, 
                                                this);
        loadingWin.setClosed(true);  //We won't get this notification.
        loadingWin = null;
    }
    
    /**
     * Creates the classification window, registers for event notification,
     * and finally sets the window on screen.
     */
    private void createClassifWin()
    {
        if (model.getMode() == Classifier.CLASSIFICATION_MODE)
            classifWin = new AddWin(model.getMetadata());
        else  //Declassification mode.
            classifWin = new RemoveWin(model.getMetadata());
        classifWin.addPropertyChangeListener(
                ClassifierWin.SELECTED_CATEGORY_PROPERTY, this);
        classifWin.addPropertyChangeListener(
                ClassifierWin.CLOSED_PROPERTY, this);
        classifWin.setOnScreen();
    }
    
    /**
     * Creates a new instance.
     * The {@link #initialize() initialize} method should be called straigh 
     * after to link this Controller to the other MVC components.
     */
    ClassifierControl() {}
    
    /**
     * Links this Controller to its Model.
     * 
     * @param model  Reference to the {@link ClassifierComponent} component,
     *               which, in this context, is regarded as the Model.
     *               Mustn't be <code>null</code>.
     */
    void initialize(ClassifierComponent model) 
    { 
        this.model = model;
        model.addChangeListener(this);
    }

    /**
     * Listens to property changes in the {@link #loadingWin} and
     * {@link #classifWin}.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent pce)
    {
        //These notifications can come either from loadingWin or classifWin.
        if (pce.getSource() == loadingWin) {  //Must be the CLOSED_PROPERTY.
            model.discard();  //classifWin will never be brought up.
        } else {  //It's coming from classifWin.
            if (ClassifierWin.SELECTED_CATEGORY_PROPERTY.equals(
                    pce.getPropertyName()))  
                //The user selected a Category for (de-)classification.
                //The classifWin automatically closes itself after selection,
                //so we're about to get a CLOSED_PROPERTY notification.
                model.save((CategoryData) pce.getNewValue());
            else  //Must be the CLOSED_PROPERTY.
                //Either the user selected a Category or just pressed the
                //close button.
                model.discard();
        }
    }

    /**
     * Listens to state changes in the Model and brings up the progress
     * window or the classification window, depending on the Model's state.
     * @see ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent ce)
    {
        //These event notifications come from the Model.
        switch(model.getState()) {
            case Classifier.LOADING_METADATA:
                createLoadingWin();
                break;
            case Classifier.READY:
                discardLoadingWin();
                createClassifWin();
            case Classifier.DISCARDED:
                //An error occurred while loading the metadata.
                discardLoadingWin();
        }
    }
    
}
