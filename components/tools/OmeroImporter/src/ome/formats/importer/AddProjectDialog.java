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
import ome.model.containers.Project;


public class AddProjectDialog extends JDialog implements ActionListener
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

    String                  projectName;
    String                  projectDescription;
    
    Project                 project;
    
    OMEROMetadataStore      store;
    
    AddProjectDialog(Window owner, String title, Boolean modal, OMEROMetadataStore store)
    {
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
                "Cancel adding a project", "1, 1, f, c", debug);
        cancelBtn.addActionListener(this);

        OKBtn = gui.addButton(mainPanel, "OK", 'O',
                "Accept your new project", "3, 1, f, c", debug);
        OKBtn.addActionListener(this);

        this.getRootPane().setDefaultButton(OKBtn);
        gui.enterPressesWhenFocused(OKBtn);
        
        double internalTable[][] = 
            {{160, TableLayout.FILL}, // columns
            {30, 30, TableLayout.FILL}}; // rows
        
        internalPanel = gui.addMainPanel(this, internalTable, 10,10,10,10, debug);

        String message = "Please enter your project name and an optional " +
                "description below.";

        @SuppressWarnings("unused")
        JTextPane instructions = 
                gui.addTextPane(internalPanel, message, "0,0,1,0", debug);

        nameField = gui.addTextField(internalPanel, "Project Name: ", "", 'E',
        "Input your project name here.", "", TableLayout.PREFERRED, "0, 1, 1, 1", debug);
        
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
            projectName = nameField.getText();
            projectDescription = descriptionArea.getText();
            
            if (projectName.trim().length() > 0)
            {
                project = addProject(projectName, projectDescription);
                userPrefs.putLong("savedProject", project.getId());
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

    private Project addProject(String name, String description)
    {
        project = new Project();
        if (name.length() != 0)
            project.setName(name);
        if (description.length() != 0)
            project.setDescription(description);
        
        Project storedProject = null;
        
        if (store != null)
        {
            IUpdate iUpdate = store.getIUpdate();
            storedProject = iUpdate.saveAndReturnObject(project);
            return storedProject;
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
        new AddProjectDialog(null, "Add a Project", true, null);
    }
}
