/*
 * ome.formats.testclient.TestClient
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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import loci.formats.ReaderWrapper;

import ome.formats.OMEROMetadataStore;
import ome.formats.importer.util.Actions;
import ome.model.containers.Dataset;


@SuppressWarnings("serial")
public class FileQueueHandler 
    extends JPanel 
    implements ActionListener, PropertyChangeListener
{

    private Preferences    userPrefs = 
        Preferences.userNodeForPackage(Main.class);
    
    private String savedDirectory = userPrefs.get("savedDirectory", "");
    
    @SuppressWarnings("unused")
    private ImportHandler       importHandler;
    private OMEROMetadataStore  store;
    private OMEROWrapper    reader;
    private Main          viewer;
    
    FileQueueChooser fileChooser = null;
    FileQueueTable qTable = null;

    
    FileQueueHandler(Main viewer)
    {        
        this.viewer = viewer;
        reader = new OMEROWrapper();
        reader.setChannelStatCalculationStatus(true);
     
        setLayout(new BorderLayout());
        fileChooser = new FileQueueChooser();
        fileChooser.addActionListener(this);
        fileChooser.addPropertyChangeListener(this);
        

        //fc.setAccessory(new FindAccessory(fc));
        
        qTable = new FileQueueTable();
        qTable.addPropertyChangeListener(this);
        qTable.importBtn.setEnabled(false);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                fileChooser, qTable);

        //splitPane.setResizeWeight(0.1);
        
        add(splitPane, BorderLayout.CENTER);
        
        
    }

//  where member variables are declared
    File[] files = null;
    File file = null;
    int[] rows = null;
    
    public void actionPerformed(ActionEvent e)
    {
        String action = e.getActionCommand();
        //System.err.println("Action Fired: " + prop);
        
        //If the directory changed, don't show an image.
        if (action.equals(JFileChooser.APPROVE_SELECTION)) {
            file = fileChooser.getSelectedFile();
            store = viewer.loginHandler.getMetadataStore();                    

            if (store != null)
            {
                ImportDialog dialog = 
                    new ImportDialog(viewer, "Import", true, store);
                if (dialog.cancelled == true || dialog.dataset == null) 
                    return;

                addFileToQueue(file, dialog.dataset,
                        dialog.dataset.getName(), dialog.project.getName(), 
                        dialog.useFullPath, dialog.numOfDirectories, 
                        dialog.archiveImage.isSelected());
            } else { 
                JOptionPane.showMessageDialog(viewer, 
                        "Due to an error the application is unable to \n" +
                        "retrieve an OMEROMetadataStore and cannot continue." +
                        "The most likely cause for this error is that you" +
                "are not logged in. Please try to login again.");
            }
        }
    }

    public void propertyChange(PropertyChangeEvent e)
    {
        String prop = e.getPropertyName();
        if (prop.equals(Actions.ADD))
        {
            files = fileChooser.getSelectedFiles();
            store = viewer.loginHandler.getMetadataStore();                    

            Boolean fileSelected = false;
            for (File f : files)
            {
                if (f.isFile() && reader.isThisType(f.getName())) 
                    fileSelected = true;
            }


            if (fileSelected != true)
            {
                JOptionPane.showMessageDialog(viewer, 
                        "You must select at least one importable file to\n" +
                        "add to the import queue. Choose an image in the\n" +
                "left-hand panel first before continuing.");
                return;
            }

            if (fileSelected == true && store != null)
            {
                ImportDialog dialog = 
                    new ImportDialog(viewer, "Import", true, store);
                if (dialog.cancelled == true || dialog.dataset == null) 
                    return;                    
                for (File f : files)
                {
                    if (f.isFile() && reader.isThisType(f.getName())) 
                        addFileToQueue(f, dialog.dataset, 
                                dialog.dataset.getName(), 
                                dialog.project.getName(),
                                dialog.useFullPath, 
                                dialog.numOfDirectories,
                                dialog.archiveImage.isSelected());
                }
            } else {
                JOptionPane.showMessageDialog(viewer, 
                        "Due to an error the application is unable to \n" +
                        "retrieve an OMEROMetadataStore and cannot continue." +
                        "The most likely cause for this error is that you" +
                "are not logged in. Please try to login again.");
            }
        }
        if (prop.equals(Actions.REMOVE))
        {
            try {

                rows = qTable.queue.getSelectedRows();   

                if (rows.length == 0)
                {
                    JOptionPane.showMessageDialog(viewer, 
                            "You must select at least one file in the queue to\n" +
                            "remove. Choose an image in the right-hand panel \n" +
                    "first before removing.");
                    return;
                }

                while (rows.length > 0)
                {
                    if (qTable.queue.getValueAt(rows[0], 2) == "added")
                    {
                        removeFileFromQueue(rows[0]);
                        rows = qTable.queue.getSelectedRows();                    
                    }
                }                
            } catch (Exception ex) { 
                ex.printStackTrace();
                return; 
            }

        }
        
        if (prop.equals(Actions.IMPORT))
        {   
            try {
                if (qTable.importing == false)
                {
                    ImportContainer[] fads = qTable.getFilesAndDataset();

                    if (fads != null)
                    {
                        store = viewer.loginHandler.getMetadataStore();                    
                        if (store != null)
                            importHandler = 
                                new ImportHandler(viewer, qTable, store, reader, fads);
                    }
                    qTable.importing = true;
                } else {
                    qTable.cancel = true;
                    qTable.importing = false;
                    qTable.importBtn.setText("Wait...");
                    qTable.importBtn.setEnabled(false);
                    viewer.statusBar.setStatusIcon("gfx/import_cancelling_16.png",
                    "Cancelling import... please wait.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return;  
            }
        }
        if (prop.equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY))
        {
            savedDirectory = fileChooser.getCurrentDirectory().getAbsolutePath();
            userPrefs.put("savedDirectory", savedDirectory);
        }
    }
    
   
    @SuppressWarnings("unchecked")
    private void addFileToQueue(File file, Dataset dataset, String dName, 
            String project, Boolean useFullPath, 
            int numOfDirectories, boolean archiveImage)
    {
        Vector row = new Vector();
        
        String imageName = getImageName(file, useFullPath, numOfDirectories);
               
        row.add(imageName);
        row.add(project + "/" + dName);
        row.add("added");
        row.add(dataset);
        row.add(file);
        row.add(archiveImage);
        qTable.table.addRow(row);
        if (qTable.table.getRowCount() == 1)
            qTable.importBtn.setEnabled(true);
    }
    
    private String getImageName(File file, Boolean useFullPath, int numOfDirectories)
    {
       // standardize the format of files from window '\' to unix '/' 
       String path = file.getAbsolutePath().replace( '\\', '/' );
        
       if (useFullPath == true) return path;
       else if (numOfDirectories == 0) return file.getName();      
       else 
       {
           String[] directories = splitDirectories(path);
           if (numOfDirectories > directories.length - 1) 
               numOfDirectories = directories.length - 1;
           
           int start = directories.length - numOfDirectories - 1;
           
           String fileName = "";
               
           for (int i = start; i < directories.length - 1; i++)
           {
               //viewer.appendToDebugLn(directories[i]);
               if (directories[i].length() != 0)
               {
                   if (i == start)
                   {
                       fileName = directories[i];
                   } else
                   {
                       fileName = fileName + "/" + directories[i];                       
                   }
               }
           }

           fileName = fileName + "/" + file.getName();  
           
           return fileName;
       }
    }

    // Split the directories by file seperator character ("/" or "\")
    private String[] splitDirectories(String path)
    {
        //viewer.appendToDebugLn(path);
        String[] fields = path.split("/");
        Integer length = fields.length;
        //viewer.appendToDebugLn(length.toString());
       
        
        return fields;
    }
    
    private void removeFileFromQueue(int row)
    {
        qTable.table.removeRow(row);
        if (qTable.table.getRowCount() == 0)
            qTable.importBtn.setEnabled(false);
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
        
        FileQueueHandler fqh = new FileQueueHandler(null); 
        JFrame f = new JFrame();   
        f.getContentPane().add(fqh);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.pack();
    }
}
