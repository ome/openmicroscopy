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


//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.agents.util.classifier.view.Classifier;
import pojos.ImageData;

/** 
 * Command to classify or declassify a collection of images.
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
     * Flag to denote that a {@link Classifier} was created to classify an
     * Image.
     */
    public static final int     CLASSIFICATION_MODE = Classifier.CLASSIFY_MODE;
    
    /** 
     * Flag to denote that a {@link Classifier} was created to declassify an
     * Image.
     */
    public static final int     DECLASSIFICATION_MODE = 
    									Classifier.DECLASSIFY_MODE;
    
    /** 
     * The classification mode.
     * This is one of the constants defined by the {@link Classifier} interface
     * and tells whether we're classifying or declassifying.
     */
    private int                 mode;
    
    /** The images to classify/declassify. */
    private ImageData[]         images;
    
    /** Reference to the model. */
    private HiViewer			model;
    
    /**
     * Controls if the passed mode is supported.
     * 
     * @param m The value to control.
     */
    private void checkMode(int m)
    {
    	switch (m) {
			case DECLASSIFICATION_MODE:
			case CLASSIFICATION_MODE:
				break;
	
			default:
				throw new IllegalArgumentException("Mode not supported.");
		}
    }
    
    /**
     * Utility method to get an {@link ImageData} from the 
     * <code>model</code>.
     * 
     * @return 		The {@link ImageData} hierarchy object in the browser's
     *         		current Image node or <code>null</code> if the browser's
     *         		current display is not an Image node. 
     */
    private ImageData[] getImages()
    {
        ImageData[] images = null;
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
            }
        }
        return images;
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
    	if (model == null)
    		throw new IllegalArgumentException("No model.");
    	checkMode(mode);
    	this.mode = mode;
    	this.model = model;
    	images = getImages();
    }
    
    /**
     * Creates a new command to classify/declassify the Image in the browser's
     * currently selected node, if the node is an Image node.
     * If the node is not an Image node, no action is taken.
     * 
     * @param model 	The Model which has a reference to the browser.
     *              	Mustn't be <code>null</code>.
     * @param image     The image to classify/declassify.
     * 					Mustn't be <code>null</code>.            
     * @param mode  	The classification mode. This is one of the constants 
     *              	defined by the {@link Classifier} interface and tells 
     *              	whether we're classifying or declassifying.
     */
    public ClassifyCmd(HiViewer model, ImageData image, int mode) 
    { 
    	if (model == null)
    		throw new IllegalArgumentException("No model.");
    	if (image == null)
    		throw new IllegalArgumentException("No image to categorise.");
    	checkMode(mode);
    	this.mode = mode;
    	this.model = model;
    	images = new ImageData[1];
    	images[0] = image;
    }
    
    /** 
     * Classifies or declassifies the images depending on the {@link #mode}.
     * @see ActionCmd#execute() 
     */
    public void execute()
    {
        model.classifyImages(images, mode);
    }

}
