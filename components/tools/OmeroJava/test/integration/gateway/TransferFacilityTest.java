/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2015 University of Dundee. All rights reserved.
 *
 *
 *  This program is free software; you can redistribute it and/or modify
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
package integration.gateway;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.List;
import java.util.Set;

import ome.specification.XMLMockObjects;
import ome.specification.XMLWriter;
import omero.gateway.exception.DSAccessException;
import omero.gateway.exception.DSOutOfServiceException;
import omero.gateway.exception.ImportException;
import omero.gateway.model.ImportCallback;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import pojos.PixelsData;

/**
 *
 * @author Dominik Lindner &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:d.lindner@dundee.ac.uk">d.lindner@dundee.ac.uk</a>
 * @since 5.1
 */

public class TransferFacilityTest extends GatewayTest {

    private File testFile;
    private long imgId;

    @Override
    @BeforeClass(alwaysRun = true)
    protected void setUp() throws Exception {
        super.setUp();
        initData();
    }

    @Test
    public void testUploadImage() throws DSAccessException,
            DSOutOfServiceException, ImportException {
        ImportCallback cb = new ImportCallback();
        transferFacility.uploadImage(rootCtx, testFile, cb);

        int c = 0;
        while (!cb.isFinished() && c < 10) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }

        Object obj = cb.getImportResult();
        PixelsData p = (PixelsData) ((Set) obj).iterator().next();
        imgId = p.getImage().getId();

        Assert.assertEquals(cb.getNumberOfImportedFiles(), 1);
        Assert.assertNull(cb.getException());
    }

    @Test(dependsOnMethods = { "testUploadImage" })
    public void testDownloadImage() throws Exception {
        File dstDir = new File(testFile.getParent(), "download");
        dstDir.mkdir();

        List<File> result = transferFacility.downloadImage(rootCtx,
                dstDir.getAbsolutePath(), imgId);

        Assert.assertEquals(result.size(), 1);

        File resultFile = result.get(0);

        Assert.assertEquals(resultFile.getName(), testFile.getName());
        Assert.assertEquals(getBytes(resultFile), getBytes(testFile));
        
        resultFile.delete();
        testFile.delete();
    }

    private void initData() throws Exception {
        testFile = File.createTempFile("testImportFile", ".ome");
        testFile.deleteOnExit();
        XMLMockObjects xml = new XMLMockObjects();
        XMLWriter writer = new XMLWriter();
        writer.writeFile(testFile, xml.createImage(), true);
    }

    private byte[] getBytes(File f) throws Exception {
        RandomAccessFile rf = new RandomAccessFile(f, "r");
        byte[] b = new byte[(int) rf.length()];
        rf.read(b);
        rf.close();
        return b;
    }
}
