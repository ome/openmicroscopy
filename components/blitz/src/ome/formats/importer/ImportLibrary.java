/*
 * ome.formats.importer.ImportLibrary
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005-2013 Open Microscopy Environment
 *      Massachusetts Institute of Technology,
 *      National Institutes of Health,
 *      University of Dundee
 *
 *
 *------------------------------------------------------------------------------
 */

package ome.formats.importer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import loci.formats.FormatException;
import loci.formats.FormatReader;

import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.util.ErrorHandler;
import ome.services.blitz.repo.path.ClientFilePathTransformer;
import ome.services.blitz.repo.path.FilePathRestrictionInstance;
import ome.services.blitz.repo.path.FilePathRestrictions;
import ome.services.blitz.repo.path.MakePathComponentSafe;
import ome.services.blitz.util.ChecksumAlgorithmMapper;
import ome.util.checksum.ChecksumProvider;
import ome.util.checksum.ChecksumProviderFactory;
import ome.util.checksum.ChecksumProviderFactoryImpl;
import omero.ChecksumValidationException;
import omero.ServerError;
import omero.api.IMetadataPrx;
import omero.api.RawFileStorePrx;
import omero.api.ServiceFactoryPrx;
import omero.cmd.CmdCallbackI;
import omero.cmd.ERR;
import omero.cmd.HandlePrx;
import omero.cmd.Response;
import omero.cmd.Status;
import omero.grid.ImportProcessPrx;
import omero.grid.ImportRequest;
import omero.grid.ImportResponse;
import omero.grid.ImportSettings;
import omero.grid.ManagedRepositoryPrx;
import omero.grid.ManagedRepositoryPrxHelper;
import omero.grid.RepositoryMap;
import omero.grid.RepositoryPrx;
import omero.model.Annotation;
import omero.model.Dataset;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Fileset;
import omero.model.FilesetI;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.Screen;
import omero.sys.Parameters;
import omero.sys.ParametersI;

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import Ice.Current;

/**
 * support class for the proper usage of {@link OMEROMetadataStoreClient} and
 * {@link FormatReader} instances. This library was factored out of
 * ImportHandler to support ImportFixture
 *
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision: 1167 $, $Date: 2006-12-15 10:39:34 +0000 (Fri, 15 Dec 2006) $
 * @see FormatReader
 * @see OMEROMetadataStoreClient
 * @see ImportFixture
 * @see IObservable
 * @see IObserver
 * @since 3.0-M3
 */
public class ImportLibrary implements IObservable
{
    private static Logger log = LoggerFactory.getLogger(ImportLibrary.class);

    /** The class used to identify the dataset target.*/
    private static final String DATASET_CLASS = "omero.model.Dataset";

    /** The class used to identify the screen target.*/
    private static final String SCREEN_CLASS = "omero.model.Screen";

    /* checksum provider factory for verifying file integrity in upload */
    private static final ChecksumProviderFactory checksumProviderFactory = new ChecksumProviderFactoryImpl();

    private final ArrayList<IObserver> observers = new ArrayList<IObserver>();

    private final OMEROMetadataStoreClient store;

    private final ManagedRepositoryPrx repo;

    private final ServiceFactoryPrx sf;

    /**
     * Adapter for use with any callbacks created by the library.
     */
    private final Ice.ObjectAdapter oa;

    /**
     * Router category which allows callbacks to be accessed behind a firewall.
     */
    private final String category;

    /**
     * The library will not close the client instance. The reader will be closed
     * between calls to import.
     *
     * @param store not null
     * @param reader not null
     */
    public ImportLibrary(OMEROMetadataStoreClient client, OMEROWrapper reader)
    {
        if (client == null || reader == null)
        {
            throw new NullPointerException(
            "All arguments to ImportLibrary() must be non-null.");
        }

        this.store = client;
        repo = lookupManagedRepository();
        // Adapter which should be used for callbacks. This is more
        // complicated than it needs to be at the moment. We're only sure that
        // the OMSClient has a ServiceFactory (and not necessarily a client)
        // so we have to inspect various fields to get the adapter.
        sf = store.getServiceFactory();
        oa = sf.ice_getConnection().getAdapter();
        final Ice.Communicator ic = oa.getCommunicator();
        category = omero.client.getRouter(ic).getCategoryForClient();
    }

