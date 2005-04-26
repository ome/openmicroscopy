/*
 * org.openmicroscopy.shoola.agents.hiviewer.browser.ImageFinder
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

package org.openmicroscopy.shoola.agents.hiviewer.browser;




//Java imports
import java.util.HashSet;
import java.util.Set;

//Third-party libraries

//Application-internal dependencies

/** 
 * Initializes two set: one containing the imageNodes displayed
 * and a second containing the corresponding <code>DataObject</code>s.
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
class ImageFinder
    implements ImageDisplayVisitor
{

    /** Set of <code>ImageNode</code>s */
    private Set imageNodes;
    
    /** Set of corresponding <code>DataObject</code>s */
    private Set images;
    
    /** Creates a new instance. */
    ImageFinder()
    {
        images = new HashSet();
        imageNodes = new HashSet();
    }
    
    /** Returns the set of {@link ImageNode}s displayed. */
    Set getImageNodes() { return imageNodes; }
    
    /** Returns the set of corresponding <code>DataObject</code>s. */
    Set getImages() { return images; }
    
    /** Implemented as specified by {@link ImageDisplayVisitor}. */
    public void visit(ImageNode node)
    {
        imageNodes.add(node);
        images.add(node.getHierarchyObject());
    }

    /** Implemented as specified by {@link ImageDisplayVisitor}. */
    public void visit(ImageSet node) {}

}
