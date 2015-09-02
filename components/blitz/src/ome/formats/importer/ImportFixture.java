/*
 * ome.formats.testclient.ImportFixture
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
package ome.formats.importer;

// Java imports
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import loci.formats.FormatReader;
import ome.formats.OMEROMetadataStoreClient;
import omero.model.Dataset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * test fixture for importing files without a GUI.
 *
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision: 1167 $, $Date: 2006-12-15 10:39:34 +0000 (Fri, 15 Dec 2006) $
 * @see OMEROMetadataStoreClient
 * @see ExampleUnitTest
 * @since 3.0-M3
 */
public class ImportFixture
{

    Logger                        log = LoggerFactory.getLogger(ImportFixture.class);

    @SuppressWarnings("unused")

    private OMEROMetadataStoreClient store;

    private OMEROWrapper       reader;

    private ImportLibrary      library;

    private Map<File,Dataset>  fads = new HashMap<File, Dataset>();

    public ImportFixture(OMEROMetadataStoreClient store, OMEROWrapper reader)
    {
        this.store = store;
        this.reader = reader;
    }

    public ImportFixture put(File file, Dataset ds)
    {
	if ( file == null || ds == null )
	    // FIXME: Blitz transition, ApiUsageException no longer client side.
		throw new RuntimeException("Arguments cannot be null.");

	fads.put(file, ds);
	return this;
    }

    public ImportFixture putAll(Map<File, Dataset> map)
    {
	for (File f : map.keySet()) {
			put(f,map.get(f));
		}
	return this;
    }

    /**
     * checks for the necessary fields and initializes the {@link ImportLibrary}
     *
     * @throws Exception
     */
    public void setUp() throws Exception
    {
        this.library = new ImportLibrary(store, reader);
    }

    /**
     * sets {@link ImportLibrary}, {@link OMEROMetadataStoreClient}, and file array
     * to null. Also attempts to call {@link FormatReader#close()}.
     */
    public void tearDown()
    {
        this.fads = null;
        this.store = null;
        this.library = null;
        try
        {
            this.reader.close();
        } catch (Exception e)
        {
            throw new RuntimeException(e);
        } finally
        {
            this.reader = null;
        }
    }

    /**
     * runs import by looping through all files and then calling:
     * <ul>
     * <li>{@link ImportLibrary#open(String)}</li>
     * <li>{@link ImportLibrary#calculateImageCount(String)}</li>
     * <li>{@link ImportLibrary#importMetadata()}</li>
     * <li>{@link ImportLibrary#importData(long, String)}</li>
     * </ul>
     *
     * @param step an action to take per plane. not null.
     * @throws Exception
     */
    public void doImport() throws Throwable
    {
    	ImportContainer ic;
        for (File file : fads.keySet())
        {
		ic = new ImportContainer(file, fads.get(file),
					null, null, null, null);
		ic.setUserSpecifiedName(file.getAbsolutePath());
		library.importImage(ic, 0, 0, 1);
        /*
		library.importImage(file, 0, 0, 1, file.getAbsolutePath(),
				        null,
				        false,  // To archive?
				        false,  // Create a metadata file?
				        null,
				        fads.get(file));
				        */
        }
    }

}
