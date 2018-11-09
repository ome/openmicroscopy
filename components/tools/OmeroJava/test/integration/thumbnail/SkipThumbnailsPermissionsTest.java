/*
 * Copyright (C) 2006-2018 University of Dundee & Open Microscopy Environment.
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

package integration.thumbnail;

import integration.AbstractServerImportTest;
import ome.formats.importer.ImportConfig;
import omero.ServerError;
import omero.api.RenderingEnginePrx;
import omero.api.ThumbnailStorePrx;
import omero.model.EventI;
import omero.model.EventLogI;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.Pixels;
import omero.model.StatsInfo;
import omero.sys.EventContext;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests utilizing single thumbnail loading APIs by users other than the
 * owner of the image. In each test an image is imported into OMERO without
 * thumbnails (--skip thumbnails).
 * @author Riad Gozim &nbsp;&nbsp;&nbsp;&nbsp; <a
 * href="mailto:r.gozim@dundee.ac.uk">r.gozim@dundee.ac.uk</a>
 * @since 5.4.10
 */
@SuppressWarnings("Duplicates")
public class SkipThumbnailsPermissionsTest extends AbstractServerImportTest {

    /* Total wait time will be WAITS * INTERVAL milliseconds */
    /**
     * Maximum number of intervals to wait for pyramid
     **/
    private static final int WAITS = 500;

    /**
     * Wait time in milliseconds
     **/
    private static final long INTERVAL = 1000L;

    /**
     * The collection of files that have to be deleted.
     */
    private List<File> files;

    @BeforeMethod
    protected void startup() throws Throwable {
        files = new ArrayList<>();
        createImporter();
    }

    @AfterMethod
    public void cleanup() {
        for (File file : files) {
            file.delete();
        }
        files.clear();
    }

    @Test(dataProvider = "permissions")
    public void testGetThumbnail(String permissions, boolean isAdmin,
                                 boolean isGroupOwner) throws Throwable {
        // Create two users in same group
        EventContext user1 = newUserAndGroup(permissions);
        loginUser(user1);

        ImportConfig config = new ImportConfig();
        config.doThumbnails.set(false); // skip thumbnails

        // Import image without thumbnails
        Pixels pixels = importFile(config);

        // View image as user 1
        ThumbnailStorePrx svc = factory.createThumbnailStore();
        Utils.setThumbnailStoreToPixels(svc, pixels.getId().getValue());
        Utils.getThumbnail(svc);
        svc.close();

        // Create new user in group and login as that user
        EventContext user2 = newUserInGroup(user1, isGroupOwner);

        // If user is an admin, add them to the system group
        if (isAdmin) {
            ExperimenterGroup systemGroup = new ExperimenterGroupI(iAdmin.getSecurityRoles().systemGroupId, false);
            addUsers(systemGroup, Collections.singletonList(user2.userId), false);
        }
        disconnect();
        // Login as user2
        loginUser(user2);

        // Try to load image
        svc = factory.createThumbnailStore();
        try {
            Utils.setThumbnailStoreToPixels(svc, pixels.getId().getValue());
            Utils.getThumbnail(svc);
        } catch (omero.ResourceError | omero.ReadOnlyGroupSecurityViolation e) {
            // With permission rw----, the image is private to user 1.
            // Expect this to fail for user 2.
            if (!permissions.equalsIgnoreCase("rw----")) {
                throw e;
            }
        } catch (omero.ApiUsageException e) {
            //group member trying to set settings
            if (!(permissions.equalsIgnoreCase("rwr---") || permissions.equalsIgnoreCase("rw----"))) {
                throw e;
            }
        } finally {
            svc.close();
        }
    }

    @Test(dataProvider = "permissions")
    public void testGetThumbnailWithoutDefault(String permissions, boolean isAdmin, boolean isGroupOwner) throws Throwable {
        // Create two users in same group
        EventContext user1 = newUserAndGroup(permissions);
        loginUser(user1);

        // Obtain image
        ImportConfig config = new ImportConfig();
        config.doThumbnails.set(false); // skip thumbnails

        // Import image without thumbnails
        Pixels pixels = importFile(config);

        // View image as user 1
        ThumbnailStorePrx svc = factory.createThumbnailStore();
        Utils.setThumbnailStoreToPixels(svc, pixels.getId().getValue());
        Utils.getThumbnailWithoutDefault(svc);
        svc.close();

        // Create new user in group and login as that user
        addUserAndLogin(user1, isAdmin, isGroupOwner);

        // Try to load image
        svc = factory.createThumbnailStore();
        try {
            Utils.setThumbnailStoreToPixels(svc, pixels.getId().getValue());
            Utils.getThumbnailWithoutDefault(svc);
        } catch (omero.ResourceError | omero.ReadOnlyGroupSecurityViolation e) {
            // With permission rw----, the image is private to user 1.
            // Expect this to fail for user 2.
            if (!permissions.equalsIgnoreCase("rw----")) {
                throw e;
            }
        } catch (omero.ApiUsageException e) {
            //group member trying to set settings
            if (!(permissions.equalsIgnoreCase("rwr---") || permissions.equalsIgnoreCase("rw----"))) {
                throw e;
            }
        } finally {
            svc.close();
        }
    }

