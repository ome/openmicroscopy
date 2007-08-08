/*
 * ome.formats.testclient.ImportDialog
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

package ome.formats.importer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.formats.OMEROMetadataStore;
import ome.formats.importer.util.GuiCommonElements;
import ome.formats.importer.util.GuiCommonElements.WholeNumberField;
import ome.model.containers.Dataset;
import ome.model.containers.Project;

import layout.TableLayout;

/**
 * @author "Brian W. Loranger"
 */
@SuppressWarnings("serial")
public class ImportDialog extends JDialog implements ActionListener
{
    boolean debug = false;

    private GuiCommonElements       gui;

    private Integer                 dialogHeight = 350;
    private Integer                 dialogWidth = 400;

    private JPanel  mainPanel;
    private JPanel  pdPanel;
    private JPanel  namedPanel;
    private JPanel  numOfDirPanel;

    private JTextPane instructions;

    // Add graphic for add button
    String addIcon = "gfx/add_text.png";

    // Size of the add/remove/refresh buttons (which are square).
    private int buttonSize = 34;

    private JRadioButton fullPathButton;
    private JRadioButton partPathButton;

    private WholeNumberField numOfDirectoriesField;

    public JCheckBox archiveImage;

    private JButton             addProjectBtn;
    private JButton             addDatasetBtn;
    private JButton             cancelBtn;
    private JButton             importBtn;

    private JComboBox pbox;
    private JComboBox dbox;

    public  Dataset dataset;
    public  Project project;
    
    public  Project newProject;
    
    public  DatasetItem[] datasetItems = null;
    public  ProjectItem[] projectItems = null;

    public boolean    cancelled = true;

    /** Logger for this class. */
    @SuppressWarnings("unused")
    private static Log          log     = LogFactory.getLog(ImportDialog.class);

    public OMEROMetadataStore store;

    private Preferences    userPrefs = 
        Preferences.userNodeForPackage(ImportDialog.class);

    private Long savedProject = userPrefs.getLong("savedProject", 0);
    private Long savedDataset = userPrefs.getLong("savedDataset", 0);
    public Boolean useFullPath = userPrefs.getBoolean("savedFileNaming", true);
    public Integer numOfDirectories = userPrefs.getInt("savedNumOfDirs", 0);


    ImportDialog(JFrame owner, String title, boolean modal, OMEROMetadataStore store)
    {
        super(owner);
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

        gui = new GuiCommonElements();

        // Set up the main panel for tPane, quit, and send buttons
        double mainTable[][] =
            {{TableLayout.FILL, 100, 5, 100, TableLayout.FILL}, // columns
            {TableLayout.PREFERRED, 10, TableLayout.PREFERRED, 
                TableLayout.FILL, 40, 30}}; // rows

        mainPanel = gui.addMainPanel(this, mainTable, 10,20,10,20, debug);

        String message = "Import these images into which dataset?";
        instructions = gui.addTextPane(mainPanel, message, "0, 0, 4, 0", debug);

        // Set up the project/dataset table
        double pdTable[][] =
        {{TableLayout.FILL, 5, 40}, // columns
                {35, 35}}; // rows

        // Panel containing the project / dataset layout

        pdPanel = gui.addMainPanel(mainPanel, pdTable, 0, 0, 0, 0, debug);

        pbox = gui.addComboBox(pdPanel, "Project: ", projectItems, 'P', 
                "Select dataset to use for this import.", 60, "0,0,f,c", debug);

        // Fixing broken mac buttons.
        String offsetButtons = ",c";
        //if (gui.offsetButtons == true) offsetButtons = ",t";

        addProjectBtn = gui.addIconButton(pdPanel, "", addIcon, 20, 60, null, null, "2,0,f" + offsetButtons, debug);
        addProjectBtn.addActionListener(this);
        
        dbox = gui.addComboBox(pdPanel, "Dataset: ", datasetItems, 'D',
                "Select dataset to use for this import.", 60, "0,1,f,c", debug);

        dbox.setEnabled(false);

        addDatasetBtn = gui.addIconButton(pdPanel, "", addIcon, 20, 60, null, null, "2,1,f" + offsetButtons, debug);
        addDatasetBtn.addActionListener(this);
        
        addDatasetBtn.setEnabled(false);
        
        mainPanel.add(pdPanel, "0, 2, 4, 2");

        // File naming section

        double namedTable[][] =
        {{TableLayout.FILL}, // columns
                {24, TableLayout.PREFERRED, 
            TableLayout.PREFERRED, TableLayout.FILL}}; // rows      

        namedPanel = gui.addBorderedPanel(mainPanel, namedTable, "File Naming", debug);

        message = "The imported file name on the server should include:";
        instructions = gui.addTextPane(namedPanel, message, "0, 0", debug);

        String fullPathTooltip = "This will use the full path and file name for " +
        "the file. For example: \"c:/myfolder/mysubfolder/myfile.dv\"";

        String partPathTooltip = "This will use a partial path and file name for " +
        "the file. For example: \"mysubfolder/myfile.dv\"";

        fullPathButton = gui.addRadioButton(namedPanel, 
                "the full path and file name from you local system", 'u', 
                fullPathTooltip, "0,1", debug);

        partPathButton = gui.addRadioButton(namedPanel, 
                "a partial path which includes the file name and...", 'u', 
                partPathTooltip, "0,2", debug);

        numOfDirectoriesField = gui.addWholeNumberField(namedPanel, 
                "      " , "0", "of the directories immediately before it.", 0, 
                "Add this number of directories to the file names",
                3, 40, "0,3,l,c", debug);
        
        numOfDirectoriesField.setText(numOfDirectories.toString());

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

        if (useFullPath == true )
            group.setSelected(fullPathButton.getModel(), true);
        else
            group.setSelected(partPathButton.getModel(), true);

        mainPanel.add(namedPanel, "0, 3, 4, 2");

        // Buttons at the bottom of the form

        cancelBtn = gui.addButton(mainPanel, "Cancel", 'L',
                "Cancel", "1, 5, f, c", debug);
        cancelBtn.addActionListener(this);

        importBtn = gui.addButton(mainPanel, "Add to Q", 'Q',
                "Import", "3, 5, f, c", debug);
        importBtn.addActionListener(this);

        this.getRootPane().setDefaultButton(importBtn);
        gui.enterPressesWhenFocused(importBtn);

        archiveImage = gui.addCheckBox(mainPanel, 
                "Archive the original imported file(s) to the server.", "0,4,4,t", debug);
        archiveImage.setSelected(false);
        archiveImage.setVisible(true);
        
        this.add(mainPanel);

        importBtn.setEnabled(false);
        this.getRootPane().setDefaultButton(importBtn);

        fullPathButton.addActionListener(this);
        partPathButton.addActionListener(this);
        numOfDirectoriesField.addActionListener(this);
        cancelBtn.addActionListener(this);
        importBtn.addActionListener(this);
        pbox.addActionListener(this);

        buildProjectsAndDatasets();
        setVisible(true);
    }

