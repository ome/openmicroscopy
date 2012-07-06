/*
 * $Id$
 *
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */
package integration.delete;

import static omero.rtypes.rlong;
import static omero.rtypes.rstring;
import integration.AbstractServerTest;
import integration.DeleteServiceTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import omero.RLong;
import omero.RString;
import omero.cmd.Delete;
import omero.model.Annotation;
import omero.model.Channel;
import omero.model.FileAnnotation;
import omero.model.FileAnnotationI;
import omero.model.IObject;
import omero.model.Image;
import omero.model.ImageAnnotationLink;
import omero.model.ImageAnnotationLinkI;
import omero.model.LongAnnotation;
import omero.model.LongAnnotationI;
import omero.model.OriginalFile;
import omero.model.Pixels;
import omero.model.PlaneInfo;
import omero.model.Roi;
import omero.model.TagAnnotationI;
import omero.sys.EventContext;

import org.testng.annotations.Test;

/**
 * Tests for deleting user ratings.
 *
 * @see ticket:2997
 * @see ticket:2994
 * @since 4.2.1
 */
@Test(groups = { "delete", "integration", "ticket:2615" })
public class AnnotationDeleteTest extends AbstractServerTest {

	/** Reference to the <code>Rating</code> name space. */
    public final static RString RATING = rstring(
    		omero.constants.metadata.NSINSIGHTRATING.value);

    /** Reference to the <code>Experiment</code> name space. */
    public final static RString EXPERIMENT = rstring(
    		omero.constants.metadata.NSEDITOREXPERIMENT.value);

    /** Reference to the <code>Protocol</code> name space. */
    public final static RString PROTOCOL = rstring(
    		omero.constants.metadata.NSEDITORPROTOCOL.value);
    
    /**
     * Tests that the object, an annotation, and the link are all deleted.
     * 
     * @param obj The Object to annotate.
     * @param command The command indicating the object to delete.
     * @param id The identifier of the object to delete.
     */
    private void annotateSaveDeleteAndCheck(IObject obj, String command, 
    		RLong id) throws Exception {
        annotateSaveDeleteAndCheck(obj, command, id, true);
    }

    /**
     * Tests that the object, the annotation link, and optionally the annotation
     * are all deleted.
     * 
     * @param obj The Object to annotate.
     * @param command The command indicating the object to delete.
     * @param id The identifier of the object to delete.
     * @param annIsDeleted Pass <code>true</code> if the annotation is deleted,
     *                     <code>false</code> otherwise.
     */
    private void annotateSaveDeleteAndCheck(IObject obj, String command, 
    		RLong id, boolean annIsDeleted) throws Exception {
        Annotation ann = (Annotation) iUpdate
                .saveAndReturnObject(new TagAnnotationI());
        IObject link = mmFactory.createAnnotationLink(obj.proxy(), ann);
        link = iUpdate.saveAndReturnObject(link);
        delete(client, new Delete(command, id.getValue(), null));
        assertDoesNotExist(obj);
        assertDoesNotExist(link);
        if (annIsDeleted) {
            assertDoesNotExist(ann);
        } else {
            assertExists(ann);
        }
    }
    
