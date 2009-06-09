/*
 * org.openmicroscopy.shoola.agents.util.ui.ChannelButtonPopupMenu 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2009 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.agents.util.ui;

//Java imports
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.border.BevelBorder;

//Third-party libraries

//Application-internal dependencies
import org.openmicroscopy.shoola.util.ui.IconManager;

/** 
 * The popup menu for the channel.
 *
 * @author  Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
class ChannelButtonPopupMenu  
	extends JPopupMenu
{
    
    /** The text of the color picker menu item. */
    private static final String COLOR_PICKER = "Color picker";
 
    /** The item to bring up the . */
    private JMenuItem       colorPickerItem;
    
    /** The channel this menu is for. */
    private ChannelButton   channelButton;
    
    /** Creates the menu items. */
    private void createItems()
    {
        IconManager icons = IconManager.getInstance();
        colorPickerItem = new JMenuItem(COLOR_PICKER, 
                            icons.getIcon(IconManager.COLOR_PICKER));
        colorPickerItem.addActionListener(new ActionListener() {
        
            public void actionPerformed(ActionEvent e)
            {
                channelButton.showColorPicker();
        
            }
        });
    }
    
    /** Builds and lays out the UI. */
    private void buildGUI()
    {
        setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        add(colorPickerItem);
    }
    
    /**
     * Creates a new instance.
     * 
     * @param channelButton The button the menu is for.
     */
    ChannelButtonPopupMenu(ChannelButton channelButton)
    {
        if (channelButton == null)
            throw new IllegalArgumentException("No channel button.");
        this.channelButton = channelButton;
        createItems();
        buildGUI();
    }
    
}