    @Test(dataProvider = "permissions")
    public void testGetThumbnailLarge(String permissions, boolean isAdmin, boolean isGroupOwner) throws Throwable {
        // Create two users in same group
        EventContext user1 = newUserAndGroup(permissions);
        loginUser(user1);

        // Obtain image
        ImportConfig config = new ImportConfig();
        config.doThumbnails.set(false); // skip thumbnails

        // Create a png file, with all RGB values FFFFFF
        Pixels pixels = importLargeFile(config);

        // View image as user 1
        ThumbnailStorePrx svc = factory.createThumbnailStore();
        Utils.setThumbnailStoreToPixels(svc, pixels.getId().getValue());
        Utils.getThumbnail(svc);
        svc.close();

        // Create new user in group and login as that user
        addUserAndLogin(user1, isAdmin, isGroupOwner);

        // Try to load image
        svc = factory.createThumbnailStore();
        try {
            Utils.setThumbnailStoreToPixels(svc, pixels.getId().getValue());
            byte[] data = Utils.getThumbnail(svc);
            Assert.assertTrue(data.length > 0);
        } catch (omero.ResourceError | omero.ReadOnlyGroupSecurityViolation e) {
            // With permission rw----, the image is private to user 1.
            // Expect this to fail for user 2.
            if (!permissions.equalsIgnoreCase("rw----")) {
                throw e;
            }
        } catch (omero.ApiUsageException e) {
            //group member trying to set settings
            if (!(permissions.equalsIgnoreCase("rwr---") || permissions.equalsIgnoreCase("rw----"))) {
                throw e;
            }
        } finally {
            svc.close();
        }
    }

    @Test(dataProvider = "permissions")
    public void testGetThumbnailLargeAsUser2Only(String permissions, boolean isAdmin, boolean isGroupOwner) throws Throwable {
        // Create two users in same group
        EventContext user1 = newUserAndGroup(permissions);
        loginUser(user1);

        // Obtain image
        ImportConfig config = new ImportConfig();
        config.doThumbnails.set(false); // skip thumbnails

        // Create a png file, with all RGB values FFFFFF
        Pixels pixels = importLargeFile(config);

        // Create new user in group and login as that user
        addUserAndLogin(user1, isAdmin, isGroupOwner);

        // Try to load image
        ThumbnailStorePrx svc = factory.createThumbnailStore();
        try {
            Utils.setThumbnailStoreToPixels(svc, pixels.getId().getValue());
            byte[] data = Utils.getThumbnail(svc);
            Assert.assertTrue(data.length > 0);
        } catch (omero.ResourceError | omero.ReadOnlyGroupSecurityViolation e) {
            // With permission rw----, the image is private to user 1.
            // Expect this to fail for user 2.
            if (!permissions.equalsIgnoreCase("rw----")) {
                throw e;
            }
        } catch (omero.ApiUsageException e) {
            //group member trying to set settings
            if (!(permissions.equalsIgnoreCase("rwr---") || permissions.equalsIgnoreCase("rw----"))) {
                throw e;
            }
        } finally {
            svc.close();
        }
    }