    //
    // Observable methods
    //

    public boolean addObserver(IObserver object)
    {
        return observers.add(object);
    }

    public boolean deleteObserver(IObserver object)
    {
        return observers.remove(object);

    }

    /* (non-Javadoc)
     * @see ome.formats.importer.IObservable#notifyObservers(ome.formats.importer.ImportEvent)
     */
    public void notifyObservers(ImportEvent event)
    {
        for (IObserver observer : observers) {
            observer.update(this, event);
        }
    }


    // ~ Actions
    // =========================================================================

    /**
     * Primary user method for importing a number
     *
     * @param config The configuration information.
     * @param candidates Hosts information about the files to import.
     */
    public boolean importCandidates(ImportConfig config, ImportCandidates candidates)
    {
        List<ImportContainer> containers = candidates.getContainers();
        if (containers != null) {
            int numDone = 0;
            for (int index = 0; index < containers.size(); index++) {
                ImportContainer ic = containers.get(index);
                if (DATASET_CLASS.equals(config.targetClass.get()))
                {
                    ic.setTarget(store.getTarget(
                            Dataset.class, config.targetId.get()));
                }
                else if (SCREEN_CLASS.equals(config.targetClass.get()))
                {
                    ic.setTarget(store.getTarget(
                            Screen.class, config.targetId.get()));
                }

                try {
                    importImage(ic,index,numDone,containers.size());
                    numDone++;
                } catch (Throwable t) {
                    log.error("Error on import", t);
                    if (!config.contOnError.get()) {
                        log.info("Exiting on error");
                        return false;
                    } else {
                        log.info("Continuing after error");
                    }
                }
            }
        }
        return true;
    }

    /**
     * Delete files from the managed repository.
     * @param container The current import container containing usedFiles to be
     * deleted.
     * @return List of files that could not be deleted.
     */
    public List<String> deleteFilesFromRepository(ImportContainer container)
            throws ServerError
    {
        checkManagedRepo();
        // FIXME List<String> undeleted = repo.deleteFiles(container.getUsedFiles());
        // return undeleted;
        return null;
    }

    /**
     * Provide initial configuration to the server in order to create the
     * {@link ImportProcessPrx} which will manage state server-side.
     * @throws IOException if the used files' absolute path could not be found
     */
    public ImportProcessPrx createImport(final ImportContainer container)
        throws ServerError, IOException {
        checkManagedRepo();
        String[] usedFiles = container.getUsedFiles();
        File target = container.getFile();
        if (log.isDebugEnabled()) {
            log.debug("Main file: " + target.getAbsolutePath());
            log.debug("Used files before:");
            for (String f : usedFiles) {
                log.debug(f);
            }
        }

        // TODO: allow looser sanitization according to server configuration
        final FilePathRestrictions portableRequiredRules =
                FilePathRestrictionInstance.getFilePathRestrictions(FilePathRestrictionInstance.WINDOWS_REQUIRED,
                                                                    FilePathRestrictionInstance.UNIX_REQUIRED);
        final ClientFilePathTransformer sanitizer = new ClientFilePathTransformer(new MakePathComponentSafe(portableRequiredRules));

        final ImportSettings settings = new ImportSettings();
        // TODO: here or on container.fillData, we need to
        // check if the container object has ChecksumAlgorithm
        // present and pass it into the settings object
        final Fileset fs = new FilesetI();
        container.fillData(new ImportConfig(), settings, fs, sanitizer);
        return repo.importFileset(fs, settings);

    }

    /**
     * Upload files to the managed repository.
     *
     * This is done by first passing in the possibly absolute local file paths.
     * A common selection of those are chosen and passed back to the client.
     *
     * As each file is written to the server, a message digest is kept updated
     * of the bytes that are being written. These are then returned to the
     * caller so they can be checked against the values found on the server.
     *
     * @param container The current import container we're to handle.
     * @return A list of the client-side (i.e. local) hashes for each file.
     */
    public List<String> uploadFilesToRepository(
            final String[] srcFiles, final ImportProcessPrx proc)
    {
        final byte[] buf = new byte[store.getDefaultBlockSize()];
        final int fileTotal = srcFiles.length;
        final List<String> checksums = new ArrayList<String>(fileTotal);

        log.debug("Used files created:");
        for (int i = 0; i < fileTotal; i++) {
            try {
                checksums.add(uploadFile(proc, srcFiles, i, checksumProviderFactory, buf));
            } catch (ServerError e) {
                log.error("Server error uploading file.", e);
                break;
            } catch (IOException e) {
                log.error("I/O error uploading file.", e);
                break;
            }
        }
        return checksums;
    }

