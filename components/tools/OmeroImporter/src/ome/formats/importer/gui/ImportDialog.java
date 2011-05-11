/*
 * ome.formats.importer.gui.History
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.importer.gui;

import static omero.rtypes.rstring;
import info.clearthought.layout.TableLayout;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.gui.GuiCommonElements.DecimalNumberField;
import ome.formats.importer.gui.GuiCommonElements.WholeNumberField;
import omero.RLong;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.Project;
import omero.model.ProjectI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author "Brian W. Loranger"
 */
@SuppressWarnings("serial")
public class ImportDialog extends JDialog implements ActionListener
{
    boolean debug = false;

    private ImportConfig       	config;

    private Integer                 dialogHeight = 360;
    private Integer                 dialogWidth = 400;

    private JTabbedPane tabbedPane;
    
    private JPanel  importPanel;
    private JPanel  pdPanel;
    private JPanel  namedPanel;
    
    private JPanel  metadataPanel;
    private JPanel  pixelPanel;
    private JPanel  channelPanel;

    // Add graphic for add button
    String addIcon = "gfx/add_text.png";

    private JRadioButton fullPathButton;
    private JRadioButton partPathButton;

    private WholeNumberField numOfDirectoriesField;
    private DecimalNumberField xPixelSize, yPixelSize, zPixelSize;
    private WholeNumberField rChannel, gChannel, bChannel;

    public JCheckBox archiveImage, useCustomNamingChkBox;

    private JButton             addProjectBtn;
    private JButton             addDatasetBtn;
    private JButton             cancelBtn;
    private JButton             importBtn;

    private JComboBox pbox;
    private JComboBox dbox;

    public  Dataset dataset;
    public  Project project;
    
    Double pixelSizeX, pixelSizeY, pixelSizeZ;
    public  int redChannel, greenChannel, blueChannel;
    
    public  ProjectI newProject;
    
    public  DatasetItem[] datasetItems = null;
    public  ProjectItem[] projectItems = null;

    public boolean    cancelled = true;
    
    private boolean ARCHIVE_ENABLED = true;

    /** Logger for this class. */
    @SuppressWarnings("unused")
    private static Log          log     = LogFactory.getLog(ImportDialog.class);

    public OMEROMetadataStoreClient store;

