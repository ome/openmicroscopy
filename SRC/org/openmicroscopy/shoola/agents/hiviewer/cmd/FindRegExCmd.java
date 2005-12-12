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
import java.util.regex.Pattern;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.browser.Browser;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageSet;
import org.openmicroscopy.shoola.agents.hiviewer.layout.LayoutFactory;
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
    
    /** Indicates to search in the title of the dataObject. */
    public static final int IN_TITLE = 0;
    
    /** Indicates to search in the annotation of the dataObject. */
    public static final int IN_ANNOTATION = 1;
    
    /** Indicates to search in the title and annotation of the dataObject. */
    public static final int IN_T_AND_A = 2;
    
    /** Reference to the model. */
    private HiViewer    model;
    
    /** The pattern created from the regular expression.*/
    private Pattern     pattern;
    
    /** One of the constants defined above. */
    private int         index;
    
    /**
     * Checks the index passed.
     * 
     * @param i The passed index.
     * @return true if the index is one the constants defined by this class.
     */
    private boolean checkIndex(int i)
    {
        switch (i) {
            case IN_TITLE:
            case IN_ANNOTATION:
            case IN_T_AND_A:
                return true;
        }
        return false;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model     The <code>HiViewer</code> model.
     *                  Mustn't be <code>null</code>. 
     * @param pattern   The pattern. Mustn't be <code>null</code>. 
     * @param index     The search index.
     *                  One of the constants defined by this class.
     */
    public FindRegExCmd(HiViewer model, Pattern pattern, int index)
    {
        if (model == null)
            throw new IllegalArgumentException("No model.");
        if (pattern == null)
            throw new IllegalArgumentException("No pattern.");
        if (!checkIndex(index))
            throw new IllegalArgumentException("Search index not valid.");
        this.model = model;
        this.pattern = pattern;
        this.index = index;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
        FindRegExVisitor visitor = null;
        switch (index) {
            case IN_TITLE:
                visitor = new FindRegExTitleVisitor(model, pattern);
                break;
            case IN_ANNOTATION:
                visitor = new FindRegExAnnotationVisitor(model, pattern);
                break;
            case IN_T_AND_A:
                visitor = new FindRegExTitleAndAnnotationVisitor(model,
                                pattern);    
        }
        if (visitor == null) return;
        Browser browser = model.getBrowser();
        ImageDisplay selectedDisplay = browser.getSelectedDisplay();
        if (selectedDisplay.getParentDisplay() == null) //root
            browser.accept(visitor);
        else {
            if (selectedDisplay instanceof ImageSet)
                selectedDisplay.accept(visitor);
        } 
        if (browser.getSelectedLayout() == LayoutFactory.TREE_LAYOUT) {
            if (browser.getTreeDisplay() != null)
                browser.getTreeDisplay().repaint();
        }
        model.getClipBoard().setSearchResults(visitor.getFoundNodes());
    }

}
