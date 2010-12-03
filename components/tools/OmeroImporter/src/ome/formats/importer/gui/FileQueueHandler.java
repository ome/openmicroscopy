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
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
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
import omero.model.Dataset;
import omero.model.IObject;
import omero.model.Screen;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Brian Loranger brain at lifesci.dundee.ac.uk
 *
 */
public class FileQueueHandler extends JPanel 
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
    
    private final FileQueueChooser fileChooser;
    private final FileQueueTable qTable;
    
	private final HistoryTable historyTable;
    
    private final AtomicInteger count = new AtomicInteger(0);
    private final ScheduledExecutorService scanEx;
    private final ScheduledExecutorService importEx;
    private JProgressBar directoryProgressBar;
    private JDialog progressDialog;

    protected boolean cancelScan = false;

    private boolean candidatesFormatException = false;
    
    /**
     * @param scanEx ScheduledExecutorService for scanning
     * @param importEx ScheduledExecutorService for importing
     * @param viewer Parent viewer class
     * @param config ImportConfig
     */
    FileQueueHandler(ScheduledExecutorService scanEx, ScheduledExecutorService importEx,
            GuiImporter viewer, ImportConfig config)
    {
        this.scanEx = scanEx;
        this.importEx = importEx;
        this.config = config;
        this.viewer = viewer;
        this.historyTable = viewer.getHistoryTable();
        this.importReader = new OMEROWrapper(config);
        this.scanReader = new OMEROWrapper(config);

        //reader.setChannelStatCalculationStatus(true);
        
        setLayout(new BorderLayout());
        fileChooser = new FileQueueChooser(config, scanReader);
        fileChooser.addActionListener(this);
        fileChooser.addPropertyChangeListener(this);
        
        qTable = new FileQueueTable();
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
    
    /**
     * @return qTable;
     */
    public FileQueueTable getTable() {
		return qTable;
	}
    
    /**
     * @return OMEROMetadataStoreClient
     */
    protected OMEROMetadataStoreClient getOMEROMetadataStoreClient() {
        return viewer.getLoginHandler().getMetadataStore();
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent event)
    {
        final String action = event.getActionCommand();
        //final File[] files = fileChooser.getSelectedFiles();
        
        //If the directory changed, don't show an image.
        if (action.equals(JFileChooser.APPROVE_SELECTION)) {
        	doSelection(true);
        }
    }

    private void addEnabled(boolean enabled)
    {
    	if (enabled)
    		viewer.setCursor(Cursor.getDefaultCursor());
    	else
    		viewer.setCursor(java.awt.Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    	
    	qTable.addBtn.setEnabled(enabled);
    	qTable.removeBtn.setEnabled(enabled);
    }
    
    /**
     * @param enterDirectories -if True, will enter a directory rather then import it
     * This is used to allow 'normal' functionality for JFileChooser.APPROVE_SELECTION
     * using the VK_ENTER key.
     */
    private void doSelection(boolean enterDirectories)
    {
    	boolean filesOnly = true;
    	
        for (File file: fileChooser.getSelectedFiles())
        {
            if (file.isDirectory()) filesOnly = false;
        }
    	
    	if (enterDirectories && !filesOnly && fileChooser.getSelectedFiles().length == 1)
    		fileChooser.setCurrentDirectory(fileChooser.getSelectedFile());
    	else 
    		addSelectedFiles();
    }
    
    /**
     * Add all files that are selected in the JFileChooser
     * 
     */
    private void addSelectedFiles()
    {
    	
        boolean filesOnly = true;
        
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
        
        addEnabled(false);
		
        if (filesOnly)
        {
            boolean isSPW = false;
            final List<ImportContainer> containers = new ArrayList<ImportContainer>();
            boolean runCandidates = false;
            for (File file: fileChooser.getSelectedFiles())
            {
                String fileStr = file.toString();
                String fileExt = fileStr.substring(
                        fileStr.lastIndexOf('.') + 1, fileStr.length());
                fileExt = fileExt.toLowerCase();
                if (fileExt.equals("flex")
                    || fileExt.equals("xdce")
                    || fileExt.equals("mea")
                    || fileExt.equals("res")
                    || fileExt.equals("htd")
                    || fileExt.equals("pnl"))
                {
                    isSPW = true;
                } 
                else if (fileExt.equals("txt")
                         || fileExt.equals("xml")
                         || fileExt.equals("exp")
                         || fileExt.equals("log"))
                { // arbitrary file extension so we must do candidates
                    runCandidates = true;
                    break;
                }

                ImportContainer container = new ImportContainer(file, null, null, false, null, null, null, isSPW);
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
    
    /**
     * Retrieve the file chooser's selected reader then iterate over
     * each of our supplied containers filtering out those whose format
     * do not match those of the selected reader.
     * 
     * @param allContainers List of ImporterContainers
     */
    private void handleFiles(List<ImportContainer> allContainers)
    {
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
        	String b = ic.getReader();
        	if (a.equals(b) || b == null)
        	{
        		containers.add(ic);
        	}
        	else
        	{
        		log.debug(String.format("Skipping %s (%s != %s)",
        				ic.getFile().getAbsoluteFile(), a, b));        		
        	}
    	}

    	Boolean spw = spwOrNull(containers);
        
        if (containers.size() == 0 && !candidatesFormatException)
        {
        	final JOptionPane optionPane = new JOptionPane("\nNo importable files found in this selection.", JOptionPane.WARNING_MESSAGE);
            final JDialog errorDialog = new JDialog(viewer, "No Importable Files Found", true);
            errorDialog.setContentPane(optionPane);

            optionPane.addPropertyChangeListener(
                    new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent e) {
                            String prop = e.getPropertyName();

                            if (errorDialog.isVisible() 
                                    && (e.getSource() == optionPane)
                                    && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                                errorDialog.dispose();
                            }
                        }
                    });

            errorDialog.toFront();
            errorDialog.pack();
            errorDialog.setLocationRelativeTo(viewer);
            errorDialog.setVisible(true);
        }
    	
        if (candidatesFormatException)
        {
            viewer.candidateErrorsCollected(viewer);
            candidatesFormatException = false;
        }
                     
        if (spw == null) {
        	addEnabled(true);
            containers.clear();
            return; // Invalid containers.
        }
        
        if (getOMEROMetadataStoreClient() != null && spw.booleanValue())
        {
        	addEnabled(true);
            SPWDialog dialog =
                new SPWDialog(config, viewer, "Screen Import", true, getOMEROMetadataStoreClient());
            if (dialog.cancelled == true || dialog.screen == null) 
                return;                    
            for (ImportContainer ic : containers)
            {
                ic.setTarget(dialog.screen);
                String title = dialog.screen.getName().getValue(); 
                addFileToQueue(ic, title, false, 0);
            }
            
            qTable.centerOnRow(qTable.getQueue().getRowCount()-1);
            qTable.importBtn.requestFocus();

        }
        else if (getOMEROMetadataStoreClient() != null)
        {
        	addEnabled(true);
            ImportDialog dialog = 
                new ImportDialog(config, viewer, "Image Import", true, getOMEROMetadataStoreClient());
            if (dialog.cancelled == true || dialog.dataset == null) 
                return;  

            Double[] pixelSizes = new Double[] {dialog.pixelSizeX, dialog.pixelSizeY, dialog.pixelSizeZ};
            Boolean useFullPath = config.useFullPath.get();
            if (dialog.useCustomNamingChkBox.isSelected() == false)
                useFullPath = null; //use the default bio-formats naming
                
            for (ImportContainer ic : containers)
            {
                ic.setTarget(dialog.dataset);
                ic.setUserPixels(pixelSizes);
                ic.setArchive(dialog.archiveImage.isSelected());
                ic.setProjectID(dialog.project.getId().getValue());
                
                String title =
                dialog.project.getName().getValue() + " / " +
                dialog.dataset.getName().getValue();
                
                addFileToQueue(ic, title, useFullPath, config.numOfDirectories.get());
            }
            
            qTable.centerOnRow(qTable.getQueue().getRowCount()-1);
            qTable.importBtn.requestFocus();
        } else {
        	addEnabled(true);
            JOptionPane.showMessageDialog(viewer, 
                    "Due to an error the application is unable to \n" +
                    "retrieve an OMEROMetadataStore and cannot continue." +
                    "The most likely cause for this error is that you" +
                    "are not logged in. Please try to login again.");
        }
    }
    
    /**
     * Dialog 'must select at least one importable file'
     */
    private void mustSelectFile()
    {
        JOptionPane.showMessageDialog(viewer, 
                "You must select at least one importable file to\n" +
                "add to the import queue. Choose an image in the\n" +
                "left-hand panel first before continuing.");
    }
    
    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent e)
    {
        String prop = e.getPropertyName();
        if (prop.equals(ADD))
        {
        	doSelection(false);
        }
        
        else if (prop.equals(REMOVE))
        {
                int[] rows = qTable.getQueue().getSelectedRows();   

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
                    if (qTable.getQueue().getValueAt(rows[0], 2) == "added"
                        || qTable.getQueue().getValueAt(rows[0], 2) == "pending")
                    {
                        removeFileFromQueue(rows[0]);
                        rows = qTable.getQueue().getSelectedRows();                    
                    }
                }                
        }
        
        else if (prop.equals(CLEARDONE))
        {
                int numRows = qTable.getQueue().getRowCount();

                for (int i = (numRows - 1); i >= 0; i--)
                {
                    if (qTable.getQueue().getValueAt(i, 2) == "done")
                    {
                        removeFileFromQueue(i);                    
                    }
                }
                qTable.clearDoneBtn.setEnabled(false);
        }
        
        else if (prop.equals(CLEARFAILED))
        {
                int numRows = qTable.getQueue().getRowCount();

                for (int i = (numRows - 1); i >= 0; i--)
                {
                    if (qTable.getQueue().getValueAt(i, 2) == "failed" || 
                    		qTable.getQueue().getValueAt(i, 2) == "unreadable")
                    {
                        removeFileFromQueue(i);                    
                    }
                }  
                qTable.clearFailedBtn.setEnabled(false);
        }
       
        else if (prop.equals(IMPORT))
        {
            if (viewer.getLoggedIn() == false)
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
                    ImportContainer[] candidates = qTable.getImportContainersFromTable();

                    if (candidates != null)
                    {
                        ImportLibrary library = new ImportLibrary(getOMEROMetadataStoreClient(), importReader);
                        library.addObserver(new LoggingImportMonitor());

                        if (getOMEROMetadataStoreClient() != null) {
                            ImportHandler importHandler = new ImportHandler(importEx, viewer, qTable, config, library, candidates);
                            importHandler.addObserver(viewer);
                            importHandler.addObserver(qTable);
                        }
                    }
                    qTable.importing = true;
                    qTable.getQueue().setRowSelectionAllowed(false);
                    qTable.removeBtn.setEnabled(false);
                } else {
                    qTable.importBtn.setText("Wait...");
                    qTable.importBtn.setEnabled(false);
                    viewer.getStatusBar().setStatusIcon("gfx/import_cancelling_16.png",
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


	public void enableImports(boolean b)
	{
		qTable.addBtn.setEnabled(b);
		qTable.removeBtn.setEnabled(b);
		if (b==true && qTable.getTable().getRowCount() > 0)
			qTable.importBtn.setEnabled(true);
		else if (b==false)
			qTable.importBtn.setEnabled(false);
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

            if (isSPW != null && importContainer.getIsSPW() != isSPW.booleanValue()) {
                JOptionPane.showMessageDialog(viewer, 
                        "You have chosen some Screen-based images and some \n "+
                        "non-screen-based images. Please import only one type at a time.");
                log.debug("Directory import found SPW and non-SPW data:");
                for (ImportContainer ic2 : containers) {
                    log.debug(String.format("  Spw? %5s\t%s",ic2.getIsSPW(),ic2.getFile()));
                }
                return null;
            }
            isSPW = importContainer.getIsSPW();
        }
        return isSPW;
    }
    
    
    /**
     * Import cancelled dialog
     * 
     * @param frame parent frame
     * @return - true / false if import cancelled
     */
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
           
           StringBuffer buf = new StringBuffer();
               
           for (int i = start; i < directories.length - 1; i++)
           {
               //viewer.appendToDebugLn(directories[i]);
               if (directories[i].length() != 0)
               {
                   if (i == start)
                   {
                       buf.append(directories[i]);
                   } else
                   {
                       buf.append("/");
                       buf.append(directories[i]);                     
                   }
               }
           }

           buf.append("/");
           buf.append(file.getName());
           return buf.toString();
       }
    }

    /**
     * Split the directories by file seperator character ("/" or "\")
     * @param path
     * @return
     */
    private String[] splitDirectories(String path)
    {
        //viewer.appendToDebugLn(path);
        String[] fields = path.split("/");
        //Integer length = fields.length;
        //viewer.appendToDebugLn(length.toString());

        return fields;
    }
    
    /**
     * Remove file from Queue
     * 
     * @param row - row index of file to remove
     */
    private void removeFileFromQueue(int row)
    {
        qTable.getTable().removeRow(row);
        //qTable.table.fireTableRowsDeleted(row, row);
        if (qTable.getTable().getRowCount() == 0)
            qTable.importBtn.setEnabled(false);
    }

    /* (non-Javadoc)
     * @see ome.formats.importer.IObserver#update(ome.formats.importer.IObservable, ome.formats.importer.ImportEvent)
     */
    @SuppressWarnings("unchecked")
    public void update(IObservable observable, ImportEvent event)
    {
        final OMEROMetadataStoreClient store = viewer.getLoginHandler().getMetadataStore();  

        if (event instanceof ome.formats.importer.util.ErrorHandler.EXCEPTION_EVENT)
        {
            viewer.getErrorHandler().update(observable, event);
            
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
        	addEnabled(false);
        	
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
                    progressDialog.add(directoryProgressBar,"1,1,2,1");
                    JButton cancelBtn = new JButton("Cancel");
                    
                    progressDialog.setDefaultCloseOperation(
                            WindowConstants.DO_NOTHING_ON_CLOSE); 
                    
                    cancelBtn.addActionListener(new ActionListener() {
                        
                        public void actionPerformed(ActionEvent event)
                        {
                            cancelScan  = true;
                            progressDialog.dispose();
                            progressDialog = null;
                            addEnabled(true);
                        }
                    });      

                    progressDialog.add(cancelBtn, "2,3,R,C");
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
                      addEnabled(true);
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
            
            int[] selectedRows = null;
            
            if (historyTable != null)
            	selectedRows = historyTable.eTable.getSelectedRows();
            
            for (int r : selectedRows)
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
                                
                // TODO: The 6th parameter should be 'format', not 'fileName'!
                ImportContainer container = new ImportContainer(file, projectID, object, cancelScan, null, fileName, null, cancelScan);
                
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
                qTable.getTable().addRow(row);
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

            
            if (qTable.getTable().getRowCount() >  0)
                qTable.importBtn.setEnabled(true);
            	qTable.importBtn.doClick();
        }
    }

    
    /**
     * Add file to queue
     * 
     * @param container - container for file adding
     * @param pdsString - project/dataset/screen string
     * @param useFullPath - use full path / partial path for name
     * @param numOfDirectories how many directories to use for partial path
     */
    @SuppressWarnings("unchecked")
    private void addFileToQueue(ImportContainer container, String pdsString, Boolean useFullPath, int numOfDirectories) {
        Vector row = new Vector();
        String imageName;
        
        if (useFullPath != null) {
            imageName = getImageName(container.getFile(), useFullPath, numOfDirectories);
            container.setCustomImageName(imageName);
        } else {
            imageName = container.getFile().getAbsolutePath();
        }

        row.add(imageName);
        row.add(pdsString);
        row.add("added");
        row.add(container);
        qTable.getTable().addRow(row);
        if (qTable.getTable().getRowCount() == 1)
            qTable.importBtn.setEnabled(true);
    }
    
    /**
     * Update progress 
     * 
     * @param totalFiles
     * @param numFiles
     */
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
