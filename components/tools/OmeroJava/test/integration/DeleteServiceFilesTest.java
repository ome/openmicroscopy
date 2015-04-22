/*
 * $Id$
 *
 *   Copyright 2006-2015 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ome.formats.OMEROMetadataStoreClient;
import ome.io.bioformats.BfPyramidPixelBuffer;
import ome.io.nio.PixelsService;
import omero.ApiUsageException;
import omero.RLong;
import omero.RString;
import omero.RType;
import omero.ResourceError;
import omero.ServerError;
import omero.api.RawFileStorePrx;
import omero.api.ThumbnailStorePrx;
import omero.cmd.Delete2;
import omero.cmd.Delete2Response;
import omero.grid.RawAccessRequest;
import omero.grid.RepositoryMap;
import omero.grid.RepositoryPrx;
import omero.model.Dataset;
import omero.model.DatasetI;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.Plate;
import omero.model.Well;
import omero.sys.EventContext;
import omero.sys.Parameters;
import omero.sys.ParametersI;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.util.ResourceUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;

import pojos.FileAnnotationData;

/**
 * Collections of tests for the <code>Delete</code> service.
 *
 * @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
 * @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:donald@lifesci.dundee.ac.uk"
 *         >donald@lifesci.dundee.ac.uk</a>
 * @author Colin Blackburn &nbsp;&nbsp;&nbsp;&nbsp; <a
 *         href="mailto:c.blackburn@dundee.ac.uk">c.blackburn@dundee.ac.uk</a>
 * @version 3.0 <small> (<b>Internal version:</b> $Revision: $Date: $) </small>
 * @since 3.0-Beta4
 */
public class DeleteServiceFilesTest extends AbstractServerTest {

    /** Reference to the <code>Pixels</code> class. */
    private static final String REF_PIXELS = "Pixels";

    /** Reference to the <code>OriginalFile</code> class. */
    private static final String REF_ORIGINAL_FILE = "OriginalFile";

    /** Reference to the <code>Thumbnail</code> class. */
    private static final String REF_THUMBNAIL = "Thumbnail";

    /** Enum representing type of pyramid file */
    enum PyramidFileType {
        PYRAMID, PYRAMID_LOCK, PYRAMID_TMP
    };

    /** Reference to the standard directory. */
    private String dataDir;

    /**
     * Creates an original file.
     *
     * @return See above.
     * @throws ServerError
     *             Thrown if an error occurred.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    private OriginalFile makeFile() throws ServerError, Exception {
        OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
                .createOriginalFile());

        long ofId = of.getId().getValue();
        RawFileStorePrx rfPrx = factory.createRawFileStore();
        try {
            rfPrx.setFileId(ofId);
            rfPrx.write(new byte[] { 1, 2, 3, 4 }, 0, 4);
            of = rfPrx.save();
        } finally {
            rfPrx.close();
        }
        return of;
    }

    /**
     * Makes an image with pixels files.
     *
     * @return See above.
     * @throws ServerError
     *             Thrown if an error occurred.
     * @throws Exception
     *             Thrown if an error occurred.
     *
     */
    private Image makeImageWithPixelsFile() throws ServerError, Exception {
        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        Pixels pix = img.getPrimaryPixels();
        String path = getPath(REF_PIXELS, pix.getId().getValue());
        RepositoryPrx legacy = getLegacyRepository();
        // FIXME: legacy.create(path);
        return img;
    }

    /**
     * Makes an image with pixels files.
     *
     * @return See above.
     * @throws ServerError
     *             Thrown if an error occurred.
     * @throws Exception
     *             Thrown if an error occurred.
     *
     */
    private Image makeImageWithPixelsFile(boolean pixels, boolean pyramid,
            boolean lock, boolean tmp) throws ServerError, Exception {
        String path;
        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        Pixels pix = img.getPrimaryPixels();
        RepositoryPrx legacy = getLegacyRepository();
        if (pixels) {
            path = getPath(REF_PIXELS, pix.getId().getValue());
            // FIXME: legacy.create(path);
        }
        if (pyramid) {
            path = getOtherPixelsPath(pix.getId().getValue(),
                    PyramidFileType.PYRAMID);
            // FIXME: legacy.create(path);
        }
        if (lock) {
            path = getOtherPixelsPath(pix.getId().getValue(),
                    PyramidFileType.PYRAMID_LOCK);
            // FIXME: legacy.create(path);
        }
        if (tmp) {
            path = getOtherPixelsPath(pix.getId().getValue(),
                    PyramidFileType.PYRAMID_TMP);
            // FIXME: legacy.create(path);
        }
        return img;
    }

