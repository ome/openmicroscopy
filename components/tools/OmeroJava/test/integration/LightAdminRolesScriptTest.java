/*
 *------------------------------------------------------------------------------
 *  Copyright (C) 2017 University of Dundee & Open Microscopy Environment.
 *  All rights reserved.
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
package integration;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import omero.SecurityViolation;
import omero.ServerError;
import omero.api.IScriptPrx;
import omero.api.RawFileStorePrx;
import omero.gateway.util.Requests;
import omero.gateway.util.Requests.Delete2Builder;
import omero.model.FileAnnotation;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.OriginalFile;
import omero.model.OriginalFileI;
import omero.model.enums.AdminPrivilegeChown;
import omero.model.enums.AdminPrivilegeDeleteScriptRepo;
import omero.model.enums.AdminPrivilegeWriteFile;
import omero.model.enums.AdminPrivilegeWriteOwned;
import omero.model.enums.AdminPrivilegeWriteScriptRepo;
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.testng.Assert;
import org.testng.annotations.Test;


/**
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
 * <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @since 5.4
 */
public class LightAdminRolesScriptTest extends RolesTests {

    /**
     * Light admin (lightAdmin) tries to upload a File Attachment (fileAnnotation)
     * with original file (originalFile) into a group they are not member of (normalUser's group).
     * lightAdmin then tries to link fileAnnotation to an image of the user (normalUser).
     * lightAdmin then tries to transfer the ownership of the fileAnnotation and link to normalUser.
     * @param permChown if to test a user who has the <tt>Chown</tt> privilege
     * @param permWriteOwned if to test a user who has the <tt>WriteOwned</tt> privilege
     * @param permWriteFile if to test a user who has the <tt>WriteFile</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     * @see <a href="https://downloads.openmicroscopy.org/resources/experimental/tests/graph-permissions/0.1/testFileAttachmentNoSudo.pptx">graphical explanation</a>
     */
    @Test(dataProvider = "fileAttachment privileges cases")
    public void testFileAttachmentNoSudo(boolean permChown, boolean permWriteOwned,
            boolean permWriteFile, String groupPermissions) throws Exception {
        /* Upload or creation of fileAttachment in not-your-group is permitted for lightAdmin
         * with WriteOwned and WriteFile permissions.*/
        boolean isExpectSuccessCreateFileAttachment = permWriteOwned && permWriteFile;
        /* Linking of fileAttachment to others' image is permitted when the creation
         * in not-your-group is permitted in all group types except private.*/
        boolean isExpectSuccessLinkFileAttachemnt = isExpectSuccessCreateFileAttachment && !(groupPermissions == "rw----");
        /* Chown permission is needed for lightAdmin for successful transfer of ownership of the
         * fileAttachment to normalUser.*/
        boolean isExpectSuccessCreateFileAttAndChown = isExpectSuccessCreateFileAttachment && permChown;
        boolean isExpectSuccessCreateLinkAndChown = isExpectSuccessLinkFileAttachemnt && permChown;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* Set up the light admin's permissions for this test.*/
        List<String> permissions = new ArrayList<String>();
        if (permChown) permissions.add(AdminPrivilegeChown.value);
        if (permWriteOwned) permissions.add(AdminPrivilegeWriteOwned.value);
        if (permWriteFile) permissions.add(AdminPrivilegeWriteFile.value);

        /* normalUser creates an image with pixels in normalUser's group.*/
        loginUser(normalUser);
        Image image = mmFactory.createImage();
        Image sentImage = (Image) iUpdate.saveAndReturnObject(image);
        /* Login as lightAdmin.*/
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        /* lightAdmin tries to create a fileAttachment in normalUser's group.*/
        FileAnnotation fileAnnotation = mmFactory.createFileAnnotation();
        OriginalFile originalFile;
        try {
            fileAnnotation = (FileAnnotation) iUpdate.saveAndReturnObject(fileAnnotation);
            originalFile = (OriginalFile) fileAnnotation.getFile();
            Assert.assertTrue(isExpectSuccessCreateFileAttachment);
        } catch (SecurityViolation sv) {
            Assert.assertFalse(isExpectSuccessCreateFileAttachment);
            /* Finish the test in case fileAttachment could not be created.*/
            return;
        }
        /* Check that the value of canChown on the fileAnnotation is matching the boolean
         * isExpectSuccessCreateFileAttAndChown.*/
        Assert.assertEquals(getCurrentPermissions(fileAnnotation).canChown(), isExpectSuccessCreateFileAttAndChown);
        /* lightAdmin tries to link the fileAnnotation to the normalUser's image.
         * This will not work in private group. See definition of the boolean
         * isExpectSuccessLinkFileAttachment.*/
        ImageAnnotationLink link = null;
        try {
            link = (ImageAnnotationLink) linkParentToChild(sentImage, fileAnnotation);
            /* Check the value of canAnnotate on the image is true in successful linking case.*/
            Assert.assertTrue(getCurrentPermissions(sentImage).canAnnotate());
            Assert.assertTrue(isExpectSuccessLinkFileAttachemnt);
        } catch (SecurityViolation sv) {
            /* Check the value of canAnnotate on the image is false in case linking fails.*/
            Assert.assertFalse(getCurrentPermissions(sentImage).canAnnotate());
            Assert.assertFalse(isExpectSuccessLinkFileAttachemnt);
            /* Finish the test in case no link could be created.*/
            return;
        }
        /* lightAdmin tries to transfer the ownership of fileAnnotation to normalUser.
         * The test was terminated (see above) in all cases
         * in which the fileAnnotation was not created.*/
        doChange(client, factory, Requests.chown().target(fileAnnotation).toUser(normalUser.userId).build(), isExpectSuccessCreateFileAttAndChown);
        if (isExpectSuccessCreateFileAttAndChown) {
            /* First case: fileAnnotation creation and chowning succeeded.*/
            assertOwnedBy(fileAnnotation, normalUser);
            assertOwnedBy(originalFile, normalUser);
        } else {
            /* Second case: creation of fileAnnotation succeeded, but the chown failed.*/
            assertOwnedBy(fileAnnotation, lightAdmin);
            assertOwnedBy(originalFile, lightAdmin);
        }
        /* Check the value of canChown on the link is matching the boolean
         * isExpectSuccessCreateLinkAndChown.*/
        Assert.assertEquals(getCurrentPermissions(link).canChown(), isExpectSuccessCreateLinkAndChown);
        /* lightAdmin tries to transfer the ownership of link to normalUser.
         * The test was terminated (see above) in all cases
         * in which the link was not created.*/
        doChange(client, factory, Requests.chown().target(link).toUser(normalUser.userId).build(), isExpectSuccessCreateLinkAndChown);
        if (isExpectSuccessCreateLinkAndChown) {
            /* First case: link was created and chowned, the whole workflow succeeded.*/
            link = (ImageAnnotationLink) iQuery.findByQuery("FROM ImageAnnotationLink l JOIN FETCH"
                    + " l.child JOIN FETCH l.parent WHERE l.child.id = :id",
                    new ParametersI().addId(fileAnnotation.getId()));
            assertOwnedBy(link, normalUser);
            assertOwnedBy(fileAnnotation, normalUser);
            assertOwnedBy(originalFile, normalUser);
        } else {
            /* Second case: link was created but could not be chowned.*/
            link = (ImageAnnotationLink) iQuery.findByQuery("FROM ImageAnnotationLink l JOIN FETCH"
                    + " l.child JOIN FETCH l.parent WHERE l.child.id = :id",
                    new ParametersI().addId(fileAnnotation.getId()));
            assertOwnedBy(link, lightAdmin);
        }
    }

