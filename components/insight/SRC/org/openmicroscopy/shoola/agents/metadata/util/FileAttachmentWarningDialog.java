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

import org.openmicroscopy.shoola.agents.metadata.FileAnnotationCheckResult;
import org.openmicroscopy.shoola.agents.metadata.IconManager;
import org.openmicroscopy.shoola.agents.util.EditorUtil;
import org.openmicroscopy.shoola.util.ui.TitlePanel;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import omero.gateway.model.FileAnnotationData;

/**
 * 
 * @author  Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class FileAttachmentWarningDialog extends JDialog {
    
    /** The property which is fired when the delete button is clicked */
    public static final String DELETE_PROPERTY = "delete";
    
    /** Title of the dialog */
    private static final String TITLE = "Delete Attachments";
    
    /** Message of the dialog */
    private static final String MESSAGE = "These attachments are not linked to any other data, removing them means they will be permanently deleted from the data repository.";
    
    /** The button to close the dialog.*/
    private JButton closeButton;
    
    /** Delete button */
    private JButton deleteButton;
    
    /** The result to display.*/
    private FileAnnotationCheckResult result;
    
    /** Closes and disposes.*/
    private void close()
    {
            setVisible(false);
            dispose();
    }
    
    /**
     * Fires delete property and disposes 
     */
    private void delete() {
        firePropertyChange(DELETE_PROPERTY, null, null);
        close();
    }
    
    /** Initializes the component.*/
    private void initialize()
    {
            closeButton = new JButton("Close");
            closeButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) { close(); }
            });
            deleteButton = new JButton("Delete");
            deleteButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent evt) { delete();}
            });
    }
    
    /** 
     * Builds and lays out the buttons.
     * 
     * @return See above.
     */
    private JPanel buildToolBar()
    {
            JPanel bar = new JPanel();
            bar.add(deleteButton);
            bar.add(Box.createRigidArea(UIUtilities.H_SPACER_SIZE));
            bar.add(closeButton);
            bar.add(Box.createRigidArea(UIUtilities.H_SPACER_SIZE));
            return UIUtilities.buildComponentPanelRight(bar);
    }
    
    /** Builds and lays out the UI.*/
    private void buildGUI()
    {
            Icon attachIcon = IconManager.getInstance().getIcon(IconManager.ATTACHMENT);
            
            setTitle(TITLE);
            
            TitlePanel tp = new TitlePanel(TITLE, MESSAGE, null);
            
            Container c = getContentPane();
            c.add(tp, BorderLayout.NORTH);
            
            JPanel p = new JPanel();
            p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
            p.setAlignmentX(LEFT_ALIGNMENT);
            p.setAlignmentY(TOP_ALIGNMENT);
            
            List<FileAnnotationData> annos = result.getDeleteCandidates();
            Collections.sort(annos, new FileAnnotationDataComparator());
            for(FileAnnotationData fd :annos) {
                JLabel label = new JLabel(attachIcon);
                label.setText(EditorUtil.truncate(fd.getFileName()));
                p.add(label);
            }
            p.add(Box.createVerticalGlue());
            
            JScrollPane sp = new JScrollPane(p);
            c.add(sp, BorderLayout.CENTER);
            
            c.add(buildToolBar(), BorderLayout.SOUTH);
    }
    
    
    /**
     * Creates a new instance.
     * 
     * @param owner The owner of the dialog.
     * @param result The result of the file annotation check.
     */
    public FileAttachmentWarningDialog(JFrame owner, FileAnnotationCheckResult result)
    {
            super(owner);
            if (result.getDeleteCandidates().isEmpty())
                    throw new IllegalArgumentException("No result to display");
            this.result = result;
            initialize();
            buildGUI();
            pack();
            setSize(new Dimension(400,300));
    }
    
    /**
     * A Comparator for sorting FileAnnotationData by their file name
     */
    class FileAnnotationDataComparator implements Comparator<FileAnnotationData> {

        @Override
        public int compare(FileAnnotationData o1, FileAnnotationData o2) {
            return String.CASE_INSENSITIVE_ORDER.compare(o1.getFileName(), o2.getFileName());
        }
        
    }
}