    /**
     * Checks if thumbnails, files and pixels have not been deleted.
     *
     * @param report
     *            The report from the delete operation
     */
    private void assertNoUndeletedBinaries(Delete2Response report) {
        assertNoUndeletedThumbnails(report);
        assertNoUndeletedFiles(report);
        assertNoUndeletedPixels(report);
    }

    /**
     * Checks if the thumbnails have been deleted.
     *
     * @param report
     *            The report from the delete operation
     */
    private void assertNoUndeletedThumbnails(Delete2Response report) {
        List<Long> tbIds = report.deletedObjects.get(REF_THUMBNAIL);
        assertTrue(CollectionUtils.isEmpty(tbIds));
    }

    /**
     * Checks if the files have been deleted.
     *
     * @param report
     *            The report from the delete operation
     */
    private void assertNoUndeletedFiles(Delete2Response report) {
        List<Long> fileIds = report.deletedObjects.get(REF_ORIGINAL_FILE);
        assertTrue(CollectionUtils.isEmpty(fileIds));
    }

    /**
     * Checks if the pixels have been deleted.
     *
     * @param report
     *            The report from the delete operation
     */
    private void assertNoUndeletedPixels(Delete2Response report) {
        List<Long> pixIds = report.deletedObjects.get(REF_PIXELS);
        assertTrue(CollectionUtils.isEmpty(pixIds));
    }

    /**
     * Set the data directory for the tests. This is needed to find the correct
     * repository to test whether deletes have been successful.
     */
    @BeforeClass
    public void setDataDir() throws Exception {
        dataDir = root.getSession().getConfigService()
                .getConfigValue("omero.data.dir");
    }

    /**
     * Since so many tests rely on counting the number of objects present
     * globally, we're going to start each method with a new user in a new
     * group.
     */
    @BeforeMethod
    public void createNewUser() throws Exception {
        newUserAndGroup("rw----");
    }

    /**
     * Since we are creating a new client on each invocation, we should also
     * clean it up. Note: {@link #newUserAndGroup(String)} also closes, but not
     * the very last invocation.
     */
    @AfterMethod
    public void close() throws Exception {
        if (client != null) {
            client.__del__();
            client = null;
        }
    }

    /**
     * Basic asynchronous delete command. Used in order to reduce the number of
     * places that we do the same thing in case the API changes.
     *
     * @param dc
     *            The SINGLE command to handle.
     * @throws ApiUsageException
     * @throws ServerError
     * @throws InterruptedException
     */
    private Delete2Response deleteWithReport(Delete2 dc) throws ApiUsageException,
            ServerError, InterruptedException {
        return singleDeleteWithReport(client, dc);
    }

    /**
     * Forms a path depending on the type of file to be deleted and its id.
     *
     * @param dataDir
     *            The path to the directory
     * @param klass
     *            The type of object to handle.
     * @param id
     *            The identifier of the object.
     */
    private String getPath(String klass, Long id) throws Exception {
        String suffix = "";
        String prefix = "";
        Long remaining = id;
        Long dirno = 0L;

        if (id == null) {
            throw new NullPointerException("Expecting a not-null id.");
        }

        if (klass.equals(REF_ORIGINAL_FILE)) {
            prefix = FilenameUtils.concat(dataDir, "Files");
        } else if (klass.equals(REF_PIXELS)) {
            prefix = FilenameUtils.concat(dataDir, "Pixels");
        } else if (klass.equals(REF_THUMBNAIL)) {
            prefix = FilenameUtils.concat(dataDir, "Thumbnails");
        } else {
            throw new Exception("Unknown class: " + klass);
        }

        while (remaining > 999) {
            remaining /= 1000;

            if (remaining > 0) {
                Formatter formatter = new Formatter();
                dirno = remaining % 1000;
                suffix = formatter.format("Dir-%03d", dirno).out().toString()
                        + File.separator + suffix;
            }
        }

        String path = FilenameUtils.concat(prefix, suffix + id);
        return path;
    }

