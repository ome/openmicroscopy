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

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * @author Brian W. Loranger
 *
 */
public class HistoryHandler extends JPanel 
{
	private static final long serialVersionUID = 1L;
	
	public final static String CLEARHISTORY = "CLEARHISTORY";
    public final static String HISTORYSEARCH = "HISTORYSEARCH";
    public final static String HISTORYREIMPORT = "HISTORYREIMPORT";
    
    public final HistoryTable table;
    
    HistoryHandler(GuiImporter viewer)
    {
        this.setOpaque(false);
        setLayout(new BorderLayout());
        this.table = new HistoryTable(viewer);
        add(table, BorderLayout.CENTER);
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
        
        HistoryHandler hh = new HistoryHandler(null); 
        JFrame f = new JFrame();   
        f.getContentPane().add(hh);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
    }
}
