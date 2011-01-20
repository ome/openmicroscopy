/*
 * org.openmicroscopy.shoola.agents.treeviewer.cmd.BrowserVisitor
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006 University of Dundee. All rights reserved.
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

package org.openmicroscopy.shoola.agents.treeviewer.cmd;



//Java imports

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.treeviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageNode;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageSet;

/** 
 * SuperClass, that all visitors that need to know about the status of the
 * browser should extend.
 * For example, some visitors may need to know which node has been selected.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision$ $Date$)
 * </small>
 * @since OME2.2
 */
public class BrowserVisitor
    implements TreeImageDisplayVisitor
{
    
    /** Reference to the {@link Browser model}. */
    protected Browser model;

    /**
     * Creates a new instance.
     * 
     * @param model     Reference to the {@link Browser model}.
     *                  Mustn't be <code>null</code>
     */
    public BrowserVisitor(Browser model)
    {
        if (model == null) throw new IllegalArgumentException("No model.");
        this.model = model;
    }
    /** 
     * Required by {@link TreeImageDisplayVisitor} I/F. Sub-classes
     * will implement the method.
     * @see TreeImageDisplayVisitor#visit(TreeImageNode)
     */
    public void visit(TreeImageNode node) {}

    /** 
     * Required by {@link TreeImageDisplayVisitor} I/F. Sub-classes
     * will implement the method.
     * @see TreeImageDisplayVisitor#visit(TreeImageSet)
     */
    public void visit(TreeImageSet node) {}
    
}