    /**
     * Helper to resolve file names for pyramid-related files
     */
    String getOtherPixelsPath(Long id, PyramidFileType kind) throws Exception {
        String path = getPath(REF_PIXELS, id);
        if (kind.equals(PyramidFileType.PYRAMID)) {
            path += PixelsService.PYRAMID_SUFFIX;
        } else if (kind.equals(PyramidFileType.PYRAMID_LOCK)) {
            File file = new File(path);
            File dir = file.getParentFile();
            File lockFile = new File(dir, "." + id
                    + PixelsService.PYRAMID_SUFFIX
                    + BfPyramidPixelBuffer.PYR_LOCK_EXT);
            path = lockFile.getAbsolutePath();
        } else if (kind.equals(PyramidFileType.PYRAMID_TMP)) {
            File file = new File(path);
            File dir = file.getParentFile();
            File tmpFile = new File(dir, "." + id
                    + PixelsService.PYRAMID_SUFFIX + "1234567890.tmp");
            path = tmpFile.getAbsolutePath();
        } else {
            throw new Exception("Unknown kind: " + kind);
        }
        return path;
    }

    /**
     * Gets a public repository on the OMERO data directory if one exists.
     *
     * @return See above.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    RepositoryPrx getLegacyRepository() throws Exception {
        RepositoryPrx legacy = null;
        RepositoryMap rm = factory.sharedResources().repositories();
        int repoCount = 0;
        String s = dataDir;
        for (OriginalFile desc : rm.descriptions) {
            String repoPath = desc.getPath().getValue()
                    + desc.getName().getValue();
            s += "\nFound repository:" + desc.getPath().getValue()
                    + desc.getName().getValue();
            if (FilenameUtils.equals(
                    FilenameUtils.normalizeNoEndSeparator(dataDir),
                    FilenameUtils.normalizeNoEndSeparator(repoPath))) {
                legacy = rm.proxies.get(repoCount);
                break;
            }
            repoCount++;
        }
        if (legacy == null) {
            throw new Exception("Unable to find legacy repository: " + s);
        }
        return legacy;
    }

    /**
     * Makes sure that the OMERO file exists of the given type and id
     *
     * @param id
     *            The object id corresponding to the filename.
     * @param klass
     *            The class (table name) of the object.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    void assertFileExists(Long id, String klass) throws Exception {
        String path = getPath(klass, id);
        RepositoryPrx legacy = getLegacyRepository();
        assertTrue(path + " does not exist!", legacy.fileExists(path));
    }

    /**
     * Makes sure that the OMERO file exists of the given type and id
     *
     * @param id
     *            The object id corresponding to the filename.
     * @param klass
     *            The class (table name) of the object.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    void assertOtherPixelsFileExists(Long id, PyramidFileType kind)
            throws Exception {
        String path = getOtherPixelsPath(id, kind);
        RepositoryPrx legacy = getLegacyRepository();
        assertTrue(path + " does not exist!", legacy.fileExists(path));
    }

    /**
     * Forcibly delete a file.
     *
     * @param id
     * @param klass
     * @throws Exception
     * @see ticket:3140
     */
    void removeFile(Long id, String klass) throws Exception {
        String path = getPath(klass, id);
        RepositoryPrx legacy = getLegacyRepository();
        // For admins only. Primarily a test feature.
        RawAccessRequest raw = new RawAccessRequest();
        raw.repoUuid = legacy.root().getHash().getValue();
        raw.command = "rm";
        raw.args = Arrays.asList(path);
        doChange(client, factory, raw, true);
    }

    /**
     * Makes sure that the OMERO file exists of the given type and id
     *
     * @param id
     *            The object id corresponding to the filename.
     * @param klass
     *            The class (table name) of the object.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    void assertFileDoesNotExist(Long id, String klass) throws Exception {
        String path = getPath(klass, id);
        RepositoryPrx legacy = getLegacyRepository();
        assertFalse(path + " exists!", legacy.fileExists(path));
    }

    /**
     * Makes sure that the OMERO file exists of the given type and id
     *
     * @param id
     *            The object id corresponding to the filename.
     * @param klass
     *            The class (table name) of the object.
     * @throws Exception
     *             Thrown if an error occurred.
     */
    void assertOtherPixelsFileDoesNotExist(Long id, PyramidFileType kind)
            throws Exception {
        String path = getOtherPixelsPath(id, kind);
        RepositoryPrx legacy = getLegacyRepository();
        assertFalse(path, legacy.fileExists(path));
    }

