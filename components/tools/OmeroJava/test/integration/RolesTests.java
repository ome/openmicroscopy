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
import java.util.Map;

import org.testng.annotations.BeforeClass;

import com.google.common.collect.ImmutableMap;

import ome.system.Login;
import omero.RLong;
import omero.RString;
import omero.RType;
import omero.SecurityViolation;
import omero.ServerError;
import omero.model.Dataset;
import omero.model.IObject;
import omero.model.Image;
import omero.model.OriginalFile;
import omero.model.Permissions;
import omero.model.PermissionsI;
import omero.model.Session;
import omero.sys.EventContext;
import omero.sys.ParametersI;
import omero.sys.Principal;
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

    /* These permissions do not permit anything.*/
    @SuppressWarnings("serial")
    private static final Permissions NO_PERMISSIONS = new PermissionsI("------") {
        @Override
        public boolean isDisallow(int restriction, Ice.Current c) {
            return true;
        }
    };

    /**
     * Get the current permissions for the given object.
     * @param object a model object previously retrieved from the server
     * @return the permissions for the object in the current context
     * @throws ServerError if the query caused a server error
     * (except for security violations, returns NO_PERMISSIONS)
     */
    protected Permissions getCurrentPermissions(IObject object) throws ServerError {
        final String objectClass = object.getClass().getSuperclass().getSimpleName();
        final long objectId = object.getId().getValue();
        try {
            final IObject objectRetrieved;
            if (objectClass.endsWith("Link")) {
                objectRetrieved = iQuery.findByQuery("FROM " + objectClass + " link JOIN FETCH link.child WHERE link.id = :id",
                        new ParametersI().addId(objectId), ALL_GROUPS_CONTEXT);
            } else {
                objectRetrieved = iQuery.get(objectClass, objectId, ALL_GROUPS_CONTEXT);
            }

            return objectRetrieved.getDetails().getPermissions();
        } catch (SecurityViolation sv) {
            return NO_PERMISSIONS;
        }
    }

    /**
     * Import an image with original file into a given dataset.
     * @param dataset dataset to which to import the image if not null
     * @return the original file and the imported image
     * @throws Exception if the import fails
     */
    protected List<IObject> importImageWithOriginalFile(Dataset dataset) throws Exception {
        final List<IObject> originalFileAndImage = new ArrayList<IObject>();
        final RString imageName = omero.rtypes.rstring(fakeImageFile.getName());
        final List<List<RType>> result = iQuery.projection(
                "SELECT id FROM OriginalFile WHERE name = :name ORDER BY id DESC LIMIT 1",
                new ParametersI().add("name", imageName));
        final long previousId = result.isEmpty() ? -1 : ((RLong) result.get(0).get(0)).getValue();
        List<String> path = Collections.singletonList(fakeImageFile.getPath());
        importFileset(path, path.size(), dataset);
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

    /**
     * Sudo to the given user.
     * @param targetName the name of a user
     * @return context for a session owned by the given user
     * @throws Exception if the sudo could not be performed
     */
    protected EventContext sudo(String targetName) throws Exception {
        final Principal principal = new Principal();
        principal.name = targetName;
        final Session session = factory.getSessionService().createSessionWithTimeout(principal, 100 * 1000);
        final omero.client client = newOmeroClient();
        final String sessionUUID = session.getUuid().getValue();
        client.createSession(sessionUUID, sessionUUID);
        return init(client);
    }
}
