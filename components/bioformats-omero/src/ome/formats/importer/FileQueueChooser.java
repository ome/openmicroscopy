/*
 *   $Id$
 *
 *   Copyright 2006 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.formats.importer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Stack;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import ome.formats.importer.util.Actions;

import layout.TableLayout;
import loci.formats.gui.ComboFileFilter;
import loci.formats.gui.FormatFileFilter;
import loci.formats.IFormatReader;
import loci.formats.ImageReader;


public class FileQueueChooser 
    extends JFileChooser implements ActionListener 
{
    boolean DEBUG = false;
    
    private static final long serialVersionUID = 1L;

    private Preferences    userPrefs = 
        Preferences.userNodeForPackage(Main.class);
    
    private String savedDirectory = userPrefs.get("savedDirectory", "");
    
    private String laf = UIManager.getLookAndFeel().getClass().getName();
    
    // This could be either a list or a table depending on the LAF
    private Component[] fileListObjects;
    
    private Component fileList = null;
    
    ImageReader reader = new ImageReader();
    
    JButton refreshBtn;
    
    FileQueueChooser() {
        
        try {
            JPanel fp = null;
            JToolBar tb = null;
            
            String refreshIcon = "gfx/recycled12.png";
            refreshBtn = addButton("Refresh ", refreshIcon, null);
            refreshBtn.setActionCommand(Actions.REFRESH);
            refreshBtn.addActionListener(this);
            JPanel panel = new JPanel();

            // Set up the main panel for tPane, quit, and send buttons
            double mainTable[][] =
            {{10, TableLayout.FILL,TableLayout.PREFERRED, TableLayout.FILL,10}, // columns
                    {TableLayout.PREFERRED}}; // rows

            TableLayout tl = new TableLayout(mainTable);
            panel.setLayout(tl);
            
            // Here's a nice little pieces of test code to find all components
            if (DEBUG)
            {
                try {
                    Component[] components = this.getComponents();
                    Component component = null;
                    System.err.println("Components: " + components.length);
                    for (int i = 0; i < components.length; i++)
                    {
                        component = components[i];
                        System.err.println("Component " + i + " = " + component.getClass());
                    }
                } catch (Exception e) {}
            }
            
            if (laf.contains("AquaLookAndFeel"))
            {
                //Do Aqua implimentation
                fp = (JPanel) this.getComponent(1);
                fp.setLayout(new BoxLayout(fp, BoxLayout.X_AXIS));
                fp.add(refreshBtn);
            }
            else if (laf.contains("QuaquaLookAndFeel"))
            {
                //do Quaqua implimentation
                fp = (JPanel) this.getComponent(1);
                panel.add(refreshBtn, "1,0,c,c");
                panel.add(fp.getComponent(0), "2,0,c,c");
                fp.add(panel, BorderLayout.NORTH);
            }
            else if (laf.contains("Windows"))
            {                
                try {
                	//Do windows implimentation
                	tb = (JToolBar) this.getComponent(1);
                    refreshBtn.setToolTipText("Refresh");
                    refreshBtn.setText(null);
                	tb.add(refreshBtn,8);
                } catch (Exception e) {}
            }
            else if (laf.contains("MetalLookAndFeel"))
            {
                //Do Metal implimentation
                JPanel prefp = (JPanel) this.getComponent(0);
                fp = (JPanel) prefp.getComponent(0);
                refreshBtn.setToolTipText("Refresh");
                refreshBtn.setText(null);
                Dimension size = new Dimension(24,24);
                refreshBtn.setMaximumSize(size);
                refreshBtn.setPreferredSize(size);
                refreshBtn.setMinimumSize(size);
                refreshBtn.setSize(size);
                fp.add(Box.createRigidArea(new Dimension(5,0)));
                fp.add(refreshBtn);
            }
            else if (laf.contains("GTKLookAndFeel"))
            {
                //do GTK implimentation
                fp = (JPanel) this.getComponent(0);
                refreshBtn.setIcon(null);
                fp.add(refreshBtn);
            }
            else if (laf.contains("MotifLookAndFeel"))
            {
                //do Motif implimentation
                fp = (JPanel) this.getComponent(0);
                fp.add(refreshBtn);
            }
            
            if (fp != null && DEBUG == true)
            {
            fp.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.red),
                    fp.getBorder()));
            System.err.println(fp.getLayout());
            }

            if (tb != null && DEBUG == true)
            {
            tb.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.red),
                    tb.getBorder()));
            System.err.println(tb.getLayout());
            }
        } 
        catch (ArrayIndexOutOfBoundsException e) {}
        
        
        if (savedDirectory.equals("") || !(new File(savedDirectory).exists()))
        {
            this.setCurrentDirectory(
                    this.getFileSystemView().getHomeDirectory());
        } else {
            this.setCurrentDirectory(new File(savedDirectory));   
        }
        
        this.setControlButtonsAreShown(false);
        this.setFileSelectionMode(JFileChooser.FILES_ONLY);
        this.setMultiSelectionEnabled(true);
        this.setDragEnabled(true);
        //this.setAccessory(new FindAccessory(this));
        
        IFormatReader[] readers = ((ImageReader) reader).getReaders();
        Vector v = new Vector();
        for (int i=0; i<readers.length; i++) {
            // NB: By default, some readers might need to open a file to
            // determine if it is the proper type, when the extension alone
            // isn't enough to distinguish.
            //
            // We want to disable that behavior for ImageReader,
            // because otherwise the combination filter is too slow.
            //
            // Also, most of the formats that do this are TIFF-based, and the
            // TIFF reader will already green-light anything with .tif
            // extension, making more thorough checks redundant.
            //if (readers[i].isMetadataComplete())
                v.add(new FormatFileFilter(readers[i], false));
        }
        
        FileFilter ff[] = ComboFileFilter.sortFilters(v);
        
        int readerFFSize = ff.length;
        /** Gets a JFileChooser that recognizes accepted file types. */
        
        setAcceptAllFileFilterUsed(false);
        ff = new FileFilter[readerFFSize + 6];
        System.arraycopy(ComboFileFilter.sortFilters(
                loci.formats.gui.GUITools.buildFileFilters(reader)), 0, ff, 0, readerFFSize);
        ff[readerFFSize] = new DashFileFilter();
        ff[readerFFSize + 1] = new R3DNewFileFilter();
        ff[readerFFSize + 2] = new R3DOldFileFilter();
        ff[readerFFSize + 3] = new D3DNewFileFilter();
        ff[readerFFSize + 4] = new D3DOldFileFilter();
        ff[readerFFSize + 5] = new D3DNPrjFileFilter();

        // set up the full filter for all supported types
        FileFilter[] comboFF = new FileFilter[readerFFSize];
        System.arraycopy(ComboFileFilter.sortFilters(
                loci.formats.gui.GUITools.buildFileFilters(reader)), 0, comboFF, 0, readerFFSize);
        FileFilter combo = null;
        if (comboFF.length > 1)
        {
            combo = new ComboFileFilter(comboFF, "All supported file types");
            addChoosableFileFilter(combo);
        }
        for (int i = 0; i < ff.length; i++)
            this.addChoosableFileFilter(ff[i]);
        if (combo != null) this.setFileFilter(combo);
        
         //Retrieve all JLists and JTables from the fileChooser
        fileListObjects = getFileListObjects(this);
        
         //For now, assume the first list/table found is the correct one
         //(this will need to be adjusted if LAF bugs crop up)
         //Shouldn't break anything since dblclick will just stop working if
         //this changes for some reason
        if (fileListObjects.length > 0 && !laf.contains("Windows")) {
            fileList = fileListObjects[0];
            MouseCommand mc = new MouseCommand();
            fileList.addMouseListener(mc);
        }
    }
    
    // Get all JLists and JTables if the LAF uses lists/tables
    protected Component[] getFileListObjects(JFileChooser fc) {
        Vector<Component> v = new Vector<Component>();
        Stack<Component> s = new Stack<Component>();
        s.push(fc);
        while (!s.isEmpty()) {
            Component c = (Component) s.pop();
 
            if (c instanceof Container) {
                Container d = (Container) c;
                for (int i = 0; i < d.getComponentCount(); i++) {

                    if (d.getComponent(i) instanceof JTable)
                    {
                        v.add(d.getComponent(i));
                    }
                    else
                        s.push(d.getComponent(i));
                }
            }
        }
        Component[] arr = new Component[v.size()];
        for (int i = 0; i < arr.length; i++)
            arr[i] =  v.get(i);
 
        return arr;
    }

