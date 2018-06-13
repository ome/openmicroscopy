package org.openmicroscopy.shoola.agents.fsimporter.util;

import java.awt.Color;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;

import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.FileAnnotationData;

import org.openmicroscopy.shoola.env.data.model.FileObject;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.util.Status;
import org.openmicroscopy.shoola.util.file.ImportErrorObject;

public interface FileImportComponentI {

    /** Indicates that the container is of type <code>Project</code>. */
    public static final int PROJECT_TYPE = 0;
    /** Indicates that the container is of type <code>Screen</code>. */
    public static final int SCREEN_TYPE = 1;
    /** Indicates that the container is of type <code>Dataset</code>. */
    public static final int DATASET_TYPE = 2;
    /** Indicates that no container specified. */
    public static final int NO_CONTAINER = 3;
    /** Bound property indicating to retry an upload.*/
    public static final String RETRY_PROPERTY = "retry";
    /** 
     * Bound property indicating that the error to submit is selected or not.
     */
    public static final String SUBMIT_ERROR_PROPERTY = "submitError";
    /** Bound property indicating to display the error.*/
    public static final String DISPLAY_ERROR_PROPERTY = "displayError";
    /** Bound property indicating to cancel the import.*/
    public static final String CANCEL_IMPORT_PROPERTY = "cancelImport";
    /** Bound property indicating to browse the node. */
    public static final String BROWSE_PROPERTY = "browse";
    /** Bound property indicating to increase the number of files to import. */
    public static final String IMPORT_FILES_NUMBER_PROPERTY = "importFilesNumber";
    /**
     * Bound property indicating to load the content of the log file.
     */
    public static final String LOAD_LOGFILEPROPERTY = "loadLogfile";
    /**
     * Bound property indicating to retrieve the log file.
     */
    public static final String RETRIEVE_LOGFILEPROPERTY = "retrieveLogfile";
    /**
     * Bound property indicating to show the checksums,
     */
    public static final String CHECKSUM_DISPLAY_PROPERTY = "checksumDisplay";
    /** The number of extra labels for images to add. */
    public static final int MAX_THUMBNAILS = 3;

    /**
     * Returns the file hosted by this component.
     * 
     * @return See above.
     */
    public abstract FileObject getFile();

    /**
     * Returns the file hosted by this component.
     * 
     * @return See above.
     */
    public abstract FileObject getOriginalFile();

    /**
     * Sets the location where to import the files.
     * 
     * @param data The data where to import the folder or screening data.
     * @param dataset The dataset if any.
     * @param refNode The node of reference.
     */
    public abstract void setLocation(DataObject data, DatasetData dataset,
            Object refNode);

    /**
     * Sets the log file annotation.
     * 
     * @param data The annotation associated to the file set.
     * @param id The id of the file set.
     */
    public abstract void setImportLogFile(Collection<FileAnnotationData> data,
            long id);

    /**
     * Returns the dataset or <code>null</code>.
     * 
     * @return See above.
     */
    public abstract DatasetData getDataset();

    /**
     * Returns the object or <code>null</code>.
     * 
     * @return See above.
     */
    public abstract DataObject getDataObject();

    /**
     * Returns <code>true</code> if the parent is set.
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public abstract boolean hasParent();

    /**
     * Returns the components displaying the status of an on-going import.
     * 
     * @return See above.
     */
    public abstract Status getStatus();

    /**
     * Sets the result of the import.
     * @param image The image.
     */
    public abstract void setStatus(Object image);

    /**
     * Returns the files that failed to import.
     * 
     * @return See above.
     */
    public abstract List<FileImportComponentI> getImportErrors();

    /**
     * Returns the id of the group.
     * 
     * @return See above.
     */
    public abstract long getGroupID();

    /**
     * Returns the id of the experimenter.
     * 
     * @return See above.
     */
    public abstract long getExperimenterID();

    /**
     * Returns the import error object.
     * 
     * @return See above.
     */
    public abstract ImportErrorObject getImportErrorObject();

