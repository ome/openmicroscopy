/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package org.openmicroscopy.shoola.util.ui;

import java.awt.BorderLayout;
import java.awt.LayoutManager;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.JXBusyLabel;

/**
 * A {@link JPanel} with some customizable text shown in its center and a
 * {@link JXBusyLabel} in the top right corner
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class DummyPanel extends JPanel {

    /** Label showing the text */
    private JLabel label;

    /** The busy indicator */
    private JXBusyLabel busyLabel;

    /**
     * Creates a new instance
     */
    public DummyPanel() {
        init();
    }

    /**
     * Creates a new instance
     * 
     * @param isDoubleBuffered
     *            See {@link JPanel#JPanel(boolean)}
     */
    public DummyPanel(boolean isDoubleBuffered) {
        super(isDoubleBuffered);
        init();
    }

    /**
     * Creates a new instance
     * 
     * @param layout
     *            See {@link JPanel#JPanel(LayoutManager, boolean)}
     * @param isDoubleBuffered
     *            See {@link JPanel#JPanel(LayoutManager, boolean)}
     */
    public DummyPanel(LayoutManager layout, boolean isDoubleBuffered) {
        super(layout, isDoubleBuffered);
        init();
    }

    /**
     * Creates a new instance
     * 
     * @param layout
     *            See {@link JPanel#JPanel(LayoutManager)}
     */
    public DummyPanel(LayoutManager layout) {
        super(layout);
        init();
    }

    /**
     * Set up the UI
     */
    private void init() {
        setBackground(UIUtilities.BACKGROUND);
        setLayout(new BorderLayout());

        busyLabel = new JXBusyLabel();
        busyLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        busyLabel.setVisible(false);
        busyLabel.setBusy(false);
        add(busyLabel, BorderLayout.NORTH);

        label = new JLabel("", SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);
    }

    /**
     * Set the text shown in the center of the component
     * 
     * @param text
     *            The text to display
     */
    public void setText(String text) {
        setText(text, false);
    }

    /**
     * Set the text shown in the center of the component
     * 
     * @param text
     *            The text to display
     * @param showBusy
     *            Flag to show the component as busy
     */
    public void setText(String text, boolean showBusy) {
        this.label.setText(text);
        busyLabel.setVisible(showBusy);
        busyLabel.setBusy(showBusy);
    }
}
