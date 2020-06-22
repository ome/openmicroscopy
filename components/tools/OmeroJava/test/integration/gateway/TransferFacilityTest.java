/*
 * Copyright (C) 2020 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package integration.gateway;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.io.File;
import java.util.UUID;
import omero.gateway.model.DatasetData;
import omero.gateway.model.ImageData;

import org.testng.Assert;
import org.testng.annotations.Test;


public class TransferFacilityTest extends GatewayTest {

    /**
     * Creates the files to import and returns a collection of absolute paths.
     *
     * @param n The number of files to create.
     * @return See above
     */
    private List<String> createFiles(int n) throws Exception {

        List<String> paths = new ArrayList<String>(n);
        for (int i = 0; i < n; i++) {
            File file = File.createTempFile("testImportImages"+i, ".fake");
            file.deleteOnExit();
            Assert.assertNotNull(file);
            paths.add(file.getAbsolutePath());
        }
        return paths;
    }

    @Test
    public void testImportImageInDataset() throws Exception {
        //Create a tmp file and upload to OMERO
        List<String> paths = createFiles(1);

        DatasetData dataset = new DatasetData();
        dataset.setName(UUID.randomUUID().toString());
        dataset = (DatasetData) datamanagerFacility.createDataset(rootCtx, dataset, null);

        //Import the file
        Boolean result = transferFacility.uploadImagesDirect(rootCtx, paths, dataset);
        Assert.assertTrue(result.booleanValue());
        //Check the number of object in the dataset
        Collection<Long> ids = new ArrayList<Long>(1);
        ids.add(dataset.getId());
        Collection<ImageData> images = browseFacility.getImagesForDatasets(rootCtx, ids);
        Assert.assertEquals(images.size(), paths.size());
    }

    @Test
    public void testImportImageOrphan() throws Exception {
        List<String> paths = createFiles(1);

        //Import the file
        Boolean result = transferFacility.uploadImagesDirect(rootCtx, paths, null);
        Assert.assertTrue(result.booleanValue());
    }

    @Test
    public void testImportImagesInDataset() throws Exception {
        List<String> paths = createFiles(2);

        DatasetData dataset = new DatasetData();
        dataset.setName(UUID.randomUUID().toString());
        dataset = (DatasetData) datamanagerFacility.createDataset(rootCtx, dataset, null);

        //Import the file
        Boolean result = transferFacility.uploadImagesDirect(rootCtx, paths, dataset);
        Assert.assertTrue(result.booleanValue());
        //Check the number of object in the dataset
        Collection<Long> ids = new ArrayList<Long>(1);
        ids.add(dataset.getId());
        Collection<ImageData> images = browseFacility.getImagesForDatasets(rootCtx, ids);
        Assert.assertEquals(images.size(), paths.size());
    }
}
