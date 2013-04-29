/*
 * Copyright (C) 2013 Glencoe Software, Inc. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package integration.chgrp;

import integration.AbstractServerTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.testng.annotations.Test;

import omero.RString;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.util.TempFileManager;

/**
 */
public class MultiImageFilesetMoveTest extends AbstractServerTest {

    protected List<Image> importMIF(int seriesCount) throws Throwable {
        File fake = TempFileManager.create_path("importMIF",
                String.format("&series=%d.fake", seriesCount));
        List<Pixels> pixels = importFile(importer, fake, null, false, null);
        assertEquals(seriesCount, pixels.size());
        List<Image> images = new ArrayList<Image>();
        for (Pixels pixel : pixels) {
            images.add(pixel.getImage());
        }
        return images;
    }

    /**
     * Creates a list of the given number of {@link Dataset} instances with
     * names of the form "name [1]", "name [2]", etc. and
     * returns them in a list.
     * 
     * @param count
     * @param baseName
     * @return
     * @throws Throwable
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected List<Dataset> createDatasets(int count, String baseName)
            throws Throwable {

        List<IObject> rv = new ArrayList<IObject>();
        for (int i = 0; i < count; i++) {
            Dataset dataset = new DatasetI();
            String suffix = " [" + (i + 1) + "]";
            RString name = omero.rtypes.rstring(baseName + suffix);
            dataset.setName(name);
            rv.add(dataset);
        }
        return (List) iUpdate.saveAndReturnArray(rv);
    }

    /**
     * Simplest example of the MIF chgrp edge case: a single fileset containing
     * 2 images is split among 2 datasets. Each sibling CANNOT be moved
     * independently of the other.
     */
    @Test
    public void testBasicWorkflow() throws Throwable {
        List<Dataset> datasets = createDatasets(2, "testBasicWorkflow");
        List<Image> images = importMIF(2);
        for (int i = 0; i < 2; i++) {
            DatasetImageLink link = new DatasetImageLinkI();
            link.setParent(datasets.get(i));
            link.setChild(images.get(i));
            link = (DatasetImageLink) iUpdate.saveAndReturnObject(link);
        }
    }

}
