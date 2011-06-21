/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;


//Java imports
import java.util.Iterator;
import java.util.List;

//Third-party libraries
import org.testng.annotations.Test;

//Application-internal dependencies
import omero.api.IScriptPrx;
import omero.api.RawFileStorePrx;
import omero.model.OriginalFile;

/** 
 * Collections of tests for the <code>RawFileStore</code> service.
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
public class RawFileStoreTest 
	extends AbstractTest
{

    /**
     * Tests the upload of a file. This tests uses the <code>write</code>
     * method.
     * @throws Exception Thrown if an error occurred.
     */
    @Test
    public void testUploadFile() 
    	throws Exception 
    {
    	RawFileStorePrx svc = factory.createRawFileStore();
    	//create an original file
    	OriginalFile f = mmFactory.createOriginalFile();
    	f = (OriginalFile) iUpdate.saveAndReturnObject(f);
    	svc.setFileId(f.getId().getValue());
    	byte[] data = new byte[]{1};
    	svc.write(data, 0, data.length);
    	OriginalFile ff = svc.save(); //save
    	assertTrue(ff.getId().getValue() == f.getId().getValue());
    	svc.close();
    }
    
    /**
     * Tests the download of a file. This tests uses the <code>read</code>
     * method.
     * @throws Exception Thrown if an error occurred.
     * @see #testUploadFile() 
     */
    @Test
    public void testDownloadFile() 
    	throws Exception 
    {
    	//first write the file. See 
    	RawFileStorePrx svc = factory.createRawFileStore();
    	//create an original file
    	OriginalFile f = mmFactory.createOriginalFile();
    	f = (OriginalFile) iUpdate.saveAndReturnObject(f);
    	svc.setFileId(f.getId().getValue());
    	byte[] data = new byte[]{1};
    	svc.write(data, 0, data.length);
    	f = svc.save(); //save
    	
    	int size = (int) f.getSize().getValue();
    	byte[] values = svc.read(0, size);
    	assertNotNull(values);
    	assertTrue(values.length == data.length);
    	for (int i = 0; i < values.length; i++) {
    		assertTrue(values[i] == data[i]);
		}
    	svc.close();
    }
    
    /**
     * Tests the download of the scripts. This tests uses the <code>read</code>
     * method.
     * @throws Exception Thrown if an error occurred.
     * @see #testUploadFile() 
     */
    @Test
    public void testDownloadScript() 
    	throws Exception 
    {
    	IScriptPrx svc = factory.getScriptService();
    	List<OriginalFile> scripts = svc.getScripts();
    	Iterator<OriginalFile> i = scripts.iterator();
    	OriginalFile f;
    	RawFileStorePrx store;
    	byte[] values;
    	int size;
    	assertTrue(scripts.size() > 0);
    	while (i.hasNext()) {
			f = i.next();
			store = factory.createRawFileStore();
			store.setFileId(f.getId().getValue());
			size = (int) f.getSize().getValue();
	    	values = store.read(0, size);
	    	assertNotNull(values);
	    	assertTrue(values.length > 0);
	    	store.close();
		}
    }
    
}
