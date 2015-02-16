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
package org.openmicroscopy.shoola.agents.metadata.util;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openmicroscopy.shoola.agents.metadata.IconManager;

/**
 * A {@link ExpandableTextPane} with a header and expand/collapse icon
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class ExpandableHeaderTextPane extends JPanel {

    /** Reference to the plus icon */
    private static final Icon PLUS = IconManager.getInstance().getIcon(
            IconManager.PLUS_12);

    /** Reference to the minus icon */
    private static final Icon MINUS = IconManager.getInstance().getIcon(
            IconManager.MINUS_12);

    /** The header label */
    private final JLabel header;

    /** The icon label */
    private final JLabel icon;

    /** The text pane */
    private final ExpandableTextPane pane;

    /**
     * Creates a new instance without header and the default number of lines (
     * {@link ExpandableTextPane.DEFAULT_LINES}) shown in collapsed state.
     */
    public ExpandableHeaderTextPane() {
        this(ExpandableTextPane.DEFAULT_LINES);
    }

    /**
     * Creates a new instance without header and a specific number of lines
     * shown in collapsed state.
     */
    public ExpandableHeaderTextPane(int showLines) {
        this("", ExpandableTextPane.DEFAULT_LINES);
    }

    /**
     * Creates a new instance with header and the default number of lines (
     * {@link ExpandableTextPane.DEFAULT_LINES}) shown in collapsed state.
     */
    public ExpandableHeaderTextPane(String headerText) {
        this(headerText, ExpandableTextPane.DEFAULT_LINES);
    }

    /**
     * Creates a new instance
     * 
     * @param headerText
     *            The text shown in the header
     * @param showLines
     *            The number of lines shown in collapsed state
     */
    public ExpandableHeaderTextPane(String headerText, int showLines) {
        if (!headerText.contains("<(H|h)(T|t)(M|m)(L|l)/?>"))
            headerText = "<html>" + headerText + "</html>";
        header = new JLabel(headerText);
        header.setVisible(headerText != null && headerText.trim().length() > 0);

        pane = new ExpandableTextPane(showLines);

        icon = new JLabel(PLUS);
        icon.setVisible(false);

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.NONE;
        c.anchor = GridBagConstraints.WEST;

        add(header, c);

        c.gridx++;
        c.anchor = GridBagConstraints.EAST;

        add(icon, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;

        add(pane, c);

        icon.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    if (pane.isExpanded()) {
                        pane.setExpanded(false);
                        icon.setIcon(PLUS);
                    } else {
                        pane.setExpanded(true);
                        icon.setIcon(MINUS);
                    }
                }
            }

        });
    }

    /**
     * Sets the text
     * 
     * @param text
     *            The text to show
     */
    public void setText(String text) {
        pane.setText(text);
        icon.setVisible(pane.isExpandable());
        revalidate();
    }
}
