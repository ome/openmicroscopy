/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.editor.EditorPane
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard.editor;



//Java imports
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Map;

import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardPane;

import pojos.ImageData;

/** 
 * Basic editor to display <code>Image</code> related information.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 2.2
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class EditorPane
    extends ClipBoardPane
{

    /** The component hosting the display. */
    private EditorPaneUI    uiDelegate;
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public EditorPane(ClipBoard model)
    {
        super(model);
        uiDelegate = new EditorPaneUI(this);
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.weightx = 1;
        
        // add uiDelegate which is the FindPaneUI. 
        add(uiDelegate, c);
    }
    
    /**
     * Overriden to update the UI components when a new node is selected in the
     * <code>Browser</code>.
     * @see ClipBoardPane#onDisplayChange(ImageDisplay)
     */
    public void onDisplayChange(ImageDisplay selectedDisplay)
    {
        if (model.getSelectedPaneIndex() != ClipBoard.INFO_PANE) return;
        if (selectedDisplay == null) {
            uiDelegate.displayDetails(null, null);
            return;
        }
        Object ho = selectedDisplay.getHierarchyObject();
        if (ho == null) {
            uiDelegate.displayDetails(null, null);
            return; //root
        }
        if (ho instanceof ImageData) {
            ImageData data = (ImageData) ho;
            Map details = 
                EditorPaneUtil.transformPixelsData(data.getDefaultPixels());
            uiDelegate.displayDetails(details, data.getName());
        } else uiDelegate.displayDetails(null, null);
    }
    
    /**
     * Overriden to return the name of this UI component.
     * @see ClipBoardPane#getPaneName()
     */
    public String getPaneName() { return "Info"; }

    /**
     * Overriden to return the name of this UI component.
     * @see ClipBoardPane#getPaneIcon()
     */
    public Icon getPaneIcon()
    {
        return IconManager.getInstance().getIcon(IconManager.INFO);
    }

    /**
     * Overriden to return the index of this UI component.
     * @see ClipBoardPane#getPaneIndex()
     */
    public int getPaneIndex() { return ClipBoard.INFO_PANE; }
    
    /**
     * Overriden to return the description of this UI component.
     * @see ClipBoardPane#getPaneDescription()
     */
    public String getPaneDescription() { return "Image's information."; }

}
