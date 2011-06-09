/*
 *   Copyright (C) 2006-2011 University of Dundee & Open Microscopy Environment.
 *   All rights reserved.
 *
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package ome.formats.importer.util;

// Java imports
import java.io.File;
import java.util.UUID;

//import ome.formats.OMEROMetadataStore;
//import ome.formats.importer.ImportFixture;
//import ome.formats.importer.ImportLibrary;
//import ome.formats.importer.OMEROWrapper;
import ome.model.containers.Dataset;
import ome.model.core.Pixels;
import ome.system.ServiceFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.ResourceUtils;

/**
 * test fixture which uses a hard-coded file ("tinyTest.d3d.dv") from the 
 * classpath, and adds them to a new UUID-named dataset. 
 * 
 * @author Josh Moore, josh.moore at gmx.de
 * @version $Revision: 1167 $, $Date: 2006-12-15 10:39:34 +0000 (Fri, 15 Dec 2006) $
 * @see OMEROMetadataStore
 * @see ExampleUnitTest
 * @since 3.0-M3
 */
// @RevisionDate("$Date: 2006-12-15 10:39:34 +0000 (Fri, 15 Dec 2006) $")
// @RevisionNumber("$Revision: 1167 $")
public class TinyImportFixture //extends ImportFixture
{

	/** Hard-coded filename of the image to be imported */
	public final static String FILENAME = "tinyTest.d3d.dv";
	
    Log                        log = LogFactory.getLog(TinyImportFixture.class);

    private Dataset d;
    
    private ServiceFactory sf;
    
    public TinyImportFixture(ServiceFactory services) throws Exception
    {
        //super(new OMEROMetadataStore(services), new OMEROWrapper());
        this.sf = services;
    }

    /**
     * checks for the necessary fields and initializes the {@link ImportLibrary}
     * 
     * @throws Exception
     */
    public void setUp() throws Exception
    {
		d = new Dataset();
		d.setName(UUID.randomUUID().toString());
		d = sf.getUpdateService().saveAndReturnObject(d);
		
		File 	tinyTest = ResourceUtils.getFile("classpath:"+FILENAME);
		
		//super.put( tinyTest, d );
    	//super.setUp();
    }

	public void doImport() {}
	public void tearDown() {}
    
    /** provides access to the created {@link Dataset} instance.
     */
    public Dataset getDataset()
    {
    	return d;
    }
    
    public Pixels getPixels()
    {
        return sf.getQueryService().findByQuery("select p from Dataset d " +
        		"join d.imageLinks dil " +
        		"join dil.child img " +
        		"join img.pixels p where d.id = "+d.getId(), null);
    }
}