    /**
     * Test to ensure that a user cannot force a delete.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:2959")
    public void testForceCannotBeSetByUser() throws Exception {
        EventContext owner = newUserAndGroup("rwrw--");
        FileAnnotation fa = new FileAnnotationI();
        fa.setNs(EXPERIMENT);
        fa.setFile(mmFactory.createOriginalFile());
        fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        OriginalFile file = fa.getFile();
        disconnect();

        newUserInGroup(owner);
        Map<String, String> options = new HashMap<String, String>();
        options.put(DeleteServiceTest.REF_ANN, DeleteServiceTest.FORCE); 
        delete(false, client, new Delete(
                DeleteServiceTest.REF_ANN, fa.getId().getValue(), options));

        assertExists(fa);
        assertExists(file);
    }

    /**
     * Test to ensure that an administrator can force a delete.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = "ticket:2959")
    public void testForceCanBeSetByAdmin() throws Exception {
        EventContext owner = newUserAndGroup("rwrw--");
        FileAnnotation fa = new FileAnnotationI();
        fa.setNs(EXPERIMENT);
        fa.setFile(mmFactory.createOriginalFile());
        fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
        OriginalFile file = fa.getFile();
        disconnect();

        logRootIntoGroup(owner);
        Map<String, String> options = new HashMap<String, String>();
     // Would delete other users' data
        options.put(DeleteServiceTest.REF_ANN, DeleteServiceTest.FORCE); 
        delete(true, client, new Delete(
                DeleteServiceTest.REF_ANN, fa.getId().getValue(), options));

        assertDoesNotExist(fa);
        assertDoesNotExist(file);
    }

    /**
     * Test to delete the file annotation of a given namespace.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "ticket:2994" })
    public void testDeleteFileAnnotationOfGivenNamespace() 
    	throws Exception {

        newUserAndGroup("rw----");
        List<RString> ns = new ArrayList<RString>();
        ns.add(EXPERIMENT);
        ns.add(PROTOCOL);
        FileAnnotation fa;
        OriginalFile file;
        Iterator<RString> i = ns.iterator();
        while (i.hasNext()) {
			fa = new FileAnnotationI();
	        fa.setNs(i.next());
	        fa.setFile(mmFactory.createOriginalFile());
	        fa = (FileAnnotation) iUpdate.saveAndReturnObject(fa);
	        file = fa.getFile();

	        delete(client, new Delete(DeleteServiceTest.REF_ANN, 
	        		fa.getId().getValue(), null));

	        assertDoesNotExist(fa);
	        assertDoesNotExist(file);
		}
    }

    /**
     * Test to make sure that the ratings linked to an image are deleted
     * when the image is deleted even if the ratings where made by others.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = false, groups = { "ticket:2997" })
    public void testOtherUsersRatingsIsDeleted() throws Exception {

        EventContext owner = newUserAndGroup("rwrw--");
        Image i1 = (Image) iUpdate.saveAndReturnObject(mmFactory.createImage());
        disconnect();

        newUserInGroup(owner);
        LongAnnotation rating = new LongAnnotationI();
        rating.setNs(RATING);
        rating.setLongValue(rlong(1L));
        ImageAnnotationLink link = new ImageAnnotationLinkI();
        link.link((Image) i1.proxy(), rating);
        link = (ImageAnnotationLink) iUpdate.saveAndReturnObject(link);
        rating = (LongAnnotation) link.getChild();
        disconnect();

        loginUser(owner);
        delete(client, new Delete(DeleteServiceTest.REF_IMAGE, i1
                .getId().getValue(), null));

        assertDoesNotExist(i1);
        assertDoesNotExist(link);
        assertDoesNotExist(rating);
        disconnect();
    }

    @Test(enabled = false, groups = { "ticket:2997" })
    public void testOtherUsersRatingsIsNotDeletedIfReused() throws Exception {
        fail("NYI");
    }

    //
    // Tests for the less common annotated types
    //

    /**
     * Test to make sure that the annotations linked to an annotation are 
     * deleted when the annotation is deleted.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(enabled = false, groups = { "ticket:3002", "ticket:3015" })
    public void testAnnotationsRemovedFromAnnotation() throws Exception {
        newUserAndGroup("rw----");
        Annotation ann = (Annotation) iUpdate
                .saveAndReturnObject(new TagAnnotationI());
        annotateSaveDeleteAndCheck(ann, DeleteServiceTest.REF_ANN, ann.getId(),
                false);
    }

    /**
     * Test to make sure that the annotations linked to a channel are deleted
     * when the channel is deleted.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "ticket:3002" })
    public void testAnnotationsRemovedFromChannel() throws Exception {
        newUserAndGroup("rw----");
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        Channel ch = image.getPrimaryPixels().getChannel(0);
        annotateSaveDeleteAndCheck(ch, DeleteServiceTest.REF_IMAGE,
                image.getId());
    }

    /**
     * Test to make sure that the annotations linked to an original file are 
     * deleted when the original file is deleted.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "ticket:3002" })
    public void testAnnotationsRemovedFromOriginalFile() throws Exception {
        newUserAndGroup("rw----");
        OriginalFile file = (OriginalFile) iUpdate
                .saveAndReturnObject(mmFactory.createOriginalFile());
        annotateSaveDeleteAndCheck(file, DeleteServiceTest.REF_ORIGINAL_FILE,
                file.getId());
    }

    /**
     * Test to make sure that the annotations linked to a pixels set are deleted
     * when the pixels set is deleted.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "ticket:3002" })
    public void testAnnotationsRemovedFromPixels() throws Exception {
        newUserAndGroup("rw----");
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        Pixels pixels = image.getPrimaryPixels();
        annotateSaveDeleteAndCheck(pixels, DeleteServiceTest.REF_IMAGE,
                image.getId());
    }

    /**
     * Test to make sure that the annotations linked to a plane info are deleted
     * when the plane info is deleted.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "ticket:3002" })
    public void testAnnotationsRemovedFromPlaneInfo() throws Exception {
        newUserAndGroup("rw----");
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImage());
        PlaneInfo info = image.getPixels(0).copyPlaneInfo().get(0);
        annotateSaveDeleteAndCheck(info, DeleteServiceTest.REF_IMAGE,
                image.getId());
    }

    /**
     * Test to make sure that the annotations linked to an ROI are deleted
     * when the ROI is deleted.
     * @throws Exception Thrown if an error occurred.
     */
    @Test(groups = { "ticket:3002" })
    public void testAnnotationsRemovedFromRoi() throws Exception {
        newUserAndGroup("rw----");
        Image image = (Image) iUpdate.saveAndReturnObject(mmFactory
                .createImageWithRoi());
        Roi roi = image.copyRois().get(0);
        annotateSaveDeleteAndCheck(roi, DeleteServiceTest.REF_ROI, roi.getId());
    }
    
}
