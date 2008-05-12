package ome.formats.importer;

import java.awt.BorderLayout;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class HistoryHandler 
    extends JPanel 
{

    ArrayList<IObserver> observers = new ArrayList<IObserver>();
    
    private HistoryTable    table;
    
    HistoryHandler()
    {
        this.setOpaque(false);
        setLayout(new BorderLayout());
        
        table = HistoryTable.getHistoryTable();
        
        //JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
        //        null, table);
        
        //splitPane.setResizeWeight(0.1);
        
        add(table, BorderLayout.CENTER);
    }
    
    
    /**
     * Creates a singularity of the History Handler
     * @param viewer
     * @return
     */
    public static synchronized HistoryHandler getHistoryHandler()
    {
        if (ref == null) 
            ref = new HistoryHandler();
        return ref;
    }
    
    private static HistoryHandler ref;
    
    
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
        
        HistoryHandler hh = new HistoryHandler(); 
        JFrame f = new JFrame();   
        f.getContentPane().add(hh);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
    }
}
