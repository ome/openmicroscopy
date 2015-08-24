/*
 * Copyright (C) 2014-2015 University of Dundee & Open Microscopy Environment.
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

package org.openmicroscopy.shoola.agents.metadata.util;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import omero.model.Fileset;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.agents.metadata.editor.ImportType;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.tdialog.TinyDialog;

import pojos.FilesetData;

/**
 * A {@link TinyDialog} displaying file paths
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class FilesetInfoDialog extends TinyDialog {

    /** This dialog's default width */
    public final static int DEFAULT_WIDTH = 400;

    /** This dialog's default height */
    public final static int DEFAULT_HEIGHT = 300;

    /**
     * Creates a new instance
     */
    public FilesetInfoDialog() {
        super(null, null, TinyDialog.CLOSE_ONLY, "");
    }

    /**
     * Sets the data to display
     * 
     * @param set
     *            The fileset which paths should be shown
     * @param importType
     *            The import type
     */
    public void setData(Set<FilesetData> set, ImportType importType) {
        if (set == null)
            return;

        JPanel content = new JPanel();
        content.setLayout(new GridBagLayout());
        content.setBackground(UIUtilities.BACKGROUND_COLOR);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.NORTHEAST;

        if (CollectionUtils.isEmpty(set)) {
            JLabel l = new JLabel("No information available.");
            l.setBackground(UIUtilities.BACKGROUND_COLOR);
            content.add(l, c);
        } else {
            int size = 0;
            FilesetData fsd = set.iterator().next();
            if (Fileset.class.isAssignableFrom(fsd.asIObject().getClass())) {
                size = ((Fileset)fsd.asIObject()).sizeOfUsedFiles();
            }
            String txt = size == 1 ? "Image file" : "Image files";
            JLabel l = new JLabel(size + " " + txt);
            l.setBackground(UIUtilities.BACKGROUND_COLOR);
            content.add(l, c);
            c.gridy++;

            JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
            sep.setBackground(UIUtilities.BACKGROUND_COLOR);
            content.add(sep, c);
            c.gridy++;

            String header = (importType == ImportType.HARDLINK || importType == ImportType.SOFTLINK) ? "Imported with <b>--transfer="
                    + importType.getSymbol() + "</b> from:"
                    : "Imported from:";
            
            ExpandableTextPane t1 = new ExpandableTextPane();
            t1.setBackground(UIUtilities.BACKGROUND_COLOR);
            t1.setText(header + "<br/>" + getOriginPaths(set));
            content.add(t1, c);
            c.gridy++;

            JSeparator sep2 = new JSeparator(JSeparator.HORIZONTAL);
            sep2.setBackground(UIUtilities.BACKGROUND_COLOR);
            content.add(sep2, c);
            c.gridy++;

            ExpandableTextPane t2 = new ExpandableTextPane();
            t2.setBackground(UIUtilities.BACKGROUND_COLOR);
            t2.setText("Path on server:<br/>" + getServerPaths(set));
            content.add(t2, c);
            
        }

        setCanvas(new JScrollPane(content));
    }

    /**
     * Get the original file paths; html formatted
     * @param set The fileset to extract the information from
     * @return See above
     */
    private String getOriginPaths(Set<FilesetData> set) {
        StringBuilder sb = new StringBuilder();
        
        Iterator<FilesetData> i = set.iterator();
        FilesetData data;
        List<String> paths;
        Iterator<String> j;
        while (i.hasNext()) {
            data = i.next();
            paths = data.getUsedFilePaths();
            j = paths.iterator();
            while (j.hasNext()) {
                sb.append(j.next());
                sb.append("<br/>");
            }
        }

        return sb.toString();
    }

    /**
     * Get the server paths; html formatted
     * @param set The fileset to extract the information from
     * @return See above.
     */
    private String getServerPaths(Set<FilesetData> set) {
        StringBuilder sb = new StringBuilder();

        Iterator<FilesetData> i = set.iterator();
        FilesetData data;
        List<String> paths;
        Iterator<String> j;
        while (i.hasNext()) {
            data = i.next();
            paths = data.getAbsolutePaths();
            j = paths.iterator();
            while (j.hasNext()) {
                sb.append(j.next());
                sb.append("<br/>");
            }
        }

        return sb.toString();
    }
    
    /**
     * Shows the dialog in the center of the screen
     */
    public void open() {
        open(null);
    }

    /**
     * Shows the dialog in a certain location
     * 
     * @param location
     *            See above
     */
    public void open(Point location) {
        setResizable(true);
        getContentPane().setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
        pack();
        Dimension size = getPreferredSize();
        if (size.width > DEFAULT_WIDTH)
            size.width = DEFAULT_WIDTH;
        if (size.height > DEFAULT_HEIGHT)
            size.height = DEFAULT_HEIGHT;
        // add some more pixels for the horiz. JScrollbar which
        // might be shown at the bottom
        size.height += 20;
        setSize(size);
        if (location != null) {
            setLocation(location);
            setVisible(true);
        } else {
            UIUtilities.centerAndShow(this);
        }
    }

}
