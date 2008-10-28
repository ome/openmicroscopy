/*
 * org.openmicroscopy.shoola.agents.hiviewer.clipboard.info.InfoPane
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

package org.openmicroscopy.shoola.agents.hiviewer.clipboard.info;


//Java imports
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.Icon;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.agents.hiviewer.IconManager;
import org.openmicroscopy.shoola.agents.hiviewer.browser.ImageDisplay;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoard;
import org.openmicroscopy.shoola.agents.hiviewer.clipboard.ClipBoardPane;
import pojos.ChannelData;
import pojos.ImageData;

/** 
 * Basic editor to present some basic metadata related to an <code>Image</code>.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * 				<a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author	Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 *  <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $ $Date: $)
 * </small>
 * @since OME2.2
 */
public class InfoPane
    extends ClipBoardPane
{

    /** The component hosting the display. */
    private InfoPaneUI    uiDelegate;
    
    /**
     * Creates a new instance.
     * 
     * @param model Reference to the Model. Mustn't be <code>null</code>.
     */
    public InfoPane(ClipBoard model)
    {
        super(model);
        uiDelegate = new InfoPaneUI(this);
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
     * Sets the channels metadata.
     * 
     * @param l 	The value to set.
     * @param data The image linked to metadata.
     */
    public void setChannelsMetadata(List l, ImageData data)
    {
        if (data != null && l != null) {
            Map details = 
                InfoPaneUtil.transformPixelsData(data.getDefaultPixels());
            String s = "";
            
            Iterator k = l.iterator();
            int j = 0;
            while (k.hasNext()) {
                s += 
                   ((ChannelData) k.next()).getEmissionWavelength();
                if (j != l.size()-1) s +=", ";
                j++;
            }
            details.put(InfoPaneUtil.WAVELENGTHS, s);
            uiDelegate.displayDetails(details, data.getName());
        }
    }
    
    
    /**
     * Overridden to update the UI components when a new node is selected in the
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
            model.retrieveChannelsMetadata(data);
            Map details = 
                InfoPaneUtil.transformPixelsData(data.getDefaultPixels());
            uiDelegate.displayDetails(details, data.getName());
        } else uiDelegate.displayDetails(null, null);
    }
    
    /**
     * Overridden to return the name of this UI component.
     * @see ClipBoardPane#getPaneName()
     */
    public String getPaneName() { return "Info"; }

    /**
     * Overridden to return the name of this UI component.
     * @see ClipBoardPane#getPaneIcon()
     */
    public Icon getPaneIcon()
    {
        return IconManager.getInstance().getIcon(IconManager.INFO);
    }

    /**
     * Overridden to return the index of this UI component.
     * @see ClipBoardPane#getPaneIndex()
     */
    public int getPaneIndex() { return ClipBoard.INFO_PANE; }
    
    /**
     * Overridden to return the description of this UI component.
     * @see ClipBoardPane#getPaneDescription()
     */
    public String getPaneDescription() { return "Image's information."; }
    
    /**
     * Overridden to return the name of this UI component.
     * @see ClipBoardPane#hasDataToSave()
     */
    public boolean hasDataToSave() {  return false; }

}

