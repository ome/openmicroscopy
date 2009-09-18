package ome.formats.importer.gui;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.IObserver;

@SuppressWarnings("serial")
public class HistoryHandler 
    extends JPanel 
{
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
     * @param args
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