    /**
     * Returns <code>true</code> if the import has failed, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    public abstract boolean hasImportFailed();

    /**
     * Returns <code>true</code> if it was a failure prior or during the
     * upload, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public abstract boolean hasUploadFailed();

    /**
     * Returns <code>true</code> if the import has been cancelled,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public abstract boolean isCancelled();

    /**
     * Returns <code>true</code> if the component has imports to cancel,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public abstract boolean hasImportToCancel();

    /**
     * Returns <code>true</code> if the file can be re-imported,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public abstract boolean hasFailuresToReimport();

    /**
     * Returns <code>true</code> if the file can be re-imported,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public abstract boolean hasFailuresToReupload();

    /**
     * Returns <code>true</code> if the import has failed, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    public abstract boolean hasImportStarted();

    /**
     * Returns <code>true</code> the error can be submitted, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    public abstract boolean hasFailuresToSend();

    /**
     * Returns <code>true</code> if the folder has components added,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public abstract boolean hasComponents();

    /**
     * Returns the status of the import process one of the
     * values defined in @see ImportStatus
     * 
     * @return See above.
     */
    public abstract ImportStatus getImportStatus();

    /**
     * Returns <code>true</code> if refresh whole tree, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    public abstract boolean hasToRefreshTree();

    /**
     * Returns <code>true</code> if some files were imported, 
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public abstract boolean toRefresh();

    /** Indicates the import has been cancelled. */
    public abstract void cancelLoading();

    /**
     * Sets the type. 
     * 
     * @param type One of the constants defined by this class.
     */
    public abstract void setType(int type);

    /**
     * Returns the supported type. One of the constants defined by this class.
     * 
     * @return See above.
     */
    public abstract int getType();

    /**
     * Returns <code>true</code> if the folder has been converted into a
     * container, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public abstract boolean isFolderAsContainer();

    /**
     * Returns the object corresponding to the folder.
     * 
     * @return See above.
     */
    public abstract DataObject getContainerFromFolder();

    /**
     * Returns <code>true</code> if the file has already been marked for
     * re-import, <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public abstract List<FileImportComponentI> getFilesToReupload();

    /**
     * Sets to <code>true</code> to mark the file for reimport.
     * <code>false</code> otherwise.
     * 
     * @param reimported Pass <code>true</code> to mark the file for reimport,
     * <code>false</code> otherwise.
     */
    public abstract void setReimported(boolean reimported);

    /**
     * Sets the result of the import for the specified file.
     * 
     * @param result The result.
     */
    public abstract void uploadComplete(Object result);

    /**
     * Returns the index associated to the main component.
     * 
     * @return See above.
     */
    public abstract int getIndex();

    /**
     * Returns the result of the import either a collection of
     * <code>PixelsData</code> or an exception.
     * 
     * @return See above.
     */
    public abstract Object getImportResult();

    /**
     * Returns <code>true</code> if it is a HCS file, <code>false</code>
     * otherwise.
     * 
     * @return See above.
     */
    public abstract boolean isHCS();

    /**
     * Returns the size of the upload.
     * 
     * @return See above.
     */
    public abstract long getImportSize();

    /**
     * Returns <code>true</code> if the result has already been set,
     * <code>false</code> otherwise.
     * 
     * @return See above.
     */
    public abstract boolean hasResult();

    /**
     * Returns the importable object associated to the parent,
     * <code>null</code> if no parent.
     * 
     * @return See above.
     */
    public abstract ImportableFile getImportableFile();

    /**
     * Indicates the results saving status.
     *
     * @param message The message to display
     * @param busy Pass <code>true</code> when saving,
     *             <code>false</code> otherwise.
     */
    public abstract void onResultsSaving(String message, boolean busy);

    /**
     * Listens to property fired by the <code>StatusLabel</code>.
     * @see PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public abstract void propertyChange(PropertyChangeEvent evt);

    /**
     * Returns the name of the file and group's id and user's id.
     * @see #toString()
     */
    public abstract String toString();

}