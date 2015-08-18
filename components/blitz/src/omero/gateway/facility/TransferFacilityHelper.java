/*
 * Copyright (C) 2015 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package omero.gateway.facility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.util.ProportionalTimeEstimatorImpl;
import ome.formats.importer.util.TimeEstimator;
import ome.util.checksum.ChecksumProviderFactory;
import ome.util.checksum.ChecksumProviderFactoryImpl;
import omero.ChecksumValidationException;
import omero.RType;
import omero.ServerError;
import omero.api.IQueryPrx;
import omero.api.RawFileStorePrx;
import omero.cmd.HandlePrx;
import omero.gateway.Gateway;
import omero.gateway.SecurityContext;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.exception.ImportException;
import omero.gateway.model.ImportCallback;
import omero.gateway.model.ImportableFile;
import omero.gateway.model.ImportableObject;
import omero.gateway.util.ModelMapper;
import omero.gateway.util.Utils;
import omero.grid.ImportProcessPrx;
import omero.grid.ImportRequest;
import omero.log.LogMessage;
import omero.model.Annotation;
import omero.model.Dataset;
import omero.model.Fileset;
import omero.model.FilesetEntry;
import omero.model.IObject;
import omero.model.OriginalFile;
import omero.model.Project;
import omero.model.ProjectDatasetLink;
import omero.model.Screen;
import omero.model.ScreenI;
import omero.model.TagAnnotation;
import omero.sys.ParametersI;

import org.apache.commons.collections.CollectionUtils;

import pojos.DataObject;
import pojos.DatasetData;
import pojos.ExperimenterData;
import pojos.FilesetData;
import pojos.ImageData;
import pojos.ScreenData;
import pojos.TagAnnotationData;
import pojos.util.PojoMapper;

/**
 * Encapsulates some functionality needed by the {@link TransferFacility}
 * 
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 */
public class TransferFacilityHelper {

    /* checksum provider factory for verifying file integrity in upload */
    private static final ChecksumProviderFactory checksumProviderFactory = new ChecksumProviderFactoryImpl();

    /** Maximum size of bytes read at once. */
    private static final int INC = 262144;//

    private BrowseFacility browse;

    private Gateway gateway;

    private TransferFacility parent;

    private DataManagerFacility datamanager;

    /**
     * Creates a new instance.
     *
     * @param gateway Reference to the gateway.
     * @param datamanager Reference to the manager facility.
     * @param parent Reference to the parent.
     * @throws ExecutionException
     */
    TransferFacilityHelper(Gateway gateway,
            DataManagerFacility datamanager, TransferFacility parent)
            throws ExecutionException {
        this.gateway = gateway;
        this.datamanager = datamanager;
        this.parent = parent;
        this.browse = gateway.getFacility(BrowseFacility.class);
    }

