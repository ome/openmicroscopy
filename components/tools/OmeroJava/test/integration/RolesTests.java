/*
 * Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
 * All rights reserved.
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

package integration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.testng.annotations.BeforeClass;

import omero.RLong;
import omero.RString;
import omero.RType;
import omero.model.Dataset;
import omero.model.IObject;
import omero.model.Image;
import omero.model.OriginalFile;
import omero.sys.ParametersI;
import omero.util.TempFileManager;

/**
 * Helper methods class for supporting of "new role" workflow
 * and security tests.
 * @author p.walczysko@dundee.ac.uk
 * @since 5.4.0
 */
public class RolesTests extends AbstractServerImportTest {

    private static final TempFileManager TEMPORARY_FILE_MANAGER = new TempFileManager(
            "test-" + LightAdminRolesTest.class.getSimpleName());

    protected File fakeImageFile = null;

    /**
     * Create a fake image file for use in import tests.
     * @throws IOException unexpected
     */
    @BeforeClass
    public void createFakeImageFile() throws IOException {
        final File temporaryDirectory = TEMPORARY_FILE_MANAGER.createPath("images", null, true);
        fakeImageFile = new File(temporaryDirectory, "image.fake");
        fakeImageFile.createNewFile();
    }

    /**
     * Import an image with original file into a given dataset.
     * @param dat dataset to which to import the image if not null
     * @return the original file and the imported image
     * @throws Exception unexpected
     */
    protected List<IObject> importImageWithOriginalFile(Dataset dat) throws Exception {
        final List<IObject> originalFileAndImage = new ArrayList<IObject>();
        final RString imageName = omero.rtypes.rstring(fakeImageFile.getName());
        final List<List<RType>> result = iQuery.projection(
                "SELECT id FROM OriginalFile WHERE name = :name ORDER BY id DESC LIMIT 1",
                new ParametersI().add("name", imageName));
        final long previousId = result.isEmpty() ? -1 : ((RLong) result.get(0).get(0)).getValue();
        List<String> path = Collections.singletonList(fakeImageFile.getPath());
        importFileset(path, path.size(), dat);
        final OriginalFile remoteFile = (OriginalFile) iQuery.findByQuery(
                "FROM OriginalFile o WHERE o.id > :id AND o.name = :name",
                new ParametersI().addId(previousId).add("name", imageName));
        originalFileAndImage.add(remoteFile);
        final Image image = (Image) iQuery.findByQuery(
                "FROM Image WHERE fileset IN "
                + "(SELECT fileset FROM FilesetEntry WHERE originalFile.id = :id)",
                new ParametersI().addId(remoteFile.getId()));
        originalFileAndImage.add(image);
        return originalFileAndImage;
    }

}