    public String uploadFile(final ImportProcessPrx proc,
            final String[] srcFiles, int index) throws ServerError, IOException
    {
        final byte[] buf = new byte[store.getDefaultBlockSize()];
        return uploadFile(proc, srcFiles, index, checksumProviderFactory, buf);
    }

    public String uploadFile(final ImportProcessPrx proc,
            final String[] srcFiles, final int index,
            final ChecksumProviderFactory cpf, final byte[] buf)
            throws ServerError, IOException {

        ChecksumProvider cp = cpf.getProvider(
                ChecksumAlgorithmMapper.getChecksumType(
                        proc.getImportSettings().checksumAlgorithm));
        String digestString = null;
        File file = new File(srcFiles[index]);
        long length = file.length();
        FileInputStream stream = null;
        RawFileStorePrx rawFileStore = null;
        try {
            stream = new FileInputStream(file);
            rawFileStore = proc.getUploader(index);
            int rlen = 0;
            long offset = 0;

            // Fields used for timing measurements
            long start, timeLeft = 0L;
            float alpha, chunkTime;
            int sampleSize = 5;
            Buffer samples = new CircularFifoBuffer(sampleSize);

            notifyObservers(new ImportEvent.FILE_UPLOAD_STARTED(
                    file.getAbsolutePath(), index, srcFiles.length,
                    null, length, null));

            // "touch" the file otherwise zero-length files
            rawFileStore.write(new byte[0], offset, 0);
            notifyObservers(new ImportEvent.FILE_UPLOAD_BYTES(
                    file.getAbsolutePath(), index, srcFiles.length,
                    offset, length, timeLeft, null));

            while (true) {
                // Due to weirdness with System.nanoTime() on multi-core
                // CPUs, falling back to currentTimeMillis()
                chunkTime = 0;
                start = System.currentTimeMillis();
                rlen = stream.read(buf);
                if (rlen == -1) {
                    break;
                }
                cp.putBytes(buf, 0, rlen);
                rawFileStore.write(buf, offset, rlen);
                offset += rlen;
                samples.add(System.currentTimeMillis() - start);
                alpha = 2f / (samples.size() + 1);
                for (int i = 0; i < samples.size(); i++) {
                    chunkTime = alpha * (Long) samples.get()
                            + (1 - alpha) * chunkTime;
                }
                timeLeft = rlen == 0 ? 0 : (long) chunkTime * ((length-offset)/rlen);
                notifyObservers(new ImportEvent.FILE_UPLOAD_BYTES(
                        file.getAbsolutePath(), index, srcFiles.length, offset,
                        length, timeLeft, null));
            }

            digestString = cp.checksumAsString();

            OriginalFile ofile = rawFileStore.save();
            if (log.isDebugEnabled()) {
                log.debug(String.format("%s/%s id=%s",
                        ofile.getPath().getValue(),
                        ofile.getName().getValue(),
                        ofile.getId().getValue()));
                log.debug(String.format("checksums: client=%s,server=%s",
                        digestString, ofile.getHash().getValue()));
            }
            notifyObservers(new ImportEvent.FILE_UPLOAD_COMPLETE(
                    file.getAbsolutePath(), index, srcFiles.length,
                    offset, length, null));

        }
        catch (IOException e) {
            notifyObservers(new ImportEvent.FILE_UPLOAD_ERROR(
                    file.getAbsolutePath(), index, srcFiles.length,
                    null, null, e));
            throw e;
        }
        catch (ServerError e) {
            notifyObservers(new ImportEvent.FILE_UPLOAD_ERROR(
                    file.getAbsolutePath(), index, srcFiles.length,
                    null, null, e));
            throw e;
        }
        finally {
            cleanupUpload(rawFileStore, stream);
        }

        return digestString;
    }