    /**
     * Imports the specified file. Returns the image.
     *
     * @param ctx
     *            The security context.
     * @param object
     *            Information about the file to import.
     * @param container
     *            The folder to import the image.
     * @param ic
     *            The import container.
     * @param status
     *            The component used to give feedback.
     * @param close
     *            Pass <code>true</code> to close the import, <code>false</code>
     *            otherwise.
     * @param userName
     *            The user's name.
     * @return See above.
     * @throws ImportException
     *             If an error occurred while importing.
     */
    Object importImageFile(SecurityContext ctx, ImportableObject object,
            IObject container, ImportContainer ic, ImportCallback status,
            boolean close, String userName) throws ImportException,
            DSAccessException, DSOutOfServiceException {
        status.setImportContainer(ic);
        ImportConfig config = new ImportConfig();
        // FIXME: unclear why we would need to set these values on
        // both the ImportConfig and the ImportContainer.
        if (container != null) {
            config.targetClass.set(container.getClass().getSimpleName());
            config.targetId.set(container.getId().getValue());
            ic.setTarget(container);
        }

        ic.setUserPixels(object.getPixelsSize());
        OMEROMetadataStoreClient omsc = null;
        OMEROWrapper reader = null;
        try {
            omsc = gateway.getImportStore(ctx, userName);
            reader = new OMEROWrapper(config);
            ImportLibrary library = new ImportLibrary(omsc, reader);
            library.addObserver(status);

            // TODO create the handler
            // TMP Code to be moved to the import
            final ImportProcessPrx proc = library.createImport(ic);
            final HandlePrx handle;
            final String[] srcFiles = ic.getUsedFiles();
            final List<String> checksums = new ArrayList<String>();
            final byte[] buf = new byte[omsc.getDefaultBlockSize()];
            Map<Integer, String> failingChecksums = new HashMap<Integer, String>();
            final TimeEstimator estimator = new ProportionalTimeEstimatorImpl(
                    ic.getUsedFilesTotalSize());

            if (status.isMarkedAsCancel())
                return Boolean.valueOf(false);
            library.notifyObservers(new ImportEvent.FILESET_UPLOAD_START(null,
                    0, srcFiles.length, null, null, null));

            for (int i = 0; i < srcFiles.length; i++) {
                checksums.add(library.uploadFile(proc, srcFiles, i,
                        checksumProviderFactory, estimator, buf));
            }

            try {
                handle = proc.verifyUpload(checksums);
            } catch (ChecksumValidationException cve) {
                failingChecksums = cve.failingChecksums;
                return new ImportException(cve);
            } finally {
                try {
                    proc.close();
                } catch (Exception e) {
                    gateway.getLogger().error(this,
                            "Cannot close import process.");
                }
                library.notifyObservers(new ImportEvent.FILESET_UPLOAD_END(
                        null, 0, srcFiles.length, null, null, srcFiles,
                        checksums, failingChecksums, null));
            }
            final ImportRequest req = (ImportRequest) handle.getRequest();
            final Fileset fs = req.activity.getParent();
            status.setFilesetData(new FilesetData(fs));
            return library.createCallback(proc, handle, ic);
        } catch (Throwable e) {
            try {
                if (reader != null)
                    reader.close();
            } catch (Exception ex) {
            }

            parent.handleException(this, e, "Could not import image.");

            if (close)
                gateway.closeImport(ctx, userName);
            return new ImportException(e);
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (Exception ex) {
            }
            if (omsc != null && close)
                gateway.closeImport(ctx, userName);
        }
    }