    /**
     * Light admin (lightAdmin) tries to upload an official script.
     * lightAdmin succeeds in this if they have <tt>WriteScriptRepo</tt> permission.
     * @param isPrivileged if to test a user who has the <tt>WriteScriptRepo</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testOfficialScriptUploadNoSudo(boolean isPrivileged, String groupPermissions) throws Exception {
        /* isPrivileged translates in this test into WriteScriptRepo permission, see below.*/
        boolean isExpectSuccessUploadOfficialScript = isPrivileged;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        /* Set up the light admin's permissions for this test.*/
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeWriteScriptRepo.value);
        loginNewAdmin(true, permissions);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        IScriptPrx iScript = factory.getScriptService();
        /* lightAdmin fetches a script from the server.*/
        OriginalFile scriptFile = iScript.getScriptsByMimetype(ScriptServiceTest.PYTHON_MIMETYPE).get(0);
        String actualScript;
        RawFileStorePrx rfs = null;
        try {
            rfs = factory.createRawFileStore();
            rfs.setFileId(scriptFile.getId().getValue());
            actualScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        } finally {
            if (rfs != null) rfs.close();
        }
        /* lightAdmin tries uploading the script as a new script in normalUser's group.*/
        iScript = factory.getScriptService();
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        long testScriptId = -1;
        try {
            testScriptId = iScript.uploadOfficialScript(testScriptName, actualScript);
            Assert.assertTrue(isExpectSuccessUploadOfficialScript);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccessUploadOfficialScript);
            /* Upload failed so finish the test.*/
            return;
        }
        /* Check that the new script exists in the "user" group.*/
        loginUser(normalUser);
        scriptFile = (OriginalFile) iQuery.get("OriginalFile", testScriptId);
        Assert.assertEquals(scriptFile.getDetails().getOwner().getId().getValue(), roles.rootId);
        Assert.assertEquals(scriptFile.getDetails().getGroup().getId().getValue(), roles.userGroupId);
        /* Check if the script is correctly uploaded.*/
        String currentScript;
        rfs = null;
        try {
            rfs = factory.createRawFileStore();
            rfs.setFileId(testScriptId);
            currentScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        } finally {
            if (rfs != null) rfs.close();
        }
        Assert.assertEquals(currentScript, actualScript);
    }

    /**
     * Light admin (lightAdmin) tries to delete official script. 
     * lightAdmin will succeed if they have the <tt>DeleteScriptRepo</tt> privilege.
     * @param isPrivileged if to test a user who has the <tt>DeleteScriptRepo</tt> privilege
     * @param groupPermissions to test the effect of group permission level
     * @throws Exception unexpected
     */
    @Test(dataProvider = "isPrivileged cases")
    public void testOfficialScriptDeleteNoSudo(boolean isPrivileged, String groupPermissions) throws Exception {
        boolean isExpectSuccessDeleteOfficialScript = isPrivileged;
        final EventContext normalUser = newUserAndGroup(groupPermissions);
        List<String> permissions = new ArrayList<String>();
        if (isPrivileged) permissions.add(AdminPrivilegeDeleteScriptRepo.value);
        final EventContext lightAdmin;
        lightAdmin = loginNewAdmin(true, permissions);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        IScriptPrx iScript = factory.getScriptService();
        /* lightAdmin fetches a script from the server.*/
        OriginalFile scriptFile = iScript.getScriptsByMimetype(ScriptServiceTest.PYTHON_MIMETYPE).get(0);
        String actualScript;
        RawFileStorePrx rfs = null;
        try {
            rfs = factory.createRawFileStore();
            rfs.setFileId(scriptFile.getId().getValue());
            actualScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
        } finally {
            if (rfs != null) rfs.close();
        }
        /* Another light admin (anotherLightAdmin) with appropriate permissions
         * uploads the script as a new script.*/
        loginNewAdmin(true, AdminPrivilegeWriteScriptRepo.value);
        iScript = factory.getScriptService();
        final String testScriptName = "Test_" + getClass().getName() + '_' + UUID.randomUUID() + ".py";
        final long testScriptId = iScript.uploadOfficialScript(testScriptName, actualScript);
        /* Delete any jobs associated with the script.*/
        final Delete2Builder delete = Requests.delete().option(Requests.option().excludeType("OriginalFile").build());
        for (final IObject scriptJob : iQuery.findAllByQuery(
                "SELECT DISTINCT link.parent FROM JobOriginalFileLink link WHERE link.child.id = :id",
                new ParametersI().addId(testScriptId))) {
            delete.target(scriptJob);
        }
        doChange(delete.build());
        /* Check that the new script exists.*/
        final OriginalFile testScript = new OriginalFileI(testScriptId, false);
        assertExists(testScript);
        /* lightAdmin tries deleting the script.*/
        loginUser(lightAdmin);
        client.getImplicitContext().put("omero.group", Long.toString(normalUser.groupId));
        iScript = factory.getScriptService();
        try {
            iScript.deleteScript(testScriptId);
            Assert.assertTrue(isExpectSuccessDeleteOfficialScript);
        } catch (ServerError se) {
            Assert.assertFalse(isExpectSuccessDeleteOfficialScript);
        }
        /* normalUser checks if the script was deleted or left intact.*/
        loginUser(normalUser);
        if (isExpectSuccessDeleteOfficialScript) {
            assertDoesNotExist(testScript);
        } else {
            assertExists(testScript);
        }
        rfs = null;
        try {
            rfs = factory.createRawFileStore();
            rfs.setFileId(testScriptId);
            final String currentScript = new String(rfs.read(0, (int) rfs.size()), StandardCharsets.UTF_8);
            Assert.assertEquals(currentScript, actualScript);
            Assert.assertFalse(isExpectSuccessDeleteOfficialScript);
        } catch (Ice.LocalException | ServerError se) {
            /* Have to catch both types of exceptions because
             * {@link #RawFileStoreTest.testBadFileId, testBadFileId} is broken.*/
            Assert.assertTrue(isExpectSuccessDeleteOfficialScript);
        } finally {
            if (rfs != null) rfs.close();
        }
    }

}
