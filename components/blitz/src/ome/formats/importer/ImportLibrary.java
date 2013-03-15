/*
 * ome.formats.importer.ImportLibrary
 *
 *------------------------------------------------------------------------------
 *
 *  Copyright (C) 2005 Open Microscopy Environment
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
import java.util.List;

import loci.formats.FormatException;
import loci.formats.FormatReader;
import ome.formats.OMEROMetadataStoreClient;
import ome.formats.importer.util.ErrorHandler;
import ome.services.blitz.repo.path.ClientFilePathTransformer;
import ome.services.blitz.repo.path.MakePathComponentSafe;
import ome.util.checksum.ChecksumProvider;
import ome.util.checksum.ChecksumProviderFactory;
import ome.util.checksum.ChecksumProviderFactoryImpl;
import ome.util.checksum.ChecksumType;
import omero.ServerError;
import omero.api.RawFileStorePrx;
import omero.api.ServiceFactoryPrx;
import omero.cmd.CmdCallbackI;
import omero.cmd.ERR;
import omero.cmd.HandlePrx;
import omero.cmd.Response;
import omero.grid.ImportProcessPrx;
import omero.grid.ImportRequest;
import omero.grid.ImportResponse;
import omero.grid.ImportSettings;
import omero.grid.ManagedRepositoryPrx;
import omero.grid.ManagedRepositoryPrxHelper;
import omero.grid.RepositoryMap;
import omero.grid.RepositoryPrx;
import omero.model.Dataset;
import omero.model.Fileset;
import omero.model.FilesetI;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.Screen;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
    private static Log log = LogFactory.getLog(ImportLibrary.class);

    /** The class used to identify the dataset target.*/
    private static final String DATASET_CLASS = "omero.model.Dataset";

    /** The class used to identify the screen target.*/
    private static final String SCREEN_CLASS = "omero.model.Screen";

    private static final ClientFilePathTransformer sanitizer = 
            new ClientFilePathTransformer(new MakePathComponentSafe());
    
    private final ArrayList<IObserver> observers = new ArrayList<IObserver>();

    private final OMEROMetadataStoreClient store;

    private final ManagedRepositoryPrx repo;

    private final ChecksumProviderFactory checksumProviderFactory =
            new ChecksumProviderFactoryImpl();

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

        final ImportSettings settings = new ImportSettings();
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
        final List<String> checksums = new ArrayList<String>();
        final byte[] buf = new byte[store.getDefaultBlockSize()];
        final int fileTotal = srcFiles.length;

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

        ChecksumProvider cp = cpf.getProvider(ChecksumType.SHA1);
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

            notifyObservers(new ImportEvent.FILE_UPLOAD_STARTED(
                    file.getAbsolutePath(), index, srcFiles.length,
                    null, length, null));

            // "touch" the file otherwise zero-length files
            rawFileStore.write(new byte[0], offset, 0);
            notifyObservers(new ImportEvent.FILE_UPLOAD_BYTES(
                    file.getAbsolutePath(), index, srcFiles.length,
                    offset, length, null));

            while (stream.available() != 0) {
                rlen = stream.read(buf);
                cp.putBytes(buf, 0, rlen);
                rawFileStore.write(buf, offset, rlen);
                offset += rlen;
                notifyObservers(new ImportEvent.FILE_UPLOAD_BYTES(
                        file.getAbsolutePath(), index, srcFiles.length,
                        offset, length, null));
            }

            digestString = cp.checksumAsString();

            OriginalFile ofile = rawFileStore.save();
            if (log.isDebugEnabled()) {
                log.debug(String.format("%s/%s id=%s",
                        ofile.getPath().getValue(),
                        ofile.getName().getValue(),
                        ofile.getId().getValue()));
                log.debug(String.format("checksums: client=%s,server=%s",
                        digestString, ofile.getSha1().getValue()));
            }
            notifyObservers(new ImportEvent.FILE_UPLOAD_COMPLETE(
                    file.getAbsolutePath(), index, srcFiles.length,
                    offset, length, digestString, null));

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
        final String[] srcFiles = container.getUsedFiles();
        final List<String> checksums = new ArrayList<String>();
        final byte[] buf = new byte[store.getDefaultBlockSize()];

        notifyObservers(new ImportEvent.FILESET_UPLOAD_START(
                null, index, srcFiles.length, null, null, null));

        for (int i = 0; i < srcFiles.length; i++) {
            checksums.add(uploadFile(proc, srcFiles, i, checksumProviderFactory, buf));
        }

        notifyObservers(new ImportEvent.FILESET_UPLOAD_END(
                null, index, srcFiles.length, null, null, null));

        // At this point the import is running, check handle for number of
        // steps.
        final HandlePrx handle = proc.verifyUpload(checksums);
        final ImportRequest req = (ImportRequest) handle.getRequest();
        final Fileset fs = req.activity.getParent();
        final CmdCallbackI cb = createCallback(proc, handle, container);
        cb.loop(60*60, 1000); // Wait 1 hr per step.
        final ImportResponse rsp = getImportResponse(cb, container, fs);
        return rsp.pixels;
    }

    @SuppressWarnings("serial")
    public CmdCallbackI createCallback(ImportProcessPrx proc, HandlePrx handle,
            final ImportContainer container) throws ServerError
    {
        // Adapter which should be used for callbacks. This is more
        // complicated than it needs to be at the moment. We're only sure that
        // the OMSClient has a ServiceFactory (and not necessarily a client)
        // so we have to inspect various fields to get the adapter.
        final ServiceFactoryPrx sf = store.getServiceFactory();
        final Ice.ObjectAdapter oa = sf.ice_getConnection().getAdapter();
        final Ice.Communicator ic = oa.getCommunicator();
        final String category = omero.client.getRouter(ic).getCategoryForClient();

        return new CmdCallbackI(oa, category, handle) {
            public void step(int step, int total, Ice.Current current) {
                if (step == 1) {
                    notifyObservers(new ImportEvent.METADATA_IMPORTED(
                            0, container.getFile().getAbsolutePath(),
                            null, null, 0, null, step, total));
                } else if (step == 2) {
                    notifyObservers(new ImportEvent.PIXELDATA_PROCESSED(
                            0, container.getFile().getAbsolutePath(),
                            null, null, 0, null, step, total));
                } else if (step == 3) {
                    notifyObservers(new ImportEvent.THUMBNAILS_GENERATED(
                            0, container.getFile().getAbsolutePath(),
                            null, null, 0, null, step, total));
                } else if (step == 4) {
                    notifyObservers(new ImportEvent.METADATA_PROCESSED(
                            0, container.getFile().getAbsolutePath(),
                            null, null, 0, null, step, total));
                } else if (step == 5) {
                    notifyObservers(new ImportEvent.OBJECTS_RETURNED(
                            0, container.getFile().getAbsolutePath(),
                            null, null, 0, null, step, total));
                }
            }
        };
    }

    /**
     * Returns a non-null {@link ImportResponse} or throws notifies observers
     * and throws a {@link RuntimeException}.
     * @param cb
     * @param container
     * @return
     */
    public ImportResponse getImportResponse(CmdCallbackI cb,
            final ImportContainer container, Fileset fs)
    {
        Response rsp = cb.getResponse();
        ImportResponse rv = null;
        if (rsp instanceof ERR) {
            final ERR err = (ERR) rsp;
            final RuntimeException rt = new RuntimeException(String.format(
                    "Failure response on import!\n" +
                    "Category: %s\n" +
                    "Name: %s\n" +
                    "Parameters: %s\n", err.category, err.name,
                    err.parameters));
            notifyObservers(new ErrorHandler.INTERNAL_EXCEPTION(
                    container.getFile().getAbsolutePath(), rt,
                    container.getUsedFiles(), container.getReader()));
            throw rt;
        } else if (rsp instanceof ImportResponse) {
            rv = (ImportResponse) rsp;
        }

        if (rv == null) {
            final RuntimeException rt
                = new RuntimeException("Unknown response: " + rsp);
            notifyObservers(new ErrorHandler.INTERNAL_EXCEPTION(
                    container.getFile().getAbsolutePath(), rt,
                    container.getUsedFiles(), container.getReader()));
            throw rt;
        }

        notifyObservers(new ImportEvent.IMPORT_DONE(
                0, container.getFile().getAbsolutePath(),
                null, null, 0, null, rv.pixels, fs, rv.objects));

        return rv;
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
        store.createRoot();
    }

}
