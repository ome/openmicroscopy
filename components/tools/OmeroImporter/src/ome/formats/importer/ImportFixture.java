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
import java.util.List;
import java.util.Map;

import loci.formats.FormatReader;
import ome.formats.OMEROMetadataStoreClient;
import omero.model.DatasetI;
import omero.model.Pixels;
import omero.model.PixelsI;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * test fixture for importing files without a GUI.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision: 1167 $, $Date: 2006-12-15 10:39:34 +0000 (Fri, 15 Dec 2006) $
 * @see OMEROMetadataStoreClient
 * @see ExampleUnitTest
 * @since 3.0-M3
 */
// @RevisionDate("$Date: 2006-12-15 10:39:34 +0000 (Fri, 15 Dec 2006) $")
// @RevisionNumber("$Revision: 1167 $")
public class ImportFixture
{

    Log                        log = LogFactory.getLog(ImportFixture.class);

    @SuppressWarnings("unused")
    private String             user, pass, host, port;

    private OMEROMetadataStoreClient store;

    private OMEROWrapper       reader;

    private ImportLibrary      library;

    private Map<File,DatasetI>  fads = new HashMap<File,DatasetI>();
    
    public ImportFixture(OMEROMetadataStoreClient store)
    {
        this(store, new OMEROWrapper());
    }

    public ImportFixture(OMEROMetadataStoreClient store, OMEROWrapper reader)
    {
        this.store = store;
        this.reader = reader;
    }

    public ImportFixture put(File file, DatasetI ds)
    {
    	if ( file == null || ds == null )
    	    // FIXME: Blitz transition, ApiUsageException no longer client side.
    		throw new RuntimeException("Arguments cannot be null.");
    	
    	fads.put(file, ds);
    	return this;
    }
    
    public ImportFixture putAll(Map<File,DatasetI> map)
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
     * <li>{@link ImportLibrary#importData(long, String, ome.formats.testclient.ImportLibrary.Step)}</li>
     * </ul>
     * 
     * @param step an action to take per plane. not null.
     * @throws Exception
     */
    public void doImport(ImportLibrary.Step step) throws Exception
    {
        if (step == null)
        {
            // FIXME: Blitz transition, ApiUsageException no longer client side.
            throw new Exception("Step may not be null.");
        }
        for (File file : fads.keySet())
        {
            String fileName = file.getAbsolutePath();
            library.setDataset(fads.get(file));
            library.open(fileName);
            List<Pixels> pixelsList =
            	library.importMetadata(file.getAbsolutePath());
            Pixels pixels = pixelsList.get(0);
            library.calculateImageCount(fileName, pixels);
            library.importData(pixels.getId().getValue(), fileName, 0, step);
        }
    }

    /**
     * runs import via
     * {@link #doImport(ome.formats.testclient.ImportLibrary.Step)} with an
     * empty {@link ImportLibrary.Step#step(int)} action.
     * 
     * @throws Exception
     */
    public void doImport() throws Exception
    {
        doImport(new ImportLibrary.Step()
        {

            @Override
            public void step(int series, int n)
            {}
        });
    }

    // ~ Helpers
	// =========================================================================
    
    private ImportContainer[] fadMap(Map<File,DatasetI> map)
    {
    	int size = map.keySet().size();
    	ImportContainer[] fads = new ImportContainer[size];
    	File[] files = map.keySet().toArray( new File[size] );
    	for (int i = 0; i < fads.length; i++) {
			fads[i] = new ImportContainer(files[i],null,map.get(files[i]).getId().getValue(),files[i].toString(), false);
		}
    	return fads;
    }
    
}