    Object importFile(ImportableObject object, ImportableFile importable,
            ExperimenterData exp, boolean close) throws ImportException,
            DSAccessException, DSOutOfServiceException {
        if (importable == null || importable.getFile() == null)
            throw new IllegalArgumentException("No images to import.");
        ImportCallback status = importable.getStatus();
        SecurityContext ctx = new SecurityContext(importable.getGroup().getId());
        // If import as.
        ExperimenterData loggedIn = gateway.getLoggedInUser();
        long userID = loggedIn.getId();
        String userName = null;
        if (importable.getUser() != null) {
            userID = exp.getId();
            if (exp.getId() != loggedIn.getId())
                userName = exp.getUserName();
        }
        if (status.isMarkedAsCancel()) {
            if (close)
                gateway.closeImport(ctx, userName);
            return Boolean.valueOf(false);
        }
        Collection<TagAnnotationData> tags = object.getTags();
        List<Annotation> customAnnotationList = new ArrayList<Annotation>();
        List<IObject> l;
        // Tags
        Map<Object, Object> parameters = new HashMap<Object, Object>();
        if (!CollectionUtils.isEmpty(tags)) {
            List<TagAnnotationData> values = new ArrayList<TagAnnotationData>();
            Iterator<TagAnnotationData> i = tags.iterator();
            TagAnnotationData tag;
            l = new ArrayList<IObject>();
            while (i.hasNext()) {
                tag = i.next();
                if (tag.getId() > 0) {
                    values.add(tag);
                    customAnnotationList.add((Annotation) tag.asIObject());
                } else
                    l.add(tag.asIObject());
            }
            // save the tag.
            try {
                if (l.size() > 0) {
                    l = datamanager.saveAndReturnObject(ctx, l, parameters,
                            userName);
                }
                Iterator<IObject> j = l.iterator();
                Annotation a;
                while (j.hasNext()) {
                    a = (Annotation) j.next();
                    values.add(new TagAnnotationData((TagAnnotation) a));
                    customAnnotationList.add(a); // THIS!
                }
                object.setTags(values);
            } catch (Exception e) {
                LogMessage msg = new LogMessage();
                msg.print("Cannot create the tags.");
                msg.print(e);
                gateway.getLogger().error(this, msg);
            }
        }
        IObject link;
        // prepare the container.
        List<String> candidates;
        ImportCandidates ic = null;
        File file = importable.getFile();
        DatasetData dataset = importable.getDataset();
        DataObject container = importable.getParent();
        IObject ioContainer = null;

        DataObject createdData;
        IObject project = null;
        DataObject folder = null;
        boolean hcsFile;
        boolean hcs;
        ImportContainer importIc;
        List<ImportContainer> icContainers;
        if (file.isFile()) {
            ic = getImportCandidates(ctx, object, file, status);
            if (CollectionUtils.isEmpty(ic.getContainers())) {
                Object o = status.getImportResult();
                if (o instanceof ImportException) {
                    return o;
                }
                ImportException e = new ImportException(
                        ImportException.FILE_NOT_VALID_TEXT);
                status.setCallback(e);
                return e;
            }
            hcsFile = isHCS(ic.getContainers());
            // Create the container if required.
            if (hcsFile) {
                if (ic != null) {
                    candidates = ic.getPaths();
                    if (candidates.size() == 1) {
                        String value = candidates.get(0);
                        if (!file.getAbsolutePath().equals(value)
                                && object.isFileinQueue(value)) {
                            if (close)
                                gateway.closeImport(ctx, userName);
                            status.markedAsDuplicate();
                            return Boolean.valueOf(true);
                        }
                    }
                }
                dataset = null;
                if (!(container instanceof ScreenData))
                    container = null;
            }

            // remove hcs check if we want to create screen from folder.
            if (!hcsFile && importable.isFolderAsContainer()) {
                // we have to import the image in this container.
                folder = object.createFolderAsContainer(importable, hcsFile);
                DatasetData d = null;
                DataObject c = container;
                if (folder instanceof DatasetData)
                    d = (DatasetData) folder;
                else if (folder instanceof ScreenData)
                    c = folder;
                try {
                    ioContainer = determineContainer(ctx, d, c, object,
                            userName);
                    status.setContainerFromFolder(PojoMapper
                            .asDataObject(ioContainer));
                } catch (Exception e) {
                    LogMessage msg = new LogMessage();
                    msg.print("Cannot create the container.");
                    msg.print(e);
                    gateway.getLogger().error(this, msg);
                }
            }
            if (folder == null && dataset != null) { // dataset
                try {
                    ioContainer = determineContainer(ctx, dataset, container,
                            object, userName);
                } catch (Exception e) {
                    LogMessage msg = new LogMessage();
                    msg.print("Cannot create the container hosting the images.");
                    msg.print(e);
                    gateway.getLogger().error(this, msg);
                }
            } else { // no dataset specified.
                if (container instanceof ScreenData) {
                    if (container.getId() <= 0) {
                        // project needs to be created to.
                        createdData = object.hasObjectBeenCreated(container,
                                ctx);
                        if (createdData == null) {
                            try {
                                ioContainer = datamanager.saveAndReturnObject(
                                        ctx, container.asIObject(), parameters,
                                        userName);
                                // register
                                object.addNewDataObject(PojoMapper
                                        .asDataObject(ioContainer));
                            } catch (Exception e) {
                                LogMessage msg = new LogMessage();
                                msg.print("Cannot create the Screen hosting "
                                        + "the plate.");
                                msg.print(e);
                                gateway.getLogger().error(this, msg);
                            }
                        }
                    } else {
                        // Check that the container still exists
                        ioContainer = browse.findIObject(ctx,
                                container.asIObject());
                    }
                }
            }
            if (ImportableObject.isArbitraryFile(file)) {
                candidates = ic.getPaths();
                int size = candidates.size();
                if (size == 0) {
                    Object o = status.getImportResult();
                    if (o instanceof ImportException) {
                        return o;
                    }
                    ImportException e = new ImportException(
                            ImportException.FILE_NOT_VALID_TEXT);
                    status.setCallback(e);
                    return e;
                } else if (size == 1) {
                    String value = candidates.get(0);
                    if (!file.getAbsolutePath().equals(value)
                            && object.isFileinQueue(value)) {
                        if (close)
                            gateway.closeImport(ctx, userName);
                        status.markedAsDuplicate();
                        return Boolean.valueOf(true);
                    }
                    File f = new File(value);
                    status.resetFile(f);
                    if (ioContainer == null)
                        status.setNoContainer();
                    importIc = ic.getContainers().get(0);
                    importIc.setCustomAnnotationList(customAnnotationList);
                    status.setUsedFiles(importIc.getUsedFiles());
                    // Check after scanning
                    if (status.isMarkedAsCancel())
                        return Boolean.valueOf(false);
                    return importImageFile(ctx, object, ioContainer, importIc,
                            status, close, userName);
                } else {
                    List<ImportContainer> containers = ic.getContainers();
                    hcs = isHCS(containers);
                    Map<File, ImportCallback> files = new HashMap<File, ImportCallback>();
                    Iterator<String> i = candidates.iterator();
                    ImportCallback label;
                    int index = 0;
                    File f;
                    while (i.hasNext()) {
                        f = new File(i.next());
                        label = new ImportCallback(f);
                        label.setUsedFiles(containers.get(index).getUsedFiles());
                        files.put(f, label);
                        index++;
                    }

                    status.setFiles(files);
                    Object v = importCandidates(ctx, files, status, object,
                            ioContainer, customAnnotationList, userID, close,
                            hcs, userName);
                    if (v != null)
                        return v;
                }
            } else { // single file let's try to import it.
                if (ioContainer == null)
                    status.setNoContainer();
                ic = getImportCandidates(ctx, object, file, status);
                icContainers = ic.getContainers();
                if (icContainers.size() == 0) {
                    Object o = status.getImportResult();
                    if (o instanceof ImportException) {
                        return o;
                    }
                    return new ImportException(
                            ImportException.FILE_NOT_VALID_TEXT);
                }
                importIc = icContainers.get(0);
                importIc.setCustomAnnotationList(customAnnotationList);
                status.setUsedFiles(importIc.getUsedFiles());
                // Check after scanning
                if (status.isMarkedAsCancel())
                    return Boolean.valueOf(false);
                return importImageFile(ctx, object, ioContainer, importIc,
                        status, close, userName);
            }
        } // file import ends.
          // Checks folder import.
        ic = getImportCandidates(ctx, object, file, status);
        List<ImportContainer> lic = ic.getContainers();
        if (lic.size() == 0) {
            Object o = status.getImportResult();
            if (o instanceof ImportException) {
                return o;
            }
            return new ImportException(ImportException.FILE_NOT_VALID_TEXT);
        }
        if (status.isMarkedAsCancel()) {
            return Boolean.valueOf(false);
        }
        Map<File, ImportCallback> hcsFiles = new HashMap<File, ImportCallback>();
        Map<File, ImportCallback> otherFiles = new HashMap<File, ImportCallback>();
        Map<File, ImportCallback> files = new HashMap<File, ImportCallback>();

        File f;
        ImportCallback sl;
        int n = lic.size();

        Iterator<ImportContainer> j = lic.iterator();
        ImportContainer c;
        while (j.hasNext()) {
            c = j.next();
            hcs = c.getIsSPW();
            f = c.getFile();
            sl = new ImportCallback(f);
            sl.setUsedFiles(c.getUsedFiles());
            if (hcs) {
                if (n == 1 && file.list().length > 1)
                    hcsFiles.put(f, sl);
                else if (n > 1) {
                    if (f.getName().endsWith(ImportableObject.DAT_EXTENSION))
                        otherFiles.put(f, sl);
                    else
                        hcsFiles.put(f, sl);
                } else
                    hcsFiles.put(f, sl);
            } else
                otherFiles.put(f, sl);
            files.put(f, sl);
        }
        status.setFiles(files);
        // check candidates and see if we are dealing with HCS data
        if (hcsFiles.size() > 0) {
            if (container != null && container instanceof ScreenData) {
                if (container.getId() <= 0) {
                    // project needs to be created to.
                    createdData = object.hasObjectBeenCreated(container, ctx);
                    if (createdData == null) {
                        try {
                            ioContainer = datamanager
                                    .saveAndReturnObject(ctx,
                                            container.asIObject(), parameters,
                                            userName);
                            // register
                            object.addNewDataObject(PojoMapper
                                    .asDataObject(ioContainer));
                        } catch (Exception e) {
                            LogMessage msg = new LogMessage();
                            msg.print("Cannot create the Screen hosting the "
                                    + "plates.");
                            msg.print(e);
                            gateway.getLogger().error(this, msg);
                        }
                    }
                } else
                    ioContainer = browse
                            .findIObject(ctx, container.asIObject());
            }
            importCandidates(ctx, hcsFiles, status, object, ioContainer,
                    customAnnotationList, userID, close, true, userName);
        }
        if (otherFiles.size() > 0) {
            folder = object.createFolderAsContainer(importable);
            if (folder != null) { // folder
                // we have to import the image in this container.
                try {
                    ioContainer = datamanager.saveAndReturnObject(ctx,
                            folder.asIObject(), parameters, userName);
                    status.setContainerFromFolder(PojoMapper
                            .asDataObject(ioContainer));
                    if (folder instanceof DatasetData) {
                        if (container != null) {
                            try {
                                Project p;
                                if (container.getId() <= 0) {
                                    // project needs to be created to.
                                    createdData = object.hasObjectBeenCreated(
                                            container, ctx);
                                    if (createdData == null) {
                                        project = datamanager
                                                .saveAndReturnObject(ctx,
                                                        container.asIObject(),
                                                        parameters, userName);
                                        object.addNewDataObject(PojoMapper
                                                .asDataObject(project));
                                        p = (Project) project;
                                    } else {
                                        p = (Project) createdData.asProject();
                                    }
                                } else { // project already exists.
                                    p = (Project) container.asProject();
                                }
                                link = (ProjectDatasetLink) ModelMapper
                                        .linkParentToChild(
                                                (Dataset) ioContainer, p);
                                link = (ProjectDatasetLink) datamanager
                                        .saveAndReturnObject(ctx, link,
                                                parameters, userName);
                            } catch (Exception e) {
                                LogMessage msg = new LogMessage();
                                msg.print("Cannot create the container "
                                        + "hosting the data.");
                                msg.print(e);
                                gateway.getLogger().error(this, msg);
                            }
                        }
                    }
                } catch (Exception e) {
                }
            } else { // folder
                if (dataset != null) { // dataset
                    try {
                        ioContainer = determineContainer(ctx, dataset,
                                container, object, userName);
                    } catch (Exception e) {
                        LogMessage msg = new LogMessage();
                        msg.print("Cannot create the container "
                                + "hosting the data.");
                        msg.print(e);
                        gateway.getLogger().error(this, msg);
                    }
                }
            }
            // import the files that are not hcs files.
            importCandidates(ctx, otherFiles, status, object, ioContainer,
                    customAnnotationList, userID, close, false, userName);
        }
        return Boolean.valueOf(true);
    }

