/*
 * $Id$
 *
 *   Copyright 2006-2010 University of Dundee. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration;

import java.io.File;
import java.util.ArrayList;
import java.util.Formatter;
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
import omero.grid.RepositoryMap;
import omero.grid.RepositoryPrx;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.sys.ParametersI;

import org.apache.commons.io.FilenameUtils;
import org.testng.annotations.AfterMethod;
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
	
   /** Helper reference to the <code>IDelete</code> service. */
   private IDeletePrx iDelete;
 
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
    * @param dc The command to handle.
    * @throws ApiUsageException
    * @throws ServerError
    * @throws InterruptedException
    */
   private String delete(DeleteCommand...dc)
       throws ApiUsageException, ServerError,
       InterruptedException
   {
	   return delete(iDelete, client, dc);
   }

   /**
    * Forms a path depending on the type of file to be deleted
    * and its id.
    * 
    * @param dataDir The path to the directory
    * @param klass The type of object to handle.
    * @param id The identifier of the object.
    */
   private String getPath(String dataDir, String klass, Long id) 
   throws Exception {
       String suffix = "";
       String prefix = "";
       Long remaining = id;
       Long dirno = 0L;

       if (id == null) {
           throw new NullPointerException("Expecting a not-null id.");
       }

       if (klass.equals("OriginalFile")) {
           prefix = dataDir + "Files";
       } else if (klass.equals("Pixels")) {
           prefix = dataDir + "Pixels";
       } else if (klass.equals("Thumbnail")) { 
           prefix = dataDir + "Thumbnails";
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
       String dataDir = root.getProperty("omero.data.dir");
       for (OriginalFile desc: rm.descriptions) {
           String repoPath = desc.getPath().getValue() + 
           desc.getName().getValue() + File.separator;
           if (repoPath.equals(dataDir)) {
               legacy = rm.proxies.get(repoCount);
               break;
           }
           repoCount++;
       }
       if (legacy == null) {
           throw(new Exception("Unable to find legacy repository"));
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
       String path = getPath(root.getProperty("omero.data.dir"), klass, id);
       RepositoryPrx legacy = getLegacyRepository();
       assertTrue(legacy.fileExists(path));
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
       String path = getPath(root.getProperty("omero.data.dir"), klass, id);
       RepositoryPrx legacy = getLegacyRepository();
       assertFalse(legacy.fileExists(path));
   }  
   
   /**
    * Test to delete an image and make sure the companion file 
    * and pixels file is deleted.
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
        assertFileExists(pixId, "Pixels");
        delete(new DeleteCommand(DeleteServiceTest.REF_IMAGE, 
        		img.getId().getValue(), null));
        assertFileDoesNotExist(pixId, "Pixels");
    }

    /**
     * Test to delete an image and make sure the companion file 
     * and pixels file is deleted.
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
            rfPrx.write(new byte[]{1,2,3,4}, 0, 4);
        } finally {
            rfPrx.close();
        }

        //Now check that the files have been created and then deleted.
        assertFileExists(ofId, "OriginalFile");
        delete(new DeleteCommand(DeleteServiceTest.REF_IMAGE, 
        		img.getId().getValue(), null));
        assertFileDoesNotExist(ofId, "OriginalFile");
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
        assertFileDoesNotExist(pixId, "Pixels");
        assertFileDoesNotExist(ofId, "OriginalFile");
        delete(new DeleteCommand(DeleteServiceTest.REF_IMAGE,
        		img.getId().getValue(), null));
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
        Map<Long, byte[]> thumbnails = svc.getThumbnailSet(omero.rtypes.rint(96), 
                omero.rtypes.rint(96), ids);
        byte[] values = thumbnails.get(id);
        assertNotNull(values);
        assertTrue(values.length > 0);
        String sql = "select i from Thumbnail i where i.pixels.id = :id";
        ParametersI param = new ParametersI();
        param.addId(id);
        assertNotNull(iQuery.findByQuery(sql, param));
        
        //delete the image.
        delete(new DeleteCommand(DeleteServiceTest.REF_IMAGE, imageID, null));
        assertFileDoesNotExist(id, "Pixels");
        assertNull(iQuery.findByQuery(sql, param));
    }

}