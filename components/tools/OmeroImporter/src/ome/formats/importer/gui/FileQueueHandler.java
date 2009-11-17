/*
 * ome.formats.importer.gui.FileQueueHandler
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

package ome.formats.importer.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;
import javax.swing.filechooser.FileFilter;

import layout.TableLayout;
import loci.formats.FormatException;
import loci.formats.FormatTools;
import loci.formats.IFormatReader;
import loci.formats.gui.FormatFileFilter;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.util.ErrorHandler;
import omero.model.Dataset;
import omero.model.IObject;
import omero.model.Screen;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@SuppressWarnings("serial")
public class FileQueueHandler 
    extends JPanel 
    implements ActionListener, PropertyChangeListener, IObserver
{
    /** Logger for this class */
    private static final Log log = LogFactory.getLog(FileQueueHandler.class);

    public final static String ADD = "add";
    public final static String REMOVE = "remove";
    public final static String CLEARDONE = "clear_done";
    public final static String CLEARFAILED = "clear_failed";
    public final static String IMPORT = "import";
    public final static String REFRESH = "refresh";
    
	private final ImportConfig config;
    private final OMEROWrapper importReader, scanReader;
    private final GuiImporter viewer;
    private final GuiCommonElements gui;
    
    private final FileQueueChooser fileChooser;
    private final FileQueueTable qTable;
    private final HistoryTable historyTable;
    
    private final AtomicInteger count = new AtomicInteger(0);
    private final ScheduledExecutorService scanEx;
    private final ScheduledExecutorService importEx;
    private JProgressBar directoryProgressBar;
    private JDialog progressDialog;
    
    private Integer directoryCount;

    protected boolean cancelScan = false;

    private boolean candidatesFormatException = false;
    
    /**
     * @param viewer
     */
    FileQueueHandler(ScheduledExecutorService scanEx, ScheduledExecutorService importEx,
            GuiImporter viewer, ImportConfig config)
    {
        this.scanEx = scanEx;
        this.importEx = importEx;
        this.config = config;
        this.viewer = viewer;
        this.gui = new GuiCommonElements(config);
        this.historyTable = viewer.historyTable;
        this.importReader = new OMEROWrapper(config);
        this.scanReader = new OMEROWrapper(config);
        
        directoryCount = 0;
        
        //reader.setChannelStatCalculationStatus(true);
        
        setLayout(new BorderLayout());
        fileChooser = new FileQueueChooser(config, scanReader);
        fileChooser.addActionListener(this);
        fileChooser.addPropertyChangeListener(this);
        
        //fc.setAccessory(new FindAccessory(fc));
        
        qTable = new FileQueueTable(config);
        qTable.addPropertyChangeListener(this);
        
        // Functionality to allows the reimport button to work
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
    
    protected OMEROMetadataStoreClient store() {
        return viewer.loginHandler.getMetadataStore();
    }

    public void actionPerformed(ActionEvent e)
    {
        final String action = e.getActionCommand();
        //final File[] files = fileChooser.getSelectedFiles();
        
        //If the directory changed, don't show an image.
        if (action.equals(JFileChooser.APPROVE_SELECTION)) {
            addFiles();
        }
    }

    private void addFiles()
    {
        boolean filesOnly = true;
        
        setCursor(java.awt.Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        final File[] _files = fileChooser.getSelectedFiles();                    

        if (_files == null)
        {
            mustSelectFile();
            return;
        }

        final String[] paths = new String[_files.length];
        for (int i = 0; i < paths.length; i++) {
            paths[i] = _files[i].getAbsolutePath();
        }

        final int which = count.incrementAndGet();
        final String msg = Arrays.toString(paths);
        log.info(String.format("Scheduling candidate calculations(%s)=%s", which, msg));
        final IObserver self = this;

        for (File file: fileChooser.getSelectedFiles())
        {
            if (file.isDirectory()) filesOnly = false;
        }
        
        if (filesOnly)
        {
            boolean isSPW = false;
            final List<ImportContainer> containers = new ArrayList<ImportContainer>();
            boolean runCandidates = false;
            for (File file: fileChooser.getSelectedFiles())
            {
                String fileStr = file.toString();
                String fileExt = fileStr.substring(fileStr.lastIndexOf('.')+1, fileStr.length());
                if (fileExt.equals("flex".toLowerCase()) 
                        || fileExt.equals("xdce".toLowerCase())
                        || fileExt.equals("mea".toLowerCase())
                        || fileExt.equals("res".toLowerCase()))
                {
                    isSPW = true;
                } 
                else if (fileExt.equals("txt".toLowerCase()) || fileExt.equals("xml".toLowerCase())) 
                { // arbitrary file extension so we must do candidates
                    runCandidates = true;
                    break;
                }

                ImportContainer container = new ImportContainer(file, null, null, null, false, null, null, null, isSPW);
                containers.add(container);
            }
            
            if (!runCandidates)
            {
                log.info(String.format("Handling import containers(%s)=%s", which, msg));
                handleFiles(containers);
                return;
            }
        }
        
        Runnable run = new Runnable() {
            public void run() {
                log.info(String.format("Background: calculating candidates(%s)=%s", which, msg));
                final ImportCandidates candidates = new ImportCandidates(scanReader, paths, self);
                final List<ImportContainer> containers = candidates.getContainers();
                javax.swing.SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        log.info(String.format("Handling import containers(%s)=%s", which, msg));
                        handleFiles(containers);
                    }
                });
            }
        };
        scanEx.execute(run);
    }
    
    private void handleFiles(List<ImportContainer> allContainers)
    {
    	// Retrieve the file chooser's selected reader then iterate over
    	// each of our supplied containers filtering out those whose format
    	// do not match those of the selected reader.
    	FileFilter selectedFilter = fileChooser.getFileFilter();
    	IFormatReader selectedReader = null;
    	if (selectedFilter instanceof FormatFileFilter)
    	{
    		log.debug("Selected file filter: " + selectedFilter);
    		selectedReader = ((FormatFileFilter) selectedFilter).getReader();
    	}
    	List<ImportContainer> containers = new ArrayList<ImportContainer>();
    	for (ImportContainer ic : allContainers)
    	{
    		if (selectedReader == null)
    		{
    			// The user selected "All supported file types"
    			containers = allContainers;
    			break;
    		}
        	String a = selectedReader.getFormat();
        	String b = ic.reader;
        	if (a.equals(b) || b == null)
        	{
        		containers.add(ic);
        	}
        	else
        	{
        		log.debug(String.format("Skipping %s (%s != %s)",
        				ic.file.getAbsoluteFile(), a, b));        		
        	}
    	}

    	Boolean spw = spwOrNull(containers);
        
        if (candidatesFormatException)
        {
            viewer.candidateErrorsCollected(viewer);
            candidatesFormatException = false;
        }
                
        if (spw == null) {
            setCursor(Cursor.getDefaultCursor());
            containers.clear();
            return; // Invalid containers.
        }
        
        if (store() != null && spw.booleanValue())
        {
            setCursor(Cursor.getDefaultCursor());
            SPWDialog dialog =
                new SPWDialog(gui, viewer, "Screen Import", true, store());
            if (dialog.cancelled == true || dialog.screen == null) 
                return;                    
            for (ImportContainer ic : containers)
            {
                ic.setTarget(dialog.screen);
                ic.setImageName(ic.file.getAbsolutePath());
                String title = dialog.screen.getName().getValue(); 
                addFileToQueue(ic, title, false, 0);
            }
            
            qTable.centerOnRow(qTable.queue.getRowCount()-1);
            qTable.importBtn.requestFocus();

        }
        else if (store() != null)
        {
            setCursor(Cursor.getDefaultCursor());
            ImportDialog dialog = 
                new ImportDialog(gui, viewer, "Image Import", true, store());
            if (dialog.cancelled == true || dialog.dataset == null) 
                return;  

            Double[] pixelSizes = new Double[] {dialog.pixelSizeX, dialog.pixelSizeY, dialog.pixelSizeZ};
            Boolean useFullPath = gui.config.useFullPath.get();
            if (dialog.fileCheckBox.isSelected() == false)
                useFullPath = null; //use the default bio-formats naming
                
            for (ImportContainer ic : containers)
            {
                ic.setTarget(dialog.dataset);
                ic.setUserPixels(pixelSizes);
                ic.setArchive(dialog.archiveImage.isSelected());
                ic.setProjectID(dialog.project.getId().getValue());
                ic.setImageName(ic.file.getAbsolutePath());
                
                String title =
                dialog.project.getName().getValue() + " / " +
                dialog.dataset.getName().getValue();
                
                addFileToQueue(ic, title, useFullPath, gui.config.numOfDirectories.get());
            }
            
            qTable.centerOnRow(qTable.queue.getRowCount()-1);
            qTable.importBtn.requestFocus();

            
        } else {
            setCursor(Cursor.getDefaultCursor());
            JOptionPane.showMessageDialog(viewer, 
                    "Due to an error the application is unable to \n" +
                    "retrieve an OMEROMetadataStore and cannot continue." +
                    "The most likely cause for this error is that you" +
                    "are not logged in. Please try to login again.");
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
        if (prop.equals(ADD))
        {
            addFiles();
        }
        
        else if (prop.equals(REMOVE))
        {
                int[] rows = qTable.queue.getSelectedRows();   

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
        
        else if (prop.equals(CLEARDONE))
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
        
        else if (prop.equals(CLEARFAILED))
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
       
        else if (prop.equals(IMPORT))
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
                    ImportContainer[] candidates = qTable.getFilesAndObjectTypes();

                    if (candidates != null)
                    {
                        ImportLibrary library = new ImportLibrary(store(), importReader);
                        library.addObserver(new LoggingImportMonitor());

                        if (store() != null) {
                            ImportHandler importHandler = new ImportHandler(importEx, viewer, qTable, config, library, candidates);
                            importHandler.addObserver(viewer);
                            importHandler.addObserver(qTable);
                        }
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
                        System.exit(0);
                    } else {
                        qTable.cancel = true;
                        qTable.importing = false;
                    }
                }
            } catch (Exception ex) {
            	log.error("Generic error while updating GUI for import.", ex);
                return;  
            }
        }
        
        else if (prop.equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY))
        {
            config.savedDirectory.set(new File(fileChooser.getCurrentDirectory().getAbsolutePath()));
        }       
        
        else if (prop.equals(REFRESH))
        {
            fileChooser.setVisible(false);
            fileChooser.rescanCurrentDirectory();
            fileChooser.setVisible(true);
        }
    }

    /**
     * Checks whether all the candidate imports in the list are either SPW or
     * not SPW. If there is a mismatch, then a warning is shown and null returned,
     * otherwise a Boolean of whether or not this is a SPW import will be returned.
     */
    private Boolean spwOrNull(final List<ImportContainer> containers) {
        Boolean isSPW = null;

        for (ImportContainer importContainer : containers) {

            /*
            // Temporary fix for ome.tiff and ome.xml to dataset and not spw 
            if (importContainer.reader.equals("OME-TIFF") || 
                    importContainer.reader.equals("OME-XML")) 
                return false;
                */

            if (isSPW != null && importContainer.isSPW != isSPW.booleanValue()) {
                JOptionPane.showMessageDialog(viewer, 
                        "You have chosen some Screen-based images and some \n "+
                        "non-screen-based images. Please import only one type at a time.");
                log.debug("Directory import found SPW and non-SPW data:");
                for (ImportContainer ic2 : containers) {
                    log.debug(String.format("  Spw? %5s\t%s",ic2.isSPW,ic2.file));
                }
                return null;
            }
            isSPW = importContainer.isSPW;
        }
        return isSPW;
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

    @SuppressWarnings("unchecked")
    public void update(IObservable observable, ImportEvent event)
    {
        final OMEROMetadataStoreClient store = viewer.loginHandler.getMetadataStore();  

        if (event instanceof ome.formats.importer.util.ErrorHandler.EXCEPTION_EVENT)
        {
            viewer.errorHandler.update(observable, event);
            
            if (event instanceof ome.formats.importer.util.ErrorHandler.UNKNOWN_FORMAT 
                    && fileChooser.getSelectedFiles().length == 1 && fileChooser.getSelectedFile().isFile())
            {
                JOptionPane.showMessageDialog(viewer, 
                        "This file's format is not recognized. \n" +
                        "Perhaps the file is damaged?", "Unknown Format", JOptionPane.WARNING_MESSAGE);               
            }
            
            if (event instanceof ome.formats.importer.util.ErrorHandler.FILE_EXCEPTION)
            {
                candidatesFormatException  = true;
            }
        }

        if (event instanceof ome.formats.importer.util.ErrorHandler.MISSING_LIBRARY)
        {
            JOptionPane.showMessageDialog(viewer, 
                    "You appear to be missing a required library needed for \n" +
                    "this file import. See the debug log messages for details."); 
        }
        
        else if (event instanceof ImportCandidates.SCANNING)
        {
            ImportCandidates.SCANNING ev = (ImportCandidates.SCANNING) event;
            if (scanEx.isShutdown() || cancelScan) {
                log.info("Cancelling scan");
                ev.cancel();
                cancelScan = false;
            }

            
            if (ev.totalFiles < 0)
            {
                if (progressDialog == null)
                {
                    double layoutTable[][] =
                    {{10, 180, 100, 10}, // columns
                    {5, 20, 5, 30, 5}}; // rows
                    
                    progressDialog = new JDialog(viewer, "Processing Files");
                    progressDialog.setSize(300, 90);
                    progressDialog.setLocationRelativeTo(viewer);
                    TableLayout layout = new TableLayout(layoutTable);
                    progressDialog.setLayout(layout);
                    directoryProgressBar = new JProgressBar();
                    directoryProgressBar.setString("Please wait.");
                    directoryProgressBar.setStringPainted(true);
                    directoryProgressBar.setIndeterminate(true);
                    progressDialog.add(directoryProgressBar,"1,1,2,c");
                    JButton cancelBtn = new JButton("Cancel");
                    
                    progressDialog.setDefaultCloseOperation(
                            WindowConstants.DO_NOTHING_ON_CLOSE); 
                    
                    cancelBtn.addActionListener(new ActionListener() {
                        
                        public void actionPerformed(ActionEvent e)
                        {
                            cancelScan  = true;
                            progressDialog.dispose();
                            progressDialog = null;
                            setCursor(Cursor.getDefaultCursor());
                        }
                    });      

                    progressDialog.add(cancelBtn, "2,3,r,c");
                    progressDialog.getRootPane().setDefaultButton(cancelBtn);
                    progressDialog.setVisible(true);
                    progressDialog.toFront();
                }
            } else
            {               
                if (progressDialog != null)
                {
                    updateProgress(ev.totalFiles, ev.numFiles);
                }

                  if (ev.totalFiles == ev.numFiles && progressDialog != null)
                  {
                      progressDialog.dispose();
                      progressDialog = null;
                      setCursor(Cursor.getDefaultCursor());
                  }
            }
            
            viewer.appendToOutput("Processing files: Scanned " + ev.numFiles + " of " + ev.totalFiles + " total.\n");

            log.debug(ev.toLog());

        }
        
        else if (event instanceof ImportEvent.REIMPORT)
        {
            
            String objectName = "", projectName = "", fileName = "";
            Long objectID = 0L, projectID = 0L;
            File file = null;
            Integer finalCount = 0;
            IObject object;
            
            int count = 0;
            
            if (historyTable != null)
                count = historyTable.table.getRowCount();

            for (int r = 0; r < count; r++)
            {
                Vector row = new Vector();
                
                objectID = (Long) historyTable.table.getValueAt(r, 5);
                projectID = (Long) historyTable.table.getValueAt(r, 6);
                
                fileName = (String) historyTable.table.getValueAt(r, 0);
                file = new File((String) historyTable.table.getValueAt(r, 4));
                
                
                if (projectID == null || projectID == 0)
                {
                    object = null;

                    try {
                        object = store.getTarget(Screen.class, objectID);
                        objectName = ((Screen)object).getName().getValue();
                    } catch (Exception e)
                    {
                        log.warn("Failed to retrieve screen: " + objectID, e);
                        continue;
                    }                    
                }
                else
                {
                    object = null;
                    try {
                    	object = store.getTarget(Dataset.class, objectID);
                        objectName = ((Dataset)object).getName().getValue();
                    } catch (Exception e)
                    {
                    	log.warn("Failed to retrieve dataset: " + objectID, e);
                        continue;
                    } 
                    
                    try {
                        projectName = store.getProject(projectID).getName().getValue();
                    } catch (Exception e)
                    {
                        log.warn("Failed to retrieve project: " + projectID, e);
                        continue;
                    }
                }
                
                ImportContainer container = new ImportContainer(file, projectID, object, fileName, cancelScan, null, fileName, null, cancelScan);
                
                finalCount = finalCount + 1;
                
                Double[] pixelSizes = new Double[] {1.0, 1.0, 1.0};
                
                row.add(fileName);
                if (projectID == null || projectID == 0)
                {
                    row.add(objectName); 
                }
                else
                {
                    row.add(projectName + "/" + objectName);   
                }
                // WHY ISN'T THIS CODE USING addFiletoQueue?!!?
                row.add("added");
                row.add(container);
                row.add(file);
                row.add(false);
                row.add(projectID);
                row.add(pixelSizes);
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

    
    @SuppressWarnings("unchecked")
    private void addFileToQueue(ImportContainer container, String pdsString, Boolean useFullPath, int numOfDirectories) {
        Vector row = new Vector();
        String imageName;
        
        if (useFullPath != null) {
            imageName = getImageName(container.file, useFullPath, numOfDirectories);
            container.setUserSpecifiedFileName(imageName);
        } else {
            imageName = container.file.getAbsolutePath();
        }

        row.add(imageName);
        row.add(pdsString);
        row.add("added");
        row.add(container);
        qTable.table.addRow(row);
        if (qTable.table.getRowCount() == 1)
            qTable.importBtn.setEnabled(true);
    }
    
    private void updateProgress(final int totalFiles, final int numFiles)
    {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                directoryProgressBar.setIndeterminate(false);
                directoryProgressBar.setMaximum(totalFiles);
                directoryProgressBar.setMinimum(0);
                directoryProgressBar.setValue(numFiles);
                directoryProgressBar.setString("Scanned " + numFiles + " of " + totalFiles);   
            }
        });
    }   
}
