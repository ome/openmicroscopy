/*
 * org.openmicroscopy.xdoc.navig.NavMenuManager
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

package org.openmicroscopy.xdoc.navig;


//Java imports
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;

//Third-party libraries

//Application-internal dependencies

/** 
 * Transforms events in the navigation tree into requests to display
 * document pages.
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
class NavMenuManager
    implements TreeSelectionListener
{

    /** The UI that displays the table of contents. */
    private NavMenuUI       toc;
    
    /** A viewer capable to show the document pages. */
    private DocumentViewer  docViewer;
    
    /**
     * Creates a new instance.
     * 
     * @param toc The UI that displays the table of contents.
     *              Mustn't be <code>null</code>.
     * @param docViewer A viewer capable to show the document pages.
     *                  Mustn't be <code>null</code>.
     */
    NavMenuManager(NavMenuUI toc, DocumentViewer docViewer)
    {
        if (toc == null) throw new NullPointerException("No toc.");
        if (docViewer == null) throw new NullPointerException("No docViewer.");
        this.toc = toc;
        this.docViewer = docViewer;
        toc.addTreeSelectionListener(this);
    }
    
    /**
     * Every time a selection is made in the navigation tree, make a call
     * to display the corresponding document page.
     * @see TreeSelectionListener#valueChanged(TreeSelectionEvent)
     */
    public void valueChanged(TreeSelectionEvent tse)
    {
        DefaultMutableTreeNode node = toc.getLastSelectedPathComponent();
        if (node == null) return;
        SectionDescriptor section = (SectionDescriptor) node.getUserObject();
        docViewer.showPage(section.url);
    }

}