    /**
     * Display a dialog so the user can choose project/dataset for importing
     * 
     * @param config - ImportConfig
     * @param owner - parent frame
     * @param title - dialog title
     * @param modal - modal yes/no
     * @param store - Initialized OMEROMetadataStore
     */
    ImportDialog(ImportConfig config, JFrame owner, String title, boolean modal, OMEROMetadataStoreClient store)
    {
        this.store = store;

        if (store != null)
        {
            projectItems = ProjectItem.createProjectItems(store.getProjects());
        }
        
        setLocation(200, 200);
        setTitle(title);
        setModal(modal);
        setResizable(false);
        setSize(new Dimension(dialogWidth, dialogHeight));
        setLocationRelativeTo(owner);

        tabbedPane = new JTabbedPane();
        tabbedPane.setOpaque(false); // content panes must be opaque

        this.config = config;

        
        /////////////////////// START IMPORT PANEL ////////////////////////
        
        // Set up the import panel for tPane, quit, and send buttons
        
        double mainTable[][] =
            {{TableLayout.FILL, 120, 5, 160, TableLayout.FILL}, // columns
            {TableLayout.PREFERRED, 10, TableLayout.PREFERRED, 
                TableLayout.FILL, 40, 30}}; // rows

        importPanel = GuiCommonElements.addMainPanel(tabbedPane, mainTable, 0,10,0,10, debug);

        String message = "Import these images into which dataset?";
        GuiCommonElements.addTextPane(importPanel, message, "0, 0, 4, 0", debug);

        // Set up the project/dataset table
        double pdTable[][] =
        {{TableLayout.FILL, 5, 40}, // columns
                {35, 35}}; // rows

        // Panel containing the project / dataset layout

        pdPanel = GuiCommonElements.addMainPanel(importPanel, pdTable, 0, 0, 0, 0, debug);

        pbox = GuiCommonElements.addComboBox(pdPanel, "Project: ", projectItems, 'P', 
                "Select dataset to use for this import.", 60, "0,0,F,C", debug);
        pbox.addActionListener(this);

        // Fixing broken mac buttons.
        String offsetButtons = ",C";
        //if (GuiCommonElements.offsetButtons == true) offsetButtons = ",t";

        addProjectBtn = GuiCommonElements.addIconButton(pdPanel, "", addIcon, 20, 60, null, null, "2,0,f" + offsetButtons, debug);
        addProjectBtn.addActionListener(this);
        
        dbox = GuiCommonElements.addComboBox(pdPanel, "Dataset: ", datasetItems, 'D',
                "Select dataset to use for this import.", 60, "0,1,F,C", debug);

        dbox.setEnabled(false);

        addDatasetBtn = GuiCommonElements.addIconButton(pdPanel, "", addIcon, 20, 60, null, null, "2,1,f" + offsetButtons, debug);
        addDatasetBtn.addActionListener(this);
        
        //addDatasetBtn.setEnabled(false);
        
        importPanel.add(pdPanel, "0, 2, 4, 2");

        // File naming section

        double namedTable[][] =
        {{30, TableLayout.FILL}, // columns
                {24, TableLayout.PREFERRED, 
            TableLayout.PREFERRED, TableLayout.FILL}}; // rows      

        namedPanel = GuiCommonElements.addBorderedPanel(importPanel, namedTable, "File Naming", debug);

        String fullPathTooltip = "The full file+path name for the file. For example: \"c:/myfolder/mysubfolder/myfile.dv\"";

        String partPathTooltip = "A partial path and file name for the file. For example: \"mysubfolder/myfile.dv\"";

        fullPathButton = GuiCommonElements.addRadioButton(namedPanel, "the full path+file name of your file", 'u', 
                fullPathTooltip, "1,1", debug);
        fullPathButton.addActionListener(this);

        partPathButton = GuiCommonElements.addRadioButton(namedPanel, "a partial path+file name with...", 'u', 
                partPathTooltip, "1,2", debug);
        partPathButton.addActionListener(this);

        numOfDirectoriesField = GuiCommonElements.addWholeNumberField(namedPanel, "" , "0", "of the directories immediately before it.", 0, 
                "Add this number of directories to the file names", 3, 40, "1,3,L,C", debug);
        numOfDirectoriesField.addActionListener(this);
        
        numOfDirectoriesField.setText(Integer.toString(config.getNumOfDirectories()));

        // focus on the partial path button if you enter the numofdirfield
        numOfDirectoriesField.addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) {
                partPathButton.setSelected(true);
            }

