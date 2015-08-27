/*
 * org.openmicroscopy.shoola.agents.treeviewer.util.StatusLabel
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2015 University of Dundee. All rights reserved.
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
package org.openmicroscopy.shoola.env.data.util;


import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.ImportEvent.FILESET_UPLOAD_END;
import ome.formats.importer.util.ErrorHandler;
import omero.cmd.CmdCallback;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.openmicroscopy.shoola.env.data.ImportException;
import org.openmicroscopy.shoola.env.data.model.FileObject;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

import pojos.util.PojoMapper;
import pojos.DataObject;
import pojos.FilesetData;
import pojos.PixelsData;

/**
 * Component displaying the status of a specific import.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @version 3.0
 * @since 3.0-Beta4
 */
public class StatusLabel
    extends JPanel
    implements IObserver
{

    /** The text displayed when the file is already selected.*/
    public static final String DUPLICATE = "Already processed, skipping";

    /** The text indicating the scanning steps. */
    public static final String SCANNING_TEXT = "Scanning...";

    /** 
     * Bound property indicating that the original container has been reset.
     * */
    public static final String NO_CONTAINER_PROPERTY = "noContainer";

    /** Bound property indicating that children files have been set. */
    public static final String FILES_SET_PROPERTY = "filesSet";

    /** 
     * Bound property indicating that the file has to be reset
     * This should be invoked if the log file for example has been selected.
     */
    public static final String FILE_RESET_PROPERTY = "fileReset";

    /** Bound property indicating that the import of the file has started. */
    public static final String FILE_IMPORT_STARTED_PROPERTY =
            "fileImportStarted";

    /** 
     * Bound property indicating that the container corresponding to the
     * folder has been created. 
     * */
    public static final String CONTAINER_FROM_FOLDER_PROPERTY =
            "containerFromFolder";

    /** Bound property indicating that the status has changed.*/
    public static final String CANCELLABLE_IMPORT_PROPERTY =
            "cancellableImport";

    /** Bound property indicating that the debug text has been sent.*/
    public static final String DEBUG_TEXT_PROPERTY = "debugText";

    /** Bound property indicating that the import is done. */
    public static final String IMPORT_DONE_PROPERTY = "importDone";

    /** Bound property indicating that the upload is done. */
    public static final String UPLOAD_DONE_PROPERTY = "uploadDone";

    /** Bound property indicating that the scanning has started. */
    public static final String SCANNING_PROPERTY = "scanning";

    /** Bound property indicating that the scanning has started. */
    public static final String PROCESSING_ERROR_PROPERTY = "processingError";

    /** The default text of the component.*/
    public static final String DEFAULT_TEXT = "Pending...";

    /** Text to indicate that the import is cancelled. */
    private static final String CANCEL_TEXT = "Cancelled";

    /** Text to indicate that no files to import. */
    private static final String NO_FILES_TEXT = "No Files to Import.";

    /** The width of the upload bar.*/
    private static final int WIDTH = 200;

    /** The maximum number of value for upload.*/
    private static final int MAX = 100;

    /** 
     * The number of processing sets.
     * 1. Importing Metadata
     * 2. Processing Pixels
     * 3. Generating Thumbnails
     * 4. Processing Metadata
     * 5. Generating Objects
     */
    /** Map hosting the description of each step.*/
    private static final Map<Integer, String> STEPS;

    /** Map hosting the description of the failure at a each step.*/
    private static final Map<Integer, String> STEP_FAILURES;

    static {
        STEPS = new HashMap<Integer, String>();
        STEPS.put(1, "Importing Metadata");
        STEPS.put(2, "Reading Pixels");
        STEPS.put(3, "Generating Thumbnails");
        STEPS.put(4, "Reading Metadata");
        STEPS.put(5, "Generating Objects");
        STEPS.put(6, "Complete");
        STEP_FAILURES = new HashMap<Integer, String>();
        STEP_FAILURES.put(1, "Failed to Import Metadata");
        STEP_FAILURES.put(2, "Failed to Read Pixels");
        STEP_FAILURES.put(3, "Failed to Generate Thumbnails");
        STEP_FAILURES.put(4, "Failed to Read Metadata");
        STEP_FAILURES.put(5, "Failed to Generate Objects");
    }

    /** The container.*/
    private ImportContainer ic;

    /** The number of images in a series. */
    private int seriesCount;

    /** Flag indicating that the import has been cancelled. */
    private boolean markedAsCancel;

    /** Flag indicating that the import can or not be cancelled.*/
    private boolean cancellable;

    /** 
     * Flag indicating that the file has already been imported or already
     * in the queue.
     */
    private boolean markedAsDuplicate;

    /** The size of the file.*/
    private String fileSize;

    /** The size units.*/
    private String units;

    /** The total size of uploaded files.*/
    private long totalUploadedSize;

    /** The label displaying the general import information.*/
    private JLabel generalLabel;

    /** Indicate the progress of the upload.*/
    private JProgressBar uploadBar;

    /** Indicate the progress of the processing.*/
    private JProgressBar processingBar;

    /** The size of the upload,*/
    private long sizeUpload;

    /** The labels displaying information before the progress bars.*/
    private List<JLabel> labels;

    /** Checksum event stored for later retrieval */
    private FILESET_UPLOAD_END checksumEvent;

    /** The exception if an error occurred.*/
    private ImportException exception;

    /** The list of pixels' identifiers returned when the import is complete.*/
    private Set<PixelsData> pixels;

    /** The file associated to that import.*/
    private FilesetData fileset;

    /** The callback. This should only be set when importing a directory.*/
    private Object callback;

    /** Indicates that the file scanned is a directory.*/
    //private boolean directory;

    /** The id of the log file.*/
    private long logFileID;

    /** The processing step.*/
    private int step;

    /** Indicates if the upload ever started.*/
    private boolean uploadStarted;

    /** The file or folder this component is for.*/
    private FileObject sourceFile;

    /** 
     * Formats the size of the uploaded data.
     * 
     * @param value The value to display.
     * @return See above.
     */
    private String formatUpload(long value)
    {
        StringBuffer buffer = new StringBuffer();
        String v = FileUtils.byteCountToDisplaySize(value);
        String[] values = v.split(" ");
        if (values.length > 1) {
            String u = values[1];
            if (units.equals(u)) buffer.append(values[0]);
            else buffer.append(v);
        } else buffer.append(v);
        buffer.append("/");
        buffer.append(fileSize);
        return buffer.toString();
    }

    /** Builds and lays out the UI.*/
    private void buildUI()
    {
        labels = new ArrayList<JLabel>();
        setLayout(new FlowLayout(FlowLayout.LEFT));
        add(generalLabel);
        JLabel label = new JLabel("Upload");
        label.setVisible(false);
        labels.add(label);
        add(label);
        add(uploadBar);
        add(Box.createHorizontalStrut(5));
        label = new JLabel("Processing");
        label.setVisible(false);
        labels.add(label);
        add(label);
        add(processingBar);
        setOpaque(false);
    }

    /** Initializes the components.*/
    private void initialize()
    {
        step = 0;
        sizeUpload = 0;
        fileSize = "";
        seriesCount = 0;
        markedAsCancel = false;
        cancellable = true;
        totalUploadedSize = 0;
        generalLabel = new JLabel(DEFAULT_TEXT);
        Font f = generalLabel.getFont();
        Font derived = f.deriveFont(f.getStyle(), f.getSize()-2);
        uploadBar = new JProgressBar(0, MAX);
        uploadBar.setFont(derived);
        uploadBar.setStringPainted(true);
        Dimension d = uploadBar.getPreferredSize();
        uploadBar.setPreferredSize(new Dimension(WIDTH, d.height));
        processingBar = new JProgressBar(0, STEPS.size());
        processingBar.setStringPainted(true);
        processingBar.setString(DEFAULT_TEXT);
        processingBar.setFont(derived);
        uploadBar.setVisible(false);
        processingBar.setVisible(false);
    }

    /**
     * Handles error that occurred during the processing.
     * 
     * @param text The text to display if any.
     * @param fire Indicate to fire a property.
     */
    private void handleProcessingError(String text, boolean fire)
    {
        if (isMarkedAsCancel()) return;
        generalLabel.setText(text);
        cancellable = false;
        if (step > 0)
            processingBar.setString(STEP_FAILURES.get(step));
        if (fire)
            firePropertyChange(PROCESSING_ERROR_PROPERTY, null, this);
    }

    /**
     * Creates a new instance.
     * 
     * @param sourceFile The file associated to that label.
     */
    public StatusLabel(FileObject sourceFile)
    {
        this.sourceFile = sourceFile;
        initialize();
        buildUI();
    }

    /** 
     * Sets the file set when the upload is complete.
     * To be modified.
     * 
     * @param fileset The value to set.
     */
    public void setFilesetData(final FilesetData fileset)
    {
        this.fileset = fileset;
    }

    /**
     * Returns <code>true</code> if it is a HCS file, <code>false</code>
     * otherwise.
     *
     * @return See above.
     */
    public boolean isHCS()
    {
        if (ic == null) return false;
        Boolean b = ic.getIsSPW();
        if (b == null) return false;
        return b.booleanValue();
    }

    /**
     * Returns the file set associated to the import.
     * 
     * @return See above.
     */
    public FilesetData getFileset() { return fileset; }

    /**
     * Sets the collection of files to import.
     * 
     * @param usedFiles The value to set.
     */
    public void setUsedFiles(String[] usedFiles)
    {
        if (usedFiles == null) return;
        for (int i = 0; i < usedFiles.length; i++) {
            sizeUpload += (new File(usedFiles[i])).length();
        }
        fileSize = FileUtils.byteCountToDisplaySize(sizeUpload);
        String[] values = fileSize.split(" ");
        if (values.length > 1) units = values[1];
    }

    /**
     * Sets the callback. This method should only be invoked when the 
     * file is imported from a folder.
     * 
     * @param cmd The object to handle.
     */
    public void setCallback(Object cmd)
    {
        if (cmd instanceof ImportException) exception = (ImportException) cmd;
        else if (cmd instanceof CmdCallback || cmd instanceof Boolean)
            callback = cmd;
        firePropertyChange(UPLOAD_DONE_PROPERTY, null, this);
    }

    /**
     * Sets the text of {@link #generalLabel}.
     * 
     * @param text The value to set.
     */
    public void setText(String text)
    {
        if (CommonsLangUtils.isEmpty(text)) {
            String value = generalLabel.getText();
            if (DEFAULT_TEXT.equals(value) || SCANNING_TEXT.equals(value))
                generalLabel.setText(text);
        } else generalLabel.setText(text);
    }

    /**
     * Displays message when saving rois.
     *
     * @param text The text displayed
     * @param completed Update progress bar.
     */
    public void updatePostProcessing(String text, boolean completed)
    {
        if (!completed) {
            processingBar.setMaximum(processingBar.getMaximum()+1);
            processingBar.setValue(processingBar.getValue()+1);
        } else {
            processingBar.setValue(processingBar.getMaximum());
        }
        processingBar.setString(text);
    }

    /** Marks the import has cancelled. */
    public void markedAsCancel()
    {
        generalLabel.setText(CANCEL_TEXT);
        this.markedAsCancel = true;
    }

    /**
     * Returns <code>true</code> if the import is marked as cancel,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isMarkedAsCancel() { return markedAsCancel; }

    /** Marks the import has duplicate. */
    public void markedAsDuplicate()
    {
        this.markedAsDuplicate = true;
        generalLabel.setText(DUPLICATE);
    }

    /**
     * Returns <code>true</code> if the import is marked as duplicate,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isMarkedAsDuplicate() { return markedAsDuplicate; }

    /**
     * Returns the text if an error occurred.
     * 
     * @return See above.
     */
    public String getErrorText() { return ""; }

    /**
     * Returns the source files that have checksum values or <code>null</code>
     * if no event stored.
     * 
     * @return See above.
     */
    public List<String> getChecksums()
    {
        if (!hasChecksum()) return null;
        return checksumEvent.checksums;
    }

    /**
     * Returns the checksum values or <code>null</code> if no event stored.
     * 
     * @return See above.
     */
    public Map<Integer, String> getFailingChecksums()
    {
        if (!hasChecksum()) return null;
        return checksumEvent.failingChecksums;
    }

    /**
     * Returns the source files that have checksum values or <code>null</code>
     * if no event stored.
     * 
     * @return See above.
     */
    public String[] getChecksumFiles()
    {
        if (!hasChecksum()) return null;
        return checksumEvent.srcFiles;
    }

    /** 
     * Returns <code>true</code> if the checksums have been calculated,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean hasChecksum() { return checksumEvent != null; }

    /**
     * Fires a property indicating to import the files.
     * 
     * @param files The file to handle.
     */
    public void setFiles(Map<File, StatusLabel> files)
    {
        if (isMarkedAsCancel()) return;
        generalLabel.setText(NO_FILES_TEXT);
        if (!CollectionUtils.isEmpty(files.entrySet())) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("Importing ");
            buffer.append(files.size());
            buffer.append(" file");
            if (files.size() > 1) buffer.append("s");
            generalLabel.setText(buffer.toString());
        }
        firePropertyChange(FILES_SET_PROPERTY, null, files);
    }

    /**
     * Indicates that the original container has been reset.
     */
    public void setNoContainer()
    {
        firePropertyChange(NO_CONTAINER_PROPERTY,
                Boolean.valueOf(false), Boolean.valueOf(true));
    }

    /**
     * Sets the container corresponding to the folder.
     * 
     * @param container The container to set.
     */
    public void setContainerFromFolder(DataObject container)
    {
        firePropertyChange(CONTAINER_FROM_FOLDER_PROPERTY, null, container);
    }

    /**
     * Replaces the initial file by the specified one. This should only be
     * invoked if the original file was an arbitrary one requiring to use the
     * import candidate e.g. <code>.log</code>.
     * 
     * @param file The new file.
     */
    public void resetFile(File file)
    {
        firePropertyChange(FILE_RESET_PROPERTY, null, file);
    }

    /**
     * Returns the number of series.
     * 
     * @return See above.
     */
    public int getSeriesCount() { return seriesCount; }

    /**
     * Returns <code>true</code> if the import can be cancelled,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isCancellable() { return cancellable; }

    /**
     * Returns the result of the import either a collection of
     * <code>PixelsData</code> or an exception.
     * 
     * @return See above.
     */
    public Object getImportResult()
    {
        if (exception != null) return exception;
        if (pixels != null) return pixels;
        return callback;
    }

    /**
     * Returns the number of pixels objects created or <code>0</code>.
     * 
     * @return See above.
     */
    public int getNumberOfImportedFiles()
    {
        if (pixels != null) return pixels.size();
        return 0;
    }

    /**
     * Returns the size of the upload.
     * 
     * @return See above.
     */
    public long getFileSize() { return sizeUpload; }

    /**
     * Returns the ID associated to the log file.
     * 
     * @return See above.
     */
    public long getLogFileID() { return logFileID; }

    /**
     * Returns <code>true</code> if the upload ever started, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    public boolean didUploadStart() { return uploadStarted; }

    /**
     * Returns the container.
     *
     * @return See above.
     */
    public ImportContainer getImportContainer() { return ic; }

    /**
     * Sets the import container.
     *
     * @param ic The value to set.
     */
    public void setImportContainer(ImportContainer ic) { this.ic = ic; }

    /**
     * Displays the status of an on-going import.
     * @see IObserver#update(IObservable, ImportEvent)
     */
    public void update(IObservable observable, ImportEvent event)
    {
        if (event == null) return;
        cancellable = false;
        if (event instanceof ImportEvent.IMPORT_DONE) {
            step = 6;
            processingBar.setValue(step);
            processingBar.setString(STEPS.get(step));
            pixels = (Set<PixelsData>) PojoMapper.asDataObjects(
                    ((ImportEvent.IMPORT_DONE) event).pixels);
            firePropertyChange(IMPORT_DONE_PROPERTY, null, this);
        } else if (event instanceof ImportCandidates.SCANNING) {
            if (!markedAsCancel) cancellable = true;
            if (!markedAsCancel && exception == null)
                generalLabel.setText(SCANNING_TEXT);
            if (exception == null)
                firePropertyChange(SCANNING_PROPERTY, null, this);
        } else if (event instanceof ErrorHandler.MISSING_LIBRARY) {
            exception = new ImportException(ImportException.MISSING_LIBRARY_TEXT,
                    ((ErrorHandler.MISSING_LIBRARY) event).exception);
            handleProcessingError(ImportException.MISSING_LIBRARY_TEXT, false);
        } else if (event instanceof ErrorHandler.UNKNOWN_FORMAT) {
            exception = new ImportException(ImportException.UNKNOWN_FORMAT_TEXT,
                    ((ErrorHandler.UNKNOWN_FORMAT) event).exception);
            if (sourceFile != null && !sourceFile.isDirectory())
                handleProcessingError(ImportException.UNKNOWN_FORMAT_TEXT, true);
        } else if (event instanceof ErrorHandler.FILE_EXCEPTION) {
            ErrorHandler.FILE_EXCEPTION e = (ErrorHandler.FILE_EXCEPTION) event;
            exception = new ImportException(e.exception);
            String text = ImportException.FILE_NOT_VALID_TEXT;
            if (sourceFile != null && sourceFile.isDirectory()) text = "";
            handleProcessingError(text, false);
        } else if (event instanceof ErrorHandler.INTERNAL_EXCEPTION) {
            ErrorHandler.INTERNAL_EXCEPTION e =
                    (ErrorHandler.INTERNAL_EXCEPTION) event;
            exception = new ImportException(e.exception);
            handleProcessingError("", true);
        }  else if (event instanceof ImportEvent.FILE_UPLOAD_BYTES) {
            ImportEvent.FILE_UPLOAD_BYTES e =
                    (ImportEvent.FILE_UPLOAD_BYTES) event;
            long v = totalUploadedSize+e.uploadedBytes;
            if (sizeUpload != 0) {
                uploadBar.setValue((int) (v*MAX/sizeUpload));
            }
            StringBuffer buffer = new StringBuffer();
            if (v != sizeUpload) buffer.append(formatUpload(v));
            else  buffer.append(fileSize);
            buffer.append(" ");
            if (e.timeLeft != 0) {
                String s = UIUtilities.calculateHMSFromMilliseconds(e.timeLeft,
                        true);
                buffer.append(s);
                if (CommonsLangUtils.isNotBlank(s)) buffer.append(" Left");
                else buffer.append("complete");
            }
            uploadBar.setString(buffer.toString());
        } else if (event instanceof ImportEvent.FILE_UPLOAD_COMPLETE) {
            ImportEvent.FILE_UPLOAD_COMPLETE e =
                    (ImportEvent.FILE_UPLOAD_COMPLETE) event;
            totalUploadedSize += e.uploadedBytes;
        } else if (event instanceof ImportEvent.FILESET_UPLOAD_END) {
            checksumEvent = (ImportEvent.FILESET_UPLOAD_END) event;
            if (exception == null) {
                step = 1;
                processingBar.setValue(step);
                processingBar.setString(STEPS.get(step));
            }
        } else if (event instanceof ImportEvent.METADATA_IMPORTED) {
            step = 2;
            processingBar.setValue(step);
            processingBar.setString(STEPS.get(step));
        } else if (event instanceof ImportEvent.PIXELDATA_PROCESSED) {
            step = 3;
            processingBar.setValue(step);
            processingBar.setString(STEPS.get(step));
        } else if (event instanceof ImportEvent.THUMBNAILS_GENERATED) {
            step = 4;
            processingBar.setValue(step);
            processingBar.setString(STEPS.get(step));
        } else if (event instanceof ImportEvent.METADATA_PROCESSED) {
            step = 5;
            processingBar.setValue(step);
            processingBar.setString(STEPS.get(step));
        } else if (event instanceof ImportEvent.FILESET_UPLOAD_START) {
            uploadStarted = true;
            Iterator<JLabel> i = labels.iterator();
            while (i.hasNext()) {
                i.next().setVisible(true);
            }
            generalLabel.setText("");
            uploadBar.setVisible(true);
            processingBar.setVisible(true);
            firePropertyChange(FILE_IMPORT_STARTED_PROPERTY, null, this);
        } else if (event instanceof ImportEvent.FILESET_UPLOAD_PREPARATION) {
            generalLabel.setText("Preparing upload...");
        } else if (event instanceof ImportEvent.IMPORT_STARTED) {
            ImportEvent.IMPORT_STARTED e =
                    (ImportEvent.IMPORT_STARTED) event;
            if (e.logFileId != null) {
                logFileID = e.logFileId;
            }
        } else if (event instanceof ImportEvent.POST_UPLOAD_EVENT) {
            ImportEvent.POST_UPLOAD_EVENT e =
                    (ImportEvent.POST_UPLOAD_EVENT) event;
            ic = e.container;
            
        }
    }

}
