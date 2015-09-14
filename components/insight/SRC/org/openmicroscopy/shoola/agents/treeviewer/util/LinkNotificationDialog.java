/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2014 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
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
package org.openmicroscopy.shoola.agents.treeviewer.util;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;

import org.openmicroscopy.shoola.agents.treeviewer.IconManager;
import org.openmicroscopy.shoola.agents.util.ui.FixedSizeSeparator;
import org.openmicroscopy.shoola.env.data.model.ImageCheckerResult;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;


/**
 * Notifies the user that some images he wants to delete are linked to multiple
 * DataSets
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; 
 * <ahref="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class LinkNotificationDialog extends JDialog {

    /** The maximum number of datasets to show per image */
    private static final int MAX_DATASETS_PER_IMAGE = 3;

    /** Bound property indicating to move all the objects. */
    public static final String DELETE_PROPERTY = "delete";

    /** The title of the dialog if it is a <code>Delete</code> action. */
    private static final String TITLE = "Delete";

    private static final String MESSAGE = "These images are linked to multiple datasets.\nDeleting them will remove them from all datasets!";

    /** The button to close the dialog. */
    private JButton closeButton;

    /** The button to close the dialog. */
    private JButton deleteButton;

    /** The result to display. */
    private ImageCheckerResult result;

    /** Closes and disposes. */
    private void close() {
        setVisible(false);
        dispose();
    }

    /** Initializes the component. */
    private void initialize() {
        closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {

            /**
             * Closes the dialog.
             */
            public void actionPerformed(ActionEvent evt) {
                close();
            }
        });

        deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {

            /**
             * Fires delete property and closes the dialog.
             */
            public void actionPerformed(ActionEvent evt) {
                firePropertyChange(DELETE_PROPERTY, null, null);
                close();
            }
        });
    }

    /**
     * Builds and lays out the buttons.
     * 
     * @return See above.
     */
    private JPanel buildToolBar() {
        JPanel bar = new JPanel();
        bar.add(deleteButton);
        bar.add(Box.createRigidArea(UIUtilities.H_SPACER_SIZE));
        bar.add(closeButton);
        bar.add(Box.createRigidArea(UIUtilities.H_SPACER_SIZE));
        return UIUtilities.buildComponentPanelRight(bar);
    }

    /** Builds and lays out the UI. */
    private void buildGUI() {
        Icon dsIcon = IconManager.getInstance().getIcon(IconManager.DATASET);

        String title = TITLE;
        setTitle(title);

        TitlePanel tp = new TitlePanel(title, MESSAGE, null);

        Container c = getContentPane();
        c.add(tp, BorderLayout.NORTH);

        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setAlignmentY(0);
        p.setAlignmentX(0);

        List<ImageData> imgs = result.getMultiLinkedImages();
        Collections.sort(imgs, new ImageDataComparator());

        for (ImageData img : imgs) {
            List<DatasetData> ds = result.getDatasets(img);

            JPanel row = new JPanel();
            row.setLayout(new BoxLayout(row, BoxLayout.Y_AXIS));

            String imgName = img.getName();
            if (imgName.length() > 50) {
                imgName = "..."
                        + imgName.substring(imgName.length() - 50,
                                imgName.length());
            }
            JLabel imgNameLabel = new JLabel(imgName);
            row.add(imgNameLabel);

            row.add(Box.createRigidArea(new Dimension(0, 10)));

            int i = 0;
            for (DatasetData dd : ds) {
                if (i < MAX_DATASETS_PER_IMAGE) {
                    JLabel dsLabel = new JLabel(dsIcon);
                    dsLabel.setText(dd.getName());
                    // indent
                    dsLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0,
                            0));
                    row.add(dsLabel);
                } else {
                    JLabel dsLabel = new JLabel("... (" + (ds.size() - i)
                            + " more)");
                    // indent
                    dsLabel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0,
                            0));
                    row.add(dsLabel);
                    break;
                }
                i++;
            }
            p.add(row);
            p.add(Box.createRigidArea(new Dimension(0, 10)));
            p.add(new FixedSizeSeparator(JSeparator.HORIZONTAL));
        }
        p.add(Box.createVerticalGlue());

        JScrollPane sp = new JScrollPane(p);
        c.add(sp, BorderLayout.CENTER);

        c.add(buildToolBar(), BorderLayout.SOUTH);
    }

    /**
     * Creates a new instance.
     * 
     * @param owner
     *            The owner of the dialog.
     * @param result
     *            The result of the Image-Dataset-Linkcheck.
     */
    public LinkNotificationDialog(JFrame owner, ImageCheckerResult result) {
        super(owner);
        this.result = result;
        initialize();
        buildGUI();
        pack();
        setSize(new Dimension(400, 400));
    }

    /**
     * Comparator to sort ImageData objects according to their names
     */
    class ImageDataComparator implements Comparator<ImageData> {

        @Override
        public int compare(ImageData o1, ImageData o2) {
            return String.CASE_INSENSITIVE_ORDER.compare(o1.getName(),
                    o2.getName());
        }

    }
}
