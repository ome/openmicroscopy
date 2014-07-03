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

package org.openmicroscopy.shoola.agents.metadata.util;

import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JScrollPane;

import org.openmicroscopy.shoola.util.ui.MultilineLabel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import org.openmicroscopy.shoola.util.ui.tdialog.TinyDialog;

import pojos.FilesetData;

/**
 * A {@link TinyDialog} displaying file paths
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class FilesetInfoDialog extends TinyDialog {

    /** This dialog's default width */
    public final static int DEFAULT_WIDTH = 400;

    /** This dialog's default height */
    public final static int DEFAULT_HEIGHT = 100;

    /**
     * Creates a new instance
     */
    public FilesetInfoDialog() {
        super(null, null, TinyDialog.CLOSE_ONLY, "");
    }

    /**
     * Sets the data to display
     * @param set The fileset which paths should be shown
     * @param inPlaceImport Flag if this is an inplace import
     */
    public void setData(Set<FilesetData> set, boolean inPlaceImport) {
        if (set == null) return;
        Iterator<FilesetData> i = set.iterator();
        FilesetData data;
        MultilineLabel label = new MultilineLabel();
        StringBuffer buffer = new StringBuffer();
        List<String> paths;
        Iterator<String> j;
        int n = 0;
        while (i.hasNext()) {
            data = i.next();
            if (inPlaceImport) {
                paths = data.getUsedFilePaths();
            }
            else {
                paths = data.getAbsolutePaths();
            }
            j = paths.iterator();
            n += paths.size();
            while (j.hasNext()) {
                buffer.append(j.next());
                buffer.append(System.getProperty("line.separator"));
            }
        }
        label.setText(buffer.toString());

        setCanvas(new JScrollPane(label));

        setTitle(n+" File path(s)");
    }

    /**
     * Shows the dialog in the center of the screen
     */
    public void open() {
        open(null);
    }

    /**
     * Shows the dialog in a certain location
     * @param location See above
     */
    public void open(Point location) {
        addWindowFocusListener(new WindowFocusListener() {

            /**
             * Closes the dialog when the window loses focus.
             * 
             * @see WindowFocusListener#windowLostFocus(WindowEvent)
             */
            public void windowLostFocus(WindowEvent evt) {
                TinyDialog d = (TinyDialog) evt.getSource();
                d.setClosed(true);
                d.closeWindow();
            }

            /**
             * Required by the I/F but no-operation in our case.
             * 
             * @see WindowFocusListener#windowGainedFocus(WindowEvent)
             */
            public void windowGainedFocus(WindowEvent evt) {
            }
        });
        setResizable(true);
        getContentPane().setBackground(UIUtilities.BACKGROUND_COLOUR_EVEN);
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
        if (location != null) {
            setLocation(location);
            setVisible(true);
        }
        else {
            UIUtilities.centerAndShow(this);
        }
    }

}
