package ome.formats.importer.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.UIManager;

import layout.TableLayout;
import ome.formats.OMEROMetadataStoreClient;
import omero.model.Screen;


@SuppressWarnings("serial")
public class AddScreenDialog extends JDialog implements ActionListener
{
    boolean debug = false;

    Window                  owner;
    
    JPanel                  mainPanel;
    JPanel                  internalPanel;

    JButton                 OKBtn;
    JButton                 cancelBtn;

    JTextField              nameField;
    JTextArea               descriptionArea;

    String                  screenName;
    String                  screenDescription;
    
    Screen                  screen;
    GuiCommonElements       gui;
    OMEROMetadataStoreClient      store;
    
    AddScreenDialog(GuiCommonElements gui, Window owner, String title, Boolean modal, OMEROMetadataStoreClient store)
    {
        this.gui = gui;
        this.store = store;
        this.owner = owner;
        
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
                "Cancel adding a screen", "1, 1, f, c", debug);
        cancelBtn.addActionListener(this);

        OKBtn = gui.addButton(mainPanel, "OK", 'O',
                "Accept your new screen", "3, 1, f, c", debug);
        OKBtn.addActionListener(this);

        this.getRootPane().setDefaultButton(OKBtn);
        gui.enterPressesWhenFocused(OKBtn);
        
        double internalTable[][] = 
            {{160, TableLayout.FILL}, // columns
            {30, 30, TableLayout.FILL}}; // rows
        
        internalPanel = gui.addMainPanel(this, internalTable, 10,10,10,10, debug);

        String message = "Please enter your screen name and an optional " +
                "description below.";

        @SuppressWarnings("unused")
        JTextPane instructions = 
                gui.addTextPane(internalPanel, message, "0,0,1,0", debug);

        nameField = gui.addTextField(internalPanel, "Screen Name: ", "", 'E',
        "Input your screen name here.", "", TableLayout.PREFERRED, "0, 1, 1, 1", debug);
        
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
            screenName = nameField.getText();
            screenDescription = descriptionArea.getText();
            
            if (screenName.length() > 255)
            {
                JOptionPane.showMessageDialog(this, "The screen's name can not be longer than 255 characters.");                
            }
            else if (screenName.trim().length() > 0)
            {
                screen = store.addScreen(screenName, screenDescription);
                gui.config.savedScreen.set(screen.getId().getValue());
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "The screen's name can not be blank.");
            }
        }
        
        if (source == cancelBtn)
        {
            dispose();
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
        new AddScreenDialog(null, null, "Add a Screen", true, null);
    }
}
