/*
 *
 *------------------------------------------------------------------------------
 *  Copyright (C) 2006-2012 University of Dundee. All rights reserved.
 *
 *
 * 	This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 *------------------------------------------------------------------------------
 */
package integration;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import ome.formats.importer.IObservable;
import ome.formats.importer.IObserver;
import ome.formats.importer.ImportCandidates;
import ome.formats.importer.ImportConfig;
import ome.formats.importer.ImportContainer;
import ome.formats.importer.ImportEvent;
import ome.formats.importer.ImportLibrary;
import ome.formats.importer.OMEROWrapper;
import omero.model.Pixels;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;


/** 
 * Tests import methods exposed by the ImportLibrary.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @version 4.5
 * <small>
 * (<b>Internal version:</b> $Revision: $Date: $)
 * </small>
 * @since 4.5
 */
@Test(groups = {"import", "integration"})
public class ImportLibraryTest 
	extends AbstractServerTest
{

	/** The collection of files that have to be deleted. */
	private List<File> files;
	
	/**
	 * Overridden to initialize the list.
	 * @see AbstractServerTest#setUp()
	 */
    @Override
    @BeforeClass
    protected void setUp() 
    	throws Exception
    {
    	super.setUp();
    	files = new ArrayList<File>();
    }
    
	/**
	 * Overridden to delete the files.
	 * @see AbstractServerTest#tearDown()
	 */
    @Override
    @AfterClass
    public void tearDown() 
    	throws Exception
    {
    	Iterator<File> i = files.iterator();
    	while (i.hasNext()) {
			i.next().delete();
		}
    	files.clear();
    }
    
    /**
     * Returns the import candidates corresponding to the specified file.
     * 
     * @param f The file to handle.
     * @return See above.
     */
    private ImportCandidates getCandidates(File f)
    	throws Exception
    
    {
    	ImportConfig config = new ImportConfig();
		OMEROWrapper reader = new OMEROWrapper(config);
		String[] paths = new String[1];
		paths[0] = f.getAbsolutePath();
		IObserver o = new IObserver() {
	        public void update(IObservable importLibrary, ImportEvent event) {
	            
	        }
	    };
		return new ImportCandidates(reader, paths, o);
    }
    
    /**
     * Tests the <code>ImportCandidates</code> method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
	public void testImportCandidates()
		throws Exception
	{
    	File f = File.createTempFile("testImportCandidates"
				+ModelMockFactory.FORMATS[0], "."+ModelMockFactory.FORMATS[0]);
		mmFactory.createImageFile(f, ModelMockFactory.FORMATS[0]);
		files.add(f);
		ImportCandidates candidates = getCandidates(f);
		assertNotNull(candidates);
		assertNotNull(candidates.getContainers().get(0));
	}
    
    /**
     * Tests the <code>ImportImage</code> method using an import container
     * returned by the import candidates method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
	public void testImportImage()
		throws Exception
	{
    	File f = File.createTempFile("testImportImage"
				+ModelMockFactory.FORMATS[0], "."+ModelMockFactory.FORMATS[0]);
		mmFactory.createImageFile(f, ModelMockFactory.FORMATS[0]);
		files.add(f);
    	ImportConfig config = new ImportConfig();
		ImportLibrary library = new ImportLibrary(importer,
				new OMEROWrapper(config));
		ImportContainer ic = getCandidates(f).getContainers().get(0);
		ic = library.uploadFilesToRepository(ic);
		//library.importCandidates(new ImportConfig(), c);
		List<Pixels> pixels = library.importMetadataOnly(ic, 0, 0, 1);
		assertNotNull(pixels);
		assertEquals(pixels.size(), 1);
	}
    
    /**
     * Tests the <code>ImportImage</code> method using an import container
     * returned by the import candidates method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
	public void testImportMetadataOnly()
		throws Exception
	{
    	
	}
    
}
