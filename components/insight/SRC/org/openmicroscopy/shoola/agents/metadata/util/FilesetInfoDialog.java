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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

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
    public final static int DEFAULT_HEIGHT = 300;

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
        if (set == null)
            return;

        StringBuffer buffer = new StringBuffer();
        buffer.append("<html>");
        buffer.append(set.size() + " Image file");
        buffer.append("<hr/>");
        buffer.append("Imported ");
        if (inPlaceImport) {
            buffer.append("with <b>--transfer=ln</b> ");
        }
        buffer.append("from:<br/>");

        Iterator<FilesetData> i = set.iterator();
        FilesetData data;
        List<String> paths;
        Iterator<String> j;
        while (i.hasNext()) {
            data = i.next();
            paths = data.getUsedFilePaths();
            j = paths.iterator();
            while (j.hasNext()) {
                buffer.append(j.next());
                buffer.append("<br/>");
            }
        }

        if (!inPlaceImport) {
            buffer.append("<hr/>");
            buffer.append("Path on server:<br/>");
            i = set.iterator();
            while (i.hasNext()) {
                data = i.next();
                paths = data.getAbsolutePaths();
                j = paths.iterator();
                while (j.hasNext()) {
                    buffer.append(j.next());
                    buffer.append("<br/>");
                }
            }
        }

        buffer.append("</html>");

        JTextPane content = new JTextPane();
        content.setContentType("text/html");
        content.setEditable(false);
        content.setText(buffer.toString());
        content.setBackground(UIUtilities.BACKGROUND_COLOR);
        setCanvas(new JScrollPane(content));
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
        pack();
        Dimension size = getSize();
        if (size.width > DEFAULT_WIDTH)
            size.width = DEFAULT_WIDTH;
        if (size.height > DEFAULT_HEIGHT)
            size.height = DEFAULT_HEIGHT;
        setSize(size);
        if (location != null) {
            setLocation(location);
            setVisible(true);
        }
        else {
            UIUtilities.centerAndShow(this);
        }
    }

}
