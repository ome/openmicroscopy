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
import java.util.Iterator;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.collections.CollectionUtils;

import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * This is a UI which represents the lookup tables as a list.
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class LUTUI extends JPanel {

    /** The name of the 'None' lookup table entry */
    private static final String NONE = "None";

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
                LookupTableItem lut = (LookupTableItem) lutList.getSelectedValue();
                if (lut == null || lut.equals(NONE))
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
        LookupTableItem[] lutsArray = null;
        if (CollectionUtils.isNotEmpty(c.getAvailableLookupTables())) {
            lutsArray = new LookupTableItem[c.getAvailableLookupTables().size() + 1];
            lutsArray[0] = new LookupTableItem(NONE);
            int i = 1;
            Iterator<String> it = c.getAvailableLookupTables().iterator();
            while (it.hasNext()) {
                lutsArray[i] =  new LookupTableItem(it.next());
                i++;
            }
        }
        Arrays.sort(lutsArray);
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
                if (( (LookupTableItem)lutList.getModel().getElementAt(i) ).matchesFilename(c.getLUT())) {
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

    /**
     * Item for the Lookup Table list, which shows a nicely formatted name for
     * the lookup table, based on the file name.
     */
    class LookupTableItem implements Comparable<LookupTableItem> {

        /** The file name **/
        private String filename;

        /** More readable name generated from the filename */
        private String readableName;

        /**
         * Create new instance
         * 
         * @param filename
         *            The lut file name
         */
        LookupTableItem(String filename) {
            this.filename = filename;
            this.readableName = generateReadableName(filename);
        }

        /**
         * Generates a more readable name for the given lut filename by removing
         * '*.lut' extension, underscores and using upper case at the beginning
         * of words.
         * 
         * @param filename
         *            The filename
         * @return See above
         */
        private String generateReadableName(String filename) {
            filename = filename.replace(".lut", "");
            String[] parts = filename.replace(".lut", "").split("_");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                sb.append(part.substring(0, 1).toUpperCase());
                if (part.length() > 1) {
                    sb.append(part.substring(1));
                }

                if (i < parts.length - 1)
                    sb.append(' ');
            }

            return sb.toString();
        }

        @Override
        public String toString() {
            return readableName;
        }

        /**
         * Get the lut file name
         * 
         * @return See above
         */
        public String getFilename() {
            return this.filename;
        }

        /**
         * @param filename
         *            The file name
         * @return <code>true</code> if the given filename matches the filename
         *         of this {@link LookupTableItem}
         */
        public boolean matchesFilename(String filename) {
            return this.filename.equals(filename);
        }

        @Override
        public int compareTo(LookupTableItem o) {
            return this.readableName.compareTo(o.readableName);
        }
    }
}