    /**
     * Test to delete an image and make sure pixels file is deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = {"ticket:2880", "broken"})
    public void testDeleteImageWithPixelsOnDisk() throws Exception {
        Image img = makeImageWithPixelsFile();
        Pixels pix = img.getPrimaryPixels();

        // Now check that the files have been created and then deleted.
        assertFileExists(pix.getId().getValue(), REF_PIXELS);

        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img.getId().getValue()));
        Delete2Response report = deleteWithReport(dc);
        assertFileDoesNotExist(pix.getId().getValue(), REF_PIXELS);
        assertEquals(report.deletedObjects.get(REF_PIXELS).size(), 1);
    }

    /**
     * Test to delete an image and make sure pyramid file is deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "broken")
    public void testDeleteImageWithPyramidOnDisk() throws Exception {
        Image img = makeImageWithPixelsFile(false, true, false, false);
        Pixels pix = img.getPrimaryPixels();

        // Now check that the files have been created and then deleted.
        assertOtherPixelsFileExists(pix.getId().getValue(),
                PyramidFileType.PYRAMID);
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img.getId().getValue()));
        Delete2Response report = deleteWithReport(dc);
        assertOtherPixelsFileDoesNotExist(pix.getId().getValue(),
                PyramidFileType.PYRAMID);
        assertEquals(report.deletedObjects.get(REF_PIXELS).size(), 1);
    }

    /**
     * Test to delete an image and make sure pixels and pyramid files are
     * deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "broken")
    public void testDeleteImageWithPixelsAndPyramidOnDisk() throws Exception {
        Image img = makeImageWithPixelsFile(true, true, false, false);
        Pixels pix = img.getPrimaryPixels();

        // Now check that the files have been created and then deleted.
        assertFileExists(pix.getId().getValue(), REF_PIXELS);
        assertOtherPixelsFileExists(pix.getId().getValue(),
                PyramidFileType.PYRAMID);
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img.getId().getValue()));
        Delete2Response report = deleteWithReport(dc);
        assertFileDoesNotExist(pix.getId().getValue(), REF_PIXELS);
        assertOtherPixelsFileDoesNotExist(pix.getId().getValue(),
                PyramidFileType.PYRAMID);
        assertEquals(report.deletedObjects.get(REF_PIXELS).size(), 1);
    }

    /**
     * Test to delete an image and make sure pixels and pyramid files are
     * deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "broken")
    public void testDeleteImageWithAllPyramidOnDisk() throws Exception {
        Image img = makeImageWithPixelsFile(false, true, true, true);
        Pixels pix = img.getPrimaryPixels();

        // Now check that the files have been created and then deleted.
        assertOtherPixelsFileExists(pix.getId().getValue(),
                PyramidFileType.PYRAMID);
        assertOtherPixelsFileExists(pix.getId().getValue(),
                PyramidFileType.PYRAMID_LOCK);
        assertOtherPixelsFileExists(pix.getId().getValue(),
                PyramidFileType.PYRAMID_TMP);
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img.getId().getValue()));
        Delete2Response report = deleteWithReport(dc);
        assertOtherPixelsFileDoesNotExist(pix.getId().getValue(),
                PyramidFileType.PYRAMID);
        assertOtherPixelsFileDoesNotExist(pix.getId().getValue(),
                PyramidFileType.PYRAMID_LOCK);
        assertOtherPixelsFileDoesNotExist(pix.getId().getValue(),
                PyramidFileType.PYRAMID_TMP);
        assertEquals(report.deletedObjects.get(REF_PIXELS).size(), 1);
    }

    /**
     * Test to delete an image and make sure the companion file is deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = {"ticket:2880", "broken"})
    public void testDeleteImageWithOriginalFileOnDisk() throws Exception {
        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());

        // This creates an attached OriginalFle and a subsequent Files file.
        // Is there a more concise way to achieve the same thing? cgb
        OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
                .createOriginalFile());
        FileAnnotation fa = new FileAnnotationI();
        fa.setNs(omero.rtypes.rstring(FileAnnotationData.COMPANION_FILE_NS));
        fa.setFile(of);
        fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        ImageAnnotationLink l = new ImageAnnotationLinkI();
        l.setChild(fa);
        l.setParent(img);
        iUpdate.saveAndReturnObject(l);
        long ofId = of.getId().getValue();
        RawFileStorePrx rfPrx = factory.createRawFileStore();
        try {
            rfPrx.setFileId(ofId);
            rfPrx.write(new byte[] { 1, 2, 3, 4 }, 0, 4);
        } finally {
            rfPrx.close();
        }

        // Now check that the files have been created and then deleted.
        assertFileExists(ofId, REF_ORIGINAL_FILE);
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img.getId().getValue()));
        Delete2Response report = deleteWithReport(dc);
        assertFileDoesNotExist(ofId, REF_ORIGINAL_FILE);
        assertNoUndeletedBinaries(report);
    }

    /**
     * Test to delete an image with no files associated. No exceptions should
     * arise if the files don't exist.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = "ticket:2880")
    public void testDeleteImageWithoutFilesOnDisk() throws Exception {
        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage());
        Pixels pixels = img.getPrimaryPixels();
        long pixId = pixels.getId().getValue();

        OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(mmFactory
                .createOriginalFile());
        FileAnnotation fa = new FileAnnotationI();
        fa.setNs(omero.rtypes.rstring(FileAnnotationData.COMPANION_FILE_NS));
        fa.setFile(of);
        fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        ImageAnnotationLink l = new ImageAnnotationLinkI();
        l.setChild(fa);
        l.setParent(img);
        iUpdate.saveAndReturnObject(l);
        long ofId = of.getId().getValue();
        // Now check that the files have NOT been created and then deleted.
        assertFileDoesNotExist(pixId, REF_PIXELS);
        assertFileDoesNotExist(ofId, REF_ORIGINAL_FILE);
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img.getId().getValue()));
        Delete2Response report = deleteWithReport(dc);
        assertNoUndeletedBinaries(report);
    }

    /**
     * Test to delete an image and make sure the thumbnail on disk is deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = {"ticket:2880", "broken"})
    public void testDeleteImageWithThumbnailOnDisk() throws Exception {
        File f = File.createTempFile("testDeleteImageWithThumbnailOnDisk"
                + ModelMockFactory.FORMATS[0], "."
                + ModelMockFactory.FORMATS[0]);
        mmFactory.createImageFile(f, ModelMockFactory.FORMATS[0]);
        OMEROMetadataStoreClient importer = new OMEROMetadataStoreClient();
        importer.initialize(factory);

        List<Pixels> list;
        try {
            list = importFile(importer, f, ModelMockFactory.FORMATS[0], false);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels pixels = list.get(0);
        long id = pixels.getId().getValue();
        List<Long> ids = new ArrayList<Long>();
        ids.add(id);
        long imageID = pixels.getImage().getId().getValue();

        ThumbnailStorePrx svc = factory.createThumbnailStore();
        // make sure we have a thumbnail on disk
        // request a different size to make sure all thumbnails are deleted.
        Map<Long, byte[]> thumbnails = svc.getThumbnailSet(
                omero.rtypes.rint(40), omero.rtypes.rint(40), ids);
        byte[] values = thumbnails.get(id);
        assertNotNull(values);
        assertTrue(values.length > 0);
        String sql = "select i from Thumbnail i where i.pixels.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);
        List<IObject> objects = iQuery.findAllByQuery(sql, param);
        assertNotNull(objects);
        assertTrue(objects.size() > 0);

        List<Long> thumbIds = new ArrayList<Long>();
        Iterator<IObject> i = objects.iterator();
        long thumbId;
        while (i.hasNext()) {
            thumbId = i.next().getId().getValue();
            assertFileExists(thumbId, REF_THUMBNAIL);
        }

        // delete the image.
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(imageID));
        Delete2Response report = deleteWithReport(dc);

        assertNoUndeletedBinaries(report);
        assertFileDoesNotExist(id, "Pixels");
        Iterator<Long> j = thumbIds.iterator();
        while (j.hasNext()) {
            assertFileDoesNotExist(j.next(), REF_THUMBNAIL);
        }
    }

    /**
     * Test to delete an image and make sure the thumbnail on disk is deleted.
     * The image has been viewed another member of the group.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test
    public void testDeleteImageViewedByOtherWithThumbnailOnDiskRWRW()
            throws Exception {
        EventContext ownerCtx = newUserAndGroup("rwrw--");
        File f = File.createTempFile("testDeleteImageWithThumbnailOnDisk"
                + ModelMockFactory.FORMATS[0], "."
                + ModelMockFactory.FORMATS[0]);
        mmFactory.createImageFile(f, ModelMockFactory.FORMATS[0]);
        OMEROMetadataStoreClient importer = new OMEROMetadataStoreClient();
        importer.initialize(factory);

        List<Pixels> list;
        try {
            list = importFile(importer, f, ModelMockFactory.FORMATS[0], false);
        } catch (Throwable e) {
            throw new Exception("cannot import image", e);
        }
        Pixels pixels = list.get(0);
        long id = pixels.getId().getValue();
        List<Long> ids = new ArrayList<Long>();
        ids.add(id);

        long imageID = pixels.getImage().getId().getValue();
        ThumbnailStorePrx svc = factory.createThumbnailStore();
        // make sure we have a thumbnail on disk
        // request a different size to make sure all thumbnails are deleted.
        int sizeX = 96;
        int sizeY = 96;
        Map<Long, byte[]> thumbnails = svc.getThumbnailSet(
                omero.rtypes.rint(sizeX), omero.rtypes.rint(sizeY), ids);
        assertNotNull(thumbnails.get(id));
        String sql = "select i from Thumbnail i where i.pixels.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);
        List<IObject> objects = iQuery.findAllByQuery(sql, param);
        assertNotNull(objects);
        assertTrue(objects.size() > 0);

        List<Long> thumbIds = new ArrayList<Long>();
        Iterator<IObject> i = objects.iterator();
        while (i.hasNext()) {
            thumbIds.add(i.next().getId().getValue());
        }

        newUserInGroup(ownerCtx);
        svc = factory.createThumbnailStore();
        thumbnails = svc.getThumbnailSet(omero.rtypes.rint(sizeX),
                omero.rtypes.rint(sizeY), ids);
        assertNotNull(thumbnails.get(id));

        objects = iQuery.findAllByQuery(sql, param);
        assertTrue(objects.size() > 0);
        i = objects.iterator();
        long thumbId;
        while (i.hasNext()) {
            thumbId = i.next().getId().getValue();
            if (!thumbIds.contains(thumbId))
                thumbIds.add(thumbId);
        }
        disconnect();

        loginUser(ownerCtx);
        // Now try to delete the image.
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(imageID));
        Delete2Response report = deleteWithReport(dc);
        Iterator<Long> j = thumbIds.iterator();
        while (j.hasNext()) {
            assertFileDoesNotExist(j.next(), REF_THUMBNAIL);
        }
        assertNoUndeletedBinaries(report);
    }

    /**
     * Test to delete a dataset containing and image that is also in another
     * dataset. The Image and its Pixels file should NOT be deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = {"ticket:3031", "broken"})
    public void testDeletingDatasetWithPixelsFiles() throws Exception {

        Dataset ds1 = new DatasetI();
        ds1.setName(omero.rtypes.rstring("#3031.1"));
        Dataset ds2 = new DatasetI();
        ds2.setName(omero.rtypes.rstring("#3031.2"));

        Image img = makeImageWithPixelsFile();
        Pixels pix = img.getPrimaryPixels();

        // Now check that the files have been created and then deleted.
        assertFileExists(pix.getId().getValue(), REF_PIXELS);

        ds1.linkImage(img);
        ds1 = (Dataset) iUpdate.saveAndReturnObject(ds1);
        ds2.linkImage(img);
        ds2 = (Dataset) iUpdate.saveAndReturnObject(ds2);

        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Dataset.class.getSimpleName(),
                Collections.singletonList(ds2.getId().getValue()));
        delete(client, dc);

        assertDoesNotExist(ds2);
        assertExists(ds1);
        assertExists(img);
        assertFileExists(pix.getId().getValue(), REF_PIXELS);
    }

    /**
     * Test to delete a dataset containing multiple images all Pixels files
     * should be deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = {"ticket:3130", "broken"})
    public void testDeletingDatasetWithSeveralPixelsFiles() throws Exception {

        Dataset ds = new DatasetI();
        ds.setName(omero.rtypes.rstring("#3130"));

        Image img1 = makeImageWithPixelsFile();
        Pixels pix1 = img1.getPrimaryPixels();
        // A second Image
        Image img2 = makeImageWithPixelsFile();
        Pixels pix2 = img2.getPrimaryPixels();

        // link to dataset
        ds.linkImage(img1);
        ds = (Dataset) iUpdate.saveAndReturnObject(ds);
        ds.linkImage(img2);
        ds = (Dataset) iUpdate.saveAndReturnObject(ds);

        // Now check that the files have been created and then deleted.
        assertFileExists(pix1.getId().getValue(), REF_PIXELS);
        assertFileExists(pix2.getId().getValue(), REF_PIXELS);

        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Dataset.class.getSimpleName(),
                Collections.singletonList(ds.getId().getValue()));
        Delete2Response report = deleteWithReport(dc);

        assertNoUndeletedBinaries(report);
        assertNoneExist(ds, img1, img2, pix1, pix2);
        assertFileDoesNotExist(pix1.getId().getValue(), REF_PIXELS);
        assertFileDoesNotExist(pix2.getId().getValue(), REF_PIXELS);
    }

    /**
     * Test to delete a dataset containing multiple images all Pixels files
     * should be deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = {"ticket:3148", "broken"})
    public void testDeletingImageWithSeveralOriginalFiles() throws Exception {
        Image img = (Image) iUpdate
                .saveAndReturnObject(mmFactory.createImage()).proxy();

        // This creates an attached OriginalFle and a subsequent Files file.
        // Is there a more concise way to achieve the same thing? cgb

        OriginalFile of1 = makeFile();

        FileAnnotation fa = new FileAnnotationI();
        fa.setNs(omero.rtypes.rstring(FileAnnotationData.COMPANION_FILE_NS));
        fa.setFile(of1);
        fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        ImageAnnotationLink l = new ImageAnnotationLinkI();
        l.setChild(fa);
        l.setParent(img);
        iUpdate.saveAndReturnObject(l);

        OriginalFile of2 = makeFile();

        fa = new FileAnnotationI();
        fa.setNs(omero.rtypes.rstring(FileAnnotationData.COMPANION_FILE_NS));
        fa.setFile(of2);
        fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        l = new ImageAnnotationLinkI();
        l.setChild(fa);
        l.setParent(img);
        iUpdate.saveAndReturnObject(l);

        // Now check that the files have been created and then deleted.
        assertFileExists(of1.getId().getValue(), REF_ORIGINAL_FILE);
        assertFileExists(of2.getId().getValue(), REF_ORIGINAL_FILE);

        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img.getId().getValue()));
        Delete2Response report = deleteWithReport(dc);

        assertNoneExist(img, of1, of2);
        assertFileDoesNotExist(of1.getId().getValue(), REF_ORIGINAL_FILE);
        assertFileDoesNotExist(of2.getId().getValue(), REF_ORIGINAL_FILE);
        assertNoUndeletedBinaries(report);
    }

    /**
     * Test to delete a dataset containing and image that is from a Well. The
     * Image and its Pixels file should NOT be deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = {"ticket:2946", "broken"})
    public void testDeleteDatasetThatContainsImageFromAWell() throws Exception {
        Image img = makeImageWithPixelsFile();
        Pixels pix = img.getPrimaryPixels();
        Plate p = (Plate) iUpdate.saveAndReturnObject(mmFactory.createPlate(1,
                1, 1, 0, false));
        List<Well> wells = loadWells(p.getId().getValue(), false);
        wells.get(0).copyWellSamples().get(0).setImage(img);
        Well well = (Well) iUpdate.saveAndReturnObject(wells.get(0));

        Dataset ds = new DatasetI();
        ds.setName(omero.rtypes.rstring("#2946"));
        // link to dataset
        ds.linkImage(img);
        ds = (Dataset) iUpdate.saveAndReturnObject(ds);

        // Now check that the file has been created.
        assertFileExists(pix.getId().getValue(), REF_PIXELS);

        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Dataset.class.getSimpleName(),
                Collections.singletonList(ds.getId().getValue()));
        Delete2Response report = deleteWithReport(dc);

        // The dataset should be gone but nothing else.
        assertNoneExist(ds);
        assertAllExist(p, well, img, pix);
        assertFileExists(pix.getId().getValue(), REF_PIXELS);
        assertNoUndeletedBinaries(report);
    }

    /**
     * Test to try to delete an image owned by another user in a collaborative
     * group i.e. RWR---
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = {"ticket:2946", "broken"})
    public void testDeleteImagePixelsFileOwnedByOtherRWR() throws Exception {
        // set up collaborative group and one user, "the owner"
        newUserAndGroup("rwr---");

        Image img = makeImageWithPixelsFile();
        Pixels pix = img.getPrimaryPixels();

        // Now check that the file has been created.
        assertFileExists(pix.getId().getValue(), REF_PIXELS);

        // create another user and try to delete the image
        newUserInGroup();
        Delete2 dc = new Delete2();
        dc.targetObjects = ImmutableMap.<String, List<Long>>of(
                Image.class.getSimpleName(),
                Collections.singletonList(img.getId().getValue()));
        Delete2Response report = deleteWithReport(dc);

        // check the image exists as the owner
        assertExists(img);
        // Now check that the file has not been deleted.
        assertFileExists(pix.getId().getValue(), REF_PIXELS);
        assertNoUndeletedBinaries(report);
    }

    /**
     * Test to remove a file and try to save it using the RawFileStore.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = {"ticket:3140", "broken"},
            expectedExceptions = ResourceError.class)
    public void testSaveThrowsResourceErrorIfDeleted() throws Exception {
        OriginalFile of = makeFile();
        RawFileStorePrx rfs = factory.createRawFileStore();
        try {
            rfs.setFileId(of.getId().getValue());
            rfs.write(new byte[1], 0, 1);
            removeFile(of.getId().getValue(), "OriginalFile");
            rfs.save();
            fail("Should not reach here.");
        } finally {
            rfs.close();
        }
    }

    /**
     * Test to check the <code>close</code> method of the RawFileStore after the
     * file has been deleted.
     *
     * @throws Exception
     *             Thrown if an error occurred.
     */
    @Test(groups = {"ticket:3140", "broken"},
            expectedExceptions = ResourceError.class)
    public void testCloseThrowsResourceErrorIfDeleted() throws Exception {
        OriginalFile of = makeFile();
        RawFileStorePrx rfs = factory.createRawFileStore();
        try {
            rfs.setFileId(of.getId().getValue());
            rfs.write(new byte[1], 0, 1);
            removeFile(of.getId().getValue(), "OriginalFile");
            fail("Should not reach here.");
        } finally {
            rfs.close();
        }
    }

