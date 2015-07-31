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
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.treeviewer.util;

//Java imports
import ij.ImagePlus;
import ij.WindowManager;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openmicroscopy.shoola.agents.events.treeviewer.SaveResultsEvent;
import org.openmicroscopy.shoola.agents.treeviewer.TreeViewerAgent;
import org.openmicroscopy.shoola.env.data.model.FileObject;
import org.openmicroscopy.shoola.env.data.model.ResultsObject;
import org.openmicroscopy.shoola.env.event.SaveEvent;
import omero.gateway.SecurityContext;
import org.openmicroscopy.shoola.util.ui.MessageBox;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.ExperimenterData;


/** 
 * Dialog used to save results from ImageJ.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.1.0
 */
public class SaveResultsDialog
    extends JDialog
{

    /** Select to save or not the ROI.*/
    private JCheckBox roi;

    /** Select to save or not the results table.*/
    private JCheckBox table;
    
    /** Flag indicating to select the image from the active window.*/
    private boolean activeWindow;

    /** Close the dialog.*/
    private JButton cancelButton;

    /** Close the dialog.*/
    private JButton saveButton;

    /** Initializes the components.
     * 
     * @param index The index indicating what to save.
     */
    private void initialize(int index)
    {
        activeWindow = true;
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });
        saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });
        ChangeListener l = new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
                saveButton.setEnabled(roi.isSelected() || table.isSelected());
              }
            };
        roi = new JCheckBox("ROI");
        roi.addChangeListener(l);
        table = new JCheckBox("Measurements");
        table.addChangeListener(l);
        if (index == SaveEvent.ALL) {
            roi.setSelected(true);
            table.setSelected(true);
        } else if (index == SaveEvent.ROIS) {
            roi.setSelected(true);
        } else if (index == SaveEvent.RESULTS) {
            table.setSelected(true);
        }
    }

    /** Closes the dialog.*/
    private void cancel()
    {
        setVisible(false);
        dispose();
    }

    /** Closes the dialog.*/
    private void save()
    {
        List<Object> images = new ArrayList<Object>();
        FileObject img;
        List<Object> toImport = new ArrayList<Object>();
        ImagePlus plus;
        if (activeWindow) {
            plus = WindowManager.getCurrentImage();
            if (plus != null) {
                img = new FileObject(plus);
                if (img.getOMEROID() < 0 || img.isNewImage()) {
                    toImport.add(img);
                  //check if there are associated files
                    int[] values = WindowManager.getIDList();
                    String path = img.getAbsolutePath();
                    if (path != null) {
                        FileObject ff;
                        for (int i = 0; i < values.length; i++) {
                            ff = new FileObject(WindowManager.getImage(values[i]));
                            if (path.equals(ff.getAbsolutePath())) {
                                img.addAssociatedFile(ff);
                            }
                        }
                    }
                } else images.add(img);
            }
        } else {
            int[] values = WindowManager.getIDList();
            if (values != null) {
                for (int i = 0; i < values.length; i++) {
                    plus = WindowManager.getImage(values[i]);
                    img = new FileObject(plus);
                    if (img.getOMEROID() < 0 || img.isNewImage()) {
                        toImport.add(img);
                    } else {
                        images.add(img);
                    }
                }
            }
        }
        //Check if the images are OMERO images
        ResultsObject result;
        if (toImport.size() > 0) { //ask if they want to import the image
            StringBuffer buf = new StringBuffer();
            buf.append("Do you wish to import any selected images not already "
                    + "saved in OMERO to the OMERO server?");
            MessageBox box = new MessageBox(this, "Import images", buf.toString());
            if (box.centerMsgBox() == MessageBox.YES_OPTION) {
                 result = new ResultsObject(toImport);
                 result.setROI(roi.isSelected());
                 result.setTable(table.isSelected());
                 TreeViewerAgent.getRegistry().getEventBus().post(
                         new SaveResultsEvent(result, true));
            }
        }
        if (images.size() > 0) {
            result = new ResultsObject(images);
            result.setROI(roi.isSelected());
            result.setTable(table.isSelected());
            ExperimenterData exp = TreeViewerAgent.getUserDetails();
            SecurityContext ctx = new SecurityContext(exp.getGroupId());
            ctx.setExperimenter(exp);
            TreeViewerAgent.getRegistry().getUserNotifier().notifyActivity(ctx,
                    result);
        }
        cancel();
    }

    /**
     * Builds and lays out the controls
     *
     * @return See above.
     */
    private JPanel buildToolBar()
    {
        JPanel bar = new JPanel();
        bar.add(saveButton);
        bar.add(Box.createRigidArea(UIUtilities.H_SPACER_SIZE));
        bar.add(cancelButton);
        bar.add(Box.createRigidArea(UIUtilities.H_SPACER_SIZE));
        return UIUtilities.buildComponentPanelRight(bar);
    }

    /**
     * Builds and lays out the elements indicating what to save.
     *
     * @return See above.
     */
    private JPanel buildContents()
    {
        JPanel buttons = new JPanel();
        buttons.add(UIUtilities.setTextFont("Save results for"));
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.Y_AXIS));
        ButtonGroup group = new ButtonGroup();
        JRadioButton b = new JRadioButton("Image from current window");
        b.setSelected(activeWindow);
        buttons.add(b);
        group.add(b);
        b.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                activeWindow = (e.getStateChange() == ItemEvent.SELECTED);
            }
        });
        b = new JRadioButton("Images from all image windows");
        b.setSelected(activeWindow);
        buttons.add(b);
        group.add(b);
        buttons.add(Box.createRigidArea(UIUtilities.H_SPACER_SIZE));
        buttons.add(UIUtilities.setTextFont("Save"));
        buttons.add(roi);
        buttons.add(table);
        return UIUtilities.buildComponentPanel(buttons);
    }

    /**
     * Builds and lays out the UI.
     */
    private void buildGUI()
    {
        Container c = getContentPane();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.add(buildContents(), BorderLayout.CENTER);
        c.add(buildToolBar(), BorderLayout.SOUTH);
    }

    /**
     * Creates a new instance.
     *
     * @param parent The owner of the frame.
     * @param index The save index.
     */
    public SaveResultsDialog(JFrame parent, int index)
    {
        super(parent);
        setTitle("Save ImageJ results");
        initialize(index);
        buildGUI();
        pack();
    }
}
