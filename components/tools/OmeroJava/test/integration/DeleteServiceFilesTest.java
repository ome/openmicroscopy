/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import ome.formats.OMEROMetadataStoreClient;
import omero.ApiUsageException;
import omero.ServerError;
import omero.api.IDeletePrx;
import omero.api.IRenderingSettingsPrx;
import omero.api.RawFileStorePrx;
import omero.api.ThumbnailStorePrx;
import omero.api.delete.DeleteCommand;
import omero.api.delete.DeleteReport;
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
import omero.sys.EventContext;
import omero.sys.ParametersI;

import org.apache.commons.io.FilenameUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import pojos.FileAnnotationData;

/** 
* Collections of tests for the <code>Delete</code> service.
*
* @author Jean-Marie Burel &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:j.burel@dundee.ac.uk">j.burel@dundee.ac.uk</a>
* @author Donald MacDonald &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:donald@lifesci.dundee.ac.uk">donald@lifesci.dundee.ac.uk</a>
* @author Colin Blackburn &nbsp;&nbsp;&nbsp;&nbsp;
* <a href="mailto:c.blackburn@dundee.ac.uk">c.blackburn@dundee.ac.uk</a>
* @version 3.0
* <small>
* (<b>Internal version:</b> $Revision: $Date: $)
* </small>
* @since 3.0-Beta4
*/
@Test(groups = {"delete", "integration"})
public class DeleteServiceFilesTest 
   extends AbstractTest
{
	
	/** Reference to the <code>Pixels</code> class. */
	private static final String REF_PIXELS = "Pixels";
	
	/** Reference to the <code>OriginalFile</code> class. */
	private static final String REF_ORIGINAL_FILE = "OriginalFile";
	
	/** Reference to the <code>Thumbnail</code> class. */
	private static final String REF_THUMBNAIL = "Thumbnail";

	/** Reference to the standard directory. */
	private String dataDir; 

	/**
	 * Set the data directory for the tests. This is needed to find the 
	 * correct repository to test whether deletes have been successful.
	 */
	@BeforeClass
	public void setDataDir() throws Exception {
		dataDir = root.getSession().getConfigService().getConfigValue(
		"omero.data.dir");
	}

	/**
	 * Since so many tests rely on counting the number of objects present
	 * globally, we're going to start each method with a new user in a new
	 * group.
	 */
	@BeforeMethod
	public void createNewUser() throws Exception {
		newUserAndGroup("rw----");
		iDelete = factory.getDeleteService();
	}

	/**
	 * Since we are creating a new client on each invocation, we should also
	 * clean it up. Note: {@link #newUserAndGroup(String)} also closes, but
	 * not the very last invocation.
	 */
	@AfterMethod
	public void close() throws Exception {
		if (client == null) {
			client.__del__();
			client = null;
		}
	}

	/**
	 * Basic asynchronous delete command. Used in order to reduce the number
	 * of places that we do the same thing in case the API changes.
	 * 
	 * @param dc The SINGLE command to handle.
	 * @throws ApiUsageException
	 * @throws ServerError
	 * @throws InterruptedException
	 */
	private DeleteReport deleteWithReport(DeleteCommand dc)
	throws ApiUsageException, ServerError,
	InterruptedException
	{
		return singleDeleteWithReport(iDelete, client, dc);
	}

	/**
	 * Forms a path depending on the type of file to be deleted
	 * and its id.
	 * 
	 * @param dataDir The path to the directory
	 * @param klass The type of object to handle.
	 * @param id The identifier of the object.
	 */
	private String getPath(String klass, Long id) 
	throws Exception 
	{
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
			prefix = FilenameUtils.concat(dataDir,"Thumbnails");
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
	 * Gets a public repository on the OMERO data directory if one exists.
	 * 
	 * @return See above.
	 * @throws Exception  Thrown if an error occurred.
	 */
	RepositoryPrx getLegacyRepository()
	throws Exception
	{
		RepositoryPrx legacy = null;
		RepositoryMap rm = factory.sharedResources().repositories();
		int repoCount = 0;
		String s = dataDir;
		for (OriginalFile desc: rm.descriptions) {
			String repoPath = desc.getPath().getValue() + 
			desc.getName().getValue();
			s += "\nFound repository:" + desc.getPath().getValue() + 
			desc.getName().getValue();
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
	 * @param id The object id corresponding to the filename.
	 * @param klass The class (table name) of the object.
	 * @throws Exception  Thrown if an error occurred.
	 */
	void assertFileExists(Long id, String klass)
	throws Exception
	{   
		String path = getPath(klass, id);
		RepositoryPrx legacy = getLegacyRepository();
		assertTrue(path + " does not exist!", legacy.fileExists(path));
	}

	/**
	 * Makes sure that the OMERO file exists of the given type and id
	 * 
	 * @param id The object id corresponding to the filename.
	 * @param klass The class (table name) of the object.
	 * @throws Exception  Thrown if an error occurred.
	 */
	void assertFileDoesNotExist(Long id, String klass) 
	throws Exception 
	{  
		String path = getPath(klass, id);
		RepositoryPrx legacy = getLegacyRepository();
		assertFalse(path + " exists!", legacy.fileExists(path));
	}  

	/**
	 * Test to delete an image and make sure pixels file is deleted.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test(groups = "ticket:2880")
	public void testDeleteImageWithPixelsOnDisk() 
	throws Exception 
	{
		Image img = (Image) iUpdate.saveAndReturnObject(
				mmFactory.createImage());
		Pixels pixels = img.getPrimaryPixels();
		long pixId = pixels.getId().getValue();
		IRenderingSettingsPrx rsPrx = factory.getRenderingSettingsService();
		List<Long> ids = new ArrayList<Long>();
		ids.add(pixId);
		rsPrx.resetDefaultsInSet(Pixels.class.getName(), ids);

		//Now check that the files have been created and then deleted.
		assertFileExists(pixId, REF_PIXELS);

		DeleteReport report = deleteWithReport(
				new DeleteCommand(DeleteServiceTest.REF_IMAGE, 
						img.getId().getValue(), null));
		assertFileDoesNotExist(pixId, REF_PIXELS);
		assertTrue(report.undeletedFiles.get(REF_PIXELS).length == 0);
	}

	/**
	 * Test to delete an image and make sure the companion file  is deleted.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test(groups = "ticket:2880")
	public void testDeleteImageWithOriginalFileOnDisk() 
	throws Exception 
	{
		Image img = (Image) iUpdate.saveAndReturnObject(
				mmFactory.createImage());

		// This creates an attached OriginalFle and a subsequent Files file.
		// Is there a more concise way to achieve the same thing? cgb
		OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(
				mmFactory.createOriginalFile());
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
			rfPrx.write(new byte[]{1, 2, 3, 4}, 0, 4);
		} finally {
			rfPrx.close();
		}

		//Now check that the files have been created and then deleted.
		assertFileExists(ofId, REF_ORIGINAL_FILE);
		DeleteReport report = deleteWithReport(
				new DeleteCommand(DeleteServiceTest.REF_IMAGE, 
						img.getId().getValue(), null));
		assertFileDoesNotExist(ofId, REF_ORIGINAL_FILE);
		assertNoUndeletedBinaries(report);
	}

	/**
	 * Test to delete an image with no files associated.
	 * No exceptions should arise if the files don't exist.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test(groups = "ticket:2880")
	public void testDeleteImageWithoutFilesOnDisk() 
	throws Exception
	{
		Image img = (Image) iUpdate.saveAndReturnObject(
				mmFactory.createImage());
		Pixels pixels = img.getPrimaryPixels();
		long pixId = pixels.getId().getValue();

		OriginalFile of = (OriginalFile) iUpdate.saveAndReturnObject(
				mmFactory.createOriginalFile());
		FileAnnotation fa = new FileAnnotationI();
		fa.setNs(omero.rtypes.rstring(FileAnnotationData.COMPANION_FILE_NS));
		fa.setFile(of);
		fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
		ImageAnnotationLink l = new ImageAnnotationLinkI();
		l.setChild(fa);
		l.setParent(img);
		iUpdate.saveAndReturnObject(l);
		long ofId = of.getId().getValue();
		//Now check that the files have NOT been created and then deleted.
		assertFileDoesNotExist(pixId, REF_PIXELS);
		assertFileDoesNotExist(ofId, REF_ORIGINAL_FILE);
		DeleteReport report = deleteWithReport(
				new DeleteCommand(DeleteServiceTest.REF_IMAGE,
						img.getId().getValue(), null));
        assertNoUndeletedBinaries(report);
	}

	/**
	 * Test to delete an image and make sure the thumbnail on disk is deleted.
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test(groups = "ticket:2880")
	public void testDeleteImageWithThumbnailOnDisk() 
	throws Exception
	{
		File f = File.createTempFile("testDeleteImageWithThumbnailOnDisk"
				+ModelMockFactory.FORMATS[0], 
				"."+ModelMockFactory.FORMATS[0]);
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
		//make sure we have a thumbnail on disk
		//request a different size to make sure all thumbnails are deleted.
		Map<Long, byte[]> thumbnails = svc.getThumbnailSet(
				omero.rtypes.rint(40),  omero.rtypes.rint(40), ids);
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

		//delete the image.
		DeleteReport report = deleteWithReport(new DeleteCommand(
				DeleteServiceTest.REF_IMAGE, imageID, null));

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
	 * @throws Exception Thrown if an error occurred.
	 */
	@Test
	public void testDeleteImageViewedByOtherWithThumbnailOnDiskRWRW() 
	throws Exception
	{
		EventContext ownerCtx = newUserAndGroup("rwrw--");
		File f = File.createTempFile("testDeleteImageWithThumbnailOnDisk"
				+ModelMockFactory.FORMATS[0], 
				"."+ModelMockFactory.FORMATS[0]);
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
		//make sure we have a thumbnail on disk
		//request a different size to make sure all thumbnails are deleted.
		int sizeX = 96;
		int sizeY = 96;
		Map<Long, byte[]> thumbnails = svc.getThumbnailSet(
				omero.rtypes.rint(sizeX),  omero.rtypes.rint(sizeY), ids);
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
		thumbnails = svc.getThumbnailSet(
				omero.rtypes.rint(sizeX),  omero.rtypes.rint(sizeY), ids);
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
		//Now try to delete the image.
		DeleteReport report = deleteWithReport(new DeleteCommand(
				DeleteServiceTest.REF_IMAGE, imageID, null));
		Iterator<Long> j = thumbIds.iterator();
		while (j.hasNext()) {
			assertFileDoesNotExist(j.next(), REF_THUMBNAIL);
		}
		assertNoUndeletedBinaries(report);
	}

    /**
     * Test to delete a dataset containing and image that is 
     * also in another dataset. The Image and its Pixels file
     * should NOT be deleted.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:3031")
    public void testDeletingDatasetWithPixelsFiles() throws Exception {

        Dataset ds1 = new DatasetI();
        ds1.setName(omero.rtypes.rstring("#3031.1"));
        Dataset ds2 = new DatasetI();
        ds2.setName(omero.rtypes.rstring("#3031.2"));

        Image img = (Image) iUpdate.saveAndReturnObject(
                mmFactory.createImage());
        Pixels pixels = img.getPrimaryPixels();
        long pixId = pixels.getId().getValue();
        IRenderingSettingsPrx rsPrx = factory.getRenderingSettingsService();
        List<Long> ids = new ArrayList<Long>();
        ids.add(pixId);
        rsPrx.resetDefaultsInSet(Pixels.class.getName(), ids);

        //Now check that the files have been created and then deleted.
        assertFileExists(pixId, REF_PIXELS);

        ds1.linkImage(img);
        ds1 = (Dataset) iUpdate.saveAndReturnObject(ds1);
        ds2.linkImage(img);
        ds2 = (Dataset) iUpdate.saveAndReturnObject(ds2);

        delete(client, new DeleteCommand(DeleteServiceTest.REF_DATASET, 
                ds2.getId().getValue(),
                null));

        assertDoesNotExist(ds2);
        assertExists(ds1);
        assertExists(img);
        assertFileExists(pixId, REF_PIXELS);
    }
    
    /**
     * Test to delete a dataset containing multiple images
     * all Pixels files should be deleted.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:3130")
    public void testDeletingDatasetWithSeveralPixelsFiles() throws Exception {

        Dataset ds = new DatasetI();
        ds.setName(omero.rtypes.rstring("#3130"));
 
        Image img1 = (Image) iUpdate.saveAndReturnObject(
                mmFactory.createImage());
        Pixels pix1 = img1.getPrimaryPixels();
        long pixId1 = pix1.getId().getValue();
        IRenderingSettingsPrx rsPrx = factory.getRenderingSettingsService();
        List<Long> ids = new ArrayList<Long>();
        ids.add(pixId1);
        rsPrx.resetDefaultsInSet(Pixels.class.getName(), ids);

        // A second Image
        Image img2 = (Image) iUpdate.saveAndReturnObject(
                mmFactory.createImage());
        Pixels pix2 = img2.getPrimaryPixels();
        long pixId2 = pix2.getId().getValue();
        rsPrx = factory.getRenderingSettingsService();
        ids = new ArrayList<Long>();
        ids.add(pixId2);
        rsPrx.resetDefaultsInSet(Pixels.class.getName(), ids);

        // link to dataset
        ds.linkImage(img1);
        ds = (Dataset) iUpdate.saveAndReturnObject(ds);
        ds.linkImage(img2);
        ds = (Dataset) iUpdate.saveAndReturnObject(ds);

        //Now check that the files have been created and then deleted.
        assertFileExists(pixId1, REF_PIXELS);
        assertFileExists(pixId2, REF_PIXELS);

        DeleteReport report = deleteWithReport(
                new DeleteCommand(DeleteServiceTest.REF_DATASET, 
                ds.getId().getValue(),
                null));        
        
        assertNoUndeletedBinaries(report);
        assertNoneExist(ds, img1, img2, pix1, pix2);
        assertFileDoesNotExist(pixId1, REF_PIXELS);
        assertFileDoesNotExist(pixId2, REF_PIXELS);

    }
    
    /**
     * Test to delete a dataset containing multiple images
     * all Pixels files should be deleted.
     * 
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:3148")
    public void testDeletingImageWithSeveralOriginalFiles() throws Exception {

        Image img = (Image) iUpdate.saveAndReturnObject(
                mmFactory.createImage()).proxy();

        // This creates an attached OriginalFle and a subsequent Files file.
        // Is there a more concise way to achieve the same thing? cgb
        OriginalFile of1 = (OriginalFile) iUpdate.saveAndReturnObject(
                mmFactory.createOriginalFile());
        FileAnnotation fa = new FileAnnotationI();
        fa.setNs(omero.rtypes.rstring(FileAnnotationData.COMPANION_FILE_NS));
        fa.setFile(of1);
        fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        ImageAnnotationLink l = new ImageAnnotationLinkI();
        l.setChild(fa);
        l.setParent(img);
        iUpdate.saveAndReturnObject(l);
        long ofId1 = of1.getId().getValue();
        RawFileStorePrx rfPrx = factory.createRawFileStore();
        try {
            rfPrx.setFileId(ofId1);
            rfPrx.write(new byte[]{1, 2, 3, 4}, 0, 4);
        } finally {
            rfPrx.close();
        }

        OriginalFile of2 = (OriginalFile) iUpdate.saveAndReturnObject(
                mmFactory.createOriginalFile());
        fa = new FileAnnotationI();
        fa.setNs(omero.rtypes.rstring(FileAnnotationData.COMPANION_FILE_NS));
        fa.setFile(of2);
        fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        l = new ImageAnnotationLinkI();
        l.setChild(fa);
        l.setParent(img);
        iUpdate.saveAndReturnObject(l);
        long ofId2 = of2.getId().getValue();
        rfPrx = factory.createRawFileStore();
        try {
            rfPrx.setFileId(ofId2);
            rfPrx.write(new byte[]{1, 2, 3, 4}, 0, 4);
        } finally {
            rfPrx.close();
        }

        //Now check that the files have been created and then deleted.
        assertFileExists(ofId1, REF_ORIGINAL_FILE);
        assertFileExists(ofId2, REF_ORIGINAL_FILE);
        
        DeleteReport report = deleteWithReport(
                new DeleteCommand(DeleteServiceTest.REF_IMAGE, 
                        img.getId().getValue(), null));
        
        assertNoneExist(img, of1, of2);
        assertFileDoesNotExist(ofId1, REF_ORIGINAL_FILE);
        assertFileDoesNotExist(ofId2, REF_ORIGINAL_FILE);
        assertNoUndeletedBinaries(report);
    }

    private void assertNoUndeletedBinaries(DeleteReport report) {
        assertNoUndeletedThumbnails(report);
        assertNoUndeletedFiles(report);
        assertNoUndeletedPixels(report);
    }

    private void assertNoUndeletedThumbnails(DeleteReport report) {
        long[] tbIds = report.undeletedFiles.get(REF_THUMBNAIL);
        assertTrue(Arrays.toString(tbIds), tbIds == null || tbIds.length == 0);
    }

    private void assertNoUndeletedFiles(DeleteReport report) {
        long[] fileIds = report.undeletedFiles.get(REF_ORIGINAL_FILE);
        assertTrue(Arrays.toString(fileIds), fileIds == null || fileIds.length == 0);
    }

    private void assertNoUndeletedPixels(DeleteReport report) {
        long[] pixIds = report.undeletedFiles.get(REF_PIXELS);
        assertTrue(Arrays.toString(pixIds), pixIds == null || pixIds.length == 0);
    }

}