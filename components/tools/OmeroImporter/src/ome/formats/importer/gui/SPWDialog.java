/*
 *  ome.formats.importer.gui.StatusBar
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

package ome.formats.importer.gui;

import static omero.rtypes.rstring;
import info.clearthought.layout.TableLayout;

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

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportConfig;
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

    private ImportConfig	       	config;

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

    /**
     * Create and display a Screen/Plate/Well dialog for import selection
     * 
     * @param config - ImportConfig for saving/retrieving defaults
     * @param owner - parent dialog
     * @param title - dialog tible
     * @param modal - modal yes/no
     * @param store - OMEROMetadataStore to retrieve SPW data from
     */
    SPWDialog(ImportConfig config, JFrame owner, String title, boolean modal, OMEROMetadataStoreClient store)
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

        this.config = config;

        
        /////////////////////// START IMPORT PANEL ////////////////////////
        
        // Set up the import panel for tPane, quit, and send buttons
        
        double mainTable[][] =
            {{TableLayout.FILL, 120, 5, 160, TableLayout.FILL}, // columns
            {TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 
                TableLayout.FILL, 40, 30}}; // rows

        importPanel = GuiCommonElements.addMainPanel(tabbedPane, mainTable, 5,10,0,10, debug);

        String message = "Import this plate into which screen?";
        GuiCommonElements.addTextPane(importPanel, message, "0, 0, 4, 0", debug);

        // Set up the project/dataset table
        double pdTable[][] =
        {{TableLayout.FILL, 5, 40}, // columns
                {35}}; // rows

        // Panel containing the project / dataset layout

        sPanel = GuiCommonElements.addMainPanel(importPanel, pdTable, 0, 0, 0, 0, debug);

        sbox = GuiCommonElements.addComboBox(sPanel, "Screen: ", screenItems, 'P', 
                "Select dataset to use for this import.", 50, "0,0,F,C", debug);

        // Fixing broken mac buttons.
        String offsetButtons = ",C";
        //if (GuiCommonElements.offsetButtons == true) offsetButtons = ",t";

        int addBtnSize = 60;
        
        if (GuiCommonElements.getIsMac() == true)
            addBtnSize = 20;
            
        
        addScreenBtn = GuiCommonElements.addIconButton(sPanel, "", addIcon, 20, addBtnSize, null, null, "2,0,f" + offsetButtons, debug);
        addScreenBtn.addActionListener(this);
        
        importPanel.add(sPanel, "0, 2, 4, 2");

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

    /**
     * Build the screen list for drop down dialog
     */
    private void buildScreens()
    {
        if (config.savedScreen.get() != 0 && screenItems != null) {
            for (int i = 0; i < screenItems.length; i++)
            {
                RLong pId = screenItems[i].getScreen().getId();

                if (pId != null && pId.getValue() == config.savedScreen.get())
                {
                    sbox.setSelectedIndex(i);
                }
            }
        }

    }

    /**
     * Refresh drop down and set the screen
     */
    private void refreshAndSetScreen()
    {
        if (store != null)
        {
            //sbox.removeAllItems();
            screenItems = ScreenItem.createScreenItem(store.getScreens());            
            for (int k = 0; k < screenItems.length; k++ )
            {
                RLong pId = screenItems[k].getScreen().getId();                
                if (pId != null && pId.getValue() == config.savedScreen.get())
                {
                    sbox.insertItemAt(screenItems[k], k);
                    sbox.setSelectedIndex(k);
                }                        
            }
            buildScreens();
        }
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event)
    {
        if (event.getSource() == addScreenBtn)
        {
            new AddScreenDialog(config, this, "Add a new Screen", true, store);
            refreshAndSetScreen();
        }

        if (event.getSource() == cancelBtn)
        {
            cancelled = true;
            this.dispose();
        }
        if (event.getSource() == importBtn)
        {
            cancelled = false;
            importBtn.requestFocus();
            screen = ((ScreenItem) sbox.getSelectedItem()).getScreen();
            config.savedScreen.set(
                    ((ScreenItem) sbox.getSelectedItem()).getScreen().getId().getValue());            
            this.dispose();
        }
        if (event.getSource() == sbox)
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

    /**
     * Internal testing main (for debugging only)
     * @param args
     * @throws Exception 
     */
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

// Helper classes used by the dialog combo boxes


/**
 * @author "Brian W. Loranger"
 */
class ScreenItem
{
    private Screen screen;

    /**
     * Initialize a new screen item
     * 
     * @param screen - screen for this item
     */
    public ScreenItem(Screen screen)
    {
        this.screen = screen;
    }

    /**
     * @return screen used for this item
     */
    public Screen getScreen()
    {
        return screen;
    }

    /* Return the screen's name
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return screen.getName().getValue();
    }

    /**
     * @return screen's id
     */
    public Long getId()
    {
        return screen.getId().getValue();
    }

    /**
     * Create a new item for pull down
     * 
     * @param screens - list of Screen's
     * @return screen item array
     */
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