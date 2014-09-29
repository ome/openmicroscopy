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

package org.openmicroscopy.shoola.agents.measurement.util.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import org.openmicroscopy.shoola.agents.measurement.view.GraphPane;
import org.openmicroscopy.shoola.util.filter.file.JPEGFilter;
import org.openmicroscopy.shoola.util.filter.file.PNGFilter;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.filechooser.FileChooser;
import org.openmicroscopy.shoola.util.ui.graphutils.ChartObject;

/**
 * Action to export a graph as JPEG or PNG image file.
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * 
 */
public class SaveAction extends AbstractAction {

    /** Reference to the GraphPane */
    private GraphPane graphPane;

    /**
     * Creates a new instance
     * 
     * @param graphPane Reference to the GraphPane
     */
    public SaveAction(GraphPane graphPane) {
        this(graphPane, "Save as...", "");
    }

    /**
     * Creates a new instance with a custom name/description
     * @param graphPane  Reference to the GraphPane
     * @param name Name
     * @param desc Description
     */
    public SaveAction(GraphPane graphPane, String name, String desc) {
        this.graphPane = graphPane;
        putValue(Action.NAME, name);
        putValue(Action.SHORT_DESCRIPTION, desc);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        List<FileFilter> filterList = new ArrayList<FileFilter>();
        filterList.add(new JPEGFilter());
        filterList.add(new PNGFilter());
        FileChooser chooser = new FileChooser(
                (JFrame) SwingUtilities.windowForComponent(graphPane),
                FileChooser.SAVE, "Save Graph",
                "Save the graph as JPEG or PNG", filterList);
        try {
            File f = UIUtilities.getDefaultFolder();
            if (f != null)
                chooser.setCurrentDirectory(f);
        } catch (Exception ex) {
        }
        if (chooser.showDialog() != JFileChooser.APPROVE_OPTION)
            return;
        File file = chooser.getFormattedSelectedFile();
        FileFilter filter = chooser.getSelectedFilter();

        int type = (filter instanceof JPEGFilter) ? ChartObject.SAVE_AS_JPEG
                : ChartObject.SAVE_AS_PNG;

        graphPane.saveGraph(file, type);
    }

}