    private void buildProjectsAndDatasets()
    {
        if (savedProject != 0 && projectItems != null) {
            for (int i = 0; i < projectItems.length; i++)
            {


                Long pId = projectItems[i].getId();

                if (pId != null && pId.equals(savedProject))
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
                            Long dId = datasetItems[k].getId();
                            dbox.setEnabled(true);
                            addDatasetBtn.setEnabled(true);
                            importBtn.setEnabled(true);
                            dbox.addItem(datasetItems[k]);
                            if (dId != null && dId.equals(savedDataset))
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
            savedProject = userPrefs.getLong("savedProject", 0);
            for (int k = 0; k < projectItems.length; k++ )
            {
                Long pId = projectItems[k].getId();                
                if (pId != null && pId.equals(savedProject))
                {
                    pbox.addItem(projectItems[k]);
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
        savedDataset = userPrefs.getLong("savedDataset", 0);
        datasetItems = 
            DatasetItem.createDatasetItems(store.getDatasets(p));
        dbox.removeAllItems();
        for (int k = 0; k < datasetItems.length; k++ )
        {
            Long dId = datasetItems[k].getId();
            dbox.setEnabled(true);
            addDatasetBtn.setEnabled(true);
            importBtn.setEnabled(true);
            dbox.addItem(datasetItems[k]);
            if (dId != null && dId.equals(savedDataset))
            {
                dbox.setSelectedIndex(k);
            }                        
        }
    }
    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == addProjectBtn)
        {
            new AddProjectDialog(this, "Add a new Project", true, store);
            refreshAndSetProject();
        }

        if (e.getSource() == addDatasetBtn)
        {
            project = ((ProjectItem) pbox.getSelectedItem()).getProject();
            new AddDatasetDialog(this, "Add a new Dataset to: " + project.getName(), true, project, store);
            refreshAndSetDataset(project);
        }
        
        if (e.getSource() == fullPathButton)
        {
            useFullPath = true;

        }
        if (e.getSource() == partPathButton)
        {
            useFullPath = false;
        }
        if (e.getSource() == cancelBtn)
        {
            cancelled = true;
            this.dispose();
        }
        if (e.getSource() == importBtn)
        {
            cancelled = false;
            importBtn.requestFocus();
            numOfDirectories = numOfDirectoriesField.getValue();
            dataset = ((DatasetItem) dbox.getSelectedItem()).getDataset();
            project = ((ProjectItem) pbox.getSelectedItem()).getProject();
            userPrefs.putLong("savedProject", 
                    ((ProjectItem) pbox.getSelectedItem()).getId());
            userPrefs.putLong("savedDataset", dataset.getId());
            if (fullPathButton.isSelected() == true)
                userPrefs.putBoolean("savedFileNaming", true);
            else 
                userPrefs.putBoolean("savedFileNaming", false);
            userPrefs.putInt("savedNumOfDirs", numOfDirectoriesField.getValue());

            this.dispose();
        }
        if (e.getSource() == pbox)
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

        ImportDialog dialog = new ImportDialog(null, "Import Dialog", true, null);
        if (dialog != null) System.exit(0);
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
        return dataset.getName();
    }

    public Long getId() {
        return dataset.getId();
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
        Dataset d = new Dataset();
        d.setName("--- Empty Set ---");
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
        return project.getName();
    }

    public Long getId()
    {
        return project.getId();
    }

    public static ProjectItem[] createProjectItems(List<Project> projects)
    {
        ProjectItem[] items = new ProjectItem[projects.size() + 1];
        Project p = new Project();
        p.setName("--- Select Project ---");
        items[0] = new ProjectItem(p);

        for (int i = 1; i < (projects.size() + 1); i++)
        {
            items[i] = new ProjectItem(projects.get(i - 1));
        }
        return items;
    }
}