    /**
     * Check that a set of original files may be deleted by a single {@link Delete2} request
     * regardless of how they are ordered with regard to containing one another in a directory hierarchy,
     * as enforced by {@code _fs_dir_delete trigger} and by the underlying filesystem.
     * @throws Throwable unexpected
     */
    @Test
    public void testRecursiveDelete() throws Throwable {
        /* for HQL queries */
        String query;
        Parameters params;
        List<List<RType>> results;

        /* keep count of how many files are expected to be deleted */
        int fileCount = 0;

        /* import a small image to discover a suitable location in the repository for further testing */
        final File imageFile = ResourceUtils.getFile("classpath:red.png");
        final long filesetId = importFile(imageFile, "png").get(0).getImage().getFileset().getId().getValue();
        fileCount += 2;  /* for PNG file and import log */

        /* find the managed repository directory for the imported image file */
        query = "SELECT originalFile.path FROM FilesetEntry WHERE fileset.id = :id";
        params = new Parameters();
        params.map = ImmutableMap.<String, RType>of("id", omero.rtypes.rlong(filesetId));
        results = iQuery.projection(query, params);
        String pathName = ((RString) results.get(0).get(0)).getValue();

        /* create a deeply nested directory hierarchy with files at every level */
        final int count = 32;
        final RepositoryPrx mrepo = getManagedRepository();
        final String directoryName = "bar";
        final String fileName = "baz";
        final List<Long> fileIds = new ArrayList<Long>();
        final byte[] toWrite = "dummy file".getBytes();
        query = "SELECT id FROM OriginalFile WHERE mimetype = 'Directory' AND path = :path AND name = :name";
        /* recurse into deeper directories */
        for (int i = 0; i < count; i++) {
            /* create another directory and note its ID */
            mrepo.makeDir(pathName + directoryName, false);
            params = new Parameters();
            params.map = ImmutableMap.<String, RType>of("path", omero.rtypes.rstring(pathName),
                                                        "name", omero.rtypes.rstring(directoryName));
            results = iQuery.projection(query, params);
            fileIds.add(((RLong) results.get(0).get(0)).getValue());
            fileCount++;

            /* create another file and note its ID */
            final RawFileStorePrx rfs = mrepo.file(pathName + fileName, "rw");
            rfs.write(toWrite, 0, toWrite.length);
            fileIds.add(rfs.save().getId().getValue());
            rfs.close();
            fileCount++;

            pathName += directoryName + '/';
        }
        /* shuffle the file ID list randomly */
        Collections.shuffle(fileIds);

        /* prepare to delete all the files and directories that were created */
        final Delete2 request = new Delete2();
        request.targetObjects = new HashMap<String, List<Long>>();
        request.targetObjects.put("Fileset", Collections.singletonList(filesetId));
        request.targetObjects.put("OriginalFile", fileIds);

        /* perform the deletion and confirm that it successfully deletes all the files */
        final Delete2Response deletions = (Delete2Response) doChange(root, root.getSession(), request, true);
        assertEquals(fileCount, deletions.deletedObjects.get(ome.model.core.OriginalFile.class.getName()).size());
    }
}
