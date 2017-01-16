/*
 *   Copyright 2006-2016 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.util.Iterator;
import java.util.List;

import omero.ServerError;
import omero.api.IScriptPrx;
import omero.api.RawFileStorePrx;
import omero.model.OriginalFile;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Collections of tests for the <code>RawFileStore</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class RawFileStoreTest extends AbstractServerTest {

    /**
     * Tests the upload of a file. This tests uses the <code>write</code>
     * method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testUploadFile() throws Exception {
        RawFileStorePrx svc = factory.createRawFileStore();
        // create an original file
        OriginalFile f = mmFactory.createOriginalFile();
        f = (OriginalFile) iUpdate.saveAndReturnObject(f);
        svc.setFileId(f.getId().getValue());
        byte[] data = new byte[] { 1 };
        svc.write(data, 0, data.length);
        OriginalFile ff = svc.save(); // save
        Assert.assertEquals(f.getId().getValue(), ff.getId().getValue());
        svc.close();
    }

    /**
     * Tests the download of a file. This tests uses the <code>read</code>
     * method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     * @see #testUploadFile()
     */
    @Test
    public void testDownloadFile() throws Exception {
        // first write the file. See
        RawFileStorePrx svc = factory.createRawFileStore();
        // create an original file
        OriginalFile f = mmFactory.createOriginalFile();
        f = (OriginalFile) iUpdate.saveAndReturnObject(f);
        svc.setFileId(f.getId().getValue());
        byte[] data = new byte[] { 1 };
        svc.write(data, 0, data.length);
        f = svc.save(); // save

        int size = (int) f.getSize().getValue();
        byte[] values = svc.read(0, size);
        Assert.assertNotNull(values);
        Assert.assertEquals(data.length, values.length);
        for (int i = 0; i < values.length; i++) {
            Assert.assertEquals(data[i], values[i]);
        }
        svc.close();
    }

    /**
     * Tests the download of the scripts. This tests uses the <code>read</code>
     * method.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     * @see #testUploadFile()
     */
    @Test
    public void testDownloadScript() throws Exception {
        IScriptPrx svc = factory.getScriptService();
        List<OriginalFile> scripts = svc.getScripts();
        Iterator<OriginalFile> i = scripts.iterator();
        OriginalFile f;
        RawFileStorePrx store;
        byte[] values;
        int size;
        Assert.assertFalse(scripts.isEmpty());
        while (i.hasNext()) {
            f = i.next();
            store = factory.createRawFileStore();
            store.setFileId(f.getId().getValue());
            size = (int) f.getSize().getValue();
            values = store.read(0, size);
            Assert.assertNotNull(values);
            Assert.assertNotEquals(values.length, 0);
            store.close();
        }
    }

    /**
     * Test that a sensible exception is thrown when a bad file ID is set.
     * @throws Exception for bad file ID
     */
    @Test(expectedExceptions = ServerError.class, groups = "broken")
    public void testBadFileId() throws Exception {
        newUserAndGroup("rw----");
        final RawFileStorePrx rfs = factory.createRawFileStore();
        try {
            rfs.setFileId(-1);
            Assert.fail("should not be able to open file with bad ID");
        } finally {
            rfs.close();
        }
    }
}
