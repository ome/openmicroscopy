/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2013 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.util.ui;

//Java imports
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JMenu;

/**
 * JMenu behaving like a JCheckBoxMenuItem.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.0
 */
public class SelectableMenu
    extends JMenu
{

    /** The default selected icon.*/
    private static final Icon DEFAULT_SELECTED;

    /** The default deselected icon.*/
    private static final Icon DEFAULT_DESELECTED;

    static {
        IconManager icons = IconManager.getInstance();
        DEFAULT_DESELECTED = icons.getIcon(IconManager.TRANSPARENT);
        DEFAULT_SELECTED = icons.getIcon(IconManager.SELECTED);
    }

    /** The icon used when the menu is selected.*/
    private Icon selectedIcon;

    /** The icon used when the menu is not selected.*/
    private Icon deselectedIcon;

    /**
     * Creates a new instance.
     *
     * @param selectedIcon The icon used when the menu is selected.
     * @param deselectedIcon The icon used when the menu is not selected.
     */
    public SelectableMenu(Icon selectedIcon, Icon deselectedIcon)
    {
        this(selectedIcon, deselectedIcon, false, "");
    }

    /**
     * Creates a new instance.
     *
     * @param selectedIcon The icon used when the menu is selected.
     * @param deselectedIcon The icon used when the menu is not selected.
     * @param text The text of the menu.
     */
    public SelectableMenu(Icon selectedIcon, Icon deselectedIcon, String text)
    {
        this(selectedIcon, deselectedIcon, false, "");
    }

    /**
     * Creates a new instance.
     *
     * @param selectedIcon The icon used when the menu is selected.
     * @param deselectedIcon The icon used when the menu is not selected.
     * @param selected Pass <code>true</code> to select the menu,
     *                 <code>false</code> otherwise.
     */
    public SelectableMenu(Icon selectedIcon, Icon deselectedIcon,
            boolean selected)
    {
        this(selectedIcon, deselectedIcon, selected, "");
    }

    /**
     * Creates a new instance.
     *
     * @param selected Pass <code>true</code> to select the menu,
     *                 <code>false</code> otherwise.
     * @param text The text of the menu.
     */
    public SelectableMenu(boolean selected, String text)
    {
        this(DEFAULT_SELECTED, DEFAULT_DESELECTED, selected, text);
    }

    /**
     * Creates a new instance.
     *
     * @param selectedIcon The icon used when the menu is selected.
     * @param deselectedIcon The icon used when the menu is not selected.
     * @param selected Pass <code>true</code> to select the menu,
     *                 <code>false</code> otherwise.
     * @param text The text of the menu.
     */
    public SelectableMenu(Icon selectedIcon, Icon deselectedIcon,
            boolean selected, String text)
    {
        this.selectedIcon = selectedIcon;
        this.deselectedIcon = deselectedIcon;
        setMenuSelected(selected);
        setText(text);
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                setMenuSelected(!isMenuSelected());
                repaint();
            }
        });
    }

    /**
     * Returns <code>true</code> if the menu is selected, <code>false</code>
     * otherwise.
     *
     * @return See above.
     */
    public boolean isMenuSelected()
    {
        return getIcon() == selectedIcon;
    }

    /**
     * Sets the icon corresponding to the specified value.
     *
     * @param selected Pass <code>true</code> to select the menu,
     *                 <code>false</code> otherwise.
     */
    public void setMenuSelected(boolean selected)
    {
        if (selected) setIcon(selectedIcon);
        else setIcon(deselectedIcon);
    }

}