            public void focusLost(FocusEvent e) {}

        });        
        
        ButtonGroup group = new ButtonGroup();
        group.add(fullPathButton);
        group.add(partPathButton);

        //if (config.useFullPath.get() == true )
        if (config.getUserFullPath() == true )
            group.setSelected(fullPathButton.getModel(), true);
        else
            group.setSelected(partPathButton.getModel(), true);
        
        useCustomNamingChkBox = GuiCommonElements.addCheckBox(namedPanel, "Override default file naming. Instead use:", "0,0,1,0", debug);
        useCustomNamingChkBox.addActionListener(this);
        //if (config.useCustomImageNaming.get() == true)
        if (config.getCustomImageNaming() == true)
        {
        	useCustomNamingChkBox.setSelected(true);
        	enabledPathButtons(true);
        } else  {
        	useCustomNamingChkBox.setSelected(false);
        	enabledPathButtons(false);
        }

        importPanel.add(namedPanel, "0, 3, 4, 2");

        archiveImage = GuiCommonElements.addCheckBox(importPanel, 
        		"Archive the original imported file(s) to the server.", "0,4,4,4", debug);
        archiveImage.addActionListener(this);
        
        archiveImage.setSelected(config.archiveImage.get());
        
        // Override config.archiveImage.get() if 
        // import.config is set for forceFileArchiveOn
        if (config.getForceFileArchiveOn() == true)
        	archiveImage.setSelected(true);
        
        if (ARCHIVE_ENABLED)
        {
        	archiveImage.setVisible(true);
        } else {
        	archiveImage.setVisible(false);                
        }
                
        // Buttons at the bottom of the form

        cancelBtn = GuiCommonElements.addButton(importPanel, "Cancel", 'L', "Cancel", "1, 5, f, c", debug);
        cancelBtn.addActionListener(this);

        importBtn = GuiCommonElements.addButton(importPanel, "Add to Queue", 'Q', "Import", "3, 5, f, c", debug);
        importBtn.addActionListener(this);
        importBtn.setEnabled(false);
        this.getRootPane().setDefaultButton(importBtn);

        this.getRootPane().setDefaultButton(importBtn);
        GuiCommonElements.enterPressesWhenFocused(importBtn);


        /////////////////////// START METADATA PANEL ////////////////////////
        
        double metadataTable[][] =
        {{TableLayout.FILL}, // columns
        {TableLayout.FILL, 10, TableLayout.FILL}}; // rows
        
        metadataPanel = GuiCommonElements.addMainPanel(tabbedPane, metadataTable, 0,10,0,10, debug);
        
        double pixelTable[][] =
            {{10,TableLayout.FILL, 10,TableLayout.FILL, 10, TableLayout.FILL,10}, // columns
             {68, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL}}; // rows      

        pixelPanel = GuiCommonElements.addBorderedPanel(metadataPanel, pixelTable, "Pixel Size Defaults", debug);
        
        message = "These X, Y & Z pixel size values (typically measured in microns) " +
        		"will be used if no values are included in the image file metadata:";
        GuiCommonElements.addTextPane(pixelPanel, message, "1, 0, 6, 0", debug);
        
        xPixelSize = GuiCommonElements.addDecimalNumberField(pixelPanel, 
                "X: " , null, "", 0, "", 8, 80, "1,1,L,C", debug);

        yPixelSize = GuiCommonElements.addDecimalNumberField(pixelPanel, 
                "Y: " , null, "", 0, "", 8, 80, "3,1,L,C", debug);

        zPixelSize = GuiCommonElements.addDecimalNumberField(pixelPanel, 
                "Z: " , null, "", 0, "", 8, 80, "5,1,L,C", debug);
        
        metadataPanel.add(pixelPanel, "0, 0");

        double channelTable[][] =
        {{10,TableLayout.FILL, 10,TableLayout.FILL, 10, TableLayout.FILL,10}, // columns
         {68, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL}}; // rows      

        channelPanel = GuiCommonElements.addBorderedPanel(metadataPanel, channelTable, "Channel Defaults", debug);
        
        rChannel = GuiCommonElements.addWholeNumberField(channelPanel, 
                "R: " , "0", "", 0, "", 8, 80, "1,1,L,C", debug);

        gChannel = GuiCommonElements.addWholeNumberField(channelPanel, 
                "G: " , "1", "", 0, "", 8, 80, "3,1,L,C", debug);

        bChannel = GuiCommonElements.addWholeNumberField(channelPanel, 
                "B: " , "2", "", 0, "", 8, 80, "5,1,L,C", debug);
        
        message = "These RGB channel wavelengths (typically measured in nanometers)" +
        		" will be used if no channel values are included in the image file metadata:";
        GuiCommonElements.addTextPane(channelPanel, message, "1, 0, 6, 0", debug);
        
        //metadataPanel.add(channelPanel, "0, 2");

    
        /////////////////////// START TABBED PANE ////////////////////////
        
        this.add(tabbedPane);
        tabbedPane.addTab("Import Settings", null, importPanel, "Import Settings");
        tabbedPane.addTab("Metadata Defaults", null, metadataPanel, "Metadata Defaults");

        getProjectDatasets(projectItems[0].getProject());
        buildProjectsAndDatasets();
        setVisible(true);
    }

    /**
     * Create projects and dataset pulldown lists
     */
    private void buildProjectsAndDatasets()
    {
        long savedProject = config.savedProject.get();
        long savedDataset = config.savedDataset.get();
        
        if (projectItems != null) {
            for (int i = 0; i < projectItems.length; i++)
            {
                RLong pId = projectItems[i].getProject().getId();

                if (pId != null && pId.getValue() == savedProject)
                {
                    pbox.setSelectedIndex(i);

                    Project p = ((ProjectItem) pbox.getSelectedItem()).getProject();
                    datasetItems = DatasetItem.createDatasetItems(store.getDatasets(p));
                    dbox.removeAllItems();
                    if (datasetItems.length == 0)
                    {
                        datasetItems = DatasetItem.createEmptyDataset();
                        dbox.addItem(datasetItems[0]);
                        dbox.setEnabled(false);
                        //addDatasetBtn.setEnabled(false);
                        addDatasetBtn.setEnabled(true);
                        importBtn.setEnabled(false);
                    } else {
                        for (int k = 0; k < datasetItems.length; k++ )
                        {
                            RLong dId = datasetItems[k].getDataset().getId();
                            dbox.setEnabled(true);
                            addDatasetBtn.setEnabled(true);
                            importBtn.setEnabled(true);
                            dbox.addItem(datasetItems[k]);
                            if (dId != null && dId.getValue() == savedDataset)
                            {
                                dbox.setSelectedIndex(k);
                            }                        
                        }
                    }
                }
            }
        }

    }

    /**
     * Refresh project menu (when new project added)
     */
    private void refreshAndSetProject()
    {
		addProjectBtn.setFocusPainted(false); // remove focus state on button
        if (store != null)
        {
            //pbox.removeAllItems();
            projectItems = ProjectItem.createProjectItems(store.getProjects());            
            for (int k = 0; k < projectItems.length; k++ )
            {
                RLong pId = projectItems[k].getProject().getId();                
                if (pId != null && pId.getValue() == config.savedProject.get())
                {
                    pbox.insertItemAt(projectItems[k], k);
                    pbox.setSelectedIndex(k);
                }                        
            }
            datasetItems = DatasetItem.createEmptyDataset();
            buildProjectsAndDatasets();
            addDatasetBtn.setEnabled(true);
        }
    }

    /**
     * Refresh dataset when project changes
     * 
     * @param p - project parent of dataset
     */
    private void refreshAndSetDataset(Project p)
    {
		addDatasetBtn.setFocusPainted(false); // remove focus state on button
    	datasetItems = DatasetItem.createDatasetItems(store.getDatasets(p));
    	if (datasetItems == null) {
    		return; // user cancelled.. do nothing.
    	}
    	dbox.removeAllItems();
    	if (datasetItems.length == 0)
    	{
    		datasetItems = DatasetItem.createEmptyDataset();
    		dbox.addItem(datasetItems[0]);
    		dbox.setEnabled(false);
    		// Clear button focus
    		addDatasetBtn.setFocusable(false);
    		addDatasetBtn.setFocusable(true);
    		addDatasetBtn.setSelected(false);
    		importBtn.setEnabled(false);
    	} else {
    		for (int k = 0; k < datasetItems.length; k++ )
    		{
    			RLong dId = datasetItems[k].getDataset().getId();
    			dbox.setEnabled(true);
    			addDatasetBtn.setEnabled(true);
    			importBtn.setEnabled(true);
    			dbox.insertItemAt(datasetItems[k], k);
    			if (dId != null && dId.getValue() == config.savedDataset.get())
    			{
    				dbox.setSelectedIndex(k);
    			}                        
    		}
    	}
    }
    
    /**
     * Dialog explaining metadata limitations when changing the main dialog's naming settings
     * 
     * @param frame - parent component
     */
    public void sendNamingWarning(Component frame)
    {
        final JOptionPane optionPane = new JOptionPane(
                "\nNOTE: Some file formats do not include the file name in their metadata, " +
        		"\nand disabling this option may result in files being imported without a " +
        		"\nreference to their file name. For example, 'myfile.lsm [image001]' " +
        		"\nwould show up as 'image001' with this optioned turned off.", JOptionPane.WARNING_MESSAGE);
        final JDialog warningDialog = new JDialog(this, "Naming Warning!", true);
        warningDialog.setContentPane(optionPane);

        optionPane.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent e) {
                        String prop = e.getPropertyName();

                        if (warningDialog.isVisible() 
                                && (e.getSource() == optionPane)
                                && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                            warningDialog.dispose();
                        }
                    }
                });

        warningDialog.toFront();
        warningDialog.pack();
        warningDialog.setLocationRelativeTo(frame);
        warningDialog.setVisible(true);
    }

    /**
     * Enable/Disable the options under the custom naming section 
     * of the dialog
     * 
     * @param enable
     */
    private void enabledPathButtons(boolean enable)
    {
		fullPathButton.setEnabled(enable);
		partPathButton.setEnabled(enable); 
		numOfDirectoriesField.setEnabled(enable);
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event)
    {
       
        if (event.getSource() == useCustomNamingChkBox)
        {
        	if (!useCustomNamingChkBox.isSelected()) sendNamingWarning(this);
            enabledPathButtons(useCustomNamingChkBox.isSelected());
        } 
        else if (event.getSource() == addProjectBtn)
        {
            new AddProjectDialog(config, this, "Add a new Project", true, store);
            refreshAndSetProject();
        } 
        else if (event.getSource() == addDatasetBtn)
        {
            project = ((ProjectItem) pbox.getSelectedItem()).getProject();
            new AddDatasetDialog(config, this, "Add a new Dataset to: " + project.getName().getValue(), true, project, store);
            refreshAndSetDataset(project);
        } 
        else if (event.getSource() == cancelBtn)
        {
            cancelled = true;
            this.dispose();
        }
        else if (event.getSource() == importBtn)
        {
            cancelled = false;
            importBtn.requestFocus();
            
            config.setCustomImageNaming(useCustomNamingChkBox.isSelected());
            config.useCustomImageNaming.set(useCustomNamingChkBox.isSelected());
            config.useFullPath.set(fullPathButton.isSelected());
            config.archiveImage.set(archiveImage.isSelected());
            config.numOfDirectories.set(numOfDirectoriesField.getValue());
            config.setUserFullPath(fullPathButton.isSelected());
            config.setNumOfDirectories(numOfDirectoriesField.getValue());
            
            dataset = ((DatasetItem) dbox.getSelectedItem()).getDataset();
            project = ((ProjectItem) pbox.getSelectedItem()).getProject();
            if (pbox.getSelectedIndex() != 0)
            {
            	config.savedProject.set(
            			((ProjectItem) pbox.getSelectedItem()).getProject().getId().getValue());
            }
            config.savedDataset.set(dataset.getId().getValue());
            
            pixelSizeX = xPixelSize.getValue();
            pixelSizeY = yPixelSize.getValue();
            pixelSizeZ = zPixelSize.getValue();
            
            redChannel = rChannel.getValue();
            greenChannel = gChannel.getValue();
            blueChannel = bChannel.getValue();
            
            this.dispose();
        }
        else if (event.getSource() == pbox)
        {
            cancelled = false;
            Project p = ((ProjectItem) pbox.getSelectedItem()).getProject();
            getProjectDatasets(p);
        }
    }

    private void getProjectDatasets(Project p) {
        datasetItems = 
        	DatasetItem.createDatasetItems(store.getDatasets(p));
        addDatasetBtn.setEnabled(true);

        dbox.removeAllItems();
        if (datasetItems.length == 0)
        {
            datasetItems = DatasetItem.createEmptyDataset();
            dbox.addItem(datasetItems[0]);
            dbox.setEnabled(false);
            importBtn.setEnabled(false);
        } else {
            for (int i = 0; i < datasetItems.length; i++ )
            {
                dbox.setEnabled(true);
                importBtn.setEnabled(true);
                dbox.addItem(datasetItems[i]);
            }
        }
	}

	public void stateChanged(ChangeEvent e)
    {
        System.err.println("Test");
    }
}

