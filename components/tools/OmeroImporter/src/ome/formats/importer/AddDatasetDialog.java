/*
 * ome.formats.AddDatasetDialog
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2008 University of Dundee. All rights reserved.
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

package ome.formats.importer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import layout.TableLayout;

import ome.api.IUpdate;
import ome.formats.OMEROMetadataStore;
import ome.formats.importer.util.GuiCommonElements;
import ome.model.containers.Dataset;
import ome.model.containers.Project;


public class AddDatasetDialog extends JDialog implements ActionListener
{
    boolean debug = false;

    private Preferences    userPrefs = 
        Preferences.userNodeForPackage(ImportDialog.class);
    
    GuiCommonElements       gui;
    
    Window                  owner;
    
    JPanel                  mainPanel;
    JPanel                  internalPanel;

    JButton                 OKBtn;
    JButton                 cancelBtn;

    JTextField              nameField;
    JTextArea               descriptionArea;

    String                  datasetName;
    String                  datasetDescription;
    
    Dataset                 dataset;
    
    OMEROMetadataStore      store;

    Project                 project;
    
    AddDatasetDialog(Window owner, String title, Boolean modal, Project project, OMEROMetadataStore store)
    {
        this.project = project;
        this.store = store;
        this.owner = owner;
        
        gui = new GuiCommonElements();
        
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        setTitle(title);
        setModal(modal);
        setResizable(true);
        setSize(new Dimension(480, 300));
        setLocationRelativeTo(owner);
        
        // Set up the main panel for tPane, quit, and send buttons
        double mainTable[][] =
                {{TableLayout.FILL, 100, 5, 100, 10}, // columns
                {TableLayout.FILL, 40}}; // rows

        mainPanel = gui.addMainPanel(this, mainTable, 10,10,10,10, debug);
        
        // Add the quit and send buttons to the main panel
        cancelBtn = gui.addButton(mainPanel, "Cancel", 'C',
                "Cancel adding a dataset", "1, 1, f, c", debug);
        cancelBtn.addActionListener(this);

        OKBtn = gui.addButton(mainPanel, "OK", 'O',
                "Accept your new dataset", "3, 1, f, c", debug);
        OKBtn.addActionListener(this);

        this.getRootPane().setDefaultButton(OKBtn);
        gui.enterPressesWhenFocused(OKBtn);
        
        double internalTable[][] = 
            {{160, TableLayout.FILL}, // columns
            {30, 30, TableLayout.FILL}}; // rows
        
        internalPanel = gui.addMainPanel(this, internalTable, 10,10,10,10, debug);

        String message = "Please enter your dataset name and an optional " +
                "description below.";

        @SuppressWarnings("unused")
        JTextPane instructions = 
                gui.addTextPane(internalPanel, message, "0,0,1,0", debug);

        nameField = gui.addTextField(internalPanel, "Dataset Name: ", "", 'E',
        "Input your dataset name here.", "", TableLayout.PREFERRED, "0, 1, 1, 1", debug);
        
        descriptionArea = gui.addTextArea(internalPanel, "Description: (optional)", 
                "", 'W', "0, 2, 1, 2", debug);
        
        // Add the tab panel to the main panel
        mainPanel.add(internalPanel, "0, 0, 4, 0");
        add(mainPanel, BorderLayout.CENTER);
        
        setVisible(true);      

    }
        
    public void actionPerformed(ActionEvent e)
    {
        Object source = e.getSource();
        
        if (source == OKBtn)
        {  
            datasetName = nameField.getText();
            datasetDescription = descriptionArea.getText();
            
            if (datasetName.trim().length() > 0)
            {
                dataset = addDataset(datasetName, datasetDescription);
                userPrefs.putLong("savedDataset", dataset.getId());
                dispose();
            } else {
                JOptionPane.showMessageDialog(owner, "The project's name can not be blank.");
            }
        }
        
        if (source == cancelBtn)
        {
            dispose();
        }
    }

    private Dataset addDataset(String name, String description)
    {
        dataset = new Dataset();
        if (name.length() != 0)
            dataset.setName(name);
        if (description.length() != 0)
            dataset.setDescription(description);
        Project p = new Project(project.getId(), false);
        dataset.linkProject(p);
        
        Dataset storedDataset = null;
        
        if (store != null)
        {
            IUpdate iUpdate = store.getIUpdate();
            storedDataset = iUpdate.saveAndReturnObject(dataset);
            return storedDataset;
        } else {
            return null;
        }
    }

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception
    {
        String laf = UIManager.getSystemLookAndFeelClassName() ;
        //laf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        //laf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        //laf = "javax.swing.plaf.metal.MetalLookAndFeel";
        //laf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        
        if (laf.equals("apple.laf.AquaLookAndFeel"))
        {
            System.setProperty("Quaqua.design", "panther");
            
            try {
                UIManager.setLookAndFeel(
                    "ch.randelshofer.quaqua.QuaquaLookAndFeel"
                );
           } catch (Exception e) { System.err.println(laf + " not supported.");}
        } else {
            try {
                UIManager.setLookAndFeel(laf);
            } catch (Exception e) 
            { System.err.println(laf + " not supported."); }
        }
        new AddDatasetDialog(null, "Add dataset to...", true, null, null);
    }
}
