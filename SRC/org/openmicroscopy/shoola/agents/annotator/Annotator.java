/*
 * org.openmicroscopy.shoola.agents.annotator.Annotator
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
 
package org.openmicroscopy.shoola.agents.annotator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JInternalFrame;

import org.openmicroscopy.shoola.env.Agent;
import org.openmicroscopy.shoola.env.config.Registry;
import org.openmicroscopy.shoola.env.data.model.DatasetData;
import org.openmicroscopy.shoola.env.data.model.ImageData;
import org.openmicroscopy.shoola.env.event.AgentEvent;
import org.openmicroscopy.shoola.env.event.AgentEventListener;
import org.openmicroscopy.shoola.env.ui.TopFrame;

/**
 * Model and agent for the Annotator set of widgets.  The annotator makes
 * it so multiple annotation windows can be opened at the same time.  There are
 * also two alternate windows, as the annotator both handles text annotations
 * and scalar attribute definitions.  The text annotations are inherently
 * (from a UI perspective) different, and are thus shown in a different
 * format.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class Annotator implements Agent, AgentEventListener
{
    private Registry registry;
    
    private TopFrame topFrame;
    
    // these two lists have to be in sync...
    private List activeUIs;
    
    private List activeControls;
    
    public Annotator()
    {
        activeUIs = new ArrayList();
        activeControls = new ArrayList();
    }
    
    /**
     * Process and handle events destined for the Annotator.  NB to JM/Andrea:
     * I *believe* that there would only be one real event: EditAnnotation, or
     * the like, but there may be a distinction between a dataset and image
     * annotation, so... we'll likely talk about this on 3/9.
     * 
     * @param e The event to be captured by the annotator.
     */
    public void eventFired(AgentEvent e)
    {
        // TODO figure out what events to throw here, and do so.
        // do one of the trigger events on a EditAnnotation and
        // EditAttribute event, perhaps.
    }
    
    /**
     * Don't do anything, because of nature of annotator (triggered explicitly,
     * not when the application becomes active)
     * 
     * @see org.openmicroscopy.shoola.env.Agent#activate()
     */
    public void activate()
    {
        // do nothing
    }
    
    /**
     * Checks any open annotator windows to see if there is unsaved
     * information.  On a cancel, will return false.  Otherwise, if there is
     * no unsaved information or if the user makes a decisive choice to save
     * or not save an annotation, returns true.
     * 
     * @return Whether or not the agent can be terminated.
     * @see org.openmicroscopy.shoola.env.Agent#canTerminate()
     */
    public boolean canTerminate()
    {
        for(Iterator iter = activeControls.iterator(); iter.hasNext();)
        {
            AnnotationCtrl ctrl = (AnnotationCtrl)iter.next();
            if(!ctrl.isSaved())
            {
                // TODO prompt dialog, result in ctrl.close condition or
                // return false condition on cancel
            }
        }
        return true;
    }
    
    /**
     * Does nothing.
     * 
     * @see org.openmicroscopy.shoola.env.Agent#terminate()
     */
    public void terminate()
    {
        // do nothing.
    }
    
    /**
     * Sets the registry (and operating context) of the Annotator.
     * 
     * @see org.openmicroscopy.shoola.env.Agent#setContext(org.openmicroscopy.shoola.env.config.Registry)
     * @throws IllegalArgumentException If the parameter is null.
     */
    public void setContext(Registry ctx)
        throws IllegalArgumentException
    {
        if(ctx == null)
        {
            throw new IllegalArgumentException("Annotator cannot operate " +
                "in null context");
        }
        this.registry = ctx;
        topFrame = registry.getTopFrame();
    }
    
    // trigger a text annotator GUI for a dataset.
    private void triggerEditAnnotation(DatasetData datasetModel)
    {
        // 
    }
    
    // trigger a text annotator GUI for an image.
    private void triggerEditAnnotation(ImageData imageModel)
    {
        ImageAnnotationCtrl iac =
            new ImageAnnotationCtrl(this,imageModel.getID());
    }
    
    // trigger an attribute editor GUI for a dataset.
    private void triggerEditAttributes(DatasetData datasetModel)
    {
    }
    
    // trigger an attribute editor GUI for an image.
    private void triggerEditAttributes(ImageData imageModel)
    {
        ImageAnnotationCtrl iac =
            new ImageAnnotationCtrl(this,imageModel.getID());
    }
    
    /**
     * Closes, hides, and then removes the UI attached to the specified
     * control.  Does not force an update (assumed done)
     * @param control The controller to remove.
     */
    public void close(AnnotationCtrl control)
    {
        if(control == null)
        {
            return;
        }
        
        int index = activeControls.indexOf(control);
        if(index == -1)
        {
            return;
        } 
        
        JInternalFrame frame = (JInternalFrame)activeUIs.get(index);
        frame.setVisible(false);
        activeControls.remove(control);
        activeUIs.remove(frame);
    }

    /**
     * Retrieve the dataset annotation information from the database.
     * @param datasetID the ID of the dataset to retrieve annotations from.
     * @return A list of annotations for that dataset.
     */
    List getDatasetAnnotations(int datasetID)
    {
        // TODO call registry to find out this information and change this
        return new ArrayList();
    }
    
    /**
     * Retrieve the semantic attributes associated with this database from
     * the dataset.
     * @param datasetID the ID of the dataset to retrieve attributes from.
     * @return A list of attributes for that dataset.
     */
    List getDatasetAttributes(int datasetID)
    {
        // TODO call registry to find out this information and change this
        return new ArrayList();
    }
    
    /**
     * Retrieve the image annotation information from the database.
     * @param imageID the ID of the image to retrieve annotations from.
     * @return A list of annotations for that image.
     */
    List getImageAnnotations(int imageID)
    {
        // TODO call registry to find out this information and change this
        return new ArrayList();
    }
    
    /**
     * Retrieve the image attribute information from the database.
     *
     * @param imageID The ID of the image to retrieve attributes from.
     * @return A list of attributes for that image.
     */
    List getImageAttributes(int imageID)
    {
        // TODO call registry to find out this information and change this
        return new ArrayList();
    }
    
    /**
     * Commits all the attributes to the specified values.
     * @param attributes The attributes to update (and specified values)
     */
    boolean updateDatasetInfo(List attributes)
    {
        // TODO call registry to get STS handle to do update.
        // return false on error
        return true;
    }
    

}
