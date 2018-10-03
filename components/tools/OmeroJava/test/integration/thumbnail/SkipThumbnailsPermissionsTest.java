package integration.thumbnail;

import integration.AbstractServerImportTest;
import integration.ModelMockFactory;
import ome.formats.importer.ImportConfig;
import omero.ServerError;
import omero.api.RenderingEnginePrx;
import omero.api.ThumbnailStorePrx;
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

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Tests utilising single thumbnail loading APIs by users other than the
 * owner of the image. In each test an image is imported into OMERO without
 * thumbnails (--skip thumbnails).
 */
@SuppressWarnings("Duplicates")
public class SkipThumbnailsPermissionsTest extends AbstractServerImportTest {

    /**
     * The format tested here.
     */
    private static final String FORMAT = "png";

    /* Total wait time will be WAITS * INTERVAL milliseconds */
    /** Maximum number of intervals to wait for pyramid **/
    private static final int WAITS = 100;

    /** Wait time in milliseconds **/
    private static final long INTERVAL = TimeUnit.HOURS.toMillis(1);

    /** The collection of files that have to be deleted. */
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
        } catch (omero.ResourceError e) {
            // With permission rw----, the image is private to user 1.
            // Expect this to fail for user 2.
            if (!permissions.equalsIgnoreCase("rw----")) {
                throw e;
            }
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
        Utils.getThumbnailWithoutDefault(svc);
        svc.close();

        // Create new user in group and login as that user
        addUserAndLogin(user1, isAdmin, isGroupOwner);

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
    public void testGetThumbnailWithoutDefaultLarge(String permissions, boolean isAdmin, boolean isGroupOwner) throws Throwable {


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
        String format = ModelMockFactory.FORMATS[0];
        File file = createImageFile(format);
        return importFile(config, file, format).get(0);
    }

    private Pixels importLargeFile(ImportConfig config) throws Throwable {
        BufferedImage bi = new BufferedImage(4096, 4096, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < bi.getWidth(); x++) {
            for (int y = 0; y < bi.getHeight(); y++) {
                bi.setRGB(x, y, Integer.valueOf("FFFFFF", 16));
            }
        }
        File f = createImageFileWithBufferedImage(bi, FORMAT);
        files.add(f);
        return importAndWaitForPyramid(config, f, FORMAT);
    }

    private File createImageFile(String extension) throws Throwable {
        File f = File.createTempFile("testImportGraphicsImages" + extension, "."
                + extension);
        mmFactory.createImageFile(f, extension);
        return f;
    }

    /**
     * Create an image file from a BufferedImage of the given format.
     */
    private File createImageFileWithBufferedImage(BufferedImage bi, String format) throws Exception {
        File f = File.createTempFile("testImage", "." + format);
        Iterator writers = ImageIO.getImageWritersByFormatName(format);
        ImageWriter writer = (ImageWriter) writers.next();
        ImageOutputStream ios = ImageIO.createImageOutputStream(f);
        writer.setOutput(ios);
        writer.write(bi);
        ios.close();
        return f;
    }

    /**
     * Import an image file of the given format then wait for a pyramid file to
     * be generated by checking if stats exists.
     */
    private Pixels importAndWaitForPyramid(ImportConfig config, File f, String format)
            throws Exception {
        List<Pixels> pixels = null;
        try {
            pixels = importFile(config, f, format);
        } catch (Throwable e) {
            Assert.fail("Cannot import image file: " + f.getAbsolutePath()
                    + " Reason: " + e.toString());
        }
        // Wait for a pyramid to be built (stats will be not null)
        Pixels p = factory.getPixelsService().retrievePixDescription(
                pixels.get(0).getId().getValue());
        StatsInfo stats = p.getChannel(0).getStatsInfo();
        int waits = 0;
        while (stats == null && waits < WAITS) {
            Thread.sleep(INTERVAL);
            waits++;
            p = factory.getPixelsService().retrievePixDescription(
                    pixels.get(0).getId().getValue());
            stats = p.getChannel(0).getStatsInfo();
        }
        if (stats == null) {
            Assert.fail("No pyramid after " + WAITS * INTERVAL / 1000.0 + " seconds");
        }
        return p;
    }
}
