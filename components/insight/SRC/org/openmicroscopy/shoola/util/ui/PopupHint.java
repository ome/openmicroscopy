/*
 * Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.openmicroscopy.shoola.util.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 * A popup hint in the style of a tooltip, e. g. for displaying a 'lightweight'
 * warning message
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class PopupHint {

    /** Delay until the tooltip vanishes again */
    private static final int DEFAULT_SHOW_TIME = 3000;

    /** The width of the empty border around the tooltip */
    private static final int INSETS = 3;

    /** The component the hint is attached to */
    private JComponent component;

    /** The hint, which is actually a popup menu */
    private JPopupMenu popup;

    /** The timer providing the hide delay */
    private Timer hideTimer;

    /** The message which is shown */
    private JLabel text;

    /**
     * Creates a new instance
     * 
     * @param component
     *            The component the hint is attached to
     * @param message
     *            The message to show
     */
    public PopupHint(final JComponent component, String message) {
        this(component, message, DEFAULT_SHOW_TIME);
    }

    /**
     * Creates a new instance
     * 
     * @param component
     *            The component the hint is attached to
     * @param message
     *            The message to show
     * @param showTime
     *            The duration for showing the hint
     */
    public PopupHint(final JComponent component, String message, int showTime) {
        this.component = component;

        Color c = UIUtilities.TOOLTIP_COLOR;
        Font f = (FontUIResource) UIManager.get("ToolTip.font");

        popup = new JPopupMenu();
        popup.setLayout(new BoxLayout(popup, BoxLayout.PAGE_AXIS));
        popup.setBorder(BorderFactory.createEmptyBorder(INSETS, INSETS, INSETS,
                INSETS));
        popup.setBackground(c);
        popup.setFont(f);

        text = new JLabel(message);
        text.setBackground(c);
        text.setFont(f);
        popup.add(text);

        hideTimer = new Timer(showTime, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                popup.setVisible(false);
                hideTimer.stop();
            }
        });

    }

    /**
     * Shows the hint
     */
    public void show() {
        if (!popup.isVisible()) {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Dimension textDim = text.getPreferredSize();
            
            if (component.isShowing()) {
                // make sure the hint is not shown outside the screen area
                int xOffset = 0;
                int xScreen = component.getLocationOnScreen().x + textDim.width;
                if (xScreen > (screenSize.width - 5)) {
                    xOffset = xScreen - screenSize.width + 5;
                }

                int x = component.getLocation().x - xOffset;
                int y = component.getLocation().y - textDim.height - 5;
                popup.show(component, x, y);
            } else
                UIUtilities.centerAndShow(popup);
            
            hideTimer.start();
        } else {
            hideTimer.restart();
        }
    }
}
