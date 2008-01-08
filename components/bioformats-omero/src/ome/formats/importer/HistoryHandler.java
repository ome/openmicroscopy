package ome.formats.importer;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class HistoryHandler
    extends JPanel 
    implements PropertyChangeListener
{

    private Main            viewer;
    private HistoryTable    table;
    
    
    
    HistoryHandler(Main viewer)
    {
        this.viewer = viewer;
        this.setOpaque(false);
        setLayout(new BorderLayout());
        
        table = new HistoryTable(viewer);
        
        //JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
        //        null, table);
        
        //splitPane.setResizeWeight(0.1);
        
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

    public void propertyChange(PropertyChangeEvent arg0)
    {
    }

}
