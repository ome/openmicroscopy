/*
 * integration.ThumbnailStoreTest 
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2010 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
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
package integration;


//Java imports
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.ArrayList;

//Third-party libraries
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

//Application-internal dependencies
import ome.formats.OMEROMetadataStoreClient;
import omero.api.ThumbnailStorePrx;
import omero.model.Pixels;

/** 
 * Collections of tests for the <code>ThumbnailStore</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
 * @version 3.0
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 3.0-Beta4
 */
@Test(groups = { "client", "integration", "blitz" })
public class ThumbnailStoreTest 
	extends AbstractTest
{

	/** Reference to the importer store. */
	private OMEROMetadataStoreClient importer;
	
	/**
	 * Overridden to initialize the list.
	 * @see AbstractTest#setUp()
	 */
    @Override
    @BeforeClass
    protected void setUp() 
    	throws Exception
    {
    	super.setUp();
    	importer = new OMEROMetadataStoreClient();
    	importer.initialize(factory);
    }
    
	/**
     * Test to retrieve the newly created image.
     * Tests thumbnailService methods: getThumbnail(rint, rint) 
     * and getThumbnailByLongestSide(rint)
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testGetThumbnail() 
    	throws Exception
    {
    	ThumbnailStorePrx svc = factory.createThumbnailStore();
    	//first import an image already tested see ImporterTest
    	String format = ModelMockFactory.FORMATS[0];
    	File f = File.createTempFile("testImportGraphicsImages"+format, 
				"."+format);
    	mmFactory.createImageFile(f, format);
    	List<Pixels> pixels = null;
		try {
			pixels = importFile(importer, f, format, false);
		} catch (Throwable e) {
			throw new Exception("cannot import image", e);
		}
		f.delete();
		Pixels p = pixels.get(0);
		long pixelsID = p.getId().getValue();
		if (!(svc.setPixelsId(pixelsID))) {
			svc.resetDefaults();
			svc.setPixelsId(pixelsID);
		}
		int sizeX = 48;
		int sizeY = 48;
		byte[] values = svc.getThumbnail(omero.rtypes.rint(sizeX), 
				omero.rtypes.rint(sizeY));
		assertNotNull(values);
		assertTrue(values.length > 0);
		
		byte[] lsValues = svc.getThumbnailByLongestSide(omero.rtypes.rint(sizeX));
		assertNotNull(lsValues);
		assertTrue(lsValues.length > 0);
    }
    
    /**
     * Tests thumbnailService methods: getThumbnailSet(rint, rint, list<long>) 
     * and getThumbnailByLongestSideSet(rint, list<long>)
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testGetThumbnailSet() 
    	throws Exception
    {
    	ThumbnailStorePrx svc = factory.createThumbnailStore();
    	//first import an image already tested see ImporterTest
    	String format = ModelMockFactory.FORMATS[0];
    	File f = File.createTempFile("testImportGraphicsImages"+format, 
				"."+format);
    	mmFactory.createImageFile(f, format);
    	List<Long> pixelsIds = new ArrayList<Long>();
    	int thumbNailCount = 20;
		try {
		    for (int i=0; i<thumbNailCount; i++) {
			    List<Pixels> pxls = importFile(importer, f, format, false);
			    pixelsIds.add(pxls.get(0).getId().getValue());
			}
		} catch (Throwable e) {
			throw new Exception("cannot import image", e);
		}
		f.delete();
		
		int sizeX = 48;
		int sizeY = 48;
		Map<Long, byte[]> thmbs = svc.getThumbnailSet(omero.rtypes.rint(sizeX), omero.rtypes.rint(sizeY), pixelsIds);
		Map<Long, byte[]> lsThmbs = svc.getThumbnailByLongestSideSet(omero.rtypes.rint(sizeX), pixelsIds);
		Iterator it = thmbs.entrySet().iterator();
		byte[] t = null;
		int tnCount = 0;
		Map.Entry keyValue;
		while (it.hasNext()) {
		    keyValue = (Map.Entry)it.next();
		    t = (byte[])keyValue.getValue();
		    assertNotNull(t);
		    assertNotNull(t.length > 0);
		    tnCount++;
		}
		assertEquals(tnCount, thumbNailCount);
		
		Iterator lit = lsThmbs.entrySet().iterator();
		tnCount = 0;
		while (lit.hasNext()) {
		    keyValue = (Map.Entry)lit.next();
		    t = (byte[])keyValue.getValue();
		    assertNotNull(t);
		    assertNotNull(t.length > 0);
		    tnCount++;
		}
		assertEquals(tnCount, thumbNailCount);
	}
    
}
