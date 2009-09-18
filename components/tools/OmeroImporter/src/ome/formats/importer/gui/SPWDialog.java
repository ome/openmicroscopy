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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import layout.TableLayout;
import ome.formats.OMEROMetadataStoreClient;
import omero.RLong;
import omero.model.Screen;
import omero.model.ScreenI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author "Brian W. Loranger"
 */
@SuppressWarnings("serial")
public class SPWDialog extends JDialog implements ActionListener
{
    boolean debug = false;

    private GuiCommonElements       gui;

    private Integer                 dialogHeight = 200;
    private Integer                 dialogWidth = 400;

    private JTabbedPane tabbedPane;
    
    private JPanel  importPanel;
    private JPanel  sPanel;

    // Add graphic for add button
    String addIcon = "gfx/add_text.png";

    public JCheckBox archiveImage;

    private JButton             addScreenBtn;
    private JButton             cancelBtn;
    private JButton             importBtn;

    private JComboBox sbox;

    public  Screen screen;
    public  ScreenItem[] screenItems = null;

    public boolean    cancelled = true;
    
    private boolean ARCHIVE_ENABLED = true;

    /** Logger for this class. */
    @SuppressWarnings("unused")
    private static Log          log     = LogFactory.getLog(SPWDialog.class);

    public OMEROMetadataStoreClient store;

    SPWDialog(GuiCommonElements gui, JFrame owner, String title, boolean modal, OMEROMetadataStoreClient store)
    {
        super(owner);
        this.store = store;

        if (store != null)
        {
            screenItems = ScreenItem.createScreenItem(store.getScreens());
        }

        setLocation(200, 200);
        setTitle(title);
        setModal(modal);
        setResizable(false);
        setSize(new Dimension(dialogWidth, dialogHeight));
        setLocationRelativeTo(owner);

        tabbedPane = new JTabbedPane();
        tabbedPane.setOpaque(false); // content panes must be opaque

        this.gui = gui;

        
        /////////////////////// START IMPORT PANEL ////////////////////////
        
        // Set up the import panel for tPane, quit, and send buttons
        
        double mainTable[][] =
            {{TableLayout.FILL, 120, 5, 160, TableLayout.FILL}, // columns
            {TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 
                TableLayout.FILL, 40, 30}}; // rows

        importPanel = gui.addMainPanel(tabbedPane, mainTable, 5,10,0,10, debug);

        String message = "Import this plate into which screen?";
        gui.addTextPane(importPanel, message, "0, 0, 4, 0", debug);

        // Set up the project/dataset table
        double pdTable[][] =
        {{TableLayout.FILL, 5, 40}, // columns
                {35}}; // rows

        // Panel containing the project / dataset layout

        sPanel = gui.addMainPanel(importPanel, pdTable, 0, 0, 0, 0, debug);

        sbox = gui.addComboBox(sPanel, "Screen: ", screenItems, 'P', 
                "Select dataset to use for this import.", 50, "0,0,f,c", debug);

        // Fixing broken mac buttons.
        String offsetButtons = ",c";
        //if (gui.offsetButtons == true) offsetButtons = ",t";

        int addBtnSize = 60;
        
        if (gui.getIsMac() == true)
            addBtnSize = 20;
            
        
        addScreenBtn = gui.addIconButton(sPanel, "", addIcon, 20, addBtnSize, null, null, "2,0,f" + offsetButtons, debug);
        addScreenBtn.addActionListener(this);
        
        importPanel.add(sPanel, "0, 2, 4, 2");

        // Buttons at the bottom of the form

        cancelBtn = gui.addButton(importPanel, "Cancel", 'L',
                "Cancel", "1, 5, f, c", debug);
        cancelBtn.addActionListener(this);

        importBtn = gui.addButton(importPanel, "Add to Queue", 'Q',
                "Import", "3, 5, f, c", debug);
        importBtn.addActionListener(this);

        this.getRootPane().setDefaultButton(importBtn);
        gui.enterPressesWhenFocused(importBtn);

        
            archiveImage = gui.addCheckBox(importPanel, 
                    "Archive the original imported file(s) to the server.", "0,4,4,t", debug);
            archiveImage.setSelected(false);
            if (ARCHIVE_ENABLED)
            {
                archiveImage.setVisible(true);
            } else {
                archiveImage.setVisible(false);                
            }

        /////////////////////// START TABBED PANE ////////////////////////
        
        this.add(tabbedPane);
        tabbedPane.addTab("Import Settings", null, importPanel, "Import Settings");
        //this.add(mainPanel);

        importBtn.setEnabled(false);
        this.getRootPane().setDefaultButton(importBtn);

        cancelBtn.addActionListener(this);
        importBtn.addActionListener(this);
        sbox.addActionListener(this);

        buildScreens();
        setVisible(true);
    }

    private void buildScreens()
    {
        if (gui.config.savedScreen.get() != 0 && screenItems != null) {
            for (int i = 0; i < screenItems.length; i++)
            {
                RLong pId = screenItems[i].getScreen().getId();

                if (pId != null && pId.getValue() == gui.config.savedScreen.get())
                {
                    sbox.setSelectedIndex(i);
                }
            }
        }

    }

    private void refreshAndSetProject()
    {
        if (store != null)
        {
            //sbox.removeAllItems();
            screenItems = ScreenItem.createScreenItem(store.getScreens());            
            for (int k = 0; k < screenItems.length; k++ )
            {
                RLong pId = screenItems[k].getScreen().getId();                
                if (pId != null && pId.getValue() == gui.config.savedScreen.get())
                {
                    sbox.insertItemAt(screenItems[k], k);
                    sbox.setSelectedIndex(k);
                }                        
            }
            buildScreens();
        }
    }

    public void actionPerformed(ActionEvent e)
    {
        if (e.getSource() == addScreenBtn)
        {
            new AddScreenDialog(gui, this, "Add a new Screen", true, store);
            refreshAndSetProject();
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
            screen = ((ScreenItem) sbox.getSelectedItem()).getScreen();
            gui.config.savedScreen.set(
                    ((ScreenItem) sbox.getSelectedItem()).getScreen().getId().getValue());            
            this.dispose();
        }
        if (e.getSource() == sbox)
        {
            cancelled = false;

            if (sbox.getSelectedIndex() == 0)
            {
                importBtn.setEnabled(false);
            } else
            {
                importBtn.setEnabled(true);
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

        SPWDialog dialog = new SPWDialog(null, null, "Plate Import Dialog", true, null);
        if (dialog != null) System.exit(0);
    }
}

//Helper classes used by the dialog comboboxes

class ScreenItem
{
    private Screen screen;

    public ScreenItem(Screen screen)
    {
        this.screen = screen;
    }

    public Screen getScreen()
    {
        return screen;
    }

    @Override
    public String toString()
    {
        return screen.getName().getValue();
    }

    public Long getId()
    {
        return screen.getId().getValue();
    }

    public static ScreenItem[] createScreenItem(List<Screen> screens)
    {
        ScreenItem[] items = new ScreenItem[screens.size() + 1];
        ScreenI s = new ScreenI();
        s.setName(rstring("--- Select Screen ---"));
        items[0] = new ScreenItem(s);

        for (int i = 1; i < (screens.size() + 1); i++)
        {
            items[i] = new ScreenItem(screens.get(i - 1));
        }
        return items;
    }
}