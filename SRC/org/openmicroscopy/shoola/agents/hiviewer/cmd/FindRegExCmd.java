/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.FindRegExCmd
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

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.search.SearchExplorer;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/** 
 * 
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
    
    public static final int IN_TITLE = 0;
    
    public static final int IN_ANNOTATION = 1;
    
    /** Reference to the model. */
    private HiViewer    model;
    
    /** The expression.*/
    private String      regEx;
    
    /** One of the constants defined above. */
    private int         index;
    
    /** Creates a new instance.*/
    public FindRegExCmd(HiViewer model, String regEx, int index)
    {
        if (model == null)
            throw new IllegalArgumentException("no model");
        this.model = model;
        this.regEx = regEx;
        this.index = index;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
        FindRegExVisitor visitor = null;
        String title = "";
        switch (index) {
            case IN_TITLE:
                title = "With title";
                visitor = new FindRegExTitleVisitor(model, regEx);
                break;
            case IN_ANNOTATION:
                title = "With annotation";
                visitor = new FindRegExAnnotationVisitor(model, regEx);
        }
        if (visitor == null) return;
        //Clear fisrt.
        ClearCmd cmd = new ClearCmd(model);
        cmd.execute();
        Browser browser = model.getBrowser();
        ImageDisplay selectedDisplay = browser.getSelectedDisplay();
        if (selectedDisplay.getParentDisplay() == null) //root
            browser.accept(visitor);
        else selectedDisplay.accept(visitor);
        SearchExplorer explorer = new SearchExplorer(model.getUI(), title, 
                visitor.getFoundNodes());
        UIUtilities.centerAndShow(explorer);
    }

}
