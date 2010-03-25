/*
 * ome.formats.importer.gui.ImportDialog
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
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
import javax.swing.UIManager;
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

    public JCheckBox archiveImage, fileCheckBox;

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

    ImportDialog(ImportConfig config, JFrame owner, String title, boolean modal, OMEROMetadataStoreClient store)
    {
        this.store = store;

        if (store != null)
        {
            projectItems = ProjectItem.createProjectItems(store.getProjects());
            datasetItems = DatasetItem.createEmptyDataset();
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
                "Select dataset to use for this import.", 60, "0,0,f,c", debug);

        // Fixing broken mac buttons.
        String offsetButtons = ",c";
        //if (GuiCommonElements.offsetButtons == true) offsetButtons = ",t";

        addProjectBtn = GuiCommonElements.addIconButton(pdPanel, "", addIcon, 20, 60, null, null, "2,0,f" + offsetButtons, debug);
        addProjectBtn.addActionListener(this);
        
        dbox = GuiCommonElements.addComboBox(pdPanel, "Dataset: ", datasetItems, 'D',
                "Select dataset to use for this import.", 60, "0,1,f,c", debug);

        dbox.setEnabled(false);

        addDatasetBtn = GuiCommonElements.addIconButton(pdPanel, "", addIcon, 20, 60, null, null, "2,1,f" + offsetButtons, debug);
        addDatasetBtn.addActionListener(this);
        
        addDatasetBtn.setEnabled(false);
        
        importPanel.add(pdPanel, "0, 2, 4, 2");

        // File naming section

        double namedTable[][] =
        {{30, TableLayout.FILL}, // columns
                {24, TableLayout.PREFERRED, 
            TableLayout.PREFERRED, TableLayout.FILL}}; // rows      

        namedPanel = GuiCommonElements.addBorderedPanel(importPanel, namedTable, "File Naming", debug);

        fileCheckBox = GuiCommonElements.addCheckBox(namedPanel, "Override default file naming. Instead use:", "0,0,1,0", debug);
       	fileCheckBox.setSelected(!config.overrideImageName.get());
        

        String fullPathTooltip = "The full file+path name for " +
        "the file. For example: \"c:/myfolder/mysubfolder/myfile.dv\"";

        String partPathTooltip = "A partial path and file name for " +
        "the file. For example: \"mysubfolder/myfile.dv\"";

        fullPathButton = GuiCommonElements.addRadioButton(namedPanel, 
                "the full path+file name of your file", 'u', 
                fullPathTooltip, "1,1", debug);

        partPathButton = GuiCommonElements.addRadioButton(namedPanel, 
                "a partial path+file name with...", 'u', 
                partPathTooltip, "1,2", debug);

        numOfDirectoriesField = GuiCommonElements.addWholeNumberField(namedPanel, 
                "" , "0", "of the directories immediately before it.", 0, 
                "Add this number of directories to the file names",
                3, 40, "1,3,l,c", debug);
        
        numOfDirectoriesField.setText(Integer.toString(config.numOfDirectories.get()));

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

        if (config.useFullPath.get() == true )
            group.setSelected(fullPathButton.getModel(), true);
        else
            group.setSelected(partPathButton.getModel(), true);
        
        
        

        importPanel.add(namedPanel, "0, 3, 4, 2");

        // Buttons at the bottom of the form

        cancelBtn = GuiCommonElements.addButton(importPanel, "Cancel", 'L',
                "Cancel", "1, 5, f, c", debug);
        cancelBtn.addActionListener(this);

        importBtn = GuiCommonElements.addButton(importPanel, "Add to Queue", 'Q',
                "Import", "3, 5, f, c", debug);
        importBtn.addActionListener(this);

        this.getRootPane().setDefaultButton(importBtn);
        GuiCommonElements.enterPressesWhenFocused(importBtn);

        
            archiveImage = GuiCommonElements.addCheckBox(importPanel, 
                    "Archive the original imported file(s) to the server.", "0,4,4,4", debug);
            archiveImage.setSelected(false);
            if (ARCHIVE_ENABLED)
            {
                archiveImage.setVisible(true);
            } else {
                archiveImage.setVisible(false);                
            }

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
                "X: " , null, "", 0, "", 8, 80, "1,1,l,c", debug);

        yPixelSize = GuiCommonElements.addDecimalNumberField(pixelPanel, 
                "Y: " , null, "", 0, "", 8, 80, "3,1,l,c", debug);

        zPixelSize = GuiCommonElements.addDecimalNumberField(pixelPanel, 
                "Z: " , null, "", 0, "", 8, 80, "5,1,l,c", debug);
        
        metadataPanel.add(pixelPanel, "0, 0");

        double channelTable[][] =
        {{10,TableLayout.FILL, 10,TableLayout.FILL, 10, TableLayout.FILL,10}, // columns
         {68, TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.FILL}}; // rows      

        channelPanel = GuiCommonElements.addBorderedPanel(metadataPanel, channelTable, "Channel Defaults", debug);
        
        rChannel = GuiCommonElements.addWholeNumberField(channelPanel, 
                "R: " , "0", "", 0, "", 8, 80, "1,1,l,c", debug);

        gChannel = GuiCommonElements.addWholeNumberField(channelPanel, 
                "G: " , "1", "", 0, "", 8, 80, "3,1,l,c", debug);

        bChannel = GuiCommonElements.addWholeNumberField(channelPanel, 
                "B: " , "2", "", 0, "", 8, 80, "5,1,l,c", debug);
        
        message = "These RGB channel wavelengths (typically measured in nanometers)" +
        		" will be used if no channel values are included in the image file metadata:";
        GuiCommonElements.addTextPane(channelPanel, message, "1, 0, 6, 0", debug);
        
        //metadataPanel.add(channelPanel, "0, 2");

    
        /////////////////////// START TABBED PANE ////////////////////////
        
        this.add(tabbedPane);
        tabbedPane.addTab("Import Settings", null, importPanel, "Import Settings");
        tabbedPane.addTab("Metadata Defaults", null, metadataPanel, "Metadata Defaults");
        //this.add(mainPanel);

        importBtn.setEnabled(false);
        //this.getRootPane().setDefaultButton(importBtn);

        fullPathButton.addActionListener(this);
        partPathButton.addActionListener(this);
        numOfDirectoriesField.addActionListener(this);
        cancelBtn.addActionListener(this);
        importBtn.addActionListener(this);
        pbox.addActionListener(this);
        fileCheckBox.addActionListener(this);
        buildProjectsAndDatasets();
        setVisible(true);
    }

    private void buildProjectsAndDatasets()
    {
        long savedProject = config.savedProject.get();
        long savedDataset = config.savedDataset.get();
        
        if (savedProject != 0 && projectItems != null) {
            for (int i = 0; i < projectItems.length; i++)
            {
                RLong pId = projectItems[i].getProject().getId();

                if (pId != null && pId.getValue() == savedProject)
                {
                    pbox.setSelectedIndex(i);

                    Project p = ((ProjectItem) pbox.getSelectedItem()).getProject();
                    datasetItems = 
                        DatasetItem.createDatasetItems(store.getDatasets(p));
                    dbox.removeAllItems();
                    if (datasetItems.length == 0 || pbox.getSelectedIndex() == 0)
                    {
                        datasetItems = 
                            DatasetItem.createEmptyDataset();
                        dbox.addItem(datasetItems[0]);
                        dbox.setEnabled(false);
                        addDatasetBtn.setEnabled(false);
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

    private void refreshAndSetProject()
    {
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

    private void refreshAndSetDataset(Project p)
    {
        datasetItems = 
            DatasetItem.createDatasetItems(store.getDatasets(p));
        dbox.removeAllItems();
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
    
    public void sendingNamingWarning(Component frame)
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

    
    public void actionPerformed(ActionEvent event)
    {
       
        if (event.getSource() == fileCheckBox && !fileCheckBox.isSelected())
        {
            sendingNamingWarning(this);   
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
        else if (event.getSource() == fullPathButton)
        {
            config.useFullPath.set(true);

        }
        else if (event.getSource() == partPathButton)
        {
            config.useFullPath.set(false);
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
            config.numOfDirectories.set(numOfDirectoriesField.getValue());
            dataset = ((DatasetItem) dbox.getSelectedItem()).getDataset();
            project = ((ProjectItem) pbox.getSelectedItem()).getProject();
            config.savedProject.set(
                    ((ProjectItem) pbox.getSelectedItem()).getProject().getId().getValue());
            config.savedDataset.set(dataset.getId().getValue());
            config.overrideImageName.set(!fileCheckBox.isSelected());
            config.savedFileNaming.set(fullPathButton.isSelected());
            
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

            if (pbox.getSelectedIndex() == 0)
            {
                dbox.setEnabled(false);
                addDatasetBtn.setEnabled(false);
            } else
            {
                Project p = ((ProjectItem) pbox.getSelectedItem()).getProject();
                datasetItems = 
                    DatasetItem.createDatasetItems(store.getDatasets(p));
                addDatasetBtn.setEnabled(true);
            }

            dbox.removeAllItems();
            if (datasetItems.length == 0 || pbox.getSelectedIndex() == 0)
            {
                datasetItems = 
                    DatasetItem.createEmptyDataset();
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
    }

    public static void main (String[] args) {

        String laf = UIManager.getSystemLookAndFeelClassName() ;
        //laf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        //laf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        //laf = "javax.swing.plaf.metal.MetalLookAndFeel";
        //laf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";

        try {
            UIManager.setLookAndFeel(laf);
        } catch (Exception e) 
        { System.err.println(laf + " not supported."); }

        ImportDialog dialog = new ImportDialog(null, null, "Import Dialog", true, null);
        if (dialog != null) System.exit(0);
    }

    public void stateChanged(ChangeEvent e)
    {
        System.err.println("Test");
    }
}

//Helper classes used by the dialog comboboxes
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
        p.setName(rstring("--- Select Project ---"));
        items[0] = new ProjectItem(p);

        for (int i = 1; i < (projects.size() + 1); i++)
        {
            items[i] = new ProjectItem(projects.get(i - 1));
        }
        return items;
    }
}