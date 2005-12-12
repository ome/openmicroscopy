/*
 * org.openmicroscopy.shoola.agents.hiviewer.cmd.LayoutCmd
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
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplayVisitor;
import org.openmicroscopy.shoola.agents.hiviewer.layout.LayoutFactory;
import org.openmicroscopy.shoola.agents.hiviewer.view.HiViewer;

/** 
 * Lays out the {@link ImageDisplay} objects according to the specified index.
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
public class LayoutCmd
    implements ActionCmd
{
    
    /** Reference to the model. */
    private HiViewer    model;
    
    /** One of the constants defined by the {@link LayoutFactory}. */
    private int         layoutIndex;
    
    /**
     * Checks if the index of the layout is supported.
     * 
     * @param index The passed index.
     * @return <code>true</code> if supported, <code>false</code> otherwise.
     */
    private boolean checkIndex(int index)
    {
        switch (index) {
            case LayoutFactory.SQUARY_LAYOUT:
            case LayoutFactory.TREE_LAYOUT:    
                return true;
        }
        return false;
    }
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the model. Mustn't be <code>null</code>.
     * @param layoutIndex The index of the layout.
     */
    public LayoutCmd(HiViewer model, int layoutIndex)
    {
        if (model == null) throw new IllegalArgumentException("No model");
        if (!checkIndex(layoutIndex))
            throw new IllegalArgumentException("Index not supported.");
        this.model = model;
        this.layoutIndex = layoutIndex;
    }
    
    /** Implemented as specified by {@link ActionCmd}. */
    public void execute()
    {
        Browser browser = model.getBrowser();
        if (browser.getSelectedLayout() == layoutIndex) return; 
        switch (layoutIndex) {
            case LayoutFactory.SQUARY_LAYOUT:
                browser.accept(LayoutFactory.createLayout(
                        LayoutFactory.SQUARY_LAYOUT, null),
                        ImageDisplayVisitor.IMAGE_SET_ONLY);
                browser.setTreeDisplay(null);
                browser.setSelectedLayout(LayoutFactory.SQUARY_LAYOUT);
                break;
            case LayoutFactory.TREE_LAYOUT:
                browser.accept(LayoutFactory.createLayout(
                        LayoutFactory.TREE_LAYOUT, browser),
                        ImageDisplayVisitor.IMAGE_SET_ONLY);
                browser.setSelectedLayout(LayoutFactory.TREE_LAYOUT);
        }
    }

}