    @Test(dataProvider = "permissions")
    public void testGetThumbnailWithoutDefaultLarge(String permissions, boolean isAdmin, boolean isGroupOwner) throws Throwable {
        // Create two users in same group
        EventContext user1 = newUserAndGroup(permissions);
        loginUser(user1);

        // Obtain image
        ImportConfig config = new ImportConfig();
        config.doThumbnails.set(false); // skip thumbnails

        // Create a png file, with all RGB values FFFFFF
        Pixels pixels = importLargeFile(config);

        // View image as user 1
        ThumbnailStorePrx svc = factory.createThumbnailStore();
        Utils.setThumbnailStoreToPixels(svc, pixels.getId().getValue());
        Utils.getThumbnailWithoutDefault(svc);
        svc.close();

        // Create new user in group and login as that user
        addUserAndLogin(user1, isAdmin, isGroupOwner);

        // Try to load image
        svc = factory.createThumbnailStore();
        try {
            Utils.setThumbnailStoreToPixels(svc, pixels.getId().getValue());
            byte[] data = Utils.getThumbnailWithoutDefault(svc);
            Assert.assertTrue(data.length > 0);
        } catch (omero.ResourceError | omero.ReadOnlyGroupSecurityViolation e) {
            // With permission rw----, the image is private to user 1.
            // Expect this to fail for user 2.
            if (!permissions.equalsIgnoreCase("rw----")) {
                throw e;
            }
        } catch (omero.ApiUsageException e) {
            //group member trying to set settings
            if (!(permissions.equalsIgnoreCase("rwr---") || permissions.equalsIgnoreCase("rw----"))) {
                throw e;
            }
        } finally {
            svc.close();
        }
    }

    @Test(dataProvider = "permissions")
    public void testGetThumbnailWithoutDefaultLargeAsUser2Only(String permissions, boolean isAdmin, boolean isGroupOwner) throws Throwable {
        // Create two users in same group
        EventContext user1 = newUserAndGroup(permissions);
        loginUser(user1);

        // Obtain image
        ImportConfig config = new ImportConfig();
        config.doThumbnails.set(false); // skip thumbnails

        // Create a png file, with all RGB values FFFFFF
        Pixels pixels = importLargeFile(config);

        // Create new user in group and login as that user
        addUserAndLogin(user1, isAdmin, isGroupOwner);

        // Try to load image
        ThumbnailStorePrx svc = factory.createThumbnailStore();
        try {
            Utils.setThumbnailStoreToPixels(svc, pixels.getId().getValue());
            byte[] data = Utils.getThumbnailWithoutDefault(svc);
            Assert.assertTrue(data.length > 0);
        } catch (omero.ResourceError | omero.ReadOnlyGroupSecurityViolation e) {
            // With permission rw----, the image is private to user 1.
            // Expect this to fail for user 2.
            if (!permissions.equalsIgnoreCase("rw----")) {
                throw e;
            }
        } catch (omero.ApiUsageException e) {
            //group member trying to set settings
            if (!(permissions.equalsIgnoreCase("rwr---") || permissions.equalsIgnoreCase("rw----"))) {
                throw e;
            }
        } finally {
            svc.close();
        }
    }

    /**
     * Test scenario outlined on:
     * https://trello.com/c/itoDPkxB/24-read-only-settings-and-thumbnails-generation
     * <p>
     * 1. User 1 import image and skip thumbnail generation (don't view it)
     * 2. User 2 view the image (create rendering settings)
     * 3. User 1 view the image and change the rendering settings
     * 4. User 2 view load their thumbnail and compare to user 1's thumbnail
     *
     * @throws Throwable
     */
    @Test(dataProvider = "permissions")
    public void testGetThumbnailWithRenderingSettingsChange(String permissions, boolean isAdmin,
                                                            boolean isGroupOwner) throws Throwable {
        // Skip this test for rw---- group
        if (permissions.equalsIgnoreCase("rw----")) {
            return;
        }

        EventContext user1 = newUserAndGroup(permissions);
        loginUser(user1);

        ImportConfig config = new ImportConfig();
        config.doThumbnails.set(false); // skip thumbnails

        // Import image without thumbnails
        Pixels pixels = importLargeFile(config);
        final long pixelsId = pixels.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(pixelsId);
        if (!re.lookupRenderingDef(pixelsId)) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(pixelsId);
        }
        disconnect();
        // Create new user in group and login as that user
        EventContext user2 = newUserInGroup(user1, isGroupOwner);

        // If user is an admin, add them to the system group
        if (isAdmin) {
            ExperimenterGroup systemGroup = new ExperimenterGroupI(iAdmin.getSecurityRoles().systemGroupId, false);
            addUsers(systemGroup, Collections.singletonList(user2.userId), false);
        }

        // Login as user2
        loginUser(user2);

