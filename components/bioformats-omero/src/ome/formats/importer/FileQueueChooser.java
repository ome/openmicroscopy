package ome.formats.importer;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.Stack;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;

import loci.formats.ComboFileFilter;
import loci.formats.ImageReader;

public class FileQueueChooser extends JFileChooser {
    private static final long serialVersionUID = 1L;

    private Preferences userPrefs = Preferences.userNodeForPackage(Main.class);

    private String savedDirectory = userPrefs.get("savedDirectory", "");

    private String laf = UIManager.getLookAndFeel().getClass().getName();

    // This could be either a list or a table depending on the LAF
    private Component[] fileListObjects;

    private Component fileList = null;

    ImageReader reader = new ImageReader();

    FileQueueChooser() {

        if (savedDirectory.equals("") || !(new File(savedDirectory).exists())) {
            this.setCurrentDirectory(this.getFileSystemView()
                    .getHomeDirectory());
        } else {
            this.setCurrentDirectory(new File(savedDirectory));
        }

        this.setControlButtonsAreShown(false);
        this.setFileSelectionMode(JFileChooser.FILES_ONLY);
        this.setMultiSelectionEnabled(true);
        this.setDragEnabled(true);
        // this.setAccessory(new FindAccessory(this));

        int readerFFSize = reader.getFileFilters().length;
        /** Gets a JFileChooser that recognizes accepted file types. */

        FileFilter[] ff = new FileFilter[readerFFSize + 6];
        System.arraycopy(ComboFileFilter.sortFilters(reader.getFileFilters()),
                0, ff, 0, readerFFSize);
        ff[readerFFSize] = new DashFileFilter();
        ff[readerFFSize + 1] = new R3DNewFileFilter();
        ff[readerFFSize + 2] = new R3DOldFileFilter();
        ff[readerFFSize + 3] = new D3DNewFileFilter();
        ff[readerFFSize + 4] = new D3DOldFileFilter();
        ff[readerFFSize + 5] = new D3DNPrjFileFilter();

        // ff = ComboFileFilter.sortFilters(ff);
        FileFilter combo = null;
        if (ff.length > 1) {
            combo = new ComboFileFilter(ff, "All supported file types");
            addChoosableFileFilter(combo);
        }
        for (int i = 0; i < ff.length; i++)
            this.addChoosableFileFilter(ff[i]);
        if (combo != null)
            this.setFileFilter(combo);

        // Retrieve all JLists and JTables from the fileChooser
        fileListObjects = getFileListObjects(this);

        // For now, assume the first list/table found is the correct one
        // (this will need to be adjusted if LAF bugs crop up)
        // Shouldn't break anything since dblclick will just stop working if
        // this changes for some reason
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

                    if (d.getComponent(i) instanceof JTable) {
                        v.add(d.getComponent(i));
                    } else
                        s.push(d.getComponent(i));
                }
            }
        }
        Component[] arr = new Component[v.size()];
        for (int i = 0; i < arr.length; i++)
            arr[i] = v.get(i);

        return arr;
    }

    // ----- Utility Classes -----

    class MouseCommand implements MouseListener {
        public void mousePressed(MouseEvent evt) {
            Object src = evt.getSource();

            if (src == fileList) {
                if (evt.getModifiers() != InputEvent.BUTTON1_MASK)
                    return;
                File[] arr = getSelectedFiles();
                if (evt.getClickCount() > 1 && arr.length == 1
                        && arr[0].isFile()) {
                    approveSelection();
                }

            }
        }

        public void mouseReleased(MouseEvent evt) {
        }

        public void mouseClicked(MouseEvent evt) {
        }

        public void mouseEntered(MouseEvent evt) {
        }

        public void mouseExited(MouseEvent evt) {
        }
    }

    class DashFileFilter extends javax.swing.filechooser.FileFilter {

        @Override
        public boolean accept(File f) {
            return true;
        }

        @Override
        public String getDescription() {
            return "-------------------------";
        }

    }

    class R3DNewFileFilter extends javax.swing.filechooser.FileFilter {
        @Override
        public boolean accept(File f) {
            return f.isDirectory()
                    || f.getName().toLowerCase().endsWith("r3d.dv");
        }

        @Override
        public String getDescription() {
            return "Deltavision Files - Raw (*R3D.dv)";
        }
    }

    class D3DNewFileFilter extends javax.swing.filechooser.FileFilter {
        @Override
        public boolean accept(File f) {
            return f.isDirectory()
                    || f.getName().toLowerCase().endsWith("r3d_d3d.dv");
        }

        @Override
        public String getDescription() {
            return "Deltavision Files - Decon'd (*r3d_d3d.dv)";
        }
    }

    class R3DOldFileFilter extends javax.swing.filechooser.FileFilter {
        @Override
        public boolean accept(File f) {
            return f.isDirectory()
                    || f.getName().toLowerCase().endsWith(".r3d");
        }

        @Override
        public String getDescription() {
            return "Deltavision Files - Raw (*.r3d)";
        }
    }

    class D3DOldFileFilter extends javax.swing.filechooser.FileFilter {
        @Override
        public boolean accept(File f) {
            return f.isDirectory()
                    || f.getName().toLowerCase().endsWith(".r3d_d3d");
        }

        @Override
        public String getDescription() {
            return "Deltavision Files - Decon'd (*.r3d_d3d)";
        }
    }

    class D3DNPrjFileFilter extends javax.swing.filechooser.FileFilter {
        @Override
        public boolean accept(File f) {
            return f.isDirectory()
                    || f.getName().toLowerCase().endsWith("_prj.dv");
        }

        @Override
        public String getDescription() {
            return "Deltavision Files - Projected (*_prj.dv)";
        }
    }

    // ----- Main class used for testing ------

    public static void main(String[] args) {

        String laf = UIManager.getSystemLookAndFeelClassName();
        laf = "ch.randelshofer.quaqua.QuaquaLookAndFeel";
        // laf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel";
        // laf = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        // laf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel";
        // laf = "javax.swing.plaf.metal.MetalLookAndFeel";

        System.err.println(laf);

        try {
            UIManager.setLookAndFeel(laf);
        } catch (Exception e) {
            System.err.println(laf + " not supported.");
        }
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
