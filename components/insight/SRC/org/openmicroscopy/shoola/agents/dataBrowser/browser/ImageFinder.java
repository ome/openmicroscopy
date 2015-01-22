/*
 * org.openmicroscopy.shoola.agents.dataBrowser.browser.ImageFinder 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.dataBrowser.browser;




//Java imports
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;

//Third-party libraries

//Application-internal dependencies
import pojos.DataObject;
import pojos.ExperimenterData;
import pojos.FileData;
import pojos.ImageData;
import pojos.WellSampleData;

/** 
 * Initializes two sets: one containing the imageNodes displayed
 * and a second containing the corresponding <code>DataObject</code>s.
 *
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since OME3.0
 */
public class ImageFinder     
	implements ImageDisplayVisitor
{

    /** Set of <code>ImageNode</code>s */
    private Set<ImageDisplay>	imageNodes;
    
    /** Set of corresponding <code>DataObject</code>s */
    private Set<DataObject>		images;
    
    /** Set of <code>ImageNode</code>s */
    private Set<ImageNode>		visibleImageNodes;
    
    /** Set of corresponding visible <code>DataObject</code>s */
    private Set<DataObject>		visibleImages;
    
    /** Creates a new instance. */
    public ImageFinder()
    {
    	 images = new HashSet<DataObject>();
         imageNodes = new HashSet<ImageDisplay>();
         visibleImages = new HashSet<DataObject>();
         visibleImageNodes = new HashSet<ImageNode>();
    }
   
    /** 
     * Returns the set of {@link ImageNode}s displayed. 
     * 
     * @return See above.
     */
    public Set<ImageDisplay> getImageNodes() { return imageNodes; }
    
    /** 
     * Returns the set of corresponding <code>DataObject</code>s. 
     * 
     * @return See above.
     */
    public Set<DataObject> getImages() { return images; }
    
    /** 
     * Returns the set of {@link ImageNode}s displayed. 
     * 
     * @return See above.
     */
    public Set<ImageNode> getVisibleImageNodes()
    { 
    	return visibleImageNodes; 
    }
    
    /** 
     * Returns the set of visible <code>DataObject</code>s. 
     * 
     * @return See above.
     */
    public Set<DataObject> getVisibleImages() { return visibleImages; }
    
    /** 
     * Implemented as specified by {@link ImageDisplayVisitor}. 
     * @see ImageDisplayVisitor#visit(ImageNode)
     */
    public void visit(ImageNode node)
    {
        imageNodes.add(node);
        visibleImageNodes.add(node);
        Object ho = node.getHierarchyObject();
        if (ho instanceof WellSampleData) {
        	WellSampleData wsd = (WellSampleData) ho;
        	ho = wsd.getImage();
        }
        if (ho instanceof ImageData) images.add((ImageData) ho);
    }

    /** 
     * Implemented as specified by {@link ImageDisplayVisitor}. 
     * @see ImageDisplayVisitor#visit(ImageSet)
     */
    public void visit(ImageSet node)
    {
    	if (node == null) return;
    	//if (node.containsImages()) {
    		JComponent desktop = node.getInternalDesktop();
    		Component[] comps = desktop.getComponents();
    		if (comps != null) {
    			Component c;
    			ImageNode n;
    			Object ho;
    			WellSampleData wsd;
    			for (int i = 0; i < comps.length; i++) {
					c = comps[i];
					if (c instanceof ImageNode) {
						n = (ImageNode) c;
						ho = n.getHierarchyObject();
						if (ho instanceof WellSampleData) {
				        	wsd = (WellSampleData) ho;
				        	ho = wsd.getImage();
				        } else if (ho instanceof ImageData) {
							visibleImages.add((ImageData) ho);
							visibleImageNodes.add(n);
						} else if (ho instanceof FileData) {
							visibleImages.add((FileData) ho);
							visibleImageNodes.add(n);
						} else if (ho instanceof ExperimenterData) {
							visibleImages.add((ExperimenterData) ho);
							visibleImageNodes.add(n);
						}
					}
				}
    		}
    	//}
    }

}
