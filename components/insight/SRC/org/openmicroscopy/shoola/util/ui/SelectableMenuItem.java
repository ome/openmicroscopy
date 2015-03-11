/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
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
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JMenuItem;

/**
 * The purpose of this class is to get a JCheckboxMenuItem with the same style
 * as the {@link SelectableMenu}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class SelectableMenuItem extends JMenuItem {

    /** Bound property indicating that menuitem is selected. */
    public static final String SELECTION_PROPERTY;

    /** The default selected icon. */
    private static final Icon DEFAULT_SELECTED;

    /** The default deselected icon. */
    private static final Icon DEFAULT_DESELECTED;

    public static Color BG_COLOR;
    
    static {
        IconManager icons = IconManager.getInstance();
        DEFAULT_DESELECTED = icons.getIcon(IconManager.NOT_SELECTED);
        DEFAULT_SELECTED = icons.getIcon(IconManager.SELECTED);
        SELECTION_PROPERTY = "SelectableMenuItem.SELECTION_PROPERTY";
        BG_COLOR = (new JMenuItem()).getBackground();
    }

    /** The icon used when the menuitem is selected. */
    private Icon selectedIcon;

    /** The icon used when the menuitem is not selected. */
    private Icon deselectedIcon;

    /** Flag indicating if the menuitem is selectable or not. */
    private boolean selectable;

    private boolean fireProperty = true;
    
    /**
     * Creates a new instance.
     *
     * @param selectedIcon
     *            The icon used when the menu is selected.
     * @param deselectedIcon
     *            The icon used when the menu is not selected.
     * @param selectable
     *            Pass <code>true</code> to allow user selection,
     *            <code>false</code> otherwise.
     */
    public SelectableMenuItem(Icon selectedIcon, Icon deselectedIcon,
            boolean selectable) {
        this(selectedIcon, deselectedIcon, false, "", selectable);
    }

    /**
     * Creates a new instance.
     *
     * @param selectedIcon
     *            The icon used when the menu is selected.
     * @param deselectedIcon
     *            The icon used when the menu is not selected.
     * @param text
     *            The text of the menu.
     * @param selectable
     *            Pass <code>true</code> to allow user selection,
     *            <code>false</code> otherwise.
     */
    public SelectableMenuItem(Icon selectedIcon, Icon deselectedIcon,
            String text, boolean selectable) {
        this(selectedIcon, deselectedIcon, false, text, selectable);
    }

    /**
     * Creates a new instance.
     *
     * @param selectedIcon
     *            The icon used when the menu is selected.
     * @param deselectedIcon
     *            The icon used when the menu is not selected.
     * @param selected
     *            Pass <code>true</code> to select the menu, <code>false</code>
     *            otherwise.
     * @param selectable
     *            Pass <code>true</code> to allow user selection,
     *            <code>false</code> otherwise.
     */
    public SelectableMenuItem(Icon selectedIcon, Icon deselectedIcon,
            boolean selected, boolean selectable) {
        this(selectedIcon, deselectedIcon, selected, "", selectable);
    }

    /**
     * Creates a new instance.
     *
     * @param selected
     *            Pass <code>true</code> to select the menu, <code>false</code>
     *            otherwise.
     * @param text
     *            The text of the menu.
     * @param selectable
     *            Pass <code>true</code> to allow user selection,
     *            <code>false</code> otherwise.
     */
    public SelectableMenuItem(boolean selected, String text, boolean selectable) {
        this(DEFAULT_SELECTED, DEFAULT_DESELECTED, selected, text, selectable);
    }

    @Override
    protected void processMouseEvent(MouseEvent e) {
        // All MouseEvents have to be caught, because the JMenuItem's
        // MouseListeners shall not be triggered (i. e. makes sure the popup
        // menu stays open)!
        if (e.getButton() == MouseEvent.BUTTON1 && selectable) {
            setSelected(!isSelected());
            repaint();
            
            // but the ActionListeners have to triggered
            for(ActionListener l : getActionListeners()) {
                l.actionPerformed(new ActionEvent(this, 0, ""));
            }
        }
    }

    /**
     * Creates a new instance.
     *
     * @param selectedIcon
     *            The icon used when the menu is selected.
     * @param deselectedIcon
     *            The icon used when the menu is not selected.
     * @param selected
     *            Pass <code>true</code> to select the menu, <code>false</code>
     *            otherwise.
     * @param text
     *            The text of the menu.
     * @param selectable
     *            Pass <code>true</code> to allow user selection,
     *            <code>false</code> otherwise.
     */
    public SelectableMenuItem(Icon selectedIcon, Icon deselectedIcon,
            boolean selected, String text, boolean selectable) {
        super(text);
        this.selectedIcon = selectedIcon;
        this.deselectedIcon = deselectedIcon;
        fireProperty = false;
        setSelected(selected);
        fireProperty = true;
        this.selectable = selectable;
        setBackground(BG_COLOR);
    }
    
    @Override
    public boolean isSelected() {
        return getIcon() == selectedIcon;
    }

    
    @Override
    public void setSelected(boolean b) {
        if (b)
            setIcon(selectedIcon);
        else
            setIcon(deselectedIcon);
        if (fireProperty) {
            firePropertyChange(SELECTION_PROPERTY, !b, b);
        }
    }

    /**
     * Returns <code>true</code> if the item can be selected, <code>false</code>
     * otherwise.
     *
     * @return See above.
     */
    public boolean isSelectable() {
        return selectable;
    }

}
