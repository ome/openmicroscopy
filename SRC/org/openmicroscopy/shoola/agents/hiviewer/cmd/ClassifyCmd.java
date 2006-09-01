/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.ClassifyCmd
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

package org.openmicroscopy.shoola.agents.hiviewer.cmd;


//Java imports
import java.util.Iterator;
import java.util.Set;

import javax.swing.JFrame;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.clsf.Classifier;
import org.openmicroscopy.shoola.agents.hiviewer.clsf.ClassifierFactory;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;

import pojos.ImageData;

/** 
 * Command to classify/declassify a collection of images.
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
public class ClassifyCmd
    implements ActionCmd
{
    
    /**
     * Utility method to get an {@link ImageData} from the 
     * <code>model</code>.
     * 
     * @param model The Model from which to extract the Image.
     * @return The {@link ImageData} hierarchy object in the browser's
     *         current Image node or <code>null</code> if the browser's
     *         current display is not an Image node. 
     */
    private static ImageData[] getImages(HiViewer model)
    {
        ImageData[] images = null;
        if (model != null) {
            Set nodes = model.getBrowser().getSelectedDisplays();
            if (nodes != null) {
                Iterator i = nodes.iterator();
                Object x;
                images = new ImageData[nodes.size()];
                int index = 0;
                while (i.hasNext()) {
                    x = ((ImageDisplay) i.next()).getHierarchyObject();
                    if (x instanceof ImageData) {
                        images[index] = (ImageData) x;
                        index++;
                    }
                    index++;
                }
            }
        }
        return images;
    }
    
    /** 
     * The classification mode.
     * This is one of the constants defined by the {@link Classifier} interface
     * and tells whether we're classifying or declassifying.
     */
    private int                 mode;
    
    /** The images to classify/declassify. */
    private ImageData[]         images;
    
    /** The window from which this command was invoked. */
    private JFrame              owner;
    
    /** The id of the current user. */
    private long                userID;
    
    /** The id of the user's group used for data retrieval. */
    private long                groupID;
        
    /**
     * Creates a new command to classify/declassify the specified Image.
     * 
     * @param images    Represents the Image to classify/declassify.
     *                  If <code>null</code>, no acion is taken.
     * @param mode      The classification mode.  This is one of the constants 
     *                  defined by the {@link Classifier} interface and tells 
     *                  whether we're classifying or declassifying.
     * @param owner     The window from which this command was invoked.
     *                  Mustn't be <code>null</code>.
     * @param userID    The id of the current user.
     * @param groupID   The id of the user's group used for data retrieval.              
     */
    public ClassifyCmd(ImageData[] images, int mode, JFrame owner, long userID, 
                       long groupID)
    {
        if (owner == null) throw new NullPointerException("No owner.");
        this.images = images;
        this.mode = mode;
        this.owner = owner;
        this.userID = userID;
        this.groupID = groupID;
    }
     
    /**
     * Creates a new command to classify/declassify the specified Image.
     * 
     * @param image    Represents the Image to classify/declassify.
     *                  If <code>null</code>, no acion is taken.
     * @param mode      The classification mode.  This is one of the constants 
     *                  defined by the {@link Classifier} interface and tells 
     *                  whether we're classifying or declassifying.
     * @param owner     The window from which this command was invoked.
     *                  Mustn't be <code>null</code>.
     * @param userID    The id of the current user.
     * @param groupID   The id of the user's group used for data retrieval.                
     */
    public ClassifyCmd(ImageData image, int mode, JFrame owner, long userID, 
                        long groupID)
    {
        if (owner == null) throw new NullPointerException("No owner.");
        if (image != null) {
            images = new ImageData[1];
            images[0] = image;
        }
        this.mode = mode;
        this.owner = owner;
        this.userID = userID;
        this.groupID = groupID;
    }
    /**
     * Creates a new command to classify/declassify the Image in the browser's
     * currently selected node, if the node is an Image node.
     * If the node is not an Image node, no action is taken.
     * 
     * @param model The Model which has a reference to the browser.
     *              Mustn't be <code>null</code>.
     * @param mode  The classification mode.  This is one of the constants 
     *              defined by the {@link Classifier} interface and tells 
     *              whether we're classifying or declassifying.
     */
    public ClassifyCmd(HiViewer model, int mode) 
    { 
        this(getImages(model), mode, model.getUI(), 
                model.getUserDetails().getId(), model.getRootID()); 
    }
    
    /** 
     * Classifies or declassifies the image passed.
     * 
     * @see ActionCmd#execute() 
     */
    public void execute()
    {
        if (images == null) return;
        Classifier classifier = ClassifierFactory.createComponent(mode, images, 
                                            owner, userID, groupID);
        classifier.activate();
    }

}