    private void cleanupUpload(RawFileStorePrx rawFileStore,
            FileInputStream stream) throws ServerError {
        try {
            if (rawFileStore != null) {
                rawFileStore.close();
            }
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                }
                catch (Exception e) {
                    log.error("I/O error closing stream.", e);
                }
            }
        }

    }

    /**
     * Perform an image import uploading files if necessary.
     * @param container The import container which houses all the configuration
     * values and target for the import.
     * @param index Index of the import in a set. <code>0</code> is safe if
     * this is a singular import.
     * @param numDone Number of imports completed in a set. <code>0</code> is
     * safe if this is a singular import.
     * @param total Total number of imports in a set. <code>1</code> is safe
     * if this is a singular import.
     * @return List of Pixels that have been imported.
     * @throws FormatException If there is a Bio-Formats image file format
     * error during import.
     * @throws IOException If there is an I/O error.
     * @throws ServerError If there is an error communicating with the OMERO
     * server we're importing into.
     * @since OMERO Beta 4.2.1.
     */
    public List<Pixels> importImage(final ImportContainer container, int index,
                                    int numDone, int total)
            throws FormatException, IOException, Throwable
    {
        final ImportProcessPrx proc = createImport(container);
        final HandlePrx handle;
        final String[] srcFiles = container.getUsedFiles();
        final List<String> checksums = new ArrayList<String>();
        final byte[] buf = new byte[store.getDefaultBlockSize()];
        Map<Integer, String> failingChecksums = new HashMap<Integer, String>();

        notifyObservers(new ImportEvent.FILESET_UPLOAD_START(
                null, index, srcFiles.length, null, null, null));

        for (int i = 0; i < srcFiles.length; i++) {
            checksums.add(uploadFile(proc, srcFiles, i, checksumProviderFactory,
                    buf));
        }

        try {
            handle = proc.verifyUpload(checksums);
        } catch (ChecksumValidationException cve) {
            failingChecksums = cve.failingChecksums;
            throw cve;
        } finally {
            notifyObservers(new ImportEvent.FILESET_UPLOAD_END(
                    null, index, srcFiles.length, null, null, srcFiles,
                    checksums, failingChecksums, null));
        }

        // At this point the import is running, check handle for number of
        // steps.
        final ImportCallback cb = createCallback(proc, handle, container);
        cb.loop(60*60, 1000); // Wait 1 hr per step.
        final ImportResponse rsp = cb.getImportResponse();
        return rsp.pixels;
    }

    public ImportCallback createCallback(ImportProcessPrx proc,
        HandlePrx handle, ImportContainer container) throws ServerError {
        return new ImportCallback(proc, handle, container);
    }

    @SuppressWarnings("serial")
    public class ImportCallback extends CmdCallbackI {

        final ImportContainer container;

        final Long logFileId;

        /**
         * If null, then {@link #onFinished(Response, Status, Current)} has
         * not yet been called with a non-error response. Field is volatile
         * because a separate {@link Thread} will fill in the value.
         */
        volatile ImportResponse importResponse = null;

        public ImportCallback(ImportProcessPrx proc, HandlePrx handle,
                ImportContainer container) throws ServerError {
                super(oa, category, handle);
                this.container = container;
                this.logFileId = loadLogFile();
                initializationDone();
        }

        protected Long loadLogFile() throws ServerError {
            final ImportRequest req = (ImportRequest) handle.getRequest();
            final Long fsId = req.activity.getParent().getId().getValue();
            final IMetadataPrx metadataService = sf.getMetadataService();
            final List<String> nsToInclude = new ArrayList<String>(
                    Arrays.asList(omero.constants.namespaces.NSLOGFILE.value));
            final List<String> nsToExclude = new ArrayList<String>();
            final List<Long> rootIds = new ArrayList<Long>(Arrays.asList(fsId));
            final Parameters param = new ParametersI();
            Map<Long,List<Annotation>> annotationMap = new HashMap<Long,List<Annotation>>();
            List<Annotation> annotations = new ArrayList<Annotation>();
            Long ofId = null;
            try {
                annotationMap = metadataService.loadSpecifiedAnnotationsLinkedTo(
                        FileAnnotation.class.getName(), nsToInclude, nsToExclude,
                        Fileset.class.getName(), rootIds, param);
                if (annotationMap.containsKey(fsId)) {
                    annotations = annotationMap.get(fsId);
                    if (annotations.size() != 0) {
                        FileAnnotation fa = (FileAnnotationI) annotations.get(0);
                        ofId = fa.getFile().getId().getValue();
                    }
                }
            } catch (ServerError e) {
                ofId = null;
            }
            return ofId;
        }

        @Override
        public void step(int step, int total, Ice.Current current) {
            if (step == 1) {
                notifyObservers(new ImportEvent.METADATA_IMPORTED(
                        0, container.getFile().getAbsolutePath(),
                        null, null, 0, null, step, total, logFileId));
            } else if (step == 2) {
                notifyObservers(new ImportEvent.PIXELDATA_PROCESSED(
                        0, container.getFile().getAbsolutePath(),
                        null, null, 0, null, step, total, logFileId));
            } else if (step == 3) {
                notifyObservers(new ImportEvent.THUMBNAILS_GENERATED(
                        0, container.getFile().getAbsolutePath(),
                        null, null, 0, null, step, total, logFileId));
            } else if (step == 4) {
                notifyObservers(new ImportEvent.METADATA_PROCESSED(
                        0, container.getFile().getAbsolutePath(),
                        null, null, 0, null, step, total, logFileId));
            } else if (step == 5) {
                notifyObservers(new ImportEvent.OBJECTS_RETURNED(
                        0, container.getFile().getAbsolutePath(),
                        null, null, 0, null, step, total, logFileId));
            }
        }

        /**
         * Overridden to handle the end of the process.
         * @see CmdCallbackI#onFinished(Response, Status, Current)
         */
        @Override
        public void onFinished(Response rsp, Status status, Current c)
        {
            waitOnInitialization(); // Need non-null container
            ImportResponse rv = null;
            final ImportRequest req = (ImportRequest) handle.getRequest();
            final Fileset fs = req.activity.getParent();
            if (rsp instanceof ERR) {
                final ERR err = (ERR) rsp;
                final RuntimeException rt = new RuntimeException(
                        String.format(
                        "Failure response on import!\n" +
                        "Category: %s\n" +
                        "Name: %s\n" +
                        "Parameters: %s\n", err.category, err.name,
                        err.parameters));
                notifyObservers(new ErrorHandler.INTERNAL_EXCEPTION(
                        container.getFile().getAbsolutePath(), rt,
                        container.getUsedFiles(), container.getReader()));
            } else if (rsp instanceof ImportResponse) {
                rv = (ImportResponse) rsp;
                if (this.importResponse == null)
                {
                    // Only respond once.
                    notifyObservers(new ImportEvent.IMPORT_DONE(
                        0, container.getFile().getAbsolutePath(),
                        null, null, 0, null, rv.pixels, fs, rv.objects));
                }
                this.importResponse = rv;
            } else {
                final RuntimeException rt
                    = new RuntimeException("Unknown response: " + rsp);
                notifyObservers(new ErrorHandler.INTERNAL_EXCEPTION(
                        container.getFile().getAbsolutePath(), rt,
                        container.getUsedFiles(), container.getReader()));
            }
            onFinishedDone();
        }

        /**
         * Assumes that users have already waited on proper
         * completion, i.e. that {@link #onFinished(Response, Status, Current)}
         * has been called.
         *
         * @return may be null.
         */
        public ImportResponse getImportResponse()
        {
            waitOnFinishedDone();
            return importResponse;
        }
    }

    // ~ Helpers
    // =========================================================================

    /**
     * Retrieves the first managed repository from the list of current active
     * repositories.
     * @return Active proxy for the legacy repository.
     */
    private ManagedRepositoryPrx lookupManagedRepository()
    {
        try
        {
            ManagedRepositoryPrx rv = null;
            ServiceFactoryPrx sf = store.getServiceFactory();
            RepositoryMap map = sf.sharedResources().repositories();
            for (int i = 0; i < map.proxies.size(); i++)
            {
                RepositoryPrx proxy = map.proxies.get(i);
                if (proxy != null) {
                    rv = ManagedRepositoryPrxHelper.checkedCast(proxy);
                    if (rv != null) {
                        return rv;
                    }
                }
            }
            return null;
        }
        catch (ServerError e)
        {
            throw new RuntimeException(e);
        }
    }

    private void checkManagedRepo() {
        if (repo == null) {
            throw new RuntimeException("No FS! Cannot proceed");
        }
    }

    public void clear()
    {
        store.setGroup(null);
        store.setCurrentLogFile(null);
        store.createRoot();
    }

}
