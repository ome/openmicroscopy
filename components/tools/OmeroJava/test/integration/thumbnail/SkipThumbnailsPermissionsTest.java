package integration.thumbnail;

import integration.AbstractServerImportTest;
import integration.ModelMockFactory;
import ome.formats.importer.ImportConfig;
import omero.api.RenderingEnginePrx;
import omero.api.ThumbnailStorePrx;
import omero.model.ExperimenterGroup;
import omero.model.ExperimenterGroupI;
import omero.model.Pixels;
import omero.sys.EventContext;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests utilising single thumbnail loading APIs by users other than the
 * owner of the image. In each test an image is imported into OMERO without
 * thumbnails (--skip thumbnails).
 */
public class SkipThumbnailsPermissionsTest extends AbstractServerImportTest {

    private ExperimenterGroup systemGroup;

    @BeforeMethod
    protected void startup() throws Throwable {
        createImporter();
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

        // Login as user2
        loginUser(user2);

        // Try to load image
        svc = factory.createThumbnailStore();
        try {
            Utils.setThumbnailStoreToPixels(svc, pixels.getId().getValue());
            Utils.getThumbnail(svc);
        } catch (omero.ResourceError e) {
            // With permission rw----, the image is private to user 1.
            // Expect this to fail for user 2.
            if (!permissions.equalsIgnoreCase("rw----")) {
                throw e;
            }
        }
    }

    @Test(dataProvider = "permissions")
    public void testGetThumbnailWithoutDefault(String permissions, boolean isAdmin,
                                               boolean isGroupOwner) throws Throwable {
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
        EventContext user2 = newUserInGroup(user1, isGroupOwner);

        // If user is an admin, add them to the system group
        if (isAdmin) {
            ExperimenterGroup systemGroup = new ExperimenterGroupI(iAdmin.getSecurityRoles().systemGroupId, false);
            addUsers(systemGroup, Collections.singletonList(user2.userId), false);
        }

        // Login as user2
        loginUser(user2);

        // Try to load image
        svc = factory.createThumbnailStore();
        try {
            Utils.setThumbnailStoreToPixels(svc, pixels.getId().getValue());
            Utils.getThumbnailWithoutDefault(svc);
        } catch (omero.ResourceError e) {
            // With permission rw----, the image is private to user 1.
            // Expect this to fail for user 2.
            if (!permissions.equalsIgnoreCase("rw----")) {
                throw e;
            }
        }
    }

    @Test(dataProvider = "permissions")
    public void testGetThumbnailLarge(String permissions, boolean isAdmin,
                                 boolean isGroupOwner) throws Throwable {



    }

    @Test(dataProvider = "permissions")
    public void testGetThumbnailWithoutDefaultLarge(String permissions, boolean isAdmin,
                                      boolean isGroupOwner) throws Throwable {



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
        Pixels pixels = importFile(config);
        final long pixelsId = pixels.getId().getValue();

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
        RenderingEnginePrx re = factory.createRenderingEngine();
        re.lookupPixels(pixelsId);
        if (!re.lookupRenderingDef(pixelsId)) {
            re.resetDefaultSettings(true);
            re.lookupRenderingDef(pixelsId);
        }
        // re.load();
        re.close();

        // Load thumbnail as user 2 to create thumbnail on disk
        ThumbnailStorePrx svc = factory.createThumbnailStore();
        Utils.setThumbnailStoreToPixels(svc, pixelsId);
        Utils.getThumbnailWithoutDefault(svc);
        svc.close();

        // Switch to user 1
        loginUser(user1);

        // Load and change to trigger rendering settings and thumbnail creation for user 1
        svc = factory.createThumbnailStore();
        Utils.setThumbnailStoreToPixels(svc, pixels.getId().getValue());
        Utils.getThumbnailWithoutDefault(svc);

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
        svc.close();

        // Login as user 2 and get their version of the thumbnail
        loginUser(user2);
        svc = factory.createThumbnailStore();
        Utils.setThumbnailStoreToPixels(svc, pixels.getId().getValue());
        byte[] user2Thumbnail = Utils.getThumbnailWithoutDefault(svc);
        svc.close();

        // Quick check
        Assert.assertEquals(user1Thumbnail.length, user2Thumbnail.length);
        Assert.assertTrue(Arrays.equals(user1Thumbnail, user2Thumbnail));
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

    private Pixels importFile(ImportConfig config) throws Throwable {
        String format = ModelMockFactory.FORMATS[0];
        File file = createImageFile(format);
        return importFile(config, file, format).get(0);
    }

    private Pixels importLargeFile(ImportConfig config) throws Throwable {
        String format = ModelMockFactory.FORMATS[0];
        File file = createImageFile(format);
        return importFile(config, file, format).get(0);
    }

    private File createImageFile(String format) throws Throwable {
        File f = File.createTempFile("testImportGraphicsImages" + format, "."
                + format);
        mmFactory.createImageFile(f, format);
        return f;
    }
}