// ----- Utility Classes -----    
    
    class MouseCommand implements MouseListener
    {
        public void mousePressed(MouseEvent evt) {
            Object src = evt.getSource();
     
            if (src == fileList) {
                if (evt.getModifiers() != InputEvent.BUTTON1_MASK) return;                               
                File[] arr = getSelectedFiles();
                if (evt.getClickCount() > 1 && arr.length == 1 && arr[0].isFile())
                {
                    approveSelection();
                }

            }
        }
        
        public void mouseReleased(MouseEvent evt) {}
        public void mouseClicked(MouseEvent evt) {}
        public void mouseEntered(MouseEvent evt) {}
        public void mouseExited(MouseEvent evt) {}
    }

    class DashFileFilter extends javax.swing.filechooser.FileFilter
    {

        @Override
        public boolean accept(File f)
        {
           return true;
        }

        @Override
        public String getDescription()
        {
            return "-------------------------";
        }
        
    }
    
    class R3DNewFileFilter extends javax.swing.filechooser.FileFilter
    {
        @Override
        public boolean accept(File f)
        {
           return f.isDirectory() || f.getName().toLowerCase().endsWith("r3d.dv");
        }

        @Override
        public String getDescription()
        {
            return "Deltavision Files - Raw (*R3D.dv)";
        }  
    }
    
    class D3DNewFileFilter extends javax.swing.filechooser.FileFilter
    {
        @Override
        public boolean accept(File f)
        {
           return f.isDirectory() || f.getName().toLowerCase().endsWith("r3d_d3d.dv");
        }

        @Override
        public String getDescription()
        {
            return "Deltavision Files - Decon'd (*r3d_d3d.dv)";
        }  
    }
  
    class R3DOldFileFilter extends javax.swing.filechooser.FileFilter
    {
        @Override
        public boolean accept(File f)
        {
           return f.isDirectory() || f.getName().toLowerCase().endsWith(".r3d");
        }

        @Override
        public String getDescription()
        {
            return "Deltavision Files - Raw (.r3d)";
        }  
    }
    
    class D3DOldFileFilter extends javax.swing.filechooser.FileFilter
    {
        @Override
        public boolean accept(File f)
        {
           return f.isDirectory() || f.getName().toLowerCase().endsWith(".r3d_d3d");
        }

        @Override
        public String getDescription()
        {
            return "Deltavision Files - Decon'd (.r3d_d3d)";
        }  
    }
    
    class D3DNPrjFileFilter extends javax.swing.filechooser.FileFilter
    {
        @Override
        public boolean accept(File f)
        {
           return f.isDirectory() || f.getName().toLowerCase().endsWith("_prj.dv");
        }

        @Override
        public String getDescription()
        {
            return "Deltavision Files - Projected (*_prj.dv)";
        }  
    }

    static JButton addButton(String name, String image, String tooltip)
    {
        JButton button = null;

        if (image == null) 
        {
            button = new JButton(name);
        } else {
            java.net.URL imgURL = Main.class.getResource(image);
            if (imgURL != null)
            {
                button = new JButton(name, new ImageIcon(imgURL));
            } else {
                button = new JButton(name);
                System.err.println("Couldn't find icon: " + image);
            }
        }
        return button;
    }

    public void actionPerformed(ActionEvent e)
    {
        Object src = e.getSource();
        if (src == refreshBtn)
            //firePropertyChange(Actions.REFRESH, false, true);
            this.setVisible(false);
            this.rescanCurrentDirectory();
            this.setVisible(true);
    }

    
    // ----- Main class used for testing ------    
    
    public static void main(String[] args)
    {
       
        String laf = UIManager.getSystemLookAndFeelClassName() ;
        //laf = "ch.randelshofer.quaqua.QuaquaLookAndFeel";
        //laf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        //laf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        //laf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        //laf = "javax.swing.plaf.metal.MetalLookAndFeel";
        
        System.err.println(laf);
        
        try {
            UIManager.setLookAndFeel(laf);
        } catch (Exception e) 
        { System.err.println(laf + " not supported."); }
        System.err.println("laf: " + UIManager.getLookAndFeel());
        
        FileQueueChooser c = new FileQueueChooser();
        
        JFrame f = new JFrame(); 
        c.setMultiSelectionEnabled(true);
        
        f.getContentPane().add(c);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
    }

}
