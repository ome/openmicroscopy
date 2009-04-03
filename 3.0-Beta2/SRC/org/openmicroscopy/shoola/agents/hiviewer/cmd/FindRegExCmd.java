/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.FindRegExCmd
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

package org.openmicroscopy.shoola.agents.hiviewer.cmd;


//Java imports
import java.util.regex.Pattern;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.finder.FindData;
import org.openmicroscopy.shoola.agents.hiviewer.treeview.TreeView;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;

/** 
 * Command used to retrieve a regular expression.
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
public class FindRegExCmd
    implements ActionCmd
{

    /** Reference to the model. */
    private HiViewer    model;
    
    /** The pattern created from the regular expression.*/
    private Pattern     pattern;
    
    /** The context of the find. */
    private FindData    findContext;
    
    /**
     * Creates a new instance.
     * 
     * @param model         The <code>HiViewer</code> model.
     *                      Mustn't be <code>null</code>. 
     * @param pattern       The pattern to find. Mustn't be <code>null</code>. 
     * @param findContext   The context of the find action.
     *                      Mustn't be <code>null</code>. 
     */
    public FindRegExCmd(HiViewer model, Pattern pattern, FindData findContext)
    {
        if (model == null)
            throw new IllegalArgumentException("No model.");
        if (pattern == null)
            throw new IllegalArgumentException("No pattern.");
        if (findContext == null)
            throw new IllegalArgumentException("No context.");
        this.model = model;
        this.pattern = pattern;
        this.findContext = findContext;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
        Browser browser = model.getBrowser();
        FindRegExVisitor visitor = new FindRegExVisitor(model, pattern, 
                findContext);
        if (browser != null) browser.accept(visitor);
        TreeView tree = model.getTreeView();
        if (tree != null) tree.repaint();
        model.setFoundResults(visitor.getFoundNodes());
    }

}
