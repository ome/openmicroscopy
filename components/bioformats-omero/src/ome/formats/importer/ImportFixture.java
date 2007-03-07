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
import ome.conditions.ApiUsageException;
import ome.formats.OMEROMetadataStore;
import ome.model.containers.Dataset;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * test fixture for importing files without a GUI.
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision: 1167 $, $Date: 2006-12-15 10:39:34 +0000 (Fri, 15 Dec 2006) $
 * @see OMEROMetadataStore
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

    private OMEROMetadataStore store;

    private OMEROWrapper       reader;

    private ImportLibrary      library;

    private Map<File,Dataset>  fads = new HashMap<File,Dataset>();
    
    public ImportFixture(OMEROMetadataStore store)
    {
        this(store, new OMEROWrapper());
    }

    public ImportFixture(OMEROMetadataStore store, OMEROWrapper reader)
    {
        this.store = store;
        this.reader = reader;
    }

    public ImportFixture put(File file, Dataset ds)
    {
    	if ( file == null || ds == null )
    		throw new ApiUsageException("Arguments cannot be null.");
    	
    	fads.put(file, ds);
    	return this;
    }
    
    public ImportFixture putAll(Map<File,Dataset> map)
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
        this.library = new ImportLibrary(store, reader, fadMap(this.fads));
    }

    /**
     * sets {@link ImportLibrary}, {@link OMEROMetadataStore}, and file array
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
            throw new ApiUsageException("Step may not be null.");
        }
        for (File file : fads.keySet())
        {
            String fileName = file.getAbsolutePath();
            library.setDataset(fads.get(file));
            library.open(fileName);
            library.calculateImageCount(fileName);
            long pixId = library.importMetadata(file.getAbsolutePath());
            library.importData(pixId, fileName, step);
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
            public void step(int n)
            {}
        });
    }

    // ~ Helpers
	// =========================================================================
    
    private ImportContainer[] fadMap(Map<File,Dataset> map)
    {
    	int size = map.keySet().size();
    	ImportContainer[] fads = new ImportContainer[size];
    	File[] files = map.keySet().toArray( new File[size] );
    	for (int i = 0; i < fads.length; i++) {
			fads[i] = new ImportContainer(files[i],map.get(files[i]),files[i].toString(), false);
		}
    	return fads;
    }
    
}
