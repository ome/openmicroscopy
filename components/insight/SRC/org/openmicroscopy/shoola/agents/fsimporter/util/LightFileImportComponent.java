/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2016 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package org.openmicroscopy.shoola.agents.fsimporter.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import omero.cmd.CmdCallback;
import omero.cmd.CmdCallbackI;
import omero.gateway.SecurityContext;
import omero.gateway.model.DataObject;
import omero.gateway.model.DatasetData;
import omero.gateway.model.FileAnnotationData;
import omero.gateway.model.FilesetData;
import omero.gateway.model.PixelsData;
import omero.gateway.model.ProjectData;
import omero.gateway.model.ScreenData;
import omero.gateway.model.TagAnnotationData;

import org.apache.commons.collections.CollectionUtils;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImage;
import org.openmicroscopy.shoola.agents.events.iviewer.ViewImageObject;
import org.openmicroscopy.shoola.agents.fsimporter.ImporterAgent;
import org.openmicroscopy.shoola.agents.util.browser.TreeImageDisplay;
import org.openmicroscopy.shoola.agents.util.ui.ThumbnailLabel;
import org.openmicroscopy.shoola.env.data.ImportException;
import org.openmicroscopy.shoola.env.data.model.FileObject;
import org.openmicroscopy.shoola.env.data.model.ImportableFile;
import org.openmicroscopy.shoola.env.data.util.Status;
import org.openmicroscopy.shoola.env.event.EventBus;
import org.openmicroscopy.shoola.util.file.ImportErrorObject;

/**
 * Component hosting the file to import and displaying the status of the import
 * process.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @author Blazej Pindelski, bpindelski at dundee.ac.uk
 * @version 3.0
 * @since 3.0-Beta4
 */