//Helper classes used by the dialog comboboxes

/**
 * @author "Brian W. Loranger"
 */
class DatasetItem
{
    private Dataset dataset;

    public DatasetItem(Dataset dataset)
    {
        this.dataset = dataset;
    }

    public Dataset getDataset()
    {
        return dataset;
    }

    @Override
    public String toString()
    {
        if (dataset == null) return "";
        return dataset.getName().getValue();
    }

    public Long getId() {
        return dataset.getId().getValue();
    }

    public static DatasetItem[] createDatasetItems(List<Dataset> datasets)
    {
    	if (datasets == null)
    		return null;
        DatasetItem[] items = new DatasetItem[datasets.size()];
        for (int i = 0; i < datasets.size(); i++)
        {
            items[i] = new DatasetItem(datasets.get(i));
        }
        return items;
    }

    public static DatasetItem[] createEmptyDataset()
    {
        DatasetI d = new DatasetI();
        d.setName(rstring("--- Empty Set ---"));
        DatasetItem[] items = new DatasetItem[1];
        items[0] = new DatasetItem(d);
        return items;
    }
}

/**
 * @author "Brian W. Loranger"
 */
class ProjectItem
{
    private Project project;

    public ProjectItem(Project project)
    {
        this.project = project;
    }

    public Project getProject()
    {
        return project;
    }

    @Override
    public String toString()
    {
        return project.getName().getValue();
    }

    public Long getId()
    {
        return project.getId().getValue();
    }

    public static ProjectItem[] createProjectItems(List<Project> projects)
    {
        ProjectItem[] items = new ProjectItem[projects.size() + 1];
        ProjectI p = new ProjectI();
        p.setName(rstring("--- No Project ---"));
        items[0] = new ProjectItem(p);

        for (int i = 1; i < (projects.size() + 1); i++)
        {
            items[i] = new ProjectItem(projects.get(i - 1));
        }
        return items;
    }
}
