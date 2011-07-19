/*
 * ome.formats.importer.gui.AddDatasetDialog
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

package ome.formats.importer.gui;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import loci.formats.gui.ComboFileFilter;

import ome.formats.importer.ImportConfig;
import ome.formats.importer.OMEROWrapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Brian Loranger brain at lifesci.dundee.ac.uk
 *
 */
public class FileQueueChooser extends JFileChooser implements ActionListener
{
    public final static String REFRESHED = "refreshed";
    
	/** Logger for this class */
	@SuppressWarnings("unused")
	private Log log = LogFactory.getLog(FileQueueChooser.class);
	
    boolean DEBUG = false;
    
    private static final long serialVersionUID = 1L;
    
    private String laf = UIManager.getLookAndFeel().getClass().getName();
    
    // This could be either a list or a table depending on the LAF
    private Component[] fileListObjects;
    
    private Component fileList = null;
    
    JButton refreshBtn;
    
    /**
     * File chooser on the file picker tab of the importer
     * 
     * @param config ImportConfig
     * @param scanReader OmeroWrapper
     */
    FileQueueChooser(ImportConfig config, OMEROWrapper scanReader) {
        
        try {
            JPanel fp = null;
            JToolBar tb = null;
            
            String refreshIcon = "gfx/recycled12.png";
            refreshBtn = GuiCommonElements.addBasicButton("Refresh ", refreshIcon, null);
            refreshBtn.setActionCommand(REFRESHED);
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
                } catch (Exception e) { log.info("component exception ignore");}
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
                panel.add(refreshBtn, "1,0,C,C");
                panel.add(fp.getComponent(0), "2,0,C,C");
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
                } catch (Exception e) { log.info("Exception ignored.");}
            }
        	/* Disabled temporarily */
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
        
        File dir = null;
        if (config != null)
        	dir = config.savedDirectory.get();
        
        if (dir != null) {
            this.setCurrentDirectory(dir);
        } else {
            this.setCurrentDirectory(this.getFileSystemView().getHomeDirectory());
        }
        
        this.setControlButtonsAreShown(false);
        this.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        this.setMultiSelectionEnabled(true);
        this.setDragEnabled(true);
        
        setAcceptAllFileFilterUsed(false);
        
        FileFilter[] originalFF = null;
        int readerFFSize = 0;
        if (scanReader != null)
        {
	        originalFF = loci.formats.gui.GUITools.buildFileFilters(scanReader.getImageReader());
	        FileFilter filter;
	        List<FileFilter> extensionFilters = new ArrayList<FileFilter>();
	        for (int i = 0; i < originalFF.length; i++) {
	        	filter = originalFF[i];
				if (filter instanceof ComboFileFilter) {
					ComboFileFilter cff = (ComboFileFilter) filter;
					extensionFilters.add(cff);
					extensionFilters.addAll(Arrays.asList(cff.getFilters()));
					break;
				}
			}
	        if (extensionFilters != null) {
	            originalFF =extensionFilters.toArray(
	                    new FileFilter[extensionFilters.size()]);
	        }
	        readerFFSize = originalFF.length;
        }

        FileFilter[] ff = new FileFilter[readerFFSize + 7];
        ff[0] = new DashFileFilter();
        ff[readerFFSize+1] = new DashFileFilter();
        ff[readerFFSize + 2] = new R3DNewFileFilter();
        ff[readerFFSize + 3] = new R3DOldFileFilter();
        ff[readerFFSize + 4] = new D3DNewFileFilter();
        ff[readerFFSize + 5] = new D3DOldFileFilter();
        ff[readerFFSize + 6] = new D3DNPrjFileFilter();

        if (originalFF != null)
        	System.arraycopy(originalFF, 0, ff, 1, originalFF.length);

        //this.addChoosableFileFilter(new DashFileFilter());
        
        //FileFilter combo = null;
        for (int i = 0; i < ff.length; i++)
            this.addChoosableFileFilter(ff[i]);
        this.setFileFilter(ff[1]);
        
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
    
    /**
     * Get all JLists and JTables if the LAF uses lists/tables
     * 
     * @param fileChooser fileChooser
     * @return fileListObjects
     */
    public Component[] getFileListObjects(JFileChooser fileChooser) {
        Vector<Component> v = new Vector<Component>();
        Stack<Component> s = new Stack<Component>();
        s.push(fileChooser);
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
    
    /**
     * @author Brian Loranger brain at lifesci.dundee.ac.uk
     *
     */
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
        
        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
         */
        public void mouseReleased(MouseEvent evt) {}
        
        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
         */
        public void mouseClicked(MouseEvent evt) {}
        
        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
         */
        public void mouseEntered(MouseEvent evt) {}
        
        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
         */
        public void mouseExited(MouseEvent evt) {}
    }

    /**
     * @param evt key pressed event
     */
    public void keyPressed(KeyEvent evt)
    {
        Object src = evt.getSource();
        int keyCode = evt.getKeyCode();
        
        if (src == fileList && keyCode == KeyEvent.VK_ENTER) {                            
            File[] arr = getSelectedFiles();
            if (arr.length == 1 && arr[0].isFile())
            {
                approveSelection();
            }
        }
    }

    /**
     * @author Brian Loranger brain at lifesci.dundee.ac.uk
     *
     */
    static class DashFileFilter extends javax.swing.filechooser.FileFilter
    {
        /* (non-Javadoc)
         * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
         */
        @Override
        public boolean accept(File f)
        {
           return true;
        }

        /* (non-Javadoc)
         * @see javax.swing.filechooser.FileFilter#getDescription()
         */
        @Override
        public String getDescription()
        {
            return "----------------------------------------------";
        }
        
    }
    
    /**
     * @author Brian Loranger brain at lifesci.dundee.ac.uk
     *
     */
    static class R3DNewFileFilter extends javax.swing.filechooser.FileFilter
    {
        /* (non-Javadoc)
         * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
         */
        @Override
        public boolean accept(File f)
        {
           return f.isDirectory() || f.getName().toLowerCase().endsWith("r3d.dv");
        }

        /* (non-Javadoc)
         * @see javax.swing.filechooser.FileFilter#getDescription()
         */
        @Override
        public String getDescription()
        {
            return "Deltavision Files - Raw (*R3D.dv)";
        }  
    }
    
    /**
     * @author Brian Loranger brain at lifesci.dundee.ac.uk
     *
     */
    static class D3DNewFileFilter extends javax.swing.filechooser.FileFilter
    {
        /* (non-Javadoc)
         * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
         */
        @Override
        public boolean accept(File f)
        {
           return f.isDirectory() || f.getName().toLowerCase().endsWith("r3d_d3d.dv");
        }

        /* (non-Javadoc)
         * @see javax.swing.filechooser.FileFilter#getDescription()
         */
        @Override
        public String getDescription()
        {
            return "Deltavision Files - Decon'd (*r3d_d3d.dv)";
        }  
    }
  
    /**
     * @author Brian Loranger brain at lifesci.dundee.ac.uk
     *
     */
    static class R3DOldFileFilter extends javax.swing.filechooser.FileFilter
    {
        /* (non-Javadoc)
         * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
         */
        @Override
        public boolean accept(File f)
        {
           return f.isDirectory() || f.getName().toLowerCase().endsWith(".r3d");
        }

        /* (non-Javadoc)
         * @see javax.swing.filechooser.FileFilter#getDescription()
         */
        @Override
        public String getDescription()
        {
            return "Deltavision Files - Raw (.r3d)";
        }  
    }
    
    /**
     * @author Brian Loranger brain at lifesci.dundee.ac.uk
     *
     */
    static class D3DOldFileFilter extends javax.swing.filechooser.FileFilter
    {
        /* (non-Javadoc)
         * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
         */
        @Override
        public boolean accept(File f)
        {
           return f.isDirectory() || f.getName().toLowerCase().endsWith(".r3d_d3d");
        }

        /* (non-Javadoc)
         * @see javax.swing.filechooser.FileFilter#getDescription()
         */
        @Override
        public String getDescription()
        {
            return "Deltavision Files - Decon'd (.r3d_d3d)";
        }  
    }
    
    /**
     * @author Brian Loranger brain at lifesci.dundee.ac.uk
     *
     */
    static class D3DNPrjFileFilter extends javax.swing.filechooser.FileFilter
    {
        /* (non-Javadoc)
         * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
         */
        @Override
        public boolean accept(File f)
        {
           return f.isDirectory() || f.getName().toLowerCase().endsWith("_prj.dv");
        }

        /* (non-Javadoc)
         * @see javax.swing.filechooser.FileFilter#getDescription()
         */
        @Override
        public String getDescription()
        {
            return "Deltavision Files - Projected (*_prj.dv)";
        }  
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event)
    {
        Object src = event.getSource();
        if (src == refreshBtn)
            this.setVisible(false);
            this.rescanCurrentDirectory();
            this.setVisible(true);
    } 
    
    /**
     * Main for testing (debugging only)
     * 
     * @param args
     * @throws Exception 
     */
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
        
        FileQueueChooser c = new FileQueueChooser(null, null);
        
        JFrame f = new JFrame(); 
        c.setMultiSelectionEnabled(true);
        
        f.getContentPane().add(c);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
    }
}
