/*
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.ImportEvent.FILESET_UPLOAD_END;
import ome.formats.importer.util.ErrorHandler;
import omero.cmd.CmdCallback;
import omero.gateway.model.DataObject;
import omero.gateway.model.FilesetData;
import omero.gateway.model.PixelsData;
import omero.gateway.util.PojoMapper;

import org.apache.commons.io.FileUtils;
import org.openmicroscopy.shoola.env.data.ImportException;
import org.openmicroscopy.shoola.env.data.model.FileObject;
import org.openmicroscopy.shoola.util.CommonsLangUtils;
import org.openmicroscopy.shoola.util.ui.UIUtilities;

/**
 * Component tracking the status of a specific import.
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class Status implements IObserver {

    /** The text displayed when the file is already selected. */
    public static final String DUPLICATE = "Already processed, skipping";

    /** The text indicating the scanning steps. */
    public static final String SCANNING_TEXT = "Scanning...";

    /** Pass through of IObserver events **/
    public static final String IMPORT_EVENT = "importEvenPassThrough";
    
    /**
     * Bound property indicating that the original container has been reset.
     * */
    public static final String NO_CONTAINER_PROPERTY = "noContainer";

    /** Bound property indicating that children files have been set. */
    public static final String FILES_SET_PROPERTY = "filesSet";

    /**
     * Bound property indicating that the file has to be reset This should be
     * invoked if the log file for example has been selected.
     */
    public static final String FILE_RESET_PROPERTY = "fileReset";

    /** Bound property indicating that the import of the file has started. */
    public static final String FILE_IMPORT_STARTED_PROPERTY = "fileImportStarted";

    /**
     * Bound property indicating that the container corresponding to the folder
     * has been created.
     * */
    public static final String CONTAINER_FROM_FOLDER_PROPERTY = "containerFromFolder";

    /** Bound property indicating that the status has changed. */
    public static final String CANCELLABLE_IMPORT_PROPERTY = "cancellableImport";

    /** Bound property indicating that the debug text has been sent. */
    public static final String DEBUG_TEXT_PROPERTY = "debugText";

    /** Bound property indicating that the import is done. */
    public static final String IMPORT_DONE_PROPERTY = "importDone";

    /** Bound property indicating that the upload is done. */
    public static final String UPLOAD_DONE_PROPERTY = "uploadDone";

    /** Bound property indicating that the scanning has started. */
    public static final String SCANNING_PROPERTY = "scanning";

    /** Bound property indicating that the scanning has started. */
    public static final String PROCESSING_ERROR_PROPERTY = "processingError";

    /** The default text of the component. */
    public static final String DEFAULT_TEXT = "Pending...";

    /**
     * The number of processing sets. 1. Importing Metadata 2. Processing Pixels
     * 3. Generating Thumbnails 4. Processing Metadata 5. Generating Objects
     */
    /** Map hosting the description of each step. */
    public static final Map<Integer, String> STEPS;

    /** Map hosting the description of the failure at a each step. */
    public static final Map<Integer, String> STEP_FAILURES;

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

    /** The container. */
    private ImportContainer ic;

    /** The number of images in a series. */
    private int seriesCount;

    /** Flag indicating that the import has been cancelled. */
    private boolean markedAsCancel;

    /** Flag indicating that the import can or not be cancelled. */
    private boolean cancellable;

    /**
     * Flag indicating that the file has already been imported or already in the
     * queue.
     */
    private boolean markedAsDuplicate;

    /** The size of the file. */
    private String fileSize;

    /** The size units. */
    private String units;

    /** The total size of uploaded files. */
    private long totalUploadedSize;

    /** The size of the upload, */
    private long sizeUpload;

    /** Checksum event stored for later retrieval */
    private FILESET_UPLOAD_END checksumEvent;

    /** The exception if an error occurred. */
    private ImportException exception;

    /** The list of pixels' identifiers returned when the import is complete. */
    private Collection<PixelsData> pixels;

    /** The file associated to that import. */
    private FilesetData fileset;

    /** The callback. This should only be set when importing a directory. */
    private Object callback;

    /** Indicates that the file scanned is a directory. */
    // private boolean directory;

    /** The id of the log file. */
    private long logFileID;

    /** The processing step. */
    private int step;

    /** Indicates if the upload ever started. */
    private boolean uploadStarted;

    /** The file or folder this component is for. */
    private FileObject sourceFile;

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void firePropertyChange(String name, Object oldValue, Object newValue) {
        this.pcs.firePropertyChange(name, oldValue, newValue);
    }

    /**
     * Formats the size of the uploaded data.
     * 
     * @param value
     *            The value to display.
     * @return See above.
     */
    private String formatUpload(long value) {
        StringBuffer buffer = new StringBuffer();
        String v = FileUtils.byteCountToDisplaySize(value);
        String[] values = v.split(" ");
        if (values.length > 1) {
            String u = values[1];
            if (units.equals(u))
                buffer.append(values[0]);
            else
                buffer.append(v);
        } else
            buffer.append(v);
        buffer.append("/");
        buffer.append(fileSize);
        return buffer.toString();
    }

    /** Initializes the components. */
    private void initialize() {
        step = 0;
        sizeUpload = 0;
        fileSize = "";
        seriesCount = 0;
        markedAsCancel = false;
        cancellable = true;
        totalUploadedSize = 0;
    }

    /**
     * Handles error that occurred during the processing.
     * 
     * @param text
     *            The text to display if any.
     * @param fire
     *            Indicate to fire a property.
     */
    private void handleProcessingError(String text, boolean fire) {
        if (isMarkedAsCancel())
            return;
        cancellable = false;
    }

    /**
     * Creates a new instance.
     * 
     * @param sourceFile
     *            The file associated to that label.
     */
    public Status(FileObject sourceFile) {
        this.sourceFile = sourceFile;
        initialize();
    }

    /**
     * Sets the file set when the upload is complete. To be modified.
     * 
     * @param fileset
     *            The value to set.
     */
    public void setFilesetData(final FilesetData fileset) {
        this.fileset = fileset;
    }

    /**
     * Returns <code>true</code> if it is a HCS file, <code>false</code>
     * otherwise.
     *
     * @return See above.
     */
    public boolean isHCS() {
        if (ic == null)
            return false;
        Boolean b = ic.getIsSPW();
        if (b == null)
            return false;
        return b.booleanValue();
    }

    /**
     * Returns the file set associated to the import.
     * 
     * @return See above.
     */
    public FilesetData getFileset() {
        return fileset;
    }

    /**
     * Sets the collection of files to import.
     * 
     * @param usedFiles
     *            The value to set.
     */
    public void setUsedFiles(String[] usedFiles) {
        if (usedFiles == null)
            return;
        for (int i = 0; i < usedFiles.length; i++) {
            sizeUpload += (new File(usedFiles[i])).length();
        }
        fileSize = FileUtils.byteCountToDisplaySize(sizeUpload);
        String[] values = fileSize.split(" ");
        if (values.length > 1)
            units = values[1];
    }

    /**
     * Sets the callback. This method should only be invoked when the file is
     * imported from a folder.
     * 
     * @param cmd
     *            The object to handle.
     */
    public void setCallback(Object cmd) {
        if (cmd instanceof ImportException)
            exception = (ImportException) cmd;
        else if (cmd instanceof CmdCallback || cmd instanceof Boolean)
            callback = cmd;
        firePropertyChange(UPLOAD_DONE_PROPERTY, null, this);
    }

    /** Marks the import has cancelled. */
    public void markedAsCancel() {
        this.markedAsCancel = true;
    }

    /**
     * Returns <code>true</code> if the import is marked as cancel,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isMarkedAsCancel() {
        return markedAsCancel;
    }

    /** Marks the import has duplicate. */
    public void markedAsDuplicate() {
        this.markedAsDuplicate = true;
    }

    /**
     * Returns <code>true</code> if the import is marked as duplicate,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isMarkedAsDuplicate() {
        return markedAsDuplicate;
    }

    /**
     * Returns the text if an error occurred.
     * 
     * @return See above.
     */
    public String getErrorText() {
        return "";
    }

    /**
     * Returns the source files that have checksum values or <code>null</code>
     * if no event stored.
     * 
     * @return See above.
     */
    public List<String> getChecksums() {
        if (!hasChecksum())
            return null;
        return checksumEvent.checksums;
    }

    /**
     * Returns the checksum values or <code>null</code> if no event stored.
     * 
     * @return See above.
     */
    public Map<Integer, String> getFailingChecksums() {
        if (!hasChecksum())
            return null;
        return checksumEvent.failingChecksums;
    }

    /**
     * Returns the source files that have checksum values or <code>null</code>
     * if no event stored.
     * 
     * @return See above.
     */
    public String[] getChecksumFiles() {
        if (!hasChecksum())
            return null;
        return checksumEvent.srcFiles;
    }

    /**
     * Returns <code>true</code> if the checksums have been calculated,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean hasChecksum() {
        return checksumEvent != null;
    }

    /**
     * Fires a property indicating to import the files.
     * 
     * @param files
     *            The file to handle.
     */
    public void setFiles(Map<File, Status> files) {
        if (isMarkedAsCancel())
            return;
        firePropertyChange(FILES_SET_PROPERTY, null, files);
    }

    /**
     * Indicates that the original container has been reset.
     */
    public void setNoContainer() {
        firePropertyChange(NO_CONTAINER_PROPERTY, Boolean.valueOf(false),
                Boolean.valueOf(true));
    }

    /**
     * Sets the container corresponding to the folder.
     * 
     * @param container
     *            The container to set.
     */
    public void setContainerFromFolder(DataObject container) {
        firePropertyChange(CONTAINER_FROM_FOLDER_PROPERTY, null, container);
    }

    /**
     * Replaces the initial file by the specified one. This should only be
     * invoked if the original file was an arbitrary one requiring to use the
     * import candidate e.g. <code>.log</code>.
     * 
     * @param file
     *            The new file.
     */
    public void resetFile(File file) {
        firePropertyChange(FILE_RESET_PROPERTY, null, file);
    }

    /**
     * Returns the number of series.
     * 
     * @return See above.
     */
    public int getSeriesCount() {
        return seriesCount;
    }

    /**
     * Returns <code>true</code> if the import can be cancelled,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public boolean isCancellable() {
        return cancellable;
    }

    /**
     * Returns the result of the import either a collection of
     * <code>PixelsData</code> or an exception.
     * 
     * @return See above.
     */
    public Object getImportResult() {
        if (exception != null)
            return exception;
        if (pixels != null)
            return pixels;
        return callback;
    }

    /**
     * Returns the number of pixels objects created or <code>0</code>.
     * 
     * @return See above.
     */
    public int getNumberOfImportedFiles() {
        if (pixels != null)
            return pixels.size();
        return 0;
    }

    /**
     * Returns the size of the upload.
     * 
     * @return See above.
     */
    public long getSizeUpload() {
        return sizeUpload;
    }

    /**
     * Returns the ID associated to the log file.
     * 
     * @return See above.
     */
    public long getLogFileID() {
        return logFileID;
    }

    /**
     * Returns <code>true</code> if the upload ever started, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    public boolean didUploadStart() {
        return uploadStarted;
    }

    /**
     * Returns the container.
     *
     * @return See above.
     */
    public ImportContainer getImportContainer() {
        return ic;
    }

    /**
     * Sets the import container.
     *
     * @param ic
     *            The value to set.
     */
    public void setImportContainer(ImportContainer ic) {
        this.ic = ic;
    }

    /**
     * Displays the status of an on-going import.
     * 
     * @see IObserver#update(IObservable, ImportEvent)
     */
    public void update(IObservable observable, ImportEvent event) {
        if (event == null)
            return;
        cancellable = false;
        if (event instanceof ImportEvent.IMPORT_DONE) {
            step = 6;
            pixels = PojoMapper
                    .<PixelsData> convertToDataObjects(((ImportEvent.IMPORT_DONE) event).pixels);
            firePropertyChange(IMPORT_DONE_PROPERTY, null, this);
        } else if (event instanceof ImportCandidates.SCANNING) {
            if (!markedAsCancel)
                cancellable = true;
            if (exception == null)
                firePropertyChange(SCANNING_PROPERTY, null, this);
        } else if (event instanceof ErrorHandler.MISSING_LIBRARY) {
            exception = new ImportException(
                    ImportException.MISSING_LIBRARY_TEXT,
                    ((ErrorHandler.MISSING_LIBRARY) event).exception);
            handleProcessingError(ImportException.MISSING_LIBRARY_TEXT, false);
        } else if (event instanceof ErrorHandler.UNKNOWN_FORMAT) {
            exception = new ImportException(
                    ImportException.UNKNOWN_FORMAT_TEXT,
                    ((ErrorHandler.UNKNOWN_FORMAT) event).exception);
            if (sourceFile != null && !sourceFile.isDirectory())
                handleProcessingError(ImportException.UNKNOWN_FORMAT_TEXT, true);
        } else if (event instanceof ErrorHandler.FILE_EXCEPTION) {
            ErrorHandler.FILE_EXCEPTION e = (ErrorHandler.FILE_EXCEPTION) event;
            exception = new ImportException(e.exception);
            String text = ImportException.FILE_NOT_VALID_TEXT;
            if (sourceFile != null && sourceFile.isDirectory())
                text = "";
            handleProcessingError(text, false);
        } else if (event instanceof ErrorHandler.INTERNAL_EXCEPTION) {
            ErrorHandler.INTERNAL_EXCEPTION e = (ErrorHandler.INTERNAL_EXCEPTION) event;
            exception = new ImportException(e.exception);
            handleProcessingError("", true);
        } else if (event instanceof ImportEvent.FILE_UPLOAD_BYTES) {
            ImportEvent.FILE_UPLOAD_BYTES e = (ImportEvent.FILE_UPLOAD_BYTES) event;
            long v = totalUploadedSize + e.uploadedBytes;
            StringBuffer buffer = new StringBuffer();
            if (v != sizeUpload)
                buffer.append(formatUpload(v));
            else
                buffer.append(fileSize);
            buffer.append(" ");
            if (e.timeLeft != 0) {
                String s = UIUtilities.calculateHMSFromMilliseconds(e.timeLeft,
                        true);
                buffer.append(s);
                if (CommonsLangUtils.isNotBlank(s))
                    buffer.append(" Left");
                else
                    buffer.append("complete");
            }
        } else if (event instanceof ImportEvent.FILE_UPLOAD_COMPLETE) {
            ImportEvent.FILE_UPLOAD_COMPLETE e = (ImportEvent.FILE_UPLOAD_COMPLETE) event;
            totalUploadedSize += e.uploadedBytes;
        } else if (event instanceof ImportEvent.FILESET_UPLOAD_END) {
            checksumEvent = (ImportEvent.FILESET_UPLOAD_END) event;
            if (exception == null) {
                step = 1;
            }
        } else if (event instanceof ImportEvent.METADATA_IMPORTED) {
            step = 2;
        } else if (event instanceof ImportEvent.PIXELDATA_PROCESSED) {
            step = 3;
        } else if (event instanceof ImportEvent.THUMBNAILS_GENERATED) {
            step = 4;
        } else if (event instanceof ImportEvent.METADATA_PROCESSED) {
            step = 5;
        } else if (event instanceof ImportEvent.FILESET_UPLOAD_START) {
            uploadStarted = true;
            firePropertyChange(FILE_IMPORT_STARTED_PROPERTY, null, this);
        } else if (event instanceof ImportEvent.IMPORT_STARTED) {
            ImportEvent.IMPORT_STARTED e = (ImportEvent.IMPORT_STARTED) event;
            if (e.logFileId != null) {
                logFileID = e.logFileId;
            }
        } else if (event instanceof ImportEvent.POST_UPLOAD_EVENT) {
            ImportEvent.POST_UPLOAD_EVENT e = (ImportEvent.POST_UPLOAD_EVENT) event;
            ic = e.container;

        }
        
        firePropertyChange(IMPORT_EVENT, null, event);
    }

    public String getUnits() {
        return units;
    }

    public String getFileSize() {
        return fileSize;
    }

    public int getStep() {
        return step;
    }

    public long getTotalUploadedSize() {
        return totalUploadedSize;
    }
    
}