public class LightFileImportComponent implements PropertyChangeListener,
        FileImportComponentI {

    /** Text indicating that the folder does not contain importable files. */
    private static final String EMPTY_FOLDER = "No data to import";

    /** The maximum width used for the component. */
    private static final int LENGTH = 350;

    /**
     * private static final String EMPTY_DIRECTORY = "No data to import";
     * 
     * /** One of the constants defined by this class.
     */
    private int type;

    /** The imported image. */
    private Object image;

    /** Set to <code>true</code> if attempt to re-import. */
    private boolean reimported;

    /** Indicates the status of the on-going import. */
    private Status status;

    /** Keep tracks of the components. */
    private Map<File, LightFileImportComponent> components;

    /** The data object corresponding to the folder. */
    private DataObject containerFromFolder;

    /** The node where to import the folder. */
    private DataObject data;

    /** The dataset if any. */
    private DatasetData dataset;

    /** The node of reference if any. */
    private Object refNode;

    /** The object where the data have been imported. */
    private DataObject containerObject;

    /** The parent of the node. */
    private FileImportComponentI parent;

    /** The state of the import */
    private ImportStatus resultIndex;

    /** The index associated to the main component. */
    private int index;

    /** Reference to the callback. */
    private CmdCallback callback;

    /** The importable object. */
    private ImportableFile importable;

    /** The collection of tags added to the imported images. */
    private Collection<TagAnnotationData> tags;

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private void firePropertyChange(String name, Object oldValue,
            Object newValue) {
        this.pcs.firePropertyChange(name, oldValue, newValue);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
         this.pcs.addPropertyChangeListener(listener);
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        return this.pcs.getPropertyChangeListeners();
    }

    /** Indicates that the import was successful or if it failed. */
    private void formatResult() {
        if (callback != null) {
            try {
                ((CmdCallbackI) callback).close(true);
            } catch (Exception e) {
            }
        }
        Object result = status.getImportResult();
        if (image instanceof ImportException)
            result = image;
        if (result instanceof ImportException) {
            ImportException e = (ImportException) result;
            int status = e.getStatus();
            if (status == ImportException.CHECKSUM_MISMATCH)
                resultIndex = ImportStatus.UPLOAD_FAILURE;
            else if (status == ImportException.MISSING_LIBRARY)
                resultIndex = ImportStatus.FAILURE_LIBRARY;
            else
                resultIndex = ImportStatus.FAILURE;
        } else if (result instanceof CmdCallback) {
            callback = (CmdCallback) result;
        } else {
            resultIndex = ImportStatus.SUCCESS;
        }
    }

    /**
     * Indicates that the file will not be imported.
     * 
     * @param fire
     *            Pass <code>true</code> to fire a property, <code>false</code>
     *            otherwise.
     */
    private void cancel(boolean fire) {
        boolean b = status.isCancellable() || getFile().isDirectory();
        if (!isCancelled() && !hasImportFailed() && b
                && !status.isMarkedAsDuplicate()) {
            status.markedAsCancel();
            firePropertyChange(CANCEL_IMPORT_PROPERTY, null, this);
        }
    }

    /** Initializes the components. */
    private void initComponents() {
        status = new Status(importable.getFile());
        status.addPropertyChangeListener(this);
        image = null;
    }

    /**
     * Attaches the listeners to the newly created component.
     * 
     * @param c
     *            The component to handle.
     */
    private void attachListeners(LightFileImportComponent c) {
        PropertyChangeListener[] listeners = getPropertyChangeListeners();
        if (listeners != null && listeners.length > 0) {
            for (int j = 0; j < listeners.length; j++) {
                c.addPropertyChangeListener(listeners[j]);
            }
        }
    }

    /**
     * Adds the specified files to the list of import data.
     * 
     * @param files
     *            The files to import.
     */
    private void insertFiles(Map<File, Status> files) {
        resultIndex = ImportStatus.SUCCESS;
        if (files == null || files.size() == 0)
            return;
        components = Collections
                .synchronizedMap(new HashMap<File, LightFileImportComponent>());

        Entry<File, Status> entry;
        Iterator<Entry<File, Status>> i = files.entrySet().iterator();
        LightFileImportComponent c;
        File f;
        DatasetData d = dataset;
        Object node = refNode;
        if (importable.isFolderAsContainer()) {
            node = null;
            d = new DatasetData();
            d.setName(getFile().getName());
        }
        ImportableFile copy;
        while (i.hasNext()) {
            entry = i.next();
            f = entry.getKey();
            copy = importable.copy();
            copy.setFile(f);
            c = new LightFileImportComponent(copy, getIndex(), tags);
            if (f.isFile()) {
                c.setLocation(data, d, node);
                c.setParent(this);
            }
            c.setType(getType());
            attachListeners(c);
            c.setStatusLabel(entry.getValue());
            entry.getValue().addPropertyChangeListener(this);
            components.put((File) entry.getKey(), c);
        }
    }

    /**
     * Creates a new instance.
     * 
     * @param importable
     *            The component hosting information about the file.
     * @param browsable
     *            Flag indicating that the container can be browsed or not.
     * @param singleGroup
     *            Passes <code>true</code> if the user is member of only one
     *            group, <code>false</code> otherwise.
     * @param index
     *            The index of the parent.
     * @param tags
     *            The tags that will be linked to the objects.
     */
    public LightFileImportComponent(ImportableFile importable, int index,
            Collection<TagAnnotationData> tags) {
        if (importable == null)
            throw new IllegalArgumentException("No file specified.");
        if (importable.getGroup() == null)
            throw new IllegalArgumentException("No group specified.");
        this.index = index;
        this.tags = tags;
        this.importable = importable;
        resultIndex = ImportStatus.QUEUED;
        initComponents();
        setLocation(importable.getParent(), importable.getDataset(),
                importable.getRefNode());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #getFile()
     */
    @Override
    public FileObject getFile() {
        return importable.getFile();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #getOriginalFile()
     */
    @Override
    public FileObject getOriginalFile() {
        return importable.getOriginalFile();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #setLocation(omero.gateway.model.DataObject,
     * omero.gateway.model.DatasetData, java.lang.Object)
     */
    @Override
    public void setLocation(DataObject data, DatasetData dataset, Object refNode) {
        this.data = data;
        this.dataset = dataset;
        this.refNode = refNode;
        if (refNode != null && refNode instanceof TreeImageDisplay) {
            TreeImageDisplay n = (TreeImageDisplay) refNode;
            Object ho = n.getUserObject();
            if (ho instanceof DatasetData || ho instanceof ProjectData
                    || ho instanceof ScreenData) {
                containerObject = (DataObject) ho;
            }
            return;
        }
        if (dataset != null) {
            containerObject = dataset;
            return;
        }
        if (data != null && data instanceof ScreenData) {
            containerObject = data;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #setImportLogFile(java.util.Collection, long)
     */
    @Override
    public void setImportLogFile(Collection<FileAnnotationData> data, long id) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #getDataset()
     */
    @Override
    public DatasetData getDataset() {
        return dataset;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #getDataObject()
     */
    @Override
    public DataObject getDataObject() {
        return data;
    }

    /**
     * Replaces the initial status label.
     * 
     * @param label
     *            The value to replace.
     */
    void setStatusLabel(Status label) {
        status = label;
        status.addPropertyChangeListener(this);
    }

    /**
     * Sets the parent of the component.
     * 
     * @param parent
     *            The value to set.
     */
    void setParent(FileImportComponentI parent) {
        this.parent = parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #hasParent()
     */
    @Override
    public boolean hasParent() {
        return parent != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #getStatus()
     */
    @Override
    public Status getStatus() {
        return status;
    }

    /**
     * Returns the associated file if any.
     *
     * @param series
     *            See above.
     * @return See above.
     */
    private FileObject getAssociatedFile(int series) {
        List<FileObject> l = getFile().getAssociatedFiles();
        Iterator<FileObject> i = l.iterator();
        FileObject f;
        while (i.hasNext()) {
            f = i.next();
            if (f.getIndex() == series) {
                return f;
            }
        }
        return null;
    }

    /**
     * Returns <code>true</code> if the file has some associated files,
     * <code>false</code> otherwise.
     *
     * @return See above.
     */
    private boolean hasAssociatedFiles() {
        List<FileObject> l = getFile().getAssociatedFiles();
        return CollectionUtils.isNotEmpty(l);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #setStatus(java.lang.Object)
     */
    @Override
    public void setStatus(Object image) {
        this.image = image;
        if (image instanceof Collection) {
            Collection<?> c = (Collection) image;
            if (!c.isEmpty()) {
                Object obj = c.iterator().next();
                if (obj instanceof PixelsData) {
                    // Result from the import itself
                    this.image = null;
                    formatResult();
                }
            }
        } else if (image instanceof ImportException) {
            if (getFile().isDirectory()) {
                this.image = null;
            } else
                formatResult();
        } else if (image instanceof Boolean) {
            if (status.isMarkedAsCancel() || status.isMarkedAsDuplicate()) {
                resultIndex = ImportStatus.IGNORED;
                this.image = null;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #getImportErrors()
     */
    @Override
    public List<FileImportComponentI> getImportErrors() {
        List<FileImportComponentI> l = null;
        if (getFile().isFile()) {
            Object r = status.getImportResult();
            if (r instanceof Exception || image instanceof Exception) {
                l = new ArrayList<FileImportComponentI>();
                l.add(this);
                return l;
            }
        } else {
            if (components != null) {
                Collection<LightFileImportComponent> values = components
                        .values();
                synchronized (components) {
                    Iterator<LightFileImportComponent> i = values.iterator();
                    FileImportComponentI fc;
                    l = new ArrayList<FileImportComponentI>();
                    List<FileImportComponentI> list;
                    while (i.hasNext()) {
                        fc = i.next();
                        list = fc.getImportErrors();
                        if (!CollectionUtils.isEmpty(list))
                            l.addAll(list);
                    }
                }
            }
        }
        return l;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #getGroupID()
     */
    @Override
    public long getGroupID() {
        return importable.getGroup().getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #getExperimenterID()
     */
    @Override
    public long getExperimenterID() {
        return importable.getUser().getId();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #getImportErrorObject()
     */
    @Override
    public ImportErrorObject getImportErrorObject() {
        Object r = status.getImportResult();
        Exception e = null;
        if (r instanceof Exception)
            e = (Exception) r;
        else if (image instanceof Exception)
            e = (Exception) image;
        if (e == null)
            return null;
        ImportErrorObject object = new ImportErrorObject(getFile()
                .getTrueFile(), e, getGroupID());
        object.setImportContainer(status.getImportContainer());
        long id = status.getLogFileID();
        if (id <= 0) {
            FilesetData data = status.getFileset();
            if (data != null) {
                id = data.getId();
                object.setRetrieveFromAnnotation(true);
            }
        }
        object.setLogFileID(id);
        return object;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #hasImportFailed()
     */
    @Override
    public boolean hasImportFailed() {
        return resultIndex == ImportStatus.FAILURE
                || resultIndex == ImportStatus.UPLOAD_FAILURE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #hasUploadFailed()
     */
    @Override
    public boolean hasUploadFailed() {
        return resultIndex == ImportStatus.UPLOAD_FAILURE
                || (resultIndex == ImportStatus.FAILURE && !status
                        .didUploadStart());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #isCancelled()
     */
    @Override
    public boolean isCancelled() {
        boolean b = status.isMarkedAsCancel();
        if (b || getFile().isFile())
            return b;
        if (components == null)
            return false;
        Collection<LightFileImportComponent> values = components.values();
        synchronized (components) {
            Iterator<LightFileImportComponent> i = values.iterator();
            while (i.hasNext()) {
                if (i.next().isCancelled())
                    return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #hasImportToCancel()
     */
    @Override
    public boolean hasImportToCancel() {
        boolean b = status.isMarkedAsCancel();
        if (b)
            return false;
        if (getFile().isFile() && !hasImportStarted())
            return true;
        if (components == null)
            return false;
        Collection<LightFileImportComponent> values = components.values();
        synchronized (components) {
            Iterator<LightFileImportComponent> i = values.iterator();
            FileImportComponentI fc;
            while (i.hasNext()) {
                fc = i.next();
                if (!fc.isCancelled() && !fc.hasImportStarted())
                    return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #hasFailuresToReimport()
     */
    @Override
    public boolean hasFailuresToReimport() {
        if (getFile().isFile())
            return hasUploadFailed() && !reimported;
        if (components == null)
            return false;
        Collection<LightFileImportComponent> values = components.values();
        synchronized (components) {
            Iterator<LightFileImportComponent> i = values.iterator();
            while (i.hasNext()) {
                if (i.next().hasUploadFailed())
                    return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #hasFailuresToReupload()
     */
    @Override
    public boolean hasFailuresToReupload() {
        if (getFile().isFile())
            return hasUploadFailed() && !reimported;
        if (components == null)
            return false;
        Collection<LightFileImportComponent> values = components.values();
        synchronized (components) {
            Iterator<LightFileImportComponent> i = values.iterator();
            while (i.hasNext()) {
                if (i.next().hasFailuresToReupload())
                    return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #hasImportStarted()
     */
    @Override
    public boolean hasImportStarted() {
        if (getFile().isFile())
            return resultIndex != ImportStatus.QUEUED;
        if (components == null)
            return false;
        Collection<LightFileImportComponent> values = components.values();
        int count = 0;
        synchronized (components) {
            Iterator<LightFileImportComponent> i = values.iterator();
            while (i.hasNext()) {
                if (i.next().hasImportStarted())
                    count++;
            }
        }
        return count == components.size();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #hasFailuresToSend()
     */
    @Override
    public boolean hasFailuresToSend() {
        if (getFile().isFile())
            return resultIndex == ImportStatus.FAILURE;
        if (components == null)
            return false;
        Collection<LightFileImportComponent> values = components.values();
        synchronized (components) {
            Iterator<LightFileImportComponent> i = values.iterator();
            while (i.hasNext()) {
                if (i.next().hasFailuresToSend())
                    return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #hasComponents()
     */
    @Override
    public boolean hasComponents() {
        return components != null && components.size() > 0;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #getImportStatus()
     */
    @Override
    public ImportStatus getImportStatus() {
        if (getFile().isFile()) {
            if (hasImportFailed())
                return ImportStatus.FAILURE;
            return resultIndex;
        }
        if (components == null || components.size() == 0) {
            if (image instanceof Boolean) {
                if (getFile().isDirectory()) {
                    return ImportStatus.SUCCESS;
                } else {
                    if (!status.isMarkedAsCancel()
                            && !status.isMarkedAsDuplicate())
                        return ImportStatus.FAILURE;
                }
            }
            return resultIndex;
        }

        Collection<LightFileImportComponent> values = components.values();
        int n = components.size();
        int count = 0;
        synchronized (components) {
            Iterator<LightFileImportComponent> i = values.iterator();
            while (i.hasNext()) {
                if (i.next().hasImportFailed())
                    count++;
            }
        }
        if (count == n)
            return ImportStatus.FAILURE;
        if (count > 0)
            return ImportStatus.PARTIAL;
        return ImportStatus.SUCCESS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #hasToRefreshTree()
     */
    @Override
    public boolean hasToRefreshTree() {
        if (getFile().isFile()) {
            if (hasImportFailed())
                return false;
            switch (type) {
            case PROJECT_TYPE:
            case NO_CONTAINER:
                return true;
            default:
                return false;
            }
        }
        if (components == null)
            return false;
        if (importable.isFolderAsContainer() && type != PROJECT_TYPE) {
            Collection<LightFileImportComponent> values = components.values();
            synchronized (components) {
                Iterator<LightFileImportComponent> i = values.iterator();
                while (i.hasNext()) {
                    if (i.next().toRefresh())
                        return true;
                }
            }
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #toRefresh()
     */
    @Override
    public boolean toRefresh() {
        /*
         * if (file.isFile()) { if (deleteButton.isVisible()) return false; else
         * if (errorBox.isVisible()) return !(errorBox.isEnabled() &&
         * errorBox.isSelected()); return true; } if (components == null) return
         * false; Iterator<FileImportComponent> i =
         * components.values().iterator(); int count = 0; while (i.hasNext()) {
         * if (i.next().hasFailuresToSend()) count++; } return components.size()
         * != count;
         */
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #cancelLoading()
     */
    @Override
    public void cancelLoading() {
        if (components == null || components.isEmpty()) {
            cancel(getFile().isFile());
            return;
        }
        Collection<LightFileImportComponent> values = components.values();
        synchronized (components) {
            Iterator<LightFileImportComponent> i = values.iterator();
            while (i.hasNext()) {
                i.next().cancelLoading();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #setType(int)
     */
    @Override
    public void setType(int type) {
        this.type = type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #getType()
     */
    @Override
    public int getType() {
        return type;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #isFolderAsContainer()
     */
    @Override
    public boolean isFolderAsContainer() {
        return importable.isFolderAsContainer();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #getContainerFromFolder()
     */
    @Override
    public DataObject getContainerFromFolder() {
        return containerFromFolder;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #getFilesToReupload()
     */
    @Override
    public List<FileImportComponentI> getFilesToReupload() {
        List<FileImportComponentI> l = null;
        if (getFile().isFile()) {
            if (hasFailuresToReupload() && !reimported) {
                ArrayList<FileImportComponentI> ret = new ArrayList<FileImportComponentI>();
                ret.add(this);
                return ret;
            }
        } else {
            if (components != null) {
                Collection<LightFileImportComponent> values = components
                        .values();
                synchronized (components) {
                    Iterator<LightFileImportComponent> i = values.iterator();
                    FileImportComponentI fc;
                    l = new ArrayList<FileImportComponentI>();
                    List<FileImportComponentI> list;
                    while (i.hasNext()) {
                        fc = i.next();
                        list = fc.getFilesToReupload();
                        if (!CollectionUtils.isEmpty(list))
                            l.addAll(list);
                    }
                }
            }
        }
        return l;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #setReimported(boolean)
     */
    @Override
    public void setReimported(boolean reimported) {
        this.reimported = reimported;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #uploadComplete(java.lang.Object)
     */
    @Override
    public void uploadComplete(Object result) {
        if (result instanceof CmdCallback)
            callback = (CmdCallback) result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #getIndex()
     */
    @Override
    public int getIndex() {
        return index;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #getImportResult()
     */
    @Override
    public Object getImportResult() {
        return status.getImportResult();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #isHCS()
     */
    @Override
    public boolean isHCS() {
        return status.isHCS();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #getImportSize()
     */
    @Override
    public long getImportSize() {
        return status.getSizeUpload();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #hasResult()
     */
    @Override
    public boolean hasResult() {
        return image != null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #getImportableFile()
     */
    @Override
    public ImportableFile getImportableFile() {
        return importable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #onResultsSaving(java.lang.String, boolean)
     */
    @Override
    public void onResultsSaving(String message, boolean busy) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #propertyChange(java.beans.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String name = evt.getPropertyName();
        if (Status.FILES_SET_PROPERTY.equals(name)) {
            if (isCancelled()) {
                return;
            }
            Map<File, Status> files = (Map<File, Status>) evt.getNewValue();
            int n = files.size();
            insertFiles(files);
            firePropertyChange(IMPORT_FILES_NUMBER_PROPERTY, null, n);
        } else if (Status.FILE_IMPORT_STARTED_PROPERTY.equals(name)) {
            resultIndex = ImportStatus.STARTED;
            Status sl = (Status) evt.getNewValue();
            if (sl.equals(status)) {
                firePropertyChange(Status.FILE_IMPORT_STARTED_PROPERTY, null,
                        this);
            }
        } else if (Status.UPLOAD_DONE_PROPERTY.equals(name)) {
            Status sl = (Status) evt.getNewValue();
            if (sl.equals(status) && hasParent()) {
                if (sl.isMarkedAsCancel())
                    cancel(true);
                else {
                    formatResult();
                    firePropertyChange(Status.UPLOAD_DONE_PROPERTY, null, this);
                }
            }
        } else if (Status.FILE_RESET_PROPERTY.equals(name)) {
            importable.setFile((File) evt.getNewValue());
        } else if (ThumbnailLabel.BROWSE_PLATE_PROPERTY.equals(name)) {
            firePropertyChange(BROWSE_PROPERTY, evt.getOldValue(),
                    evt.getNewValue());
        } else if (Status.CONTAINER_FROM_FOLDER_PROPERTY.equals(name)) {
            containerFromFolder = (DataObject) evt.getNewValue();
            if (containerFromFolder instanceof DatasetData) {
                containerObject = containerFromFolder;
            } else if (containerFromFolder instanceof ScreenData) {
                containerObject = containerFromFolder;
            }
        } else if (Status.DEBUG_TEXT_PROPERTY.equals(name)) {
            firePropertyChange(name, evt.getOldValue(), evt.getNewValue());
        } else if (ThumbnailLabel.VIEW_IMAGE_PROPERTY.equals(name)) {
            // use the group
            SecurityContext ctx = new SecurityContext(importable.getGroup()
                    .getId());
            EventBus bus = ImporterAgent.getRegistry().getEventBus();
            Long id = (Long) evt.getNewValue();
            bus.post(new ViewImage(ctx, new ViewImageObject(id), null));
        } else if (Status.IMPORT_DONE_PROPERTY.equals(name)
                || Status.PROCESSING_ERROR_PROPERTY.equals(name)) {
            Status sl = (Status) evt.getNewValue();
            if (sl.equals(status))
                firePropertyChange(Status.IMPORT_DONE_PROPERTY, null, this);
        }
        else if (Status.STEP_PROPERTY.equals(name)) {
            firePropertyChange(Status.STEP_PROPERTY, evt.getOldValue(), evt.getNewValue());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.openmicroscopy.shoola.agents.fsimporter.util.FileImportComponentI
     * #toString()
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(getFile().getAbsolutePath());
        if (importable.getGroup() != null)
            buf.append("_" + importable.getGroup().getId());
        if (importable.getUser() != null)
            buf.append("_" + importable.getUser().getId());
        return buf.toString();
    }

}