    /**
     * Returns the import candidates.
     *
     * @param ctx
     *            The security context.
     * @param object
     *            Host information about the file to import.
     * @param file
     *            The file to import.
     * @param archived
     *            Pass <code>true</code> to archived the files,
     *            <code>false</code> otherwise.
     * @param depth
     *            The depth used to set the name. This will be taken into
     *            account if the file is a directory.
     * @return See above.
     * @throws ImportException
     *             If an error occurred while importing.
     */
    ImportCandidates getImportCandidates(SecurityContext ctx,
            ImportableObject object, File file, ImportCallback status)
            throws ImportException {
        OMEROWrapper reader = null;
        try {
            ImportConfig config = new ImportConfig();
            reader = new OMEROWrapper(config);
            String[] paths = new String[1];
            paths[0] = file.getAbsolutePath();
            ImportCandidates icans = new ImportCandidates(reader, paths, status);

            if (object.isOverrideName()) {
                String name = Utils.getDisplayedFileName(
                        file.getAbsolutePath(), object.getDepthForName());
                for (ImportContainer ic : icans.getContainers()) {
                    ic.setUserSpecifiedName(name);
                }
            }

            return icans;
        } catch (Throwable e) {
            throw new ImportException(e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ex) {
                }
            }
        }
    }

    /**
     * Imports the specified candidates.
     * 
     * @param ctx
     *            The security context.
     * @param candidates
     *            The file to import.
     * @param status
     *            The original status.
     * @param object
     *            The object hosting information about the import.
     * @param ioList
     *            The containers where to import the files.
     * @param list
     *            The list of annotations.
     * @param userID
     *            The identifier of the user.
     * @param hcs
     *            Value returns by the import containers.
     * @param userName
     *            The login name of the user to import for.
     */
    private Object importCandidates(SecurityContext ctx,
            Map<File, ImportCallback> files, ImportCallback status,
            ImportableObject object, IObject ioContainer,
            List<Annotation> list, long userID, boolean close, boolean hcs,
            String userName) throws DSAccessException, DSOutOfServiceException {
        if (status.isMarkedAsCancel()) {
            if (close)
                gateway.closeImport(ctx, userName);
            return Boolean.valueOf(false);
        }
        Entry<File, ImportCallback> entry;
        Iterator<Entry<File, ImportCallback>> jj = files.entrySet().iterator();
        ImportCallback label = null;
        File file;
        boolean toClose = false;
        int n = files.size() - 1;
        int index = 0;
        ImportCandidates ic;
        List<ImportContainer> icContainers;
        ImportContainer importIc;
        while (jj.hasNext()) {
            entry = jj.next();
            file = (File) entry.getKey();
            if (hcs && !file.getName().endsWith(ImportableObject.DAT_EXTENSION))
                if (ioContainer != null
                        && !(ioContainer.getClass().equals(Screen.class) || ioContainer
                                .getClass().equals(ScreenI.class)))
                    ioContainer = null;
            label = (ImportCallback) entry.getValue();
            if (close) {
                toClose = index == n;
                index++;
            }
            if (!label.isMarkedAsCancel()) {
                try {
                    if (ioContainer == null)
                        label.setNoContainer();
                    ic = getImportCandidates(ctx, object, file, status);
                    icContainers = ic.getContainers();
                    if (icContainers.size() == 0) {
                        Object o = status.getImportResult();
                        if (o instanceof ImportException) {
                            label.setCallback(o);
                        } else {
                            label.setCallback(new ImportException(
                                    ImportException.FILE_NOT_VALID_TEXT));
                        }
                    } else {
                        // Check after scanning
                        if (label.isMarkedAsCancel())
                            label.setCallback(Boolean.valueOf(false));
                        else {
                            importIc = icContainers.get(0);
                            importIc.setCustomAnnotationList(list);
                            label.setCallback(importImageFile(ctx, object,
                                    ioContainer, importIc, label, toClose,
                                    userName));
                        }
                    }
                } catch (Exception e) {
                    label.setCallback(e);
                }
            } else {
                label.setCallback(Boolean.valueOf(false));
            }
        }
        if (close)
            gateway.closeImport(ctx, userName);
        return null;
    }

    /**
     * Returns <code>true</code> if the containers are <code>HCS</code>
     * containers, <code>false</code> otherwise.
     * 
     * @param containers
     *            The collection to handle.
     * @return See above.
     */
    private boolean isHCS(List<ImportContainer> containers) {
        if (CollectionUtils.isEmpty(containers))
            return false;
        int count = 0;
        Iterator<ImportContainer> i = containers.iterator();
        ImportContainer ic;
        while (i.hasNext()) {
            ic = i.next();
            if (ic.getIsSPW())
                count++;
        }
        return count == containers.size();
    }

    /**
     * Recycles or creates the container.
     * 
     * @param ctx
     *            The security context.
     * @param dataset
     *            The dataset to create or recycle.
     * @param container
     *            The container to create and link the dataset to.
     * @param object
     *            The object hosting the import option.
     * @param userName
     *            The name of the user to create the data for.
     * @return See above.
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMERO
     *             service.
     */
    private IObject determineContainer(SecurityContext ctx,
            DatasetData dataset, DataObject container, ImportableObject object,
            String userName) throws DSOutOfServiceException, DSAccessException {
        IObject ioContainer = null;
        Map<Object, Object> parameters = new HashMap<Object, Object>();
        DataObject createdData;
        IObject project = null;
        IObject link;
        if (dataset != null) { // dataset
            if (dataset.getId() <= 0) {
                // Check if it has been already been created.
                // need to create it first
                if (container != null) {
                    if (container.getId() <= 0) {
                        // project needs to be created to.
                        createdData = object.hasObjectBeenCreated(container,
                                ctx);
                        if (createdData == null) {
                            project = datamanager
                                    .saveAndReturnObject(ctx,
                                            container.asIObject(), parameters,
                                            userName);
                            // register
                            object.addNewDataObject(PojoMapper
                                    .asDataObject(project));
                            // now create the dataset
                            ioContainer = datamanager.saveAndReturnObject(ctx,
                                    dataset.asIObject(), parameters, userName);
                            // register
                            object.registerDataset(project.getId().getValue(),
                                    (DatasetData) PojoMapper
                                            .asDataObject(ioContainer));
                            link = (ProjectDatasetLink) ModelMapper
                                    .linkParentToChild((Dataset) ioContainer,
                                            (Project) project);
                            link = (ProjectDatasetLink) datamanager
                                    .saveAndReturnObject(ctx, link, parameters,
                                            userName);
                        } else {
                            DatasetData d;
                            d = object.isDatasetCreated(createdData.getId(),
                                    dataset);
                            if (d == null) {
                                ioContainer = datamanager.saveAndReturnObject(
                                        ctx, dataset.asIObject(), parameters,
                                        userName);
                                // register
                                object.registerDataset(createdData.getId(),
                                        (DatasetData) PojoMapper
                                                .asDataObject(ioContainer));
                                link = (ProjectDatasetLink) ModelMapper
                                        .linkParentToChild(
                                                (Dataset) ioContainer,
                                                (Project) createdData
                                                        .asProject());
                                link = (ProjectDatasetLink) datamanager
                                        .saveAndReturnObject(ctx, link,
                                                parameters, userName);
                            } else
                                ioContainer = d.asIObject();
                        }
                    } else { // project already exists.
                        createdData = object.isDatasetCreated(
                                container.getId(), dataset);
                        if (createdData == null) {
                            ioContainer = datamanager.saveAndReturnObject(ctx,
                                    dataset.asIObject(), parameters, userName);
                            // register
                            object.registerDataset(container.getId(),
                                    (DatasetData) PojoMapper
                                            .asDataObject(ioContainer));
                            // Check that the project still exists
                            IObject ho = browse.findIObject(ctx,
                                    container.asIObject());
                            if (ho != null) {
                                link = (ProjectDatasetLink) ModelMapper
                                        .linkParentToChild(
                                                (Dataset) ioContainer,
                                                (Project) container.asProject());
                                link = (ProjectDatasetLink) datamanager
                                        .saveAndReturnObject(ctx, link,
                                                parameters, userName);
                            }

                        } else
                            ioContainer = createdData.asIObject();
                    }
                } else { // dataset w/o project.
                    createdData = object.hasObjectBeenCreated(dataset, ctx);
                    if (createdData == null) {
                        ioContainer = datamanager.saveAndReturnObject(ctx,
                                dataset.asIObject(), parameters, userName);
                        // register
                        object.addNewDataObject(PojoMapper
                                .asDataObject(ioContainer));
                    } else
                        ioContainer = createdData.asIObject();
                }
            } else
                ioContainer = dataset.asIObject();
        } else { // check on the container.
            if (container != null) {
                if (container.getId() <= 0) {
                    // container needs to be created to.
                    createdData = object.hasObjectBeenCreated(container, ctx);
                    if (createdData == null) {
                        ioContainer = datamanager.saveAndReturnObject(ctx,
                                container.asIObject(), parameters, userName);
                        // register
                        object.addNewDataObject(PojoMapper
                                .asDataObject(project));
                    } else {
                        ioContainer = createdData.asIObject();
                    }
                } else
                    ioContainer = container.asIObject();
            }
        }
        // Check that the container still exist
        return browse.findIObject(ctx, ioContainer);
    }

    /**
     * Downloads the original file of an image from the server.
     *
     * @param context The security context.
     * @param targetPath Path to the file.
     * @param imageId The identifier of the image.
     * @return See above
     * @throws DSOutOfServiceException
     *             If the connection is broken, or logged in.
     * @throws DSAccessException
     *             If an error occurred while trying to retrieve data from OMEDS
     *             service.
     */
    List<File> downloadImage(SecurityContext context, String targetPath,
            long imageId) throws DSAccessException, DSOutOfServiceException {
        List<File> files = new ArrayList<File>();

        ImageData image = browse.findObject(context, ImageData.class, imageId, true);

        String query;
        List<?> filesets;
        try {
            IQueryPrx service = gateway.getQueryService(context);
            ParametersI param = new ParametersI();
            long id;
            if (image.isFSImage()) {
                id = image.getId();
                List<RType> l = new ArrayList<RType>();
                l.add(omero.rtypes.rlong(id));
                param.add("imageIds", omero.rtypes.rlist(l));
                query = createFileSetQuery();
            } else {//Prior to FS
                if (image.isArchived()) {
                    StringBuffer buffer = new StringBuffer();
                    id = image.getDefaultPixels().getId();
                    buffer.append("select ofile from OriginalFile as ofile ");
                    buffer.append("join fetch ofile.hasher ");
                    buffer.append("left join ofile.pixelsFileMaps as pfm ");
                    buffer.append("left join pfm.child as child ");
                    buffer.append("where child.id = :id");
                    param.map.put("id", omero.rtypes.rlong(id));
                    query = buffer.toString();
                } else return null;
            }
            filesets = service.findAllByQuery(query, param);
        } catch (Exception e) {
            throw new DSAccessException("Cannot retrieve original file", e);
        }

        if (CollectionUtils.isEmpty(filesets))
            return files;
        Iterator<?> i;
        List<OriginalFile> values = new ArrayList<OriginalFile>();
        if (image.isFSImage()) {
            i = filesets.iterator();
            Fileset set;
            List<FilesetEntry> entries;
            Iterator<FilesetEntry> j;
            while (i.hasNext()) {
                set = (Fileset) i.next();
                entries = set.copyUsedFiles();
                j = entries.iterator();
                while (j.hasNext()) {
                    FilesetEntry fs = j.next();
                    values.add(fs.getOriginalFile());
                }
            }
        } else
            values.addAll((List<OriginalFile>) filesets);

        RawFileStorePrx store = null;
        OriginalFile of;
        long size;
        FileOutputStream stream = null;
        long offset = 0;
        i = values.iterator();
        File f = null;

        while (i.hasNext()) {
            of = (OriginalFile) i.next();

            try {
                store = gateway.getRawFileService(context);
                store.setFileId(of.getId().getValue());

                f = new File(targetPath, of.getName().getValue());
                files.add(f);

                stream = new FileOutputStream(f);
                size = of.getSize().getValue();
                try {
                    try {
                        for (offset = 0; (offset + INC) < size;) {
                            stream.write(store.read(offset, INC));
                            offset += INC;
                        }
                    } finally {
                        stream.write(store.read(offset, (int) (size - offset)));
                        stream.close();
                    }
                } catch (Exception e) {
                    if (stream != null)
                        stream.close();
                    if (f != null) {
                        f.delete();
                        files.remove(f);
                    }
                }
            } catch (IOException e) {
                if (f != null) {
                    f.delete();
                    files.remove(f);
                }
                throw new DSAccessException("Cannot create file in folderPath",
                        e);
            } catch (Throwable t) {
                throw new DSAccessException("ServerError on retrieveArchived",
                        t);
            } finally {
                try {
                    store.close();
                } catch (ServerError e) {
                }
            }
        }

        return files;
    }
    
    /**
     * Creates the query to load the file set corresponding to a given image.
     *
     * @return See above.
     */
    private String createFileSetQuery()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("select fs from Fileset as fs ");
        buffer.append("join fetch fs.images as image ");
        buffer.append("left outer join fetch fs.usedFiles as usedFile ");
        buffer.append("join fetch usedFile.originalFile as f ");
        buffer.append("join fetch f.hasher ");
        buffer.append("where image.id in (:imageIds)");
        return buffer.toString();
    }
}
