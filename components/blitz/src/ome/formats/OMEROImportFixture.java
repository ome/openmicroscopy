/*
 *   $Id$
 *
 *   Copyright 2009 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats;

// Java imports
import java.io.File;
import java.util.List;

import loci.formats.FormatReader;
import ome.formats.importer.IObservable;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import ome.formats.importer.util.ErrorHandler; // Was previously cli for sending debug text
import ome.formats.importer.util.ErrorHandler.EXCEPTION_EVENT;
import omero.model.Pixels;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * test fixture for importing files without a GUI. Sample usage:
 *
 * <pre>
 * OMEROMetadataStoreClient client = new OMEROMetadataStoreClient(sf);
 * OMEROImportFixture fixture = new OMEROImportFixture(client);
 * fixture.setUp();
 * fixture.setFile(ResourceUtils.getFile(&quot;classpath:tinyTest.d3d.dv&quot;));
 * fixture.setName(name);
 * fixture.doImport();
 * List&lt;Pixels&gt; p = fixture.getPixels();
 * fixture.tearDown();
 * i.setName(name);
 * i = userSave(i);
 * </pre>
 *
 * This class is <em>not</em> thread safe.
 *
 * @author Josh Moore, josh.moore at gmx.de
 * @see OMEROMetadataStoreClient
 * @since 4.0
 */
public class OMEROImportFixture {

    Logger log = LoggerFactory.getLogger(OMEROImportFixture.class);

    protected OMEROMetadataStoreClient store;

    private OMEROWrapper reader;

    private ImportLibrary library;

    private File file;

    private List<Pixels> pixels;

    private EXCEPTION_EVENT exception = null;

    @SuppressWarnings("unused")
	private String name;

    public OMEROImportFixture(OMEROMetadataStoreClient store,
            OMEROWrapper reader) {
        this.store = store;
        this.reader = reader;
    }

    /**
     * checks for the necessary fields and initializes the {@link ImportLibrary}
     *
     * @throws Exception if the import library could not be instantiated
     */
    public void setUp() throws Exception {
        this.library = new ImportLibrary(store, reader);
    }

    /**
     * sets {@link ImportLibrary}, {@link OMEROMetadataStore}, and file array to
     * null. Also attempts to call {@link FormatReader#close()}.
     */
    public void tearDown() {
        if (this.store != null) {
            this.store.logout();
            this.store = null;
        }

        this.library = null;

        try {
            if (this.reader != null) {
                this.reader.close();
                this.reader = null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            this.reader = null;
        }
    }

    /**
     * Provides one complete import cycle.
     * @param f the file to import
     * @param name the name (ignored)
     * @return the {@link Pixels} instance(s) created by the import
     * @throws Exception if the import failed
     */
    public List<Pixels> fullImport(File f, String name) throws Exception {
        this.setUp();
        try {
            this.setFile(f);
            this.setName(name);
            this.doImport();
            return this.getPixels();
        } finally {
            this.tearDown();
        }
    }

    /**
     * Runs import by looping through all files and then calling
     * {@link ImportLibrary#importCandidates(ImportConfig, ImportCandidates)}.
     * @throws Exception if import failed in a way that is not handled by an {@link EXCEPTION_EVENT}
     */
    public void doImport() throws Exception {
        String fileName = file.getAbsolutePath();
        ImportConfig config = new ImportConfig();
        ErrorHandler handler = new ErrorHandler(config) {
            @Override
            public void onUpdate(IObservable importLibrary, ImportEvent event) {
                // super.onUpdate(importLibrary, event);
                // Was previously cli.ErrorHandler, which sends feedback.
                if (event instanceof ImportEvent.IMPORT_DONE) {
                    pixels = ((ImportEvent.IMPORT_DONE) event).pixels;
                } else if (event instanceof EXCEPTION_EVENT) {
                    exception = (EXCEPTION_EVENT) event;
                }
            }
        };
        ImportCandidates candidates = new ImportCandidates(reader, new String[]{fileName}, handler);
        library.addObserver(handler);
        library.importCandidates(config, candidates);
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Accessor for the created pixels. Should be called before the next call to
     * {@link #doImport()}
     * @return the {@link Pixels} instance(s) created by the import
     * @throws Exception from an {@link EXCEPTION_EVENT} if the import failed
     */
    public List<Pixels> getPixels() throws Exception {
        if (exception != null) {
            throw exception.exception;
        }
        return this.pixels;
    }

}
