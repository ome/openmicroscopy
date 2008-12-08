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
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.Serializable;
import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.UIManager;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.util.Actions;
import omero.model.Dataset;
import omero.model.DatasetI;

@SuppressWarnings("serial")
public class FileQueueHandler 
    extends JPanel 
    implements ActionListener, PropertyChangeListener, IObserver
{

    private Preferences    userPrefs = 
        Preferences.userNodeForPackage(Main.class);
    
    private String savedDirectory = userPrefs.get("savedDirectory", "");
    
    @SuppressWarnings("unused")
    public ImportHandler       importHandler;
    private OMEROMetadataStoreClient  store;
 
    private OMEROWrapper    reader;
    
    private Main          viewer;
    
    FileQueueChooser fileChooser = null;
    public FileQueueTable qTable = null;
    private HistoryTable historyTable = null;
    
    /**
     * @param viewer
     */
    FileQueueHandler(Main viewer)
    {        
        this.viewer = viewer;
        
        reader = new OMEROWrapper();

        //reader.setChannelStatCalculationStatus(true);
        
        setLayout(new BorderLayout());
        fileChooser = new FileQueueChooser(reader);
        fileChooser.addActionListener(this);
        fileChooser.addPropertyChangeListener(this);
        
        //fc.setAccessory(new FindAccessory(fc));
        
        qTable = new FileQueueTable();
        qTable.addPropertyChangeListener(this);
        
        // Functionality to allows the reimport button to work
        historyTable = HistoryTable.getHistoryTable();
        if (historyTable != null)
        {
            historyTable.addObserver(this);
            addPropertyChangeListener(historyTable);
        }
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                fileChooser, qTable);
        
        splitPane.setResizeWeight(0.1);
        
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
                        dialog.dataset.getName().getValue(), dialog.project.getName().getValue(), 
                        dialog.useFullPath, dialog.numOfDirectories, 
                        dialog.archiveImage.isSelected(), dialog.project.getId().getValue());
            } else { 
                JOptionPane.showMessageDialog(viewer, 
                        "Due to an error the application is unable to \n" +
                        "retrieve an OMEROMetadataStore and cannot continue." +
                        "The most likely cause for this error is that you" +
                "are not logged in. Please try to login again.");
            }
        }
    }

    private void mustSelectFile()
    {
        JOptionPane.showMessageDialog(viewer, 
                "You must select at least one importable file to\n" +
                "add to the import queue. Choose an image in the\n" +
                "left-hand panel first before continuing.");
    }
    
    public void propertyChange(PropertyChangeEvent e)
    {
        String prop = e.getPropertyName();
        if (prop.equals(Actions.ADD))
        {
            if (fileChooser.getSelectedFile() == null)
            {
                mustSelectFile();
                return;
            }
            files = fileChooser.getSelectedFiles();                    

            for (File f : files)
            {
                if (!f.isFile()) 
                {
                    mustSelectFile();
                    return;
                }
            }

            store = viewer.loginHandler.getMetadataStore();
            
            if (store != null)
            {
                ImportDialog dialog = 
                    new ImportDialog(viewer, "Import", true, store);
                if (dialog.cancelled == true || dialog.dataset == null) 
                    return;                    
                for (File f : files)
                {
                    if (f.isFile()) 
                        addFileToQueue(f, dialog.dataset, 
                                dialog.dataset.getName().getValue(), 
                                dialog.project.getName().getValue(),
                                dialog.useFullPath, 
                                dialog.numOfDirectories,
                                dialog.archiveImage.isSelected(),
                                dialog.project.getId().getValue());
                }
                
                qTable.centerOnRow(qTable.queue.getRowCount()-1);
                
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
                    if (qTable.queue.getValueAt(rows[0], 2) == "added"
                        || qTable.queue.getValueAt(rows[0], 2) == "pending")
                    {
                        removeFileFromQueue(rows[0]);
                        rows = qTable.queue.getSelectedRows();                    
                    }
                }                
        }
        if (prop.equals(Actions.CLEARDONE))
        {
                int numRows = qTable.queue.getRowCount();

                for (int i = (numRows - 1); i >= 0; i--)
                {
                    if (qTable.queue.getValueAt(i, 2) == "done")
                    {
                        removeFileFromQueue(i);                    
                    }
                }
                qTable.clearDoneBtn.setEnabled(false);
        }
        if (prop.equals(Actions.CLEARFAILED))
        {
                int numRows = qTable.queue.getRowCount();

                for (int i = (numRows - 1); i >= 0; i--)
                {
                    if (qTable.queue.getValueAt(i, 2) == "failed")
                    {
                        removeFileFromQueue(i);                    
                    }
                }  
                qTable.clearFailedBtn.setEnabled(false);
        }
        if (prop.equals(Actions.IMPORT))
        {
            if (viewer.loggedIn == false)
            {
                JOptionPane.showMessageDialog(viewer, 
                        "You must be logged in before you can import.");
                return;
            }
            
            qTable.clearDoneBtn.setEnabled(false);
            qTable.clearFailedBtn.setEnabled(false);
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
                    qTable.queue.setRowSelectionAllowed(false);
                    qTable.removeBtn.setEnabled(false);
                } else {
                    qTable.importBtn.setText("Wait...");
                    qTable.importBtn.setEnabled(false);
                    viewer.statusBar.setStatusIcon("gfx/import_cancelling_16.png",
                    "Cancelling import... please wait.");
                    //JOptionPane.showMessageDialog(viewer, 
                    //        "You import will be cancelled after the " +
                    //        "current file has finished importing.");
                    if (cancelImportDialog(viewer) == true)
                    {
                        qTable.cancel = true;
                        qTable.abort = true;
                        qTable.importing = false;
                    } else {
                        qTable.cancel = true;
                        qTable.importing = false;
                    }
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
        if (prop.equals(Actions.REFRESH))
        {
            fileChooser.setVisible(false);
            fileChooser.rescanCurrentDirectory();
            fileChooser.setVisible(true);
        }
    }
    
    
    private boolean cancelImportDialog(Component frame) {
        String s1 = "OK";
        String s2 = "Force Quit Now";
        Object[] options = {s1, s2};
        int n = JOptionPane.showOptionDialog(frame,
                "Click 'OK' to cancel after the current file has\n" +
                "finished importing, or click 'Force Quit Now' to\n" +
                "force the importer to quit importing immediately.\n\n" +
                "You should only force quit the importer if there\n" +
                "has been an import problem, as this leaves partial\n" +
                "files in your server dataset.\n\n",
                "Cancel Import",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                s1);
        if (n == JOptionPane.NO_OPTION) {
            return true;
        } else {
            return false;
        }
    }
    
    @SuppressWarnings("unchecked")
    private void addFileToQueue(File file, Dataset dataset, String dName, 
            String project, Boolean useFullPath, 
            int numOfDirectories, boolean archiveImage, Long projectID)
    {
        Vector row = new Vector();
        
        String imageName = getImageName(file, useFullPath, numOfDirectories);
               
        row.add(imageName);
        row.add(project + "/" + dName);
        row.add("added");
        row.add(dataset.getId().getValue());
        row.add(file);
        row.add(archiveImage);
        row.add(projectID);
        qTable.table.addRow(row);
        if (qTable.table.getRowCount() == 1)
            qTable.importBtn.setEnabled(true);
    }
    
    /**
     * Return a stipped down string containing the file name and X number of directories above it.
     * Used for display purposes.
     * @param file
     * @param useFullPath
     * @param numOfDirectories
     * @return
     */
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
        @SuppressWarnings("unused")
        Integer length = fields.length;
        //viewer.appendToDebugLn(length.toString());
       
        
        return fields;
    }
    
    private void removeFileFromQueue(int row)
    {
        qTable.table.removeRow(row);
        //qTable.table.fireTableRowsDeleted(row, row);
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

    public void update(IObservable observable, Object message, Object[] args)
    {
        if (message == "REIMPORT")
        {
            store = viewer.loginHandler.getMetadataStore();  
            
            String datasetName = "", projectName = "", fileName = "";
            Long datasetID = 0L, projectID = 0L;
            File file = null;
            Integer finalCount = 0;
            
            int count = 0;
            
            if (historyTable != null)
                count = historyTable.table.getRowCount();

            //System.err.println(count);
            for (int r = 0; r < count; r++)
            {
                Vector<Serializable> row = new Vector<Serializable>();
                
                datasetID = (Long) historyTable.table.getValueAt(r, 5);
                projectID = (Long) historyTable.table.getValueAt(r, 6);
                
                fileName = (String) historyTable.table.getValueAt(r, 0);
                file = new File((String) historyTable.table.getValueAt(r, 4));
                
                try {
                    datasetName = store.getDataset(datasetID).getName().getValue();
                } catch (Exception e)
                {
                    //System.err.println("failed getDatasetName:" + datasetID);
                    //e.printStackTrace();
                    continue;
                } 

                
                try {
                    projectName = store.getProject(projectID).getName().getValue();
                } catch (Exception e)
                {
                    //System.err.println("failed getProjectName:" + projectID);
                    //e.printStackTrace();
                    continue;
                }
                
                finalCount = finalCount + 1;
                Dataset d = store.getDataset(datasetID);
                
                row.add(fileName);
                row.add(projectName + "/" + datasetName);
                row.add("added");
                // FIXME: Blitz types are not serializable.
                row.add(d.getId().getValue());
                row.add(file);
                row.add(false);
                row.add(projectID);
                qTable.table.addRow(row);
            }
            
            if (finalCount == 0)
            {
                JOptionPane.showMessageDialog(viewer, 
                        "None of the images in this history\n" +
                        "list can be reimported.");                
            } else if (finalCount == 1)
            {
                JOptionPane.showMessageDialog(viewer, 
                        "One of the images in this history list has been\n" +
                        "re-added to the import queue for reimport.");                 
            } else if (finalCount > 1)
            {
                JOptionPane.showMessageDialog(viewer, 
                        finalCount + " images in this history list have been re-added\n" +
                        "to the import queue for reimport.");                 
            }

            
            if (qTable.table.getRowCount() >  0)
                qTable.importBtn.setEnabled(true);
        }
    }
}


/*

Vector row = new Vector();

String imageName = getImageName(file, useFullPath, numOfDirectories);
       
row.add(imageName);
row.add(project + "/" + dName);
row.add("added");
row.add(dataset);
row.add(file);
row.add(archiveImage);
row.add(projectID);
qTable.table.addRow(row);
if (qTable.table.getRowCount() == 1)
    qTable.importBtn.setEnabled(true);

*/