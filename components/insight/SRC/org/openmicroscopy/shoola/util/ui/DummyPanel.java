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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jdesktop.swingx.JXBusyLabel;

/**
 * A {@link JPanel} displaying a customizable text and an optional
 * {@link JXBusyLabel} in the center; does not allow to change the
 * {@link LayoutManager}
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
     *            Ignored; equivalent to {@link DummyPanel#DummyPanel(boolean)}
     * @param isDoubleBuffered
     *            See {@link JPanel#JPanel(LayoutManager, boolean)}
     */
    public DummyPanel(LayoutManager layout, boolean isDoubleBuffered) {
        this(isDoubleBuffered);
    }

    /**
     * Creates a new instance
     * 
     * @param layout
     *            Ignored; equivalent to {@link DummyPanel#DummyPanel()}
     */
    public DummyPanel(LayoutManager layout) {
        this();
    }

    /**
     * No effect - Not allowed to change LayoutManager
     */
    public void setLayout(LayoutManager mgr) {
    }

    /**
     * Set up the UI
     */
    private void init() {
        super.setLayout(new GridBagLayout());
        setBackground(UIUtilities.BACKGROUND);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets(0, 2, 0, 2);
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.CENTER;

        busyLabel = new JXBusyLabel(
                new Dimension(UIUtilities.DEFAULT_ICON_WIDTH,
                        UIUtilities.DEFAULT_ICON_HEIGHT));
        busyLabel.setVisible(false);
        busyLabel.setBusy(false);
        add(busyLabel, c);
        c.gridx++;

        label = new JLabel("");
        add(label, c);
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
