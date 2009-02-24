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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.util.GuiCommonElements;
import ome.formats.importer.util.IniFileLoader;

import layout.TableLayout;

/**
 * @author "Brian W. Loranger"
 */
@SuppressWarnings("serial")
public class OptionsDialog extends JDialog implements ActionListener
{
    boolean debug = false;

    private GuiCommonElements       gui;

    private Integer                 dialogHeight = 310;
    private Integer                 dialogWidth = 374;

    private JTabbedPane tabbedPane;
    
    private JPanel  fileChooserPanel;
    private JPanel  singlePanePanel;
    private JPanel  triplePanePanel;

    private JRadioButton singlePaneBtn;
    private JRadioButton triplePaneBtn;
    
    public static final String SINGLE_PANE_IMAGE = "gfx/single_pane_fileChooser.png";
    public static final String TRIPLE_PANE_IMAGE = "gfx/triple_pane_fileChooser.png";

    public JCheckBox archiveImage;

    private JButton             cancelBtn;
    private JButton             okBtn;

    public boolean    cancelled = true;

    /** Logger for this class. */
    @SuppressWarnings("unused")
    private static Log          log     = LogFactory.getLog(OptionsDialog.class);

    public OMEROMetadataStoreClient store;
    
    private IniFileLoader ini;

    Component owner;
    
    OptionsDialog(JFrame owner, String title, boolean modal)
    {
        super(owner);
        
        this.owner = owner;

        // Load up the main ini file
        ini = IniFileLoader.getIniFileLoader();
        
        setLocation(200, 200);
        setTitle(title);
        setModal(modal);
        setResizable(false);
        setSize(new Dimension(dialogWidth, dialogHeight));
        setLocationRelativeTo(owner);

        tabbedPane = new JTabbedPane();
        tabbedPane.setOpaque(false); // content panes must be opaque

        gui = new GuiCommonElements();

        
        /////////////////////// START IMPORT PANEL ////////////////////////
        
        // Set up the import panel for tPane, quit, and send buttons
        
        double mainTable[][] =
            {{TableLayout.FILL, 120, 5, 120, TableLayout.FILL}, // columns
            {TableLayout.PREFERRED, 10, TableLayout.FILL, 10, 30}}; // rows

        fileChooserPanel = gui.addMainPanel(tabbedPane, mainTable, 0,10,0,10, debug);

        String message = "Switch between single pane view and triple pane view. " +
        		"You will need to reboot the importer before your changes will take effect.";
        gui.addTextPane(fileChooserPanel, message, "0, 0, 4, 0", debug);
        
        // Set up single pane table
        double singlePaneTable[][] =
        {{24, 5, TableLayout.FILL}, // columns
                {TableLayout.FILL}}; // rows

        // Panel containing the single pane layout

        singlePanePanel = gui.addMainPanel(fileChooserPanel, singlePaneTable, 0, 0, 0, 0, debug);
        
        singlePaneBtn = gui.addRadioButton(singlePanePanel, 
                null, 'u', 
                null, "0,0", debug);
        
        gui.addImagePanel(singlePanePanel, SINGLE_PANE_IMAGE, "2,0", debug);
        
        fileChooserPanel.add(singlePanePanel, "0, 2, 1, 2");

        // Set up triple pane table
        double triplePaneTable[][] =
        {{24, 5, TableLayout.FILL}, // columns
                {TableLayout.FILL}}; // rows
        
        // Panel containing the triple pane layout

        triplePanePanel = gui.addMainPanel(fileChooserPanel, triplePaneTable, 0, 0, 0, 0, debug);

        triplePaneBtn = gui.addRadioButton(triplePanePanel, 
                null, 'u', 
                null, "0,0", debug);
        
        
        gui.addImagePanel(triplePanePanel, TRIPLE_PANE_IMAGE, "2,0", debug);
        
        fileChooserPanel.add(triplePanePanel, "3, 2, 4, 2");
        
        ButtonGroup group = new ButtonGroup();
        group.add(singlePaneBtn);
        group.add(triplePaneBtn);
        
        System.err.println(ini.getUseQuaqua());
        if (ini.getUseQuaqua() == true)
        {
            triplePaneBtn.setSelected(true);
            singlePaneBtn.setSelected(false);
        } 
        else
        {
            triplePaneBtn.setSelected(false);
            singlePaneBtn.setSelected(true);
        }

        // Buttons at the bottom of the form

        cancelBtn = gui.addButton(fileChooserPanel, "Cancel", 'L',
                "Cancel", "1, 4, f, c", debug);
        cancelBtn.addActionListener(this);

        okBtn = gui.addButton(fileChooserPanel, "OK", 'Q',
                "Import", "3, 4, f, c", debug);
        okBtn.addActionListener(this);

        this.getRootPane().setDefaultButton(okBtn);
        gui.enterPressesWhenFocused(okBtn);
    
        /////////////////////// START TABBED PANE ////////////////////////
        
        this.add(tabbedPane);
        tabbedPane.addTab("FileChooser Settings", null, fileChooserPanel, "FileChooser Settings");
        //this.add(mainPanel);

        this.getRootPane().setDefaultButton(okBtn);
        cancelBtn.addActionListener(this);
        okBtn.addActionListener(this);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == cancelBtn)
        {
            cancelled = true;
            this.dispose();
        }
        if (e.getSource() == okBtn && this.isDisplayable())
        {
            if (singlePaneBtn.isSelected())
                ini.setUseQuaqua(false);
            else
                ini.setUseQuaqua(true);
            this.dispose();
            gui.restartNotice(owner, null);
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

        OptionsDialog dialog = new OptionsDialog(null, "Options Dialog", true);
        if (dialog != null) System.exit(0);
    }
}