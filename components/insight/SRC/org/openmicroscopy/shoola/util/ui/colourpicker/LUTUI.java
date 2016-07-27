/*
 * org.openmicroscopy.shoola.util.ui.colourpicker.ColourMenuUI
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2016 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.util.ui.colourpicker;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.collections.CollectionUtils;

/**
 * This is a UI which represents the lookup tables as a list.
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class LUTUI extends JPanel {

    /** List of lookup tables. */
    private JList lutList;

    /** Scroll pane which contains the JList component. */
    private JScrollPane scrollpane;

    /** Listener for the lookup table list. */
    private ListSelectionListener selectionListener;

    /**
     * Boolean variable, true if the current component is active, this is
     * controlled from the parent component. It tells the UI whether or not to
     * ignore refresh events.
     */
    private boolean active;

    /** Reference to the RGBControl */
    private RGBControl c;

    /**
     * Create the UI
     */
    private void createUI() {
        LookupTableItem[] lutsArray = createLutsArray();
        if (lutsArray != null)
            lutList = new JList(lutsArray);
        else
            lutList = new JList();

        lutList.setCellRenderer(new LookupTableListRenderer());
        
        int index = 0;
        if (c.getLUT() != null && lutsArray != null) {
            for (int i = 0; i < lutsArray.length; i++) {
                if (lutsArray[i].matchesFilename(c.getLUT())) {
                    index = i;
                    break;
                }
            }
        }

        lutList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lutList.setLayoutOrientation(JList.VERTICAL);
        lutList.setVisibleRowCount(-1);
        lutList.setSelectedIndex(index);
        selectionListener = new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                LookupTableItem lut = (LookupTableItem) lutList
                        .getSelectedValue();
                if (lut == null || lut == LookupTableItem.NONE
                        || lut == LookupTableItem.SEPARATOR)
                    c.setLUT(null);
                else
                    c.setLUT(lut.getFilename());
            }

        };
        lutList.addListSelectionListener(selectionListener);

        scrollpane = new JScrollPane(lutList);
        this.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 80;
        gbc.weighty = 580;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);
        this.add(scrollpane, gbc);
    }

    /**
     * Creates an array of lookup table names based on the available lookup
     * tables plus including {@link #NONE} as first entry.
     * 
     * @return See above
     */
    private LookupTableItem[] createLutsArray() {
        List<LookupTableItem> list = new ArrayList<LookupTableItem>();

        if (CollectionUtils.isNotEmpty(c.getAvailableLookupTables())) {
            Iterator<String> it = c.getAvailableLookupTables().iterator();
            while (it.hasNext()) {
                list.add(new LookupTableItem(it.next()));
            }
        }

        Collections.sort(list);

        list.add(0, LookupTableItem.SEPARATOR);
        list.add(0, LookupTableItem.NONE);

        LookupTableItem[] lutsArray = new LookupTableItem[list.size()];
        lutsArray = list.toArray(lutsArray);
        return lutsArray;
    }

    /**
     * Create the UI and attach the control c.
     */
    LUTUI(RGBControl c) {
        this.c = c;
        createUI();
        active = false;
    }

    /** Resets the selection */
    void revert() {
        int index = 0;
        if (c.getLUT() != null && lutList.getModel().getSize() > 0) {
            for (int i = 0; i < lutList.getModel().getSize(); i++) {
                if (((LookupTableItem) lutList.getModel().getElementAt(i))
                        .matchesFilename(c.getLUT())) {
                    index = i;
                    break;
                }
            }
        }

        lutList.removeListSelectionListener(selectionListener);
        lutList.clearSelection();
        lutList.setSelectedIndex(index);
        lutList.addListSelectionListener(selectionListener);
    }

    /**
     * Sets the current component Active, called from parent control letting
     * this component know it should listen to refresh events.
     * 
     * @param act
     *            Pass <code>true</code> to set the component to active,
     *            <code>false</code> otherwise..
     */
    void setActive(boolean act) {
        active = act;
    }

    /**
     * Refresh method will be called by tabbedpanelUI when the model has
     * changed.
     */
    void refresh() {
        if (!(active))
            return;
        revert();
        repaint();
    }
}
