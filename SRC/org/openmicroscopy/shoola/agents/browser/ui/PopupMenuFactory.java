/*
 * org.openmicroscopy.shoola.agents.browser.ui.PopupMenuFactory
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

/*------------------------------------------------------------------------------
 *
 * Written by:    Jeff Mellen <jeffm@alum.mit.edu>
 *
 *------------------------------------------------------------------------------
 */
 
package org.openmicroscopy.shoola.agents.browser.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.openmicroscopy.shoola.agents.browser.events.PiccoloAction;
import org.openmicroscopy.shoola.agents.browser.events.PiccoloActionFactory;
import org.openmicroscopy.shoola.agents.browser.images.Thumbnail;

/**
 * Generates a popup menu for a set of selected/particular thumbnails.
 *
 * @author Jeff Mellen, <a href="mailto:jeffm@alum.mit.edu">jeffm@alum.mit.edu</a><br>
 * <b>Internal version:</b> $Revision$ $Date$
 * @version 2.2
 * @since OME2.2
 */
public class PopupMenuFactory
{
    /**
     * Returns a popup menu based on the type of object that triggered the
     * popup.  Will return null if none of the objects should generate a
     * popup menu when right-clicked.
     * 
     * @param o The source of the popup.
     * @return A popup menu if the object is a proper source, null otherwise.
     */
    public static JPopupMenu getMenu(Object o)
    {
        if(o instanceof Thumbnail)
        {
            return getThumbnailMenu((Thumbnail)o);
        }
        else if(o instanceof BrowserView.BackgroundNode)
        {
            return getBackgroundMenu((BrowserView.BackgroundNode)o);
        }
        else return null;
    }
    
    /**
     * Returns a popup menu showing options for a single thumbnail.  Should
     * be selected when a *single* thumbnail is right-clicked.
     * @param t The thumbnail to base the actions upon.
     * @return A JPopupMenu with triggerable actions, to be shown over that
     *         particular thumbnail.
     */
    public static JPopupMenu getThumbnailMenu(final Thumbnail t)
    {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem openItem = new JMenuItem("Open");
        openItem.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent arg0)
            {
                PiccoloAction action =
                    PiccoloActionFactory.getOpenInViewerAction(t);
                action.execute();
            }
        });
        menu.add(openItem);
        menu.addSeparator();
        return menu;
    }
    
    /**
     * Returns a popup menu showing options for the background (i.e., the
     * dataset options for the entire browser)
     * 
     * 
     * @param bn The background node to base the actions upon.
     * @return A JPopupMenu with triggerable actions, to be shown over that
     *         point in the browser background.
     */
    public static JPopupMenu getBackgroundMenu(final BrowserView.BackgroundNode bn)
    {
        JPopupMenu menu = new JPopupMenu();
        JMenuItem sampleItem = new JMenuItem("Nothing!");
        menu.add(sampleItem);
        return menu;        
    }
}