        // Generate rendering settings for user 2
        re = factory.createRenderingEngine();
        re.lookupPixels(pixelsId);
        if (!re.lookupRenderingDef(pixelsId)) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(pixelsId);
        }
        re.close();

        // Load thumbnail as user 2 to create thumbnail on disk
        ThumbnailStorePrx svc = factory.createThumbnailStore();
        Utils.setThumbnailStoreToPixels(svc, pixelsId);
        byte[] user2Thumbnail = Utils.getThumbnailWithoutDefault(svc);
        Utils.checkSize(user2Thumbnail, Utils.DEFAULT_SIZE_X,
                Utils.DEFAULT_SIZE_Y);
        svc.close();

        // Switch to user 1
        disconnect();
        loginUser(user1);

        // Load and change to trigger rendering settings and thumbnail creation for user 1
        svc = factory.createThumbnailStore();
        Utils.setThumbnailStoreToPixels(svc, pixels.getId().getValue());

        // Get rendering settings for pixels object as user 1
        re = factory.createRenderingEngine();
        re.lookupPixels(pixelsId);
        if (!re.lookupRenderingDef(pixelsId)) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(pixelsId);
        }
        re.load();
        re.setActive(0, false);
        re.saveCurrentSettings();
        re.close();

        // Get thumbnail for user 1
        byte[] user1Thumbnail = Utils.getThumbnailWithoutDefault(svc);
        Utils.checkSize(user1Thumbnail, 96, 96);
        svc.close();

        // Check that the thumbnails are different
        Assert.assertFalse(Arrays.equals(user1Thumbnail, user2Thumbnail));
    }

    /**
     * Test scenario outlined on:
     * https://trello.com/c/itoDPkxB/24-read-only-settings-and-thumbnails-generation
     * <p>
     * 1. User 1 import image and skip thumbnail generation (don't view it)
     * 2. User 2 view the image (create rendering settings)
     * 3. User 1 view the image and change the rendering settings
     * 4. User 2 view load their thumbnail and compare to user 1's thumbnail
     *
     * @throws Throwable
     */
    @Test(dataProvider = "permissions")
    public void testGetThumbnailWithRenderingSettingsChangeSmallImage(String permissions, boolean isAdmin,
                                                            boolean isGroupOwner) throws Throwable {
        // Skip this test for rw---- group
        if (permissions.equalsIgnoreCase("rw----")) {
            return;
        }

        EventContext user1 = newUserAndGroup(permissions);
        loginUser(user1);

        ImportConfig config = new ImportConfig();
        config.doThumbnails.set(false); // skip thumbnails

        // Import image without thumbnails
        Pixels pixels = importLargeFile(config);
        final long pixelsId = pixels.getId().getValue();
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(pixelsId);
        if (!re.lookupRenderingDef(pixelsId)) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(pixelsId);
        }
        disconnect();
        // Create new user in group and login as that user
        EventContext user2 = newUserInGroup(user1, isGroupOwner);

        // If user is an admin, add them to the system group
        if (isAdmin) {
            ExperimenterGroup systemGroup = new ExperimenterGroupI(iAdmin.getSecurityRoles().systemGroupId, false);
            addUsers(systemGroup, Collections.singletonList(user2.userId), false);
        }

        // Login as user2
        loginUser(user2);

        // Generate rendering settings for user 2
        re = factory.createRenderingEngine();
        re.lookupPixels(pixelsId);
        if (!re.lookupRenderingDef(pixelsId)) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(pixelsId);
        }
        re.close();

        // Load thumbnail as user 2 to create thumbnail on disk
        ThumbnailStorePrx svc = factory.createThumbnailStore();
        Utils.setThumbnailStoreToPixels(svc, pixelsId);
        byte[] user2Thumbnail = Utils.getThumbnailWithoutDefault(svc);
        Utils.checkSize(user2Thumbnail, Utils.DEFAULT_SIZE_X,
                Utils.DEFAULT_SIZE_Y);
        svc.close();

        // Switch to user 1
        disconnect();
        loginUser(user1);

        // Load and change to trigger rendering settings and thumbnail creation for user 1
        svc = factory.createThumbnailStore();
        Utils.setThumbnailStoreToPixels(svc, pixels.getId().getValue());

        // Get rendering settings for pixels object as user 1
        re = factory.createRenderingEngine();
        re.lookupPixels(pixelsId);
        if (!re.lookupRenderingDef(pixelsId)) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(pixelsId);
        }
        re.load();
        re.setActive(0, false);
        re.saveCurrentSettings();
        re.close();

        // Get thumbnail for user 1
        byte[] user1Thumbnail = Utils.getThumbnailWithoutDefault(svc);
        Utils.checkSize(user1Thumbnail, 96, 96);
        svc.close();

        // Check that the thumbnails are different
        Assert.assertFalse(Arrays.equals(user1Thumbnail, user2Thumbnail));
    }
   
    @SuppressWarnings("Duplicates")
    @DataProvider(name = "permissions")
    public Object[][] providePermissions() {
        int index = 0;
        final int PERMISSION = index++;
        final int IS_ADMIN = index++;
        final int IS_GROUP_OWNER = index++;

        boolean[] booleanCases = new boolean[]{false, true};
        String[] permsCases = new String[]{"rw----", "rwr---", "rwra--", "rwrw--"};
        List<Object[]> testCases = new ArrayList<>();

        for (boolean isAdmin : booleanCases) {
            for (boolean isGroupOwner : booleanCases) {
                for (String groupPerms : permsCases) {
                    Object[] testCase = new Object[index];
                    testCase[PERMISSION] = groupPerms;
                    testCase[IS_ADMIN] = isAdmin;
                    testCase[IS_GROUP_OWNER] = isGroupOwner;
                    testCases.add(testCase);
                }
            }
        }
        return testCases.toArray(new Object[testCases.size()][]);
    }

    private EventContext addUser(EventContext user1, boolean isAdmin, boolean isGroupOwner) throws Exception {
        // Create new user in group and login as that user
        EventContext newUser = newUserInGroup(user1, isGroupOwner);
        if (isAdmin) {
            // If user is an admin, add them to the system group
            ExperimenterGroup systemGroup = new ExperimenterGroupI(iAdmin.getSecurityRoles().systemGroupId, false);
            addUsers(systemGroup, Collections.singletonList(newUser.userId), false);
        }
        return newUser;
    }

    private EventContext addUserAndLogin(EventContext user1, boolean isAdmin, boolean isGroupOwner) throws Exception {
        // Login as new user
        EventContext newUser = addUser(user1, isAdmin, isGroupOwner);
        loginUser(newUser);
        return newUser;
    }

    private Pixels importFile(ImportConfig config) throws Throwable {
        File file = createImageFile("fake");
        return importFile(config, file, "fake").get(0);
    }

    private Pixels importLargeFile(ImportConfig config) throws Throwable {
        File f = File.createTempFile("bigImageFake&sizeX=3500&sizeY=3500&little=false", ".fake");
        f.deleteOnExit();
        return importAndWaitForPyramid(config, f, "fake");
    }

    private File createImageFile(String extension) throws Throwable {
        File f = File.createTempFile("imageFake", "."+ extension);
        f.deleteOnExit();
        return f;
    }

    private void triggerPyramidGeneration(long pixelsId) throws ServerError {
        // This strange out of place event log is required for triggering
        // generation of pyramids for imported file.
        EventLogI el = new EventLogI();
        el.setAction(omero.rtypes.rstring("PIXELDATA"));
        el.setEntityId(omero.rtypes.rlong(pixelsId));
        el.setEntityType(omero.rtypes.rstring(ome.model.core.Pixels.class.getName()));
        el.setEvent(new EventI(0, false));

        // Need to use root session to save eventlog otherwise you get a
        // security violation
        root.getSession().getUpdateService().saveObject(el);
    }

    /**
     * Import an image file of the given format then wait for a pyramid file to
     * be generated by checking if stats exists.
     */
    private Pixels importAndWaitForPyramid(ImportConfig config, File file, String format)
            throws Exception {
        Pixels pixels = null;
        try {
            pixels = importFile(config, file, format).get(0);
        } catch (Throwable e) {
            Assert.fail("Cannot import image file: " + file.getAbsolutePath()
                    + " Reason: " + e.toString());
        }
        triggerPyramidGeneration(pixels.getId().getValue());

        // Wait for a pyramid to be built (stats will be not null)
        Pixels p = factory.getPixelsService().retrievePixDescription(pixels.getId().getValue());
        StatsInfo stats = p.getChannel(0).getStatsInfo();
        int waits = 0;
        Assert.assertEquals(stats, null);
        while (stats == null && waits < WAITS) {
            Thread.sleep(INTERVAL);
            waits++;
            factory.createRawPixelsStore();
            p = factory.getPixelsService().retrievePixDescription(
                    pixels.getId().getValue());
            stats = p.getChannel(0).getStatsInfo();
        }
        if (stats == null) {
            Assert.fail("No pyramid after " + WAITS + " seconds");
        }
        return p;
    }
}
