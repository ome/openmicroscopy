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
import java.util.Map;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import omero.RString;
import omero.api.IUpdatePrx;
import omero.cmd.Chgrp;
import omero.cmd.ChgrpERR;
import omero.cmd.Delete;
import omero.cmd.Response;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.DatasetImageLink;
import omero.model.DatasetImageLinkI;
import omero.model.ExperimenterGroup;
import omero.model.FilesetI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.Pixels;
import omero.sys.EventContext;
import omero.util.TempFileManager;

/**
 */
public class MultiImageFilesetMoveTest extends AbstractServerTest {

    ExperimenterGroup secondGroup;

    @BeforeClass
    public void setupSecondGroup() throws Exception {
        EventContext ec = iAdmin.getEventContext();
        secondGroup = newGroupAddUser("rwrw--", ec.userId);
        iAdmin.getEventContext(); // Refresh.
    }

    static class Fixture {
        final List<Dataset> datasets;
        final List<Image> images;
        Fixture(List<Dataset> datasets, List<Image> images) {
            this.datasets = datasets;
            this.images = images;
        }
        DatasetImageLink link(IUpdatePrx iUpdate, int datasetIndex, int imageIndex) throws Exception {
            DatasetImageLink link = new DatasetImageLinkI();
            link.setParent(datasets.get(datasetIndex));
            link.setChild(images.get(imageIndex));
            link = (DatasetImageLink) iUpdate.saveAndReturnObject(link);
            return link;
        }
    }

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


    protected Fixture createFixture(int datasetCount, int imageCount) throws Throwable {
        List<Dataset> datasets = createDatasets(datasetCount, "MIF");
        List<Image> images = importMIF(imageCount);
        return new Fixture(datasets, images);
    }

    /**
     * Simplest example of the MIF chgrp edge case: a single fileset containing
     * 2 images is split among 2 datasets. Each sibling CANNOT be moved
     * independently of the other.
     */
    @Test(groups = {"fs", "integration"})
    public void testBasicProblem() throws Throwable {
        Fixture f = createFixture(2, 2);
        f.link(iUpdate, 0, 0);
        f.link(iUpdate, 1, 1);

        long img0 = f.images.get(0).getId().getValue();
        long img1 = f.images.get(1).getId().getValue();
        long fs0 = f.images.get(0).getFileset().getId().getValue();
        long fs1 = f.images.get(1).getFileset().getId().getValue();
        assertEquals(fs0, fs1);

        Chgrp command = new Chgrp("/Image", img0,
                null, secondGroup.getId().getValue());

        Response rsp = doChange(client, factory, command, false); // Don't pass
        ChgrpERR err = (ChgrpERR) rsp;
        Map<String, long[]> constraints = err.constraints;
        long[] filesetIds = constraints.get("Fileset");
        assertEquals(1, filesetIds.length);
        assertEquals(fs0, filesetIds[0]);

        // However, it should still be possible to delete the 2 images
        // and have the fileset cleaned up.
        delete(true, client, new Delete("/Image", img0, null),
                new Delete("/Image", img1, null));

        // FIXME: This needs to be worked on. The fileset still exists.
        assertDoesNotExist(new FilesetI(fs0, false));
    }

}
